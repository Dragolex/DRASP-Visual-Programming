package dataTypes.contentValueRepresentations;

import dataTypes.minor.Pair;
import settings.GlobalSettings;

public class ValueOrVariable extends AbstractContentValue
{
	public static final String typeName = "Number";

	Double value;
	
	public ValueOrVariable()
	{
		super();
		typeStr = typeName;
	}
	public ValueOrVariable(String argData)
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
		if (val instanceof Double)
		{
			value = (Double) val;
			hasContent = true;
			updateDisplayString();
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
			newStr = newStr.replaceAll(",", ".");
			try
			{
				value = Double.parseDouble(newStr); // try to parse
				hasContent = true;
				res = new Pair<Boolean, String>(true, "Value accepted ('" + value + "').\n" + GlobalSettings.variableDescriptionText);
			}
			catch(NumberFormatException e)
			{ value = null; res = null; }
		}
		
		if (res == null)
			res = new Pair<Boolean, String>(false, "Input not allowed.\nPlease type a number with '.' or ',' as a decimal symbol.\nAlternatively use a variable (starting with '" + GlobalSettings.varSymbol + "').");
		
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
		return(new ValueOrVariable(displayString));
	}
	
}
