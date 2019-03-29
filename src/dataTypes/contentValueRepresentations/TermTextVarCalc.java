package dataTypes.contentValueRepresentations;

import dataTypes.minor.Pair;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.handlers.VariableHandler;
import settings.GlobalSettings;

public class TermTextVarCalc extends AbstractContentValue
{
	public static final String typeName = "CalcTextTerm";

	Term value;
	
	
	public TermTextVarCalc()
	{
		super();
		makePropString();
		typeStr = typeName;
	}
	public TermTextVarCalc(boolean passingAsRealVariable)
	{
		super();
		makePropString();
		typeStr = typeName;
		setSpecial(passingAsRealVariable, false);
	}
	
	
	
	public TermTextVarCalc(String argData)
	{
		super();
		makePropString();
		typeStr = typeName;
		checkInitForString(argData);
		updateDisplayString();
	}
	public TermTextVarCalc(String argData, boolean passingAsRealVariable)
	{
		super();
		makePropString();
		typeStr = typeName;
		checkInitForString(argData);
		updateDisplayString();
		setSpecial(passingAsRealVariable, false);
	}
	


	
	String proposalString;
	private void makePropString()
	{
		proposalString =  "Input not allowed.\nPlease type a text NOT starting with following symbol '" + GlobalSettings.varSymbol + "', unless you want to use a variable.\nYou can concat two texts together by placing '+=' at the beginning.\nTo actually begin a string with '+=' use '#+=' instead.";
	}
	
	
	@Override
	public void checkAndSetForInit(Object val)
	{
		if (!checkInitForString(val))
		if (val instanceof Term)
		{
			value = (Term) val;
			hasContent = true;
			updateDisplayString();
		}
	}
	
	
	
	// Allows terms with calculations: +
	@Override
	public Pair<Boolean, String> checkAndSetFromString(String newStr)
	{
		boolean hadVariable = hasVariable;
		Variable oldVariable = null;
		if (hasVariable)
			oldVariable = variable;
		

		hasContent = false;
		hasVariable = false;
		
		
		if (value != null)
			if (value.getRightSideType() == Variable.variableType)
				VariableHandler.removeAnOccurence(VariableHandler.getVariableName((Variable) value.getRightSide()));
		
		
		//newStr = newStr.replaceAll(" ", ""); // remove spaces
		
		
		int termType = 0;
		Object innerValue = null;
		int termRightSideType = Variable.noType;
		
		Pair<Boolean, String> res = null;
		
		
		if (!newStr.startsWith("#+="))
		{
			res = checkForVariable(newStr);
			
			if (hasVariable)
			{
				innerValue = variable;
				termRightSideType = Variable.variableType;
				hasVariable = false;
				variable = null;
			}
			
			
			if ((res == null) || (!res.first))
			if (!newStr.isEmpty())
			{
			if (Term.startsWithPossibleCalcSymbol(newStr)) // if the String begins with one of the allowed termSymbols
			{
				String symbolType = Term.startsWithCalcSymbolBasedTask(newStr);
				termType = Term.startsWithCalcSymbolBasedIndex(newStr);
				
				newStr = newStr.substring(Term.getCalcSymbolString(termType).length(), newStr.length());
				
				
				// Try as variable
				res = checkForVariable(newStr);
				if (hasVariable)
				{
					innerValue = variable;
					termRightSideType = Variable.variableType;
					hasVariable = false;
					variable = null;
				}
				
				
				
				// Try for text
				if (res == null)
				if (!newStr.isEmpty())
				if (Term.isAllowedForCalcString(symbolType))
				if (!newStr.startsWith(GlobalSettings.varSymbol))		
				{
					innerValue = newStr;
					termRightSideType = Variable.textType;
					res = new Pair<Boolean, String>(true, "Value accepted (" + symbolType +" '" + innerValue + "').");
				}
				
				
				if (res == null)
					res = new Pair<Boolean, String>(false, "The input after the task symbol ('" + symbolType + "') is not allowed.\nPlease type a text NOT starting with following symbol '" + GlobalSettings.varSymbol + "', unless you want to use a variable.");
				
			}
			else
			{
				// Try for text
				if ((res == null) || (!res.first))
				if (!newStr.isEmpty())
				if (!newStr.startsWith(GlobalSettings.varSymbol))		
				{
					innerValue = newStr;
					termRightSideType = Variable.textType;
					res = new Pair<Boolean, String>(true, "Value accepted ('" + innerValue + "').");
				}
					
				if (res == null)
					res = new Pair<Boolean, String>(false, proposalString);
			}
			}
			else
			{
				res = new Pair<Boolean, String>(false, proposalString);
			}
				
		}
		else
		{
			newStr = newStr.substring(1, newStr.length());
			
			
			if (!newStr.isEmpty())
			if (!newStr.startsWith(GlobalSettings.varSymbol))		
			{
				innerValue = newStr;
				termRightSideType = -2;
				res = new Pair<Boolean, String>(true, "Value accepted ('" + newStr + "').");
			}
					
			if (res == null)
				res = new Pair<Boolean, String>(false, "Value not allowed.\n\nPlease type a text NOT starting with following symbol '" + GlobalSettings.varSymbol + "', unless you want to use a variable.");
			
		}
		
		
		if (!hasVariable)
		{
			if (res.first)
			{
				hasContent = true;
				value = new Term(termType, innerValue, termRightSideType);
			}
			
			if (hadVariable);
				VariableHandler.removeAnOccurence(VariableHandler.getVariableName(oldVariable));
		}
		
		if (!newStr.isEmpty())
			hasContent = true;
		
		
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
		return(new TermTextVarCalc(displayString));
	}

	
}
