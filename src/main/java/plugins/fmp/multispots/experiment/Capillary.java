package plugins.fmp.multispots.experiment;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;

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




public class Capillary implements Comparable <Capillary> 
{

	private ROI2D 						roi 			= null;
	private ArrayList<KymoROI2D>		roisForKymo 	= new ArrayList<KymoROI2D>();
	private String						kymographName 	= null;
	public int							kymographIndex 	= -1;
	private String						kymographPrefix	= null;
	
	public String 						version 		= null;
	public String						filenameTIFF	= null;
	
	public ArrayList<int[]> 			cap_Integer		= null;
	
	public String 						capStimulus		= new String("..");
	public String 						capConcentration= new String("..");
	public String						capSide			= ".";
	public int							capNFlies		= 1;
	public int							capCageID		= 0;
	public double 						capVolume 		= 5.;
	public int 							capPixels 		= 5;
	public boolean						descriptionOK	= false;
	public int							versionInfos	= 0;
	
	public BuildSeriesOptions 			limitsOptions	= new BuildSeriesOptions();
	
	public  final String 				ID_TOPLEVEL 	= "toplevel";	
	public  final String 				ID_BOTTOMLEVEL 	= "bottomlevel";	
	public  final String 				ID_DERIVATIVE 	= "derivative";	
	
	public CapillaryLevel				ptsTop  		= new CapillaryLevel(ID_TOPLEVEL); 
	public CapillaryLevel				ptsBottom 		= new CapillaryLevel(ID_BOTTOMLEVEL); 
	public CapillaryLevel				ptsDerivative 	= new CapillaryLevel(ID_DERIVATIVE); 
	public CapillaryGulps 				ptsGulps 		= new CapillaryGulps(); 
	
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
	private final String 				ID_NAME 		= "nameMC";
	private final String 				ID_NAMETIFF 	= "filenameTIFF";
	private final String 				ID_VERSION		= "version"; 
	private final String 				ID_VERSIONNUM	= "1.0.0"; 
	
	// ----------------------------------------------------
	
	public Capillary(ROI2D roiCapillary) 
	{
		this.roi = roiCapillary;
		this.kymographName = replace_LR_with_12(roiCapillary.getName());
	}
	
	Capillary(String name) 
	{
		this.kymographName = replace_LR_with_12(name);
	}
	
	public Capillary() 
	{
	}

	@Override
	public int compareTo(Capillary o) 
	{
		if (o != null)
			return this.kymographName.compareTo(o.kymographName);
		return 1;
	}
	
	// ------------------------------------------
	
	public void copy(Capillary cap) 
	{
		kymographIndex 	= cap.kymographIndex;
		kymographName 	= cap.kymographName;
		version 		= cap.version;
		roi 			= (ROI2D) cap.roi.getCopy();
		filenameTIFF	= cap.filenameTIFF;
		
		capStimulus		= cap.capStimulus;
		capConcentration= cap.capConcentration;
		capSide			= cap.capSide;
		capNFlies		= cap.capNFlies;
		capCageID		= cap.capCageID;
		capVolume 		= cap.capVolume;
		capPixels 		= cap.capPixels;
		
		limitsOptions	= cap.limitsOptions;
		
		ptsGulps.copy(cap.ptsGulps);
		ptsTop.copy(cap.ptsTop); 
		ptsBottom.copy(cap.ptsBottom); 
		ptsDerivative.copy(cap.ptsDerivative); 
	}
	
	public String getKymographName() 
	{
		return kymographName;
	}
	
	public void setKymographName(String name) 
	{
		this.kymographName = name;
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
	
	public String getLast2ofCapillaryName() 
	{
		if (roi == null)
			return "missing";
		return roi.getName().substring(roi.getName().length() -2);
	}
	
	public String getRoiNamePrefix() 
	{
		return kymographPrefix;
	}
	
 	public String getCapillarySide() 
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
		if (!name .contains("line"))
			return -1;
		return Integer.valueOf(name.substring(4, 5));
	}
	
	public String getSideDescriptor(EnumXLSExportType xlsExportOption) 
	{
		String value = null;
		capSide = getCapillarySide();
		switch (xlsExportOption) 
		{
		case DISTANCE:
		case ISALIVE:
			value = capSide + "(L=R)";
			break;
		case SUMGULPS_LR:
		case TOPLEVELDELTA_LR:
		case TOPLEVEL_LR:
			if (capSide.equals("L"))
				value = "sum";
			else
				value = "PI";
			break;
		case XYIMAGE:
		case XYTOPCAGE:
		case XYTIPCAPS:
			if (capSide .equals ("L"))
				value = "x";
			else
				value = "y";
			break;
		default:
			value = capSide;
			break;
		}
		return value;
	}
	
