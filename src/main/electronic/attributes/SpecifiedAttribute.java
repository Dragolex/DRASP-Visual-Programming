package main.electronic.attributes;

public class SpecifiedAttribute {
	
	String name;
	String description;
	ComponentAttribute attribute;
	
	boolean externallyWritten = false;
	public double locatedAtX;
	public double locatedAtY;
	public int fontSize = 10;
	public int rotation = 0;
	
	public SpecifiedAttribute(String name, String description, ComponentAttribute attribute)
	{
		this.name = name;
		this.description = description;
		this.attribute = attribute;
	}
	public SpecifiedAttribute(String name, String description, ComponentAttribute attribute, double locatedAtX, double locatedAtY, int fontSize, int rotation)
	{
		this.name = name;
		this.description = description;
		this.attribute = attribute;
		
		this.locatedAtX = locatedAtX;
		this.locatedAtY = locatedAtY;
		this.fontSize = fontSize;
		this.rotation = rotation;
		
		externallyWritten = true;
	}

	public void replaceAttribute(ComponentAttribute newAttribute)
	{
		attribute = newAttribute;
	}
	
	public String getName()
	{
		return(name);
	}
	public String getDescription()
	{
		return(description);
	}
	
	public ComponentAttribute getAttribute()
	{
		return(attribute);
	}
	
	public boolean isExternallyWritten()
	{
		return(externallyWritten);
	}


}
