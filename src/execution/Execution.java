package execution;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import dataTypes.FunctionalityContent;
import dataTypes.specialContentValues.Variable;
import execution.handlers.InfoErrorHandler;
import javafx.application.Platform;
import javafx.stage.Stage;
import main.functionality.SharedComponents;
import main.functionality.helperControlers.AlarmControler;
import otherHelpers.InitWindow;
import productionGUI.ProductionGUI;
import productionGUI.additionalWindows.DataConsole;
import productionGUI.additionalWindows.VariableOverviewList;
import productionGUI.controlers.ButtonsRegionControl;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.elements.VisualizableProgramElement;
import productionGUI.tutorialElements.TutorialControler;
import settings.GlobalSettings;
import staticHelpers.GuiMsgHelper;
import staticHelpers.LocationPreparator;
import staticHelpers.OtherHelpers;

@SuppressWarnings("deprecation")
public class Execution
{
	public static boolean pure = true; // Is set to false if any modifiers like simulated, visualizedInGUI or tracked are active.
	
	private static boolean simulated = false;
	private static boolean visualizedInGUI = false;
	private static boolean tracked = false;
	
	private static boolean hadError = false;
	
	private static DataConsole console;
	private static VariableOverviewList varOverview;

	
	volatile static Runnable finishProgramTask;
	
	volatile private static boolean programRunning = false;
	volatile private static boolean paused = false;
	
	volatile private static List<EventInstance> runningEvents = Collections.synchronizedList(new ArrayList<EventInstance>());
	volatile private static List<Stage> activeExternalStages = Collections.synchronizedList(new ArrayList<Stage>());
	volatile private static String error = "";
	
	volatile private static long currentVagueTime = System.currentTimeMillis();
	private static Thread timeEvaluationThread = null;
	
	private volatile static List<String> trackText = new ArrayList<>();
	
	
	private volatile static boolean alreadyFinished = false;
	private volatile static boolean noAutoQuit = false;
	
	
	public static boolean isSimulated()
	{
		return(simulated);
	}
	
	public static boolean isRunningInGUI()
	{
		return(visualizedInGUI);
	}
	
	public static boolean isTracked()
	{
		return(tracked);
	}
	
	public static boolean isRunning()
	{
		return(programRunning);
	}
	
	public static boolean isPaused()
	{
		return(paused);
	}
	
	public static boolean isRunningDeployed()
	{
		if (GlobalSettings.destination == null)
			return(false);
		return(GlobalSettings.destination.isRunning());
	}
	
	
	
	
	synchronized public static void print(String message)
	{
		if (console != null)
			console.externallyAddLine(message);
		
		if (tracked)
			trackText.add(trackText.size() + ": " + message);
		
		System.out.println(message);
	}
	
	
	public static void updateDebugVariable(String identifier, String type, String value)
	{
		updateDebugVariable(identifier, type, value, false);
	}
	public static void updateDebugVariable(String identifier, String type, String value, boolean ignoreIfOften)
	{
		if (isSimulated())
		{
			VariableOverviewList.updateVariable(identifier, type, value, false, false, ignoreIfOften);
			
			if (tracked)
			{
				StringBuilder str = new StringBuilder();
				
				str.append("Simulated value '");			
				str.append(identifier);
				str.append("' of type '");
				str.append(type);
				str.append("' performed with/to: ");
				str.append(value);

				print(str.toString());
			}
		}
		else
		if (GlobalSettings.eventDebug && (!ignoreIfOften))
		{
			StringBuilder str = new StringBuilder(GlobalSettings.innerDataChangeSignal);
			
			str.append(identifier);			
			str.append("|");
			str.append(type);
			str.append("|");
			str.append(value);
			
			print(str.toString());
		}
	}

	public static void addFunctionalDebugVariable(String identifier, String type, String value, Runnable changeFunc)
	{
		if (isSimulated())
		{
			VariableOverviewList.updateVariable(identifier, type, value, false, true, changeFunc);
			
			if (tracked)
			{
				StringBuilder str = new StringBuilder();
				
				str.append("Simulated value '");			
				str.append(identifier);
				str.append("' of type '");
				str.append(type);
				str.append("' performed with/to: ");
				str.append(value);
				
				print(str.toString());
			}
		}
		else
			updateDebugVariable(identifier, type, value);
	}
	
	
	
	
	
	
	// Setters - all protected to be only accessible through the exxecution core
	
	protected static void setSimulated(boolean simulated)
	{
		Execution.simulated = simulated;
	}
	protected static void setRunningInGUI(boolean visualized)
	{
		Execution.visualizedInGUI = visualized;
	}
	public static void setTracked(boolean isTracked)
	{
		trackText.clear();
		tracked = isTracked;
	}
	
