package productionGUI;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.reactfx.util.FxTimer;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import execution.Program;
import execution.handlers.InfoErrorHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.DataControler;
import main.MainControler;
import main.functionality.Functionality;
import otherHelpers.DragAndDropHelper;
import productionGUI.additionalWindows.MainInfoScreen;
import productionGUI.controlers.ButtonsRegionControl;
import productionGUI.controlers.StageOrderControler;
import productionGUI.controlers.UndoRedoControler;
import productionGUI.guiEffectsSystem.EffectsClient;
import productionGUI.sections.elementManagers.ActionsSectionManager;
import productionGUI.sections.elementManagers.ConditionsSectionManager;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.elementManagers.EventsSectionManager;
import productionGUI.sections.elementManagers.StructuresSectionManager;
import productionGUI.targetConnection.TargetConnection;
import productionGUI.tutorialElements.TutorialControler;
import settings.EnvironmentDataHandler;
import settings.GlobalSettings;
import settings.HaveDoneFileHandler;
import staticHelpers.FastFadeTransitionHelper;
import staticHelpers.GuiMsgHelper;
import staticHelpers.KeyChecker;
import staticHelpers.LocationPreparator;
import staticHelpers.StringHelpers;

public class ProductionGUI extends Application
{
	private static List<Runnable> finalizationEvents = new ArrayList<Runnable>();
	
	public static Stage primaryStage;
	public static Scene scene;
	
	private static boolean available = false;
	
	private volatile static boolean changedSinceSave = false;
	private volatile static boolean hasLoaded = false;

	private volatile static String currentDocumentPath = "";
	private volatile static String currentDocumentName = "";
	
	private static Rectangle2D primaryScreenBounds;	
	
	private final static Program visualizedProgram = new Program(); // the unique, visualized program
	
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		available = true;
		ProductionGUI.primaryStage = primaryStage;
		
