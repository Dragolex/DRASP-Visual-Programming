package main.functionality.helperControlers.hardware.analog;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.pi4j.gpio.extension.ads.ADS1015GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider.ProgrammableGainAmplifierValue;
import com.pi4j.gpio.extension.mcp.MCP3004GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008GpioProvider;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.gpio.impl.PinImpl;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.io.spi.SpiChannel;

import execution.Execution;
import main.functionality.SharedComponents;
import main.functionality.helperControlers.hardware.analog.ValueSources.BH1750source;
import main.functionality.helperControlers.hardware.analog.ValueSources.BMP280source;
import main.functionality.helperControlers.hardware.analog.ValueSources.DS18B20source;
import main.functionality.helperControlers.hardware.analog.ValueSources.GY511LSM303source;
import main.functionality.helperControlers.hardware.analog.ValueSources.INA219source;
import main.functionality.helperControlers.hardware.analog.ValueSources.MPU6050source;
import main.functionality.helperControlers.hardware.analog.ValueSources.Pi4JanalogSource;
import main.functionality.helperControlers.hardware.analog.ValueSources.ValueSource;
import staticHelpers.GuiMsgHelper;
import staticHelpers.LocationPreparator;

public class SensorDevice extends SharedComponents
{
	static Map<String, GpioProvider> providers = new HashMap<>();
	static Map<String, ValueSource> multiSensorSources = new HashMap<>();
	
	
	public static SensorDevice createDevice(String type, int VAL_A, int VAL_B, int channel, double gain)
	{
		StringBuilder key = new StringBuilder(type); // based on type
		key.append("_");
		key.append(VAL_A);
		key.append("_");
		key.append(VAL_B);
		
		String keyStr = key.toString();
		
		
		if (!SIMULATED) // Do not access hardware if simulated
		{		
			GpioProvider provider = providers.getOrDefault(key, null);
			
			if (provider == null)
			{
				try
				{
					// Create corresponding analog gpio provider
					switch(type)
					{
					case "MCP3004": provider = new MCP3004GpioProvider((VAL_B == 1) ? SpiChannel.CS1 : SpiChannel.CS0); break;
					case "MCP3008": provider = new MCP3008GpioProvider((VAL_B == 1) ? SpiChannel.CS1 : SpiChannel.CS0); break;
					case "ADS1015": provider = new ADS1015GpioProvider(LocationPreparator.i2c_bus_ind(), VAL_B); break;
					case "ADS1115": provider = new ADS1115GpioProvider(LocationPreparator.i2c_bus_ind(), VAL_B); break;
					}
					
					providers.put(key.toString(), provider);
				}
				catch (IOException | UnsupportedBusNumberException e)
				{
					Execution.setError("Error at connecting an " + type +" analog input.\nError: " + e.getMessage(), false);
				}
				
			}
			
			switch(type)
			{
			case "MCP3004": return(new SensorDevice(keyStr, MCP3004GpioProvider.NAME, channel, provider, 1023, -1));
			case "MCP3008": return(new SensorDevice(keyStr, MCP3008GpioProvider.NAME, channel, provider, 1023, -1));
			case "ADS1015": return(new SensorDevice(keyStr, ADS1015GpioProvider.NAME, channel, provider, 1023, gain));
			case "ADS1115": return(new SensorDevice(keyStr, ADS1115GpioProvider.NAME, channel, provider, 1023, gain));
			}
			
			return(null);
		}
		else
			return(new SensorDevice(keyStr,keyStr + " Ch: " + channel, 1023, false));
	}
	
	
	
	public static SensorDevice createBH1750sensor( int addr) throws UnsupportedBusNumberException, IOException
	{
		if(!SIMULATED)
			return(new SensorDevice(new BH1750source( addr)));
		else
			return(new SensorDevice("BH1750","BH1750", 1, true));
	}
	
