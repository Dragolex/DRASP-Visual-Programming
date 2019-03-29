package execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import dataTypes.BreakPointResponse;
import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import dataTypes.specialContentValues.FuncLabel;
import dataTypes.specialContentValues.Variable;
import execution.handlers.BreakPointHandler;
import execution.handlers.ExecutionErrorHandler;
import execution.handlers.InfoErrorHandler;
import execution.handlers.LabelHandler;
import execution.handlers.VariableHandler;
import main.functionality.Functionality;
import main.functionality.SharedComponents;
import productionGUI.sections.elements.VisualizableProgramElement;
import settings.ExecutionSettings;
import settings.GlobalSettings;
import staticHelpers.OtherHelpers;

@SuppressWarnings("deprecation")
public class EventInstance
{
	volatile private boolean running = false; // True if still checking. Is volatile because it will be changed from another thread
	
	
	Program program; // associated program
	DataNode<ProgramElement> eventNode;
	ProgramEventContent eventContent; // Content of this event
	String eventDebugIdentifier; // Identifier of the event (only used in the debug-event message)
	VisualizableProgramElement eventVisualized; // Visualized content of this event (only not null if the program runs in the GUI
	
	FunctionalityContent[] directActions; // Array with all directly associated children which will be executed
									// when the event fires. Sub nodes of conditions are NOT in this array
									// but the children of Sub-Events ARE (because events are fired when inside others)
	
	Callable<Boolean> eventCondition;

	
	CountDownLatch precompileLatch, initEventsFirstLatch; 	// Latch to synchronize the execution of all events (after the initialization events)
	
	
	boolean alreadyPrepared = false;

	
	List<Thread> activatedThreads = new ArrayList<>();
	
	
	
	public List<Object> neededParameters = new ArrayList<>();
	private int localSubEventOffset = 0;
	
	EventInstance possibleParentEvent;
	
	
	public List<Object[]> allListsssss = new ArrayList<>();
	
	
	// Standard constructor to run for an event
	EventInstance(DataNode<ProgramElement> eventNode, Program program, EventInstance possibleParentEvent)
	{
		this.eventNode = eventNode;
		if (eventNode.getData() instanceof VisualizableProgramElement) // if it is a visualized element
			eventVisualized = (VisualizableProgramElement) eventNode.getData();
		
		eventContent = (ProgramEventContent) eventNode.getData().getContent(); // Get the content even if it is a VisualizableProgramElement
		eventDebugIdentifier = ExecutionStarter.getProgramIndex(eventContent);
		eventCondition = eventContent.getEventCondition();
		this.program = program;
		this.precompileLatch = program.getPprecompileLatch();
		this.initEventsFirstLatch = program.getInitializationLatch();
		
		
		this.possibleParentEvent = possibleParentEvent;
		updateLocalVariables();
		
		
		eventContent.setAssociatedEventInstance(this);

		//InfoErrorHandler.printExecutionInfoMessage("Created EventHandler");
	}
	
