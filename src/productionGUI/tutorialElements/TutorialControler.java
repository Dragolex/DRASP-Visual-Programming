package productionGUI.tutorialElements;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.reactfx.util.FxTimer;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.minor.Pair;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import main.DataControler;
import otherHelpers.DragAndDropHelper;
import productionGUI.ProductionGUI;
import productionGUI.additionalWindows.DataConsole;
import productionGUI.additionalWindows.OverlayMenu;
import productionGUI.additionalWindows.SettingsMenu;
import productionGUI.additionalWindows.VariableOverviewList;
import productionGUI.controlers.ButtonsRegionControl;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.elements.ProgramElementOnGUI;
import productionGUI.sections.elements.VisualizableProgramElement;
import productionGUI.sections.subelements.SubElementField;
import settings.GlobalSettings;
import staticHelpers.GuiMsgHelper;
import staticHelpers.KeyChecker;
import staticHelpers.LocationPreparator;
import staticHelpers.OtherHelpers;

public class TutorialControler
{	
	static List<Tutorial> tutorials = new ArrayList<Tutorial>();
	
	static private Map<String, FunctionalityContent> elementMarks = new HashMap<>();
	static private Map<String, Pair<Node, Runnable>> runnableMarks = new HashMap<>();
	
	static private OverlayMenu currentTutWindow = null;
	
	static private Map<String, String> specialColors = new HashMap<>();
	
	static private volatile boolean executedOnce = false;
	
	static private OverlayMenu exerciseMessage = null;
	
	
	public static Tutorial createTutorial(String name)
	{
		Tutorial tut = new Tutorial(name);
		tutorials.add(tut);
		createParameterAttentionRect();
		return(tut);
	}
	
	public static ElementTask createElementTask(Tutorial tut, String elName)
	{
		if (!elementMarks.containsKey(elName.toLowerCase()))
			InfoErrorHandler.callEnvironmentError("Problem when loading the tutorial '" + tut.getName() + "': The functionality element named '" + elName + "' does not exist!");
		
		ElementTask task = new ElementTask(tut, elName.toLowerCase());
		tut.addTask(task);
		return(task);
	}
	public static ParameterTask createParameterTask(Tutorial tut, int paramIndex)
	{
		ParameterTask task = new ParameterTask(tut, paramIndex);
		tut.addTask(task);
		return(task);
	}
	public static Task createButtonTask(Tutorial tut, String buttonName)
	{
		ButtonTask task = new ButtonTask(tut, buttonName.toLowerCase());
		tut.addTask(task);
		return(task);
	}
	

	
	
	public static void setWindow(OverlayMenu window)
	{
		currentTutWindow = window;
	}
	
	
	public static void setWindowTransparent(boolean transparent)
	{
		if (currentTutWindow != null)
			if (!currentTutWindow.isFading())
				currentTutWindow.setTransparent(transparent);
		
		if (DataConsole.getSelf() != null)
			DataConsole.getSelf().setTransparent(transparent);
		if (VariableOverviewList.getSelf() != null)
			VariableOverviewList.getSelf().setTransparent(transparent);
	}
	
	
	
	
	public static void addPossibleMark(FunctionalityContent element)
	{
		if (element != null)
			elementMarks.put(element.getFunctionalityName().toLowerCase(), element);
	}
	public static void addPossibleMark(String name, Node node, Runnable runnable)
	{
		runnableMarks.put(name.toLowerCase(), new Pair(node, runnable));
	}
	
	
	protected static VisualizableProgramElement getVisualizedContentByIndex(int index)
	{
		ProgramElement[] ele = new ProgramElement[1];
		VisualizableProgramElement[] res = new VisualizableProgramElement[1];
		Integer[] count = new Integer[1];
		count[0] = 0;
		
		ContentsSectionManager.getSelf().getRootElementNode().applyToChildrenTotal(ele, () -> {
			if (count[0] == index)
				res[0] = ele[0].getContent().getVisualization();
			count[0]++;
		}, true);
		
		return(res[0]);
	}
	
	
	
	
	volatile static Tutorial currentTut = null;
	volatile static Task currentTask = null;
	
	volatile static int taskIndex = 0;
	
	private static boolean allowSkipByN = false;
	
