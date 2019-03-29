package functionality.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

import com.jcraft.jsch.Channel;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.LabelString;
import dataTypes.contentValueRepresentations.TextOrValueOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.FuncLabel;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.DataControler;
import main.functionality.Functionality;
import main.functionality.helperControlers.network.FTPhandlerStatic;
import main.functionality.helperControlers.network.ImplementJSch;
import productionGUI.additionalWindows.WaitPopup;
import productionGUI.sections.elements.VisualizableProgramElement;
import productionGUI.targetConnection.ConnectedExternalLinux;
import productionGUI.targetConnection.TargetConnection;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;
import staticHelpers.LocationPreparator;

public class Act04_ShellFTP extends Functionality {

	public static int POSITION = 4;
	public static String NAME = "Shell/Bash/URL/FTP";
	public static String IDENTIFIER = "ActShellFtpNode";
	public static String DESCRIPTION = "Actions to call shell commands locally or via network on linux and access web links and FTP servers.";
	
	
	public static ProgramElement create_ElExecCommand()
	{
		Object[] params = new Object[3];
		FunctionalityContent content[] = new FunctionalityContent[1];
		
		InputStream[] streams = new InputStream[2];
		Queue<String> outputQueue = new LinkedList<String>();
		Callable<Boolean>[] checkAndEndTask = new Callable[1];
		
		content[0] = new FunctionalityContent( "ElExecCommand",
				params,
				() -> {
					
						StringBuilder command = new StringBuilder();
						command.append((String) params[2]);
						
						outputQueue.clear();
						
						int extArgs = content[0].getTotalOptionalOrExpandedArgumentsArray().length;
						for(int i = 1; i < extArgs; i++)
						{
							command.append(" ");
							String argument = (String) content[0].getTotalOptionalOrExpandedArgumentsArray()[i];
							if (argument.contains(" "))
							{
								command.append("\"");
								command.append(argument);
								command.append("\"");
							}
							else
								command.append(argument);
						}
						
						//System.out.println("------- VAR NAME: " + VariableHandler.getVariableName((Variable) content[0].getOptionalArgumentValue(0)));
						TargetConnection possibleDestination = GlobalSettings.localDestination;
						
						if (content[0].hasOptionalArgument(0))
							possibleDestination = getExternalTargetFromVarOrLocal(content[0].getOptionalArgumentValue(0));
						
						if (possibleDestination == null)
							return;
						
						
						if (possibleDestination != GlobalSettings.localDestination) // if not local -> meant to execute on an external target
						{
							ConnectedExternalLinux destination = (ConnectedExternalLinux) possibleDestination;
							
								
							if (!destination.establishConnection())
							{
								Execution.setError("Connecting externally failed!\nTarget data correct and the hardware is online?", true);
								return;
							}
							
							Channel channel = destination.getJSH().executeCommandAndGetChannel(command.toString());
							
							try {
								streams[0] = channel.getInputStream(); // get the streams
								streams[1] = channel.getExtInputStream(); // both
							} catch (IOException e) {
								Execution.setError("Connecting externally failed!\nTarget data correct and the hardware is online?", true);
								channel.disconnect();
								destination.getJSH().close(); // create a runnable with closing function when the streams have ended
								return;
							}
							
							ConnectedExternalLinux extCon = destination;
							checkAndEndTask[0] = () -> {
								if(channel.isClosed())
								{
									channel.disconnect();
									extCon.getJSH().close(); // create a runnable with closing function when the streams have ended
									return(true);
								}
								return(false);
							};
	
						}
						else
						{
						try
						{
							Runtime rt = Runtime.getRuntime();
							Process process = rt.exec(command.toString());
							
							streams[0] = process.getInputStream();
							streams[1] = process.getErrorStream();
							
							checkAndEndTask[0] = () -> {
								return(!process.isAlive());
							};							

						}
						catch (IOException e) {
							Execution.setError("Could not execute the command: " + command.toString()+"\nException: " + e.getMessage(), false);
							}
						}						
					
					},
				() -> {
					
					Variable output = null;
					if (params[1] != null)
					{
						output = (Variable) params[1];
						if (output.isType(Variable.noType))
							output.initTypeAndSet(Variable.textType, "");
					}
					
					
					boolean waitForFinish = (boolean) params[0];
					
					if (!waitForFinish)
						return(false);

					
					if (!outputQueue.isEmpty())
					{
						while(outputQueue.peek().isEmpty() && !outputQueue.isEmpty())
		        			outputQueue.poll();
						
						if (outputQueue.isEmpty()) return(false);
						
						output.set(outputQueue.poll());
						return(true);
					}
					
					
					
					byte[] tmp=new byte[1024];
					boolean outputFound = false;
					
					
					//do
					{
						boolean ended = false;
						
						if (streams[0] == null || streams[1] == null)
							return(false);
				
						while(!ended && !outputFound && Execution.isRunning())
						{
							while(streams[0].available()>0 || streams[1].available()>0 && Execution.isRunning())
					        {
					        	if (streams[0].available()>0)
					        	{
							        int i=streams[0].read(tmp, 0, 1024);
							        
							        if (i>=0)
							        {
								        String str = new String(tmp, 0, i);
								        if (str.length() > 0)
								        {
								        	if (output != null)
								        	{
								        		String[] lines = str.split("\n|" + Character.toString((char)13));
								        		for(int j = 0; j < lines.length; j++)
								        			outputQueue.add(lines[j]);
								        		
								        		while(outputQueue.peek().isEmpty() && !outputQueue.isEmpty())
								        			outputQueue.poll();
								        		
								        		if (!outputQueue.isEmpty())
								        			output.set(outputQueue.poll());								        		
								        	}
								        	outputFound = true;
								        	break;
								        }							        
							        }
							        else {ended = true; break;}
					        	}
					        	
					        	
					        	if (streams[1].available()>0)
					        	{
							        int i=streams[1].read(tmp, 0, 1024);
							        
							        if (i>=0)
							        {
								        String str = new String(tmp, 0, i);
								        if (str.length() > 0)
								        {
								        	if (output != null)
								        	{
								        		String[] lines = str.split("\n|" + Character.toString((char)13));
								        		for(int j = 0; j < lines.length; j++)
								        			outputQueue.add(lines[j]);
								        		
								        		while(outputQueue.peek().isEmpty() && !outputQueue.isEmpty())
								        			outputQueue.poll();
								        		
								        		if (!outputQueue.isEmpty())
								        			output.set(outputQueue.poll());								        		
								        	}
								        	outputFound = true;
								        	break;
								        }							        
							        }
							        else {ended = true; break;}
					        	}
					        	
					        	if (!outputFound)
									Thread.sleep(GlobalSettings.constantCheckDelay);
					        }
						
							/*
							if (process[0] != null)
							if (!process[0].isAlive())
								break;
								*/
							if (checkAndEndTask[0].call())
								break;							

							Thread.sleep(GlobalSettings.constantCheckDelay);
						}
					}
					//while(/*waitForFinish &&*/ false && outputFound && process[0].isAlive());
					
					if (!outputQueue.isEmpty())
						return(true);
					
					return(outputFound);
					},
				() -> {
					
				});
		return(content[0]);
	}
	public static ProgramElement visualize_ElExecCommand(FunctionalityContent content)
	{
		VisualizableProgramElement elExecCommand;
		elExecCommand = new VisualizableProgramElement(content, "Execute Shell", "Execute a shell/bash command on the system or an external linux system (raspberry).\nThe command is composed from the 'Base Command' (which may include 'sudo')\nand all expanded arguments. If an argument contains a whitespace\nthe entire argument wil be surrounded by '\"' smybols.\n\nThis action optionaly works as a loop.\nChild elements are executed once for every out-line the command produces.\nThe line is placed in 'Output'.\nIf the argument 'Wait Finish' is false, the action will instantly continue (independant of child elements).");
		elExecCommand.setArgumentDescription(0, new BooleanOrVariable(), "Wait Finish");
		elExecCommand.setArgumentDescription(1, new VariableOnly(true, true), "Output");
		elExecCommand.setArgumentDescription(2, new TextOrVariable(), "Base Command");
		elExecCommand.addOptionalParameter(0, new VariableOnly("#LOCAL", true, false), "Target Computer", "Provide an external computer prepared with the 'External Computer' action to execute the command there by remote.\nDoing this does not require Drasp to run on the target system.\nUse '#LOCAL' to execute on the local system (default) and\n'#DEPLOY' to use the prepared deployment target from the options menu.\nNote that the later is not saved with the program document.");
		elExecCommand.setExpandableArgumentDescription(TextOrVariable.class, null, false, false, "Parameter #", 16, "Up to 16 appended parameters for the call.");
		return(elExecCommand);
	}
	
	
	
	
	
	
	
