package plugins.fmp.multiSPOTS.tools.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.roi.ROI2D;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.spots.Spot;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSExportType;
import plugins.fmp.multiSPOTS.tools.toExcel.XLSExport;
import plugins.fmp.multiSPOTS.tools.toExcel.XLSExportOptions;
import plugins.fmp.multiSPOTS.tools.toExcel.XLSResults;
import plugins.fmp.multiSPOTS.tools.toExcel.XLSResultsArray;

public class ChartSpots extends IcyFrame {
	public JPanel mainChartPanel = null;
	public IcyFrame mainChartFrame = null;

	private Point pt = new Point(0, 0);
	private boolean flagMaxMinSet = false;
	private double globalYMax = 0;
	private double globalYMin = 0;
	private double globalXMax = 0;

	private double ymax = 0;
	private double ymin = 0;
	private double xmax = 0;

	int nCagesAlongX = 1;
	int nCagesAlongY = 1;

	JPanel[][] panelHolder = null;

	// ----------------------------------------

//	public void createSpotsChartPanel(MultiSPOTS parent, String title) {
//		parent0 = parent;
//		mainChartPanel = new JPanel();
//		mainChartPanel.setLayout(new BoxLayout(mainChartPanel, BoxLayout.LINE_AXIS));
//		mainChartFrame = GuiUtil.generateTitleFrame(title, new JPanel(), new Dimension(300, 70), true, true, true,
//				true);
//		mainChartFrame.add(mainChartPanel);
//	}

	public void setLocationRelativeToRectangle(Rectangle rectv, Point deltapt) {
		pt = new Point(rectv.x + deltapt.x, rectv.y + deltapt.y);
	}

	public void setUpperLeftLocation(Rectangle rectv) {
		pt = new Point(rectv.x, rectv.y);
	}

	private XYLineAndShapeRenderer getSubPlotRenderer(XYSeriesCollection xySeriesCollection, Paint[] chartColor) {
		XYLineAndShapeRenderer subPlotRenderer = new XYLineAndShapeRenderer(true, false);
		int icolor = 0;
		int maxcolor = 1; // chartColor.length;
		Stroke stroke = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
				new float[] { 2.0f, 4.0f }, 0.0f);
		for (int i = 0; i < xySeriesCollection.getSeriesCount(); i++, icolor++) {
			if (icolor > maxcolor) {
				icolor = icolor + 13; // 0;
				subPlotRenderer.setSeriesStroke(i, stroke);
			}
			subPlotRenderer.setSeriesPaint(i, chartColor[icolor]);
		}
		return subPlotRenderer;
	}

	private XYPlot buildSubPlot(XYSeriesCollection xySeriesCollection, Paint[] chartColor) {
		XYLineAndShapeRenderer subPlotRenderer = getSubPlotRenderer(xySeriesCollection, chartColor);
		String[] description = xySeriesCollection.getSeries(0).getDescription().split("_");
		NumberAxis xAxis = new NumberAxis(description[0]);
		final XYPlot subplot = new XYPlot(xySeriesCollection, xAxis, null, subPlotRenderer);
		int nflies = Integer.valueOf(description[1]);
		if (nflies < 1) {
			subplot.setBackgroundPaint(Color.LIGHT_GRAY);
			subplot.setDomainGridlinePaint(Color.WHITE);
			subplot.setRangeGridlinePaint(Color.WHITE);
		} else {
			subplot.setBackgroundPaint(Color.WHITE);
			subplot.setDomainGridlinePaint(Color.GRAY);
			subplot.setRangeGridlinePaint(Color.GRAY);
		}
		return subplot;
	}

