package execution;

import java.util.ArrayList;
import java.util.List;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.specialContentValues.FuncLabel;
import dataTypes.specialContentValues.Variable;
import execution.handlers.ExecutionErrorHandler;
import execution.handlers.LabelHandler;
import main.functionality.Functionality;
import main.functionality.Structures;

@SuppressWarnings("deprecation")
public class PreparationHelper
{
	
	// Initializes all actions by calling the initialization runnable
	protected static boolean initializeActions(FunctionalityContent[] directActions) // returns false if requested to abort
	{
		for(FunctionalityContent act: directActions)
			if (act != null)
				if (!performInitialization(act))
					return(false);
		return(true);
	}

	private static boolean performInitialization(FunctionalityContent actionContent)
	{
		if (!performCheckedInitialization(actionContent)) // initialize this action
			return(false);
			
		FunctionalityContent[] subActions = actionContent.getPreparedSubActions();
		if (subActions != null) // if sub-actions are available
			for(FunctionalityContent subAct: subActions)
				if (subAct != null)
					if (!performInitialization(subAct)) // Recursively initialize all subactions
						return(false);
		
		return(true);
	}
	
	
	
	public static boolean performCheckedInitialization(FunctionalityContent actionContent)
	{
		//if (actionContent.isInitialized()) return(true);
		
		boolean retry = false;
		do
		{
			try
			{
				if (actionContent.getInitialization() != null)
					actionContent.getInitialization().run(); // execute
			}
			catch (Exception e)
			{
				if (ExecutionErrorHandler.showError("Initializing an action or event caused an error!", e, actionContent))
					retry = true;
				else
					return(false);
			}
		}
		while(retry);		
		
		return(true);
	}
	
	
	

	/*
	 * This recursive function allows to go through all events inside the tree starting at a given root-node (which has to be an event itself)
	 * It counts the number of nodes (which are the actions inside the events) and adds them to the array if the argument "addContentToArray" is true.
	 * The number of traversed nodes is returned.
	 * 
	 * The event nodes themselves are ignored and sub-nodes of conditional contents are not added to the main array either
	 */
	

