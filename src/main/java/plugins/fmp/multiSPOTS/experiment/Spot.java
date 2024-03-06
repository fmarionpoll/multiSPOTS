package plugins.fmp.multiSPOTS.experiment;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;

import icy.roi.BooleanMask2D;
import icy.roi.ROI2D;
import icy.util.XMLUtil;
import plugins.fmp.multiSPOTS.series.BuildSeriesOptions;
import plugins.fmp.multiSPOTS.tools.ROI2DUtilities;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSColumnHeader;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSExportType;





public class Spot implements Comparable <Spot> 
{

	private ROI2D 						roi 			= null;
	private ArrayList<ROI2DAlongTime>	listRoiAlongTime= new ArrayList<ROI2DAlongTime>();
	public BooleanMask2D 				mask2D			= null;

	public int							kymographIndex 	= -1;
	private String						kymographPrefix	= null;
	public String 						version 		= null;

	public String 						stimulus		= new String("..");
	public String 						concentration	= new String("..");
	public String						cageSide			= ".";
	public int							nFlies			= 1;
	public int							cageID			= 0;
	public double 						volume 			= 1;
	public int 							pixels 			= 5;
	public int							radius			= 30;
	public boolean						descriptionOK	= false;
	public int							versionInfos	= 0;
	
	public BuildSeriesOptions 			limitsOptions	= new BuildSeriesOptions();
	 
	public SpotArea						sum  			= new SpotArea("sum"); 
	public SpotArea						sum2  			= new SpotArea("sum2"); 
	public SpotArea						cntPix  		= new SpotArea("cntPix"); 
	public SpotArea						meanGrey		= new SpotArea("meanGrey"); 

	public boolean						valid			= true;

	private final String 				ID_META 		= "metaMC";
	private final String				ID_NFLIES		= "nflies";
	private final String				ID_CAGENB		= "cage_number";
	private final String 				ID_SPOTVOLUME 	= "volume";
	private final String 				ID_PIXELS 		= "pixels";
	private final String 				ID_RADIUS 		= "radius";
	private final String 				ID_STIML 		= "stimulus";
	private final String 				ID_CONCL 		= "concentration";
	private final String 				ID_SIDE 		= "side";
	private final String 				ID_DESCOK 		= "descriptionOK";
	private final String				ID_VERSIONINFOS	= "versionInfos";
	
	private final String 				ID_INTERVALS 	= "INTERVALS";
	private final String				ID_NINTERVALS	= "nintervals";
	private final String 				ID_INTERVAL 	= "interval_";
	
	private final String 				ID_INDEXIMAGE 	= "indexImageMC";

	private final String 				ID_VERSION		= "version"; 
	private final String 				ID_VERSIONNUM	= "1.0.0"; 
	
	// ----------------------------------------------------
	
	public Spot(ROI2D roiCapillary) 
	{
		this.roi = roiCapillary;
	}
	
	Spot(String name) 
	{
	}
	
	public Spot() 
	{
	}

	@Override
	public int compareTo(Spot o) 
	{
		if (o != null)
			return (this.roi.getName()).compareTo(o.roi.getName());
		return 1;
	}
	
	// ------------------------------------------
	
	public void copy(Spot spot) 
	{
		kymographIndex 	= spot.kymographIndex;
		version 		= spot.version;
		roi 			= (ROI2D) spot.roi.getCopy();
		
		stimulus		= spot.stimulus;
		concentration	= spot.concentration;
		cageSide		= spot.cageSide;
		nFlies			= spot.nFlies;
		cageID			= spot.cageID;
		volume 			= spot.volume;
		pixels 			= spot.pixels;
		radius 			= spot.radius;
		
		limitsOptions	= spot.limitsOptions;
		
		sum .copy(spot.sum);
		sum2 .copy(spot.sum2);
		cntPix .copy(spot.cntPix);	
		meanGrey .copy(spot.meanGrey);	
	}
	
	public ROI2D getRoi() 
	{
		return roi;
	}
	
	public void setRoi(ROI2D roi) 
	{
		this.roi = roi;
	}
	
	public void setRoiName(String name) 
	{
		roi.setName(name);
	}
	
	public String getRoiName() 
	{
		return roi.getName();
	}
	
	public String getLast2ofSpotName() 
	{
		if (roi == null)
			return "missing";
		return roi.getName().substring(roi.getName().length() -2);
	}
	
