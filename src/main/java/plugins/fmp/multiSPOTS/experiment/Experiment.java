package plugins.fmp.multiSPOTS.experiment;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import icy.image.IcyBufferedImage;
import icy.image.ImageUtil;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.util.XMLUtil;
import plugins.fmp.multiSPOTS.tools.Directories;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformEnums;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformInterface;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSColumnHeader;




public class Experiment 
{
	public final static String 	RESULTS				= "results";
	public final static String 	BIN					= "bin_";
	
	private String			strImagesDirectory		= null;
	private String			strExperimentDirectory	= null;
	private String			strBinSubDirectory		= null;
		
	public SequenceCamData 	seqCamData 				= null;
	public SequenceKymos 	seqKymos				= null;
	public Sequence 		seqReference			= null;
	public CapillariesArray capillaries 			= new CapillariesArray();
	public SpotsArray		spotsArray				= new SpotsArray();
	public Cages			cages 					= new Cages();
	
	public FileTime			firstImage_FileTime;
	public FileTime			lastImage_FileTime;
	
	// __________________________________________________
	
	public 	long			camImageFirst_ms		= -1;
	public 	long			camImageLast_ms			= -1;
	public 	long			camImageBin_ms			= -1;
	public  long[] 			camImages_ms			= null;
	
	public 	long			binFirst_ms				= 0;
	public 	long			binLast_ms				= 0;
	public 	long			binDuration_ms			= 60000;
	
	// _________________________________________________
	
	private String			field_boxID 			= new String("..");
	private String			field_experiment		= new String("..");
	private String 			field_comment1			= new String("..");
	private String 			field_comment2			= new String("..");
	private String 			field_strain			= new String("..");
	private String			field_sex				= new String("..");
	private String 			field_cond1				= new String("..");
	private String 			field_cond2				= new String("..");
	
	
	public int				col						= -1;
	public Experiment 		chainToPreviousExperiment = null;	
	public Experiment 		chainToNextExperiment	= null;	
	public long				chainImageFirst_ms 		= 0;
	public int				experimentID 			= 0;
	
	private final static String ID_VERSION			= "version"; 
	private final static String ID_VERSIONNUM		= "1.0.0"; 
	private final static String ID_TIMEFIRSTIMAGE	= "fileTimeImageFirstMinute"; 
	private final static String ID_TIMELASTIMAGE 	= "fileTimeImageLastMinute";
	
	private final static String ID_TIMEFIRSTIMAGEMS	= "fileTimeImageFirstMs"; 
	private final static String ID_TIMELASTIMAGEMS 	= "fileTimeImageLastMs";
	private final static String ID_FIRSTKYMOCOLMS	= "firstKymoColMs"; 
	private final static String ID_LASTKYMOCOLMS 	= "lastKymoColMs";
	private final static String ID_BINKYMOCOLMS 	= "binKymoColMs";	

	private final static String ID_IMAGESDIRECTORY 	= "imagesDirectory";
	private final static String ID_MCEXPERIMENT 	= "MCexperiment";
	private final static String ID_MCEXPERIMENT_XML = "MCexperiment.xml";
	private final static String ID_MCDROSOTRACK_XML = "MCdrosotrack.xml";
	
	private final static String ID_BOXID 			= "boxID";
	private final static String ID_EXPERIMENT 		= "experiment";
	private final static String ID_COMMENT1 		= "comment";
	private final static String ID_COMMENT2 		= "comment2";
	private final static String ID_STRAIN			= "strain";
	private final static String ID_SEX				= "sex";
	private final static String ID_COND1			= "cond1";
	private final static String ID_COND2			= "cond2";
	
	private final static int EXPT_DIRECTORY = 1;
	private final static int IMG_DIRECTORY = 2;
	private final static int BIN_DIRECTORY = 3;
	// ----------------------------------
	
	public Experiment() 
	{
		seqCamData = new SequenceCamData();
		seqKymos   = new SequenceKymos();
	}
	
	public Experiment(String expDirectory) 
	{
		seqCamData = new SequenceCamData();
		seqKymos   = new SequenceKymos();
		this.strExperimentDirectory = expDirectory;
	}
	
	public Experiment(SequenceCamData seqCamData) 
	{
		this.seqCamData = seqCamData;
		this.seqKymos   = new SequenceKymos();
		strExperimentDirectory = this.seqCamData.getImagesDirectory() + File.separator + RESULTS;
		getFileIntervalsFromSeqCamData();
		
		xmlLoadExperiment(concatenateExptDirectoryWithSubpathAndName(null, ID_MCEXPERIMENT_XML));
	}
	
	public Experiment(ExperimentDirectories eADF) 
	{
		String imgDir = null;
		if (eADF.cameraImagesList.size() > 0)
			imgDir = eADF.cameraImagesList.get(0);
		strImagesDirectory = Directories.getDirectoryFromName(imgDir);
		strExperimentDirectory = eADF.resultsDirectory;
		String binDirectory = strExperimentDirectory + File.separator + eADF.binSubDirectory;
		Path binDirectoryPath = Paths.get(binDirectory);
		Path lastSubPath = binDirectoryPath.getName(binDirectoryPath.getNameCount()-1);
		strBinSubDirectory = lastSubPath.toString();
		
		seqCamData = new SequenceCamData(eADF.cameraImagesList);
		getFileIntervalsFromSeqCamData();
		seqKymos = new SequenceKymos(eADF.kymosImagesList);
		
		xmlLoadExperiment(concatenateExptDirectoryWithSubpathAndName(null, ID_MCEXPERIMENT_XML));
	}
	
	// ----------------------------------
	
	public String getExperimentDirectory() 
	{
		return strExperimentDirectory;
	}
	
	public String toString () 
	{
		return strExperimentDirectory;
	}
	
