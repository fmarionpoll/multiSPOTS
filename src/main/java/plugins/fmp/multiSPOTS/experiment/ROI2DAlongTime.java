package plugins.fmp.multiSPOTS.experiment;

import java.util.ArrayList;

import org.w3c.dom.Node;

import icy.file.xml.XMLPersistent;
import icy.roi.ROI2D;
import icy.util.XMLUtil;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;

public class ROI2DAlongTime implements XMLPersistent 
{
	private int		index			= 0;
	private ROI2D 	roi 			= null;	
	private long 	start 			= 0;
	private ArrayList<ArrayList<int[]>> masksList = null;
	
	private final String ID_META 	= "metaT";
	private final String ID_INDEX	= "indexT";
	private final String ID_START 	= "startT";
	
	
	public ROI2DAlongTime(long start, ROI2D roi) {
		setRoi(roi);
		this.start = start;
	}
	
	public ROI2DAlongTime() {
	}

	public long getStart() {
		return start;
	}
	
	public ROI2D getRoi() {
		return roi;
	}
	
	public ArrayList<ArrayList<int[]>> getMasksList() {
		return masksList;
	}
	
	public void setStart(long start) {
		this.start = start;
	}
	
	public void setRoi(ROI2D roi) {
		this.roi = (ROI2D) roi.getCopy();
	}
	
	public void setMasksList(ArrayList<ArrayList<int[]>> masksList) {
		this.masksList = masksList;
	}

	@Override
	public boolean loadFromXML(Node node) {
		final Node nodeMeta = XMLUtil.getElement(node, ID_META);
	    if (nodeMeta == null)
	    	return false;
	    
    	index 	= XMLUtil.getElementIntValue(nodeMeta, ID_INDEX, 0);
        start 	= XMLUtil.getElementLongValue(nodeMeta, ID_START, 0);
        roi 	= ROI2DUtilities.loadFromXML_ROI(nodeMeta);
        return true;    
	}

	@Override
	public boolean saveToXML(Node node) {
		final Node nodeMeta = XMLUtil.setElement(node, ID_META);
	    if (nodeMeta == null) 
	    	return false;
	    XMLUtil.setElementIntValue(nodeMeta, ID_INDEX, index);
        XMLUtil.setElementLongValue(nodeMeta, ID_START, start);
        ROI2DUtilities.saveToXML_ROI(nodeMeta, roi);
		return true;
	}
}
