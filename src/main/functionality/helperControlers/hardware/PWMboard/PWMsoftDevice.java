package main.functionality.helperControlers.hardware.PWMboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.wiringpi.SoftPwm;

import dataTypes.exceptions.NonExistingPinException;
import main.functionality.SharedComponents;

@SuppressWarnings("unused")
// Not using all commands - yet.
public class PWMsoftDevice implements PWMdevice {

	int maximum_pwm_value;
	public PWMsoftDevice(int maximum_pwm_value, int frequency, boolean SIMULATED)
	{
		if (SIMULATED)
			return;
		
		this.maximum_pwm_value = maximum_pwm_value;
		
		// TODO: Do something with frequency
	}
	
	List<Integer> usedPins = new ArrayList<>();
	
	
	public void setAllPWM(int on, int off) throws IOException
	{
		for (int p: usedPins)
			SoftPwm.softPwmWrite(p, on);
	}
	
	public void stopAllPWM() throws IOException
	{
		for (int p: usedPins)
			SoftPwm.softPwmStop(p);
		usedPins.clear();
	}
	
	public void setChannelPWM(int pin, int on, int off) throws IOException
	{
		SoftPwm.softPwmWrite(pin, on);
	}

	public void preparePin(int pin) throws NonExistingPinException
	{
		int jpin = SharedComponents.getPinForJava(pin);
		SoftPwm.softPwmCreate(jpin, 0, maximum_pwm_value); // pin, init value, range
		usedPins.add(jpin);
	}
	
	
}
