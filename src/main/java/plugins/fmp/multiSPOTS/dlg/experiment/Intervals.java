package plugins.fmp.multiSPOTS.dlg.experiment;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.dlg.JComponents.JComboMs;
import plugins.fmp.multiSPOTS.experiment.Experiment;



public class Intervals extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5739112045358747277L;
	JSpinner 	frameFirstJSpinner	= new JSpinner(new SpinnerNumberModel(0., 0., 10000., 1.)); 
	JSpinner 	frameLastJSpinner	= new JSpinner(new SpinnerNumberModel(99999999., 1., 99999999., 1.));
	JSpinner 	binSizeJSpinner		= new JSpinner(new SpinnerNumberModel(1., 0., 1000., 1.));
	JComboMs 	binUnit 			= new JComboMs();
	JButton		applyButton 		= new JButton("Apply changes");
	JButton		refreshButton 		= new JButton("Refresh");
	private MultiSPOTS 	parent0 	= null;
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{
		setLayout(capLayout);
		this.parent0 = parent0;

		int bWidth = 50;
		int bHeight = 21;
		binSizeJSpinner.setPreferredSize(new Dimension(bWidth, bHeight));
		
		FlowLayout layout1 = new FlowLayout(FlowLayout.LEFT);
		layout1.setVgap(1);
		
		JPanel panel0 = new JPanel(layout1);
		panel0.add(new JLabel("Frame ", SwingConstants.RIGHT));
		panel0.add(frameFirstJSpinner);
		panel0.add(new JLabel(" to "));
		panel0.add(frameLastJSpinner);
		add(panel0);
		
		JPanel panel1 = new JPanel(layout1);
		panel1.add(new JLabel("Time between frames ", SwingConstants.RIGHT));
		panel1.add(binSizeJSpinner);
		panel1.add(binUnit);
		add(panel1);

		panel1.add(refreshButton);
		panel1.add(applyButton);
	
		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{
		applyButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{
				Experiment exp =(Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) 
					setExptParmsFromDialog(exp);
			}});
			
		refreshButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null)
					refreshBinSize(exp);
			}});
	}
	
	private void setExptParmsFromDialog(Experiment exp) {
		exp.camImageBin_ms = (long) (((double) binSizeJSpinner.getValue())* binUnit.getMsUnitValue());
		double bin_ms = exp.camImageBin_ms;
		exp.binFirst_ms = (long) ((double) frameFirstJSpinner.getValue() * bin_ms);
		exp.binLast_ms = (long) ((double) frameLastJSpinner.getValue() * bin_ms);
	}
	
	public void displayCamDataIntervals (Experiment exp) 
	{
		refreshBinSize(exp);
		
		double bin_ms = exp.camImageBin_ms;
		double dFirst = exp.binFirst_ms/bin_ms;
		frameFirstJSpinner.setValue(dFirst);
		if(exp.binLast_ms <= 0)
			exp.binLast_ms = (long) (exp.getSeqCamSizeT() * bin_ms);
		double dLast = exp.binLast_ms/bin_ms;
		frameLastJSpinner.setValue(dLast);
		exp.getFileIntervalsFromSeqCamData();
	}
	
	private void refreshBinSize(Experiment exp) 
	{
		exp.loadFileIntervalsFromSeqCamData();
		binUnit.setSelectedIndex(1);
		binSizeJSpinner.setValue(exp.camImageBin_ms/(double)binUnit.getMsUnitValue());
	}
}