	public static void launchTutorial(Tutorial tut)
	{		
		if (!ButtonsRegionControl.getSelf().New()) // Create new file
			return;
		
		if (DataConsole.getSelf() != null)
			DataConsole.getSelf().close();
		if (VariableOverviewList.getSelf() != null)
			VariableOverviewList.getSelf().close();
		
		currentTut = tut;
		taskIndex = 0;
		
		resetThatExecuted();
		
		
		for(Entry<String, FunctionalityContent> entr: elementMarks.entrySet())
		if (entr.getValue() != null)
			if (entr.getValue().getVisualization() != null)
				if (entr.getValue().getVisualization().getControlerOnGUI() != null)
					entr.getValue().getVisualization().getControlerOnGUI().forceCollapse(true); // collapse all elements
		
		if (allowSkipByN)
			KeyChecker.addKeyToCheck(KeyChecker.getKeyCode("N"));
		
		launchNextTask();
	}
	
	public int getCurrentTaskConditionType()
	{
		if ((currentTask == null) || (currentTut == null))
			return(-1);
		
		return(currentTask.getType());
	}
	
	
	private static void launchNextTask()
	{
		new Thread( () ->
		{	
			if (currentTutWindow != null)
				if (!currentTutWindow.isFading())
					currentTutWindow.highlightAndFadeout();
			
			if (taskIndex >= currentTut.tasks.size())
			{
				currentTut.showConclusion();
				return;
			}
			
			currentTask = currentTut.tasks.get(taskIndex++);
			
			OtherHelpers.sleepNonException(750);
			
			resetThatExecuted();
			
			//TutorialControler.showAllElements();
			currentTask.start();
			
			Task thisCurrentTask = currentTask;
			
			if (allowSkipByN)
				while(currentTask == thisCurrentTask)
					if (KeyChecker.isDown(KeyChecker.getKeyCode("N")))
					{
						KeyChecker.cancelDown(KeyChecker.getKeyCode("N"));
						launchNextTask();
						return;
					}
			
		}).start();		
	}
	
	
	public static FunctionalityContent getProgramContentElement(String name)
	{
		return(elementMarks.get(name.toLowerCase()));
	}
	
	public static Node getNodeElement(String name)
	{
		return(runnableMarks.get(name.toLowerCase()).first);
	}
	public static Runnable getNodeElementRunnable(String name)
	{
		return(runnableMarks.get(name.toLowerCase()).second);
	}

	
	

	public static boolean handleElementDrag(DataNode<ProgramElement> root,
			VisualizableProgramElement newCreated, String contentType)
	{
		if (Execution.isRunning() || Execution.isRunningDeployed())
			return(false); // No drag and drop while running
		
		TutorialReaderAndMaker.handleElementDrag(root, newCreated, contentType);
		
		if (currentTask == null) return(true);
		
		if (currentTask.getType() == Task.ElementDragTaskType)
		{
			DataNode<ProgramElement> ele = root.getNodeOfInTree(newCreated);
			boolean res;
			if (ele.getParent() == root)
				res = ((ElementTask) currentTask).finish(root.indexInTree(newCreated), -1, contentType);
			else
				res = ((ElementTask) currentTask).finish(root.indexInTree(newCreated), root.indexInTree(ele.getParent().getData()), contentType);
			
			if (res)
				launchNextTask();
			else
				currentTutWindow.highlightBad();
			return(res);
		}
		else
		{
			currentTutWindow.highlightBad();
			return(false);
		}
		
	}
	
	
	
