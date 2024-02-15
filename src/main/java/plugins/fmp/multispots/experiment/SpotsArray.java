package plugins.fmp.multispots.experiment;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.type.geom.Polygon2D;
import icy.util.XMLUtil;

import plugins.fmp.multispots.tools.Comparators;
import plugins.fmp.multispots.tools.ROI2DUtilities;
import plugins.fmp.multispots.tools.toExcel.EnumXLSExportType;
import plugins.kernel.roi.roi2d.ROI2DShape;


public class SpotsArray 
{	
	public SpotsDescription 	spotsDescription	= new SpotsDescription();
	public SpotsDescription 	desc_old			= new SpotsDescription();
	public ArrayList <Spot> 	spotsList			= new ArrayList <Spot>();
	private	KymoIntervals 		spotsListTimeIntervals = null;
		
	private final static String ID_SPOTTRACK 		= "spotTrack";
	private final static String ID_NSPOTS			= "N_spots";
	private final static String ID_LISTOFSPOTS 		= "List_of_spots";
	private final static String ID_SPOT_ 			= "spot_";
	private final static String ID_MCSPOTS_XML 		= "MCspots.xml";

	// ---------------------------------
	
	public boolean load_Measures(String directory) 
	{
		boolean flag = false;
		try {
			flag = csvLoadSpots_Measures(directory);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!flag) {
			flag = xmlLoad_Measures(directory);
		}
		return flag;
	}
	
	public boolean save_Measures(String directory) 
	{
		if (directory == null)
			return false;
		
		csvSaveSpotsMeasures_Data(directory);
		return true;
	}
	
	// ---------------------------------
	
	public String getXMLNameToAppend() 
	{
		return ID_MCSPOTS_XML;
	}

	public boolean xmlSave_Descriptors(String csFileName) 
	{
		if (csFileName != null) {
			final Document doc = XMLUtil.createDocument(true);
			if (doc != null) 
			{
				spotsDescription.xmlSaveCapillaryDescription (doc);
				xmlSaveListOfSpots(doc);
				return XMLUtil.saveDocument(doc, csFileName);
			}
		}
		return false;
	}
	
	// ---------------------------------
	
	private boolean xmlSaveListOfSpots(Document doc) 
	{
		Node node = XMLUtil.getElement(XMLUtil.getRootElement(doc), ID_SPOTTRACK);
		if (node == null)
			return false;
		XMLUtil.setElementIntValue(node, "version", 2);
		Node nodecaps = XMLUtil.setElement(node, ID_LISTOFSPOTS);
		XMLUtil.setElementIntValue(nodecaps, ID_NSPOTS, spotsList.size());
		int i= 0;
		Collections.sort(spotsList);
		for (Spot cap: spotsList) 
		{
			Node nodecapillary = XMLUtil.setElement(node, ID_SPOT_+i);
			cap.saveToXML_CapillaryOnly(nodecapillary);
			i++;
		}
		return true;
	}
	
	public boolean loadMCSpots_Descriptors(String csFileName) 
	{	
		boolean flag = false;
		if (csFileName == null)
			return flag;
		
		final Document doc = XMLUtil.loadDocument(csFileName);
		if (doc != null) 
		{
			spotsDescription.xmlLoadCapillaryDescription(doc);
			flag = xmlLoadSpots_Only_v1(doc);
		}
		return flag;
	}
	
	public boolean xmlLoadOldSpots_Only(String csFileName) 
	{
		if (csFileName == null)
			return false;			
		final Document doc = XMLUtil.loadDocument(csFileName);
		if (doc != null) 
		{
			spotsDescription.xmlLoadCapillaryDescription(doc);
			switch (spotsDescription.version) 
			{
			case 1: // old xml storage structure
				xmlLoadSpots_Only_v1(doc);
				break;
			case 0: // old-old xml storage structure
				xmlLoad_v0(doc, csFileName);
				break;
			default:
				xmlLoadSpots_Only_v2(doc, csFileName);
				return false;
			}		
			return true;
		}
		return false;
	}
	
	private boolean xmlLoad_Measures(String directory) 
	{
		boolean flag = false;
		int ncapillaries = spotsList.size();
		for (int i = 0; i < ncapillaries; i++) 
		{
			String csFile = directory + File.separator + spotsList.get(i).getKymographName() + ".xml";
			final Document capdoc = XMLUtil.loadDocument(csFile);
			Node node = XMLUtil.getRootElement(capdoc, true);
			Spot cap = spotsList.get(i);
			cap.kymographIndex = i;
			flag |= cap.loadFromXML_MeasuresOnly(node);
		}
		return flag;
	}
	