//	public void displayData(Experiment exp, XLSExportOptions xlsExportOptions) {
//		xyChartList.clear();
//
//		ymax = 0;
//		ymin = 0;
//		flagMaxMinSet = false;
//		List<XYSeriesCollection> xyDataSetList2 = null;
//		List<XYSeriesCollection> xyDataSetList = getDataArrays(exp, xlsExportOptions);
//		if (xlsExportOptions.exportType == EnumXLSExportType.AREA_SUMCLEAN) {
//			xlsExportOptions.exportType = EnumXLSExportType.AREA_SUM;
//			xyDataSetList2 = getDataArrays(exp, xlsExportOptions);
//			xlsExportOptions.exportType = EnumXLSExportType.AREA_SUMCLEAN;
//		}
//
//		NumberAxis yAxis = new NumberAxis(xlsExportOptions.exportType.toUnit());
//		if (xlsExportOptions.relativeToT0 || xlsExportOptions.relativeToMedianT0) {
//			yAxis.setLabel("ratio (t-t0)/t0 of " + yAxis.getLabel());
//			yAxis.setAutoRange(false);
//			yAxis.setRange(-0.2, 1.2);
//		} else {
//			yAxis.setAutoRange(true);
//			yAxis.setAutoRangeIncludesZero(false);
//		}
//
//		final CombinedRangeXYPlot combinedXYPlot = new CombinedRangeXYPlot(yAxis);
//		Paint[] chartColor = ChartColor.createDefaultPaintArray();
//
//		int firstSeries = 0;
//		int lastSeries = xyDataSetList.size();
//		if (xlsExportOptions.seriesIndexFirst >= 0) {
//			firstSeries = xlsExportOptions.seriesIndexFirst;
//			lastSeries = xlsExportOptions.seriesIndexLast;
//		}
//
//		for (int iseries = firstSeries; iseries < lastSeries; iseries++) {
//			XYSeriesCollection xySeriesCollection = createXYSeries(iseries, xyDataSetList, xyDataSetList2);
//			final XYPlot subplot = buildSubPlot(xySeriesCollection, chartColor);
//			combinedXYPlot.add(subplot);
//		}
//
//		JFreeChart chart = new JFreeChart(xlsExportOptions.exportType.toTitle(), null, combinedXYPlot, false); // true);
//		Font font = chart.getTitle().getFont().deriveFont(Font.BOLD, (float) 14.);
//		chart.getTitle().setFont(font);
//
//		int width = 800;
//		int height = 300;
//		int minimumDrawWidth = width;
//		int minimumDrawHeight = 300;
//		int maximumDrawWidth = 800;
//		int maximumDrawHeight = 500;
//		boolean useBuffer = true;
//
//		final ChartPanel panel = new ChartPanel(chart, width, height, minimumDrawWidth, minimumDrawHeight,
//				maximumDrawWidth, maximumDrawHeight, useBuffer, true, true, true, false, true); // boolean properties,
//																								// boolean save, boolean
//																								// print, boolean zoom,
//																								// boolean tooltips)
//
//		panel.addChartMouseListener(new ChartMouseListener() {
//			public void chartMouseClicked(ChartMouseEvent e) {
//				Spot clikedSpot = getClickedSpot(e);
//				selectSpot(exp, clikedSpot);
//				selectT(exp, xlsExportOptions, clikedSpot);
//				selectKymograph(exp, clikedSpot);
//			}
//
//			public void chartMouseMoved(ChartMouseEvent e) {
//			}
//		});
//
//		mainChartPanel.add(panel);
//		mainChartFrame.pack();
//		mainChartFrame.setLocation(pt);
//		mainChartFrame.addToDesktopPane();
//		mainChartFrame.setVisible(true);
//	}