	public static boolean handleElementOpen(VisualizableProgramElement element)
	{
		TutorialReaderAndMaker.handleElementOpen(element);
		
		if (currentTask == null) return(true);
		
		if (currentTask.getType() == Task.ElementOpenTaskType)
		{
			boolean res = ((ElementTask) currentTask).finish(element);
			if (res)
			{
				FxTimer.runLater(
				        Duration.ofMillis(GlobalSettings.collapseDuration),
				        () -> launchNextTask());
			}
			else
				currentTutWindow.highlightBad();
			return(res);
		}
		else
		{
			currentTutWindow.highlightBad();
			return(false);
		}
	}
	
	
	public static boolean handleButtonPress(String buttonText)
	{
		TutorialReaderAndMaker.handleButtonPress(buttonText);
		
		//SettingsMenu.hookOntoSuccessfulChange(null);
		
		if (currentTask == null) return(true);
		
		if (currentTask.getType() == Task.ButtonTaskType)
		{
			
			boolean res = (((ButtonTask) currentTask).finish(buttonText));
			
			
			if (res)
			{
				if (buttonText.equalsIgnoreCase("simulate") || buttonText.equalsIgnoreCase("deploy"))
				{
					/*
					if (currentTutWindow != null)
						if (!currentTutWindow.isFading())
							currentTutWindow.highlightAndFadeout();*/

					Execution.hookOntoFinish(() -> {
						if (!Execution.lastPerformingHadError())
						{
							OtherHelpers.sleepNonException(1500);
							launchNextTask();
						}
					});
				}
				else
				if (buttonText.equalsIgnoreCase("options"))
				{
					/*
					if (currentTutWindow != null)
						if (!currentTutWindow.isFading())
							currentTutWindow.highlightAndFadeout();*/
					
					launchNextTask();
					
					SettingsMenu.hookOntoSuccessfulChange(() -> {
						if (tutorialRunning())
						{
							OtherHelpers.sleepNonException(500);
							launchNextTask();
						}
					});
				}
				else
					if (!buttonText.equalsIgnoreCase("test and save"))
						launchNextTask();
			}
			
			String taskOriginButton = ((ButtonTask) currentTask).getButton();

			if (((taskOriginButton.equalsIgnoreCase("simulate") || taskOriginButton.equalsIgnoreCase("continue")) || taskOriginButton.equalsIgnoreCase("deploy")) && buttonText.equalsIgnoreCase("stop"))
				return(true);
			
			if (buttonText.equalsIgnoreCase("continue"))
				return(true);

			if(!res)
				currentTutWindow.highlightBad();
			
			return(res);
		}
		else
		{
			currentTutWindow.highlightBad();
			return(false);
		}
	}
	
	
	public static boolean handleParameterChange(SubElementField field)
	{
		if (Execution.isRunningDeployed())
			return(false); // No parameter change while running externally
		
		if (currentTask == null) return(true);
		
		if (executedOnce) return(true);
		
		if (currentTask.getType() == Task.ParameterTaskType)
		{
			boolean res = ((ParameterTask) currentTask).checkRightField(field);
			if (!res)
				currentTutWindow.highlightBad();
			return(res);
		}
		else
		{
			currentTutWindow.highlightBad();
			return(false);
		}
	}
	
	public static boolean handleParameterFinish(SubElementField field, ProgramElementOnGUI elOnGui)
	{
		if (Execution.isRunningDeployed())
			return(false); // No parameter change while running externally
		
		TutorialReaderAndMaker.handleParameterFinish(field, elOnGui);
		
		if (currentTask == null) return(true);
		
		if (executedOnce) return(true);
		
		if (currentTask.getType() == Task.ParameterTaskType)
		{
			boolean res = ((ParameterTask) currentTask).finish(field);
			if (res)
				launchNextTask();
			else
			{
				currentTutWindow.highlightBad();
				System.out.println("Highlight bad A");
			}
			return(res);
		}
		else
		{
			currentTutWindow.highlightBad();
			System.out.println("Highlight bad B. Current type: " + currentTask.getType() + " vs Param type: " + Task.ParameterTaskType);
			return(false);
		}
	}
	
	
	
	
	public static void signalizeThatExecuted()
	{
		executedOnce = true;
	}
	public static void resetThatExecuted()
	{
		executedOnce = false;
	}
		
	
	
	static public Rectangle attentionRectangle;
	static public FadeTransition attentionRectangleTransition;
	
	static private void createParameterAttentionRect()
	{
		if (attentionRectangle != null) return;
		
		attentionRectangle = new Rectangle(7, 1, 1, 1);
		attentionRectangle.setArcHeight(5);
		attentionRectangle.setArcWidth(5);
		attentionRectangle.setManaged(false);
		attentionRectangle.setMouseTransparent(true);

		attentionRectangleTransition = new FadeTransition(GlobalSettings.attentionBlinkDuration, attentionRectangle);
		attentionRectangleTransition.setFromValue(GlobalSettings.attentionRectangleMinAlpha);
		attentionRectangleTransition.setToValue(GlobalSettings.attentionRectangleMaxAlpha);
		attentionRectangleTransition.setCycleCount(Timeline.INDEFINITE);
		attentionRectangleTransition.setAutoReverse(true);
	}
	
	
	
	public static void printKnowings()
	{
		System.out.println("KNOW:");
		
		Set<String> knowings = new TreeSet<>();
		
		for(Tutorial tut: tutorials)
		{
			for(String str: tut.getKnowLines())
				knowings.add(str);
		}		
		
		for (String str: knowings)
		{
			System.out.println(str);
		}		
	}
	
	
	public static void printLearnings()
	{
		System.out.println("LEARN:");
		
		//Set<String> knowings = new TreeSet<>();
		List<String> knowings = new ArrayList<>();
		
		for(Tutorial tut: tutorials)
		{
			for(String str: tut.getLearnLines())
				if (!knowings.contains(str))
					knowings.add(str);
		}		
		
		for (String str: knowings)
		{
			System.out.println(str);
		}
		
	}

