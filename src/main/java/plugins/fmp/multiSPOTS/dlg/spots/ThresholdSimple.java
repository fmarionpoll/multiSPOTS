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
	
	private String 				detectString 			= "        Detect     ";
	private JButton 			detectButton 			= new JButton(detectString);
	private JCheckBox 			allSeriesCheckBox 		= new JCheckBox("ALL (current to last)", false);
	
	private JLabel 				spotsFilterLabel 		= new JLabel("Spots filter");
	private String[]  directions = new String[] {" threshold >", " threshold <" };
	
	
	
	ImageTransformEnums[] transforms = new ImageTransformEnums[] {
			ImageTransformEnums.R_RGB, 		ImageTransformEnums.G_RGB, 		ImageTransformEnums.B_RGB, 
			ImageTransformEnums.R2MINUS_GB, ImageTransformEnums.G2MINUS_RB, ImageTransformEnums.B2MINUS_RG, ImageTransformEnums.RGB,
			ImageTransformEnums.GBMINUS_2R, ImageTransformEnums.RBMINUS_2G, ImageTransformEnums.RGMINUS_2B, ImageTransformEnums.RGB_DIFFS,
			ImageTransformEnums.H_HSB, 		ImageTransformEnums.S_HSB, 		ImageTransformEnums.B_HSB
			};
	private JComboBox<ImageTransformEnums> spotsTransformsComboBox = new JComboBox<ImageTransformEnums> (transforms);
	private JComboBox<String> 	spotsDirectionComboBox 	= new JComboBox<String> (directions);
	private JSpinner 			spotsThresholdSpinner 	= new JSpinner(new SpinnerNumberModel(35, 0, 255, 1));
	private JCheckBox 			spotsOverlayCheckBox 	= new JCheckBox("overlay");
	private JToggleButton 		spotsViewButton 		= new JToggleButton("View");
	
	private JLabel 				fliesFilterLabel 		= new JLabel("  Flies filter");
	private JComboBox<ImageTransformEnums> fliesTransformsComboBox = new JComboBox<ImageTransformEnums> (transforms);
	private JComboBox<String> 	fliesDirectionComboBox 	= new JComboBox<String> (directions);
	private JSpinner 			fliesThresholdSpinner 	= new JSpinner(new SpinnerNumberModel(15, 0, 255, 1));
	private JCheckBox 			fliesOverlayCheckBox 	= new JCheckBox("overlay");
	private JToggleButton 		fliesViewButton 		= new JToggleButton("View");
	
	private OverlayThreshold 	overlayThreshold 		= null;
	private DetectSpots 		threadDetectLevels 		= null;	
	private MultiSPOTS 			parent0 				= null;
	
	
	
	public void init(GridLayout gridLayout, MultiSPOTS parent0) 
	{	
		setLayout(gridLayout);
		this.parent0 = parent0;
		FlowLayout layoutLeft = new FlowLayout(FlowLayout.LEFT);
		layoutLeft.setVgap(0);

		JPanel panel0 = new JPanel(layoutLeft);
		panel0.add(detectButton);
		panel0.add(allSeriesCheckBox);
		add(panel0);
		
		JPanel panel1 = new JPanel(layoutLeft);
		panel1.add(spotsFilterLabel);
		panel1.add(spotsTransformsComboBox);	
		panel1.add(spotsDirectionComboBox);
		panel1.add(spotsThresholdSpinner);
		panel1.add(spotsViewButton);
		panel1.add(spotsOverlayCheckBox);
		add(panel1);
		
		JPanel panel2 = new JPanel(layoutLeft);
		panel2.add(fliesFilterLabel);
		panel2.add(fliesTransformsComboBox);	
		panel2.add(fliesDirectionComboBox);
		panel2.add(fliesThresholdSpinner);
		panel2.add(fliesViewButton);
		panel2.add(fliesOverlayCheckBox);
		add(panel2);
		
		spotsTransformsComboBox.setSelectedItem(ImageTransformEnums.RGB_DIFFS);
		spotsDirectionComboBox.setSelectedIndex(1);
		
		fliesTransformsComboBox.setSelectedItem(ImageTransformEnums.B_RGB);
		fliesDirectionComboBox.setSelectedIndex(1);
		declareListeners();
	}
	
	private void declareListeners() 
	{
		spotsOverlayCheckBox.addItemListener(new ItemListener() 
		{
		  public void itemStateChanged(ItemEvent e) 
		  {
			  Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
			  if (exp != null) 
			  {
				  if (spotsOverlayCheckBox.isSelected()) 
				  {
					  updateOverlay(exp);
					  updateOverlayThreshold();
				  }
				  else
					  removeOverlay(exp);
			  }
		  }});
		
		spotsTransformsComboBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null && exp.seqKymos != null) 
				{				
					int index = spotsTransformsComboBox.getSelectedIndex();
					Canvas2DWithFilters canvas = getCanvas2DWithFilters(exp);
					updateTransformFunctionsOfCanvas(exp);
					if (!spotsViewButton.isSelected()) {
						spotsViewButton.setSelected(true);
					}
					canvas.imageTransformFunctionsCombo.setSelectedIndex(index +1);
					updateOverlayThreshold();
				}
			}});
		
		spotsDirectionComboBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				updateOverlayThreshold();
			}});

		spotsThresholdSpinner.addChangeListener(new ChangeListener() 
		{
			 public void stateChanged(ChangeEvent e) 
		     {
		    	  updateOverlayThreshold();
		      }});
		
		spotsViewButton.addActionListener(new ActionListener () 
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

		ifGreater = (spotsDirectionComboBox.getSelectedIndex() == 0); 
		threshold = (int) spotsThresholdSpinner.getValue();
		transform = (ImageTransformEnums) spotsTransformsComboBox.getSelectedItem();
		
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
		options.detectAllSeries = allSeriesCheckBox.isSelected();
		if (!allSeriesCheckBox.isSelected()) 
		{
			options.seriesLast = options.seriesFirst;
		}
		else
		{
			options.seriesFirst = 0;
		}
		// other parameters
		options.transform01 		= (ImageTransformEnums) spotsTransformsComboBox.getSelectedItem();
		options.thresholdUp 		= (spotsDirectionComboBox.getSelectedIndex() == 0);
		options.detectLevel1Threshold= (int) spotsThresholdSpinner.getValue();
				
		options.analyzePartOnly		= false; //fromCheckBox.isSelected();
		
		options.overlayTransform = (ImageTransformEnums) spotsTransformsComboBox.getSelectedItem(); 
		options.overlayIfGreater = (spotsDirectionComboBox.getSelectedIndex() == 0);
		options.overlayThreshold = (int) spotsThresholdSpinner.getValue();
		
		options.thresholdFly  = (int) fliesThresholdSpinner.getValue();
		
		return options;
	}

	private void displayTransform (Experiment exp)
	{
		boolean displayCheckOverlay = false;
		if (spotsViewButton.isSelected()) {
			updateTransformFunctionsOfCanvas( exp);
			displayCheckOverlay = true;
		}
		else
		{
			removeOverlay(exp);
			spotsOverlayCheckBox.setSelected(false);
			getCanvas2DWithFilters(exp).imageTransformFunctionsCombo.setSelectedIndex(0);
			
		}
		spotsOverlayCheckBox.setEnabled(displayCheckOverlay);
	}
	
	private void updateTransformFunctionsOfCanvas(Experiment exp)
	{
		Canvas2DWithFilters canvas = getCanvas2DWithFilters(exp);
		if (canvas.imageTransformFunctionsCombo.getItemCount() < (spotsTransformsComboBox.getItemCount()+1)) 
		{
			canvas.updateListOfImageTransformFunctions(transforms);
		}
		int index = spotsTransformsComboBox.getSelectedIndex();
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
