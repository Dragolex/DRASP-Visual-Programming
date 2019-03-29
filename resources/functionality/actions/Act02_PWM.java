package functionality.actions;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.helperControlers.hardware.PWMboard.PWMcontrol;
import main.functionality.helperControlers.spline.DataSpline;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.DebugMsgHelper;

public class Act02_PWM extends Functionality {

	public static int POSITION = 2;
	public static String NAME = "PWM Output";
	public static String IDENTIFIER = "ActPWMnode";
	public static String DESCRIPTION = "Actions related to a Pulse-Width-Modulated output.\nThose can be used to control server motors and dim LEDs";
	
	

	
	public static ProgramElement create_ElSetupPinPwm()
	{
		Object[] params = new Object[3];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		
		return(cont[0] = new FunctionalityContent( "ElSetupPinPwm",
				params,
				() -> {					
					int p = getINTparam(params[0]);
					
					try {
						if (p == 13)
							PWMctrl.addNativePin(13, getINTparam( params[2] ));
						else
						if (p == 18)
							PWMctrl.addNativePin(18, getINTparam( params[2] ));
						else
							PWMctrl.addSoftwarePin(p, getINTparam( params[2] ));
					}
					catch (Exception e)
					{
						Execution.setError("Error at setup of a PWM pin. Error: " + e.getMessage(), false);
					}
					
					}));
	}
	public static ProgramElement visualize_ElSetupPinPwm(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Setup PWM Pin", "Setup a pin of the rasppberry output as a PWM pin to use in the 'Set PWM' element.\nThis setups either software PWM or one of the hardware PWMs if desired!");
		vis.addParameter(0, new ValueOrVariable("18"), "GPIO Pin", "Note that PWM works best with the special dedicated hardware PWM pins. Those are GPIO 13 and GPIO 18.\nIt is recommended to use those first. All other will not have reliably accurate timing.\nLEDs might flicker and servos jitter.\nAlso ntoe that youc annot use those pins for PWM togetehr with sound!\nTo achieve a larger number of reliable hardware PWM pins, use an external PCA9685 board.\nThat can be used with the corresponding action below." );
		vis.addParameter(1, new ValueOrVariable("0"), "PWM channel", "PWM channel to associate the given GPIO with.\nNote that you may not assign the same number twice!");
		vis.addParameter(2, new ValueOrVariable("50"), "Frequency", "Determines the number of pulses per second.\nIf you want to drive servo motors with PWM, a frequency of 50 or 60 is usually recommended.\nWhen powering LEDs or similar things, that can cause flickering though.\nThus increase the number to several hundreds.\nNote that only the first set frequency is used and it is the same for both hardware PWM pins.");
		
		return(vis);
	}

	
	
	
	// PWM actions (on Adafruit 16channel pwm board via I2C
	
