package plugins.fmp.multiSPOTS.experiment;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import icy.roi.ROI;
import icy.type.geom.Polyline2D;
import icy.util.StringUtil;
import icy.util.XMLUtil;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;



public class SpotArea 
{
	public Level2D 	polylineLevel 	= new Level2D();
	public Level2D 	polyline_old 	= new Level2D();
	public double [] measure		= null;
	
	public String	name 			= "noname";
	public int		index 			= -1;
	public String 	header 			= null;
	
	private final String ID_NPOINTS	= "npoints";
	private final String ID_NAME	= "name";
	private final String ID_N		= "n";
	private final String ID_X		= "x";
	private final String ID_Y		= "y";
	
	// -------------------------
	
	SpotArea(String capName) 
	{
		this.name = capName;
	}
	
	public SpotArea(String name, int indexImage, List<Point2D> limit) 
	{
		this.name = name;
		this.index = indexImage;
		polylineLevel = new Level2D(limit);
	}
	
	public void clear() 
	{
		polylineLevel = new Level2D();
	}
	
	public void setPolylineLevelFromTempData(String name, int indexImage, int xStart, int xEnd) 
	{
		this.name = name;
		this.index = indexImage;
		int npoints = xEnd-xStart+1;
		double [] xpoints = new double [npoints];
		double [] ypoints = new double [npoints];
		int j= 0;
		for (int i = xStart; i <= xEnd; i++, j++) 
		{
			xpoints[j] = i;
			ypoints[j] = measure[j];
		}
		polylineLevel = new Level2D(xpoints, ypoints, npoints);
	}
	
	public void setPolylineLevelFromTempData(String name, int indexImage) 
	{
		this.name = name;
		this.index = indexImage;
		int xStart = 0;
		int xEnd = measure.length-1;
		int npoints = xEnd-xStart+1;
		double [] xpoints = new double [npoints];
		double [] ypoints = new double [npoints];
		int j= 0;
		for (int i = xStart; i <= xEnd; i++, j++) 
		{
			xpoints[j] = i;
			ypoints[j] = measure[j];
		}
		polylineLevel = new Level2D(xpoints, ypoints, npoints);
	}
	
	public void setTempDataFromPolylineLevel() 
	{
		int npoints = polylineLevel.npoints;
		measure = new double [npoints];
		for (int j = 0; j < npoints; j++) 
		{
			measure[j] = polylineLevel.ypoints[j];
		}
	}
	
	int getNPoints() 
	{
		if (polylineLevel == null)
			return 0;
		return polylineLevel.npoints;
	}

	int restoreNPoints()  
	{
		if (polyline_old != null) 
			polylineLevel = polyline_old.clone();
		return polylineLevel.npoints;
	}
	
	void cropToNPoints(int npoints) 
	{
		if (npoints > polylineLevel.npoints)
			return;
		
		if (polyline_old == null) 
			polyline_old = polylineLevel.clone();
        
		Polyline2D pol = new Polyline2D();
        for (int i = 0; i < npoints; i++)
            pol.addPoint(polylineLevel.xpoints[i], polylineLevel.ypoints[i]);

		polylineLevel = new Level2D(pol); 
	}
	
	void copy(SpotArea sourceSpotArea) 
	{
		if (sourceSpotArea.polylineLevel != null)
			polylineLevel = sourceSpotArea.polylineLevel.clone(); 
	}
	
	boolean isThereAnyMeasuresDone() 
	{
		return (polylineLevel != null && polylineLevel.npoints > 0);
	}
	
	ArrayList<Integer> getMeasures(long seriesBinMs, long outputBinMs) 
	{
		if (polylineLevel == null || polylineLevel.npoints == 0)
			return null;
		long maxMs = (polylineLevel.ypoints.length -1) * seriesBinMs;
		long npoints = (maxMs / outputBinMs)+1;
		ArrayList<Integer> arrayInt = new ArrayList<Integer>((int) npoints);
		for (double iMs = 0; iMs <= maxMs; iMs += outputBinMs) 
		{
			int index = (int) (iMs  / seriesBinMs);
			arrayInt.add((int) polylineLevel.ypoints[index]);
		}
		return arrayInt;
	}
	
