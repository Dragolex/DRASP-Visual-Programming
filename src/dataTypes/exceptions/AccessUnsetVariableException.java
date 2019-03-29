package dataTypes.exceptions;

import dataTypes.specialContentValues.Variable;

public class AccessUnsetVariableException extends Exception
{
	private static final long serialVersionUID = -5796537781963903367L;
	
	private Variable source;
	
	public AccessUnsetVariableException(Variable source)
	{
		this.source = source;
	}
	
	public Variable getSourceVariable()
	{
		return(source);
	}

}
