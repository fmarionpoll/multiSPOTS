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
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.tools.JComponents.JComboBoxMs;



public class Intervals extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5739112045358747277L;
	Long 		val 				= 0L; //set your own value, I used to check if it works
	Long 		min 				= 0L;
	Long 		max 				= 10000L;
	Long 		step 				= 1L;
	Long 		maxLast 			= 99999999L;
	JSpinner 	frameFirstJSpinner	= new JSpinner(new SpinnerNumberModel(val, min, max, step)); 
	JSpinner 	frameLastJSpinner	= new JSpinner(new SpinnerNumberModel(maxLast, step, maxLast, step));
	JSpinner 	binSizeJSpinner		= new JSpinner(new SpinnerNumberModel(1., 0., 1000., 1.));
	JComboBoxMs	binUnit 			= new JComboBoxMs();
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
		applyButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) {
				Experiment exp =(Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) 
					setExptParmsFromDialog(exp);
			}});
			
		refreshButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) {
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null)
					refreshBinSize(exp);
			}});
	}
	
	private void setExptParmsFromDialog(Experiment exp) 
	{
		exp.camImageBin_ms 	= (long) (((double) binSizeJSpinner.getValue())* binUnit.getMsUnitValue());
		long bin_ms 		= exp.camImageBin_ms;
		exp.binT0 			= (long) frameFirstJSpinner.getValue();
		exp.binFirst_ms 	=  exp.binT0 * bin_ms;
		exp.binLast_ms 		= ((long) frameLastJSpinner.getValue()) * bin_ms;
	}
	
	public void displayCamDataIntervals (Experiment exp) 
	{
		refreshBinSize(exp);
		long bin_ms = exp.camImageBin_ms;
		long dFirst = (long) exp.binFirst_ms/bin_ms;
		frameFirstJSpinner.setValue(dFirst);
		if(exp.binLast_ms <= 0)
			exp.binLast_ms = (long) (exp.getSeqCamSizeT() * bin_ms);
		long dLast = (long) exp.binLast_ms/bin_ms;
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
