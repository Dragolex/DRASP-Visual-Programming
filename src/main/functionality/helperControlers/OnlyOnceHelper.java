package main.functionality.helperControlers;

public class OnlyOnceHelper
{
	boolean once = false;
	
	public boolean handle(boolean res, boolean onlyOnce)
	{
		if(res)
		{
			if (!once)
			{
				once = onlyOnce;
				return(true);
			}
			else
				return(false);
		}
		else
			once = false;
			
		return(false);
	}
	
	
	public boolean do_it()
	{
		if (once)
			return(false);
		once = true;
		return(true);
	}
	
}