	List<Integer> getMeasures() 
	{
		return getIntegerArrayFromPolyline2D();
	}
	
	int getLastMeasure() 
	{	
		if (polylineLevel == null || polylineLevel.npoints == 0)
			return 0;
		int lastitem = polylineLevel.ypoints.length - 1;
		int ivalue = (int) polylineLevel.ypoints[lastitem];
		return ivalue;
	}
	
	int getT0Measure() 
	{	
		if (polylineLevel == null|| polylineLevel.npoints == 0)
			return 0;
		return (int) polylineLevel.ypoints[0];
	}
	
	int getLastDeltaMeasure() 
	{	
		if (polylineLevel == null|| polylineLevel.npoints == 0)
			return 0;
		int lastitem = polylineLevel.ypoints.length - 1;
		return (int) (polylineLevel.ypoints[lastitem] - polylineLevel.ypoints[lastitem-1]);
	}

	boolean transferROIsToMeasures(List<ROI> listRois) 
	{	
		for (ROI roi: listRois) 
		{		
			String roiname = roi.getName();
			if (roi instanceof ROI2DPolyLine && roiname .contains (name)) 
			{
				polylineLevel = new Level2D(((ROI2DPolyLine)roi).getPolyline2D());
				return true;
			}
		}
		return false;
	}

	List<Integer> getIntegerArrayFromPolyline2D() 
	{
		if (polylineLevel == null || polylineLevel.npoints == 0)
			return null;
		List<Integer> arrayInt = new ArrayList<Integer>(polylineLevel.ypoints.length);
		for (double i: polylineLevel.ypoints)
			arrayInt.add((int) i);
		return arrayInt;
	}

	// ----------------------------------------------------------------------
	
	public int loadCapillaryLimitFromXML(Node node, String nodename, String header) 
	{
		final Node nodeMeta = XMLUtil.getElement(node, nodename);
		int npoints = 0;
		polylineLevel = null;
	    if (nodeMeta != null)  
	    {
	    	name =  XMLUtil.getElementValue(nodeMeta, ID_NAME, nodename);
	    	if (!name.contains("_")) 
	    	{
	    		this.header = header;
	    		name = header + name;
	    	} 
	    	polylineLevel = loadPolyline2DFromXML(nodeMeta);
		    if (polylineLevel != null)
		    	npoints = polylineLevel.npoints;
	    }
		final Node nodeMeta_old = XMLUtil.getElement(node, nodename+"old");
		if (nodeMeta_old != null) 
			polyline_old = loadPolyline2DFromXML(nodeMeta_old);
	    return npoints;
	}

	Level2D loadPolyline2DFromXML(Node nodeMeta) 
	{
		Level2D line = null;
    	int npoints1 = XMLUtil.getElementIntValue(nodeMeta, ID_NPOINTS, 0);
    	if (npoints1 > 0) 
    	{
	    	double[] xpoints = new double [npoints1];
	    	double[] ypoints = new double [npoints1];
	    	for (int i=0; i< npoints1; i++) 
	    	{
	    		Element elmt = XMLUtil.getElement(nodeMeta, ID_N+i);
	    		if (i ==0)
	    			xpoints[i] = XMLUtil.getAttributeDoubleValue(elmt, ID_X, 0);
	    		else
	    			xpoints[i] = i+xpoints[0];
	    		ypoints[i] = XMLUtil.getAttributeDoubleValue(elmt, ID_Y, 0);
			}
	    	line = new Level2D(xpoints, ypoints, npoints1);
    	}
    	return line;
    }
	
	public void saveCapillaryLimit2XML(Node node, String nodename) 
	{
		if (polylineLevel == null || polylineLevel.npoints == 0)
			return;
		final Node nodeMeta = XMLUtil.setElement(node, nodename);
	    if (nodeMeta != null) 
	    {
	    	XMLUtil.setElementValue(nodeMeta, ID_NAME, name);
	    	saveLevel2XML(nodeMeta, polylineLevel);
	    	final Node nodeMeta_old = XMLUtil.setElement(node, nodename+"old");
		    if (polyline_old != null && polyline_old.npoints != polylineLevel.npoints) 
		    	saveLevel2XML(nodeMeta_old,  polyline_old);
	    }
	}
	