	public String getRoiNamePrefix() 
	{
		return kymographPrefix;
	}
	
 	public String getSpotSide() 
	{
		return roi.getName().substring(roi.getName().length() -2);
	}
	
	public static String replace_LR_with_12(String name) 
	{
		String newname = name;
		if (name .contains("R"))
			newname = name.replace("R", "2");
		else if (name.contains("L"))
			newname = name.replace("L", "1");
		return newname;
	}
	
	public int getCageIndexFromRoiName() 
	{
		String name = roi.getName();
		if (!name .contains("spot"))
			return -1;
		return Integer.valueOf(name.substring(5, 6));
	}
	
	public String getSideDescriptor(EnumXLSExportType xlsExportOption) 
	{
		String value = null;
		cageSide = getSpotSide();
		switch (xlsExportOption) 
		{
		case DISTANCE:
		case ISALIVE:
			value = cageSide + "(L=R)";
			break;
		case TOPLEVELDELTA_LR:
		case TOPLEVEL_LR:
			if (cageSide.equals("00"))
				value = "sum";
			else
				value = "PI";
			break;
		case XYIMAGE:
		case XYTOPCAGE:
		case XYTIPCAPS:
			if (cageSide .equals ("00"))
				value = "x";
			else
				value = "y";
			break;
		default:
			value = cageSide;
			break;
		}
		return value;
	}
	
	public String getSpotField(EnumXLSColumnHeader fieldEnumCode)
	{
		String stringValue = null;
		switch(fieldEnumCode) 
		{
		case CAP_STIM:
			stringValue = stimulus;
			break;
		case CAP_CONC:
			stringValue = concentration;
			break;
		default:
			break;
		}
		return stringValue;
	}
	
	public void setSpotField(EnumXLSColumnHeader fieldEnumCode, String stringValue)
	{
		switch(fieldEnumCode) 
		{
		case CAP_STIM:
			stimulus = stringValue;
			break;
		case CAP_CONC:
			concentration = stringValue;
			break;
		default:
			break;
		}
	}
	
	// -----------------------------------------
	
	public boolean isThereAnyMeasuresDone(EnumXLSExportType option) 
	{
		SpotArea spotArea = getSpotArea(option);
		if (spotArea != null)
			return spotArea.isThereAnyMeasuresDone();
		return false;
	}
		
	public ArrayList<Integer> getSpotMeasuresForXLSPass1(EnumXLSExportType option, long seriesBinMs, long outputBinMs) 
	{
		SpotArea spotArea = getSpotArea(option);
		if (spotArea != null)
			return spotArea.getMeasures(seriesBinMs, outputBinMs);
		return null;
	}

	public void cropMeasuresToNPoints (int npoints) 
	{
		cropSpotAreaToNPoints(sum , npoints);
		cropSpotAreaToNPoints(sum2 , npoints);
		cropSpotAreaToNPoints(cntPix , npoints);
		cropSpotAreaToNPoints(meanGrey , npoints);
	}
	
	private void cropSpotAreaToNPoints(SpotArea spotArea, int npoints) 
	{
		if (spotArea.polylineLevel != null)
			spotArea.cropToNPoints(npoints);
	}
	
	public void restoreClippedMeasures () 
	{
		restoreSpotAreaClippedMeasures( sum );
		restoreSpotAreaClippedMeasures( sum2 );
		restoreSpotAreaClippedMeasures( cntPix );
		restoreSpotAreaClippedMeasures( meanGrey );
	}
	
	private void restoreSpotAreaClippedMeasures(SpotArea spotArea)
	{
		if (spotArea.polylineLevel != null)
			spotArea.restoreNPoints();
	}
	
	public void setGulpsOptions (BuildSeriesOptions options) 
	{
		limitsOptions = options;
	}
	
	public BuildSeriesOptions getGulpsOptions () 
	{
		return limitsOptions;
	}
	
	public void computeSpotAreaMeanGrey(int cntPixel) 
	{
		int nFrames = sum.measure.length;
		for (int i = 0; i < nFrames; i++)
		{
			meanGrey.measure[i] = sum.measure[i]/cntPixel;
		}
	}
	
	public void computeSpotAreaSum2() 
	{
		int nFrames = sum.measure.length;
		for (int i = 0; i < nFrames; i++)
		{
			double value = sum.measure[i];
			sum2.measure[i] = value*value;
		}
	}
	