	private void xmlLoad_v0(Document doc, String csFileName) 
	{
		List<ROI> listOfCapillaryROIs = ROI.loadROIsFromXML(XMLUtil.getRootElement(doc));
		spotsList.clear();
		Path directorypath = Paths.get(csFileName).getParent();
		String directory = directorypath + File.separator;
		int t = 0;
		for (ROI roiCapillary: listOfCapillaryROIs) 
		{
			xmlLoadIndividualSpots_v0((ROI2DShape) roiCapillary, directory, t);
			t++;
		}
	}
	
	private void xmlLoadIndividualSpots_v0(ROI2D roiCapillary, String directory, int t) 
	{
		Spot cap = new Spot(roiCapillary);
		if (!isPresent(cap))
			spotsList.add(cap);
		String csFile = directory + roiCapillary.getName() + ".xml";
		cap.kymographIndex = t;
		final Document dockymo = XMLUtil.loadDocument(csFile);
		if (dockymo != null) 
		{
			NodeList nodeROISingle = dockymo.getElementsByTagName("roi");					
			if (nodeROISingle.getLength() > 0) {	
				List<ROI> rois = new ArrayList<ROI>();
                for (int i=0; i< nodeROISingle.getLength(); i++) 
                {
                	Node element = nodeROISingle.item(i);
                    ROI roi_i = ROI.createFromXML(element);
                    if (roi_i != null)
                        rois.add(roi_i);
                }
				cap.transferROIsToMeasures(rois);
			}
		}
	}
	
	private boolean xmlLoadSpots_Only_v1(Document doc) 
	{
		Node node = XMLUtil.getElement(XMLUtil.getRootElement(doc), ID_SPOTTRACK);
		if (node == null)
			return false;
		Node nodecaps = XMLUtil.getElement(node, ID_LISTOFSPOTS);
		int nitems = XMLUtil.getElementIntValue(nodecaps, ID_NSPOTS, 0);
		spotsList = new ArrayList<Spot> (nitems);
		for (int i = 0; i < nitems; i++) 
		{
			Node nodecapillary = XMLUtil.getElement(node, ID_SPOT_+i);
			Spot cap = new Spot();
			cap.loadFromXML_CapillaryOnly(nodecapillary);
			if (spotsDescription.grouping == 2 && (cap.capStimulus != null && cap.capStimulus.equals(".."))) 
			{
				if (cap.getCapillarySide().equals("R")) 
				{
					cap.capStimulus = spotsDescription.stimulusR;
					cap.capConcentration = spotsDescription.concentrationR;
				} 
				else 
				{
					cap.capStimulus = spotsDescription.stimulusL;
					cap.capConcentration = spotsDescription.concentrationL;
				}
			}
			if (!isPresent(cap))
				spotsList.add(cap);
		}
		return true;
	}

	private void xmlLoadSpots_Only_v2(Document doc, String csFileName) 
	{
		xmlLoadSpots_Only_v1(doc);
		Path directorypath = Paths.get(csFileName).getParent();
		String directory = directorypath + File.separator;
		for (Spot cap: spotsList) 
		{
			String csFile = directory + cap.getKymographName() + ".xml";
			final Document capdoc = XMLUtil.loadDocument(csFile);
			Node node = XMLUtil.getRootElement(capdoc, true);
			cap.loadFromXML_CapillaryOnly(node);
		}
	}	

	// ---------------------------------
	
	public void copy (SpotsArray cap) 
	{
		spotsDescription.copy(cap.spotsDescription);
		spotsList.clear();
		for (Spot ccap: cap.spotsList) 
		{
			Spot capi = new Spot();
			capi.copy(ccap);
			spotsList.add(capi);
		}
	}
	
	public boolean isPresent(Spot capNew) 
	{
		boolean flag = false;
		for (Spot cap: spotsList) 
		{
			if (cap.getKymographName().contentEquals(capNew.getKymographName())) 
			{
				flag = true;
				break;
			}
		}
		return flag;
	}

	public void mergeLists(SpotsArray caplist)  
	{
		for (Spot capm : caplist.spotsList ) 
		{
			if (!isPresent(capm))
				spotsList.add(capm);
		}
	}
	
