package dataTypes.contentValueRepresentations;

import dataTypes.minor.Pair;
import settings.GlobalSettings;

public class BooleanOrVariable extends AbstractContentValue
{
	public static final String typeName = "Bool";

	boolean value;
	
	public BooleanOrVariable()
	{
		super();
		typeStr = typeName;
	}
	public BooleanOrVariable(String argData)
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
		if (val instanceof Boolean)
		{
			value = (Boolean) val;
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
		if (newStr.toLowerCase().equals("true") ||
			newStr.toLowerCase().equals("yes") ||
			newStr.toLowerCase().equals("1"))
		{
			hasContent = true;
			value = true;
			res = new Pair<Boolean, String>(true, "Value accepted ('True').\n" + GlobalSettings.variableDescriptionText);
		}
		else
		if (newStr.toLowerCase().equals("false") ||
			newStr.toLowerCase().equals("no") ||
			newStr.toLowerCase().equals("0"))
		{
			hasContent = true;
			value = false;
			res = new Pair<Boolean, String>(true, "Value accepted ('False').\n" + GlobalSettings.variableDescriptionText);
		}
		
		
		if (res == null)
			res = new Pair<Boolean, String>(false, "Value not allowed.\n\nPlease type 'True' or 'False' or simply '1' or '0' or use a variable (starting with '" + GlobalSettings.varSymbol + "').");
		
		
		updateDisplayString();
		
		
		return(res);
	}

	@Override
	protected void updateDisplayString()
	{
		if (needDisplayString())
			displayString = value ? "True" : "False";
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
		return(new BooleanOrVariable(displayString));
	}
	
	

}