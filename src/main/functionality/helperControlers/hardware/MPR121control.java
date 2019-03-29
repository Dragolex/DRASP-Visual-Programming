package main.functionality.helperControlers.hardware;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import dataTypes.ProgramEventContent;
import dataTypes.exceptions.NonExistingPinException;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.SharedComponents;
import staticHelpers.DebugMsgHelper;
import staticHelpers.LocationPreparator;

/**
 * Utility class for communicating with the Mpr121 through the I2C channel
 * 
 * @author Mauro Miranda
 * 
 * https://github.com/grandeemme/mpr121/blob/master/src/main/java/com/mauromiranda/mpr121/Mpr121.java
 */
public class MPR121control extends SharedComponents {
	
	public static byte MHD_R = 0x2B;
	public static byte NHD_R = 0x2C;
	public static byte NCL_R = 0x2D;
	public static byte FDL_R = 0x2E;
	public static byte MHD_F = 0x2F;
	public static byte NHD_F = 0x30;
	public static byte NCL_F = 0x31;
	public static byte FDL_F = 0x32;
	
	public static byte ELE_VAL = 0x41; // (T = uneven numbers; R = even numbers)
	
	public static byte FIL_CFG = 0x5D;
	public static byte ELE_CFG = 0x5E;
	public static byte GPIO_CTRL0 = 0x73;
	public static byte GPIO_CTRL1 = 0x74;
	public static byte GPIO_DATA = 0x75;
	public static byte GPIO_DIR = 0x76;
	public static byte GPIO_EN = 0x77;
	public static byte GPIO_SET = 0x78;
	public static byte GPIO_CLEAR = 0x79;
	public static byte GPIO_TOGGLE = 0x7A;
	public static byte ATO_CFG0 = 0x7B;
	public static byte ATO_CFGU = 0x7D;
	public static byte ATO_CFGL = 0x7E;
	public static byte ATO_CFGT = 0x7F;

	// Global Constants
	public static byte TOU_THRESH = 0x06;
	public static byte REL_THRESH = 0x0A;
	

	protected I2CDevice device;

	protected GpioController gpio;

	protected GpioPinDigitalInput interrupt;

	boolean[] touchStates = new boolean[12];

	protected Lock lock = new ReentrantLock();

	protected Pin gpioInterrupt;
	
	
	static Map<Integer, MPR121control> controlers = new HashMap<>();
	static Map<Integer, Integer> associatedPins = new HashMap<>();
	
	static public void reset()
	{
		controlers.clear();
		associatedPins.clear();
	}
	
	static public void setupInput(int addr, int channel, int pinIndex, int gpioInterrupt, byte touchThreshold, byte releaseThreshold) throws IOException, UnsupportedBusNumberException, NonExistingPinException
	{
		MPR121control cont;
		if (controlers.containsKey(addr))
			cont = controlers.get(addr);
		else
			cont = new MPR121control(addr, gpioInterrupt);
		
		cont.assignPin(channel, pinIndex, touchThreshold, releaseThreshold);
	}
	
