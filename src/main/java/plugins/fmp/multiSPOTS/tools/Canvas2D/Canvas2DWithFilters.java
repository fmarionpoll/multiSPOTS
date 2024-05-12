package plugins.fmp.multiSPOTS.tools.Canvas2D;


import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JToolBar;

import icy.canvas.Canvas2D;
import icy.gui.component.button.IcyButton;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import plugins.fmp.multiSPOTS.resource.ResourceUtilFMP;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformEnums;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformInterface;
import icy.resource.icon.IcyIcon;


public class Canvas2DWithFilters extends Canvas2D
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 8827595503996677250L;
	public ImageTransformEnums[] imageTransform1 = new ImageTransformEnums[] {ImageTransformEnums.NONE,
			ImageTransformEnums.R_RGB, ImageTransformEnums.G_RGB, ImageTransformEnums.B_RGB, 
			ImageTransformEnums.R2MINUS_GB, ImageTransformEnums.G2MINUS_RB, ImageTransformEnums.B2MINUS_RG, ImageTransformEnums.RGB,
			ImageTransformEnums.GBMINUS_2R, ImageTransformEnums.RBMINUS_2G, ImageTransformEnums.RGMINUS_2B, ImageTransformEnums.RGB_DIFFS,
			ImageTransformEnums.H_HSB, ImageTransformEnums.S_HSB, ImageTransformEnums.B_HSB
			};
	public JComboBox<ImageTransformEnums> imageTransformFunctionsCombo1 = new JComboBox<ImageTransformEnums> (imageTransform1);
	ImageTransformInterface transform1 = ImageTransformEnums.NONE.getFunction();
 
	public ImageTransformEnums[] imageTransform2 = new ImageTransformEnums[] {ImageTransformEnums.NONE,
			ImageTransformEnums.SORT_SUMDIFFCOLS,
			ImageTransformEnums.SORT_REDCOLS
			};
	public JComboBox<ImageTransformEnums> imageTransformFunctionsCombo2 = new JComboBox<ImageTransformEnums> (imageTransform2);
	ImageTransformInterface transform2 = ImageTransformEnums.NONE.getFunction();
	
    public Canvas2DWithFilters(Viewer viewer)
    {
        super(viewer);
    }
    
    @Override
    public void customizeToolbar(JToolBar toolBar)
    {
    	for (int i = 3; i >= 0; i--)
    		toolBar.remove(i);
    	toolBar.addSeparator();
        toolBar.add(imageTransformFunctionsCombo1);

		IcyIcon fitY = ResourceUtilFMP.ICON_FIT_YAXIS;
		IcyButton fitYAxisButton = new IcyButton(fitY);
		fitYAxisButton.setSelected(false);
		fitYAxisButton.setFocusable(false);
		fitYAxisButton.setToolTipText("Set image scale ratio to 1:1 and fit Y axis to the window height");
		toolBar.add(fitYAxisButton);
		
		IcyIcon fitX = ResourceUtilFMP.ICON_FIT_XAXIS;
		IcyButton fitXAxisButton = new IcyButton(fitX);
		fitXAxisButton.setSelected(false);
		fitXAxisButton.setFocusable(false);
		fitXAxisButton.setToolTipText("Fit X and Y axis to the window size");
		toolBar.add(fitXAxisButton);
        
		super.customizeToolbar(toolBar);
       
        fitYAxisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	zoomImage_1_1();
            }});
        
        fitXAxisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	shrinkImage_to_fit() ;
            }});
        
        imageTransformFunctionsCombo1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	ImageTransformEnums transformEnum = (ImageTransformEnums) imageTransformFunctionsCombo1.getSelectedItem();
            	transform1 = transformEnum.getFunction();
            	refresh();
            }});
        
        imageTransformFunctionsCombo1.setSelectedIndex(0);
        refresh();
    }   	        
    
    void zoomImage_1_1() 
	{
		Sequence seqKymograph = getSequence();
		Rectangle rectImage = seqKymograph.getBounds2D();
		Rectangle rectCanvas = getCanvasVisibleRect();
		
		int offsetX = (int) (rectCanvas.width / getScaleX() / 2); 
		double scaleY = rectCanvas.getHeight() / rectImage.getHeight();;  
		double scaleX = scaleY; 
		setMouseImagePos(offsetX, rectImage.height  / 2);
		setScale(scaleX, scaleY, true, true);
	}
    
    void shrinkImage_to_fit() 
	{
		Sequence seqKymograph = getSequence();
		Rectangle rectImage = seqKymograph.getBounds2D();
		Rectangle rectCanvas = getCanvasVisibleRect();
		
		double scaleX = rectCanvas.getWidth() / rectImage.getWidth(); 
		double scaleY = rectCanvas.getHeight() / rectImage.getHeight();
		setMouseImagePos(rectImage.width/2, rectImage.height/ 2);
		setScale(scaleX, scaleY, true, true);
	}
    
    @Override
    public IcyBufferedImage getImage(int t, int z, int c)
    {
    	IcyBufferedImage img = super.getImage(t, z, c);
    	IcyBufferedImage img2 = transform1.getTransformedImage (img, null);
        return img2;
    }
    
    public void updateListOfImageTransformFunctions(ImageTransformEnums[] transformArray) 
    {
        // remove listeners
        ActionListener[] listeners = imageTransformFunctionsCombo1.getActionListeners();
        for (int i = 0; i < listeners.length; i++)
        	imageTransformFunctionsCombo1.removeActionListener(listeners[i]);

        if (imageTransformFunctionsCombo1.getItemCount() > 0)
        	imageTransformFunctionsCombo1.removeAllItems();

        // add contents
        imageTransformFunctionsCombo1.addItem(ImageTransformEnums.NONE);
        for (int i = 0; i < transformArray.length; i++) {
        	imageTransformFunctionsCombo1.addItem(transformArray[i]);
        }

        // restore listeners
        for (int i = 0; i < listeners.length; i++)
        	imageTransformFunctionsCombo1.addActionListener(listeners[i]);
    }
    
    public void selectImageTransformFunction(int iselected) 
    {
		imageTransformFunctionsCombo1.setSelectedIndex(iselected);
    }
  
    public void addButtons(JToolBar toolBar)
    {
    	toolBar.addSeparator();
        
		IcyButton previousButton = new IcyButton(ResourceUtilFMP.ICON_PREVIOUS_IMAGE);
		previousButton.setSelected(false);
		previousButton.setFocusable(false);
		previousButton.setToolTipText("Previous");
        toolBar.add(previousButton, 0); 
		
        IcyButton nextButton = new IcyButton(ResourceUtilFMP.ICON_NEXT_IMAGE);
        nextButton.setSelected(false);
        nextButton.setFocusable(false);
        nextButton.setToolTipText("Next");
		toolBar.add(nextButton, 1);
		
		super.customizeToolbar(toolBar);
        
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setPositionT( getPositionT()-1);
            }});
        
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	setPositionT( getPositionT()+1);
            }});
        
        toolBar.add(imageTransformFunctionsCombo2, 5);
    }  
 	
}
