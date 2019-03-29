package dataTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import dataTypes.exceptions.AccessUnsetVariableException;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.handlers.ExecutionErrorHandler;
import execution.handlers.InfoErrorHandler;
import main.functionality.Functionality;
import main.functionality.SharedComponents;
import main.functionality.Structures;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.OtherHelpers;


public class FunctionalityContent extends ProgramElement
{
	private String functionalityName;
	
	private Runnable initialization, executation, nextIterationExecution;
	protected Callable<Boolean> condition;
	
	private boolean initialized = false;

	volatile private Object[] executionValues;
	protected volatile Object[] additionalExecutionValues = new Object[0];
	
	private boolean[] argumentIsConstant;
	private boolean[] additionalArgumentIsConstant;


	int codeLineIndex;
	String codePageName = "";
	
	
	protected boolean markedAsEvent = false;

	protected boolean markedAsCondition = false;

	private boolean undeletableNode = false;
	
	private boolean outcommented = false;
	
	protected int specialVariant = Functionality.Normal;
	
	
	
	// Only for execution
	private FunctionalityContent[] preparedSubActions;

	
	private VisualizableProgramElement visualisation = null;
	private boolean collapsedInitialized = false;
	private boolean beenTraversed = false;
	
    private boolean isBreakpoint = false;
    
    protected boolean fixedOptionalArgumentsRemoved = false;
    
	protected boolean forTutorial = false;
	
	private boolean needsFunctionalityName = true;

	
	// Create a "uselessElement". This is only for style purposes to allow visualizing those on the GUI.
	// Since those style nodes are never draggable, they do not appear within the program queue.
	public FunctionalityContent(String visualizerName)
	{
		this.functionalityName = visualizerName;
		needsFunctionalityName = false;
	}
	

	// Normal action
	public FunctionalityContent(String visualizerName, Object[] arguments, Runnable executation)
	{
		visualizerName = SharedComponents.extractFunctionalityName(3);
		
		//String name = sun.reflect.Reflection.getCallerClass(2).getName();

		
		executionValues = arguments;
		
		this.functionalityName = visualizerName;
		
		this.executation = executation;
	}
	public FunctionalityContent(String visualizerName, Object[] arguments, Runnable initialization, Runnable executation) // with initialization
	{
		executionValues = arguments;
		
		this.functionalityName = visualizerName;
		
		this.initialization = initialization;
		this.executation = executation;
	}
	
	
	
	// Action variant for loop
	public FunctionalityContent(String visualizerName, Object[] arguments, Runnable loopStart,
			Callable<Boolean> condition, Runnable nextIteration)
	{
		executionValues = arguments;
		
		this.functionalityName = visualizerName;
		
		this.executation = loopStart;
		this.condition = condition;
		this.nextIterationExecution = nextIteration;
		
		specialVariant = Functionality.ExtraDefinedLoop;
	}
	
	public FunctionalityContent(String visualizerName, Object[] arguments, Runnable initialization, Runnable loopStart, // with initialization
			Callable<Boolean> condition, Runnable nextIteration)
	{	
		executionValues = arguments;
		
		this.functionalityName = visualizerName;
		
		this.initialization = initialization;
		this.executation = loopStart;
		this.condition = condition;
		this.nextIterationExecution = nextIteration;
		
		specialVariant = Functionality.ExtraDefinedLoop;
	}
	
	
	
	// Special variants
	
	/*
	 * Action variants:
	 * 
	 * 1 -> Is Event-repeater
	 * 2 -> Is Indentation-repeater
	 * 3 -> Is target label
	 * 4 -> Is jumper to target label
 	 * 5 -> Is setter for variable
	 * 6 -> Is if clause for variable
	 * 7 -> Else
	 * 
	 * 
	 * Event variants
	 * 
	 * 101 -> Is Init event
	 * */

	public FunctionalityContent(String visualizerName, int specialVariant, int argCount)
	{
		this.functionalityName = visualizerName;
		
		this.specialVariant = specialVariant;
		executionValues = new Object[argCount];
		
		if (specialVariant >= 100)
			markedAsEvent = true;
	}
	
	
	
	
	public Object[] getArgumentValues()
	{
		return(executionValues);
	}
	
	public Object getArgumentValue(int index)
	{
		return(executionValues[index]);
	}
	
	
	public boolean hasOptionalArgument(int index)
	{
		return(index < additionalExecutionValues.length);
	}

