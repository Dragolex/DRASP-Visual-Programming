package dataTypes.contentValueRepresentations;

import dataTypes.minor.Pair;
import settings.GlobalSettings;

public class TextOnly extends AbstractContentValue
{
	public static final String typeName = "TextOnly";

	String value;
	
	public TextOnly()
	{
		super();
		typeStr = typeName;
	}
	public TextOnly(String argData)
	{
		super();
		typeStr = typeName;
		checkInitForString(argData);
		updateDisplayString();
	}

	
	
	@Override
	public void checkAndSetForInit(Object val)
	{
		if (!checkInitForString(val))
		if (val instanceof String)
		{
			value = (String) val;
			hasContent = true;
			updateDisplayString();
		}
	}

	
	@Override
	public Pair<Boolean, String> checkAndSetFromString(String newStr)
	{
		hasContent = false;
		hasVariable = false;

		
		Pair<Boolean, String> res = null;

		if (res == null)
		if (!newStr.isEmpty())
		if (!newStr.startsWith(GlobalSettings.varSymbol))		
		{
			hasContent = true;
			value = newStr;
			res = new Pair<Boolean, String>(true, "Value accepted ('" + newStr + "').");
		}
			
		if (res == null)
			res = new Pair<Boolean, String>(false, "Value not allowed.\n\nPlease type a text NOT starting with following symbol '" + GlobalSettings.varSymbol + "'.");

		
		updateDisplayString();
		
		
		return(res);
	}

	
	
	

	@Override
	protected void updateDisplayString()
	{
		if (needDisplayString())
			displayString = value;
	}


	
	@Override
	protected Object getArgumentValue()
	{
		return(value);
	}
	
	@Override
	public AbstractContentValue clone()
	{
		updateDisplayString();
		return(new TextOnly(displayString));
	}

	
}