	public void setExperimentDirectory(String fileName) 
	{
		strExperimentDirectory = ExperimentDirectories.getParentIf(fileName, BIN);
	}
	
	public String getKymosBinFullDirectory() 
	{
		String filename = strExperimentDirectory;
		if (strBinSubDirectory != null)
			filename += File.separator + strBinSubDirectory;
		return filename;
	}
	
	public void setBinSubDirectory (String bin) 
	{
		strBinSubDirectory = bin;
	}
	
	public String getBinSubDirectory () 
	{
		return strBinSubDirectory;
	}
	
	public boolean createDirectoryIfDoesNotExist(String directory) 
    {	
		Path pathDir = Paths.get(directory);
		if (Files.notExists(pathDir))  
		{
			try 
			{
				Files.createDirectory(pathDir);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				System.out.println("Experiment:createDirectoryIfDoesNotExist() Creating directory failed: "+ directory);
				return false;
			}
		}
		return true;
    }
	  
	public void checkKymosDirectory(String kymosSubDirectory) 
	{
		if (kymosSubDirectory == null) 
		{
			List<String> listTIFFlocations = Directories.getSortedListOfSubDirectoriesWithTIFF(getExperimentDirectory());
			if (listTIFFlocations.size() < 1)
				return;
			boolean found = false;
			for (String subDir : listTIFFlocations) 
			{
				String test = subDir.toLowerCase();
				if (test.contains(Experiment.BIN)) 
				{
					kymosSubDirectory = subDir;
					found = true;
					break;
				}
				if (test.contains(Experiment.RESULTS)) 
				{
					found = true;
					break;
				}
			}
			if (!found) 
			{
				int lowest = getBinStepFromDirectoryName( listTIFFlocations.get(0)) + 1;
				for (String subDir: listTIFFlocations) 
				{
					int val = getBinStepFromDirectoryName( subDir);
					if (val < lowest) 
					{
						lowest = val;
						kymosSubDirectory = subDir;
					}
				}
			}
		}
		setBinSubDirectory(kymosSubDirectory);
	}
		
	public void setImagesDirectory(String name) 
	{
		strImagesDirectory = name;
	}
	
	public String getImagesDirectory() 
	{
		return strImagesDirectory;
	}
	
	public void closeSequences() 
	{
		if (seqKymos != null) 
			seqKymos.closeSequence();
		if (seqCamData != null) 
			seqCamData.closeSequence();
		if (seqReference != null) 
			seqReference.close();
	}
	
	public boolean openCapillarieMeasures() 
	{
		if (seqCamData == null) 
			seqCamData = new SequenceCamData();
		loadXML_MCExperiment ();
		
		getFileIntervalsFromSeqCamData();
		
		if (seqKymos == null)
			seqKymos = new SequenceKymos();

		loadMCCapillaries_Only();
		if (!capillaries.load_Measures(getKymosBinFullDirectory())) 
			return false;
		
		return true;
	}
	
	public boolean openPositionsMeasures() 
	{
		if (seqCamData == null) 
			seqCamData = new SequenceCamData();
		loadXML_MCExperiment ();
		
		getFileIntervalsFromSeqCamData();
		
		if (seqKymos == null)
			seqKymos = new SequenceKymos();


		return xmlReadDrosoTrack(null);
	}
	
	public boolean openSpotsMeasures() 
	{
		if (seqCamData == null) 
			seqCamData = new SequenceCamData();
		loadXML_MCExperiment ();
		
		getFileIntervalsFromSeqCamData();
		
		loadMCSpots_Only();
		if (!spotsArray.load_Measures(getKymosBinFullDirectory())) 
			return false;
	
		return true;
	}
	
	private String getRootWithNoResultNorBinString(String directoryName) 
	{
		String name = directoryName.toLowerCase();
		while (name .contains(RESULTS) || name .contains(BIN)) 
			name = Paths.get(strExperimentDirectory).getParent().toString();
		return name;
	}
	
	private SequenceCamData loadImagesForSequenceCamData(String filename) 
	{
		strImagesDirectory = ExperimentDirectories.getImagesDirectoryAsParentFromFileName(filename);			
		List<String> imagesList = ExperimentDirectories.getV2ImagesListFromPath(strImagesDirectory);
		imagesList = ExperimentDirectories.keepOnlyAcceptedNames_List(imagesList, "jpg");
		if (imagesList.size() < 1) 
		{
			seqCamData = null;
		} 
		else 
		{
			seqCamData = new SequenceCamData();
			seqCamData.setImagesList(imagesList);
			seqCamData.attachSequence(seqCamData.loadSequenceFromImagesList(imagesList));
		}
		return seqCamData;
	}
	
	public boolean loadCamDataImages()
	{	
		if (seqCamData != null) 
			seqCamData.loadImages();
		
		return (seqCamData != null && seqCamData.seq != null);
	}
	
	public boolean loadCamDataCapillaries()
	{	
		if( capillaries.capillariesList.size() > 0) 
		{
			for (Capillary cap: capillaries.capillariesList)
				seqCamData.seq.removeROI(cap.getRoi());	
		}
		loadMCCapillaries_Only();
		if (seqCamData != null && seqCamData.seq != null) 
		{
			capillaries.transferCapillaryRoiToSequence(seqCamData.seq);
		}
		
		return (seqCamData != null && seqCamData.seq != null);
	}
	
	public boolean loadCamDataSpots()
	{	
		loadMCSpots_Only();
		if (seqCamData != null && seqCamData.seq != null) 
			spotsArray.transferSpotRoiToSequence(seqCamData.seq);
		
		return (seqCamData != null && seqCamData.seq != null);
	}
	
	public boolean loadKymosImages() 
	{
		if (seqKymos != null)
			seqKymos.loadImages();
		return (seqKymos != null && seqKymos.seq != null);
	}
		
