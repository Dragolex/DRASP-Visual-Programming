package main.functionality.helperControlers.hardware.analog.ValueSources;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import staticHelpers.LocationPreparator;
import staticHelpers.OtherHelpers;

public class BMP280source extends ValueSource
{
	I2CDevice device;
	int dig_T1, dig_T2, dig_T3, dig_P1, dig_P2, dig_P3, dig_P4, dig_P5, dig_P6, dig_P7, dig_P8, dig_P9, type;
	
	public BMP280source(int addr) throws UnsupportedBusNumberException, IOException
	{
		// Create I2C bus
		I2CBus bus = I2CFactory.getInstance(LocationPreparator.i2c_bus_ind() == 1 ? I2CBus.BUS_1 : I2CBus.BUS_0);
		// Get I2C device, BMP280 I2C address is 0x76(108)
		device = bus.getDevice(addr); // default is 0x76
		
		// Read 24 bytes of data from address 0x88(136)
		byte[] b1 = new byte[24];
		device.read(0x88, b1, 0, 24);
		
		// Convert the data
		// temp coefficents
		dig_T1 = (b1[0] & 0xFF) + ((b1[1] & 0xFF) * 256);
		dig_T2 = (b1[2] & 0xFF) + ((b1[3] & 0xFF) * 256);
		if(dig_T2 > 32767)
		{
			dig_T2 -= 65536;
		}
		int dig_T3 = (b1[4] & 0xFF) + ((b1[5] & 0xFF) * 256);
		if(dig_T3 > 32767)
		{
			dig_T3 -= 65536;
		}
		
		// pressure coefficents
		dig_P1 = (b1[6] & 0xFF) + ((b1[7] & 0xFF) * 256);
		dig_P2 = (b1[8] & 0xFF) + ((b1[9] & 0xFF) * 256);
		if(dig_P2 > 32767)
		{
			dig_P2 -= 65536;
		}
		int dig_P3 = (b1[10] & 0xFF) + ((b1[11] & 0xFF) * 256);
		if(dig_P3 > 32767)
		{
			dig_P3 -= 65536;
		}
		int dig_P4 = (b1[12] & 0xFF) + ((b1[13] & 0xFF) * 256);
		if(dig_P4 > 32767)
		{
			dig_P4 -= 65536;
		}
		int dig_P5 = (b1[14] & 0xFF) + ((b1[15] & 0xFF) * 256);
		if(dig_P5 > 32767)
		{
			dig_P5 -= 65536;
		}
		int dig_P6 = (b1[16] & 0xFF) + ((b1[17] & 0xFF) * 256);
		if(dig_P6 > 32767)
		{
			dig_P6 -= 65536;
		}
		int dig_P7 = (b1[18] & 0xFF) + ((b1[19] & 0xFF) * 256);
		if(dig_P7 > 32767)
		{
			dig_P7 -= 65536;
		}
		int dig_P8 = (b1[20] & 0xFF) + ((b1[21] & 0xFF) * 256);
		if(dig_P8 > 32767)
		{
			dig_P8 -= 65536;
		}
		int dig_P9 = (b1[22] & 0xFF) + ((b1[23] & 0xFF) * 256);
		if(dig_P9 > 32767)
		{
			dig_P9 -= 65536;
		}
		// Select control measurement register
		// Normal mode, temp and pressure over sampling rate = 1
		device.write(0xF4 , (byte)0x27);
		// Select config register
		// Stand_by time = 1000 ms
		device.write(0xF5 , (byte)0xA0);
		
		
		OtherHelpers.sleepNonException(500);		
	}
	
	
	@Override
	public double getValue(int typeIfMultitype) throws IOException
	{
		// Read 8 bytes of data from address 0xF7(247)
		// pressure msb1, pressure msb, pressure lsb, temp msb1, temp msb, temp lsb, humidity lsb, humidity msb
		byte[] data = new byte[8];
		device.read(0xF7, data, 0, 8);
		
		// Convert pressure and temperature data to 19-bits
		long adc_p = (((long)(data[0] & 0xFF) * 65536) + ((long)(data[1] & 0xFF) * 256) + (long)(data[2] & 0xF0)) / 16;
		long adc_t = (((long)(data[3] & 0xFF) * 65536) + ((long)(data[4] & 0xFF) * 256) + (long)(data[5] & 0xF0)) / 16;
		
		// Temperature offset calculations
		double var1 = (((double)adc_t) / 16384.0 - ((double)dig_T1) / 1024.0) * ((double)dig_T2);
		double var2 = ((((double)adc_t) / 131072.0 - ((double)dig_T1) / 8192.0) *
						(((double)adc_t)/131072.0 - ((double)dig_T1)/8192.0)) * ((double)dig_T3);
		double t_fine = (long)(var1 + var2);
		
		
		
		double pressure = 0;
		
		typeIfMultitype = Math.max(0, Math.min(typeIfMultitype, 3));
		
		switch(typeIfMultitype)
		{
		case 0: // pressure
		case 1: // attitude
			// Pressure offset calculations
			var1 = ((double)t_fine / 2.0) - 64000.0;
			var2 = var1 * var1 * ((double)dig_P6) / 32768.0;
			var2 = var2 + var1 * ((double)dig_P5) * 2.0;
			var2 = (var2 / 4.0) + (((double)dig_P4) * 65536.0);
			var1 = (((double) dig_P3) * var1 * var1 / 524288.0 + ((double) dig_P2) * var1) / 524288.0;
			var1 = (1.0 + var1 / 32768.0) * ((double)dig_P1);
			double p = 1048576.0 - (double)adc_p;
			p = (p - (var2 / 4096.0)) * 6250.0 / var1;
			var1 = ((double) dig_P9) * p * p / 2147483648.0;
			var2 = p * ((double) dig_P8) / 32768.0;
			pressure = (p + (var1 + var2 + ((double)dig_P7)) / 16.0) / 100;		
		}
		
		switch(typeIfMultitype)
		{
		case 0: return(pressure);
		
		case 1: // attitude
	        double p0 = 1037;
	        double dp = pressure / 100d;
	        double power = 1d / 5.255d;
	        double division = dp / p0;
	        double pw = Math.pow(division, power);

	        return(Double.valueOf(44330 * (1 - pw)));
		
		case 2: // celsius temp
			return((var1 + var2) / 5120.0);

		case 3: // attitude
			return(((var1 + var2) / 5120.0) * 1.8 + 32);
		}
		
		return(0.0);
	}
	
	
}
