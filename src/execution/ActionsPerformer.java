package execution;

import java.util.ArrayList;
import java.util.List;

import dataTypes.BreakPointResponse;
import dataTypes.FunctionalityContent;
import dataTypes.specialContentValues.DataList;
import dataTypes.specialContentValues.FuncLabel;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.handlers.BreakPointHandler;
import execution.handlers.ExecutionErrorHandler;
import main.functionality.Functionality;
import main.functionality.helperControlers.AlarmControler;
import productionGUI.sections.elements.ProgramElementOnGUI;
import settings.ExecutionSettings;
import settings.GlobalSettings;


@SuppressWarnings("deprecation")
public class ActionsPerformer
{
	
	int jumpLevels = -1; // variables required for special key features
	boolean jumpReCheck = false;
	FunctionalityContent jumpPossibleErrorSource;
	
	EventInstance thisEvent;
	
	static final int successful = -10000;
	
	
	final boolean isVisualized;
	final boolean isTracked;
	
	
	Object[] localArguments;
	
	
	protected ActionsPerformer(FunctionalityContent[] directActions, int startIndex, EventInstance thisEvent, Object[] localArguments)
	{
		this.isVisualized = Execution.isRunningInGUI();
		this.isTracked = Execution.isTracked();
		this.thisEvent = thisEvent;
		this.localArguments = localArguments;
		
		
		if (thisEvent.breakNext)
			breakNext = true;
		
		boolean repeat = false;
		do
		{
			repeat = false;
			
			switch(performActions(directActions, startIndex, startIndex==0))
			{
			case Functionality.EventRepeater: repeat = true; break;
			
			case Functionality.ClauseRepeater:			
				if (jumpLevels > 0) // if still needing to jump levels
				{
					jumpLevels = 0;
					Execution.setError("Trying to repeat more Condition-Blocks ('Levels') than possible!\nNote that this feature works only within the content of one event.\nIf you want to jump across event borders,\nuse 'Labels' instead.", false);
					ExecutionErrorHandler.showError("A clause repeater caused an error!", null, jumpPossibleErrorSource, false, true);
					return; // Quit because those are absolute errors
				}
				else
				{
					jumpLevels = 0;
					repeat = true;
				}
				break;
			
			case Functionality.JumpOutOf:
				jumpLevels--;
				if (jumpLevels > 0) // if still needing to jump levels
				{
					Execution.setError("Trying to jump out of more Condition-Blocks than possible!\nNote that this feature works only within the content of one event.\nIf you want to jump across event borders,\nuse 'Labels' instead.", false);
					ExecutionErrorHandler.showError("An Exit Block caused an error!", null, jumpPossibleErrorSource, false, true);
					return;
				}
				break;
				// else just continue normally (repeat stays false)
				
			}
			
			startIndex = 0;
		}
		while(repeat && Execution.isRunning());
	}