	public void updateLocalVariables()
	{
		if (possibleParentEvent != null)
		{
			neededParameters = possibleParentEvent.neededParameters;
			localSubEventOffset = getContent().getPreparedArgumentsLocalOffset();
		}
		else
		{
			boolean markGlobal = eventContent.isSpecial(Functionality.InitEvent);
			
			ProgramElement[] dat = new ProgramElement[1];
			eventNode.applyToChildrenTotal(dat, () -> {
				//if (!dat[0].getContent().hasPreparedLocalVars())
				{
					
					dat[0].getContent().setPreparedArgumentsLocalOffset(neededParameters.size());
					//neededVariables.add(dat[0].getContent().getArgumentValues());
					for (Object obj: dat[0].getContent().getArgumentValues())
					{
						neededParameters.add(obj);
						if(markGlobal)
						if (!dat[0].getContent().isOutcommented())
						if (obj instanceof Variable)
							((Variable) obj).markAsGlobal(true);
					}
					dat[0].getContent().setPreparedOptionalArgumentsLocalOffset(neededParameters.size());
					//neededVariables.add(dat[0].getContent().getTotalOptionalOrExpandedArgumentsArray());
					for (Object obj: dat[0].getContent().getTotalOptionalOrExpandedArgumentsArray())
					{
						neededParameters.add(obj);
						if(markGlobal)
						if (!dat[0].getContent().isOutcommented())
						if (obj instanceof Variable)
							((Variable) obj).markAsGlobal(true);
					}
					
				}
					
			}, true);
					
		}
	}
	
	
	public void activateEventThread()
	{
		Thread thr = new Thread(() -> activate());
		activatedThreads.add(thr);
		thr.start();
	}
	
	
	private Object[] prepareSetOfLocalArguments()
	{
		Map<Variable, Variable> localVars = new HashMap<>();
				
		int len = neededParameters.size();
		Object[] localCopy = new Object[len];
		localCopy = neededParameters.toArray(localCopy);
		
		
		if (!eventContent.getUniqueExecution() || eventContent.isSpecial(Functionality.LabeledEvent)) // only if execution is not unique
		for(int i = 0; i < len; i++)
		{
			if (neededParameters.get(i) instanceof Variable)
			{
				if (!((Variable) neededParameters.get(i)).isMarkedGlobal()) // if not global
				{
					
					if (localVars.containsKey((Variable) neededParameters.get(i))) // this variable has already been reproduced locally
						localCopy[i] = localVars.get((Variable) neededParameters.get(i));
					else
					{
						localCopy[i] = new Variable();
						localVars.put((Variable) neededParameters.get(i), (Variable) localCopy[i]);			
						if (SharedComponents.DEBUG)
							VariableHandler.addTempMultiVar((Variable) neededParameters.get(i), (Variable) localCopy[i]);
					}
				}
			}
		}
		// Todo: "Pool" this list for more efficiency!
		
		allListsssss.add(localCopy);
		if (allListsssss.size() > 2)
			allListsssss.remove(0);
		
		return(localCopy);
	}
	
	public Object[] prepareSetOfLocalArgumentsForLabeledEvent(Object[] callerLocalArgs, int offsetIndex, int argumentsNumber)
	{
		Object[] localVars = prepareSetOfLocalArguments();
		
		if (argumentsNumber != eventContent.getTotalOptionalOrExpandedArgumentsCount())
		{
			Execution.setError("Number of arguments set to call a Label\ndoes not match with the arguments requried by the label!", false);
			return(null);
		}
		
		int j = offsetIndex;
		for(int i = 0; i < argumentsNumber; i++)
		{			
			if (callerLocalArgs[j] instanceof Double)
				((Variable) localVars[i+1+localSubEventOffset]).initTypeAndSet(Variable.doubleType, callerLocalArgs[j]);
			else
			if (callerLocalArgs[j] instanceof String)
				((Variable) localVars[i+1+localSubEventOffset]).initTypeAndSet(Variable.textType, callerLocalArgs[j]);
			else
			if (callerLocalArgs[j] instanceof Variable)
				((Variable) localVars[i+1+localSubEventOffset]).initTypeAndSet(((Variable) callerLocalArgs[j]).getType(), ((Variable) callerLocalArgs[j]).getUnchecked());
			
			j++;
		}
		
		return(localVars);
	}
	

	
	
	private volatile boolean currentlyExecuting = false;
	private volatile long lastFinishTime = 0;
	
	public void externallyCheckConditionAndPerform(Callable<Boolean> cond)
	{
		Thread thr = new Thread(() -> {
			
			Object[] localArguments = prepareSetOfLocalArguments();
			
			boolean exec;
			
			synchronized(eventContent) // synchronize on this content because otherwise the overwriting of the executionValues array causes issues! Sync has to include everything from variable update to the condition verification.
			{
				if (!eventContent.resolveVariableArguments(localArguments))
					return;			
			
				if (currentlyExecuting)
					if (eventContent.getUniqueExecution()) // abort if already executing and should only run once
						return;
				
				if ((Execution.currentTimeMillisVague() - lastFinishTime) < eventContent.getMinimumWaitTime()) // abort if trying to execute again before the minimum delay hasn't passed
					return;
				
				exec = checkCondition(cond, localArguments);
			}
			
			if (exec)
			{
				if (!eventContent.getUniqueExecution())
					lastFinishTime = Execution.currentTimeMillisVague();					
				
				currentlyExecuting = true;
				
				Execution.addRunningEvent(this);
				performActions(0, localArguments);
				Execution.removeRunningEvent(this);
				
				currentlyExecuting = false;
				
				lastFinishTime = Execution.currentTimeMillisVague();
			}
			
			
		});
		activatedThreads.add(thr);
		thr.start();
	}
	
	

