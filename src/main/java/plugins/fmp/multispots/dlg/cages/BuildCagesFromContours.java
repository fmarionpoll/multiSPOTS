package plugins.fmp.multispots.dlg.cages;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.roi.ROI2D;
import icy.type.DataType;
import icy.type.geom.Polygon2D;
import plugins.fmp.multispots.MultiSPOTS;
import plugins.fmp.multispots.experiment.Capillary;
import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.experiment.SequenceCamData;
import plugins.fmp.multispots.tools.Blobs;
import plugins.fmp.multispots.tools.ROI2DUtilities;
import plugins.fmp.multispots.tools.ImageTransform.ImageTransformEnums;
import plugins.fmp.multispots.tools.Overlay.OverlayThreshold;
import plugins.kernel.roi.roi2d.ROI2DPolygon;



public class BuildCagesFromContours  extends JPanel implements ChangeListener 
{
	/**
	 * 
	 */
	private static final long serialVersionUID 	= -121724000730795396L;
	private JButton 	createCagesButton 		= new JButton("Create cages");
	private JSpinner 	thresholdSpinner 		= new JSpinner(new SpinnerNumberModel(60, 0, 10000, 1));
	public 	JCheckBox 	overlayCheckBox			= new JCheckBox("Overlay ", false);
	private JButton 	deleteButton 			= new JButton("Cut points within selected polygon");
	JComboBox<ImageTransformEnums> transformForLevelsComboBox = new JComboBox<ImageTransformEnums> (
		new ImageTransformEnums[] {
				ImageTransformEnums.R_RGB, ImageTransformEnums.G_RGB, ImageTransformEnums.B_RGB, 
				ImageTransformEnums.R2MINUS_GB, ImageTransformEnums.G2MINUS_RB, ImageTransformEnums.B2MINUS_RG, ImageTransformEnums.RGB,
				ImageTransformEnums.GBMINUS_2R, ImageTransformEnums.RBMINUS_2G, ImageTransformEnums.RGMINUS_2B, 
				ImageTransformEnums.H_HSB, ImageTransformEnums.S_HSB, ImageTransformEnums.B_HSB	});
	private OverlayThreshold overlayThreshold 	= null;
	private MultiSPOTS 			parent0			= null;
	
	
	
