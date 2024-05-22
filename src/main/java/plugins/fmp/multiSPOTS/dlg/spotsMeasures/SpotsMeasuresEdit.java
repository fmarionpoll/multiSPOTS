package plugins.fmp.multiSPOTS.dlg.spotsMeasures;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import icy.gui.util.GuiUtil;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.type.geom.Polyline2D;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.Level2D;
import plugins.fmp.multiSPOTS.experiment.SequenceKymos;
import plugins.fmp.multiSPOTS.experiment.Spot;
import plugins.fmp.multiSPOTS.experiment.SpotMeasure;


public class SpotsMeasuresEdit  extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2580935598417087197L;
	private MultiSPOTS 			parent0;
	private boolean[] 			isInside		= null;
	private JComboBox<String> 	roiTypeCombo 	= new JComboBox<String> (new String[] 
			{"SUM", "sumCLEAN", "Present/absent", "all"});
	private JButton 			cutAndInterpolateButton 	= new JButton("Cut & interpolate");
	private JButton 			restoreButton 	= new JButton("Restore");
	
	
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{
		setLayout(capLayout);	
		this.parent0 = parent0;
		
		JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());
		panel1.add(new JLabel("Apply to ", SwingConstants.LEFT), BorderLayout.WEST); 
		panel1.add(roiTypeCombo, BorderLayout.CENTER);
		
		add(GuiUtil.besidesPanel(new JLabel(" "), panel1));
		add(GuiUtil.besidesPanel(new JLabel(" "), cutAndInterpolateButton));
		
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout()); 
		panel2.add(restoreButton, BorderLayout.EAST);
		add(GuiUtil.besidesPanel(new JLabel(" "), panel2));

		defineListeners();
	}
	
	private void defineListeners() 
	{
		cutAndInterpolateButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
					cutAndInterpolate(exp);
			}});
	}

	
//	int findLastXLeftOfRoi(Spot spot, ROI2D roiRef) 
//	{
//		int lastX = -1;
//		Rectangle2D rectRef = roiRef.getBounds2D();
//		double xleft = rectRef.getX();
//		
//		Polyline2D polyline = spot.ptsTop.polylineLevel;
//		for (int i=0; i < polyline.npoints; i++) 
//		{
//			if (polyline.xpoints[i] < xleft)
//				continue;
//			lastX = i-1;
//			break;
//		}
//		return lastX;
//	}
	
	void cutAndInterpolate(Experiment exp) 
	{
		SequenceKymos seqKymos = exp.seqKymos;
		int t = seqKymos.seq.getFirstViewer().getPositionT();
		ROI2D roi = seqKymos.seq.getSelectedROI2D();
		if (roi == null)
			return;
		
		seqKymos.transferKymosRoi_atT_ToCapillaries_Measures(t, exp.spotsArray);
		Spot spot = exp.spotsArray.spotsList.get(t);
		String optionSelected = (String) roiTypeCombo.getSelectedItem();
		if (optionSelected .contains("gulp")) 
		{
			seqKymos.removeROIsPolylineAtT(t);
			List<ROI2D> listOfRois = spot.transferMeasuresToROIs();
			seqKymos.seq.addROIs (listOfRois, false);
			for (ROI lroi: listOfRois)
				seqKymos.seq.roiChanged(lroi);
		} 
		else 
		{
			if (optionSelected .contains("SUM")) 
				removeAndUpdate(seqKymos, spot, spot.sum, roi);
			if (optionSelected.contains("CLEAN"))
				removeAndUpdate(seqKymos, spot, spot.sumClean, roi);
			if (optionSelected.contains("present"))
				removeAndUpdate(seqKymos, spot, spot.flyPresent, roi);
		}
	}
	
	private void removeAndUpdate(SequenceKymos seqKymos, Spot cap, SpotMeasure caplimits, ROI2D roi) 
	{
		removeMeasuresEnclosedInRoi(caplimits, roi);
		seqKymos.updateROIFromCapillaryMeasure(cap, caplimits);
	}
	
	void removeMeasuresEnclosedInRoi(SpotMeasure caplimits, ROI2D roi) 
	{
		Polyline2D polyline = caplimits.polylineLevel;
		int npointsOutside = polyline.npoints - getPointsWithinROI(polyline, roi);
		if (npointsOutside > 0) 
		{
			double [] xpoints = new double [npointsOutside];
			double [] ypoints = new double [npointsOutside];
			int index = 0;
			for (int i = 0; i < polyline.npoints; i++) 
			{
				if (!isInside[i]) 
				{
					xpoints[index] = polyline.xpoints[i];
					ypoints[index] = polyline.ypoints[i];
					index++;
				}
			}
			caplimits.polylineLevel = new Level2D(xpoints, ypoints, npointsOutside);	
		} 
		else 
		{
			caplimits.polylineLevel = null;
		}
	}
	
	int getPointsWithinROI(Polyline2D polyline, ROI2D roi) 
	{
		isInside = new boolean [polyline.npoints];
		int npointsInside= 0;
		for (int i=0; i< polyline.npoints; i++) 
		{
			isInside[i] = (roi.contains(polyline.xpoints[i], polyline.ypoints[i]));
			npointsInside += isInside[i]? 1: 0;
		}
		return npointsInside;
	}

}
