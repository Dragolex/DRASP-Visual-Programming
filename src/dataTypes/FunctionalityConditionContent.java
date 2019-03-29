package dataTypes;

import java.util.concurrent.Callable;

import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import productionGUI.sections.elements.VisualizableProgramElement;

public class FunctionalityConditionContent extends FunctionalityContent
{

	public FunctionalityConditionContent() {
		super("");
	}
	
	
	
	// Action with condition
	public FunctionalityConditionContent(String visualizerName, Object[] arguments, Callable<Boolean> condition)
	{
		super(visualizerName, arguments, null);
		
		this.condition = condition;
		this.markedAsCondition = true;
	}
	public FunctionalityConditionContent(String visualizerName, Object[] arguments, Runnable initialization, Callable<Boolean> condition) // with initialization
	{
		super(visualizerName, arguments, initialization, null);
		
		this.condition = condition;
		this.markedAsCondition = true;
	}
	
	
	




	int resInversedIndex = 100;
	int targetVariableIndex = 100;
	
	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void addFixedOptionalArguments(VisualizableProgramElement visElement)
	{
		if (fixedOptionalArgumentsRemoved) return;
		
		
		String resInversedTxt = "If true, the result of the condition is inversed.";
		String targetVarTxt = "Places the response of the check into this boolean variable (1 = True, 0 = False).\nThe element can still be used normally and have child-elements.";
		
		if (resInversedIndex >= additionalExecutionValues.length)
			resInversedIndex = 50 + visElement.addOptionalParameter(-1, new BooleanOrVariable("false"), "Not", resInversedTxt);
		else
			visElement.addOptionalParameter(-1, new BooleanOrVariable("false"), "Not", resInversedTxt);
		
		if (targetVariableIndex >= additionalExecutionValues.length)
			targetVariableIndex = 50 + visElement.addOptionalParameter(-1, new VariableOnly(true, true), "Target", targetVarTxt);
		else
			visElement.addOptionalParameter(-1, new VariableOnly(true, true), "Target", targetVarTxt);
	}
		
	
	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean callCondition() throws Exception
	{
		boolean res = false;
		
		if (Execution.isTracked())
		{
			Execution.print("Element #" + getCodeLineIndex() + ": Evaluating condition of '" + getFunctionalityName()+"'");
			if (hasAnyParameters())
				Execution.print("Element #" + getCodeLineIndex() + ": Parameters: " + getParametersConcatString());
			
			res = condition.call();	
			
			Execution.print("Element #" + getCodeLineIndex() + ": Result was: " + res);
		}
		else
			res = condition.call();	
		
		
		if (targetVariableIndex < 50)
			((Variable) additionalExecutionValues[targetVariableIndex-1]).initTypeAndSet(Variable.boolType, res);
		
		if (resInversedIndex < 50)
			if ((boolean) additionalExecutionValues[resInversedIndex-1])
				return(!res);
			else
				return(res);
		
		return(res);
	}
	
	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void reloadIfPossible(int index)
	{
		if (index == (targetVariableIndex-50))
			targetVariableIndex -= 50;
		
		if (index == (resInversedIndex-50))
			resInversedIndex -= 50;
	}
	
	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void resetIfPossible(int index)
	{
		if (index == resInversedIndex)
			resInversedIndex = -(resInversedIndex-1);
		
		if (index == targetVariableIndex)
			targetVariableIndex = -(targetVariableIndex-1);
	}
	
	
	@Override
	public void setSpecialOptionalArgIndex()
	{
		if (resInversedIndex > 50)
			resInversedIndex = additionalExecutionValues.length;
		else
		if (targetVariableIndex > 50)
			targetVariableIndex = additionalExecutionValues.length;
	}
	
	@Override
	public boolean checkSpecialFixedArgument(int index)
	{
		if (index == (resInversedIndex+getArgumentValues().length))
			return(true);
		if (index == (targetVariableIndex+getArgumentValues().length))
			return(true);
		
		return(false);
	}	

	
	
	
}
