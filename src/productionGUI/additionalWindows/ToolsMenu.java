package productionGUI.additionalWindows;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map.Entry;

import org.reactfx.util.FxTimer;

import execution.ExecutionStarter;
import execution.Program;
import execution.handlers.ToolsDatabase;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.DataControler;
import productionGUI.controlers.StageOrderControler;
import productionGUI.targetConnection.ConnectedExternalLinux;
import settings.GlobalSettings;
import staticHelpers.GuiMsgHelper;
import staticHelpers.KeyChecker;
import staticHelpers.LocationPreparator;
import staticHelpers.OtherHelpers;
import staticHelpers.StringHelpers;


public class ToolsMenu extends Application
{
	private static boolean isOpen;
	
	public static boolean isOpen()
	{
		return(isOpen);
	}
	
	
	
	private static Stage stage;
	
	public ToolsMenu()
	{
        FXMLLoader loader = new FXMLLoader(ToolsMenu.class.getResource("/productionGUI/additionalWindows/ScrollableWindow.fxml"));
        loader.setController(this);
		
        Pane root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
        stage = new Stage();
        stage.setTitle("Tools");
        stage.setScene(new Scene(root, GlobalSettings.extraWindowWidth*1.25, GlobalSettings.extraWindowWidth));
        stage.show();
        
        stage.toFront();
        stage.centerOnScreen();
        stage.setAlwaysOnTop(GlobalSettings.menusAlwaysOnTop);
        
        
        StageOrderControler.addAdditionalStage(stage);
		KeyChecker.initForStage(stage);
		
		stage.setOnCloseRequest((event) -> close());

		isOpen = true;
		
		init();
		
		FxTimer.runLater(
		        Duration.ofMillis(GlobalSettings.backToFrontDelay),
		        () -> stage.toFront());
	}
	
	

	@Override
	public void start(Stage theStage)
	{
		stage = theStage;
	}
	
	

	@FXML Label titleText, titleText2;
	
	@FXML GridPane mainPane;
	@FXML BorderPane borderPane;
	@FXML VBox sectionBox;
	@FXML ScrollPane scrollPane;
	
	@FXML VBox contentPane;
	
	static VBox mainBox;
	
	
	String bashToolsFileName = "BashTools.txt";
	
	// The basic buttons
	Button shutdownButton, rebootButton, execButton, autostartButton, scheduledButton;
	
	private int spacing = 5;
	