	/*
	public static void resetConsole()
	{
		if (console != null)
			console.reset();
	}
	*/
	
	public static void prepareConsole(String title)
	{
		prepareConsole(title, false);
	}
	public static void prepareConsole(String title, boolean checkFirst)
	{
		if (checkFirst)
			if (console != null)
				if (console.isVisible() && console.isReady())
					return;
		
		boolean[] test = new boolean[1];
		test[0] = false;
		new Thread(() -> {
			Platform.runLater(() -> 
			{				
				if (!GlobalSettings.showConsoleWindow)
					console.close();
				if ((console != null) && console.isVisible())
					console.reset();
				else
					if (GlobalSettings.showConsoleWindow)
						console = new DataConsole(title);
				
				
				if (!GlobalSettings.showDebugWindow)
					varOverview.close();				
				if ((varOverview != null) && varOverview.isVisible())
					varOverview.reset();
				else
					if (GlobalSettings.showDebugWindow)
						varOverview = new VariableOverviewList();
				
				
				console.setSubtitle("Program: " + ProductionGUI.getCurrentDocumentName());
				
				console.toFront();
				varOverview.toFront();

				test[0] = true;
			});
		}).start();
		
		
		if (!checkFirst)
		{
			while( (!test[0]) || (console == null) || (!console.isReady()) || (varOverview == null) || (!varOverview.isReady()))
				OtherHelpers.sleepNonException(20);
			varOverview = VariableOverviewList.getSelf();
			console = DataConsole.getSelf();
		}
	}
	
	public static void finalizeConsole(InputStream inputStream, InputStream errorStream)
	{
		if (console != null)
		{
			console.addConstantStreamLines(inputStream, "");
			console.addConstantStreamLines(errorStream, "-fx-fill: CRIMSON;");
		}
	}
	
	/*
	public static void putConsoleOntop()
	{
		if (console != null)
			console.toFront();
	}*/

	
	


	protected static void start()
	{	
		pure = ((!simulated) && (!visualizedInGUI) && (!tracked));
		
		error = null;
				
		alreadyFinished = false;
		noAutoQuit = false;
		
		if (timeEvaluationThread == null)
		{
			timeEvaluationThread = new Thread(() ->
			{
				while(true)
				{
					currentVagueTime = System.currentTimeMillis();
					
					AlarmControler.checkAlarms(currentVagueTime);
					
					try {
						Thread.sleep(GlobalSettings.constantCheckDelay);
					} catch (InterruptedException e) {}
				}
			});
			timeEvaluationThread.start();
		}
		
		programRunning = true;
		hadError = false;
		
		if (visualizedInGUI)
		{
			TutorialControler.signalizeThatExecuted();
			ButtonsRegionControl.getSelf().switchToRunningButtons(false);
		}
	}
	
	public static void stop()
	{
		unpause();
		
		if (!isRunning())
			return;
		
		stopAllEvents();
		if (visualizedInGUI)
			ButtonsRegionControl.getSelf().switchToStandardButtons();
	}

	public static void pause()
	{
		if(!paused)
			InfoErrorHandler.printExecutionInfoMessage("PROGRAM PAUSED");
		
		paused = true;
	}
	
	public static void unpause()
	{
		if (paused)
		InfoErrorHandler.printExecutionInfoMessage("PROGRAM CONTINUES");

		paused = false;
	}
	
	
	
	protected static void addRunningEvent(EventInstance eventHandler)
	{
		//if (!runningEvents.contains(eventHandler))
			runningEvents.add(eventHandler);				
	}
	
	protected static void removeRunningEvent(EventInstance eventHandler)
	{
		runningEvents.remove(eventHandler);
		
		if (!alreadyFinished)
		if (runningEvents.isEmpty() && !AlarmControler.hasAlarms() && !noAutoQuit)
		{
			closeExternalStages();
			programRunning = false;
			if (visualizedInGUI)
				ButtonsRegionControl.getSelf().switchToStandardButtons();
			
			InfoErrorHandler.printExecutionInfoMessage("No more events running. PROGRAM FINISHED!");
			if (finishProgramTask != null)
				finishProgramTask.run();
			finishProgramTask = null;
			
			alreadyFinished = true;
		}
		
	}
	
	
	private static boolean aWindowInitialized = false;
	
	public static void addActiveExternalStages(Stage stage, boolean focusableWindow)
	{
		if (focusableWindow)
			aWindowInitialized = true;

		activeExternalStages.add(stage);				
	}
	
	public static int countActiveExternalStages()
	{
		return(activeExternalStages.size());
	}
	
