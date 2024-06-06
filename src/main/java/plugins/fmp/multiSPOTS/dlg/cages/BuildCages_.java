package plugins.fmp.multiSPOTS.dlg.cages;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import plugins.fmp.multiSPOTS.MultiSPOTS;

public class BuildCages_ extends JPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	BuildCagesAsArray tabBuildCagesAsArray = new BuildCagesAsArray();
	BuildCagesFromContours tabBuildCagesAsContours = new BuildCagesFromContours();
	JTabbedPane tabsPane = new JTabbedPane();
	int iTAB_CAGES1 = 0;
	int iTAB_CAGES2 = 1;
	MultiSPOTS parent0 = null;

	public void init(GridLayout capLayout, MultiSPOTS parent0) {
		this.parent0 = parent0;
		createTabs(capLayout);
		add(tabsPane);
		tabsPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int selectedIndex = tabsPane.getSelectedIndex();
				displayOverlay(selectedIndex == iTAB_CAGES2);
			}
		});
		tabsPane.setSelectedIndex(0);
	}

	void createTabs(GridLayout capLayout) {
		tabsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		int iTab = 0;
		iTAB_CAGES1 = iTab;
		tabBuildCagesAsArray.init(capLayout, parent0);
		tabBuildCagesAsArray.addPropertyChangeListener(this);
		tabsPane.addTab("Define array cols/rows", null, tabBuildCagesAsArray, "Build cages as an array of cells");

		iTab++;
		iTAB_CAGES2 = iTab;
		tabBuildCagesAsContours.init(capLayout, parent0);
		tabBuildCagesAsContours.addPropertyChangeListener(this);
		tabsPane.addTab("Detect contours of cages", null, tabBuildCagesAsContours, "Detect contours to build cages");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
//		JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
		int index = tabsPane.getSelectedIndex();
		displayOverlay(index == 1);
	}

	private void displayOverlay(boolean activateOverlay) {
		tabBuildCagesAsContours.overlayCheckBox.setSelected(activateOverlay);
	}
}
