package main.functionality.helperControlers.hardware.analog.ValueSources;

import java.io.IOException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import staticHelpers.LocationPreparator;


public class INA219source extends ValueSource
{
	INA219instance device;
	
	
	public INA219source(int addr, double resistor, double maxExpectedCurrent, int voltageRange16or32, int gain1or2or4or8, int busAdcRange9to12, int shuntAdcRange9to12) throws UnsupportedBusNumberException, IOException
	{
		//int addr = 0x40;// ADDR_41(0x41), ADDR_44(0x44), ADDR_45(0x45); //20e-3;
        device = new INA219instance(LocationPreparator.i2c_bus_ind(), addr,
        		resistor,
   				maxExpectedCurrent,
   				INA219instance.Brng.getBy(voltageRange16or32),
   				INA219instance.Pga.getBy(gain1or2or4or8),
   				INA219instance.Adc.getBy(busAdcRange9to12),
   				INA219instance.Adc.getBy(shuntAdcRange9to12)
  		);
	}
	
	
	@Override
	public double getValue(int typeIfMultitype) throws IOException
	{
		switch(typeIfMultitype)
		{
		case 0: return(device.getCurrent());
		case 1: return(device.getShuntVoltage());
		case 2: return(device.getBusVoltage());
		case 3: return(device.getPower());
		}
		
		throw new IOException("Only the numbers 0,1,2 and 3 are allowed! Not '"+typeIfMultitype + "'!");
	}
	
	
}
