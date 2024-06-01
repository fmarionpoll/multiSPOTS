package plugins.fmp.multiSPOTS.dlg.experiment;

import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;

public class Options extends JPanel {
	private static final long serialVersionUID = 6565346204580890307L;

	JCheckBox cagesCheckBox = new JCheckBox("cages", true);
	JCheckBox measuresCheckBox = new JCheckBox("measures", true);
	public JCheckBox graphsCheckBox = new JCheckBox("graphs", true);

	public JCheckBox viewSpotsCheckBox = new JCheckBox("spots", true);
	public JCheckBox viewCagesCheckbox = new JCheckBox("cages", true);
	JCheckBox viewFlyCheckbox = new JCheckBox("flies center", false);
	JCheckBox viewFlyRectCheckbox = new JCheckBox("flies rect", false);
	private MultiSPOTS parent0 = null;

	void init(GridLayout capLayout, MultiSPOTS parent0) {
		setLayout(capLayout);
		this.parent0 = parent0;

		FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
		layout.setVgap(1);

		JPanel panel2 = new JPanel(layout);
		panel2.add(new JLabel("Load: "));
		panel2.add(cagesCheckBox);
		panel2.add(measuresCheckBox);
		panel2.add(graphsCheckBox);
		panel2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		add(panel2);

		JPanel panel1 = new JPanel(layout);
		panel1.add(new JLabel("View : "));
		panel1.add(viewSpotsCheckBox);
		panel1.add(viewCagesCheckbox);
		panel1.add(viewFlyCheckbox);
		panel1.add(viewFlyRectCheckbox);
		add(panel1);

		defineActionListeners();
	}

	private void defineActionListeners() {
		viewSpotsCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				displayROIsCategory(viewSpotsCheckBox.isSelected(), "line");
				displayROIsCategory(viewSpotsCheckBox.isSelected(), "spot");
			}
		});

		viewCagesCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				displayROIsCategory(viewCagesCheckbox.isSelected(), "cage");
			}
		});

		viewFlyCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				displayROIsCategory(viewFlyCheckbox.isSelected(), "det");
			}
		});

		viewFlyRectCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				displayROIsCategory(viewFlyRectCheckbox.isSelected(), "det");
			}
		});
	}

	public void displayROIsCategory(boolean isVisible, String pattern) {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp == null)
			return;
		exp.seqCamData.displayROIs(isVisible, pattern);
	}

}
