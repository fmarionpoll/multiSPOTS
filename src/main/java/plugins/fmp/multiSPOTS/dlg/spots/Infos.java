package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Capillary;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.Spot;



public class Infos extends JPanel 
{
	/**
	 * 
	 */
	private static final long 	serialVersionUID 			= 4950182090521600937L;
	
	private JButton			editCapillariesButton	= new JButton("Edit polyline Rois infos...");
	private JButton			editSpotsButton			= new JButton("Edit spots infos...");
	private MultiSPOTS 		parent0 				= null;
	
	private CapillaryTable infosCapillaryTable 		= null;
	private SpotTable	   infosSpotTable			= null;
	private List <Capillary> capillariesArrayCopy 	= new ArrayList<Capillary>();
	private List<Spot>		spotsArrayCopy			= new ArrayList<Spot>();
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{
		setLayout(capLayout);
		this.parent0 = parent0;
				
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 1));
		panel1.add( editCapillariesButton);
		add(panel1);
		
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 1));
		panel2.add( editSpotsButton);
		add(panel2);


		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{
		editCapillariesButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					exp.capillaries.transferDescriptionToCapillaries();
					if (infosCapillaryTable == null)
						infosCapillaryTable = new CapillaryTable();
					infosCapillaryTable.initialize(parent0, capillariesArrayCopy);
				}
			}});
		
		editSpotsButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					exp.spotsArray.transferDescriptionToSpots();
					if (infosSpotTable == null)
						infosSpotTable = new SpotTable();
					infosSpotTable.initialize(parent0, spotsArrayCopy);
				}
			}});
	}

	// set/ get
	public int getLengthFirstCapillaryROI() {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		int npixels = 0;
		if (exp != null)
		{
			exp.capillaries.updateCapillariesFromSequence(exp.seqCamData.seq);
			if (exp.capillaries.capillariesList.size() > 0) 
			{
				Capillary cap = exp.capillaries.capillariesList.get(0);
				npixels = cap.getCapillaryROILength();
			}
		}
		return npixels;
	}
						
}
