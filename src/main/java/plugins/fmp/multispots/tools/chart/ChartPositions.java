package plugins.fmp.multispots.tools.chart;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import plugins.fmp.multispots.experiment.Cage;
import plugins.fmp.multispots.experiment.XYTaSeriesArrayList;
import plugins.fmp.multispots.tools.MaxMinDouble;
import plugins.fmp.multispots.tools.toExcel.EnumXLSExportType;


public class ChartPositions extends IcyFrame 
{
	public JPanel 	mainChartPanel = null;
	private ArrayList<ChartPanel> chartsInMainChartPanel = null;
	public IcyFrame mainChartFrame = null;
	private String 	title;
	private Point 	pt = new Point (0,0);
	private double globalXMax = 0;
	
	public void createPanel(String cstitle) 
	{
		title = cstitle; 
		mainChartFrame = GuiUtil.generateTitleFrame(title, new JPanel(), new Dimension(300, 70), true, true, true, true);	    
		mainChartPanel = new JPanel(); 
		mainChartPanel.setLayout( new BoxLayout( mainChartPanel, BoxLayout.LINE_AXIS ) );
		mainChartFrame.add(mainChartPanel);
	}
	
	public void setLocationRelativeToRectangle(Rectangle rectv, Point deltapt) 
	{
		pt = new Point(rectv.x + deltapt.x, rectv.y + deltapt.y);
	}
	
	public void displayData(List<Cage> cageList, EnumXLSExportType option) 
	{
		List<XYSeriesCollection> xyDataSetList = new ArrayList <XYSeriesCollection>();
		MaxMinDouble yMaxMin = new MaxMinDouble();
		int count = 0;
		for (Cage cage: cageList) 
		{
			if (cage.flyPositions != null && cage.flyPositions.xytArrayList.size() > 0) 
			{	
				ChartData chartData = getDataSet(cage, option);
				XYSeriesCollection xyDataset = chartData.xyDataset;
				yMaxMin = chartData.yMaxMin;
				if (count != 0)
					yMaxMin.getMaxMin(chartData.yMaxMin);
				xyDataSetList.add(xyDataset);
				count++;
			}
		}
		
		cleanChartsPanel(chartsInMainChartPanel);
		int width = 100;
		boolean displayLabels = false; 
		
		for (XYSeriesCollection xyDataset: xyDataSetList) 
		{
			JFreeChart xyChart = ChartFactory.createXYLineChart(null, null, null, xyDataset, PlotOrientation.VERTICAL, true, true, true);
			xyChart.setAntiAlias( true );
			xyChart.setTextAntiAlias( true );
			
			ValueAxis yAxis = xyChart.getXYPlot().getRangeAxis(0);
			yAxis.setRange(yMaxMin.min, yMaxMin.max);
			yAxis.setTickLabelsVisible(displayLabels);
			
			ValueAxis xAxis = xyChart.getXYPlot().getDomainAxis(0);
			xAxis.setRange(0, globalXMax);
			
			ChartPanel xyChartPanel = new ChartPanel(xyChart, width, 200, 50, 100, 100, 200, false, false, true, true, true, true);
			mainChartPanel.add(xyChartPanel);
			width = 100;
			displayLabels = false; 
		}

		mainChartFrame.pack();
		mainChartFrame.setLocation(pt);
		mainChartFrame.addToDesktopPane ();
		mainChartFrame.setVisible(true);
	}
	
	private MaxMinDouble addPointsToXYSeries(Cage cage, EnumXLSExportType option, XYSeries seriesXY) 
	{
		XYTaSeriesArrayList results = cage.flyPositions;
		int itmax = results.xytArrayList.size();
		MaxMinDouble yMaxMin = null;
		if (itmax > 0) 
		{
			switch (option) 
			{
			case DISTANCE:
				double previousY = results.xytArrayList.get(0).rectBounds.getY() 
									+ results.xytArrayList.get(0).rectBounds.getHeight()/2;
				
				for ( int it = 0; it < itmax;  it++) 
				{
					double currentY = results.xytArrayList.get(it).rectBounds.getY() 
							+ results.xytArrayList.get(it).rectBounds.getHeight()/2;
					double ypos = currentY - previousY;
					addxyPos(seriesXY, results, it, ypos);
					previousY = currentY;
				}
				Rectangle rect = cage.cageRoi2D.getBounds();
				double length_diagonal = Math.sqrt((rect.height*rect.height) + (rect.width*rect.width));
				yMaxMin = new MaxMinDouble(0.0, length_diagonal);
				break;
				
			case ISALIVE:
				for ( int it = 0; it < itmax;  it++) 
				{
					boolean alive = results.xytArrayList.get(it).bAlive;
					double ypos = alive? 1.0: 0.0;
					addxyPos(seriesXY, results, it, ypos);
				}
				yMaxMin = new MaxMinDouble(0., 1.2);
				break;
				
			case SLEEP:
				for ( int it = 0; it < itmax;  it++) 
				{
					boolean sleep = results.xytArrayList.get(it).bSleep;
					double ypos = sleep ? 1.0: 0.0;
					addxyPos(seriesXY, results, it, ypos);
				}
				yMaxMin = new MaxMinDouble(0., 1.2);
				break;
				
			default:
				Rectangle rect1 = cage.cageRoi2D.getBounds();
				double yOrigin = rect1.getY() + rect1.getHeight();	
				for ( int it = 0; it < itmax;  it++) 
				{
					Rectangle2D itRect = results.xytArrayList.get(it).rectBounds;
					double ypos = yOrigin - itRect.getY();
					addxyPos(seriesXY, results, it, ypos);
				}
				yMaxMin = new MaxMinDouble(0., rect1.height * 1.2);
				break;
			}
		}
		return yMaxMin;
	}
	
	private void addxyPos(XYSeries seriesXY, XYTaSeriesArrayList positionxyt, int it, Double ypos)
	{
		double indexT = positionxyt.xytArrayList.get(it).indexT;
		seriesXY.add( indexT, ypos );
		if (globalXMax < indexT)
			globalXMax = indexT;
	}
	
	private ChartData getDataSet(Cage cage, EnumXLSExportType option) 
	{
		XYSeriesCollection xyDataset = new XYSeriesCollection();	
		String name = cage.cageRoi2D.getName();
		XYSeries seriesXY = new XYSeries(name);
		seriesXY.setDescription(name);
		MaxMinDouble yMaxMin = addPointsToXYSeries(cage, option, seriesXY);
		xyDataset.addSeries(seriesXY);
		return new ChartData(new MaxMinDouble(globalXMax, 0), yMaxMin, xyDataset);
	}
	
	private void cleanChartsPanel (ArrayList<ChartPanel> chartsPanel) 
	{
		if (chartsPanel != null && chartsPanel.size() > 0) 
			chartsPanel.clear();
	}

}
