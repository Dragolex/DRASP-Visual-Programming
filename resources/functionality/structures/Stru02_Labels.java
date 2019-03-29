package functionality.structures;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.LabelString;
import dataTypes.contentValueRepresentations.TextOrValueOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Stru02_Labels extends Functionality {

	public static int POSITION = 2;
	public static String NAME = "Labels";
	public static String IDENTIFIER = "StruLabelNode";
	public static String DESCRIPTION = "Structures to work with labels.\nThose are named positions in the action queue defined by a text.\nThey can be used like function calls in other programming languages.";
	
	

	
	public static ProgramElement create_StLabel()
	{
		return(new FunctionalityContent("StLabel", LabeledPosition, 1));
	}
	public static ProgramElement visualize_StLabel(FunctionalityContent content)
	{
		VisualizableProgramElement StLabel;
		StLabel = new VisualizableProgramElement(content, "Label", "Marks a position in the queue where the program can jump to with the 'Jump to Label' - element.\nNote: Mimicing function calls (known from other programming languages)\nis possible with this, but using a 'Labeled Event' is often suited better.");
		StLabel.addParameter(0, new LabelString(), "Label Name", "The label can have any name consisting of text.");
		return(StLabel);		
	}
	
	
	public static ProgramElement create_StLabelJump()
	{
		return(new FunctionalityContent("StLabelJump", LabelJump, 1));
	}
	public static ProgramElement visualize_StLabelJump(FunctionalityContent content)
	{	
		VisualizableProgramElement StLabelJump;
		StLabelJump = new VisualizableProgramElement(content, "Jump to Label", "Causes the program to continue at a given 'Label' or 'Labeled Event'.\nThe current event which calls this element ends instantly.");
		StLabelJump.addParameter(0, new LabelString(), "Target Label", "The label can have any name consisting of text\nor be another variable starting with '#'.");
		StLabelJump.setExpandableArgumentDescription(TextOrValueOrVariable.class, null, true, false, "Arg #", 16, "If you call a 'Labeled Event', optional values can be expanded and their content will be transmitted to the 'Labeled Event'\n if it's used. Ensure to use the same number of 'arguments'!\nIf you jump to a simple 'Label', thsoe arguments will be ignored.");
		return(StLabelJump);
	}
	
	
	public static ProgramElement create_StLabelExecute()
	{
		return(new FunctionalityContent("StLabelExecute", LabelExecute, 2));
	}
	public static ProgramElement visualize_StLabelExecute(FunctionalityContent content)
	{	
		
		VisualizableProgramElement StLabelJump;
		StLabelJump = new VisualizableProgramElement(content, "Execute Label", "Causes the program to execute the elements after a given 'Label' or the content of 'Labeled Event'.The current event will continue in any case.\nATTENTION: This function uses what's called 'recursive calls' in many program languages.\nThat means the number of encapsulated calls\n(aka, call a label which calls another label which calls another, etc.)\n is limited to a couple of thousand and relatively inefficient.\nThus it is not recomended to use this to form loops!Try to use the 'Jump to label' element when possible.");
		StLabelJump.addParameter(0, new LabelString(), "Target Label", "The label can have any name consisting of text\nor be another variable starting with '#'.");
		StLabelJump.addParameter(1, new BooleanOrVariable(), "Wait to Finish", "If true, the current execution of this action is halting\\nuntil the 'Labeled Event' or the event containing the simple 'Label' has finished.");
		StLabelJump.setExpandableArgumentDescription(TextOrValueOrVariable.class, null, true, false, "Arg #", 16, "Optional values can be expanded and their content will be transmitted to the 'Labeled Event'\n if it's used. Ensure to use the same number of 'arguments'!");
		return(StLabelJump);
	}
	
	
	public static ProgramElement create_StLabelAlarm()
	{
		return(new FunctionalityContent("StLabelAlarm", LabelAlarm, 3));
	}
	public static ProgramElement visualize_StLabelAlarm(FunctionalityContent content)
	{	
		VisualizableProgramElement StLabelJump;
		StLabelJump = new VisualizableProgramElement(content, "Alarm Label", "Causes the program to execute the elements after a given 'Label' or the content of 'Labeled Event' after a given delay.");
		StLabelJump.addParameter(0, new LabelString(), "Target Label", "The label can have any name consisting of text\nor be another variable starting with '#'.");
		StLabelJump.addParameter(1, new ValueOrVariable(), "Delay (ms)", "The delay in milliseconds until the alarm should execute.\nIf you use a negative number,\nthe alarm will be aborted if 'Overwrite' is true.");
		StLabelJump.addParameter(2, new BooleanOrVariable(), "Overwrite", "If true and a label has already been alarmed,\nthe alarm will be reset to the given delay.");
		StLabelJump.setExpandableArgumentDescription(TextOrValueOrVariable.class, null, true, false, "Arg #", 16, "Optional values can be expanded and their content will be transmitted to the 'Labeled Event'\n if it's used. Ensure to use the same number of 'arguments'!");
		return(StLabelJump);
	}
	
	
}


