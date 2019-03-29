package main.functionality.helperControlers.hardware.analog.ValueSources;

import java.io.IOException;
import java.math.BigDecimal;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import staticHelpers.LocationPreparator;

public class BH1750source extends ValueSource
{
	I2CDevice device;
	
	public BH1750source(int addr) throws IOException, UnsupportedBusNumberException
	{
		I2CBus bus = I2CFactory.getInstance(LocationPreparator.i2c_bus_ind() == 1 ? I2CBus.BUS_1 : I2CBus.BUS_0);
		device = bus.getDevice(addr);
		device.write((byte) 0x10);  //11x resolution 120ms
	}
	
	@Override
	public double getValue(int typeIfMultitype) throws IOException
	{
		 byte[] p = new byte[2];

	        int v = device.read(p, 0, 2);

	        if (v != 2) {
	            throw new IllegalStateException("Read Error: r = " + v);
	        }
	        return(new BigDecimal((p[0] << 8) | p[1]).doubleValue());
	}
}
