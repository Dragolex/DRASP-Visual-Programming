package main.functionality.helperControlers.hardware.analog.ValueSources;

import com.pi4j.io.gpio.GpioPinAnalogInput;

public class Pi4JanalogSource extends ValueSource
{
	GpioPinAnalogInput pin;

	public Pi4JanalogSource(GpioPinAnalogInput pin)
	{
		this.pin = pin;
	}

	@Override
	public double getValue(int typeIfMultitype)
	{
		return(pin.getValue());
	}
}
