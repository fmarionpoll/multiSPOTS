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



public class SpotMeasure 
{
	public Level2D 	level2D 		= new Level2D();
	public Level2D 	leve2D_old		= new Level2D();
	public double [] measureValues	= null;
	public boolean [] measureBooleans = null;
	
	public String	name 			= "noname";
	public int		index 			= -1;
	public String 	header 			= null;
	
	private final String ID_NPOINTS	= "npoints";
	private final String ID_NAME	= "name";
	private final String ID_N		= "n";
	private final String ID_X		= "x";
	private final String ID_Y		= "y";
	
	// -------------------------
	
	SpotMeasure(String capName) 
	{
		this.name = capName;
	}
	
	public SpotMeasure(String name, int indexImage, List<Point2D> limit) 
	{
		this.name = name;
		this.index = indexImage;
		level2D = new Level2D(limit);
	}
	
	public void clear() 
	{
		level2D = new Level2D();
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
			ypoints[j] = measureValues[j];
		}
		level2D = new Level2D(xpoints, ypoints, npoints);
	}
	
	public void setPolylineLevelFromMeasureValues(String name, int indexImage) 
	{
		this.name = name;
		this.index = indexImage;
		int ii_start = 0;
		int ii_end = measureValues.length;
		int npoints = ii_end-ii_start+1;
		double [] xpoints = new double [npoints];
		double [] ypoints = new double [npoints];
		int j= 0;
		for (int i = ii_start; i < ii_end; i++, j++) 
		{
			xpoints[j] = i;
			ypoints[j] = measureValues[j];
		}
		level2D = new Level2D(xpoints, ypoints, npoints);
	}
	
	public void setPolylineLevelFromMeasureBoolean(String name, int indexImage) 
	{
		this.name = name;
		this.index = indexImage;
		int xStart = 0;
		int xEnd = measureBooleans.length;
		int npoints = xEnd-xStart+1;
		double [] xpoints = new double [npoints];
		double [] ypoints = new double [npoints];
		int j= 0;
		for (int i = xStart; i < xEnd; i++, j++) 
		{
			xpoints[j] = i;
			ypoints[j] = measureBooleans[j] ? 1d : 0d;
		}
		level2D = new Level2D(xpoints, ypoints, npoints);
	}
	
	public void setTempDataFromPolylineLevel() 
	{
		int npoints = level2D.npoints;
		measureValues = new double [npoints];
		for (int j = 0; j < npoints; j++) 
		{
			measureValues[j] = level2D.ypoints[j];
		}
	}
	
	int getNPoints() 
	{
		if (level2D == null)
			return 0;
		return level2D.npoints;
	}

	int restoreNPoints()  
	{
		if (leve2D_old != null) 
			level2D = leve2D_old.clone();
		return level2D.npoints;
	}
	
	void cropToNPoints(int npoints) 
	{
		if (npoints > level2D.npoints)
			return;
		
		if (leve2D_old == null) 
			leve2D_old = level2D.clone();
        
		Polyline2D pol = new Polyline2D();
        for (int i = 0; i < npoints; i++)
            pol.addPoint(level2D.xpoints[i], level2D.ypoints[i]);

		level2D = new Level2D(pol); 
	}
	
	void copy(SpotMeasure sourceSpotArea) 
	{
		if (sourceSpotArea.level2D != null)
			level2D = sourceSpotArea.level2D.clone(); 
	}
	
	boolean isThereAnyMeasuresDone() 
	{
		return (level2D != null && level2D.npoints > 0);
	}
	
	ArrayList<Double> getMeasures(long seriesBinMs, long outputBinMs) 
	{
		if (level2D == null || level2D.npoints == 0)
			return null;
		long maxMs = (level2D.ypoints.length -1) * seriesBinMs;
		long npoints = (maxMs / outputBinMs)+1;
		ArrayList<Double> arrayDouble = new ArrayList<Double>((int) npoints);
		for (double iMs = 0; iMs <= maxMs; iMs += outputBinMs) 
		{
			int index = (int) (iMs  / seriesBinMs);
			arrayDouble.add(level2D.ypoints[index]);
		}
		return arrayDouble;
	}
	
	List<Double> getMeasures() 
	{
		return getDoubleArrayFromPolyline2D();
	}

	boolean transferROIsToMeasures(List<ROI> listRois) 
	{	
		for (ROI roi: listRois) 
		{		
			String roiname = roi.getName();
			if (roi instanceof ROI2DPolyLine && roiname .contains (name)) 
			{
				level2D = new Level2D(((ROI2DPolyLine)roi).getPolyline2D());
				return true;
			}
		}
		return false;
	}

	List<Double> getDoubleArrayFromPolyline2D() 
	{
		if (level2D == null || level2D.npoints == 0)
			return null;
		List<Double> arrayDouble = new ArrayList<Double>(level2D.ypoints.length);
		for (double i: level2D.ypoints)
			arrayDouble.add(i);
		return arrayDouble;
	}

	// ----------------------------------------------------------------------
	
	public int loadCapillaryLimitFromXML(Node node, String nodename, String header) 
	{
		final Node nodeMeta = XMLUtil.getElement(node, nodename);
		int npoints = 0;
		level2D = null;
	    if (nodeMeta != null)  
	    {
	    	name =  XMLUtil.getElementValue(nodeMeta, ID_NAME, nodename);
	    	if (!name.contains("_")) 
	    	{
	    		this.header = header;
	    		name = header + name;
	    	} 
	    	level2D = loadPolyline2DFromXML(nodeMeta);
		    if (level2D != null)
		    	npoints = level2D.npoints;
	    }
		final Node nodeMeta_old = XMLUtil.getElement(node, nodename+"old");
		if (nodeMeta_old != null) 
			leve2D_old = loadPolyline2DFromXML(nodeMeta_old);
	    return npoints;
	}

	Level2D loadPolyline2DFromXML(Node nodeMeta) 
	{
		Level2D line = null;
    	int npoints = XMLUtil.getElementIntValue(nodeMeta, ID_NPOINTS, 0);
    	if (npoints > 0) 
    	{
	    	double[] xpoints = new double [npoints];
	    	double[] ypoints = new double [npoints];
	    	for (int i=0; i< npoints; i++) 
	    	{
	    		Element elmt = XMLUtil.getElement(nodeMeta, ID_N+i);
	    		if (i ==0)
	    			xpoints[i] = XMLUtil.getAttributeDoubleValue(elmt, ID_X, 0);
	    		else
	    			xpoints[i] = i+xpoints[0];
	    		ypoints[i] = XMLUtil.getAttributeDoubleValue(elmt, ID_Y, 0);
			}
	    	line = new Level2D(xpoints, ypoints, npoints);
    	}
    	return line;
    }
	
	public void saveCapillaryLimit2XML(Node node, String nodename) 
	{
		if (level2D == null || level2D.npoints == 0)
			return;
		final Node nodeMeta = XMLUtil.setElement(node, nodename);
	    if (nodeMeta != null) 
	    {
	    	XMLUtil.setElementValue(nodeMeta, ID_NAME, name);
	    	saveLevel2XML(nodeMeta, level2D);
	    	final Node nodeMeta_old = XMLUtil.setElement(node, nodename+"old");
		    if (leve2D_old != null && leve2D_old.npoints != level2D.npoints) 
		    	saveLevel2XML(nodeMeta_old,  leve2D_old);
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
		if (level2D == null || level2D.npoints == 0)
			return;
		int npoints = level2D.npoints;
		int npoints_old = 0;
		if (leve2D_old != null && leve2D_old.npoints > npoints) 
			npoints_old = leve2D_old.npoints;
		if (npoints == imageSize || npoints_old == imageSize)
			return;
		
		// reduce polyline npoints to imageSize
		if (npoints > imageSize) 
		{
			int newSize = imageSize;
			if (npoints < npoints_old)
				newSize = 1 + imageSize *npoints / npoints_old;
			level2D = level2D.contractPolylineToNewWidth(newSize);
			if (npoints_old != 0)
				leve2D_old = leve2D_old.contractPolylineToNewWidth(imageSize);
		}
		// expand polyline npoints to imageSize
		else 
		{ 
			int newSize = imageSize;
			if (npoints < npoints_old)
				newSize = imageSize *npoints / npoints_old;
			level2D = level2D.expandPolylineToNewWidth(newSize);
			if (npoints_old != 0)
				leve2D_old = leve2D_old.expandPolylineToNewWidth(imageSize);
		}
	}

	public void cropToImageWidth(int imageSize) 
	{
		if (level2D == null || level2D.npoints == 0)
			return;
		int npoints = level2D.npoints;
		if (npoints == imageSize)
			return;
		
		int npoints_old = 0;
		if (leve2D_old != null && leve2D_old.npoints > npoints) 
			npoints_old = leve2D_old.npoints;
		if (npoints == imageSize || npoints_old == imageSize)
			return;
		
		// reduce polyline npoints to imageSize
		int newSize = imageSize;
		level2D = level2D.cropPolylineToNewWidth(newSize);		
	}
	
	// ----------------------------------------------------------------------
	
	
	public boolean cvsExportXYDataToRow(StringBuffer sbf, String sep) 
	{
		int npoints = 0;
		if (level2D != null && level2D.npoints > 0)
			npoints = level2D.npoints; 
			
		sbf.append(Integer.toString(npoints)+ sep);
		if (npoints > 0) {
			for (int i = 0; i < level2D.npoints; i++)
	        {
	            sbf.append(StringUtil.toString((double) level2D.xpoints[i]));
	            sbf.append(sep);
	            sbf.append(StringUtil.toString((double) level2D.ypoints[i]));
	            sbf.append(sep);
	        }
		}
		return true;
	}
	
	public boolean cvsExportYDataToRow(StringBuffer sbf, String sep) 
	{
		int npoints = 0;
		if (level2D != null && level2D.npoints > 0)
			npoints = level2D.npoints; 
			
		sbf.append(Integer.toString(npoints)+ sep);
		if (npoints > 0) {
			for (int i = 0; i < level2D.npoints; i++)
	        {
	            sbf.append(StringUtil.toString((double) level2D.ypoints[i]));
	            sbf.append(sep);
	        }
		}
		return true;
	}
	
	public boolean csvImportXYDataFromRow(String[] data, int startAt) 
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
			level2D = new Level2D(x, y, npoints);
		}
		return true;
	}
	
	public boolean csvImportYDataFromRow(String[] data, int startAt) 
	{
		if (data.length < startAt)
			return false;
		
		int npoints = Integer.valueOf(data[startAt]);
		if (npoints > 0) {
			double[] x = new double[npoints];
			double[] y = new double[npoints];
			int offset = startAt+1;
			for (int i = 0; i < npoints; i++) { 
				x[i] = i;
				y[i] = Double.valueOf(data[offset]);
				offset++;
			}
			level2D = new Level2D(x, y, npoints);
		}
		return true;
	}

	
}