	public SequenceCamData openSequenceCamData() 
	{
		loadImagesForSequenceCamData(strImagesDirectory);
		if (seqCamData != null) 
		{
			loadXML_MCExperiment();
			getFileIntervalsFromSeqCamData();
		}
		return seqCamData;
	}
	
	public void getFileIntervalsFromSeqCamData() 
	{
		if (seqCamData != null && (camImageFirst_ms < 0 || camImageLast_ms < 0 || camImageBin_ms < 0))
		{
			loadFileIntervalsFromSeqCamData();
		}
	}
	
	public void loadFileIntervalsFromSeqCamData() 
	{
		if (seqCamData != null)
		{	
			seqCamData.setImagesDirectory(strImagesDirectory);
			firstImage_FileTime = seqCamData.getFileTimeFromStructuredName(0);
			lastImage_FileTime = seqCamData.getFileTimeFromStructuredName(seqCamData.nTotalFrames-1);
			if (firstImage_FileTime != null && lastImage_FileTime != null)
			{
				camImageFirst_ms = firstImage_FileTime.toMillis();
				camImageLast_ms = lastImage_FileTime.toMillis();
				camImageBin_ms = (camImageLast_ms - camImageFirst_ms)/(seqCamData.nTotalFrames-1);
				if (camImageBin_ms == 0)
					System.out.println("Experiment:loadFileIntervalsFromSeqCamData() error / file interval size");
			}
			else
			{
				System.out.println("Experiment:loadFileIntervalsFromSeqCamData() error / file intervals of " + seqCamData.getImagesDirectory());
			}
		}
	}

	public long[] build_MsTimeIntervalsArray_From_SeqCamData_FileNamesList() 
	{
		camImages_ms = new long[seqCamData.nTotalFrames];
		
		FileTime firstImage_FileTime = seqCamData.getFileTimeFromStructuredName(0);
		long firstImage_ms = firstImage_FileTime.toMillis();
		for (int i = 0; i < seqCamData.nTotalFrames; i++) {
			FileTime image_FileTime = seqCamData.getFileTimeFromStructuredName(i);
			long image_ms = image_FileTime.toMillis() - firstImage_ms;
			camImages_ms[i] = image_ms;
		}
		return camImages_ms;
	}
	
	public int findNearestIntervalWithBinarySearch(long value, int low, int high) {
	    int result = -1;
	    if(high-low>1){
	            int mid = (low + high)/2;
	        
	            if(camImages_ms[mid]>value)
	                result = findNearestIntervalWithBinarySearch(value, low, mid);
	            else if(camImages_ms[mid]<value)
	                result = findNearestIntervalWithBinarySearch(value, mid, high);
	            else
	                result = mid;
	        } else
	            result = Math.abs(value-camImages_ms[low]) < Math.abs(value-camImages_ms[high]) ? low : high;
	        
	        return result;
	}
	
	public int getClosestInterval(int icentral, long valueToCompare)
	{
		long deltacentral = Math.abs(valueToCompare - camImages_ms[icentral]);
		if (deltacentral == 0)
			return icentral;
		
		int ilow = icentral-1;
		int ihigh = icentral+1;
		if (icentral <= 0) {
			ilow = 0;
			ihigh = 2;
		}
		if (icentral >= camImages_ms.length-1) {
			ihigh = camImages_ms.length-1;
			ilow = ihigh -2;
		}
		long deltalow = Math.abs(valueToCompare - camImages_ms[ilow]);
		long deltahigh = Math.abs(valueToCompare - camImages_ms[ihigh]);
		
		int ismallest = icentral;
		long deltasmallest = deltacentral;
		
		if (deltalow <= deltasmallest) {
			ismallest = ilow;
			deltasmallest = deltalow;
		}
		
		if (deltahigh <= deltasmallest) {
		    ismallest = ihigh;
		} 
		
		return ismallest;
	}

	public String getBinNameFromKymoFrameStep() 
	{
		return BIN + binDuration_ms/1000;
	}
	
	public String getDirectoryToSaveResults() 
	{
		Path dir = Paths.get(strExperimentDirectory);
		if (strBinSubDirectory != null) 
			dir = dir.resolve(strBinSubDirectory);
		String directory = dir.toAbsolutePath().toString();
		if (!createDirectoryIfDoesNotExist(directory))
			directory = null;
		return directory;
	}
	
	// -------------------------------
	
	public boolean loadXML_MCExperiment () 
	{
		if (strExperimentDirectory == null && seqCamData != null) 
		{
			strImagesDirectory = seqCamData.getImagesDirectory() ;
			strExperimentDirectory = strImagesDirectory + File.separator + RESULTS;
		}
		boolean found = xmlLoadExperiment(concatenateExptDirectoryWithSubpathAndName(null, ID_MCEXPERIMENT_XML));
		return found;
	}
	
