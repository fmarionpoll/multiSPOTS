package plugins.fmp.multiSPOTS.tools.ROI2D;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.type.geom.Polygon2D;
import icy.type.geom.Polyline2D;
import icy.util.XMLUtil;

import plugins.kernel.roi.roi2d.ROI2DLine;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DShape;

import plugins.fmp.multiSPOTS.experiment.SequenceKymos;
import plugins.fmp.multiSPOTS.tools.Comparators;





public class ROI2DUtilities  
{
	private static final String ID_ROIMC = "roiMC";
    
	
	public static void interpolateMissingPointsAlongXAxis (ROI2DPolyLine roiLine, int nintervals) 
	{
		if (nintervals <= 1)
			return;
		// interpolate points so that each x step has a value	
		// assume that points are ordered along x
		Polyline2D polyline = roiLine.getPolyline2D();
		int roiLine_npoints = polyline.npoints;
		if (roiLine_npoints == 0)
			return;
		
		if (roiLine_npoints > nintervals)
			roiLine_npoints = nintervals;

		List<Point2D> pts = new ArrayList <Point2D>(roiLine_npoints);
		double ylast = polyline.ypoints[roiLine_npoints-1];
		int xfirst0 = (int) polyline.xpoints[0];
		
		for (int i=1; i< roiLine_npoints; i++) {			
			int xfirst = (int) polyline.xpoints[i-1];
			if (xfirst < 0)
				xfirst = 0;
			int xlast = (int) polyline.xpoints[i];
			if (xlast > xfirst0 + nintervals -1)
				xlast = xfirst0 + nintervals -1;
			double yfirst = polyline.ypoints[i-1];
			ylast = polyline.ypoints[i]; 
			for (int j = xfirst; j< xlast; j++) {
				int val = (int) (yfirst + (ylast-yfirst)*(j-xfirst)/(xlast-xfirst));
				Point2D pt = new Point2D.Double(j, val);
				pts.add(pt);
			}
		}
		Point2D pt = new Point2D.Double(polyline.xpoints[roiLine_npoints-1], ylast);
		pts.add(pt);
		roiLine.setPoints(pts);
	}

	private static List<Integer> transferROIYpointsToIntList(ROI2DPolyLine roiLine) 
	{
		Polyline2D line = roiLine.getPolyline2D();
		List<Integer> intArray = new ArrayList<Integer> (line.npoints);
		for (int i=0; i< line.npoints; i++) 
			intArray.add((int) line.ypoints[i]);
		return intArray;
	}
	
	public static void mergeROIsListNoDuplicate(List<ROI2D> seqList, List<ROI2D> listRois, Sequence seq) 
	{
		if (seqList.isEmpty()) {
			for (ROI2D roi : listRois)
				if (roi != null)
					seqList.add(roi);
		}
		
		for (ROI2D seqRoi: seqList) {
			Iterator <ROI2D> iterator = listRois.iterator();
			while(iterator.hasNext()) {
				ROI2D roi = iterator.next();
				if (seqRoi == roi )
					iterator.remove();
				else if (seqRoi.getName().equals (roi.getName() )) {
					seqRoi.copyFrom(roi);
					iterator.remove();
				}
			}
		}
	}
	
	public static void removeROIsMissingChar(List<ROI2D> listRois, char character) 
	{
		Iterator <ROI2D> iterator = listRois.iterator();
		while(iterator.hasNext()) {
			ROI2D roi = iterator.next();
			if (roi.getName().indexOf(character ) < 0) 
				iterator.remove();
		}
	}
	
	public static ROI2DPolyLine transfertDataArrayToROI(List<Integer> intArray) 
	{
		Polyline2D line = new Polyline2D();
		for (int i =0; i< intArray.size(); i++) {
			Point2D pt = new Point2D.Double(i, intArray.get(i));
			line.addPoint(pt);
		}
		return new ROI2DPolyLine(line);
	}
	
	public static List<Integer> copyFirstROIMatchingFilterToDataArray (SequenceKymos seqKymos, String filter) 
	{
		List<ROI2D> listRois = seqKymos.seq.getROI2Ds();
		int width = seqKymos.seq.getWidth();
		for (ROI2D roi: listRois)  {
			if (roi.getName().contains(filter)) 
			{ 
				interpolateMissingPointsAlongXAxis ((ROI2DPolyLine)roi, width);
				return transferROIYpointsToIntList((ROI2DPolyLine)roi);
			}
		}
		return null;
	}
		
