package staticHelpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import execution.handlers.InfoErrorHandler;

public class RunnableExporterA {
	
	public static boolean exportRunnableJar(String outputJarFile, String workspaceDirPath, String antFilePath)
	{
		System.out.println("ANT FILE: " + antFilePath);
		List<String> origFileLines = FileHelpers.readAllLines(new File(antFilePath));
		
		int requiredReplaces = 3;
		
		for(int i = 0; i < origFileLines.size(); i++)
		{
			String line = origFileLines.get(i);
			
			if (line.startsWith("    <property name=\"dir.workspace\" value="))
			{
				line = "    <property name=\"dir.workspace\" value=\""+FileHelpers.adaptPathForLinux(workspaceDirPath)+"\"/>"; // alter the workspace value
				requiredReplaces--;
			}

			if (line.startsWith("        <jar destfile=\"${dir.jarfile}"))
			{
				line = "        <jar destfile=\"" + FileHelpers.adaptPathForLinux(outputJarFile) + "\" filesetmanifest=\"mergewithoutmain\">"; // alter the output jar file
				requiredReplaces--;
			}
			
			if (line.startsWith("            <fileset dir=\"${dir.workspace}"))
				if (line.endsWith("bin\"/>"))
				{
					String newLine = line.substring(0, line.length()-6) +  "resources\" includes=\"/functionality/**/*.java\"/>";
					origFileLines.add(i+1, newLine);

					newLine = line.substring(0, line.length()-6) +  "resources\" includes=\"/electronic/**/*.java\"/>";
					origFileLines.add(i+1, newLine);
				
					requiredReplaces--;
				}
			
			
			origFileLines.set(i, line);
		}
		
		String editedAntFilePath = antFilePath.substring(0, antFilePath.length()-4)+"_edited.xml";
		
		if (requiredReplaces != 0)
		{
			InfoErrorHandler.callEnvironmentError("The default ANT file is malformed!");
			return false;
		}
		
		try {
			FileHelpers.fileWriteLines(editedAntFilePath, origFileLines);
		} catch (IOException e) {
			InfoErrorHandler.callEnvironmentError("Saving the ANT file failed!\nDoes DRASP have writing rights for the file: " + editedAntFilePath + "?");
			return false;
		}
		
		
		System.out.println("Target file: " + editedAntFilePath);
		
		ProcessBuilder builder = new ProcessBuilder("cmd", "/c", "ant", "-buildfile", editedAntFilePath);
		

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
			InfoErrorHandler.callEnvironmentError("Executing ANT failed! Is it installed and added to the correct PATH environment variables?\nLine message:\n" + out.toString()+"\nError: " + e.getMessage());
			return false;
        }
		finally
		{
			InfoErrorHandler.printEnvironmentInfoMessage(out.toString());
			
			OtherHelpers.sleepNonException(500); // wait for ant to give the file free
			
			// remove the manifest file
			File f = new File(editedAntFilePath);
			f.delete();	
		}
		
		
		
		
		if (!FileHelpers.fileExists(outputJarFile))
		{
			InfoErrorHandler.callEnvironmentError("Executing ANT failed! Is it installed and added to the correct PATH environment variables?");
			return false;
		}
		
		/*
		// Find all Java Functionality files
		List<File> additionalLibJars = new ArrayList<File>();
		
		FileHelpers.listFiles(LocationPreparator.getFunctionalityDirectory(), additionalLibJars, true, true);
		FileHelpers.listFiles(LocationPreparator.getElectronicDirectory(), additionalLibJars, true, true);
		*/
		

		
		return(true);
	}

	
	//public static String addJavaSources(String outputJarFile, String workspaceDirPath, String antFilePath)
	//{
	//}

}
