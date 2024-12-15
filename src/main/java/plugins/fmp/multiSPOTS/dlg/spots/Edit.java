package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;

public class Edit extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7582410775062671523L;

	private JButton editSpotsButton = new JButton("Edit spots position with time");
	private MultiSPOTS parent0 = null;
	private EditPositionWithTime editSpotsTable = null;

	void init(GridLayout capLayout, MultiSPOTS parent0) {
		setLayout(capLayout);
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
		flowLayout.setVgap(0);

		JPanel panel0 = new JPanel(flowLayout);
		panel0.add(new JLabel("* this dialog is experimental"));
		add(panel0);

		JPanel panel1 = new JPanel(flowLayout);
		panel1.add(editSpotsButton);
		add(panel1);

		defineActionListeners();
		this.setParent0(parent0);
	}

	private void defineActionListeners() {
		editSpotsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				openDialog();
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
}
