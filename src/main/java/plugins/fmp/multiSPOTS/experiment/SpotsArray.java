package plugins.fmp.multiSPOTS.experiment;

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

import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.type.geom.Polygon2D;
import icy.util.XMLUtil;
import plugins.fmp.multiSPOTS.tools.Comparators;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSExportType;
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
	private final 		 String	csvFileName 		= "SpotsMeasures.csv";

	// ---------------------------------
	
	public boolean load_Measures(String directory) 
	{
		boolean flag = false;
		try 
		{
			flag = csvLoadSpots(directory, EnumSpotMeasures.SPOTS_MEASURES);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	public boolean load_Spots(String directory) 
	{
		boolean flag = false;
		try 
		{
			flag = csvLoadSpots(directory, EnumSpotMeasures.ALL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	public boolean save_Spots(String directory) 
	{
		boolean flag = false;
		try 
		{
			flag = csvSaveSpots(directory);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	public boolean save_Measures(String directory) 
	{
		if (directory == null)
			return false;
		
		csvSaveSpots(directory);
		return true;
	}
	
	// ---------------------------------
	
	public String getXMLNameToAppend() 
	{
		return ID_MCSPOTS_XML;
	}

	public boolean xmlSave_Spots_Descriptors(String csFileName) 
	{
		if (csFileName != null) {
			final Document doc = XMLUtil.createDocument(true);
			if (doc != null) 
			{
				spotsDescription.xmlSaveSpotsDescription (doc);
				xmlSave_ListOfSpots(doc);
				return XMLUtil.saveDocument(doc, csFileName);
			}
		}
		return false;
	}
	
	private boolean xmlSave_ListOfSpots(Document doc) 
	{
		Node node = XMLUtil.getElement(XMLUtil.getRootElement(doc), ID_SPOTTRACK);
		if (node == null)
			return false;
		XMLUtil.setElementIntValue(node, "version", 2);
		Node nodeSpotsArray = XMLUtil.setElement(node, ID_LISTOFSPOTS);
		XMLUtil.setElementIntValue(nodeSpotsArray, ID_NSPOTS, spotsList.size());
		int i= 0;
		Collections.sort(spotsList);
		for (Spot spot: spotsList) 
		{
			Node nodeSpot = XMLUtil.setElement(node, ID_SPOT_+i);
			spot.saveToXML_SpotOnly(nodeSpot);
			i++;
		}
		return true;
	}
	
	public boolean xmlLoad_MCSpots_Descriptors(String csFileName) 
	{	
		boolean flag = false;
		if (csFileName == null)
			return flag;
		
		final Document doc = XMLUtil.loadDocument(csFileName);
		if (doc != null) 
		{
			spotsDescription.xmlLoadSpotsDescription(doc);
			flag = xmlLoad_Spots_Only_v1(doc);
		}
		return flag;
	}
	
	private boolean xmlLoad_Spots_Only_v1(Document doc) 
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
			Spot spot = new Spot();
			spot.loadFromXML_SpotOnly(nodecapillary);
//			if (spotsDescription.grouping == 2 
//			&& (spot.spotStim != null 
//			&& spot.spotStim.equals(".."))) 
//			{
//				if (spot.getSpotSide().equals("00")) 
//				{
//					spot.spotStim = spotsDescription.stimulusL;
//					spot.spotConc = spotsDescription.concentrationL;
//				} 
//				else 
//				{
//					spot.spotStim = spotsDescription.stimulusR;
//					spot.spotConc = spotsDescription.concentrationR;
//				}
//			}
			if (!isPresent(spot))
				spotsList.add(spot);
		}
		return true;
	}

	// ---------------------------------
	
	public void copy (SpotsArray sourceSpotArray) 
	{
		spotsDescription.copy(sourceSpotArray.spotsDescription);
		spotsList.clear();
		for (Spot sourceSpot: sourceSpotArray.spotsList) 
		{
			Spot spot = new Spot();
			spot.copy(sourceSpot);
			spotsList.add(spot);
		}
	}
	
	public boolean isPresent(Spot capNew) 
	{
		boolean flag = false;
		for (Spot spot: spotsList) 
		{
			if (spot.getRoiName().contentEquals(capNew.getRoiName())) 
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
		for (Spot spot: spotsList) 
			spot.adjustToImageWidth(imageWidth);
	}
	
	public void cropToImageWidth (int imageWidth) 
	{
		for (Spot spot: spotsList) 
			spot.cropToImageWidth(imageWidth);
	}

	public void transferDescriptionToSpots() 
	{
		for (Spot spot: spotsList) 
		{
			transferCapGroupCageIDToSpot(spot);
		}
	}
	
	private void transferCapGroupCageIDToSpot (Spot spot) 
	{
		if (spotsDescription.grouping != 2)
			return;
		String	name = spot.getRoiName();
		String letter = name.substring(name.length() - 1);
		spot.spotCageSide = letter;
		if (letter .equals("R")) 
		{	
			String nameL = name.substring(0, name.length() - 1) + "L";
			Spot spot00 = getSpotFromName(nameL);
			if (spot00 != null) 
			{
//				spot.capNFlies = cap0.capNFlies;
				spot.cageIndex = spot00.cageIndex;
			}
		}
	}
	
	public Spot getSpotFromName(String name) 
	{
		Spot spotFound = null;
		for (Spot spot: spotsList) 
		{
			if (spot.getRoiName().equals(name)) 
			{
				spotFound = spot;
				break;
			}
		}
		return spotFound;
	}
	
	public Spot getSpotContainingName(String name) 
	{
		Spot spotFound = null;
		for (Spot spot: spotsList) 
		{
			if (spot.getRoiName().contains(name)) 
			{
				spotFound = spot;
				break;
			}
		}
		return spotFound;
	}

	public void updateSpotsFromSequence(Sequence seq) 
	{
		List<ROI2DShape> listROISSpot = ROI2DUtilities.getROIs2DAreaContainingString ("spot", seq);
		Collections.sort(listROISSpot, new Comparators.ROI2D_Name_Comparator());
		for (Spot spot: spotsList) 
		{
			spot.valid = false;
			String spotName = spot.getRoiName();
			Iterator <ROI2DShape> iterator = listROISSpot.iterator();
			while(iterator.hasNext()) 
			{ 
				ROI2D roi = iterator.next();
				String roiName = roi.getName();
				if (roiName.equals (spotName) && (roi instanceof ROI2DShape) )
				{
					spot.setRoi((ROI2DShape) roi);
					spot.valid = true;
				}
				if (spot.valid) 
				{
					iterator.remove();
					break;
				}
			}
		}
		Iterator <Spot> iterator = spotsList.iterator();
		while (iterator.hasNext()) 
		{
			Spot spot = iterator.next();
			if (!spot.valid )
				iterator.remove();
		}
		if (listROISSpot.size() > 0) 
		{
			for (ROI2D roi: listROISSpot) 
			{
				Spot spot = new Spot((ROI2DShape) roi);
				if (!isPresent(spot))
					spotsList.add(spot);
			}
		}
		Collections.sort(spotsList);
		return;
	}

	public void transferSpotRoiToSequence(Sequence seq) 
	{
		ROI2DUtilities.removeRoisContainingString(-1, "spot", seq);
		for (Spot spot: spotsList) 
			seq.addROI(spot.getRoi());
	}
	
	public void transferSpotsMeasuresToSequence(Sequence seq) 
	{
		List<ROI2D> seqRoisList = seq.getROI2Ds(false);
		ROI2DUtilities.removeROIsMissingChar(seqRoisList, '_');
		
		List<ROI2D> newRoisList = new ArrayList<ROI2D>();
		int nspots = spotsList.size();
		int height = seq.getHeight();
		for (int i = 0; i < nspots; i++) 
		{
			List<ROI2D> listOfRois = spotsList.get(i).transferMeasuresToROIs(height);
			for (ROI2D roi: listOfRois) {
				if (roi != null) 
					roi.setT(i);
			}
			newRoisList.addAll(listOfRois);
		}
		ROI2DUtilities.mergeROIsListNoDuplicate(seqRoisList, newRoisList, seq);
		seq.removeAllROI();
		seq.addROIs(seqRoisList, false);
	}

	public void initSpotsWith10Cages(int nflies)
	{
		int spotArraySize = spotsList.size();
		for (int i = 0; i < spotArraySize; i++)
		{
			Spot spot = spotsList.get(i);
			spot.spotNFlies = nflies;
			if (i <= 1  || i >= spotArraySize-2 )
				spot.spotNFlies = 0;
			spot.cageIndex = i/2;
		}
	}
	
	public void initSpotsWith6Cages(int nflies) 
	{
		int spotArraySize = spotsList.size();
		for (int i = 0; i < spotArraySize; i++) 
		{
			Spot spot = spotsList.get(i);
			spot.spotNFlies = 1;
			if (i <= 1 ) 
			{
				spot.spotNFlies = 0;
				spot.cageIndex = 0;
			}
			else if (i >= spotArraySize-2 ) 
			{
				spot.spotNFlies = 0;
				spot.cageIndex = 5;
			}
			else 
			{
				spot.spotNFlies = nflies;
				spot.cageIndex = 1 + (i-2)/4;
			}
		}
	}
	
	public void initSpotsWithNFlies(int nflies) 
	{
		int spotArraySize = spotsList.size();
		for (int i = 0; i < spotArraySize; i++) 
		{
			Spot spot = spotsList.get(i);
			spot.spotNFlies = nflies;
		}
	}
	
	// -------------------------------------------------
	
	public double getScalingFactorToPhysicalUnits(EnumXLSExportType xlsoption) 
	{
		double scalingFactorToPhysicalUnits = 1.; 
//		switch (xlsoption) 
//		{
//			case AUTOCORREL:
//			case CROSSCORREL:
//			case CROSSCORREL_LR:
//				scalingFactorToPhysicalUnits = 1.;
//				break;
//			default:
//				scalingFactorToPhysicalUnits = spotsDescription.volume / spotsDescription.pixels;
//				break;
//		}
		return scalingFactorToPhysicalUnits;
	}
	
	public Polygon2D get2DPolygonEnclosingSpots() 
	{
		Rectangle  outerRectangle = null;
		for (Spot spot : spotsList) 
		{
			Rectangle rect = spot.getRoi().getBounds();
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
	
	public void transferSumToSumClean() 
	{
		for (Spot spot: spotsList) {
			spot.transferSumToSumClean();	
		}
	}
	
	public void transferLimitMeasuresToPolyline() 
	{
		for (Spot spot: spotsList) {
			spot.transferToPolyline();	
		}
	}
	
	public KymoIntervals getKymoIntervalsFromSpots() 
	{
		if (spotsListTimeIntervals == null) 
		{
			spotsListTimeIntervals = new KymoIntervals();
			for (Spot spot: spotsList) {
				for (ROI2DAlongTime roiFK: spot.getROIsAlongTime()) {
					Long[] interval = {roiFK.getStart(), (long) -1}; 
					spotsListTimeIntervals.addIfNew(interval);
				}
			}
		}
		return spotsListTimeIntervals;
	}
	
	public int findKymoROI2DIntervalStart(long intervalT) 
	{
		return spotsListTimeIntervals.findStartItem(intervalT);
	}
	
	public long getKymoROI2DIntervalsStartAt(int selectedItem) 
	{
		return spotsListTimeIntervals.get(selectedItem)[0];
	}
	
	public int addKymoROI2DInterval(long start) 
	{
		Long[] interval = {start, (long) -1};
		int item = spotsListTimeIntervals.addIfNew(interval);
		
		for (Spot spot: spotsList) 
		{
			List<ROI2DAlongTime> listROI2DForKymo = spot.getROIsAlongTime();
			ROI2D roi = spot.getRoi();
			if (item>0 ) 
				roi = (ROI2D) listROI2DForKymo.get(item-1).getRoi().getCopy();
			listROI2DForKymo.add(item, new ROI2DAlongTime(start, roi));
		}
		return item;
	}
	
	public void deleteKymoROI2DInterval(long start) 
	{
		spotsListTimeIntervals.deleteIntervalStartingAt(start);
		for (Spot spot: spotsList) 
			spot.removeROI2DIntervalStartingAt(start);
	}
	
	// --------------------------------
	
	final String csvSep = ";";
	
	private boolean csvLoadSpots(String directory, EnumSpotMeasures option) throws Exception 
	{
		String pathToCsv = directory + File.separator + csvFileName;
		File csvFile = new File(pathToCsv);
		if (!csvFile.isFile()) 
			return false;
		
		BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToCsv));
		String row;
		String sep = ";";
		while ((row = bufferedReader.readLine()) != null) 
		{
			if (row.charAt(0) == '#') 
				sep = String.valueOf(row.charAt(1));
			
		    String[] data = row.split(sep);
		    if (data[0] .equals("#")) 
		    {
		    	switch(data[1]) 
		    	{
		    	case "DESCRIPTION":
		    		csvLoadDescription (bufferedReader, sep);
		    		break;
		    	case "SPOTS":
		    		csvLoadSpotsArray (bufferedReader, sep);
		    		break;
		    	case "AREA_SUM":
		    		csvLoadSpotsMeasures(bufferedReader, EnumSpotMeasures.AREA_SUM, sep);
		    		break;
		    	case "AREA_SUMCLEAN":
		    		csvLoadSpotsMeasures(bufferedReader, EnumSpotMeasures.AREA_SUMCLEAN, sep);
		    		break;
		    	case "AREA_FLYPRESENT":
		    		csvLoadSpotsMeasures(bufferedReader, EnumSpotMeasures.AREA_FLYPRESENT, sep);
		    		break;
//		    	case "AREA_CNTPIX":
//		    		csvLoadSpotsMeasures(bufferedReader, EnumSpotMeasures.AREA_CNTPIX, sep);
//		    		break;
	    		default:
	    			break;
		    	}
		    }
		}
		bufferedReader.close();
		
		return true;
	}
	
	private String csvLoadSpotsArray (BufferedReader csvReader, String csvSep) 
	{
		String row;
		try {
			row = csvReader.readLine();
			String[] data0 = row.split(csvSep);
			boolean dummyColumn = data0[0].contains("prefix");
			
			while ((row = csvReader.readLine()) != null) 
			{
				String[] data = row.split(csvSep);
				if (data[0] .equals( "#")) 
					return data[1];
				Spot spot = getSpotFromName(data[dummyColumn? 2: 1]);
				if (spot == null)
					spot = new Spot();
				spot.csvImportDescription(data, dummyColumn);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String csvLoadDescription (BufferedReader csvReader, String csvSep) 
	{
		String row;
		try {
			row = csvReader.readLine();
			row = csvReader.readLine();
			String[] data = row.split(csvSep);
			spotsDescription.csvImportSpotsDescriptionData(data);
			row = csvReader.readLine();
			data = row.split(csvSep);
			if ( data[0].substring(0, Math.min( data[0].length(), 5)).equals("n spot"))
			{
				int nspots = Integer.valueOf(data[1]);
				if (nspots >= spotsList.size())
					spotsList.ensureCapacity(nspots);
				else
					spotsList.subList(nspots, spotsList.size()).clear();
				row = csvReader.readLine();
				data = row.split(csvSep);
			}
			if (data[0] .equals( "#")) {
			  	return data[1];
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String csvLoadSpotsMeasures(BufferedReader csvReader, EnumSpotMeasures measureType, String csvSep ) 
	{
		String row;
		try {
			row = csvReader.readLine();
			boolean y = true;
			boolean x = row.contains("xi");
			while ((row = csvReader.readLine()) != null) {
				String[] data = row.split(csvSep);
				if (data[0] .equals( "#")) 
					return data[1];

				Spot spot = getSpotFromName(data[0]);
				if (spot == null)
					spot = new Spot();
				spot.csvImportMeasures_OneType(measureType, data, x, y);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	// ---------------------------------
	
	private boolean csvSaveSpots(String directory) 
	{
		Path path = Paths.get(directory);
		if (!Files.exists(path))
			return false;
		
		try {
			FileWriter csvWriter = new FileWriter(directory + File.separator + csvFileName);
			
			csvSave_DescriptionSection(csvWriter);			
			csvSave_MeasuresSection(csvWriter, EnumSpotMeasures.AREA_SUM);
			csvSave_MeasuresSection(csvWriter, EnumSpotMeasures.AREA_SUMCLEAN);
			csvSave_MeasuresSection(csvWriter, EnumSpotMeasures.AREA_FLYPRESENT);
//			csvSave_MeasuresSection(csvWriter, EnumSpotMeasures.AREA_CNTPIX);
			csvWriter.flush();
			csvWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private boolean csvSave_DescriptionSection(FileWriter csvWriter) 
	{
		try {
			csvWriter.append(spotsDescription.csvExportSectionHeader(csvSep));
			csvWriter.append(spotsDescription.csvExportExperimentDescriptors(csvSep));
			csvWriter.append("n spots="+csvSep + Integer.toString(spotsList.size()) + "\n");
			csvWriter.append("#"+csvSep+"#\n");
			
			if (spotsList.size() > 0) {
				csvWriter.append(spotsList.get(0).csvExportSpotArrayHeader(csvSep));
				for (Spot spot:spotsList) 
					csvWriter.append(spot.csvExportDescription(csvSep));
				csvWriter.append("#"+csvSep+"#\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private boolean csvSave_MeasuresSection(FileWriter csvWriter, EnumSpotMeasures measureType) 
	{
		try {
			if (spotsList.size() <= 1)
				return false;
			csvWriter.append(spotsList.get(0).csvExportMeasures_SectionHeader(measureType, csvSep));
			for (Spot spot:spotsList) {
				csvWriter.append(spot.csvExportMeasures_OneType(measureType, csvSep));
			}
			csvWriter.append("#"+csvSep+"#\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	

}