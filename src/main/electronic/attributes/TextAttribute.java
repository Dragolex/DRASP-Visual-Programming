package main.electronic.attributes;

public class TextAttribute extends ComponentAttribute {
	
	public TextAttribute(String text)
	{
		super(text, "");
	}
	
	public String get()
	{
		return((String) super.get());
	}
	
	@Override
	protected String getAsStr()
	{
		return((String) super.get());
	}
}