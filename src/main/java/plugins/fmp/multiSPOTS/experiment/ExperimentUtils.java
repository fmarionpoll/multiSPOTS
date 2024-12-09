package plugins.fmp.multiSPOTS.experiment;

import java.util.Iterator;
import java.util.List;

import icy.roi.ROI2D;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.kernel.roi.roi2d.ROI2DPolygon;

public class ExperimentUtils {

	public static void transferCamDataROIStoSpots(Experiment exp) {
		if (exp.spotsArray == null)
			exp.spotsArray = new SpotsArray();

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

	public static void transferSpotsToCamDataSequence(Experiment exp) {
		if (exp.spotsArray == null)
			return;

		List<ROI2D> listROISSpots = ROI2DUtilities.getROIs2DContainingString("spot", exp.seqCamData.seq);
		// roi with no corresponding cap? add ROI
		for (Spot spot : exp.spotsArray.spotsList) {
			boolean found = false;
			for (ROI2D roi : listROISSpots) {
				if (roi.getName().equals(spot.getRoiName())) {
					found = true;
					break;
				}
			}
			if (!found)
				exp.seqCamData.seq.addROI(spot.getRoi_in());
		}
	}

}
