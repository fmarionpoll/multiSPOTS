package plugins.fmp.multiSPOTS.experiment;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;

import icy.image.IcyBufferedImage;
import icy.roi.BooleanMask2D;
import icy.roi.ROI2D;
import icy.util.XMLUtil;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DShape;

import plugins.fmp.multiSPOTS.series.BuildSeriesOptions;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSColumnHeader;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSExportType;




public class Spot implements Comparable <Spot> 
{

	private ROI2DShape 					spotRoi			= null;
	private ROI2DShape 					spotRoi_old		= null;
	private ArrayList<ROI2DAlongTime>	listRoiAlongTime= new ArrayList<ROI2DAlongTime>();
	public BooleanMask2D 				mask2D			= null;

	public int							cageIndex 		= -1;
	public String 						version 		= null;
	public String 						spotStim		= new String("..");
	public String 						spotConc		= new String("..");
	public String						spotCageSide	= ".";
	public int							spotNFlies		= 1;
	public int							spotIndex		= 0;
	public double 						spotVolume 		= 1;
	public int 							spotNPixels		= 1;
	public int							radius			= 30;
	public boolean						descriptionOK	= false;
	public int							versionInfos	= 0;
	
	public BuildSeriesOptions 			limitsOptions	= new BuildSeriesOptions();

	public int							spot_KymographIndex = -1;
	public String						spot_filenameTIFF = null;
	public IcyBufferedImage 			spot_Image		= null;
	 
	public SpotMeasure					sum  			= new SpotMeasure("sum"); 
	public SpotMeasure					sumClean		= new SpotMeasure("clean"); 
	public SpotMeasure					flyPresent		= new SpotMeasure("flyPresent"); 
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
	
	public Spot(ROI2DShape roi) 
	{
		this.spotRoi = roi;
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
			return (this.spotRoi.getName()).compareTo(o.spotRoi.getName());
		return 1;
	}
	
	// ------------------------------------------
	
	public void copySpot(Spot spotFrom) 
	{
		cageIndex 		= spotFrom.cageIndex;
		version 		= spotFrom.version;
		spotRoi 		= (ROI2DShape) spotFrom.spotRoi.getCopy();
		
		spotStim		= spotFrom.spotStim;
		spotConc		= spotFrom.spotConc;
		spotCageSide	= spotFrom.spotCageSide;
		spotNFlies		= spotFrom.spotNFlies;
		spotIndex		= spotFrom.spotIndex;
		spotVolume 		= spotFrom.spotVolume;
		spotNPixels 	= spotFrom.spotNPixels;
		radius 			= spotFrom.radius;
		
		limitsOptions	= spotFrom.limitsOptions;
		
		sum.copyLevel2D(spotFrom.sum);
		sumClean .copyLevel2D(spotFrom.sumClean);
		flyPresent.copyLevel2D(spotFrom.flyPresent);	
	}
	
	public ROI2D getRoi() 
	{
		return spotRoi;
	}
	
	public ROI2D getRoi_old() 
	{
		return spotRoi_old;
	}
	
	public void setRoi(ROI2DShape roi) 
	{
		this.spotRoi = roi; 
		listRoiAlongTime.clear();
	}
	
	public void setRoi_old(ROI2DShape roi) 
	{
		this.spotRoi_old = roi;
	}
	
	public void setRoiName(String name) 
	{
		spotRoi.setName(name);
	}
	
	public String getRoiName() 
	{
		return spotRoi.getName();
	}
	
	public String getLast2ofSpotName() 
	{
		if (spotRoi == null)
			return "missing";
		return spotRoi.getName().substring(spotRoi.getName().length() -2);
	}
	
 	public String getSpotSide() 
	{
		return spotRoi.getName().substring(spotRoi.getName().length() -2);
	}
	