	public static void removeActiveExternalStages(Stage stage)
	{
		activeExternalStages.remove(stage);
		
		if (stage.isFocused())
		{
			if (activeExternalStages.isEmpty())
			{
				if (isRunningInGUI())
					Platform.runLater(() -> ProductionGUI.primaryStage.requestFocus());
			}
			else
			{
				//Stage lstage = activeExternalStages.get(activeExternalStages.size()-1);
				
				//if (lstage.isShowing())
					//Platform.runLater(() -> lstage.requestFocus());
			}
		}
	}
	
	public static boolean moreActiveStagesPossible()
	{
		return(activeExternalStages.size() < GlobalSettings.maxPossibleExternalStages);
	}


	public static boolean JFXgraphicsAvailable()
	{
		return(aWindowInitialized);
	}
	
	
	
	
	
	protected static void stopAllEvents()
	{
		programRunning = false;
		
		unpause();
		
		new Thread() {
			public void run()
			{
				InfoErrorHandler.printExecutionInfoMessage("EVENTS TO STOP: " + runningEvents.size());	
				
				AlarmControler.clearAlarms();
				
				synchronized (runningEvents)
				{
					Iterator<EventInstance> events = runningEvents.iterator(); // Must be in synchronized block
				    while (events.hasNext())
				    {
				    	events.next().stopExecution();
				    }
				}
					
				closeExternalStages();
					
				try {
					Thread.sleep(GlobalSettings.waitForEventsEndTime/10);
				} catch (InterruptedException e) { e.printStackTrace(); }
					
				boolean killAll = false;
				
				
				while(!runningEvents.isEmpty())
				{					
					try {	
						Thread.sleep(GlobalSettings.waitForEventsEndTime);
					} catch (InterruptedException e) { e.printStackTrace(); }
						
					if (!runningEvents.isEmpty() && visualizedInGUI)
					{
						
						int res = GuiMsgHelper.askQuestionDirect("Not all events have finished yet (" + runningEvents.size() +" running).\nDo you want to force-kill all events?", "Unfortunately this might result in unexpected behavior.");
						if (res == 1)
						{
							killAll = true;
							break;
						}				
						if (res == -1)
						{
							AlarmControler.clearAlarms();
							return;
						}
					}
				}
					
				if (visualizedInGUI)
				{
					for(String name: ProductionGUI.getVisualizedProgram().getPages())
					{	
						VisualizableProgramElement[] node = new VisualizableProgramElement[1];
						ProductionGUI.getVisualizedProgram().getPageRoot(name).applyToChildrenTotal(node, () -> {
							
							if (node[0].getControlerOnGUI() != null)
								node[0].getControlerOnGUI().fadeoutMarking(); // fadeout executing of all elements
								
						}, true);
					}
						
					ButtonsRegionControl.getSelf().switchToStandardButtons();
				}
				
				
				if (killAll)
					killAllEvents();
					
				runningEvents.clear();
				
				AlarmControler.clearAlarms();
				
				InfoErrorHandler.printExecutionInfoMessage("PROGRAM STOPPED!");
		
			}}.start();
			
			//if (!isRunningInGUI())
				SharedComponents.quitGlobalObjects();
	}
	
	
	private static void closeExternalStages()
	{
		if (JFXgraphicsAvailable())
		Platform.runLater(() -> {
			synchronized (activeExternalStages)
			{
					for(Stage extStage: activeExternalStages)
						if (extStage.isShowing())
							extStage.close();
			}		
		});
	}

	private static void killAllEvents()
	{
		InfoErrorHandler.printExecutionInfoMessage("TRYING TO KILL THREADS");
		
		synchronized (runningEvents)
		{
			Iterator<EventInstance> events = runningEvents.iterator(); // Must be in synchronized block
		    while (events.hasNext())
		    	events.next().stopThread(); // Using it for any case because the alternative would be to force all contents of "Functionality" not to take care of the possibility that a thread is killed. However NORMALLY all execution-relevant loops listen to the volatile programRunning variable.

		    runningEvents.clear();
		}
	}
	
	
	synchronized public static boolean hasError()
	{
		return(!(error == null || error.isEmpty()));
	}
	synchronized public static void setError(String errorMessage, boolean instantError)
	{
		if (!isRunning())
			return;
		
		error = errorMessage;
		
		if (instantError)
		{
			InfoErrorHandler.printExecutionErrorMessage("Problem: " + errorMessage);
			throwException();
		}
	}
	synchronized public static void throwException()
	{
		throw new RuntimeException();
	}
	public static String getError()
	{
		return(error);
	}
	