	public static void printTutorials()
	{
		for(Tutorial tut: tutorials)
		{
			tut.printItself();
			
			System.out.println("\n");
		}	
		
	}
	
	
	
	public static String getEquivalentColor(String text)
	{
		return(specialColors.getOrDefault(text, "-fx-background-color: rgba(255, 255, 255, 0); -fx-stroke: rgba(255, 255, 255, 0); "));
	}

	public static void prepareSpecialColors()
	{
		Set<String> data = new TreeSet<>();
		
		for(Tutorial tut: tutorials)
		{
			for(String str: tut.getLearnLines())
				data.add(str);
			
			for(String str: tut.getKnowLines())
				data.add(str);
		}		
		
		int size = data.size();
		int hue = 0;
		int hueDif = 360/size;
		
		for (String str: data)
		{
			specialColors.put(str, "-fx-border-radius: 3 3 3 3; -fx-background-color: hsba(" + hue + ", 100%, 50%, 0.3); -fx-stroke: hsba(" + hue + ", 100%, 50%, 0.45); -fx-fill: hsba(" + hue + ", 100%, 75%, 0.45);  ");
			hue += hueDif;
		}		
		
	}
	
	
	
	public static void hideAllElementsExcept(FunctionalityContent except)
	{
		for(Entry<String, FunctionalityContent> dat: elementMarks.entrySet())
		if (dat.getValue().getVisualization().getControlerOnGUI() != null)
			dat.getValue().getVisualization().getControlerOnGUI().getBasePane().setOpacity(0.5);
		
		if (except != null)
		if (except.getVisualization().getControlerOnGUI() != null)
			except.getVisualization().getControlerOnGUI().getBasePane().setOpacity(1);
	}
	
	
	public static boolean endTutorial(boolean ask)
	{
		if (currentTut == null)
		{
			for(Node node: ProductionGUI.getScene().getRoot().getChildrenUnmodifiable())
			{
				if (node != DragAndDropHelper.getTopPane())
					node.setOpacity(1);
			}
			
			if (currentTutWindow != null)
				currentTutWindow.fade(false);
			
			if (exerciseMessage != null)
				exerciseMessage.fade(false);
			
			return(true);
		}
		
		if (ask)
		if (GuiMsgHelper.askQuestionDirect("Are you sure you want to quit the current tutorial?") != 1)
			return(false);
		
		for(Node node: ProductionGUI.getScene().getRoot().getChildrenUnmodifiable())
		{
			if (node != DragAndDropHelper.getTopPane())
				node.setOpacity(1);
		}
		
		TutorialControler.showAllElements();
		currentTutWindow.fade(false);
		
		currentTut = null;
		currentTask = null;
		
		return(true);
	}
	
	private static void showAllElements()
	{
		for(Entry<String, FunctionalityContent> dat: elementMarks.entrySet())
		if (dat.getValue().getVisualization().getControlerOnGUI() != null)
		{
			dat.getValue().getVisualization().getControlerOnGUI().getBasePane().setOpacity(1);
			dat.getValue().getVisualization().getControlerOnGUI().fadeoutMarking(true);
		}
	}

	public static boolean tutorialRunning()
	{
		return(currentTut != null);
	}
	
	
	
	public static void startExercises(List<TutExercise> exercises)
	{
		if (DataConsole.getSelf() != null)
			DataConsole.getSelf().close();
		
		int ind = 0;
		for(TutExercise exercise: exercises)
		{
			DataControler.loadProgramFile(LocationPreparator.getExternalDirectory()+"tutorialExercises"+File.separator+exercise.getName()+"."+GlobalSettings.standardProgramFileTermination, false, true, exercise.getName());
			ContentsSectionManager.getSelf().deactivateButFlashNewestPage();
		}
		
		
		ContentsSectionManager.getSelf().addNewPage("Exercise", false);
		
		
		//OverlayMenu message = new OverlayMenu(ContentsSectionManager.getSelf().getLastPageNode(),  null, false, false, true);
		OverlayMenu message = new OverlayMenu(null,  null, false, false);
		
		
		message.addTextDirect("EXERCISES", false);
		
		message.addSeparator("blue");
		message.addTextDirect("For every exercise:\nTry to form a program which fulfills the goal.\n\nIf you need help, re-do the associated\n tutorial or click onto the currently blinking\npage-buttons above the main program.\nThose contain an ideal solution.", true);
		
		
		
		for(TutExercise exercise: exercises)
		{
			message.addSeparator("yellow");
			message.addTextDirect(exercise.getName(), false);
			message.addSeparator("lime");
			message.addTextDirect(exercise.getMidText(), true);
			String bottom = exercise.getBottomText();
			if (bottom != null)
			{
				message.addSeparator("lime");
				message.addTextDirect(exercise.getBottomText(), true);
			}
		}
		
		
		message.addCornerButton("End Exercising", () -> message.fade(false), 0,  "/guiGraphics/if_close.png", "/guiGraphics/if_close_hover.png", true);
		message.addCornerButton(null, () -> {}, -1,  "/guiGraphics/if_directions.png", "/guiGraphics/if_directions_hover.png", true);

		message.makeDraggable();
		
		exerciseMessage = message;
	}

