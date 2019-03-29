package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.SwingUtilities;

import org.apache.commons.codec.binary.Base64;

import dataTypes.DataNode;
import dataTypes.SimpleTable;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.ExecutionStarter;
import execution.Program;
import execution.handlers.InfoErrorHandler;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import main.functionality.Functionality;
import main.functionality.SharedComponents;
import main.functionality.helperControlers.network.FTPhandlerStatic;
import main.functionality.helperControlers.network.ImplementJSch;
import main.functionality.helperControlers.screen.JFXgraph;
import main.functionality.helperControlers.screen.JFXwindow;
import main.functionality.helperControlers.spline.DataSpline;
import productionGUI.ProductionGUI;
import productionGUI.additionalWindows.StartScreen;
import productionGUI.controlers.UndoRedoControler;
import settings.GlobalSettings;
import staticHelpers.Benchmarker;
import staticHelpers.FileHelpers;
import staticHelpers.GuiMsgHelper;
import staticHelpers.LocationPreparator;
import staticHelpers.OtherHelpers;


public class MainControler
{
	private static long startTime = System.currentTimeMillis();
	private static StartScreen startScreen;
	

	
	public static void main(String[] args)
	{		
		boolean showGUI = false;
		String programFile = "";
		
		GlobalSettings.init();
		LocationPreparator.init();
		
		
		/*
		String ext = LocationPreparator.getExternalDirectory()+ File.separator + "optionalResources";

		if (LocationPreparator.isUnix())
		{
			System.load(ext+"integratedJFX/libfxplugins.so");
			System.load(ext+"integratedJFX/libdecora_sse.so");
			System.load(ext+"integratedJFX/libglass_monocle_x11.so");
			System.load(ext+"integratedJFX/libglass_monocle.so");
			System.load(ext+"integratedJFX/libgstreamer-lite.so");
			System.load(ext+"integratedJFX/libjavafx_font_freetype.so");
			System.load(ext+"integratedJFX/libjavafx_font_pango.so");
			System.load(ext+"integratedJFX/libjavafx_font.so");
			System.load(ext+"integratedJFX/libjavafx_iio.so");
			System.load(ext+"integratedJFX/libjfxmedia.so");
			//System.load(ext+"integratedJFX/libjfxwebkit.so");
			System.load(ext+"integratedJFX/libprism_common.so");
			System.load(ext+"integratedJFX/libprism_es2_eglfb.so");
			System.load(ext+"integratedJFX/libprism_es2_monocle.so");
			System.load(ext+"integratedJFX/libprism_sw.so");
			System.load(ext+"integratedJFX/libglass.so");
		}
		*/
		
		
		
		//List<String> arguments = new ArrayList<String>(Arrays.asList(args).subList(Math.min(args.length, 1), args.length));
		List<String> arguments = new ArrayList<String>(Arrays.asList(args));
		
		if (args.length >= 1) // at least an argument is available -> the (FTP) path to a file to load and run
		{
			programFile = args[0];
			
			if (programFile.toLowerCase().startsWith("ftp:")) // FTP provided (Form needed: ftp:FILEPATH|HOST|USERNAME|PASSWORD|ALTERNATIVE_LOCAL_FILE where the last argument is optional.
			{
				String[] data = programFile.split("\\|");
				String ftpFilePath = data[0];
				
				programFile = ftpFilePath.substring(ftpFilePath.lastIndexOf('/') + 1);	        					
				
				String err = FTPhandlerStatic.download(data[1].trim(), data[2].trim(), data[3].trim(), programFile, ftpFilePath);			
				if (!err.isEmpty())
				{
					System.out.println("FTP DOWNLAOD OF '" + programFile + "' FAILED FOR REASON: " + err);

					if (data.length >= 5)
					{
						programFile = data[4].trim(); // the alternative file
						System.out.println("STARTING ALTERNATIVE LOCAL FILE: " + programFile);
					}
					else
						System.out.println("NO ALTERNATIVE FILE PROVIDED. ABORTING.");
				}
			}
			// else // programFile is already the right file path
		}
		
		
		for(int i = 0; i < arguments.size(); i++)
			arguments.set(i, arguments.get(i).toUpperCase() ); // make all attributes upper case for easier comparison
		
		
		
		
		if (arguments.isEmpty() // No arguments, means starting application with GUI
			|| arguments.contains("GUI")) // or explicitly called GUI
				showGUI = true;
		
		boolean checkForRunning = (arguments.contains(GlobalSettings.keyCheckForRunning)); // Check on the "RUNNING" file to allow stopping execution externally
		
		if (arguments.contains(GlobalSettings.keyVisualDebug)) // visual debug messages
			GlobalSettings.visualDebug = true;
		
		if (arguments.contains(GlobalSettings.keyEventDebug)) // visual debug messages
			GlobalSettings.eventDebug = true;

		if (arguments.contains(GlobalSettings.keyFullDebug)) // visual debug messages
			GlobalSettings.fullDebug = true;
		
		boolean tracking = arguments.contains(GlobalSettings.keyExternalTracking);
		
		
		if (GlobalSettings.fullDebug)
			GlobalSettings.eventDebug = true;
		
		
		Execution.init(showGUI);
		
		DataControler.init();//true, true);

		
		boolean loadProgram = false;
		
		if (!programFile.isEmpty())
		{
			File file = new File(programFile);
			if (file.exists())
				loadProgram = true;
			else
				InfoErrorHandler.callEnvironmentError("The program file you want to load does not exist! Path: " + programFile);
		}
		else
			InfoErrorHandler.printEnvironmentInfoMessage("No filepath to a program given. Starting GUI.");
		
		
		
		
		class ShutdownHook extends Thread // In the case of unexpected end of the Java Runtime
		{
		    public void run()
		    {
		    	InfoErrorHandler.printExecutionInfoMessage("PROGRAM STOPPED.");
		    	SharedComponents.quitGlobalObjects(); // Shutdown hardware components (like GPIO)
		    }
		}
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		
		
		if (showGUI)
		{			
			GlobalSettings.fullDebug = false;
			Variable.enableSignalizeSet();
			SharedComponents.setFunctionalityDebugAndSimulated(true, true);
			
			
			startScreen = new StartScreen();
			ProductionGUI pr = new ProductionGUI();
			

			Platform.runLater( () ->
			{
				try { startScreen.start(new Stage()); } catch (Exception e) { e.printStackTrace(); }		
				
				Platform.runLater( () -> // Run in a nested execution block so it doesn't block the start-screen
				{
					try { pr.start(new Stage()); } catch (Exception e) { e.printStackTrace(); }		
				});
					
			});
			
			if (loadProgram)
			{
				while(UndoRedoControler.getSelf() == null) // Wait till the GUI has loaded
					OtherHelpers.sleepNonException(100);
				OtherHelpers.sleepNonException(100);
				
				String prog = programFile;
				Platform.runLater( () ->
				{
					DataControler.loadProgramFile(prog, true, true);
				});
			}
		}
		else
		{
			Functionality.loadElements(false);
			
			InfoErrorHandler.printEnvironmentInfoMessage("Starting program: " + programFile);
			Program program = DataControler.loadProgramFile(programFile, true, false);
			
			if (GlobalSettings.fullDebug)
				Variable.enableSignalizeSet();
			if (GlobalSettings.eventDebug)
				SharedComponents.setFunctionalityDebugAndSimulated(true, false);
			
			if (checkForRunning)
				checkForRunning();
			
			Execution.hookOntoFinish(() -> {  // Hook the exit command onto the execution
				Execution.stop(); // In case some thread is still running, give the chance to finish gracefully
				InfoErrorHandler.printEnvironmentInfoMessage("PROGRAM STOPPED.");
				SharedComponents.quitGlobalObjects(); // Shutdown special hardware components
				OtherHelpers.sleepNonException(GlobalSettings.runningCheckInterval);
				System.exit(0);
			});
			
			ExecutionStarter.startProgram(program, false, false, tracking); // Start the program (not simulated)
		}		
		
	}
	
	
	
	

