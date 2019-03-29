package execution.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import dataTypes.FunctionalityContent;
import dataTypes.specialContentValues.FuncLabel;
import dataTypes.specialContentValues.Variable;
import execution.EventInstance;
import execution.Execution;

public class LabelHandler
{
	static Map<String, FuncLabel> labels = new HashMap<>();
	static Map<FuncLabel, FunctionalityContent> usedLabels = new HashMap<>();
	static int autoCreatedlabelIndex = 0;
	
	
	public static void clearLabels()
	{
		autoCreatedlabelIndex = 0; // reset
		
		for(FuncLabel lb: labels.values())
			lb.reset(); // reset all variables so their associated events and positions are deleted. Reason: the FuncLabel instances are saved by the GUI and reused
		
		labels.clear();
		usedLabels.clear();
	}
	
	public static FuncLabel getOrCreate(String name)
	{
		FuncLabel lb = labels.get(name);
		
		if (lb == null)
			return(new FuncLabel(name)); // create if it doesn't exist
		
		return(lb);
	}	
	
	
	public static boolean labelAlreadyExists(FuncLabel lb)
	{
		return(labels.containsValue(lb));
	}


	
	public static String getNewAutocreatedName()
	{
		return("AutoLabel_" + (autoCreatedlabelIndex++));
	}
	
	public static void saveLabel(FuncLabel funcLabel)
	{
		labels.put(funcLabel.getName(), funcLabel);		
	}
	
	
	
	
	
	public static boolean addLabeledEvent(EventInstance event, FuncLabel label)
	{
		InfoErrorHandler.printExecutionInfoMessage("PREPARED EVENT LABEL: " + label);
		//InfoErrorHandler.printExecutionInfoMessage("LABEL NUMBERS: " + lableIndices.size());
		
		if (label == null)
		{
			Execution.setError("A label name has not been set!", false);
			return(false);
		}
		
		//if(labelAlreadyExists(label))
		if (label.getEvent() != null)
		{
			Execution.setError("There is already a label (event) with the given name '" + label.getName() + "'!", false);
			return(false);
		}
		
		label.setEvent(event);
		label.setPositionIndex(0);
		
		if (!labels.containsValue(label))
			saveLabel(label); // ensure that it is in the list
		
		return(true);
	}



	public static boolean addLabeledPosition(FunctionalityContent eventContent, int lablePosition, FuncLabel label) // Use the hasPreparedSubactions of content and start from the index
	{
		InfoErrorHandler.printExecutionInfoMessage("PREPARED LABELED POSITION: " + label.getName());
		
		//if(labelAlreadyExists(label))
		if (label.getEvent() != null)
		{
			Execution.setError("There is already a label with the given name '" + label.getName() + "'!", false);
			return(false);
		}
		
		EventInstance event = label.getEvent();
		if (event == null)
		{
			Execution.setError("A labeled position with the name '" + label.getName() +"' has no event.", false);
			return(false);
		}
		
		label.setPositionIndex(lablePosition);
		
		if (!labels.containsValue(label))
			saveLabel(label); // ensure that it is in the list
		
		return(true);
	}
	
	
	/*
	public static void executeLabelDirect(FuncLabel label, Object[] argsToTransfer)
	{
		EventInstance ev = label.getEvent();
		
		//setEventExpandedArguments(ev, argsToTransfer);
		
		if (label.getPositionIndex() == null)
			ev.performActions();
		else
			ev.performActions(label.getPositionIndex());
		
	}

	
	public static void executeLabelThreaded(FuncLabel label, Object[] argsToTransfer, int offsetIndex)
	{
		EventInstance ev = label.getEvent();		
		
		//setEventExpandedArguments(ev, argsToTransfer, offsetIndex);
		
		ev.externallyActivateAndPerform(label.getPositionIndex(), argsToTransfer, offsetIndex);
	}
	*/
	
	/*
	private static void setEventExpandedArguments(EventInstance targetEv, Object[] argsToTransfer, int offsetIndex)
	{
		if (argsToTransfer != null && (argsToTransfer.length != 0))
		{
			Object[] evArgs = targetEv.getContent().getTotalOptionalOrExpandedArgumentsArray();
			
			if ((argsToTransfer.length-offsetIndex) != evArgs.length)
				Execution.setError("Number of arguments set to call a Label\ndoes not match with the arguments requried by the label!", true);
			
			int args = argsToTransfer.length-offsetIndex;
			int j = offsetIndex;
			for(int i = 0; i < args; i++)
			{
				if (argsToTransfer[j] instanceof Double)
				{
					System.out.println("WWWWWWWW Set DOUBLE. Value: " + argsToTransfer[j]);
					
					((Variable) evArgs[i]).initTypeAndSet(Variable.doubleType, argsToTransfer[j]);
				}
				else
				if (argsToTransfer[j] instanceof String)
					((Variable) evArgs[i]).initTypeAndSet(Variable.textType, argsToTransfer[j]);
				else
				if (argsToTransfer[j] instanceof Variable)
					((Variable) evArgs[i]).initTypeAndSet(((Variable) argsToTransfer[j]).getType(), ((Variable) argsToTransfer[j]).getUnchecked());
					
				// TODO: Find a way to work without instanceof. However performance impact is not meassurable
				
				j++;
			}
		}
	}
*/

	
	public static FuncLabel getByPossibleVariable(Object argumentValue)
	{
		FuncLabel label;
		
		if (argumentValue instanceof Variable) // The label argument contains a variable
		{
			Variable labelVar = (Variable) argumentValue;
			
			if (!labelVar.hasValue()) // label has no value yet
			{
				label = new FuncLabel(); // create a new label with an automatically generated name
				labelVar.initTypeAndSet(Variable.labelType, label);
			}
			else
				label = (FuncLabel) labelVar.getUnchecked();
		}
		else
			label = (FuncLabel) argumentValue;
		
		return(label);
	}
	
	
	public static void signalizeUsageOfAlabel(FuncLabel argumentValue, FunctionalityContent associatedContent)
	{
		usedLabels.put(argumentValue, associatedContent);
	}
	
	
	public static Entry<FuncLabel, FunctionalityContent> verifyLabelMatches()
	{
		for(Entry<FuncLabel, FunctionalityContent> used: usedLabels.entrySet())
		{
			if (!labels.containsValue(used.getKey())) // value does not exist in the map of label events -> missing!
				return(used);
		}
		return(null);
	}
	
	
}