	public void adjustToImageWidth (int imageWidth) 
	{
		for (Spot cap: spotsList) 
		{
			cap.ptsTop.adjustToImageWidth(imageWidth);
			cap.ptsBottom.adjustToImageWidth(imageWidth);
			cap.ptsDerivative.adjustToImageWidth(imageWidth);
			cap.ptsGulps.gulps.clear(); 
		}
	}
	
	public void cropToImageWidth (int imageWidth) 
	{
		for (Spot cap: spotsList) 
		{
			cap.ptsTop.cropToImageWidth(imageWidth);
			cap.ptsBottom.cropToImageWidth(imageWidth);
			cap.ptsDerivative.cropToImageWidth(imageWidth);
			cap.ptsGulps.gulps.clear();
		}
	}

	public void transferDescriptionToSpots() 
	{
		for (Spot cap: spotsList) 
		{
			transferCapGroupCageIDToSpot(cap);
			cap.setVolumeAndPixels (spotsDescription.volume, spotsDescription.pixels);
		}
	}
	
	private void transferCapGroupCageIDToSpot (Spot cap) 
	{
		if (spotsDescription.grouping != 2)
			return;
		String	name = cap.getRoiName();
		String letter = name.substring(name.length() - 1);
		cap.capSide = letter;
		if (letter .equals("R")) 
		{	
			String nameL = name.substring(0, name.length() - 1) + "L";
			Spot cap0 = getSpotFromRoiName(nameL);
			if (cap0 != null) 
			{
//				cap.capNFlies = cap0.capNFlies;
				cap.capCageID = cap0.capCageID;
			}
		}
	}
	
	public Spot getSpotFromRoiName(String name) 
	{
		Spot capFound = null;
		for (Spot cap: spotsList) 
		{
			if (cap.getRoiName().equals(name)) 
			{
				capFound = cap;
				break;
			}
		}
		return capFound;
	}
	
	public Spot getSpotFromKymographName(String name) 
	{
		Spot capFound = null;
		for (Spot cap: spotsList) 
		{
			if (cap.getKymographName().equals(name)) 
			{
				capFound = cap;
				break;
			}
		}
		return capFound;
	}
	
	public Spot getSpotFromRoiNamePrefix(String name) 
	{
		Spot capFound = null;
		for (Spot cap: spotsList) 
		{
			if (cap.getRoiNamePrefix().equals(name)) 
			{
				capFound = cap;
				break;
			}
		}
		return capFound;
	}

	public void updateSpotsFromSequence(Sequence seq) 
	{
		List<ROI2D> listROISCap = ROI2DUtilities.getROIs2DContainingString ("line", seq);
		Collections.sort(listROISCap, new Comparators.ROI2D_Name_Comparator());
		for (Spot cap: spotsList) 
		{
			cap.valid = false;
			String capName = Spot.replace_LR_with_12(cap.getRoiName());
			Iterator <ROI2D> iterator = listROISCap.iterator();
			while(iterator.hasNext()) 
			{ 
				ROI2D roi = iterator.next();
				String roiName = Spot.replace_LR_with_12(roi.getName());
				if (roiName.equals (capName)) 
				{
					cap.setRoi((ROI2DShape) roi);
					cap.valid = true;
				}
				if (cap.valid) 
				{
					iterator.remove();
					break;
				}
			}
		}
		Iterator <Spot> iterator = spotsList.iterator();
		while (iterator.hasNext()) 
		{
			Spot cap = iterator.next();
			if (!cap.valid )
				iterator.remove();
		}
		if (listROISCap.size() > 0) 
		{
			for (ROI2D roi: listROISCap) 
			{
				Spot cap = new Spot((ROI2DShape) roi);
				if (!isPresent(cap))
					spotsList.add(cap);
			}
		}
		Collections.sort(spotsList);
		return;
	}

	public void transferSpotRoiToSequence(Sequence seq) 
	{
		seq.removeAllROI();
		for (Spot cap: spotsList) 
		{
			seq.addROI(cap.getRoi());
		}
	}

	public void initSpotsWith10Cages(int nflies)
	{
		int capArraySize = spotsList.size();
		for (int i = 0; i < capArraySize; i++)
		{
			Spot cap = spotsList.get(i);
			cap.capNFlies = nflies;
			if (i <= 1  || i>= capArraySize-2 )
				cap.capNFlies = 0;
			cap.capCageID = i/2;
		}
	}
	