	private SpotArea getSpotArea (EnumXLSExportType option)
	{
		switch (option) 
		{
		case AREA_SUM:		
		case AREA_SUM_LR:
			return sum;
		case AREA_SUM2:	
//		case AREA_SUM2_LR:
			return sum2;
		case AREA_CNTPIX:
		case AREA_CNTPIX_LR:
			return cntPix;
		case AREA_MEANGREY:
		case AREA_MEANGREY_LR:
			return meanGrey;
		default:
			return null;
		}
	}
	
	public int getLastMeasure(EnumXLSExportType option) 
	{
		SpotArea spotArea = getSpotArea(option);
		if (spotArea != null)
			return spotArea.getLastMeasure();	
		return 0;
	}
	
	public int getLastDeltaMeasure(EnumXLSExportType option) 
	{
		SpotArea spotArea = getSpotArea(option);
		if (spotArea != null)
			return spotArea.getLastDeltaMeasure();
		return 0;
	}
	
	public int getT0Measure(EnumXLSExportType option) 
	{
		SpotArea spotArea = getSpotArea(option);
		if (spotArea != null)
			return spotArea.getT0Measure();
		return 0;
	}
	
	// -----------------------------------------------------------------------------

	public boolean loadFromXML_SpotOnly(Node node) 
	{
	    final Node nodeMeta = XMLUtil.getElement(node, ID_META);
	    boolean flag = (nodeMeta != null); 
	    if (flag) 
	    {
	    	version 		= XMLUtil.getElementValue(nodeMeta, ID_VERSION, "0.0.0");
	    	kymographIndex 	= XMLUtil.getElementIntValue(nodeMeta, ID_INDEXIMAGE, kymographIndex);
     
	        descriptionOK 	= XMLUtil.getElementBooleanValue(nodeMeta, ID_DESCOK, false);
	        versionInfos 	= XMLUtil.getElementIntValue(nodeMeta, ID_VERSIONINFOS, 0);
	        nFlies 			= XMLUtil.getElementIntValue(nodeMeta, ID_NFLIES, nFlies);
	        cageID 			= XMLUtil.getElementIntValue(nodeMeta, ID_CAGENB, cageID);
	        volume 			= XMLUtil.getElementDoubleValue(nodeMeta, ID_SPOTVOLUME, Double.NaN);
			pixels 			= XMLUtil.getElementIntValue(nodeMeta, ID_PIXELS, 5);
			radius			= XMLUtil.getElementIntValue(nodeMeta, ID_RADIUS, 30);
			stimulus 		= XMLUtil.getElementValue(nodeMeta, ID_STIML, ID_STIML);
			concentration	= XMLUtil.getElementValue(nodeMeta, ID_CONCL, ID_CONCL);
			cageSide 		= XMLUtil.getElementValue(nodeMeta, ID_SIDE, ".");
			
	        roi = ROI2DUtilities.loadFromXML_ROI(nodeMeta);
	        limitsOptions.loadFromXML(nodeMeta);
	        
	        loadFromXML_intervals(node);
	    }
	    return flag;
	}
	
	private boolean loadFromXML_intervals(Node node) 
	{
		listRoiAlongTime.clear();
		final Node nodeMeta2 = XMLUtil.getElement(node, ID_INTERVALS);
	    if (nodeMeta2 == null)
	    	return false;
	    int nitems = XMLUtil.getElementIntValue(nodeMeta2, ID_NINTERVALS, 0);
		if (nitems > 0) {
        	for (int i=0; i < nitems; i++) {
        		Node node_i = XMLUtil.setElement(nodeMeta2, ID_INTERVAL+i);
        		ROI2DAlongTime roiInterval = new ROI2DAlongTime();
        		roiInterval.loadFromXML(node_i);
        		listRoiAlongTime.add(roiInterval);
        		
        		if (i == 0) {
        			roi = listRoiAlongTime.get(0).getRoi();
        		}
        	}
        }
        return true;
	}
	
	// -----------------------------------------------------------------------------

