package main.functionality.helperControlers.hardware;

import java.io.IOException;
import java.util.List;

import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.helperControlers.HelperParent;

public class OneWireControl extends HelperParent
{
	W1Master master;
	List<W1Device> w1Devices;
	
	@Override
	protected void start()
	{		
		master = new W1Master();

		readAllDevices();
	}
	
	
	public void printAllDevices()
	{
		startIfNeeded();
		
		for (W1Device device : w1Devices)
		{
			Execution.print("NAME: " + device.getName());
			Execution.print("FAMILY ID: " + device.getFamilyId());
			Execution.print("ID: " + device.getId());
			
			try
			{
				Execution.print("VALUE: " + device.getValue());
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
			/*
		    //this line is enough if you want to read the temperature
		    System.out.println("Temperature: " + ((TemperatureSensor) device).getTemperature());
		    //returns the temperature as double rounded to one decimal place after the point

		    try
		    {
		        System.out.println("1-Wire ID: " + device.getId() +  " value: " + device.getValue());
		        //returns the ID of the Sensor and the  full text of the virtual file
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		    */
		    
		}
	}
	
	
	// 0x28 for DS18B20
	public String getDeviceValueByFamily(int deviceFamily) throws IOException
	{
		startIfNeeded();
		
		for (W1Device device : w1Devices)
		{
			if (deviceFamily == device.getFamilyId());
				return(device.getValue());		    
		}
		
		return("");
	}
	
	public String getDeviceValueById(String deviceId) throws IOException
	{
		startIfNeeded();
		
		for (W1Device device : w1Devices)
		{
			if (deviceId.equals(device.getId()))
				return(device.getValue());
		}
		
		return("");
	}
	
	
	
	
	public void interpretAndSet(String interpretationVariant, String valueText, Object variable)
	{
		switch(interpretationVariant)
		{
		case "DS18B20":
			initVariableAndSet(variable, Variable.doubleType, Double.valueOf(valueText));
		break;
		
		default:
			initVariableAndSet(variable, Variable.textType, valueText);
		break;
		
		}
		
		// TODO
		
	}
	
	
	
	private void readAllDevices()
	{
		w1Devices = master.getDevices(); // TmpDS18B20DeviceType.FAMILY_CODE
	}
	
	
	
	protected void reset()
	{
		readAllDevices();
	}

	protected void quit()
	{
		
	}
	
	
	
	
	
}
