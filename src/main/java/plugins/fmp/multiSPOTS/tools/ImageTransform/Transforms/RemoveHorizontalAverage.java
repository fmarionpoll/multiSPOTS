package plugins.fmp.multiSPOTS.tools.ImageTransform.Transforms;

import icy.image.IcyBufferedImage;
import icy.type.collection.array.Array1DUtil;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformFunctionAbstract;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformInterface;
import plugins.fmp.multiSPOTS.tools.ImageTransform.ImageTransformOptions;

public class RemoveHorizontalAverage extends ImageTransformFunctionAbstract implements ImageTransformInterface {
	@Override
	public IcyBufferedImage getTransformedImage(IcyBufferedImage sourceImage, ImageTransformOptions options) {
		IcyBufferedImage img2 = new IcyBufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), 3,
				sourceImage.getDataType_());
		int imgSizeX = sourceImage.getSizeX();
		int imgSizeY = sourceImage.getSizeY();

		int nchannels = sourceImage.getSizeC();
		for (int c = 0; c < nchannels; c++) {
			double[] Rn = Array1DUtil.arrayToDoubleArray(sourceImage.getDataXY(c), sourceImage.isSignedDataType());
			for (int iy = 0; iy < imgSizeY; iy++) {
				int iydelta = iy * imgSizeX;
				double sum = 0;
				for (int ix = 0; ix < imgSizeX; ix++) {
					sum += Rn[iydelta + ix];
				}
				double average = sum / imgSizeX;

				for (int ix = 0; ix < imgSizeX; ix++) {
					Rn[iydelta + ix] -= average;
				}
			}
			Array1DUtil.doubleArrayToSafeArray(Rn, img2.getDataXY(c), true);
			img2.setDataXY(c, img2.getDataXY(c));
		}
		return img2;
	}
}
