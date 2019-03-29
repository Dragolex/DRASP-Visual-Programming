package main.functionality.helperControlers;

import dataTypes.FunctionalityContent;
import execution.Execution;

public class Parameters {
	
	Object[] params;
	Object[] optionalParams;
	int optCount = 0;
	
	// Call this with the corresponding ProgramContent.
	// You can place it directly in the return statement because it returns the content.
	public FunctionalityContent attach(FunctionalityContent content)
	{
		params = content.getArgumentValues();
		optionalParams = content.getTotalOptionalOrExpandedArgumentsArray();
		optCount = optionalParams.length;
		
		return(content);
	}
	
	
	private String inaccessibleErr = "Trying to access a parameter with an index that is out of bounds!\nIndex: ";
	
	// Return the argument with the given index and verify the given type
	public Object get(int index, Class<?> classType)
	{
		try {
			return(classType.cast(params[index]));
		}
		catch (ClassCastException e)
		{
			Execution.setError("A parameter needs to be of type '" + classType.getSimpleName() + "'\nbut had the type: " + params[index].getClass().getSimpleName(), false);
		}
		catch (IndexOutOfBoundsException e)
		{
			Execution.setError(inaccessibleErr + index, false);
		}
		return(null);
	}

	
	public boolean getBool(int index)
	{
		try {
			return((boolean) params[index]);
		}
		catch (ClassCastException e)
		{
			Execution.setError("A parameter needs to be a binary value (Boolean)\nbut had the type: " + params[index].getClass().getSimpleName(), false);
		}
		catch (IndexOutOfBoundsException e)
		{
			Execution.setError(inaccessibleErr + index, false);
		}

		return(false);
	}
	
	
	public int getInt(int index)
	{
		try {
			return((int) (double) params[index]);
		}
		catch (ClassCastException e)
		{
			Execution.setError("A parameter needs to be a numeric value (Double)\nbut had the type: " + params[index].getClass().getSimpleName(), false);
		}
		catch (IndexOutOfBoundsException e)
		{
			Execution.setError(inaccessibleErr + index, false);
		}

		return(0);
	}
	
	
	public double getDouble(int index)
	{
		try {
			return((double) params[index]);
		}
		catch (ClassCastException e)
		{
			Execution.setError("A parameter needs to be a numeric value (Double)\nbut had the type: " + params[index].getClass().getSimpleName(), false);
		}
		catch (IndexOutOfBoundsException e)
		{
			Execution.setError(inaccessibleErr + index, false);
		}
		
		return(0);
	}

	
	public float getFloat(int index)
	{
		try {
			return((float) (double) params[index]);
		}
		catch (ClassCastException e)
		{
			Execution.setError("A parameter needs to be a numeric value (Double)\nbut had the type: " + params[index].getClass().getSimpleName(), false);
		}
		catch (IndexOutOfBoundsException e)
		{
			Execution.setError(inaccessibleErr + index, false);
		}

		return(0);
	}
	
	
		
	
	
	
	
	
	/// OPTIONAL PARAMETERS ///
	
	// Return whether an optional argument with the given index exists
	public boolean hasOptParam(int index)
	{
		return(index < optCount);
	}
	
	// Return the number of optional arguments
	public int getOptCount()
	{
		return(optCount);
	}
	
	// Return the array of optional parameters
	public Object[] getOptArray()
	{
		return(optionalParams);
	}
	
	
	private String inaccessibleOptErr = "Trying to access an optional parameter that has not been provided.\nThe functionality should verify that using hasOptParam(index).\nIndex: ";

	
	// Return the optional argument of the given index and verify the given type
	public Object getOpt(int index, Class<?> classType)
	{
		try {
			return(classType.cast(optionalParams[index]));
		}
		catch (ClassCastException e)
		{
			Execution.setError("An optional parameter needs to be of type '" + classType.getSimpleName() + "'\nbut had the type: " + optionalParams[index].getClass().getSimpleName(), false);
		}
		catch (IndexOutOfBoundsException e)
		{
			Execution.setError(inaccessibleOptErr + index, false);
		}
		return(null);
	}
	public Object getOpt(int index, Class<?> classType, Object def)
	{
		if (index < optCount)
			return(getOpt(index, classType));
		return(def);
	}

	
	public boolean getOptBool(int index)
	{
		try {
			return((boolean) optionalParams[index]);
		}
		catch (ClassCastException e)
		{
			Execution.setError("An optional parameter needs to be a binary value (Boolean)\nbut had the type: " + optionalParams[index].getClass().getSimpleName(), false);
		}
		catch (IndexOutOfBoundsException e)
		{
			Execution.setError(inaccessibleOptErr + index, false);
		}

		return(false);
	}
	public int getOptBool(int index, int def)
	{
		if (index < optCount)
			return(getOptInt(index));
		return(def);
	}

	
	public int getOptInt(int index)
	{
		try {
			return((int) (double) optionalParams[index]);
		}
		catch (ClassCastException e)
		{
			Execution.setError("An optional parameter needs to be a numeric value (Double)\nbut had the type: " + optionalParams[index].getClass().getSimpleName(), false);
		}
		catch (IndexOutOfBoundsException e)
		{
			Execution.setError(inaccessibleOptErr + index, false);
		}

		return(0);
	}
	public int getOptInt(int index, int def)
	{
		if (index < optCount)
			return(getOptInt(index));
		return(def);
	}

	
	public double getOptDouble(int index)
	{
		try {
			return((double) optionalParams[index]);
		}
		catch (ClassCastException e)
		{
			Execution.setError("An optional parameter needs to be a numeric value (Double)\nbut had the type: " + optionalParams[index].getClass().getSimpleName(), false);
		}
		catch (IndexOutOfBoundsException e)
		{
			Execution.setError(inaccessibleOptErr + index, false);
		}

		return(0);
	}
	public double getOptDouble(int index, double def)
	{
		if (index < optCount)
			return(getOptDouble(index));
		return(def);
	}

	
	public float getOptFloat(int index)
	{
		try {
			return((float) (double) optionalParams[index]);
		}
		catch (ClassCastException e)
		{
			Execution.setError("An optional parameter needs to be a numeric value (Double)\nbut had the type: " + optionalParams[index].getClass().getSimpleName(), false);
		}
		catch (IndexOutOfBoundsException e)
		{
			Execution.setError(inaccessibleOptErr + index, false);
		}

		return(0);
	}
	public float getOptFloat(int index, float def)
	{
		if (index < optCount)
			return(getOptFloat(index));
		return(def);
	}
	
}
