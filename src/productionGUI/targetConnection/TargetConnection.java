package productionGUI.targetConnection;

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

import dataTypes.DataNode;
import dataTypes.ProgramElement;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import execution.handlers.VariableHandler;
import productionGUI.ProductionGUI;
import productionGUI.controlers.ButtonsRegionControl;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;
import staticHelpers.GuiMsgHelper;
import staticHelpers.LocationPreparator;

public abstract class TargetConnection
{
	protected String destinationDirectory = "";
	protected String resSourceDirectory = "";
	
	protected volatile boolean isExternallyRunning;
	
	protected boolean isExternalConnection;
	
	public TargetConnection(String destinationDirectory, String resSourceDirectory)
	{
		this.destinationDirectory = FileHelpers.resolveUniversalFilePath(destinationDirectory);
		this.resSourceDirectory = FileHelpers.resolveUniversalFilePath(resSourceDirectory);
		
		isExternalConnection = false;
	}
	
	
	public String getDestinationDirectory()
	{
		return(destinationDirectory);
	}
	
	public String getResSourceDirectory()
	{
		return(resSourceDirectory);
	}
	
	
	protected boolean hasProgram() // Todo: Change into hasWorkingProgram and make response dependent on the last simulation. Perhaps only warn if the last simulation gave errors.
	{
		for(String page: ProductionGUI.getVisualizedProgram().getPages())
		{
			DataNode<ProgramElement> rootNode = ProductionGUI.getVisualizedProgram().getPageRoot(page); // Get the root-node for every page
			
			if (!rootNode.hasChildrenHidden() && ((ContentsSectionManager.getSelf()==null) || ContentsSectionManager.getSelf().pageIsActive(page))) // if the page is active and not deactivated
				if (!rootNode.isLeaf()) return(true);
		}
		
		return(false);
	}
	
	public abstract boolean testDeploy();
	public abstract boolean deploy();
	public abstract void launchDeployed(String additionalKeys);
	public abstract void executeAndHandleDeployed(String additionalKeys);
	
	public abstract Map<String, String> getSaveableSettings();
	//public abstract void readSaveableSettings(Map<String, String> settings);
	
	
	public abstract boolean directoryIsValid(String destinationPath);
	public abstract void makeMissingDirectories(String destinationPath);
	public abstract void resetEmptyDestinationDirectory();
	public abstract void copyDirectory(String sourcePath, String targetPath);
	public abstract void forceQuit();
	
	protected abstract void deployRunner(File originalRunnerFile, boolean direct);
	protected abstract void deployDocument();
	
	
	protected boolean checkDestinationDirectory()
	{
		if (destinationDirectory.isEmpty())
			return(true);

		
		String destinationPath = "";
		
		String[] pathParts = destinationDirectory.split("\\"+File.separator);

		int partsCount = pathParts.length;
		int checkIndex = partsCount;
		
		String testDirectory = "";
		
		do
		{
			/*
			if (checkIndex < 0)
			{
				InfoErrorHandler.showProblemMessage("The destination directory path is not valid!\nGiven path: " + destinationDirectory);
				return(false);
			}*/
			
			if (checkIndex < 0)
				break;
			
			testDirectory = "";
			for(int i = 0; i < checkIndex; i++)
			{
				testDirectory += pathParts[i];
				if (i < (checkIndex-1))
					testDirectory += File.separator;
			}
			
			destinationPath = testDirectory;
			
			checkIndex--;
		}
		while(!directoryIsValid(testDirectory));
		
			
		if (checkIndex < pathParts.length-1)
		{
			if (GuiMsgHelper.askQuestionDirect("The given destination directory path is not leading to an existing directory.\nFull given path: " + destinationDirectory + "\nValid part of the path: " + testDirectory + "\nDo you want to create the missing directory structure (" + (partsCount-(checkIndex+1)) + " new)?") >= 1)
			{
				makeMissingDirectories(destinationDirectory);
			}
			else
				return(false);
		}
		else
			destinationDirectory = destinationPath;
		
		return(true);
	}
	