	public void externallyActivateAndPerform(int startIndex, Object[] callerLocalArgs, int offsetIndex, int argumentsNumber)
	{
		Thread thr;
		if (startIndex > -1)
			thr = new Thread(() -> {
				
				Execution.addRunningEvent(this);
				
				Object[] localVars = prepareSetOfLocalArgumentsForLabeledEvent(callerLocalArgs, offsetIndex, argumentsNumber);
				
				if (localVars == null)
					return;
				
				performActions(startIndex, localVars); // Execute the actions of this event starting at the given index
											// This index is needed to allow jumps to labels inside the program
					
				Execution.removeRunningEvent(this);
				
			});
		else
			thr = new Thread(() -> activate(startIndex));
		activatedThreads.add(thr);
		thr.start();
	}
	
	
	
	private void activate()
	{
		activate(-1);
	}
	private void activate(int startIndex)
	{
		//if (startIndex == -1)
			//InfoErrorHandler.printExecutionInfoMessage("STARTED THREAD FOR EVENT: " + eventContent.getFunctionalityName());
		
		Execution.addRunningEvent(this);
		
		
		if (startIndex > -1) // Enforce direct execution
		{
			performActions(startIndex, prepareSetOfLocalArguments()); // Execute the actions of this event starting at the given index
										// This index is needed to allow jumps to labels inside the program
			
			Execution.removeRunningEvent(this);
			return;
		}
		
		
		
		if (alreadyPrepared) // If already prepared
		{
			///// Latch Barrier /////
			OtherHelpers.LatchBarrier(precompileLatch); // Wait until all other precompilations have performed (for the unlikely case that there are still any
			
			startEventConditionCheckLoop(); // Start the event-check and execution loop
		}
		else
		{
			
			// Prepares the nodes for later execution
			InfoErrorHandler.printExecutionInfoMessage("PREPARING EVENT: " + eventContent.getFunctionalityName());
			
			if (eventContent.isSpecial(Functionality.LabeledEvent)) // TODO: Perhaps copy this directly to the core function which creates EventInstancesHandlers
			{
				FuncLabel label = LabelHandler.getByPossibleVariable(eventContent.getArgumentValue(0));
				
				if (!LabelHandler.addLabeledEvent(this, label ))
					ExecutionErrorHandler.showError("Label Problem!", null, eventContent, false, true);
			}
			
			
			
			List<FunctionalityContent> directActionsList = new ArrayList<>();
			List<FunctionalityContent> preparedSubActions = new ArrayList<>();
			
			PreparationHelper.traverseEventNodes(eventNode, directActionsList, preparedSubActions, 0, true);
			
			directActions = new FunctionalityContent[directActionsList.size()];
			directActions = directActionsList.toArray(directActions);
			
			eventContent.setPreparedSubActions(directActions);
			
			
			
			///// Latch Barrier /////
			OtherHelpers.LatchBarrier(precompileLatch); // Wait until all precompilations have performed
			
			
			
			if (!PreparationHelper.performCheckedInitialization(eventContent)) // initialize the event
			{
				Execution.removeRunningEvent(this); // abort if an issue occured
				return;
			}
			
			if (!PreparationHelper.initializeActions(directActions)) // and actions
			{
				Execution.removeRunningEvent(this); // abort if an issue occured
				return;
			}
			
			alreadyPrepared = true;
		}
		
		
		
		if (!Execution.isRunning())
		{
			Execution.removeRunningEvent(this); // abort if program already not running anymore
			return;
		}
		
		
		
		switch(eventContent.getEventType())
		{
		
		case Functionality.Normal:
			
			///// Latch Barrier /////
			OtherHelpers.LatchBarrier(initEventsFirstLatch); // Wait for all other threads to finish preparing initializing and the special initialization-events executed.
			
			/// Executes the event checking-loop.
			startEventConditionCheckLoop(); // It eventually executes the actions of this event when the condition is met
			/// The function ends when the program is aborted
			
			break;		
		
		case Functionality.RhythmStepEvent:
			
			///// Latch Barrier /////
			OtherHelpers.LatchBarrier(initEventsFirstLatch); // Wait for all other threads to finish preparing initializing and the special initialization-events executed.
			Execution.setNoAutoQuit();
			
			new Thread(() ->
			{
				while(Execution.isRunning()) // main while
				{
					Execution.sleepIfPaused();
					
					Object[] localArguments = prepareSetOfLocalArguments();
					
					if (!eventContent.resolveVariableArguments(localArguments)) // Set the variable arguments of this event. Returns false if it failed!
						return; // therefore interrupt the check
					
					Execution.checkedSleep(Math.max(1, (int) (double) eventContent.getArgumentValue(0))); // sleep to wait
					
					if (eventContent.getOptionalArgTrue(0)) // if the event is a skippable type
						if (currentlyExecuting)
							continue; // skip the current step
					
					
					// execute treaded (to avoid disturbing the rhythm of the main while )
					new Thread(() -> {
						
						currentlyExecuting = true;
						
						Execution.addRunningEvent(this);
						performActions(localArguments);
						Execution.removeRunningEvent(this);
						
						currentlyExecuting = false;
						
					}).start();
					
				}
			}).start();
			
			
			break;
			
		/*
		case Functionality.StepEvent: // Implemented differently by now
			
			
			launchLatch.countDown(); // decrease the latch
			Execution.removeRunningEvent(this);			
			
			///// Latch Barrier /////
			OtherHelpers.LatchBarrier(initEventsFirstLatch); // Wait for all other threads to finish preparing initializing and the special initialization-events executed.
			

			new Thread(() ->  {
				while(Execution.isRunning())
				{
					Execution.sleepIfPaused();
					//Execution.checkedSleep(duration);
					
				}
			}).start();
			
			
			break;
		*/
			
		case Functionality.InitEvent:	// If it is an initialization event, that means that it should be executed instantly before decreasing the launchLatch.
			
			program.waitForInitTurn(this);
			
			performActions(prepareSetOfLocalArguments()); // Execute the actions of this event now. Those are the special initialization events which are performing before all other.
			
			program.finishedAnInitEvent(this);
			
			initEventsFirstLatch.countDown(); // decrease the latch but do not wait for it
			
			if (initEventsFirstLatch.getCount() == 0)
				InfoErrorHandler.printExecutionInfoMessage("FINISHED INITIALIZATION EVENTS (Other events running)");
			
			
			break;
		
		case Functionality.DefinedInternallyTriggeredEvent:	// If it is an initialization event, that means that it should be executed instantly before decreasing the launchLatch.
			///// Latch Barrier /////
			OtherHelpers.LatchBarrier(initEventsFirstLatch); // Wait for all other threads to finish preparing initializing and the special initialization-events executed.
			Execution.setNoAutoQuit();
			break;
			
			
			
		default: // other special event types like LabeledEvent. Those are triggered externally
			
			///// Latch Barrier /////
			OtherHelpers.LatchBarrier(initEventsFirstLatch); // Wait for all other threads to finish preparing initializing and the special initialization-events executed.
			
			break;
			
		}
		
		Execution.removeRunningEvent(this); // Remove from the list

	}

	
	
