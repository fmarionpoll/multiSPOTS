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
	private static final long 	serialVersionUID = 2580935598417087197L;
	private JComboBox<String> 	roiTypeCombo 	= new JComboBox<String> (new String[] 
								{"sum", "clean", "fly present/absent"});
	private JButton 			cutAndInterpolateButton = new JButton("Cut & interpolate");
	private JButton 			buildMedianButton 	= new JButton("Build Median");
//	private JButton 			restoreButton 	= new JButton("Restore");
//	private JButton 			saveButton 		= new JButton("Save");
	private MultiSPOTS 			parent0			= null;
	
	
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{
		setLayout(capLayout);	
		this.parent0 = parent0;
		FlowLayout layoutLeft = new FlowLayout(FlowLayout.LEFT);
		layoutLeft.setVgap(0);
		
		JPanel panel1 = new JPanel(layoutLeft);
		panel1.add(cutAndInterpolateButton);
		panel1.add(new JLabel("Apply to ", SwingConstants.LEFT)); 
		panel1.add(roiTypeCombo);
		add(panel1);
		
		JPanel panel2 = new JPanel(layoutLeft);
		panel2.add(buildMedianButton);
		add(panel2);
		
		JPanel panel3 = new JPanel(layoutLeft);
//		panel3.add(restoreButton);
//		panel3.add(saveButton);
		add( panel3);

//		restoreButton.setEnabled(false);
//		saveButton.setEnabled(false);
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
		
		buildMedianButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					int imageHeight = exp.seqKymos.seq.getHeight();
					for (Spot spot: exp.spotsArray.spotsList) {
						spot.buildRunningMedianFromSumLevel2D(imageHeight);
					}
				}
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
		spotMeasure.transferROItoLevel2D();
	}
	
	void cutAndInterpolatePointsEnclosedInSelectedRoi(SpotMeasure spotMeasure, ROI2D roi) 
	{
		Polyline2D polyline = spotMeasure.getRoi().getPolyline2D();
		int first_pt_inside = -1;
		int last_pt_inside = -1;
		for (int i = 0; i < polyline.npoints; i++) {
			boolean isInside = roi.contains(polyline.xpoints[i], polyline.ypoints[i]);
			if (first_pt_inside < 0) {
				if (!isInside)
					continue; 
				else
					first_pt_inside = i;
				continue;
			}
			if (isInside) { 
				last_pt_inside = i;
				continue;
			}
			else
				last_pt_inside = i-1;
			if (first_pt_inside >= 0 && last_pt_inside >= 0) {
				extrapolateBetweenLimits(polyline, first_pt_inside, last_pt_inside);
				first_pt_inside = -1;
				last_pt_inside = -1;
			}
		}
		
		if (first_pt_inside >= 0 && last_pt_inside < 0) {
			extrapolateBetweenLimits(polyline, first_pt_inside, last_pt_inside);
		}
		spotMeasure.getRoi().setPolyline2D(polyline);	
	}
	
	void extrapolateBetweenLimits(Polyline2D polyline, int first_pt_inside, int last_pt_inside)
	{
		int first = first_pt_inside - 1;
		if (first < 0) first = 0;
		int last = last_pt_inside + 1;
		if (last >= polyline.npoints)
			last = polyline.npoints -1;
		if (last == 0)
			last = first;
		int npoints = last_pt_inside - first_pt_inside + 1;
		double deltaX = (polyline.xpoints[last] - polyline.xpoints[first])/npoints;
		double deltaY = (polyline.ypoints[last] - polyline.ypoints[first])/npoints;
		double startX = polyline.xpoints[first];
		double startY = polyline.ypoints[first];
		int k = 0;
		for (int j = first_pt_inside; j < last_pt_inside+1; j++, k++) {
			polyline.xpoints[j] = startX + deltaX * k;
			polyline.ypoints[j] = startY + deltaY * k;
		}
	}
	

}
