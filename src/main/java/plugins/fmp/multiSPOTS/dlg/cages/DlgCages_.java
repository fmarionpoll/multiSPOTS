package plugins.fmp.multiSPOTS.dlg.cages;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.gui.component.PopupPanel;
import plugins.fmp.multiSPOTS.MultiSPOTS;



public class DlgCages_ extends JPanel implements PropertyChangeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3457738144388946607L;
	
			BuildCages 		tabBuildCages	= new BuildCages();
			Infos		tabInfos		= new Infos();
			Detect1 		tabDetect1 		= new Detect1();

			Detect2DetectFlies tabDetect2 = new Detect2DetectFlies();
			Edit			tabEdit			= new Edit();
	public 	LoadSaveCages 	tabFile 		= new LoadSaveCages();
	public 	PlotPositions 	tabGraphics 	= new PlotPositions();
	public	PopupPanel 		capPopupPanel	= null;
			JTabbedPane 	tabsPane		= new JTabbedPane();
			int				previouslySelected	= -1;
	public 	boolean			bTrapROIsEdit	= false;
	
			int 			iTAB_CAGE2		= 1;
			int 			iTAB_INFOS 		= iTAB_CAGE2+1;
			int 			iTAB_DETECT1	= iTAB_INFOS+1;
			int 			iTAB_DETECT2	= iTAB_DETECT1+1;
			int				iTAB_EDIT		= iTAB_DETECT2+1;
			
			
			MultiSPOTS 		parent0			= null;

	
	public void init (JPanel mainPanel, String string, MultiSPOTS parent0) 
	{
		this.parent0 = parent0;
		
		capPopupPanel = new PopupPanel(string);
		JPanel capPanel = capPopupPanel.getMainPanel();
		capPanel.setLayout(new BorderLayout());
		capPopupPanel.collapse();
		
		mainPanel.add(capPopupPanel);
		GridLayout capLayout = new GridLayout(4, 1);
		createTabs(capLayout);
		
		tabsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		capPanel.add(tabsPane);
		tabsPane.setSelectedIndex(0);
		
		tabsPane.addChangeListener(new ChangeListener() {
			@Override 
	        public void stateChanged(ChangeEvent e) {
	            int selectedIndex = tabsPane.getSelectedIndex();
	            tabBuildCages.tabBuildCages2.overlayCheckBox.setSelected(selectedIndex == 0);
	            	
	            tabDetect1.overlayCheckBox.setSelected(selectedIndex == iTAB_DETECT1);
	            if (selectedIndex == iTAB_DETECT1 || selectedIndex == iTAB_DETECT2){
//	            	parent0.dlgExperiment.capPopupPanel.expand();
	    			parent0.dlgExperiment.tabsPane.setSelectedIndex(0);
	            }
	            
	            if (selectedIndex == iTAB_EDIT) {
	            	bTrapROIsEdit = true;
	            	parent0.dlgExperiment.tabOptions.displayROIsCategory (false, "spot");
	            	parent0.dlgExperiment.tabOptions.displayROIsCategory(false, "cage");
	            } 
	            else {
	            	if (bTrapROIsEdit) {
	            		parent0.dlgExperiment.tabOptions.displayROIsCategory (parent0.dlgExperiment.tabOptions.viewSpotsCheckBox.isSelected(), "spot");
		            	parent0.dlgExperiment.tabOptions.displayROIsCategory(parent0.dlgExperiment.tabOptions.viewCagesCheckbox.isSelected(), "cage");
	            	}
	            	bTrapROIsEdit = false;
	            }
	            previouslySelected = selectedIndex;
	        }});
		
		capPopupPanel.addComponentListener(new ComponentAdapter() 
		{
			@Override
			public void componentResized(ComponentEvent e) 
			{
				parent0.mainFrame.revalidate();
				parent0.mainFrame.pack();
				parent0.mainFrame.repaint();
			}});
	}
	
	void createTabs(GridLayout capLayout) 
	{
		int iTab = 0;
		tabBuildCages.init(capLayout, parent0);
		tabBuildCages.addPropertyChangeListener(this);
		tabsPane.addTab("Cages", null, tabBuildCages, "Define cages");

		iTab++;
		iTAB_INFOS = iTab;
		tabInfos.init(capLayout,parent0);
		tabInfos.addPropertyChangeListener(this);
		tabsPane.addTab("Infos", null, tabInfos, "Display infos about cages and flies positions");
		
		iTab++;
		iTAB_DETECT1 = iTab;
		tabDetect1.init(capLayout, parent0);
		tabDetect1.addPropertyChangeListener(this);
		tabsPane.addTab("Detect1", null, tabDetect1, "Detect flies position using thresholding on image overlay");
		
		iTab++;
		iTAB_DETECT2 = iTab;
		tabDetect2.init(capLayout, parent0);
		tabDetect2.addPropertyChangeListener(this);
		tabsPane.addTab("Detect2", null, tabDetect2, "Detect flies position using thresholding on image overlay");
		
		iTab++;
		iTAB_EDIT	= iTab;
		tabEdit.init(capLayout, parent0);
		tabEdit.addPropertyChangeListener(this);
		tabsPane.addTab("Edit", null, tabEdit, "Edit flies detection");
	
		iTab++;
		tabGraphics.init(capLayout, parent0);		
		tabGraphics.addPropertyChangeListener(this);
		tabsPane.addTab("Graphs", null, tabGraphics, "Display results as graphics");

		iTab++;
		tabFile.init(capLayout, parent0);
		tabFile.addPropertyChangeListener(this);
		tabsPane.addTab("Load/Save", null, tabFile, "Load/save cages and flies position");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (evt.getPropertyName().equals("LOAD_DATA"))
			tabBuildCages.tabBuildCages1.updateNColumnsFieldFromSequence();
	}
	
	
}

