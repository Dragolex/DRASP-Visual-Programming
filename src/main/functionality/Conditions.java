package main.functionality;

import dataTypes.DataNode;
import dataTypes.ProgramElement;

public abstract class Conditions extends UsedFunctionalityFlags {

	private static int nodeTreeStructureVersion = 105; // Increase when changing the tree! It's required for loading files correctly

	protected static int getVersion() {return(nodeTreeStructureVersion);};
	
	
	public static void applyStandardTree(DataNode<ProgramElement> conditions)
	{
		// Conditions
		
		/*
		DataNode<ProgrammElement> condvariableNode = attachToNodeUndelatable(conditions, create_CondVariableNode());
		
		attachToNodeUndelatable(condvariableNode, create_ConIfVarExists());
		attachToNodeUndelatable(condvariableNode, create_ConIfBoolVar());
		attachToNodeUndelatable(condvariableNode, create_ConIfNumVar());
		attachToNodeUndelatable(condvariableNode, create_ConIfTexVar());
		// ... more VARIABLE conditions
		*/
		
		/*
		DataNode<ProgrammElement> condInputNode = attachToNodeUndelatable(conditions, create_CondInputNode());
		
		attachToNodeUndelatable(condInputNode, create_ConKeyDown());
		attachToNodeUndelatable(condInputNode, create_ConGPIOresp());
		// ... More INPUT conditions
		*/

		//DataNode<ProgrammElement> condConnNode = attachToNodeUndelatable(conditions, create_CondConnNode());
		//attachToNodeUndelatable(condConnNode, create_ConConnExist()); // moved to misc nodes
		
		/*
		DataNode<ProgrammElement> condMiscNode = attachToNodeUndelatable(conditions, create_CondMiscNode());
		attachToNodeUndelatable(condMiscNode, create_ConAskUser());
		attachToNodeUndelatable(condMiscNode, create_ConIfLucky());
		attachToNodeUndelatable(condMiscNode, create_ConConnExist());
		attachToNodeUndelatable(condMiscNode, create_ConDeviceAccessible());
		 */		
		

		
		
		//DataNode<ProgrammElement> condCustomNode = 
				//attachToNodeUndelatable(conditions, create_CondCustomNode());
				

		//////////
	}
	
	
	
	
	// CONDITIONS root nodes
	
	/*
	// Variables
	static public ProgramContent create_CondVariableNode()
	{
		return(new ProgramContent("CondVariableNode"));
	}
	static public VisualizableProgrammElement visualize_CondVariableNode(ProgramContent content)
	{
		return(new VisualizableProgrammElement(content, "Variable Check", "Conditions related to variables set and modified by the corespüondings tructures.\n"
																		+ "Drag into the queue and provide the desired parameters.", false));
	}
	*/
	
	/*
	// Hardware
	static public ProgramContent create_CondInputNode()
	{
		return(new ProgramContent("CondInputNode"));
	}
	static public VisualizableProgrammElement visualize_CondInputNode(ProgramContent content)
	{
		return(new VisualizableProgrammElement(content, "Hardware Input", "Conditions related to hardware input like keyboards or GPIO.\n"
																		+ "Drag into the queue and provide the desired parameters.", false));
	}
	
	
	
	// Misc
	static public ProgramContent create_CondMiscNode()
	{
		return(new ProgramContent("CondMiscNode"));
	}
	static public VisualizableProgrammElement visualize_CondMiscNode(ProgramContent content)
	{
		return(new VisualizableProgrammElement(content, "Miscellaneous", "Various conditions for purpsoes not justifying their own category.", false));
	}

	
	*/
	
	/*
	// custom
	static public ProgramContent create_CondCustomNode()
	{
		return(new ProgramContent("CondCustomNode"));
	}
	static public VisualizableProgrammElement visualize_CondCustomNode(ProgramContent content)
	{
		return(new VisualizableProgrammElement(content, "Custom with Values", "Conditions with defined values.\nDrag here from the queue to save for re-use.\n"
																			+ "Keep <CTRL> pressed when dragging to delete from here!", true));
	}
	*/
	
	
	
	
	// CONDITIONS
	
