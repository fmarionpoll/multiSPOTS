package plugins.fmp.multispots.workinprogress_gpu;


public enum EnumCLFunction 
{ 
	MULTIPLY2ARRAYS ("Multiply2Arrays");
	
	private String label;
	
	EnumCLFunction (String label) 
	{ 
		this.label = label;
	}
	
	public String toString() 
	{ 
		return label;
	}	
	
	public static EnumCLFunction findByText(String abbr)
	{
	    for(EnumCLFunction v : values()) 
	    	if( v.toString().equals(abbr))  
	    		return v;   
	    return null;
	}

}
