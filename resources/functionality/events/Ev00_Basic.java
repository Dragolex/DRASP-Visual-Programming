package functionality.events;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.LabelString;
import dataTypes.contentValueRepresentations.SelectableType;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;
import settings.GlobalSettings;

public class Ev00_Basic extends Functionality {

	public static int POSITION = 0;
	public static String NAME = "Basic";
	public static String IDENTIFIER = "EvenBaseNode";
	public static String DESCRIPTION = "Events with generic purposes.\nDrag into the queue and provide the desired parameters.\nWhen the condition is met, nested actions are executed.";
	


	public static ProgramElement create_EvInit()
	{
		 return(new ProgramEventContent("EvInit", InitEvent, 0).removeFixedOptionalArguments());
	}
	public static ProgramElement visualize_EvInit(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Initialization", "Event guaranteed to be executed before any other event or action.\nAttention, do not include an endless loop inside this event because\nno other, normal event can fire until all its actions have completed.\nMultiple initialization events are allowed and they will\nexecute either in parallel (unpredictable order) or in order\nof the optional argument from low number to high.");
		ev.addOptionalParameter(0, new ValueOrVariable("0"), "Order", "Determines execution order of multiple initialization events.\nThe event with the smallest number including negative numbers\nwill execute first. Same numbers execute in parallel.\nIf you provide a variable, it will be reevaluated after every event\nbut an error will ocur if the number is smaller than the currently newest init event.");
		return(ev);
	}
	
	
	public static ProgramElement create_EvLabeled()
	{
		return(new ProgramEventContent("EvLabeled", LabeledEvent, 1).removeFixedOptionalArguments());
	}
	public static ProgramElement visualize_EvLabeled(FunctionalityContent content)
	{
		VisualizableProgramElement evLabeled;
		evLabeled = new VisualizableProgramElement(content, "Labeled Event", "Event executed when called through the 'Go to Label' - action.\nIt allows to define a varying number of variables to transfer values.\nThis acts like a function header known from many programming languages.\nAttention: As of now all variables are global therefore, executing the same 'Labeled Event' in parallel\nwill use the same variables and thus cause unforseen effects!");
		evLabeled.setArgumentDescription(0, new LabelString(), "Label Name");
		evLabeled.setExpandableArgumentDescription(VariableOnly.class, null, true, true, "Arg #", 16, "Up to 16 variables which will contain the passed values.");
		return(evLabeled);
	}
	
	
	
	// Step events
	
	
	public static ProgramElement create_EvRhythmStep()
	{
		return(new ProgramEventContent("EvRhythmStep", RhythmStepEvent, 1).removeFixedOptionalArguments());
	}
	public static ProgramElement visualize_EvRhythmStep(FunctionalityContent content)
	{
		VisualizableProgramElement evKeyDown;
		evKeyDown = new VisualizableProgramElement(content, "Rhythmic Step", "This event automatically executes repeatedly with a given interval.");
		evKeyDown.addParameter(0, new ValueOrVariable(), "Delay", "Interval between two calls in milliseconds.\nExample: 33 for approximately 30 executions per second.");
		evKeyDown.addOptionalParameter(0, new BooleanOrVariable("False"), "Skippable", "If true, calling the event will be skipped\nif the last execution is still running.\nEfectively it is 'unique' but the rhythm is still always maintained.");
		return(evKeyDown);
	}
	
	
	public static ProgramElement create_EvStep()
	{
		Object[] param = new Object[1];
		
		return(new ProgramEventContent( "EvStep",
				param,
				() -> {
						Execution.checkedSleep(Math.max(0, ((long) (double) param[0]) - GlobalSettings.constantCheckDelay));
						return(true);
					}).removeFixedOptionalArguments());//.removeFixedOptionalArgumentsAndChangeDefault(false, 0));
	}
	public static ProgramElement visualize_EvStep(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Continuous Step", "This event automatically executes repeatedly\nand always waits the given interval inbetween.\nNote that if the content of this event consumes time, the next step will be delayed.\nTo avoid this, use 'Rhythmic Step'.");
		ev.addParameter(0, new ValueOrVariable(), "Delay", "Interval between two calls in milliseconds.\nExample: 33 for approximately 30 executions per second.");
		return(ev);
	}
	
	
	

