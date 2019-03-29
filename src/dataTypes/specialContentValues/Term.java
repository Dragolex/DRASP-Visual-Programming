package dataTypes.specialContentValues;

import java.util.ArrayList;
import java.util.List;

import execution.Execution;
import main.functionality.helperControlers.hardware.analog.SensorDevice;
import settings.GlobalSettings;

public class Term {
	
	
	/*
	 * Term calc tasks:
	 * 0: +
	 * 1: -
	 * 2: *
	 * 3: /
	 */
	
	@SuppressWarnings("serial")
	static private List<String> calcTermSymbolsList = new ArrayList<String>() {{add(""); add("+="); add("-="); add("*="); add("/=");}};
	@SuppressWarnings("serial")
	static private List<String> calcTermSymbolTaskList = new ArrayList<String>() {{add(""); add("Add"); add("Subtract"); add("Multiplicate"); add("Divide");}};
	
	
	public static boolean startsWithPossibleCalcSymbol(String symbol)
	{
		for(String st: calcTermSymbolsList)
			if (!st.isEmpty())
			if (symbol.startsWith(st)) return(true);
		
		return(false);
	}
	public static int startsWithCalcSymbolBasedIndex(String symbol)
	{
		int ind = 0;
		for(String st: calcTermSymbolsList)
			if ((!st.isEmpty()) && symbol.startsWith(st)) return(ind); else ind++;
		
		return(-1);	
	}
	public static String startsWithCalcSymbolBasedTask(String symbol)
	{
		return(calcTermSymbolTaskList.get(startsWithCalcSymbolBasedIndex(symbol)));		
	}
	public static boolean isAllowedForCalcString(String symbolType)
	{
		return(symbolType.equals(calcTermSymbolTaskList.get(1))); // Only allow Adding
	}
	public static String getCalcPossibilitiesString()
	{
		String str = "";
		for(int i = 1; i < calcTermSymbolsList.size(); i++)
			str += "'"+calcTermSymbolsList.get(i)+"' to " + calcTermSymbolTaskList.get(i).toLowerCase()+", ";
		str = str.substring(0, str.length()-2);
		return(str);
	}
	public static String getCalcSymbolString(int type)
	{
		return(calcTermSymbolsList.get(type));
	}
	
	
	
	
	final static int compOffs = 20;
	
	
	/*
	 * Term compare:
	 * 0: =
	 * 1: <
	 * 2: >
	 * 3: <=
	 * 4: >=
	 */
	
	@SuppressWarnings("serial")
	static public List<String> compTermSymbolsList = new ArrayList<String>() {{add(""); add("="); add("<"); add(">"); add("<="); add(">=");}};
	@SuppressWarnings("serial")
	static public List<String> compTermSymbolTaskList = new ArrayList<String>() {{add(""); add("Equal"); add("Smaller than"); add("Larger than"); add("Smaller/Equal"); add("Larger/Equal");}};
	
	
	public static boolean startsWithPossibleCompSymbol(String symbol)
	{
		for(String st: compTermSymbolsList)
			if (!st.isEmpty())
			if (symbol.startsWith(st)) return(true);
		
		return(false);
	}
	public static int startsWithCompSymbolBasedIndex(String symbol)
	{
		int ind = 0;
		for(String st: compTermSymbolsList)
			if ((!st.isEmpty()) && symbol.startsWith(st)) return(ind + compOffs); else ind++;
		
		return(-1);
	}
	public static String startsWithCompSymbolBasedTask(String symbol)
	{
		return(compTermSymbolTaskList.get(startsWithCompSymbolBasedIndex(symbol)-compOffs));		
	}
	public static boolean isAllowedForCompString(String symbolType)
	{
		return(symbolType.equals(compTermSymbolTaskList.get(1))); // Only allow simple comparison
	}
	public static String getCompPossibilitiesString()
	{
		String str = "";
		for(int i = 1; i < compTermSymbolsList.size(); i++)
			str += "'"+compTermSymbolsList.get(i)+"' to " + compTermSymbolTaskList.get(i).toLowerCase()+", ";
		str = str.substring(0, str.length()-2);
		return(str);
	}
	public static String getCompSymbolString(int type)
	{
		return(compTermSymbolsList.get(type-compOffs));
	}
	
	
	
	
	