	/*
	// Variable check
	public static ProgrammElement create_ConIfVarExists()
	{
		Object[] input = new Object[1];
		ProgramContent[] content = new ProgramContent[1];
		
		return(content[0] = new ProgramConditionContent("ConIfVarExists",
				input,
				() -> {
					
					return(((Variable) input[0]).hasValue());
					
					}));
	}
	public static ProgrammElement visualize_ConIfVarExists(ProgramContent content)
	{	
		VisualizableProgrammElement ConIfNumVar;
		ConIfNumVar = new VisualizableProgrammElement(content, "Variable Exists", "Executes the nested blocks if the variable already exists\nand therefore has a value. Note that you can use the optional\n 'Not' argument to execute if it does not exist\nor use the else block.");
		ConIfNumVar.setArgumentDescription(0, new VariableOnly(true, false), "Variable");
		return(ConIfNumVar);
	}
	
	
	
	*/
	// Is used by an Event as well!
	/*
	public static boolean checkMultiNumVar(Object[] input, ProgramContent[] content, int firstExpArg)
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
	*/
	/*
	
	// Variable check
	public static ProgrammElement create_ConIfNumVar()
	{
		Object[] input = new Object[2];
		ProgramContent[] content = new ProgramContent[1];
		
		return(content[0] = new ProgramConditionContent("ConIfNumVar",
				input,
				() -> {
					
						return(checkMultiNumVar(input, content, 2));
						
					}));
	}
	public static ProgrammElement visualize_ConIfNumVar(ProgramContent content)
	{	
		VisualizableProgrammElement ConIfNumVar;
		ConIfNumVar = new VisualizableProgrammElement(content, "Check Number", "Executes the child-elements if the comparation of the number value\nof a variable with a term, value or other variable, figures as 'true'.\n"
				+ "Examples of comparisons: '= 20', '< 150', etc.\n"
				+ "You can use additional terms to check multiple conditions at once\n"
				+ "for example to check whether the value lies between two values.");
		ConIfNumVar.setArgumentDescription(0, new VariableOnly(true, false), "Variable");
		ConIfNumVar.setArgumentDescription(1, new TermValueVarComp(true), "Term or Value");
		ConIfNumVar.setExpandableArgumentDescription(TermValueVarComp.class, null, true, false, "Term or Value #", 16, "Up to 16 mroe terms.");
		return(ConIfNumVar);
	}
	
	
	
	
	// Binary check
	public static ProgrammElement create_ConIfBoolVar()
	{		
		Object[] input = new Object[1];
		return( new ProgramConditionContent("ConIfBoolVar",
				input,
				() -> {}
				,
				() -> {
						return(checkIfBoolVar((Variable) input[0]));
					}));
	}
	public static ProgrammElement visualize_ConIfBoolVar(ProgramContent content)
	{	
		VisualizableProgrammElement ConIfNumVar;
		ConIfNumVar = new VisualizableProgrammElement(content, "Check True", "Executes the child elements if the variable contains something binary figuring as 'true'.\nAmong actually binary variables that includes any number larger or equal to 0.5\nand also a text consisting of 'true' (upper and lowercase are ignored).");
		ConIfNumVar.setArgumentDescription(0, new VariableOnly(true, false), "Variable");
		return(ConIfNumVar);
	}
	
	
	
	
	public static ProgrammElement create_ConIfTexVar()
	{
	Object[] params = new Object[2];
	ProgramConditionContent[] content = new ProgramConditionContent[1];
	
	return(content[0] = new ProgramConditionContent("ConIfTexVar",
			params,
			() -> {}
			,
			() -> {
					if (content[0].getOptionalArgTrue(0))
						return(((Variable) params[0]).get().toString().equalsIgnoreCase((((Variable) params[1]).get()).toString()));
					return(((Variable) params[0]).get().toString().equals((((Variable) params[1]).get()).toString()));
				}));
	}
	public static ProgrammElement visualize_ConIfTexVar(ProgramContent content)
	{	
		VisualizableProgrammElement ConIfTexVar;
		ConIfTexVar = new VisualizableProgrammElement(content, "Check Text", "Executes the children elements if the text held by a variable\nis equal to a given text or another variable content.");
		ConIfTexVar.setArgumentDescription(0, new VariableOnly(), "Variable");
		ConIfTexVar.setArgumentDescription(1, new TextOrVariable(), "Text");
		ConIfTexVar.addOptionalParameter(0, new BooleanOrVariable("False"), "Ignore Case", "If true, upper- and lowercase differences are ignored.");
		return(ConIfTexVar);
	}
	*/
	
	
	/*
	// Keyboard input
	static public ProgramContent create_ConKeyDown()
	{
		Object[] input = new Object[1];
		return(new ProgramConditionContent( "ConKeyDown",
				input,
				() -> {
					}
				,
				() -> {
						KeyCode key = KeyChecker.getKeyCode(((String) input[0]).toUpperCase());
						if (key == null)
						{
							Execution.setError("The following text is not a valid key representation: " + (String) input[0], false);
							return(false);
						}
						return(KeyChecker.getCurrentlyDown().contains(key));
						
					}));
	}
	static public VisualizableProgrammElement visualize_ConKeyDown(ProgramContent content)
	{
		VisualizableProgrammElement conKeyDown;
		conKeyDown = new VisualizableProgrammElement(content, "Keyboard Down", "Executes nested actions when a given button or letter is held down on a keyboard.\nThe 'Key' should be text (or a variable with text) consisting of the letter to check\nor another 'KeyCode' like 'CONTROL', 'SPACE', etc.\nSearch for 'JavaFX KeyCode' for a list of all possibilities.");
		conKeyDown.setArgumentDescription(0, new TextOrVariable(), "Key Name");
		return(conKeyDown);
	}
		
		
		
	// GPIO Conditional action
	static public ProgramContent create_ConGPIOresp()
	{
		Object[] gpioVarsA = new Object[2];
		ProgramContent[] content = new ProgramContent[1];
		content[0] = new ProgramConditionContent( "ConGPIOresp",
				gpioVarsA,
				() -> {
					
					int debounce = defaultDebounce;
					if (content[0].hasOptionalArgument(1))
						debounce = getIntegerParameter( content[0].getOptionalArgumentValue(0));
					
					return(GPIOctrl.checkInputPin(getIntegerParameter( gpioVarsA[0]), (boolean) gpioVarsA[1], debounce));
					
					});
		return(content[0]);
	}
	static public VisualizableProgrammElement visualize_ConGPIOresp(ProgramContent content)
	{
		VisualizableProgrammElement conGPIOresp;
		conGPIOresp = new VisualizableProgrammElement(content, "GPIO Signal Check", "Executes nested actions if a GPIO button has been activated.");
		conGPIOresp.setArgumentDescription(0, new ValueOrVariable(), "GPIO Pin", "GPIO pin to check." + gpioPinReferenceText);
		conGPIOresp.setArgumentDescription(1, new BooleanOrVariable("False"), "Pull-Up-Res", "If true, the system relies on having a PULL-UP-resistor applied to the GPIO (see the graphic)\nand that means your attached switch/button has to conntect to ground.\nIf false, the PULL-DOWN resistor is assumed and the switch/button should connect to Vcc (3.3v).");
		conGPIOresp.setOptionalArgument(0, new ValueOrVariable(String.valueOf(defaultDebounce)), "Debounce", "Physical switches, buttons and relais tend to 'bounce' when used.\nThat means for a tiny fraction of a second, the signal jumps up and down.\n'Debouncing' is a countermeassure which simply expects the signal to\nstay steadily for a number of milliseconds.\30ms is reccommended and perfect for all kind of human input.\nUse 0 if you have electronic inputs (like from a microcontroler or another PI).");
		return(conGPIOresp);
	}
	*/
	