	public static void init(boolean showGUI)
	{
		if (LocationPreparator.isWindows() && showGUI)
		{
			new Thread( new Runnable() {
			    @Override
			    public void run() {
					javafx.application.Application.launch(InitWindow.class); // Launch the window in its own thread
			    }
			}).start();
			
			Platform.setImplicitExit(false);

			while(!InitWindow.hasFinishedLaunch()) {try { Thread.sleep(1); } catch (InterruptedException e) {}}
			InitWindow.close();
			
			Platform.setImplicitExit(false);
			

			//com.sun.javafx.application.PlatformImpl.startup(() -> {int a = 0; a++;});
			
			aWindowInitialized = true;
		}
	}

	public static int getRunningEvents()
	{
		return(runningEvents.size());
	}
	
	
	
	
	private static FunctionalityContent lastContent = null;

	public static boolean specialInterpretLine(String line)
	{
		if (!ProductionGUI.isAvailable())
			return(true);
		
		FunctionalityContent content = null;
		
		if (line.startsWith(GlobalSettings.errorSignal))
		{			
			if (lastContent != null)
				lastContent.getVisualization().getControlerOnGUI().fadeoutMarking();
			lastContent = null;
			
			content = ExecutionStarter.getProgramContent(line.substring(6));
			
			if (content != null)
			{
				if (!content.getCodePageName().equals(ContentsSectionManager.getSelf().getCurrentPage()))
					return(false);
			
				content.getVisualization().getControlerOnGUI().markAsErrorcause();
			}
			
			return(false);
		}
		
		
		if (line.startsWith(GlobalSettings.startExecutionSignal))
		{
			if (lastContent != null)
				lastContent.getVisualization().getControlerOnGUI().fadeoutMarking();
			lastContent = null;
			
			content = ExecutionStarter.getProgramContent(line.substring(6));
			
			if (content != null)
			{
				if (!content.getCodePageName().equals(ContentsSectionManager.getSelf().getCurrentPage()))
					return(false);
				
				content.getVisualization().getControlerOnGUI().markAsExecuting();
				
				if (!content.isEvent())
					lastContent = content;
			}
			
			return(false);
		}
		
		if (line.startsWith(GlobalSettings.endEventSignal))
		{
			if (lastContent != null)
				lastContent.getVisualization().getControlerOnGUI().fadeoutMarking();
			lastContent = null;
			
			content = ExecutionStarter.getProgramContent(line.substring(6));
			
			if (content != null)
			{
				if (!content.getCodePageName().equals(ContentsSectionManager.getSelf().getCurrentPage()))
					return(false);

				content.getVisualization().getControlerOnGUI().fadeoutMarking();
			}
			
			return(false);
		}		
		
		
		if (line.startsWith(GlobalSettings.variableSetSignal))
		{
			int a = line.indexOf('|', 6)+1;
			int b = line.indexOf('|', a);
			
			VariableOverviewList.updateVariable(line.substring(6, a-1), Variable.getDebugTypeName( Integer.valueOf(line.substring(a, b)) ), line.substring(b+1), true, false);
			
			return(false);
		}
		if (line.startsWith(GlobalSettings.innerDataChangeSignal))
		{
			int a = line.indexOf('|', 6)+1;
			int b = line.indexOf('|', a);
			
			VariableOverviewList.updateVariable(line.substring(6, a-1), line.substring(a, b), line.substring(b+1), false, false);
			
			return(false);
		}
		
		
		return(true);
	}

	public static void hookOntoFinish(Runnable task)
	{
		finishProgramTask = task;
	}

	public static void finishedExternalExecution()
	{
		if (finishProgramTask != null)
			finishProgramTask.run();
		finishProgramTask = null;
	}

	public static void checkedSleep(long duration)
	{
		try
		{
			while(duration > (GlobalSettings.maxTaskDurationTilCheck))
			{
				sleepIfPaused();
				
				Thread.sleep(GlobalSettings.maxTaskDurationTilCheck);
				if (!Execution.isRunning())
					return;
				duration -= GlobalSettings.maxTaskDurationTilCheck;
			}
			Thread.sleep(duration);
			sleepIfPaused();
		}
		catch (InterruptedException e) {}
	}

	public static long currentTimeMillisVague()
	{
		return(currentVagueTime);
	}
	
	public static List<String> getTrackingResult()
	{
		return(trackText);
	}
	
	public static void sleepIfPaused()
	{
		try {
			while(paused)
				Thread.sleep(GlobalSettings.runningCheckInterval/2);
		} catch (InterruptedException e) {}
	}

	public static boolean lastPerformingHadError()
	{
		return(hadError);
	}

	public static void signalizeError()
	{
		hadError = true;
	}

	public static void resetLastPerformingHadError()
	{
		hadError = false;
	}

	public static void setNoAutoQuit()
	{
		noAutoQuit = true;
	}

	
	

	
	

}
