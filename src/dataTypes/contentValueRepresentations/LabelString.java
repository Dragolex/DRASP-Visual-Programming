package dataTypes.contentValueRepresentations;

import dataTypes.minor.Pair;
import dataTypes.specialContentValues.FuncLabel;
import execution.handlers.LabelHandler;
import settings.GlobalSettings;

public class LabelString extends AbstractContentValue
{
	public static final String typeName = "Label";
	
	String tempLabelName = null;
	FuncLabel value;
	
	public LabelString()
	{
		super();
		typeStr = typeName;
	}
	public LabelString(String argData)
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
		if (val instanceof FuncLabel)
		{
			tempLabelName = ((FuncLabel) val).getName();
			
			value = (FuncLabel) val;
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
		if (!newStr.isEmpty())
		if (!newStr.startsWith(GlobalSettings.varSymbol))
		{
			hasContent = true; 
			tempLabelName = newStr;
			
			res = new Pair<Boolean, String>(true, "Value accepted ('" + newStr + "').");
		}
			
		if (res == null)
			res = new Pair<Boolean, String>(false, "Value not allowed.\n\nPlease type a text.\nAlternatively use a variable (starting with '" + GlobalSettings.varSymbol + "').");

		
		updateDisplayString();
		
		
		return(res);
	}
	
	@Override
	protected void updateDisplayString()
	{
		if (needDisplayString())
		{
			//LabelHandler.getSelf().registerLabel(displayString, value);
			displayString = tempLabelName;
		}
	}
	
	
	
	@Override
	protected Object getArgumentValue()
	{
		if ((value == null) || !value.getName().equals(tempLabelName)) 
			value = LabelHandler.getOrCreate(tempLabelName);
		return(value);
	}
	
	
	@Override
	public AbstractContentValue clone()
	{
		updateDisplayString();
		return(new LabelString(displayString));
	}

}