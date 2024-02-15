package plugins.fmp.multispots.experiment;



public enum EnumSpotMeasures 
{
	TOPLEVEL ("TOPLEVEL", "top capillary limit"), 
	BOTTOMLEVEL ("BOTTOMLEVEL", "bottom capillary limit"), 
	TOPDERIVATIVE ("TOPDERIVATIVE", "derivative of top capillary limit"), 
	GULPS ("GULPS", "gulps detected from derivative"), 
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
