package plugins.fmp.multiSPOTS.series;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;

import icy.file.Saver;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageCursor;
import icy.image.IcyBufferedImageUtil;
import icy.sequence.Sequence;
import icy.system.SystemUtil;
import icy.system.thread.Processor;
import icy.type.DataType;
import loci.formats.FormatException;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.ROI2DAlongT;
import plugins.fmp.multiSPOTS.experiment.SequenceCamData;
import plugins.fmp.multiSPOTS.experiment.SequenceKymos;
import plugins.fmp.multiSPOTS.experiment.Spot;
import plugins.fmp.multiSPOTS.tools.GaspardRigidRegistration;

public class BuildSpotsKymos extends BuildSeries {
	public Sequence seqData = new Sequence();
	private Viewer vData = null;
	private int kymoImageWidth = 0;

	// -----------------------------------

	void analyzeExperiment(Experiment exp) {
		if (!loadExperimentDataToBuildKymos(exp) || exp.spotsArray.spotsList.size() < 1)
			return;
		openKymoViewers(exp);
		getTimeLimitsOfSeqCamData(exp);
		if (buildKymo(exp))
			saveComputation(exp);

		closeKymoViewers();
		exp.seqSpotKymos.closeSequence();
	}

	private boolean loadExperimentDataToBuildKymos(Experiment exp) {
		boolean flag = exp.loadMCSpots_Only();
		exp.seqCamData.seq = exp.seqCamData.initSequenceFromFirstImage(exp.seqCamData.getImagesList(true));
		return flag;
	}

	private void getTimeLimitsOfSeqCamData(Experiment exp) {
		exp.getFileIntervalsFromSeqCamData();
		exp.seqCamData.binDuration_ms = options.t_Ms_BinDuration;
		if (options.isFrameFixed) {
			exp.seqCamData.binFirst_ms = options.t_Ms_First;
			exp.seqCamData.binLast_ms = options.t_Ms_Last;
			if (exp.seqCamData.binLast_ms + exp.seqCamData.firstImage_ms > exp.seqCamData.lastImage_ms)
				exp.seqCamData.binLast_ms = exp.seqCamData.lastImage_ms - exp.seqCamData.firstImage_ms;
		} else {
			exp.seqCamData.binFirst_ms = 0;
			exp.seqCamData.binLast_ms = exp.seqCamData.lastImage_ms - exp.seqCamData.firstImage_ms;
		}
	}