	public String getCapillaryField(EnumXLSColumnHeader fieldEnumCode)
	{
		String stringValue = null;
		switch(fieldEnumCode) 
		{
		case CAP_STIM:
			stringValue = capStimulus;
			break;
		case CAP_CONC:
			stringValue = capConcentration;
			break;
		default:
			break;
		}
		return stringValue;
	}
	
	public void setCapillaryField(EnumXLSColumnHeader fieldEnumCode, String stringValue)
	{
		switch(fieldEnumCode) 
		{
		case CAP_STIM:
			capStimulus = stringValue;
			break;
		case CAP_CONC:
			capConcentration = stringValue;
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
		case DERIVEDVALUES:
			yes= (ptsDerivative.isThereAnyMeasuresDone());
			break;
		case SUMGULPS:
			yes= (ptsGulps.isThereAnyMeasuresDone());
			break;
		case BOTTOMLEVEL:
			yes= ptsBottom.isThereAnyMeasuresDone();
			break;
		case TOPLEVEL:
		default:
			yes= ptsTop.isThereAnyMeasuresDone();
			break;
		}
		return yes;
	}
		
	public ArrayList<Integer> getCapillaryMeasuresForXLSPass1(EnumXLSExportType option, long seriesBinMs, long outputBinMs) 
	{
		ArrayList<Integer> datai = null;
		switch (option) 
		{
		case DERIVEDVALUES:
			datai = ptsDerivative.getMeasures(seriesBinMs, outputBinMs);
			break;
		case SUMGULPS:
		case SUMGULPS_LR:
		case NBGULPS:
		case AMPLITUDEGULPS:
		case TTOGULP:
		case TTOGULP_LR:
		case AUTOCORREL:
		case AUTOCORREL_LR:
		case CROSSCORREL:
		case CROSSCORREL_LR:
			if (ptsGulps != null)
				datai = ptsGulps.getMeasuresFromGulps(option, ptsTop.getNPoints(), seriesBinMs, outputBinMs);
			break;
		case BOTTOMLEVEL:
			datai = ptsBottom.getMeasures(seriesBinMs, outputBinMs);
			break;
		case TOPLEVEL:
		case TOPRAW:
		case TOPLEVEL_LR:
		case TOPLEVELDELTA:
		case TOPLEVELDELTA_LR:
			default:
			datai = ptsTop.getMeasures(seriesBinMs, outputBinMs);
			break;
		}
		return datai;
	}
		
	public void cropMeasuresToNPoints (int npoints) 
	{
		if (ptsTop.polylineLevel != null)
			ptsTop.cropToNPoints(npoints);
		if (ptsBottom.polylineLevel != null)
			ptsBottom.cropToNPoints(npoints);
		if (ptsDerivative.polylineLevel != null)
			ptsDerivative.cropToNPoints(npoints);
	}
	
	public void restoreClippedMeasures () 
	{
		if (ptsTop.polylineLevel != null)
			ptsTop.restoreNPoints();
		if (ptsBottom.polylineLevel != null)
			ptsBottom.restoreNPoints();
		if (ptsDerivative.polylineLevel != null)
			ptsDerivative.restoreNPoints();
	}
	
	public void setGulpsOptions (BuildSeriesOptions options) 
	{
		limitsOptions = options;
	}
	
	public BuildSeriesOptions getGulpsOptions () 
	{
		return limitsOptions;
	}
	
	public void initGulps() 
	{
		if (ptsGulps == null) {
			ptsGulps = new CapillaryGulps();
			ptsGulps.gulps = new ArrayList <> ();
		}
		
		if (limitsOptions.analyzePartOnly) {
			int searchFromXFirst = (int) limitsOptions.searchArea.getX();
			int searchFromXLast = (int) limitsOptions.searchArea.getWidth() + searchFromXFirst;
			ptsGulps.removeGulpsWithinInterval(searchFromXFirst, searchFromXLast);
		}
		else 
			ptsGulps.gulps.clear();
	}
	
	public void detectGulps() 
	{
		int indexPixel = 0;
		int firstPixel = 1;
		if (ptsTop.polylineLevel == null)
			return;
		int lastPixel = ptsTop.polylineLevel.npoints;
		if (limitsOptions.analyzePartOnly){
			firstPixel = (int) limitsOptions.searchArea.getX();
			lastPixel = (int) limitsOptions.searchArea.getWidth() + firstPixel;
			
		} 
		int threshold = (int) ((limitsOptions.detectGulpsThreshold_uL / capVolume) * capPixels);
		ArrayList<Point2D> gulpPoints = new ArrayList<Point2D>();
		int indexLastDetected = -1;
		
		for (indexPixel = firstPixel; indexPixel < lastPixel; indexPixel++) 
		{
			int derivativevalue = (int) ptsDerivative.polylineLevel.ypoints[indexPixel-1];
			if (derivativevalue >= threshold) 
				indexLastDetected = addPointMatchingThreshold(indexPixel, gulpPoints, indexLastDetected); 
		}
		if (indexLastDetected > 0)
			addNewGulp(gulpPoints);
	}
	
