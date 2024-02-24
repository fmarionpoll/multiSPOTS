package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.EnumSpotMeasures;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.Spot;
import plugins.fmp.multiSPOTS.tools.Canvas2DWithFilters;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformEnums;
import plugins.fmp.multiSPOTS.tools.chart.ChartAreas;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSExportType;

public class Graphs extends JPanel implements SequenceListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7079184380174992501L;
	private ChartAreas 	plotAreaPixels			= null;
	private MultiSPOTS 	parent0 				= null;
	private JButton 	displayResultsButton 	= new JButton("Display results");
	EnumSpotMeasures[] measures = new EnumSpotMeasures[] {
			EnumSpotMeasures.AREA_NPIXELS, EnumSpotMeasures.AREA_SUM, EnumSpotMeasures.AREA_SUMSQ, EnumSpotMeasures.AREA_CNTPIX
			};
	JComboBox<EnumSpotMeasures> resultsComboBox = new JComboBox<EnumSpotMeasures> (measures);

	
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{	
		setLayout(capLayout);
		this.parent0 = parent0;
		setLayout(capLayout);
		FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
		layout.setVgap(0);
		
		JPanel panel = new JPanel(layout);
		panel.add(new JLabel("results "));
		panel.add(resultsComboBox);
		add(panel);
		JPanel panel1 = new JPanel(layout);
		add(panel1);
		
		add(GuiUtil.besidesPanel(displayResultsButton, new JLabel(" "))); 
		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{
		
		resultsComboBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null && exp.seqKymos != null) 
				{				
					EnumSpotMeasures item = (EnumSpotMeasures) resultsComboBox.getSelectedItem();
					selectResults(item);
				}
			}});
		
		displayResultsButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null) 
				{
					displayGraphsPanels(exp);
				}
			}});
	}
	
	private void selectResults(EnumSpotMeasures index) 
	{
		
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
		
		if (isThereAnyDataToDisplay(exp, EnumXLSExportType.AREA_NPIXELS))  
		{
			plotAreaPixels = plotToChart(exp, "N pixels above threshold", 
					EnumXLSExportType.AREA_NPIXELS, 
					plotAreaPixels, rectv);
			rectv.translate(dx, dy);
		}
	}
	
	private ChartAreas plotToChart(Experiment exp, String title, EnumXLSExportType option, 
											ChartAreas iChart, Rectangle rectv ) 
	{	
		if (iChart != null) 
			iChart.mainChartFrame.dispose();
		iChart = new ChartAreas();
		iChart.createChartPanel(parent0, title);
		iChart.setUpperLeftLocation(rectv);
		iChart.displayData(exp, option, title, false);
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
