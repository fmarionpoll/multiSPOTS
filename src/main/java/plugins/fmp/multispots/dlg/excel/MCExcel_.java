package plugins.fmp.multispots.dlg.excel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import icy.gui.component.PopupPanel;
import icy.system.thread.ThreadUtil;
import plugins.fmp.multispots.multiSPOTS;
import plugins.fmp.multispots.dlg.JComponents.Dialog;
import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.tools.toExcel.XLSExportCapillariesResults;
import plugins.fmp.multispots.tools.toExcel.XLSExportGulpsResults;
import plugins.fmp.multispots.tools.toExcel.XLSExportMoveResults;
import plugins.fmp.multispots.tools.toExcel.XLSExportOptions;


public class MCExcel_  extends JPanel implements PropertyChangeListener 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4296207607692017074L;
	public	PopupPanel 		capPopupPanel	= null;
	private JTabbedPane 	tabsPane 		= new JTabbedPane();
	public Options			tabCommonOptions= new Options();
	private Levels			tabLevels		= new Levels();
	private Gulps			tabGulps		= new Gulps();
	private Move 			tabMove  		= new Move();
	private multiSPOTS 		parent0 = null;

	
	public void init (JPanel mainPanel, String string, multiSPOTS parent0) 
	{
		this.parent0 = parent0;
		
		capPopupPanel = new PopupPanel(string);
		JPanel capPanel = capPopupPanel.getMainPanel();
		capPanel.setLayout(new BorderLayout());
		capPopupPanel.collapse();
		mainPanel.add(capPopupPanel);
		GridLayout capLayout = new GridLayout(3, 2);
		
		tabCommonOptions.init(capLayout);
		tabsPane.addTab("Common options", null, tabCommonOptions, "Define common options");
		tabCommonOptions.addPropertyChangeListener(this);
		
		tabLevels.init(capLayout);
		tabsPane.addTab("Capillaries", null, tabLevels, "Export capillary levels to file");
		tabLevels.addPropertyChangeListener(this);
		
		tabGulps.init(capLayout);
		tabsPane.addTab("Gulps", null, tabGulps, "Export gulps to file");
		tabGulps.addPropertyChangeListener(this);
		
		
		tabMove.init(capLayout);
		tabsPane.addTab("Move", null, tabMove, "Export fly positions to file");
		tabMove.addPropertyChangeListener(this);
		
		capPanel.add(tabsPane);
		tabsPane.setSelectedIndex(0);
		
		capPopupPanel.addComponentListener(new ComponentAdapter() 
		{
			@Override
			public void componentResized(ComponentEvent e) 
			{
				parent0.mainFrame.revalidate();
				parent0.mainFrame.pack();
				parent0.mainFrame.repaint();
			}
		});
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) 
	{	
		Experiment exp = (Experiment)  parent0.expListCombo.getSelectedItem();
		if (exp == null) 
			return;
		
		if (evt.getPropertyName().equals("EXPORT_MOVEDATA")) 
		{
			String file = defineXlsFileName(exp, "_move.xlsx");
			if (file == null)
				return;
			updateParametersCurrentExperiment(exp);
			ThreadUtil.bgRun( new Runnable() 
			{ 
				@Override public void run() 
				{
					XLSExportMoveResults xlsExport = new XLSExportMoveResults();
					xlsExport.exportToFile(file, getMoveOptions());
				}});
		} 
		else if (evt.getPropertyName().equals("EXPORT_KYMOSDATA")) 
		{
			String file = defineXlsFileName(exp, "_feeding.xlsx");
			if (file == null)
				return;
			updateParametersCurrentExperiment(exp);
			ThreadUtil.bgRun( new Runnable() 
			{ 
				@Override public void run() 
				{
				XLSExportCapillariesResults xlsExport2 = new XLSExportCapillariesResults();
				xlsExport2.exportToFile(file, getLevelsOptions());
			}});
		}
		else if (evt.getPropertyName().equals("EXPORT_GULPSDATA")) 
		{
			String file = defineXlsFileName(exp, "_gulps.xlsx");
			if (file == null)
				return;
			updateParametersCurrentExperiment(exp);
			ThreadUtil.bgRun( new Runnable() 
			{ 
				@Override public void run() 
				{
					XLSExportGulpsResults xlsExport2 = new XLSExportGulpsResults();
					xlsExport2.exportToFile(file, getGulpsOptions());
				}});	
		}
	}
	
	private String defineXlsFileName(Experiment exp, String pattern) 
	{
		String filename0 = exp.seqCamData.getFileNameFromImageList(0);
		Path directory = Paths.get(filename0).getParent();
		Path subpath = directory.getName(directory.getNameCount()-1);
		String tentativeName = subpath.toString()+ pattern;
		return Dialog.saveFileAs(tentativeName, directory.getParent().toString(), "xlsx");
	}
	
	private void updateParametersCurrentExperiment(Experiment exp) 
	{
		parent0.paneCapillaries.getDialogCapillariesInfos(exp);
		parent0.paneExperiment.tabInfos.getExperimentInfosFromDialog(exp);
	}
	
	private XLSExportOptions getMoveOptions() 
	{
		XLSExportOptions options = new XLSExportOptions();
		options.xyImage 		= tabMove.xyCenterCheckBox.isSelected(); 
		options.xyCage			= tabMove.xyCageCheckBox.isSelected();
		options.xyCapillaries	= tabMove.xyTipCapsCheckBox.isSelected();
		options.distance 		= tabMove.distanceCheckBox.isSelected();
		options.alive 			= tabMove.aliveCheckBox.isSelected(); 
		options.onlyalive 		= tabMove.deadEmptyCheckBox.isSelected();
		options.sleep			= tabMove.sleepCheckBox.isSelected();
		options.ellipseAxes		= tabMove.rectSizeCheckBox.isSelected();
		getCommonOptions(options);
		return options;
	}
	
	private XLSExportOptions getLevelsOptions() 
	{
		XLSExportOptions options = new XLSExportOptions();
		options.sumGulps 		= false; 
		options.nbGulps 		= false;
		
		options.topLevel 		= tabLevels.topLevelCheckBox.isSelected(); 
		options.topLevelDelta   = tabLevels.topLevelDeltaCheckBox.isSelected();
		options.bottomLevel 	= tabLevels.bottomLevelCheckBox.isSelected();  
		options.sumGulps 		= false; 
		options.lrPI 			= tabLevels.lrPICheckBox.isSelected(); 
		options.lrPIThreshold 	= (double) tabLevels.lrPIThresholdJSpinner.getValue();
		options.sumPerCage 		= tabLevels.sumPerCageCheckBox.isSelected();
		options.t0 				= tabLevels.t0CheckBox.isSelected();
		options.subtractEvaporation = tabLevels.subtractEvaporationCheckBox.isSelected();
		getCommonOptions(options);
		return options;
	}
	
	private XLSExportOptions getGulpsOptions() 
	{
		XLSExportOptions options= new XLSExportOptions();
		options.topLevel 		= false; 
		options.topLevelDelta   = false;
		options.bottomLevel 	= false; 
		options.derivative 		= tabGulps.derivativeCheckBox.isSelected(); 
		options.sumPerCage 		= false;
		options.t0 				= false;
		options.sumGulps 		= tabGulps.sumGulpsCheckBox.isSelected(); 
		options.lrPI 			= tabGulps.sumCheckBox.isSelected(); 
		options.nbGulps 		= tabGulps.nbGulpsCheckBox.isSelected();
		options.amplitudeGulps 	= tabGulps.amplitudeGulpsCheckBox.isSelected();

		options.autocorrelation		= tabGulps.autocorrelationCheckBox.isSelected();
		options.crosscorrelation	= tabGulps.crosscorrelationCheckBox.isSelected();
		options.nbinscorrelation	= (int) tabGulps.nbinsJSpinner.getValue();
		
		options.subtractEvaporation = false;
		getCommonOptions(options);
		return options;
	}
	
	private void getCommonOptions(XLSExportOptions options) 
	{
		options.transpose 		= tabCommonOptions.transposeCheckBox.isSelected();
		options.buildExcelStepMs= tabCommonOptions.getExcelBuildStep() ;
		options.buildExcelUnitMs= tabCommonOptions.binUnit.getMsUnitValue();
		options.fixedIntervals 	= tabCommonOptions.isFixedFrameButton.isSelected();
		options.startAll_Ms 	= tabCommonOptions.getStartAllMs();
		options.endAll_Ms 		= tabCommonOptions.getEndAllMs();
		
		options.collateSeries 	= tabCommonOptions.collateSeriesCheckBox.isSelected();
		options.padIntervals 	= tabCommonOptions.padIntervalsCheckBox.isSelected();
		options.absoluteTime	= false; //tabCommonOptions.absoluteTimeCheckBox.isSelected();
		options.onlyalive 		= tabCommonOptions.onlyAliveCheckBox.isSelected();
		options.exportAllFiles 	= tabCommonOptions.exportAllFilesCheckBox.isSelected();
		
		options.expList = parent0.expListCombo; 
		options.expList.expListBinSubDirectory = parent0.paneKymos.tabDisplay.getBinSubdirectory() ;
		if (tabCommonOptions.exportAllFilesCheckBox.isSelected()) {
			options.firstExp 	= 0;
			options.lastExp 	= options.expList.getItemCount() - 1;
		} else {
			options.firstExp 	= parent0.expListCombo.getSelectedIndex();
			options.lastExp 	= parent0.expListCombo.getSelectedIndex();
		}
	}
}
