package functionality.events;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import javafx.scene.input.KeyCode;
import main.functionality.Functionality;
import main.functionality.helperControlers.KeyToCheck;
import main.functionality.helperControlers.OnlyOnceHelper;
import main.functionality.helperControlers.hardware.analog.RotaryDevice;
import main.functionality.helperControlers.hardware.analog.SensorDevice;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.DebugMsgHelper;
import staticHelpers.KeyChecker;

public class Ev01_HardwareInput extends Functionality {

	public static int POSITION = 1;
	public static String NAME = "Hardware Input";
	public static String IDENTIFIER = "EvenInputNode";
	public static String DESCRIPTION = "Events based on hardware input like a keyboard or GPIO.\nDrag into the queue and provide the desired parameters.\nWhen the condition is met, nested actions are executed.";
	
	
	// Keyboard event
	public static ProgramElement create_EvKeyPressed()
	{
		Object[] input = new Object[2];
		Object[] params = new Object[1];
		
		KeyToCheck keyChecker = new KeyToCheck();
		
		ProgramEventContent[] cont = new ProgramEventContent[1];
		
		
		cont[0] = new ProgramEventContent("EvKeyPressed",
				input, params,
				() -> {
					keyPressedEventContents.add(cont[0]);
					keyChecker.clear();
				},
				() -> {
					
					if (cont[0].getArgumentIsConstant(0))
					{
						if(!keyChecker.isPrepared())
							keyChecker.update((String) params[0]);
					}
					else
						keyChecker.updateIfChanged((String) params[0]);
										
					if (keyChecker.check((KeyCode) input[0], ((boolean) input[1]), true, cont[0].getOptionalArgTrue(0) ))
					{
						if (cont[0].hasOptionalArgument(1))
							applyCorrespondingKeyToVariable((Variable) cont[0].getOptionalArgumentValue(1), (KeyCode) input[0]);
						return(true);
					}
					else
						return(false);
					
				});
		
		//if (!currentlyLoadingIDEelements)
			//keyPressedEventContents.add(cont[0]);
		
		return(cont[0]);
	}
	public static ProgramElement visualize_EvKeyPressed(FunctionalityContent content)
	{
		VisualizableProgramElement evKeyDown;
		evKeyDown = new VisualizableProgramElement(content, "Keyboard Pressed", "Activates when a given key or key-type is presssed on a keyboard.");
		evKeyDown.addParameter(0, new TextOrVariable(), "Key Name", keyboardKeyDescription);
		evKeyDown.addOptionalParameter(0, new BooleanOrVariable("False"), "Auto Repeat", "If true and the key is a letter,\nthe event will be executed multiple times while the key is held down.\nUnlike the 'Keyboard Down', system settings are used for that.");
		evKeyDown.addOptionalParameter(1, new VariableOnly(true, true), "Key Variable", "If using a keyword involving multiple keys,\nthe key or letter will be placed in this variable.");
		return(evKeyDown);
	}
	
	
	// Keyboard event
	public static ProgramElement create_EvKeyDown()
	{ 
		KeyToCheck keyChecker = new KeyToCheck();
		Object[] params = new Object[1];
		
		ProgramEventContent[] cont = new ProgramEventContent[1];
		return(cont[0] = new ProgramEventContent( "EvKeyDown",
				params,
				() -> {
					keyChecker.clear();
				},
				() -> {
					
						if (cont[0].getArgumentIsConstant(0))
						{
							if(!keyChecker.isPrepared())
								keyChecker.update((String) params[0]);
						}
						else
							keyChecker.updateIfChanged((String) params[0]);
						
						for(KeyCode key: KeyChecker.getCurrentlyDown())
						{
							if (keyChecker.check(key, true, true, true ))
							{
								if (cont[0].hasOptionalArgument(1))
									applyCorrespondingKeyToVariable((Variable) cont[0].getOptionalArgumentValue(1), key);
								
								return(!cont[0].getOptionalArgTrue(0));
							}
							else
								return(cont[0].getOptionalArgTrue(0));
						}
						
						return(false);
					}));
	}
	public static ProgramElement visualize_EvKeyDown(FunctionalityContent content)
	{
		VisualizableProgramElement evKeyDown;
		evKeyDown = new VisualizableProgramElement(content, "Keyboard Down", "Constantly activates when a given key or key-type is held down on a keyboard.");
		evKeyDown.addParameter(0, new TextOrVariable(), "Key Name", keyboardKeyDescription);
		evKeyDown.addOptionalParameter(0, new BooleanOrVariable("False"), "NOT", "If true, the checking is inverted and the event is triggered when the key is not down.");
		evKeyDown.addOptionalParameter(1, new VariableOnly(true, true), "Key Variable", "If using a keyword involving multiple keys,\nthe key or letter will be placed in this variable.");
		return(evKeyDown);
	}
	
	
	// Keyboard event
	public static ProgramElement create_EvKeyReleased()
	{
		Object[] input = new Object[2];
		Object[] params = new Object[1];
		
		KeyToCheck keyChecker = new KeyToCheck();
		
		ProgramEventContent[] cont = new ProgramEventContent[1];
		cont[0] = new ProgramEventContent("EvKeyReleased",
				input, params,
				() -> {
					keyChecker.clear();
				},
				() -> {
					
					if (cont[0].getArgumentIsConstant(0))
					{
						if(!keyChecker.isPrepared())
							keyChecker.update((String) params[0]);
					}
					else
						keyChecker.updateIfChanged((String) params[0]);
										
					if (keyChecker.check((KeyCode) input[0], ((boolean) input[1]), false, false ))
					{
						if (cont[0].hasOptionalArgument(0))
							applyCorrespondingKeyToVariable((Variable) cont[0].getOptionalArgumentValue(0), (KeyCode) input[0]);
						return(true);
					}
					else
						return(false);
					
				});
		
		if (!currentlyLoadingIDEelements)
			keyPressedEventContents.add(cont[0]);
		
		return(cont[0]);
	}
	public static ProgramElement visualize_EvKeyReleased(FunctionalityContent content)
	{
		VisualizableProgramElement evKeyDown;
		evKeyDown = new VisualizableProgramElement(content, "Keyboard Released",  "Activates when a given key or key-type is released on a keyboard.");
		evKeyDown.addParameter(0, new TextOrVariable(), "Key Name", keyboardKeyDescription);
		evKeyDown.addOptionalParameter(0, new VariableOnly(true, true), "Key Variable", "If using a keyword involving multiple keys,\nthe key or letter will be placed in this variable.");
		return(evKeyDown);
	}
	
	
	static private void applyCorrespondingKeyToVariable(Variable var, KeyCode key)
	{
		if(key.isWhitespaceKey())
			var.initTypeAndSet(Variable.textType, " "); // As upper case
		else
		// Set the new variable value
		if (KeyChecker.isDown(KeyCode.SHIFT))
			var.initTypeAndSet(Variable.textType, key.getName()); // As upper case
		else
			var.initTypeAndSet(Variable.textType, key.getName().toLowerCase()); // As lower case
	}

	
	
