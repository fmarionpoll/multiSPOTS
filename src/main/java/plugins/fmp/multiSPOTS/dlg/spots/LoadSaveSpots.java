package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import icy.gui.util.FontUtil;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;



public class LoadSaveSpots extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4019075448319252245L;
	
	private JButton		loadButton	= new JButton("Load...");
	private JButton		saveButton	= new JButton("Save...");
	private MultiSPOTS 	parent0 				= null;
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{
		setLayout(capLayout);
		
		JLabel loadsaveText = new JLabel ("-> Spots, polylines (xml) ", SwingConstants.RIGHT);
		loadsaveText.setFont(FontUtil.setStyle(loadsaveText.getFont(), Font.ITALIC));
		FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
		flowLayout.setVgap(0);
		JPanel panel1 = new JPanel(flowLayout);
		panel1.add(loadsaveText);
		panel1.add(loadButton);
		panel1.add(saveButton);
		panel1.validate();
		add( panel1);
			
		this.parent0 = parent0;
		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{	
		loadButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) 
				{ 
					loadCapillaries_File(exp);
					loadSpotsArray_File(exp);
					firePropertyChange("CAP_ROIS_OPEN", false, true);
				}
			}}); 
		
		saveButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) 
				{
					saveCapillaries_file(exp);
					saveSpotsArray_file(exp); 
					firePropertyChange("CAP_ROIS_SAVE", false, true);
				}
			}});	
	}
	
	public boolean loadCapillaries_File(Experiment exp) 
	{	
		boolean flag = exp.loadMCCapillaries_Only();
		exp.capillaries.transferCapillaryRoiToSequence(exp.seqCamData.seq);
		return flag;
	}
	
	public boolean saveCapillaries_file(Experiment exp) 
	{
//		parent0.paneSpots.getDialogCapillariesInfos(exp);  // get data into desc
		parent0.paneExperiment.getExperimentInfosFromDialog(exp);
		exp.capillaries.transferDescriptionToCapillaries();
	
		exp.saveXML_MCExperiment ();
		exp.capillaries.updateCapillariesFromSequence(exp.seqCamData.seq);
		return exp.xmlSave_MCCapillaries_Only();
	}

	public boolean loadSpotsArray_File(Experiment exp) 
	{	
		boolean flag = exp.loadMCSpots_Only();
		exp.loadSpotsMeasures(); 
		exp.spotsArray.transferSpotRoiToSequence(exp.seqCamData.seq);
		return flag;
	}
	
	public boolean saveSpotsArray_file(Experiment exp) 
	{
//		parent0.paneSpots.getDialogCapillariesInfos(exp);  // get data into desc
		parent0.paneExperiment.getExperimentInfosFromDialog(exp);
		exp.spotsArray.transferDescriptionToSpots();
	
		boolean flag = exp.saveXML_MCExperiment ();
		exp.spotsArray.updateSpotsFromSequence(exp.seqCamData.seq);
		flag &= exp.xmlSave_MCSpots_Only();
		flag &= exp.saveSpotsMeasures();
		return flag;
	}

}
