package plugins.fmp.multiSPOTS.tools.ImageTransform;

import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.Deriche;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.H1H2H3;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.L1DistanceToColumn;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.LinearCombination;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.LinearCombinationNormed;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.NegativeDifference;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.None;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.RGBtoHSB;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.RGBtoHSV;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.RemoveHorizontalAverage;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.SortRedColumns;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.SortSumDiffColumns;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.SubtractColumn;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.SubtractReferenceImage;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.SumDiff;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.ThresholdColors;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.ThresholdSingleValue;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.XDiffn;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.XYDiffn;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.YDifferenceL;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.YDiffn;
import plugins.fmp.multiSPOTS.tools.ImageTransform.Filters.YDiffn1D;

public enum ImageTransformEnums 
{
	R_RGB			("R(RGB)", 					new LinearCombination(1, 0, 0)),
    G_RGB			("G(RGB)", 					new LinearCombination(0, 1, 0)),
    B_RGB			("B(RGB)", 					new LinearCombination(0, 0, 1)),
    R2MINUS_GB 		("2R-(G+B)", 				new LinearCombination(2, -1, -1)), 
	G2MINUS_RB		("2G-(R+B)", 				new LinearCombination(-1, 2, -1)), 
	B2MINUS_RG		("2B-(R+G)", 				new LinearCombination(-1, -1, 2)), 
	GBMINUS_2R 		("(G+B)-2R", 				new LinearCombination(-2, 1, 1)),  
	RBMINUS_2G		("(R+B)-2G", 				new LinearCombination(1, -2, 1)),  
	RGMINUS_2B		("(R+G)-2B", 				new LinearCombination(1, 1, -2)), 
	RGB_DIFFS		("S(diffRGB)", 				new SumDiff()),
	RGB 			("(R+G+B)/3", 				new LinearCombination(1/3, 1/3, 1/3)),
	H_HSB 			("H(HSB)", 					new RGBtoHSB(0)), 
	S_HSB 			("S(HSB)", 					new RGBtoHSB(1)), 
	B_HSB			("B(HSB)", 					new RGBtoHSB(2)),
	H_HSV 			("H(HSV)", 					new RGBtoHSV(0)), 
	S_HSV 			("S(HSV)", 					new RGBtoHSV(1)), 
	V_HSV			("B(HSV)", 					new RGBtoHSV(2)),
	XDIFFN			("XDiffn", 					new XDiffn(3)), 
	YDIFFN			("YDiffn", 					new YDiffn(5)), 
	YDIFFN2			("YDiffn_1D", 				new YDiffn1D(4)), 
	XYDIFFN			("XYDiffn", 				new XYDiffn(5)), 
	SUBTRACT_T0		("t-start",			 		new SubtractReferenceImage()), 
	SUBTRACT_TM1	("t-(t-1)", 				new SubtractReferenceImage()), 
	SUBTRACT_REF	("t-ref",		 			new SubtractReferenceImage()),
	SUBTRACT		("neg(t-ref)", 				new NegativeDifference()),
	NORM_BRMINUSG	("|aR+bG+cB|",				new LinearCombinationNormed(-1, 2, -1)),
	RGB_TO_H1H2H3	("H1H2H3", 					new H1H2H3()), 
	SUBTRACT_1RSTCOL("[t-t0]", 					new SubtractColumn(0)), 
	L1DIST_TO_1RSTCOL("L1[t-t0]", 				new L1DistanceToColumn(0)),
	COLORDISTANCE_L1_Y("color dist L1", 		new YDifferenceL(0, 0, 4, 0, false)), 
	COLORDISTANCE_L2_Y("color dist L2", 		new YDifferenceL(0, 0, 5, 0, true)),
	DERICHE			("edge detection", 			new Deriche(1., true)), 
	DERICHE_COLOR	("Deriche's edges", 		new Deriche(1., false)),
	MINUSHORIZAVG	("remove Hz traces", 		new RemoveHorizontalAverage()),
	THRESHOLD_SINGLE("threshold 1 value",		new ThresholdSingleValue()),
	THRESHOLD_COLORS("threshold colors",		new ThresholdColors()),
	SORT_REDCOLS	("sort red columns", 		new SortRedColumns()),
	SORT_SUMDIFFCOLS("sort cols / SumDiff",    	new SortSumDiffColumns()),
	ZIGZAG			("remove spikes",			new None()),
	NONE			("none",					new None());

	private ImageTransformInterface klass;
    private String label;
	
    ImageTransformEnums(String label, ImageTransformInterface klass ) 
	{ 
		this.label = label; 
		this.klass = klass;
	}
    
	public String toString() 
	{ 
		return label; 
	}
	
	public ImageTransformInterface getFunction() 
	{ 
		return klass; 
	}
	
	public static ImageTransformEnums findByText(String abbr)
	{
	    for(ImageTransformEnums v : values())
	    { 
	    	if ( v.toString().equals(abbr)) 
	    		return v;  
	    }
	    return null;
	}

}
