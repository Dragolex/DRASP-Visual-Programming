package staticHelpers;

import java.util.HashMap;
import java.util.Map;

import dataTypes.FunctionalityContent;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.helperControlers.hardware.analog.RotaryDevice;
import settings.GlobalSettings;

public class DebugMsgHelper {

	public final static String PINSstr = "||GPIO ";
	public final static String PINSstrWithVar = "||GPIO |V#|";
	public final static String PINstrIN = "IN";
	public final static String PINstrOUT = "OUT";
	
	static private Map<Integer, String> pinToVariableNameAssociation = new HashMap<>();
	static private Map<Integer, String> PWMpinToVariableNameAssociation = new HashMap<>();

	
	public static void setCheckPinDebug(int pinInd, Runnable task)
	{
		Execution.addFunctionalDebugVariable(getPinName(pinInd), PINstrIN, String.valueOf("False"), task);
	}
	public static void setPinStateDebug(int pinInd, boolean out, boolean state)
	{
		Execution.updateDebugVariable(getPinName(pinInd), out ? PINstrOUT : PINstrIN, state ? "True" : "False", false);
	}
	
	private static String getPinName(int pinInd)
	{
		String asoc = pinToVariableNameAssociation.get(pinInd);
		
		String name = String.valueOf(pinInd);
		if (asoc != null)
			name = PINSstrWithVar + asoc.substring(1) + " (GPIO " + pinInd +")";
		else
			name = PINSstr + name;	
		
		return(name);
	}
	
	
	
	public static void associateGPIOvariable(FunctionalityContent cont, int paramInd)
	{
		if(!Execution.isSimulated()) return;
		
		if (!cont.getArgumentIsConstant(paramInd)) // if the pin argument is not constant -> a variable is used
		{
			Variable v = cont.getVisualization().getArgumentsData()[paramInd].getVariableIfPossible();
			if (v != null)
				pinToVariableNameAssociation.put((int) (double) cont.getArgumentValue(0), v.toString());
		}
	}
	

	public static void rotaryDevice(RotaryDevice rotaryDevice)
	{
    	Execution.addFunctionalDebugVariable(GlobalSettings.debugButtonStartSymbol + "->: " + rotaryDevice.toString(), "Rotary Input", "Rotate Forth", () -> {
    		rotaryDevice.setIncremented();
			Execution.updateDebugVariable(GlobalSettings.debugButtonStartSymbol + "->: " + rotaryDevice.toString(), "Rotary Input", "Rotate Forth", false);
    	});
    	Execution.addFunctionalDebugVariable(GlobalSettings.debugButtonStartSymbol + "<-: " + rotaryDevice.toString(), "Rotary Input", "Rotate Back", () -> {
    		rotaryDevice.setDecremented();
			Execution.updateDebugVariable(GlobalSettings.debugButtonStartSymbol + "<-: " + rotaryDevice.toString(), "Rotary Input", "Rotate Back", false);
    	});
	}

	
	
	
	// PWM
	
	static private final String pwmChannelStr = "PWM Ch ";

	public static void setPwm(int channel, double value)
	{
		value = ((double)Math.round((value * 100000)))/100000;
		Execution.updateDebugVariable(getPWMName(channel), "Set", String.valueOf(value) , false);
	}
	public static void setPwmExt(int channel, double valOn, double valOff)
	{
		valOn = ((double)Math.round((valOn * 100000)))/100000;
		valOff = ((double)Math.round((valOff * 100000)))/100000;
		Execution.updateDebugVariable(getPWMName(channel), "Set", String.valueOf(valOn) +" - " + String.valueOf(valOff) , false);
	}


	public static void setPwmSpecial(int channel, String type, double value, boolean ignorable)
	{
		value = ((double)Math.round((value * 100000)))/100000;
		Execution.updateDebugVariable(getPWMName(channel), type, String.valueOf(value) , ignorable);
	}
	
	private static String getPWMName(int channel)
	{
		String asoc = PWMpinToVariableNameAssociation.get(channel);
		
		String name = String.valueOf(channel);
		if (asoc != null)
			name = asoc.substring(1) + " (" + pwmChannelStr +" " + name + " )";
		else
			name = pwmChannelStr + name;	
		
		return(name);
	}


	public static void associatePWMChannelvariable(FunctionalityContent cont, int paramInd)
	{
		if(!Execution.isSimulated()) return;
		
		if (!cont.getArgumentIsConstant(paramInd)) // if the pin argument is not constant -> a variable is used
		{
			Variable v = cont.getVisualization().getArgumentsData()[paramInd].getVariableIfPossible();
			if (v != null)
			{
				PWMpinToVariableNameAssociation.put((int) (double) cont.getArgumentValue(0), v.toString());
			}
		}
	}

	
}

