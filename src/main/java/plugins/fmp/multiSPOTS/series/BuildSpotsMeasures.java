package plugins.fmp.multiSPOTS.series;

import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;

import icy.gui.frame.progress.ProgressFrame;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageCursor;
import icy.roi.BooleanMask2D;
import icy.sequence.Sequence;
import icy.system.SystemUtil;
import icy.system.thread.Processor;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.ROI2DAlongT;
import plugins.fmp.multiSPOTS.experiment.SequenceCamData;
import plugins.fmp.multiSPOTS.experiment.Spot;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformInterface;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformOptions;

public class BuildSpotsMeasures extends BuildSeries {
	public Sequence seqData = new Sequence();
	private Viewer vData = null;

	// --------------------------------------------

	void analyzeExperiment(Experiment exp) {
		loadExperimentDataToMeasureSpots(exp);

		openViewers(exp);
		getTimeLimitsOfSequence(exp);
		if (measureSpots(exp))
			saveComputation(exp);

		closeViewers();
	}

	private boolean loadExperimentDataToMeasureSpots(Experiment exp) {
		boolean flag = exp.loadMCSpots_Only();
		exp.seqCamData.seq = exp.seqCamData.initSequenceFromFirstImage(exp.seqCamData.getImagesList(true));
		return flag;
	}

	private void getTimeLimitsOfSequence(Experiment exp) {
		exp.getFileIntervalsFromSeqCamData();
		exp.loadFileIntervalsFromSeqCamData();
		exp.binDuration_ms = exp.camImageBin_ms;
		System.out.println("sequence bin size = " + exp.binDuration_ms);
		if (options.isFrameFixed) {
			exp.binFirst_ms = options.t_Ms_First;
			exp.binLast_ms = options.t_Ms_Last;
			if (exp.binLast_ms + exp.camImageFirst_ms > exp.camImageLast_ms)
				exp.binLast_ms = exp.camImageLast_ms - exp.camImageFirst_ms;
		} else {
			exp.binFirst_ms = 0;
			exp.binLast_ms = exp.camImageLast_ms - exp.camImageFirst_ms;
		}
	}

	private void saveComputation(Experiment exp) {
		if (options.doCreateBinDir)
			exp.setBinSubDirectory(exp.getBinNameFromKymoFrameStep());
		String directory = exp.getDirectoryToSaveResults();
		if (directory == null)
			return;

		exp.spotsArray.transferSumToSumClean();
		exp.spotsArray.initLevel2DMeasures();
		exp.saveXML_MCExperiment();
		exp.save_SpotsMeasures();
	}

	private boolean measureSpots(Experiment exp) {
		if (exp.spotsArray.spotsList.size() < 1) {
			System.out.println("DetectAreas:measureAreas Abort (1): nbspots = 0");
			return false;
		}

		threadRunning = true;
		stopFlag = false;
		exp.build_MsTimeIntervalsArray_From_SeqCamData_FileNamesList();

		final Processor processor = new Processor(SystemUtil.getNumberOfCPUs());
		processor.setThreadName("buildSpots");
		processor.setPriority(Processor.NORM_PRIORITY);
		int ntasks = exp.spotsArray.spotsList.size(); //
		ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(ntasks);
		tasks.clear();

		final int tFirst = (int) exp.binT0;
		int tLast = exp.seqCamData.nTotalFrames - 1;
		vData.setTitle(exp.seqCamData.getCSCamFileName() + ": " + tFirst + "-" + tLast);

		ProgressFrame progressBar1 = new ProgressFrame("Analyze stack");

		initMasks2D(exp);
		initSpotsDataArrays(exp);
		ImageTransformOptions transformOptions = new ImageTransformOptions();
		transformOptions.transformOption = options.transform01;
		transformOptions.setSingleThreshold(options.spotThreshold, options.spotThresholdUp);
		ImageTransformInterface transformFunction = options.transform01.getFunction();

		for (int ii = tFirst; ii <= tLast; ii++) {
			if (options.concurrentDisplay) {
				IcyBufferedImage sourceImage0 = imageIORead(exp.seqCamData.getFileNameFromImageList(ii));
				seqData.setImage(0, 0, sourceImage0);
				vData.setTitle("Frame #" + ii + " /" + tLast);
			}

			final int t = ii;
			tasks.add(processor.submit(new Runnable() {
				@Override
				public void run() {
					progressBar1.setMessage("Analyze frame: " + t + "//" + tLast);
					final IcyBufferedImage sourceImage = imageIORead(exp.seqCamData.getFileNameFromImageList(t));
					final IcyBufferedImage workImage = transformFunction.getTransformedImage(sourceImage,
							transformOptions);
					IcyBufferedImageCursor cursorSource = new IcyBufferedImageCursor(sourceImage);
					IcyBufferedImageCursor cursorWork = new IcyBufferedImageCursor(workImage);
					int ii = t - tFirst;
					ResultsThreshold results = new ResultsThreshold();
					for (Spot spot : exp.spotsArray.spotsList) {
						ROI2DAlongT roiT = spot.getROIAtT(t);
						spot.flyPresent.isPresent[ii] = isFlyPresentInSpotAreaAtT(cursorSource, roiT) > 0;
						results = measureSpotOverThresholdAtT(cursorWork, roiT.mask2DPoints_in);
						spot.sum_in.values[ii] = results.sum / results.npoints_in;

//						spot.sum_out.values[ii] = measureMedianBackgroundAroundSpotAtT(cursorWork,
//						roiT.mask2DPoints_out);
//						spot.sum_out.values[ii] = measureAverageBackgroundAroundSpotAtT(cursorWork,
//						roiT.mask2DPoints_out);//						spot.sum_diff.measureValues[ii] = spot.sum_in.measureValues[ii]
//								- spot.sum_out.measureValues[ii] * results.nPointsOverThreshold /results.npoints_in;
//						spot.sum_diff.values[ii] = results.sumTot / results.npoints_in - spot.sum_out.values[ii];
					}
				}
			}));
		}
		waitFuturesCompletion(processor, tasks, null);
		progressBar1.close();
		return true;
	}

