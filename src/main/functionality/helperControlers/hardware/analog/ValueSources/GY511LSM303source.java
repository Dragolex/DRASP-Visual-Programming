package main.functionality.helperControlers.hardware.analog.ValueSources;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import staticHelpers.LocationPreparator;
import staticHelpers.OtherHelpers;

// Base: https://github.com/ControlEverythingCommunity/LSM303DLHC/tree/master/Java

public class GY511LSM303source extends ValueSource
{
	I2CDevice device_accl, device_mag;

	public GY511LSM303source(int addr) throws UnsupportedBusNumberException, IOException
	{
		// Create I2C Bus
		I2CBus bus = I2CFactory.getInstance(LocationPreparator.i2c_bus_ind() == 1 ? I2CBus.BUS_1 : I2CBus.BUS_0);
		
		// Get I2C device, LSM303DLHC I2C address is 0x19(25)
		device_accl = bus.getDevice(0x19);

		// Select control register1
		// X, Y and Z-axis enable, power on mode, o/p data rate 10 Hz
		device_accl.write(0x20 ,(byte)0x27);
		// Select control register4
		// Full scale +/- 2g, continuous update
		device_accl.write(0x23 ,(byte)0x00);
		
		
		OtherHelpers.sleepNonException(500);

		
		

		// Get I2C device, LSM303DLHC MAGNETO I2C address is 0x1E(30)
		I2CDevice device_mag = bus.getDevice(0x1E);

		// Select MR register
		// Continuous conversion
		device_mag.write(0x02, (byte)0x00);
		// Select CRA register
		// Data output rate = 15Hz
		device_mag.write(0x00, (byte)0x10);
		// Select CRB register
		// Set gain = +/- 1.3g
		device_mag.write(0x01, (byte)0x20);
		
		
		OtherHelpers.sleepNonException(500);
	}
	
	
	@Override
	public double getValue(int typeIfMultitype) throws IOException
	{		
		typeIfMultitype = Math.max(0, Math.min(typeIfMultitype, 6));
		
		
		byte byteA = 0, byteB = 0;
		switch(typeIfMultitype)
		{
		case 0:
			byteA = (byte)device_accl.read(0x28);
			byteB = (byte)device_accl.read(0x29);
			break;
		case 1:
			byteA = (byte)device_accl.read(0x2A);
			byteB = (byte)device_accl.read(0x2B);
			break;
		case 2:
			byteA = (byte)device_accl.read(0x2C);
			byteB = (byte)device_accl.read(0x2D);
			break;
		case 3:
			byteA = (byte)device_accl.read(0x03);
			byteB = (byte)device_accl.read(0x04);
			break;
		case 4:
			byteA = (byte)device_accl.read(0x05);
			byteB = (byte)device_accl.read(0x06);
			break;
		case 5:
			byteA = (byte)device_accl.read(0x07);
			byteB = (byte)device_accl.read(0x08);
			break;
		}
		
		int value = ((byteB & 0xFF) * 256 + (byteA & 0xFF)) ;
		if(value > 32767)
			value -= 65536;
		
		return(value);
		
		
		// NOTE: Perhaps swap type 4 and 5 because it's swapped in the soruce below! )
		
		

		/*
		// Read 6 bytes of data
		// xAccl lsb, xAccl msb, yAccl lsb, yAccl msb, zAccl lsb, zAccl msb
		byte[] data = new byte[6];
		data[0] = (byte)device_accl.read(0x28);
		data[1] = (byte)device_accl.read(0x29);
		data[2] = (byte)device_accl.read(0x2A);
		data[3] = (byte)device_accl.read(0x2B);
		data[4] = (byte)device_accl.read(0x2C);
		data[5] = (byte)device_accl.read(0x2D);

		// Convert the data
		int xAccl = ((data[1] & 0xFF) * 256 + (data[0] & 0xFF)) ;
		if(xAccl > 32767)
		{
			xAccl -= 65536;
		}	

		int yAccl = ((data[3] & 0xFF) * 256 + (data[2] & 0xFF)) ;
		if(yAccl > 32767)
		{
			yAccl -= 65536;
		}
		int zAccl = ((data[5] & 0xFF) * 256 + (data[4] & 0xFF)) ;
		if(zAccl > 32767)
		{
			zAccl -= 65536;
		}
		*/
		
		
		
		/*
		// Read 6 bytes of data
		// xMag msb, xMag lsb, zMag msb, zMag lsb, yMag msb, yMag lsb
		byte[] data2 = new byte[6];
		data2[0] = (byte)device_mag.read(0x03);
		data2[1] = (byte)device_mag.read(0x04);
		data2[2] = (byte)device_mag.read(0x05);
		data2[3] = (byte)device_mag.read(0x06);
		data2[4] = (byte)device_mag.read(0x07);
		data2[5] = (byte)device_mag.read(0x08);

		// Convert the data
		int xMag = ((data2[0] & 0xFF) * 256 + (data2[1] & 0xFF));
		if(xMag > 32767)
		{
			xMag -= 65536;
		}	

		int yMag = ((data2[4] & 0xFF) * 256 + (data2[5] & 0xFF)) ;
		if(yMag > 32767)
		{
			yMag -= 65536;
		}

		int zMag = ((data2[2] & 0xFF) * 256 + (data2[3] & 0xFF)) ;
		if(zMag > 32767)
		{
			zMag -= 65536;
		}*/
	}
	
	
}