	/*
	// Keyboard event
	public static ProgrammElement create_EvKeyPressed()
	{
		Object[] kybVarsB = new Object[1];
		Boolean[] lastState = new Boolean[1];
		
		String[] lastKeyString = new String[1];
		Boolean[] universalKeySetup = new Boolean[7];
		String[] lastKey = new String[1];
		List<KeyCode> universalAdditionalKeyCodes = new ArrayList<>();

		ProgramEventContent[] content = new ProgramEventContent[1];
		
		
		return(content[0] = new ProgramEventContent( "EvKeyPressed",
				kybVarsB,
				() -> {
					for(int i = 0; i < universalKeySetup.length; i++)
						universalKeySetup[i] = false;
					
					lastKeyString[0] = null;
					universalAdditionalKeyCodes.clear();
					lastKey[0] = null;
				},
				
				() -> {
					
						boolean res = false;//advancedCheckKey((String) kybVarsB[0], content, lastKeyString, universalKeySetup, universalAdditionalKeyCodes, lastKey, lastState);
					
						if (lastState[0] == null)
						{
							lastState[0] = res;
							return(false);
						}
						else
							if (!lastState[0])
								return(lastState[0] = res);
							else
								lastState[0] = res;
						return(false);
						
					}));
	}
	public static ProgrammElement visualize_EvKeyPressed(ProgramContent content)
	{
		VisualizableProgrammElement evKeyDown;
		evKeyDown = new VisualizableProgrammElement(content, "Keyboard Pressed", "Activates when a given key or letter is presssed on a keyboard.\nThe 'Key' should be text (or a variable with text) consisting of the letter to check.\nFor example 'A', 'B', '1' and so on.\nAlternatively, special keys are available like 'CONTROL', 'SPACE', etc.\nSearch online for 'JavaFX KeyCode' for a list of all possibilities.\n\nATTENTION: If you use the optional parameter 'Unique' to false,\ndo not use less than 100 ms for 'min wait'\nor an endless loop can occur because the key is never released!");
		evKeyDown.setArgumentDescription(0, new TextOrVariable(), "Key Name");
		evKeyDown.setOptionalArgument(0, new VariableOnly(true, true), "Key Variable", "If using a keyword involving multiple keys,\nthe key or letter will be placed in this variable.");
		return(evKeyDown);
	}
	
	
	
	// Keyboard event
	public static ProgrammElement create_EvKeyDown()
	{ 
		
		Object[] kybVarsB = new Object[1];
		ProgramEventContent[] content = new ProgramEventContent[1];
		return(content[0] = new ProgramEventContent( "EvKeyDown",
				kybVarsB,
				() -> {
					}
				,
				() -> {
					
						KeyCode key = KeyChecker.getKeyCode(((String) kybVarsB[0]).toUpperCase());
						if (key == null)
						{
							Execution.setError("The following text is not a valid key representation: " + (String) kybVarsB[0], false);
							return(false);
						}
						
						KeyChecker.addKeyToCheck(key);
						if (content[0].getOptionalArgTrue(0))
							return(!KeyChecker.isDown(key));
						else
							return(KeyChecker.isDown(key));						
					}));
	}
	public static ProgrammElement visualize_EvKeyDown(ProgramContent content)
	{
		VisualizableProgrammElement evKeyDown;
		evKeyDown = new VisualizableProgrammElement(content, "Keyboard Down", "Constantly activates when a given key or letter is held down on a keyboard.\nThe 'Key' should be text (or a variable with text) consisting of the letter to check.\nFor example 'A', 'B', '1' and so on.\nAlternatively, special keys are available like 'CONTROL', 'SPACE', etc.\nSearch online for 'JavaFX KeyCode' for a list of all possibilities.\n\nATTENTION: If you set 'Unique' to false, do not use at least 100 ms for 'min wait'\nor an endless loop can occur because the key is never released!");
		evKeyDown.setArgumentDescription(0, new TextOrVariable(), "Key Name");
		evKeyDown.setOptionalArgument(0, new BooleanOrVariable("False"), "NOT", "If true, the checking is inverted and the event is triggered when the key is not down.");
		return(evKeyDown);
	}
	
	
	// Keyboard event
	public static ProgrammElement create_EvKeyReleased()
	{
		Object[] kybVarsB = new Object[1];
		ProgramEventContent[] content = new ProgramEventContent[1];
		Boolean[] lastState = new Boolean[1];
		return(content[0] = new ProgramEventContent( "EvKeyReleased",
				kybVarsB,
				() -> {
						KeyCode key = KeyChecker.getKeyCode(((String) kybVarsB[0]).toUpperCase());					
						if (key == null)
						{
							Execution.setError("The following text is not a valid key representation: " + (String) kybVarsB[0], false);
							return(false);
						}
						
						KeyChecker.addKeyToCheck(key);
												
						if (lastState[0] == null)
						{
							lastState[0] = KeyChecker.isDown(key);
							return(false);
						}
						else
							if (lastState[0])
								return(!(lastState[0] = KeyChecker.isDown(key)));
							else
								lastState[0] = KeyChecker.isDown(key);
						return(false);
					}));
	}
	public static ProgrammElement visualize_EvKeyReleased(ProgramContent content)
	{
		VisualizableProgrammElement evKeyDown;
		evKeyDown = new VisualizableProgrammElement(content, "Keyboard Released",  "Activates when a given key or letter is released on a keyboard.\nThe 'Key' should be text (or a variable with text) consisting of the letter to check.\nFor example 'A', 'B', '1' and so on.\nAlternatively, special keys are available like 'CONTROL', 'SPACE', etc.\nSearch online for 'JavaFX KeyCode' for a list of all possibilities.\n\nATTENTION: If you use the optional parameter 'Unique' to false,\ndo not use less than 100 ms for 'min wait'\nor an endless loop can occur because the key is never released!");
		evKeyDown.setArgumentDescription(0, new TextOrVariable(), "Key Name");
		return(evKeyDown);
	}
	*/
	
	
	
	
	public static ProgramElement create_EvGPIOchanged()
	{
		Object[] input = new Object[2];
		Object[] params = new Object[3];
		
		Integer[] currentPinInd = new Integer[1];
		Boolean[] currentPinRes = new Boolean[1];
		
		Boolean[] checkedConstant = new Boolean[1];
		checkedConstant[0] = false;
		
		ProgramEventContent[] cont = new ProgramEventContent[1];
		
		
		cont[0] = new ProgramEventContent("EvGPIOchanged",
				input, params,
				() -> {
					GPIOchangedEventContents.add(cont[0]);
					currentPinInd[0] = null;
					currentPinRes[0] = null;
					checkedConstant[0] = false;
				},
				() -> {
					
					if (!checkedConstant[0])
					if (cont[0].getArgumentIsConstant(0) && cont[0].getArgumentIsConstant(2) && ((!cont[0].hasOptionalArgument(0)) || cont[0].getAdditionalArgumentIsConstant(0))) // if the pin argument, the res argument as well as the debounce values are constant
					{
						GPIOctrl.checkInputPin(getINTparam( params[0] ), (boolean) params[2], 
								(cont[0].hasOptionalArgument(0)) ? 
										getINTparam( cont[0].getOptionalArgumentValue(0) )
										: defaultDebounce
								); // check the input once so the event-caller system is active
						
						checkedConstant[0] = true;
					}
					else // arguments are not constant
					{
						if (((currentPinInd[0] != getINTparam( params[0] )))
								|| (currentPinRes[0] != (Boolean) params[2]))
						{
							currentPinInd[0] = getINTparam( params[0] );
							currentPinRes[0] = (Boolean) params[2];
							
							GPIOctrl.resetCheckingInputPin(getINTparam( params[0] )); // reset so it can be reinitialized
							
							if (DEBUG) DebugMsgHelper.associateGPIOvariable(cont[0], 0);
							
							GPIOctrl.checkInputPin(getINTparam( params[0] ), (boolean) params[2], 
								(cont[0].hasOptionalArgument(0)) ? 
									getINTparam( cont[0].getOptionalArgumentValue(0) )
										: defaultDebounce
								); // check the input once so the event-caller system is active
						}
					}
					
					return(false);
				},
				() -> {
					
					if ((int) input[0] != getINTparam( params[0] )) // if the pin doesn't match
						return(false);
					
					return( ((boolean) input[1]) == ((boolean) params[1]) ); // return whether the given value matches with the desired value
					
				});
		
		
		return(cont[0]);
	}
	public static ProgramElement visualize_EvGPIOchanged(FunctionalityContent content)
	{
		VisualizableProgramElement evGPIOresp;
		evGPIOresp = new VisualizableProgramElement(content, "GPIO Changed", "Activates when a GPIO pin changes its connected state.");
		evGPIOresp.addParameter(0, new ValueOrVariable(), "GPIO Pin", "Pin to check." + gpioPinReferenceText);
		evGPIOresp.addParameter(1, new BooleanOrVariable("True"), "When", "If 'true', the event will fire when the pin went from not-connected, to connected,\nso if a button is attached to the pin, this event fires when pressed.\nIf the argument is 'false', it triggers when the button is released.");
		evGPIOresp.addParameter(2, new BooleanOrVariable("False"), "Pull-Up-Res", "If true, the system relies on having a PULL-UP-resistor applied to the GPIO (see the graphic)\nand that means your attached switch/button has to conntect to ground.\nIf false, the PULL-DOWN resistor is assumed and the switch/button should connect to Vcc (3.3v).");
		evGPIOresp.addOptionalParameter(0, new ValueOrVariable(String.valueOf(defaultDebounce)), "Debounce", "Physical switches, buttons and relais tend to 'bounce' when used.\nThat means for a tiny fraction of a second, the signal jumps up and down.\n'Debouncing' is a countermeassure which simply expects the signal to\nstay steadily for a number of milliseconds.\30ms is reccommended and perfect for all kind of human input.\nUse 0 if you have electronic inputs (like from a microcontroler or another PI).");
		return(evGPIOresp);
	}

	

	
	// GPIO Event
	public static ProgramElement create_EvGPIOresp()
	{
		Object[] gpioVarsB = new Object[3];
		ProgramEventContent[] content = new ProgramEventContent[1];
		return(content[0] = new ProgramEventContent( "EvGPIOresp",
				gpioVarsB,
				() -> {
						if (DEBUG) DebugMsgHelper.associateGPIOvariable(content[0], 0);
					
						int debounce = defaultDebounce;
						if (content[0].hasOptionalArgument(1))
							debounce = getINTparam( content[0].getOptionalArgumentValue(0) );
						
						return(((boolean) gpioVarsB[1]) == GPIOctrl.checkInputPin(getINTparam( gpioVarsB[0] ), (boolean) gpioVarsB[2], debounce));
						
					}));
	}
	public static ProgramElement visualize_EvGPIOresp(FunctionalityContent content)
	{
		VisualizableProgramElement evGPIOresp;
		evGPIOresp = new VisualizableProgramElement(content, "GPIO State", "Fires constantly when a GPIO has the desired state (connected/not conncted).");
		evGPIOresp.addParameter(0, new ValueOrVariable(), "GPIO Pin", "Pin to check." + gpioPinReferenceText);
		evGPIOresp.addParameter(1, new BooleanOrVariable("True"), "When", "If 'true', the event will fire while the pin is connected.\nOtherwise it executes all the time when it is not connected.");
		evGPIOresp.addParameter(2, new BooleanOrVariable("False"), "Pull-Up-Res", "If true, the system relies on having a PULL-UP-resistor applied to the GPIO (see the graphic)\nand that means your attached switch/button has to conntect to ground.\nIf false, the PULL-DOWN resistor is assumed and the switch/button should connect to Vcc (3.3v).");
		evGPIOresp.addOptionalParameter(0, new ValueOrVariable(String.valueOf(defaultDebounce)), "Debounce", "Physical switches, buttons and relais tend to 'bounce' when used.\nThat means for a tiny fraction of a second, the signal jumps up and down.\n'Debouncing' is a countermeassure which simply expects the signal to\nstay steadily for a number of milliseconds.\n30ms is reccommended and perfect for all kind of human input.\nUse 0 if you have electronic inputs (like from a microcontroler or another PI).");
		return(evGPIOresp);
	}
	
	

