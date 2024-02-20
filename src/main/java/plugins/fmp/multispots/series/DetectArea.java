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
	
	// --------------------------------------------
	
	void analyzeExperiment(Experiment exp) 
	{
		loadExperimentDataToMeasureAreas(exp);
		
		openViewers(exp);
		getTimeLimitsOfSequence(exp);
		if (measureAreas(exp)) 
			saveComputation(exp);

		closeViewers();
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
		
	
		for (Spot spot: exp.spotsArray.spotsList) 
		{
			spot.areaNPixels.setPolylineLevelFromTempData(
					spot.getRoi().getName(), 
					spot.areaNPixels.capIndexKymo, 
					0, spot.areaNPixels.limit.length-1);		
		}
		exp.saveMCExperiment();
		exp.saveSpotsMeasures();
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
		transformOptions.transformOption = options.transform01;
		transformOptions.setSingleThreshold (options.detectLevel1Threshold, options.directionUp1) ;
		getReferenceImage (exp, 0, transformOptions);
		ImageTransformInterface transformFunction = options.transform01.getFunction();
		
		for (int ii = 0; ii < nFrames; ii++) 
		{
			final int fromSourceImageIndex = ii;
			
			String title = "Frame #"+ fromSourceImageIndex + " /" + exp.seqCamData.nTotalFrames;
//			System.out.println(title);
//			progressBar.setMessage(title);
			
			IcyBufferedImage sourceImage = imageIORead(exp.seqCamData.getFileNameFromImageList(fromSourceImageIndex));
			final IcyBufferedImage workImage = transformFunction.getTransformedImage(sourceImage, transformOptions); 
			vData.setTitle(title);
			seqData.setImage(0, 0, workImage); // add option??
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
	


	private void closeViewers() 
	{
		closeViewer(vData);
		closeSequence(seqData);
	}
	
	private void openViewers(Experiment exp) 
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