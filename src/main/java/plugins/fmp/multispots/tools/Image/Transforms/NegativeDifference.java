package plugins.fmp.multispots.tools.Image.Transforms;

import icy.image.IcyBufferedImage;
import icy.type.collection.array.Array1DUtil;
import plugins.fmp.multispots.tools.Image.ImageTransformFunctionAbstract;
import plugins.fmp.multispots.tools.Image.ImageTransformInterface;
import plugins.fmp.multispots.tools.Image.ImageTransformOptions;

public class NegativeDifference extends ImageTransformFunctionAbstract implements ImageTransformInterface
{
	@Override
	public IcyBufferedImage getTransformedImage(IcyBufferedImage sourceImage, ImageTransformOptions options) 
	{
		if (options.backgroundImage == null)
			return null;
		
		IcyBufferedImage img2 = new IcyBufferedImage(sourceImage.getSizeX(), sourceImage.getSizeY(),sourceImage.getSizeC(), sourceImage.getDataType_());
		
		for (int c = 0; c < sourceImage.getSizeC(); c++) 
		{
//			boolean changed = false;
			int [] imgSourceInt = Array1DUtil.arrayToIntArray(sourceImage.getDataXY(0), sourceImage.isSignedDataType());
			int [] img2Int = Array1DUtil.arrayToIntArray(img2.getDataXY(0), img2.isSignedDataType());
			int [] imgReferenceInt = Array1DUtil.arrayToIntArray(options.backgroundImage.getDataXY(c), options.backgroundImage.isSignedDataType());	
			for (int i=0; i< imgSourceInt.length; i++) 
			{
				int val = imgSourceInt[i] - imgReferenceInt[i];
				if (val < options.simplethreshold) 
				{
					img2Int[i] = 0xff;
				}
				else 
				{
					img2Int[i] = 0;
//					changed = true;
//					imgReferenceInt[i] = imgSourceInt[i];
				}
			}
			Array1DUtil.intArrayToSafeArray(img2Int, img2.getDataXY(c), true, img2.isSignedDataType());
			img2.setDataXY(c, img2.getDataXY(c));
//			if (changed) 
//			{
//				Array1DUtil.intArrayToSafeArray(imgReferenceInt, options.referenceImage.getDataXY(c), true, options.referenceImage.isSignedDataType());
//				options.referenceImage.setDataXY(c, options.referenceImage.getDataXY(c));
//			}
		}
		return img2;
	}

}
