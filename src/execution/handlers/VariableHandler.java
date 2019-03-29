package execution.handlers;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import dataTypes.minor.Pair;
import dataTypes.specialContentValues.Variable;
import javafx.scene.control.Label;
import settings.GlobalSettings;

public class VariableHandler
{
	static Map<String, Variable> variables = new Hashtable<String, Variable>();
	static Map<Variable, String> varNames = new Hashtable<Variable, String>();
	static Map<String, Integer> varCounter = new Hashtable<String, Integer>();
	
	//static List<Variable> globalVariables = new ArrayList<Variable>();

	
	// For visualisation
	static Map<String, ArrayList<Label>> varLabels = new Hashtable<String, ArrayList<Label>>();
	
	
	// provides the variable during precompilation
	public static Variable getIfExistingOrCreateNew(String variableName)
	{
		Variable newVar = variables.getOrDefault(variableName, null);
		
		if (newVar != null)
		{
			varCounter.replace(variableName, varCounter.get(variableName)+1);
			return(newVar);
		}
		else
		{
			newVar = new Variable();
			newVar.set(255);
			variables.put(variableName, newVar);
			varNames.put(newVar, variableName);
			varCounter.put(variableName, 1);
			return(newVar);
		}
		
	}
	
	/*
	public static boolean variableIsGlobal(Variable var)
	{
		return(globalVariables.contains(var));
	}
	*/
	
	public static Variable getOnlyIfExisting(String variableName)
	{
		return(variables.getOrDefault(variableName, null));
	}
	
	public static void removeAnOccurence(String variableName)
	{
		if (variableName != null)
		if (variables.containsKey(variableName))
		{
			int occ = varCounter.get(variableName)-1;
			
			/*
			if (occ <= 0)
			{
				varNames.remove(variables.get(variableName));
				variables.remove(variableName);

				varCounter.remove(variableName);
			}
			*/
				
			varCounter.replace(variableName, Math.max(0, occ));
		}
		//else
			//InfoErrorHandler.callBugError("Trying to remove a variable name which has never been added!");	
	}
	

	public static void markAnOccurence(Variable var)
	{
		String variableName = "";
		for(Entry<String, Variable> ent: variables.entrySet())
			if (ent.getValue() == var)
				variableName = ent.getKey();
		
		if (variableName.isEmpty())
			InfoErrorHandler.callBugError("Trying to mark an occurence for a non-existing variable!");
		else
			varCounter.replace(variableName, varCounter.get(variableName)+1);
	}
	
	public static String getVariableName(Variable var)
	{
		if (var != null)
		{
			String name = varNames.get(var);
			if (name == null)
			{
				if (varNames.size() < 100)
					InfoErrorHandler.callBugError("Trying to get the name of a non-existing variable!");
				return("");
			}
			else
				return(name);
		}
		
		return(null);
	}
	
	public static void clear()
	{
		variables.clear();
		varNames.clear();
		varCounter.clear();
		externallyChangedVariables.clear();
		lastVariableString.clear();
	}
	
	public static void clearVariables()
	{
		externallyChangedVariables.clear();
		lastVariableString.clear();
		
		for (Variable var: variables.values())
			var.forceReset();
	}

	
	public static Set<String> getVariableNames()
	{
		return(variables.keySet());
	}
	public static int getVariableOccurences(String name)
	{
		return(varCounter.get(name));
	}
	
	
	
	
	
	public static Pair<Boolean, String> possibleVariable(String newStr)
	{
		Pair<Boolean, String> res = null;
		
		
		String expl = "Variables in use:\n";
		int vars = 0;
		for(String name: variables.keySet())
			if (varCounter.get(name)>0)
				if (!(name.equals(newStr) && varCounter.get(name) == 1))
				{
					vars++;
					expl += name + " (Occurences: " + varCounter.get(name) + ")\n";
				}
			
		if (vars == 0)
			expl = "No variables used in this program yet.\n";
		
		
		
		if (!newStr.startsWith(GlobalSettings.varSymbol))
			res = new Pair<Boolean, String>(false, "A variable has to start with '"+GlobalSettings.varSymbol+"'.\n\n"+expl);

		if (newStr.length()<=1)
			res = new Pair<Boolean, String>(false, "A variable needs a name consisting of letters and numbers after '"+GlobalSettings.varSymbol+"'.\n\n"+expl);
		else
		if (!newStr.substring(1).matches("^[a-zA-Z0-9_]*$"))
			res = new Pair<Boolean, String>(false, "A variable may only contain letters and numbers after '"+GlobalSettings.varSymbol+"'.\n\n"+expl);
		
		if (res == null)
			res = new Pair<Boolean, String>(true, "Variable accepted.\n\n"+expl);
		
		return(res);		
	}

	
	
