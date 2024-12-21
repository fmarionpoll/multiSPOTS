package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import icy.roi.ROI2D;
import icy.type.geom.Polygon2D;
import icy.type.geom.Polyline2D;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.spots.Spot;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DAlongT;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DPolygon;

public class Edit extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7582410775062671523L;

	private JButton editSpotsButton = new JButton("Change spots position with time");
	private JCheckBox showFrameButton = new JCheckBox("Show frame");
	private JButton selectSpotsWithinFrameButton = new JButton("Select spots within frame");
	private JButton updateSpotsButton = new JButton("Update center of spots");
	private MultiSPOTS parent0 = null;
	private EditPositionWithTime editSpotsTable = null;

	private final String dummyname = "perimeter_enclosing";
	private ROI2DPolygon envelopeRoi = null;
	private ArrayList<Spot> enclosedSpots = null;
	private ROI2DPolyLine snakeRoi = null;

	void init(GridLayout capLayout, MultiSPOTS parent0) {
		this.setParent0(parent0);
		setLayout(capLayout);
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
		flowLayout.setVgap(0);

		JPanel panel0 = new JPanel(flowLayout);
		panel0.add(showFrameButton);
		panel0.add(selectSpotsWithinFrameButton);
		panel0.add(updateSpotsButton);
		add(panel0);

		JPanel panel1 = new JPanel(flowLayout);
		panel1.add(editSpotsButton);
		add(panel1);

		defineActionListeners();
		selectSpotsWithinFrameButton.setEnabled(false);
		updateSpotsButton.setEnabled(false);
	}

	private void defineActionListeners() {
		editSpotsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp == null)
					return;
				openDialog();
			}
		});

		showFrameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp == null)
					return;
				selectSpotsWithinFrameButton.setEnabled(showFrameButton.isSelected());
				updateSpotsButton.setEnabled(showFrameButton.isSelected());
				showFrame(showFrameButton.isSelected());
			}
		});

		selectSpotsWithinFrameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp == null)
					return;
				selectSpotsWithinFrame(exp);
			}
		});

		updateSpotsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp == null)
					return;
				updateSpots(exp);
			}
		});
	}

	public MultiSPOTS getParent0() {
		return parent0;
	}

	public void setParent0(MultiSPOTS parent0) {
		this.parent0 = parent0;
	}

	private Point getFramePosition() {
		Point spot = new Point();
		Component currComponent = (Component) editSpotsButton;
		int index = 0;
		while (currComponent != null && index < 12) {
			Point relativeLocation = currComponent.getLocation();
			spot.translate(relativeLocation.x, relativeLocation.y);
			currComponent = currComponent.getParent();
			index++;
		}
		return spot;
	}

	public void openDialog() {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp != null) {
//			exp.spotsArray.transferDescriptionToSpots();
			if (editSpotsTable == null)
				editSpotsTable = new EditPositionWithTime();
			editSpotsTable.initialize(parent0, getFramePosition());
		}
	}

	public void closeDialog() {
		editSpotsTable.close();
	}

	// --------------------------------------

	private void showFrame(boolean show) {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp == null)
			return;

		if (show) {
			int t = exp.seqCamData.seq.getFirstViewer().getPositionT();
			addFrameAroundSpots(t, exp);
		} else {
			if (envelopeRoi != null)
				exp.seqCamData.seq.removeROI(envelopeRoi);
			if (snakeRoi != null)
				exp.seqCamData.seq.removeROI(snakeRoi);
		}
	}

	private void addFrameAroundSpots(int t, Experiment exp) {
		if (envelopeRoi == null) {
			ArrayList<ROI2D> listRoisAtT = new ArrayList<ROI2D>();
			for (Spot spot : exp.spotsArray.spotsList) {
				ROI2DAlongT kymoROI2D = spot.getROIAtT(t);
				listRoisAtT.add(kymoROI2D.getRoi_in());
			}
			Polygon2D polygon = ROI2DUtilities.getPolygonEnclosingSpots(listRoisAtT);

			envelopeRoi = new ROI2DPolygon(polygon);
			envelopeRoi.setName(dummyname);
			envelopeRoi.setColor(Color.YELLOW);
		}

		exp.seqCamData.seq.removeROI(envelopeRoi);
		exp.seqCamData.seq.addROI(envelopeRoi);
		exp.seqCamData.seq.setSelectedROI(envelopeRoi);
		if (snakeRoi != null)
			exp.seqCamData.seq.removeROI(snakeRoi);
	}

	private void selectSpotsWithinFrame(Experiment exp) {
		enclosedSpots = exp.spotsArray.getSpotsEnclosed(envelopeRoi);
		if (enclosedSpots.size() > 0) {
			ArrayList<Point2D> listPoint = new ArrayList<Point2D>();
			for (Spot spot : enclosedSpots) {
				listPoint.add(new Point2D.Double(spot.spotXCoord, spot.spotYCoord));
			}
			snakeRoi = new ROI2DPolyLine(listPoint);
			exp.seqCamData.seq.addROI(snakeRoi);
			exp.seqCamData.seq.setSelectedROI(snakeRoi);
			exp.seqCamData.seq.removeROI(envelopeRoi);
		}
	}

	private void updateSpots(Experiment exp) {
		if (enclosedSpots != null && enclosedSpots.size() > 0 && snakeRoi != null) {
			Polyline2D snake = snakeRoi.getPolyline2D();
			int i = 0;
			for (Spot spot : enclosedSpots) {
				double deltax = snake.xpoints[i] - spot.spotXCoord;
				double deltay = snake.ypoints[i] - spot.spotYCoord;
				spot.spotXCoord = (int) snake.xpoints[i];
				spot.spotYCoord = (int) snake.ypoints[i];
				spot.getRoi_in().translate(deltax, deltay);
				i++;
			}
		}
	}

}
