package dataTypes.contentValueRepresentations;

import dataTypes.minor.Pair;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.handlers.VariableHandler;
import settings.GlobalSettings;

public class TermValueVarCalc extends AbstractContentValue
{
	public static final String typeName = "CalcValueTerm";

	Term value;
	
	
	public TermValueVarCalc()
	{
		super();
		makePropString();
		typeStr = typeName;
	}
	public TermValueVarCalc(boolean passingAsRealVariable)
	{
		super();
		makePropString();
		typeStr = typeName;
		setSpecial(passingAsRealVariable, false);
	}
	
	
	public TermValueVarCalc(String argData)
	{
		super();
		makePropString();
		typeStr = typeName;
		checkInitForString(argData);
		updateDisplayString();
	}
	public TermValueVarCalc(String argData, boolean passingAsRealVariable)
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
		proposalString =  "Input not allowed.\nPlease type a number with '.' or ',' as a decimal symbol.\nAlternatively use a variable (starting with '" + GlobalSettings.varSymbol + "').\nSimple calculations can be performed by placing the coresponding symbol as the first sign together with a '='.\nUse " + Term.getCalcPossibilitiesString() + ".";	
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
	
	
	
	// Allows terms with calculations * + - etc
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
		
		
		newStr = newStr.replaceAll(" ", ""); // remove spaces
		
		
		int termType = 0;
		Object innerValue = null;
		int termRightSideType = Variable.noType;
		
		Pair<Boolean, String> res = checkForVariable(newStr);
		

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
				
				
				// Try for a value
				if (!newStr.isEmpty())
				if (res == null)
				{
					try
					{
						innerValue = Double.parseDouble(newStr.replaceAll(",", ".")); // try to parse
						newStr = newStr.replaceAll(",", ".");
						termRightSideType = Variable.doubleType;
						res = new Pair<Boolean, String>(true, "Value accepted (" + symbolType +" '" + innerValue + "').\n" + GlobalSettings.variableDescriptionText);
					}
					catch(NumberFormatException e)
					{ value = null; res = null; }
				}
				
				
				if (res == null)
					res = new Pair<Boolean, String>(false, proposalString);
				
			}
			else
			{
				
				// Try for a value
				if (res == null)
				{
					try
					{
						innerValue = Double.parseDouble(newStr.replaceAll(",", ".")); // try to parse
						newStr = newStr.replaceAll(",", ".");
						termRightSideType = Variable.doubleType;
						res = new Pair<Boolean, String>(true, "Value accepted ('" + innerValue + "').\n" + GlobalSettings.variableDescriptionText);
					}
					catch(NumberFormatException e)
					{ value = null; res = null; }
				}
				
				
				if (res == null)
					res = new Pair<Boolean, String>(false, proposalString);
				
			}
			}
			else
			{
				res = new Pair<Boolean, String>(false, proposalString);
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
		return(new TermValueVarCalc(displayString));
	}

	
}
