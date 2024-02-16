package plugins.fmp.multispots.dlg.cages;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import plugins.fmp.multispots.multiSPOTS;
import plugins.fmp.multispots.experiment.Cage;
import plugins.fmp.multispots.experiment.Experiment;



public class Infos extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3325915033686366985L;
	private JButton		editCagesButton		= new JButton("Edit cages infos...");
	private multiSPOTS 	parent0 			= null;
	private Table 		dialog 				= null;
	private List <Cage> cagesArrayCopy 		= new ArrayList<Cage>();
	
	JRadioButton useCapillaries = new JRadioButton("capillary");
	JRadioButton useCages = new JRadioButton("cages");
	JRadioButton useManual = new JRadioButton("manual entry");
	ButtonGroup useGroup = new ButtonGroup();
	
	private JSpinner 	lengthSpinner	= new JSpinner(new SpinnerNumberModel(78., 0., 100., 1.));
	private JSpinner 	pixelsSpinner	= new JSpinner(new SpinnerNumberModel(5, 0, 1000, 1));
	private JButton		measureButton = new JButton ("get 1rst capillary");
	
	
  
	
	void init(GridLayout capLayout, multiSPOTS parent0) 
	{
		setLayout(capLayout);
		this.parent0 = parent0;
		
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
		flowLayout.setVgap(0);
		
		JPanel panel0a = new JPanel(flowLayout);
		panel0a.add(new JLabel("Use as reference: "));
		panel0a.add(useCapillaries);
		panel0a.add(useCages);
		panel0a.add(useManual);
		add(panel0a);
		useGroup.add(useCapillaries);
		useGroup.add(useCages);
		useGroup.add(useManual);
		useCages.setSelected(true);
		
		JPanel panel00 = new JPanel(flowLayout);
		panel00.add(new JLabel("length in mm:", SwingConstants.RIGHT));
		panel00.add(lengthSpinner);
		add(panel00);
		
		JPanel panel0 = new JPanel(flowLayout);
		panel0.add(new JLabel("length in pixels:", SwingConstants.RIGHT));
		panel0.add(pixelsSpinner);
		panel0.add(measureButton);
		add(panel0);
		
		JPanel panel1 = new JPanel(flowLayout);
		panel1.add( editCagesButton);
		add(panel1);

		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{		
		editCagesButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					exp.capillaries.transferDescriptionToCapillaries();
					exp.cages.transferNFliesFromCapillariesToCages(exp.capillaries.capillariesList);
					dialog = new Table();
	            	dialog.initialize(parent0, cagesArrayCopy);
				}
			}});
		
		useCapillaries.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				lengthSpinner.setValue(23.);
				measureButton.setText("get length 1rst capillary");
				measureButton.setVisible(true);
			}});
		
		useCages.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				lengthSpinner.setValue(78.);
				measureButton.setText("get span between 1rst and last cage");
				measureButton.setVisible(true);
			}});
		
		useManual.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				measureButton.setVisible(false);
			}});
		
		measureButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				if (useCapillaries.isSelected()) {
					measureFirstCapillary();
				}
				else if (useCages.isSelected()) {
					measureCagesSpan();
				}
			}});
	}
	
	void measureFirstCapillary() {
		int npixels = parent0.paneSpots.tabInfos.getLengthFirstCapillaryROI();
		if (npixels > 0) 
			pixelsSpinner.setValue(npixels);
	}
	
	void measureCagesSpan() {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp != null)
		{
			exp.capillaries.updateCapillariesFromSequence(exp.seqCamData.seq);
			if (exp.capillaries.capillariesList.size() > 0) 
			{
				int npixels = exp.cages.getHorizontalSpanOfCages();
				if (npixels > 0) 
					pixelsSpinner.setValue(npixels);
			}
		}
	}
	

}
