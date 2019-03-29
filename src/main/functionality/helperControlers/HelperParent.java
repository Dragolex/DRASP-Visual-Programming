package main.functionality.helperControlers;

import main.functionality.SharedComponents;

public abstract class HelperParent extends SharedComponents
{
	private volatile boolean started = false;
	
	
	abstract protected void start();	
	abstract protected void reset();
	abstract protected void quit();	
	
	
	public void startIfNeeded()
	{
		synchronized(HelperParent.class)
		{			
			if (started) return;
		
			start(); // Call the overridden child function
			
			started = true;
		}
	}
	
	
	public void doReset()
	{
		if (started)
			reset();
	}
	
	public void doQuit()
	{
		if (started)
			quit();
	}
	
	
}
