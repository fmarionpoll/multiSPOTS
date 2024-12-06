package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.gui.frame.progress.AnnounceFrame;
import icy.roi.ROI2D;
import icy.type.geom.Polygon2D;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.ExperimentUtils;
import plugins.fmp.multiSPOTS.experiment.SequenceCamData;
import plugins.fmp.multiSPOTS.tools.polyline.PolygonUtilities;
import plugins.kernel.roi.roi2d.ROI2DEllipse;
import plugins.kernel.roi.roi2d.ROI2DPolygon;

public class CreateSpots extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5257698990389571518L;

	private JButton displayFrameDButton = new JButton("(1) Display frame");
	private JButton createCirclesButton = new JButton("(2) Create circles");

	private JSpinner cageNColumnsJSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 500, 1));
	private JSpinner cageNrowsJSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
	private JSpinner nFliesPerCageJSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 500, 1));
	private JSpinner pixelRadiusSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 1000, 1));

	private JSpinner nRowsJSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 100, 1));
	private JSpinner nColumnsJSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));

	private Polygon2D roiPolygon = null;
	private String[] flyString = new String[] { "fly", "flies" };
	private JLabel flyLabel = new JLabel(flyString[0]);

	private String[] position = new String[] { "left", "right" };
	private JComboBox<String> notchJComboBox = new JComboBox<String>(position);
	private String[] viewFrom = new String[] { "bottom", "top" };
	private JComboBox<String> viewFromJComboBox = new JComboBox<String>(viewFrom);

	private MultiSPOTS parent0 = null;

	void init(GridLayout capLayout, MultiSPOTS parent0) {
		setLayout(capLayout);
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
		flowLayout.setVgap(0);

		JPanel panel0 = new JPanel(flowLayout);
		panel0.add(displayFrameDButton);
		panel0.add(createCirclesButton);
		panel0.add(pixelRadiusSpinner);
		pixelRadiusSpinner.setPreferredSize(new Dimension(40, 20));
		panel0.add(new JLabel("pixels"));

		JPanel panel1 = new JPanel(flowLayout);
		panel1.add(new JLabel("Spots:"));
		panel1.add(nColumnsJSpinner);
		nColumnsJSpinner.setPreferredSize(new Dimension(40, 20));
		panel1.add(new JLabel("cols"));
		panel1.add(nRowsJSpinner);
		nRowsJSpinner.setPreferredSize(new Dimension(40, 20));
		panel1.add(new JLabel("rows"));
		panel1.add(notchJComboBox);
		panel1.add(new JLabel("notch"));
		panel1.add(viewFromJComboBox);
		panel1.add(new JLabel("view"));

		JPanel panel2 = new JPanel(flowLayout);
		panel2.add(new JLabel("Cage:"));

		panel2.add(cageNColumnsJSpinner);
		cageNColumnsJSpinner.setPreferredSize(new Dimension(40, 20));
		panel2.add(new JLabel("cols"));
		panel2.add(cageNrowsJSpinner);
		cageNrowsJSpinner.setPreferredSize(new Dimension(40, 20));
		panel2.add(new JLabel("rows with"));

		panel2.add(nFliesPerCageJSpinner);
		nFliesPerCageJSpinner.setPreferredSize(new Dimension(40, 20));
		panel2.add(flyLabel);

		add(panel0);
		add(panel1);
		add(panel2);

		defineActionListeners();
		this.parent0 = parent0;
	}

	private void defineActionListeners() {
		displayFrameDButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
//				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
//				if ((exp != null) && (exp.capillaries != null)) {
//					Polygon2D extPolygon = exp.capillaries.get2DPolygonEnclosingCapillaries();
//					if (extPolygon == null) {
//						extPolygon = getCapillariesPolygon(exp.seqCamData);
//					}
//					ROI2DPolygon extRect = new ROI2DPolygon(extPolygon);
//					exp.capillaries.deleteAllCapillaries();
//					exp.capillaries.updateCapillariesFromSequence(exp.seqCamData.seq);
//					exp.seqCamData.seq.removeAllROI();
//					final String dummyname = "perimeter_enclosing_capillaries";
//					extRect.setName(dummyname);
//					exp.seqCamData.seq.addROI(extRect);
//					exp.seqCamData.seq.setSelectedROI(extRect);
//				} else
				create2DPolygon();
//			}
//				}
			}
		});

//		createPolylinesButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(final ActionEvent e) {
//				roisGenerateFromPolygon();
//				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
//				if (exp != null) {
//					ExperimentUtils.transferCamDataROIStoCapillaries(exp);
//					int nbFliesPerCage = (int) nbFliesPerCageJSpinner.getValue();
//					exp.capillaries.initCapillariesWith10Cages(nbFliesPerCage, false);
//				}
//			}
//		});

		createCirclesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					roisGenerateFromPolygon();
