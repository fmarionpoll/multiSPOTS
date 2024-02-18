package plugins.fmp.multispots.dlg.kymos;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import plugins.fmp.multispots.MultiSPOTS;
import plugins.fmp.multispots.experiment.Experiment;




public class Intervals extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1530811745749103710L;
	private MultiSPOTS parent0 				= null;
	private 	JComboBox<String> binUnit 	= new JComboBox<String> (new String[] {"ms", "s", "min", "h", "day"});
	JButton		applyButton					= new JButton("Apply");
	JButton		getFromCamDataButton		= new JButton("Get from stack of images");
	JSpinner 	firstColumnJSpinner			= new JSpinner(new SpinnerNumberModel(0., 0., 10000., 1.)); 
	JSpinner 	lastColumnJSpinner			= new JSpinner(new SpinnerNumberModel(99999999., 0., 99999999., 1.));
	JSpinner 	binColumnJSpinner			= new JSpinner(new SpinnerNumberModel(1., 1., 1000., 1.));
	
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{
		setLayout(capLayout);
		this.parent0 = parent0;

		FlowLayout layout1 = new FlowLayout(FlowLayout.LEFT);
		layout1.setVgap(0);
		
		JPanel panel1 = new JPanel(layout1);
		panel1.add(new JLabel("Column ", SwingConstants.RIGHT));
		panel1.add(firstColumnJSpinner);
		panel1.add(new JLabel(" to "));
		panel1.add(lastColumnJSpinner);
		panel1.add(getFromCamDataButton);
		add(panel1);
		
		JPanel panel2 = new JPanel(layout1);
		panel2.add(new JLabel("  bin size "));
		panel2.add(binColumnJSpinner);
		panel2.add(binUnit);
		binUnit.setSelectedIndex(2);
		panel2.add(applyButton);
		add(panel2); 
		
		getFromCamDataButton.setEnabled(false);
		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{
		applyButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null)
					setKymoIntervalsFromDialog(exp);
			}});
	}
	
	public int getBuildStep() 
	{
		int buildStep = ((int) binColumnJSpinner.getValue()) * getBinSize_Ms();
		return buildStep;
	}
	
	private int getBinSize_Ms() 
	{
		int binsize = 1;
		int iselected = binUnit.getSelectedIndex();
		switch (iselected) {
		case 1: binsize = 1000; break;
		case 2: binsize = 1000 * 60; break;
		case 3: binsize = 1000 * 60 * 60; break;
		case 4: binsize = 1000 * 60 * 60 * 24; break;
		case 0:
		default:
			break;
		}
		return binsize;
	}
	
	void setKymoIntervalsFromDialog(Experiment exp) 
	{
		double binsize_Ms = getBinSize_Ms();
		exp.kymoFirst_ms = (long) ( (double) firstColumnJSpinner.getValue() * binsize_Ms);
		exp.kymoLast_ms  = (long) (((double) lastColumnJSpinner.getValue()) * binsize_Ms);
		exp.kymoBin_ms = (long) (((double) binColumnJSpinner.getValue()) * binsize_Ms);
	}
	
	void displayDlgKymoIntervals (Experiment exp) 
	{
		double binsize_Ms = getBinSize_Ms();
		firstColumnJSpinner.setValue(0.);
		lastColumnJSpinner.setValue((double) exp.seqKymos.imageWidthMax);
		if (exp.kymoBin_ms <= 0)
			exp.kymoBin_ms = (long) binsize_Ms;
		binColumnJSpinner.setValue((double) exp.kymoBin_ms/binsize_Ms);
	}

}
