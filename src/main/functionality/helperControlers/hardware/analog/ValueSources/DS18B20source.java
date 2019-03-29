package main.functionality.helperControlers.hardware.analog.ValueSources;

import java.io.IOException;

import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

import execution.Execution;

public class DS18B20source extends ValueSource
{
	W1Device device;
	
	String notThis;
	int repeatDelay, maxRepeats;
	
	public DS18B20source(int oneWireDeviceID, String notThis, int repeatDelay, int maxRepeats)
	{
		W1Master master = new W1Master();
		
		for (W1Device dev : master.getDevices())
		{
			if (oneWireDeviceID == dev.getFamilyId());
				device = dev;
		}
		
		
		
	}
	
	@Override
	public double getValue(int typeIfMultitype) throws IOException
	{
		String valStr;
		
		valStr = device.getValue();
		
		int count = maxRepeats;
		
		if (!valStr.isEmpty())
		{
			while(notThis.equals(valStr) && count > 0)
			{
				Execution.checkedSleep(repeatDelay);
				valStr = device.getValue();
				count--;
			}
			
			if(notThis.equals(valStr))
				return(0);
		}
		
		return(Double.valueOf(valStr));
	}
}
