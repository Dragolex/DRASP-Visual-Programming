package main.functionality.helperControlers.hardware.PWMboard;

import java.io.IOException;

import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.system.SystemInfo;
import com.pi4j.wiringpi.Gpio;

import dataTypes.exceptions.NonExistingPinException;
import main.functionality.SharedComponents;


// Based on: https://github.com/Pi4J/pi4j/blob/master/pi4j-example/src/main/java/PwmExample.java

public class PWMpi4jDevice implements PWMdevice {

	//GpioPinPwmOutput[] possiblePins = new GpioPinPwmOutput[40]; // using an array instead of a map to make accesses faster!
	
	GpioPinPwmOutput pwm13 = null;
	GpioPinPwmOutput pwm18 = null;
	
	public PWMpi4jDevice(int maximum_pwm_value, int frequency, boolean SIMULATED) throws UnsupportedOperationException, IOException, InterruptedException
	{
		if (SIMULATED)
			return;
		
		Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
        Gpio.pwmSetRange(maximum_pwm_value);
        Gpio.pwmSetClock((int) SystemInfo.getClockFrequencyCore() / frequency); // TODO: Verify this! Default value: 500
	}
	
	
	public void setAllPWM(int on, int off) throws IOException
	{
		if (pwm13 != null)
			pwm13.setPwm(on);
		if (pwm18 != null)
			pwm18.setPwm(on);
	}
	
	public void setChannelPWM(int channelIndex, int on, int off) throws IOException
	{
		if (channelIndex == 13)
			pwm13.setPwm(on);
		if (channelIndex == 18)
			pwm18.setPwm(on);
	}

	public void preparePin(int pin) throws NonExistingPinException
	{
		GpioPinPwmOutput pwm = SharedComponents.GPIOctrl.gpio.provisionPwmOutputPin(SharedComponents.GPIOctrl.getPin(pin));	       
		
		if (pin == 13) // only 13 and 18 are available. This is checked in the action element code
			pwm13 = pwm;		

		if (pin == 18)
			pwm18 = pwm;
	}
	

	
	
}
