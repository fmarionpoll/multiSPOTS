package plugins.fmp.multiSPOTS.experiment;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import icy.util.StringUtil;



public class SpotMeasure 
{
	private Level2D 	level2D 			= new Level2D();
	private Level2D		leve2D_old			= new Level2D();
	public	double [] 	measureValues		= null;
	public 	boolean [] 	measureBooleans 	= null;
	
	private String		name 				= "noname";
	
	// -------------------------
	
	SpotMeasure(String name) 
	{
		this.setName(name);
	}
	
	public SpotMeasure(String name, List<Point2D> limit) 
	{
		this.setName(name);
		setLevel2D(new Level2D(limit));
	}
	
	void copyLevel2D(SpotMeasure sourceSpotMeasure) 
	{
		if (sourceSpotMeasure.getLevel2D() != null)
			setLevel2D(sourceSpotMeasure.getLevel2D().clone()); 
	}
	
	void clearLevel2D() 
	{
		setLevel2D(new Level2D());
	}

	void initLevel2D_fromValues(String name) 
	{
		this.setName(name);
		int ii_start = 0;
		int ii_end = measureValues.length-1;
		int npoints = ii_end-ii_start+1;

		double [] xpoints = new double [npoints];
		double [] ypoints = new double [npoints];
		int j = 0;
		for (int i = ii_start; i < ii_end; i++, j++) {
			xpoints[j] = i;
			ypoints[j] = measureValues[j];
		}
		setLevel2D(new Level2D(xpoints, ypoints, npoints));
	}
	
	void initLevel2D_fromBooleans(String name) 
	{
		this.setName(name);
		int xStart = 0;
		int xEnd = measureBooleans.length;
		int npoints = xEnd-xStart+1;
		double [] xpoints = new double [npoints];
		double [] ypoints = new double [npoints];
		int j= 0;
		for (int i = xStart; i < xEnd; i++, j++) {
			xpoints[j] = i;
			ypoints[j] = measureBooleans[j] ? 1d : 0d;
		}
		setLevel2D(new Level2D(xpoints, ypoints, npoints));
	}

	int getLevel2DNPoints() 
	{
		if (getLevel2D() == null)
			return 0;
		return getLevel2D().npoints;
	}

	Level2D getLevel2D() {
		return level2D;
	}

	void setLevel2D(Level2D level2d) {
		level2D = level2d;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	boolean isThereAnyMeasuresDone() 
	{
		return (getLevel2D() != null && getLevel2D().npoints > 0);
	}
	
	ArrayList<Double> getLevel2D_Y_subsampled(long seriesBinMs, long outputBinMs) 
	{
		if (getLevel2D() == null || getLevel2D().npoints == 0)
			return null;
		
		long maxMs = (getLevel2D().ypoints.length -1) * seriesBinMs;
		long npoints = (maxMs / outputBinMs)+1;
		ArrayList<Double> arrayDouble = new ArrayList<Double>((int) npoints);
		for (double iMs = 0; iMs <= maxMs; iMs += outputBinMs) {
			int index = (int) (iMs  / seriesBinMs);
			arrayDouble.add(getLevel2D().ypoints[index]);
		}
		return arrayDouble;
	}
	
	List<Double> getLevel2D_Y() 
	{
		if (getLevel2D() == null || getLevel2D().npoints == 0)
			return null;
		List<Double> arrayDouble = new ArrayList<Double>(getLevel2D().ypoints.length);
		for (double i: getLevel2D().ypoints)
			arrayDouble.add(i);
		return arrayDouble;
	}

	// ----------------------------------------------------------------------
	
	void adjustLevel2DToImageWidth(int imageWidth) 
	{
		if (getLevel2D() == null || getLevel2D().npoints == 0)
			return;
		int npoints = getLevel2D().npoints;
		int npoints_old = 0;
		if (leve2D_old != null && leve2D_old.npoints > npoints) 
			npoints_old = leve2D_old.npoints;
		if (npoints == imageWidth || npoints_old == imageWidth)
			return;
		
		// reduce polyline npoints to imageSize
		if (npoints > imageWidth) {
			int newSize = imageWidth;
			if (npoints < npoints_old)
				newSize = 1 + imageWidth *npoints / npoints_old;
			setLevel2D(getLevel2D().contractPolylineToNewWidth(newSize));
			if (npoints_old != 0)
				leve2D_old = leve2D_old.contractPolylineToNewWidth(imageWidth);
		}
		// expand polyline npoints to imageSize
		else { 
			int newSize = imageWidth;
			if (npoints < npoints_old)
				newSize = imageWidth *npoints / npoints_old;
			setLevel2D(getLevel2D().expandPolylineToNewWidth(newSize));
			if (npoints_old != 0)
				leve2D_old = leve2D_old.expandPolylineToNewWidth(imageWidth);
		}
	}

	void cropLevel2DToNPoints(int npoints) 
	{
		if (npoints >= getLevel2D().npoints)
			return;
		
		if (leve2D_old == null) 
			leve2D_old = getLevel2D().clone();
		
		setLevel2D(getLevel2D().cropPolylineToNewWidth(npoints));		
	}
	
	int restoreCroppedLevel2D()  
	{
		if (leve2D_old != null) 
			setLevel2D(leve2D_old.clone());
		return getLevel2D().npoints;
	}
	
	// ----------------------------------------------------------------------
	
	public boolean cvsExportXYDataToRow(StringBuffer sbf, String sep) 
	{
		int npoints = 0;
		if (getLevel2D() != null && getLevel2D().npoints > 0)
			npoints = getLevel2D().npoints; 
			
		sbf.append(Integer.toString(npoints)+ sep);
		if (npoints > 0) {
			for (int i = 0; i < getLevel2D().npoints; i++) {
	            sbf.append(StringUtil.toString((double) getLevel2D().xpoints[i]));
	            sbf.append(sep);
	            sbf.append(StringUtil.toString((double) getLevel2D().ypoints[i]));
	            sbf.append(sep);
	        }
		}
		return true;
	}
	
	public boolean cvsExportYDataToRow(StringBuffer sbf, String sep) 
	{
		int npoints = 0;
		if (getLevel2D() != null && getLevel2D().npoints > 0)
			npoints = getLevel2D().npoints; 
			
		sbf.append(Integer.toString(npoints)+ sep);
		if (npoints > 0) {
			for (int i = 0; i < getLevel2D().npoints; i++) {
	            sbf.append(StringUtil.toString((double) getLevel2D().ypoints[i]));
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
			setLevel2D(new Level2D(x, y, npoints));
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
			setLevel2D(new Level2D(x, y, npoints));
		}
		return true;
	}



	
}