	public boolean saveXML_MCExperiment () 
	{
		final Document doc = XMLUtil.createDocument(true);
		if (doc != null) 
		{
			Node xmlRoot = XMLUtil.getRootElement(doc, true);
			Node node = XMLUtil.setElement(xmlRoot, ID_MCEXPERIMENT);
			if (node == null)
				return false;
			
			XMLUtil.setElementValue(node, ID_VERSION, ID_VERSIONNUM);
			XMLUtil.setElementLongValue(node, ID_TIMEFIRSTIMAGEMS, camImageFirst_ms);
			XMLUtil.setElementLongValue(node, ID_TIMELASTIMAGEMS, camImageLast_ms);
			
			XMLUtil.setElementLongValue(node, ID_FIRSTKYMOCOLMS, binFirst_ms); 
			XMLUtil.setElementLongValue(node, ID_LASTKYMOCOLMS, binLast_ms);
			XMLUtil.setElementLongValue(node, ID_BINKYMOCOLMS, binDuration_ms); 	
			
			XMLUtil.setElementValue(node, ID_BOXID, field_boxID);
	        XMLUtil.setElementValue(node, ID_EXPERIMENT, field_experiment);
	        XMLUtil.setElementValue(node, ID_COMMENT1, field_comment1);
	        XMLUtil.setElementValue(node, ID_COMMENT2, field_comment2);
	        XMLUtil.setElementValue(node, ID_STRAIN, field_strain);
	        XMLUtil.setElementValue(node, ID_SEX, field_sex);
	        XMLUtil.setElementValue(node, ID_COND1, field_cond1); 
	        XMLUtil.setElementValue(node, ID_COND2, field_cond2);
	        
	        if (strImagesDirectory == null ) 
	        	strImagesDirectory = seqCamData.getImagesDirectory();
	        XMLUtil.setElementValue(node, ID_IMAGESDIRECTORY, strImagesDirectory);

	        String tempname = concatenateExptDirectoryWithSubpathAndName(null, ID_MCEXPERIMENT_XML) ;
	        return XMLUtil.saveDocument(doc, tempname);
		}
		return false;
	}
	
 	public boolean loadKymographs() 
 	{
		if (seqKymos == null) 
			seqKymos = new SequenceKymos();
		List<ImageFileDescriptor> myList = seqKymos.loadListOfPotentialKymographsFromCapillaries(getKymosBinFullDirectory(), capillaries);
		ImageFileDescriptor.getExistingFileNames(myList);
		return seqKymos.loadImagesFromList(myList, true);
	}
	
 	// ------------------------------------------------
 	
	public boolean loadMCCapillaries_Only() 
	{
		String mcCapillaryFileName = findFile_3Locations(capillaries.getXMLNameToAppend(), EXPT_DIRECTORY, BIN_DIRECTORY, IMG_DIRECTORY);
		if (mcCapillaryFileName == null && seqCamData != null) 
			return xmlLoad_OldCapillaries();
		
		boolean flag = capillaries.loadMCCapillaries_Descriptors(mcCapillaryFileName);
		if (capillaries.capillariesList.size() < 1)
			flag = xmlLoad_OldCapillaries();
		
		// load MCcapillaries description of experiment
		if (field_boxID .contentEquals("..")
				&& field_experiment.contentEquals("..") 
				&& field_comment1.contentEquals("..")
				&& field_comment2.contentEquals("..")
				&& field_sex.contentEquals("..")
				&& field_strain.contentEquals("..")) 
		{
			field_boxID 	= capillaries.capillariesDescription.old_boxID;
			field_experiment= capillaries.capillariesDescription.old_experiment;
			field_comment1 	= capillaries.capillariesDescription.old_comment1;
			field_comment2 	= capillaries.capillariesDescription.old_comment2;
			field_sex 		= capillaries.capillariesDescription.old_sex;
			field_strain 	= capillaries.capillariesDescription.old_strain;
			field_cond1 	= capillaries.capillariesDescription.old_cond1;
			field_cond2 	= capillaries.capillariesDescription.old_cond2;
		}
		return flag;
	}
	
	public boolean loadMCSpots_Only() 
	{
		String mcSpotsFileName = findFile_3Locations(spotsArray.getXMLNameToAppend(), EXPT_DIRECTORY, BIN_DIRECTORY, IMG_DIRECTORY);
		if (mcSpotsFileName == null && seqCamData != null) 
			return false;
		
		boolean flag = spotsArray.xmlLoad_MCSpots_Descriptors(mcSpotsFileName);
		
		// load MCcapillaries description of experiment
		if (field_boxID .contentEquals("..")
				&& field_experiment.contentEquals("..") 
				&& field_comment1.contentEquals("..")
				&& field_comment2.contentEquals("..")
				&& field_sex.contentEquals("..")
				&& field_strain.contentEquals("..")) 
		{
			field_boxID 	= spotsArray.spotsDescription.old_boxID;
			field_experiment= spotsArray.spotsDescription.old_experiment;
			field_comment1 	= spotsArray.spotsDescription.old_comment1;
			field_comment2 	= spotsArray.spotsDescription.old_comment2;
			field_sex 		= spotsArray.spotsDescription.old_sex;
			field_strain 	= spotsArray.spotsDescription.old_strain;
			field_cond1 	= spotsArray.spotsDescription.old_cond1;
			field_cond2 	= spotsArray.spotsDescription.old_cond2;
		}
		return flag;
	}
	
	public boolean loadMCCapillaries() 
	{
		String xmlCapillariesFileName = findFile_3Locations(capillaries.getXMLNameToAppend(), EXPT_DIRECTORY, BIN_DIRECTORY, IMG_DIRECTORY);
		boolean flag1 = capillaries.loadMCCapillaries_Descriptors(xmlCapillariesFileName);
		String kymosImagesDirectory = getKymosBinFullDirectory();
		boolean flag2 = capillaries.load_Measures(kymosImagesDirectory);
		if (flag1 & flag2) 
			seqKymos.loadListOfPotentialKymographsFromCapillaries(kymosImagesDirectory, capillaries);
		return flag1 & flag2;
	}
	
	public boolean load_Spots() 
	{
		boolean flag1 = loadMCSpots_Only();
		return flag1 & spotsArray.load_Spots(strExperimentDirectory);
	}
	
	public boolean save_Spots() 
	{
		return spotsArray.save_Spots(strExperimentDirectory);
	}
	
