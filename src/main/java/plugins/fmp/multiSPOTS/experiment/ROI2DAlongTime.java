package plugins.fmp.multiSPOTS.experiment;

import java.awt.Point;
import java.util.ArrayList;

import org.w3c.dom.Node;

import icy.file.xml.XMLPersistent;
import icy.roi.BooleanMask2D;
import icy.roi.ROI2D;
import icy.util.XMLUtil;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;

public class ROI2DAlongTime implements XMLPersistent {
	private int index = 0;
	private ROI2D roi = null;
	private long t = 0;
	private ArrayList<ArrayList<int[]>> masksList = null;
	private BooleanMask2D mask2D = null;
	private int mask2D_n_valid_points = 0;
	public Point[] mask2DPoints = null;

	private final String ID_META = "metaT";
	private final String ID_INDEX = "indexT";
	private final String ID_START = "startT";

	public ROI2DAlongTime(long t, ROI2D roi) {
		setRoi(roi);
		this.t = t;
	}

	public ROI2DAlongTime() {
	}

	public long getT() {
		return t;
	}

	public ROI2D getRoi() {
		return roi;
	}

	public ArrayList<ArrayList<int[]>> getMasksList() {
		return masksList;
	}

	public void setT(long t) {
		this.t = t;
	}

	public void setRoi(ROI2D roi) {
		this.roi = (ROI2D) roi.getCopy();
	}

	public void setMasksList(ArrayList<ArrayList<int[]>> masksList) {
		this.masksList = masksList;
	}

	public void buildMask2DFromRoi() {
		try {
			mask2D = roi.getBooleanMask2D(0, 0, 1, true); // z, t, c, inclusive
			mask2DPoints = mask2D.getPoints();
			int length = mask2DPoints.length;
			mask2D_n_valid_points = 0;
			for (int i = 0; i < length; i++) {
				if (mask2D.mask[i])
					mask2D_n_valid_points++;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getMask2D_N_Valid_Points() {
		return mask2D_n_valid_points;
	}

	public BooleanMask2D getMask2D() {
		return mask2D;
	}

	@Override
	public boolean loadFromXML(Node node) {
		final Node nodeMeta = XMLUtil.getElement(node, ID_META);
		if (nodeMeta == null)
			return false;

		index = XMLUtil.getElementIntValue(nodeMeta, ID_INDEX, 0);
		t = XMLUtil.getElementLongValue(nodeMeta, ID_START, 0);
		roi = ROI2DUtilities.loadFromXML_ROI(nodeMeta);
		return true;
	}

	@Override
	public boolean saveToXML(Node node) {
		final Node nodeMeta = XMLUtil.setElement(node, ID_META);
		if (nodeMeta == null)
			return false;
		XMLUtil.setElementIntValue(nodeMeta, ID_INDEX, index);
		XMLUtil.setElementLongValue(nodeMeta, ID_START, t);
		ROI2DUtilities.saveToXML_ROI(nodeMeta, roi);
		return true;
	}

}
