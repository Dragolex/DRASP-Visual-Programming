package functionality.conditions;

import dataTypes.FunctionalityConditionContent;
import dataTypes.FunctionalityContent;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TermValueVarComp;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import main.functionality.Functionality;
import main.functionality.helperControlers.Parameters;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Cond01_Conditions extends Functionality {

	public static int POSITION = 1;
	public static String NAME = "Variable Check";
	public static String IDENTIFIER = "CondVariableNode";
	public static String DESCRIPTION = "Conditions related to variables set and modified by the coresponding structures.\n"
									 + "Drag into the queue and provide the desired parameters.";
	
	

	// GPIO Conditional action
	static public FunctionalityContent create_ConGPIOresp()
	{
		Object[] _params = new Object[2];
		
		Parameters params = new Parameters(); // 2
		
		return(params.attach(new FunctionalityConditionContent( "ConGPIOresp",
				_params,
				() -> {						
					
					int debounce = defaultDebounce;
					if (params.hasOptParam(1))// content[0].hasOptionalArgument(1))
						debounce = params.getOptInt(0);//  content[0].getOptionalArgumentValue(0));
		
					return(GPIOctrl.checkInputPin(params.getInt(0), params.getBool(1), debounce));
					})));
	}
	static public VisualizableProgramElement visualize_ConGPIOresp(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "GPIO Signal Check", "Executes nested actions if a GPIO button has been activated.");
		vis.addParameter(0, new ValueOrVariable(), "GPIO Pin", "GPIO pin to check." + gpioPinReferenceText);
		vis.addParameter(1, new BooleanOrVariable("False"), "Pull-Up-Res", "If true, the system relies on having a PULL-UP-resistor applied to the GPIO (see the graphic)\nand that means your attached switch/button has to conntect to ground.\nIf false, the PULL-DOWN resistor is assumed and the switch/button should connect to Vcc (3.3v).");
		vis.addOptionalParameter(0, new ValueOrVariable(String.valueOf(defaultDebounce)), "Debounce", "Physical switches, buttons and relais tend to 'bounce' when used.\nThat means for a tiny fraction of a second, the signal jumps up and down.\n'Debouncing' is a countermeassure which simply expects the signal to\nstay steadily for a number of milliseconds.\30ms is reccommended and perfect for all kind of human input.\nUse 0 if you have electronic inputs (like from a microcontroler or another PI).");
		return(vis);
	}
	
	
	
	// Variable check
	public static FunctionalityContent create_ConIfVarExists()
	{
		Object[] input = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];

		return(content[0] = new FunctionalityConditionContent("ConIfVarExists",
				input,
				() -> {
					
						return(((Variable) input[0]).hasValue());
					
					}));
	}
	public static VisualizableProgramElement visualize_ConIfVarExists(FunctionalityContent content)
	{	
		VisualizableProgramElement ConIfNumVar;
		ConIfNumVar = new VisualizableProgramElement(content, "Variable Exists", "Executes the nested blocks if the variable already exists\nand therefore has a value. Note that you can use the optional\n 'Not' argument to execute if it does not exist\nor use the else block.");
		ConIfNumVar.setArgumentDescription(0, new VariableOnly(true, false), "Variable");
		return(ConIfNumVar);
	}
	
	
	
	
	// Is used by an Event as well!
	public static boolean checkMultiNumVar(Object[] input, FunctionalityContent[] content, int firstExpArg)
	{
		if (content[0].hasOptionalArgument(firstExpArg))
		{
			Object[] args = content[0].getTotalOptionalOrExpandedArgumentsArray();
			int argsCount = args.length;
			
			Variable inp = (Variable) input[0];
			
			if (! ((boolean) ((Term) input[1]).applyTo( inp )))
				return(false);
			
			for(; firstExpArg < argsCount; firstExpArg++)
			{
				if (! ((boolean) ((Term) args[firstExpArg]).applyTo( inp )) )
					return(false);
			}
		}
	
		return( (boolean) ((Term) input[1]).applyTo( (Variable) input[0]) );
	}
	
	
	// Variable check
	public static FunctionalityContent create_ConIfNumVar()
	{
		Object[] input = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		
		return(content[0] = new FunctionalityConditionContent("ConIfNumVar",
				input,
				() -> {
					
						return(checkMultiNumVar(input, content, 2));
						
					}));
	}
	public static VisualizableProgramElement visualize_ConIfNumVar(FunctionalityContent content)
	{	
		VisualizableProgramElement ConIfNumVar;
		ConIfNumVar = new VisualizableProgramElement(content, "Check Number", "Executes the child-elements if the comparation of the number value\nof a variable with a term, value or other variable, figures as 'true'.\n"
				+ "Examples of comparisons: '= 20', '< 150', etc.\n"
				+ "You can use additional terms to check multiple conditions at once\n"
				+ "for example to check whether the value lies between two values.");
		ConIfNumVar.setArgumentDescription(0, new VariableOnly(true, false), "Variable");
		ConIfNumVar.setArgumentDescription(1, new TermValueVarComp(true), "Term or Value");
		ConIfNumVar.setExpandableArgumentDescription(TermValueVarComp.class, null, true, false, "Term or Value #", 16, "Up to 16 mroe terms.");
		return(ConIfNumVar);
	}
	
	
	// Binary check
	public static FunctionalityContent create_ConIfBoolVar()
	{		
		Object[] input = new Object[1];
		return( new FunctionalityConditionContent("ConIfBoolVar",
				input,
				() -> {}
				,
				() -> {
						return(checkIfBoolVar((Variable) input[0]));
					}));
	}
	public static VisualizableProgramElement visualize_ConIfBoolVar(FunctionalityContent content)
	{	
		VisualizableProgramElement ConIfNumVar;
		ConIfNumVar = new VisualizableProgramElement(content, "Check True", "Executes the child elements if the variable contains something binary figuring as 'true'.\nAmong actually binary variables that includes any number larger or equal to 0.5\nand also a text consisting of 'true' (upper and lowercase are ignored).");
		ConIfNumVar.setArgumentDescription(0, new VariableOnly(true, false), "Variable");
		return(ConIfNumVar);
	}
	
	
	
	
	public static FunctionalityContent create_ConIfTexVar()
	{
	Object[] params = new Object[2];
	FunctionalityConditionContent[] content = new FunctionalityConditionContent[1];
	
	return(content[0] = new FunctionalityConditionContent("ConIfTexVar",
			params,
			() -> {}
			,
			() -> {
					if (content[0].getOptionalArgTrue(0))
						return(((Variable) params[0]).get().toString().equalsIgnoreCase((((Variable) params[1]).get()).toString()));
					return(((Variable) params[0]).get().toString().equals((((Variable) params[1]).get()).toString()));
				}));
	}
	public static VisualizableProgramElement visualize_ConIfTexVar(FunctionalityContent content)
	{	
		VisualizableProgramElement ConIfTexVar;
		ConIfTexVar = new VisualizableProgramElement(content, "Check Text", "Executes the children elements if the text held by a variable\nis equal to a given text or another variable content.");
		ConIfTexVar.setArgumentDescription(0, new VariableOnly(), "Variable");
		ConIfTexVar.setArgumentDescription(1, new TextOrVariable(), "Text");
		ConIfTexVar.addOptionalParameter(0, new BooleanOrVariable("False"), "Ignore Case", "If true, upper- and lowercase differences are ignored.");
		return(ConIfTexVar);
	}
	
}