//	private Spot getClickedSpot(ChartMouseEvent e) {
//		final MouseEvent trigger = e.getTrigger();
//		if (trigger.getButton() != MouseEvent.BUTTON1)
//			return null;
//
//		JFreeChart chart = e.getChart();
//		MouseEvent mouseEvent = e.getTrigger();
//		ChartPanel panel = (ChartPanel) mainChartPanel.getComponent(0);
//		PlotRenderingInfo plotInfo = panel.getChartRenderingInfo().getPlotInfo();
//		Point2D pointClicked = panel.translateScreenToJava2D(mouseEvent.getPoint());
//		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
//
//		// get chart
//		int subplotindex = plotInfo.getSubplotIndex(pointClicked);
//		CombinedRangeXYPlot combinedXYPlot = (CombinedRangeXYPlot) chart.getPlot();
//		@SuppressWarnings("unchecked")
//		List<XYPlot> subplots = combinedXYPlot.getSubplots();
//
//		// get item in the chart
//		Spot spotFound = null;
//		String description = null;
//		ChartEntity chartEntity = e.getEntity();
//		if (chartEntity != null && chartEntity instanceof XYItemEntity) {
//			XYItemEntity xyItemEntity = (XYItemEntity) chartEntity;
//			int isel = xyItemEntity.getSeriesIndex();
//			XYDataset xyDataset = xyItemEntity.getDataset();
//			description = (String) xyDataset.getSeriesKey(isel);
//
//			spotFound = exp.spotsArray.getSpotContainingName(description.substring(0, 5));
//			spotFound.spot_CamData_T = xyItemEntity.getItem();
//		} else if (subplotindex >= 0) {
//			XYDataset xyDataset = subplots.get(subplotindex).getDataset(0);
//			description = (String) xyDataset.getSeriesKey(0);
//
//			spotFound = exp.spotsArray.getSpotContainingName(description.substring(0, 5));
//		} else {
//			System.out.println("Graph clicked but source not found");
//			return null;
//		}
//		String lastN = description.substring(4, 5);
//		int foo;
//		try {
//			foo = Integer.parseInt(lastN);
//		} catch (NumberFormatException e1) {
//			foo = 0;
//		}
//		spotFound.spot_Kymograph_T = 2 * spotFound.cageID + foo;
//		return spotFound;
//	}

//	private void selectSpot(Experiment exp, Spot spot) {
//		Viewer v = exp.seqCamData.seq.getFirstViewer();
//		if (v != null && spot != null) {
//			ROI2D roi = spot.getRoi_in();
//			exp.seqCamData.seq.setFocusedROI(roi);
//		}
//	}

//	private void selectT(Experiment exp, XLSExportOptions xlsExportOptions, Spot spot) {
//		Viewer v = exp.seqCamData.seq.getFirstViewer();
//		if (v != null && spot != null && spot.spot_CamData_T > 0) {
//			int ii = (int) (spot.spot_CamData_T * xlsExportOptions.buildExcelStepMs / exp.seqCamData.binDuration_ms);
//			v.setPositionT(ii);
//		}
//	}

