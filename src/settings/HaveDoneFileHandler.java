package settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import execution.handlers.InfoErrorHandler;
import staticHelpers.FileHelpers;
import staticHelpers.LocationPreparator;

public class HaveDoneFileHandler
{
	private static String fullPath;
	private static String fileName = "HaveDone.dat";
	
	private static List<String> haveDoneStrings = new ArrayList<String>();
	
	public static String getFileName()
	{
		return(fileName);
	}
	

	public static void init()
	{
		fullPath = FileHelpers.addSubfile(LocationPreparator.getExternalDirectory(), fileName);		
		
		File f = new File(fullPath);
		if((!f.exists()) || f.isDirectory())
		{
			InfoErrorHandler.printEnvironmentInfoMessage("No HaveDone.dat file found at: " + fullPath + "\nCreating new file.");
			writeFile(fullPath);
		}
		
		handleFile(fullPath);		
	}
	

	private static void writeFile(String path)
	{
		try {
			
			FileWriter fileWriter = new FileWriter(path);

	        // Wrap FileWriter in BufferedWriter.
	        BufferedWriter bufferedWriter =
	            new BufferedWriter(fileWriter);
	        
	        for(String str: haveDoneStrings)
	        {
	        	bufferedWriter.write(str);
		        bufferedWriter.newLine();
	        }	        
	        
	        // Close files.
	        bufferedWriter.close();
        
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	
	private static void handleFile(String path)
	{

		haveDoneStrings = FileHelpers.readAllLines(new File(path));
	
	}
	

	public static boolean haveDone(String string, boolean markDown)
	{
		if (haveDoneStrings.contains(string))
			return(true);
		
		if (markDown)
		{
			haveDoneStrings.add(string);
			writeFile(fullPath);
		}
		
		return(false);
	}

	
}
