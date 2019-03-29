package functionality.conditions;

import dataTypes.FunctionalityConditionContent;
import dataTypes.FunctionalityContent;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import execution.Execution;
import javafx.scene.input.KeyCode;
import main.functionality.Functionality;
import main.functionality.helperControlers.Parameters;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.KeyChecker;

public class Cond02_Input extends Functionality {

	public static int POSITION = 2;
	public static String NAME = "Hardware Input";
	public static String IDENTIFIER = "CondInputNode";
	public static String DESCRIPTION = "Conditions related to hardware input like keyboards or GPIO.\n"
									 + "Drag into the queue and provide the desired parameters.";
	
	public static boolean DEACTIVATED = true;
	
	/*
	//////////
	// Keyboard input
	static public FunctionalityContent create_ConKeyDown()
	{			
		FunctionalityContent functionality = new FunctionalityConditionContent();
		
		return(functionality.setup(
				() -> {
						Parameters params = functionality.getParameters();
					
						KeyCode key = KeyChecker.getKeyCode(params.getString(0).toUpperCase());
						if (key == null)
						{
							Execution.setError("The following text is not a valid key representation: " + params.getString(0), false);
							return(false);
						}
						return(KeyChecker.getCurrentlyDown().contains(key));
						
					}));
	}
	static public VisualizableProgramElement visualize_ConKeyDown(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Keyboard Down", "Executes nested actions when a given button or letter is held down on a keyboard.\nThe 'Key' should be text (or a variable with text) consisting of the letter to check\nor another 'KeyCode' like 'CONTROL', 'SPACE', etc.\nSearch for 'JavaFX KeyCode' for a list of all possibilities.");
		vis.setArgumentDescription(0, new TextOrVariable(), "Key Name");
		return(vis);
	}
	
		

	
	// GPIO Conditional action
	static public FunctionalityContent create_ConGPIOresp()
	{
		FunctionalityContent functionality = new FunctionalityConditionContent();
		
		return(functionality.setup(
				() -> {
					Parameters params = functionality.getParameters();
					
					int debounce = defaultDebounce;
					if (params.hasOptParam(1))// content[0].hasOptionalArgument(1))
						debounce = params.getOptInt(0);//  content[0].getOptionalArgumentValue(0));
		
					return(GPIOctrl.checkInputPin(params.getInt(0), params.getBool(1), debounce));
					}));
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
	*/
	
	
	/*
	// ALREADY DEFINED IN another file!
	
	
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
	*/
	
	
}


