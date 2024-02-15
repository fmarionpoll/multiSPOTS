package plugins.fmp.multispots.experiment;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.util.XMLUtil;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DRectangle;
import icy.type.geom.Polygon2D;



public class Cage 
{
	public ROI2D 		cageRoi2D				= null;
	public BooleanMask2D cageMask2D				= null;
	public XYTaSeriesArrayList 	flyPositions 	= new XYTaSeriesArrayList();
	public int 			cageNFlies  			= 0;
	public int 			cageAge 				= 5;
	public String 		strCageComment 			= "..";
	public String 		strCageSex 				= "..";
	public String 		strCageStrain 			= "..";
	private String 		strCageNumber 			= null;
	public	boolean		valid					= false;
	public	boolean		bDetect					= true;
	public  boolean 	initialflyRemoved		= false;
	
	private final String ID_CAGELIMITS 			= "CageLimits";
	private final String ID_FLYPOSITIONS		= "FlyPositions";
	private final String ID_NFLIES 				= "nflies"; 
	private final String ID_AGE 				= "age"; 
	private final String ID_COMMENT				= "comment";
	private final String ID_SEX					= "sex";
	private final String ID_STRAIN				= "strain";
	
	
	
	public boolean xmlSaveCage (Node node, int index) 
	{
		if (node == null)
			return false;
		Element xmlVal = XMLUtil.addElement(node, "Cage"+index);		
		xmlSaveCageLimits(xmlVal);
		xmlSaveCageParameters(xmlVal);
		if (cageNFlies > 0)
			xmlSaveFlyPositions(xmlVal);
		return true;
	}
	
	public boolean xmlSaveCageParameters(Element xmlVal) 
	{
		XMLUtil.setElementIntValue(xmlVal, ID_NFLIES, cageNFlies);
		XMLUtil.setElementIntValue(xmlVal, ID_AGE, cageAge);
		XMLUtil.setElementValue(xmlVal, ID_COMMENT, strCageComment);
		XMLUtil.setElementValue(xmlVal, ID_SEX, strCageSex);
		XMLUtil.setElementValue(xmlVal, ID_STRAIN, strCageStrain);
		return true;
	}
	
	public boolean xmlSaveCageLimits(Element xmlVal) 
	{
		Element xmlVal2 = XMLUtil.addElement(xmlVal, ID_CAGELIMITS);
		if (cageRoi2D != null) 
		{
			cageRoi2D.setSelected(false);
			cageRoi2D.saveToXML(xmlVal2);
		}
		return true;
	}
	
	public String csvExportCageDescription() 
	{	
		StringBuffer sbf = new StringBuffer();
		List<String> row = new ArrayList<String>();
		row.add(strCageNumber);
		row.add(cageRoi2D.getName());
		row.add(Integer.toString(cageNFlies));
		row.add(Integer.toString(cageAge));
		row.add(strCageComment);
		row.add(strCageStrain);
		row.add(strCageSex);
		
		int npoints = 0;
		if (cageRoi2D != null) 
		{			
			Polygon2D polygon = ((ROI2DPolygon) cageRoi2D).getPolygon2D();
			row.add(Integer.toString(polygon.npoints));
			for (int i= 0; i< npoints; i++) {
				row.add(Integer.toString((int) polygon.xpoints[i]));
				row.add(Integer.toString((int) polygon.ypoints[i]));
			}
		}
		else
			row.add("0");
		sbf.append(String.join(",", row));
		sbf.append("\n");
		return sbf.toString();
	}
	
	public boolean xmlSaveFlyPositions(Element xmlVal) 
	{
		Element xmlVal2 = XMLUtil.addElement(xmlVal, ID_FLYPOSITIONS);
		flyPositions.saveXYTseriesToXML(xmlVal2);
		return true;
	}
	
	public boolean xmlLoadCage (Node node, int index) 
	{
		if (node == null)
			return false;
		Element xmlVal = XMLUtil.getElement(node, "Cage"+index);
		if (xmlVal == null)
			return false;
		xmlLoadCageLimits(xmlVal);
		xmlLoadCageParameters(xmlVal);
		xmlLoadFlyPositions(xmlVal); 
		return true;
	}
	
