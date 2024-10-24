package plugins.fmp.multiSPOTS.dlg.experiment;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.tools.JComponents.JComboBoxMs;

public class Intervals extends JPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5739112045358747277L;
	Long val = 0L; // set your own value, I used to check if it works
	Long min = 0L;
	Long max = 10000L;
	Long step = 1L;
	Long maxLast = 99999999L;
	JSpinner indexFirstImageJSpinner = new JSpinner(new SpinnerNumberModel(val, min, max, step));
	JCheckBox clipNumberImagesCheckbox = new JCheckBox("clip number of images", false);
	JSpinner numberImagesJSpinner = new JSpinner(new SpinnerNumberModel(maxLast, step, maxLast, step));
	JSpinner binSizeJSpinner = new JSpinner(new SpinnerNumberModel(1., 0., 1000., 1.));
	JComboBoxMs binUnit = new JComboBoxMs();
	JButton applyButton = new JButton("Apply changes");
	JButton refreshButton = new JButton("Refresh");
	private MultiSPOTS parent0 = null;

	void init(GridLayout capLayout, MultiSPOTS parent0) {
		setLayout(capLayout);
		this.parent0 = parent0;

		int bWidth = 50;
		int bHeight = 21;
		binSizeJSpinner.setPreferredSize(new Dimension(bWidth, bHeight));

		FlowLayout layout1 = new FlowLayout(FlowLayout.LEFT);
		layout1.setVgap(1);

		JPanel panel0 = new JPanel(layout1);
		panel0.add(new JLabel("Frame ", SwingConstants.RIGHT));
		panel0.add(indexFirstImageJSpinner);
		// panel0.add(new JLabel(" N frames"));
		panel0.add(clipNumberImagesCheckbox);
		panel0.add(numberImagesJSpinner);
		add(panel0);

		JPanel panel1 = new JPanel(layout1);
		panel1.add(new JLabel("Time between frames ", SwingConstants.RIGHT));
		panel1.add(binSizeJSpinner);
		panel1.add(binUnit);
		add(panel1);

		panel1.add(refreshButton);
		panel1.add(applyButton);

		numberImagesJSpinner.setEnabled(false);
		defineActionListeners();
	}

	private void defineActionListeners() {
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
					setExptParms(exp);
			}
		});

		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
					refreshBinSize(exp);
			}
		});

		indexFirstImageJSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					exp.seqCamData.indexFirstImage = (long) indexFirstImageJSpinner.getValue();
					exp.seqCamData.loadImageList();
					long bin_ms = exp.seqCamData.binImage_ms;
					exp.seqCamData.binFirst_ms = exp.seqCamData.indexFirstImage * bin_ms;
					exp.saveXML_MCExperiment();
					// exp.loadCamDataImages();
				}
			}
		});

		numberImagesJSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					exp.seqCamData.numberOfImagesClipped = (long) numberImagesJSpinner.getValue();
					exp.seqCamData.loadImageList();
					long bin_ms = exp.seqCamData.binImage_ms;
					exp.seqCamData.binLast_ms = (((long) numberImagesJSpinner.getValue())
							- exp.seqCamData.indexFirstImage) * bin_ms;
				}
			}
		});

		binSizeJSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					long bin_ms = (long) (((double) binSizeJSpinner.getValue()) * binUnit.getMsUnitValue());
					exp.seqCamData.binImage_ms = bin_ms;
					exp.seqCamData.binFirst_ms = exp.seqCamData.indexFirstImage * bin_ms;
					exp.seqCamData.binLast_ms = (exp.seqCamData.numberOfImagesClipped - 1) * bin_ms;
				}
			}
		});

		clipNumberImagesCheckbox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					numberImagesJSpinner.setEnabled(clipNumberImagesCheckbox.isSelected());
				}
			}
		});
	}

	private void setExptParms(Experiment exp) {
		exp.seqCamData.binImage_ms = (long) (((double) binSizeJSpinner.getValue()) * binUnit.getMsUnitValue());
		long bin_ms = exp.seqCamData.binImage_ms;
		exp.seqCamData.indexFirstImage = (long) indexFirstImageJSpinner.getValue();
		exp.seqCamData.binFirst_ms = exp.seqCamData.indexFirstImage * bin_ms;
		exp.seqCamData.binLast_ms = (exp.seqCamData.numberOfImagesClipped - 1) * bin_ms;
	}

	public void getExptParms(Experiment exp) {
		refreshBinSize(exp);
		long bin_ms = exp.seqCamData.binImage_ms;
		long dFirst = exp.seqCamData.indexFirstImage;

		indexFirstImageJSpinner.setValue(dFirst);
		if (exp.seqCamData.binLast_ms <= 0)
			exp.seqCamData.binLast_ms = (long) (exp.getSeqCamSizeT() * bin_ms);

		numberImagesJSpinner.setValue(exp.seqCamData.numberOfImagesClipped);
		exp.getFileIntervalsFromSeqCamData();
	}

	private void refreshBinSize(Experiment exp) {
		exp.loadFileIntervalsFromSeqCamData();
		binUnit.setSelectedIndex(1);
		binSizeJSpinner.setValue(exp.seqCamData.binImage_ms / (double) binUnit.getMsUnitValue());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub

	}
}