	void init(GridLayout capLayout, MultiSPOTS parent0) 
	{
		setLayout(capLayout);
		this.parent0 = parent0;
		
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
		flowLayout.setVgap(0);
		
		JPanel panel1 = new JPanel(flowLayout);
		panel1.add(createCagesButton);
		add(panel1);
		
		JLabel videochannel = new JLabel("detect from ");
		videochannel.setHorizontalAlignment(SwingConstants.RIGHT);
		transformForLevelsComboBox.setSelectedIndex(2);
		JPanel panel2 = new JPanel(flowLayout);
		panel2.add( videochannel);
		panel2.add(transformForLevelsComboBox);
		panel2.add(overlayCheckBox);
		panel2.add(thresholdSpinner);
		add(panel2);
		
		JPanel panel3 = new JPanel(flowLayout);
		panel3.add(deleteButton);
		add(panel3);
		
		defineActionListeners();
		thresholdSpinner.addChangeListener(this);
		overlayCheckBox.addChangeListener(this);
	}
	
	
	private void defineActionListeners() 
	{
		createCagesButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) 
				{
					ROI2DUtilities.removeRoisContainingString(-1, "cage", exp.seqCamData.seq);
					exp.cages.removeCages();
					createROIsFromSelectedPolygon(exp);
					exp.cages.cagesFromROIs(exp.seqCamData);
					exp.cages.setFirstAndLastCageToZeroFly();
					if(exp.capillaries.capillariesList.size() > 0)
						exp.cages.transferNFliesFromCapillariesToCages(exp.capillaries.capillariesList);
				}
			}});
		
		transformForLevelsComboBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
					updateOverlay(exp);
			}});
		
		deleteButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp =  (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
					try {
						deletePointsIncluded(exp);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}});
	}

	public void updateOverlay (Experiment exp) 
	{
		SequenceCamData seqCamData = exp.seqCamData;
		if (seqCamData == null)
			return;
		if (overlayThreshold == null) 
		{
			overlayThreshold = new OverlayThreshold(seqCamData);
			seqCamData.seq.addOverlay(overlayThreshold);
		}
		else 
		{
			seqCamData.seq.removeOverlay(overlayThreshold);
			overlayThreshold.setSequence(seqCamData);
			seqCamData.seq.addOverlay(overlayThreshold);
		}
		exp.cages.detect_threshold = (int) thresholdSpinner.getValue();
		overlayThreshold.setThresholdTransform(
				exp.cages.detect_threshold,  
				(ImageTransformEnums) transformForLevelsComboBox.getSelectedItem(),
				false);
		seqCamData.seq.overlayChanged(overlayThreshold);
		seqCamData.seq.dataChanged();		
	}
	
	
	public void removeOverlay(Experiment exp) 
	{
		if (exp.seqCamData != null && exp.seqCamData.seq != null)
			exp.seqCamData.seq.removeOverlay(overlayThreshold);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) 
	{
		if (e.getSource() == thresholdSpinner) 
		{
			Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
			if (exp != null)
				updateOverlay(exp);
		}
		else if (e.getSource() == overlayCheckBox)  
		{
    	  	Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
    	  	if (exp != null) 
    	  	{
	  			if (overlayCheckBox.isSelected()) 
	  			{
					if (overlayThreshold == null)
						overlayThreshold = new OverlayThreshold(exp.seqCamData);
					exp.seqCamData.seq.addOverlay(overlayThreshold);
					updateOverlay(exp);
				}
				else
					removeOverlay(exp);
    	  	}
		}
	}

	private void createROIsFromSelectedPolygon(Experiment exp) 
	{
		exp.cages.removeAllRoiCagesFromSequence(exp.seqCamData);
		int t = exp.seqCamData.currentFrame;
		IcyBufferedImage img0 = IcyBufferedImageUtil.convertToType(
				overlayThreshold.getTransformedImage(t), 
				DataType.INT, 
				false);
		Rectangle rectGrid = new Rectangle(0,0, img0.getSizeX(), img0.getSizeY());
		Blobs blobs = new Blobs(img0);
		blobs.getPixelsConnected ();
		blobs.getBlobsConnected();
		blobs.fillBlanksPixelsWithinBlobs ();
	
		List<Integer> blobsfound = new ArrayList<Integer> ();
		for (Capillary cap : exp.capillaries.capillariesList) 
		{
			Point2D pt = cap.getCapillaryROILowestPoint();
			if (pt != null) 
			{
				int ix = (int) (pt.getX() - rectGrid.x);
				int iy = (int) (pt.getY() - rectGrid.y);
				int blobi = blobs.getBlobAt(ix, iy);
				boolean found = false;
				for (int i: blobsfound) 
				{
					if (i == blobi) 
					{
						found = true;
						break;
					}
				}
				if (!found) 
				{
					blobsfound.add(blobi);
					ROI2DPolygon roiP = new ROI2DPolygon (blobs.getBlobPolygon2D(blobi));
					roiP.translate(rectGrid.x, rectGrid.y);
					int cagenb = cap.getCageIndexFromRoiName();
					roiP.setName("cage" + String.format("%03d", cagenb));
					cap.capCageID = cagenb;
					exp.seqCamData.seq.addROI(roiP);
				}
			}
		}
	}
		
	void deletePointsIncluded(Experiment exp) throws InterruptedException 
	{
		SequenceCamData seqCamData = exp.seqCamData;
		ROI2D roiSnip = seqCamData.seq.getSelectedROI2D();
		if (roiSnip == null)
			return;
		
		List <ROI2D> roiList = ROI2DUtilities.getROIs2DContainingString("cage", seqCamData.seq);
		for (ROI2D cageRoi: roiList) 
		{
			if (roiSnip.intersects(cageRoi) && cageRoi instanceof ROI2DPolygon) 
			{
				Polygon2D oldPolygon = ((ROI2DPolygon) cageRoi).getPolygon2D();
				if (oldPolygon == null)
					continue;
				Polygon2D newPolygon = new Polygon2D();
				for (int i = 0; i < oldPolygon.npoints; i++) 
				{
					if (roiSnip.contains(oldPolygon.xpoints[i], oldPolygon.ypoints[i]))
						continue;
					newPolygon.addPoint(oldPolygon.xpoints[i], oldPolygon.ypoints[i]);
				}
				((ROI2DPolygon)cageRoi).setPolygon2D(newPolygon);
			}
		}
	}
	
}

