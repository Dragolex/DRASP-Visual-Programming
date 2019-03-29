package functionality.events;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TermValueVarComp;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import functionality.conditions.Cond01_Conditions;
import main.functionality.Functionality;
import main.functionality.helperControlers.OnlyOnceHelper;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.OtherHelpers;

public class Ev02_Variable extends Functionality {

	public static int POSITION = 2;
	public static String NAME = "Variable Check";
	public static String IDENTIFIER = "EvenVarNode";
	public static String DESCRIPTION = "Events based on variables.\nNote that those event conditions need to be checked constantly in the background\nand might have an impact on performance if too many are used.";
	
	

	// if number
	public static ProgramElement create_EvIfNumVar()
	{
		Object[] input = new Object[2];
		ProgramEventContent[] content = new ProgramEventContent[1];
		boolean[] alreadyOnce = new boolean[1];
		alreadyOnce[0] = false;
		
		return(content[0] = new ProgramEventContent( "EvIfNumVar",
				input,
				() -> {}
				,
				() -> {
						if (content[0].hasOptionalArgument(1))
							OtherHelpers.sleepNonException( getIntegerVariable(content[0].getOptionalArgumentValue(1)) ); // Perform delay if needed
						else
							OtherHelpers.sleepNonException(200);
						
						
						if(Cond01_Conditions.checkMultiNumVar(input, content, 3))
						{
							if (!alreadyOnce[0])
							{
								alreadyOnce[0] = content[0].getOptionalArgTrue(0);
								return(true);
							}
							else
								return(false);
						}
						else
							alreadyOnce[0] = false;
							
						return(false);
						
					}));
	}
	public static ProgramElement visualize_EvIfNumVar(FunctionalityContent content)
	{
		VisualizableProgramElement evIfNumVar;
		evIfNumVar = new VisualizableProgramElement(content, "Check Number", "Executes the child-elements if the comparation of the number value\nof a variable with a term, value or other variable, figures as 'true'.\n"
				+ "Examples of comparisons: '= 20', '< 150', etc.\n"
				+ "You can use additional terms to check multiple conditions at once\n"
				+ "for example to check whether the value lies between two values.\n\nNote that to avoid an error, the variable has to be initialized in an initialization event!");
		evIfNumVar.addParameter(0, new VariableOnly(true, false), "Variable", "Variable containing the value to compare with.");
		evIfNumVar.addParameter(1, new TermValueVarComp(true), "Comparison Term", "Term like '>20' or '=#myVar' or '<=3.1415'");
		evIfNumVar.addOptionalParameter(0, new BooleanOrVariable("false"), "Only Once", "If true, the event will be executed only once during\nthe whole duration the value meets the condition.\nIf false, the event will be executed constantly.");
		evIfNumVar.addOptionalParameter(1, new ValueOrVariable("200"), "Delay", "Delay between two checks to avoid unnecessary checks.\nNote that there is a minimum enforced on all event-conditions of ~8 milliseconds.");
		evIfNumVar.setExpandableArgumentDescription(TermValueVarComp.class, null, true, false, "Term or Value #", 16, "Additional terms or valeus to compare against.");

		return(evIfNumVar);
	}
	
	
	
