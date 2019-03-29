package productionGUI.controlers;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.controlsfx.control.PopOver;
import org.reactfx.util.FxTimer;

import dataTypes.DesignedImage;
import dataTypes.FunctionalityContent;
import execution.Execution;
import execution.ExecutionStarter;
import execution.handlers.InfoErrorHandler;
import execution.handlers.VariableHandler;
import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import main.DataControler;
import productionGUI.ProductionGUI;
import productionGUI.additionalWindows.MainInfoScreen;
import productionGUI.additionalWindows.OverlayMenu;
import productionGUI.additionalWindows.SettingsMenu;
import productionGUI.additionalWindows.ToolsMenu;
import productionGUI.additionalWindows.WaitPopup;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.elementManagers.StaticAbstractSectionManagerHelper;
import productionGUI.tutorialElements.TutorialControler;
import productionGUI.tutorialElements.TutorialReaderAndMaker;
import settings.EnvironmentDataHandler;
import settings.ExecutionSettings;
import settings.GlobalSettings;
import staticHelpers.FastFadeTransitionHelper;
import staticHelpers.GuiMsgHelper;
import staticHelpers.LocationPreparator;

public class ButtonsRegionControl
{
	@FXML private GridPane buttonsRegionGrid;
	
	@FXML private Button btInfo;
	
	@FXML private Button btUndo;
	@FXML private Button btRedo;
	
	@FXML private Button btNew;	
	@FXML private Button btLoad;
	@FXML private Button btAdd;
	@FXML private Button btSave;

	@FXML private Button btSettings;
	@FXML private Button btTools;
	
	@FXML private Button btSimulate;
	@FXML private Button btDeplRun;
	
	@FXML private CheckBox fulLDebugCheckbox;
	
	
	static ButtonsRegionControl self;
	
	public ButtonsRegionControl()
	{
		self = this;
	}
	public static ButtonsRegionControl getSelf()
	{
		return(self);
	}
	
	
	Rectangle attentionRectangle;
	FadeTransition attentionRectangleTransition;
	