	private int addPointMatchingThreshold(int indexPixel, ArrayList<Point2D> gulpPoints, int indexLastDetected) 
	{
		if (indexLastDetected > 0 && ((indexPixel - indexLastDetected) > 1)) {
			if (gulpPoints.size() == 1)
				gulpPoints.add(new Point2D.Double(indexPixel-1, ptsTop.polylineLevel.ypoints[indexPixel-1]));
			addNewGulp(gulpPoints);
			gulpPoints.clear();
			gulpPoints.add(new Point2D.Double(indexPixel-1, ptsTop.polylineLevel.ypoints[indexPixel-1]));
		}
		gulpPoints.add(new Point2D.Double(indexPixel, ptsTop.polylineLevel.ypoints[indexPixel]));
		return indexPixel;
	}
	
	private void addNewGulp(ArrayList<Point2D> gulpPoints) 
	{
		ptsGulps.addNewGulpFromPoints(gulpPoints);
	}
	
	public int getLastMeasure(EnumXLSExportType option) 
	{
		int lastMeasure = 0;
		switch (option) 
		{
		case DERIVEDVALUES:
			lastMeasure = ptsDerivative.getLastMeasure();
			break;
		case SUMGULPS:
			if (ptsGulps != null) 
			{
				List<Integer> datai = ptsGulps.getCumSumFromGulps(ptsTop.getNPoints());
				lastMeasure = datai.get(datai.size()-1);
			}
			break;
		case BOTTOMLEVEL:
			lastMeasure = ptsBottom.getLastMeasure();
			break;
		case TOPLEVEL:
		default:
			lastMeasure = ptsTop.getLastMeasure();
			break;
		}
		return lastMeasure;
	}
	
	public int getLastDeltaMeasure(EnumXLSExportType option) 
	{
		int lastMeasure = 0;
		switch (option) 
		{
		case DERIVEDVALUES:
			lastMeasure = ptsDerivative.getLastDeltaMeasure();
			break;
		case SUMGULPS:
			if (ptsGulps != null) {
				List<Integer> datai = ptsGulps.getCumSumFromGulps(ptsTop.getNPoints());
				lastMeasure = datai.get(datai.size()-1) - datai.get(datai.size()-2);
			}
			break;
		case BOTTOMLEVEL:
			lastMeasure = ptsBottom.getLastDeltaMeasure();
			break;
		case TOPLEVEL:
		default:
			lastMeasure = ptsTop.getLastDeltaMeasure();
			break;
		}
		return lastMeasure;
	}
	
	public int getT0Measure(EnumXLSExportType option) 
	{
		int t0Measure = 0;
		switch (option) 
		{
		case DERIVEDVALUES:
			t0Measure = ptsDerivative.getT0Measure();
			break;
		case SUMGULPS:
			if (ptsGulps != null) {
				List<Integer> datai = ptsGulps.getCumSumFromGulps(ptsTop.getNPoints());
				t0Measure = datai.get(0);
			}
			break;
		case BOTTOMLEVEL:
			t0Measure = ptsBottom.getT0Measure();
			break;
		case TOPLEVEL:
		default:
			t0Measure = ptsTop.getT0Measure();
			break;
		}
		return t0Measure;
	}

	public List<ROI2D> transferMeasuresToROIs() 
	{
		List<ROI2D> listrois = new ArrayList<ROI2D> ();
		getROIFromCapillaryLevel(ptsTop, listrois);
		getROIFromCapillaryLevel(ptsBottom, listrois);
		getROIFromCapillaryLevel(ptsDerivative, listrois);
		getROIsFromCapillaryGulps(ptsGulps, listrois);	
		return listrois;
	}
	
	private void getROIFromCapillaryLevel(CapillaryLevel capLevel, List<ROI2D> listrois) 
	{
		if (capLevel.polylineLevel == null || capLevel.polylineLevel.npoints == 0)
			return;
		
		ROI2D roi = new ROI2DPolyLine(capLevel.polylineLevel);
		String name = kymographPrefix + "_" + capLevel.capName;
		roi.setName(name);
		roi.setT(kymographIndex);
		if (capLevel.capName.contains(ID_DERIVATIVE)) {
			roi.setColor(Color.yellow);
			roi.setStroke(1);
		}
		listrois.add( roi);
	}
	