	public static List<ROI2D> loadROIsFromXML(Document doc) 
	{
		List<ROI> localList = ROI.loadROIsFromXML(XMLUtil.getRootElement(doc));
		List<ROI2D> finalList = new ArrayList<ROI2D>(localList.size());
		for (ROI roi: localList)
			finalList.add((ROI2D) roi);
		return finalList;		
	}
	
	public static List<ROI2D> getROIs2DContainingString (String string, Sequence seq) 
	{
		List<ROI2D> roiList = seq.getROI2Ds();
		Collections.sort(roiList, new Comparators.ROI2D_Name_Comparator());
		List<ROI2D> listROIsMatchingString = new ArrayList<ROI2D>();
		for ( ROI2D roi : roiList ) {
			if ((roi instanceof ROI2DShape) && roi.getName().contains(string)) 
				listROIsMatchingString.add(roi);
		}
		return listROIsMatchingString;
	}
	
	public static List<ROI2DShape> getROIs2DAreaContainingString (String string, Sequence seq) 
	{
		List<ROI2D> roiList = seq.getROI2Ds();
		Collections.sort(roiList, new Comparators.ROI2D_Name_Comparator());
		List<ROI2DShape> listROIsMatchingString = new ArrayList<ROI2DShape>();
		for ( ROI2D roi : roiList ) {
			if ((roi instanceof ROI2DShape) && roi.getName().contains(string)) 
				listROIsMatchingString.add((ROI2DShape) roi);
		}
		return listROIsMatchingString;
	}
	
	public static void removeRoisContainingString(int t, String string, Sequence seq) 
	{
		for (ROI roi: seq.getROIs()) {
			if (roi instanceof ROI2D 
					&& roi.getName().contains(string)
					&&  (t < 0 || ((ROI2D) roi).getT() == t ))
				seq.removeROI(roi);
		}
	}

	public static Polygon2D getPolygonEnclosingSpots(ArrayList<ROI2D> listRois) 
	{
		ROI2D roi1 = listRois.get(0);
		Rectangle rect0 = roi1.getBounds();
		int x0 = (int) rect0.getX();
		int y0 = (int) rect0.getY();
		int width0 = (int) rect0.getWidth();
		int height0 = (int) rect0.getHeight();
		
		for (ROI2D roi : listRois) {
			if (!roi.getName().contains("spot")) 
				continue;
			Rectangle rect1 = roi.getBounds();
			int x1 = (int) rect1.getX();
			int y1 = (int) rect1.getY();
			int width1 = x1 - x0 +(int) rect1.getWidth();
			int height1 = y1 - y0 + (int) rect1.getHeight();
			if (width1 > width0) width0 = width1;
			if (height1 > height0) height0 = height1;
		}
		return new Polygon2D(new Rectangle2D.Double(x0, y0, width0, height0)); 
	}

	public static ArrayList<Point2D> getPoints2DArrayFromROI2D (ROI2D roi) 
	{
		ArrayList<Point2D> points = new ArrayList<Point2D>();		
		if (roi instanceof ROI2DPolyLine) {
			Polyline2D line = ((ROI2DPolyLine) roi).getPolyline2D();
			for (int i = 0; i < line.npoints; i++) {
				Point2D pt = new Point2D.Double(line.xpoints[i],  line.ypoints[i]);
				points.add(pt);
			}
		} 
		else if (roi instanceof ROI2DLine) {
			Line2D line = ((ROI2DLine) roi).getLine();
			Point2D pt = new Point2D.Double(line.getP1().getX(),  line.getP1().getY());
			points.add(pt);
			pt = new Point2D.Double(line.getP2().getX(),  line.getP2().getY());
			points.add(pt);
		}
		return points;
	}
	
	public static void saveToXML_ROI(Node node, ROI2D roi) 
	{
		final Node nodeROI = XMLUtil.setElement(node, ID_ROIMC);
        if (!roi.saveToXML(nodeROI)) {
            XMLUtil.removeNode(node, nodeROI);
            System.err.println("Error: the roi " + roi.getName() + " was not correctly saved to XML !");
        }
	}
	
	public static ROI2D loadFromXML_ROI(Node node) 
	{
		final Node nodeROI = XMLUtil.getElement(node, ID_ROIMC);
        if (nodeROI != null) {
			ROI2D roi = (ROI2D) ROI2D.createFromXML(nodeROI);
	        return roi;
        }
        return null;
	}
 
}
