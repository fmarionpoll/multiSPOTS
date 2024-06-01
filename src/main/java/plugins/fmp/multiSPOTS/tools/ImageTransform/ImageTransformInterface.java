package plugins.fmp.multiSPOTS.tools.ImageTransform;

import icy.image.IcyBufferedImage;

public interface ImageTransformInterface {
	public IcyBufferedImage getTransformedImage(IcyBufferedImage sourceImage, ImageTransformOptions options);
}