	public static SensorDevice createBMP280sensor( int addr, int typeIfMultitype) throws UnsupportedBusNumberException, IOException // Todo: Enable a way to read the temperature from this sensor too
	{
		StringBuilder key = new StringBuilder("BMP280"); // based on type
		key.append("_");
		key.append(addr);
		
		if(!SIMULATED)
		{
			if (multiSensorSources.containsKey(key.toString()))
				return(new SensorDevice(multiSensorSources.get(key.toString()), typeIfMultitype));
			else
			{
				ValueSource v = new BMP280source(addr);
				multiSensorSources.put(key.toString(), v);
				return(new SensorDevice(v, typeIfMultitype));
			}
		}
		else
			return(new SensorDevice("BMP280", key.toString(), 1, true));
	}
	
	
	public static SensorDevice createLSM303Dsensor(int addr, int typeIfMultitype) throws UnsupportedBusNumberException, IOException
	{
		StringBuilder key = new StringBuilder("LSM303D"); // based on type
		key.append("_");
		key.append(addr);
		
		if(!SIMULATED)
		{
			if (multiSensorSources.containsKey(key.toString()))
				return(new SensorDevice(multiSensorSources.get(key.toString()), typeIfMultitype));
			else
			{
				ValueSource v = new GY511LSM303source(addr);
				multiSensorSources.put(key.toString(), v);
				return(new SensorDevice(v, typeIfMultitype));
			}
		}
		else
			return(new SensorDevice("LSM303D", key.toString(), 1, true));
	}
	
	
	public static SensorDevice createMPU6050sensor(int addr, int typeIfMultitype) throws UnsupportedBusNumberException, IOException
	{
		StringBuilder key = new StringBuilder("MPU6050"); // based on type
		key.append("_");
		key.append(addr);
		
		if(!SIMULATED)
		{
			if (multiSensorSources.containsKey(key.toString()))
				return(new SensorDevice(multiSensorSources.get(key.toString()), typeIfMultitype));
			else
			{
				ValueSource v = new MPU6050source(addr);
				multiSensorSources.put(key.toString(), v);
				return(new SensorDevice(v, typeIfMultitype));
			}
		}
		else
			return(new SensorDevice("MPU6050", key.toString(), 1, true));
	}
	

	public static SensorDevice createINA219sensor( int addr, int typeIfMultitype, double resistor, double maxExpectedCurrent, int voltageRange16or32, int gain1or2or4or8, int busAdcRange9to12, int shuntAdcRange9to12) throws UnsupportedBusNumberException, IOException
	{
		StringBuilder key = new StringBuilder("INA219"); // based on type
		key.append("_");
		key.append(addr);
		
		if(!SIMULATED)
		{
			if (multiSensorSources.containsKey(key.toString()))
				return(new SensorDevice(multiSensorSources.get(key.toString()), typeIfMultitype));
			else
			{
				ValueSource v = new INA219source(addr, resistor, maxExpectedCurrent, voltageRange16or32, gain1or2or4or8, busAdcRange9to12, shuntAdcRange9to12);
				
				multiSensorSources.put(key.toString(), v);
				return(new SensorDevice(v, typeIfMultitype));
			}
		}
		else
			return(new SensorDevice("INA219","INA219", 1, true));
	}
	

	public static SensorDevice createDS18B20sensor(int oneWireDeviceID, String notThis, int repeatDelay, int maxRepeats)
	{
		if(!SIMULATED)
			return(new SensorDevice(new DS18B20source(oneWireDeviceID, notThis, repeatDelay, maxRepeats)));
		else
			return(new SensorDevice("DS18B20","DS18B20", 1, true));
	}




	private String key = "";
	
	private double bottomMargin = 0, topMargin = 1, range = 1;
	private boolean useAbsolute = false, directOnly = false;
	
	
	private ValueSource valueSource;
	private double limit;
	
	private String debugName;
	
	private double simulatedValue = 0;
	
