package main.functionality.helperControlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import dataTypes.exceptions.MalformedTermException;
import dataTypes.minor.Quad;
import dataTypes.specialContentValues.Variable;
import execution.handlers.VariableHandler;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.tokenizer.ParseException;

public class ComputationHelper {
	
	private static Scope scope = new Scope();
	private static Map<String, Quad<Expression, List<AtomicReference<Object>>, List<parsii.eval.Variable>, List<Integer>>> existingTerms = new HashMap<>();
	
	public static void clear()
	{
		existingTerms.clear();
	}
	
	
	public static Double compute(String term) throws MalformedTermException
	{
		Quad<Expression, List<AtomicReference<Object>>, List<parsii.eval.Variable>, List<Integer>> dat = existingTerms.getOrDefault(term, null);

		if(dat == null)
		{
			List<AtomicReference<Object>> varDat = new ArrayList<>();
			List<parsii.eval.Variable> vars = new ArrayList<>();
			List<Integer> types = new ArrayList<>();
			
			String[] varLines = term.split("#");
			for(String line: varLines)
			{
				int ind = line.indexOf(" ");
				if (ind == -1)
					ind = line.indexOf("+");
				if (ind == -1)
					ind = line.indexOf("-");
				if (ind == -1)
					ind = line.indexOf("*");
				if (ind == -1)
					ind = line.indexOf("/");
				if (ind == -1)
					ind = line.indexOf("(");
				if (ind == -1)
					ind = line.indexOf(")");
				
				String var = line.substring(1, ind);
				
				
				Variable v = VariableHandler.getOnlyIfExisting(var);
				if (v == null)
					throw new MalformedTermException("A variable with the name: " + var + " is not existing!");
				
				types.add(v.getType());
				
				varDat.add(v.getInternalValueContainer());
				parsii.eval.Variable vv = scope.getVariable(var);
				vars.add(vv);
				vv.setValue((double) v.getUnchecked());
			}
			
			
			dat = new Quad<Expression, List<AtomicReference<Object>>, List<parsii.eval.Variable>, List<Integer>>();
			
			try
			{
				dat.first = Parser.parse(term.replaceAll("#", ""), scope);
			} catch (ParseException e)
			{
				throw new MalformedTermException("Failing to parse the expression: " + term);
			}
			dat.second = varDat;
			dat.third = vars;
			dat.fourth = types;
		}
		else
		{
			int s = dat.second.size();
			for(int i = 0; i < s; i++)
			{
				switch(dat.fourth.get(i))
				{
				case Variable.doubleType:
					dat.third.get(i).setValue((double) dat.second.get(i).get());
					break;
				case Variable.boolType:
					dat.third.get(i).setValue(((boolean) dat.second.get(i).get()) ? 1 : 0);
					break;
				}
			}
		}
		
		return(dat.first.evaluate());
		
	}
	
}