	@SuppressWarnings("deprecation")
	public static ProgramElement create_ElExecRemoteEvent()
	{
		Object[] params = new Object[4];
		FunctionalityContent content[] = new FunctionalityContent[1];
		
		InputStream[] streams = new InputStream[2];
		Queue<String> outputQueue = new LinkedList<String>();
		Callable<Boolean>[] checkAndEndTask = new Callable[1];
		
		//Process[] process = new Process[1];
		//Runnable[] endTask = new Runnable[1];
		
		content[0] = new FunctionalityContent( "ElExecRemoteEvent",
				params,
				() -> {
						
						// Save the event into a file
						FuncLabel lbl = (FuncLabel) params[0];
					
						if (lbl == null)
						{
							Execution.setError("The Labeled Event named '" + (String) params[0] + "' does not exist!", false);
							return;
						}
						
	
						ConnectedExternalLinux destination = getExternalTargetFromVar(params[1]);
						if (destination == null)
							return;
						
						//EventInstance instance = ((ProgramEventContent) lbl.getEvent().getContent()).getAssociatedEventInstance();
						DataNode<ProgramElement> root = new DataNode<ProgramElement>(null);
						root.addChild(lbl.getEvent().getContent().getVisualization().getNode());
						
						
						
						String localProgramFile = FileHelpers.addSubfile(LocationPreparator.getRunnerFileDirectory(), "external_event_file_" + lbl.getName() + ".dra");
						
						DataControler.saveProgramFile(localProgramFile, false, root);
						
						WaitPopup popup = new WaitPopup("Establishing connection.\nTransfering Data.\n\nPlease wait.");
						
						
						if (destination.deploy()) // if successful
						{							
							destination.executeAndHandleDeployed(""); // execute with corresponding arguments
						
							ImplementJSch jsh = destination.jsh;
							
							streams[0] = jsh.getStandardStream();
							streams[1] = jsh.getErrorStream();

							checkAndEndTask[0] = () -> jsh.checkCommandRunning(true);	
						}
						
						popup.close();
					
					},
				() -> {
					
					Variable output = null;
					if (params[3] != null)
					{
						output = (Variable) params[3];
						if (output.isType(Variable.noType))
							output.initTypeAndSet(Variable.textType, "");
					}
					
					
					boolean waitForFinish = (boolean) params[2];
					
					if (!waitForFinish)
						return(false);

					
					if (!outputQueue.isEmpty())
					{
						while(outputQueue.peek().isEmpty() && !outputQueue.isEmpty())
		        			outputQueue.poll();
						
						if (outputQueue.isEmpty()) return(false);
						
						output.set(outputQueue.poll());
						return(true);
					}
					
					
					
					byte[] tmp=new byte[1024];
					boolean outputFound = false;
					
					
					//do
					{
						boolean ended = false;
						
						if (streams[0] == null || streams[1] == null)
							return(false);
				
						while(!ended && !outputFound && Execution.isRunning())
						{
							while(streams[0].available()>0 || streams[1].available()>0 && Execution.isRunning())
					        {
					        	if (streams[0].available()>0)
					        	{
							        int i=streams[0].read(tmp, 0, 1024);
							        
							        if (i>=0)
							        {
								        String str = new String(tmp, 0, i);
								        if (str.length() > 0)
								        {
								        	if (output != null)
								        	{
								        		String[] lines = str.split("\n|" + Character.toString((char)13));
								        		for(int j = 0; j < lines.length; j++)
								        			outputQueue.add(lines[j]);
								        		
								        		while(outputQueue.peek().isEmpty() && !outputQueue.isEmpty())
								        			outputQueue.poll();
								        		
								        		if (!outputQueue.isEmpty())
								        			output.set(outputQueue.poll());								        		
								        	}
								        	outputFound = true;
								        	break;
								        }							        
							        }
							        else {ended = true; break;}
					        	}
					        	
					        	
					        	if (streams[1].available()>0)
					        	{
							        int i=streams[1].read(tmp, 0, 1024);
							        
							        if (i>=0)
							        {
								        String str = new String(tmp, 0, i);
								        if (str.length() > 0)
								        {
								        	if (output != null)
								        	{
								        		String[] lines = str.split("\n|" + Character.toString((char)13));
								        		for(int j = 0; j < lines.length; j++)
								        			outputQueue.add(lines[j]);
								        		
								        		while(outputQueue.peek().isEmpty() && !outputQueue.isEmpty())
								        			outputQueue.poll();
								        		
								        		if (!outputQueue.isEmpty())
								        			output.set(outputQueue.poll());								        		
								        	}
								        	outputFound = true;
								        	break;
								        }							        
							        }
							        else {ended = true; break;}
					        	}
					        	
					        	if (!outputFound)
									Thread.sleep(GlobalSettings.constantCheckDelay);
					        }
						
							/*
							if (process[0] != null)
							if (!process[0].isAlive())
								break;
								*/
							if (checkAndEndTask[0].call())
								break;							

							Thread.sleep(GlobalSettings.constantCheckDelay);
						}
					}
					//while(/*waitForFinish &&*/ false && outputFound && process[0].isAlive());
					
					if (!outputQueue.isEmpty())
						return(true);
					
					return(outputFound);
					},
				() -> {
					
				});
		return(content[0]);
	}
	public static ProgramElement visualize_ElExecRemoteEvent(FunctionalityContent content)
	{
		VisualizableProgramElement elExecCommand;
		elExecCommand = new VisualizableProgramElement(content, "Remote Execute Event", "Executes the given Labled Event on the current external deployment target of the GUI.\nNote this cannot be used outside the GUI!");
		elExecCommand.setArgumentDescription(0, new LabelString(), "Labeled Event");
		elExecCommand.setArgumentDescription(1, new VariableOnly(true, false), "Target Computer");
		elExecCommand.setArgumentDescription(2, new BooleanOrVariable(), "Wait Finish");
		elExecCommand.setArgumentDescription(3, new VariableOnly(true, true), "Output");
		elExecCommand.setExpandableArgumentDescription(TextOrValueOrVariable.class, null, true, false, "Arg #", 16, "Optional values can be expanded and their content will be transmitted to the 'Labeled Event'\n if it's used. Ensure to use the same number of 'arguments'!");
		return(elExecCommand);
	}
	
	
	
	

