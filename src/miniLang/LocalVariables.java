package miniLang;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LocalVariables {

	private Map<String, String> variables = new HashMap<>();
	
	public void add(String name, String content)
	{
		variables.put(name, content);
	}
	
	public String apply(String str)
	{
		for(Entry<String, String> ent: variables.entrySet())
		{
			str = str.replaceAll(ent.getKey(), ent.getValue());
		}
		
		return(str);
	}

	public boolean hasVariable(String element)
	{
		return(variables.containsKey(element));
	}
	public String getVariable(String element)
	{
		return(variables.get(element));
	}
	
}
