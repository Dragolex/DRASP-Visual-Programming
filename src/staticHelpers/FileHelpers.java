package staticHelpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import execution.Execution;
import execution.handlers.InfoErrorHandler;

/**
 * Provides simple functions to write and read in section-based files
 *
 * @author Alexander Georgescu
 */
public class FileHelpers {

	private final static String sectionSalt = "#:#";


	/**
	 * Read the section of a line
	 *
	 * @param filePath
	 * @param section
	 * @param subline
	 * @return
	 */
	static public String readFileSection(String filePath, String section, int subline)
	{
		if (fileExists(filePath))
		{
			List<String> lines = fileReadLines(filePath);

			for(int i = 0; i < lines.size(); i++)
			{
				if (lines.get(i).equals(sectionSalt+section))
					if (i+1+subline < lines.size())
						return(lines.get(i+1+subline));
			}
		}
		return("");
	}


	/**
	 * Write the a named section of a file
	 *
	 * @param filePath
	 * @param section
	 * @param subline
	 * @param content
	 * @return
	 */
	static public String writeFileSection(String filePath, String section, int subline, String content)
	{
		List<String> lines;

		if (fileExists(filePath))
			lines = fileReadLines(filePath);
		else
			lines = new ArrayList<String>();

		int targetLine = -1;

		// Find section
		for(int i = 0; i < lines.size(); i++)
		{
			if (lines.get(i).equals(sectionSalt+section))
				targetLine = i;
		}

		// No section found
		if (targetLine == -1)
		{
			lines.add(sectionSalt+section);
			for(int i = 0; i < subline; i++)
				lines.add("");
			lines.add(content);
		}
		else // Section found
		if (targetLine+1+subline < lines.size())
			lines.set(targetLine+1+subline, content); // change content
		else lines.add(content);


		// Write all lines back
		try {
			fileWriteLines(filePath, lines);
		} catch (IOException e) {e.printStackTrace();}

		return("");
	}




	/**
	 * Read an entire file into a list of lines
	 *
	 * @param filePath File to read
	 * @return List with lines as elements
	 */
	public static List<String> fileReadLines(String filePath)
	{
		List<String> lines = new ArrayList<String>();

		if (fileExists(filePath))
		{
			try {
				BufferedReader br = new BufferedReader(new FileReader(filePath));
				String line = null;
				while((line = br.readLine()) != null)
					lines.add(line);

				br.close();
			} catch (IOException e) {e.printStackTrace();}
		}
		return(lines);
	}


	/**
	 * Write a list of lines into a file
	 *
	 * @param filePath Path of the file to write into
	 * @param lines List with content
	 * @throws IOException
	 */
	public static void fileWriteLines(String filePath, List<String> lines) throws IOException
	{
		if ("".equals(filePath)) return;

		FileWriter wr = new FileWriter(filePath);

		for(int i = 0; i < lines.size(); i++)
			wr.write(lines.get(i)+"\n");

		wr.close();
	}


	/**
	 * Whether file exists
	 *
	 * @param filePath Filepath to check
	 * @return
	 */
	public static boolean fileExists(String filePath)
	{
		File f = new File(filePath);
		return((f.exists()) && (!f.isDirectory()));
	}

	public static boolean directoryExists(String dirPath)
	{
		File f = new File(dirPath);
		return((f.exists()) && (f.isDirectory()));
	}

	
	public static void listFiles(String folder, List<File> files, boolean addSub)
	{
		listFiles(folder, files, addSub, true);
	}
	
	
	public static void listFiles(String folder, List<File> files, boolean addSub, boolean includeHidden)
	{
	    File directory = new File(folder);

	    // get all the files from a directory
	    File[] fList = directory.listFiles();
	    
	    if (fList != null && fList.length > 0)
	    for (File file : fList)
	    {
	    	if (!includeHidden && file.isHidden()) continue;
	    	
	    	if (file.isFile())
	            files.add(file);
	        else
        	if (file.isDirectory() && addSub)
        		listFiles(file.getAbsolutePath(), files, addSub, includeHidden);
	    }
	}
	
