package plugins.fmp.multispots.dlg.areas;


import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

import icy.gui.component.PopupPanel;
import plugins.fmp.multispots.multiSPOTS;


public class MCAreas_ extends JPanel implements PropertyChangeListener 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public	PopupPanel areasPopupPanel		= null;
	multiSPOTS	parent0 		= null;
	
	
	public void init (JPanel mainPanel, String string, multiSPOTS parent0) 
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
