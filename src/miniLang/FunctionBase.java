package miniLang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import execution.handlers.ToolsDatabase;

public class FunctionBase {
		
	public static FunctionBase fromXMLLikeLines(String[] lines, String keyword)
	{
		FunctionBase caller = new FunctionBase();
		
		int count = lines.length;
		
		for(int i = 0; i < count; i++)
		{
			String line = lines[i];
			
			if (line.startsWith(keyword))
			{
				String name = lines[++i];
				
				StringBuilder code = new StringBuilder();

				String next = lines[++i];
				
				while((i < count) && (!next.isEmpty()))
				{
					code.append(next);
					next = lines[++i];
				}
				
				if (!code.toString().isEmpty())
				{
					final String bash = code.toString();
					
					Function<List<String>, String> cal = (List<String> parameters)-> {return(ToolsDatabase.execAndRetrieve(applyParameters(bash, parameters)));};
					
					caller.add(name, cal);
				}
			}
		}
		
		return(caller);
	}
	

	
	private static String applyParameters(String bash, List<String> parameters)
	{
		int i = 0;
		for(String str: parameters)
		{
			bash = bash.replaceAll("#"+i, str);
		}
		
		return(bash);
	}
	


	private Map<String,  Function<List<String>, String> > callers = new HashMap<>();	
	

	
	public void add(String name, Function<List<String>, String> cal)
	{
		callers.put(name, cal);
	}


	public String execute(String callerName, List<String> parameters)
	{
		try {
			return(callers.get(callerName).apply(parameters));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return("");
	}
	
	

}
