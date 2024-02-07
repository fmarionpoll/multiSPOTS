package plugins.fmp.multispots.dlg.levels;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.util.StringUtil;
import plugins.fmp.multispots.multiSPOTS;
import plugins.fmp.multispots.experiment.Capillary;
import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.series.BuildSeriesOptions;
import plugins.fmp.multispots.series.DetectLevels;
import plugins.fmp.multispots.tools.KymosCanvas2D;
import plugins.fmp.multispots.tools.Image.ImageTransformEnums;
import plugins.fmp.multispots.tools.Overlay.OverlayThreshold;
import plugins.kernel.roi.roi2d.ROI2DRectangle;



public class Levels extends JPanel implements PropertyChangeListener 
{
	private static final long serialVersionUID 	= -6329863521455897561L;
	
	private JCheckBox	pass1CheckBox 			= new JCheckBox ("pass1", true);
	private JComboBox<String> direction1ComboBox = new JComboBox<String> (new String[] {" threshold >", " threshold <" });
	private JSpinner 	threshold1Spinner 		= new JSpinner(new SpinnerNumberModel(35, 1, 255, 1));
	ImageTransformEnums[] transformPass1 		= new ImageTransformEnums[] {
			ImageTransformEnums.R_RGB, ImageTransformEnums.G_RGB, ImageTransformEnums.B_RGB, 
			ImageTransformEnums.R2MINUS_GB, ImageTransformEnums.G2MINUS_RB, ImageTransformEnums.B2MINUS_RG, ImageTransformEnums.RGB,
			ImageTransformEnums.GBMINUS_2R, ImageTransformEnums.RBMINUS_2G, ImageTransformEnums.RGMINUS_2B, ImageTransformEnums.RGB_DIFFS,
			ImageTransformEnums.H_HSB, ImageTransformEnums.S_HSB, ImageTransformEnums.B_HSB
			};
	JComboBox<ImageTransformEnums> transformPass1ComboBox = new JComboBox<ImageTransformEnums> (transformPass1);
	private JToggleButton transformPass1DisplayButton = new JToggleButton("View");
	private JCheckBox 	overlayPass1CheckBox 	= new JCheckBox("overlay");
	
	private JCheckBox	pass2CheckBox 			= new JCheckBox ("pass2", false);
	private JComboBox<String> direction2ComboBox= new JComboBox<String> (new String[] {" threshold >", " threshold <" });
	private JSpinner 	threshold2Spinner 		= new JSpinner(new SpinnerNumberModel(40, 1, 255, 1));
	ImageTransformEnums[] transformPass2 		= new ImageTransformEnums[] {
			ImageTransformEnums.YDIFFN, ImageTransformEnums.YDIFFN2,
			ImageTransformEnums.DERICHE, ImageTransformEnums.DERICHE_COLOR,
			ImageTransformEnums.MINUSHORIZAVG,
			ImageTransformEnums.COLORDISTANCE_L1_Y, ImageTransformEnums.COLORDISTANCE_L2_Y,
			ImageTransformEnums.SUBTRACT_1RSTCOL, ImageTransformEnums.L1DIST_TO_1RSTCOL
			};
	JComboBox<ImageTransformEnums> transformPass2ComboBox = new JComboBox<ImageTransformEnums> (transformPass2);
	private JToggleButton transformPass2DisplayButton = new JToggleButton("View");
	private JCheckBox 	overlayPass2CheckBox 	= new JCheckBox("overlay");
	
	private JCheckBox	allKymosCheckBox 		= new JCheckBox ("all kymographs", true);
	private JSpinner	spanTopSpinner			= new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
	private String 		detectString 			= "        Detect     ";
	private JButton 	detectButton 			= new JButton(detectString);
	private JCheckBox	fromCheckBox 			= new JCheckBox (" detection from ROI rectangle", false);
	