	protected boolean breakNext = false;
	
	private void startEventConditionCheckLoop()
	{
		running = true;
		long lastTimeFirstTrue = Long.MAX_VALUE;
		
		while(running && Execution.isRunning())
		{
			breakNext = false;
			
			Execution.sleepIfPaused();
			
			Object[] localArguments = prepareSetOfLocalArguments();
			
			if (checkCondition(localArguments))
			{
				boolean eventUniquePerform = eventContent.getUniqueExecution(); // get the first argument/variable value which is always telling whether the event may run multiple times in parallel
				int eventMinWaitValue = eventContent.getMinimumWaitTime(); // get the second argument/variable value which is always the minimum wait-duration for any event
				
				
				/*
				if (!eventUniquePerform) // means that multi-execution is allowed
				{
					duplicateThread(eventMinWaitValue); // duplicate the thread (to allow excuting multiple times simultanously)
					performActions(); // Execute the actions of this event
					return; // Quit this loop and thus quit this thread
				}
				performActions(); // Execute the actions of this event (the loop continues though)
				*/
				
				if (lastTimeFirstTrue == Long.MAX_VALUE)
					lastTimeFirstTrue = Execution.currentTimeMillisVague();
				
				// If the "Min Wait" is either (positive) OR (negative AND the time since the last time the condition was false, is larger than abs('Min Wait'))
				if ((eventMinWaitValue >= 0) || ((eventMinWaitValue < 0) && ((Execution.currentTimeMillisVague()+eventMinWaitValue) > lastTimeFirstTrue)))
				{
					if (!eventUniquePerform) // means that multi-execution is allowed
					{
						new Thread(() -> {
						
							Execution.addRunningEvent(this);
							performActions(localArguments);
							Execution.removeRunningEvent(this);
						
						}).start();
					}
					else
						performActions(localArguments);
									
					if (eventMinWaitValue > 0)
						Execution.checkedSleep(eventMinWaitValue); // Sleep the required minimum time before the next check happens and the vent could be executed again
					else
						lastTimeFirstTrue = Execution.currentTimeMillisVague();
				}
				
			}
			else
				lastTimeFirstTrue = Long.MAX_VALUE;
			
		}
		
	}
	
	
	boolean breakOnlyIfConditionTrue = false;
	
	
	public boolean checkCondition(Object[] localArguments)
	{
		return(checkCondition(eventCondition, localArguments));
	}
	