	public static OverlayMenu getCurrentWindow()
	{
		return(currentTutWindow);
	}

	public static Map<String, FunctionalityContent> getElementMarks()
	{
		return(elementMarks);
	}
	
	
	

	
	/*
	public static void launchTutorial(String tutorialName)
	{
		new Thread( () ->
		{
			int stepNum = 1;
			for(Tripple<Object, Integer, String> step: _tutorials.get(tutorialName))
			{
				highlightMark(step.first);
				GuiMsgHelper.showMessageNonblockingUI("Step " + stepNum + ": \n" + step.third);
			}
			
		}).start();
	}
	*/
	
	/*
	public static void highlightMark(Object target)
	{
		if (target instanceof ProgramContent)			
			((ProgramContent) target).getVisualization().getControlerOnGUI().markAsExecuting();
		else
			((Runnable) target).run();
	}
	*/

	
	
	

/*	
	public static void addTutorialStepText(String tutorialName, String stepName, Integer dragToLine, String stepTextPartial)
	{
		if (_tutorials.containsKey(tutorialName))
		{
			List<Tripple<Object, Integer, String>> dataList = _tutorials.get(tutorialName);
			
			Double num = OtherHelpers.convDouble(stepName);
			Object content;
			if (num == null)
				content = marks.get(stepName.toLowerCase());
			else
				content = getVisualizedContentByIndex(num);
			
			int ind = getFromPairsListContains(dataList, content, dragToLine);
			if (ind >= 0)
			{
				Tripple<Object, Integer, String> old = dataList.get(ind);
				String newStr = old.third + "\n" + stepTextPartial;
				dataList.set(ind, new Tripple<Object, Integer, String>(old.first, old.second, newStr));
			}
			else
			{
				if (stepTextPartial.isEmpty())
					return;
				
				if (content instanceof ProgramContent)
					stepTextPartial = applyElementName(stepTextPartial, (ProgramContent) content);
				else
				if (content instanceof VisualizableProgrammElement)
					stepTextPartial = applyArgName(stepTextPartial, (VisualizableProgrammElement) content, dragToLine);
				else
					stepTextPartial = stepTextPartial.replace(elementNameReplacer, stepName);
					
				Tripple<Object, Integer, String> newStep = new Tripple<Object, Integer, String>(content, dragToLine, stepTextPartial);
				dataList.add(newStep);
			}
		}
		else
		{
			_tutorials.put(tutorialName, new ArrayList<Tripple<Object, Integer, String>>());
			addTutorialStepText(tutorialName, stepName, dragToLine, stepTextPartial);
		}
	}

	
	private static String applyElementName(String text, ProgramContent content)
	{
		String name = content.getVisualization().getName();
		text = text.replace(elementNameReplacer.toUpperCase(), name);
		return(text);
	}
	private static String applyArgName(String text, VisualizableProgrammElement element, Integer argIndex)
	{
		String name = ((SubElementName) element.getControlerOnGUI().getSubelement(1+argIndex*2)).getName();
		text = text.replace(argNameReplacer.toUpperCase(), name);
		return(name);
	}
	
	
	
	private static int getFromPairsListContains(List<Tripple<Object, Integer, String>> pairsList, Object identifier, Integer secondIdentifier)
	{
		int i = 0;
		for (Tripple<Object, Integer, String> pr: pairsList)
		{			
			if ((pr.first.equals(identifier)) && (pr.second.equals(secondIdentifier)))
				return(i);
			i++;
		}
		
		return(-1);
	}
	
	
	public static void reset(String elementNameReplacer, String argNameReplacer)
	{
		TutorialControler.elementNameReplacer = elementNameReplacer;
		TutorialControler.argNameReplacer = argNameReplacer;
		
		_tutorials.clear();
	}

 	*/
	
}