	public static ProgramElement create_EvFade()
	{
		Object[] param = new Object[6];		
		
		
		Boolean[] reversed = new Boolean[1];
		reversed[0] = null;
		
		Integer[] counter = new Integer[1];

		Double[] increment = new Double[1];
		
		
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new ProgramEventContent( "EvFade",
				param,() -> {
					reversed[0] = null;
				},
				() -> {
						
						if (reversed[0] == null) // not initialized yet
						{
							reversed[0] = false;
							
							initVariableAndSet(param[4], Variable.doubleType, (double) param[0]);
							
							if (cont[0].hasOptionalArgument(0))
								initVariableAndSet(cont[0].getOptionalArgumentValue(0), Variable.doubleType, (double) 0);

							
							counter[0] = 0;
						}

						switch((int) param[2])
						{
						case 0: increment[0] = Math.abs((double) param[3]); break;
						case 1: increment[0] = Math.abs((double) param[0] - (double) param[1])/Math.abs((double) param[3]); break; // compute the increment needed to achieve the given number of steps
						case 2: increment[0] = Math.abs((double) param[0] - (double) param[1])/Math.abs((double) param[3]/(double) param[5]); break; // compute the increment needed to achieve the given total time
						}
						
						
						
						double val = (double) ((Variable) param[4]).getUnchecked(); // current value of the incremented variable
						
						if (cont[0].hasOptionalArgument(0))
							((Variable) cont[0].getOptionalArgumentValue(0)).set((double) counter[0]);
						
						if ((double) param[reversed[0] ? 0 : 1] > (double) param[reversed[0] ? 1 : 0]) // end is larger than start
						{
							val += increment[0]; // add a positive increment
							
							if (val >= (double) param[reversed[0] ? 0 : 1]) // past the limit
							{
								val = (double) param[reversed[0] ? 0 : 1];
								reversed[0] = !reversed[0];
							}
						}
						else
						{
							val -= increment[0]; // subtract a positive increment
							
							if (val <= (double) param[reversed[0] ? 0 : 1]) // past the limit
							{
								val = (double) param[reversed[0] ? 0 : 1];
								reversed[0] = !reversed[0];
							}
						}
						
						setVariable(param[4], val);
						
						
						counter[0] += 1;
						
						
						Execution.checkedSleep(Math.max(0, ((long) (double) param[5]) - GlobalSettings.constantCheckDelay));
						return(true);

						
					}).removeFixedOptionalArguments());//.removeFixedOptionalArgumentsAndChangeDefault(false, 0));
	}
	public static ProgramElement visualize_EvFade(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Fading Event", "This event automatically executes repeatedly and provides a value that fades between a given start and end value.\nYou can use this to easily fade lights up and down.");
		vis.setArgumentDescription(0, new ValueOrVariable(), "Start Value");
		vis.setArgumentDescription(1, new ValueOrVariable(), "End Value");
		vis.addParameter(2, new SelectableType(new String[] {"By Increment", "By Steps", "By Time"}), "Iteration", "By Increment means you provide a value to increase on every step.\nBy step means you provide the number of steps and the increment\nto achieve so many steps will be computed automatically.\nBy Time means you provide the number of milliseconds the fading will take and the number\nof steps will be computed by the given delay.");
		vis.addParameter(3, new ValueOrVariable(), "Incr/Steps/Time", "May not be zero.\nNegative or positive increment does not matter\nas it will be adapted to reach the end value.\nNote that if you provide a variable for steps and change its value at runtime,\nthat will only take effect after reaching the next limit.");
		vis.addParameter(4, new VariableOnly(true, true), "Value Variable", "This variable will hold the new value during every iteration.\nNote that you can manipulate this value to skip iterations!");
		vis.addParameter(5, new ValueOrVariable(), "Delay", "Interval between two calls in milliseconds.\nExample: 33 for approximately 30 executions per second.");
		vis.addOptionalParameter(0, new VariableOnly(true, true), "Index Variable", "This variable will hold the index of the current iteration.");
		return(vis);
	}
		
	
}


