package plugins.fmp.multispots.dlg.areas;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.gui.component.PopupPanel;
import icy.gui.util.FontUtil;
import plugins.fmp.multispots.tools.EnumAreaDetection;
import plugins.fmp.multispots.MultiSPOTS;


public class MCAreas_ extends JPanel implements ChangeListener 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public	PopupPanel areasPopupPanel		= null;
	TabColors dlgTabThresholdColors 	= new TabColors();
	TabFilter dlgTabThresholdFunction 	= new TabFilter();
	
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
		
		areasPopupPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				parent0.mainFrame.revalidate();
				parent0.mainFrame.pack();
				parent0.mainFrame.repaint();
			}});
		mainPanel.add(areasPopupPanel);

		FlowLayout layoutLeft = new FlowLayout(FlowLayout.LEFT); 
		JPanel panel0 = new JPanel(layoutLeft);
		((FlowLayout)panel0.getLayout()).setVgap(0);
		panel0.add(detectAreaCheckBox);
		panel0.add(rbFilterbyColor);
		panel0.add(rbFilterbyFunction);
		ButtonGroup bgchoice = new ButtonGroup();
		bgchoice.add(rbFilterbyColor);
		bgchoice.add(rbFilterbyFunction);
		panel0.add(overlayCheckBox);
		spotsPanel.add(panel0, BorderLayout.PAGE_START);
		
		GridLayout capLayout = new GridLayout(3, 2);
		dlgTabThresholdColors.init(tabbedPane, capLayout, parent0);
		dlgTabThresholdFunction.init(tabbedPane, capLayout, parent0);
		spotsPanel.add(tabbedPane, BorderLayout.CENTER);
		
		JLabel loadsaveText1 = new JLabel ("-> File (xml) ");
		loadsaveText1.setHorizontalAlignment(SwingConstants.RIGHT); 
		loadsaveText1.setFont(FontUtil.setStyle(loadsaveText1.getFont(), Font.ITALIC));
		FlowLayout layoutRight = new FlowLayout(FlowLayout.RIGHT); 
		JPanel panel2 = new JPanel(layoutRight);
		panel2.add(loadsaveText1);
		panel2.add(loadButton);
		panel2.add(saveButton);
		spotsPanel.add(panel2, BorderLayout.PAGE_END);
		
		detectAreaCheckBox.setSelected(true);
		tabbedPane.setSelectedIndex(0);
		rbFilterbyColor.setSelected(true);
		
		declareActionListeners();
	}
	
	private void declareActionListeners() {
		loadButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
//				loadParameters(); 
			}});
		
		saveButton.addActionListener(new ActionListener () {
			@Override public void actionPerformed( final ActionEvent e ) { 
//				saveParameters(); 
			}});
		
		rbFilterbyColor.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) {
			if (rbFilterbyColor.isSelected())
				selectTab(0);
			}});
		
		rbFilterbyFunction.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) {
			if (rbFilterbyFunction.isSelected())
				selectTab(1);
			}});
		
		overlayCheckBox.addActionListener(new ActionListener () {
			@Override public void actionPerformed( final ActionEvent e) {
//				parent0.setOverlay(overlayCheckBox.isSelected());
			}});
	}
	
	private void selectTab(int index) {
		
		tabbedPane.setSelectedIndex(index);
	}

	
	@Override
	public void stateChanged (ChangeEvent e) {
		
		if (e.getSource() == tabbedPane) {
			int selectedTab = tabbedPane.getSelectedIndex();
//			updateThresholdOverlayParameters(selectedTab);
			if (selectedTab == 0) {
				rbFilterbyColor.setSelected(true);
//				parent0.detectionParameters.areaDetectionMode = EnumAreaDetection.COLORARRAY;
			}
			else if (selectedTab == 1) {
				rbFilterbyFunction.setSelected(true);
//				parent0.detectionParameters.areaDetectionMode = EnumAreaDetection.SINGLE;
			}
		}
	}


}
