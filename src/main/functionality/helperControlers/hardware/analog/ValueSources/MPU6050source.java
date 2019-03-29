package main.functionality.helperControlers.hardware.analog.ValueSources;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import staticHelpers.LocationPreparator;
import staticHelpers.OtherHelpers;

/*
 * Base: https://github.com/ControlEverythingCommunity/MPU-6000/blob/master/Java/MPU_6000.java
 */

public class MPU6050source extends ValueSource
{
	I2CDevice device;
	
	public MPU6050source(int addr) throws IOException, UnsupportedBusNumberException
	{
		I2CBus bus = I2CFactory.getInstance(LocationPreparator.i2c_bus_ind() == 1 ? I2CBus.BUS_1 : I2CBus.BUS_0);
		device = bus.getDevice(addr); // Default address: 0x68
		
		// Select gyroscope configuration register
		device.write(0x1B, (byte)0x18);
		// Select accelerometer configuration register
		device.write(0x1C, (byte)0x18);
		// Select power management register1
		device.write(0x6B, (byte)0x01);
		
		OtherHelpers.sleepNonException(500);
	}
	
	@Override
	public double getValue(int typeIfMultitype) throws IOException
	{
		//byte[] data = new byte[6];
		//device.read(0x3B, data, 0, 6);
		
		byte[] data = new byte[2];
		
		if (typeIfMultitype < 3)
			device.read(0x3B, data, typeIfMultitype*2, 2);
		else
			device.read(0x43, data, typeIfMultitype*2, 2);
		
		
		// Convert the data
		int val = ((data[0] & 0xFF) * 256 + (data[1] & 0xFF));
		if(val > 32767)
			val -= 65536;
		
		return(val);
	}
}
