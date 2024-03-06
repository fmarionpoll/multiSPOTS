package plugins.fmp.multiSPOTS.tools.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.tools.toExcel.XLSExport;
import plugins.fmp.multiSPOTS.tools.toExcel.XLSExportOptions;
import plugins.fmp.multiSPOTS.tools.toExcel.XLSResults;
import plugins.fmp.multiSPOTS.tools.toExcel.XLSResultsArray;

public class ChartAreas extends IcyFrame
{
	public JPanel 	mainChartPanel 	= null;
	public IcyFrame mainChartFrame 	= null;
	private MultiSPOTS 	parent0 	= null;
	
	private Point pt = new Point (0,0);
	private boolean flagMaxMinSet = false;
	private double globalYMax = 0;
	private double globalYMin = 0;
	private double globalXMax = 0;

	private double ymax = 0;
	private double ymin = 0;
	private double xmax = 0;
	private List<JFreeChart> xyChartList  = new ArrayList <JFreeChart>();


	//----------------------------------------
	
	public void createChartPanel(MultiSPOTS parent, String cstitle) 
	{
		parent0 = parent;
		
		mainChartPanel = new JPanel(); 
		mainChartPanel.setLayout( new BoxLayout( mainChartPanel, BoxLayout.LINE_AXIS ) );
		
		mainChartFrame = GuiUtil.generateTitleFrame(cstitle, new JPanel(), new Dimension(300, 70), true, true, true, true);	    
		mainChartFrame.add(mainChartPanel);
	}

	public void setLocationRelativeToRectangle(Rectangle rectv, Point deltapt) 
	{
		pt = new Point(rectv.x + deltapt.x, rectv.y + deltapt.y);
	}
	
	public void setUpperLeftLocation(Rectangle rectv) 
	{
		pt = new Point(rectv.x, rectv.y);
	}
	
	public void displayData(Experiment exp, XLSExportOptions xlsExportOptions) 
	{
		xyChartList.clear();
		ymax = 0;
		ymin = 0;
		flagMaxMinSet = false;
		List<XYSeriesCollection> xyDataSetList = getDataArrays(exp, xlsExportOptions);
		
		int icage = 0;
        final NumberAxis yAxis = new NumberAxis(xlsExportOptions.exportType.toUnit());
        yAxis.setAutoRangeIncludesZero(false);  
//        yAxis.setInverted(true);
        final CombinedRangeXYPlot combinedXYPlot = new CombinedRangeXYPlot(yAxis);
        Paint[] color = ChartColor.createDefaultPaintArray();

		for (XYSeriesCollection xySeriesCollection : xyDataSetList) 
		{
			NumberAxis xAxis = new NumberAxis("Cage " + icage);
			XYLineAndShapeRenderer subPlotRenderer = new XYLineAndShapeRenderer(true, false);
			final XYPlot subplot = new XYPlot(xySeriesCollection, xAxis, null, subPlotRenderer);
			int icolor = 0;
			for (int i = 0; i < xySeriesCollection.getSeriesCount(); i++, icolor++ )
			{
				if (icolor > color.length)
					icolor = 0;
				subPlotRenderer.setSeriesPaint(i, color[icolor]);
			}
			subplot.setBackgroundPaint(Color.LIGHT_GRAY);
			subplot.setDomainGridlinePaint(Color.WHITE);
			subplot.setRangeGridlinePaint(Color.WHITE);
			combinedXYPlot.add(subplot);
						
			icage++;
		}
		
        JFreeChart chart = new JFreeChart(xlsExportOptions.exportType.toTitle(), null, combinedXYPlot, true);
        Font font = chart.getTitle().getFont().deriveFont(Font.BOLD, (float) 14.);
        chart.getTitle().setFont(font);

        int width= 800;
        int height= 300;
        int minimumDrawWidth = width;
        int minimumDrawHeight = 300;
        int maximumDrawWidth = 800;
        int maximumDrawHeight = 500;
        boolean useBuffer= true;
        
        final ChartPanel panel = new ChartPanel(chart, 
        		width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight, 
        		useBuffer,
        		true, true, true, false, true); // boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips)
        panel.addChartMouseListener(new ChartMouseListener() {
		    public void chartMouseClicked(ChartMouseEvent e) {
		    	selectKymoImage(getSelectedCurve(e)); }
		    public void chartMouseMoved(ChartMouseEvent e) {}
		});
    	
		mainChartPanel.add(panel);
		mainChartFrame.pack();
		mainChartFrame.setLocation(pt);
		mainChartFrame.addToDesktopPane ();
		mainChartFrame.setVisible(true);
	}
	