	private void saveComputation(Experiment exp) {
		if (options.doCreateBinDir)
			exp.setBinSubDirectory(exp.getBinNameFromKymoFrameStep());
		String directory = exp.getDirectoryToSaveResults();
		if (directory == null)
			return;

		ProgressFrame progressBar = new ProgressFrame("Save kymographs");

		int nframes = exp.seqSpotKymos.seq.getSizeT();
		int nCPUs = SystemUtil.getNumberOfCPUs();
		final Processor processor = new Processor(nCPUs);
		processor.setThreadName("buildkymo2");
		processor.setPriority(Processor.NORM_PRIORITY);
		ArrayList<Future<?>> futuresArray = new ArrayList<Future<?>>(nframes);
		futuresArray.clear();

		for (int t = 0; t < exp.seqSpotKymos.seq.getSizeT(); t++) {
			final int t_index = t;
			futuresArray.add(processor.submit(new Runnable() {
				@Override
				public void run() {
					Spot spot = exp.spotsArray.spotsList.get(t_index);
					String filename = directory + File.separator + spot.getRoiName() + ".tiff";
					File file = new File(filename);
					IcyBufferedImage image = exp.seqSpotKymos.getSeqImage(t_index, 0);
					try {
						Saver.saveImage(image, file, true);
					} catch (FormatException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}));
		}
		waitFuturesCompletion(processor, futuresArray, progressBar);
		progressBar.close();
		exp.saveXML_MCExperiment();
	}

	private boolean buildKymo(Experiment exp) {
		if (exp.spotsArray.spotsList.size() < 1) {
			System.out.println("BuildKymoSpots:buildKymo Abort (1): nb spots = 0");
			return false;
		}

		initArraysToBuildKymographImages(exp);

		threadRunning = true;
		stopFlag = false;

		final int iiFirst = (int) exp.seqSpotKymos.indexFirstImage;
		final int iiLast = (int) exp.seqSpotKymos.indexLastImage;
		final int iiDelta = (int) exp.seqSpotKymos.deltaImage;
		ProgressFrame progressBar1 = new ProgressFrame("Analyze stack frame ");

		final Processor processor = new Processor(SystemUtil.getNumberOfCPUs());
		processor.setThreadName("buildKymograph");
		processor.setPriority(Processor.NORM_PRIORITY);
		int ntasks = exp.spotsArray.spotsList.size(); //
		ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(ntasks);
		tasks.clear();

		vData.setTitle(exp.seqCamData.getCSCamFileName());

		for (int ii = iiFirst; ii < iiLast; ii += iiDelta) {
			final int t = ii;

			if (options.concurrentDisplay) {
				IcyBufferedImage sourceImage0 = imageIORead(exp.seqCamData.getFileNameFromImageList(t));
				seqData.setImage(0, 0, sourceImage0);
				vData.setTitle("Frame #" + ii + " /" + iiLast);
			}

			tasks.add(processor.submit(new Runnable() {
				@Override
				public void run() {
					progressBar1.setMessage("Analyze frame: " + t + "//" + iiLast);
					IcyBufferedImage sourceImage = loadImageFromIndex(exp, t);
					int sizeC = sourceImage.getSizeC();
					IcyBufferedImageCursor cursorSource = new IcyBufferedImageCursor(sourceImage);
					for (Spot spot : exp.spotsArray.spotsList) {
						analyzeImageWithSpot(cursorSource, spot, t - iiFirst, sizeC);
					}
				}
			}));
		}
		waitFuturesCompletion(processor, tasks, null);
		progressBar1.close();

		ProgressFrame progressBar2 = new ProgressFrame("Combine results into kymograph");
		int sizeC = seqData.getSizeC();
		exportSpotImages_to_Kymograph(exp, sizeC);
		progressBar2.close();

		return true;
	}

	private void analyzeImageWithSpot(IcyBufferedImageCursor cursorSource, Spot spot, int t, int sizeC) {
		ROI2DAlongT roiT = spot.getROIAtT(t);
		for (int chan = 0; chan < sizeC; chan++) {
			IcyBufferedImageCursor cursor = new IcyBufferedImageCursor(spot.spot_Image);
			try {
				for (int y = 0; y < roiT.mask2DPoints_in.length; y++) {
					Point pt = roiT.mask2DPoints_in[y];
					cursor.set(t, y, chan, cursorSource.get((int) pt.getX(), (int) pt.getY(), chan));
				}
			} finally {
				cursor.commitChanges();
			}
		}
	}

	private IcyBufferedImage loadImageFromIndex(Experiment exp, int frameIndex) {
		IcyBufferedImage sourceImage = imageIORead(exp.seqCamData.getFileNameFromImageList(frameIndex));
		if (options.doRegistration) {
			String referenceImageName = exp.seqCamData.getFileNameFromImageList(options.referenceFrame);
			IcyBufferedImage referenceImage = imageIORead(referenceImageName);
			adjustImage(sourceImage, referenceImage);
		}
		return sourceImage;
	}

	private void exportSpotImages_to_Kymograph(Experiment exp, final int sizeC) {
		Sequence seqKymo = exp.seqSpotKymos.seq;
		seqKymo.beginUpdate();
		final Processor processor = new Processor(SystemUtil.getNumberOfCPUs());
		processor.setThreadName("buildKymograph");
		processor.setPriority(Processor.NORM_PRIORITY);
		int nbspots = exp.spotsArray.spotsList.size();
		ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(nbspots);
		tasks.clear();
		int vertical_resolution = 512;

		for (int ispot = 0; ispot < nbspots; ispot++) {
			final Spot spot = exp.spotsArray.spotsList.get(ispot);
			final int indexSpot = ispot;
			tasks.add(processor.submit(new Runnable() {
				@Override
				public void run() {
					IcyBufferedImage kymoImage = IcyBufferedImageUtil.scale(spot.spot_Image, spot.spot_Image.getWidth(),
							vertical_resolution);
					seqKymo.setImage(indexSpot, 0, kymoImage);
					spot.spot_Image = null;
				}
			}));
		}

		waitFuturesCompletion(processor, tasks, null);
		seqKymo.endUpdate();
	}

	private void initArraysToBuildKymographImages(Experiment exp) {
		if (exp.seqSpotKymos == null)
			exp.seqSpotKymos = new SequenceKymos();
		SequenceKymos seqKymos = exp.seqSpotKymos;
		seqKymos.seq = new Sequence();

		SequenceCamData seqCamData = exp.seqCamData;
		if (seqCamData.seq == null)
			seqCamData.seq = exp.seqCamData.initSequenceFromFirstImage(exp.seqCamData.getImagesList(true));

//		kymoImageWidth = (int) ((exp.binLast_ms - exp.binFirst_ms) / exp.binDuration_ms +1);
		kymoImageWidth = (int) (exp.seqCamData.nTotalFrames - exp.seqSpotKymos.indexFirstImage);
		int numC = seqCamData.seq.getSizeC();
		if (numC <= 0)
			numC = 3;

		DataType dataType = seqCamData.seq.getDataType_();
		if (dataType.toString().equals("undefined"))
			dataType = DataType.UBYTE;

		for (Spot spot : exp.spotsArray.spotsList) {
			int imageHeight = 0;
//			double scale = 2.;
			for (ROI2DAlongT roiT : spot.getROIAlongTList()) {
//				roiT.buildRoi_outAndMask2D(scale);
				roiT.buildMask2DFromRoi_in();

				// TODO transform into ROIT and add to outer
				// subtract booleanmap from booleantmap of roiT

				int imageHeight_i = roiT.mask2DPoints_in.length;
				if (imageHeight_i > imageHeight)
					imageHeight = imageHeight_i;
			}
			spot.spot_Image = new IcyBufferedImage(kymoImageWidth, imageHeight, numC, dataType);
		}
	}

	private void adjustImage(IcyBufferedImage workImage, IcyBufferedImage referenceImage) {
		int referenceChannel = 0;
		GaspardRigidRegistration.correctTranslation2D(workImage, referenceImage, referenceChannel);
		boolean rotate = GaspardRigidRegistration.correctRotation2D(workImage, referenceImage, referenceChannel);
		if (rotate)
			GaspardRigidRegistration.correctTranslation2D(workImage, referenceImage, referenceChannel);
	}

	private void closeKymoViewers() {
		closeViewer(vData);
		closeSequence(seqData);
	}

	private void openKymoViewers(Experiment exp) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					seqData = newSequence("analyze stack starting with file " + exp.seqCamData.seq.getName(),
							exp.seqCamData.getSeqImage(0, 0));
					vData = new Viewer(seqData, true);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
