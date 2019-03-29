package dataTypes.contentValueRepresentations;

import dataTypes.minor.Pair;
import settings.GlobalSettings;

public class VariableOnly extends AbstractContentValue
{
	public static final String typeName = "Vari";

	String value;
	
	public VariableOnly()
	{
		super();
		typeStr = typeName;
	}
	
	public VariableOnly(boolean passingAsRealVariable, boolean canBeEditedByElement)
	{
		super();
		typeStr = typeName;
		setSpecial(passingAsRealVariable, canBeEditedByElement);
	}

	
	public VariableOnly(String argData)
	{
		super();
		typeStr = typeName;
		checkInitForString(argData);
		updateDisplayString();
	}
	public VariableOnly(String argData, boolean passingAsRealVariable, boolean canBeEditedByElement)
	{
		super();
		typeStr = typeName;
		checkInitForString(argData);
		updateDisplayString();
		setSpecial(passingAsRealVariable, canBeEditedByElement);
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
		
		Pair<Boolean, String> res = checkForVariable(newStr);
		
		if (res == null)
			res = new Pair<Boolean, String>(false, "Value not allowed.\n\nPlease type a text starting with '" + GlobalSettings.varSymbol + "' and consisting of letters, numbers or the underscore symbol.");
		
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
		return(new VariableOnly(displayString));
	}

	
}