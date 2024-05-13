package plugins.fmp.multiSPOTS.tools.ImageTransform.Transforms;

import icy.image.IcyBufferedImage;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformFunctionAbstract;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformInterface;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformOptions;

public class None extends ImageTransformFunctionAbstract implements ImageTransformInterface
{
	@Override
	public IcyBufferedImage getTransformedImage(IcyBufferedImage sourceImage, ImageTransformOptions options) 
	{
		return sourceImage;
	}


}
