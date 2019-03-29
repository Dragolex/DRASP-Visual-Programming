package dataTypes.contentValueRepresentations;

import dataTypes.minor.Pair;
import dataTypes.specialContentValues.Variable;
import execution.handlers.InfoErrorHandler;
import execution.handlers.VariableHandler;
import javafx.scene.layout.Pane;
import settings.GlobalSettings;

public abstract class AbstractContentValue
{
	
	public static AbstractContentValue interpretLine(String line, boolean passingAsRealVariable, boolean canBeEditedByElement)
	{
		return(interpretLine(line).setSpecial(passingAsRealVariable, canBeEditedByElement));
	}
	
	public static AbstractContentValue interpretLine(String line)
	{
		int ind = line.indexOf(GlobalSettings.argumentSeparator);
		String typeName = line.substring(0, ind);
		String argData = line.substring(ind+2);
		
		if (argData.equals(GlobalSettings.notSetSymbol))
			argData = "";

		switch(typeName)
		{
		case BooleanOrVariable.typeName: return(new BooleanOrVariable(argData));
		case TextOnly.typeName: return(new TextOnly(argData));
		case LabelString.typeName: return(new LabelString(argData));
		case SelectableType.typeName: return(new SelectableType(argData));
		case ValueOrVariable.typeName: return(new ValueOrVariable(argData));
		case TermValueVarCalc.typeName: return(new TermValueVarCalc(argData));
		case TermValueVarComp.typeName: return(new TermValueVarComp(argData));
		case TermTextVarCalc.typeName: return(new TermTextVarCalc(argData));
		case TextOrVariable.typeName: return(new TextOrVariable(argData));
		case VariableOnly.typeName: return(new VariableOnly(argData));
		}
		
		
		InfoErrorHandler.callPrecompilingError("An argument line could not be interpreted!\nUnknown type in line: " + line);
		
		return(null);
	}
	
	
	
	
	protected String typeStr;
	
	protected String displayString;
	protected boolean hasContent;
	
	protected Variable variable;
	protected boolean hasVariable; // is a variable representation
	
	private boolean passesAsRealVariable = false;
	private boolean canBeEditedByElement = false;

	protected boolean globalVariable;

	
	// Empty constructor
	public AbstractContentValue()
	{
		displayString = "";
		hasContent = false;
		hasVariable = false;
	}
	


	
	public String getDisplayString()
	{
		return(displayString);
	}


	public boolean hasContent()
	{
		return(hasContent);
	}

	public boolean hasVariable()
	{
		return(hasVariable);
	}
	
	
	
	protected void setAsVariable(String variableName)
	{	
		Variable newVariable;
		
		newVariable = VariableHandler.getIfExistingOrCreateNew(variableName);
		
		VariableHandler.removeAnOccurence(VariableHandler.getVariableName(variable));
		
		variable = newVariable;
		hasVariable = true;
		hasContent = true;
		
		updateDisplayString();
	}
	protected void setAsVariableDirect(Variable var)
	{
		VariableHandler.markAnOccurence(var);
		
		hasVariable = true;
		variable = var;
		hasContent = true;
		
		updateDisplayString();
	}

	
	protected abstract void checkAndSetForInit(Object val);
	
	public void checkAndSetForInitTop(Object val)
	{
		if (val instanceof Variable) // if it is a variable
			setAsVariableDirect((Variable) val);
		else
			checkAndSetForInit(val);
	}
	
	
	public abstract Pair<Boolean, String> checkAndSetFromString(String newStr);

	protected abstract void updateDisplayString();
	


	
	protected boolean checkInitForString(Object val)
	{
		if (val instanceof String)
		{
			String str = (String) val;
			
			if (str.isEmpty())
				return(false);
			
			Pair<Boolean, String> res = checkAndSetFromString(str);
			if (!res.first)
				InfoErrorHandler.callPrecompilingError("Problem with creating a content value of type: " + typeStr + "\nUsing the value: " + val + "\nReason: " + res.second);
			else return(true);
		}
		return(false);
	}
	
	
	protected Pair<Boolean, String> checkForVariable(String newStr)
	{
		Pair<Boolean, String> res = null;
		
		if (newStr.contains(GlobalSettings.varSymbol))
			res = VariableHandler.possibleVariable(newStr);
		
		if (res != null)
		if (res.first)
		{
			setAsVariable(newStr);
			res = new Pair<Boolean, String>(true, "Using variable named: " + newStr + "\n" + res.second);
		}
		
		if (hasVariable)
			res = VariableHandler.possibleVariable(newStr);
		else
		if (variable != null)
		{
			VariableHandler.removeAnOccurence(VariableHandler.getVariableName(variable));
			variable = null;
			res = VariableHandler.possibleVariable(newStr);
		}

		
		return(res);
	}
	
	
	public boolean needDisplayString()
	{
		if (!hasContent)
			displayString = "";
		else
		if (hasVariable)
			displayString = VariableHandler.getVariableName(variable);
		else
			return(true);
		
		return(false);
	}
	
	
	
	public String toInterpretableString()
	{
		updateDisplayString();
		if (displayString.isEmpty())
			return(typeStr + GlobalSettings.argumentSeparator + GlobalSettings.notSetSymbol);
		else
			return(typeStr + GlobalSettings.argumentSeparator + displayString);
	}
	


	protected abstract Object getArgumentValue();
	
	protected abstract AbstractContentValue clone();
	
	
	public AbstractContentValue cloneThis()
	{
		AbstractContentValue newContent = clone();
		
		newContent.setSpecialTooltipLoad(getSepcialTooltipLoad());
		
		if (hasVariable)
			newContent.setAsVariableDirect(variable); // Clone as variable too
		
		newContent.setSpecial(passesAsRealVariable, canBeEditedByElement);
		
		return(newContent);
	}
	
	

	

	public Object getOutputValue()
	{
		if (hasVariable)
			return(variable);
		else
			return(getArgumentValue());
	}
	
	public Variable getVariableIfPossible()
	{
		if (hasVariable)
			return(variable);
		else return(null);
	}



	public void destroy()
	{
		VariableHandler.removeAnOccurence(VariableHandler.getVariableName(variable));
	}
	
	
	public AbstractContentValue setSpecial(boolean passingAsRealVariable, boolean canBeEditedByElement)
	{
		this.passesAsRealVariable = passingAsRealVariable;
		this.canBeEditedByElement = canBeEditedByElement;
		
		return(this);
	}
	
	public boolean passesAsRealVar()
	{
		return(passesAsRealVariable);
	}
	public boolean canBeEditedByElement()
	{
		return(canBeEditedByElement);
	}

	
	
	
	Pane specialTooltipImage = null;
	
	public void setSpecialTooltipLoad(Pane img)
	{
		specialTooltipImage = img;
	}
	
	public Pane getSepcialTooltipLoad()
	{
		return(specialTooltipImage);
	}


	

}