	public void initSpotsWith6Cages(int nflies) 
	{
		int capArraySize = spotsList.size();
		for (int i = 0; i < capArraySize; i++) 
		{
			Spot cap = spotsList.get(i);
			cap.capNFlies = 1;
			if (i <= 1 ) 
			{
				cap.capNFlies = 0;
				cap.capCageID = 0;
			}
			else if (i >= capArraySize-2 ) 
			{
				cap.capNFlies = 0;
				cap.capCageID = 5;
			}
			else 
			{
				cap.capNFlies = nflies;
				cap.capCageID = 1 + (i-2)/4;
			}
		}
	}
	
	public void initSpotsWithNFlies(int nflies) 
	{
		int capArraySize = spotsList.size();
		for (int i = 0; i < capArraySize; i++) 
		{
			Spot cap = spotsList.get(i);
			cap.capNFlies = nflies;
		}
	}
	
	// -------------------------------------------------
	
	public KymoIntervals getKymoIntervalsFromCapillaries() 
	{
		if (spotsListTimeIntervals == null) 
		{
			spotsListTimeIntervals = new KymoIntervals();
			
			for (Spot cap: spotsList) 
			{
				for (KymoROI2D roiFK: cap.getROIsForKymo()) 
				{
					Long[] interval = {roiFK.getStart(), (long) -1}; 
					spotsListTimeIntervals.addIfNew(interval);
				}
			}
		}
		return spotsListTimeIntervals;
	}
	
	public int addKymoROI2DInterval(long start) 
	{
		Long[] interval = {start, (long) -1};
		int item = spotsListTimeIntervals.addIfNew(interval);
		
		for (Spot cap: spotsList) 
		{
			List<KymoROI2D> listROI2DForKymo = cap.getROIsForKymo();
			ROI2D roi = cap.getRoi();
			if (item>0 ) 
				roi = (ROI2D) listROI2DForKymo.get(item-1).getRoi().getCopy();
			listROI2DForKymo.add(item, new KymoROI2D(start, roi));
		}
		return item;
	}
	
	public void deleteKymoROI2DInterval(long start) 
	{
		spotsListTimeIntervals.deleteIntervalStartingAt(start);
		for (Spot cap: spotsList) 
			cap.removeROI2DIntervalStartingAt(start);
	}
	
	public int findKymoROI2DIntervalStart(long intervalT) 
	{
		return spotsListTimeIntervals.findStartItem(intervalT);
	}
	
	public long getKymoROI2DIntervalsStartAt(int selectedItem) 
	{
		return spotsListTimeIntervals.get(selectedItem)[0];
	}
	
	public double getScalingFactorToPhysicalUnits(EnumXLSExportType xlsoption) 
	{
		double scalingFactorToPhysicalUnits; 
		switch (xlsoption) 
		{
			case NBGULPS:
			case TTOGULP:
			case TTOGULP_LR:
			case AUTOCORREL:
			case CROSSCORREL:
			case CROSSCORREL_LR:
				scalingFactorToPhysicalUnits = 1.;
				break;
			default:
				scalingFactorToPhysicalUnits = spotsDescription.volume / spotsDescription.pixels;
				break;
		}
		return scalingFactorToPhysicalUnits;
	}
	
	public Polygon2D get2DPolygonEnclosingSpots() 
	{
		Rectangle  outerRectangle = null;
		for (Spot cap : spotsList) 
		{
			Rectangle rect = cap.getRoi().getBounds();
			if (outerRectangle == null) {
				outerRectangle = rect;
			}
			else
				outerRectangle.add(rect);
		}
		if (outerRectangle == null)
			return null;
		
		return new Polygon2D(outerRectangle);
	}
	
	public void deleteAllSpots() 
	{
		spotsList.clear();
	}
	
	// --------------------------------
	
