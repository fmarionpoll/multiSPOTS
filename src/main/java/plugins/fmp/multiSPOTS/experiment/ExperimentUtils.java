package plugins.fmp.multiSPOTS.experiment;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import icy.roi.ROI2D;
import icy.type.geom.Polyline2D;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.kernel.roi.roi2d.ROI2DEllipse;
import plugins.kernel.roi.roi2d.ROI2DLine;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DShape;

public class ExperimentUtils {
	public static void transferCamDataROIStoCapillaries(Experiment exp) {
		if (exp.capillaries == null)
			exp.capillaries = new CapillariesArray();

		// rois not in cap? add
		List<ROI2D> listROISCap = ROI2DUtilities.getROIs2DContainingString("line", exp.seqCamData.seq);
		for (ROI2D roi : listROISCap) {
			boolean found = false;
			for (Capillary cap : exp.capillaries.capillariesList) {
				if (cap.getRoi() != null && roi.getName().equals(cap.getRoiName())) {
					found = true;
					break;
				}
			}
			if (!found)
				exp.capillaries.capillariesList.add(new Capillary((ROI2DShape) roi));
		}

		// cap with no corresponding roi? remove
		Iterator<Capillary> iterator = exp.capillaries.capillariesList.iterator();
		while (iterator.hasNext()) {
			Capillary cap = iterator.next();
			boolean found = false;
			for (ROI2D roi : listROISCap) {
				if (roi.getName().equals(cap.getRoiName())) {
					found = true;
					break;
				}
			}
			if (!found)
				iterator.remove();
		}
	}

	public static void transferCamDataROIStoSpots(Experiment exp) {
		if (exp.capillaries == null)
			exp.capillaries = new CapillariesArray();

		List<ROI2D> listROISCap = ROI2DUtilities.getROIs2DContainingString("spot", exp.seqCamData.seq);
		for (ROI2D roi : listROISCap) {
			boolean found = false;
			for (Spot spot : exp.spotsArray.spotsList) {
				if (spot.getRoi_in() != null && roi.getName().equals(spot.getRoiName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				ROI2DPolygon roi_new = new ROI2DPolygon();
				exp.spotsArray.spotsList.add(new Spot(roi_new));
			}
		}

		// cap with no corresponding roi? remove
		Iterator<Spot> iterator = exp.spotsArray.spotsList.iterator();
		while (iterator.hasNext()) {
			Spot cap = iterator.next();
			boolean found = false;
			for (ROI2D roi : listROISCap) {
				if (roi.getName().equals(cap.getRoiName())) {
					found = true;
					break;
				}
			}
			if (!found)
				iterator.remove();
		}
	}

	public static void transferCapillariesToCamData(Experiment exp) {
		if (exp.capillaries == null)
			return;

		List<ROI2D> listROISCap = ROI2DUtilities.getROIs2DContainingString("line", exp.seqCamData.seq);
		// roi with no corresponding cap? add ROI
		for (Capillary cap : exp.capillaries.capillariesList) {
			boolean found = false;
			for (ROI2D roi : listROISCap) {
				if (roi.getName().equals(cap.getRoiName())) {
					found = true;
					break;
				}
			}
			if (!found)
				exp.seqCamData.seq.addROI(cap.getRoi());
		}
	}

	public static void transferSpotsToCamData(Experiment exp) {
		if (exp.spotsArray == null)
			return;

		List<ROI2D> listROISSpots = ROI2DUtilities.getROIs2DContainingString("spot", exp.seqCamData.seq);
		// roi with no corresponding cap? add ROI
		for (Spot cap : exp.spotsArray.spotsList) {
			boolean found = false;
			for (ROI2D roi : listROISSpots) {
				if (roi.getName().equals(cap.getRoiName())) {
					found = true;
					break;
				}
			}
			if (!found)
				exp.seqCamData.seq.addROI(cap.getRoi_in());
		}
	}

	public static void transformPolygon2DROISintoSpots(Experiment exp, int radius) {
		ROI2DUtilities.removeRoisContainingString(-1, "spot", exp.seqCamData.seq);
		List<ROI2D> listROISCap = ROI2DUtilities.getROIs2DContainingString("line", exp.seqCamData.seq);
		if (listROISCap.size() < 1)
			return;

		exp.spotsArray.deleteAllSpots();
		exp.spotsArray = new SpotsArray();

		int spotIndex = 0;
		int cageIndex = 0;
		for (ROI2D roi : listROISCap) {
			String baseName = roi.getName();
			String substring = baseName.length() > 2 ? baseName.substring(baseName.length() - 2) + "_" : baseName + "_";

			ArrayList<Point2D> centers = new ArrayList<Point2D>();
			if (roi instanceof ROI2DLine) {
				Line2D line = ((ROI2DLine) roi).getLine();
				centers.add(line.getP1());
				centers.add(line.getP2());
			} else if (roi instanceof ROI2DPolyLine) {
				Polyline2D polyline = ((ROI2DPolyLine) roi).getPolyline2D();
				for (int i = 0; i < polyline.npoints; i++) {
					centers.add(new Point2D.Double(polyline.xpoints[i], polyline.ypoints[i]));
				}
			} else
				continue;

			double delta = Math.sqrt(2. * radius * radius) / 2;
			delta = radius;
			int i = 0;
			for (Point2D point : centers) {
				double x = point.getX() - delta;
				double y = point.getY() - delta;
				Ellipse2D ellipse = new Ellipse2D.Double(x, y, 2 * radius, 2 * radius);
				ROI2DEllipse roiEllipse = new ROI2DEllipse(ellipse);
				roiEllipse.setName("spot" + substring + String.format("%02d", i));
				i++;

				Spot spot = new Spot(roiEllipse);
				spot.spotIndex = spotIndex;
				spot.cageIndex = cageIndex;
				spot.radius = radius;
				spot.setSpotRoi_InColorAccordingToSpotIndex();
				exp.spotsArray.spotsList.add(spot);
				spotIndex++;
			}
			cageIndex++;
		}

//		// TODO remove the lines?
//		for (ROI2D roi:listROISCap) 
//			exp.seqCamData.seq.removeROI(roi);

	}

}
