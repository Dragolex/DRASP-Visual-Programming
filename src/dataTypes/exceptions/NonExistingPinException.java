package dataTypes.exceptions;

import execution.Execution;

public class NonExistingPinException extends Exception
{
	private static final long serialVersionUID = -5796537781963903367L;
	
	private int attemptedPin;
	
	public NonExistingPinException(int attemptedPin)
	{
		this.attemptedPin = attemptedPin;
	}
	
	public int getAttemptedPin()
	{
		return(attemptedPin);
	}

	public void callException()
	{
		Execution.setError("The pin '" + attemptedPin + "' does not exist! Are you using the right layout (not the Java-Pin variant) as intended?" , false);
	}
	
	@Override
	public String getMessage()
	{
		return("The pin '" + attemptedPin + "' does not exist! Are you using the right layout (not the Java-Pin variant) as intended?");
	}
	
}