	public Object getOptionalArgumentValue(int index)
	{
		return(additionalExecutionValues[index]);
	}
	public Object getOptionalArgumentValueOR(int index, Object def)
	{
		if (index < additionalExecutionValues.length)
			return(additionalExecutionValues[index]);
		else
			return(def);
	}	
	public int getOptionalArgumentValueOR(int index, int def)
	{
		if (index < additionalExecutionValues.length)
			return((int) (double) additionalExecutionValues[index]);
		else
			return(def);
	}	
	
	
	public Object[] getTotalOptionalOrExpandedArgumentsArray()
	{
		return(additionalExecutionValues);
	}
	
	public int getTotalOptionalOrExpandedArgumentsCount()
	{
		return(additionalExecutionValues.length);
	}
	
	public boolean getOptionalArgTrue(int index)
	{
		return((index < additionalExecutionValues.length) && (boolean) additionalExecutionValues[index]);
	}	
	
	public FunctionalityContent removeFixedOptionalArguments()
	{
		fixedOptionalArgumentsRemoved = true;
		return(this);
	}
	
	
	public boolean getArgumentIsConstant(int index)
	{
		return(argumentIsConstant[index]);
	}
	public boolean getAdditionalArgumentIsConstant(int index)
	{
		return(additionalArgumentIsConstant[index]);
	}
	
	
	
	
	
	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isEvent()
	{
		return(markedAsEvent);
	}
	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isConditionalElement()
	{
		return(markedAsCondition);
	}

	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public String makeStringRepresentation()
	{
		return null;
	}
	
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!	
	public boolean isInitialized()
	{
		if (initialization == null)
			return(true);
		
		return(initialized);
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public Runnable getInitialization()
	{
		initialized = true;
		return(initialization);
	}

	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public Runnable getExecutation()
	{
		return(executation);
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean callCondition() throws Exception
	{
		if (condition == null)
		{
			InfoErrorHandler.callBugError("Trying to call condition of a non-ConditionContent!");
			return(false);
		}
		else
			return(condition.call());
	}
	


	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public Runnable getNextIterationExecution()
	{
		return(nextIterationExecution);
	}
	
	
	
	List<Integer> variableIndices = new ArrayList<>();
	List<Variable> variables = new ArrayList<>();
	int firstExpandedIndex = 0;
	
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void clearSetArguments()
	{
		variableIndices.clear();
		variables.clear();
		firstExpandedIndex = 0;
		localVarIndOffs = -1;
		localOptVarIndOffs = -1;
	}
	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setArgumentDirect(int index, Object argumentValue, boolean passesAsRealVar)
	{
		int vals = executionValues.length;
		
		
		if (index < vals)
		{
			if (argumentIsConstant == null)
				argumentIsConstant = new boolean[vals];
			
			
			executionValues[index] = argumentValue;
			
			
			if (argumentValue instanceof Variable)
			if (!passesAsRealVar)
			{
				int pos = variableIndices.indexOf(index);
				if (pos > -1)
					variables.set(pos, (Variable) argumentValue);
				else
				{
					variableIndices.add(index);
					variables.add((Variable) argumentValue);
					firstExpandedIndex++;
				}
			}
			
			// TRUE if the argument is constant - that means it is NOT a variable and is not a Term containing a variable.
			argumentIsConstant[index] = (!(argumentValue instanceof Variable)) && (!((argumentValue instanceof Term) && (((Term) argumentValue).getRightSideType() == Variable.variableType) ));
			
		}
		else
		{
			additionalArgumentIsConstant = OtherHelpers.largenArray(additionalArgumentIsConstant, Math.max(additionalExecutionValues.length, (index-vals)+1), ((index-vals) >= 0));
			
			additionalExecutionValues = OtherHelpers.largenArray(additionalExecutionValues, Math.max(additionalExecutionValues.length, (index-vals)+1), ((index-vals) >= 0));

			
			/*
			if (additionalExecutionValues.length == 0)
				additionalExecutionValues = new Object[1];
			else
				if ((index-vals) >= 0)
					additionalExecutionValues = java.util.Arrays.copyOf(additionalExecutionValues, Math.max(additionalExecutionValues.length, (index-vals)+1)); // Resize
			*/
			
			additionalExecutionValues[index-vals] = argumentValue;
			
			
			if (argumentValue instanceof Variable)
			if (!passesAsRealVar)
			{
				int pos = variableIndices.indexOf(index);
				if (pos > -1)
					variables.set(pos, (Variable) argumentValue);
				else
				{
					variableIndices.add(index);
					variables.add((Variable) argumentValue);
				}
			}
			
			// TRUE if the argument is constant - that means it is NOT a variable and is not a Term containing a variable.
			additionalArgumentIsConstant[index-vals] = (!(argumentValue instanceof Variable)) && (!((argumentValue instanceof Term) && (((Term) argumentValue).getRightSideType() == Variable.variableType) ));
			
			
		}
	}
	
	
	int localVarIndOffs = -1;
	int localOptVarIndOffs = -1;
	
	
	/*
	public boolean hasPreparedLocalVars()
	{
		return(localVarIndOffs >= 0);
	}
	*/
	
	public void setPreparedArgumentsLocalOffset(int ind)
	{		
		localVarIndOffs = ind;
	}
	
	public void setPreparedOptionalArgumentsLocalOffset(int ind)
	{
		localOptVarIndOffs = ind;
	}
	
	public int getPreparedArgumentsLocalOffset()
	{
		return(localVarIndOffs);
	}
	
	public int getPreparedOptionalArgumentsLocalOffset()
	{
		return(localOptVarIndOffs);
	}
	
	
	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean resolveVariableArguments(Object[] localArguments)
	{
		for (int i = 0; i < executionValues.length; i++)
			executionValues[i] = localArguments[localVarIndOffs + i];
		for (int i = 0; i < additionalExecutionValues.length; i++)
			additionalExecutionValues[i] = localArguments[localOptVarIndOffs + i];
		
		
		boolean retry = false;
		do
		{
			retry = false;			
			
			int i = 0;
			try {
				
				for(i = 0; i < firstExpandedIndex; i++)
					executionValues[variableIndices.get(i)] = ((Variable) localArguments[localVarIndOffs + variableIndices.get(i)]).get();  //variables.get(i).get();
				
				for(i = firstExpandedIndex; i < variableIndices.size(); i++)
					additionalExecutionValues[variableIndices.get(i)-executionValues.length] = ((Variable) localArguments[localOptVarIndOffs + (variableIndices.get(i)-executionValues.length)]).get(); //variables.get(i).get();
			
			} catch (AccessUnsetVariableException e)
			{
				int res = 0;
				if (visualisation != null)
					res = ExecutionErrorHandler.showError("Attempting to access a variable which has not been set to a value yet.\nThe problematic argument is number " + variableIndices.get(i) + ", named '" + visualisation.getArgumentDescriptions()[variableIndices.get(i)] + "'.\nDid you provide the right variable?", e, this, true, false);
				else
					res = ExecutionErrorHandler.showError("Attempting to access a variable which has not been set to a value yet.\nThe problematic argument is number " + variableIndices.get(i) + ". Did you provide the right variable?", e, this, true, false);
				
				if (res == 1)
					retry = true;
				else
					return(false);
			}
			catch (NullPointerException e)
			{
				int res = ExecutionErrorHandler.showError("Evaluating an argument resulted in an error! Index: " + i, e, this, true, false);
				if (res == 1)
					retry = true;
				else
					return(false);			
			}
		}
		while(retry);
		
		return(true);
	}
	
	// Only used for the initialization event mechanism!
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void resolveVariableArgumentsWithoutErrorButSet(double newVal)
	{
		int i = 0;
		for(i = 0; i < firstExpandedIndex; i++)
		{
			if (variables.get(i).hasValue())
				executionValues[variableIndices.get(i)] = variables.get(i).getUnchecked();
			else
				executionValues[variableIndices.get(i)] = newVal;
		}
				
		for(i = firstExpandedIndex; i < variableIndices.size(); i++)
		{
			if (variables.get(i).hasValue())
				additionalExecutionValues[variableIndices.get(i)-executionValues.length] = variables.get(i).getUnchecked();
			else
				additionalExecutionValues[variableIndices.get(i)-executionValues.length] = newVal;
		}
	}
	
	
	
	
	// Expanded arguments

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void addAdditionalArgument()
	{
		reloadIfPossible(additionalExecutionValues.length);
		
		if (additionalExecutionValues == null)
			additionalExecutionValues = new Object[1];
		else
			additionalExecutionValues = java.util.Arrays.copyOf(additionalExecutionValues, additionalExecutionValues.length+1); // Resize
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void removeExpandedArgument()
	{
		resetIfPossible(additionalExecutionValues.length-1);
		
		if (additionalExecutionValues.length == 1)
			additionalExecutionValues = new Object[0];
		else
			additionalExecutionValues = java.util.Arrays.copyOf(additionalExecutionValues, additionalExecutionValues.length-1); // Resize		
	}

	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void resetIfPossible(int index) {}
	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void reloadIfPossible(int index) {}

	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	@Override
	public String getFunctionalityName()
	{
		return(functionalityName);
	}
	
	
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean canHaveChildElements()
	{
		return(markedAsCondition || markedAsEvent || isSpecial(Functionality.Comment) || isSpecial(Functionality.ForLoop) || isSpecial(Functionality.ListLoop) || isSpecial(Functionality.ElseClause) || isSpecial(Functionality.ExtraDefinedLoop));
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean canHaveUserAddedChildElements()
	{
		if (getContent().getVisualization().getControlerOnGUI().hidesChildren())
			return(false);		
		return((canHaveChildElements() && !isUndeletable()) || visualisation.getIsUserModifiableParentNode());
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setUndeletable(boolean undeletableNode)
	{
		this.undeletableNode = undeletableNode;
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isUndeletable()
	{
		return(undeletableNode);
	}


	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public FunctionalityContent getContent()
	{
		return(this);
	}

	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setPreparedSubActions(FunctionalityContent[] preparedSubActions)
	{
		this.preparedSubActions = preparedSubActions;
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public FunctionalityContent[] getPreparedSubActions()
	{
		return(preparedSubActions);
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean hasPreparedSubactions()
	{
		return(preparedSubActions != null);
	}

	/*
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isInitializationEvent()
	{
		return(isSpecial(Functionality.InitEvent));
	}*/

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isSpecial(int variant)
	{
		return(variant == specialVariant);
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public int getEventType()
	{
		return(specialVariant);
	}

	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public int getSpecial()
	{
		return(specialVariant);
	}
	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setCodeLineIndex(int codeLineIndex)
	{
		this.codeLineIndex = codeLineIndex;
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public int getCodeLineIndex()
	{
		return(codeLineIndex);
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setCodePageName(String pageName)
	{
		codePageName = pageName;
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public String getCodePageName()
	{
		return(codePageName);
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isVisualized()
	{
		return(visualisation != null);
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setVisualization(VisualizableProgramElement visualisation)
	{
		this.visualisation = visualisation;
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public VisualizableProgramElement getVisualization()
	{
		return(visualisation);
	}
	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setOutcommented(boolean mode)
	{
		if(false)
		if (Execution.isRunning())
		{
			DataNode<ProgramElement> par = getVisualization().getNode().getParent();
			while(!par.isRoot())
			{
				if (par.getData().getContent() instanceof ProgramEventContent)
				{
					((ProgramEventContent) par.getData().getContent()).associatedEventInstance.updateLocalVariables();
				}
				par = par.getParent();
			}
			
			par = getVisualization().getNode().getParent();
			while(!par.isRoot())
			{
				if (par.getData().getContent() instanceof ProgramEventContent)
				{
					((ProgramEventContent) par.getData().getContent()).associatedEventInstance.updateLocalVariables();
				}
				par = par.getParent();
			}

		}
		
		outcommented = mode;		
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isOutcommented()
	{
		return(outcommented);
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setCollapsedInitialized(boolean collapsed)
	{
		collapsedInitialized = collapsed;
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isCollapsedInitialized()
	{
		return(collapsedInitialized);
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setBeenTraversed(boolean beenTraversed)
	{
		this.beenTraversed = beenTraversed;
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean hasBeenTraversed()
	{
		return(beenTraversed);
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void addFixedOptionalArguments(VisualizableProgramElement visElement)
	{
		// Empty but can be overridden
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setBreakpoint(boolean isBreakpoint)
	{
		this.isBreakpoint = isBreakpoint;
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isBreakpoint()
	{
		return(isBreakpoint);
	}
	
	
	public void setSpecialOptionalArgIndex()
	{
		// Empty but can be overridden!
	}

	public boolean checkSpecialFixedArgument(int index)
	{
		return(false);
		// Empty but can be overridden!
	}
	
	
	// Can be useful to verify whether the parameters have been set correctly
	public String getParametersConcatString()
	{
		return(OtherHelpers.listArrayHorizontally("- Param #: ", 0, getArgumentValues()) +
				OtherHelpers.listArrayHorizontally("- Param #: ", getArgumentValues().length, getTotalOptionalOrExpandedArgumentsArray()));
	}

	public boolean hasAnyParameters()
	{
		return((getArgumentValues() != null && getArgumentValues().length>0) || ( getTotalOptionalOrExpandedArgumentsArray()!=null && getTotalOptionalOrExpandedArgumentsArray().length>0));
	}
	
	
	@Override
	public String toString()
	{
		return(getFunctionalityName());
	}

	
	
	/*
	@Override
	public ProgramElement recreateContent()
	{
		ProgramContent newContent = Functionality.createProgramContent(getFunctionalityName());
		
		newContent.setVisualization(visualisation);
		//System.arraycopy(executionValues, 0, newContent.getArgumentValues(), 0, executionValues.length);
		
		return(newContent);		
	}
	*/

}