	public static void getVariableColorStyle(String var, Label associatedLabel)
	{
		int totalVars = 0;
		int thisVarInd = -1;
		
		// Delete the associatedLabel from, all lists
		for(Entry<String, ArrayList<Label>> ent: varLabels.entrySet())
			for (Iterator<Label> iterator = ent.getValue().iterator(); iterator.hasNext();)
				if (iterator.next() == associatedLabel)
					iterator.remove();
		
		
		ArrayList<Label> labels;
		if (varLabels.containsKey(var))
			labels = varLabels.get(var);
		else
		{
			labels = new ArrayList<Label>();
			varLabels.put(var, labels);
		}
		if (!labels.contains(associatedLabel))
			labels.add(associatedLabel);
		
		
		/*
		for(Entry<String, Variable> ent: variables.entrySet())
		{
			if (varCounter.get(ent.getKey()) >= 1)
			{			
				if (ent.getKey().equals(var)) thisVarInd = totalVars;
				totalVars++;
			}
		}
		
		String style = "";
		if (thisVarInd != -1)
		{
			float quot = (float)thisVarInd / (float)totalVars;
			style = "-fx-background-color: hsba(" + (int)(quot*360) + ", 80%, 80%, 0.3);";
		}
		*/
		
		
		for(Entry<String, ArrayList<Label>> ent: varLabels.entrySet())
			if (!ent.getValue().isEmpty())
			if (varCounter.containsKey(ent.getKey()) && (varCounter.get(ent.getKey()) > 1))
				totalVars++;

		
		thisVarInd = 0;
		for(Entry<String, ArrayList<Label>> ent: varLabels.entrySet())
		{
			String style = "";
			
			if (!ent.getValue().isEmpty())
			if (varCounter.containsKey(ent.getKey()) && (varCounter.get(ent.getKey()) > 1))
			{
				thisVarInd++;
				
				float quot = (float)thisVarInd / ((float)totalVars+1);
				style = "-fx-background-color: hsba(" + (int)(quot*360) + ", 100%, 100%, 0.6);";
			}
			
			for (Iterator<Label> iterator = ent.getValue().iterator(); iterator.hasNext();)
				iterator.next().setStyle(style);

			
		}



					
	}

	
	
	private static List<Variable> externallyChangedVariables = new ArrayList<>();
	private static List<String> lastVariableString = new ArrayList<>();

	public static void handleExternallyChangedVariable(Variable variable)
	{
		synchronized(externallyChangedVariables) 
		{
			if (!externallyChangedVariables.contains(variable))
			{
				externallyChangedVariables.add(variable);
				lastVariableString.add(Variable.getDebugTypeValueString(variable.getType(), variable.getUnchecked()));
			}
		}
	}

	public static void handleExternal()
	{
		synchronized(externallyChangedVariables) 
		{
			int ind = 0;
			for(Variable var: externallyChangedVariables)
			{
				//if (var.getUnchecked() instanceof Integer)
					//continue;
				
				String newStr = Variable.getDebugTypeValueString(var.getType(), var.getUnchecked());
				if (!newStr.equals(lastVariableString.get(ind)))
				{
					lastVariableString.set(ind, newStr);
					var.signalizeForDebugExternally();
				}				
				ind++;
			}
		}
	}

	public static void addTempMultiVar(Variable origVariable, Variable variable)
	{
		if (varNames.size() < 100)
			varNames.put(variable, "Local: " + varNames.get(origVariable));
	}
	
	
}
