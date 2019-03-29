package otherHelpers;

public class SeparatableString {
	
	String separator;
	StringBuilder str = new StringBuilder();
	public SeparatableString(String separator)
	{
		this.separator = separator;
	}
	
	public void append(Object obj)
	{
		str.append(obj);
		str.append(separator);
	}
	
	public String toString()
	{
		str.delete(str.length()-separator.length()-1, str.length()-1);
		return(str.toString());
	}
	
	

}
