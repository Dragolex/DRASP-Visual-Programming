package dataTypes.contentValueRepresentations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataTypes.minor.Pair;
import settings.GlobalSettings;

public class SelectableType extends AbstractContentValue
{
	public static final String typeName = "SelectableType";

	List<String> valueList;
	private String value;
	int valueInd = -1;
	
	public SelectableType()
	{
		super();

		typeStr = typeName;
	}
	public SelectableType(String argData)
	{
		super();

		typeStr = typeName;
		checkInitForString(argData);
		updateDisplayString();
	}

	
	
	public SelectableType(List<String> options)
	{
		super();

		typeStr = typeName;
		valueList = options;
	}
	public SelectableType(String[] options)
	{
		super();

		typeStr = typeName;
		valueList = new ArrayList<String>(Arrays.asList(options));
	}
	
	
	
	@Override
	public void checkAndSetForInit(Object val)
	{
		if (val == null)
		{
	        value = "";
	        hasContent = false;
			return;
		}
		
		
		if (val instanceof String)
			checkFullInit((String) val);
		else
		{
			valueInd = (int) val;
			
	        if (valueInd >= 0)
	        {
	        	value = valueList.get(valueInd);
	        	hasContent = true;
	        }
	        else
	        {
		        value = "";
		        hasContent = false;
	        }   
		}
			
		
		if (!checkInitForString(val))
		if (val instanceof String)
		{
			value = (String) val;
			valueInd = valueList.indexOf(value);
			hasContent = true;
		}
		
		updateDisplayString();
	}

	
	private boolean checkFullInit(String str)
	{
		String[] splitted = str.split(GlobalSettings.argumentSeparator);
		
	    try
	    {
	        valueInd = Integer.parseInt(splitted[0].trim());
	        
	        valueList = new ArrayList<>();
	        for(int i = 1; i < splitted.length; i++)
	        	valueList.add(splitted[i].trim()); // Fill the new list
	        
	        
	        if (valueInd >= 0)
	        {
	        	value = valueList.get(valueInd);
	        	hasContent = true;
		        return(true);
	        }
	        else
	        {
		        value = "";
		        hasContent = false;
		        return(false);
	        }
	        
	        
	    } catch (Exception e){} // nothing
	    
	    return(false);
	}
	
	@Override
	public Pair<Boolean, String> checkAndSetFromString(String newStr)
	{
		hasContent = false;
		hasVariable = false;

		Pair<Boolean, String> res = null;
		
		
		if (checkFullInit(newStr))
			res = new Pair<Boolean, String>(true, "Value accepted ('" + value + "').");
		
		
		if (res == null)
		if (!newStr.isEmpty())
		if (valueList.contains(newStr))
		{
			hasContent = true;
			value = newStr;
			valueInd = valueList.indexOf(value);
			res = new Pair<Boolean, String>(true, "Value accepted ('" + newStr + "').");
		}
			
		if (res == null)
			res = new Pair<Boolean, String>(false, "Value not allowed.\n\nPlease type or chose an entry from the list.");
		
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
		return(valueInd);
	}
	
	@Override
	public AbstractContentValue clone()
	{
		updateDisplayString();
		SelectableType sel = new SelectableType(valueList);
		sel.checkAndSetForInit(value);
		return(sel);
	}

	@Override
	public String toInterpretableString()
	{
		updateDisplayString();
		
		StringBuilder valueListString = new StringBuilder();
		valueListString.append(valueInd);
		for (String option: valueList)
		{
			valueListString.append(GlobalSettings.argumentSeparator);
			valueListString.append(option);
		}
		
		return(typeStr + GlobalSettings.argumentSeparator + valueListString.toString());
	}
	public List<String> getValueList()
	{
		return(valueList);
	}
}