	public static void listDirectories(String folder, List<File> files, boolean addSub, boolean includeHidden)
	{
	    File directory = new File(folder);

	    // get all the files from a directory
	    File[] fList = directory.listFiles();
	    if (fList != null && fList.length > 0)
	    for (File file : fList)
	    {
	    	if (!includeHidden && file.isHidden()) continue;
	    	
	    	if (file.isDirectory())
	    	{	    		
	    		files.add(file);
	           	if (addSub)
	           		listDirectories(file.getAbsolutePath(), files, addSub, includeHidden);
	    	}
	    }
	}

	
	public static void listFiles(String folder, List<File> files, boolean addSub, String endsWith)
	{
		listFiles(folder, files, addSub, endsWith, true);
	}
	public static void listFiles(String folder, List<File> files, boolean addSub, String endsWith, boolean includeHidden)
	{
	    File directory = new File(folder);

	    // get all the files from a directory
	    File[] fList = directory.listFiles();
	    if (fList != null && fList.length > 0)
	    for (File file : fList)
	    {  
	    	if (!includeHidden && file.isHidden()) continue;
	    	
	    	if (file.isFile() && fileEndsWith(file.getPath(), endsWith))
	            files.add(file);
	        else
        	if (file.isDirectory() && addSub)
        		listFiles(file.getAbsolutePath(), files, addSub);
	    }
	}
	
	
	
	private static boolean fileEndsWith(String path, String endsWith)
	{
		if (endsWith.trim().isEmpty())
			return(true);
		
		boolean res = false;
		endsWith = endsWith.toLowerCase();
		
		for(String end: endsWith.split("\\|"))
			if (path.toLowerCase().endsWith(end))
				res = true;
		
		return(res);
	}


	public static String resolveUniversalFilePath(String path)
	{
		if (path.isEmpty()) return(path);
		
		if (!File.separator.equals("\\"))
			path = path.replaceAll("\\", File.separator);
		if (!File.separator.equals("/"))
			path = path.replaceAll("/", "\\"+File.separatorChar);
		
		if (path.startsWith(File.separator))
			path = path.substring(1);
		
		return(path);
	}


	public static String adaptPathForLinux(String destinationPath)
	{
		return(destinationPath.replaceAll("\\"+File.separator, "/"));
	}


	public static String addSubfile(String root, String subfile)
	{
		if (!root.endsWith(File.separator))
			return(root + File.separator + subfile);
		else
			return(root + subfile);
	}


	public static void writeLineListToFile(String filePath, List<String> lines, String error)
	{

        try {
        	
        	FileOutputStream outStream = new FileOutputStream(filePath);
        	OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, StandardCharsets.UTF_8);
        	
			BufferedWriter bufferedWriter = new BufferedWriter(outStreamWriter);
		
			//FileWriter fileWriter = new FileWriter(filePath);
			
	        //BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	        
			
			for(String line: lines)
			{
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}
			
			bufferedWriter.close();
			outStreamWriter.close();
			outStream.close();
			
		} catch (IOException e)
        {
			error += "\nReason: " + e.getMessage();
			
			if (Execution.isRunning())
			{
				Execution.setError(error, false);
				InfoErrorHandler.callEnvironmentError(error);
			}
			else
				InfoErrorHandler.callEnvironmentError(error);
		}
        
	}


	public static List<String> readAllLines(File file)
	{
		List<String> allLines = new ArrayList<>();		
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				allLines.add(line);
				
				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			InfoErrorHandler.callEnvironmentError("Reading the following file failed: " + file.getAbsolutePath());
			return(null);
		}
		
		/*
		// For whatever reason, the fast method below is failing sometimes for unknown reasons
		 
		Path pt = FileSystems.getDefault().getPath(file.getPath());
		
		try {
			allLines = Files.readAllLines(pt);
		} catch (IOException e) {}
		*/
		
		return(allLines);
	}


	public static String convertIfExternal(String filePath)
	{
		
		if (LocationPreparator.isCompiled())
			return(filePath);
		
		if (!((new File(filePath)).isAbsolute())) // is a relative path
		{
			if (filePath.startsWith(File.separator))
				return("resources" + filePath);
			else
				return(File.separator +"resources" + File.separator + filePath);
		}
		
		return(filePath);
	}


	public static void fileDeleteChecked(String fileString)
	{
		File file = new File(fileString);
		
		try
		{
			if (file.exists())
				Files.deleteIfExists(file.toPath());
		}
		catch (IOException e)
		{
			InfoErrorHandler.printMinorExecutionErrorMessage("Deleting the file '" + fileString + "' failed! Probably it is in use by an external program.");
		}		
	}


	public static void copy(String pathA, String pathB) throws IOException
	{
		Files.copy(new File(pathA).toPath(), new File(pathB).toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	public static void copyChecked(String pathA, String pathB)
	{
		try {
			copy(pathA, pathB);
		} catch (IOException e)
		{
			InfoErrorHandler.printMinorExecutionErrorMessage("Copying the file \n'" + pathA + "'\nto\n'" + pathB +"'\nfailed!");
		}
	}
	
	
}