	public boolean saveToXML_SpotOnly(Node node) 
	{
	    final Node nodeMeta = XMLUtil.setElement(node, ID_META);
	    if (nodeMeta == null)
	    	return false;
    	if (version == null)
    		version = ID_VERSIONNUM;
    	XMLUtil.setElementValue(nodeMeta, ID_VERSION, version);
        XMLUtil.setElementIntValue(nodeMeta, ID_INDEXIMAGE, kymographIndex);

        XMLUtil.setElementBooleanValue(nodeMeta, ID_DESCOK, descriptionOK);
        XMLUtil.setElementIntValue(nodeMeta, ID_VERSIONINFOS, versionInfos);
        XMLUtil.setElementIntValue(nodeMeta, ID_NFLIES, nFlies);
        XMLUtil.setElementIntValue(nodeMeta, ID_CAGENB, cageID);
		XMLUtil.setElementDoubleValue(nodeMeta, ID_SPOTVOLUME, volume);
		XMLUtil.setElementIntValue(nodeMeta, ID_PIXELS, pixels);
		XMLUtil.setElementIntValue(nodeMeta, ID_RADIUS, radius);
		XMLUtil.setElementValue(nodeMeta, ID_STIML, stimulus);
		XMLUtil.setElementValue(nodeMeta, ID_SIDE, cageSide);
		XMLUtil.setElementValue(nodeMeta, ID_CONCL, concentration);

		ROI2DUtilities.saveToXML_ROI(nodeMeta, roi); 
		
		boolean flag = saveToXML_intervals(node);
	    return flag;
	}
	
	private boolean saveToXML_intervals(Node node) 
	{
		final Node nodeMeta2 = XMLUtil.setElement(node, ID_INTERVALS);
	    if (nodeMeta2 == null)
	    	return false;
		int nitems = listRoiAlongTime.size();
		XMLUtil.setElementIntValue(nodeMeta2, ID_NINTERVALS, nitems);
        if (nitems > 0) {
        	for (int i=0; i < nitems; i++) {
        		Node node_i = XMLUtil.setElement(nodeMeta2, ID_INTERVAL+i);
        		listRoiAlongTime.get(i).saveToXML(node_i);
        	}
        }
        return true;
	}
	
	// --------------------------------------------
	
	public List<ROI2DAlongTime> getROIsForKymo() 
	{
		if (listRoiAlongTime.size() < 1) 
			initROI2DForKymoList();
		return listRoiAlongTime;
	}
	
 	public ROI2DAlongTime getROI2DKymoAt(int i) 
 	{
		if (listRoiAlongTime.size() < 1) 
			initROI2DForKymoList();
		return listRoiAlongTime.get(i);
	}
 	
 	public ROI2DAlongTime getROI2DKymoAtIntervalT(long t) 
 	{
		if (listRoiAlongTime.size() < 1) 
			initROI2DForKymoList();
		
		ROI2DAlongTime capRoi = null;
		for (ROI2DAlongTime item : listRoiAlongTime) {
			if (t < item.getStart())
				break;
			capRoi = item;
		}
		return capRoi;
	}
 	
 	public void removeROI2DIntervalStartingAt(long start) 
 	{
 		ROI2DAlongTime itemFound = null;
 		for (ROI2DAlongTime item : listRoiAlongTime) {
			if (start != item.getStart())
				continue;
			itemFound = item;
		}
 		if (itemFound != null)
 			listRoiAlongTime.remove(itemFound);
	}
	
	private void initROI2DForKymoList() 
	{ 
		listRoiAlongTime.add(new ROI2DAlongTime(0, roi));		
	}
	
	public void setVolumeAndPixels(double volume, int pixels) 
	{
		this.volume = volume;
		this.pixels = pixels;
		descriptionOK = true;
	}
	
	public void adjustToImageWidth (int imageWidth) 
	{
		sum.adjustToImageWidth(imageWidth);
		sum2.adjustToImageWidth(imageWidth);
		cntPix.adjustToImageWidth(imageWidth);
		meanGrey.adjustToImageWidth(imageWidth);
	}

	public void cropToImageWidth (int imageWidth) 
	{
		sum.cropToImageWidth(imageWidth);
		sum2.cropToImageWidth(imageWidth);
		cntPix.cropToImageWidth(imageWidth);
		meanGrey.cropToImageWidth(imageWidth);
	}
	
	public void transferLimitMeasuresToPolyline() 
	{
		sum.setPolylineLevelFromTempData(getRoi().getName(), kymographIndex);
		sum2.setPolylineLevelFromTempData(getRoi().getName(), kymographIndex);
		cntPix.setPolylineLevelFromTempData(getRoi().getName(), kymographIndex);
		meanGrey.setPolylineLevelFromTempData(getRoi().getName(), kymographIndex);
	}
	
