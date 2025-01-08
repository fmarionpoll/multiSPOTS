package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import icy.gui.frame.progress.AnnounceFrame;
import icy.roi.ROI2D;
import icy.type.geom.Polygon2D;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.ExperimentUtils;
import plugins.fmp.multiSPOTS.experiment.SequenceCamData;
import plugins.fmp.multiSPOTS.experiment.cages.Cage;
import plugins.fmp.multiSPOTS.tools.polyline.PolygonUtilities;
import plugins.kernel.roi.roi2d.ROI2DPolygon;

public class CreateCages extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5257698990389571518L;
	private JButton displayFrameDButton = new JButton("(1) Display frame");
	private JButton createCagesButton = new JButton("Create/add (from Polygon 2D)");

	private JSpinner nCagesPerPlateAlongXJSpinner = new JSpinner(new SpinnerNumberModel(6, 0, 10000, 1));
	private JSpinner nCagesPerPlateAlongYJSpinner = new JSpinner(new SpinnerNumberModel(8, 0, 10000, 1));

	private JSpinner width_cageTextField = new JSpinner(new SpinnerNumberModel(40, 0, 10000, 1));
	private JSpinner width_intervalTextField = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));

	private int width_cage = 10;
	private int width_interval = 1;

	private Polygon2D polygon2D = null;

	private MultiSPOTS parent0;

	void init(GridLayout capLayout, MultiSPOTS parent0) {
		setLayout(capLayout);
		this.parent0 = parent0;

		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
		flowLayout.setVgap(0);

		JPanel panel0 = new JPanel(flowLayout);
		panel0.add(displayFrameDButton);
		panel0.add(createCagesButton);
		add(panel0);

		JPanel panel1 = new JPanel(flowLayout);
		panel1.add(new JLabel("N columns "));
		panel1.add(nCagesPerPlateAlongXJSpinner);
		panel1.add(new JLabel("N rows "));
		panel1.add(nCagesPerPlateAlongYJSpinner);
		add(panel1);

		JPanel panel2 = new JPanel(flowLayout);
		panel2.add(new JLabel("cage width "));
		panel2.add(width_cageTextField);
		panel2.add(new JLabel("space between cages"));
		panel2.add(width_intervalTextField);
		add(panel2);

		defineActionListeners();
	}

	private void defineActionListeners() {
		displayFrameDButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				create2DPolygon();
			}
		});

		createCagesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
//					ROI2DUtilities.removeRoisContainingString(-1, "cage", exp.seqCamData.seq);
//					exp.cagesArray.removeCages();
					createCagessFromSelectedPolygon(exp);
					ExperimentUtils.transferCagesToCamDataSequence(exp); // TODO

//					int nbFliesPerCage = (int) nFliesPerCageJSpinner.getValue();
//					exp.spotsArray.initSpotsWithNFlies(nbFliesPerCage);