	public static ProgramElement create_EvAnalogInputChanged()
	{
		Object[] params = new Object[4];
		ProgramEventContent[] content = new ProgramEventContent[1];
		
		return(content[0] = new ProgramEventContent( "EvAnalogInputChanged",
				params,
				() -> {}
				,
				() -> {
					
					SensorDevice device = (SensorDevice) params[0];
					
					Execution.checkedSleep(getINTparam(params[2]));
					
					double val = device.getValue();
					Double lastVal = device.getLastValue(val);
					
					
					if (lastVal == null)
					{
						if (content[0].getOptionalArgTrue(0))
						{
							initVariableAndSet(params[3], Variable.doubleType, val);
							
							if(content[0].hasOptionalArgument(1))
								initVariableAndSet(content[0].getOptionalArgumentValue(1), Variable.doubleType, val);
							
							return(true);
						}
					}
					else
					if (Math.abs(val - lastVal) > ((double) params[1]))
					{
						initVariableAndSet(params[3], Variable.doubleType, val);
						if(content[0].hasOptionalArgument(1))
							initVariableAndSet(content[0].getOptionalArgumentValue(1), Variable.doubleType, val-lastVal);
						
						return(true);
					}

					return(false);
					
					}));
	}
	public static ProgramElement visualize_EvAnalogInputChanged(FunctionalityContent content)
	{
		VisualizableProgramElement EvIfTexVar;
		EvIfTexVar = new VisualizableProgramElement(content, "Sensor Changed", "Executes when an analog/sensor value changes.\nThe exact, new value is placed in a variable.");
		EvIfTexVar.addParameter(0, new VariableOnly(), "Sensor Device", "Device providing the analog input.");
		EvIfTexVar.addParameter(1, new ValueOrVariable(), "Tolerance", "Tolerance value. When the difference is higher than that, the event will be triggered.");
		EvIfTexVar.addParameter(2, new ValueOrVariable("250"), "Check Delay", "Delay between two checks because values\ncannot change physically fast for certain sensors\n(for example temperature sensors).");	
		EvIfTexVar.addParameter(3, new VariableOnly(true, true), "Value Var", "Variable which will contain the value.");
		EvIfTexVar.addOptionalParameter(0, new BooleanOrVariable("False"), "At Start", "If true, the event will be executed with the very first value once when the program starts.");
		EvIfTexVar.addOptionalParameter(1, new VariableOnly(true, true), "Difference", "Difference to the last value.");
		return(EvIfTexVar);
	}
	
	
	
	
	
