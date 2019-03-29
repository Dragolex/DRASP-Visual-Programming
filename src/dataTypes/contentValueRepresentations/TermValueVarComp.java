package dataTypes.contentValueRepresentations;

import dataTypes.minor.Pair;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.handlers.VariableHandler;
import settings.GlobalSettings;

public class TermValueVarComp extends AbstractContentValue
{
	public static final String typeName = "CompTerm";

	Term value;
	

	public TermValueVarComp()
	{
		super();
		makePropString();
		typeStr = typeName;
	}
	public TermValueVarComp(boolean passingAsRealVariable)
	{
		super();
		makePropString();
		typeStr = typeName;
		setSpecial(passingAsRealVariable, false);
	}

	
	
	public TermValueVarComp(String argData)
	{
		super();
		makePropString();
		typeStr = typeName;
		checkInitForString(argData);
		updateDisplayString();
	}
	public TermValueVarComp(String argData, boolean passingAsRealVariable)
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
		proposalString =  "Input not allowed.\nPlease type a number with '.' or ',' as a decimal symbol or a text.\nAlternatively use a variable (starting with '" + GlobalSettings.varSymbol + "').\nSimple comparations can be performed by placing the coresponding symbol as the first sign.\nUse " + Term.getCompPossibilitiesString() + ".";	
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
	
	
	
	// Allows terms with comparators < > <= etc
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
			if (Term.startsWithPossibleCompSymbol(newStr)) // if the String begins with one of the allowed termSymbols
			{
				String symbolType = Term.startsWithCompSymbolBasedTask(newStr);
				termType = Term.startsWithCompSymbolBasedIndex(newStr);
				
				
				newStr = newStr.substring( Term.getCompSymbolString(termType).length() , newStr.length());
				
				
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
				
				
				
				// Try for text
				if (res == null)
				if (!newStr.isEmpty())
				if (Term.isAllowedForCompString(symbolType))
				if (!newStr.startsWith(GlobalSettings.varSymbol))		
				{
					innerValue = newStr;
					termRightSideType = Variable.textType;
					res = new Pair<Boolean, String>(true, "Value accepted (" + symbolType +" '" + innerValue + "').");
				}
				
				
				
				if (res == null)
					res = new Pair<Boolean, String>(false, proposalString);
				
			}
			else
			{
				
				
				// Try for a value
				/*
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
				}*/
				
				/*
				// Try for text
				if (res == null)
				if (!newStr.isEmpty())
				if (!newStr.startsWith(GlobalSettings.varSymbol))
				{
					innerValue = newStr;
					termRightSideType = Variable.textType;
					res = new Pair<Boolean, String>(true, "Value accepted ('" + innerValue + "').");
				}
				*/
				
				
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
		return(new TermValueVarComp(displayString));
	}

	
}
