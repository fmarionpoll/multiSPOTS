package plugins.fmp.multiSPOTS.dlg.spots;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.Spot;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformEnums;



public class Edit extends JPanel 
{
	/**
	 * 
	 */
	private static final long 	serialVersionUID 	= 4950182090521600937L;
	
	private JButton			editSpotsButton			= new JButton("Edit spots infos...");
	private SpotTable	   	infosSpotTable			= null;
	private List<Spot>		spotsArrayCopy			= new ArrayList<Spot>();
	
	private JButton 			reduceSpotAreasButton 	= new JButton("Reduce spots areas");
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
	
	private MultiSPOTS 		parent0 				= null;
	
	
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
		panel0.add(reduceSpotAreasButton);
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
		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{
		editSpotsButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					exp.spotsArray.transferDescriptionToSpots();
					if (infosSpotTable == null) {
						infosSpotTable = new SpotTable();
						infosSpotTable.initialize(parent0, spotsArrayCopy);
					}
					else
						infosSpotTable.requestFocus();
				}
			}});
	}
				
}