	//List<Integer> commentEndIndices = new ArrayList<>();
	
	
	private int performActions(FunctionalityContent[] actions)
	{
		return(performActions(actions, 0, false));
	}
	private int performActions(FunctionalityContent[] actions, int startIndex, boolean directEventStart)
	{		
		boolean insideForLoop = false;
		Variable forLoopVariable = null;
		
		DataList listLoopList = null;
		Variable listLoopOutputVar = null;
		Variable listLoopIndexVar = null;
		int insideLoopIndex = -1;
		
		int nextCanBeElse = 0;
		
		List<Integer> commentStartIndices = new ArrayList<>();
		
		
		int actionsCount = actions.length;
		for(int i = 0; i < actionsCount; i++)		
		{
			if (!Execution.isRunning())
				return(Functionality.StoppedAction);
			
			Execution.sleepIfPaused();
			
			nextCanBeElse--;
			
			if (actions[i] == null)
			{
				if (i != 0)
				if (!commentStartIndices.contains(i))
					commentStartIndices.add(i);
				continue;
			}
			
			if (i < startIndex) continue;
			
			
			if (actions[i].isOutcommented())
				continue;
			
			
			//synchronized(actions[i]) // synchronize on this content because otherwise the overwriting of the executionValues array causes issues! Sync has to include everything from variable update to the condition verification.
			{
				
				if (!actions[i].resolveVariableArguments(localArguments)) // Set the variable arguments of this element. Returns false if it failed!
				{
					if (actions[i].isConditionalElement()) // if a conditional action
						if (i < actionsCount-1) // if not the last element
							if (actions[i+1].isSpecial(Functionality.ElseClause)) // if the next element is an else clause
								i++; // Increase the index to skip this else too	
					
					continue;
				}
				
				
				boolean executeSubActions = false;
				
				visExec(actions[i]);
				
				if (actions[i].isConditionalElement()) // Check whether it is a conditional element
				{
					
					boolean retry = false;
					do
					{
						retry = false;
						
						try
						{
							if ((i < actionsCount-1) && (actions[i+1] != null) && (actions[i+1].isSpecial(Functionality.ElseClause))) // has else-counterpart
							{
								if (checkCondition(actions[i]))
								{
									executeSubActions = true;
									nextCanBeElse = 2;
								}
								else
								{
									executeSubActions = true;
									forceEndVisExec(actions[i]);
									visExec(actions[i+1]);
									i++; // Increase the index to execute the subactions of the else clause and skip it afterwards in the main loop.
								}
								
							}
							else
							if (checkCondition(actions[i]))
								executeSubActions = true;
							
							if (Execution.hasError())
								throw new Exception();							
						}
						catch (Exception e)
						{
							if (!Execution.hasError())
								Execution.setError(e.getLocalizedMessage(), false);
							
							switch(ExecutionErrorHandler.showError("Checking a conditional action caused an error!", e, actions[i], true, false))
							{
								case 1:
									if (actions[i].isVisualized())
										actions[i].getVisualization().applyGUIargumentData();
									actions[i].resolveVariableArguments(localArguments);
									retry = true;
								break;
								case 0:
									retry = false; // skip the action
									
									if (i < actionsCount-1) // if not the last element
									if (actions[i+1].isSpecial(Functionality.ElseClause)) // if the next element is an else clause
										i++; // Increase the index to skip this else too							
								break;
								case -1:
									endVisExec();
									return(Functionality.StoppedAction);
							}
						}
					}
					while(retry);
					
				}
				else
				{
					switch(actions[i].getSpecial())
					{
					case Functionality.Normal: // normal
						{
							int res = executeCheckedAction(actions[i], actions[i].getExecutation());
							if (res != successful)
								return(res);
						}
						break;
						
					case Functionality.ElseClause:
						if (nextCanBeElse > 0)
						{
							nextCanBeElse = 0;
							forceEndVisExec(actions[i]);
						}
						else
						{
							Execution.setError("Else Block without a corresponding condition!", false);
							ExecutionErrorHandler.showError("Incorrect Else Block!", null, actions[i], false, false);
						}
						break;
						
					case Functionality.EventRepeater:
						endVisExec();
						insideForLoop = false;
						insideLoopIndex = -1;
						return(Functionality.EventRepeater);
	
					case Functionality.ClauseRepeater:
						
						jumpLevels = (int) (double) actions[i].getArgumentValue(0);
						jumpReCheck = (boolean) actions[i].getArgumentValue(1);
						
						jumpPossibleErrorSource = actions[i];
						
						insideForLoop = false;
						
						int r = i;
						boolean inner = true;
						
						if (commentStartIndices.size() > jumpLevels)
						{
							i = commentStartIndices.get(commentStartIndices.size()-1-jumpLevels);
							inner = false;
						}
	
						jumpLevels -= commentStartIndices.size();
						
						
						if ((jumpLevels == 0) && ((!directEventStart) && (!jumpReCheck)))
							i = -1;
						else
						if (inner || (jumpLevels > 0))
						{
							
							if (!jumpReCheck || (!directEventStart) || (directEventStart && thisEvent.checkCondition(localArguments)))
							{
								endVisExec();
								jumpLevels--;
								if (thisEvent.breakNext) breakNext = true;
								return(Functionality.ClauseRepeater);
							}
							else
							{
								i = r;
								if (thisEvent.breakNext) breakNext = true;
							}
						}
						
	
						break;
					
					case Functionality.EventQuitter:
						endVisExec();
						return(Functionality.EventQuitter);
						
					case Functionality.LoopQuitter:
						endVisExec();
						insideForLoop = false;
						forLoopVariable = null;
						insideLoopIndex = -1;
						return(Functionality.LoopQuitter);
	
						
					case Functionality.JumpOutOf:
						jumpLevels = (int) (double) actions[i].getArgumentValue(0);
						jumpPossibleErrorSource = actions[i];
						
						/*
						if (commentStartIndices.size() > jumpLevels)
						{
							i = commentEndIndices.get(commentStartIndices.size()-1-jumpLevels);
							inner = false;
						}
						
						if (jumpLevels > 0)
						{
							jumpLevels--;
							
						}
						*/
						
						// TODO
						/*
						while(!depthIndices.isEmpty() && ((jumpLevels--)>0))
							i = depthIndices.pop();
						
						if (jumpLevels > 0)
						{
							endVisExec();
							return(Functionality.JumpOutOf);
						}
						if (jumpLevels == 0)
						{
							i = 0;
						*/
						
						
						
						endVisExec();
						return(Functionality.JumpOutOf);
					
					case Functionality.LabeledPosition:
						// Just continue
						break;
					
						
					case Functionality.LabelJump:
						
						FunctionalityContent cont = actions[i];
						
						if (((FuncLabel) cont.getArgumentValue(0)).getEvent() == thisEvent)
							i = ((FuncLabel) cont.getArgumentValue(0)).getPositionIndex();
						else
						{
							((FuncLabel) cont.getArgumentValue(0)).getEvent()
								.externallyActivateAndPerform(((FuncLabel) cont.getArgumentValue(0)).getPositionIndex(), localArguments, cont.getPreparedOptionalArgumentsLocalOffset(), cont.getTotalOptionalOrExpandedArgumentsCount());
							
							endVisExec();
							return(Functionality.StoppedAction);
						}
						
						break;
						
					case Functionality.LabelExecute:
						if ((boolean) actions[i].getArgumentValue(1)) // true means wait
						{
							//LabelHandler.executeLabelDirect((int) actions[i].getArgumentValue(0)); // the number is the label index
							
							EventInstance ev = ((FuncLabel) actions[i].getArgumentValue(0)).getEvent();
							
							Object[] newLocalArgs = ev.prepareSetOfLocalArgumentsForLabeledEvent(localArguments, actions[i].getPreparedOptionalArgumentsLocalOffset(), actions[i].getTotalOptionalOrExpandedArgumentsCount());
							
							if (((FuncLabel) actions[i].getArgumentValue(0)).getPositionIndex() == null)
								ev.performActions(newLocalArgs);
							else
								ev.performActions(((FuncLabel) actions[i].getArgumentValue(0)).getPositionIndex(), newLocalArgs);
							
							//LabelHandler.executeLabelDirect((FuncLabel) actions[i].getArgumentValue(0), actions[i].getTotalOptionalOrExpandedArgumentsArray()); // the number is the label index
						}
						else
						{
							//LabelHandler.executeLabelThreaded((int) actions[i].getArgumentValue(0));
							((FuncLabel) actions[i].getArgumentValue(0)).getEvent()
								.externallyActivateAndPerform(((FuncLabel) actions[i].getArgumentValue(0)).getPositionIndex(), localArguments, actions[i].getPreparedOptionalArgumentsLocalOffset(), actions[i].getTotalOptionalOrExpandedArgumentsCount());

						}
						break;
						
					case Functionality.LabelAlarm:
						AlarmControler.setAlarm((FuncLabel) actions[i].getArgumentValue(0), (double) actions[i].getArgumentValue(1), (boolean) actions[i].getArgumentValue(2),
								localArguments, actions[i].getPreparedOptionalArgumentsLocalOffset(), actions[i].getTotalOptionalOrExpandedArgumentsCount());
						break;					
					
						
					case Functionality.ExtraDefinedLoop: // Universal loop
						
						if (insideLoopIndex < 0)
						{
							int res = executeCheckedAction(actions[i], actions[i].getExecutation());
							if (res != successful) return(res);
							
							insideLoopIndex = 0;
						}
						else
						{
							int res = executeCheckedAction(actions[i], actions[i].getNextIterationExecution());
							if (res != successful) return(res);
						}
						
						
						boolean retry = false;
						do
						{
							retry = false;
							executeSubActions = false;
							
							try
							{
								if (checkCondition(actions[i]))
									executeSubActions = true;
								else
									insideLoopIndex = -1;
								
								if (Execution.hasError())
									throw new Exception();
							}
							catch (Exception e)
							{
								Execution.setError(e.getLocalizedMessage(), false);
								
								switch(ExecutionErrorHandler.showError("Checking a conditional action caused an error!", e, actions[i], true, false))
								{
									case 1:
										if (actions[i].isVisualized())
											actions[i].getVisualization().applyGUIargumentData();
										actions[i].resolveVariableArguments(localArguments);
										retry = true;
									break;
									case 0:
										retry = false; // skip the action
										insideLoopIndex = -1;
										executeSubActions = false;
									break;
									case -1:
										endVisExec();
										return(Functionality.StoppedAction);
								}
							}
						}
						while(retry);
						
						break; 
						
					case Functionality.ForLoop: // For loop as it is the most common loop type is defined explicitly here to increase efficiency
						if (!insideForLoop) // if not already looping
						{
							forLoopVariable = (Variable) actions[i].getArgumentValue(0); // Get the for loop based variable
							forLoopVariable.initTypeAndSet(Variable.doubleType, actions[i].getArgumentValue(1));  // Set the start value of the For Loop	
						}
						
						if ((boolean) ((Term) actions[i].getArgumentValue(2)).applyTo(forLoopVariable)) // If the comparator is true
						{
							executeSubActions = true;
							insideForLoop = true;
						}
						else
						{
							insideForLoop = false;
							forLoopVariable = null;
						}					
						break;
					
					
					case Functionality.ListLoop:
						if (insideLoopIndex < 0)
						{
							insideLoopIndex = 0;
							
							listLoopList = (DataList) actions[i].getArgumentValue(0);
							
							listLoopOutputVar = (Variable) actions[i].getArgumentValue(1);
							listLoopOutputVar.initType(listLoopList.getType() );
							
							if (actions[i].hasOptionalArgument(0))
							{
								listLoopIndexVar = (Variable) actions[i].getOptionalArgumentValue(0);
								listLoopIndexVar.initTypeAndSet(Variable.doubleType, 0);
							}
							else listLoopIndexVar = null;
						}
						else
							insideLoopIndex++;
						
						if (insideLoopIndex < listLoopList.getSize())
						{
							listLoopOutputVar.set( listLoopList.get(insideLoopIndex));
							if (listLoopIndexVar != null) listLoopIndexVar.set((double) insideLoopIndex);
							executeSubActions = true;
						}
						else
							insideLoopIndex = -1;
						break;
					}
					
				}
				
				
				endVisExec();
				
				if (executeSubActions)
				{
					
					switch(performActions(actions[i].getPreparedSubActions()))
					{
					case Functionality.EventRepeater:
						insideForLoop = false;
						insideLoopIndex = -1;
						return(Functionality.EventRepeater);
						
					case Functionality.ClauseRepeater:
						
						insideForLoop = false;
						insideLoopIndex = -1;
	
						boolean inner = true;
						
						int r = i;
						
						if (jumpLevels >= 0)
						if (commentStartIndices.size() > jumpLevels)
						{
							i = commentStartIndices.get(commentStartIndices.size()-1-jumpLevels);
							inner = false;
						}
	
						jumpLevels -= commentStartIndices.size();
						
						if (inner || (jumpLevels > 0))
						{	
							//InfoErrorHandler.printExecutionInfoMessage("SUB JUMP ELVELS: " + jumpLevels);
							
							if (jumpLevels > 0)
							{
								endVisExec();
								//jumpLevels--;
								return(Functionality.ClauseRepeater);
							}
							else
							if (jumpLevels == 0)
							{
								if (!jumpReCheck || thisEvent.checkCondition(localArguments))
								{
									endVisExec();
									if (thisEvent.breakNext) breakNext = true;
									return(Functionality.ClauseRepeater);
								}
								else
								{
									i = r;
									if (thisEvent.breakNext) breakNext = true;
								}
							}
							else
								if (jumpReCheck)
									i--;
	
							/*	
							if (jumpReCheck)
								i--;
								*/
						}
						//else
							//if (jumpReCheck)
								//i = r-1;
								
						
						break;
						
						
					case Functionality.EventQuitter:
						endVisExec();
						return(Functionality.EventQuitter);
					
					case Functionality.LoopQuitter:
						endVisExec();
						if (!insideForLoop && (insideLoopIndex < 0))
							return(Functionality.LoopQuitter);
						else
						{
							insideForLoop = false;
							forLoopVariable = null;
							insideLoopIndex = -1;
							break;
						}
						
					case Functionality.JumpOutOf:
						jumpLevels--;
						if (jumpLevels > 0) // if still needing to jump levels
							return(Functionality.JumpOutOf);
						break;
						// else just continue normally
								
					}
					
					if (insideForLoop)
					{
						forLoopVariable.set( ((Term)actions[i].getArgumentValue(3)).applyTo( forLoopVariable ) );
						i--; // try to repeat
					}
					if (insideLoopIndex >= 0)
						i--;
				}
			
			}
		}
		
		return(Functionality.Normal); // return normal, meaning everything executed simply
	}
	
	
	boolean breakNext = false;