		createMainPage();
		Functionality.loadElements(true);
		
		
		try {
			EnvironmentDataHandler.init(); // handle the environment file
			HaveDoneFileHandler.init();
			FastFadeTransitionHelper.init();
			
			GlobalSettings.destination = TargetConnection.readDeploymentSettings(LocationPreparator.getExternalDirectory()); // Read the deployment setting
			
			// Main interface through FXML
			//Pane root = FXMLLoader.load(Paths.get("src/productionGUI/ProductionGUI.fxml").toUri().toURL());
			Pane root = FXMLLoader.load(getClass().getResource("/productionGUI/ProductionGUI.fxml"));

			scene = new Scene(root,GlobalSettings.productionWindowWidth,GlobalSettings.productionWindowHeight);
			
			if (LocationPreparator.isCompiled())
				primaryStage.setMaximized(true);
			
			primaryStage.setTitle(GlobalSettings.titleLineBaseString);

			primaryStage.setScene(scene);
			primaryStage.show();
			
			StageOrderControler.addBaseStage(primaryStage);
			
			KeyChecker.initForStage(ProductionGUI.getStage());
			
			
			primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			
			
			KeyChecker.addKeyToCheck(KeyCode.E);
			
			
			new UndoRedoControler();

			
			/**
			 * What happens when closing the window
			 */
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	            @Override
	            public void handle(WindowEvent ev)
	            {
	            	if (changedSinceSave)
	            		switch(GuiMsgHelper.askQuestion("The current document has not been saved yet!\n\nHint:\nWhile working you can press 'Control + S' \nto save the document to the current file.", new String[]{"Save", "Close"}, true))
	            		{
	            		case 0:
	            			if (!ButtonsRegionControl.getSelf().Save()) // If saving has been successful
	            			{
	            				ev.consume();
	            				return;
	            			}
	            			else break;
	            		case 1: break;
	            		default:
	            			ev.consume();
	            			return;
	            		}
	            	
	            	if ((GlobalSettings.destination != null) && GlobalSettings.destination.isRunning())
	            	{
	            		switch(GuiMsgHelper.askQuestion("A deployed program is still running! To you want to quit it now?\nOtherwise it will keep running.", new String[]{"Quit", "Keep"}, true))
	            		{
	            		case 0: GlobalSettings.destination.forceQuit(); break;
	            		case 1: break;
	            		default: ev.consume(); return;
	            		}
	            	}
	            	
	                Platform.exit();
	                System.exit(0);
	            }
			});
			
				        
			
			primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>()
			{
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
				{
					
					/*
					
					if (!oldValue)
					if (newValue)
						StageOrderControler.rearangeStages();
					
					*/
					
					
					/*
					FxTimer.runLater(
					        Duration.ofMillis(100),
					        () -> Execution.putConsoleOntop());
					        */
					        
					        
					
					//if (newValue)
						//Execution.putConsoleOntop();
				}
			});
			

			// Execute events for finalisation
			for(Runnable event: finalizationEvents)
				event.run();

			
			DragAndDropHelper.initDragDummyArea(scene);
			
			

			///////
			
			// Checking the last opened file and path
			if (EnvironmentDataHandler.lastFileFullExists())
			{
				if (DataControler.loadProgramFile(EnvironmentDataHandler.getLastFileFull(), true, true) == null)
					EnvironmentDataHandler.setLastUsedFile("");// Load it if it exists
			}
			else
				InfoErrorHandler.printEnvironmentInfoMessage("Last loaded file does not exist anymore.");
			
			
			/*
			 *  Modes:
			 *  
			 *  0: Not possible
			 *  1: Move and copy recreating (keep in source and target)
			 *  2: Move and keep the data (delete from source)
			 *  3: Only delete from source
			 *  4: Move and copy and keep the data (keep in source and target)
			 */
			
			DragAndDropHelper.addDragDropInteraction(ActionsSectionManager.class, ContentsSectionManager.class, 4);
			DragAndDropHelper.addDragDropInteraction(ConditionsSectionManager.class, ContentsSectionManager.class, 4);
			DragAndDropHelper.addDragDropInteraction(EventsSectionManager.class, ContentsSectionManager.class, 4);
			DragAndDropHelper.addDragDropInteraction(StructuresSectionManager.class, ContentsSectionManager.class, 4);
			
			
			DragAndDropHelper.addDragDropInteraction(ContentsSectionManager.class, ContentsSectionManager.class, 2);
			
			DragAndDropHelper.addDragDropInteraction(ActionsSectionManager.class, ActionsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(ConditionsSectionManager.class, ConditionsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(EventsSectionManager.class, EventsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(StructuresSectionManager.class, StructuresSectionManager.class, 0);


			DragAndDropHelper.addDragDropInteraction(ActionsSectionManager.class, ConditionsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(ActionsSectionManager.class, EventsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(ActionsSectionManager.class, StructuresSectionManager.class, 0);

			DragAndDropHelper.addDragDropInteraction(EventsSectionManager.class, ActionsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(EventsSectionManager.class, ConditionsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(EventsSectionManager.class, StructuresSectionManager.class, 0);

			DragAndDropHelper.addDragDropInteraction(StructuresSectionManager.class, ActionsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(StructuresSectionManager.class, ConditionsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(StructuresSectionManager.class, EventsSectionManager.class, 0);

			DragAndDropHelper.addDragDropInteraction(ConditionsSectionManager.class, ActionsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(ConditionsSectionManager.class, EventsSectionManager.class, 0);
			DragAndDropHelper.addDragDropInteraction(ConditionsSectionManager.class, StructuresSectionManager.class, 0);


			DragAndDropHelper.addDragDropInteraction(ContentsSectionManager.class, ActionsSectionManager.class, 4);
			DragAndDropHelper.addDragDropInteraction(ContentsSectionManager.class, ConditionsSectionManager.class, 4);
			DragAndDropHelper.addDragDropInteraction(ContentsSectionManager.class, EventsSectionManager.class, 4);
			DragAndDropHelper.addDragDropInteraction(ContentsSectionManager.class, StructuresSectionManager.class, 4);
			
			

			EffectsClient.init();
			
			
			//if (((currentDocumentName == null) || (currentDocumentName.isEmpty())))
				for(Entry<String, FunctionalityContent> entr: TutorialControler.getElementMarks().entrySet())
					if (entr.getValue().getVisualization().getControlerOnGUI() != null)
						if (entr.getValue().getVisualization().getNode().getChildren().size() > 0)
							entr.getValue().getVisualization().getControlerOnGUI().forceCollapse(true); // collapse all elements
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		FxTimer.runLater(
		        Duration.ofMillis(250),
		        () -> {
			        MainControler.closeStartWindow();
					if (!HaveDoneFileHandler.haveDone("Closed Info Window", false))
						new MainInfoScreen();
			});
		
		
		KeyChecker.addPressedHook(KeyCode.F1, () -> {if (!MainInfoScreen.isOpen()) new MainInfoScreen();});
		
	}
	
	/**
	 * Add an event to launch when the scene is laoded (some JavaFX functions cannot be called earlier)
	 * @param event
	 */
	public static void addFinalizationEvent(Runnable event)
	{
		finalizationEvents.add(event);
	}

	public static Scene getScene()
	{
		return(scene);
	}
	
	public static Stage getStage()
	{
		return(primaryStage);
	}

	
	
	public static void hasChangedContent()
	{
		if (!changedSinceSave)
			primaryStage.setTitle(primaryStage.getTitle()+"*");			
		
		changedSinceSave = true;
	}
	public static void setCurrentFile(String filePath)
	{
		currentDocumentPath = filePath;
		
		if (filePath.isEmpty())
		{
			currentDocumentName = "";
			primaryStage.setTitle(GlobalSettings.titleLineBaseString);
			changedSinceSave = true;
			return;
		}
		
		String[] pathSpl = filePath.split(Pattern.quote(File.separator));
		currentDocumentName = pathSpl[pathSpl.length-1];
		
		primaryStage.setTitle(GlobalSettings.titleLineBaseString + currentDocumentName);					
		changedSinceSave = false;
	}
	public static String getCurrentFile()
	{
		return(currentDocumentPath);
	}
	public static String getCurrentDocumentName()
	{
		return(currentDocumentName);
	}	
	
	public static boolean hasChangedSinceSave()
	{
		return(changedSinceSave);
	}
	
	
	public static void setLoadedFile(boolean loaded)
	{
		hasLoaded = loaded;
	}

	
	public static boolean hasLoadedFile()
	{		
		return(hasLoaded);
	}


	public static boolean alreadyHasLoadedFile()
	{
		return(!GlobalSettings.titleLineBaseString.equals(primaryStage.getTitle()));
	}

	public static boolean isAvailable()
	{
		return(available);
	}

	private static String lastTempFile = "";

	
	
	public static String getCurrentDocumentNameOrUnsaved()
	{
		if (currentDocumentName.isEmpty())
			return("unsaved" + GlobalSettings.deployTempStr + "." + GlobalSettings.standardProgramFileTermination);
		else
			return(currentDocumentName);
	}
	
	public static String getCurrentTempFile()
	{
		String str;
		if (getCurrentFile().isEmpty())
			str = "unsaved" + GlobalSettings.deployTempStr + "." + GlobalSettings.standardProgramFileTermination;
		else
			str = StringHelpers.insertAt(getCurrentFile(), - (GlobalSettings.standardProgramFileTermination.length()+1), GlobalSettings.deployTempStr);
		
		if (!lastTempFile.isEmpty())
		if (str != lastTempFile)
		{
			File f = new File(lastTempFile);
			if (f.exists()) f.delete();			
			lastTempFile = str;
		}
		
		new File(str).deleteOnExit();
		
		return(str);
	}

	public static Rectangle2D getPrimaryScreenBounds()
	{
		return(primaryScreenBounds);
	}
	
	public static Program getVisualizedProgram()
	{
		return(visualizedProgram);
	}
	
	
	public static DataNode<ProgramElement> getOrCreateMainPage()
	{
		if (visualizedProgram.containsPage(ContentsSectionManager.getStartPageName()))
			return(visualizedProgram.getPageRoot(ContentsSectionManager.getStartPageName()));
		
		return(createMainPage());
	}

	
	public static DataNode<ProgramElement> createMainPage()
	{
		DataNode<ProgramElement> newNode = new DataNode<ProgramElement>(null);
		
		visualizedProgram.addPage(ContentsSectionManager.getStartPageName(), newNode);		
		
		return(newNode);
	}

	
	public static void toFront()
	{
		Platform.runLater(() -> getStage().toFront());
	}



}
