package plugins.fmp.multispots.series;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;

import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.roi.BooleanMask2D;
import icy.sequence.Sequence;
import icy.system.SystemUtil;
import icy.system.thread.Processor;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;


import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.experiment.SequenceCamData;
import plugins.fmp.multispots.experiment.Spot;
import plugins.fmp.multispots.tools.ImageTransform.ImageTransformInterface;
import plugins.fmp.multispots.tools.ImageTransform.ImageTransformOptions;



public class DetectArea extends BuildSeries  
{
	public Sequence seqData = new Sequence();
	private Viewer vData = null;
	ArrayList<IcyBufferedImage>	cap_bufKymoImage = null;
	int imageWidth = 0;
	
	// --------------------------------------------
	
	void analyzeExperiment(Experiment exp) 
	{
		loadExperimentDataToMeasureAreas(exp);
		
		openKymoViewers(exp);
		getTimeLimitsOfSequence(exp);
		if (measureAreas(exp)) 
			saveComputation(exp);

		closeKymoViewers();
		exp.seqKymos.closeSequence();
	}
	
	private boolean loadExperimentDataToMeasureAreas(Experiment exp) 
	{
		boolean flag = exp.loadMCSpots_Only();
		exp.seqCamData.seq = exp.seqCamData.initSequenceFromFirstImage(exp.seqCamData.getImagesList(true));
		return flag;
	}
	
	private void getTimeLimitsOfSequence(Experiment exp)
	{
		exp.getFileIntervalsFromSeqCamData();
		exp.binDuration_ms = options.binDuration_ms;
		if (options.isFrameFixed) {
			exp.binFirst_ms = options.t_Ms_First;
			exp.binLast_ms = options.t_Ms_Last;
			if (exp.binLast_ms + exp.camImageFirst_ms > exp.camImageLast_ms)
				exp.binLast_ms = exp.camImageLast_ms - exp.camImageFirst_ms;
		} 
		else {
			exp.binFirst_ms = 0;
			exp.binLast_ms = exp.camImageLast_ms - exp.camImageFirst_ms;
		}
	}
			
	private void saveComputation(Experiment exp) 
	{	
		if (options.doCreateBinDir) 
			exp.setBinSubDirectory (exp.getBinNameFromKymoFrameStep());
		String directory = exp.getDirectoryToSaveResults(); 
		if (directory == null)
			return;
		
//		ProgressFrame progressBar = new ProgressFrame("Save measures");
//		int nframes = exp.seqCamData.seq.getSizeT();
//		int nCPUs = SystemUtil.getNumberOfCPUs();
//	    final Processor processor = new Processor(nCPUs);
//	    processor.setThreadName("buildAreaResults");
//	    processor.setPriority(Processor.NORM_PRIORITY);
//        ArrayList<Future<?>> futuresArray = new ArrayList<Future<?>>(nframes);
//		futuresArray.clear();
//		
//		for (int t = 0; t < exp.seqKymos.seq.getSizeT(); t++) {
//			final int t_index = t;
//			futuresArray.add(processor.submit(new Runnable () {
//				@Override
//				public void run() {	
//					Spot cap = exp.spotsArray.spotsList.get(t_index);
//					String filename = directory + File.separator + cap.getKymographName() + ".tiff";
//					File file = new File (filename);
//					IcyBufferedImage image = exp.seqKymos.getSeqImage(t_index, 0);
//					try {
//						Saver.saveImage(image, file, true);
//					} 
//					catch (FormatException e) {
//						e.printStackTrace();
//					} 
//					catch (IOException e) {
//						e.printStackTrace();
//					}
//				}}));
//		}
//		waitFuturesCompletion(processor, futuresArray, progressBar);
//		progressBar.close();
		exp.saveMCExperiment();
	}
	
	private void getReferenceImage (Experiment exp, int t, ImageTransformOptions options) 
	{
		switch (options.transformOption) 
		{
			case SUBTRACT_TM1: 
				options.backgroundImage = imageIORead(exp.seqCamData.getFileNameFromImageList(t));
				break;
				
			case SUBTRACT_T0:
			case SUBTRACT_REF:
				if (options.backgroundImage == null)
					options.backgroundImage = imageIORead(exp.seqCamData.getFileNameFromImageList(0));
				break;
				
			case NONE:
			default:
				break;
		}
	}
	
