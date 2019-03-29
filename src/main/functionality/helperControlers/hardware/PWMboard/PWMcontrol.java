package main.functionality.helperControlers.hardware.PWMboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.wiringpi.Gpio;

import dataTypes.exceptions.NonExistingPinException;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import execution.handlers.ToolsDatabase;
import main.functionality.SharedComponents;
import main.functionality.helperControlers.HelperParent;
import main.functionality.helperControlers.spline.DataSpline;
import staticHelpers.DebugMsgHelper;

public class PWMcontrol extends HelperParent
{
	static public final int minimum_pwm_value = 0;
	static public final int maximum_pwm_value = 4095;
	
	
	List<PWMdevice> devices = new ArrayList<>();
	
	PWMdevice[] channelToDeviceMap = null;
	short[] channelOffsetMap = null;
	short[] localChannelValue = null;	
	
	float absoluteFactor = (maximum_pwm_value-minimum_pwm_value);// / 100;
	
	
	@Override
	protected void start() {} // Not required here because the addChannelRange creates the corresponding objects
	
	
	
	
	

	private PWMpi4jDevice pi4jPWMdevice;
	
	public void addNativePin(int pin, int frequency) throws NonExistingPinException, UnsupportedOperationException, IOException, InterruptedException
	{
		startIfNeeded();
		
		if (pi4jPWMdevice == null)
			pi4jPWMdevice = new PWMpi4jDevice(maximum_pwm_value, frequency, SIMULATED);
		
		
		if (!SIMULATED)
			pi4jPWMdevice.preparePin(pin);
		
		initChannelData(pin, pin, 0, pi4jPWMdevice);
	}

	
	
	static boolean initializeWiringPiSetup = true;
	
	private PWMsoftDevice softPWMdevice;
	
	public void addSoftwarePin(int pin, int frequency) throws NonExistingPinException {
		
		startIfNeeded();
		
		if (initializeWiringPiSetup)
			Gpio.wiringPiSetup();
		initializeWiringPiSetup = false;
		
		if (softPWMdevice == null)
			softPWMdevice = new PWMsoftDevice(maximum_pwm_value, frequency, SIMULATED);
				
		GPIOctrl.startIfNeeded();
		
		if (!SIMULATED)
			softPWMdevice.preparePin(pin);
		
		int jpin = SharedComponents.getPinForJava(pin);
		initChannelData(pin, pin, pin-jpin, pi4jPWMdevice);
	}

	
	
	public void addPCA9685ChannelRange(boolean useBus1, int i2cAddress, int frequency, int minChannel, int maxChannel)// throws IOException, UnsupportedBusNumberException
	{
		startIfNeeded();
		
		maxChannel += 1; // increase maxChannel by one because that channel should be included!
		
		PCA9685device device = null;
		
		try {
			device = new PCA9685device(useBus1 ? 1 : 0, i2cAddress);
		} catch (IOException | UnsupportedBusNumberException e)
		{
			if (!ToolsDatabase.getI2Cconfig())
			{
				InfoErrorHandler.printEnvironmentInfoMessage("Accessing I2C failed. Attempting to activate using raspi-config.");
				ToolsDatabase.setI2Cconfig(true);
			}
			
			try {
				device = new PCA9685device(useBus1 ? 1 : 0, i2cAddress);
				
				InfoErrorHandler.printEnvironmentInfoMessage("Activating I2C successful! You might need to restart the program though.");
			} catch (IOException | UnsupportedBusNumberException e2)
			{
				Execution.setError("I2C device address not valid or I2C could not be activated on the system.", false);
				return;
			}
		}
		
		try {
			device.setPWMFreqency(frequency);
		} catch (IOException e)
		{
			Execution.setError("The given I2C frequency (" + frequency + ") is not supported!", false);
			return;
		}
		
		initChannelData(minChannel, maxChannel, minChannel, device);
	}
	
	
	private void initChannelData(int minChannel, int maxChannel, int offset, PWMdevice device)
	{
		startIfNeeded();
		
		if (channelToDeviceMap == null)
		{
			// Prepare the array of possible channels
			channelToDeviceMap = new PWMdevice[maxChannel+1];
			channelOffsetMap = new short[maxChannel+1];
			localChannelValue = new short[maxChannel+1];
			for (int i = minChannel; i <= maxChannel; i++)
			{
				channelToDeviceMap[i] = device;
				channelOffsetMap[i] = (short) offset;
				localChannelValue[i] = 0;
			}
		}	
		else
		{
			if (maxChannel >= channelToDeviceMap.length)
			{
				channelToDeviceMap = java.util.Arrays.copyOf(channelToDeviceMap, maxChannel+1);
				channelOffsetMap = java.util.Arrays.copyOf(channelOffsetMap, maxChannel+1);
				localChannelValue = java.util.Arrays.copyOf(localChannelValue, maxChannel+1);
			}
			for (int i = minChannel; i <= maxChannel; i++)
			{
				if (channelToDeviceMap[i] != null)
				{
					Execution.setError("Channel range already occupated by another device!\nUse a free channel range please.", false);
					return;
				}
				channelToDeviceMap[i] = device;
				channelOffsetMap[i] = (short) offset;
				localChannelValue[i] = 0;
			}
		}
		
		if (!devices.contains(device))
			devices.add(device);
	}
	
	
	public void setChannelPower(int channel, double value, boolean absolute)// throws IOException
	{	
		startIfNeeded();
		
		if (DEBUG) DebugMsgHelper.setPwm(channel, value);
		
		if (channelToDeviceMap[channel] == null)
		{
			Execution.setError("The PWM channel is not available!\nInititalization of a device with a range of channels covering the desired number is required.", false);
			return;
		}
		
		if (SIMULATED)
		{
			if (absolute)
				localChannelValue[channel] = (short) value;
			else
				localChannelValue[channel] = (short) (value*absoluteFactor);
			return;
		}
		else
		try {
			if (absolute)
				channelToDeviceMap[channel].setChannelPWM(channel-channelOffsetMap[channel], 0, localChannelValue[channel] = (short) value);
			else
				channelToDeviceMap[channel].setChannelPWM(channel-channelOffsetMap[channel], 0, localChannelValue[channel] = (short) (absoluteFactor*value));

		} catch (IOException e)
		{
			Execution.setError("Setting a PWM channel value failed with the following error: " + e.getMessage(), false);
		}
	}
	