//	private void selectKymograph(Experiment exp, Spot spot) {
//		if (exp.seqSpotKymos != null) {
//			Viewer v = exp.seqSpotKymos.seq.getFirstViewer();
//			if (v != null && spot != null) {
//				v.setPositionT(spot.spot_Kymograph_T);
//			}
//		}
//	}

	private XLSResultsArray getDataAsResultsArray(Experiment exp, XLSExportOptions xlsExportOptions) {
		XLSExport xlsExport = new XLSExport();
		return xlsExport.getSpotsDataFromOneExperiment(exp, xlsExportOptions);
	}

	private void updateGlobalMaxMin() {
		if (!flagMaxMinSet) {
			globalYMax = ymax;
			globalYMin = ymin;
			globalXMax = xmax;
			flagMaxMinSet = true;
		} else {
			if (globalYMax < ymax)
				globalYMax = ymax;
			if (globalYMin >= ymin)
				globalYMin = ymin;
			if (globalXMax < xmax)
				globalXMax = xmax;
		}
	}

	private XYSeries getXYSeries(XLSResults results, String name) {
		XYSeries seriesXY = new XYSeries(name, false);
		if (results.valuesOut != null && results.valuesOut.length > 0) {
			xmax = results.valuesOut.length;
			ymax = results.valuesOut[0];
			ymin = ymax;
			addPointsAndUpdateExtrema(seriesXY, results, 0);
		}
		return seriesXY;
	}

	private void addPointsAndUpdateExtrema(XYSeries seriesXY, XLSResults results, int startFrame) {
		int x = 0;
		int npoints = results.valuesOut.length;
		for (int j = 0; j < npoints; j++) {
			double y = results.valuesOut[j];
			seriesXY.add(x + startFrame, y);
			if (ymax < y)
				ymax = y;
			if (ymin > y)
				ymin = y;
			x++;
		}
	}

	public void createSpotsChartPanel2(String title, Experiment exp) {
		mainChartPanel = new JPanel();
		mainChartFrame = GuiUtil.generateTitleFrame(title, new JPanel(), new Dimension(300, 70), true, true, true,
				true);
		mainChartFrame.add(mainChartPanel);

		nCagesAlongX = exp.spotsArray.nColumnsPerPlate / exp.spotsArray.nColumnsPerCage;
		nCagesAlongY = exp.spotsArray.nRowsPerPlate / exp.spotsArray.nRowsPerCage;
		panelHolder = new JPanel[nCagesAlongX][nCagesAlongY];
		mainChartPanel.setLayout(new GridLayout(nCagesAlongX, nCagesAlongY));

		for (int m = 0; m < nCagesAlongX; m++) {
			for (int n = 0; n < nCagesAlongY; n++) {
				panelHolder[m][n] = new JPanel();
				add(panelHolder[m][n]);
			}
		}

//		Then later, you can add directly to one of the JPanel objects:
//		panelHolder[2][3].add(new JButton("Foo"));

	}

	public void displayData2(Experiment exp, XLSExportOptions xlsExportOptions) {

		ymax = 0;
		ymin = 0;
		flagMaxMinSet = false;

		NumberAxis yAxis = new NumberAxis(xlsExportOptions.exportType.toUnit());
		if (xlsExportOptions.relativeToT0 || xlsExportOptions.relativeToMedianT0) {
			yAxis.setLabel("ratio (t-t0)/t0 of " + yAxis.getLabel());
			yAxis.setAutoRange(false);
			yAxis.setRange(-0.2, 1.2);
		} else {
			yAxis.setAutoRange(true);
			yAxis.setAutoRangeIncludesZero(false);
		}

		Paint[] chartColor = ChartColor.createDefaultPaintArray();
		
		XLSResultsArray xlsResultsArray = getDataAsResultsArray(exp, xlsExportOptions);
		XLSResultsArray xlsResultsArray2 = null;
		if (xlsExportOptions.exportType == EnumXLSExportType.AREA_SUMCLEAN) {
			xlsExportOptions.exportType = EnumXLSExportType.AREA_SUM;
			xlsResultsArray2 = getDataAsResultsArray(exp, xlsExportOptions);
			xlsExportOptions.exportType = EnumXLSExportType.AREA_SUMCLEAN;
		}
		

		// ---------------------------
		int cageID = 0;
		for (int row = 0; row < nCagesAlongY; row++) {
			for (int col = 0; col < nCagesAlongX; col++) {
				CombinedRangeXYPlot combinedXYPlot = getCombinedRangeXYPlotOfOneCage(cageID, yAxis, chartColor, xlsResultsArray, xlsResultsArray2 );
				JFreeChart chart = new JFreeChart(xlsExportOptions.exportType.toTitle(), null, combinedXYPlot, false); // true);
				Font font = chart.getTitle().getFont().deriveFont(Font.BOLD, (float) 14.);
				chart.getTitle().setFont(font);

				int width = 800;
				int height = 300;
				int minimumDrawWidth = width;
				int minimumDrawHeight = 300;
				int maximumDrawWidth = 800;
				int maximumDrawHeight = 500;
				boolean useBuffer = true;

				final ChartPanel panel = new ChartPanel(chart, width, height, minimumDrawWidth, minimumDrawHeight,
						maximumDrawWidth, maximumDrawHeight, useBuffer, true, true, true, false, true);
				// boolean properties, boolean save, boolean print, boolean zoom, boolean
				// tooltips)

				// panel.addChartMouseListener(new ChartMouseListener() {
				// public void chartMouseClicked(ChartMouseEvent e) {
				// Spot clikedSpot = getClickedSpot(e);
				// selectSpot(exp, clikedSpot);
				// selectT(exp, xlsExportOptions, clikedSpot);
				// selectKymograph(exp, clikedSpot);
				// }
				//
				// public void chartMouseMoved(ChartMouseEvent e) {
				// }
				// });

				panelHolder[col][row].add(panel);
				cageID++;
			}
		}

		// -----------------------------------
		mainChartFrame.pack();
		mainChartFrame.setLocation(pt);
		mainChartFrame.addToDesktopPane();
		mainChartFrame.setVisible(true);
	}

	private CombinedRangeXYPlot getCombinedRangeXYPlotOfOneCage(int cageID, NumberAxis yAxis, Paint[] chartColor, XLSResultsArray xlsResultsArray, XLSResultsArray xlsResultsArray2) {
		CombinedRangeXYPlot combinedXYPlot = new CombinedRangeXYPlot(yAxis);
		List<XYSeriesCollection> xyDataSetList = getDataArraysOfOneCage(xlsResultsArray, cageID);
		List<XYSeriesCollection> xyDataSetList2 = null;
		if (xlsResultsArray2 != null) 
			xyDataSetList2 = getDataArraysOfOneCage(xlsResultsArray2, cageID);	
		
		for (int iseries = 0; iseries < xyDataSetList.size(); iseries++) {
			boolean flag = true;
			if (flag) {
				XYSeriesCollection xySeriesCollection = createXYSeriesCollection(iseries, xyDataSetList, xyDataSetList2);
				final XYPlot subplot = buildSubPlot(xySeriesCollection, chartColor);
				combinedXYPlot.add(subplot);
			}
		}
		return combinedXYPlot;
	}
	
	private List<XYSeriesCollection> getDataArraysOfOneCage(XLSResultsArray xlsResultsArray, int cageID) {		
		XYSeriesCollection xySeriesCollection = null;


		List<XYSeriesCollection> xyList = new ArrayList<XYSeriesCollection>();
		for (int i = 0; i < xlsResultsArray.size(); i++) {
			XLSResults xlsResults = xlsResultsArray.getRow(i);
			if (cageID != xlsResults.cageID) {
				xySeriesCollection = new XYSeriesCollection();
				xyList.add(xySeriesCollection);
			}
			XYSeries seriesXY = getXYSeries(xlsResults, xlsResults.name); //.substring(4));
			seriesXY.setDescription("ID:" + xlsResults.cageID + ":Pos:" + xlsResults.cagePosition + ":nflies:"
					+ xlsResults.nflies);
			xySeriesCollection.addSeries(seriesXY);
			updateGlobalMaxMin();
		}
		return xyList;
	}

