package dataTypes;

import java.util.ArrayList;
import java.util.List;

import productionGUI.tutorialElements.Tutorial;

public class ProgramTutorialContent extends FunctionalityContent
{
	Tutorial tutorial;
	String text;
	String goal = "";
	List<String> subtexts = new ArrayList<>();
	
	public ProgramTutorialContent(String visualizerName, Tutorial tutorial, String text, String goal)
	{
		super(visualizerName);
		this.forTutorial = true;
		this.tutorial = tutorial;
		this.text = text;
		this.goal = goal;
	}
	
	public ProgramTutorialContent(String visualizerName, Tutorial tutorial, String text, List<String> subtexts)
	{
		super(visualizerName);
		this.forTutorial = true;
		this.tutorial = tutorial;
		this.text = text;
		this.subtexts = subtexts;
	}

	
	public ProgramTutorialContent(String visualizerName, Tutorial tutorial, String text)
	{
		super(visualizerName);
		this.forTutorial = true;
		this.tutorial = tutorial;
		this.text = text;
	}

	public Tutorial getTut()
	{
		return(tutorial);
	}

	public String getText()
	{
		return(text);
	}
	
	public String getGoal()
	{
		return(goal);
	}
	
	public List<String> getSubtexts()
	{
		return(subtexts);
	}

	
	
	
}