	// -----------------------------------------------------------------------------
	
	public String csvExportSpotArrayHeader() 
	{
		StringBuffer sbf = new StringBuffer();
		
		sbf.append("#,SPOTS,describe each spot\n");
		List<String> row2 = Arrays.asList(
				"prefix",
				"kymoIndex", 
				"name", 
				"cage",
				"nflies",
				"volume", 
				"npixel", 
				"radius",
				"stim", 
				"conc", 
				"side");
		sbf.append(String.join(",", row2));
		sbf.append("\n");
		return sbf.toString();
	}
	
	public String csvExportDescription() 
	{	
		StringBuffer sbf = new StringBuffer();
		if (kymographPrefix == null)
			kymographPrefix = getLast2ofSpotName();
		
		List<String> row = Arrays.asList(
				kymographPrefix,
				Integer.toString(kymographIndex), 
				getRoi().getName(), 
				Integer.toString(cageID),
				Integer.toString(nFlies),
				Double.toString(volume), 
				Integer.toString(pixels), 
				Integer.toString(radius),
				stimulus, 
				concentration, 
				cageSide);
		sbf.append(String.join(",", row));
		sbf.append("\n");
		return sbf.toString();
	}
	
	public String csvExportMeasures_SectionHeader(EnumSpotMeasures measureType) 
	{
		StringBuffer sbf = new StringBuffer();
		String explanation1 = "\n name,index, npts, y0, y1, etc\n";
		switch(measureType) 
		{
			case AREA_SUM:
			case AREA_SUM2:
			case AREA_CNTPIX:	
			case AREA_MEANGREY:
				sbf.append("#,"+measureType.toString()+"," + explanation1);
				break;

			default:
				sbf.append("#,UNDEFINED,------------\n");
				break;
		}
		return sbf.toString();
	}
	
	public String csvExportMeasures_OneType(EnumSpotMeasures measureType) 
	{
		StringBuffer sbf = new StringBuffer();
		sbf.append(roi.getName()+ ","+ kymographIndex +",");
		
		switch(measureType) 
		{
			case AREA_SUM:  	sum.cvsExportYDataToRow(sbf); break;
			case AREA_SUM2:  	sum2.cvsExportYDataToRow(sbf); break;
			case AREA_CNTPIX:  	cntPix.cvsExportYDataToRow(sbf); break;
			case AREA_MEANGREY: meanGrey.cvsExportYDataToRow(sbf); break;
			default:
				break;
		}
		sbf.append("\n");
		return sbf.toString();
	}
	
	// --------------------------------------------
	
	public void csvImportDescription(String[] data) 
	{
		int i = 0;
		kymographPrefix = data[i]; i++;
		kymographIndex = Integer.valueOf(data[i]); i++; 
		roi.setName(data[i]); i++;
		cageID = Integer.valueOf(data[i]); i++;
		nFlies = Integer.valueOf(data[i]); i++;
		volume = Double.valueOf(data[i]); i++; 
		pixels = Integer.valueOf(data[i]); i++; 
		radius = Integer.valueOf(data[i]); i++;
		stimulus = data[i]; i++; 
		concentration = data[i]; i++; 
		cageSide = data[i]; 
	}
		
	public void csvImportMeasures_OneType(EnumSpotMeasures measureType, String[] data, boolean x, boolean y) 
	{
		if (x && y) 
		{
			switch(measureType) 
			{
			case AREA_SUM:  	sum.csvImportXYDataFromRow( data, 2); break;
			case AREA_SUM2:  	sum2.csvImportXYDataFromRow( data, 2); break;
			case AREA_CNTPIX:  	cntPix.csvImportXYDataFromRow( data, 2); break;
			case AREA_MEANGREY: meanGrey.csvImportXYDataFromRow( data, 2); break;
			default:
				break;
			}
		}
		else if (!x && y) 
		{
			switch(measureType) 
			{
			case AREA_SUM:  	sum.csvImportYDataFromRow( data, 2); break;
			case AREA_SUM2:  	sum2.csvImportYDataFromRow( data, 2); break;
			case AREA_CNTPIX:  	cntPix.csvImportYDataFromRow( data, 2); break;
			case AREA_MEANGREY: meanGrey.csvImportYDataFromRow( data, 2); break;
			default:
				break;
			}
		}
	}
		
}