//	private List<XYSeriesCollection> getDataArrays(Experiment exp, XLSExportOptions xlsExportOptions) {
//		XLSResultsArray xlsResultsArray = getDataAsResultsArray(exp, xlsExportOptions);
	
//		XYSeriesCollection xySeriesCollection = null;
//		int oldcage = -1;
//
//		List<XYSeriesCollection> xyList = new ArrayList<XYSeriesCollection>();
//		for (int iRow = 0; iRow < xlsResultsArray.size(); iRow++) {
//			XLSResults xlsResults = xlsResultsArray.getRow(iRow);
//			if (oldcage != xlsResults.cageID) {
//				xySeriesCollection = new XYSeriesCollection();
//				oldcage = xlsResults.cageID;
//				xyList.add(xySeriesCollection);
//			}
//			XYSeries seriesXY = getXYSeries(xlsResults, xlsResults.name); //.substring(4));
//			seriesXY.setDescription("ID:" + xlsResults.cageID + ":Pos:" + xlsResults.cagePosition + ":nflies:"
//					+ xlsResults.nflies);
//			xySeriesCollection.addSeries(seriesXY);
//			updateGlobalMaxMin();
//		}
//		return xyList;
//	}

	private XYSeriesCollection createXYSeriesCollection(int iseries, List<XYSeriesCollection> xyDataSetList,
			List<XYSeriesCollection> xyDataSetList2) {
		XYSeriesCollection xySeriesCollection = xyDataSetList.get(iseries);

		if (xyDataSetList2 != null) {
			XYSeriesCollection xySeriesCollection2 = xyDataSetList2.get(iseries);
			for (int j = 0; j < xySeriesCollection2.getSeriesCount(); j++) {
				XYSeries xySeries = xySeriesCollection2.getSeries(j);
				System.out.println("iseries=" + iseries + " j=" + j + " key=" + xySeries.getKey() + " description:"
						+ xySeries.getDescription());
				xySeries.setKey(xySeries.getKey() + "*");
				xySeriesCollection.addSeries(xySeries);
			}
		}
		return xySeriesCollection;
	}

}
