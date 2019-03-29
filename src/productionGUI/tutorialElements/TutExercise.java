package productionGUI.tutorialElements;

import settings.GlobalSettings;

public class TutExercise {
	
	String name;
	String text = "";
	
	public TutExercise(String name)
	{
		if (name.endsWith(GlobalSettings.standardProgramFileTermination))
			this.name = name.substring(0, name.length()-GlobalSettings.standardProgramFileTermination.length()-1);
		else
			this.name = name;
	}
	
	public void addDescriptionLine(String line)
	{
		text += line+"\n";
	}

	public String getName()
	{
		return(name);
	}

	public String getMidText()
	{
		int split = text.indexOf(Tutorial.textSeparator);
		
		if (split == -1)
			return(text);
		else
			return(text.substring(0, split));
	}

	public String getBottomText()
	{
		int split = text.indexOf(Tutorial.textSeparator);
		
		if (split == -1)
			return(null);
		else
			return(text.substring(split+4));
	}
	
}