	private static void checkForRunning()
	{
		new Thread(() -> {
			
			try {
				//String root = new File(MainControler.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
				String root = LocationPreparator.getRunnerFileDirectory();
				
				
				if (!root.endsWith(File.separator))
					root += File.separator;
				File f = new File(root+"RUNNING");
				
				Thread.sleep(50);
				
				while(true)
				{
					Thread.sleep(GlobalSettings.runningCheckInterval); // check at regular interval
					
					if (!f.exists())	
					{
						InfoErrorHandler.printEnvironmentInfoMessage("'RUNNING' file has been deleted externally.");
						InfoErrorHandler.printEnvironmentInfoMessage("Stopping the program now.");
						
				    	Thread.sleep(100);
						
				    	Execution.stop();
				    	
				    	Thread.sleep((long) (GlobalSettings.waitForEventsEndTime*1.25));
				    	
				    	SharedComponents.quitGlobalObjects(); // Shutdown special hardware components
				    	
						InfoErrorHandler.printEnvironmentInfoMessage("PROGRAM STOPPED.");
						
						System.exit(0);
						
				    	return;
					}
				}
			} catch (InterruptedException e) { e.printStackTrace(); }

			
		}).start();
	}


	public static long getStartTime()
	{
		return(startTime);
	}
	
