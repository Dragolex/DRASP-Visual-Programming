package main.functionality.helperControlers.hardware.analog.ValueSources;

public abstract class ValueSource
{
	
	abstract public double getValue(int typeIfMultitype) throws Exception;

}
