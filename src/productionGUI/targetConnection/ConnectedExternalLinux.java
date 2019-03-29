package productionGUI.targetConnection;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import execution.Execution;
import execution.handlers.InfoErrorHandler;
import main.functionality.helperControlers.network.ImplementJSch;
import productionGUI.ProductionGUI;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;
import staticHelpers.GuiMsgHelper;

public class ConnectedExternalLinux extends TargetConnection
{
	String username = "";
	String hostname = "";
	String password = "";
	
	
	public ConnectedExternalLinux(String destinationFolder, String resSourceFolder, String username, String hostname, String password)
	{
		super(destinationFolder, resSourceFolder);
		
		this.username = username;
		this.hostname = hostname;
		this.password = password;
		
		isExternalConnection = true;
	}

	public ConnectedExternalLinux(Map<String, String> settings)
	{
		super(settings.getOrDefault("destinationDirectory", ""), settings.getOrDefault("resSourceDirectory", ""));
		username = settings.getOrDefault("username", "");
		hostname = settings.getOrDefault("hostname", "");
		password = settings.getOrDefault("password", "");
		
		isExternalConnection = true;
	}
	
	
	
	public ImplementJSch jsh;
	
	@Override
	public boolean testDeploy()
	{
		if (!establishConnection())
			return(false);
		
		if (!checkDestinationDirectory())
		{
			jsh.close();
			return(false);
		}
		
		if (!checkSourceDirectory())
		{
			jsh.close();
			return(false);		
		}
		
		jsh.close();
		return(true);
	}
	
	
	// returns empty or error 
	public String testConnection()
	{
		if (jsh != null)
			jsh.close();
		
		jsh = new ImplementJSch(username, hostname, password);
		
		String response = jsh.connectSession();
		
		if (!response.isEmpty())
		{
			jsh.close();
			return(response);
		}
		
		jsh.close();
		return("");
	}

	
	
	public boolean establishConnection()
	{
		if (jsh != null)
			jsh.close();
		
		jsh = new ImplementJSch(username, hostname, password);
		
		String response = jsh.connectSession();
		
		if (!response.isEmpty())
		{
			GuiMsgHelper.showInfoMessage(response);
			//InfoErrorHandler.showProblemMessage(response);
			jsh.close();
			return(false);
		}
		else return(true);
	}

	@Override
	public boolean directoryIsValid(String destinationPath)
	{
		destinationPath = FileHelpers.adaptPathForLinux(destinationPath);
		String response = jsh.executeCommandAndRetrieve("ls \"" + destinationPath + "\"");
		if (response == null) return(false);
		return(!response.contains("No such file or directory"));
	}
	
	
	@Override
	public void makeMissingDirectories(String destinationPath)
	{
		destinationPath = FileHelpers.adaptPathForLinux(destinationPath);
		jsh.executeCommandAndRetrieve("mkdir -p \"" + destinationPath + "\"");
	}
	
	
	@Override
	public boolean deploy()
	{
		if (!establishConnection())
			return(false);
		
		if (!performDeploy())
		{
			jsh.close();
			return(false);
		}
		else
			return(true);
	}
	
	public void launchDeployed(String additionalKeys)
	{
		InfoErrorHandler.printExecutionInfoMessage("-----STARTING DEPLOYED PROGRAM-----");
		
		isExternallyRunning = true;

		deployedRunner = FileHelpers.adaptPathForLinux(deployedRunner);
		deployedProgramDocument = FileHelpers.adaptPathForLinux(deployedProgramDocument);
		
		
		jsh.executeCommandAndRetrieve("touch " + FileHelpers.adaptPathForLinux(getRunningSignalizerFile())); // Signalizes that system is running		


		//jsh.executeCommand("sudo java -Dpi4j.linking=dynamic -jar \"" + deployedRunner + "\" \"" + deployedProgramDocument + "\" " + additionalKeys);
		jsh.executeCommand("sudo java -jar \"" + deployedRunner + "\" \"" + deployedProgramDocument + "\" " + additionalKeys);
	}
	
	@Override
	public void executeAndHandleDeployed(String additionalKeys)
	{
		launchDeployed(additionalKeys);
		
		new Thread(() ->
		{
			Execution.prepareConsole("Deployed Program Output (Console)");
			
			Execution.finalizeConsole(jsh.getStandardStream(), jsh.getErrorStream());
			
			InfoErrorHandler.printExecutionInfoMessage("Wait for finishing external program process.");
			
			while(jsh.checkCommandRunning(true))
			{
				try {
					Thread.sleep(GlobalSettings.constantCheckDelay);
				} catch (InterruptedException e) { signalizeProcessEnd(); e.printStackTrace();	}
			}
			
			InfoErrorHandler.printExecutionInfoMessage("External program process has ended.");
			
			if (isExternallyRunning)
				signalizeStop();
			signalizeProcessEnd();
			
		}).start();
		
	}
	


