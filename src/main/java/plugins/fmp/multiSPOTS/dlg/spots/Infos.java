package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.Spots.Spot;

public class Infos extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4950182090521600937L;

	private JButton editSpotsButton = new JButton("Edit spots infos...");
	private SpotTable infosSpotTable = null;
	private List<Spot> spotsArrayCopy = new ArrayList<Spot>();

	private MultiSPOTS parent0 = null;

	void init(GridLayout gridLayout, MultiSPOTS parent0) {
		setLayout(gridLayout);
		this.parent0 = parent0;

		FlowLayout layoutLeft = new FlowLayout(FlowLayout.LEFT);
		layoutLeft.setVgap(0);

		JPanel panel01 = new JPanel(layoutLeft);
		panel01.add(editSpotsButton);
		add(panel01);

		declareListeners();
	}

	private void declareListeners() {
		editSpotsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					exp.spotsArray.transferDescriptionToSpots();
					if (infosSpotTable != null) {
						infosSpotTable.close();
					}
					infosSpotTable = new SpotTable();
					infosSpotTable.initialize(parent0, spotsArrayCopy);
					infosSpotTable.requestFocus();
				}
			}
		});
	}

}