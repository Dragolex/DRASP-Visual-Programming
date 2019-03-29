package execution.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import main.functionality.helperControlers.network.ImplementJSch;
import miniLang.FunctionBase;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;

public class ToolsDatabase {
	
	private static Map<String, File> toolFile = new HashMap<>();	
	private static Map<String, String> toolDescription = new HashMap<>();

	
	
	public static void readToolsDirectory(String toolsDirectory) 
	{
		toolFile.clear();
		toolDescription.clear();
		
		List<File> files = new ArrayList<>();
		
		FileHelpers.listFiles(toolsDirectory, files, true, GlobalSettings.standardProgramFileTermination); // read all tool files
		
		for(File f: files)
			addTool(f);
	}
	
	public static void addTool(File file)
	{
		List<String> allLines = FileHelpers.readAllLines(file);
		
		String commentIdentifier = "StComEv"; // Todo: Get this from the Functionality class
		
		String description = "";
		
		for(int i = 0; i < allLines.size(); i++)
		{
			if (allLines.get(i).contains(commentIdentifier)) // Search for the first comment Block
			{
				description = allLines.get(i+1); // The content line
				description = description.substring(description.indexOf(":")+1); // crop only the content of the comment
				break;
			}
		}
		
		String name = file.getName(); // name with termination
		name = name.substring(0, name.length()-GlobalSettings.standardProgramFileTermination.length()-1); // remove termination
		toolFile.put(name, file);
		toolDescription.put(name, description.replaceAll("\\|", "\n"));
	}
	
	
	public static Set<Entry<String, String>> getToolSet()
	{
		return(toolDescription.entrySet());
	}
	public static File getToolFile(String name)
	{
		return(toolFile.get(name));
	}

	
	
	private static Map<String, String> tools = new HashMap<>();

	
	
	public static void addTool(String name, String command)
	{
		tools.put(name, command);
	}
	
	
	public static void setI2Cconfig(boolean active)
	{
		executeLocally("sudo raspi-config nonint do_i2c " + ((active) ? "0" : "1"), false); // for whatever reason 0 activates it and 0 deactivates it		
	}

	public static boolean getI2Cconfig() {
		String res = execAndRetrieve("sudo raspi-config nonint get_i2c");
		if ("1".equals(res))
			return(false); // result of this raspi-config call is inverted for whatever reason
		if ("0".equals(res))
			return(true); // result of this raspi-config call is inverted for whatever reason
		
		InfoErrorHandler.callEnvironmentError("Raspi-Config does not seem accessible on this system!");		
		return(false);
	}

	
	
	public static void setup()
	{
		String[] lines = null;
		
		FunctionBase functions = FunctionBase.fromXMLLikeLines(lines, "[TOOL]");
		
		functions.add("MESSAGE", (List<String> parameters) -> {System.out.println("MESSAGE: " + parameters.get(0)); return("");} );
		functions.add("WAITFOR", (List<String> parameters) -> {System.out.println("WAITFOR: " + parameters.get(0)); return("");} );
	}
	
	
	
	public static void executeLocally(String command, boolean threaded)
	{
		if (threaded)
			new Thread(() ->
			{
				exec(command);
			}).start();
		else
			exec(command);
	}
	private static void exec(String command)
	{
		Runtime rt = Runtime.getRuntime();

		Process pr = null;
		try {
			pr = rt.exec(command);
		} catch (IOException e1) { e1.printStackTrace(); }
			
		try {
			pr.waitFor();
		} catch (InterruptedException e) { e.printStackTrace();	}
	}
	
	public static String execAndRetrieve(String command)
	{
		Runtime rt = Runtime.getRuntime();
		
		Process process = null;
		try {
			process = rt.exec(command);
		} catch (IOException e1) { System.out.println("Error: " + e1.getMessage()); e1.printStackTrace();}
		
		
		return(ImplementJSch.retrieveDoubleStream(process.getInputStream(), process.getErrorStream(), null, process));
	}
	
	public static String[] execAndRetrieveToArray(String command)
	{
		return(execAndRetrieve(command).split("\\r?\\n")); // Todo: Rewrite this without Split
	}

	


	
	
	
}