	protected static int traverseEventNodes(DataNode<ProgramElement> node, List<FunctionalityContent> actionList, List<FunctionalityContent> preparedSubActions, int index, boolean addContentToArray)
	{
		FunctionalityContent dataContent = node.getData().getContent();
		
		if (!dataContent.hasBeenTraversed())
		{
			if (dataContent.isSpecial(Functionality.LabeledPosition))
			{
				DataNode<ProgramElement> parentNode = node;
				while((parentNode.getData() != null) && !parentNode.getData().isEvent()) // get the surrounding event
					parentNode = parentNode.getParent(); 
				
				
				
				FuncLabel label = LabelHandler.getByPossibleVariable(dataContent.getArgumentValue(0));
				
				if (!LabelHandler.addLabeledPosition(parentNode.getData().getContent(), index, label )) // save it on the LabelHandler
					ExecutionErrorHandler.showError("Label Problem!", null, dataContent, false, true);
								
			}
			
			if (!dataContent.isOutcommented())
			{
				Object specVal = null;
				if (dataContent.isSpecial(Functionality.LabelJump) || dataContent.isSpecial(Functionality.LabelExecute) || dataContent.isSpecial(Functionality.LabelAlarm))
					specVal = dataContent.getArgumentValue(0);
				else
				if (dataContent.getFunctionalityName().equals("StSetLabelVar"))
					specVal = dataContent.getArgumentValue(1);
				
				if (specVal != null)
				if (!(specVal instanceof Variable))
					LabelHandler.signalizeUsageOfAlabel((FuncLabel) specVal, dataContent);
			}			
		}
		
		dataContent.setBeenTraversed(true);
		
		
		
		if (dataContent.isEvent() || dataContent.isSpecial(Functionality.Comment)) // if it is an event or a comment block
		{
			index++;
			int subcount = 0;
			
			
			if (addContentToArray)
				actionList.add(null);
			else
				preparedSubActions.add(null);
			
		
			for(DataNode<ProgramElement> childNode: node.getChildrenAlways()) // Directly traverse through sub-children
				subcount += traverseEventNodes(childNode, actionList, preparedSubActions, index+subcount, addContentToArray); // add the sum of the subcount to the counter variable
			
			return(subcount+1); // return the number of traversed elements so far
		}
		else // if not an event
		{
			if (addContentToArray)
				actionList.add(dataContent);
			
			index++;
			
			if ( dataContent.isConditionalElement() || dataContent.isSpecial(Functionality.ForLoop) || dataContent.isSpecial(Functionality.ListLoop) || dataContent.isSpecial(Functionality.ElseClause) || dataContent.isSpecial(Functionality.ExtraDefinedLoop) ) // if it is a conditional action
			{
				int subcount = 0;
				
				if (!dataContent.hasPreparedSubactions())
				{
					//preparedSubActions.clear();
					List<FunctionalityContent> newPreparedSubActions = new ArrayList<FunctionalityContent>();
					
					for(DataNode<ProgramElement> childNode: node.getChildrenAlways()) // Directly traverse through subchildren
						subcount += traverseEventNodes(childNode, actionList, newPreparedSubActions, subcount, false); // Recursively continue like above, however "addContentToArray" is FALSE this time				
					
					
					FunctionalityContent[] subActions = new FunctionalityContent[newPreparedSubActions.size()];
					subActions = newPreparedSubActions.toArray(subActions);
					
					dataContent.setPreparedSubActions(subActions); // Create and set a new array of subactions
					
					
					//if (((ProgramContent) node.getParent().getData().getContent()).isSpecial(Functionality.InitEvent))
						//OtherHelpers.printProgramContentList(preparedSubActions, "Condition Data");
				}
				
				if (!preparedSubActions.contains(dataContent))
					preparedSubActions.add(dataContent);
				
				return(subcount+1); // return the number of traversed elements so far
			}
			else // "else" here means the element is neither an event nor a conditional action -> simple action not owning any children
			{
				if (node.getParent().getData() == null)
					return(1); // Continue only if the parent contains a node (just to go sure)
				
				preparedSubActions.add(dataContent);
			}
			
			
			return(1);
		}
		
	}
	
	
	/*
	protected static int traverseEventNodes(DataNode<ProgrammElement> node, List<ProgramContent> actionList, List<ProgramContent> preparedSubActions, int index, boolean addContentToArray)
	{
		ProgramContent dataContent = node.getData().getContent();
		
		if (dataContent.isSpecial(Functionality.LabeledPosition))
			LabelHandler.addLabeledPosition(node.getParent().getData().getContent(), index); // attention, it must be the index inside the current array!
		
		
		if (dataContent.isEvent() || dataContent.isSpecial(Functionality.Comment)) // if it is an event or a comment block
		{
			index++;
			int subcount = 0;
			
			for(DataNode<ProgrammElement> childNode: node.getChildrenAlways()) // Directly traverse through sub-children
				subcount += traverseEventNodes(childNode, actionList, preparedSubActions, index+subcount, addContentToArray); // add the sum of the subcount to the counter variable
			
			return(subcount+1); // return the number of traversed elements so far
		}
		else // if not an event
		{
			if (addContentToArray)
				actionList.add(dataContent);
			
			index++;
			
			if (dataContent.isConditionalAction()) // if it is a conditional action
			{
				int subcount = 0;
				
				
				if (!dataContent.hasPreparedSubactions())
				{
					preparedSubActions.clear();
					
					
					for(DataNode<ProgrammElement> childNode: node.getChildrenAlways()) // Directly traverse through subchildren
						subcount += traverseEventNodes(childNode, actionList, preparedSubActions, subcount, false); // Recursively continue like above, however "addContentToArray" is FALSE this time				
					
					
					ProgramContent[] subActions = new ProgramContent[preparedSubActions.size()];
					subActions = preparedSubActions.toArray(subActions);
					
					dataContent.setPreparedSubActions(subActions); // Create and set a new array of subactions
					
					
					//if (((ProgramContent) node.getParent().getData().getContent()).isSpecial(Functionality.InitEvent))
						//OtherHelpers.printProgramContentList(preparedSubActions, "Condition Data");
				}
				
				
				return(subcount+1); // return the number of traversed elements so far
			}
			else // "else" here means the element is neither an event nor a conditional action -> simple action not owning any children
			{
				if (node.getParent().getData() == null)
					return(1); // Continue only if the parent contains a node (just to go sure)
				
				preparedSubActions.add(dataContent);
			}
			
			
			return(1);
		}
		
	}
	*/
	
	
	
	
}
