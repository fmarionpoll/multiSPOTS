package plugins.fmp.multiSPOTS.series;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;

import icy.gui.frame.progress.ProgressFrame;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.sequence.Sequence;
import icy.system.SystemUtil;
import icy.system.thread.Processor;
import icy.type.collection.array.ArrayUtil;

import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.ROI2DAlongTime;
import plugins.fmp.multiSPOTS.experiment.SequenceCamData;
import plugins.fmp.multiSPOTS.experiment.Spot;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformInterface;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformOptions;



public class DetectSpots extends BuildSeries  
{
	public Sequence seqData = new Sequence();
	private Viewer vData = null;
	
	// --------------------------------------------
	
	void analyzeExperiment(Experiment exp) 
	{
		loadExperimentDataToMeasureSpots(exp);
		
		openViewers(exp);
		getTimeLimitsOfSequence(exp);
		if (measureSpots(exp)) 
			saveComputation(exp);

		closeViewers();
	}
	
	private boolean loadExperimentDataToMeasureSpots(Experiment exp) 
	{
		boolean flag = exp.loadMCSpots_Only();
		exp.seqCamData.seq = exp.seqCamData.initSequenceFromFirstImage(exp.seqCamData.getImagesList(true));
		return flag;
	}
	
	private void getTimeLimitsOfSequence(Experiment exp)
	{
		exp.getFileIntervalsFromSeqCamData();
		exp.loadFileIntervalsFromSeqCamData();
		exp.binDuration_ms = exp.camImageBin_ms;
		System.out.println("sequence bin size = "+exp.binDuration_ms);
		if (options.isFrameFixed) 
		{
			exp.binFirst_ms = options.t_Ms_First;
			exp.binLast_ms = options.t_Ms_Last;
			if (exp.binLast_ms + exp.camImageFirst_ms > exp.camImageLast_ms)
				exp.binLast_ms = exp.camImageLast_ms - exp.camImageFirst_ms;
		} 
		else 
		{
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

		exp.spotsArray.transferSumToSumClean();
		exp.spotsArray.transferLimitMeasuresToPolyline(); 
		exp.saveXML_MCExperiment();
		exp.save_SpotsMeasures();
	}
	
	private boolean measureSpots (Experiment exp) 
	{
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
	    int ntasks =  exp.spotsArray.spotsList.size(); //
	    ArrayList<Future<?>> tasks = new ArrayList<Future<?>>( ntasks);
	    tasks.clear();
	    
	    int nFrames = exp.seqCamData.nTotalFrames;
	    ProgressFrame progressBar1 = new ProgressFrame("Analyze stack frame ");
		
	    initMasks2D(exp);
		initSpotsDataArrays(exp);
		ImageTransformOptions transformOptions = new ImageTransformOptions();
		transformOptions.transformOption = options.transform01;
		transformOptions.setSingleThreshold (options.spotThreshold, options.spotThresholdUp) ;

		ImageTransformInterface transformFunction = options.transform01.getFunction();
		
		int binT0 = (int) exp.binT0;
		for (int ii = binT0; ii < nFrames; ii++) {
			final int t = ii;
			tasks.add(processor.submit(new Runnable () {
				@Override
				public void run() {	
					progressBar1.setMessage("Analyze frame: " + t + "//" + nFrames);
					final IcyBufferedImage sourceImage = imageIORead(exp.seqCamData.getFileNameFromImageList(t));
					String title = "Frame #"+ t + " /" + nFrames;
					vData.setTitle(title);
//					seqData.setImage(0, 0, sourceImage); 
					final IcyBufferedImage workImage = transformFunction.getTransformedImage(sourceImage, transformOptions); 
					for (Spot spot: exp.spotsArray.spotsList)  {
						measureSpotArea (workImage, spot, t);
						spot.flyPresent.measureBooleans[t] = isFlyPresentInSpotArea (sourceImage, spot, t) > 0;
					}
				}}));
		}
		waitFuturesCompletion(processor, tasks, null);
		return true;
	}
	
	private int isFlyPresentInSpotArea(IcyBufferedImage sourceImage, Spot spot, int t  )
	{
		int flyThreshold = options.flyThreshold;
        
        IcyBufferedImage subSourceImage = IcyBufferedImageUtil.getSubImage(sourceImage, spot.mask2D.bounds);
        int[] sourceData = (int[]) ArrayUtil.arrayToIntArray(subSourceImage.getDataXY(2), sourceImage.isSignedDataType());
        int flyFound = 0;  
        
        ROI2DAlongTime roiT = spot.getROI2DKymoAtIntervalT(t);
        if (roiT.getMask2D() == null) {
        	roiT.buildMask2DFromRoi();
        }
        boolean[] mask = roiT.getMask2D().mask;
        
        if (options.flyThresholdUp) { 
	        for (int offset = 0; offset < sourceData.length; offset++) {
	            if (mask[offset] && (sourceData[offset] > flyThreshold)) {
	            	flyFound ++;
	            	break;
	            }
	        }
        }
        else {
        	for (int offset = 0; offset < sourceData.length; offset++) {
	            if (mask[offset] && (sourceData[offset] < flyThreshold)) {
	            	flyFound++;
	            	break;
	            }
	        }
        }
        return flyFound;
	}
	
	private void measureSpotArea(IcyBufferedImage workImage, Spot spot, int t  )
	{
		int sum = 0;
        boolean spotThresholdUp = options.spotThresholdUp;
        int spotThreshold = options.spotThreshold;
        ROI2DAlongTime roiT = spot.getROI2DKymoAtIntervalT(t);
        if (roiT.getMask2D() == null) {
        	roiT.buildMask2DFromRoi();
        }
        
        IcyBufferedImage subWorkImage = IcyBufferedImageUtil.getSubImage(workImage, roiT.getMask2D().bounds);
        boolean[] mask = roiT.getMask2D().mask;
        int[] workData = (int[]) ArrayUtil.arrayToIntArray(subWorkImage.getDataXY(0), workImage.isSignedDataType());  
        
        if (spotThresholdUp) 
        {
	        for (int offset = 0; offset < workData.length; offset++) 
	        {
	            if (mask[offset])  
	            {
	                int value = workData[offset];    
                    if (value < spotThreshold) 
                    {
                        sum += value;
                    }
	            }
	        } 
        }
        else  {
	        for (int offset = 0; offset < workData.length; offset++) 
	        {
	            if (mask[offset]) {
	                int value = workData[offset];
	                if (value > spotThreshold) 
	                {
                        sum += value;
	                }
	            }
	        }
        }
        spot.sum.measureValues[t] = sum ;
	}
	
	private void initSpotsDataArrays(Experiment exp)
	{
		//int n_measures = (int) ((exp.binLast_ms - exp.binFirst_ms) / exp.binDuration_ms + 1);
		int nFrames = exp.seqCamData.nTotalFrames - (int) exp.binT0;
		for (Spot spot: exp.spotsArray.spotsList) {
			spot.sum.measureValues 			= new  double [nFrames+1];
			spot.sumClean.measureValues 	= new  double [nFrames+1];
			spot.flyPresent.measureBooleans = new  boolean [nFrames+1];	
		}
	}
	
	private void initMasks2D(Experiment exp) 
	{
		SequenceCamData seqCamData = exp.seqCamData;
		if (seqCamData.seq == null) 
			seqCamData.seq = exp.seqCamData.initSequenceFromFirstImage(exp.seqCamData.getImagesList(true));

		for (Spot spot: exp.spotsArray.spotsList) 
		{
			try {
				spot.mask2D = spot.getRoi().getBooleanMask2D( 0 , 0, 1, true );
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