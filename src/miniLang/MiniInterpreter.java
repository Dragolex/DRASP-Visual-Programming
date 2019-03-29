package miniLang;

import java.util.ArrayList;
import java.util.List;

import dataTypes.minor.Pair;

public class MiniInterpreter {
	
	FunctionBase functions;
	LocalVariables variables;
	
	public void interpret(FunctionBase functions, String[] commandLines)
	{
		functions = this.functions;
		variables = new LocalVariables();
		
		for(String line: commandLines)
		{
			while(!line.isEmpty())
				line = interpretBlocks(line, false);
		}
		
		
	}
	
	
	private String interpretBlocks(String block, boolean withReturn)
	{
		return(interpretBlocks(block, withReturn, false));
	}
	
	private String interpretBlocks(String block, boolean withReturn, boolean asIf)
	{
		block = block.trim();
		
		String element = nextElement(block);
		
		String remainder = block.substring(element.length());
		
		
		if (element.equals("if"))
		{
			equals(interpretBlocks(remainder, true, true));
			return("");
		}
		
		
		switch(remainder.trim().charAt(0))
		{
		case '(': // symbol is a bracket, therefore element is a function call
			{
				Pair<Integer, List<String>> params = getParameters(remainder);

				List<String> interpretedParams = new ArrayList<>();
				
				for(String str: params.second)
				{
					interpretedParams.add(interpretBlocks(str, true).trim()); // interpret every parameter
				}
					
				String resp = functions.execute(element, interpretedParams);
				String remainder2 = remainder.substring(params.first);
				
				if (withReturn)
					return(resp + " " + remainder2);
				return(remainder2); // return the remainder						
			}
			
		case '=': // variable set
			{
				if (asIf)
				{
					boolean res = true; // TODO
					
					String compA = interpretBlocks(element, true);
					String nextEle = nextElement(remainder.trim().substring(1));
					String compB = interpretBlocks(nextEle, true);
					
					if (compA.trim().equals(compB.trim())) // only if true
						interpretBlocks(remainder.trim().substring(1+nextEle.length()), false); // interpret the following statement
					return("");
				}
				else
				{
					variables.add(element, interpretBlocks(remainder.trim().substring(1), true));
					return("");
				}
			}
		}
		
		
		if (variables.hasVariable(block))
			return(variables.getVariable(block).trim());
		
		
		return(block);
	}
	
	
	
	String limitersRegex = " |(|=|,";
	
	private String nextElement(String str)
	{
		str = str.trim();
		if (str.charAt(0) == '"')
			return(str.substring(0, str.substring(1).indexOf("\"")+1)); 
		
		return(str.substring(0, str.indexOf(limitersRegex)).trim());
	}
	
	private String nextParameter(String str) // TODO: Ensure that ',' inside additional sets of brackets are ignore (functionc all insdie fucntion call)
	{
		str = str.trim();
		if (str.charAt(0) == '"')
			return(str.substring(0, str.substring(1).indexOf("\"")+1)); 
		
		return(str.substring(0, str.indexOf(",|)")).trim()); // search till the next occurrence of the parameter separator or the closing bracket
	}
	
	private Pair<Integer, List<String>> getParameters(String parametersString)
	{
		parametersString = parametersString.trim().substring(1).trim();
		
		String remainder = parametersString;
		
		List<String> params = new ArrayList<>();
		
		int len = 1;
		
		do
		{
			String param = nextParameter(remainder);
			params.add(param);
			len += param.length();
			remainder = remainder.substring(param.length()+1);
		}
		while(!remainder.isEmpty());
		
		return(new Pair<Integer, List<String>>(len, params));
	}
	
	
}
