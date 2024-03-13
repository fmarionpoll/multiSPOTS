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
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.Spot;



public class Edit extends JPanel 
{
	/**
	 * 
	 */
	private static final long 	serialVersionUID 			= 4950182090521600937L;
	
	private JButton			editSpotsButton			= new JButton("Edit spots infos...");
	private JButton			filterSpikesButton		= new JButton("Filter spikes...");
	private SpotTable	   	infosSpotTable			= null;
	private List<Spot>		spotsArrayCopy			= new ArrayList<Spot>();
	private MultiSPOTS 		parent0 				= null;
	
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{
		setLayout(capLayout);
		this.parent0 = parent0;
		
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 3, 1);
		
		JPanel panel01 = new JPanel(flowLayout);
		panel01.add( editSpotsButton);
		add(panel01);
		JPanel panel02 = new JPanel(flowLayout);
		panel02.add( filterSpikesButton);
		add(panel02);

		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{
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
		
		filterSpikesButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					exp.spotsArray.filterSpikes();
//					exp.saveSpotsMeasures();
					parent0.paneSpots.tabGraphs.displayGraphsPanels(exp);
				}
			}});
	}
				
}