	public static ProgramElement create_ElExternalDevice()
	{
		Object[] input = new Object[4];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElExternalDevice",
				input,
				() -> {
					
					String host = (String) input[1];
					String user = (String) input[2];
					String pass = (String) input[3];
					
					ConnectedExternalLinux destination = new ConnectedExternalLinux("", "", user, host, pass);
					initVariableAndSet(input[0], Variable.DestinationDeviceType, destination);
					
					if (cont[0].getOptionalArgTrue(0)) // test
					{
						String err = destination.testConnection();
						if (!err.isEmpty())
							Execution.setError("Connecting to a computer was not possible.\nError: " + err, true);
					}
					
					}));
	}
	public static ProgramElement visualize_ElExternalDevice(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "External Computer", "Provide a host (IP address), password and user to access another computer or device.\nThis can be used to access ftp severs or\nexecute Drasp events and commands on other devices.\nNote this is not for TCP connections like the 'Networking' actions!");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Computer Identifier");
		vis.addParameter(1, new TextOrVariable(""), "Host", "May be an IP address or a host name!");
		vis.setArgumentDescription(2, new TextOrVariable(""), "Username");
		vis.setArgumentDescription(3, new TextOrVariable(""), "Password");
		vis.addOptionalParameter(0, new BooleanOrVariable("False"), "Test", "If true, the connection will be tested directly and an error occurs if failed.\nNote that you check without error whetehr conencting is possible.\nSee the element type 'Conditions'.");
		
		return(vis);
	}

	
	
	public static ProgramElement create_ElDownloadUrl()
	{
		Object[] input = new Object[2];
		
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElDownloadUrl",
				input,
				() -> {
					}
				,
				() -> {
			        
						String link = (String) input[0];
						String remoteFileName = link.substring(link.lastIndexOf('/') + 1);

			        	String target = (String) input[1];
			        	
			        	if (new File(target).isDirectory())
			        		target = FileHelpers.addSubfile(target, remoteFileName);
			        	
			        try {
			        	URL url = new URL(link);
			        	InputStream stream = url.openStream();
						Files.copy(stream, Paths.get(target));
		        	
						stream.close();
					} catch (Exception e)
					{
						if (!cont[0].getOptionalArgTrue(0))
							Execution.setError("Error at donloading an URL.\nError message: " + e.getMessage(), false);
						return;
					}
					
					}));
	}
	public static ProgramElement visualize_ElDownloadUrl(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Close Window", "Close a window opened by 'Open Window'");
		vis.setArgumentDescription(0, new TextOrVariable(), "Url to Download.");
		vis.addParameter(1, new TextOrVariable(), "Local File or Dir", "If providing a path with a file-ending, the downloaded file will take that name.\nIf providing no file-ending, the last name will be interpreted as a directory\nand the file is downloaded with its original name.\nNote that missing directories will be created.");
		vis.addOptionalParameter(0, new BooleanOrVariable("False"), "Ignore Error", "If true, any error at downlaoding will be ignored.\nYou can verify success by checking whether the file exists.");
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElDownloadFTP()
	{
		Object[] input = new Object[3];
		return(new FunctionalityContent( "ElDownloadFTP",
				input,
				() -> {
					
					ConnectedExternalLinux destination = getExternalTargetFromVar(input[0]);
					if (destination == null)
						return;
		        	
		        	
					String remote = (String) input[1];
					String remoteFileName = remote.substring(remote.lastIndexOf('/') + 1);

		        	String target = (String) input[2];
		        	
		        	if (new File(target).isDirectory())
		        		target = FileHelpers.addSubfile(target, remoteFileName);		
		        	
		        	
		        	String err = FTPhandlerStatic.download(destination.getHostname(), destination.getUsername(), destination.getPassword(), target, remote);
		        	if (!err.isEmpty())
		        		Execution.setError(err, false);
		 
					}));
	}
	public static ProgramElement visualize_ElDownloadFTP(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Download by FTP", "Downloads a file from an FTP server using the given computer identifier.");
		vis.setArgumentDescription(0, new VariableOnly(true, false), "Computer Identifier");
		vis.addParameter(1, new TextOrVariable(), "FTP File or Dir", "File or directory to download from FTP.");
		vis.addParameter(2, new TextOrVariable(), "Local File or Dir", "If providing a path with a file-ending, the downloaded file will take that name.\nIf providing no file-ending, the last name will be interpreted as a directory\nand the file is downloaded with its original name.\nNote that missing directories will be created.");
		return(vis);
	}
	
	
	public static ProgramElement create_ElUploadFTP()
	{
		Object[] input = new Object[3];
		return(new FunctionalityContent( "ElUploadFTP",
				input,
				() -> {
					
					
					ConnectedExternalLinux destination = getExternalTargetFromVar(input[0]);
					if (destination == null)
						return;
					
		            
					String source = (String) input[1];
					String sourceFileName = source.substring(source.lastIndexOf('/') + 1);

					
		        	String remote = (String) input[2];

					
		        	if (new File(remote).isDirectory())
		        		remote = FileHelpers.addSubfile(source, sourceFileName);		
		        	
		        	
		        	String err = FTPhandlerStatic.upload(destination.getHostname(), destination.getUsername(), destination.getPassword(), source, remote);
		        	if (!err.isEmpty())
		        		Execution.setError(err, false);
		            
					
					}));
	}
	public static ProgramElement visualize_ElUploadFTP(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Upload by FTP", "Uploads a file to an FTP server using the given computer identifier.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Computer Identifier");
		vis.setArgumentDescription(1, new TextOrVariable(), "Local File");
		vis.addParameter(2, new TextOrVariable(), "FTP File or Dir", "File or directory to upload to FTP.\nIf you provide a directory,\nthe name of the source file will be used.");
		return(vis);
	}
	
	
}