	public MPR121control(int addr, int gpioInterrupt) throws UnsupportedBusNumberException, IOException, NonExistingPinException
	{
		I2CBus bus = I2CFactory.getInstance(LocationPreparator.i2c_bus_ind() == 1 ? I2CBus.BUS_1 : I2CBus.BUS_0);
		device = bus.getDevice(addr);
		
		setup();
		
		// provision gpio pin #02 as an input pin with its internal pull down
		// resistor enabled
		GPIOctrl.startIfNeeded();		
		interrupt = GPIOctrl.getInputPin(gpioInterrupt, false);
		
		
		// create and register gpio pin listener
		interrupt.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
			{
				try {
					lock.tryLock(100, TimeUnit.MILLISECONDS);
					byte[] reisters = new byte[42];
					device.read(reisters, 0, 42);
					
					byte LSB = reisters[0];
					byte MSB = reisters[1];
					//
					int touched = ((MSB << 8) | LSB);
					// controllo i primi 8
					for (int i = 0; i < 12; i++) {
						if ((touched & (1 << i)) != 0x00) {
							if (!touchStates[i]) {
								
								// pin i was just touched
								
								int p = associatedPins.getOrDefault(i,-1);								
								if (p != -1)
								if (!Execution.isPaused())
								if(Execution.isRunning())
									{
										Object[] dat = {p, true};
										
										for(ProgramEventContent cont: Functionality.GPIOchangedEventContents) // loop through all GPIO-changed events
											cont.triggerExternally(dat); // pass the input value and trigger
									}
																
							} else {
								
								// pin i is still being touched
								
							}
							touchStates[i] = true;
						} else {
							if (touchStates[i]) {
								
								// pin i is no longer being touched
								int p = associatedPins.getOrDefault(i,-1);								
								if (p != -1)
								if (!Execution.isPaused())
								if(Execution.isRunning())
									{
										Object[] dat = {p, false};
										
										for(ProgramEventContent cont: Functionality.GPIOchangedEventContents) // loop through all GPIO-changed events
											cont.triggerExternally(dat); // pass the input value and trigger
									}
								
							}
							touchStates[i] = false;
						}
					}
				} catch (Exception e) {
					Execution.setError("Problem at checking an MPR121 touchs ensor! Error: " + e, false);
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		});
	}
	
	private void assignPin(int channel, int pinIndex, byte touchThreshold, byte releaseThreshold) throws IOException
	{
		set_register((byte) (ELE_VAL + (byte)(channel*2)), touchThreshold);
		set_register((byte) (ELE_VAL + (byte)(channel*2) + 1), releaseThreshold);
		
		associatedPins.put(channel, pinIndex);

		touchStates[channel] = false;
		
		GPIOctrl.registerNonstandardInputPin(pinIndex, () -> {return(touchStates[channel]);} );
		
		
    	if (DEBUG) DebugMsgHelper.setCheckPinDebug(pinIndex, () -> {
    		touchStates[channel] = !touchStates[channel];
    		
			if (!Execution.isPaused())
			if(Execution.isRunning())
				{
					Object[] dat = {pinIndex, touchStates[channel]};
					
					for(ProgramEventContent cont: Functionality.GPIOchangedEventContents) // loop through all GPIO-changed events
						cont.triggerExternally(dat); // pass the input value and trigger
				}
    		
			DebugMsgHelper.setPinStateDebug(pinIndex, false, touchStates[channel]);	
    	});

	}

	public void setup() throws IOException {
		set_register(ELE_CFG, (byte) 0x00);

		// Section A - Controls filtering when data is > baseline.
		set_register(MHD_R, (byte) 0x01);
		set_register(NHD_R, (byte) 0x01);
		set_register(NCL_R, (byte) 0x00);
		set_register(FDL_R, (byte) 0x00);

		// Section B - Controls filtering when data is < baseline.
		set_register(MHD_F, (byte) 0x01);
		set_register(NHD_F, (byte) 0x01);
		set_register(NCL_F, (byte) 0xFF);
		set_register(FDL_F, (byte) 0x02);

		
		// Section D
		// Set the Filter Configuration
		// Set ESI2
		set_register(FIL_CFG, (byte) 0x04);

		// Section E
		// Electrode Configuration
		// Set ELE_CFG to 0x00 to return to standby mode
		set_register(ELE_CFG, (byte) 0x0C); // Enables all 12 Electrodes

		// Section F
		// Enable Auto Config and auto Reconfig
		/*
		 * set_register( ATO_CFG0, 0x0B); set_register( ATO_CFGU, 0xC9); // USL
		 * = (Vdd-0.7)/vdd*256 = 0xC9 @3.3V set_register( ATO_CFGL, 0x82); //
		 * LSL = 0.65*USL = 0x82 @3.3V set_register( ATO_CFGT, 0xB5);
		 */// Target = 0.9*USL = 0xB5 @3.3V

	}

	private void set_register(byte address, byte value) throws IOException {
		device.write(new byte[] { address, value }, 0, 2);
	}



}