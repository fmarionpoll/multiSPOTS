package plugins.fmp.multispots.resource;

import java.awt.Image;
import java.io.InputStream;

import icy.image.ImageUtil;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import plugins.fmp.multispots.MultiSPOTS;



// adapted from NherveToolbox

public class ResourceUtilFMP {

    public static final String ALPHA_PATH 		= "alpha/";
    public static final String ICON_PATH 		= "icon/";

    
    public static final IcyIcon ICON_PREVIOUS_IMAGE 	= new IcyIcon(ResourceUtil.getAlphaIconAsImage("br_prev.png"));
    public static final IcyIcon ICON_NEXT_IMAGE  		= new IcyIcon(ResourceUtil.getAlphaIconAsImage("br_next.png"));
    public static final IcyIcon ICON_ADAPT_YAXIS  		= new IcyIcon(getImage("fit_Y.png"));
    public static final IcyIcon ICON_ADAPT_XAXIS  		= new IcyIcon(getImage("fit_X.png"));

     
    
	private static Image getImage(String fileName) 
	{
		String name = "plugins/fmp/multispots/" + ICON_PATH + ALPHA_PATH + fileName;
		InputStream url = MultiSPOTS.class.getClassLoader().getResourceAsStream(name);
		if (url == null) {
			System.out.println("ResourceUtilFMP:getImage resource not found: at: "+ name);
		}
		return ImageUtil.load(url);
	}
	
}
