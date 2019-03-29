package productionGUI.additionalWindows;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice.Info;

import org.controlsfx.control.ToggleSwitch;
import org.reactfx.util.FxTimer;

import dataTypes.SimpleTable;
import dataTypes.minor.possibleIP;
import execution.handlers.InfoErrorHandler;
import execution.handlers.ToolsDatabase;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import productionGUI.ProductionGUI;
import productionGUI.controlers.ButtonsRegionControl;
import productionGUI.controlers.StageOrderControler;
import productionGUI.targetConnection.ConnectedExternalLinux;
import productionGUI.targetConnection.ConnectedLocal;
import productionGUI.targetConnection.TargetConnection;
import productionGUI.tutorialElements.TutorialControler;
import settings.EnvironmentDataHandler;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;
import staticHelpers.GuiMsgHelper;
import staticHelpers.KeyChecker;
import staticHelpers.LocationPreparator;
import staticHelpers.RunnableExporterA;
import staticHelpers.RunnableExporterB;
import staticHelpers.StringHelpers;


public class SettingsMenu extends Application
{
	private static boolean isOpen;
	
	public static boolean isOpen()
	{
		return(isOpen);
	}
	
	
	
	private static Stage stage;
	
	public SettingsMenu()
	{
        FXMLLoader loader = new FXMLLoader(SettingsMenu.class.getResource("/productionGUI/additionalWindows/SettingsMenu.fxml"));
        loader.setController(this);
        Pane root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
        stage = new Stage();
        stage.setTitle("Settings");
        stage.setScene(new Scene(root, GlobalSettings.extraWindowWidth, -1));
        stage.show();
        
        stage.toFront();
        stage.centerOnScreen();
        
       	stage.setAlwaysOnTop(GlobalSettings.menusAlwaysOnTop);
       	
       	
       	
		final KeyCombination exportRunnableComb = new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN);
		
		stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke)
            {
    			if (exportRunnableComb.match(ke)) // Key command to export the runnable
    			{
    				
					InfoErrorHandler.printEnvironmentInfoMessage("EXPORTING NOW!");
    				
    				if (LocationPreparator.isCompiled())
    				{
    					InfoErrorHandler.printEnvironmentInfoMessage("You can only export a runnable if the project is opened in an IDE!");
    					return;
    				}
    				
    				String exporterAntDirPath = FileHelpers.addSubfile(LocationPreparator.getExternalDirectory(), "exporterAntFiles");
    				String workspaceDir = (new File(LocationPreparator.getRunnerFileDirectory())).getParentFile().getParent();
    				String targetJar = FileHelpers.addSubfile(workspaceDir, "DRASP.jar");
    				
    				new Thread(() -> {
    					
	    				WaitPopup wait = new WaitPopup("Exporting to:\n" + targetJar + "\n\nThis can take up to a minute.");
	    				
	    				RunnableExporterA.exportRunnableJar(
	    						targetJar,
	    						workspaceDir,
	    						FileHelpers.addSubfile(exporterAntDirPath, "ExporterAnt.xml"));
	    				
	    				
	    				/*
	    				String out = RunnableExporterB.exportRunnableJar(
	    						"ExportedJar.jar", // outputJarFile
	    						"main.MainControler", // mainClass
	    						//"mainPackage.MainExport",
	    						"bin", // baseClassDir
	    						"libs", // jarLibsDir
	    						"libs\\ExportHelpers\\jarinjar", //jarinjarExportationDir
	    						"libs\\ExportHelpers");
	    						*/
	
	    				//System.out.println("EXPORTING MSG:\n" + out);
	    				
	    				
	    				
	    				
						InfoErrorHandler.printEnvironmentInfoMessage("EXPORTING FINISHED!");

	    				
	    				wait.close();
	    				
	    				/*
	    				if (cmdText.contains("Cannot run program \"jar\""))
	    				{
	    					System.out.println("The 'JAR' program cannot be executed! The JDK is not configured correctly!");
	    				}
	    				*/
    				}).start();
    				
    			}
            }});
    				
       	
        
        
        StageOrderControler.addAdditionalStage(stage);
		KeyChecker.initForStage(stage);
		
		FxTimer.runLater(
		        Duration.ofMillis(GlobalSettings.backToFrontDelay),
		        () -> stage.toFront());
		
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent ev)
            {
            	revert();
            	isOpen = false;
            }});
		
		isOpen = true;
	}
	
	

	@Override
	public void start(Stage theStage)
	{
	}
	
	
	
	
	
	@FXML GridPane mainPane;
	@FXML GridPane outerMainPane;
	
	@FXML TextField usernameField, hostnameField, passwordField, targetDirField, resDirField;
	

	@FXML ToggleSwitch layoutToggle, smoothTransToggle, linuxTargetToggle;

	
	
	@FXML ImageView QusernameField, QhostnameField, QpasswordField, QtargetDirField, QresDirField, QlayoutToggle, QsmoothTransToggle, QlinuxTargetToggle;
	
	@FXML Button btApply;
	
	
	static TargetConnection origDestination;
	static boolean origLayoutToggle, origSmoothTransToggle;
	
	private static Runnable successfulChangeTask;

	
    public void initialize()
    {
    	origDestination = GlobalSettings.destination;
    	
    	
    	origLayoutToggle = GlobalSettings.alternativeLayout;
    	layoutToggle.setSelected(origLayoutToggle);
    	layoutToggle.selectedProperty().addListener(new ChangeListener<Boolean> () {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				layoutToggle.setDisable(true);
				
				if (newValue != GlobalSettings.alternativeLayout)
					if (GlobalSettings.layoutAlternativesKey == KeyCode.NONCONVERT)
						layoutToggle.setSelected(GlobalSettings.alternativeLayout);
					else
						ButtonsRegionControl.getSelf().switchLayout(newValue);
				
				FxTimer.runLater(
				        Duration.ofMillis(2000),
				        () -> {
				        	layoutToggle.setDisable(false);
				        });
			}
        });
    	
    	
    	origSmoothTransToggle = GlobalSettings.smoothDragTransition;
    	smoothTransToggle.setSelected(origSmoothTransToggle);
    	smoothTransToggle.selectedProperty().addListener(new ChangeListener<Boolean> () {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (newValue != GlobalSettings.smoothDragTransition)
					GlobalSettings.smoothDragTransition = newValue;
			}
        });
    	
    	
    	
    	
    	if (GlobalSettings.destination != null) // A destination already exists
    	{
    		targetDirField.setText(GlobalSettings.destination.getDestinationDirectory());
    		resDirField.setText(GlobalSettings.destination.getResSourceDirectory());
    		
    		if (GlobalSettings.destination instanceof ConnectedExternalLinux)
    		{
    			usernameField.setText(((ConnectedExternalLinux) GlobalSettings.destination).getUsername());
    			hostnameField.setText(((ConnectedExternalLinux) GlobalSettings.destination).getHostname());
    			passwordField.setText(((ConnectedExternalLinux) GlobalSettings.destination).getPassword());
    		}
    	}
    	
    	
    	// Deployment settings
    	
    	linuxTargetToggle.setSelected(GlobalSettings.destination instanceof ConnectedExternalLinux);
    	changeTargetToggle(GlobalSettings.destination instanceof ConnectedExternalLinux);
    	linuxTargetToggle.selectedProperty().addListener(new ChangeListener<Boolean> () {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				changeTargetToggle(newValue);
			}
        });
    	
    	
    	
    	resDirField.setOnMouseClicked(new EventHandler<MouseEvent> () {
			@Override
			public void handle(MouseEvent ev)
			{
				// TODO SEARCH FOR FOLDER
				// System. out.println("Search for folder A");
			}
		});
    	
    	
		targetDirField.setOnMouseClicked(new EventHandler<MouseEvent> () {
			@Override
			public void handle(MouseEvent ev)
			{
				if (!linuxTargetToggle.isSelected()) // if target is not linux
				{
					// TODO SEARCH FOR FOLDER
					// System. out.println("Search for folder B");
				}
				
			}
		});
		
		
		TutorialControler.addPossibleMark("test and save", btApply, () -> {});
		
		
		
		
		GuiMsgHelper.createSizedImage(QusernameField, "/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", "Username on target machine.\nIf not changed by yourself,\nthe default on a Raspberry is 'pi'.");
		GuiMsgHelper.createSizedImage(QhostnameField, "/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", "Host IP-Address of the target machine.\nFor a Raspberry Pi find this out by\nattaching a monitor, enabling WLAN and executing 'sudo ip addr show' in a console.");
		GuiMsgHelper.createSizedImage(QpasswordField, "/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", "Password on target machine.\nIf not changed by yourself,\nthe default on a Raspberry is 'raspberry'.");
		GuiMsgHelper.createSizedImage(QtargetDirField, "/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", "Target directory where the program will be placed.\nIf the path does not exist, it will be created.\nWorks on windows and linux (raspberry) targets.");
		GuiMsgHelper.createSizedImage(QresDirField, "/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", "Directory with ressources on your\nmain machine (aka the one you currently work with).\nThe contents of the give directory will be\ncopied to the target machine/directory as well.");
		GuiMsgHelper.createSizedImage(QlayoutToggle, "/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", "An alternative layout for the IDE.");
		GuiMsgHelper.createSizedImage(QsmoothTransToggle, "/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", "Drag and Drop actions and animations are smooth\nbut can look bad on slow machines.");
		GuiMsgHelper.createSizedImage(QlinuxTargetToggle, "/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", "The target machine is in the network\nand not the current machine.\nFor example: A Raspberry Pi.\nExternal machines are accessed via 'SSH',\nthus you need to provide a user,\na password and a host-ip-address.");
		
    }
	
	
	private void changeTargetToggle(Boolean newValue)
	{
		if (newValue) // linux destination
		{
			usernameField.setEditable(true);
			hostnameField.setEditable(true);
			passwordField.setEditable(true);
			usernameField.setOpacity(1);
			hostnameField.setOpacity(1);
			passwordField.setOpacity(1);
		}
		else
		{
			usernameField.setEditable(false);
			hostnameField.setEditable(false);
			passwordField.setEditable(false);
			usernameField.setOpacity(0.5);
			hostnameField.setOpacity(0.5);
			passwordField.setOpacity(0.5);
		}
	}
	
	public void setAutoHost()
	{
		(new Thread(()->_setAutoHost())).start();
	}
	
	private void showProbMsg()
	{
		new Thread(()->GuiMsgHelper.showMessageNonblockingUI("This cannot be used for DRASP because it does not support an SSH connection\n"
				+ "\nUse either the WLAN/SSH tool from the Tools-Window to enable it on the SD card,"
				+ "\nor use the configuration menu on the Raspberry itself if it has a screen and keyboard.")).start();
	}
	
	public void _setAutoHost()
	{
		List<possibleIP> foundAddresses = new ArrayList<>();
		
		String myIp = "";
		try {
			final DatagramSocket socket = new DatagramSocket();
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			myIp = socket.getLocalAddress().getHostAddress();
			socket.close();
		} catch (IOException e)
		{
			InfoErrorHandler.callEnvironmentError("Retrieving own IP address failed!\nIs this computer in a network?\nProblem: " + e.getMessage());
			return;
		}
				
		String resp = ToolsDatabase.execAndRetrieve("arp -a");
		List<String> ips = StringHelpers.extractIPaddresses(resp);
		ips.remove(myIp);
			
		for(String ip: ips)
			foundAddresses.add(new possibleIP(ip));

		List<Thread> threads = new ArrayList<>();
		
		
		
		SimpleTable<String> variableTable = new SimpleTable<String>();
		variableTable.addColumn("Hostname");
		variableTable.addColumn("IP");
		variableTable.addColumn("Status");
		

		
		for(possibleIP ip: foundAddresses)
		{
			threads.add(new Thread(() -> {
				
				if (ip.isSSHconnectable()) // is reachable and connectable
				{
					String host = ip.getHost();
					
					synchronized(variableTable)
					{
						Platform.runLater(() -> variableTable.addOrUpdateRow(new String[] {host, ip.getIP(), "Connectable"}, "msgElementBackgroundClickable", () -> hostnameField.setText(ip.getIP()), true, -1, false));
					}
				}
				else // is only reachable
				if (ip.isReachable())
				{
					String host = ip.getHost();
					
					synchronized(variableTable)
					{
						Platform.runLater(() -> variableTable.addOrUpdateRow(new String[] {host, ip.getIP(), "Only Accessible"}, "msgElementBackgroundClickable", () -> showProbMsg(), false, -1, false));
					}
				}
			
			}));
		}
		
		
		String msg = "The following devices have been found in your network (wait a few seconds).\nClick to chose."
					+ "\n\nIf your target device is 'Only Accessible' that means it does not support SSH!\n   ";
			
		for(Thread thr: threads)
			thr.start();

		
		GuiMsgHelper.showNonblockingUIWithTableDynamic(msg, variableTable, new String[] {"Close"});		

	}
	
	public void apply()
	{
		if (!TutorialControler.handleButtonPress("test and save"))
			return;
		
		String destinationFolder = targetDirField.getText();
		String resSourceFolder = resDirField.getText();
		
		TargetConnection oldDestination = GlobalSettings.destination;
		
		
		if (linuxTargetToggle.isSelected())
		{
			// Linux/External target
			
			if (destinationFolder.isEmpty())
				if (!GuiMsgHelper.askForContinue("The Target Directory is empty!\nThat means the program will be placed directly in the user directory (usually 'pi').\nAre you sure you want to continue?"))
				{
					stage.toFront();
					return;
				}
			
			
			String username = usernameField.getText();
			String hostname = hostnameField.getText();
			String password = passwordField.getText();
			
			GlobalSettings.destination = new ConnectedExternalLinux(destinationFolder, resSourceFolder, username, hostname, password);			
		}
		else
		{
			// Windows/Local target
			GlobalSettings.destination = new ConnectedLocal(destinationFolder, resSourceFolder);
		}
		
		
		if (GlobalSettings.destination.testDeploy())
		{
			EnvironmentDataHandler.rewriteSettings();
			TargetConnection.writeDeploymentSettings(GlobalSettings.destination, LocationPreparator.getExternalDirectory());
			
			isOpen = false;
			stage.close();
			StageOrderControler.removeStage(stage);
		}
		else
		{
			GlobalSettings.destination = oldDestination;
			stage.toFront();
			
			return;
		}
		
		new Thread(()-> 
		{
			if (linuxTargetToggle.isSelected())
				GuiMsgHelper.showMessageNonblockingUI("Tested connection successfully!\nYou can deploy programs now.");
			else
				GuiMsgHelper.showMessageNonblockingUI("You can deploy programs now.");
			
			if (TutorialControler.tutorialRunning())
			if (successfulChangeTask != null)
				successfulChangeTask.run();
			successfulChangeTask = null;
		}).start();
	}
	
	private void revert()
	{
		// Revert changed settings
		//Platform.runLater(() -> {
		if (origLayoutToggle != GlobalSettings.alternativeLayout)
			if (GlobalSettings.layoutAlternativesKey != KeyCode.NONCONVERT)
				ButtonsRegionControl.getSelf().switchLayout(origLayoutToggle);				
		GlobalSettings.smoothDragTransition = origSmoothTransToggle;
		//});
	}
	
	public void cancel()
	{
		if (!TutorialControler.handleButtonPress("cancel"))
			return;
		
		revert();
		isOpen = false;
		stage.close();
		StageOrderControler.removeStage(stage);
	}



	public static void toFront()
	{
		stage.toFront();
	}



	public static void hookOntoSuccessfulChange(Runnable task)
	{
		successfulChangeTask = task;
	}
	
	
}
