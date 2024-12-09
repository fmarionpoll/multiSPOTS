package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import icy.gui.util.FontUtil;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;

public class LoadSaveSpots extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4019075448319252245L;

	private JButton loadButton = new JButton("Load...");
	private JButton saveButton = new JButton("Save...");
	private MultiSPOTS parent0 = null;

	void init(GridLayout capLayout, MultiSPOTS parent0) {
		setLayout(capLayout);

		JLabel loadsaveText = new JLabel("-> Spots, polylines (xml) ", SwingConstants.RIGHT);
		loadsaveText.setFont(FontUtil.setStyle(loadsaveText.getFont(), Font.ITALIC));
		FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
		flowLayout.setVgap(0);
		JPanel panel1 = new JPanel(flowLayout);
		panel1.add(loadsaveText);
		panel1.add(loadButton);
		panel1.add(saveButton);
		panel1.validate();
		add(panel1);

		this.parent0 = parent0;
		defineActionListeners();
	}

	private void defineActionListeners() {
		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					loadSpotsArray_File(exp);
					firePropertyChange("SPOTS_ROIS_OPEN", false, true);
				}
			}
		});

		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					saveSpotsArray_file(exp);
					firePropertyChange("SPOTS_ROIS_SAVE", false, true);
				}
			}
		});
	}

	public boolean loadSpotsArray_File(Experiment exp) {
		boolean flag = exp.loadMCSpots_Only();
		exp.load_SpotsMeasures();
		exp.spotsArray.transferSpotRoiToSequence(exp.seqCamData.seq);
		return flag;
	}

	public boolean saveSpotsArray_file(Experiment exp) {
		parent0.dlgExperiment.getExperimentInfosFromDialog(exp);
		exp.spotsArray.transferDescriptionToSpots();
		boolean flag = exp.saveXML_MCExperiment();
		exp.spotsArray.updateSpotsFromSequence(exp.seqCamData.seq);
		flag &= exp.save_MCSpots_Only();
		flag &= exp.save_SpotsMeasures();
		return flag;
	}

}
