package productionGUI.targetConnection;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

import execution.Execution;
import execution.handlers.InfoErrorHandler;
import main.MainControler;
import productionGUI.ProductionGUI;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;
import staticHelpers.LocationPreparator;

public class ConnectedLocal extends TargetConnection
{
	
	public ConnectedLocal(String destinationdirectory, String resSourcedirectory)
	{
		super(destinationdirectory, resSourcedirectory);
	}
	
	public ConnectedLocal(Map<String, String> settings)
	{
		super(settings.getOrDefault("destinationDirectory", ""), settings.getOrDefault("resSourceDirectory", ""));
	}
	

	@Override
	public boolean testDeploy()
	{
		if (!checkDestinationDirectory())
			return(false);
		
		if (!checkSourceDirectory())
			return(false);		
		
		return(true);
	}
	
	@Override
	public boolean directoryIsValid(String destinationPath)
	{
		File dir = new File(destinationPath);
		return( dir.exists() && (dir.isDirectory()) );
	}
	

	@Override
	public void makeMissingDirectories(String destinationPath)
	{
		(new File(destinationPath)).mkdirs();
	}
	
	
	

	@Override
	public boolean deploy()
	{
		return(performDeploy());
	}
	
	

	private File deployedRunner = null;
	private File deployedProgramDocument = null;
	

	@Override
	public void resetEmptyDestinationDirectory()
	{
		destinationDirectory = LocationPreparator.getRunnerFileDirectory();
		destinationDirectory = FileHelpers.resolveUniversalFilePath(destinationDirectory);

		if (!destinationDirectory.endsWith(File.separator))
			destinationDirectory += File.separator;
	}

	@Override
	public void copyDirectory(String sourceResPath, String targetPath)
	{
		try
		{
			FileUtils.copyDirectory(new File(sourceResPath), new File(targetPath));
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	@Override
	protected void deployRunner(File originalRunnerFile, boolean direct)
	{
		if (direct)
		{
			String destDir = destinationDirectory;
			if (!destDir.endsWith(File.separator))
				destDir = destDir + File.separator;
			
			deployedRunner = new File(destDir + GlobalSettings.deployedExecutorName);
			return;
		}
		
		deployedRunner = getRunnerAtDestination(originalRunnerFile);
		
		if (deployedRunner == null) // No runner found at the destination directory
		{
			InfoErrorHandler.printEnvironmentInfoMessage("The runner is not present at the deployment target yet. Copying now.");
			
			String destDir = destinationDirectory;
			if (!destDir.endsWith(File.separator))
				destDir = destDir + File.separator;

			// Deploy the runner			
			deployedRunner = new File(destDir+GlobalSettings.deployedExecutorName);
			
			try {
				Files.copy(originalRunnerFile, deployedRunner); // Copy the executor file
			} catch (IOException e) { e.printStackTrace(); }
		}
		else
			InfoErrorHandler.printEnvironmentInfoMessage("The runner is already present at the deployment target.");
	}


	@Override
	protected void deployDocument()
	{
		// Deploy the document
		String destDir = destinationDirectory;
		if (!destDir.endsWith(File.separator))
			destDir = destDir + File.separator;
		
		deployedProgramDocument = new File(destDir + ProductionGUI.getCurrentDocumentNameOrUnsaved());
		
		try {
			Files.copy(new File(ProductionGUI.getCurrentTempFile()), deployedProgramDocument); // Copy the program document
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	
	
	private File getRunnerAtDestination(File originalRunnerFile)
	{
		
		long origFileSize = originalRunnerFile.length();
		long origFileModified = originalRunnerFile.lastModified();
		
		List<File> files = new ArrayList<File>();
		FileHelpers.listFiles(destinationDirectory, files, false, ".jar");
		
		if (files.isEmpty())
			return(null);
		

		for (Iterator<File> iterator = files.iterator(); iterator.hasNext();)
		{
			File f = iterator.next();
			if ((f.length() != origFileSize) || (f.lastModified() != origFileModified))
				iterator.remove();
		}
		
		if (files.isEmpty())
			return(null);
		else
			return(files.get(0));		
	}
	
	
	
	
	
	private Process process = null;
	

	@Override
	public void launchDeployed(String additionalKeys)
	{
		executeAndHandleDeployed(additionalKeys);
	}
	
	@Override
	public void executeAndHandleDeployed(String additionalKeys)
	{
		String command = "java -jar \"" + deployedRunner.getPath() + "\" \"" + deployedProgramDocument.getPath() + "\" " + additionalKeys;
		
		Runtime rt = Runtime.getRuntime();
		
		InfoErrorHandler.printExecutionInfoMessage("-----STARTING DEPLOYED DOCUMENT-----");

		
		File signal = new File(getRunningSignalizerFile());
		try {
			signal.createNewFile(); // Create the "RUNNING" file
		} catch (IOException e1) { e1.printStackTrace(); }
		
		new Thread(() ->
		{
			isExternallyRunning = true;
			
			Execution.prepareConsole("Deployed Program Output (Console)");
			try
			{
				process = rt.exec(command);
				Execution.finalizeConsole(process.getInputStream(), process.getErrorStream());
				
				InfoErrorHandler.printExecutionInfoMessage("Wait for finishing external program process.");
				
				try {
					process.waitFor();
				} catch (InterruptedException e) { e.printStackTrace();	}
				
				InfoErrorHandler.printExecutionInfoMessage("External program process had ended.");
				
				signalizeProcessEnd();
			}
			catch (IOException e) { signalizeProcessEnd(); e.printStackTrace(); }
			
			signal.delete();
			
		}).start();
		
	}
	

	@Override
	public void forceQuit()
	{
		if (isExternallyRunning)
		{
			File signal = new File(getRunningSignalizerFile());
			signal.delete();
			
			new Thread(() ->
			{
				try {
					Thread.sleep((long) (GlobalSettings.runningCheckInterval*1.5));
				} catch (InterruptedException e) {}
				if (process != null)
					process.destroyForcibly();
			}).start();
		}
		isExternallyRunning = false;
	}
	

	
	@Override
	public Map<String, String> getSaveableSettings()
	{
		Map<String, String> settings = new HashMap<>();
		
		settings.put("destinationDirectory", destinationDirectory);
		settings.put("resSourceDirectory", resSourceDirectory);
		
		return(settings);
	}

	
}
