package main.functionality.helperControlers.hardware.analog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

import execution.Execution;
import staticHelpers.LocationPreparator;

public class ProtocolHelpers {

    public static List<String> scanI2C()
    {    	
        List<String> validAddresses = new ArrayList<String>();
        I2CBus bus;
		try {
			bus = I2CFactory.getInstance(LocationPreparator.i2c_bus_ind() == 1 ? I2CBus.BUS_1 : I2CBus.BUS_0);
		} catch (UnsupportedBusNumberException | IOException e)
		{
			Execution.setError("Scanning I2C ports failed!\nError: " + e.getMessage(), false);
			return(null);
		}
		
        for (int i = 1; i < 128; i++) {
          try {
              I2CDevice device  = bus.getDevice(i);
              device.write((byte)0);
              validAddresses.add(Integer.toHexString(i));
          } catch (Exception ignore) { }
        }
        
        /*
        System.out.println("Found: ---");
        for (String a : validAddresses) {
            System.out.println("Address: " + a);
        }
        System.out.println("----------");
        */
        
        return(validAddresses);
    }
    
    

    public static void writeRegister(I2CDevice device, final int ra, final int value) throws IOException
    {
        device.write(ra, new byte[] { (byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF) });
    }

    public static int readRegister(I2CDevice device, final int ra) throws IOException
    {
        byte[] buf = new byte[2];
        device.read(ra, buf, 0, buf.length);
        return(((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF));
    }

    public static short readSignedRegister(I2CDevice device, final int ra) throws IOException
    {
        byte[] buf = new byte[2];
        device.read(ra, buf, 0, buf.length);
        return((short) ((buf[0] << 8) | (buf[1] & 0xFF)));
    }
    
    
    
	public static List<String> scanOneWire()
	{
		List<String> validAddresses = new ArrayList<String>();
		
		W1Master master = new W1Master();

		for (W1Device dev : master.getDevices())
		{
			validAddresses.add( String.valueOf(dev.getFamilyId()) );
			//validAddresses.add( String.valueOf(dev.getId()) );
		}
		
		return(validAddresses);
	}

	
}