	public boolean xmlLoadCageLimits (Element xmlVal) 
	{
		Element xmlVal2 = XMLUtil.getElement(xmlVal, ID_CAGELIMITS);
		if (xmlVal2 != null) 
		{
			cageRoi2D = (ROI2D) ROI.createFromXML(xmlVal2 );
	        cageRoi2D.setSelected(false);
		}
		return true;
	}
	
	public boolean xmlLoadCageParameters (Element xmlVal) 
	{
		cageNFlies 		= XMLUtil.getElementIntValue(xmlVal, ID_NFLIES, cageNFlies);
		cageAge 		= XMLUtil.getElementIntValue(xmlVal, ID_AGE, cageAge);
		strCageComment 	= XMLUtil.getElementValue(xmlVal, ID_COMMENT, strCageComment);
		strCageSex 		= XMLUtil.getElementValue(xmlVal, ID_SEX, strCageSex);
		strCageStrain 	= XMLUtil.getElementValue(xmlVal, ID_STRAIN, strCageStrain);
		return true;
	}
	
	public boolean xmlLoadFlyPositions(Element xmlVal) 
	{
		Element xmlVal2 = XMLUtil.getElement(xmlVal, ID_FLYPOSITIONS);
		if (xmlVal2 != null) 
		{
			flyPositions.loadXYTseriesFromXML(xmlVal2);
			return true;
		}
		return false;
	}

	// ------------------------------------
	
	public String getCageNumber() 
	{
		if (strCageNumber == null) 
			strCageNumber = cageRoi2D.getName().substring(cageRoi2D.getName().length() - 3);
		return strCageNumber;
	}
	
	public int getCageNumberInteger() 
	{
		int cagenb = -1;
		strCageNumber = getCageNumber();
		if (strCageNumber != null) 
		{
			try 
			{
			    return Integer.parseInt(strCageNumber);
			} 
			catch (NumberFormatException e) 
			{
			    return cagenb;
			}
		}
		return cagenb;
	}
	
	public void clearMeasures () 
	{
		flyPositions.clear();
	}
	
	public Point2D getCenterTopCage() 
	{
		Rectangle2D rect = cageRoi2D.getBounds2D();
		Point2D pt = new Point2D.Double(rect.getX() + rect.getWidth()/2, rect.getY());
		return pt;
	}
	
	public Point2D getCenterTipCapillaries(SpotsArray capList) 
	{
		List<Point2D> listpts = new ArrayList<Point2D>();
		for (Spot cap: capList.spotsList) 
		{
			Point2D pt = cap.getCapillaryTipWithinROI2D(cageRoi2D);
			if (pt != null)
				listpts.add(pt);
		}
		double x = 0;
		double y = 0;
		int n = listpts.size();
		for (Point2D pt: listpts) 
		{
			x  += pt.getX();
			y += pt.getY();
		}
		Point2D pt = new Point2D.Double(x/n, y/n);
		return pt;
	}
	
	public void copyCage (Cage cage) 
	{
		cageRoi2D			= cage.cageRoi2D;
		cageNFlies  	= cage.cageNFlies;
		strCageComment 	= cage.strCageComment;
		strCageNumber 	= cage.strCageNumber;
		valid 			= false; 
		flyPositions.copyXYTaSeries(cage.flyPositions);
	}
	
	public ROI2DRectangle getRoiRectangleFromPositionAtT(int t) 
	{
		int nitems = flyPositions.xytArrayList.size();
		if (nitems == 0 || t >= nitems)
			return null;
		XYTaValue aValue = flyPositions.xytArrayList.get(t);
		
		ROI2DRectangle flyRoiR = new ROI2DRectangle(aValue.rectBounds);
		flyRoiR.setName("detR"+getCageNumber() +"_" + t );
		flyRoiR.setT( t );
		return flyRoiR;
	}
	
	public void transferRoisToPositions(List<ROI2D> detectedROIsList) 
	{
		String filter = "detR"+getCageNumber();
		for (ROI2D roi: detectedROIsList) 
		{
			String name = roi.getName();
			if (!name .contains(filter))
				continue;
			Rectangle2D rect = ((ROI2DRectangle) roi).getRectangle();
			int t = roi.getT();	
			flyPositions.xytArrayList.get(t).rectBounds = rect;
		}
	}
	
	public void computeCageBooleanMask2D() throws InterruptedException 
	{
		cageMask2D = cageRoi2D.getBooleanMask2D( 0 , 0, 1, true );
	}
	
	
}
