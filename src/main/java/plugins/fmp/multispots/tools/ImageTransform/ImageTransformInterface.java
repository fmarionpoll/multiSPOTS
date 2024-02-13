package plugins.fmp.multispots.tools.ImageTransform;

import icy.image.IcyBufferedImage;

public interface ImageTransformInterface 
{
	public IcyBufferedImage getTransformedImage (IcyBufferedImage sourceImage, ImageTransformOptions options);
}