	// PWM channel preparation
	public static ProgramElement create_ElPrepPwm()
	{
		Object[] pwmVars = new Object[4];
		return( new FunctionalityContent( "ElPrepPwm",
				pwmVars,
				() -> {
					
						String i2cAddress = (String) pwmVars[0];
						int frequency = getINTparam( pwmVars[1] );
						int min = getINTparam( pwmVars[2] );
						int max = getINTparam( pwmVars[3] );
						
						if (!i2cAddress.startsWith("0x"))
							i2cAddress = "0x" + i2cAddress;
						int addressNumber = Integer.decode(i2cAddress);
						
						if ((max-min) > 16)
						{
							Execution.setError("The PWM Device channel range is larger than 16! The i2c channel board currently has a maximum of 16 channels.\nUse multiple physical baords with different addresses to acheive more channels.", false);
							return;
						}
						if ((max<=min))
						{
							Execution.setError("The PWM Device channel range is 0 or negative ('Last Channel' <= 'First Channel')!", false);
							return;
						}
						
						PWMctrl.addPCA9685ChannelRange((boolean) pwmVars[0], addressNumber, frequency, min, max);						
					}));
	}
	public static ProgramElement visualize_ElPrepPwm(FunctionalityContent content)
	{
		VisualizableProgramElement elPwmSet;
		elPwmSet = new VisualizableProgramElement(content, "Init PCA9685", "Initialize required values for a 16-channel PWM-Board based on the PCA9685 chip.\nSuch a board has to be connected to the raspberry PI via the I2C bus.\nThe board (of which cheap, identical replicas exist) as well as examples\ncan be googled with 'Raspberry 16 channel PWM'."
				+ "\nWith PWM you can accurately control servos, normal motors and dim other consumers (like LEDs).\nUsing it to send signals to other digital devices is possible as well.\nNOTE: Do not call this Action multiple times for the same board!\nMultiple boards are allowed though due to the serial features of I2C.");
		elPwmSet.addParameter(0, new TextOrVariable("0x40"), "I2C Address", "The I2C address will be mentioned if you do the official Bash-based tutorial for such a board.\nHowever look into the 'Tools' for some helpful features implemented in DRASP.\nAddress is in hexadecimal like it is also displayed by the system.");
		elPwmSet.addParameter(1, new ValueOrVariable("50"), "Frequency", "Determines the number of pulses per second.\nIf you want to drive servo motors with PWM, a frequency of 50 or 60 is usually recommended.\nWhen powering LEDs or similar things, that can cause flickering though.\nThus increase the number to several hundreds. 1000 is the recommended maximum.\nNote that only ONE frequency is allowed for all channels of one board.");
		elPwmSet.addParameter(2, new ValueOrVariable("0"), "First Channel", "First channel associated with this board.\nRecommended to change if using multiple boards.\nIf you have two boards, the firstone can be channel 0-15 and the second one, 16 to 31.\nSmaller numbers (partially using each baord) are allowed as well.");
		elPwmSet.addParameter(3, new ValueOrVariable("15"), "Last Channel", "Last channel associated with this board." );
		
		return(elPwmSet);
	}
	
	
	
	
	

	
	
	
	