//					exp.cagesArray.cagesFromROIs(exp.seqCamData);
//					if (exp.spotsArray.spotsList.size() > 0)
//						exp.cagesArray.transferNFliesFromSpotsToCages(exp.spotsArray);
				}
			}
		});

	}

	void updateNColumnsFieldFromSequence() {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp != null) {
			int nrois = exp.cagesArray.cagesList.size();
			if (nrois > 0) {
				nCagesPerPlateAlongXJSpinner.setValue(exp.cagesArray.nCagesPerPlateAlongX);
				nCagesPerPlateAlongYJSpinner.setValue(exp.cagesArray.nCagesPerPlateAlongY);
			}
		}
	}

	private void create2DPolygon() {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp == null)
			return;
		SequenceCamData seqCamData = exp.seqCamData;
		final String dummyname = "perimeter_enclosing_cages";
		if (isRoiPresent(seqCamData, dummyname))
			return;

		ROI2DPolygon roi = new ROI2DPolygon(getSpotsPolygon(exp));
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

	private Polygon2D getSpotsPolygon(Experiment exp) {
		if (polygon2D == null) {
			if (exp.spotsArray.spotsList.size() > 0) {
				polygon2D = exp.spotsArray.getPolygon2DEnclosingAllSpots();
			} else {
				Rectangle rect = exp.seqCamData.seq.getBounds2D();
				List<Point2D> points = new ArrayList<Point2D>();
				points.add(new Point2D.Double(rect.x + rect.width / 5, rect.y + rect.height / 5));
				points.add(new Point2D.Double(rect.x + rect.width * 4 / 5, rect.y + rect.height / 5));
				points.add(new Point2D.Double(rect.x + rect.width * 4 / 5, rect.y + rect.height * 2 / 3));
				points.add(new Point2D.Double(rect.x + rect.width / 5, rect.y + rect.height * 2 / 3));
				polygon2D = new Polygon2D(points);
			}
		}
		return polygon2D;
	}

	private void createCagessFromSelectedPolygon(Experiment exp) {
		SequenceCamData seqCamData = exp.seqCamData;
		ROI2D roi = seqCamData.seq.getSelectedROI2D();
		if (!(roi instanceof ROI2DPolygon)) {
			new AnnounceFrame("The frame must be a ROI2D Polygon");
			return;
		}
		polygon2D = PolygonUtilities.orderVerticesOf4CornersPolygon(((ROI2DPolygon) roi).getPolygon());
		seqCamData.seq.removeROI(roi);

		int n_columns = 10;
		int n_rows = 1;
		try {
			n_columns = (int) nCagesPerPlateAlongXJSpinner.getValue();
			n_rows = (int) nCagesPerPlateAlongYJSpinner.getValue();
			width_cage = (int) width_cageTextField.getValue();
			width_interval = (int) width_intervalTextField.getValue();
		} catch (Exception e) {
			new AnnounceFrame("Can't interpret one of the ROI parameters value");
		}

		createCagesArray(exp, polygon2D, n_columns, n_rows, width_cage, width_interval);

	}

	private void createCagesArray(Experiment exp, Polygon2D roiPolygonMin, int ncolumns, int nrows, int width_cage,
			int width_interval) {
		// generate cage frames

		int iRoot = exp.cagesArray.removeAllRoiCagesFromSequence(exp.seqCamData);
		String cageRoot = "cage";

		Polygon2D roiPolygon = PolygonUtilities.inflate(roiPolygonMin, ncolumns, nrows, width_cage, width_interval);

		double deltax_top = (roiPolygon.xpoints[3] - roiPolygon.xpoints[0]) / ncolumns;
		double deltax_bottom = (roiPolygon.xpoints[2] - roiPolygon.xpoints[1]) / ncolumns;
		double deltay_top = (roiPolygon.ypoints[3] - roiPolygon.ypoints[0]) / ncolumns;
		double deltay_bottom = (roiPolygon.ypoints[2] - roiPolygon.ypoints[1]) / ncolumns;

		for (int column = 0; column < ncolumns; column++) {
			double[][] xyi = initColumn_i(roiPolygon, deltax_top, deltax_bottom, deltay_top, deltay_bottom, column);

			for (int row = 0; row < nrows; row++) {

				double[][] xyij = initRow_j(roiPolygon, xyi, nrows, row);

				ROI2DPolygon roiP = createRoiPolygon(xyij);
				roiP.setName(cageRoot + String.format("%03d", iRoot));
				roiP.setColor(Color.YELLOW);

				Cage cage = new Cage(roiP);
				cage.arrayIndex = iRoot;
				cage.arrayColumn = column;
				cage.arrayRow = row;

				iRoot++;
				exp.seqCamData.seq.addROI(roiP);
			}
		}
	}

	private ROI2DPolygon createRoiPolygon(double[][] xyij) {
		// shrink by
		int k = 0;
		double xspacer_top = (xyij[3][k] - xyij[0][k]) * width_interval / (width_cage + 2 * width_interval);
		double xspacer_bottom = (xyij[2][k] - xyij[1][k]) * width_interval / (width_cage + 2 * width_interval);
		k = 1;
		double yspacer_left = (xyij[1][k] - xyij[0][k]) * width_interval / (width_cage + 2 * width_interval);
		double yspacer_right = (xyij[2][k] - xyij[3][k]) * width_interval / (width_cage + 2 * width_interval);

		// define intersection
		List<Point2D> points = new ArrayList<>();

		Point2D point0 = PolygonUtilities.lineIntersect(xyij[0][0] + xspacer_top, xyij[0][1],
				xyij[1][0] + xspacer_bottom, xyij[1][1], xyij[0][0], xyij[0][1] + yspacer_left, xyij[3][0],
				xyij[3][1] + yspacer_right);
		points.add(point0);

		Point2D point1 = PolygonUtilities.lineIntersect(xyij[1][0], xyij[1][1] - yspacer_left, xyij[2][0],
				xyij[2][1] - yspacer_right, xyij[0][0] + xspacer_top, xyij[0][1], xyij[1][0] + xspacer_bottom,
				xyij[1][1]);
		points.add(point1);

		Point2D point2 = PolygonUtilities.lineIntersect(xyij[1][0], xyij[1][1] - yspacer_left, xyij[2][0],
				xyij[2][1] - yspacer_right, xyij[3][0] - xspacer_top, xyij[3][1], xyij[2][0] - xspacer_bottom,
				xyij[2][1]);
		points.add(point2);

		Point2D point3 = PolygonUtilities.lineIntersect(xyij[0][0], xyij[0][1] + yspacer_left, xyij[3][0],
				xyij[3][1] + yspacer_right, xyij[3][0] - xspacer_top, xyij[3][1], xyij[2][0] - xspacer_bottom,
				xyij[2][1]);
		points.add(point3);

		ROI2DPolygon roiP = new ROI2DPolygon(points);
		return roiP;
	}

	private double[][] initColumn_i(Polygon2D roiPolygon, double deltax_top, double deltax_bottom, double deltay_top,
			double deltay_bottom, int i) {

		double[][] xyi = new double[4][2];
		int j = 0;
		xyi[0][j] = roiPolygon.xpoints[0] + deltax_top * i;
		xyi[1][j] = roiPolygon.xpoints[1] + deltax_bottom * i;
		xyi[3][j] = xyi[0][j] + deltax_top;
		xyi[2][j] = xyi[1][j] + deltax_bottom;

		j = 1;
		xyi[0][j] = roiPolygon.ypoints[0] + deltay_top * i;
		xyi[1][j] = roiPolygon.ypoints[1] + deltay_bottom * i;
		xyi[3][j] = xyi[0][j] + deltay_top;
		xyi[2][j] = xyi[1][j] + deltay_bottom;

		return xyi;
	}

	private double[][] initRow_j(Polygon2D roiPolygon, double[][] xyi, int nrows, int j) {

		double[][] xyij = new double[4][2];

		int k = 0;
		double deltax_left = (xyi[1][k] - xyi[0][k]) / nrows;
		double deltax_right = (xyi[2][k] - xyi[3][k]) / nrows;
		k = 1;
		double deltay_left = (xyi[1][k] - xyi[0][k]) / nrows;
		double deltay_right = (xyi[2][k] - xyi[3][k]) / nrows;

		k = 0;
		xyij[0][k] = xyi[0][k] + deltax_left * j;
		xyij[1][k] = xyij[0][k] + deltax_left;
		xyij[3][k] = xyi[3][k] + deltax_right * j;
		xyij[2][k] = xyij[3][k] + deltax_right;

		k = 1;
		xyij[0][k] = xyi[0][k] + deltay_left * j;
		xyij[1][k] = xyij[0][k] + deltay_left;
		xyij[3][k] = xyi[3][k] + deltay_right * j;
		xyij[2][k] = xyij[3][k] + deltay_right;

		return xyij;
	}

}