	private boolean xmlLoad_OldCapillaries() 
	{
		String filename = findFile_3Locations("capillarytrack.xml", IMG_DIRECTORY, EXPT_DIRECTORY, BIN_DIRECTORY);
		if (capillaries.xmlLoadOldCapillaries_Only(filename)) 
		{
			xmlSave_MCCapillaries_Only();
			saveCapillariesMeasures();
			try {
		        Files.delete(Paths.get(filename));
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
			return true;
		}
		filename = findFile_3Locations("roislines.xml", IMG_DIRECTORY, EXPT_DIRECTORY, BIN_DIRECTORY);
		if (xmlLoad_CamDataROIs(filename)) 
		{
			xmlLoad_RoiLineParameters(filename);
			try {
		        Files.delete(Paths.get(filename));
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
			return true;
		}
		return false;
	}
	
	private boolean xmlLoad_CamDataROIs(String fileName) 
	{
		Sequence seq = seqCamData.seq;
		if (fileName != null)  
		{
			final Document doc = XMLUtil.loadDocument(fileName);
			if (doc != null) 
			{
				List<ROI2D> seqRoisList = seq.getROI2Ds(false);
				List<ROI2D> newRoisList = ROI2DUtilities.loadROIsFromXML(doc);
				ROI2DUtilities.mergeROIsListNoDuplicate(seqRoisList, newRoisList, seq);
				seq.removeAllROI();
				seq.addROIs(seqRoisList, false);
				return true;
			}
		}
		return false;
	}
	
	private boolean xmlLoad_RoiLineParameters(String filename) 
	{
		if (filename != null)  
		{
			final Document doc = XMLUtil.loadDocument(filename);
			if (doc != null) 
				return capillaries.capillariesDescription.xmlLoadCapillaryDescription(doc); 
		}
		return false;
	}

	// ---------------------------------------------
	
	public boolean xmlSave_MCCapillaries_Only() 
	{
		String xmlCapillaryFileName = strExperimentDirectory + File.separator + capillaries.getXMLNameToAppend();
		transferExpDescriptorsToCapillariesDescriptors();
		return capillaries.xmlSaveCapillaries_Descriptors(xmlCapillaryFileName);
	}
	
	public boolean xmlSave_MCSpots_Only() 
	{
		String xmlSpotFileName = strExperimentDirectory + File.separator + spotsArray.getXMLNameToAppend();
		transferExpDescriptorsToSpotsDescriptors();
		return spotsArray.xmlSave_Spots_Descriptors(xmlSpotFileName);
	}

 	public boolean loadCapillariesMeasures() 
 	{
 		return capillaries.load_Measures(getKymosBinFullDirectory());
 	}
 	
 	public boolean saveCapillariesMeasures() 
 	{
 		return capillaries.save_Measures(getKymosBinFullDirectory());
 	}
 	
 	public boolean loadSpotsMeasures() 
 	{
 		return spotsArray.load_Measures(getExperimentDirectory());
 	}
 	
 	public boolean saveSpotsMeasures() 
 	{
 		return spotsArray.save_Measures(getExperimentDirectory());
 	}
 	
 	public boolean loadCagesMeasures() 
	{
		return xmlReadDrosoTrack(null);
	}
	
	public boolean saveCagesMeasures() 
	{
		return cages.saveCagesMeasures(getExperimentDirectory());
	}
	
	// ----------------------------------
	
	public Experiment getFirstChainedExperiment(boolean globalValue) 
	{
		Experiment exp = this;
		if (globalValue && chainToPreviousExperiment != null)
			exp = chainToPreviousExperiment.getFirstChainedExperiment(globalValue);
		return exp;
	}
		
	public Experiment getLastChainedExperiment(boolean globalValue) 
	{
		Experiment exp = this;
		if (globalValue && chainToNextExperiment != null)
			exp = chainToNextExperiment.getLastChainedExperiment(globalValue);
		return exp;
	}
	
	public void setFileTimeImageFirst(FileTime fileTimeImageFirst) 
	{
		this.firstImage_FileTime = fileTimeImageFirst;
	}
		
	public void setFileTimeImageLast(FileTime fileTimeImageLast) 
	{
		this.lastImage_FileTime = fileTimeImageLast;
	}
		
	public int getSeqCamSizeT() 
	{
		int lastFrame = 0;
		if (seqCamData != null )
			lastFrame = seqCamData.nTotalFrames -1;
		return lastFrame;
	}
	
	public String getExperimentField(EnumXLSColumnHeader fieldEnumCode)
	{
		String strField = null;
		switch (fieldEnumCode)
		{
		case EXP_STIM:
			strField = field_comment1;
			break;
		case EXP_CONC:
			strField = field_comment2;
			break;
		case EXP_EXPT:
			strField = field_experiment;
			break;
		case EXP_BOXID:
			strField = field_boxID;
			break;
		case EXP_STRAIN:
			strField = field_strain;
			break;
		case EXP_SEX:
			strField = field_sex;
			break;
		case EXP_COND1:
			strField = field_cond1;
			break;
		case EXP_COND2:
			strField = field_cond2;
			break;
		default:
			break;
		}
		return strField;
	}
	
	public void getFieldValues(EnumXLSColumnHeader fieldEnumCode, List<String> textList)
	{
		switch (fieldEnumCode)
		{
		case EXP_STIM:
		case EXP_CONC:
		case EXP_EXPT:
		case EXP_BOXID:
		case EXP_STRAIN:
		case EXP_SEX:
		case EXP_COND1:
		case EXP_COND2:
			addValue(getExperimentField(fieldEnumCode), textList);
			break;
		case CAP_STIM:
		case CAP_CONC:
			addCapillariesValues(fieldEnumCode, textList);
			addSpotsValues(fieldEnumCode, textList);
			break;
		default:
			break;
		}
	}
	
	public boolean replaceExperimentFieldIfEqualOld (EnumXLSColumnHeader fieldEnumCode, String oldValue, String newValue)
	{
		boolean flag = getExperimentField(fieldEnumCode).equals (oldValue) ;
		if (flag) {
			setExperimentFieldNoTest(fieldEnumCode, newValue);
		}
		return flag;
	}
	
	public void copyExperimentFields(Experiment expSource) 
	{
		setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_BOXID, expSource.getExperimentField(EnumXLSColumnHeader.EXP_BOXID));
		setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_EXPT, expSource.getExperimentField(EnumXLSColumnHeader.EXP_EXPT));
		setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_STIM, expSource.getExperimentField(EnumXLSColumnHeader.EXP_STIM));
		setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_CONC, expSource.getExperimentField(EnumXLSColumnHeader.EXP_CONC));	
		setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_STRAIN, expSource.getExperimentField(EnumXLSColumnHeader.EXP_STRAIN));
		setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_SEX, expSource.getExperimentField(EnumXLSColumnHeader.EXP_SEX));
		setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_COND1, expSource.getExperimentField(EnumXLSColumnHeader.EXP_COND1));
		setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_COND2, expSource.getExperimentField(EnumXLSColumnHeader.EXP_COND2));
	}
	
	public void setExperimentFieldNoTest (EnumXLSColumnHeader fieldEnumCode, String newValue)
	{
		switch (fieldEnumCode)
		{
		case EXP_STIM:
			field_comment1 = newValue;
			break;
		case EXP_CONC:
			field_comment2  = newValue;
			break;
		case EXP_EXPT:
			field_experiment = newValue;
			break;
		case EXP_BOXID:
			field_boxID  = newValue; 
			break;
		case EXP_STRAIN:
			field_strain  = newValue; 
			break;
		case EXP_SEX:
			field_sex  = newValue; 
			break;
		case EXP_COND1:
			field_cond1 = newValue;
			break;
		case EXP_COND2:
			field_cond2 = newValue;
			break;
		default:
			break;
		}
	}
	
	public void replaceFieldValue(EnumXLSColumnHeader fieldEnumCode, String oldValue, String newValue) 
	{
		switch (fieldEnumCode)
		{
		case EXP_STIM:
		case EXP_CONC:
		case EXP_EXPT:
		case EXP_BOXID:
		case EXP_STRAIN:
		case EXP_SEX:
		case EXP_COND1:
		case EXP_COND2:
			replaceExperimentFieldIfEqualOld(fieldEnumCode, oldValue, newValue);
			break;
		case CAP_STIM:
		case CAP_CONC:
			if(replaceCapillariesValuesIfEqualOld(fieldEnumCode, oldValue, newValue));
				xmlSave_MCCapillaries_Only();
			if(replaceSpotsValuesIfEqualOld(fieldEnumCode, oldValue, newValue));
				xmlSave_MCSpots_Only();	
			break;
		default:
			break;
		}
	}
	
	// --------------------------------------------
	
	public boolean adjustCapillaryMeasuresDimensions() 
	{
		if (seqKymos.imageWidthMax < 1) 
		{
			seqKymos.imageWidthMax = seqKymos.seq.getSizeX();
			if (seqKymos.imageWidthMax < 1)
				return false;
		}
		int imageWidth = seqKymos.imageWidthMax;
		capillaries.adjustToImageWidth(imageWidth);
		seqKymos.seq.removeAllROI();
		seqKymos.transferCapillariesMeasuresToKymos(capillaries);
		return true;
	}
	
	public boolean cropCapillaryMeasuresDimensions() 
	{
		if (seqKymos.imageWidthMax < 1) 
		{
			seqKymos.imageWidthMax = seqKymos.seq.getSizeX();
			if (seqKymos.imageWidthMax < 1)
				return false;
		}
		int imageWidth = seqKymos.imageWidthMax;
		capillaries.cropToImageWidth(imageWidth);
		seqKymos.seq.removeAllROI();
		seqKymos.transferCapillariesMeasuresToKymos(capillaries);
		return true;
	}
	
	public boolean saveCapillariesMeasures(String directory) 
	{
		boolean flag = false;
		if (seqKymos != null && seqKymos.seq != null) 
		{
			seqKymos.validateRois();
			seqKymos.transferKymosRoisToCapillaries_Measures(capillaries);
			flag = capillaries.save_Measures(directory);
		}
		return flag;
	}
	
	public void kymosBuildFiltered01(int zChannelSource, int zChannelDestination, ImageTransformEnums transformop1, int spanDiff) 
	{
		int nimages = seqKymos.seq.getSizeT();
		seqKymos.seq.beginUpdate();

		ImageTransformInterface transform = transformop1.getFunction();
		if (transform == null)
			return;
		
		if (capillaries.capillariesList.size() != nimages) 
			SequenceKymosUtils.transferCamDataROIStoKymo(this);
		
		for (int t= 0; t < nimages; t++) 
		{
			Capillary cap = capillaries.capillariesList.get(t);
			cap.kymographIndex = t;
			IcyBufferedImage img = seqKymos.getSeqImage(t, zChannelSource);
			IcyBufferedImage img2 = transform.getTransformedImage (img, null);
			if (seqKymos.seq.getSizeZ(0) < (zChannelDestination+1)) 
				seqKymos.seq.addImage(t, img2);
			else
				seqKymos.seq.setImage(t, zChannelDestination, img2);
		}
		
		seqKymos.seq.dataChanged();
		seqKymos.seq.endUpdate();
	}
	
	public boolean loadReferenceImage() 
	{
		BufferedImage image = null;
		File inputfile = new File(getReferenceImageFullName());
		boolean exists = inputfile.exists();
		if (!exists) 
			return false;	
		image = ImageUtil.load(inputfile, true);
		if (image == null) {
			System.out.println("Experiment:loadReferenceImage() image not loaded / not found");
			return false;
		}			
		seqCamData.refImage =  IcyBufferedImage.createFrom(image);
		seqReference = new Sequence(seqCamData.refImage);
		seqReference.setName("referenceImage");
		return true;
	}
	
	public boolean saveReferenceImage(IcyBufferedImage referenceImage) 
	{
		File outputfile = new File(getReferenceImageFullName());
		RenderedImage image = ImageUtil.toRGBImage(referenceImage);
		return ImageUtil.save(image, "jpg", outputfile);
	}
	
	public void cleanPreviousDetectedFliesROIs() 
	{
		ArrayList<ROI2D> list = seqCamData.seq.getROI2Ds();
		for (ROI2D roi: list) 
		{
			if (roi.getName().contains("det")) 
				seqCamData.seq.removeROI(roi);
		}
	}

	public String getMCDrosoTrackFullName() 
	{
		return strExperimentDirectory+File.separator+ID_MCDROSOTRACK_XML;
	}
	
	public void updateROIsAt(int t) 
	{
		seqCamData.seq.beginUpdate();
		List<ROI2D> rois = seqCamData.seq.getROI2Ds();
		for (ROI2D roi: rois) 
		{
		    if (roi.getName().contains("det") ) 
		    	seqCamData.seq.removeROI(roi);
		}
		seqCamData.seq.addROIs(cages.getPositionsAsListOfROI2DRectanglesAtT(t), false);
		seqCamData.seq.endUpdate();
	}
		
	public void saveDetRoisToPositions() 
	{
		List<ROI2D> detectedROIsList= seqCamData.seq.getROI2Ds();
		for (Cage cage : cages.cagesList) 
		{
			cage.transferRoisToPositions(detectedROIsList);
		}
	}
	
	// ----------------------------------
	
	private int getBinStepFromDirectoryName(String resultsPath) 
	{
		int step = -1;
		if (resultsPath.contains(BIN)) 
		{
			if (resultsPath.length() < (BIN.length() +1)) 
			{
				step = (int) binDuration_ms;
			} 
			else 
			{
				step = Integer.valueOf(resultsPath.substring(BIN.length()))*1000;
			}
		}
		return step;
	}
	
	private boolean xmlReadDrosoTrack(String filename) 
	{
		if (filename == null) 
		{
			filename = getXMLDrosoTrackLocation();
			if (filename == null)
				return false;
		}
		return cages.xmlReadCagesFromFileNoQuestion(filename, this);
	}
	
	private String findFile_3Locations(String xmlFileName, int first, int second, int third) 
	{
		// current directory
		String xmlFullFileName = findFile_1Location(xmlFileName, first);
		if (xmlFullFileName == null) 
			xmlFullFileName = findFile_1Location(xmlFileName, second);
		if (xmlFullFileName == null) 
			xmlFullFileName = findFile_1Location(xmlFileName, third);
		return xmlFullFileName;
	}
	
	private String findFile_1Location(String xmlFileName, int item) 
	{
		String xmlFullFileName = File.separator + xmlFileName;
		switch (item) 
		{
		case IMG_DIRECTORY:
			strImagesDirectory = getRootWithNoResultNorBinString(strExperimentDirectory);
			xmlFullFileName = strImagesDirectory + File.separator + xmlFileName;
			break;
			
		case BIN_DIRECTORY:
			// any directory (below)
			Path dirPath = Paths.get(strExperimentDirectory);
			List<Path> subFolders = Directories.getAllSubPathsOfDirectory(strExperimentDirectory, 1);
			if (subFolders == null)
				return null;
			List<String> resultsDirList = Directories.getPathsContainingString(subFolders, RESULTS);
			List<String> binDirList = Directories.getPathsContainingString(subFolders, BIN);
			resultsDirList.addAll(binDirList);
			for (String resultsSub : resultsDirList) 
			{
				Path dir = dirPath.resolve(resultsSub+ File.separator + xmlFileName);
				if (Files.notExists(dir))
					continue;
				xmlFullFileName = dir.toAbsolutePath().toString();	
				break;
			}
			break;
			
		case EXPT_DIRECTORY:
		default:
			xmlFullFileName = strExperimentDirectory + xmlFullFileName;
			break;	
		}
		
		// current directory
		if(xmlFullFileName != null && fileExists (xmlFullFileName)) 
		{
			if (item == IMG_DIRECTORY) {
				strImagesDirectory = getRootWithNoResultNorBinString(strExperimentDirectory);
				ExperimentDirectories.moveAndRename(xmlFileName, strImagesDirectory, xmlFileName,strExperimentDirectory);
				xmlFullFileName = strExperimentDirectory + xmlFullFileName;
			}
			return xmlFullFileName;
		}
		return null;
	}
	
	private boolean fileExists (String fileName) 
	{
		File f = new File(fileName);
		return (f.exists() && !f.isDirectory()); 
	}
	
	private boolean replaceCapillariesValuesIfEqualOld(EnumXLSColumnHeader fieldEnumCode, String oldValue, String newValue)
	{
		if (capillaries.capillariesList.size() == 0)
			loadMCCapillaries_Only();
		boolean flag = false;
		for (Capillary cap:  capillaries.capillariesList) 
		{
			if (cap.getCapillaryField(fieldEnumCode) .equals(oldValue))
			{
				cap.setCapillaryField(fieldEnumCode, newValue);
				flag = true;
			}
		}
		return flag;
	}
	
	private boolean replaceSpotsValuesIfEqualOld(EnumXLSColumnHeader fieldEnumCode, String oldValue, String newValue)
	{
		if (spotsArray.spotsList.size() == 0)
			loadMCSpots_Only();
		boolean flag = false;
		for (Spot spot:  spotsArray.spotsList) 
		{
			if (spot.getSpotField(fieldEnumCode) .equals(oldValue))
			{
				spot.setSpotField(fieldEnumCode, newValue);
				flag = true;
			}
		}
		return flag;
	}
	
	private String concatenateExptDirectoryWithSubpathAndName(String subpath, String name) 
	{
		if (subpath != null)
			return strExperimentDirectory + File.separator + subpath + File.separator + name;
		else
			return strExperimentDirectory + File.separator + name;
	}
	
	private boolean xmlLoadExperiment (String csFileName) 
	{	
		final Document doc = XMLUtil.loadDocument(csFileName);
		if (doc == null)
			return false;
		Node node = XMLUtil.getElement(XMLUtil.getRootElement(doc), ID_MCEXPERIMENT);
		if (node == null)
			return false;

		String version = XMLUtil.getElementValue(node, ID_VERSION, ID_VERSIONNUM);
		if (!version .equals(ID_VERSIONNUM))
			return false;
		camImageFirst_ms = XMLUtil.getElementLongValue(node, ID_TIMEFIRSTIMAGEMS, 0);
		camImageLast_ms = XMLUtil.getElementLongValue(node, ID_TIMELASTIMAGEMS, 0);
		if (camImageLast_ms <= 0) 
		{
			camImageFirst_ms = XMLUtil.getElementLongValue(node, ID_TIMEFIRSTIMAGE, 0)*60000;
			camImageLast_ms = XMLUtil.getElementLongValue(node, ID_TIMELASTIMAGE, 0)*60000;
		}

		binFirst_ms = XMLUtil.getElementLongValue(node, ID_FIRSTKYMOCOLMS, -1); 
		binLast_ms = XMLUtil.getElementLongValue(node, ID_LASTKYMOCOLMS, -1);
		binDuration_ms = XMLUtil.getElementLongValue(node, ID_BINKYMOCOLMS, -1); 	
		
		ugly_checkOffsetValues();
		
		if (field_boxID != null && field_boxID .contentEquals("..")) 
		{
			field_boxID		= XMLUtil.getElementValue(node, ID_BOXID, "..");
	        field_experiment= XMLUtil.getElementValue(node, ID_EXPERIMENT, "..");
	        field_comment1 	= XMLUtil.getElementValue(node, ID_COMMENT1, "..");
	        field_comment2 	= XMLUtil.getElementValue(node, ID_COMMENT2, "..");
	        field_strain 	= XMLUtil.getElementValue(node, ID_STRAIN, "..");
	        field_sex 		= XMLUtil.getElementValue(node, ID_SEX, "..");
	        field_cond1		= XMLUtil.getElementValue(node, ID_COND1, "..");
	        field_cond2		= XMLUtil.getElementValue(node, ID_COND2, "..");
		}
		return true;
	}
	
	private void ugly_checkOffsetValues()
	{
		if (camImageFirst_ms < 0)
			camImageFirst_ms = 0;
		if (camImageLast_ms < 0)
			camImageLast_ms = 0;
		if (binFirst_ms < 0)
			binFirst_ms = 0; 
		if (binLast_ms < 0)
			binLast_ms = 0;
		
		if (binDuration_ms < 0)
			binDuration_ms = 60000;
	}
	
	private void addCapillariesValues(EnumXLSColumnHeader fieldEnumCode, List<String> textList)
	{
		if (capillaries.capillariesList.size() == 0)
			loadMCCapillaries_Only();
		for (Capillary cap:  capillaries.capillariesList) 
			addValue(cap.getCapillaryField(fieldEnumCode), textList);
	}
	
	private void addSpotsValues(EnumXLSColumnHeader fieldEnumCode, List<String> textList)
	{
		if (spotsArray.spotsList.size() == 0)
			loadMCSpots_Only();
		for (Spot spot : spotsArray.spotsList) 
			addValue(spot.getSpotField(fieldEnumCode), textList);
	}
	
	private void addValue(String text, List<String> textList) {
		if (!isFound(text, textList))
			textList.add(text);
	}
	
	private boolean isFound (String pattern, List<String> names) 
	{
		boolean found = false;
		if (names.size() > 0) 
		{
			for (String name: names) 
			{
				found = name.equals(pattern);
				if (found)
					break;
			}
		}
		return found;
	}

	private void transferExpDescriptorsToCapillariesDescriptors() 
	{
		capillaries.capillariesDescription.old_boxID 	= field_boxID;
		capillaries.capillariesDescription.old_experiment = field_experiment;
		capillaries.capillariesDescription.old_comment1 = field_comment1;
		capillaries.capillariesDescription.old_comment2 = field_comment2;	
		capillaries.capillariesDescription.old_strain 	= field_strain;
		capillaries.capillariesDescription.old_sex 		= field_sex;
		capillaries.capillariesDescription.old_cond1 	= field_cond1;
		capillaries.capillariesDescription.old_cond2 	= field_cond2;
	}
	
	private void transferExpDescriptorsToSpotsDescriptors() 
	{
		spotsArray.spotsDescription.old_boxID 			= field_boxID;
		spotsArray.spotsDescription.old_experiment 		= field_experiment;
		spotsArray.spotsDescription.old_comment1 		= field_comment1;
		spotsArray.spotsDescription.old_comment2 		= field_comment2;	
		spotsArray.spotsDescription.old_strain 			= field_strain;
		spotsArray.spotsDescription.old_sex 			= field_sex;
		spotsArray.spotsDescription.old_cond1 			= field_cond1;
		spotsArray.spotsDescription.old_cond2 			= field_cond2;
	}

	private String getReferenceImageFullName() 
	{
		return strExperimentDirectory+File.separator+"referenceImage.jpg";
	}
	
	private String getXMLDrosoTrackLocation() 
	{
		String fileName = findFile_3Locations(ID_MCDROSOTRACK_XML, EXPT_DIRECTORY, BIN_DIRECTORY, IMG_DIRECTORY);
		if (fileName == null)  
			fileName = findFile_3Locations("drosotrack.xml", IMG_DIRECTORY, EXPT_DIRECTORY, BIN_DIRECTORY);
		return fileName;
	}
	
	
}