	public static String xreplace_LR_with_12(String name) 
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
		String name = spotRoi.getName();
		if (!name .contains("spot"))
			return -1;
		return Integer.valueOf(name.substring(4, 6));
	}
	
	public String getSideDescriptor(EnumXLSExportType xlsExportOption) 
	{
		String value = null;
		spotCageSide = getSpotSide();
		switch (xlsExportOption) {
			case DISTANCE:
			case ISALIVE:
				value = spotCageSide + "(T=B)";
				break;
			case TOPLEVELDELTA_LR:
			case TOPLEVEL_LR:
				if (spotCageSide.equals("00"))
					value = "sum";
				else
					value = "PI";
				break;
			case XYIMAGE:
			case XYTOPCAGE:
			case XYTIPCAPS:
				if (spotCageSide .equals ("00"))
					value = "x";
				else
					value = "y";
				break;
			default:
				value = spotCageSide;
				break;
		}
		return value;
	}
	
	public String getSpotField(EnumXLSColumnHeader fieldEnumCode)
	{
		String stringValue = null;
		switch(fieldEnumCode) {
			case CAP_STIM:
				stringValue = spotStim;
				break;
			case CAP_CONC:
				stringValue = spotConc;
				break;
			default:
				break;
		}
		return stringValue;
	}
	
	public void setSpotField(EnumXLSColumnHeader fieldEnumCode, String stringValue)
	{
		switch(fieldEnumCode) {
			case CAP_STIM:
				spotStim = stringValue;
				break;
			case CAP_CONC:
				spotConc = stringValue;
				break;
			default:
				break;
		}
	}
	
	public Point2D getSpotCenter () 
	{	
		Point pt = spotRoi.getPosition();
		Rectangle rect = spotRoi.getBounds();
		pt.translate(rect.height/2, rect.width/2);
		return pt;
	}

	private SpotMeasure getSpotArea (EnumXLSExportType option)
	{
		switch (option) {
			case AREA_SUM:		
			case AREA_SUM_LR:
				return sum;
			case AREA_SUMCLEAN:
			case AREA_SUMCLEAN_LR:
				return sumClean;
			case AREA_FLYPRESENT:
				return flyPresent;
			default:
				return null;
		}
	}
	
	// -----------------------------------------
	