//					int radius = (int) pixelRadiusSpinner.getValue();
//					ExperimentUtils.transformPolygon2DROISintoSpots(exp, radius);
					ExperimentUtils.transferSpotsToCamData(exp);
					int nbFliesPerCage = (int) nFliesPerCageJSpinner.getValue();
					exp.spotsArray.initSpotsWithNFlies(nbFliesPerCage);
				}
			}
		});

		nFliesPerCageJSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int i = (int) nFliesPerCageJSpinner.getValue() > 1 ? 1 : 0;
				flyLabel.setText(flyString[i]);
				nFliesPerCageJSpinner.requestFocus();
			}
		});
	}

	// ---------------------------------

	private void create2DPolygon() {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp == null)
			return;
		SequenceCamData seqCamData = exp.seqCamData;
		final String dummyname = "perimeter_enclosing_capillaries";
		if (isRoiPresent(seqCamData, dummyname))
			return;

		ROI2DPolygon roi = new ROI2DPolygon(getCapillariesPolygon(seqCamData));
		roi.setName(dummyname);
		seqCamData.seq.addROI(roi);
		seqCamData.seq.setSelectedROI(roi);
	}

	private boolean isRoiPresent(SequenceCamData seqCamData, String dummyname) {
		ArrayList<ROI2D> listRois = seqCamData.seq.getROI2Ds();
		for (ROI2D roi : listRois) {
			if (roi.getName().equals(dummyname))
				return true;
		}
		return false;
	}

	private Polygon2D getCapillariesPolygon(SequenceCamData seqCamData) {
		if (roiPolygon == null) {
			Rectangle rect = seqCamData.seq.getBounds2D();
			List<Point2D> points = new ArrayList<Point2D>();
			points.add(new Point2D.Double(rect.x + rect.width / 5, rect.y + rect.height / 5));
			points.add(new Point2D.Double(rect.x + rect.width * 4 / 5, rect.y + rect.height / 5));
			points.add(new Point2D.Double(rect.x + rect.width * 4 / 5, rect.y + rect.height * 2 / 3));
			points.add(new Point2D.Double(rect.x + rect.width / 5, rect.y + rect.height * 2 / 3));
			roiPolygon = new Polygon2D(points);
		}
		return roiPolygon;
	}

	private void roisGenerateFromPolygon() {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp == null)
			return;
		SequenceCamData seqCamData = exp.seqCamData;

		ROI2D roi = seqCamData.seq.getSelectedROI2D();
		if (!(roi instanceof ROI2DPolygon)) {
			new AnnounceFrame("The frame must be a ROI2D Polygon");
			return;
		}
		roiPolygon = PolygonUtilities.orderVerticesofPolygon(((ROI2DPolygon) roi).getPolygon());
		seqCamData.seq.removeROI(roi);

		int n_columns = 10;
		int n_rows = 1;
		try {
			n_columns = (int) nColumnsJSpinner.getValue();
			n_rows = (int) nRowsJSpinner.getValue();
		} catch (Exception e) {
			new AnnounceFrame("Can't interpret one of the ROI parameters value");
		}
//		int nSpots = n_rows * n_columns;
		Point2D.Double[][] arrayPoints = createArrayOfPointsFromSelectedPolygon(roiPolygon, n_columns, n_rows);
		int radius = (int) pixelRadiusSpinner.getValue();
		convertPoint2DArrayToCircles(arrayPoints, n_columns, n_rows, radius);

		// -----------------------------------------------------------------------

//		if (statusGroup2Mode) {
//			double colspan = (nbcapillaries / 2) * (width_between_capillaries + width_interval) - width_interval;
//			for (int i = 0; i < nbcapillaries; i += 2) {
//				double colspan0 = (width_between_capillaries + width_interval) * i / 2;
//				addROILine(seqCamData, "line" + i / 2 + "L", spotsPolygon, colspan0, colspan);
//				colspan0 += width_between_capillaries;
//				addROILine(seqCamData, "line" + i / 2 + "R", spotsPolygon, colspan0, colspan);
//			}
//		} else {
//			double colspan = nbcapillaries - 1;
//			for (int i = 0; i < nbcapillaries; i++) {
//				double colspan0 = width_between_capillaries * i;
//				addROILine(seqCamData, "line" + String.format("%02d", i), spotsPolygon, colspan0, colspan);
//			}
//		}
	}

//	private void addROILine(SequenceCamData seqCamData, String name, Polygon2D roiPolygon, double colspan0,
//			double colspan) {
//		double x0 = roiPolygon.xpoints[0] + (roiPolygon.xpoints[3] - roiPolygon.xpoints[0]) * colspan0 / colspan;
//		double y0 = roiPolygon.ypoints[0] + (roiPolygon.ypoints[3] - roiPolygon.ypoints[0]) * colspan0 / colspan;
//		if (x0 < 0)
//			x0 = 0;
//		if (y0 < 0)
//			y0 = 0;
//		double x1 = roiPolygon.xpoints[1] + (roiPolygon.xpoints[2] - roiPolygon.xpoints[1]) * colspan0 / colspan;
//		double y1 = roiPolygon.ypoints[1] + (roiPolygon.ypoints[2] - roiPolygon.ypoints[1]) * colspan0 / colspan;
//		int npoints = (int) cageNColumnsJSpinner.getValue();
//
//		ROI2DPolyLine roiL1 = new ROI2DPolyLine(createPolyline2D(x0, y0, x1, y1, npoints));
//		roiL1.setName(name);
//		roiL1.setReadOnly(false);
//		seqCamData.seq.addROI(roiL1, true);
//	}