	public void setChannelDirect(int channel, double onValue, double offValue, boolean absolute)// throws IOException
	{
		startIfNeeded();
		
		if(DEBUG) DebugMsgHelper.setPwmExt(channel, onValue, offValue);
		
		if (channelToDeviceMap[channel] == null)
			Execution.setError("The PWM channel is not available!\nInititalization of a device with a range of channels covering the desired number is required.", false);
		else
		{
			if (SIMULATED)
				return;
			
			try {
				if (absolute)
					channelToDeviceMap[channel].setChannelPWM(channel-channelOffsetMap[channel], (int) onValue, (int) offValue);
				else
					channelToDeviceMap[channel].setChannelPWM(channel-channelOffsetMap[channel], (int) (absoluteFactor*onValue), (int) (absoluteFactor*offValue));
			} catch (IOException e)
			{
				Execution.setError("Setting a PWM channel value failed with the following error: " + e.getMessage(), false);
			}
		}
	}

	
	public void rampPWMpower(int channel, int periodMS, int stepsPerS, int valueA, int valueB, boolean absolute)
	{
		startIfNeeded();
		
		int steps = (int) (((float)periodMS/1000) * stepsPerS);
		float increment = 1/(float)steps;
		
		int timeStep = (int) (1000/(float)stepsPerS);
		
		
		if (valueA < 0)
			valueA = getCurrentPWMpower(channel);
		else
			if (!absolute)
				valueA *= absoluteFactor;

		if (!absolute)
			valueB *= absoluteFactor;
		
		int dif = valueB-valueA;
		
		PWMdevice device = channelToDeviceMap[channel];
		int ch = channel-channelOffsetMap[channel];
		
		String rampStr = "";
		if (DEBUG)
			rampStr = "Ramp: " + valueA + "->" + valueB;
		
		try
		{
			if (SIMULATED)
			{
				for(float pos = 0; pos < 1; pos+=increment)
				//synchronized(this)
				{
					if (DEBUG)
					{
						localChannelValue[channel] = (short) ((short) (valueA + pos*dif));	
						DebugMsgHelper.setPwmSpecial(channel, rampStr, ((double) localChannelValue[channel] / ( absolute ? 1 : absoluteFactor )), true);
					}

					Thread.sleep(timeStep);
				}
				
				if (DEBUG)
				{
					localChannelValue[channel] = (short) ((short) valueB);						
					DebugMsgHelper.setPwmSpecial(channel, rampStr, ((double) localChannelValue[channel] / ( absolute ? 1 : absoluteFactor )), true);
				}

			}
			else
			if (DEBUG)
			{
				for(float pos = 0; pos < 1; pos+=increment)
				{
					device.setChannelPWM(ch, 0, localChannelValue[channel] = (short) (valueA + pos*dif));
					
					DebugMsgHelper.setPwmSpecial(channel, rampStr, (valueA + pos*dif) / ( absolute ? 1 : absoluteFactor ), true);
					
					Thread.sleep(timeStep);
				}
				device.setChannelPWM(ch, 0, localChannelValue[channel] = (short) valueB);
				
				DebugMsgHelper.setPwmSpecial(channel, rampStr, (valueB) / ( absolute ? 1 : absoluteFactor ), false);
				
			}
			else
			{
				for(float pos = 0; pos < 1; pos+=increment)
				{
					device.setChannelPWM(ch, 0, localChannelValue[channel] = (short) (valueA + pos*dif));
					
					Thread.sleep(timeStep);
				}
				device.setChannelPWM(ch, 0, localChannelValue[channel] = (short) valueB);
			}
		} catch (IOException | InterruptedException e)
		{
			Execution.setError("Setting a PWM channel value failed with the following error: " + e.getMessage(), false);
			return;
		}
		
		
	}

