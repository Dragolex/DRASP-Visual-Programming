package main.electronic.attributes;

import javafx.scene.paint.Color;

public class ColorAttribute extends ComponentAttribute {
	
	public ColorAttribute(Color value)
	{
		super(value, null);
	}
	
	public Color get()
	{
		return((Color) super.get());
	}
	

	@Override
	protected String getAsStr()
	{
		return(String.valueOf(super.get()));
	}
	
}
