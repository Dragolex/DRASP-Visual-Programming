package dataTypes.contentValueRepresentations;

import dataTypes.minor.Pair;
import settings.GlobalSettings;

public class TextOrValueOrVariable extends AbstractContentValue
{
	public static final String typeName = "Text";

	Object value;
	
	public TextOrValueOrVariable()
	{
		super();
		typeStr = typeName;
	}
	public TextOrValueOrVariable(String argData)
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
		{
		if (val instanceof String)
		{
			value = (String) val;
			hasContent = true;
			updateDisplayString();
		}
		else
		if ((val instanceof Double))// || (val instanceof Integer) || (val instanceof Float))
		{
			value = (Double) (double) val;
			hasContent = true;
			updateDisplayString();
		}
		}
	}
	
	@Override
	public Pair<Boolean, String> checkAndSetFromString(String newStr)
	{
		hasContent = false;
		hasVariable = false;
		
		
		Pair<Boolean, String> res = checkForVariable(newStr);

		if (res == null)
		{
			try
			{
				value = Double.parseDouble(newStr.replaceAll(",", ".")); // try to parse
				hasContent = true;
				res = new Pair<Boolean, String>(true, "Value accepted ('" + value + "').\n" + GlobalSettings.variableDescriptionText);
			}
			catch(NumberFormatException e)
			{ value = null; res = null; }
		}
		
		
		if (res == null)
		if (!newStr.isEmpty())
		if (!newStr.startsWith(GlobalSettings.varSymbol))		
		{
			hasContent = true;
			value = newStr;
			res = new Pair<Boolean, String>(true, "Value accepted ('" + newStr + "').");
		}
		
		if (res == null)
			res = new Pair<Boolean, String>(false, "Value not allowed.\n\n Please type a number with '.' or ',' as a decimal symbol or type a text NOT starting with following symbol '" + GlobalSettings.varSymbol + "', unless you want to use a variable.");
		
		updateDisplayString();
		
		
		return(res);
	}
	
	
	@Override
	protected void updateDisplayString()
	{
		if (needDisplayString())
			displayString = value.toString();
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
		return(new TextOrValueOrVariable(displayString));
	}

}