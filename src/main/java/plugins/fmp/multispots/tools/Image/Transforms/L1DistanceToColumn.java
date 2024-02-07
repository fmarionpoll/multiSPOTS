package plugins.fmp.multispots.tools.Image.Transforms;

import icy.image.IcyBufferedImage;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import plugins.fmp.multispots.tools.Image.ImageTransformFunctionAbstract;
import plugins.fmp.multispots.tools.Image.ImageTransformInterface;
import plugins.fmp.multispots.tools.Image.ImageTransformOptions;

public class L1DistanceToColumn extends ImageTransformFunctionAbstract implements ImageTransformInterface
{
	int column = 0;
	
	public L1DistanceToColumn(int column)
	{
		this.column = column;
	}
	
	@Override
	public IcyBufferedImage getTransformedImage(IcyBufferedImage sourceImage, ImageTransformOptions options) 
	{
		int imageSizeX = sourceImage.getSizeX();
		int imageSizeY = sourceImage.getSizeY();
		
		IcyBufferedImage img2 = new IcyBufferedImage (sourceImage.getWidth(), sourceImage.getHeight(), 3, sourceImage.getDataType_());
		int[] R = Array1DUtil.arrayToIntArray(sourceImage.getDataXY(0), sourceImage.isSignedDataType());
		int[] G = Array1DUtil.arrayToIntArray(sourceImage.getDataXY(1), sourceImage.isSignedDataType());
		int[] B = Array1DUtil.arrayToIntArray(sourceImage.getDataXY(2), sourceImage.isSignedDataType());
		int[] ExG = (int[]) Array1DUtil.createArray(DataType.INT, R.length);
					
		for (int iy = 0; iy < imageSizeY; iy++) 
		{	
			int deltay = iy* imageSizeX;
			int kx0 = column + deltay;
			
			for (int ix = 0; ix < imageSizeX; ix++) 
			{
				int kx = ix + deltay;
				ExG [kx]  = Math.abs(R[kx] - R[kx0])  + Math.abs(G[kx] - G[kx0]) + Math.abs(B[kx] - B[kx0]);
			}
		}
		
		copyExGIntToIcyBufferedImage(ExG, img2);
		return img2;
	}
}
