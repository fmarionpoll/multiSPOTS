package plugins.fmp.multiSPOTS.dlg.kymos;

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
import plugins.fmp.multiSPOTS.MultiSPOTS;


public class DlgKymos_  extends JPanel implements PropertyChangeListener, ChangeListener 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1122367183829360097L;
	public	PopupPanel capPopupPanel	= null;
	JTabbedPane 		tabsPane 		= new JTabbedPane();
	public Create 		tabCreate 		= new Create();
	
	private MultiSPOTS parent0 = null;

	public void init (JPanel mainPanel, String string, MultiSPOTS parent0) 
	{
		this.parent0 = parent0;
		capPopupPanel = new PopupPanel(string);
		JPanel capPanel = capPopupPanel.getMainPanel();
		capPanel.setLayout(new BorderLayout());
		capPopupPanel.collapse();
		mainPanel.add(capPopupPanel);
		GridLayout capLayout = new GridLayout(3, 1);
		
		tabCreate.init(capLayout, this.parent0);
		tabCreate.addPropertyChangeListener(this);
		tabsPane.addTab("Build kymos (experimental)", null, tabCreate, "Build pseudo-kymographs from ROIs");
	
	
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
//				tabbedCapillariesAndKymosSelected();
			}});
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) 
	{
//		if (event.getPropertyName().equals("KYMOS_OPEN")) 
//		{
//			tabsPane.setSelectedIndex(2);
//		}
//		else if (event.getPropertyName().equals("KYMOS_SAVE")) 
//		{
//			tabsPane.setSelectedIndex(1);
//		}
	}
	
//	public void updateDialogs(Experiment exp) 
//	{
//		tabIntervals.displayDlgKymoIntervals (exp);
//	}

//	void tabbedCapillariesAndKymosSelected() 
//	{
//		Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
//		if (exp == null || exp.seqCamData == null)
//			return;
//		int iselected = tabsPane.getSelectedIndex();
//		if (iselected == 0) {
//			Viewer v = exp.seqCamData.seq.getFirstViewer();
//			if (v != null)
//				v.toFront();
//			parent0.paneExperiment.capPopupPanel.expand();
//			parent0.paneExperiment.tabsPane.setSelectedIndex(0);
//		} 
//		else if (iselected == 1) 
//		{
//			parent0.paneKymos.tabDisplay.displayUpdateOnSwingThread();
//		}
//	}
	
	@Override
	public void stateChanged(ChangeEvent event) 
	{
//		if (event.getSource() == tabsPane)
//			tabbedCapillariesAndKymosSelected();
	}

}