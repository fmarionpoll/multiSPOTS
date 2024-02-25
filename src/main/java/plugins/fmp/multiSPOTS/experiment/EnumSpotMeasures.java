package plugins.fmp.multiSPOTS.experiment;



public enum EnumSpotMeasures
{
	SPOTS_DESCRIPTION("DESCRIPTION", "experiment description fields"),
	SPOTS_ARRAY("SPOTS", "array of spots"),
	SPOTS_MEASURES("MEASURES", "spot measures"),

	AREA_SUM("AREA_SUM", "sum of pixels over threshold"),
	AREA_SUMSQ("AREA_SUMSQ", "square of sum pixels over threshold"),
	AREA_CNTPIX("AREA_CNTPIX", "n pixels over threshold"),
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