	private void getROIsFromCapillaryGulps(CapillaryGulps capGulps, List<ROI2D> listrois) 
	{
		int ngulps = capGulps.gulps.size();
		if (ngulps == 0)
			return;
		
		ArrayList<ROI2D> rois = new ArrayList<ROI2D> (ngulps);
		if (capGulps.gulps.size() > 0)
			for (Polyline2D gulpLine: capGulps.gulps) {
				ROI2D roi = getROIfromGulp(gulpLine);
				if (roi != null)
					rois.add( roi);
			}
		
		listrois.addAll(rois);
	}
	
	private ROI2D getROIfromGulp(Polyline2D gulpLine)
	{
		if (gulpLine.npoints == 0)
			return null;
		ROI2DPolyLine roi = new ROI2DPolyLine (gulpLine);
		int startAt = (int) gulpLine.xpoints[0];
		String name = kymographPrefix + "_gulp_at_" + String.format("%07d", startAt);
		roi.setName(name);
		roi.setColor(Color.red);
		roi.setStroke(1);
		roi.setT(kymographIndex);
		return roi;
	}
	
	public void transferROIsToMeasures(List<ROI> listRois) 
	{
		ptsTop.transferROIsToMeasures(listRois);
		ptsBottom.transferROIsToMeasures(listRois);
		ptsGulps.transferROIsToMeasures(listRois);
		ptsDerivative.transferROIsToMeasures(listRois);
	}
	
	// -----------------------------------------------------------------------------