//	private Polyline2D createPolyline2D(double x0, double y0, double x1, double y1, int npoints) {
//		double[] xpoints = new double[npoints];
//		double[] ypoints = new double[npoints];
//		double deltax = (x1 - x0) / (npoints - 1);
//		double deltay = (y1 - y0) / (npoints - 1);
//		double x = x0;
//		double y = y0;
//		xpoints[0] = x0;
//		ypoints[0] = y0;
//		xpoints[npoints - 1] = x1;
//		ypoints[npoints - 1] = y1;
//
//		for (int i = 1; i < npoints - 1; i++) {
//			x += deltax;
//			y += deltay;
//			xpoints[i] = x;
//			ypoints[i] = y;
//		}
//		return new Polyline2D(xpoints, ypoints, npoints);
//	}

	private Point2D.Double[][] createArrayOfPointsFromSelectedPolygon(Polygon2D roiPolygon, int nbcols, int nbrows) {

		Point2D.Double[][] arrayPoints = new Point2D.Double[nbcols][nbrows];

		for (int column = 0; column < nbcols; column++) {

			double ratioX0 = column / nbcols;

			double x = roiPolygon.xpoints[0] + (roiPolygon.xpoints[3] - roiPolygon.xpoints[0]) * ratioX0;
			double y = roiPolygon.ypoints[0] + (roiPolygon.ypoints[3] - roiPolygon.ypoints[0]) * ratioX0;
			Point2D.Double ipoint0 = new Point2D.Double(x, y);

			x = roiPolygon.xpoints[1] + (roiPolygon.xpoints[2] - roiPolygon.xpoints[1]) * ratioX0;
			y = roiPolygon.ypoints[1] + (roiPolygon.ypoints[2] - roiPolygon.ypoints[1]) * ratioX0;
			Point2D.Double ipoint1 = new Point2D.Double(x, y);

//			double ratioX1 = (column+1) / nbcols;
//
//			x = roiPolygon.xpoints[1]+ (roiPolygon.xpoints[2]-roiPolygon.xpoints[1]) * ratioX1;
//			y = roiPolygon.ypoints[1]+ (roiPolygon.ypoints[2]-roiPolygon.ypoints[1]) * ratioX1;
//			Point2D.Double ipoint2 = new Point2D.Double (x, y);
//			
//			x = roiPolygon.xpoints[0]+ (roiPolygon.xpoints[3]-roiPolygon.xpoints[0]) * ratioX1;
//			y = roiPolygon.ypoints[0]+ (roiPolygon.ypoints[3]-roiPolygon.ypoints[0]) * ratioX1;
//			Point2D.Double ipoint3 = new Point2D.Double (x, y);
//			
			for (int row = 0; row < nbrows; row++) {

				double ratioY0 = row / nbrows;

				x = ipoint0.x + (ipoint1.x - ipoint0.x) * ratioY0;
				y = ipoint0.y + (ipoint1.y - ipoint0.y) * ratioY0;
				Point2D.Double point0 = new Point2D.Double(x, y);
				arrayPoints[column][row] = point0;

//				x = ipoint3.x + (ipoint2.x - ipoint3.x) * ratioY0;
//				y = ipoint3.y + (ipoint2.y - ipoint3.y) * ratioY0;
//				Point2D.Double point3 = new Point2D.Double (x, y);
//				
//				double ratioY1 = (row+1) / nbrows;
//				x = ipoint0.x + (ipoint1.x - ipoint0.x) * ratioY1;
//				y = ipoint0.y + (ipoint1.y - ipoint0.y) * ratioY1;
//				Point2D.Double point1 = new Point2D.Double (x, y);
//				
//				x = ipoint3.x + (ipoint2.x - ipoint3.x) * ratioY1;
//				y = ipoint3.y + (ipoint2.y - ipoint3.y) * ratioY1;
//				Point2D.Double point2 = new Point2D.Double (x, y);

//				List<Point2D> points = new ArrayList<>();
//				points.add(point0);
//				points.add(point1);
//				points.add(point2);
//				points.add(point3);

			}
		}

		return arrayPoints;
	}

	private void convertPoint2DArrayToCircles(Point2D.Double[][] arrayPoints, int nbcols, int nbrows, int radius) {
		for (int column = 0; column < nbcols; column++) {
			char colChar = (char) ('A' + column);
			for (int row = 0; row < nbrows; row++) {
				Point2D point = arrayPoints[column][row];
				double x = point.getX();
				double y = point.getY();
				Ellipse2D ellipse = new Ellipse2D.Double(x, y, 2 * radius, 2 * radius);
				ROI2DEllipse roiEllipse = new ROI2DEllipse(ellipse);
				roiEllipse.setName("spot" + colChar + row);
				System.out.println(roiEllipse.getName());
			}
		}
	}

}