	@FXML
	protected void initialize() throws IOException
	{
		//KeyChecker.addKeyToCheck(KeyCode.F1);
		
		final KeyCombination directSaveComb = new KeyCodeCombination(GlobalSettings.directSaveKey, GlobalSettings.shortcutBaseKey);

		final KeyCombination copyElementsComb = new KeyCodeCombination(GlobalSettings.copyElementsKey, GlobalSettings.shortcutBaseKey);
		final KeyCombination cutElementsComb = new KeyCodeCombination(GlobalSettings.cutElementsKey, GlobalSettings.shortcutBaseKey);
		final KeyCombination insertElementsComb = new KeyCodeCombination(GlobalSettings.insertElementsKey, GlobalSettings.shortcutBaseKey);
		
		final KeyCombination saveAsTestProgramComb = new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN);
		final KeyCombination createNewTutorialComb = new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN);
		
		
		ProductionGUI.getStage().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke)
            {
            	
            	if (ke.getCode() == GlobalSettings.layoutAlternativesKey) // Key command to switch layout
            	{
            		switchLayout(!GlobalSettings.alternativeLayout);
            	}
            	
            	
            	
    			if (directSaveComb.match(ke)) // Key command to save directly
    			{
    				if (ProductionGUI.getCurrentFile().isEmpty()) // no current file
    					Save();
    				else
    				{
    					DataControler.saveProgramFile(ProductionGUI.getCurrentFile(), true);
    					ProductionGUI.setCurrentFile(ProductionGUI.getCurrentFile());
    				}
    				
    				InfoErrorHandler.printEnvironmentInfoMessage("SAVED");
    			}
    			
    			if (saveAsTestProgramComb.match(ke))
    				TestControler.saveCurrentProgramAsTest();
    			
    			if (createNewTutorialComb.match(ke)) // Key command to save directly
    				TutorialReaderAndMaker.beginOrFinishMakingTutorial();
    			
    			
    			// Other shortcuts
    			if (copyElementsComb.match(ke))
    				StaticAbstractSectionManagerHelper.copyElements();

    			if (cutElementsComb.match(ke))
    				StaticAbstractSectionManagerHelper.cutElements();
    			
    			if (insertElementsComb.match(ke))
    				StaticAbstractSectionManagerHelper.insertElements();

    			
            }
		});
		
		
		GuiMsgHelper.applyStandardTooltip(btInfo, "Open the information and tutorial window.");
		
		GuiMsgHelper.applyStandardTooltip(btUndo, "Undo the last Drag and Drop action.");
		GuiMsgHelper.applyStandardTooltip(btRedo, "Redo the last undone Drag and Drop action.");
		
		GuiMsgHelper.applyStandardTooltip(btNew, "Create a new, empty program.");
		GuiMsgHelper.applyStandardTooltip(btLoad, "Load an existing program (." + GlobalSettings.standardProgramFileTermination +" file).");
		GuiMsgHelper.applyStandardTooltip(btAdd, "Add the pages of an existing program to the current one (merges programs).");
		GuiMsgHelper.applyStandardTooltip(btSave, "Save the program to a file. Shortcut: CTRL + S");
		
		GuiMsgHelper.applyStandardTooltip(btSettings, "Opens a window to provide deployment and GUI settings.");
		GuiMsgHelper.applyStandardTooltip(btTools, "Opens a window with various tools.");
		
		GuiMsgHelper.applyStandardTooltip(btSimulate, "Simulates the current program on the actual machine.\nAllows debugging with break-points and tracking.");
		GuiMsgHelper.applyStandardTooltip(btDeplRun, "Deploys the program on a target device accessible by SSH.\nPrepare the required data in the Settings menu!\nResponse will be seen through a command window.");
		
		
		
		
		attentionRectangle = new Rectangle(7, 1, 1, 1);
		attentionRectangle.setArcHeight(5);
		attentionRectangle.setArcWidth(5);
		attentionRectangle.setManaged(false);
		attentionRectangle.setMouseTransparent(true);

		attentionRectangleTransition = new FadeTransition(GlobalSettings.attentionBlinkDuration, attentionRectangle);
		attentionRectangleTransition.setFromValue(GlobalSettings.attentionRectangleMinAlpha*0.75);
		attentionRectangleTransition.setToValue(GlobalSettings.attentionRectangleMaxAlpha*0.75);
		attentionRectangleTransition.setCycleCount(Timeline.INDEFINITE);
		attentionRectangleTransition.setAutoReverse(true);
		
		
		/*
		TutorialControler.addPossibleMark("SIMULATE", btSimulate, () -> {
			Platform.runLater(() -> {
				if (attentionRectangleTransition.getStatus() != Status.RUNNING)
					applyAttentionTo(btSimulate);
				else
					endAttention(null, () -> applyAttentionTo(btSimulate));
			});
		});*/
		
		applyMark(btSimulate);
		applyMark(btDeplRun);
		applyMark(btSettings);
		applyMark(btTools);
		
		
		TutorialControler.addPossibleMark("Stop", btDeplRun, () -> {
			Platform.runLater(() -> {
				
				if (GlobalSettings.fastFade)
				{
					if (!FastFadeTransitionHelper.isRunning(attentionRectangle))
						applyAttentionTo(btDeplRun);
					else
						endAttention(null, () -> applyAttentionTo(btDeplRun));					
				}
				else
				if (attentionRectangleTransition.getStatus() != Status.RUNNING)
					applyAttentionTo(btDeplRun);
				else
					endAttention(null, () -> applyAttentionTo(btDeplRun));
			});
		});
		
		
		updateLayoutType();
		
	}
	
	private void applyMark(Button button)
	{
		// Prevents that the Space key activates the button (to avoid intferring with the regular KeyPressed event for SPACE
		button.addEventFilter(KeyEvent.KEY_PRESSED, k ->
		{
	        if ( k.getCode() == KeyCode.SPACE)
	            k.consume();
	    });
				
		
		TutorialControler.addPossibleMark(button.getText(), button, () -> {
			Platform.runLater(() -> {
				
				if (GlobalSettings.fastFade)
				{
					if (!FastFadeTransitionHelper.isRunning(attentionRectangle))
						applyAttentionTo(button);
					else
						endAttention(null, () -> applyAttentionTo(button));					
				}
				else
				if (attentionRectangleTransition.getStatus() != Status.RUNNING)
					applyAttentionTo(button);
				else
					endAttention(null, () -> applyAttentionTo(button));
			});
		});
	}
	private void applyAttentionTo(Button bt)
	{
		if (attentionRectangle.getParent() != null)
			((HBox) attentionRectangle.getParent()).getChildren().remove(attentionRectangle);
		
		if (!((HBox) bt.getParent()).getChildren().contains(attentionRectangle))
			((HBox) bt.getParent()).getChildren().add(attentionRectangle);
		
		
		attentionRectangle.widthProperty().bind(bt.widthProperty().subtract(2));
		attentionRectangle.heightProperty().bind(bt.heightProperty().subtract(2));
		
		
		attentionRectangle.setFill(Color.LIME);
		
		if (GlobalSettings.fastFade)
			FastFadeTransitionHelper.fade(attentionRectangle, GlobalSettings.attentionRectangleMinAlpha*0.75, GlobalSettings.attentionRectangleMaxAlpha*0.75, (long) GlobalSettings.attentionBlinkDurationFast.toMillis());
		else
		{		
			attentionRectangleTransition.stop();
			attentionRectangleTransition.setDuration(GlobalSettings.attentionBlinkDurationFast);
			attentionRectangleTransition.setCycleCount(2);
			attentionRectangleTransition.play();
					
			attentionRectangleTransition.setOnFinished((ActionEvent event) -> attentionRectangleTransition.play());
		}
	}
	
	private boolean handlePressedButton(Button isThis, Runnable afterwards)
	{
		FastFadeTransitionHelper.stopAll();
		
		// Check during a tutorial
		boolean res = TutorialControler.handleButtonPress(isThis.getText());
		endAttention(isThis, afterwards);
		return(res);
	}
	private void endAttention(Button isThis, Runnable afterwards)
	{		
		if ((isThis == null) || isThis.getParent().getChildrenUnmodifiable().contains(attentionRectangle))
		{			
			Platform.runLater(() -> { // Run later to enable this to work from any thread
				if (GlobalSettings.fastFade)
					FastFadeTransitionHelper.fadeout(attentionRectangle, () -> {stopAttention(); if (afterwards != null) afterwards.run();});
				else
					attentionRectangleTransition.setOnFinished((ActionEvent event) -> {stopAttention(); if (afterwards != null) afterwards.run();});
			});
		}
	}

	
	private void stopAttention()
	{
		Platform.runLater(() -> { 
			attentionRectangleTransition.setCycleCount(0);
			attentionRectangleTransition.stop();
			attentionRectangleTransition.setOnFinished(null);
			
			if (GlobalSettings.fastFade)
				FastFadeTransitionHelper.stopInstantly(attentionRectangle);
			
			if (attentionRectangle.getParent() != null)
				((HBox) attentionRectangle.getParent()).getChildren().remove(attentionRectangle);
		});
	}
	
	public void clickFullDebugCK(ActionEvent event)
	{
		event.consume();
	}
	
	
	public void switchLayout(boolean newLayout)
	{
		KeyCode origKey = GlobalSettings.layoutAlternativesKey;
		FxTimer.runLater(
		        Duration.ofMillis(2000),
		        () -> {
		        	GlobalSettings.layoutAlternativesKey = origKey;
		        });
		
		GlobalSettings.layoutAlternativesKey = KeyCode.NONCONVERT;
		GlobalSettings.alternativeLayout = newLayout;
		
		ProductionGUIcontrol.getSelf().updateLayoutType();
		updateLayoutType();
	}


	
	void updateLayoutType()
	{		
		if (GlobalSettings.alternativeLayout)
		{
			//buttonsRegionGrid.setMinHeight(buttonsRegionGrid.getHeight()-10);
			//buttonsRegionGrid.setMaxHeight(buttonsRegionGrid.getHeight()-10);

			buttonsRegionGrid.setMinHeight(38);
			buttonsRegionGrid.setMaxHeight(38);

			/*
			//double totalElements = 12.25;
			double totalElements = 12.5; // 11.25
			
			
			ColumnConstraints normal = new ColumnConstraints();
			normal.setPercentWidth(100/totalElements);

			ColumnConstraints half = new ColumnConstraints();
			half.setPercentWidth(100/(totalElements*2));
			
			ColumnConstraints separ = new ColumnConstraints();
			separ.setPercentWidth(100/(totalElements*13)); // 12
	
			ColumnConstraints large = new ColumnConstraints();
			large.setPercentWidth(100/(totalElements/1.5));
			
			
			buttonsRegionGrid.getRowConstraints().clear();
			buttonsRegionGrid.getColumnConstraints().clear();
			
			
			buttonsRegionGrid.getColumnConstraints().add(half);

			buttonsRegionGrid.getColumnConstraints().add(separ);

			
			buttonsRegionGrid.getColumnConstraints().add(normal);
			buttonsRegionGrid.getColumnConstraints().add(normal);	
			
			buttonsRegionGrid.getColumnConstraints().add(normal);
			buttonsRegionGrid.getColumnConstraints().add(normal);
	
			buttonsRegionGrid.getColumnConstraints().add(normal);
			buttonsRegionGrid.getColumnConstraints().add(normal);
	
			
			buttonsRegionGrid.getColumnConstraints().add(separ);
			
			
			buttonsRegionGrid.getColumnConstraints().add(normal);
			buttonsRegionGrid.getColumnConstraints().add(normal);
	
			
			buttonsRegionGrid.getColumnConstraints().add(separ);
	
			
			
			buttonsRegionGrid.getColumnConstraints().add(large);
			buttonsRegionGrid.getColumnConstraints().add(large);
			
			//*/
		}
		else
		{	
			double totalElements = 13.75; // 12.5

			
			RowConstraints normal = new RowConstraints();
			normal.setPercentHeight(100/totalElements);
			
			RowConstraints separ = new RowConstraints();
			separ.setPercentHeight(100/(totalElements*6));
	
			RowConstraints large = new RowConstraints();
			large.setPercentHeight(100/(totalElements/2));
			
			
			buttonsRegionGrid.getRowConstraints().clear();
			buttonsRegionGrid.getColumnConstraints().clear();


			buttonsRegionGrid.getRowConstraints().add(normal);

			buttonsRegionGrid.getRowConstraints().add(separ);

			
			buttonsRegionGrid.getRowConstraints().add(normal);
			buttonsRegionGrid.getRowConstraints().add(normal);
			
			
			buttonsRegionGrid.getRowConstraints().add(separ);
	
			
			buttonsRegionGrid.getRowConstraints().add(normal);
			buttonsRegionGrid.getRowConstraints().add(normal);
	
			buttonsRegionGrid.getRowConstraints().add(normal);
			buttonsRegionGrid.getRowConstraints().add(normal);
	
			
			buttonsRegionGrid.getRowConstraints().add(separ);
			
			
			buttonsRegionGrid.getRowConstraints().add(normal);
			buttonsRegionGrid.getRowConstraints().add(normal);
	
			
			buttonsRegionGrid.getRowConstraints().add(separ);
	
			
			buttonsRegionGrid.getRowConstraints().add(large);
			buttonsRegionGrid.getRowConstraints().add(large);
		}
		
		
		// Swap
		int ind = 0;
		for (Node el: buttonsRegionGrid.getChildren())
		{
			if (el instanceof HBox)
				if (GlobalSettings.alternativeLayout)
				{
					GridPane.setColumnIndex(el, ind);
					GridPane.setRowIndex(el, 0);
					
					((HBox) el).setMinSize(0,0);
					
					el.getStyleClass().clear();
					if (GridPane.getColumnIndex(el) != 0)
						el.getStyleClass().add("mainButtonsSurroundH");
					
					((Button) (((HBox) el).getChildren().get(0))).setWrapText(false);
				}
				else
				{
					GridPane.setRowIndex(el, ind);
					GridPane.setColumnIndex(el, 0);
					
					
					el.getStyleClass().clear();
					
					if (GridPane.getRowIndex(el) != 0)
						el.getStyleClass().add("mainButtonsSurroundV");
					
					((Button) (((HBox) el).getChildren().get(0))).setWrapText(true);
				}
			else
				((Separator)el).setOrientation(GlobalSettings.alternativeLayout ? Orientation.VERTICAL : Orientation.HORIZONTAL);
			
			ind++;
		}
	}
	
	
	
	// Functions
	
	@FXML
	private void Undo()
	{
		if (!handlePressedButton(btUndo, null))
			return;
		
		UndoRedoControler.getSelf().undo();
	}
	
	@FXML
	private void Redo()
	{
		if (!handlePressedButton(btUndo, null))
			return;
		
		UndoRedoControler.getSelf().redo();		
	}

	
	
	
	@FXML
	public boolean New()
	{
		if (!handlePressedButton(btNew, null))
			return(false);

    	if (ProductionGUI.hasChangedSinceSave())
    		switch(GuiMsgHelper.askQuestion("The current document has not been saved yet!\n\nHint:\nWhile working you can press 'Control + S' \nto save the document to the current file.", new String[]{"Save", "Close"}, true))
    		{
    		case 0:
    			if (!Save()) // If saving has not been successful
    				return(false);
    			else break;
    		case 1: break;
    		default:
    			return(false);
    		}
		
		
		boolean keepFeatures = false;
		
		VariableHandler.clear();
		
		if (DataControler.customFeaturesAdded())
			switch(GuiMsgHelper.askQuestionDirect("Custom features have been added to the Actions, Conditions, Events or Structures.", "Do you want to keep those for the new file?"))
			{
			case -1: return(false);
			case 0: keepFeatures = false; break;
			case 1: keepFeatures = true; break;
			}
		
		DataControler.clearReset(keepFeatures);
		ProductionGUI.setCurrentFile(""); // empty new file
		
		return(true);
	}
	
	
	@FXML
	private void Load()
	{
		if (!handlePressedButton(btLoad, null))
			return;

		openFileLoadDialog(false);
	}
	
	@FXML
	private void Add()
	{
		if (!handlePressedButton(btAdd, null))
			return;

		openFileLoadDialog(true);
	}
	
	
	private void openFileLoadDialog(boolean addinsteadOfLoad)
	{		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Program File");
		
		fileChooser.getExtensionFilters().addAll(GlobalSettings.extensionFilters);
		
		
		
		String lastPath = EnvironmentDataHandler.getLastOpenedPath();
		if (!lastPath.isEmpty())
			fileChooser.setInitialDirectory(new File(lastPath));
		else
			fileChooser.setInitialDirectory(new File(LocationPreparator.getRunnerFileDirectory()));

		
		File loadFile = fileChooser.showOpenDialog(ProductionGUI.getStage());
		
		if (loadFile != null) // If a file has been chosen
		{
			EnvironmentDataHandler.setLastOpenedPath(loadFile.getParent());
			
			if (DataControler.loadProgramFile(loadFile.getPath(), !addinsteadOfLoad, true) != null)
				EnvironmentDataHandler.setLastUsedFile(loadFile.getName());			
		}
	}
	
	
	
	
	@FXML
	public boolean Save()
	{
		handlePressedButton(btSave, null);
		
		boolean saveHidden = false;
		
		if (ContentsSectionManager.getSelf().hasDeactivatedPages())
			switch(GuiMsgHelper.askQuestionDirect("Some program pages are deactivated.\nDo you want to save those as well?"))
			{
			case -1: return(false);
			case 0: saveHidden = false; break;
			case 1: saveHidden = true; break;
			}		
		
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Chose File To Save");
		
		fileChooser.getExtensionFilters().addAll(GlobalSettings.extensionFilters);
		
		
		String lastPath = EnvironmentDataHandler.getLastOpenedPath();
		if (!lastPath.isEmpty())
		{
			File lastDirec = new File(lastPath);
			fileChooser.setInitialDirectory(lastDirec);
			String lastFile = EnvironmentDataHandler.getLastUsedFile();
			
			if (!lastFile.isEmpty())
			{
				File fileCheck = new File(lastDirec.getPath() + File.separator + lastFile);
				if (fileCheck.exists())
					fileChooser.setInitialFileName(lastFile);
				else
					fileChooser.setInitialDirectory(new File(LocationPreparator.getRunnerFileDirectory()));
			}
			else
				fileChooser.setInitialDirectory(new File(LocationPreparator.getRunnerFileDirectory()));
		}
		else
			fileChooser.setInitialDirectory(new File(LocationPreparator.getRunnerFileDirectory()));
		
		
		File saveFile = fileChooser.showSaveDialog(ProductionGUI.getStage());
		
		if (saveFile != null) // If a file has been chosen
		{
			EnvironmentDataHandler.setLastOpenedPath(saveFile.getParent());
			EnvironmentDataHandler.setLastUsedFile(saveFile.getName());
			
			DataControler.saveProgramFile(saveFile.getPath(), saveHidden);
			ProductionGUI.setCurrentFile(saveFile.getPath());
			
			return(true);
		}
		
		return(false);
	}
	

	@FXML
	private void Settings()
	{
		handlePressedButton(btSettings, null);
		
		if (!SettingsMenu.isOpen())
			new SettingsMenu();
		else
			FxTimer.runLater(
			        Duration.ofMillis(250),() -> SettingsMenu.toFront());
	}
	
	@FXML
	public void Info()
	{
		handlePressedButton(btInfo, null);
		
		if (!MainInfoScreen.isOpen())
			new MainInfoScreen();

		FxTimer.runLater(
		        Duration.ofMillis(250),
		        () -> {
					MainInfoScreen.toFront();
					MainInfoScreen.getStage().requestFocus();			        	
		        });
	}

	
	
	
	OverlayMenu infoMenu = null;
	
	@FXML
	public void InfoMenu(ActionEvent event)
	{
		if (infoMenu != null) return;
		
		infoMenu = new OverlayMenu((Node) event.getSource(), () -> infoMenu = null, true, true);
		
		// ToDo: Add shortcuts
		infoMenu.addButton("Last Exercise", () -> {}, false);
		infoMenu.addButton("Last Tutorial", () -> {}, false);
		
		infoMenu.addSeparator("rgb(255, 255, 255);");
		
		Button[] bt = new Button[1];
		bt[0] = infoMenu.addButton("Raspberry GPIO", () -> {
			
			PopOver specialPop = new PopOver();
			specialPop.setDetachable(false);
			
			Pane contentNodeImage = DesignedImage.getOrCreateByName("gpioOverview");
			contentNodeImage.setStyle(GlobalSettings.tooltipStyle);			
			
			specialPop.setAnimated(true);
			specialPop.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
			
			specialPop.setAutoFix(false);
			
			specialPop.setOnHidden((ev) -> {specialPop.setContentNode(null);}); 
			
			specialPop.setContentNode(contentNodeImage);
			specialPop.show(bt[0]);
			
		}, false);
	}
	
	
	

	@FXML
	private void Tools()
	{
		handlePressedButton(btTools, null);
		if (!ToolsMenu.isOpen())
			new ToolsMenu();
		else
			FxTimer.runLater(
			        Duration.ofMillis(250),() -> ToolsMenu.toFront());

	}
	
	
	volatile boolean pressedContinue = false;
	volatile boolean pressedStop = false;
	
	volatile boolean waitingForButtons = false;
	
	@FXML
	private void Simulate()
	{
		if (waitingForButtons)
		{
			pressedContinue = true;
			return;
		}
		
		if (!handlePressedButton(btSimulate, null))
			return;
		
		if (Execution.isRunning())
		if (Execution.isPaused())
		{
			Execution.unpause();
			switchToRunningButtons(false);
			pressedContinue = true;
			return;
		}
		
		if (!Execution.isRunning()) // if not running
		{
			Execution.unpause();
			new Thread(() -> ExecutionStarter.startLoadedProgram(true, true, Execution.isTracked())).start(); // Start the program (simulated)
		}
		
		pressedContinue = true;
		
		if (Execution.isRunning()) 
		{
			Execution.pause();
			switchToRunningButtons(true);
		}
	}
	
	
	boolean eventDebug = true;
	boolean fullDebug = false;
	boolean externalTracking = false;
	
	
	OverlayMenu simulateSettingsMenu = null;
	
	@FXML
	private void SimulateSettings(ActionEvent event)
	{
		if (simulateSettingsMenu != null) return;
		
		simulateSettingsMenu = new OverlayMenu((Node) event.getSource(), () -> simulateSettingsMenu = null, true, true);
		
		simulateSettingsMenu.addStandardToggle("Breakpoints Active", ExecutionSettings.breakPointsActive, "You can set elements to be Break-Points by holding <SHIFT> and rightclick.\nWhen execution hits a Break-Point,\na popup-menu will be shown and the event pauses.", () -> ExecutionSettings.breakPointsActive = false, () -> ExecutionSettings.breakPointsActive = true);
		simulateSettingsMenu.addStandardToggle("Step-by-Step Simulation", ExecutionSettings.executeStepByStep, "A pop-up message will be shown after every executed element.\nIf an event is directly a breakpoint, only the commands of that event\nare executed step by step.", () -> ExecutionSettings.executeStepByStep = false, () -> ExecutionSettings.executeStepByStep = true);
		simulateSettingsMenu.addStandardToggle("Ignore PopUps", ExecutionSettings.ignorePopUps, "If checked, Pop-Up messages which block the event are ignored\nand only printed to command line. Break-Points and error messages still occur.", () -> ExecutionSettings.ignorePopUps = false, () -> ExecutionSettings.ignorePopUps = true);
		simulateSettingsMenu.addStandardToggle("Tracking", Execution.isTracked(), "If enabled, every element of the program will send a line of text to the output window.\nThis allows to track accurately in what order, the program has performed.", () -> Execution.setTracked(false), () -> Execution.setTracked(true));
		
		simulateSettingsMenu.addStandardToggle("Output Window", GlobalSettings.showConsoleWindow, "Show the output text window.", () -> GlobalSettings.showConsoleWindow = false, () -> {GlobalSettings.showConsoleWindow = true; if (Execution.isRunning()) Execution.prepareConsole("Program Output (Console)", true);});
		simulateSettingsMenu.addStandardToggle("Debug Window", GlobalSettings.showDebugWindow, "Show the debug window. Click on values at runtime to edit!", () -> GlobalSettings.showDebugWindow = false, () -> GlobalSettings.showDebugWindow = true);
	}
	
	
	OverlayMenu deplRunSettingsMenu = null;
	
	@FXML
	private void DeplRunSettings(ActionEvent event)
	{
		if (deplRunSettingsMenu != null) return;
		
		deplRunSettingsMenu = new OverlayMenu((Node) event.getSource(), () -> deplRunSettingsMenu = null, true, true);
		
		deplRunSettingsMenu.addStandardToggle("Event Debug", eventDebug, "When an event is triggered by the program, it will be visualized here in the main windows\njust like during a simulation.\nNote that this works through the command line and therefore\ncan have a slight performance impact in fast programs.", () -> eventDebug = false, () -> eventDebug = true);
		deplRunSettingsMenu.addStandardToggle("Full Debug", fullDebug, "When an action or event is executed, it will be\nvisualized here in the main windows just like during a simulation.\nNote that this works through the command line and therefore\ncan have a significant performance impact in fast programs.", () -> fullDebug = false, () -> fullDebug = true);
		
		if (GlobalSettings.destination != null)
			deplRunSettingsMenu.addStandardToggle("Tracking", externalTracking, "If enabled, every element of the program will send a line of text to the output window.\nThis allows to track accurately in what order, the program has performed.\nNote that this can slow down execution under certain circumstances.", () -> externalTracking = false, () -> externalTracking = true);
		
		deplRunSettingsMenu.addStandardToggle("Output Window", GlobalSettings.showConsoleWindow, "Show the output text window.", () -> GlobalSettings.showConsoleWindow = false, () -> {GlobalSettings.showConsoleWindow = true; if (Execution.isRunning()) Execution.prepareConsole("Program Output (Console)", true);});
		deplRunSettingsMenu.addStandardToggle("Debug Window", GlobalSettings.showDebugWindow, "Show the debug window. Click on values at runtime to edit!", () -> GlobalSettings.showDebugWindow = false, () -> GlobalSettings.showDebugWindow = true);
	}

	
	
	@FXML
	private void DeplRun()
	{		
		if (!handlePressedButton(btDeplRun, null))
			return;
		
		
		FunctionalityContent[] cont = new FunctionalityContent[1];
		ExecutionStarter.applyToAllcontent(cont, () -> cont[0].getVisualization().getControlerOnGUI().fadeoutMarking(true));
		
		
		if (Execution.isRunningDeployed()) // External running
		{
			GlobalSettings.destination.forceQuit();
			switchToStandardButtons();
		}
		else
		if (!Execution.isRunning()) // if not running
		{
			
			new Thread(() ->
			{
				
				WaitPopup popup = new WaitPopup("Establishing connection.\nTransfering Data.\n\nPlease wait.");
				
				DataControler.saveProgramFile(ProductionGUI.getCurrentTempFile(), true);
				
				if (GlobalSettings.destination.deploy()) // if successful
				{				
					ExecutionStarter.prepareDebugProgramIndices();				
					switchToRunningButtons(false);
					
					String additionalArgs = GlobalSettings.keyCheckForRunning;
					if (fullDebug)
						additionalArgs += " " + GlobalSettings.keyFullDebug;
					else
					if (eventDebug)
						additionalArgs += " " + GlobalSettings.keyEventDebug;
	
					if (externalTracking)
						additionalArgs += " " + GlobalSettings.keyExternalTracking;
	
					GlobalSettings.destination.executeAndHandleDeployed(additionalArgs); // execute with corresponding arguments
				}
				
				popup.close();
				
			}).start();;
		}
		else
		{
			Execution.stop();
		}
		
		pressedStop = true;
	}
	
	
	
	double absoluteWidth = 0;
	
	public void switchToRunningButtons(boolean offerContinue)
	{
		Platform.runLater(() -> {
			if (absoluteWidth == 0)
				absoluteWidth = Math.max(btSimulate.getWidth(), btDeplRun.getWidth())-40;
			
			btSimulate.setMinWidth(absoluteWidth);
			btDeplRun.setMinWidth(absoluteWidth);
			
			btSimulate.setPrefWidth(absoluteWidth);
			btDeplRun.setPrefWidth(absoluteWidth);
			
			
			if (offerContinue)
				btSimulate.setText("CONTINUE");
			else
				btSimulate.setText("PAUSE");
		
			if (!btSimulate.getStyleClass().contains("mainButtonsHighlighted"))
				btSimulate.getStyleClass().add("mainButtonsHighlighted");
			
			btDeplRun.setText("STOP");
			if (!btDeplRun.getStyleClass().contains("mainButtonsHighlighted"))
				btDeplRun.getStyleClass().add("mainButtonsHighlighted");
		});
		
		pressedStop = false;
		pressedContinue = false;
	}
	
	public void switchToStandardButtons()
	{
		Platform.runLater(() -> {			
			btSimulate.setText("SIMULATE");
			btSimulate.getStyleClass().removeAll("mainButtonsHighlighted");
			
			btDeplRun.setText("DEPLOY");
			btDeplRun.getStyleClass().removeAll("mainButtonsHighlighted");
		});
		
		
		pressedStop = false;
		pressedContinue = false;
	}
	
	
	
	public boolean hasPressedContinue()
	{
		if (pressedContinue)
		{
			pressedContinue = false;
			return(true);
		}
		return(false);
	}
	public boolean hasPressedStop()
	{
		if (pressedStop)
		{
			pressedStop = false;
			return(true);
		}
		return(false);
	}
	
	public void currentlyWaitingForButtons()
	{
		waitingForButtons = true;		
	}
	
	public void finishedWaitingForButtons()
	{
		waitingForButtons = false;		
	}
	
	
	
	
	
	
}
