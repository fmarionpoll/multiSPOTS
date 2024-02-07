package plugins.fmp.multispots.dlg.kymos;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.gui.component.PopupPanel;
import icy.gui.viewer.Viewer;
import plugins.fmp.multispots.multiSPOTS;
import plugins.fmp.multispots.experiment.Experiment;


public class MCKymos_ extends JPanel implements PropertyChangeListener, ChangeListener 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1122367183829360097L;
	public	PopupPanel capPopupPanel	= null;
	JTabbedPane 		tabsPane 		= new JTabbedPane();
	public Create 		tabCreate 		= new Create();
	public Display		tabDisplay 		= new Display();
	public Intervals	tabIntervals = new Intervals();
	public LoadSaveKymos 	tabFile 		= new LoadSaveKymos();
	private multiSPOTS parent0 = null;

	public void init (JPanel mainPanel, String string, multiSPOTS parent0) 
	{
		this.parent0 = parent0;
		capPopupPanel = new PopupPanel(string);
		JPanel capPanel = capPopupPanel.getMainPanel();
		capPanel.setLayout(new BorderLayout());
		capPopupPanel.collapse();
		mainPanel.add(capPopupPanel);
		GridLayout capLayout = new GridLayout(3, 1);
		
		tabCreate.init(capLayout, parent0);
		tabCreate.addPropertyChangeListener(this);
		tabsPane.addTab("Build kymos", null, tabCreate, "Build kymographs from ROI lines placed over capillaries");
	
		tabDisplay.init(capLayout, parent0);
		tabDisplay.addPropertyChangeListener(this);
		tabsPane.addTab("Display", null, tabDisplay, "Display options of data & kymographs");

		tabIntervals.init(capLayout, parent0);
		tabIntervals.addPropertyChangeListener(this);
		tabsPane.addTab("Edit", null, tabIntervals, "Time resolution of kymograph");

		tabFile.init(capLayout, parent0);
		tabFile.addPropertyChangeListener(this);
		tabsPane.addTab("Load/Save", null, tabFile, "Load/Save xml file with capillaries descriptors");

		tabsPane.addChangeListener(this);
		tabsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		capPanel.add(tabsPane);
		
		capPopupPanel.addComponentListener(new ComponentAdapter() 
		{
			@Override
			public void componentResized(ComponentEvent e) 
			{
				parent0.mainFrame.revalidate();
				parent0.mainFrame.pack();
				parent0.mainFrame.repaint();
				tabbedCapillariesAndKymosSelected();
			}});
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) 
	{
		if (event.getPropertyName().equals("KYMOS_OPEN")) 
		{
			tabsPane.setSelectedIndex(2);
		}
		else if (event.getPropertyName().equals("KYMOS_SAVE")) 
		{
			tabsPane.setSelectedIndex(1);
		}
	}
	
	public void updateDialogs(Experiment exp) 
	{
		tabIntervals.displayDlgKymoIntervals (exp);
	}
	
	void tabbedCapillariesAndKymosSelected() 
	{
		Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
		if (exp == null || exp.seqCamData == null)
			return;
		int iselected = tabsPane.getSelectedIndex();
		if (iselected == 0) {
			Viewer v = exp.seqCamData.seq.getFirstViewer();
			if (v != null)
				v.toFront();
			parent0.paneExperiment.capPopupPanel.expand();
			parent0.paneExperiment.tabsPane.setSelectedIndex(0);
		} 
		else if (iselected == 1) 
		{
			parent0.paneKymos.tabDisplay.displayUpdateOnSwingThread();
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent event) 
	{
		if (event.getSource() == tabsPane)
			tabbedCapillariesAndKymosSelected();
	}

}