	public static ProgramElement create_EvAnalogInputBetween()
	{
		Object[] params = new Object[5];
		ProgramEventContent[] content = new ProgramEventContent[1];
		
		OnlyOnceHelper onlyOnce = new OnlyOnceHelper();
		
		return(content[0] = new ProgramEventContent( "EvAnalogInputBetween",
				params,
				() -> {}
				,
				() -> {
					
					Execution.checkedSleep(getINTparam(params[3]));
					
					SensorDevice device = (SensorDevice) params[0];
					
					double val = device.getValue();
					
					
					if ((val >= ((double) params[1])) && (val <= ((double) params[2])))
					{
						initVariableAndSet(params[4], Variable.doubleType, val);
						return(onlyOnce.handle(true, content[0].getOptionalArgTrue(0)));
					}

					return(onlyOnce.handle(false, content[0].getOptionalArgTrue(0)));
					
					}));
	}
	public static ProgramElement visualize_EvAnalogInputBetween(FunctionalityContent content)
	{
		VisualizableProgramElement EvIfTexVar;
		EvIfTexVar = new VisualizableProgramElement(content, "Sensor Between", "Executes when an analog value is inside a given range.\nThe exact value is placed in a variable.");
		EvIfTexVar.addParameter(0, new VariableOnly(), "Sensor Device", "Device providing the analog input.");
		EvIfTexVar.addParameter(1, new ValueOrVariable(), "Min Value", "Minimum value.");
		EvIfTexVar.addParameter(2, new ValueOrVariable(), "Max Value", "Maximum value.");
		EvIfTexVar.addParameter(3, new ValueOrVariable("250"), "Check Delay", "Delay between two checks because values\ncannot change physically fast for certain sensors\n(for example temperature sensors).");	
		EvIfTexVar.addParameter(4, new VariableOnly(true, true), "Value Var", "Variable which will contain the value when the event performs.");
		EvIfTexVar.addOptionalParameter(0, new BooleanOrVariable("False"), "Only Once", "If true, the event will be executed only once during\nthe whole duration the value meets the condition.\nIf false, the event will be executed constantly.");
		return(EvIfTexVar);
	}
	
	

	
	public static ProgramElement create_EvRotaryInc()
	{
		Object[] params = new Object[1];
		ProgramEventContent[] content = new ProgramEventContent[1];
				
		return(content[0] = new ProgramEventContent( "EvRotaryInc",
				params,
				() -> {					
					
					return(((RotaryDevice) params[0]).incremented());
					
				}));
	}
	public static ProgramElement visualize_EvRotaryInc(FunctionalityContent content)
	{
		VisualizableProgramElement EvIfTexVar;
		EvIfTexVar = new VisualizableProgramElement(content, "Rotary Forth", "Executes when a rotary input device has been incremented.\nAttention: You should only use one event of this kind for every rotary device!");
		EvIfTexVar.addParameter(0, new VariableOnly(), "Rotary Device", "Device providing the rotary input.");
		//EvIfTexVar.addImageToTooltip(name, link);
		return(EvIfTexVar);
	}
	
	
	
