package dataTypes;

import java.util.concurrent.Callable;

import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import execution.EventInstance;
import main.functionality.Events;
import main.functionality.Functionality;
import main.functionality.SharedComponents;
import productionGUI.sections.elements.VisualizableProgramElement;


public class ProgramEventContent extends FunctionalityContent
{
	Object[] input;
	
	Callable<Boolean> externallyCallableCondition;
	
	
	
	public ProgramEventContent(String visualizerName)
	{
		super(visualizerName);
		this.markedAsEvent = true;
	}
	
	public ProgramEventContent(String visualizerName, Object[] arguments, Callable<Boolean> condition)
	{
		super(visualizerName, arguments, null);

		this.condition = condition;
		this.markedAsEvent = true;
	}
	public ProgramEventContent(String visualizerName, Object[] arguments, Runnable initialization, Callable<Boolean> condition) // with initialization
	{
		super(visualizerName, arguments, initialization, null);
		
		this.condition = condition;
		this.markedAsEvent = true;
	}
	
	
	// special variant (by condition)
	public ProgramEventContent(String visualizerName, Object[] input, Object[] arguments, Callable<Boolean> externallyCallableCondition)
	{
		super(visualizerName, arguments, null);
		
		this.externallyCallableCondition = externallyCallableCondition;
		this.markedAsEvent = true;
		
		this.specialVariant = Functionality.DefinedInternallyTriggeredEvent;
		
		this.input = input;
	}
	public ProgramEventContent(String visualizerName, Object[] input, Object[] arguments, Runnable initialization, Callable<Boolean> externallyCallableCondition) // with initialization
	{
		super(visualizerName, arguments, initialization, null);
		
		this.externallyCallableCondition = externallyCallableCondition;
		this.markedAsEvent = true;
		
		this.specialVariant = Functionality.DefinedInternallyTriggeredEvent;
		
		this.input = input;
	}
	public ProgramEventContent(String visualizerName, Object[] input, Object[] arguments, Runnable initialization, Callable<Boolean> constantUpdatingWithArgs, Callable<Boolean> externallyCallableCondition) // with initialization and constant updating
	{
		super(visualizerName, arguments, initialization, null);
		
		this.condition = constantUpdatingWithArgs; // use the default condition too
		this.externallyCallableCondition = externallyCallableCondition; // externally activated
		this.markedAsEvent = true;

		this.input = input;
		
		// do not set specialVariant !
	}




	// special variant (by constant)
	public ProgramEventContent(String visualizerName, int specialVariant, int argCount)
	{
		super(visualizerName, specialVariant, argCount);
		
		//if (specialVariant == Functionality.InitEvent) // moved from here
			//removeFixedOptionalArguments();
	}
	
	
	
	
	boolean defaultUniqueExec = SharedComponents.defaultUniqueExecEvent;
	double defaultMinWaitTime = SharedComponents.defaultMinWaitTimeEvent;
	
	int uniqueExecIndex = 100;
	int minWaitTimeIndex = 100;
	
	
	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void addFixedOptionalArguments(VisualizableProgramElement visElement)
	{
		if (fixedOptionalArgumentsRemoved) return;
				
		String uniqueTxt = "If true, only one instance of this event will be executed at a time. Condition-Checking halts until execution finished.\nIf false, the number of events is not limited.";
		String minWaitTxtA = "If given positive number:\n   Minimum enforced delay between two event triggers in milliseconds.\nRecommended for hardware inputs for example with values around 50-100ms.\nIf negative number:\n   The event condition has to be 'true' for the given duration in milliseconds.\nThis way you can create a long-press event for buttons for example.";
		String minWaitTxtB = "Minimum enforced delay between two event triggers in milliseconds.\nRecommended for hardware inputs for example with values around 50-100ms.";
		
		String minWaitTxt = minWaitTxtA;
		if (getSpecial() == Functionality.DefinedInternallyTriggeredEvent)
			minWaitTxt = minWaitTxtB;
		
		if (uniqueExecIndex >= additionalExecutionValues.length)
			uniqueExecIndex = 50 + visElement.addOptionalParameter(-1, new BooleanOrVariable(String.valueOf(defaultUniqueExec)), "Unique", uniqueTxt);
		else
			visElement.addOptionalParameter(-1, new BooleanOrVariable(String.valueOf(defaultUniqueExec)), "Unique", uniqueTxt);
			
		if (minWaitTimeIndex >= additionalExecutionValues.length)
			minWaitTimeIndex = 50 + visElement.addOptionalParameter(-1, new ValueOrVariable(String.valueOf(defaultMinWaitTime)), "Min Wait", minWaitTxt);
		else
			visElement.addOptionalParameter(-1, new ValueOrVariable(String.valueOf(defaultMinWaitTime)), "Min Wait", minWaitTxt);
	}
	
	public FunctionalityContent removeFixedOptionalArgumentsAndChangeDefault(boolean defaultUniqueExec, double defaultMinWaitTime)
	{
		fixedOptionalArgumentsRemoved = true;
		this.defaultUniqueExec = defaultUniqueExec;
		this.defaultMinWaitTime = defaultMinWaitTime;
		
		return(this);
	}

	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean getUniqueExecution()
	{		
		if (uniqueExecIndex >= 50)
			return(defaultUniqueExec);
		return( (boolean) (additionalExecutionValues[uniqueExecIndex]) );
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public int getMinimumWaitTime()
	{
		if (minWaitTimeIndex >= 50)
			return((int) defaultMinWaitTime);
		return( (int) (double) (additionalExecutionValues[minWaitTimeIndex]) );
	}
	

	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void reloadIfPossible(int index)
	{
		if (index == (uniqueExecIndex-50))
			uniqueExecIndex -= 50;
		
		if (index == minWaitTimeIndex-50)
			minWaitTimeIndex -= 50;
	}

	
	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void resetIfPossible(int index)
	{		
		if (index == uniqueExecIndex)
			uniqueExecIndex += 50;
		
		if (index == minWaitTimeIndex)
			minWaitTimeIndex += 50;
	}
	
	@Override
	public void setSpecialOptionalArgIndex()
	{
		if (uniqueExecIndex > 50)
			uniqueExecIndex = additionalExecutionValues.length;
		else
		if (minWaitTimeIndex > 50)
			minWaitTimeIndex = additionalExecutionValues.length;
	}
	
	@Override
	public boolean checkSpecialFixedArgument(int index)
	{
		if (index == (uniqueExecIndex+getArgumentValues().length))
			return(true);
		if (index == (minWaitTimeIndex+getArgumentValues().length))
			return(true);
		
		return(false);
	}	
	
	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public Callable<Boolean> getEventCondition()
	{
		return(condition);
	}

	
	public EventInstance associatedEventInstance = null;	
	
	public void setAssociatedEventInstance(EventInstance eventInstance)
	{
		associatedEventInstance = eventInstance;
	}
	public EventInstance getAssociatedEventInstance()
	{
		return(associatedEventInstance);
	}
	
	
	
	///// For special events only!
	
	
	// Call to trigger with external input
	public synchronized void triggerExternally(Object[] externalInput)
	{
		int i = 0;
		for(Object inp: externalInput)
			input[i++] = inp;
	
		if (associatedEventInstance != null) // TODO: Find out why this check is needed
			associatedEventInstance.externallyCheckConditionAndPerform(externallyCallableCondition);
	}	
	
	
	/*
	// Call to trigger
	public void triggerExternally()
	{
		if (!resolveVariableArguments(null))
			return;
		
		associatedEventInstance.externallyCheckConditionAndPerform(externallyCallableCondition);
	}
	*/
}
