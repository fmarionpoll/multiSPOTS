package plugins.fmp.multispots.dlg.levels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import icy.gui.component.PopupPanel;

import plugins.fmp.multispots.MultiSPOTS;
import plugins.fmp.multispots.tools.ImageTransform.ImageTransformEnums;



public class MCLevels_ extends JPanel implements PropertyChangeListener 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7339633966002954720L;
	public	PopupPanel spotsPopupPanel		= null;
	private JTabbedPane tabsPane 			= new JTabbedPane();
	public 	LoadSaveLevels tabFileLevels	= new LoadSaveLevels();
			Levels 	tabDetectLevels2 		= new Levels();
			
			//DetectLevelsKMeans tabDetectLevelsK = new DetectLevelsKMeans();
			LevelsToGulps tabDetectGulps 	= new LevelsToGulps();
			EditLevels tabEdit				= new EditLevels();
			Adjust tabAdjust				= new Adjust();
	public 	Graphs 	tabGraphs 				= new Graphs();
	MultiSPOTS	parent0 					= null;

	
	public void init (JPanel mainPanel, String string, MultiSPOTS parent0) 
	{
		this.parent0 = parent0;
		spotsPopupPanel = new PopupPanel(string);
		JPanel spotsPanel = spotsPopupPanel.getMainPanel();
		spotsPanel.setLayout(new BorderLayout());
		spotsPopupPanel.collapse();
		mainPanel.add(spotsPopupPanel);

		GridLayout spotsPanelLayout = new GridLayout(4, 1);
				
		tabDetectLevels2.init(spotsPanelLayout, parent0);
		tabDetectLevels2.addPropertyChangeListener(this);
		tabsPane.addTab("Levels", null, tabDetectLevels2, "Find limits of the columns of liquid");
		
		tabDetectGulps.init(spotsPanelLayout, parent0);	
		tabsPane.addTab("Gulps", null, tabDetectGulps, "Detect gulps");
		tabDetectGulps.addPropertyChangeListener(this);
		
		tabEdit.init(spotsPanelLayout, parent0);
		tabEdit.addPropertyChangeListener(this);
		tabsPane.addTab("Edit", null, tabEdit, "Edit Rois / measures");

		tabGraphs.init(spotsPanelLayout, parent0);
		tabGraphs.addPropertyChangeListener(this);
		tabsPane.addTab("Graphs", null, tabGraphs, "Display results as a graph");
		
		tabFileLevels.init(spotsPanelLayout, parent0);
		tabFileLevels.addPropertyChangeListener(this);
		tabsPane.addTab("Load/Save", null, tabFileLevels, "Load/Save kymographs");
						
		spotsPanel.add(tabsPane);
		tabDetectLevels2.transformPass1ComboBox.setSelectedItem(ImageTransformEnums.RGB_DIFFS);
		tabsPane.setSelectedIndex(0);
		
		spotsPopupPanel.addComponentListener(new ComponentAdapter() 
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
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) 
	{
		if (arg0.getPropertyName().equals("MEASURES_SAVE")) 
		{
			tabsPane.setSelectedIndex(0);
		}
	}
	
}