	private boolean checkCondition(FunctionalityContent condition) throws Exception
	{
		if ((ExecutionSettings.breakPointsActive && condition.isBreakpoint()) || breakNext || ExecutionSettings.executeStepByStep)
			{
				BreakPointResponse resp = BreakPointHandler.elementPredecessingMessage(condition);
				breakNext = resp.breakNext;
				
				boolean res = condition.callCondition();
				
				BreakPointHandler.conditionPostMessage(resp, res);
				
				return(res);
			}

		return(condition.callCondition());
	}
	
	
	private int executeCheckedAction(FunctionalityContent action, Runnable runElement)
	{
		boolean retry = false;
		do
		{
			retry = false;
			
			if (isTracked)
			{
				Execution.print("Element #" + action.getCodeLineIndex() + ": Executing '" + action.getFunctionalityName()+"'");
				if (action.hasAnyParameters())
					Execution.print("Element #" + action.getCodeLineIndex() + ": Parameters: " + action.getParametersConcatString());
			}
			
			try
			{
				if ((ExecutionSettings.breakPointsActive && action.isBreakpoint()) || breakNext || ExecutionSettings.executeStepByStep)
				{
					BreakPointResponse resp = BreakPointHandler.elementPredecessingMessage(action);
					breakNext = resp.breakNext;
						
					runElement.run(); // execute				
					if (Execution.hasError())
						throw new Exception();
						
					BreakPointHandler.actionPostMessage(resp);
				}
				else
				{
					runElement.run(); // execute				
					if (Execution.hasError())
						throw new Exception();
				}
			}
			catch (Exception e)
			{
				switch(ExecutionErrorHandler.showError("Performing an action caused an error!", e, action, true, false))
				{
					case 1:
						if (action.isVisualized())
							action.getVisualization().applyGUIargumentData();
						retry = action.resolveVariableArguments(localArguments);
					break;
					case 0:
						retry = false; // skip the action
					break;
					case -1:
						endVisExec();
						return(Functionality.StoppedAction);
				}
			}
		}
		while(retry);
		
		return(successful);
	}


	ProgramElementOnGUI currentContent;
	
	private void visExec(FunctionalityContent programContent)
	{
		
		if (GlobalSettings.fullDebug)
			Execution.print( GlobalSettings.startExecutionSignal + ExecutionStarter.getProgramIndex(programContent));
		
		if (isVisualized)
		{
			currentContent = programContent.getVisualization().getControlerOnGUI();
			currentContent.markAsExecuting();
		}
	}
	
	
	private void endVisExec()
	{
		if (isVisualized)
		{
			Execution.sleepIfPaused();
			currentContent.fadeoutMarking();
		}
	}
	
	private void forceEndVisExec(FunctionalityContent programContent)
	{
		if (isVisualized)
		{
			Execution.sleepIfPaused();
			programContent.getVisualization().getControlerOnGUI().stopMarking();
		}
	}
	

	
	
	
	
}
