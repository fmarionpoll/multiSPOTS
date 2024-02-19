package plugins.fmp.multispots.dlg.areas;


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


public class MCAreas_ extends JPanel implements PropertyChangeListener 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public	PopupPanel popupPanel	= null;
	private JTabbedPane tabsPane 	= new JTabbedPane();
	ThresholdSimple simpleThreshold = new ThresholdSimple();
	ThresholdColors colorsThreshold = new ThresholdColors();
	MultiSPOTS	parent0 			= null;
	
	public void init (JPanel mainPanel, String string, MultiSPOTS parent0) 
	{
		this.parent0 = parent0;
		popupPanel = new PopupPanel(string);
		JPanel capPanel = popupPanel.getMainPanel();
		capPanel.setLayout(new BorderLayout());
		popupPanel.collapse();
		mainPanel.add(popupPanel);

		GridLayout gridLayout = new GridLayout(4, 1);
				
		simpleThreshold.init(gridLayout, parent0);
		simpleThreshold.addPropertyChangeListener( this);
		tabsPane.addTab("Simple threshold", null, simpleThreshold, "Measure area using a simple transform and threshold");
		
		colorsThreshold.init(gridLayout, parent0);	
		colorsThreshold.addPropertyChangeListener( this);
		tabsPane.addTab("Colors threshold", null, colorsThreshold, "Measure area using colors defined by user");
		
		capPanel.add(tabsPane);
		tabsPane.setSelectedIndex(0);
		
		popupPanel.addComponentListener(new ComponentAdapter() 
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
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("MEASURES_SAVE")) 
		{
			tabsPane.setSelectedIndex(0);
		}
		
	}
	
	


}
