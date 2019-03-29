package main.electronic.attributes;

import javafx.scene.paint.Color;

public abstract class ComponentAttribute {
	
	Class<?> type = null;
	Object value;
	String unit;
	float baseFactor = 1;
	

	/*
	public ComponentAttribute(Object value)
	{
		type = value.getClass();
		this.value = value;
		this.unit = "";
	}
	*/
	public ComponentAttribute(Object value, String unit)
	{
		type = value.getClass();
		this.value = value;
		this.unit = unit;
		this.baseFactor = 1;
	}
	public ComponentAttribute(Object value, String unit, float baseFactor)
	{
		type = value.getClass();
		this.value = value;
		this.unit = unit;
		this.baseFactor = baseFactor;
	}
	
	public Object get()
	{
		return(value);
	}

	public String getUnit()
	{
		return(unit);
	}
	
	public boolean isTextType()
	{
		return(type.equals("".getClass()));
	}

	
	public void set(Object value)
	{		
		this.value = type.cast(value);
	}

	
	@Override
	public String toString()
	{
		return(""+type+"|"+value+"|"+unit); // TODO better!
	}
	
	
	static public ComponentAttribute fromString(String str)
	{
		String[] inputs = str.split("\\|");
		
		switch(inputs[0])
		{
		case "Double": // TODO: Check whether those types are correct!
			return(new BigDecimalAttribute(Double.valueOf(inputs[1]), inputs[2]));
		
		case "String":
			return(new TextAttribute(String.valueOf(inputs[1])));

		case "Color":
			return(new ColorAttribute(Color.valueOf(inputs[1])));

		case "null": return(null);
		
		default: System.out.println("TYPE DOES NOT EXIST: " + inputs[0]);
		}
		
		return(null);
	}
	
	protected abstract String getAsStr();
	
	public String getAsText()
	{
		return(getAsStr()+unit);
	}

	
	
}
