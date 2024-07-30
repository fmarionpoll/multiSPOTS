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
	private ImageTransformOptions transformOptions01 = null;
	ImageTransformInterface transformFunctionSpot = null;
	ImageTransformOptions transformOptions02 = null;
	ImageTransformInterface transformFunctionFly = null;

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

		final int tFirst = (int) exp.frameFirst;
		final int tLast = exp.seqCamData.nTotalFrames;
		vData.setTitle(exp.seqCamData.getCSCamFileName() + ": " + tFirst + "-" + tLast);

		ProgressFrame progressBar1 = new ProgressFrame("Analyze stack");

		initMasks2D(exp);
		initSpotsDataArrays(exp);

		if (transformFunctionSpot == null) {
			transformOptions01 = new ImageTransformOptions();
			transformOptions01.transformOption = options.transform01;
			transformOptions01.copyResultsToThe3planes = false;
			transformOptions01.setSingleThreshold(options.spotThreshold, options.spotThresholdUp);
			transformFunctionSpot = options.transform01.getFunction();

			transformOptions02 = new ImageTransformOptions();
			transformOptions02.transformOption = options.transform02;
			transformOptions02.copyResultsToThe3planes = false;
			transformFunctionFly = options.transform02.getFunction();
		}

		for (int ti = tFirst; ti < tLast; ti++) {
			if (options.concurrentDisplay) {
				IcyBufferedImage sourceImage0 = imageIORead(exp.seqCamData.getFileNameFromImageList(ti));
				seqData.setImage(0, 0, sourceImage0);
				vData.setTitle("Frame #" + ti + " /" + tLast);
			}

			final int t = ti;
			tasks.add(processor.submit(new Runnable() {
				@Override
				public void run() {
					progressBar1.setMessage("Analyze frame: " + t + "//" + tLast);
					final IcyBufferedImage sourceImage = imageIORead(exp.seqCamData.getFileNameFromImageList(t));
					final IcyBufferedImage transformToMeasureArea = transformFunctionSpot.getTransformedImage(sourceImage,
							transformOptions01);
					final IcyBufferedImage transformToDetectFly = transformFunctionFly.getTransformedImage(sourceImage,
							transformOptions02);
					
					IcyBufferedImageCursor cursorToDetectFly = new IcyBufferedImageCursor(transformToDetectFly);
					IcyBufferedImageCursor cursorToMeasureArea = new IcyBufferedImageCursor(transformToMeasureArea);
					
					int ii = t - tFirst;
					for (Spot spot : exp.spotsArray.spotsList) {
						ROI2DAlongT roiT = spot.getROIAtT(t);
						ResultsThreshold results = measureSpotOverThreshold(cursorToMeasureArea, cursorToDetectFly, roiT);
						spot.flyPresent.isPresent[ii] = results.nPoints_fly_present;
						spot.sum_in.values[ii] = results.sumOverThreshold / results.npoints_in;
						if (results.nPoints_no_fly != results.npoints_in) 
							spot.sum_in.values[ii] = results.sumTot_no_fly_over_threshold / results.nPoints_no_fly;
					}
				}
			}));
		}
		waitFuturesCompletion(processor, tasks, null);
		progressBar1.close();
		return true;
	}

	private ResultsThreshold measureSpotOverThreshold(
			IcyBufferedImageCursor cursorToMeasureArea,
			IcyBufferedImageCursor cursorToDetectFly, 
			ROI2DAlongT roiT) {

		ResultsThreshold result = new ResultsThreshold();
		result.npoints_in = roiT.mask2DPoints_in.length;

		for (int offset = 0; offset < roiT.mask2DPoints_in.length; offset++) {
			Point pt = roiT.mask2DPoints_in[offset];
			int value = (int) cursorToMeasureArea.get((int) pt.getX(), (int) pt.getY(), 0);
			int value_to_detect_fly = (int) cursorToDetectFly.get((int) pt.getX(), (int) pt.getY(), 0);

			boolean isFlyThere = isFlyPresent(value_to_detect_fly);
			if (!isFlyThere) {
				result.nPoints_no_fly++;
			} else
				result.nPoints_fly_present++;

			if (isOverThreshold(value)) {
				result.sumOverThreshold += value;
				result.nPointsOverThreshold++;
				if (!isFlyThere) {
					result.sumTot_no_fly_over_threshold += value;
				}
			}
		}
		return result;
	}

	private boolean isFlyPresent(double value) {
		boolean flag = value > options.flyThreshold;
		if (!options.flyThresholdUp)
			flag = !flag;
		return flag;
	}

	private boolean isOverThreshold(double value) {

		boolean flag = value > options.spotThreshold;
		if (!options.spotThresholdUp)
			flag = !flag;
		return flag;
	}

	private void initSpotsDataArrays(Experiment exp) {
		// int n_measures = (int) ((exp.binLast_ms - exp.binFirst_ms) /
		// exp.binDuration_ms + 1);
		int nFrames = exp.seqCamData.nTotalFrames - (int) exp.frameFirst;
		for (Spot spot : exp.spotsArray.spotsList) {
			spot.sum_in.values = new double[nFrames];
			spot.sum_clean.values = new double[nFrames];
			spot.flyPresent.isPresent = new int[nFrames];
		}
	}

	private void initMasks2D(Experiment exp) {
		SequenceCamData seqCamData = exp.seqCamData;
		if (seqCamData.seq == null)
			seqCamData.seq = exp.seqCamData.initSequenceFromFirstImage(exp.seqCamData.getImagesList(true));

		for (Spot spot : exp.spotsArray.spotsList) {
			List<ROI2DAlongT> listRoiT = spot.getROIAlongTList();
			for (ROI2DAlongT roiT : listRoiT) {
				if (roiT.getMask2D_in() == null)
					roiT.buildMask2DFromRoi_in();
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