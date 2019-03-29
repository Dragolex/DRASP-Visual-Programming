package main.functionality.helperControlers.hardware.analog;

import main.functionality.SharedComponents;


public class SimpleAnalogInputDevicePin extends SharedComponents
{	
	

	
		// DELETE THIS FILE!
	
	/*
	
	public static AnalogDevice createMCP3004(int SPIind, int SPIchipselect, int channel)
	{
		StringBuilder key = new StringBuilder();
		key.append("MCP3004_"); // MCP key
		key.append(SPIind);
		key.append("_");
		key.append(SPIchipselect);
		
		
		if (!simulated) // Do not access hardware if simulated
		{		
			AdcGpioProvider provider = AnalogDevice.getIfExists(key.toString());
			
			if (provider == null)
			{
				try
				{
					// Create custom MCP3004 analog gpio provider
					switch(SPIchipselect)
					{
					case 0: provider = new MCP3004GpioProvider(SpiChannel.CS0); break;
					case 1: provider = new MCP3004GpioProvider(SpiChannel.CS1); break;
					default:
						Execution.setError("SPI Chip Select may only be 0 or 1! Given: " + SPIchipselect, false);
						return(null);
					}
					
					AnalogDevice.addProvider(key.toString(), provider);
				}
				catch (IOException e)
				{
					Execution.setError("Error at connecting an MCP3004 analog input.\nError: " + e.getMessage(), false);
				}
				
	
		        // Set the background monitoring interval timer for the underlying framework to
		        // interrogate the ADC chip for input conversion values.  The acceptable monitoring
		        // interval will be highly dependant on your specific project.  The lower this value
		        // is set, the more CPU time will be spend collecting analog input conversion values
		        // on a regular basis.  The higher this value the slower your application will get
		        // analog input value change events/notifications.  Try to find a reasonable balance
		        // for your project needs.
		        //provider.setMonitorInterval(250); // milliseconds
	
			}
			
			return(new AnalogDevice(MCP3004GpioProvider.NAME, channel, provider, 1023));
		}
		else
			return(new AnalogDevice(key.toString() + " Ch: " + channel, 1023));
	}
	
	public static AnalogDevice createMCP3008(int SPIind, int SPIchipselect, int channel)
	{
		StringBuilder key = new StringBuilder();
		key.append("MCP3008_"); // MCP key
		key.append(SPIind);
		key.append("_");
		key.append(SPIchipselect);
		
		
		if (!simulated) // Do not access hardware if simulated
		{		
			AdcGpioProvider provider = AnalogDevice.getIfExists(key.toString());
			
			if (provider == null)
			{
				try
				{
					// Create custom MCP3004 analog gpio provider
					switch(SPIchipselect)
					{
					case 0: provider = new MCP3004GpioProvider(SpiChannel.CS0); break;
					case 1: provider = new MCP3004GpioProvider(SpiChannel.CS1); break;
					default:
						Execution.setError("SPI Chip Select may only be 0 or 1! Given: " + SPIchipselect, false);
						return(null);
					}
					
					AnalogDevice.addProvider(key.toString(), provider);
				}
				catch (IOException e)
				{
					Execution.setError("Error at connecting an MCP3004 analog input.\nError: " + e.getMessage(), false);
				}
	
			}
			
			return(new AnalogDevice(MCP3004GpioProvider.NAME, channel, provider, 1023));
		}
		else
			return(new AnalogDevice(key.toString() + " Ch: " + channel, 1023));
	}
	
	
	
	/*
	public SimpleAnalogInputDevicePin(String type, int SPIind, int SPIchipselect, int channel)
	{
		GPIOctrl.startIfNeeded();
				
		key.append("MCP300"); // MCP key
		key.append(availableChannels); // 4 or 8
		key.append("_"); 
		key.append(SPIind);
		key.append(SPIchipselect);
		
		
		if (!simulated) // Do not access hardware if simulated
		{		
			AdcGpioProvider provider = providers.getOrDefault(key.toString(), null);
			
			if (provider == null)
			{
				try
				{
					// Create custom MCP3008 analog gpio provider
			        // we must specify which chip select (CS) that that ADC chip is physically connected to.
					provider = new MCP3008GpioProvider(SpiChannel.CS0);
					
					providers.put(key.toString(), provider);
					limit = 1023;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				
	
		        // Set the background monitoring interval timer for the underlying framework to
		        // interrogate the ADC chip for input conversion values.  The acceptable monitoring
		        // interval will be highly dependant on your specific project.  The lower this value
		        // is set, the more CPU time will be spend collecting analog input conversion values
		        // on a regular basis.  The higher this value the slower your application will get
		        // analog input value change events/notifications.  Try to find a reasonable balance
		        // for your project needs.
		        //provider.setMonitorInterval(250); // milliseconds
	
			}
			
			
			
			PinImpl ch = new PinImpl(MCP3008GpioProvider.NAME, channel, "PinChannel"+channel, EnumSet.of(PinMode.ANALOG_INPUT));		
			pin = GPIOctrl.gpio.provisionAnalogInputPin(provider, ch);
		}
		
		
		if (debug)
			initDebug(key.toString() + " Ch: " + channel);
		
		
		
		/*
		
        // Provision gpio analog input pins for all channels of the MCP3008.
        // (you don't have to define them all if you only use a subset in your project)
        final GpioPinAnalogInput inputs[] = {

                GPIOctrl.gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH0, "MyAnalogInput-CH0"),

                GPIOctrl.gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH1, "MyAnalogInput-CH1"),

                GPIOctrl.gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH2, "MyAnalogInput-CH2"),

                GPIOctrl.gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH3, "MyAnalogInput-CH3"),

                GPIOctrl.gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH4, "MyAnalogInput-CH4"),

                GPIOctrl.gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH5, "MyAnalogInput-CH5"),

                GPIOctrl.gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH6, "MyAnalogInput-CH6"),

                GPIOctrl.gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH7, "MyAnalogInput-CH7")

        };
		*/




        // Define the amount that the ADC input conversion value must change before
        // a 'GpioPinAnalogValueChangeEvent' is raised.  This is used to prevent unnecessary
        // event dispatching for an analog input that may have an acceptable or expected
        // range of value drift.
        //provider.setEventThreshold(100, pin); // all inputs; alternatively you can set thresholds on each input discretely
        
        



        // Print current analog input conversion values from each input channel
        /*
        for(GpioPinAnalogInput input : inputs)
        {
            System.out.println("<INITIAL VALUE> [" + input.getName() + "] : RAW VALUE = " + input.getValue());
        }
         */

        /*
        // Create an analog pin value change listener
        GpioPinListenerAnalog listener = new GpioPinListenerAnalog()
        {
            @Override
            public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event)
            {
                // get RAW value
                double value = event.getValue();

                // display output
                System.out.println("<CHANGED VALUE> [" + event.getPin().getName() + "] : RAW VALUE = " + value);
            }
        };



        // Register the gpio analog input listener for all input pins
        GPIOctrl.gpio.addListener(listener, inputs);

		*/
		
	//}
	
}
