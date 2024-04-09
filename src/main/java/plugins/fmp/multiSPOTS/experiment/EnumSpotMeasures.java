package plugins.fmp.multiSPOTS.experiment;



public enum EnumSpotMeasures
{
	SPOTS_DESCRIPTION("DESCRIPTION", "experiment description fields"),
	SPOTS_ARRAY("SPOTS", "array of spots"),
	SPOTS_MEASURES("MEASURES", "spot measures"),

	AREA_SUM("AREA_SUM", "sum grey over threshold"),
	AREA_SUMCLEAN("AREA_SUMCLEAN", "sum grey values of pixels over threshold with no fly"),
	AREA_FLYPRESENT("AREA_FLYPRESENT", "fly is present or not over the spot"),
	AREA_CNTPIX("AREA_CNTPIX", "pixels over threshold"),
//	AREA_SUM2("AREA_SUM2", "sum2 grey over threshold"),
//	AREA_MEANGREY("AREA_MEANGREY", "mean grey value of pixels over threshold"),
	ALL ("ALL", "all options");
	
	private String label;
	private String unit;
	
	EnumSpotMeasures (String label, String unit) 
	{ 
		this.label = label;
		this.unit = unit;
	}
	
	public String toString() 
	{ 
		return label;
	}
	
	public String toUnit() 
	{
		return unit;
	}
	
	public static EnumSpotMeasures findByText(String abbr)
	{
	    for(EnumSpotMeasures v : values()) 
	    { 
	    	if( v.toString().equals(abbr)) 
	    		return v;   
    	}
	    return null;
	}
}