	private int isFlyPresentInSpotAreaAtT(IcyBufferedImageCursor cursorSource, ROI2DAlongT roiT) {
		int flyThreshold = options.flyThreshold;
		int flyFound = 0;
		Point[] mask2DPoints_in = roiT.mask2DPoints_in;
		roiT.mask2D_in_nofly = (BooleanMask2D) roiT.mask2D_in.clone();

		if (options.flyThresholdUp) {
			for (int offset = 0; offset < mask2DPoints_in.length; offset++) {
				Point pt = mask2DPoints_in[offset];
				int value = (int) cursorSource.get((int) pt.getX(), (int) pt.getY(), 0);
				if (value > flyThreshold) {
					flyFound++;
					roiT.mask2D_in_nofly.mask[offset] = false;
//					break;
				}
			}
		} else {
			for (int offset = 0; offset < mask2DPoints_in.length; offset++) {
				Point pt = mask2DPoints_in[offset];
				int value = (int) cursorSource.get((int) pt.getX(), (int) pt.getY(), 0);
				if (value < flyThreshold) {
					flyFound++;
					roiT.mask2D_in_nofly.mask[offset] = false;
//					break;
				}
			}
		}
		return flyFound;
	}

	private ResultsThreshold measureSpotOverThresholdAtT(IcyBufferedImageCursor cursorWorkImage, Point[] mask2DPoints) {
		boolean spotThresholdUp = options.spotThresholdUp;
		int spotThreshold = options.spotThreshold;

		double sum = 0;
		double sumTot = 0.;
		int nPointsOverThreshold = 0;
		if (spotThresholdUp) {
			for (int offset = 0; offset < mask2DPoints.length; offset++) {
				Point pt = mask2DPoints[offset];
				int value = (int) cursorWorkImage.get((int) pt.getX(), (int) pt.getY(), 0);
				sumTot += value;
				if (value < spotThreshold) {
					sum += value;
					nPointsOverThreshold++;
				}
			}
		} else {
			for (int offset = 0; offset < mask2DPoints.length; offset++) {
				Point pt = mask2DPoints[offset];
				int value = (int) cursorWorkImage.get((int) pt.getX(), (int) pt.getY(), 0);
				sumTot += value;
				if (value > spotThreshold) {
					sum += value;
					nPointsOverThreshold++;
				}
			}
		}
		return new ResultsThreshold(sum, sumTot, nPointsOverThreshold, (double) mask2DPoints.length);
	}

//	private double measureMedianBackgroundAroundSpotAtT(IcyBufferedImageCursor cursorWorkImage, Point[] mask2DPoints) {
//		double[] values = new double[mask2DPoints.length];
//		for (int offset = 0; offset < mask2DPoints.length; offset++) {
//			Point pt = mask2DPoints[offset];
//			values[offset] = cursorWorkImage.get((int) pt.getX(), (int) pt.getY(), 0);
//		}
//		return ArrayMath.median(values, false);
//	}

//	private double measureAverageBackgroundAroundSpotAtT(IcyBufferedImageCursor cursorWorkImage, Point[] mask2DPoints) {
//		double sum = 0;
//		for (int offset = 0; offset < mask2DPoints.length; offset++) {
//			Point pt = mask2DPoints[offset];
//			sum += cursorWorkImage.get((int) pt.getX(), (int) pt.getY(), 0);
//		}
//		return sum / mask2DPoints.length;
//	}

	private void initSpotsDataArrays(Experiment exp) {
		// int n_measures = (int) ((exp.binLast_ms - exp.binFirst_ms) /
		// exp.binDuration_ms + 1);
		int nFrames = exp.seqCamData.nTotalFrames - (int) exp.binT0;
		for (Spot spot : exp.spotsArray.spotsList) {
			spot.sum_in.values = new double[nFrames];
			spot.sum_clean.values = new double[nFrames];
//			spot.sum_out.values = new double[nFrames];
//			spot.sum_diff.values = new double[nFrames];
			spot.flyPresent.isPresent = new boolean[nFrames];
		}
	}

	private void initMasks2D(Experiment exp) {
		SequenceCamData seqCamData = exp.seqCamData;
		if (seqCamData.seq == null)
			seqCamData.seq = exp.seqCamData.initSequenceFromFirstImage(exp.seqCamData.getImagesList(true));

//		double scale = 2.;
		for (Spot spot : exp.spotsArray.spotsList) {
			List<ROI2DAlongT> listRoiT = spot.getROIAlongTList();
			for (ROI2DAlongT roiT : listRoiT) {
				if (roiT.getMask2D_in() == null)
					roiT.buildMask2DFromRoi_in();
//				roiT.buildRoi_outAndMask2D(scale);
			}
		}
	}

	private void closeViewers() {
		closeViewer(vData);
		closeSequence(seqData);
	}

	private void openViewers(Experiment exp) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					seqData = newSequence(exp.seqCamData.getCSCamFileName(), exp.seqCamData.getSeqImage(0, 0));
					vData = new Viewer(seqData, true);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}