	private int getSelectedCurve(ChartMouseEvent e) 
	{
		final MouseEvent trigger = e.getTrigger();
        if (trigger.getButton() != MouseEvent.BUTTON1)
        	return -1;
        
		JFreeChart chart = e.getChart();
		ChartEntity chartEntity = e.getEntity();
		MouseEvent mouseEvent = e.getTrigger();

		int isel= 0;
		if (chartEntity != null && chartEntity instanceof XYItemEntity) {
		   XYItemEntity xyItemEntity = ((XYItemEntity) e.getEntity());
		   isel += xyItemEntity.getSeriesIndex();
		}

		CombinedRangeXYPlot combinedXYPlot = (CombinedRangeXYPlot) chart.getPlot();
		@SuppressWarnings("unchecked")
        List<XYPlot> subplots = combinedXYPlot.getSubplots();
		
		ChartPanel panel = (ChartPanel) mainChartPanel.getComponent(0);
		PlotRenderingInfo plotInfo = panel.getChartRenderingInfo().getPlotInfo();
		Point2D p = panel.translateScreenToJava2D(mouseEvent.getPoint());
		int subplotindex = plotInfo.getSubplotIndex(p);
		for (int i= 0; i < subplotindex ; i++)
			isel += subplots.get(i).getSeriesCount();

		return isel;
	}

	private void selectKymoImage(int isel)
	{
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
        Viewer v = exp.seqKymos.seq.getFirstViewer();
        if (v != null && isel >= 0)
        	v.setPositionT(isel);
	}

	private List<XYSeriesCollection> getDataArrays(Experiment exp, XLSExportOptions xlsExportOptions) 
	{
		XLSResultsArray xlsResultsArray = getDataAsResultsArray(exp, xlsExportOptions);
		XYSeriesCollection xySeriesCollection = null;
		int oldcage = -1;
		
		List<XYSeriesCollection> xyList = new ArrayList<XYSeriesCollection>();
		for (int iRow = 0; iRow < xlsResultsArray.size(); iRow++ ) 
		{
			XLSResults xlsResults = xlsResultsArray.getRow(iRow);
			if (oldcage != xlsResults.cageID ) 
			{
				xySeriesCollection = new XYSeriesCollection();
				oldcage = xlsResults.cageID; 
				xyList.add(xySeriesCollection);
			}
			XYSeries seriesXY = getXYSeries(xlsResults, xlsResults.name.substring(4));
			xySeriesCollection.addSeries(seriesXY );
			updateGlobalMaxMin();
		}
		return xyList;
	}
	
	private XLSResultsArray getDataAsResultsArray(Experiment exp, XLSExportOptions xlsExportOptions)
	{
		XLSExport xlsExport = new XLSExport();
		return xlsExport.getSpotsDataFromOneExperiment(exp, xlsExportOptions);
	}

	private void updateGlobalMaxMin() 
	{
		if (!flagMaxMinSet) 
		{
			globalYMax = ymax;
			globalYMin = ymin;
			globalXMax = xmax;
			flagMaxMinSet = true;
		}
		else 
		{
			if (globalYMax < ymax) globalYMax = ymax;
			if (globalYMin >= ymin) globalYMin = ymin;
			if (globalXMax < xmax) globalXMax = xmax;
		}
	}
	
	private XYSeries getXYSeries(XLSResults results, String name) 
	{
		XYSeries seriesXY = new XYSeries(name, false);
		if (results.valuesOut != null && results.valuesOut.length > 0) 
		{
			xmax = results.valuesOut.length;
			ymax = results.valuesOut[0];
			ymin = ymax;
			addPointsAndUpdateExtrema(seriesXY, results, 0);	
		}
		return seriesXY;
	}
	
	private void addPointsAndUpdateExtrema(XYSeries seriesXY, XLSResults results, int startFrame) 
	{
		int x = 0;
		int npoints = results.valuesOut.length;
		for (int j = 0; j < npoints; j++) 
		{
			double y = results.valuesOut[j];
			seriesXY.add( x+startFrame , y );
			if (ymax < y) ymax = y;
			if (ymin > y) ymin = y;
			x++;
		}
	}

}