	private JCheckBox 	allSeriesCheckBox 		= new JCheckBox("ALL (current to last)", false);
	private JCheckBox	leftCheckBox 			= new JCheckBox ("L", true);
	private JCheckBox	rightCheckBox 			= new JCheckBox ("R", true);
	private JCheckBox	runBackwardsCheckBox 	= new JCheckBox ("run backwards", false);
	
	
	private multiSPOTS 	parent0 				= null;
	private DetectLevels threadDetectLevels 	= null;
	
	private String SEARCHRECT = new String("search_rectangle");
	private ROI2DRectangle searchRectangleROI2D = null;
	private OverlayThreshold overlayThreshold 	= null;
	// -----------------------------------------------------
		
	void init(GridLayout capLayout, multiSPOTS parent0) 
	{
		setLayout(capLayout);
		this.parent0 = parent0;
		
		FlowLayout layoutLeft = new FlowLayout(FlowLayout.LEFT); 
		
		JPanel panel0 = new JPanel(layoutLeft);
		((FlowLayout)panel0.getLayout()).setVgap(0);
		panel0.add(detectButton);
		panel0.add(allSeriesCheckBox);
		panel0.add(allKymosCheckBox);
		panel0.add(leftCheckBox);
		panel0.add(rightCheckBox);
		add(panel0);
		
		JPanel panel01 = new JPanel(layoutLeft);
		panel01.add(pass1CheckBox);
		panel01.add(direction1ComboBox);
		((JLabel) direction1ComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		panel01.add(threshold1Spinner);
		panel01.add(transformPass1ComboBox);
		panel01.add(transformPass1DisplayButton);
		panel01.add(overlayPass1CheckBox);
		add (panel01);
		
		JPanel panel02 = new JPanel(layoutLeft);
		panel02.add(pass2CheckBox);
		panel02.add(direction2ComboBox);
		((JLabel) direction2ComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		panel02.add(threshold2Spinner);
		panel02.add(transformPass2ComboBox);
		panel02.add(transformPass2DisplayButton);
		panel02.add(overlayPass2CheckBox);
		add (panel02);
		
		JPanel panel03 = new JPanel(layoutLeft);
		panel03.add(fromCheckBox);
		panel03.add(runBackwardsCheckBox);
		add( panel03);
		
		defineActionListeners();
		defineItemListeners();
		allowItemsAccordingToSelection();
	}
	
	private void defineItemListeners() 
	{
		overlayPass1CheckBox.addItemListener(new ItemListener() 
		{
		      public void itemStateChanged(ItemEvent e) 
		      {
		    	  Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		    	  if (exp != null) 
		    	  {
		    		  if (overlayPass1CheckBox.isSelected()) 
		    			  updateOverlay(exp);
		    		  else
		    			  removeOverlay(exp);
		    	  }
		      }});
		
		overlayPass2CheckBox.addItemListener(new ItemListener() 
		{
		      public void itemStateChanged(ItemEvent e) 
		      {
		    	  Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		    	  if (exp != null) 
		    	  {
		    		  if (overlayPass2CheckBox.isSelected()) 
		    			  updateOverlay(exp);
		    		  else
		    			  removeOverlay(exp);
		    	  }
		      }});
		
		threshold1Spinner.addChangeListener(new ChangeListener() 
		{
			 public void stateChanged(ChangeEvent e) 
		     {
		    	  updateOverlayThreshold();
		      }});
		
		threshold2Spinner.addChangeListener(new ChangeListener() 
		{
			 public void stateChanged(ChangeEvent e) 
		     {
		    	  updateOverlayThreshold();
		      }});
	}
	
	private void defineActionListeners() 
	{	
		transformPass1ComboBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null && exp.seqKymos != null) 
				{
					int index = transformPass1ComboBox.getSelectedIndex();
					getKymosCanvas(exp).imageTransformFunctionsCombo.setSelectedIndex(index +1);
					updateOverlayThreshold();
				}
			}});
		
		transformPass2ComboBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				allowItemsAccordingToSelection();
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null && exp.seqCamData != null) 
				{
					int index = transformPass2ComboBox.getSelectedIndex();
					getKymosCanvas(exp).imageTransformFunctionsCombo.setSelectedIndex(index +1);
					updateOverlayThreshold();
				}
			}});
	
		detectButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				if (detectButton.getText().equals(detectString))
					startLevelsDetection();
				else 
					stopLevelsDetection();
			}});	
		
		transformPass1DisplayButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) 
				{ 
					boolean displayCheckOverlay = false;
					if (transformPass1DisplayButton.isSelected()) {
						transformPass2DisplayButton.setSelected(false);
						KymosCanvas2D canvas = getKymosCanvas(exp);
						canvas.updateListOfImageTransformFunctions( transformPass1);
						int index = transformPass1ComboBox.getSelectedIndex();
						canvas.selectImageTransformFunction(index +1);
						displayCheckOverlay = true;
					}
					else
					{
						removeOverlay(exp);
						overlayPass1CheckBox.setSelected(false);
						getKymosCanvas(exp).imageTransformFunctionsCombo.setSelectedIndex(0);
						
					}
					overlayPass1CheckBox.setEnabled(displayCheckOverlay);
				}
			}});
		
		transformPass2DisplayButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) 
				{ 
					boolean displayCheckOverlay = false;
					if (transformPass2DisplayButton.isSelected()) {
						transformPass1DisplayButton.setSelected(false);
						KymosCanvas2D canvas = getKymosCanvas(exp);
						canvas.updateListOfImageTransformFunctions( transformPass2);
						int index = transformPass2ComboBox.getSelectedIndex();
						canvas.selectImageTransformFunction(index +1);
						displayCheckOverlay = true;
					}
					else
					{
						removeOverlay(exp);
						overlayPass2CheckBox.setSelected(false);
						getKymosCanvas(exp).imageTransformFunctionsCombo.setSelectedIndex(0);
					}
					overlayPass1CheckBox.setEnabled(displayCheckOverlay);
				}
			}});
		
		allSeriesCheckBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{
				Color color = Color.BLACK;
				if (allSeriesCheckBox.isSelected()) 
					color = Color.RED;
				allSeriesCheckBox.setForeground(color);
				detectButton.setForeground(color);
		}});
		
		fromCheckBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp == null)
						return;
				if (fromCheckBox.isSelected()) 
					displaySearchArea(exp);
				else if (searchRectangleROI2D != null)
					exp.seqKymos.seq.removeROI(searchRectangleROI2D);
			}});
		
		direction1ComboBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				updateOverlayThreshold();
			}});
	}
	
	void allowItemsAccordingToSelection() 
	{
		boolean flag = false;
		switch ((ImageTransformEnums) transformPass2ComboBox.getSelectedItem())
		{
		case SUBTRACT_1RSTCOL:
		case L1DIST_TO_1RSTCOL:
			flag = true;
			break;

		default:
			break;
		}
		threshold2Spinner.setEnabled(flag);
	}
	
	void setInfosToDialog(Capillary cap) 
	{
		BuildSeriesOptions options = cap.limitsOptions;
		
		pass1CheckBox.setSelected(options.pass1);
		pass2CheckBox.setSelected(options.pass2);
		
		transformPass1ComboBox.setSelectedItem(options.transform01);
		int index = options.directionUp1 ? 0:1;
		direction1ComboBox.setSelectedIndex(index);
		threshold1Spinner.setValue(options.detectLevel1Threshold);
		
		transformPass2ComboBox.setSelectedItem(options.transform02);
		index = options.directionUp2 ? 0:1;
		direction2ComboBox.setSelectedIndex(index);
		threshold2Spinner.setValue(options.detectLevel2Threshold);
		
		allKymosCheckBox.setSelected(options.detectAllKymos);
		leftCheckBox.setSelected(options.detectL);
		rightCheckBox.setSelected(options.detectR);
		
		fromCheckBox.setSelected(false);
	}
	
	void getInfosFromDialog(Capillary cap) 
	{
		BuildSeriesOptions capOptions 		= cap.limitsOptions;
		capOptions.pass1 					= pass1CheckBox.isSelected();
		capOptions.pass2 					= pass2CheckBox.isSelected();
		capOptions.transform01 				= (ImageTransformEnums) transformPass1ComboBox.getSelectedItem();
		capOptions.transform02 				= (ImageTransformEnums) transformPass2ComboBox.getSelectedItem();
		capOptions.directionUp1 			= (direction1ComboBox.getSelectedIndex() == 0) ;
		capOptions.detectLevel1Threshold 	= (int) threshold1Spinner.getValue();
		capOptions.directionUp2 			= (direction2ComboBox.getSelectedIndex() == 0) ;
		capOptions.detectLevel2Threshold 	= (int) threshold2Spinner.getValue();
		capOptions.detectAllKymos 			= allKymosCheckBox.isSelected();
		capOptions.detectL 					= leftCheckBox.isSelected();
		capOptions.detectR 					= rightCheckBox.isSelected();
	}
	
	private BuildSeriesOptions initBuildParameters(Experiment exp) 
	{	
		BuildSeriesOptions options = new BuildSeriesOptions();
		// list of stack experiments
		options.expList = parent0.expListCombo; 
		options.expList.index0 = parent0.expListCombo.getSelectedIndex();
		if (allSeriesCheckBox.isSelected()) 
			options.expList.index1 = options.expList.getItemCount()-1;
		else
			options.expList.index1 = parent0.expListCombo.getSelectedIndex();
		// list of kymographs
		options.detectAllKymos = allKymosCheckBox.isSelected();
		parent0.paneKymos.tabDisplay.indexImagesCombo = parent0.paneKymos.tabDisplay.kymographsCombo.getSelectedIndex();
		if (!allKymosCheckBox.isSelected()) 
		{
			options.kymoFirst = parent0.paneKymos.tabDisplay.indexImagesCombo;
			options.kymoLast = options.kymoFirst;
		}
		else
		{
			options.kymoFirst = 0;
			options.kymoLast = parent0.paneKymos.tabDisplay.kymographsCombo.getItemCount()-1;
		}
		// other parameters
		options.pass1 				= pass1CheckBox.isSelected();
		options.transform01 		= (ImageTransformEnums) transformPass1ComboBox.getSelectedItem();
		options.directionUp1 		= (direction1ComboBox.getSelectedIndex() == 0);
		options.detectLevel1Threshold= (int) threshold1Spinner.getValue();
		
		options.pass2 				= pass2CheckBox.isSelected();
		options.transform02			= (ImageTransformEnums) transformPass2ComboBox.getSelectedItem();
		options.directionUp2 		= (direction2ComboBox.getSelectedIndex() == 0);
		options.detectLevel2Threshold= (int) threshold2Spinner.getValue();
		
		options.analyzePartOnly		= fromCheckBox.isSelected();
		if (fromCheckBox.isSelected() && searchRectangleROI2D != null)
			options.searchArea 	= getSearchAreaFromSearchRectangle(exp);
		 
		options.spanDiffTop			= (int) spanTopSpinner.getValue();
		options.detectL 			= leftCheckBox.isSelected();
		options.detectR				= rightCheckBox.isSelected();
		options.parent0Rect 		= parent0.mainFrame.getBoundsInternal();
		options.binSubDirectory 	= parent0.expListCombo.expListBinSubDirectory ;
		options.runBackwards		= runBackwardsCheckBox.isSelected();
		return options;
	}
	
	void startLevelsDetection() 
	{
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();	
		if (exp != null)
		{
			threadDetectLevels = new DetectLevels();
			threadDetectLevels.options = initBuildParameters(exp);
			
			threadDetectLevels.addPropertyChangeListener(this);
			threadDetectLevels.execute();
			detectButton.setText("STOP");
		}
	}

	private void stopLevelsDetection() 
	{	
		if (threadDetectLevels != null && !threadDetectLevels.stopFlag) 
			threadDetectLevels.stopFlag = true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) 
	{
		 if (StringUtil.equals("thread_ended", evt.getPropertyName())) 
		 {
			detectButton.setText(detectString);
			parent0.paneKymos.tabDisplay.selectKymographImage(parent0.paneKymos.tabDisplay.indexImagesCombo);
			parent0.paneKymos.tabDisplay.indexImagesCombo = -1;
			fromCheckBox.setSelected(false);
		 }
	}
	
	private void displaySearchArea (Experiment exp)
	{
		if (searchRectangleROI2D == null ) {
			Rectangle searchRectangle = exp.seqKymos.seq.getBounds2D();
			searchRectangle.width -= 1; 
			searchRectangle.height  -= 1;
			searchRectangleROI2D = new ROI2DRectangle(searchRectangle);
			searchRectangleROI2D.setName(SEARCHRECT);
			searchRectangleROI2D.setColor(Color.ORANGE);
		}			
		exp.seqKymos.seq.addROI(searchRectangleROI2D);
		exp.seqKymos.seq.setSelectedROI(searchRectangleROI2D);
	}
	
	private Rectangle getSearchAreaFromSearchRectangle(Experiment exp) 
	{
		Rectangle rectangle = searchRectangleROI2D.getBounds();
		Rectangle seqRectangle = exp.seqKymos.seq.getBounds2D();
		if (rectangle.x < 0) 
			rectangle.x = 0;
		if (rectangle.y < 0) 
			rectangle.y = 0;
		if ((rectangle.width + rectangle.x) > seqRectangle.width) 
			rectangle.width = seqRectangle.width -1 - rectangle.x ; 
		if ((rectangle.height + rectangle.y)> (seqRectangle.height-1)) 
			rectangle.height = seqRectangle.height -1 - rectangle.y;
		return rectangle;
	}
	
	protected KymosCanvas2D getKymosCanvas(Experiment exp) 
	{
		KymosCanvas2D canvas = (KymosCanvas2D) exp.seqKymos.seq.getFirstViewer().getCanvas();
		return canvas;
	}
	
	void updateOverlay (Experiment exp) 
	{
		if (exp.seqKymos == null)
			return;
		if (overlayThreshold == null) 
			overlayThreshold = new OverlayThreshold(exp.seqKymos);
		else 
		{
			exp.seqKymos.seq.removeOverlay(overlayThreshold);
			overlayThreshold.setSequence(exp.seqKymos);
		}
		
		if (transformPass1DisplayButton.isSelected() || transformPass2DisplayButton.isSelected()) {
			exp.seqKymos.seq.addOverlay(overlayThreshold);	
			updateOverlayThreshold();	
		}
	}
	
	void updateOverlayThreshold() 
	{
		if (overlayThreshold == null)
			return;
		
		boolean ifGreater = true; 
		int threshold = 0;
		ImageTransformEnums transform = ImageTransformEnums.NONE;
		if (transformPass1DisplayButton.isSelected() ) {
			ifGreater = (direction1ComboBox.getSelectedIndex() == 0); 
			threshold = (int) threshold1Spinner.getValue();
			transform = (ImageTransformEnums) transformPass1ComboBox.getSelectedItem();
		}
		else if (transformPass2DisplayButton.isSelected() ) 
		{
			ifGreater = (direction2ComboBox.getSelectedIndex() == 0); 
			threshold = (int) threshold2Spinner.getValue();
			transform = (ImageTransformEnums) transformPass2ComboBox.getSelectedItem();
		}
		else
			return;
		overlayThreshold.setThresholdSingle(threshold, transform, ifGreater);
		overlayThreshold.painterChanged();
	}
	
	void removeOverlay(Experiment exp) 
	{
		if (exp.seqKymos != null && exp.seqKymos.seq != null)
			exp.seqKymos.seq.removeOverlay(overlayThreshold);
	}

	
}
