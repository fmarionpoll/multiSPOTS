package plugins.fmp.multiSPOTS.dlg.spotsMeasures;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import icy.gui.util.GuiUtil;
import icy.roi.ROI2D;
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
		ROI2D roi = seqKymos.seq.getSelectedROI2D();
		if (roi == null)
			return;
		
		int t = seqKymos.seq.getFirstViewer().getPositionT();
		Spot spot = exp.spotsArray.spotsList.get(t);
		String optionSelected = (String) roiTypeCombo.getSelectedItem();
		if (optionSelected .contains("sum")) 
			removeAndUpdate(seqKymos, spot, spot.sum, roi);
		else if (optionSelected.contains("clean"))
			removeAndUpdate(seqKymos, spot, spot.sumClean, roi);
		else if (optionSelected.contains("fly"))
			removeAndUpdate(seqKymos, spot, spot.flyPresent, roi);
	}
	
	private void removeAndUpdate(SequenceKymos seqKymos, Spot spot, SpotMeasure spotMeasure, ROI2D roi) 
	{
		removeMeasuresEnclosedInRoi(spotMeasure, roi);
		seqKymos.updateROIFromSpotsMeasure(spot, spotMeasure);
	}
	
	void removeMeasuresEnclosedInRoi(SpotMeasure spotMeasure, ROI2D roi) 
	{
		Polyline2D polyline = spotMeasure.getLevel2D();
		int npointsOutside = polyline.npoints - getPointsWithinROI(polyline, roi);
		if (npointsOutside > 0) {
			double [] xpoints = new double [npointsOutside];
			double [] ypoints = new double [npointsOutside];
			int index = 0;
			for (int i = 0; i < polyline.npoints; i++) {
				if (!isInside[i]) {
					xpoints[index] = polyline.xpoints[i];
					ypoints[index] = polyline.ypoints[i];
					index++;
				}
			}
			spotMeasure.setLevel2D(new Level2D(xpoints, ypoints, npointsOutside));	
		} 
		else {
			spotMeasure.setLevel2D(null);
		}
	}
	
	int getPointsWithinROI(Polyline2D polyline, ROI2D roi) 
	{
		isInside = new boolean [polyline.npoints];
		int npointsInside= 0;
		for (int i=0; i< polyline.npoints; i++) {
			isInside[i] = (roi.contains(polyline.xpoints[i], polyline.ypoints[i]));
			npointsInside += isInside[i]? 1: 0;
		}
		return npointsInside;
	}

}
