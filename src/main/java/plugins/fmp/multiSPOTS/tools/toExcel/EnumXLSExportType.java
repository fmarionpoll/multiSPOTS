package plugins.fmp.multiSPOTS.tools.toExcel;

public enum EnumXLSExportType 
{
	TOPRAW ("topraw", "volume (ul)"),
	TOPLEVEL ("toplevel", "volume (ul)"),
	BOTTOMLEVEL ("bottomlevel", "volume (ul)"), 
	DERIVEDVALUES ("derivative", "volume (ul)"), 
	
	TOPLEVEL_LR ("toplevel_L+R", "volume (ul)"), 
	TOPLEVELDELTA ("topdelta", "volume (ul)"),
	TOPLEVELDELTA_LR ("topdelta_L+R", "volume (ul)"),
	
	SUMGULPS ("sumGulps", "volume (ul)"), 
	SUMGULPS_LR ("sumGulps_L+R", "volume (ul)"), 
	NBGULPS ("nbGulps", "volume (ul)"),
	AMPLITUDEGULPS ("amplitudeGulps", "volume (ul)"),
	TTOGULP("tToGulp", "minutes"),
	TTOGULP_LR("tToGulp_LR", "minutes"),
	
	AUTOCORREL("autocorrel", "n observ"),
	AUTOCORREL_LR("autocorrel_LR", "n observ"),
	CROSSCORREL("crosscorrel", "n observ"),
	CROSSCORREL_LR("crosscorrel_LR", "n observ"),
	
	XYIMAGE ("xy-image", "mm"), 
	XYTOPCAGE ("xy-topcage", "mm"), 
	XYTIPCAPS ("xy-tipcaps", "mm"), 
	ELLIPSEAXES ("ellipse-axes", "mm"),
	DISTANCE ("distance", "mm"), 
	ISALIVE ("_alive", "yes/no"), 
	SLEEP ("sleep", "yes, no"),
	
	AREA_SUM("AREA_SUM", "sum of pixels over threshold"),
	AREA_SUMSQ("AREA_SUMSQ", "square of sum pixels over threshold"),
	AREA_CNTPIX("AREA_CNTPIX", "n pixels over threshold");
	
//	AREA_NPIXELS("area_npixels", "n_pixels"),
//	AREA_NPIXELS_DELTA("area_delta", "n pixels difference between intervals"),
//	AREA_NPIXELS_RELATIVE("area_rel", "%initial surface"),
//	AREA_NPIXELS_LR ("area_L+R", "n_pixels"),
//	AREA_DENSITY("area_density", "grey density in detection area");
	
	private String label;
	private String title;
	
	EnumXLSExportType (String label, String unit) 
	{ 
		this.label = label;
		this.title = unit;
	}
	
	public String toString() 
	{ 
		return label;
	}
	
	public String toTitle() 
	{
		return title;
	}
	
	public static EnumXLSExportType findByText(String abbr)
	{
	    for(EnumXLSExportType v : values()) 
	    { 
	    	if( v.toString().equals(abbr)) 
	    		return v;   
    	}
	    return null;
	}
}
