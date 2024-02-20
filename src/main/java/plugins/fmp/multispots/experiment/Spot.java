package plugins.fmp.multispots.experiment;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;

import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.type.geom.Polyline2D;
import icy.util.XMLUtil;

import plugins.kernel.roi.roi2d.ROI2DLine;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;

import plugins.fmp.multispots.series.BuildSeriesOptions;
import plugins.fmp.multispots.tools.ROI2DUtilities;
import plugins.fmp.multispots.tools.toExcel.EnumXLSColumnHeader;
import plugins.fmp.multispots.tools.toExcel.EnumXLSExportType;




public class Spot implements Comparable <Spot> 
{

	private ROI2D 						roi 			= null;
	private ArrayList<KymoROI2D>		roisForKymo 	= new ArrayList<KymoROI2D>();
	public BooleanMask2D 				spotMask2D		= null;

	public int							kymographIndex 	= -1;
	private String						kymographPrefix	= null;
	
	public String 						version 		= null;
	public String						filenameTIFF	= null;
	
	public ArrayList<int[]> 			spot_Integer	= null;
	
	public String 						spotStimulus	= new String("..");
	public String 						spotConcentration= new String("..");
	public String						spotSide			= ".";
	public int							spotNFlies		= 1;
	public int							spotCageID		= 0;
	public double 						spotVolume 		= 5.;
	public int 							spotPixels 		= 5;
	public boolean						descriptionOK	= false;
	public int							versionInfos	= 0;
	
	public BuildSeriesOptions 			limitsOptions	= new BuildSeriesOptions();
	
	public  final String 				ID_AREAPIXELS 	= "areaNPixels";	
	public SpotArea						areaNPixels  	= new SpotArea(ID_AREAPIXELS); 

	
	public boolean						valid			= true;

	private final String 				ID_META 		= "metaMC";
	private final String				ID_NFLIES		= "nflies";
	private final String				ID_CAGENB		= "cage_number";
	private final String 				ID_CAPVOLUME 	= "capillaryVolume";
	private final String 				ID_CAPPIXELS 	= "capillaryPixels";
	private final String 				ID_STIML 		= "stimulus";
	private final String 				ID_CONCL 		= "concentration";
	private final String 				ID_SIDE 		= "side";
	private final String 				ID_DESCOK 		= "descriptionOK";
	private final String				ID_VERSIONINFOS	= "versionInfos";
	
	private final String 				ID_INTERVALS 	= "INTERVALS";
	private final String				ID_NINTERVALS	= "nintervals";
	private final String 				ID_INTERVAL 	= "interval_";
	
	private final String 				ID_INDEXIMAGE 	= "indexImageMC";
	private final String 				ID_NAMETIFF 	= "filenameTIFF";
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
		filenameTIFF	= spot.filenameTIFF;
		
		spotStimulus	= spot.spotStimulus;
		spotConcentration= spot.spotConcentration;
		spotSide		= spot.spotSide;
		spotNFlies		= spot.spotNFlies;
		spotCageID		= spot.spotCageID;
		spotVolume 		= spot.spotVolume;
		spotPixels 		= spot.spotPixels;
		
		limitsOptions	= spot.limitsOptions;
		