	public int termType; // >= compOffs  means it is a comparator
	Object rightSide;
	int rightSideType;
	
	
	public Term(int termType, Object rightSide, int rightSideType)
	{
		this.termType = termType;
		this.rightSide = rightSide;
		this.rightSideType = rightSideType;
	}
	
	
	public Object applyTo(Double var)
	{
		double value;
		if (rightSideType == Variable.variableType)
		{
			value = (double) (((Variable)rightSide).getConvertedValue(Variable.doubleType)); // resolve variable
		}
		else
			value = (double) rightSide;
		
		
		if (termType < compOffs)
			switch(termType) // Calculator term
			{
			case 0: return(value);
			case 1: return(var + value);
			case 2: return(var - value);
			case 3: return(var * value);
			case 4: return(var / value);
			}
		else
			switch(termType-compOffs) // Comparator term
			{
			case 0: return(value);
			case 1: return(var == value);
			case 2: return(var < value);
			case 3: return(var > value);
			case 4: return(var <= value);
			case 5: return(var >= value);
			}
		
		return(null);
	}
	
	public Object applyTo(String var)
	{
		String value;
		if (rightSideType == Variable.variableType)
		{
			value = (String) (((Variable)rightSide).getConvertedValue(Variable.textType)); // resolve variable
		}
		else
			value = (String) rightSide;
		
		if (termType < compOffs)
			switch(termType) // Calculator term
			{
			case 0: return(value);
			case 1: return(var + (String) value);
			default: Execution.setError("Trying to apply an invalid operation to two 'texts'!", false);
			}
		else
			switch(termType) // Comparator term
			{
			case 0: return(value);
			case 1: return(var.equals((String) value));
			default: Execution.setError("Trying to apply an invalid operation to two 'texts'!", false);
			}
		
		return(null);
	}
	
	public Object applyTo(Variable var)
	{
		if (var.hasValue())
		{
			return(applyTo(var.getUnchecked(), var.getType()));
		}
		else
		{
			if (termType != 0)
				Execution.setError("Trying to apply a term (Addition, subtraction, etc.)\nto a variable which has no value.", true);
			else
			{
				if (rightSideType == Variable.variableType)
					var.initType( ((Variable) rightSide).getType() );
				else
					var.initType(rightSideType);
			}
			
			return(applyTo(var.getUnchecked(), var.getType())); // If the variable has no type, then set the apropiate type first before re-attempt to apply.
		}
	}
	
	public Object applyTo(Object data, int type)
	{
		switch(type)
		{
		case Variable.doubleType: return( applyTo((double) data) );
		case Variable.textType: return(applyTo((String) data));
		case Variable.variableType: return(applyTo((Variable) data));
		default: Execution.setError("Trying to apply a variable which is not of a known data type!", false);
		}
		
		return(null);
	}
	
	
	
	
	public Object getRightSide()
	{
		return(rightSide);
	}
	
	public int getRightSideType()
	{
		return(rightSideType);
	}
	
	
	
	@Override
	public String toString()
	{
		if (rightSideType == -2)
			return("#" + rightSide.toString());	
		
		if (termType < compOffs)
			return(calcTermSymbolsList.get(termType) + rightSide.toString());
		else
			return(compTermSymbolsList.get(termType - compOffs) + rightSide.toString());
	}
	
	
	public void forceToText()
	{
		if (rightSideType == Variable.variableType)
		{
			Variable var = (Variable) rightSide;
			
			switch(var.getType())
			{
			case Variable.doubleType:
				rightSide = GlobalSettings.doubleFormatter.format(var.getUnchecked());
				rightSideType = Variable.variableType;
				break;
			case Variable.AnalogDevice:
				rightSide = GlobalSettings.doubleFormatter.format(((SensorDevice)var.getUnchecked()).getValue());
				rightSideType = Variable.textType;
				break;
			}
		}
	}

	public void forceToNum()
	{
		if (rightSideType == Variable.variableType)
		{
			Variable var = (Variable) rightSide;
			
			switch(var.getType())
			{
			case Variable.textType:
				rightSide = Double.valueOf((String) var.getUnchecked());
				rightSideType = Variable.doubleType;
				break;
			}
		}
	}
	
	
}
