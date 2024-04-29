package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.gui.component.PopupPanel;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.dlg.measureSpots.ThresholdColors;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.ExperimentUtils;



public class DlgSpots_ extends JPanel implements PropertyChangeListener, ChangeListener 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 853047648249832145L;
	public	PopupPanel 		capPopupPanel	= null;
			JTabbedPane 	tabsPane 		= new JTabbedPane();		
			CreateSpots		tabCreateSpots 	= new CreateSpots();

			ThresholdColors colorsThreshold = new ThresholdColors();

			Infos			tabInfos		= new Infos();
			Edit			tabEdit			= new Edit();

	public 	LoadSaveSpots 	tabFile  		= new LoadSaveSpots();
	
//			Adjust 		tabAdjust 		= new Adjust();

	private int			id_infos		= 1;
	private int 		id_create 		= 0;
	private MultiSPOTS 	parent0 		= null;

	
	public void init (JPanel mainPanel, String string, MultiSPOTS parent0) 
	{
		this.parent0 = parent0;
		capPopupPanel = new PopupPanel(string);
		JPanel capPanel = capPopupPanel.getMainPanel();
		capPanel.setLayout(new BorderLayout());
		capPopupPanel.collapse();
		mainPanel.add(capPopupPanel);
		
		GridLayout gridLayout = new GridLayout(3, 1);
		int order = 0;
		
		tabCreateSpots.init(gridLayout, parent0);
		tabCreateSpots.addPropertyChangeListener(this);
		tabsPane.addTab("Create", null, tabCreateSpots, "Create spots defining liquid drops");
		id_create = order;
		order++;
		
		tabInfos.init(gridLayout, parent0);
		tabInfos.addPropertyChangeListener(this);
		tabsPane.addTab("Infos", null, tabInfos, "Edit infos");
		id_infos = order;
		order++;
		
		tabEdit.init(gridLayout, parent0);
		tabEdit.addPropertyChangeListener(this);
		tabsPane.addTab("Edit", null, tabEdit, "Edit spots shape");
		order++;
		
		tabFile.init(gridLayout, parent0);
		tabFile.addPropertyChangeListener(this);
		tabsPane.addTab("Load/Save", null, tabFile, "Load/Save xml file with spots descriptors");
		order++;
		
		tabsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		capPanel.add(tabsPane);
		tabsPane.addChangeListener(this );
		
		capPopupPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				parent0.mainFrame.revalidate();
				parent0.mainFrame.pack();
				parent0.mainFrame.repaint();
			}
		});
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) 
	{
		if (event.getPropertyName().equals("CAP_ROIS_OPEN")) {
			Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
			if (exp != null) {
				displayCapillariesInformation(exp);
			  	tabsPane.setSelectedIndex(id_infos);
			  	parent0.dlgExperiment.tabIntervals.displayCamDataIntervals(exp);
			}
		}			  
		else if (event.getPropertyName().equals("CAP_ROIS_SAVE")) {
			tabsPane.setSelectedIndex(id_infos);
		}

	}
	
	public void displayCapillariesInformation(Experiment exp) 
	{
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				updateDialogs( exp);
				parent0.dlgExperiment.tabOptions.viewSpotsCheckBox.setSelected(true);
			}});
	}
	
	public void updateDialogs(Experiment exp) 
	{
		if (exp != null) {
			ExperimentUtils.transferCamDataROIStoCapillaries(exp);
			exp.capillaries.desc_old.copy(exp.capillaries.capillariesDescription);
	
			ExperimentUtils.transferCamDataROIStoSpots(exp);
			exp.spotsArray.desc_old.copy(exp.spotsArray.spotsDescription);
		}
	}
	

	@Override
	public void stateChanged(ChangeEvent e) 
	{
		JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
        int selectedIndex = tabbedPane.getSelectedIndex();
        Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp != null) {
			boolean displayCapillaries = (selectedIndex == id_create);
			if (displayCapillaries && exp.capillaries.capillariesList.size() < 1)
				exp.loadCamDataCapillaries();
			exp.seqCamData.displayROIs(displayCapillaries, "line");
			exp.seqCamData.displayROIs(true, "spots");
		}
	}
	
	public void transferPreviousExperimentCapillariesInfos(Experiment exp0, Experiment exp)
	{
		exp.capillaries.capillariesDescription.grouping = exp0.capillaries.capillariesDescription.grouping;
		exp.capillaries.capillariesDescription.volume = exp0.capillaries.capillariesDescription.volume;
	}

}
