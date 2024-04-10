package plugins.fmp.multiSPOTS;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.main.Icy;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.abstract_.PluginActionable;
import icy.preferences.GeneralPreferences;
import plugins.fmp.multiSPOTS.dlg.JComponents.ExperimentCombo;
import plugins.fmp.multiSPOTS.dlg.cages.MCCages_;
import plugins.fmp.multiSPOTS.dlg.excel.MCExcel_;
import plugins.fmp.multiSPOTS.dlg.experiment.MCExperiment_;
import plugins.fmp.multiSPOTS.dlg.spots.MCSpots_;



public class MultiSPOTS extends PluginActionable  
{
	public IcyFrame 		mainFrame 		= new IcyFrame("multiSPOTS April 10, 2024", true, true, true, true);
	public ExperimentCombo 	expListCombo 	= new ExperimentCombo();
	
	public MCExperiment_ 	paneExperiment 	= new MCExperiment_();
	public MCSpots_ 		paneSpots		= new MCSpots_();
	public MCCages_ 		paneCages 		= new MCCages_();
	public MCExcel_			paneExcel		= new MCExcel_();
	
	public JTabbedPane 		tabsPane 		= new JTabbedPane();
	
	//-------------------------------------------------------------------
	
	@Override
	public void run() 
	{		
		JPanel mainPanel = GuiUtil.generatePanelWithoutBorder();
		paneExperiment.init(mainPanel, "Experiments", this);
		paneSpots.init(mainPanel, "Spots", this);
		paneCages.init(mainPanel, "Cages", this);
		paneExcel.init(mainPanel, "Export", this);
		
		mainFrame.setLayout(new BorderLayout());
		mainFrame.add(mainPanel, BorderLayout.WEST);
		mainFrame.pack();
		mainFrame.center();
		mainFrame.setVisible(true);
		mainFrame.addToDesktopPane();
		
		paneExperiment.capPopupPanel.expand();
	}	 
	
	public static void main (String[] args)
	{
		Icy.main(args);
		GeneralPreferences.setSequencePersistence(false);
		PluginLauncher.start(PluginLoader.getPlugin(MultiSPOTS.class.getName()));
	}

}

