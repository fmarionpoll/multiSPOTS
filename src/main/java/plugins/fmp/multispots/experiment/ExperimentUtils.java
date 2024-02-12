package plugins.fmp.multispots.experiment;



import java.util.Iterator;
import java.util.List;
import icy.roi.ROI2D;
import plugins.fmp.multispots.tools.ROI2DUtilities;
import plugins.kernel.roi.roi2d.ROI2DShape;


public class ExperimentUtils 
{
	public static void transferCamDataROIStoCapillaries (Experiment exp)
	{
		if (exp.capillaries == null) 
		{
			exp.capillaries = new Capillaries();
		}
		
		// rois not in cap? add
		List<ROI2D> listROISCap = ROI2DUtilities.getROIs2DContainingString ("line", exp.seqCamData.seq);
		for (ROI2D roi:listROISCap) 
		{
			boolean found = false;
			for (Capillary cap: exp.capillaries.capillariesList) 
			{
				if (cap.getRoi()!= null && roi.getName().equals(cap.getRoiName())) 
				{
					found = true;
					break;
				}
			}
			if (!found)
				exp.capillaries.capillariesList.add(new Capillary((ROI2DShape)roi));
		}
		
		// cap with no corresponding roi? remove
		Iterator<Capillary> iterator = exp.capillaries.capillariesList.iterator();
		while(iterator.hasNext()) 
		{
			Capillary cap = iterator.next();
			boolean found = false;
			for (ROI2D roi:listROISCap) 
			{
				if (roi.getName().equals(cap.getRoiName())) 
				{
					found = true;
					break;
				}
			}
			if (!found)
				iterator.remove();
		}
	}
	
	public static void transferCapillariesToCamData (Experiment exp) 
	{
		if (exp.capillaries == null)
			return;
		List<ROI2D> listROISCap = ROI2DUtilities.getROIs2DContainingString ("line", exp.seqCamData.seq);
		// roi with no corresponding cap? add ROI
		for (Capillary cap: exp.capillaries.capillariesList) 
		{
			boolean found = false;
			for (ROI2D roi:listROISCap) {
				if (roi.getName().equals(cap.getRoiName())) 
				{
					found = true;
					break;
				}
			}
			if (!found)
				exp.seqCamData.seq.addROI(cap.getRoi());
		}
	}
	
}