	private int typeIfMultitype = -1;
	
	
	public SensorDevice(ValueSource valueSource) // simple with source only
	{
		this.valueSource = valueSource;
		directOnly = true;
	}
	public SensorDevice(ValueSource valueSource, int typeIfMultitype) // simple with source only
	{
		this.valueSource = valueSource;
		this.typeIfMultitype = typeIfMultitype;
		directOnly = true;
	}
	
	public SensorDevice(String key, String internalProviderName, int channel, GpioProvider provider, int limit, double gain)
	{
		this.key = key;
		this.limit = limit;
		
		GPIOctrl.startIfNeeded();
		
		if (!SIMULATED)
		{			
			PinImpl ch = new PinImpl(internalProviderName, channel, "PinChannel"+channel, EnumSet.of(PinMode.ANALOG_INPUT));		
			GpioPinAnalogInput pin = GPIOctrl.gpio.provisionAnalogInputPin(provider, ch);
			
	        if (gain > 0) // ignore if <= 0    
		    if (gain < 1)
		    	((ADS1x15GpioProvider) provider).setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_6_144V, pin);
		    else
			if (gain < 2)
				((ADS1x15GpioProvider) provider).setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_4_096V, pin);
			else
			if (gain < 4)
				((ADS1x15GpioProvider) provider).setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_2_048V, pin);
			else
			if (gain < 8)
				((ADS1x15GpioProvider) provider).setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_1_024V, pin);
			else
			if (gain < 16)
				((ADS1x15GpioProvider) provider).setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_0_512V, pin);
			else
				((ADS1x15GpioProvider) provider).setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_0_256V, pin);
	        
	        
	        if (DEBUG)
	        {
		    	pin.addListener(new GpioPinListenerAnalog()
		    	{
		    		@Override
		    	    public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event)
		    	    {
		    			Execution.updateDebugVariable(debugName, "Analog Input", String.valueOf( event.getValue() ), false);
		    	    }
		    	});
	        }
	    	
	    	valueSource = new Pi4JanalogSource(pin);
		}
	}
	
	public SensorDevice(String key, String name, int limit, boolean directOnly) // simulated
	{
		this.key = key;
		this.limit = limit;
		this.directOnly = directOnly;
		
		if (!DEBUG) return;
		
		debugName = name;
		
    	if (SIMULATED)
	    	Execution.addFunctionalDebugVariable(debugName, "Analog Input", String.valueOf(simulatedValue), () -> {
				Double res = GuiMsgHelper.getNumberNonblockingUI("Type in the new value of the analog input to change it instantly.\nNote that this refers to the absolute hardware output\nwithout offset or scaling applied!", simulatedValue);
				if (res != null)
				{
					simulatedValue = (double) res;
	               	Execution.updateDebugVariable(debugName, "Analog Input", String.valueOf( getValue() ), false);
				}
	    	});
    	
	    Execution.updateDebugVariable(debugName, "Analog Input", String.valueOf( getValue() ), false);
	}
	



	public void adjust(double bottomMargin, double topMargin, boolean useAbsolute)
	{
		this.bottomMargin = bottomMargin;
		this.topMargin = topMargin;
		this.useAbsolute = useAbsolute;
		
		range = Math.abs(topMargin-bottomMargin);
		
	}

	
	

	Double lastValue = null;
	
	public Double getLastValue(double newValue)
	{
		Double l = lastValue;
		lastValue = newValue;
		return(l);
	}
	
	
	
	public Double getValue()
	{
		double val;
		
		try {
			val = SIMULATED ? simulatedValue : valueSource.getValue(typeIfMultitype);
		} catch (Exception e) {
			Execution.setError("Reading a value from the sensor failed!\nError: " + e.getMessage(), false);
			return(0.0);
		}
		
		if (directOnly)
			return(val);
		
		if (!useAbsolute)
			val /= limit;
		
		double dbt = Math.max(0, val-bottomMargin);
		
		if (val > topMargin)
			return(useAbsolute ? limit : 1);
		
		return(dbt*((useAbsolute ? limit : 1)/range));
	}
	
	
	@Override
	public String toString()
	{
		return("Device: " + key.toString());
	}
	

}