	// Binary check
	public static ProgramElement create_EvIfBoolVar()
	{		
		Object[] input = new Object[1];
		ProgramEventContent[] content = new ProgramEventContent[1];
		boolean[] alreadyOnce = new boolean[1];
		alreadyOnce[0] = false;
		
		return(content[0] = new ProgramEventContent("EvIfBoolVar",
				input,
				() -> {}
				,
				() -> {
					if (content[0].hasOptionalArgument(1))
						OtherHelpers.sleepNonException( getIntegerVariable(content[0].getOptionalArgumentValue(1)) ); // Perform delay if needed
					else
						OtherHelpers.sleepNonException(200);
					
					if(checkIfBoolVar((Variable) input[0]))
					{
						if (!alreadyOnce[0])
						{
							alreadyOnce[0] = content[0].getOptionalArgTrue(0);
							return(true);
						}
						else
							return(false);
					}
					else
						alreadyOnce[0] = false;
						
					return(false);
					
					}));
	}
	public static ProgramElement visualize_EvIfBoolVar(FunctionalityContent content)
	{	
		VisualizableProgramElement EvIfBoolVar;
		EvIfBoolVar = new VisualizableProgramElement(content, "Check True", "Executes the child elements if the variable contains something figuring as 'true'.\nAmong actually binary variables that includes any number larger or equal to 0.5\nand also a text consisting of 'true' (upper and lowercase are ignored).\nNote that to avoid an error, the variable has to be initialized in an initialization event!");
		EvIfBoolVar.setArgumentDescription(0, new VariableOnly(true, false), "Variable");
		EvIfBoolVar.addOptionalParameter(0, new BooleanOrVariable("false"), "Only Once", "If true, the event will be executed only once during\nthe whole duration the value meets the condition.\nIf false, the event will be executed constantly.");
		EvIfBoolVar.addOptionalParameter(1, new ValueOrVariable("200"), "Delay", "Delay between two checks to avoid unnecessary checks.\nNote that there is a minimum delay enforced on all event-conditions of ~8 milliseconds.");
		return(EvIfBoolVar);
	}
	
	
	
	
	// if text
	public static ProgramElement create_EvIfTexVar()
	{
	Object[] params = new Object[2];
	ProgramEventContent[] content = new ProgramEventContent[1];
	OnlyOnceHelper onlyOnce = new OnlyOnceHelper();
	
	return(content[0] = new ProgramEventContent( "EvIfTexVar",
			params,
			() -> {}
			,
			() -> {
				
				if (content[0].hasOptionalArgument(2))
					OtherHelpers.sleepNonException( getIntegerVariable(content[0].getOptionalArgumentValue(2)) ); // Perform delay if needed
				else
					OtherHelpers.sleepNonException(200);
				
				boolean res;
				if (content[0].getOptionalArgTrue(0))
					res = ((Variable) params[0]).get().toString().equalsIgnoreCase((((Variable) params[1]).get()).toString());
				else
					res = ((Variable) params[0]).get().toString().equals((((Variable) params[1]).get()).toString());
				
				return(onlyOnce.handle(res, content[0].getOptionalArgTrue(1)));
				
				}));
	}
	public static ProgramElement visualize_EvIfTexVar(FunctionalityContent content)
	{
		VisualizableProgramElement EvIfTexVar;
		EvIfTexVar = new VisualizableProgramElement(content, "Check Text", "Executes the children elements if the text held by a variable\nis equal to a given text or another variable content.\nNote that to avoid an error, the variable has to be initialized in an initialization event!");
		EvIfTexVar.setArgumentDescription(0, new VariableOnly(), "Variable");
		EvIfTexVar.setArgumentDescription(1, new TextOrVariable(), "Text");
		EvIfTexVar.addOptionalParameter(0, new BooleanOrVariable("False"), "Ignore Case", "If true, upper- and lowercase differences are ignored.");
		EvIfTexVar.addOptionalParameter(1, new BooleanOrVariable("False"), "Only Once", "If true, the event will be executed only once during\nthe whole duration the value meets the condition.\nIf false, the event will be executed constantly.");
		EvIfTexVar.addOptionalParameter(2, new ValueOrVariable("200"), "Delay", "Delay between two checks to avoid unnecessary checks.\nNote that there is a minimum enforced on all event-conditions of ~8 milliseconds.");
		return(EvIfTexVar);
	}
	

	public static ProgramElement create_EvIfChVar()
	{
		Object[] params = new Object[1];
		ProgramEventContent[] content = new ProgramEventContent[1];
		Object[] lastValue = new Object[1];
		lastValue[0] = null;
		
		
		return(content[0] = new ProgramEventContent( "EvIfChVar",
				params,
				() -> {}
				,
				() -> {
					
					if (content[0].hasOptionalArgument(1))
						OtherHelpers.sleepNonException( getIntegerVariable(content[0].getOptionalArgumentValue(1)) ); // Perform delay if needed
					else
						OtherHelpers.sleepNonException(200);
					
					if (lastValue[0] == null)
					{
						lastValue[0] = params[0];
						if (content[0].getOptionalArgTrue(0))
							return(true);
					}
					else
						if (!lastValue[0].equals(params[0]))
						{
							lastValue[0] = params[0];
							return(true);
						}
					
					return(false);				
					
					}));
	}
	public static ProgramElement visualize_EvIfChVar(FunctionalityContent content)
	{
		VisualizableProgramElement EvIfTexVar;
		EvIfTexVar = new VisualizableProgramElement(content, "Changed Variable", "Executes the children elements if the variable has changed its value.\nNote that the evaluation is happening independently from the actual event\nwhich changes the variable. If you want to execute an event instantly, use 'Labeled Events'!");
		EvIfTexVar.setArgumentDescription(0, new VariableOnly(), "Variable");
		EvIfTexVar.addOptionalParameter(0, new BooleanOrVariable("False"), "Ignore Initialization", "If true, the very first initialization of the variable is ignored.");
		EvIfTexVar.addOptionalParameter(1, new ValueOrVariable("200"), "Delay", "Delay between two checks to avoid unnecessary checks.\nNote that there is a minimum enforced on all event-conditions of ~8 milliseconds.");
		return(EvIfTexVar);
	}
	

	
	
	
}


