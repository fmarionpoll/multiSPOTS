package plugins.fmp.multispots.dlg.areas;


import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import icy.gui.component.PopupPanel;
import plugins.fmp.multispots.MultiSPOTS;


public class MCAreas_ extends JPanel implements PropertyChangeListener 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public	PopupPanel areasPopupPanel		= null;
	Dlg3TabColors dlgTabThresholdColors 	= new Dlg3TabColors();
	Dlg3TabFilter dlgTabThresholdFunction 	= new Dlg3TabFilter();
	
	JCheckBox detectAreaCheckBox 			= new JCheckBox("Detect ");
	JRadioButton rbFilterbyColor 			= new JRadioButton("color array");
	JRadioButton rbFilterbyFunction			= new JRadioButton("filters");
	JCheckBox overlayCheckBox 				= new JCheckBox("overlay");
	JButton loadButton 						= new JButton("Load...");
	JButton saveButton 						= new JButton("Save...");
	JTabbedPane tabbedPane 					= new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
	
	MultiSPOTS	parent0 					= null;
	
	
	public void init (JPanel mainPanel, String string, MultiSPOTS parent0) 
	{
		this.parent0 = parent0;
		areasPopupPanel = new PopupPanel(string);
		JPanel spotsPanel = areasPopupPanel.getMainPanel();
		spotsPanel.setLayout(new BorderLayout());
		areasPopupPanel.collapse();
		mainPanel.add(areasPopupPanel);
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}


}
