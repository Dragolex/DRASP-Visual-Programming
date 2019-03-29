package settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import execution.handlers.InfoErrorHandler;
import javafx.stage.FileChooser;
import productionGUI.ProductionGUI;
import staticHelpers.FileHelpers;
import staticHelpers.LocationPreparator;

public class EnvironmentDataHandler
{
	private final static String notset = "NOT SET";
	
	private static String lastUsedFile = notset;
	private static String lastOpenedPath = notset;
	
	
	private static String fullPath;
	private static String fileName = "LastSettings.dat";
	
	private static String javaFileEditorPath = " ", javaFileEditorType = "NOTSET";
	
	
	public static String getFileName()
	{
		return(fileName);
	}
	

	public static void init()
	{
		/*
		try {
			filePath = MainControler.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			File f = new File(filePath);
			filePath = f.getParent();
		} catch (URISyntaxException e) {e.printStackTrace();}
		filePath = FileHelper.resolveUniversalFilePath(filePath);
		
		// ClassLoader.getSystemClassLoader().getResource(".").getPath();
		
		//if (filePath.startsWith("/"))
			//filePath = filePath.substring(1);
		
		if (!filePath.endsWith(File.separator))
			filePath = filePath + File.separator;
		*/
		
		fullPath = FileHelpers.addSubfile(LocationPreparator.getExternalDirectory(), fileName);
		
		
		File f = new File(fullPath);
		if((!f.exists()) || f.isDirectory())
		{
			InfoErrorHandler.printEnvironmentInfoMessage("No settings file found at: " + fullPath + "\nCreating new settings file.");
			writeFile(fullPath);
		}
		
		handleFile(fullPath);		
	}
	

	private static void writeFile(String path)
	{
		try {
			
			FileWriter fileWriter = new FileWriter(path);


	        // Always wrap FileWriter in BufferedWriter.
	        BufferedWriter bufferedWriter =
	            new BufferedWriter(fileWriter);
	        
	
	        bufferedWriter.write("Last used file:");
	        bufferedWriter.newLine();
	        bufferedWriter.write(lastUsedFile);
	        bufferedWriter.newLine();
	        bufferedWriter.write("Last opened path:");
	        bufferedWriter.newLine();
	        bufferedWriter.write(lastOpenedPath);
	        bufferedWriter.newLine();
	        bufferedWriter.write("Default Java editor:");
	        bufferedWriter.newLine();
	        bufferedWriter.write(javaFileEditorType);
  	        bufferedWriter.newLine();
   	        bufferedWriter.write(javaFileEditorPath);
	        bufferedWriter.newLine();

	        
	        bufferedWriter.write("Settings:");
	        bufferedWriter.newLine();
	        
	        
	        for(Entry<String, String> setting: GlobalSettings.getSaveableSettings().entrySet())
	        {
	        	bufferedWriter.write(setting.getKey());
		        bufferedWriter.newLine();
	        	bufferedWriter.write(setting.getValue());
		        bufferedWriter.newLine();
	        }	        
	        
	        // Always close files.
	        bufferedWriter.close();
	        
        
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	
	private static void handleFile(String path)
	{

		List<String> lines = null;

		lines = FileHelpers.readAllLines(new File(path));
		if (lines.size() < 4)
			return;
		
		int settingsInd = 5;
		
		lastUsedFile = lines.get(1);
		lastOpenedPath = lines.get(3);
		
		if (!lines.get(4).equals("Settings:"))
		{
			javaFileEditorType = lines.get(5);
			javaFileEditorPath = lines.get(6);
			settingsInd += 3;
		}
		
		Map<String, String> settings = new HashMap<>();
		
		for(int i = settingsInd; i < lines.size(); i+=2)
			settings.put(lines.get(i), lines.get(i+1));
		
		GlobalSettings.readSaveableSettings(settings);

		
	}
	
	
	public static void setLastUsedFile(String file)
	{
		lastUsedFile = file;
		
		System.out.println("------- LAST USED FILE TO: " + file);
		
		writeFile(fullPath);
	}
	public static void setLastOpenedPath(String path)
	{
		lastOpenedPath = path;
		writeFile(fullPath);
	}
	
	public static void rewriteSettings()
	{
		writeFile(fullPath);
	}
	
	
	public static String getLastUsedFile()
	{
		if (lastUsedFile.equals(notset))
			return("");
		
		return(lastUsedFile);
	}
	public static String getLastOpenedPath()
	{
		if (!(new File(lastOpenedPath).exists())) // file or dir not existing anymore
			lastOpenedPath = notset;
		
		if (lastOpenedPath.equals(notset))
			return("");
		
		return(lastOpenedPath);
	}


	public static boolean lastFileFullExists()
	{
		String filePath = getLastFileFull();
		if (filePath.isEmpty()) return(false);
		
		File file = new File(filePath);
			
		return(file.exists());
	}
	
	public static String getLastFileFull()
	{
		String lastPath = EnvironmentDataHandler.getLastOpenedPath();
		if (lastPath.isEmpty()) return("");
		String lastFile  = EnvironmentDataHandler.getLastUsedFile();
		if (lastFile.isEmpty()) return("");
		return(lastPath + File.separator + lastFile);
	}


	
	
	public static void setFileEditorRunnable(String javaFileEditorPath, String javaFileEditorType)
	{
		EnvironmentDataHandler.javaFileEditorPath = javaFileEditorPath;
		EnvironmentDataHandler.javaFileEditorType = javaFileEditorType;
		
		rewriteSettings();
	}
	public static String getFileEditorRunnable()
	{
		return(javaFileEditorPath);		
	}

	public static String getFileEditorType()
	{
		return(javaFileEditorType);
	}
	
}
