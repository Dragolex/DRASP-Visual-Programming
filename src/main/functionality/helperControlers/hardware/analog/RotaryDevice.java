package main.functionality.helperControlers.hardware.analog;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import dataTypes.exceptions.NonExistingPinException;
import execution.Execution;
import main.functionality.SharedComponents;
import settings.GlobalSettings;
import staticHelpers.DebugMsgHelper;

public class RotaryDevice extends SharedComponents
{
	int CLKpin;
	int DTpin;
	
    boolean clkLastState;
	
    volatile boolean incremented = false;
    volatile boolean decremented = false;
    volatile boolean pressed = false;
    
	public RotaryDevice(int CLKpin, int DTpin, int debounce) throws NonExistingPinException
	{
		this.CLKpin = CLKpin;
		this.DTpin = DTpin;
		
		GPIOctrl.startIfNeeded();
		
		boolean toDown = false;
		
	    if (DEBUG)
	    {
	    	DebugMsgHelper.rotaryDevice(this);
	    }
	    
	    
	    if (SIMULATED)
			return;
	    
		
	    final GpioPinDigitalInput CLK = GPIOctrl.getInputPin(CLKpin, !toDown);
	    final GpioPinDigitalInput DT = GPIOctrl.getInputPin(DTpin, !toDown);
	    
	    if (debounce > 0)
	    {
	    	CLK.setDebounce(debounce);
	    	DT.setDebounce(debounce);
	    }
	    CLK.setShutdownOptions(true);
	    DT.setShutdownOptions(true);
	    
	    
	    PinState targ = toDown ? PinState.LOW : PinState.HIGH;
	    
	    clkLastState = CLK.getState() == PinState.HIGH;
	    	
	    CLK.addListener(new GpioPinListenerDigital() {
	    
	    	@Override
	    	public synchronized void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
	        {
	       		boolean clkState = event.getState() == targ;
	       	 	boolean dtState = DT.getState() == targ;
	       		 	
	            if (clkState != clkLastState)
	            	if (dtState != clkState)
	            	{
                       	incremented = true;
                       	if (DEBUG) Execution.updateDebugVariable(GlobalSettings.debugButtonStartSymbol + "->: CLK:" + CLKpin + " DT:" + DTpin, "Rotary Input", "Rotate Forth", false);	
	            	}
                    else
                    {
                       	decremented = true;
                       	if (DEBUG) Execution.updateDebugVariable(GlobalSettings.debugButtonStartSymbol + "<-: CLK:" + CLKpin + " DT:" + DTpin, "Rotary Input", "Rotate Back", false);	
                    }
	                
                clkLastState = clkState;
	        }
	    });
	    
	    

	    
	}
	
	
	public boolean incremented()
	{
		if (incremented)
		{
			incremented = false;
			return(true);
		}
		return(false);
	}
	
	public boolean decremented()
	{
		if (decremented)
		{
			decremented = false;
			return(true);
		}
		return(false);
	}

	/*
	public boolean pressed()
	{
		if(pressed)
		{
			pressed = false;
			return(true);
		}
		return(false);
	}
	*/
	
	@Override
	public String toString()
	{
		return("CLK: " + CLKpin + " DT: " + DTpin);
	}


	public void setIncremented() // only sued by the debug helper
	{
		incremented = true;
	}
	public void setDecremented() // only sued by the debug helper
	{
		decremented = true;
	}
}
