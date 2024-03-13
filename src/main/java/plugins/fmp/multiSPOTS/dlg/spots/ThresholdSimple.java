package plugins.fmp.multiSPOTS.dlg.spots;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.util.StringUtil;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.series.BuildSeriesOptions;
import plugins.fmp.multiSPOTS.series.DetectSpots;
import plugins.fmp.multiSPOTS.tools.Canvas2DWithFilters;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformEnums;
import plugins.fmp.multiSPOTS.tools.Overlay.OverlayThreshold;

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
			JSpinner 			thresholdSpinner = new JSpinner(new SpinnerNumberModel(35, 0, 255, 1));
	private JCheckBox 			overlayCheckBox 	= new JCheckBox("overlay");
	private JToggleButton 		displayTransformButton = new JToggleButton("View");
	
	private OverlayThreshold 	overlayThreshold 	= null;
	private DetectSpots 			threadDetectLevels 	= null;
	
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
		
		transformsComboBox.setSelectedItem(ImageTransformEnums.RGB_DIFFS);
		direction1ComboBox.setSelectedIndex(1);
		declareListeners();
	}
	
	private void declareListeners() 
	{
		overlayCheckBox.addItemListener(new ItemListener() 
		{
		  public void itemStateChanged(ItemEvent e) 
		  {
			  Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
			  if (exp != null) 
			  {
				  if (overlayCheckBox.isSelected()) 
				  {
					  updateOverlay(exp);
					  updateOverlayThreshold();
				  }
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
					Canvas2DWithFilters canvas = getCanvas2DWithFilters(exp);
					updateTransformFunctionsOfCanvas(exp);
					if (!displayTransformButton.isSelected()) {
						displayTransformButton.setSelected(true);
					}
					canvas.imageTransformFunctionsCombo.setSelectedIndex(index +1);
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
					displayTransform(exp);
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
			overlayThreshold = new OverlayThreshold(exp.seqCamData.seq);
		else 
		{
			exp.seqCamData.seq.removeOverlay(overlayThreshold);
			overlayThreshold.setSequence(exp.seqCamData.seq);
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
			threadDetectLevels = new DetectSpots();
			threadDetectLevels.options = initDetectOptions(exp);
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
	
	private BuildSeriesOptions initDetectOptions(Experiment exp) 
	{	
		BuildSeriesOptions options = new BuildSeriesOptions();
		// list of stack experiments
		options.expList = parent0.expListCombo; 
		options.expList.index0 = parent0.expListCombo.getSelectedIndex();
		if (allSeriesCheckBox.isSelected()) 
			options.expList.index1 = options.expList.getItemCount()-1;
		else
			options.expList.index1 = parent0.expListCombo.getSelectedIndex();
		// list of files
		options.detectAllSeries = allSeriesCheckBox.isSelected();
//		parent0.paneKymos.tabDisplay.indexImagesCombo = parent0.paneKymos.tabDisplay.kymographsCombo.getSelectedIndex();
		if (!allSeriesCheckBox.isSelected()) 
		{
//			options.seriesFirst = parent0.paneKymos.tabDisplay.indexImagesCombo;
			options.seriesLast = options.seriesFirst;
		}
		else
		{
			options.seriesFirst = 0;
//			options.seriesLast = parent0.paneKymos.tabDisplay.kymographsCombo.getItemCount()-1;
		}
		// other parameters
		options.transform01 		= (ImageTransformEnums) transformsComboBox.getSelectedItem();
		options.overthreshold 		= (direction1ComboBox.getSelectedIndex() == 0);
		options.detectLevel1Threshold= (int) thresholdSpinner.getValue();
				
		options.analyzePartOnly		= false; //fromCheckBox.isSelected();
		
		options.overlayTransform = (ImageTransformEnums) transformsComboBox.getSelectedItem(); 
		options.overlayIfGreater = (direction1ComboBox.getSelectedIndex() == 0);
		options.overlayThreshold = (int) thresholdSpinner.getValue();
		
		return options;
	}

	private void displayTransform (Experiment exp)
	{
		boolean displayCheckOverlay = false;
		if (displayTransformButton.isSelected()) {
			updateTransformFunctionsOfCanvas( exp);
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
	
	private void updateTransformFunctionsOfCanvas(Experiment exp)
	{
		Canvas2DWithFilters canvas = getCanvas2DWithFilters(exp);
		if (canvas.imageTransformFunctionsCombo.getItemCount() < (transformsComboBox.getItemCount()+1)) 
		{
			canvas.updateListOfImageTransformFunctions(transforms);
		}
		int index = transformsComboBox.getSelectedIndex();
		canvas.selectImageTransformFunction(index +1);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		 if (StringUtil.equals("thread_ended", evt.getPropertyName())) 
		 {
			detectButton.setText(detectString);
			Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
			if (exp != null) {
				exp.loadSpotsMeasures();
				parent0.paneSpots.tabGraphs.displayGraphsPanels(exp);
			}
		 }
	}

}