	@Override
	public void forceQuit()
	{
		if (isExternallyRunning)
		{
			isExternallyRunning = false;
			signalizeStop();
			
			jsh.close();
		}
	}
	
	public void closeCommand()
	{
		if(jsh != null)
			jsh.close();
	}
	
	
	private void signalizeStop()
	{
		jsh.executeCommandAndRetrieve("rm -f " + FileHelpers.adaptPathForLinux(getRunningSignalizerFile())); // Signalize to stop
	}

	
	
	private String deployedRunner = null;
	private String deployedProgramDocument = null;

	@Override
	public void copyDirectory(String sourceResPath, String targetPath)
	{
		jsh.transferDirectory(sourceResPath, targetPath);
	}

	@Override
	protected void deployRunner(File originalRunnerFile, boolean direct)
	{
		if (direct)
		{
			String destDir = destinationDirectory;
			if (!destDir.endsWith(File.separator))
				destDir = destDir + File.separator;
			
			deployedRunner = destDir + GlobalSettings.deployedExecutorName;
			
			jsh.transferFile(originalRunnerFile.getPath(), deployedRunner);
			return;
		}
		
		deployedRunner = getRunnerAtDestination(originalRunnerFile);
		
		if (deployedRunner == null) // No runner found at the destination directory
		{
			InfoErrorHandler.printEnvironmentInfoMessage("The runner is not present at the deployment target yet. Transfering now.");
			
			String destDir = destinationDirectory;
			if (!destDir.endsWith(File.separator))
				destDir = destDir + File.separator;
			
			// Deploy the runner
			deployedRunner = destDir + GlobalSettings.deployedExecutorName;
			
			jsh.transferFile(originalRunnerFile.getPath(), deployedRunner); // Copy the executor file
		}
		else
			InfoErrorHandler.printEnvironmentInfoMessage("The runner is already present at the deployment target.");
	}
	
	
	private String getRunnerAtDestination(File originalRunnerFile)
	{
		long origFileSize = originalRunnerFile.length();
		long origFileModified = originalRunnerFile.lastModified();
		long now = new Date().getTime();
		origFileModified = (now - origFileModified)/1000;
		
		List<String> files = new ArrayList<String>();
		jsh.listFiles(destinationDirectory, files, ".jar", false);
		
		if (files.isEmpty())
			return(null);
		
		
		for (Iterator<String> iterator = files.iterator(); iterator.hasNext();)
		{
			String f = iterator.next();
			
			long fileSize = jsh.getFileSize(f);
			long fileModif = jsh.getLastFileModif(f);
			
			if ((fileSize != origFileSize) || (Math.abs(fileModif - origFileModified) > 30))
				iterator.remove();
		}
		
		if (files.isEmpty())
			return(null);
		else
			return(files.get(0));
	}
	

	@Override
	protected void deployDocument()
	{
		// Deploy the document
		String destDir = destinationDirectory;
		if (!destDir.endsWith(File.separator))
			destDir = destDir + File.separator;
		deployedProgramDocument = destDir + ProductionGUI.getCurrentDocumentNameOrUnsaved();
		
		jsh.transferFile(ProductionGUI.getCurrentTempFile(), deployedProgramDocument); // Copy the document file
	}
	
	
	
	
	@Override
	public void resetEmptyDestinationDirectory()
	{
		destinationDirectory = "";
	}
	
	
	
	
	@Override
	public Map<String, String> getSaveableSettings()
	{
		Map<String, String> settings = new HashMap<>();
		
		settings.put("destinationDirectory", destinationDirectory);
		settings.put("resSourceDirectory", resSourceDirectory);
		settings.put("username", username);
		settings.put("hostname", hostname);
		settings.put("password", password);
		
		return(settings);
	}
	
	
	public String getPassword()
	{
		return(password);
	}
	
	public String getUsername()
	{
		return(username);
	}

	public String getHostname()
	{
		return(hostname);
	}
	
	
	public String executeBashCommand(String bash)
	{
		if (!establishConnection())
			return(null);
		
		String res = jsh.executeCommandAndRetrieve(bash);
		
		jsh.close();
		
		return(res);
	}

	public ImplementJSch getJSH()
	{
		return(jsh);
	}
	
}