	public boolean checkCondition(Callable<Boolean> cond, Object[] localArguments)
	{
		synchronized(eventContent) // synchronize on this content because otherwise the overwriting of the executionValues array causes issues! Sync has to include everything from variable update to the condition verification.
		{
			if (!eventContent.resolveVariableArguments(localArguments)) // Set the variable arguments of this event. Returns false if it failed!
				return(false); // therefore interrupt the check
				
			try
			{
				boolean res;
				
				if (Execution.isRunningInGUI()
					&& (((ExecutionSettings.breakPointsActive
					&& eventContent.isBreakpoint()) || ExecutionSettings.executeStepByStep)
					&& !breakOnlyIfConditionTrue))
					{
						BreakPointResponse resp = BreakPointHandler.elementPredecessingMessage(eventContent);
						breakNext = resp.breakNext;
						breakOnlyIfConditionTrue = resp.skipToTrueEvent;
						
						res = cond.call();
						if (Execution.hasError())
							throw new Exception();
						
						BreakPointHandler.eventPostMessage(resp, res);					
					}
				else
				{
					res = cond.call();
					
					if (breakOnlyIfConditionTrue)
						if (res)
							BreakPointHandler.elementPredecessingMessage(eventContent);
					
					if (Execution.hasError())
						throw new Exception();
					
				}
				
				
				Thread.sleep(GlobalSettings.constantCheckDelay); // Enforced minimum delay to avoid hogging the cpu load
				return(res);
				
			}
			catch (Exception e)
			{
				String errorType = "";
				if (e instanceof ClassCastException)
					errorType = "\nCasting Error. Can mean that a variable has not been initialized to the correct type.";
				
				if (ExecutionErrorHandler.showError("Checking the condition of an event caused an error!" + errorType, e, eventContent))
				{
					if (eventContent.isVisualized())
						eventContent.getVisualization().applyGUIargumentData();
				}
				else
					running = false;
			}
		}
		return(false);
	}



	protected void stopExecution()
	{
		running = false;
	}
	
	
	public void performActions(Object[] localArguments)
	{
		performActions(0, localArguments);
	}
	public void performActions(int startIndex, Object[] localArguments)
	{
		Execution.sleepIfPaused();
		
		// Very fast execution without debug or visualization checks
		if (Execution.pure)
		{
			new ActionsPerformer(directActions, startIndex, this, localArguments); // Perform the actions
			Execution.sleepIfPaused();
			return;
		}
			
		
		// Execution with checks
		if (Execution.isTracked())
		{
			Execution.print("Element #" + eventContent.getCodeLineIndex() + ": Executing event '" + eventContent.getFunctionalityName() + " " + eventDebugIdentifier + "'");
			
			if (eventContent.hasAnyParameters())
				Execution.print("Element #" + eventContent.getCodeLineIndex() + ": Parameters: " + eventContent.getParametersConcatString());
		}
		
		if (eventVisualized != null) eventVisualized.getControlerOnGUI().markAsExecuting(); // visualize that currently executing
		if (GlobalSettings.eventDebug) Execution.print( GlobalSettings.startExecutionSignal + eventDebugIdentifier);
 		
		new ActionsPerformer(directActions, startIndex, this, localArguments); // Perform the actions
		
		Execution.sleepIfPaused();
		
		if (GlobalSettings.eventDebug) Execution.print( GlobalSettings.endEventSignal + eventDebugIdentifier);
		if (eventVisualized != null) eventVisualized.getControlerOnGUI().fadeoutMarking(); // fadeout visualization
		
		if (Execution.isTracked())
			Execution.print("Element #" + eventContent.getCodeLineIndex() + ": Finished executing event '" + eventContent.getFunctionalityName() + " #" + eventDebugIdentifier + "'");
	}
	
	


