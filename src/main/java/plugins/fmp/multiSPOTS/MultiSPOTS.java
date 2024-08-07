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
import plugins.fmp.multiSPOTS.dlg.browse.DlgBrowse_;
import plugins.fmp.multiSPOTS.dlg.cages.DlgCages_;
import plugins.fmp.multiSPOTS.dlg.excel.DlgExcel_;
import plugins.fmp.multiSPOTS.dlg.experiment.DlgExperiment_;
import plugins.fmp.multiSPOTS.dlg.flies.DlgDetectFlies_;
import plugins.fmp.multiSPOTS.dlg.kymos.DlgKymos_;
import plugins.fmp.multiSPOTS.dlg.spots.DlgSpots_;
import plugins.fmp.multiSPOTS.dlg.spotsMeasures.DlgSpotMeasure_;
import plugins.fmp.multiSPOTS.tools.JComponents.JComboBoxExperiment;

public class MultiSPOTS extends PluginActionable {
	public IcyFrame mainFrame = new IcyFrame("multiSPOTS July 30, 2024", true, true, true, true);
	public JComboBoxExperiment expListCombo = new JComboBoxExperiment();

	public DlgBrowse_ dlgBrowse = new DlgBrowse_();
	public DlgExperiment_ dlgExperiment = new DlgExperiment_();
	public DlgSpots_ dlgSpots = new DlgSpots_();
	public DlgKymos_ dlgKymos = new DlgKymos_();
	public DlgSpotMeasure_ dlgMeasure = new DlgSpotMeasure_();
	public DlgCages_ dlgCages = new DlgCages_();
	public DlgDetectFlies_ dlgDetectFlies = new DlgDetectFlies_();
	public DlgExcel_ dlgExcel = new DlgExcel_();

	public JTabbedPane tabsPane = new JTabbedPane();

	// -------------------------------------------------------------------

	@Override
	public void run() {
		JPanel mainPanel = GuiUtil.generatePanelWithoutBorder();
		dlgBrowse.init(mainPanel, "Browse", this);
		dlgExperiment.init(mainPanel, "Experiment", this);
		dlgSpots.init(mainPanel, "Spots", this);
		dlgKymos.init(mainPanel, "KymoSpots", this);
		dlgMeasure.init(mainPanel, "Measure spots", this);
		dlgCages.init(mainPanel, "Cages", this);
		dlgDetectFlies.init(mainPanel, "Detect flies", this);
		dlgExcel.init(mainPanel, "Export", this);

		mainFrame.setLayout(new BorderLayout());
		mainFrame.add(mainPanel, BorderLayout.WEST);
		mainFrame.pack();
		mainFrame.center();
		mainFrame.setVisible(true);
		mainFrame.addToDesktopPane();
	}

	public static void main(String[] args) {
		Icy.main(args);
		GeneralPreferences.setSequencePersistence(false);
		PluginLauncher.start(PluginLoader.getPlugin(MultiSPOTS.class.getName()));
	}

}