	public static void closeStartWindow()
	{
		startScreen.close();
	}
	
	
	
	
	
	public static void ___________main(String[] args)
	{
	    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;				 
	    
        try {
        	
	        
			//AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
            //								       channels, signed, bigEndian);
        	//AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, 16000, 8, 2, 32, 120, true);
        	
        	
        	AudioFormat format = new AudioFormat(8000, 8, 1, !true, true);
        	
        	
        	//AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, true);

        	//AudioFormat format = new AudioFormat(Encoding.PCM_UNSIGNED, 16000, 8, 1, 1, 16000, true);
        	//AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, 32000, 16, 1, 2, 16000, true);
        	
        	// PCM_SIGNED 16000.0 Hz, 8 bit, mono, 1 bytes/frame, 
        	
  //      			
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format );
            System.out.println("Info: " + info);
            /*
        	if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                return;
            }
            */
            

            
        	TargetDataLine recorderLine = (TargetDataLine) AudioSystem.getLine(info);
            recorderLine.open(format);
            recorderLine.start();   // start capturing
            
            AudioInputStream ais = new AudioInputStream(recorderLine);
            System.out.println("Frame size: " + format.getFrameSize());
            
            

            
            
            new Thread( () -> {
            	try {
                    int len = 1000;
                    byte[] data = new byte[len];
                    int count = 0;
                   	long read = 1;
                   	
                   	int rec = 4;
                   	if (rec == 0)
                   	{                   	
	                    while(read > 0)
	                    {
	                    	read = ais.read(data);
	                    	count+=read;
	                    }
	                    
	                    System.out.println("Read: " + count);
                   	}
                   	if (rec == 1)
                   	{
	                   	while(recorderLine.isOpen())
	                   	{
	                   		int v = Math.abs(128-ais.read());
	                   		if (v > 75)
	                   		{
	                   			System.out.println("Ind: " + count + " v: " + v);
	                   		}
	                   		
	                   		
	                   		//if (v > 100 && (v < 235))
	                   			//System.out.println(v);
	                   		//else
	                   			//System.out.println("-");
	                   		count++;
	                   	}
                   	}
                   	if (rec == 4)
                   	{
                   		int bl = 32;
                   		int lastOcc = 0;
                   		byte[] dt = new byte[bl];
	                   	while(recorderLine.isOpen())
	                   	{
	                   		OtherHelpers.sleepNonException(100);
	                   		
	                   		for(int j = 0; j < 25; j++)
	                   		{
		                   		ais.read(dt);
		                   		int occ = 0;
		                   		for(int i = 0; i < bl; i++)
		                   		{
			                   		int v = Math.abs(128-ais.read());
			                   		if (v > 40)
			                   			occ++;
		                   		}
		                   		
		                   		int sum = (lastOcc + occ);
		                   		if (sum > 0)
		                   			System.out.println("Sum: " + sum);
		                   		if ((sum > 3) && (sum < 26))
		                   		{
		                   			System.out.println("Clap!");
		                   			occ = 0;
		                   		}
		                   		
	                  			lastOcc = occ;
	                   		


	                   		
	                   		
	                   		//if (v > 100 && (v < 235))
	                   			//System.out.println(v);
	                   		//else
	                   			//System.out.println("-");
	                  			count++;
	                   		}

	                   	}
                   	}
                   	if (rec == 3)
                   	{
                   		int bl = 32;
                   		byte[] dt = new byte[bl];
	                   	while(recorderLine.isOpen())
	                   	{
	                   		ais.read(dt);
	                   		int cc = 0;
	                   		for(int i = 0; i < bl; i++)
	                   		{
		                   		int v = Math.abs(128-ais.read());
		                   		if (v > 75)
		                   			cc++;
	                   		}
	                   		for(int i = 0; i < bl; i++)
	                   		{
		                   		int v = Math.abs(128-ais.read());
		                   		if (v > 75)
		                   			cc++;
	                   		}
	                   		
	                   		if (cc > 2)
	                   			System.out.println("Ind: " + count);

	                   		
	                   		
	                   		//if (v > 100 && (v < 235))
	                   			//System.out.println(v);
	                   		//else
	                   			//System.out.println("-");
	                   		count++;
	                   	}
                   	}
                   	if (rec == 2)
                   	{
                   		byte[] dt = new byte[2];
	                   	while(recorderLine.isOpen())
	                   	{
	                   		ais.read(dt);
	                   		if (dt[0] < -2)
	                   			System.out.println(dt[0] + " | " + dt[1]);
	                   		if (dt[0] > 2)
	                   			System.out.println(dt[0] + " | " + dt[1]);

	                   		//int v = dt[0] + dt[1];
                   			//System.out.println(v);

	                   		//if (v > 2*100 && (v < 2*245))
	                   			//System.out.println(v);
	                   		//else
	                   			//System.out.println("-");	
	                   	}
                   	}

            		
					AudioSystem.write(ais, fileType, new File("C:\\Users\\drarr\\Dropbox\\Projekte\\JavaWorkspaces\\Drasp\\Test\\record.wav"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }).start();
            
            System.out.println("Sleeping now");

            
            
            try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            
            recorderLine.stop();
            recorderLine.close();
            
            System.out.println("Finished");
            
            /*
            System.out.println("Data:");
            long val = 0;
            String str = "";
            for(int i = 0; i < len; i++)
            {
            	val += data[i];
                str += data[i];
            }
            System.out.println(str);
            System.out.println("End Sum: " + val);
            */
            
            //00-10000-100000000000000-100000000010-10000000000000000000-1000000000000010000000100011000-11100000001000100000-10000000000-1-1-10000-10000000001010000001-101-10000000-1000-10-100000-1000000-100000000-1-100000100000-1000000000101001-11000-1-1000-10-1000-1000010000001010000001000110000000-110000000100-1000-1-10010-1000000-1-100-110100000010-10010-1100-212-3-323224546414242423131171744-5-5-14-13-17-17-23-23-28-27-29-30-33-32-30-30-28-26-25-25-21-20-19-19-20-19-18-18-14-14-11-10-9-9-9-9-11-10-15-16-21-21-24-24-27-27-32-32-37-36-37-36-37-37-36-36-34-35-30-29-26-25-26-27-26-27-27-27-30-29-32-32-34-34-38-38-40-40-44-44-45-45-43-43-39-40-36-37-33-32-30-31-27-26-25-25-24-24-22-22-23-22-24-25-27-28-31-32-31-32-32-32-34-33-31-32-30-30-29-30-28-28-29-28-29-29-29-28-30-29-29-30-30-29-30-30-29-28-27-26-21-21-19-18-16-16-13-12-14-13-13-13-12-12-11-12-11-11-12-11-11-10-10-10-10-9-5-5-2-15511121718282732334141484856555454646363657876798196939697118117107109-82-84-128-128-172412712778787072474622221-1-18-16-35-35-40-40-36-36-29-27-9-955252542435151565562615859484830311112-5-4-20-20-29-30-34-33-39-39-38-38-35-35-32-32-25-25-20-21-13-13-8-8-13-12-12-12-11-11-13-14-11-12-12-12-14-14-16-17-22-22-24-23-24-25-29-29-29-30-29-29-30-30-31-31-34-34-29-29-28-28-27-27-27-27-27-27-28-27-31-31-33-33-33-33-34-34-36-36-37-36-35-36-35-34-34-33-31-31-27-27-25-24-20-21-20-19-22-21-21-20-23-23-24-25-25-26-28-29-29-28-32-33-35-36-31-32-31-31-30-30-30-30-29-29-25-25-25-25-26-26-21-22-22-22-22-21-22-22-20-20-17-17-17-18-17-16-14-15-14-14-15-14-15-15-14-14-9-9-8-9-7-7-3-300781313191931313535424144434949505062615758787871721009983851271271150-128-128-128-128-66-64127124788074724647262567-14-15-32-31-35-36-31-32-23-22-6-64424253637454453536060585951513333171621-14-14-20-21-27-25-32-33-35-35-37-36-31-31-25-25-20-20-12-13-9-9-9-9-11-10-10-9-9-9-8-7-7-7-10-10-14-14-18-18-20-21-22-21-25-25-28-28-29-29-30-30-32-33-33-33-28-28-25-25-25-25-25-25-24-24-26-27-28-29-31-30-31-31-33-33-39-38-41-41-38-39-39-38-37-37-32-32-28-28-24-24-23-23-20-20-18-19-19-19-20-20-24-23-26-25-29-28-31-32-31-31-32-32-31-31-28-27-25-25-23-24-21-21-23-23-21-22-26-26-29-28-26-26-28-28-28-27-24-23-21-20-15-15-11-11-8-9-2-2-3-3-4-4-3-3-7-7
            //00-10000-100000000000000-100000000010-10000000000000000000-1000000000000010000000100011000-11100000001000100000-10000000000-1-1-10000-10000000001010000001-101-10000000-1000-10-100000-1000000-100000000-1-100000100000-1000000000101001-11000-1-1000-10-1000-1000010000001010000001000110000000-110000000100-1000-1-10010-1000000-1-100-11010000001000-1000001-1-1011-2-2-3-3-3-3-3-3-3-3-3-3-4-4-4-4-3-3-4-3-4-3-3-3-3-4-4-3-3-4-4-3-3-4-5-5-4-4-4-3-5-4-4-5-5-3-4-4-4-4-5-4-4-5-5-4-4-4-4-4-4-4-5-4-5-4-4-4-5-4-5-5-5-4-5-4-5-6-3-5-5-5-5-5-5-5-5-5-5-5-4-5-5-5-5-4-5-5-5-5-4-5-4-4-4-5-5-4-5-4-5-4-4-4-5-4-4-5-4-5-5-5-4-4-4-4-4-4-3-4-4-4-4-5-3-4-5-3-4-4-4-3-4-3-3-4-4-3-4-4-5-3-4-3-3-3-4-4-3-4-4-3-4-4-4-3-5-4-3-4-4-4-5-4-5-4-4-4-5-4-4-4-6-4-4-4-5-4-4-3-4-5-5-4-4-3-4-3-4-4-4-4-4-4-4-3-4-4-4-3-3-4-3-4-4-3-4-3-4-4-4-4-3-3-4-4-5-3-4-3-3-3-3-4-4-4-3-3-4-3-3-4-3-3-3-2-3-3-4-4-3-3-4-3-3-2-3-3-3-4-4-3-3-3-4-3-3-3-4-3-4-4-3-3-4-3-4-4-4-4-3-3-4-4-4-4-4-4-3-4-4-3-4-3-4-4-4-4-3-4-4-4-4-4-4-4-4-4-3-3-4-3-3-3-3-3-3-3-3-3-3-3-2-2-3-3-3-3-3-3-3-3-4-3-3-2-3-3-3-3-3-3-3-3-3-3-3-3-3-3-3-3-3-2-3-2-2-2-2-2-3-2-2-3-3-2-2-1-2-1-3-3-2-3-2-3-2-3-3-2-2-3-3-3-1-2-3-2-3-3-2-2-2-2-2-2-2-3-3-4-2-3-3-4-4-4-3-4-4-3-3-3-4-4-4-3-3-4-3-3-3-3-3-3-4-4-3-3-3-3-5-4-3-3-3-3-4-3-4-4-3-3-4-3-3-3-4-5-3-3-4-4-4-3-4-4-4-4-4-4-3-2-2-3-3-3-3-3-3-3-3-4-3-3-3-4-4-4-4-3-4-3-4-3-4-3-3-3-5-5-4-4-5-5-3-4-5-4-4-4-5-5-5-4-5-4-5-5-5-5-5-5-5-4-4-4-4-4-4-5-4-5-5-4-4-4-5-3-4-5-4-4-4-3-4-4-4-4-4-3-4-4-4-4-4-4-4-4-4-4-4-4-4-3-5-4-5-4-5-4-4-4-4-4-4-3-4-4-4-4-3-3-4-4-3-4-4-4-4-3-3-4-4-3-3-2-4-3-3-3-4-4-5-4-3-3-4-3-4-3-3-3-3-4-4-3-3-3-3-3-3-3-2-2-4-3-3-3-4-4-3-3-3-3-3-3-3-3-3-3-2-3-2-3-3-3-3-3-3-2-2-2-2-2-2-2-2-2-2-2-3-2-3-2-1-1-2-1-2-1-2-1-2-2-2-2-2-2-2-3-3-4-3-2-3-2-2-2-3-2

            
            
        } catch (LineUnavailableException /*| IOException*/ er) {
        	System.out.println("Error: " + er);
        	Execution.setError("Problem at starting sound recording. Error: " + er.getMessage(), false);
        }
	}
	
	

	// For simple testing
	public static void ____main(String[] args)
	{
		final CountDownLatch latch = new CountDownLatch(1);
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		        new JFXPanel(); // initializes JavaFX environment
		        latch.countDown();
		    }
		});
		try {
			latch.await();
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		
		new Thread(()-> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			
			
			DataSpline spline = new DataSpline(true, true, -1, -1, -1);

			spline.setValue(0, 0);

			Random ran = new Random();
			
			int numberOfVals = 10;
			
			for(int i = 0; i < 400; i += 400/numberOfVals)
			{
				double v = ran.nextDouble()*80;
				System.out.println("SET: " + i + " to: " + v);
				spline.setValue(i, v);
			}
			
			/*
			spline.setValue(0, 0);

			spline.setValue(50, 60);

			spline.setValue(250, 10);

			spline.setValue(300, 60);
			
			spline.setValue(350, 80);*/
			
			
			
			//spline.setValue(400, 0);
			
			
			
			JFXgraph graph = new JFXgraph(spline, "000000", 400, 200, false);
			//graph.setPosition(0, 50);
			JFXwindow window = new JFXwindow(400,300, "Test");
			
			window.applyItem(graph, true, 0);
			
			
			
		
			OtherHelpers.perform(new FutureTask<Object>(() -> {
				try { window.start(new Stage()); } catch (Exception e) { e.printStackTrace(); }
				return(null);
			}));
			
		}).start();
			
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		if (true) return;
		
		
		
		
		/*volstile*/ double cur = System.currentTimeMillis();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		double sum = 0;
		