//	public void clearMeasures() {
//		sum = null;
//		sumClean = null;
//		flyPresent = null;
//	}
	
	public boolean isThereAnyMeasuresDone(EnumXLSExportType option) 
	{
		SpotMeasure spotArea = getSpotArea(option);
		if (spotArea != null)
			return spotArea.isThereAnyMeasuresDone();
		return false;
	}
		
	public ArrayList<Double> getSpotMeasuresForXLSPass1(EnumXLSExportType option, long seriesBinMs, long outputBinMs) 
	{
		SpotMeasure spotArea = getSpotArea(option);
		if (spotArea != null)
			return spotArea.getLevel2D_Y_subsampled(seriesBinMs, outputBinMs);
		return null;
	}

	public void cropSpotMeasuresToNPoints (int npoints) 
	{
		cropSpotMeasureToNPoints(sum , npoints);
		cropSpotMeasureToNPoints(sumClean , npoints);
		cropSpotMeasureToNPoints(flyPresent , npoints);
	}
	
	private void cropSpotMeasureToNPoints(SpotMeasure spotMeasure, int npoints) 
	{
		if (spotMeasure.getLevel2DNPoints() > 0)
			spotMeasure.cropLevel2DToNPoints(npoints);
	}
	
	public void restoreClippedSpotMeasures () 
	{
		restoreClippedMeasures( sum );
		restoreClippedMeasures( sumClean );
		restoreClippedMeasures( flyPresent );
	}
	
	private void restoreClippedMeasures(SpotMeasure spotMeasure)
	{
		if (spotMeasure.getLevel2DNPoints() > 0)
			spotMeasure.restoreCroppedLevel2D();
	}

	// -----------------------------------------------------------------------------

	public boolean loadFromXML_SpotOnly(Node node) 
	{
	    final Node nodeMeta = XMLUtil.getElement(node, ID_META);
	    boolean flag = (nodeMeta != null); 
	    if (flag) {
	    	version 		= XMLUtil.getElementValue(nodeMeta, ID_VERSION, "0.0.0");
	    	cageIndex 		= XMLUtil.getElementIntValue(nodeMeta, ID_INDEXIMAGE, cageIndex);
     
	        descriptionOK 	= XMLUtil.getElementBooleanValue(nodeMeta, ID_DESCOK, false);
	        versionInfos 	= XMLUtil.getElementIntValue(nodeMeta, ID_VERSIONINFOS, 0);
	        spotNFlies 		= XMLUtil.getElementIntValue(nodeMeta, ID_NFLIES, spotNFlies);
	        spotIndex 		= XMLUtil.getElementIntValue(nodeMeta, ID_CAGENB, spotIndex);
	        spotVolume 		= XMLUtil.getElementDoubleValue(nodeMeta, ID_SPOTVOLUME, Double.NaN);
			spotNPixels 	= XMLUtil.getElementIntValue(nodeMeta, ID_PIXELS, 5);
			radius			= XMLUtil.getElementIntValue(nodeMeta, ID_RADIUS, 30);
			spotStim 		= XMLUtil.getElementValue(nodeMeta, ID_STIML, ID_STIML);
			spotConc		= XMLUtil.getElementValue(nodeMeta, ID_CONCL, ID_CONCL);
			spotCageSide 	= XMLUtil.getElementValue(nodeMeta, ID_SIDE, ".");
			
	        spotRoi = (ROI2DShape) ROI2DUtilities.loadFromXML_ROI(nodeMeta);
	        limitsOptions.loadFromXML(nodeMeta);
	        
	        loadFromXML_SpotAlongT(node);
	    }
	    return flag;
	}
	
	private boolean loadFromXML_SpotAlongT(Node node) 
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
        		
        		if (i == 0) 
        			spotRoi = (ROI2DShape) listRoiAlongTime.get(0).getRoi();
        	}
        }
        return true;
	}
	
	public boolean saveToXML_SpotOnly(Node node) 
	{
	    final Node nodeMeta = XMLUtil.setElement(node, ID_META);
	    if (nodeMeta == null)
	    	return false;
    	if (version == null)
    		version = ID_VERSIONNUM;
    	XMLUtil.setElementValue(nodeMeta, ID_VERSION, version);
        XMLUtil.setElementIntValue(nodeMeta, ID_INDEXIMAGE, cageIndex);

        XMLUtil.setElementBooleanValue(nodeMeta, ID_DESCOK, descriptionOK);
        XMLUtil.setElementIntValue(nodeMeta, ID_VERSIONINFOS, versionInfos);
        XMLUtil.setElementIntValue(nodeMeta, ID_NFLIES, spotNFlies);
        XMLUtil.setElementIntValue(nodeMeta, ID_CAGENB, spotIndex);
		XMLUtil.setElementDoubleValue(nodeMeta, ID_SPOTVOLUME, spotVolume);
		XMLUtil.setElementIntValue(nodeMeta, ID_PIXELS, spotNPixels);
		XMLUtil.setElementIntValue(nodeMeta, ID_RADIUS, radius);
		XMLUtil.setElementValue(nodeMeta, ID_STIML, spotStim);
		XMLUtil.setElementValue(nodeMeta, ID_SIDE, spotCageSide);
		XMLUtil.setElementValue(nodeMeta, ID_CONCL, spotConc);

		ROI2DUtilities.saveToXML_ROI(nodeMeta, spotRoi); 
		
		boolean flag = saveToXML_SpotAlongT(node);
	    return flag;
	}
	
	private boolean saveToXML_SpotAlongT(Node node) 
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
	
	public List<ROI2DAlongTime> getROIAlongTList() 
	{
		if (listRoiAlongTime.size() < 1) 
			initROIAlongTList();
		return listRoiAlongTime;
	}
		
 	public ROI2DAlongTime getROIAtT(long t) 
 	{
		if (listRoiAlongTime.size() < 1) 
			initROIAlongTList();
		
		ROI2DAlongTime capRoi = null;
		for (ROI2DAlongTime item : listRoiAlongTime) {
			if (t < item.getT())
				break;
			capRoi = item;
		}
		return capRoi;
	}
 	
 	public void removeROIAlongTListItem(long t) 
 	{
 		ROI2DAlongTime itemFound = null;
 		for (ROI2DAlongTime item : listRoiAlongTime) {
			if (t != item.getT())
				continue;
			itemFound = item;
		}
 		if (itemFound != null)
 			listRoiAlongTime.remove(itemFound);
	}
	
	private void initROIAlongTList() 
	{ 
		listRoiAlongTime.add(new ROI2DAlongTime(0, spotRoi));		
	}
	
	// --------------------------------------------
	
	public void adjustLevel2DMeasuresToImageWidth (int imageWidth) 
	{
		sum.adjustLevel2DToImageWidth(imageWidth);
		sumClean.adjustLevel2DToImageWidth(imageWidth);
		flyPresent.adjustLevel2DToImageWidth(imageWidth);
	}

	public void cropLevel2DMeasuresToImageWidth (int imageWidth) 
	{
		sum.cropLevel2DToNPoints(imageWidth);
		sumClean.cropLevel2DToNPoints(imageWidth);
		flyPresent.cropLevel2DToNPoints(imageWidth);
	}
	
	public void initLevel2DMeasures() 
	{
		sum.initLevel2D_fromValues(getRoi().getName());
		sumClean.initLevel2D_fromValues(getRoi().getName());
		flyPresent.initLevel2D_fromBooleans(getRoi().getName());
	}
	
	public void buildSUMCLEANfromSUM()
	{
		int npoints = sum.measureValues.length;
		
		for (int i = 0; i <npoints; i++) {
			if(!flyPresent.measureBooleans[i]) {
				sumClean.measureValues[i] = sum.measureValues[i];
			} 
			else if (i > 0) {
				sumClean.measureValues[i] = sumClean.measureValues[i-1];
			} 
			else {
				double value = Double.NaN;
				for (int j= i; j < npoints; j++) {
					if (!flyPresent.measureBooleans[j]) {
						value = sum.measureValues[j];
						break;
					}
				}
				sumClean.measureValues[i] = value;
			}
		}
	}
	
	public List<ROI2D> transferMeasuresToROIs(int height) 
	{
		List<ROI2D> measuresRoisList = new ArrayList<ROI2D> ();
		measureToROI(sum, Color.green, height, measuresRoisList);
		measureToROI(sumClean, Color.red, height, measuresRoisList);
		measureToROI(flyPresent, Color.blue, 10, measuresRoisList);
		return measuresRoisList;
	}
	
	private void measureToROI(SpotMeasure spotMeasure, Color color, int imageHeight, List<ROI2D> measuresRoisList) 
	{
		if (spotMeasure.getLevel2DNPoints() == 0)
			return;
		
		spotMeasure.getLevel2D().normalizeYScale(imageHeight);
		ROI2D roi = new ROI2DPolyLine(spotMeasure.getLevel2D());
		String name = spotRoi.getName() + "_" + spotMeasure.getName();
		roi.setName(name);
		roi.setT(spot_KymographIndex);
		roi.setColor(color);
		roi.setStroke(1);
		measuresRoisList.add(roi);
	}
	
	// -----------------------------------------------------------------------------
	
	public String csvExportSpotArrayHeader(String csvSep) 
	{
		StringBuffer sbf = new StringBuffer();
		sbf.append("#"+csvSep+"SPOTS"+csvSep+"describe each spot\n");
		List<String> row2 = Arrays.asList(
				"index", 
				"name", 
				"cage",
				"nflies",
				"volume", 
				"npixel", 
				"radius",
				"stim", 
				"conc", 
				"side");
		sbf.append(String.join(csvSep, row2));
		sbf.append("\n");
		return sbf.toString();
	}
	
	public String csvExportDescription(String csvSep) 
	{	
		StringBuffer sbf = new StringBuffer();	
		List<String> row = Arrays.asList(
				Integer.toString(spotIndex), 
				getRoi().getName(), 
				Integer.toString(cageIndex),
				Integer.toString(spotNFlies),
				Double.toString(spotVolume), 
				Integer.toString(spotNPixels), 
				Integer.toString(radius),
				spotStim.replace(",", "."), 
				spotConc.replace(",", "."), 
				spotCageSide.replace(",", "."));
		sbf.append(String.join(csvSep, row));
		sbf.append("\n");
		return sbf.toString();
	}
	
	public String csvExportMeasures_SectionHeader(EnumSpotMeasures measureType, String csvSep) 
	{
		StringBuffer sbf = new StringBuffer();
		List<String> listExplanation1 =  Arrays.asList("\n name", "index", "npts", "yi","\n");		
		String explanation1 = String.join(csvSep, listExplanation1);
		switch(measureType) {
			case AREA_SUM:
			case AREA_SUMCLEAN:
			case AREA_FLYPRESENT:
//			case AREA_CNTPIX:	
				sbf.append("#" + csvSep + measureType.toString() + csvSep + explanation1);
				break;
			default:
				sbf.append("#" + csvSep + "UNDEFINED"+ csvSep + "------------\n");
				break; 
		}
		return sbf.toString();
	}
	
	public String csvExportMeasures_OneType(EnumSpotMeasures measureType, String csvSep) 
	{
		StringBuffer sbf = new StringBuffer();
		sbf.append(spotRoi.getName() + csvSep + spotIndex + csvSep);
		switch(measureType) {
			case AREA_SUM:  	
				sum.cvsExportYDataToRow(sbf, csvSep); 
				break;
			case AREA_SUMCLEAN:  	
				sumClean.cvsExportYDataToRow(sbf, csvSep); 
				break;
			case AREA_FLYPRESENT:
				flyPresent.cvsExportYDataToRow(sbf, csvSep); 
				break;
			default:
				break;
		}
		sbf.append("\n");
		return sbf.toString();
	}
	
	public void csvImportDescription(String[] data, boolean dummyColumn) 
	{
		int i = dummyColumn ? 1: 0;
		spotIndex 		= Integer.valueOf(data[i]); i++; 
		spotRoi.setName(data[i]); i++;
		cageIndex 		= Integer.valueOf(data[i]); i++;
		spotNFlies 		= Integer.valueOf(data[i]); i++;
		spotVolume 		= Double.valueOf(data[i]); i++; 
		spotNPixels 			= Integer.valueOf(data[i]); i++; 
		radius 			= Integer.valueOf(data[i]); i++;
		spotStim 		= data[i]; i++; 
		spotConc 		= data[i]; i++; 
		spotCageSide 	= data[i]; 
	}
		
	public void csvImportMeasures_OneType(EnumSpotMeasures measureType, String[] data, boolean x, boolean y) 
	{
		if (x && y) {
			switch(measureType) {
				case AREA_SUM:  	sum.csvImportXYDataFromRow( data, 2); break;
				case AREA_SUMCLEAN:	sumClean.csvImportXYDataFromRow( data, 2); break;
				case AREA_FLYPRESENT:  flyPresent.csvImportXYDataFromRow( data, 2); break;
				default:
					break;
			}
		}
		else if (!x && y) {
			switch(measureType) {
				case AREA_SUM:  	sum.csvImportYDataFromRow( data, 2); break;
				case AREA_SUMCLEAN: sumClean.csvImportYDataFromRow( data, 2); break;
				case AREA_FLYPRESENT: flyPresent.csvImportYDataFromRow( data, 2); break;
				default:
					break;
			}
		}
	}
		
}
