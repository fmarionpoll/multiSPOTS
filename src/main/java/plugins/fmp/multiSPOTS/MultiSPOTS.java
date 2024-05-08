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
import plugins.fmp.multiSPOTS.dlg.cages.DlgCages_;
import plugins.fmp.multiSPOTS.dlg.excel.DlgExcel_;
import plugins.fmp.multiSPOTS.dlg.experiment.DlgExperiment_;
import plugins.fmp.multiSPOTS.dlg.kymos.DlgKymos_;
import plugins.fmp.multiSPOTS.dlg.spots.DlgSpots_;
import plugins.fmp.multiSPOTS.dlg.spotsMeasures.DlgMeasure_;
import plugins.fmp.multiSPOTS.tools.JComponents.JComboBoxExperiment;



public class MultiSPOTS extends PluginActionable  
{
	public IcyFrame 		mainFrame 		= new IcyFrame("multiSPOTS May 8, 2024", true, true, true, true);
	public JComboBoxExperiment 	expListCombo 	= new JComboBoxExperiment();
	
	public DlgExperiment_ 	dlgExperiment 	= new DlgExperiment_();
	public DlgSpots_ 		dlgSpots		= new DlgSpots_();
	public DlgKymos_		dlgKymos		= new DlgKymos_();
	public DlgMeasure_ 		dlgMeasure 		= new DlgMeasure_();
	public DlgCages_ 		dlgCages 		= new DlgCages_();
	public DlgExcel_		dlgExcel		= new DlgExcel_();
	
	public JTabbedPane 		tabsPane 		= new JTabbedPane();
	
	//-------------------------------------------------------------------
	
	@Override
	public void run() 
	{		
		JPanel mainPanel = GuiUtil.generatePanelWithoutBorder();
		dlgExperiment.init(mainPanel, "Experiments", this);
		dlgSpots.init(mainPanel, "Spots", this);
		dlgKymos.init(mainPanel, "KymoSpots", this);
		dlgMeasure.init(mainPanel, "Measure spots", this);
		dlgCages.init(mainPanel, "Cages", this);
		dlgExcel.init(mainPanel, "Export", this);
		
		mainFrame.setLayout(new BorderLayout());
		mainFrame.add(mainPanel, BorderLayout.WEST);
		mainFrame.pack();
		mainFrame.center();
		mainFrame.setVisible(true);
		mainFrame.addToDesktopPane();
		
		dlgExperiment.capPopupPanel.expand();
	}	 
	
	public static void main (String[] args)
	{
		Icy.main(args);
		GeneralPreferences.setSequencePersistence(false);
		PluginLauncher.start(PluginLoader.getPlugin(MultiSPOTS.class.getName()));
	}

}