	/*
	
	static public ProgramContent create_ConAskUser()
	{
		Object[] params = new Object[1];
		ProgramContent[] content = new ProgramContent[1];
		content[0] = new ProgramConditionContent( "ConAskUser",
				params,
				() -> {
						if (content[0].hasOptionalArgument(0))
						{
							if (content[0].getTotalOptionalOrExpandedArgumentsCount()>1)
							{
								String[] buttons = new String[content[0].getTotalOptionalOrExpandedArgumentsCount()-1];
								for(int i = 1; i < content[0].getTotalOptionalOrExpandedArgumentsCount(); i++)
									buttons[i-1] = (String) content[0].getTotalOptionalOrExpandedArgumentsArray()[i];
								
								double res = GuiMsgHelper.askQuestion((String) params[0], buttons, false);
								initVariableAndSet((Variable) content[0].getOptionalArgumentValue(0), Variable.doubleType, res);
								return(true);
							}
							else
							{								
								double res = GuiMsgHelper.askQuestion((String) params[0], new String[]{"Yes", "No"}, true);
								if (res == -1) res = 2;
								initVariableAndSet((Variable) content[0].getOptionalArgumentValue(0), Variable.doubleType, res);
								return(true);
							}
						}
						else
							return(0 == GuiMsgHelper.askQuestion((String) params[0], new String[]{"Yes", "No"}, false));
						
					}).removeFixedOptionalArguments();
		return(content[0]);
	}
	static public VisualizableProgrammElement visualize_ConAskUser(ProgramContent content)
	{
		VisualizableProgrammElement vv;
		vv = new VisualizableProgrammElement(content, "Ask Question", "Shows a popup message asking the user a given question.\nDefault are binary questions. If the user presses 'Yes',\nthe child-element-block will be executed.\nOtherwise the Else-block will.\n\nUsing the optional parameters\nyou can define your own results\nand they will be placed in a variable.");
		vv.addParameter(0, new TextOrVariable("Yes or no?"), "Question", "The question Text. Use '\\n' as a new-line symbol.");
		vv.addOptionalParameter(0, new VariableOnly(true, true), "Response Index", "Index of the button pressed by the user placed in a variable.\nThe child-elements will always be executed 8and never the else block)\nbut you can react accordingly using the value from this variable.\nNote if you provide this argument but no additional buttons,\nthe default will be 'Yes', 'No', 'Cancel'");
		vv.setExpandableArgumentDescription(TextOrVariable.class, null, false, false, "Button #", 8, "Up to 8 buttons which will be shown in the popup.");
		return(vv);
	}

	
	
	// Random based
	static public ProgramContent create_ConIfLucky()
	{
		Object[] params = new Object[1];
		ProgramContent[] content = new ProgramContent[1];
		content[0] = new ProgramConditionContent( "ConIfLucky",
				params,
				() -> {
						Random rn = randomizer;
						if (content[0].hasOptionalArgument(3))
							rn = (Random) content[0].getOptionalArgumentValue(0);
					
						return(rn.nextDouble() > (((double) params[0])/100));
					});
		return(content[0]);
	}
	static public VisualizableProgrammElement visualize_ConIfLucky(ProgramContent content)
	{
		VisualizableProgrammElement vv;
		vv = new VisualizableProgrammElement(content, "Lucky", "Executes nested actions with a given probability.\nFor example if using the value '50'\nit will execute every second time.\nYou can use the ELSE structure element with this too.");
		vv.addParameter(0, new ValueOrVariable(), "Probability (%)", "Probability between 0 to 100.");
		vv.addOptionalParameter(0, new VariableOnly(), "Randomizer Ident", "Identifier to a randomizer created with the corresponding action.\nThis makes sense if you want to set a fixed seed to the random-generator.");
		return(vv);
	}
	
	
	
	static public ProgramContent create_ConConnExist()
	{
		Object[] params = new Object[1];
		ProgramContent[] content = new ProgramContent[1];
		content[0] = new ProgramConditionContent( "ConConnExist",
				params,
				() -> {
					
						return(CONctrl.connectionExists((String) params[0], false));
						
					});
		return(content[0]);
	}
	static public VisualizableProgrammElement visualize_ConConnExist(ProgramContent content)
	{
		VisualizableProgrammElement vv;
		vv = new VisualizableProgrammElement(content, "Connection Exists", "Executes the child-elements if the device is connected\nto a device matching the given filter.\nUse this to check whether connecting to a device has been succesful.");
		vv.setArgumentDescription(0, new TextOrVariable(CONctrl.identQstr), "Device Filter");
		return(vv);
	}
	

	static public ProgramContent create_ConDeviceAccessible()
	{
		Object[] params = new Object[1];
		ProgramContent[] content = new ProgramContent[1];
		content[0] = new ProgramConditionContent( "ConDeviceAccessible",
				params,
				() -> {
					
						return(CONctrl.connectionExists((String) params[0], false));
						
					});
		return(content[0]);
	}
	static public VisualizableProgrammElement visualize_ConDeviceAccessible(ProgramContent content)
	{
		VisualizableProgrammElement vv;
		vv = new VisualizableProgrammElement(content, "Computer Accessible", "Executes the child-elements if accessing a computer defined\nwith the Action 'External Computer' is possible.");
		vv.setArgumentDescription(0, new VariableOnly(), "Computer Identifier");
		return(vv);
	}

	*/
	

	
}