	public FunctionalityContent getContent()
	{
		return(eventContent);
	}


	// forcefully stops the thread (to go sure and only used after asking the user)
	public void stopThread()
	{
		for(Thread thr: activatedThreads)
			if (thr.isAlive())
				thr.stop();
		
		activatedThreads.clear();
	}

	
	

	
	
	
	
	
	
	
	/*
	 * This recursive function allows to go through all events inside the tree starting at a given root-node (which has to be an event itself)
	 * It counts the number of nodes (which are the actions inside the events) and adds them to the array if the argument "addContentToArray" is true.
	 * The number of traversed nodes is returned.
	 * 
	 * The event nodes themselves are ignored and sub-nodes of conditional contents are not added to the main array either
	 * Additionally if "prepareConditionalActionArrays" is false, actions which are conditional actions, have their sub-action number set to create their array.
	 * If "prepareConditionalActionArrays" is true, the contents of the corresponding children are added to the array inside the node
	 * 
	 * Attention, this function only makes sense when executing twice - ocne with the booleans false and once with the booleans true
	 * The first time has the purpose to know how to size the "directActions" array and prepares the sub-action arrays
	 * and the second time actually fills both kinds of arrays.
	 */
	/*
	public int traverseEventNodes(DataNode<ProgrammElement> node, int index, boolean addContentToArray, boolean prepareConditionalActionArrays)
	{
		
		if (node.getData().isEvent()) // if it is an event
		{
			index++;
			int subcount = 0;
			
			for(DataNode<ProgramElement> childNode: node.getChildrenAlways()) // Directly traverse through sub-children
				subcount += traverseEventNodes(childNode, index+subcount, addContentToArray, prepareConditionalActionArrays); // add the sum of the subcount to the coutner variable
			
			return(subcount+1); // return the number of traversed elements so far
		}
		else // if not an event
		{
			if (addContentToArray)
				directActions[index] = node.getData().getContent(); // add to the object-wide array
			
			index++;
			
			
			if (node.getData().isConditionalAction()) // if it is a conditional action
			{
				int subcount = 0;
				
				for(DataNode<ProgrammElement> childNode: node.getChildrenAlways()) // Directly traverse through subchildren
					subcount += traverseEventNodes(childNode, subcount, false, prepareConditionalActionArrays); // Recursively continue like above, however "addContentToArray" is FALSE this time

				
				if (!prepareConditionalActionArrays) // if told not to prepare the action arrays yet
				{
					if (node.getParent().getData().getContent().getPreparedSubActions() == null) // if no preparation available yet
						node.getData().getContent().setPreparedSubActions(new ProgramContent[subcount]); // Create and set a new array of subactions
				}				
				
				return(subcount+1); // return the number of traversed elements so far
			}
			else // "else" here means the element is neither an even nor a conditional action -> simple action not owning any children
			{
				if (prepareConditionalActionArrays) // only relevant when true
				{
					if (node.getParent().getData() == null)
						return(1); // Continue only if the parent contains a node (just to go sure)

					if (node.getParent().getData().isConditionalAction()) // if the parent is a conditional action
					{
						int subind = node.getParent().getChildrenAlways().indexOf(node); // Get the index inside the node
																						 // Computing this might be possible to avoid by temporarily storing idices in the ProgramContent
																						 // But this solution is smoother and still fast enough.
						
						ProgramContent[] subActions = node.getParent().getData().getContent().getPreparedSubActions(); // get the array of sub-actions from the ProgramContent
						if (subActions[subind] == null)
							subActions[subind] = node.getData().getContent();
						else
						{
							// Debug check
							if (subActions[subind] != node.getData().getContent())
								InfoErrorHandler.callBugError("Precompilation Problem in EventHandler. Subcontents do not match when applying a second time. Index: " + subind);
						}
					}
				}
			}
			
			
			return(1);
		}
		
	}
	
	*/

	
	
	
	
	
	
	
	/*
	
	private void execute()
	{
		InfoErrorHandler.printExecutionInfoMessage("Executing events");

		
		/*
		int repeatStartIndex = 0;
		
		for(int i = 0; i < actions.size(); i++)
		{			
			actions.get(i).perform();
			
			//
			if (actions.get(i).isRepeatStart())
				repeatStartIndex = i;
			
			if (actions.get(i).isRepeatEnd())
				i = repeatStartIndex-1;
			//
			// This systems allows repeat-blocks (also working as an an event-end)
		}
		
	}
	
	*/
}
