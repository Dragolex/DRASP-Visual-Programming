package main.functionality.helperControlers.hardware.analog.ValueSources;


import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import main.functionality.helperControlers.hardware.analog.ProtocolHelpers;

/**
 * Inspiration: https://github.com/gsteckman/rpi-ina219
 */
public class INA219instance {
    private static final double SHUNT_VOLTAGE_LSB = 10e-6;
    private static final double BUS_VOLTAGE_LSB = 4e-3;
    private static final int POWER_LSB_SCALE = 20;
    private double rShunt;
    private double currentLSB;
    private int cal;
    
    private I2CDevice device;

    
    public INA219instance(int busInd, int addr, final double shuntResistance, final double maxExpectedCurrent,
            final Brng busVoltageRange, final Pga pga, final Adc badc, final Adc sadc)
            throws IOException, UnsupportedBusNumberException
    {
    	device = I2CFactory.getInstance(busInd == 1 ? I2CBus.BUS_1 : I2CBus.BUS_0).getDevice(addr);
    	
        rShunt = shuntResistance;
        currentLSB = (maxExpectedCurrent / 32768);
        cal = (int) (((0.04096 * 32768) / (maxExpectedCurrent * rShunt)));

        configure(busVoltageRange, pga, badc, sadc);
        ProtocolHelpers.writeRegister(device, 5, cal); // CALIBRATION
    }

    public double getShuntVoltage() throws IOException
    {
        int rval = ProtocolHelpers.readSignedRegister(device, 1); // SHUNT_VOLTAGE
        return rval * SHUNT_VOLTAGE_LSB;
    }

    public double getBusVoltage() throws IOException
    {
        int rval = ProtocolHelpers.readRegister(device, 2); // BUS_VOLTAGE
        return (rval >> 3) * BUS_VOLTAGE_LSB;
    }


    public double getPower() throws IOException
    {
        int rval = ProtocolHelpers.readRegister(device, 3); // POWER
        return rval * POWER_LSB_SCALE * currentLSB;
    }


    public double getCurrent() throws IOException
    {
        int rval = ProtocolHelpers.readSignedRegister(device, 4); // CURRENT
        return rval * currentLSB;
    }

    
    private void configure(final Brng busVoltageRange, final Pga pga, final Adc badc,
            final Adc sadc) throws IOException
    {
        int regValue = (busVoltageRange.getValue() << 13) | (pga.getValue() << 11) | (badc.getValue() << 7)
                | (sadc.getValue() << 3) | 0x7;

        ProtocolHelpers.writeRegister(device, 0, regValue); // CONFIGURE
    }
    
    
    
    
    
    /**
     * Enum for the Bus Voltage Range setting (BRNG)
     */
    public enum Brng {
        V16(0), // 16 Volts
        V32(1); // 32 Volts

        private int value;

        Brng(int val) {
            value = val;
        }
        
        public static Brng getBy(int v)
        {
        	return(v == 16 ? V16 : V32);
        }
        

        int getValue() {
            return value;
        }
    }

    /**
     * Enum for the PGA gain and range setting options.
     */
    public enum Pga {
        GAIN_1(0), // 1
        GAIN_2(1), // /2
        GAIN_4(2), // /4
        GAIN_8(3); // /8

        private int value;

        Pga(int val) {
            value = val;
        }
        
        public static Pga getBy(int v)
        {
        	switch(v)
        	{
        	case 1: return(GAIN_1);
        	case 2: return(GAIN_2);
        	case 4: return(GAIN_4);
        	case 8: return(GAIN_8);
        	}
        	return(GAIN_8);
        }

        int getValue() {
            return value;
        }
    }

    /**
     * Enum for the Bus and Shunt ADC Resolution/Averaging settings.
     */
    public enum Adc {
        BITS_9(0), //9 bit samples
        BITS_10(1), //10 bit samples
        BITS_11(2), //11 bit samples
        BITS_12(3), //12 bit samples
        SAMPLES_2(9), //2 sample average
        SAMPLES_4(10), //4 sample average
        SAMPLES_8(11), //8 sample average
        SAMPLES_16(12), //16 sample average
        SAMPLES_32(13), //32 sample average
        SAMPLES_64(14), //64 sample average
        SAMPLES_128(15); //128 sample average

        private int value;

        Adc(int val) {
            value = val;
        }
        
        public static Adc getBy(int v)
        {
        	switch(v)
        	{
        	case 9: return(BITS_9);
        	case 10: return(BITS_10);
        	case 11: return(BITS_11);
        	case 12: return(BITS_12);
        	}
        	return(BITS_12);
        }
        
        int getValue() {
            return value;
        }
    }
}
