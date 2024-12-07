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
import icy.sequence.Sequence;
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
	private JSpinner pixelRadiusSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));

	private JSpinner nRowsJSpinner = new JSpinner(new SpinnerNumberModel(8, 2, 100, 1));
	private JSpinner nColumnsJSpinner = new JSpinner(new SpinnerNumberModel(12, 2, 100, 1));

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
		panel1.add(new JLabel("cols"));
		panel1.add(nColumnsJSpinner);
		nColumnsJSpinner.setPreferredSize(new Dimension(40, 20));
		panel1.add(new JLabel("rows"));
		panel1.add(nRowsJSpinner);
		nRowsJSpinner.setPreferredSize(new Dimension(40, 20));
		panel1.add(new JLabel("notch"));
		panel1.add(notchJComboBox);
		panel1.add(new JLabel("view"));
		panel1.add(viewFromJComboBox);

		JPanel panel2 = new JPanel(flowLayout);
		panel2.add(new JLabel("Cage:"));

		panel2.add(new JLabel("cols"));
		panel2.add(cageNColumnsJSpinner);
		cageNColumnsJSpinner.setPreferredSize(new Dimension(40, 20));
		panel2.add(new JLabel("rows"));
		panel2.add(cageNrowsJSpinner);
		cageNrowsJSpinner.setPreferredSize(new Dimension(40, 20));
		panel2.add(new JLabel("with"));

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
				create2DPolygon();
			}
		});

		createCirclesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					roisGenerateFromPolygon();
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

		Point2D.Double[][] arrayPoints = PolygonUtilities.createArrayOfPointsFromPolygon(roiPolygon, n_columns, n_rows);
		int radius = (int) pixelRadiusSpinner.getValue();
		convertPoint2DArrayToCircles(seqCamData.seq, arrayPoints, n_columns, n_rows, radius);

	}

	private void convertPoint2DArrayToCircles(Sequence seq, Point2D.Double[][] arrayPoints, int nbcols, int nbrows,
			int radius) {
		for (int column = 0; column < nbcols; column++) {
			char colChar = (char) ('A' + column);
			for (int row = 0; row < nbrows; row++) {
				Point2D point = arrayPoints[column][row];
				double x = point.getX() - radius;
				double y = point.getY() - radius;
				Ellipse2D ellipse = new Ellipse2D.Double(x, y, 2 * radius, 2 * radius);
				ROI2DEllipse roiEllipse = new ROI2DEllipse(ellipse);
				roiEllipse.setName("spot" + colChar + row);
				seq.addROI(roiEllipse);
			}
		}
	}

}