	public boolean checkSourceDirectory()
	{
		if (resSourceDirectory.isEmpty())
			return(true);

		File resSourcePath = new File(resSourceDirectory);
			
		if ((!resSourcePath.exists()) || (!resSourcePath.isDirectory()))
		{
			GuiMsgHelper.showInfoMessage("The resource directory path is not valid!\nGiven path: " + resSourceDirectory);
			//InfoErrorHandler.showProblemMessage("The resource directory path is not valid!\nGiven path: " + resSourceDirectory);
			return(false);
		}
		
		return(true);
	}
	
	

	
	
	
	public boolean performDeploy()
	{
		VariableHandler.clearVariables();
		
		if (!hasProgram())
		{
			GuiMsgHelper.showInfoMessage("The current program is empty or all pages are disabled!");
			return(false);
		}
		
		
		File originalRunnerFile = LocationPreparator.getOriginalRunner();
		
		if (destinationDirectory.isEmpty())
			resetEmptyDestinationDirectory();
		else
			if (!destinationDirectory.endsWith(File.separator))
				destinationDirectory += File.separator;
		
		
		if (!resSourceDirectory.isEmpty()) // a resource folder is available
		{ // Copying the source folder first in case it contains the original runner
				copyDirectory(resSourceDirectory, destinationDirectory);
		}	
		
		if (LocationPreparator.isCompiled())
			deployRunner(originalRunnerFile, false);
		else
		{
			if (!GuiMsgHelper.askForContinue("The program is not running as a jar!\nTherefore the Executor could not be deployed.\nPlease ensure that you have copied it manually to the destination directory\nand named it '" + GlobalSettings.deployedExecutorName + "' before you continue."))
				return(false);
			
			deployRunner(originalRunnerFile, true);
		}
		
		deployDocument();
		
		
		InfoErrorHandler.printEnvironmentInfoMessage("Deployment has been successful.");
	
		return(true);

	}

	
	
	




	static String fileName = "DeploymentData.dat";
	
	public static String getFileName()
	{
		return(fileName);
	}
	
	public static void writeDeploymentSettings(TargetConnection connection, String environmentFileDirectory)
	{
		if (!environmentFileDirectory.endsWith(File.separator))
			environmentFileDirectory = environmentFileDirectory + File.separator;
		
		try {
			
			FileWriter fileWriter = new FileWriter(environmentFileDirectory + fileName);
		
	        BufferedWriter bufferedWriter =
	            new BufferedWriter(fileWriter);
	        
	        for(Entry<String, String> setting: connection.getSaveableSettings().entrySet())
	        {
	        	bufferedWriter.write(setting.getKey());
		        bufferedWriter.newLine();
	        	bufferedWriter.write(setting.getValue());
		        bufferedWriter.newLine();
	        }
	        
	        bufferedWriter.close();
	        
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	public static TargetConnection readDeploymentSettings(String environmentFileDirectory)
	{
		if (!environmentFileDirectory.endsWith(File.separator))
			environmentFileDirectory = environmentFileDirectory + File.separator;
		
		if (!FileHelpers.fileExists(environmentFileDirectory + fileName))
			return(null);
		
		List<String> lines = null;
		Map<String, String> settings = new HashMap<>();
		
		
		lines = FileHelpers.readAllLines(new File(environmentFileDirectory + fileName));
		
		for(int i = 0; i < lines.size(); i+=2)
			settings.put(lines.get(i), lines.get(i+1));			
		
		
		
		TargetConnection connect;
		
		if (settings.size() == 2)
			connect = new ConnectedLocal(settings);
		else
			connect = new ConnectedExternalLinux(settings);
		
		return(connect);
	}
	
	
	protected String getRunningSignalizerFile()
	{
		String destFile = destinationDirectory;
		if (!destFile.endsWith(File.separator))
			destFile = destFile + File.separator;
		return(destFile+"RUNNING");
	}
	
	
	
	public boolean isRunning()
	{
		return(isExternallyRunning);
	}


	protected void signalizeProcessEnd()
	{
		//if (isExternallyRunning)
		Execution.finishedExternalExecution();
		
		isExternallyRunning = false;
		ButtonsRegionControl.getSelf().switchToStandardButtons();
	}

	
	public boolean isExternal()
	{
		return(isExternalConnection);
	}
	
}
