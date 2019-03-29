package staticHelpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RunnableExporterB {

	public static String exportRunnableJar(String outputJarFile, String mainClass, String baseClassDir, String jarLibsDir, String jarinjarExportationDir, String exportHelpers)
	{
		
		List<File> additionalLibJars = new ArrayList<File>();
		
		listFiles(jarLibsDir, additionalLibJars, false, true);
		
		String manifestFile = "MANIFEST.MF";
		
		makeManifestWithExtLibs(manifestFile, mainClass, "libs", additionalLibJars);
		
		
		ProcessBuilder builder = new ProcessBuilder(exportHelpers+"\\jar", "cvmf", manifestFile, outputJarFile, 
																	"-C", baseClassDir, "/",
																	"-C", jarinjarExportationDir, "/",
																	"-C", ".", jarLibsDir);
		
		builder.redirectErrorStream(true);
		Process process;
		
		StringBuilder out = new StringBuilder();
		
		try {
			process = builder.start();
				
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line = null; 
	
	        while ((line = input.readLine()) != null) {
	        	out.append(line);
	        	out.append("\n");
	        }
        
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		// remove the manifest file
		File f = new File(manifestFile);
		f.delete();
		
		return(out.toString());
	}

	public final static char LF  = (char) 0x0A; 
	
	private static void makeManifestWithExtLibs(String filePath, String mainClass, String libraryBaseDirectory, List<File> libraryJarNames)
	{
		StringBuilder manifestContent = new StringBuilder("Manifest-Version: 1.0\r\n");

		// Add the Rsrc-Class-Path that consists of the lib files just separated by spaces
		manifestContent.append("Rsrc-Class-Path: ./ "+ LF + " ");
		
		for(File fl: libraryJarNames)
		{
			manifestContent.append(libraryBaseDirectory + "/" + fl.getName());
			manifestContent.append(" " + LF + " ");
		}
		
		// Remaining part of the file
		manifestContent.append("\r\nClass-Path: .\r\n");
		manifestContent.append("Rsrc-Main-Class: ");
		manifestContent.append(mainClass);
		manifestContent.append("\r\nMain-Class: org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader\r\n\r\n");
		
		
		// Writing the file down
		
		File file = new File(filePath);
        FileWriter fr = null;
        
        try {
        	
            fr = new FileWriter(file);
            fr.write(manifestContent.toString());
            
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
        try
        {
                fr.close();
        } catch (IOException e) {} }
		
		
	}
	
	
	
	private static void listFiles(String folder, List<File> files, boolean addSub, boolean includeHidden)
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
	
}
