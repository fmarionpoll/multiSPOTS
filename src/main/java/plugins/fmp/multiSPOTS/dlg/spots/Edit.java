package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

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

import icy.image.IcyBufferedImage;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.Spot;
import plugins.fmp.multiSPOTS.series.BuildSeriesOptions;
import plugins.fmp.multiSPOTS.tools.Canvas2DWithFilters;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformEnums;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformInterface;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformOptions;
import plugins.fmp.multiSPOTS.tools.Overlay.OverlayThreshold;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DMeasures;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DShape;



public class Edit extends JPanel 
{
	/**
	 * 
	 */
	private static final long 	serialVersionUID 	= 4950182090521600937L;
	
	private JButton				editSpotsButton			= new JButton("Edit spots infos...");
	private SpotTable	   		infosSpotTable			= null;
	private List<Spot>			spotsArrayCopy			= new ArrayList<Spot>();
	
	private JButton 			outlineSpotsButton 		= new JButton("Detect spots contours");
	private JButton 			useContoursButton 		= new JButton("Replace ellipses with contours");
	private JButton 			restoreSpotsButton 		= new JButton("Restore");
	
	private JLabel 				spotsFilterLabel 		= new JLabel("Spots filter");
	private String[]  			directions 				= new String[] {" threshold >", " threshold <" };
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
	
	private OverlayThreshold 	overlayThreshold 		= null;
	private MultiSPOTS 			parent0 				= null;
	
	
	void init(GridLayout gridLayout, MultiSPOTS parent0) 
	{
		setLayout(gridLayout);
		this.parent0 = parent0;
		
		FlowLayout layoutLeft = new FlowLayout(FlowLayout.LEFT);
		layoutLeft.setVgap(0);
		
		JPanel panel01 = new JPanel(layoutLeft);
		panel01.add( editSpotsButton);
		add(panel01);
		
		JPanel panel0 = new JPanel(layoutLeft);
		panel0.add(outlineSpotsButton);
		panel0.add(useContoursButton);
		panel0.add(restoreSpotsButton);
		add(panel0);
		
		JPanel panel1 = new JPanel(layoutLeft);
		panel1.add(spotsFilterLabel);
		panel1.add(spotsTransformsComboBox);	
		panel1.add(spotsDirectionComboBox);
		panel1.add(spotsThresholdSpinner);
		panel1.add(spotsViewButton);
		panel1.add(spotsOverlayCheckBox);
		add(panel1);

		spotsTransformsComboBox.setSelectedItem(ImageTransformEnums.RGB_DIFFS);
		spotsDirectionComboBox.setSelectedIndex(1);
		declareListeners();
	}
	
	private void declareListeners() 
	{
		editSpotsButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					exp.spotsArray.transferDescriptionToSpots();
					if (infosSpotTable != null) {
						infosSpotTable.close();
					}
					infosSpotTable = new SpotTable();
					infosSpotTable.initialize(parent0, spotsArrayCopy);
					infosSpotTable.requestFocus();
				}
			}});

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
		
		outlineSpotsButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					ROI2DUtilities.removeRoisContainingString(-1, "mask", exp.seqCamData.seq);
					reduceSpotArea(exp) ;
					}
			}});
		
		useContoursButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					transferContoursToSpotsRois(exp) ;
					}
			}});
		
		restoreSpotsButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					restoreOldSpotsRois(exp) ;
					}
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
	
	private BuildSeriesOptions initDetectOptions(Experiment exp) 
	{	
		BuildSeriesOptions options = new BuildSeriesOptions();
		// list of stack experiments
		options.expList = parent0.expListCombo; 
		options.expList.index0 = parent0.expListCombo.getSelectedIndex();
		options.expList.index1 = parent0.expListCombo.getSelectedIndex();
		options.detectAllSeries = false;
		options.seriesFirst = 0;
	
		// other parameters
		options.transform01 		= (ImageTransformEnums) spotsTransformsComboBox.getSelectedItem();
		options.spotThresholdUp 	= (spotsDirectionComboBox.getSelectedIndex() == 0);
		options.spotThreshold		= (int) spotsThresholdSpinner.getValue();
				
		options.analyzePartOnly		= false; //fromCheckBox.isSelected();
		
		options.overlayTransform = (ImageTransformEnums) spotsTransformsComboBox.getSelectedItem(); 
		options.overlayIfGreater = (spotsDirectionComboBox.getSelectedIndex() == 0);
		options.overlayThreshold = (int) spotsThresholdSpinner.getValue();
		
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
	
	private void reduceSpotArea(Experiment exp) 
	{
		BuildSeriesOptions options = initDetectOptions(exp);
		ImageTransformOptions transformOptions = new ImageTransformOptions();
		transformOptions.transformOption = options.transform01;
		transformOptions.setSingleThreshold (options.spotThreshold, options.spotThresholdUp) ;
	
		ImageTransformInterface transformFunction = options.transform01.getFunction();
		
		Sequence seqData = exp.seqCamData.seq;
		seqData.addOverlay(overlayThreshold);
		int t = 0;
		IcyBufferedImage sourceImage = seqData.getImage(t, 0);
		IcyBufferedImage workImage = transformFunction.getTransformedImage(sourceImage, transformOptions); 
		for (Spot spot: exp.spotsArray.spotsList)  {
			try {
				spot.mask2D = spot.getRoi().getBooleanMask2D( 0 , 0, 1, true );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ROI2DPolygon roi = ROI2DMeasures.getContourOfDetectedSpot (workImage, spot, options);
		    
		    roi.setName(spot.getRoi().getName()+"_mask");
		    roi.setColor(Color.RED);
		    seqData.addROI(roi);
		}
	}
	
	private void transferContoursToSpotsRois(Experiment exp) 
	{
		List<ROI2D> contoursList = ROI2DUtilities.getROIs2DContainingString ("mask", exp.seqCamData.seq);
		for (ROI2D contour: contoursList)
		{
			int length = contour.getName().length();
			String name = contour.getName().substring(0, length-5);
			Spot spot = exp.spotsArray.getSpotFromName(name);
			ROI2D roi_old = spot.getRoi();
			exp.seqCamData.seq.removeROI(contour);
			exp.seqCamData.seq.removeROI(roi_old);
			spot.setRoi_old((ROI2DShape) roi_old);
			contour.setName(name);
			contour.setColor(roi_old.getColor());
			spot.setRoi((ROI2DShape) contour);
			exp.seqCamData.seq.addROI(contour);
		}
	}
	
	private void restoreOldSpotsRois(Experiment exp) 
	{
		for (Spot spot: exp.spotsArray.spotsList)
		{
			ROI2D roi_old = spot.getRoi();
			ROI2D roi = spot.getRoi_old();
			spot.setRoi((ROI2DShape) roi);
			exp.seqCamData.seq.removeROI(roi_old);
			exp.seqCamData.seq.addROI(roi);
		}
	}

			
}