	public static ProgramElement create_EvRotaryDec()
	{
		Object[] params = new Object[1];
		ProgramEventContent[] content = new ProgramEventContent[1];
		
		return(content[0] = new ProgramEventContent( "EvRotaryDec",
				params,
				() -> {					
					
					return(((RotaryDevice) params[0]).decremented());
					
				}));
	}
	public static ProgramElement visualize_EvRotaryDec(FunctionalityContent content)
	{
		VisualizableProgramElement EvIfTexVar;
		EvIfTexVar = new VisualizableProgramElement(content, "Rotary Back", "Executes when a rotary input device has been decremented.\nAttention: You should only use one event of this kind for every rotary device!");
		EvIfTexVar.addParameter(0, new VariableOnly(), "Rotary Device", "Device providing the rotary input.");
		
		return(EvIfTexVar);
	}

	
	
	/*
	DEPRECIATED VARIANT OF EvGPIOchanged
	
	public static ProgrammElement create_EvGPIO_changed()
	{
		Object[] gpioVarsB = new Object[3];
		Boolean[] lastState = new Boolean[1];
		ProgramEventContent[] content = new ProgramEventContent[1];
		return(content[0] = new ProgramEventContent( "EvGPIO_changed",
				gpioVarsB,
				() -> {
					lastState[0] = null;
				},
				() -> {
						if (DEBUG) DebugMsgHelper.associateGPIOvariable(content[0], 0);
					
						int debounce = defaultDebounce;
						if (content[0].hasOptionalArgument(0))
							debounce = getINTparam( content[0].getOptionalArgumentValue(0) );

						boolean res = GPIOctrl.checkInputPin(getINTparam( gpioVarsB[0] ), (boolean) gpioVarsB[2], debounce);

						if (lastState[0] == null)
						{
							lastState[0] = res;
							return(false);
						}
						else
							if(res != (boolean) lastState[0])
							{
								lastState[0] = res;
								return(res == (boolean) gpioVarsB[1]);
							}
							else
								return(false);
					}));
	}
	public static ProgrammElement visualize_EvGPIO_changed(FunctionalityContent content)
	{
		VisualizableProgrammElement evGPIOresp;
		evGPIOresp = new VisualizableProgrammElement(content, "GPIO Changed", "Activates when a GPIO pin changes its connected state.");
		evGPIOresp.addParameter(0, new ValueOrVariable(), "GPIO Pin", "Pin to check." + gpioPinReferenceText);
		evGPIOresp.addParameter(1, new BooleanOrVariable("True"), "When", "If 'true', the event will fire when the pin went from not-connected, to connected,\nso if a button is attached to the pin, this event fires when pressed.\nIf the argument is 'false', it triggers when the button is released.");
		evGPIOresp.addParameter(2, new BooleanOrVariable("False"), "Pull-Up-Res", "If true, the system relies on having a PULL-UP-resistor applied to the GPIO (see the graphic)\nand that means your attached switch/button has to conntect to ground.\nIf false, the PULL-DOWN resistor is assumed and the switch/button should connect to Vcc (3.3v).");
		evGPIOresp.addOptionalParameter(0, new ValueOrVariable(String.valueOf(defaultDebounce)), "Debounce", "Physical switches, buttons and relais tend to 'bounce' when used.\nThat means for a tiny fraction of a second, the signal jumps up and down.\n'Debouncing' is a countermeassure which simply expects the signal to\nstay steadily for a number of milliseconds.\30ms is reccommended and perfect for all kind of human input.\nUse 0 if you have electronic inputs (like from a microcontroler or another PI).");
		return(evGPIOresp);
	}
	*/
	
	
}