		areaNPixels.copy(spot.areaNPixels); 
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
		return roi.getName().substring(roi.getName().length() -1);
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
		return Integer.valueOf(name.substring(4, 5));
	}
	
	public String getSideDescriptor(EnumXLSExportType xlsExportOption) 
	{
		String value = null;
		spotSide = getSpotSide();
		switch (xlsExportOption) 
		{
		case DISTANCE:
		case ISALIVE:
			value = spotSide + "(L=R)";
			break;
		case SUMGULPS_LR:
		case TOPLEVELDELTA_LR:
		case TOPLEVEL_LR:
			if (spotSide.equals("L"))
				value = "sum";
			else
				value = "PI";
			break;
		case XYIMAGE:
		case XYTOPCAGE:
		case XYTIPCAPS:
			if (spotSide .equals ("L"))
				value = "x";
			else
				value = "y";
			break;
		default:
			value = spotSide;
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
			stringValue = spotStimulus;
			break;
		case CAP_CONC:
			stringValue = spotConcentration;
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
			spotStimulus = stringValue;
			break;
		case CAP_CONC:
			spotConcentration = stringValue;
			break;
		default:
			break;
		}
	}
	
	// -----------------------------------------
	
	public boolean isThereAnyMeasuresDone(EnumXLSExportType option) 
	{
		boolean yes = false;
		switch (option) 
		{
		case TOPLEVEL:
		default:
			yes= areaNPixels.isThereAnyMeasuresDone();
			break;
		}
		return yes;
	}
		
	public ArrayList<Integer> getSpotMeasuresForXLSPass1(EnumXLSExportType option, long seriesBinMs, long outputBinMs) 
	{
		ArrayList<Integer> datai = null;
		switch (option) 
		{
		case TOPLEVEL:
		case TOPRAW:
		case TOPLEVEL_LR:
		case TOPLEVELDELTA:
		case TOPLEVELDELTA_LR:
			default:
			datai = areaNPixels.getMeasures(seriesBinMs, outputBinMs);
			break;
		}
		return datai;
	}
		
	public void cropMeasuresToNPoints (int npoints) 
	{
		if (areaNPixels.polylineLevel != null)
			areaNPixels.cropToNPoints(npoints);
	}
	
	public void restoreClippedMeasures () 
	{
		if (areaNPixels.polylineLevel != null)
			areaNPixels.restoreNPoints();
	}
	
	public void setGulpsOptions (BuildSeriesOptions options) 
	{
		limitsOptions = options;
	}
	
	public BuildSeriesOptions getGulpsOptions () 
	{
		return limitsOptions;
	}
	
	public int getLastMeasure(EnumXLSExportType option) 
	{
		int lastMeasure = 0;
		switch (option) 
		{
		case TOPLEVEL:
		default:
			lastMeasure = areaNPixels.getLastMeasure();
			break;
		}
		return lastMeasure;
	}
	
	public int getLastDeltaMeasure(EnumXLSExportType option) 
	{
		int lastMeasure = 0;
		switch (option) 
		{
		case TOPLEVEL:
		default:
			lastMeasure = areaNPixels.getLastDeltaMeasure();
			break;
		}
		return lastMeasure;
	}
	
	public int getT0Measure(EnumXLSExportType option) 
	{
		int t0Measure = 0;
		switch (option) 
		{
		case TOPLEVEL:
		default:
			t0Measure = areaNPixels.getT0Measure();
			break;
		}
		return t0Measure;
	}

	public List<ROI2D> transferMeasuresToROIs() 
	{
		List<ROI2D> listrois = new ArrayList<ROI2D> ();
		getROIFromCapillaryLevel(areaNPixels, listrois);
		return listrois;
	}
	
	private void getROIFromCapillaryLevel(SpotArea capLevel, List<ROI2D> listrois) 
	{
		if (capLevel.polylineLevel == null || capLevel.polylineLevel.npoints == 0)
			return;
		
		ROI2D roi = new ROI2DPolyLine(capLevel.polylineLevel);
		String name = kymographPrefix + "_" + capLevel.capName;
		roi.setName(name);
		roi.setT(kymographIndex);
		listrois.add( roi);
	}
	
	public void transferROIsToMeasures(List<ROI> listRois) 
	{
		areaNPixels.transferROIsToMeasures(listRois);
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
	        filenameTIFF 	= XMLUtil.getElementValue(nodeMeta, ID_NAMETIFF, filenameTIFF);	        
	        descriptionOK 	= XMLUtil.getElementBooleanValue(nodeMeta, ID_DESCOK, false);
	        versionInfos 	= XMLUtil.getElementIntValue(nodeMeta, ID_VERSIONINFOS, 0);
	        spotNFlies 		= XMLUtil.getElementIntValue(nodeMeta, ID_NFLIES, spotNFlies);
	        spotCageID 		= XMLUtil.getElementIntValue(nodeMeta, ID_CAGENB, spotCageID);
	        spotVolume 		= XMLUtil.getElementDoubleValue(nodeMeta, ID_CAPVOLUME, Double.NaN);
			spotPixels 		= XMLUtil.getElementIntValue(nodeMeta, ID_CAPPIXELS, 5);
			spotStimulus 	= XMLUtil.getElementValue(nodeMeta, ID_STIML, ID_STIML);
			spotConcentration= XMLUtil.getElementValue(nodeMeta, ID_CONCL, ID_CONCL);
			spotSide 		= XMLUtil.getElementValue(nodeMeta, ID_SIDE, ".");
			
	        roi = ROI2DUtilities.loadFromXML_ROI(nodeMeta);
	        limitsOptions.loadFromXML(nodeMeta);
	        
	        loadFromXML_intervals(node);
	    }
	    return flag;
	}
	
	private boolean loadFromXML_intervals(Node node) 
	{
		roisForKymo.clear();
		final Node nodeMeta2 = XMLUtil.getElement(node, ID_INTERVALS);
	    if (nodeMeta2 == null)
	    	return false;
	    int nitems = XMLUtil.getElementIntValue(nodeMeta2, ID_NINTERVALS, 0);
		if (nitems > 0) {
        	for (int i=0; i < nitems; i++) {
        		Node node_i = XMLUtil.setElement(nodeMeta2, ID_INTERVAL+i);
        		KymoROI2D roiInterval = new KymoROI2D();
        		roiInterval.loadFromXML(node_i);
        		roisForKymo.add(roiInterval);
        		
        		if (i == 0) {
        			roi = roisForKymo.get(0).getRoi();
        		}
        	}
        }
        return true;
	}
	
	public boolean loadFromXML_MeasuresOnly(Node node) 
	{
		String header = getLast2ofSpotName()+"_";
		boolean result = areaNPixels.loadCapillaryLimitFromXML(node, ID_AREAPIXELS, header) > 0;
		return result;
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
        if (filenameTIFF != null ) {
        	String filename = Paths.get(filenameTIFF).getFileName().toString();
        	XMLUtil.setElementValue(nodeMeta, ID_NAMETIFF, filename);
        }
        XMLUtil.setElementBooleanValue(nodeMeta, ID_DESCOK, descriptionOK);
        XMLUtil.setElementIntValue(nodeMeta, ID_VERSIONINFOS, versionInfos);
        XMLUtil.setElementIntValue(nodeMeta, ID_NFLIES, spotNFlies);
        XMLUtil.setElementIntValue(nodeMeta, ID_CAGENB, spotCageID);
		XMLUtil.setElementDoubleValue(nodeMeta, ID_CAPVOLUME, spotVolume);
		XMLUtil.setElementIntValue(nodeMeta, ID_CAPPIXELS, spotPixels);
		XMLUtil.setElementValue(nodeMeta, ID_STIML, spotStimulus);
		XMLUtil.setElementValue(nodeMeta, ID_SIDE, spotSide);
		XMLUtil.setElementValue(nodeMeta, ID_CONCL, spotConcentration);

		ROI2DUtilities.saveToXML_ROI(nodeMeta, roi); 
		
		boolean flag = saveToXML_intervals(node);
	    return flag;
	}
	
	private boolean saveToXML_intervals(Node node) 
	{
		final Node nodeMeta2 = XMLUtil.setElement(node, ID_INTERVALS);
	    if (nodeMeta2 == null)
	    	return false;
		int nitems = roisForKymo.size();
		XMLUtil.setElementIntValue(nodeMeta2, ID_NINTERVALS, nitems);
        if (nitems > 0) {
        	for (int i=0; i < nitems; i++) {
        		Node node_i = XMLUtil.setElement(nodeMeta2, ID_INTERVAL+i);
        		roisForKymo.get(i).saveToXML(node_i);
        	}
        }
        return true;
	}
	
	// -------------------------------------------
	
	public Point2D getCapillaryTipWithinROI2D (ROI2D roi2D) 
	{
		Point2D pt = null;		
		if (roi instanceof ROI2DPolyLine) 
		{
			Polyline2D line = (( ROI2DPolyLine) roi).getPolyline2D();
			int last = line.npoints - 1;
			if (roi2D.contains(line.xpoints[0],  line.ypoints[0]))
				pt = new Point2D.Double(line.xpoints[0],  line.ypoints[0]);
			else if (roi2D.contains(line.xpoints[last],  line.ypoints[last])) 
				pt = new Point2D.Double(line.xpoints[last],  line.ypoints[last]);
		} 
		else if (roi instanceof ROI2DLine) 
		{
			Line2D line = (( ROI2DLine) roi).getLine();
			if (roi2D.contains(line.getP1()))
				pt = line.getP1();
			else if (roi2D.contains(line.getP2())) 
				pt = line.getP2();
		}
		return pt;
	}
	
	public Point2D getCapillaryROILowestPoint () 
	{
		Point2D pt = null;		
		if (roi instanceof ROI2DPolyLine) 
		{
			Polyline2D line = ((ROI2DPolyLine) roi).getPolyline2D();
			int last = line.npoints - 1;
			if (line.ypoints[0] > line.ypoints[last])
				pt = new Point2D.Double(line.xpoints[0],  line.ypoints[0]);
			else  
				pt = new Point2D.Double(line.xpoints[last],  line.ypoints[last]);
		} 
		else if (roi instanceof ROI2DLine) 
		{
			Line2D line = ((ROI2DLine) roi).getLine();
			if (line.getP1().getY() > line.getP2().getY())
				pt = line.getP1();
			else
				pt = line.getP2();
		}
		return pt;
	}
	
	public Point2D getCapillaryROIFirstPoint () 
	{
		Point2D pt = null;		
		if (roi instanceof ROI2DPolyLine) 
		{
			Polyline2D line = ((ROI2DPolyLine) roi).getPolyline2D();
			pt = new Point2D.Double(line.xpoints[0],  line.ypoints[0]);
		} 
		else if (roi instanceof ROI2DLine) 
		{
			Line2D line = ((ROI2DLine) roi).getLine();
			pt = line.getP1();
		}
		return pt;
	}
	
	public Point2D getCapillaryROILastPoint () 
	{
		Point2D pt = null;		
		if (roi instanceof ROI2DPolyLine) 
		{
			Polyline2D line = ((ROI2DPolyLine) roi).getPolyline2D();
			int last = line.npoints - 1;
			pt = new Point2D.Double(line.xpoints[last],  line.ypoints[last]);
		} 
		else if (roi instanceof ROI2DLine) 
		{
			Line2D line = ((ROI2DLine) roi).getLine();
			pt = line.getP2();
		}
		return pt;
	}
	
	public int getCapillaryROILength () 
	{
		Point2D pt1 = getCapillaryROIFirstPoint();
		Point2D pt2 = getCapillaryROILastPoint();
		double npixels = Math.sqrt(
				(pt2.getY() - pt1.getY()) * (pt2.getY() - pt1.getY()) 
				+ (pt2.getX() - pt1.getX()) * (pt2.getX() - pt1.getX()));
		return (int) npixels;
	}
	
	// --------------------------------------------
	
	public List<KymoROI2D> getROIsForKymo() 
	{
		if (roisForKymo.size() < 1) 
			initROI2DForKymoList();
		return roisForKymo;
	}
	
 	public KymoROI2D getROI2DKymoAt(int i) 
 	{
		if (roisForKymo.size() < 1) 
			initROI2DForKymoList();
		return roisForKymo.get(i);
	}
 	
 	public KymoROI2D getROI2DKymoAtIntervalT(long t) 
 	{
		if (roisForKymo.size() < 1) 
			initROI2DForKymoList();
		
		KymoROI2D capRoi = null;
		for (KymoROI2D item : roisForKymo) {
			if (t < item.getStart())
				break;
			capRoi = item;
		}
		return capRoi;
	}
 	
 	public void removeROI2DIntervalStartingAt(long start) 
 	{
 		KymoROI2D itemFound = null;
 		for (KymoROI2D item : roisForKymo) {
			if (start != item.getStart())
				continue;
			itemFound = item;
		}
 		if (itemFound != null)
 			roisForKymo.remove(itemFound);
	}
	
	private void initROI2DForKymoList() 
	{ 
		roisForKymo.add(new KymoROI2D(0, roi));		
	}
	
	public void setVolumeAndPixels(double volume, int pixels) 
	{
		spotVolume = volume;
		spotPixels = pixels;
		descriptionOK = true;
	}
	
	public void adjustToImageWidth (int imageWidth) 
	{
		areaNPixels.adjustToImageWidth(imageWidth);
	}

	public void cropToImageWidth (int imageWidth) 
	{
		areaNPixels.cropToImageWidth(imageWidth);
	}
	
	// -----------------------------------------------------------------------------
	
	public String csvExportCapillarySubSectionHeader() 
	{
		StringBuffer sbf = new StringBuffer();
		
		sbf.append("#,CAPILLARIES,describe each capillary\n");
		List<String> row2 = Arrays.asList(
				"cap_prefix",
				"kymoIndex", 
				"kymographName", 
				"kymoFile", 
				"cap_cage",
				"cap_nflies",
				"cap_volume", 
				"cap_npixel", 
				"cap_stim", 
				"cap_conc", 
				"cap_side");
		sbf.append(String.join(",", row2));
		sbf.append("\n");
		return sbf.toString();
	}
	
	public String csvExportCapillaryDescription() 
	{	
		StringBuffer sbf = new StringBuffer();
		if (kymographPrefix == null)
			kymographPrefix = getLast2ofSpotName();
		
		List<String> row = Arrays.asList(
				kymographPrefix,
				Integer.toString(kymographIndex), 
				filenameTIFF, 
				Integer.toString(spotCageID),
				Integer.toString(spotNFlies),
				Double.toString(spotVolume), 
				Integer.toString(spotPixels), 
				spotStimulus, 
				spotConcentration, 
				spotSide);
		sbf.append(String.join(",", row));
		sbf.append("\n");
		return sbf.toString();
	}
	
	public String csvExportMeasureSectionHeader(EnumSpotMeasures measureType) 
	{
		StringBuffer sbf = new StringBuffer();
		String explanation1 = "columns=,name,index, npts,..,.(xi;yi)\n";
		switch(measureType) {
			case TOPLEVEL:
				sbf.append("#,TOPLEVEL," + explanation1);
				break;

			default:
				sbf.append("#,UNDEFINED,------------\n");
				break;
		}
		return sbf.toString();
	}
	
	public String csvExportSpotData(EnumSpotMeasures measureType) 
	{
		StringBuffer sbf = new StringBuffer();
		sbf.append(kymographPrefix+ ","+ kymographIndex +",");
		
		switch(measureType) {
			case AREANPIXELS:
				areaNPixels.cvsExportDataToRow(sbf);
				break;
			default:
				break;
		}
		sbf.append("\n");
		return sbf.toString();
	}
	
	// --------------------------------------------
	
	public void csvImportSpotDescription(String[] data) 
	{
		int i = 0;
		kymographPrefix = data[i]; i++;
		kymographIndex = Integer.valueOf(data[i]); i++; 
		filenameTIFF = data[i]; i++; 
		spotCageID = Integer.valueOf(data[i]); i++;
		spotNFlies = Integer.valueOf(data[i]); i++;
		spotVolume = Double.valueOf(data[i]); i++; 
		spotPixels = Integer.valueOf(data[i]); i++; 
		spotStimulus = data[i]; i++; 
		spotConcentration = data[i]; i++; 
		spotSide = data[i]; 
	}
		
	public void csvImportSpotData(EnumSpotMeasures measureType, String[] data) 
	{
		switch(measureType) {
		case AREANPIXELS:
			areaNPixels.csvImportDataFromRow( data, 2); 
			break;
		default:
			break;
		}
	}
		
}
