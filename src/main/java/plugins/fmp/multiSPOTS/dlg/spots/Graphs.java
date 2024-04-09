package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import icy.gui.viewer.Viewer;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.Spot;
import plugins.fmp.multiSPOTS.tools.chart.ChartAreas;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSExportType;
import plugins.fmp.multiSPOTS.tools.toExcel.XLSExportOptions;

public class Graphs extends JPanel implements SequenceListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7079184380174992501L;
	private ChartAreas 	plotAreaPixels			= null;
	private MultiSPOTS 	parent0 				= null;
	private JButton 	displayResultsButton 	= new JButton("Display results");
	EnumXLSExportType[] measures = new EnumXLSExportType[]{
			EnumXLSExportType.AREA_SUM, 
			EnumXLSExportType.AREA_SUMCLEAN, 
			EnumXLSExportType.AREA_CNTPIX
//			,EnumXLSExportType.AREA_MEANGREY
			};
	JComboBox<EnumXLSExportType> exportTypeComboBox = new JComboBox<EnumXLSExportType> (measures);
	private JCheckBox 	t0Checkbox 	= new JCheckBox("relative to t0", true);
	
	
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{	
		setLayout(capLayout);
		this.parent0 = parent0;
		setLayout(capLayout);
		FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
		layout.setVgap(0);
		
		JPanel panel01 = new JPanel(layout);
		panel01.add(new JLabel("results "));
		panel01.add(exportTypeComboBox);
		add(panel01);
		JPanel panel1 = new JPanel(layout);
		add(panel1);
		
		JPanel panel02 = new JPanel(layout);
		panel02.add(t0Checkbox);
		add(panel02);
		
		JPanel panel03 = new JPanel(layout);
		panel03.add(displayResultsButton);
		add(panel03);
		
		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{
		
		exportTypeComboBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null) 				
					displayGraphsPanels(exp);
			}});
		
		displayResultsButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) 
					displayGraphsPanels(exp);
			}});
		
		t0Checkbox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null) 				
					displayGraphsPanels(exp);
			}});
	}
	
	private Rectangle getInitialUpperLeftPosition(Experiment exp)
	{
		Rectangle rectv = new Rectangle(50, 500, 10, 10);
		Viewer v = exp.seqCamData.seq.getFirstViewer();
		if (v != null) {
			rectv = v.getBounds();
			rectv.translate(0, rectv.height);
		}
		else
		{
			rectv = parent0.mainFrame.getBounds();
			rectv.translate(rectv.width, rectv.height + 100);
		}
		return rectv;
	}
	
	public void displayGraphsPanels(Experiment exp) 
	{
		Rectangle rectv = getInitialUpperLeftPosition(exp);
			
		int dx = 5;
		int dy = 10; 
		exp.seqCamData.seq.addListener(this);
		EnumXLSExportType exportType = (EnumXLSExportType) exportTypeComboBox.getSelectedItem();
		if (isThereAnyDataToDisplay(exp, exportType))  
		{
			plotAreaPixels = plotToChart(exp, exportType, plotAreaPixels, rectv);
			rectv.translate(dx, dy);
		}
	}
	
	private ChartAreas plotToChart(Experiment exp, EnumXLSExportType exportType, ChartAreas iChart, Rectangle rectv ) 
	{	
		if (iChart != null) 
			iChart.mainChartFrame.dispose();
		iChart = new ChartAreas();
		iChart.createChartPanel(parent0, "Spots measures");
		iChart.setUpperLeftLocation(rectv);
		
		XLSExportOptions xlsExportOptions = new XLSExportOptions();
		xlsExportOptions.buildExcelStepMs = 60000;
		xlsExportOptions.relativeToT0 = t0Checkbox.isSelected();
		xlsExportOptions.subtractEvaporation = false;
		xlsExportOptions.exportType = exportType;
		
		iChart.displayData(exp, xlsExportOptions);
		iChart.mainChartFrame.toFront();
		iChart.mainChartFrame.requestFocus();
		return iChart;
	}
	
	public void closeAllCharts() 
	{
		plotAreaPixels = closeChart (plotAreaPixels); 
	}
	
	private ChartAreas closeChart(ChartAreas chart) 
	{
		if (chart != null) 
			chart.mainChartFrame.dispose();
		chart = null;
		return chart;
	}

	private boolean isThereAnyDataToDisplay(Experiment exp, EnumXLSExportType option) 
	{
		boolean flag = false;
		for (Spot spot: exp.spotsArray.spotsList) 
		{
			flag = spot.isThereAnyMeasuresDone(option);
			if (flag)
				break;
		}
		return flag;
	}

	@Override
	public void sequenceChanged(SequenceEvent sequenceEvent) 
	{
	}

	@Override
	public void sequenceClosed(Sequence sequence) 
	{
		sequence.removeListener(this);
		closeAllCharts();
	}
}
