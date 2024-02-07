package plugins.fmp.multispots.tools.Image.Transforms;

import icy.image.IcyBufferedImage;
import plugins.fmp.multispots.tools.Image.ImageTransformFunctionAbstract;
import plugins.fmp.multispots.tools.Image.ImageTransformInterface;
import plugins.fmp.multispots.tools.Image.ImageTransformOptions;

public class None extends ImageTransformFunctionAbstract implements ImageTransformInterface
{
	@Override
	public IcyBufferedImage getTransformedImage(IcyBufferedImage sourceImage, ImageTransformOptions options) 
	{
		return sourceImage;
	}


}