	private boolean csvLoadSpots_Measures(String directory) throws Exception 
	{
		String pathToCsv = directory + File.separator +"CapillariesMeasures.csv";
		File csvFile = new File(pathToCsv);
		if (!csvFile.isFile()) 
			return false;
		
		BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));
		String row;
		while ((row = csvReader.readLine()) != null) {
		    String[] data = row.split(",");
		    if (data[0] .equals( "#")) {
		    	switch(data[1]) {
		    	case "DESCRIPTION":
		    		csvLoadDescription (csvReader);
		    		break;
		    	case "CAPILLARIES":
		    		csvLoadSpotsDescription (csvReader);
		    		break;
		    	case "TOPLEVEL":
		    		csvLoadCSpotsMeasures(csvReader, EnumSpotMeasures.TOPLEVEL);
		    		break;
		    	case "BOTTOMLEVEL":
		    		csvLoadCSpotsMeasures(csvReader, EnumSpotMeasures.BOTTOMLEVEL);
		    		break;
		    	case "TOPDERIVATIVE":
		    		csvLoadCSpotsMeasures(csvReader, EnumSpotMeasures.TOPDERIVATIVE);
		    		break;
		    	case "GULPS": 
		    		csvLoadCSpotsMeasures(csvReader, EnumSpotMeasures.GULPS);
		    		break;
	    		default:
	    			break;
		    	}
		    }
		}
		csvReader.close();
		
		return true;
	}
	
	private String csvLoadSpotsDescription (BufferedReader csvReader) 
	{
		String row;
		try {
			row = csvReader.readLine();			
			while ((row = csvReader.readLine()) != null) {
				String[] data = row.split(",");
				if (data[0] .equals( "#")) 
					return data[1];
				Spot cap = getSpotFromKymographName(data[2]);
				if (cap == null)
					cap = new Spot();
				cap.csvImportCapillaryDescription(data);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String csvLoadDescription (BufferedReader csvReader) 
	{
		String row;
		try {
			row = csvReader.readLine();
			row = csvReader.readLine();
			String[] data = row.split(",");
			spotsDescription.csvImportCapillariesDescriptionData(data);
			row = csvReader.readLine();
			data = row.split(",");
			if ( data[0].substring(0, Math.min( data[0].length(), 5)).equals("n cap")) {
				int ncapillaries = Integer.valueOf(data[1]);
				if (ncapillaries >= spotsList.size())
					spotsList.ensureCapacity(ncapillaries);
				else
					spotsList.subList(ncapillaries, spotsList.size()).clear();
				row = csvReader.readLine();
				data = row.split(",");
			}
			if (data[0] .equals( "#")) {
			  	return data[1];
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String csvLoadCSpotsMeasures(BufferedReader csvReader, EnumSpotMeasures measureType) 
	{
		String row;
		try {
			while ((row = csvReader.readLine()) != null) {
				String[] data = row.split(",");
				if (data[0] .equals( "#")) 
					return data[1];
				
				Spot cap = getSpotFromRoiNamePrefix(data[0]);
				if (cap == null)
					cap = new Spot();
				cap.csvImportCapillaryData(measureType, data);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	// ---------------------------------
	
	private boolean csvSaveSpotsMeasures_Data(String directory) 
	{
		Path path = Paths.get(directory);
		if (!Files.exists(path))
			return false;
		
		try {
			FileWriter csvWriter = new FileWriter(directory + File.separator +"CapillariesMeasures.csv");
			
			csvSaveDescriptionSection(csvWriter);
			
			csvSaveMeasuresSection(csvWriter, EnumSpotMeasures.TOPLEVEL);
			csvSaveMeasuresSection(csvWriter, EnumSpotMeasures.BOTTOMLEVEL);
			csvSaveMeasuresSection(csvWriter, EnumSpotMeasures.TOPDERIVATIVE);
			csvSaveMeasuresSection(csvWriter, EnumSpotMeasures.GULPS);
			csvWriter.flush();
			csvWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private boolean csvSaveDescriptionSection(FileWriter csvWriter) 
	{
		try {
			csvWriter.append(spotsDescription.csvExportSectionHeader());
			csvWriter.append(spotsDescription.csvExportExperimentDescriptors());
			csvWriter.append("n caps=," + Integer.toString(spotsList.size()) + "\n");
			csvWriter.append("#,#\n");
			
			if (spotsList.size() > 0) {
				csvWriter.append(spotsList.get(0).csvExportCapillarySubSectionHeader());
				for (Spot cap:spotsList) 
					csvWriter.append(cap.csvExportCapillaryDescription());
				csvWriter.append("#,#\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private boolean csvSaveMeasuresSection(FileWriter csvWriter, EnumSpotMeasures measureType) 
	{
		try {
			if (spotsList.size() <= 1)
				return false;
			
			csvWriter.append(spotsList.get(0).csvExportMeasureSectionHeader(measureType));
			for (Spot cap:spotsList) 
				csvWriter.append(cap.csvExportCapillaryData(measureType));
			
			csvWriter.append("#,#\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	

}