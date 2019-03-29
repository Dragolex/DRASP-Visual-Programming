package main.functionality.helperControlers.hardware;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.wiringpi.Gpio;

import dataTypes.exceptions.NonExistingPinException;
import main.functionality.SharedComponents;

public class LowMhzTransmitter extends SharedComponents
{
	Map<Integer, GpioPinDigitalOutput> pins = new HashMap<>();
	
    private final static int pulseLength = 350;
    private final static int repeatTransmit = 10;
	
	public static void sendValue(int pinInd, double value) throws NonExistingPinException
	{
		GpioPinDigitalOutput pin = GPIOctrl.getOutputPin(pinInd);
		if (pin == null)
			pin = GPIOctrl.setOutputPin(pinInd, false);
		
		if (SIMULATED) return;
		
		
		String codeWord = String.valueOf(value);
		
		
		for (int nRepeat = 0; nRepeat < repeatTransmit; nRepeat++)
		{
            for (int i = 0; i < codeWord.length(); ++i)
            {
                switch (codeWord.charAt(i))
                {
                    case '0':
                    	transmit(pin, 1, 3);
                        transmit(pin, 1, 3);
                        break;

                    case 'F':
                    	transmit(pin, 1, 3);
                        transmit(pin, 3, 1);
                        break;

                    case '1':
                    	transmit(pin, 3, 1);
                        transmit(pin, 3, 1);
                        break;
                }
            }

            // sendSync
            transmit(pin, 1, 31);
        }
		
		
		
	}
	
	
	private static void transmit(GpioPinDigitalOutput transmitterPin, int nHighPulses, int nLowPulses)
	{
            transmitterPin.high();

            Gpio.delayMicroseconds(pulseLength * nHighPulses);

            transmitterPin.low();

            Gpio.delayMicroseconds(pulseLength * nLowPulses);
    }
}
