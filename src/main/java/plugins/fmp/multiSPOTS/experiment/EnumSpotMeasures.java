package plugins.fmp.multiSPOTS.experiment;



public enum EnumSpotMeasures
{
	SPOTS_DESCRIPTION("DESCRIPTION", "experiment description fields"),
	SPOTS_ARRAY("SPOTS", "array of spots"),
	SPOTS_MEASURES("MEASURES", "spot measures"),

	AREA_SUM("AREA_SUM", "sum grey over threshold"),
	AREA_SUMCLEAN("AREA_SUMCLEAN", "sum grey values of pixels over threshold with no fly"),
	AREA_FLYPRESENT("AREA_FLYPRESENT", "fly is present or not over the spot"),
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