	private boolean measureAreas (Experiment exp) 
	{
		if (exp.spotsArray.spotsList.size() < 1) {
			System.out.println("DetectAreas:measureAreas Abort (1): nbspots = 0");
			return false;
		}
		
		threadRunning = true;
		stopFlag = false;
		
//		final int nColumns = (int) ((exp.binLast_ms - exp.binFirst_ms) / exp.binDuration_ms +1);
		exp.build_MsTimeIntervalsArray_From_SeqCamData_FileNamesList();
//		int sourceImageIndex = exp.findNearestIntervalWithBinarySearch(exp.binFirst_ms, 0, exp.seqCamData.nTotalFrames);
//		String vDataTitle = new String(" / " + nColumns);
//		ProgressFrame progressBar = new ProgressFrame("Analyze stack frame ");

		final Processor processor = new Processor(SystemUtil.getNumberOfCPUs());
	    processor.setThreadName("buildKymograph");
	    processor.setPriority(Processor.NORM_PRIORITY);
	    int ntasks =  exp.capillaries.capillariesList.size(); //
	    ArrayList<Future<?>> tasks = new ArrayList<Future<?>>( ntasks);
	    tasks.clear();
	    
	    int nFrames = exp.seqCamData.nTotalFrames;
	    initMasks2DToMeasureAreas(exp);
		initSpotsDataArrays(exp);
		ImageTransformOptions transformOptions = new ImageTransformOptions();
		transformOptions.transformOption = options.transformop;
		getReferenceImage (exp, 0, transformOptions);
		ImageTransformInterface transformFunction = options.transformop.getFunction();
		
		for (int ii = 0; ii < nFrames; ii++) 
		{
			final int fromSourceImageIndex = ii;
			
//			String title = "Frame #"+ fromSourceImageIndex + " /" + exp.seqCamData.nTotalFrames;
//			System.out.println(title);
//			progressBar.setMessage(title);
			
			IcyBufferedImage sourceImage = imageIORead(exp.seqCamData.getFileNameFromImageList(ii));
			final IcyBufferedImage workImage = transformFunction.getTransformedImage(sourceImage, transformOptions); 
//			if (workImage == null)
//				next;
			
			tasks.add(processor.submit(new Runnable () {
				@Override
				public void run() {	

					boolean[] boolMap = getBoolMap_FromBinaryInt(workImage);
					BooleanMask2D maskAll2D = new BooleanMask2D(workImage.getBounds(), boolMap); 
				
					for (Spot spot: exp.spotsArray.spotsList) 
					{
						int sum = 0;
						BooleanMask2D intersectionMask = null;
						try {
							intersectionMask = maskAll2D.getIntersection(spot.spotMask2D );
							sum = intersectionMask.getNumberOfPoints();
							spot.areaNPixels.limit[fromSourceImageIndex] = sum;		
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}}));
//			vData.setTitle("Analyzing frame: " + (fromSourceImageIndex +1)+ vDataTitle);
//			seqData.setImage(0, 0, sourceImage); // add option??
//			progressBar.setMessage("Analyze frame: " + fromSourceImageIndex + "//" + nColumns);	
		}

		waitFuturesCompletion(processor, tasks, null);
//		progressBar.close();
	       
		return true;
	}
	
	public boolean[] getBoolMap_FromBinaryInt(IcyBufferedImage img) 
	{
		boolean[] boolMap = new boolean[ img.getSizeX() * img.getSizeY() ];
		byte [] imageSourceDataBuffer = null;
		DataType datatype = img.getDataType_();
		
		if (datatype != DataType.BYTE && datatype != DataType.UBYTE) {
			Object sourceArray = img.getDataXY(0);
			imageSourceDataBuffer = Array1DUtil.arrayToByteArray(sourceArray);
		}
		else
			imageSourceDataBuffer = img.getDataXYAsByte(0);
		
		for (int x = 0; x < boolMap.length; x++)  {
			if (imageSourceDataBuffer[x] == 0)
				boolMap[x] =  false;
			else
				boolMap[x] =  true;
		}
		return boolMap;
	}
	
	private void initSpotsDataArrays(Experiment exp)
	{
		//int n_measures = (int) ((exp.binLast_ms - exp.binFirst_ms) / exp.binDuration_ms + 1);
		int nFrames = exp.seqCamData.nTotalFrames;
		for (Spot spot: exp.spotsArray.spotsList) 
		{
			spot.areaNPixels.limit = new int [nFrames+1];
		}

	}
	
	private void initMasks2DToMeasureAreas(Experiment exp) 
	{
		SequenceCamData seqCamData = exp.seqCamData;
		if (seqCamData.seq == null) 
			seqCamData.seq = exp.seqCamData.initSequenceFromFirstImage(exp.seqCamData.getImagesList(true));

		imageWidth = (int) ((exp.binLast_ms - exp.binFirst_ms) / exp.binDuration_ms +1);
		for (Spot spot: exp.spotsArray.spotsList) 
		{
			try {
				spot.spotMask2D = spot.getRoi().getBooleanMask2D( 0 , 0, 1, true );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	


	private void closeKymoViewers() 
	{
		closeViewer(vData);
		closeSequence(seqData);
	}
	
	private void openKymoViewers(Experiment exp) 
	{
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() 
				{			
					seqData = newSequence("analyze stack starting with file " + exp.seqCamData.seq.getName(), exp.seqCamData.getSeqImage(0, 0));
					vData = new Viewer(seqData, true);
				}});
		} 
		catch (InvocationTargetException | InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
}