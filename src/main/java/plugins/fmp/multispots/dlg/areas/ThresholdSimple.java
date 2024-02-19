package plugins.fmp.multispots.dlg.areas;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.util.StringUtil;
import plugins.fmp.multispots.MultiSPOTS;
import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.series.BuildSeriesOptions;
import plugins.fmp.multispots.series.DetectArea;
import plugins.fmp.multispots.series.DetectLevels;
import plugins.fmp.multispots.tools.Canvas2DWithFilters;
import plugins.fmp.multispots.tools.ImageTransform.ImageTransformEnums;
import plugins.fmp.multispots.tools.Overlay.OverlayThreshold;

public class ThresholdSimple  extends JPanel implements PropertyChangeListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8921207247623517524L;
	
	private String 		detectString 			= "        Detect     ";
	private JButton 	detectButton 			= new JButton(detectString);
	private JCheckBox 	allSeriesCheckBox 		= new JCheckBox("ALL (current to last)", false);
	
	private JLabel videochannel = new JLabel("filter  ");
	private JComboBox<String> direction1ComboBox = new JComboBox<String> (new String[] {" threshold >", " threshold <" });
	
	ImageTransformEnums[] transforms = new ImageTransformEnums[] {
			ImageTransformEnums.R_RGB, ImageTransformEnums.G_RGB, ImageTransformEnums.B_RGB, 
			ImageTransformEnums.R2MINUS_GB, ImageTransformEnums.G2MINUS_RB, ImageTransformEnums.B2MINUS_RG, ImageTransformEnums.RGB,
			ImageTransformEnums.GBMINUS_2R, ImageTransformEnums.RBMINUS_2G, ImageTransformEnums.RGMINUS_2B, ImageTransformEnums.RGB_DIFFS,
			ImageTransformEnums.H_HSB, ImageTransformEnums.S_HSB, ImageTransformEnums.B_HSB
			};
	JComboBox<ImageTransformEnums> transformsComboBox = new JComboBox<ImageTransformEnums> (transforms);
	JSpinner thresholdSpinner = new JSpinner(new SpinnerNumberModel(35, 0, 255, 1));
	private JCheckBox 	overlayCheckBox 	= new JCheckBox("overlay");
	private JToggleButton displayTransformButton = new JToggleButton("View");
	
	private OverlayThreshold overlayThreshold 	= null;
	private DetectArea threadDetectLevels 	= null;
	
	MultiSPOTS parent0 = null;
	
	
	
	public void init(GridLayout gridLayout, MultiSPOTS parent0) 
	{	
		setLayout(gridLayout);
		this.parent0 = parent0;
		FlowLayout layoutLeft = new FlowLayout(FlowLayout.LEFT);

		JPanel panel0 = new JPanel(layoutLeft);
		((FlowLayout)panel0.getLayout()).setVgap(0);
		panel0.add(detectButton);
		panel0.add(allSeriesCheckBox);
		add(panel0);
		
		JPanel panel1 = new JPanel(layoutLeft);
		((FlowLayout)panel1.getLayout()).setVgap(0);
		panel1.add( videochannel);
		panel1.add(transformsComboBox);
		add(panel1);
		panel1.add(direction1ComboBox);
		panel1.add(thresholdSpinner);
		panel1.add(displayTransformButton);
		panel1.add(overlayCheckBox);
		add(panel1);
		
//		transformsComboBox.setSelectedItem(EnumImageOp.NORM_BRMINUSG);
		
		declareListeners();
	}
	
	private void declareListeners() {
		overlayCheckBox.addItemListener(new ItemListener() 
		{
		  public void itemStateChanged(ItemEvent e) 
		  {
			  Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
			  if (exp != null) 
			  {
				  if (overlayCheckBox.isSelected()) 
					  updateOverlay(exp);
				  else
					  removeOverlay(exp);
			  }
		  }});
		
		transformsComboBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null && exp.seqKymos != null) 
				{
					int index = transformsComboBox.getSelectedIndex();
					getCanvas2DWithFilters(exp).imageTransformFunctionsCombo.setSelectedIndex(index +1);
					updateOverlayThreshold();
				}
			}});
		
		direction1ComboBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				updateOverlayThreshold();
			}});

		thresholdSpinner.addChangeListener(new ChangeListener() 
		{
			 public void stateChanged(ChangeEvent e) 
		     {
		    	  updateOverlayThreshold();
		      }});
		
		displayTransformButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) 
				{ 
					boolean displayCheckOverlay = false;
					if (displayTransformButton.isSelected()) {
						Canvas2DWithFilters canvas = getCanvas2DWithFilters(exp);
						canvas.updateListOfImageTransformFunctions( transforms);
						int index = transformsComboBox.getSelectedIndex();
						canvas.selectImageTransformFunction(index +1);
						displayCheckOverlay = true;
					}
					else
					{
						removeOverlay(exp);
						overlayCheckBox.setSelected(false);
						getCanvas2DWithFilters(exp).imageTransformFunctionsCombo.setSelectedIndex(0);
						
					}
					overlayCheckBox.setEnabled(displayCheckOverlay);
				}
			}});
		
		detectButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				if (detectButton.getText().equals(detectString))
					startDetection();
				else 
					stopDetection();
			}});	
	}

	void updateOverlay (Experiment exp) 
	{
		if (exp.seqKymos == null)
			return;
		
		if (overlayThreshold == null) 
			overlayThreshold = new OverlayThreshold(exp.seqCamData);
		else 
		{
			exp.seqCamData.seq.removeOverlay(overlayThreshold);
			overlayThreshold.setSequence(exp.seqCamData);
		}
		exp.seqCamData.seq.addOverlay(overlayThreshold);
	}


	void removeOverlay(Experiment exp) 
	{
		if (exp.seqCamData != null && exp.seqCamData.seq != null)
			exp.seqCamData.seq.removeOverlay(overlayThreshold);
	}
	
	protected Canvas2DWithFilters getCanvas2DWithFilters(Experiment exp) 
	{
		return (Canvas2DWithFilters) exp.seqCamData.seq.getFirstViewer().getCanvas();
	}
	
	void updateOverlayThreshold() 
	{
		if (overlayThreshold == null)
			return;
		
		boolean ifGreater = true; 
		int threshold = 0;
		ImageTransformEnums transform = ImageTransformEnums.NONE;

		ifGreater = (direction1ComboBox.getSelectedIndex() == 0); 
		threshold = (int) thresholdSpinner.getValue();
		transform = (ImageTransformEnums) transformsComboBox.getSelectedItem();
		
		overlayThreshold.setThresholdSingle(threshold, transform, ifGreater);
		overlayThreshold.painterChanged();
	}
	
	void startDetection() 
	{
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();	
		if (exp != null)
		{
			threadDetectLevels = new DetectArea();
			threadDetectLevels.options = initBuildParameters(exp);
			
			threadDetectLevels.addPropertyChangeListener(this);
			threadDetectLevels.execute();
			detectButton.setText("STOP");
		}
	}

	private void stopDetection() 
	{	
		if (threadDetectLevels != null && !threadDetectLevels.stopFlag) 
			threadDetectLevels.stopFlag = true;
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
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		 if (StringUtil.equals("thread_ended", evt.getPropertyName())) 
		 {
			detectButton.setText(detectString);
			parent0.paneKymos.tabDisplay.selectKymographImage(parent0.paneKymos.tabDisplay.indexImagesCombo);
			parent0.paneKymos.tabDisplay.indexImagesCombo = -1;
		 }
	}
	
//	public void transferParametersToDialog(DetectionParameters detectionParameters) {
//		
//		transformsComboBox.setSelectedItem(detectionParameters.simpletransformop);
//		thresholdSpinner.setValue(detectionParameters.simplethreshold);
//	}
//	
//	public void transferDialogToParameters(DetectionParameters detectionParameters) {
//		detectionParameters.simpletransformop = (EnumImageOp) transformsComboBox.getSelectedItem();
//		detectionParameters.simplethreshold = (int) thresholdSpinner.getValue();
//	}
}
