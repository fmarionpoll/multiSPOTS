package plugins.fmp.multiSPOTS.dlg.spotsMeasures;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import icy.roi.ROI2D;
import icy.type.geom.Polyline2D;

import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
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
	private JComboBox<String> 	roiTypeCombo 	= new JComboBox<String> (new String[] 
			{"sum", "clean", "fly present/absent"});
	private JButton 			cutAndInterpolateButton = new JButton("Cut & interpolate");
	private JButton 			restoreButton 	= new JButton("Restore");
	private JButton 			saveButton 	= new JButton("Save");
	
	
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{
		setLayout(capLayout);	
		this.parent0 = parent0;
		FlowLayout layoutLeft = new FlowLayout(FlowLayout.LEFT);
		layoutLeft.setVgap(0);
		
		JPanel panel1 = new JPanel(layoutLeft);
		panel1.add(new JLabel("Apply to ", SwingConstants.LEFT)); 
		panel1.add(roiTypeCombo);
		add(panel1);
		
		JPanel panel2 = new JPanel(layoutLeft);
		panel2.add(cutAndInterpolateButton);
		add(panel2);
		
		JPanel panel3 = new JPanel(layoutLeft);
		panel3.add(restoreButton);
		panel3.add(saveButton);
		add( panel3);

		restoreButton.setEnabled(false);
		saveButton.setEnabled(false);
		defineListeners();
	}
	
	private void defineListeners() 
	{
		cutAndInterpolateButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
					cutAndInterpolate(exp);
			}});
	}
	
	void cutAndInterpolate(Experiment exp) 
	{
		SequenceKymos seqKymos = exp.seqKymos;
		ROI2D roiRect = seqKymos.seq.getSelectedROI2D();
		if (roiRect == null)
			return;
		
		int t = seqKymos.seq.getFirstViewer().getPositionT();
		Spot spot = exp.spotsArray.spotsList.get(t);
		String optionSelected = (String) roiTypeCombo.getSelectedItem();
		if (optionSelected .contains("sum")) 
			removeAndUpdate(seqKymos, spot, spot.sum, roiRect);
		else if (optionSelected.contains("clean"))
			removeAndUpdate(seqKymos, spot, spot.sumClean, roiRect);
		else if (optionSelected.contains("fly"))
			removeAndUpdate(seqKymos, spot, spot.flyPresent, roiRect);
	}
	
	private void removeAndUpdate(SequenceKymos seqKymos, Spot spot, SpotMeasure spotMeasure, ROI2D roi) 
	{
		cutAndInterpolatePointsEnclosedInSelectedRoi(spotMeasure, roi);
		//seqKymos.updateROIFromSpotsMeasure(spot, spotMeasure);
	}
	
	void cutAndInterpolatePointsEnclosedInSelectedRoi(SpotMeasure spotMeasure, ROI2D roi) 
	{
		Polyline2D polyline = spotMeasure.getRoi().getPolyline2D();
		int index0 = 0;
		int index1 = -1;
		for (int i = 0; i < polyline.npoints; i++) {
			boolean isInside = roi.contains(polyline.xpoints[i], polyline.ypoints[i]);
			if (index1 < 0 && !isInside) { 
				index0 = i;
				continue;
			}
			else if (isInside) { 
				index1 = i;
				continue;
			}
			else 
				break;
		}
		if (index1 > 0) {
			int npoints = index1 - index0 + 1;
			double deltaX = (polyline.xpoints[index1] - polyline.xpoints[index0])/npoints;
			double deltaY = (polyline.ypoints[index1] - polyline.ypoints[index0])/npoints;
			double startX = polyline.xpoints[index0];
			double startY = polyline.ypoints[index0];
			int i = 0;
			for (int j = index0; j < index1; j++, i++) {
				polyline.xpoints[j] = startX + deltaX * i;
				polyline.ypoints[j] = startY + deltaY * i;
			}
		}
		spotMeasure.getRoi().setPolyline2D(polyline);	
	}
	
	

}