    public void init()
    {
    	mainBox = new VBox();
    	mainBox.setSpacing(spacing);
    	
    	shutdownButton = createButton("Shutdown Target", 2);
    	rebootButton = createButton("Reboot Target", 2);

    	HBox topBox = new HBox();
    	topBox.setSpacing(spacing);
    	topBox.getChildren().add(shutdownButton);
    	topBox.getChildren().add(rebootButton);
    	
    	execButton = createButton("Create Executable", 3);
    	autostartButton = createButton("Apply Autostart", 3);
    	scheduledButton = createButton("Schedule Launch", 3);


    	contentPane.setSpacing(spacing);
    	
    	
    	HBox secBox = new HBox();
    	secBox.setSpacing(spacing);
    	secBox.getChildren().add(execButton);
    	secBox.getChildren().add(autostartButton);
    	secBox.getChildren().add(scheduledButton);
    	
    	
    	mainBox.getChildren().add(topBox);
    	mainBox.getChildren().add(secBox);
    	
    	mainBox.getChildren().add(borderPane.getCenter());
    	
    	Button closeButton = createButton("Close", 1);
    	mainBox.getChildren().add(closeButton);
    	
    	
		closeButton.setOnAction((event) -> close());
    	
    	borderPane.setCenter(mainBox);
    	
    	borderPane.setTop(null);
    	
    	mainBox.maxWidth(Double.MAX_VALUE);
    	sectionBox.maxWidth(Double.MAX_VALUE);
    	mainBox.maxHeight(Double.MAX_VALUE);
    	sectionBox.maxHeight(Double.MAX_VALUE);
    	
    	
    	sectionBox.minWidthProperty().bind(stage.widthProperty().subtract(56));
    	sectionBox.maxWidthProperty().bind(stage.widthProperty().subtract(56));
       	//mainBox.prefWidthProperty().bind(sectionBox.widthProperty().subtract(20));
           	
    	//sectionBox.maxWidthProperty().bind(scrollPane.widthProperty().subtract(50));
    	contentPane.maxWidthProperty().bind(stage.widthProperty().subtract(66));
    	contentPane.minWidthProperty().bind(stage.widthProperty().subtract(66));
    	
    	
    	//sectionBox.minHeightProperty().bind(scrollPane.heightProperty().subtract(20));
    	borderPane.prefHeightProperty().bind(stage.heightProperty().subtract(20));
    	
    	
    	stage.maxHeightProperty().bind(contentPane.heightProperty().add(200));
    	stage.setMinHeight(350);
    	
    	
    	
    	ToolsDatabase.readToolsDirectory( LocationPreparator.getToolsDirectory() );
    	
    	for(Entry<String, String> tool: ToolsDatabase.getToolSet())
    	{
    		createTool(tool.getKey(), tool.getValue(), ToolsDatabase.getToolFile(tool.getKey()));
    	}
    	
    	
    	/*
    	File bashToolsFile = new File( FileHelper.addSubfile(LocationPreparator.getExternalDirectory(), bashToolsFileName) );
    	
    	if (!bashToolsFile.exists())
		{
			Platform.runLater(()-> {
				GuiMsgHelper.showInfoMessage("The file '" + bashToolsFileName + "' is missing from the directory of the runner!" );
			});
			return;
		}
    	
    	
		Path pt = FileSystems.getDefault().getPath(bashToolsFile.getPath());
		
		List<String> allLines = null;
		
		try {
			allLines = FileHelpers.readAllLines(pt); // to edit
		} catch (IOException e) {}
		
		
		
		final String commentSymbol = "%";
		final String starterSymbol = "[";
		final String endSymbol = "]";
		final String startBash = "[Bash]";
		
		
		String currentDescription = "";
		String currentBash = "";
		boolean inDescription = false;
		boolean inBash = false;
		
		String currentName = "";
		
		
		for(String line: allLines)
		{
			if (line.startsWith(commentSymbol))
				continue;
			
			if (line.equalsIgnoreCase(startBash))
			{
				inDescription = false;
				inBash = true;
				currentBash = "";
				continue;
			}

			
			if (line.startsWith(starterSymbol))
			{
				if (inBash)
					createTool(currentName, currentDescription, currentBash);
				
				int ind = line.indexOf(endSymbol);
				
				currentName = line.substring(1, ind);
				
				inDescription = true;
				inBash = false;
				
				currentDescription = "";
				currentBash = "";
				
				continue;				
			}
			
			if (inDescription)
				currentDescription += line+"\n";
			
			if (inBash)
				currentBash += line+"\n";			
			
		}
		if (inBash)
			createTool(currentName, currentDescription, currentBash);
		
		*/
		
    }
	
    
    
    
    private void createTool(String name, String description, File programFile)
    {
    	description = StringHelpers.trimNewlines(description);  	
    	
    	String compName = name.toLowerCase();
    	
		switch(compName)
		{
		case "shutdown":
			enableButton(shutdownButton, name, description, programFile);
			return;
			
		case "reboot":
			enableButton(rebootButton, name, description, programFile);
			return;
			
		case "executable":
			enableButton(execButton, name, description, programFile);
			return;
			
		case "autostart":
			enableButton(autostartButton, name, description, programFile);
			return;
			
		case "scheduled":
			enableButton(scheduledButton, name, description, programFile);
			return;
		}
    	
		// Not a standard button
		
		
		/*
		HBox element = new HBox();
		element.setSpacing(10);
		element.setAlignment(Pos.CENTER);
		*/
		
		BorderPane element = new BorderPane();

		element.getStyleClass().add("msgHeaderBackgroundB");
		
		Label nm = new Label(name);
		Label tx = new Label(StringHelpers.firstLine(description));
		Button bt = new Button("Execute");
		
		nm.getStyleClass().add("elementContentText");
		tx.getStyleClass().add("elementContentText");
		
    	bt.getStyleClass().add("elementContentText");
    	bt.getStyleClass().add("mainButtons"); 
		
		bt.setOnAction((event) -> executeTool(programFile));
		
				
		/*
		element.getChildren().add(nm);
		element.getChildren().add(tx);
		element.getChildren().add(bt);
		*/
		
		element.setLeft(nm);
		element.setCenter(tx);
		element.setRight(bt);
		
		
		BorderPane.setAlignment(nm, Pos.CENTER_LEFT);
		BorderPane.setAlignment(tx, Pos.CENTER);
		BorderPane.setAlignment(bt, Pos.CENTER_RIGHT);

		
		
		GuiMsgHelper.applyStandardTooltip(element, description);
    	
		contentPane.getChildren().add(element);
	}
    
