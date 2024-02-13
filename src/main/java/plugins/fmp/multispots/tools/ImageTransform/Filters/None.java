package plugins.fmp.multispots.tools.ImageTransform.Filters;

import icy.image.IcyBufferedImage;
import plugins.fmp.multispots.tools.ImageTransform.ImageTransformFunctionAbstract;
import plugins.fmp.multispots.tools.ImageTransform.ImageTransformInterface;
import plugins.fmp.multispots.tools.ImageTransform.ImageTransformOptions;

public class None extends ImageTransformFunctionAbstract implements ImageTransformInterface
{
	@Override
	public IcyBufferedImage getTransformedImage(IcyBufferedImage sourceImage, ImageTransformOptions options) 
	{
		return sourceImage;
	}


}