	public boolean loadFromXML_CapillaryOnly(Node node) 
	{
	    final Node nodeMeta = XMLUtil.getElement(node, ID_META);
	    boolean flag = (nodeMeta != null); 
	    if (flag) 
	    {
	    	version 		= XMLUtil.getElementValue(nodeMeta, ID_VERSION, "0.0.0");
	    	kymographIndex 	= XMLUtil.getElementIntValue(nodeMeta, ID_INDEXIMAGE, kymographIndex);
	        kymographName 	= XMLUtil.getElementValue(nodeMeta, ID_NAME, kymographName);
	        filenameTIFF 	= XMLUtil.getElementValue(nodeMeta, ID_NAMETIFF, filenameTIFF);	        
	        descriptionOK 	= XMLUtil.getElementBooleanValue(nodeMeta, ID_DESCOK, false);
	        versionInfos 	= XMLUtil.getElementIntValue(nodeMeta, ID_VERSIONINFOS, 0);
	        capNFlies 		= XMLUtil.getElementIntValue(nodeMeta, ID_NFLIES, capNFlies);
	        capCageID 		= XMLUtil.getElementIntValue(nodeMeta, ID_CAGENB, capCageID);
	        capVolume 		= XMLUtil.getElementDoubleValue(nodeMeta, ID_CAPVOLUME, Double.NaN);
			capPixels 		= XMLUtil.getElementIntValue(nodeMeta, ID_CAPPIXELS, 5);
			capStimulus 	= XMLUtil.getElementValue(nodeMeta, ID_STIML, ID_STIML);
			capConcentration= XMLUtil.getElementValue(nodeMeta, ID_CONCL, ID_CONCL);
			capSide 		= XMLUtil.getElementValue(nodeMeta, ID_SIDE, ".");
			
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
		String header = getLast2ofCapillaryName()+"_";
		boolean result = ptsTop.loadCapillaryLimitFromXML(node, ID_TOPLEVEL, header) > 0;
		result |= ptsBottom.loadCapillaryLimitFromXML(node, ID_BOTTOMLEVEL, header) > 0;
		result |= ptsDerivative.loadCapillaryLimitFromXML(node, ID_DERIVATIVE, header) > 0;
		result |= ptsGulps.loadGulpsFromXML(node);
		return result;
	}
	
	// -----------------------------------------------------------------------------

	public boolean saveToXML_CapillaryOnly(Node node) 
	{
	    final Node nodeMeta = XMLUtil.setElement(node, ID_META);
	    if (nodeMeta == null)
	    	return false;
    	if (version == null)
    		version = ID_VERSIONNUM;
    	XMLUtil.setElementValue(nodeMeta, ID_VERSION, version);
        XMLUtil.setElementIntValue(nodeMeta, ID_INDEXIMAGE, kymographIndex);
        XMLUtil.setElementValue(nodeMeta, ID_NAME, kymographName);
        if (filenameTIFF != null ) {
        	String filename = Paths.get(filenameTIFF).getFileName().toString();
        	XMLUtil.setElementValue(nodeMeta, ID_NAMETIFF, filename);
        }
        XMLUtil.setElementBooleanValue(nodeMeta, ID_DESCOK, descriptionOK);
        XMLUtil.setElementIntValue(nodeMeta, ID_VERSIONINFOS, versionInfos);
        XMLUtil.setElementIntValue(nodeMeta, ID_NFLIES, capNFlies);
        XMLUtil.setElementIntValue(nodeMeta, ID_CAGENB, capCageID);
		XMLUtil.setElementDoubleValue(nodeMeta, ID_CAPVOLUME, capVolume);
		XMLUtil.setElementIntValue(nodeMeta, ID_CAPPIXELS, capPixels);
		XMLUtil.setElementValue(nodeMeta, ID_STIML, capStimulus);
		XMLUtil.setElementValue(nodeMeta, ID_SIDE, capSide);
		XMLUtil.setElementValue(nodeMeta, ID_CONCL, capConcentration);

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
		capVolume = volume;
		capPixels = pixels;
		descriptionOK = true;
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
			kymographPrefix = getLast2ofCapillaryName();
		
		List<String> row = Arrays.asList(
				kymographPrefix,
				Integer.toString(kymographIndex), 
				kymographName, 
				filenameTIFF, 
				Integer.toString(capCageID),
				Integer.toString(capNFlies),
				Double.toString(capVolume), 
				Integer.toString(capPixels), 
				capStimulus, 
				capConcentration, 
				capSide);
		sbf.append(String.join(",", row));
		sbf.append("\n");
		return sbf.toString();
	}
	
	public String csvExportMeasureSectionHeader(EnumCapillaryMeasures measureType) 
	{
		StringBuffer sbf = new StringBuffer();
		String explanation1 = "columns=,name,index, npts,..,.(xi;yi)\n";
		String explanation2 = "columns=,name,index, n_gulps(i), ..., gulp_i, .npts(j),.,(xij;yij))\n";
		switch(measureType) {
			case TOPLEVEL:
				sbf.append("#,TOPLEVEL," + explanation1);
				break;
			case BOTTOMLEVEL:
				sbf.append("#,BOTTOMLEVEL,"+explanation1);
				break;
			case TOPDERIVATIVE:
				sbf.append("#,TOPDERIVATIVE,"+explanation1);
				break;
			case GULPS:
				sbf.append("#,GULPS,"+explanation2);
				break;
			default:
				sbf.append("#,UNDEFINED,------------\n");
				break;
		}
		return sbf.toString();
	}
	
	public String csvExportCapillaryData(EnumCapillaryMeasures measureType) 
	{
		StringBuffer sbf = new StringBuffer();
		sbf.append(kymographPrefix+ ","+ kymographIndex +",");
		
		switch(measureType) {
			case TOPLEVEL:
				ptsTop.cvsExportDataToRow(sbf);
				break;
			case BOTTOMLEVEL:
				ptsBottom.cvsExportDataToRow(sbf);
				break;
			case TOPDERIVATIVE:
				ptsDerivative.cvsExportDataToRow(sbf);
				break;
			case GULPS:
				ptsGulps.csvExportDataToRow(sbf);
				break;
			default:
				break;
		}
		sbf.append("\n");
		return sbf.toString();
	}
	
	// --------------------------------------------
	
	public void csvImportCapillaryDescription(String[] data) 
	{
		int i = 0;
		kymographPrefix = data[i]; i++;
		kymographIndex = Integer.valueOf(data[i]); i++; 
		kymographName = data[i]; i++; 
		filenameTIFF = data[i]; i++; 
		capCageID = Integer.valueOf(data[i]); i++;
		capNFlies = Integer.valueOf(data[i]); i++;
		capVolume = Double.valueOf(data[i]); i++; 
		capPixels = Integer.valueOf(data[i]); i++; 
		capStimulus = data[i]; i++; 
		capConcentration = data[i]; i++; 
		capSide = data[i]; 
	}
		
	public void csvImportCapillaryData(EnumCapillaryMeasures measureType, String[] data) 
	{
		switch(measureType) {
		case TOPLEVEL:
			ptsTop.csvImportDataFromRow( data, 2); 
			break;
		case BOTTOMLEVEL:
			ptsBottom.csvImportDataFromRow( data, 2);
			break;
		case TOPDERIVATIVE:
			ptsDerivative.csvImportDataFromRow( data, 2); 
			break;
		case GULPS:
			ptsGulps.csvImportDataFromRow(data, 2);
			break;
		default:
			break;
		}
	}
		
}