	// Simple PWM channel value set
	public static ProgramElement create_ElPwmSet()
	{
		Object[] pwmVars = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElPwmSet",
				pwmVars,
				() -> {
						if (DEBUG) DebugMsgHelper.associatePWMChannelvariable(content[0], 0);
						
						PWMctrl.setChannelPower(getINTparam( pwmVars[0] ), (double) pwmVars[1], content[0].getOptionalArgTrue(0));
					});
		return(content[0]);
	}
	public static ProgramElement visualize_ElPwmSet(FunctionalityContent content)
	{
		VisualizableProgramElement elPwmSet;
		elPwmSet = new VisualizableProgramElement(content, "Set PWM Power", "Set the power value of a PWM channel attached to the system.\nChannels have to be prepared with a call of 'Init PWM Device'.\nBy default, values are set between 0 and 1 where 1 means practically continuous power.\nIf the optional argument 'Absolute Value' is true, the PWM-OFF position can be set directly.\nIn that case, available values are between 0 and " + PWMcontrol.maximum_pwm_value + ".\nWith this function, the PWM-ON position is always 0. To change this as well, use 'Set PWM Exact'.");
		elPwmSet.addParameter(0, new ValueOrVariable(), "Channel", "PWM channel to use.");
		elPwmSet.addParameter(1, new ValueOrVariable(), "Value", "Value between 0 and 1 or 0 and " + PWMcontrol.maximum_pwm_value + " if using absolute values.");
		elPwmSet.addOptionalParameter(0, new BooleanOrVariable("False"), "Absolute Value", "If true, values between 0 and " + PWMcontrol.maximum_pwm_value + " are expected.");
		return(elPwmSet);
	}
	
	
	// Simple PWM channel value set
	public static ProgramElement create_ElPwmSetExt()
	{
		Object[] pwmVars = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElPwmSetExt",
				pwmVars,
				() -> {
						if (DEBUG) DebugMsgHelper.associatePWMChannelvariable(content[0], 0);
					
						PWMctrl.setChannelDirect(getINTparam( pwmVars[0] ), (double) pwmVars[1], (double) pwmVars[2], content[0].getOptionalArgTrue(0));
					}));
	}
	public static ProgramElement visualize_ElPwmSetExt(FunctionalityContent content)
	{
		VisualizableProgramElement elPwmSet;
		elPwmSet = new VisualizableProgramElement(content, "Set PWM Exact", "Set the ON and OFF value of a PWM channel attached to the system.\nChannels have to be prepared with a call of 'Init PWM Device'.\nBy default, values are set between 0 and 1 where 1 means practically continuous power.\\nIf the optional argument 'Absolute Value' is true, the PWM-OFF position can be set directly.\nIn that case available values are between 0 and " + PWMcontrol.maximum_pwm_value + "\nNote that this function only makes sense for cases where the output-pwm signal is interpreted\nby certain microcontrolers.");
		elPwmSet.addParameter(0, new ValueOrVariable(), "Channel", "PWM channel to use.");
		elPwmSet.addParameter(1, new ValueOrVariable(), "ON Value", "On-Value between 0 and 1 or 0 and " + PWMcontrol.maximum_pwm_value + " if using absolute values.");
		elPwmSet.addParameter(2, new ValueOrVariable(), "OFF Value", "Off-Value between 0 and 1 or 0 and " + PWMcontrol.maximum_pwm_value + " if using absolute values.");
		elPwmSet.addOptionalParameter(0, new BooleanOrVariable("False"), "Absolute Values", "If true, values between 0 and " + PWMcontrol.maximum_pwm_value + " are expected.");
		return(elPwmSet);
	}
	
	
	
	
	
	// PWM value set over time towards a given value
	public static ProgramElement create_ElPwmSetPeriodA()
	{
		Object[] pwmVars = new Object[4];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElPwmSetPeriodA",
				pwmVars,
				() -> {
					if (DEBUG) DebugMsgHelper.associatePWMChannelvariable(content[0], 0);
					
					if (content[0].getOptionalArgTrue(1))
						new Thread(() -> PWMctrl.rampPWMpower(getINTparam( pwmVars[0] ), getINTparam( pwmVars[1] ), getINTparam( pwmVars[2] ), -1, getINTparam( pwmVars[3] ), content[0].getOptionalArgTrue(0))).start();
					else
						PWMctrl.rampPWMpower(getINTparam( pwmVars[0] ), getINTparam( pwmVars[1] ), getINTparam( pwmVars[2] ), -1, getINTparam( pwmVars[3] ), content[0].getOptionalArgTrue(0));
					});
		return(content[0]);
	}
	public static ProgramElement visualize_ElPwmSetPeriodA(FunctionalityContent content)
	{
		VisualizableProgramElement elPwmSetPeriodA;
		elPwmSetPeriodA = new VisualizableProgramElement(content, "Ramp PWM Value A", "Change the value of a PWM channel over time\nfrom the current value to the specified one.\nPeriod given in milliseconds without limit.");
		elPwmSetPeriodA.addParameter(0, new ValueOrVariable(), "Channel", "PWM channel to use.");
		elPwmSetPeriodA.addParameter(1, new ValueOrVariable(), "Period (ms)", "Total time of transition.");
		elPwmSetPeriodA.addParameter(2, new ValueOrVariable("30"), "Steps/Sec", "Number of update steps per second. Note that if you use many channels simultanously,\na very high update rate can cause delays.");
		elPwmSetPeriodA.setArgumentDescription(3, new ValueOrVariable(), "Value");
		elPwmSetPeriodA.addOptionalParameter(0, new BooleanOrVariable("False"), "Absolute Value", "If true, values between 0 and " + PWMcontrol.maximum_pwm_value + " are expected.");
		elPwmSetPeriodA.addOptionalParameter(1, new BooleanOrVariable("False"), "No Wait", "If true, the current event will instantly continue execution.\nIf false (default), the event will wait until the transition has finished.");
		return(elPwmSetPeriodA);
	}
	
	
	public static ProgramElement create_ElPwmSetPeriodAB()
	{
		// PWM value set over time between two values
		Object[] pwmVars = new Object[5];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElPwmSetPeriodAB",
				pwmVars,
				() -> {
					if (DEBUG) DebugMsgHelper.associatePWMChannelvariable(content[0], 0);
					
					if (content[0].getOptionalArgTrue(1))
						new Thread(() -> PWMctrl.rampPWMpower(getINTparam( pwmVars[0] ), getINTparam( pwmVars[1] ), getINTparam( pwmVars[2] ), getINTparam( pwmVars[3] ), getINTparam( pwmVars[4] ), content[0].getOptionalArgTrue(0))).start();
					else
						PWMctrl.rampPWMpower(getINTparam( pwmVars[0] ), getINTparam( pwmVars[1] ), getINTparam( pwmVars[2] ), getINTparam( pwmVars[3] ), getINTparam( pwmVars[4] ), content[0].getOptionalArgTrue(0));
					}));
	}
	public static ProgramElement visualize_ElPwmSetPeriodAB(FunctionalityContent content)
	{
		VisualizableProgramElement elPwmSetPeriodAB;
		elPwmSetPeriodAB = new VisualizableProgramElement(content, "Ramp PWM Value AB", "Change the value of a PWM channel over time between two specified values.\nPeriod given in milliseconds without limit.");
		elPwmSetPeriodAB.addParameter(0, new ValueOrVariable(), "Channel", "PWM channel to use.");
		elPwmSetPeriodAB.addParameter(1, new ValueOrVariable(), "Period (ms)", "Total time of transition.");
		elPwmSetPeriodAB.addParameter(2, new ValueOrVariable("30"), "Steps/Sec", "Number of update steps per second. Note that if you use many channels simultanously,\na very high update rate can cause delays.");
		elPwmSetPeriodAB.setArgumentDescription(3, new ValueOrVariable(), "Value Start");
		elPwmSetPeriodAB.setArgumentDescription(4, new ValueOrVariable(), "Value End");
		elPwmSetPeriodAB.addOptionalParameter(0, new BooleanOrVariable("False"), "Absolute Values", "If true, values between 0 and " + PWMcontrol.maximum_pwm_value + " are expected.");
		elPwmSetPeriodAB.addOptionalParameter(1, new BooleanOrVariable("False"), "No Wait", "If true, the current event will instantly continue execution.\nIf false (default), the event will wait until the transition has finished.");
		return(elPwmSetPeriodAB);
	}
	
	
	// PWM channel set with spline
	public static ProgramElement create_ElPwmSetBySpline()
	{
		Object[] pwmVars = new Object[5];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElPwmSetBySpline",
				pwmVars,
				() -> {
					if (DEBUG) DebugMsgHelper.associatePWMChannelvariable(content[0], 0);
					
					if (content[0].getOptionalArgTrue(1))
						new Thread(() -> PWMctrl.setChannelPowerBySpline(getINTparam( pwmVars[0] ), (DataSpline) pwmVars[1], (double) pwmVars[2], (double) pwmVars[3], (double) pwmVars[4], content[0].getOptionalArgTrue(0))).start();
					else
						PWMctrl.setChannelPowerBySpline(getINTparam( pwmVars[0] ), (DataSpline) pwmVars[1], (double) pwmVars[2], (double) pwmVars[3], (double) pwmVars[4], content[0].getOptionalArgTrue(0));
						
					}));
	}
	public static ProgramElement visualize_ElPwmSetBySpline(FunctionalityContent content)
	{
		VisualizableProgramElement elPwmSet;
		elPwmSet = new VisualizableProgramElement(content, "Set PWM by Spline", "Set the power value of a PWM channel attached to the system, based on a spline.\nThe value will be 'animated' by the contents of the spline by reading\nthe next value along the x axis in every step.\nUsing the factor parameters you can alter the result.\nHint: You can use variables for factors!");
		elPwmSet.addParameter(0, new ValueOrVariable(), "Channel", "PWM channel to use.");
		elPwmSet.addParameter(1, new VariableOnly(), "Spline Identifier", "Spline to set the PWM valeus from.");
		elPwmSet.addParameter(2, new ValueOrVariable("1"), "Time Factor", "Factor applied to the X-values of the Spline.");
		elPwmSet.addParameter(3, new ValueOrVariable("1"), "Value Factor", "Factor applied to the Y-values of the Spline.");
		elPwmSet.addParameter(4, new ValueOrVariable("30"), "Steps/Sec", "Number of update steps per second. Note that if you use many channels simultanously,\na very high update rate can cause delays.");
		elPwmSet.addOptionalParameter(0, new BooleanOrVariable("False"), "Absolute Value", "If true, values between 0 and " + PWMcontrol.maximum_pwm_value + " are expected.");
		elPwmSet.addOptionalParameter(1, new BooleanOrVariable("False"), "No Wait", "If true, the current event will instantly continue execution.\nIf false (default), the event will wait until the transition has finished.");
		return(elPwmSet);
	}
	
	
	
	
}
