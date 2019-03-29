package productionGUI.tutorialElements;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.Node;
import otherHelpers.DragAndDropHelper;
import productionGUI.ProductionGUI;
import productionGUI.additionalWindows.MainInfoScreen;
import productionGUI.additionalWindows.OverlayMenu;
import productionGUI.additionalWindows.VariableOverviewList;
import productionGUI.controlers.ButtonsRegionControl;
import settings.HaveDoneFileHandler;
import staticHelpers.GuiMsgHelper;

public class Tutorial
{
	static final String textSeparator = "[E]";
	
	List<Task> tasks = new ArrayList<Task>();
	String name = "";
	String conclusion = "";
	
	List<String> knowLines = new ArrayList<>();
	List<String> learnLines = new ArrayList<>();
	List<String> hardLines = new ArrayList<>();
	String goal = "";
	
	List<TutExercise> exercises = new ArrayList<>();
	
	boolean available = true;
	
	public Tutorial(String name)
	{
		this.name = name;
	}

	protected void addTask(Task task)
	{
		tasks.add(task);
	}
	
	public void addConlusionLine(String line)
	{
		if (conclusion.isEmpty())
			conclusion = line;
		else
			conclusion = conclusion+"\n"+line;
	}
	
	public void addKnowLine(String line)
	{
		if (line.startsWith("- "))
			line = line.substring(2);
		
		if (!line.trim().isEmpty())
			knowLines.add(line);
	}
	
	public void addExercise(TutExercise exercise)
	{
		exercises.add(exercise);
	}
	
	public List<String> getKnowLines()
	{
		return(knowLines);
	}
	
	public void addLearnLine(String line)
	{
		if (line.startsWith("- "))
			line = line.substring(2);
		
		if (!line.trim().isEmpty())
			learnLines.add(line);
	}
	public List<String> getLearnLines()
	{
		return(learnLines);
	}
	
	public void addGoalLine(String line)
	{
		if (!line.trim().isEmpty())
			if(goal.isEmpty())
				goal = line;
			else goal += "\n"+line;
	}
	
	public String getGoal()
	{
		return(goal);
	}
	
	public void addHardLine(String line)
	{
		if (line.startsWith("- "))
			line = line.substring(2);
		
		if (!line.trim().isEmpty())
			hardLines.add(line);		
	}
	public List<String> getHardwareLines()
	{
		return(hardLines);
	}
	
	
	final String exerciseText = "\nNow you can try out your new knowledge with exercises.\nThose will show you alternate goals for your program\nand provide hints to reach them.\nPossible, optimal solutions will open as disabled program pages.\n\n";
	final String noExerciseText = "\nNow you can try out your new knowledge on your own.\n";
	
	public void showConclusion()
	{
		Platform.runLater(() ->
		{
			for(Node node: ProductionGUI.getScene().getRoot().getChildrenUnmodifiable())
			{
				if (node != DragAndDropHelper.getTopPane())
					node.setOpacity(0.5);
			}
			
			/*
			if (DataConsole.getSelf() != null)
				DataConsole.getSelf().close();
			*/
			if (VariableOverviewList.getSelf() != null)
				VariableOverviewList.getSelf().close();
			
			
			int split = conclusion.indexOf(Tutorial.textSeparator);
			
			String mid, bottom = null;
			if (split == -1)
				mid = conclusion;
			else
			{
				mid = conclusion.substring(0, split);
				bottom = conclusion.substring(split+4);
			}
			
						
			
			OverlayMenu message = new OverlayMenu(null,  null, false, false);
			
			if (!TutorialControler.tutorialRunning()) // if starting without the tutorialr unning
				message.addText("Conclusion for: '" + name + "'", false);
			else
				message.addText("Successfully finished '" + name + "' - Well done!", false);

			message.addSeparator("blue");
			
			if (bottom != null && !bottom.isEmpty())
			{
				message.addText(mid, false);

				message.addSeparator("lime");
				message.addText(bottom+"\n\n", true);
			}
			else
				message.addText(mid+"\n", false);


			message.addSeparator("yellow");
			
			if (TutorialControler.tutorialRunning())
			{
				if (!exercises.isEmpty())
				{
					message.addText(exerciseText, true);
					message.addSeparator("yellow");
					
					message.addButton("Exercise", () -> {TutorialControler.endTutorial(false); TutorialControler.startExercises(exercises);});
				}
				else
				{
					message.addText(noExerciseText, true);
					message.addSeparator("yellow");
				}
				
				String quest = "Are you sure you do not want to do exercises\nto verify the contents of this tutorial?";
				message.addButton("Finish", () -> {if (exercises.isEmpty() || (GuiMsgHelper.askQuestionDirect(quest) == 1)) Platform.runLater(() -> TutorialControler.endTutorial(false));});
				message.addButton("More Tutorials", () -> {if (exercises.isEmpty() || (GuiMsgHelper.askQuestionDirect(quest) == 1)) {Platform.runLater(() -> {TutorialControler.endTutorial(false); ButtonsRegionControl.getSelf().Info();});}});			
			}
			else
				message.addButton("Close", () -> {TutorialControler.endTutorial(false);});
			
			//message.addCornerButton("End Tutorial", () -> TutorialControler.endTutorial(true));
			
			message.makeDraggable();
			
			HaveDoneFileHandler.haveDone("Tut: " + name, true);
			
			TutorialControler.setWindow(message);
		});
	}

	public String getName()
	{
		return(name);
	}
	
	public void printItself()
	{
		for(String str: knowLines)
			System.out.println(str);
		System.out.println("");
		System.out.println("|||| " + name.toUpperCase() + "||||");
		for(String str: goal.split("(?<=\\G.{40})"))
			System.out.println(str);
		System.out.println("");
		for(String str: learnLines)
			System.out.println(str);
	}

	public void setAvailable(boolean available)
	{
		this.available = available;
	}	
	public boolean isAvailable()
	{
		return(available);
	}
	
	
	public List<TutExercise> getExercises()
	{
		return(exercises);
	}

	List<String> innerLines;
	public void setInnerLineContent(List<String> subList)
	{
		innerLines = subList;
	}

	public void resolve()
	{
		if (tasks.isEmpty())
			MainInfoScreen.resolveTutorial(this, innerLines);
	}
	

}
