package plugins.fmp.multiSPOTS.tools.toExcel;

public enum EnumXLSExportType 
{
	TOPRAW ("topraw", "volume (ul)", "capillary level"),
	TOPLEVEL ("toplevel", "volume (ul)", "capillary top level"),
	BOTTOMLEVEL ("bottomlevel", "volume (ul)", "capillary bottom level"), 
	DERIVEDVALUES ("derivative", "volume (ul)", "derivative of capillary level (tn - tn-1)"), 
	
	TOPLEVEL_LR ("toplevel_L+R", "volume (ul)", "sum of consumption of left and right capillaries"), 
	TOPLEVELDELTA ("topdelta", "volume (ul)", "delta value of top level"),
	TOPLEVELDELTA_LR ("topdelta_L+R", "volume (ul)", "difference of total consumtion (tn - tn-1)"),

	AUTOCORREL("autocorrel", "n observ", "autocorrelation"),
	AUTOCORREL_LR("autocorrel_LR", "n observ", "autocorrelation of sum L+R"),
	CROSSCORREL("crosscorrel", "n observ", "cross-correlation"),
	CROSSCORREL_LR("crosscorrel_LR", "n observ", "cross-correlation L+R"),
	
	XYIMAGE ("xy-image", "mm", "xy image"), 
	XYTOPCAGE ("xy-topcage", "mm", "xy top cage"), 
	XYTIPCAPS ("xy-tipcaps", "mm", "xy tip capillaries"), 
	ELLIPSEAXES ("ellipse-axes", "mm", "ellipse of axes"),
	DISTANCE ("distance", "mm", "distance between consecutive points"), 
	ISALIVE ("_alive", "yes/no", "fly alive or not"), 
	SLEEP ("sleep", "yes, no", "fly sleeping"),
	
	AREA_SUM("AREA_SUM", "grey value", "sum of pixels over threshold"),
	AREA_SUM2("AREA_SUM2", "grey value*2", "square of sum pixels over threshold"),
	AREA_CNTPIX("AREA_CNTPIX", "n pixels", "n pixels over threshold"),
	AREA_MEANGREY("AREA_MEANGREY", "total grey value / n pixels", "average grey value of pixels over threshold"),;
	
//	AREA_NPIXELS("area_npixels", "n_pixels"),
//	AREA_NPIXELS_DELTA("area_delta", "n pixels difference between intervals"),
//	AREA_NPIXELS_RELATIVE("area_rel", "%initial surface"),
//	AREA_NPIXELS_LR ("area_L+R", "n_pixels"),
//	AREA_DENSITY("area_density", "grey density in detection area");
	
	private String label;
	private String unit;
	private String title;
	
	EnumXLSExportType (String label, String unit, String title) 
	{ 
		this.label = label;
		this.unit = unit;
		this.title = title;
	}
	
	public String toString() 
	{ 
		return label;
	}
	
	public String toUnit() 
	{
		return unit;
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
