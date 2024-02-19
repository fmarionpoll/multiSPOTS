package plugins.fmp.multispots;

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
import plugins.fmp.multispots.dlg.JComponents.ExperimentCombo;
import plugins.fmp.multispots.dlg.areas.MCAreas_;
import plugins.fmp.multispots.dlg.cages.MCCages_;
import plugins.fmp.multispots.dlg.excel.MCExcel_;
import plugins.fmp.multispots.dlg.experiment.MCExperiment_;
import plugins.fmp.multispots.dlg.kymos.MCKymos_;
import plugins.fmp.multispots.dlg.levels.MCLevels_;
import plugins.fmp.multispots.dlg.spots.MCSpots_;
import plugins.fmp.multispots.workinprogress_gpu.MCSpotsExp_;



public class MultiSPOTS extends PluginActionable  
{
	public IcyFrame 		mainFrame 		= new IcyFrame("multiSPOTS Feb 18, 2024", true, true, true, true);
	public ExperimentCombo 	expListCombo 	= new ExperimentCombo();
	
	public MCExperiment_ 	paneExperiment 	= new MCExperiment_();
	public MCSpots_ 		paneSpots		= new MCSpots_();
	public MCAreas_ 		paneAreas		= new MCAreas_();
	public MCKymos_			paneKymos		= new MCKymos_();
	public MCLevels_ 		paneLevels 		= new MCLevels_();
	public MCSpotsExp_		paneSpotsExp_	= new MCSpotsExp_();
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
		paneAreas.init(mainPanel, "Areas", this);
		paneKymos.init(mainPanel, "Kymographs", this);
		paneLevels.init(mainPanel, "Levels", this);
//		paneSpots.init(mainPanel, "MEASURE SPOTS", this);
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