	void saveLevel2XML(Node nodeMeta, Polyline2D polyline)  
	{
		XMLUtil.setElementIntValue(nodeMeta, ID_NPOINTS, polyline.npoints);
    	for (int i=0; i< polyline.npoints; i++) 
    	{
    		Element elmt = XMLUtil.setElement(nodeMeta, ID_N+i);
    		if (i==0)
    			XMLUtil.setAttributeDoubleValue(elmt, ID_X, polyline.xpoints[i]);
    		XMLUtil.setAttributeDoubleValue(elmt, ID_Y, polyline.ypoints[i]);
    	}
	}
	
	public void adjustToImageWidth(int imageSize) 
	{
		if (polylineLevel == null || polylineLevel.npoints == 0)
			return;
		int npoints = polylineLevel.npoints;
		int npoints_old = 0;
		if (polyline_old != null && polyline_old.npoints > npoints) 
			npoints_old = polyline_old.npoints;
		if (npoints == imageSize || npoints_old == imageSize)
			return;
		
		// reduce polyline npoints to imageSize
		if (npoints > imageSize) 
		{
			int newSize = imageSize;
			if (npoints < npoints_old)
				newSize = 1 + imageSize *npoints / npoints_old;
			polylineLevel = polylineLevel.contractPolylineToNewSize(newSize);
			if (npoints_old != 0)
				polyline_old = polyline_old.contractPolylineToNewSize(imageSize);
		}
		// expand polyline npoints to imageSize
		else 
		{ 
			int newSize = imageSize;
			if (npoints < npoints_old)
				newSize = imageSize *npoints / npoints_old;
			polylineLevel = polylineLevel.expandPolylineToNewSize(newSize);
			if (npoints_old != 0)
				polyline_old = polyline_old.expandPolylineToNewSize(imageSize);
		}
	}

	public void cropToImageWidth(int imageSize) 
	{
		if (polylineLevel == null || polylineLevel.npoints == 0)
			return;
		int npoints = polylineLevel.npoints;
		if (npoints == imageSize)
			return;
		
		int npoints_old = 0;
		if (polyline_old != null && polyline_old.npoints > npoints) 
			npoints_old = polyline_old.npoints;
		if (npoints == imageSize || npoints_old == imageSize)
			return;
		
		// reduce polyline npoints to imageSize
		int newSize = imageSize;
		polylineLevel = polylineLevel.cropPolylineToNewSize(newSize);		
	}
	
	// ----------------------------------------------------------------------
	
	public boolean cvsExportDataToRow(StringBuffer sbf) 
	{
		int npoints = 0;
		if (polylineLevel != null && polylineLevel.npoints > 0)
			npoints = polylineLevel.npoints; 
			
		sbf.append(Integer.toString(npoints)+ ",");
		if (npoints > 0) {
			for (int i = 0; i < polylineLevel.npoints; i++)
	        {
	            sbf.append(StringUtil.toString((double) polylineLevel.xpoints[i]));
	            sbf.append(",");
	            sbf.append(StringUtil.toString((double) polylineLevel.ypoints[i]));
	            sbf.append(",");
	        }
		}
		return true;
	}
	
	public boolean csvImportDataFromRow(String[] data, int startAt) 
	{
		if (data.length < startAt)
			return false;
		
		int npoints = Integer.valueOf(data[startAt]);
		if (npoints > 0) {
			double[] x = new double[npoints];
			double[] y = new double[npoints];
			int offset = startAt+1;
			for (int i = 0; i < npoints; i++) { 
				x[i] = Double.valueOf(data[offset]);
				offset++;
				y[i] = Double.valueOf(data[offset]);
				offset++;
			}
			polylineLevel = new Level2D(x, y, npoints);
		}
		return true;
	}
	
}