	public void setChannelPowerBySpline(int channel, DataSpline spline, double xfactor, double yfactor, double stepsPerS, boolean absolute)
	{
		startIfNeeded();
		
		int timeStep = (int) (1000/(float)stepsPerS);
		float[] splineData = spline.getData();
		
		int size = splineData.length;
		
		
		PWMdevice device = channelToDeviceMap[channel];
		int ch = channel-channelOffsetMap[channel];
		
		
		String splineStr = "";
		if (DEBUG)
			splineStr = "By Spline: " + spline.getIdentifierVariableName();
		
		try
		{
			if (SIMULATED)
			{
				
				for(float pos = 0; pos < size; pos+=xfactor)
				//synchronized(this)
				{
					if (DEBUG)
					{
						localChannelValue[channel] = (short) ((short) (splineData[(int) pos]*yfactor));
						DebugMsgHelper.setPwmSpecial(channel, splineStr,  ((double) localChannelValue[channel] / ( absolute ? 1 : absoluteFactor )), true);
					}
					
					Thread.sleep(timeStep);
				}
			}
			else
			if (DEBUG)
			{
				for(float pos = 0; pos < size; pos+=xfactor)
				//synchronized(this)
				{
					device.setChannelPWM(ch, 0, (localChannelValue[channel] = (short) ((short) (splineData[(int) pos]*yfactor)) ));
					
					DebugMsgHelper.setPwmSpecial(channel, splineStr,  localChannelValue[channel] / ( absolute ? 1 : absoluteFactor ), true);
					
					Thread.sleep(timeStep);
				}
			}
			else
				//if (absolute)
					for(float pos = 0; pos < size; pos+=xfactor)
					//synchronized(this)
					{
						device.setChannelPWM(ch, 0, (localChannelValue[channel] = (short) (splineData[(int) pos]*yfactor)));
						Thread.sleep(timeStep);
					}
				/*
				else						
					for(float pos = 0; pos < size; pos+=xfactor)
					//synchronized(this)
					{
						device.setChannelPWM(ch, 0, (localChannelValue[channel] = (short) (splineData[(int) pos]*yfactor / absoluteFactor)));
						Thread.sleep(timeStep);
					}*/
		
		
		} catch (IOException | InterruptedException e)
		{
			Execution.setError("Setting a PWM channel value failed with the following error: " + e.getMessage(), false);
			return;
		}
	}
	
	
	public int getCurrentPWMpower(int channel)
	{
		return(localChannelValue[channel]);
	}
	
	
	protected void reset()
	{
		channelToDeviceMap = null;
		channelOffsetMap = null;
		localChannelValue = null;
		
		System.out.println("RESETTED PWM!!");
		
		quit();
	}
	
	
	protected void quit()
	{
		try
		{
			if (!SIMULATED)
			{
				for(PWMdevice device: devices)
					device.setAllPWM(0, 0);
				
				softPWMdevice.stopAllPWM();
			}
		}
		catch (IOException e)
		{
			InfoErrorHandler.callEnvironmentError("Closing a PWM device failed.");
		}		
		
		pi4jPWMdevice = null;
		softPWMdevice = null;
	}

	

	
	
	
	/*
	private void addChannel(int channelIndex, int minValue, int maxValue, int startValue)
	{
		possibleChannels[channelIndex] = new PWMchannel(channelToDeviceMap[channelIndex], channelIndex, minValue, maxValue, startValue);
	}
	
	
	public int setChannelValue(Object channel, Object value)
	{
		Boolean res = null;
		
		// The channel is given as a string (a keyword like "all")
		if (channel instanceof String)
		{
			switch((String)channel)
			{
			case "all":	
				for(int i = 0; i < possibleChannels.length; i++)
					if (possibleChannels[i] != null) // if the channel is in use
						res = possibleChannels[i].setValue(value);
				break;
				
			}
		
		}
		

		// The channel is given as an integer index
		if (channel instanceof Integer)
		{
			if (possibleChannels[(Integer)channel] != null)
				res = possibleChannels[(Integer)channel].setValue(value);
		}

		
		if (res == null)
			return(0); // Error meaning that no valid channel has been found
		
		if (!res)
			return(-1); // Error meaning that a valid channel has been found but no valid value!
		
		return(1);
		
	}
	*/
	
	
	
	
	


}