		double time = System.nanoTime();
		
		for(int i = 0; i < 100; i++)
		{
			//sum += System.currentTimeMillis();
			//sum -= System.currentTimeMillis();
			sum += cur;
			cur *= 2;
			sum -= cur;
		}
		
		double endTime = System.nanoTime();
		
		System.out.println("Time: " + ((endTime-time)/1000));
		
		
	}

	public static void _______main(String[] args)
	{
		Object[][] vals = new Object[10][8]; 
		Object[][] cvals = new Object[10][8]; 
		
		Random ran = new Random();
		
		for(int k = 0; k < 10000; k++)
		{
			double d = ran.nextDouble();
		}
		
		Benchmarker.bench("Start");

		for(int i = 0; i < 10; i++)
		{
			for(int j = 0; j < 8; j++)
				vals[i][j] = (Double) ran.nextDouble();
		}
		
		for(int k = 0; k < 100000; k++)
		{
			

			//for(int i = 0; i < 10; i++)
			synchronized(vals)
			{
				cvals = vals.clone();
				//vals = cvals;
			}
			
		}
		
		Benchmarker.bench("End");

		
		
	}
	
	
	
	// For simple testing
	public static void _____main(String[] args)
	{
		
		
		
		LocationPreparator.init();
		Execution.init(true);
		
		SimpleTable<String> variableTable = new SimpleTable<String>();
		
		List<String> A = new ArrayList<String>();
		A.add("A");
		A.add("B");
		A.add("C");
		A.add("D");
		
		List<String> B = new ArrayList<String>();
		B.add("AA");
		B.add("BB");
		B.add("CC");
		B.add("DD");
		
		List<String> C = new ArrayList<String>();
		C.add("AAA");
		C.add("BBB");
		C.add("CCC");
		C.add("DDD");
		
		variableTable.addColumn("Argument", A);
		variableTable.addColumn("Type", B);
		variableTable.addColumn("Values", C);
		
		
		int res = GuiMsgHelper.showNonblockingUIWithTable(
				"'Continue' to execute until the next breakponext breakp.\nPress 'Break at Next' to execute the current action\nand break at the next one or the event-end.",
				variableTable, new String[] {"Continue", "Break at Next"});

		
		//if (true)
			//return;
		
		
		String line = "|SI_V|#light|12345|67.89";
		
		int a = line.indexOf('|', 6)+1;
		int b = line.indexOf('|', a);
		
		System.out.println("aa: " + a);
		System.out.println("bb: " + b);
		
		
		System.out.println("A: " + line.substring(6, a-1));
		System.out.println("B: " + line.substring(a, b));
		System.out.println("BB: " + Integer.valueOf(line.substring(a, b)));
		System.out.println("C: " + line.substring(b+1));
		
		
		System.out.println("RETRIEVED LINE: " + line);
		
	}

	/*
	public static void ___1_main(String[] args)
	{

		
			new Thread(() -> {
				try {
					TCPconnector connectorA = new TCPconnector("DraspNetworkTest");
					connectorA.openForConnection();
				} catch (IOException e) { e.printStackTrace(); }
			}).start();
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			new Thread(() -> {
				try {
					TCPconnector connectorB = new TCPconnector("DraspNetworkTest");
					connectorB.establishConnection("localhost", "DraspNetworkTest", true);
				} catch (IOException e) { e.printStackTrace(); }
			}).start();
			
			
			try {
				Thread.sleep(1000000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
 	*/
	
	// For simple testing
	public static void _main(String[] args)
	{
		
		ImplementJSch sh = new ImplementJSch("pi", "192.168.178.35", "raspberry");
		
		sh.connectSession();
		
		System.out.println("Transfering");
		
		//sh.transferFile("C:\\Users\\drarr\\Dropbox\\Projekte\\JavaWorkspaces\\Drasp\\Resources\\if_tools.png", "pi-share\\Drasp\\TTT\\testimage.png");
		
		//sh.transferDirectory("C:\\Users\\drarr\\Dropbox\\Projekte\\JavaWorkspaces\\Drasp\\Resources", "pi-share\\Drasp\\TTT3");
		
		
		
		/*
		List<String> files = new ArrayList<>();
		//sh.listFiles("pi-share\\Drasp\\TTT3", files, ".png");
		sh.listFiles("pi-share\\Drasp\\TTT3", files, "", true);
		
		System.out.println("Files: ");
		for(String str: files)
			System.out.println(str);
		*/
		
		
		/*
		long length = sh.getFileSize("pi-share\\Drasp\\TTT3\\if_settings.png");
		System.out.println("Size: " + length);
		*/
		
		long length = sh.getLastFileModif("pi-share\\Drasp\\TTT3\\if_settings.png");
		System.out.println("Last modification: " + length);
		System.out.println("Local last: "+ (new Date().getTime() - new File("C:\\Users\\drarr\\Dropbox\\Projekte\\JavaWorkspaces\\Drasp\\Resources\\if_settings.png").lastModified())/1000 );
		
		
		System.out.println("Successful");
		
		
		//System .out.println("SUCCESS");
		
		
		//InputStream stream = sh.executeCommand("ping 127.0.0.1 -c 15");
		
		//new DataConsole("Test").addConstantStreamLines(stream, "");
		
		
		
	}
	

	/*
	public static void ______main(String[] args)
	{
		class TestInst {

			Runnable st;
			public TestInst(Runnable start)
			{
				InfoErrorHandler.printDirectMessage("Created");
				
				st = start;
			}

			public void hello()
			{
				InfoErrorHandler.printDirectMessage("Executed Hello");
				
			}

			public void exec() {
				InfoErrorHandler.printDirectMessage("Executing");
				st.run();
			}

		}
		
		final TestInst[] inst = new TestInst[1];
		
		inst[0] = new TestInst(() -> {inst[0].hello(); });

		inst[0].exec();
		
	}
	*/


	
	public static void ___main(String[] args)
	{
		/*
		System.out.println("AAA");
		int[] vv = new int[1];
		vv[0] = 0;
		
		for(int i = 0; i < 100; i++)
		{
			int j = i;
			new Thread(() -> vv[0] += j).start();
		}
		
		System.out.println("BBB: " + vv[0]);
		*/
		
		
		
		try {
		
		
		BitSet A = new BitSet();
		A.set(2);
		A.set(5);
		A.set(12);
		
		printBitset(A);
		
		

		byte[] bitsetByte = A.toByteArray();
		byte[] bitsetByteEncoded = Base64.encodeBase64(bitsetByte);
		
		String bitsetString = new String(bitsetByteEncoded);

		
		InfoErrorHandler.printDirectMessage("");
		InfoErrorHandler.printDirectMessage(bitsetString);
		
		
		byte[] resultBitsetByte = bitsetString.getBytes();
		byte[] resultDecodedBitsetByte = Base64.decodeBase64(resultBitsetByte);
		
		BitSet B = BitSet.valueOf(resultDecodedBitsetByte);
		
		printBitset(B);
		
		
		
		InfoErrorHandler.printDirectMessage("");
		
		
		String filePath = "C:\\Users\\drarr\\Dropbox\\Projekte\\JavaWorkspaces\\Drasp\\Testfiles\\TESTFILE.txt";
		
		Path file = Paths.get(filePath);
		/*

		
		try {
			Files.write(file, bitsetString.getBytes(Charset.forName("UTF-8")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		
		//BufferedWriter bufferedWriter = new BufferedWriter
			//    (new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));
		
		FileWriter fileWriter = new FileWriter(filePath);
		
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        

		bufferedWriter.write(bitsetString);
        bufferedWriter.close();
		
		
        
        
        
        
		
		
		/*
		byte[] altResultBitsetByte = null;
		try {
			altResultBitsetByte = Files.readAllBytes(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
        
		List<String> allLines = FileHelpers.readAllLines(new File(filePath));
		
        
		
		
		byte[] altResultBitsetByte = allLines.get(0).getBytes();
		byte[] altResultDecodedBitsetByte = Base64.decodeBase64(altResultBitsetByte);
		
		BitSet C = BitSet.valueOf(altResultDecodedBitsetByte);
		
		printBitset(C);
		
		
		
		
		//String bitset = allLines.get(ind);
		//loadedCollapseBitset = BitSet.valueOf(Base64.decodeBase64(bitset.getBytes(StandardCharsets.UTF_8)));
		
		
		
		

		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		if (true) return;
		/*
		printBitset(A);
		//String bitset = A.toByteArray().toString();
		String bitset = new String(A.toByteArray());
		//byte[] bitset = A.toByteArray();
		System .out.println(bitset);

		BitSet B = BitSet.valueOf(bitset.getBytes());
		//BitSet B = BitSet.valueOf(bitset);
		printBitset(B);
		*/

		
		if(true) return;
		

		DataNode<String> root = new DataNode<String>("A");
		DataNode<String> ch1 = new DataNode<String>("B");
		DataNode<String> ch2 = new DataNode<String>("C");
		DataNode<String> ch3 = new DataNode<String>("D");
		DataNode<String> ch4 = new DataNode<String>("E");
		DataNode<String> ch5 = new DataNode<String>("F");
		DataNode<String> ch6 = new DataNode<String>("G");
		DataNode<String> ch7 = new DataNode<String>("H");
		
		
		
		root.addChild(ch1);
		root.addChild(ch2);
		
		ch2.addChild(ch3);
		ch2.addChild(ch4);
		ch2.addChild(ch5);
		
		root.addChild(ch6);
		root.addChild(ch7);
		
		
		/*
		String[] str = new String[1];
		//root.applyToChildrenTotal(str, () -> { System .out.println(str[0]);  });
		Integer[] depth = new Integer[1];
		root.applyToChildrenTotal(str, depth, () -> { System .out.println(str[0] + " Depth: " + depth[0] ); });
		*/
		traverseEventNodes(root, 0);
		
		root.printAll();
		
	}
	
	public static void printBitset(BitSet bitset)
	{
		for(int i = 0; i < 20; i++)
			InfoErrorHandler.printDirectMessage(String.valueOf( bitset.get(i) ? 1 : 0 ));
	}
		
	
	public static int traverseEventNodes(DataNode<String> node, int index)
	{

		if (!node.isLeaf())
		{
			index++;
			
			int subcount = 0;
			for(DataNode<String> childNode: node.getChildrenAlways()) // Directly traverse through subchildren
			{
				subcount += traverseEventNodes(childNode, index+subcount);
			}
			
			//node.setData(node.getData() + " is at pos: " + (index+count) + " has subc: " + index);
			node.setData(node.getData() + " is at pos: " + (index-1) + " has subc: " + subcount);
			
			return(subcount+1);
		}
		else
		{
			node.setData(node.getData() + " is at pos: " + (index) + " has subc: " + 0);
			index++;

			return(1);
		}
		
	}
	
	

}
