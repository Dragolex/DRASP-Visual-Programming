package dataTypes.exceptions;

public class MalformedTermException extends Exception
{
	private static final long serialVersionUID = 8999023490746853737L;
	
	String problem;
	public MalformedTermException(String problem)
	{
		this.problem = problem;
	}
	
	public String getProblem()
	{
		return(problem);
	}

}
