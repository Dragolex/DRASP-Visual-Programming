package execution.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataTypes.BreakPointResponse;
import dataTypes.FunctionalityConditionContent;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramEventContent;
import dataTypes.SimpleTable;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import staticHelpers.GuiMsgHelper;

public class BreakPointHandler
{
	public static BreakPointResponse elementPredecessingMessage(FunctionalityContent element)
	{
		String elType = "element";
		String eventAddition = "";
		
		String[] buttons = new String[] {"Continue", "Break at Next"};
		
		if (element instanceof FunctionalityConditionContent)
			elType = "condition-element";
		if (element instanceof ProgramEventContent)
		{
			eventAddition = "\n\nPress 'Only if True' to show this message only when this event actually fires (and thus returned true).";
			buttons = new String[] {"Continue", "Break at Next", "Only if True"};
			elType = "event-element";
		}

		
		String msg = "Breakpoint before " + elType + " '" + element.getVisualization().getName() + "' on line " + element.getCodeLineIndex() + " in page '" + element.getCodePageName() + "'\n"
				+ "\n"
				+ "INPUT VARIABLES";
		
		SimpleTable<String> variableTable = new SimpleTable<String>();
		
		variableTable.addColumn("Argument", element.getVisualization().getArgumentDescriptions());	
		variableTable.addColumn("Type", getContentTypesString(element));
		variableTable.addColumn("Values", getContentValuesString(element));
		
		BreakPointResponse brResp = placeContentVariablesValueStrings(element);

		int res = GuiMsgHelper.showNonblockingUIWithTable(msg + "\n\nPress 'Continue' to execute until the next breakpoint.\nPress 'Break at Next' to execute the current action and break at the next one." + eventAddition, variableTable, buttons);
		
		brResp.breakNext = (res == 1);
		brResp.skipToTrueEvent = (res == 2);
		
		return(brResp);
	}
	
	
	public static void actionPostMessage(BreakPointResponse response)
	{
		if (!Execution.isRunning()) return;
		
		SimpleTable<String> variableTable = getChangedVariablesTable(response);
		
		if (variableTable != null)
			GuiMsgHelper.showNonblockingUIWithTable("The action-element changed the following argument" + GuiMsgHelper.sIfPlur(variableTable.getColumnList(0).size()) + "/variable" + GuiMsgHelper.sIfPlur(variableTable.getColumnList(0).size()) +":", variableTable, new String[] {"Continue"});
	}

	
	
	public static void conditionPostMessage(BreakPointResponse response, boolean res)
	{
		if (!Execution.isRunning()) return;
		
		SimpleTable<String> variableTable = getChangedVariablesTable(response);
		
		if (variableTable != null)
			GuiMsgHelper.showNonblockingUIWithTable("The condition-element returned the following result: " + ((res) ? "TRUE" : "FALSE") + "\n\nIt changed the following argument" + GuiMsgHelper.sIfPlur(variableTable.getColumnList(0).size()) + "/variable" + GuiMsgHelper.sIfPlur(variableTable.getColumnList(0).size()) +":", variableTable, new String[] {"Continue"});
		GuiMsgHelper.showMessageNonblockingUI("The condition-element returned the following result: " + ((res) ? "TRUE" : "FALSE"));
	}
	
	
	
	public static void eventPostMessage(BreakPointResponse response, boolean res)
	{
		if (!Execution.isRunning()) return;
		
		SimpleTable<String> variableTable = getChangedVariablesTable(response);
		
		if (variableTable != null)
			GuiMsgHelper.showNonblockingUIWithTable("The event-condition-element returned the following result: " + ((res) ? "TRUE" : "FALSE") + "\n\nIt changed the following argument" + GuiMsgHelper.sIfPlur(variableTable.getColumnList(0).size()) + "/variable" + GuiMsgHelper.sIfPlur(variableTable.getColumnList(0).size()) +":", variableTable, new String[] {"Continue"});
		GuiMsgHelper.showMessageNonblockingUI("The condition-element returned the following result: " + ((res) ? "TRUE" : "FALSE"));
	}
	
	
	
	
	
	
	

	private static SimpleTable<String> getChangedVariablesTable(BreakPointResponse response)
	{
		List<Object> newValues = getContentValuesList(response.element);
		String[] names = response.element.getVisualization().getArgumentDescriptions();
		
		List<String> changedArgNames = new ArrayList<String>();
		List<String> changedVarNames = new ArrayList<String>();		
		List<String> oldValue = new ArrayList<String>();
		List<String> newValue = new ArrayList<String>();
		
		
		int ind = 0;
		for(Integer mainInd: response.changedIndices)
		{
			String oldVal = response.changedValues.get(ind);
			String newVal = String.valueOf(newValues.get(mainInd));
			
			
			if (! oldVal.equals(newVal)) // if changed
			{
				changedArgNames.add(names[ind]);
				changedVarNames.add(VariableHandler.getVariableName((Variable) newValues.get(mainInd)));
				oldValue.add(oldVal);
				newValue.add(newVal);
			}
			
			ind++;
			
		}
		if (changedArgNames.isEmpty()) return(null);
		
		SimpleTable<String> variableTable = new SimpleTable<String>();
		
		variableTable.addColumn("Argument", changedArgNames);	
		variableTable.addColumn("Variable", changedVarNames);	
		variableTable.addColumn("Old Value", oldValue);
		variableTable.addColumn("New Value", newValue);
		
		return(variableTable);
	}

	
	
	private static List<String> getContentValuesString(FunctionalityContent content)
	{		
		List<String> types = new ArrayList<String>();

		for(Object val: getContentValuesList(content))
		{
			if (val instanceof Variable) // variable
				types.add( Variable.getDebugTypeName(((Variable) val).getType()) );
			else
				types.add(val.getClass().getSimpleName());
		}
		
		return(types);
	}
	
	private static List<String> getContentTypesString(FunctionalityContent content)
	{		
		List<String> values = new ArrayList<String>();

		for(Object val: getContentValuesList(content))
		{
			if (val instanceof Variable) // variable
			{
				Variable var = (Variable) val;
				
				if (var.hasValue())
					values.add( VariableHandler.getVariableName(var) +  " (" + var.getUnchecked() + ")" );
				else
					values.add( VariableHandler.getVariableName(var) +  " ( --NO VALUE-- )" );
			}
			else
				values.add(val.toString());
		}

		return(values);
	}
	
	private static BreakPointResponse placeContentVariablesValueStrings(FunctionalityContent content)
	{
		List<Integer> changedIndices = new ArrayList<Integer>();
		List<String> changedValues = new ArrayList<String>();
		
		int ind = 0;
		for(Object val: getContentValuesList(content))
		{
			if (val instanceof Variable) // variable
			{
				Variable var = (Variable) val;
				
				changedIndices.add(ind);
				
				if (var.hasValue())
					changedValues.add( String.valueOf(var.getUnchecked()));
				else
					changedValues.add("--NO VALUE--");
			}
			
			ind++;
		}
		
		return(new BreakPointResponse(content, changedIndices, changedValues));
	}

	
	
	private static List<Object> getContentValuesList(FunctionalityContent content)
	{
		List<Object> totalList = new ArrayList<Object>();
		
		totalList.addAll(Arrays.asList(content.getArgumentValues()));
		totalList.addAll(Arrays.asList(content.getTotalOptionalOrExpandedArgumentsArray()));
		
		return(totalList);
	}
	
}