    private void enableButton(Button bt, String name, String description, File programFile)
    {
    	GuiMsgHelper.applyStandardTooltip(bt, description);
    	bt.setOnAction((event) -> executeTool(programFile));
    }
    
    
    
    private void executeTool(File programFile)
    {
		Program program = DataControler.loadProgramFile(programFile.getPath(), false, false);
		
		ExecutionStarter.startProgram(program, false, false, false); // Start the program (not simulated)
    }
    
    
    String commandResult = "";
    
	private void executeBash(String bash)
	{
		if (GlobalSettings.destination == null)
		{
			GuiMsgHelper.showMessageNonblockingUI("Executing commands for a device requires an externally\nconnected deploy-target like a Raspberry Pi!\nPlease provide the connection data in the Options-Menu first.");
			return;
		}

		if (!(GlobalSettings.destination instanceof ConnectedExternalLinux))
		{
			GuiMsgHelper.showMessageNonblockingUI("Executing commands for a device requires an externally\nconnected deploy-target like a Raspberry Pi!\nThe commands do not work for Windows yet.");
			return;
		}
		
		ConnectedExternalLinux target = (ConnectedExternalLinux) GlobalSettings.destination;
		
		bash = StringHelpers.trimNewlines(bash);
		String[] commands = bash.split("\n");
		
		
		commandResult = "";
		
		WaitPopup waitPopup = new WaitPopup("Please wait.");
		
		new Thread(()->{
			
			String noResponse = "$NO RESPONSE$";
			String popupWaitFor = "$WAIT FOR$";		
			
			for(String command: commands)
			{
				if (command.startsWith(noResponse))
				{
					String comm = command.substring(noResponse.length()).trim();
					new Thread(() -> target.executeBashCommand(comm)).start(); // Run threaded
					
					continue;
				}
				
				if (command.startsWith(popupWaitFor))
				{					
					int length = Integer.valueOf(command.substring(popupWaitFor.length()).trim());
					waitPopup.showTimer("Seconds: ", length/1000);
					OtherHelpers.sleepNonException(length);
					
					continue;
				}
				
				commandResult += "COMMAND: " + command + "\n";
				commandResult += "RESPONSE:\n" + target.executeBashCommand(command);
				
			}
			
			target.closeCommand();
			waitPopup.close();
			
			if (!commandResult.isEmpty())
				GuiMsgHelper.showMessageNonblockingUI("The commands had the following results:\n\n" + commandResult);
			else
				GuiMsgHelper.showMessageNonblockingUI("Command successful!");
			
		}).start();

	}



	private Button createButton(String text, int perLine)
    {
    	Button bt = new Button(text);
    	bt.setMaxWidth(Double.MAX_VALUE);
    	bt.getStyleClass().add("standardBoldText");
    	bt.getStyleClass().add("mainButtons");    	
    	bt.prefWidthProperty().bind(mainBox.widthProperty().divide(perLine));
    	return(bt);
    }
	
    
	
	private void revert()
	{
		
	}
	
	public void close()
	{
		revert();
		isOpen = false;
		stage.close();
		StageOrderControler.removeStage(stage);
	}
	
	public void onScroll()
	{
	}
	
	
	public static void toFront()
	{
		stage.toFront();
	}
}
