package main.functionality.helperControlers;

public class LoopHelper
{
	int counter = 0;
	int index = 0;
	
	public void setCounter(int counter)
	{
		this.counter = counter;
	}
	
	public boolean canContinue()
	{
		return(counter > 0);
	}
	
	public void next()
	{
		counter--;
		index++;
	}
	
	public int getIndex()
	{
		return(index);
	}

	public void reset()
	{
		index = 0;
	}

}
