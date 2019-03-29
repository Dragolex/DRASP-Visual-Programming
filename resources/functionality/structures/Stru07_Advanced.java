package functionality.structures;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.LabelString;
import dataTypes.contentValueRepresentations.SelectableType;
import dataTypes.contentValueRepresentations.TextOrValueOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Stru07_Advanced extends Functionality {

	public static int POSITION = 7;
	public static String NAME = "Advanced";
	public static String IDENTIFIER = "StruAdvancedNode";
	public static String DESCRIPTION = "Attention! Using those structures can make a program harder to comprehend!\nPlease only if when they truly bring a benefit.";
	
	
	// Repeat ind
	public static ProgramElement create_StRepIf()
	{
		return(new FunctionalityContent("StRepIf", ClauseRepeater, 2));
	}
	public static ProgramElement visualize_StRepIf(FunctionalityContent content)
	{
		VisualizableProgramElement StRepIf;		
		StRepIf = new VisualizableProgramElement(content, "Repeat Block", "Causes the program to jump back to the top of as many conditions, comment/loop blocks or events as the number of 'levels' indicates.\nVisually that means the number of indentations from the surrounde event.\nFor example if this is placed inside two conditions inside an event (meaning it is indentated by 3 steps in total), a number of 0\n will cause the current condition to repeat.\nIf the number is one, the outer condition will repeat (including if it is an event).\nNegative numbers have no effect. Note that you can use 'Repeat Event' to repeat the actual event.\nIf 'Check' is true, the condition will be checked again\nand the condeblock will have a similar behavior to a while loop.");
		StRepIf.setArgumentDescription(0, new ValueOrVariable(), "Levels"); // Todo Set default value
		StRepIf.setArgumentDescription(1, new BooleanOrVariable(), "Check");
		return(StRepIf);
	}
	
	
	// End event / jump out
	/*
	// Unfortunately this is not reliable yet TODO
	public static ProgrammElement create_StEndEvCond()
	{
		return(new FunctionalityContent("StEndEvCond", JumpOutOf, 1));	
	}
	public static ProgrammElement visualize_StEndEvCond(FunctionalityContent content)
	{	
		VisualizableProgrammElement StEndEvCond;
		StEndEvCond = new VisualizableProgrammElement(content, "Exit Block", "Quits the given number of conditions, comment blocks and events. Visually that means the number of indentations.\nFor example if this is placed in one condition inside an event (meaning it is indentated by 2 steps), a number of 0\n will cause the condition to quit and the event continue afterwards (including after any else block).\nNegative numbers will always quit the event.\nThis structure is comparable to the 'continue' keyword in some programming languages.");
		StEndEvCond.setArgumentDescription(0, new ValueOrVariable(), "Levels");
		return(StEndEvCond);
	}
	*/
	


	
	public static ProgramElement create_StVirtualEvent()
	{
		Object[] setBoolVar = new Object[4];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent("StVirtualEvent",
				setBoolVar,
				() -> {
					
					// TODO
					
				}));		
	}
	public static ProgramElement visualize_StVirtualEvent(FunctionalityContent content)
	{
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "Virtual Event", "Creates an event dynamically as if it were part of the program and uses an existing Labeled Event as its body.\nIt enables you to pass two values or variables to the Labeled Event.\nNote that the actual arguments for the virtual events need to be passed with the [+] button!\nYou can use this element to dynamically add events to things whsoe number is unknown at runtime.\nFor example if you want to create a button for every file in a directory on the users computer.");
		StSetNumVar.addParameter(0, new SelectableType(new String[] {"Press Event"}), "Event Type", "The type of event to use.");
		StSetNumVar.addParameter(1, new LabelString(), "Labeled Event", "The name of the labeled event that will be called when the virtual event fires. Note that it requires exactly two arguments (A and B below)!");
		StSetNumVar.addParameter(2, new TextOrValueOrVariable(), "Value A", "The first value passed to the Labeled Event.\nUse this for an index to identify which virtual event has been fired.");
		StSetNumVar.addParameter(3, new TextOrValueOrVariable(), "Value B", "The second value passed to the Labeled Event.\nUse this for an index to identify which virtual event has been fired.");
		return(StSetNumVar);
	}
	
	
	
	
}


