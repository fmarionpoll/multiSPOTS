package plugins.fmp.multispots.dlg.experiment;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.gui.component.PopupPanel;
import icy.gui.frame.IcyFrame;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerListener;
import icy.main.Icy;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import plugins.fmp.multispots.multiSPOTS;
import plugins.fmp.multispots.experiment.Experiment;




public class MCExperiment_ extends JPanel implements ViewerListener, ChangeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6826269677524125173L;
	
	public	PopupPanel 		capPopupPanel	= null;
	public 	JTabbedPane 	tabsPane 		= new JTabbedPane();
	public 	Options 		tabOptions 		= new Options();
	public 	Infos			tabInfos		= new Infos();
	public 	Filter			tabFilter		= new Filter();
	public 	Edit			tabEdit			= new Edit();
	public 	Intervals		tabIntervals	= new Intervals();
	public 	LoadSaveExperiment panelLoadSave = new LoadSaveExperiment();
	
	private multiSPOTS 		parent0 		= null;



	public void init (JPanel mainPanel, String string, multiSPOTS parent0) 
	{
		this.parent0 = parent0;

		capPopupPanel = new PopupPanel(string);			
		capPopupPanel.collapse();
		mainPanel.add(capPopupPanel);
		GridLayout tabsLayout = new GridLayout(3, 1);
		
		JPanel filesPanel = panelLoadSave.initPanel(parent0);
		
		tabInfos.init(tabsLayout, parent0);
		tabsPane.addTab("Infos", null, tabInfos, "Define infos for this experiment/box");
		
		tabFilter.init(tabsLayout, parent0);
		tabsPane.addTab("Filter", null, tabFilter, "Filter experiments based on descriptors");

		tabEdit.init(tabsLayout, parent0);
		tabsPane.addTab("Edit", null, tabEdit, "Edit descriptors");

		tabIntervals.init(tabsLayout, parent0);
		tabsPane.addTab("Intervals", null, tabIntervals, "View/edit time-lapse intervals");

		tabOptions.init(tabsLayout, parent0);
		tabsPane.addTab("Options", null, tabOptions, "Options to display data");
		
		tabsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		JPanel capPanel = capPopupPanel.getMainPanel();
		capPanel.setLayout(new BorderLayout());
		capPanel.add(filesPanel, BorderLayout.CENTER);
		capPanel.add(tabsPane, BorderLayout.PAGE_END);	
		
		tabsPane.addChangeListener(this );
		capPopupPanel.addComponentListener(new ComponentAdapter() 
		{
			@Override
			public void componentResized(ComponentEvent e) 
			{
				parent0.mainFrame.revalidate();
				parent0.mainFrame.pack();
				parent0.mainFrame.repaint();
			}
		});		
	}
		
	public void updateDialogs(Experiment exp) 
	{
		tabIntervals.displayCamDataIntervals(exp);
		tabInfos.transferPreviousExperimentInfosToDialog(exp, exp);
		parent0.paneKymos.tabDisplay.updateResultsAvailable(exp);
	}

	public void getExperimentInfosFromDialog(Experiment exp) 
	{
		tabInfos.getExperimentInfosFromDialog(exp);
	}
	
	public void updateViewerForSequenceCam(Experiment exp) 
	{
		Sequence seq = exp.seqCamData.seq;
		if (seq == null)
			return;
		
		final ViewerListener parent = this;
		SwingUtilities.invokeLater(new Runnable() { public void run() 
		{	
			Viewer v = seq.getFirstViewer();
			if (v == null) 
				v = new Viewer(exp.seqCamData.seq, true);
			
			if (v != null) {
				placeViewerNextToDialogBox(v, parent0.mainFrame);
				v.toFront();
				v.requestFocus();
				v.addListener( parent );
				v.setTitle(exp.seqCamData.getDecoratedImageName(0));
				v.setRepeat(false);
			}
		}});
	}
	
	private void placeViewerNextToDialogBox(Viewer v, IcyFrame mainFrame) 
	{
		Rectangle rectv = v.getBoundsInternal();
		Rectangle rect0 = mainFrame.getBoundsInternal();
		if (rect0.x+ rect0.width < Icy.getMainInterface().getMainFrame().getDesktopWidth()) 
		{
			rectv.setLocation(rect0.x+ rect0.width, rect0.y);
			v.setBounds(rectv);
		}
	}

	@Override	
	public void viewerChanged(ViewerEvent event) 
	{
		if ((event.getType() == ViewerEventType.POSITION_CHANGED)) 
		{
			if (event.getDim() == DimensionId.T) 
			{
				Viewer v = event.getSource(); 
				int idViewer = v.getSequence().getId(); 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					int idCurrentExp = exp.seqCamData.seq.getId();
					if (idViewer == idCurrentExp) 
					{
						int t = v.getPositionT(); 
						v.setTitle(exp.seqCamData.getDecoratedImageName(t));
						if (parent0.paneCages.bTrapROIsEdit) 
							exp.saveDetRoisToPositions();
						exp.updateROIsAt(t);
					}
				}
			}
		}
	}

	@Override
	public void viewerClosed(Viewer viewer) 
	{
		viewer.removeListener(this);
	}

	@Override
	public void stateChanged(ChangeEvent e) 
	{
		JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
		if (tabbedPane.getSelectedIndex() == 0)
			tabInfos.initInfosCombos();
		else if (tabbedPane.getSelectedIndex() == 1)
			tabFilter.initFilterCombos();
        else if (tabbedPane.getSelectedIndex() == 2)
			tabEdit.initEditCombos();
	}

	
}
