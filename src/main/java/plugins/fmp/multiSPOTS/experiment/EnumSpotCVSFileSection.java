package plugins.fmp.multiSPOTS.experiment;



public enum EnumSpotCVSFileSection
{
	SPOTS_DESCRIPTION("DESCRIPTION", "experiment description fields"),
	SPOTS_MEASURES("MEASURES", "spot measures"),
	SPOTS_ARRAY("SPOTS", "array of spots"),
	ALL ("ALL", "all options");
	
	private String label;
	private String unit;
	
	EnumSpotCVSFileSection (String label, String unit) 
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
	
	public static EnumSpotCVSFileSection findByText(String abbr)
	{
	    for(EnumSpotCVSFileSection v : values()) 
	    { 
	    	if( v.toString().equals(abbr)) 
	    		return v;   
    	}
	    return null;
	}
}
