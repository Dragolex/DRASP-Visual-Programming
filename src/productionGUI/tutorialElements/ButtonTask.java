package productionGUI.tutorialElements;

import javafx.scene.Node;

public class ButtonTask extends Task
{
	public static String nameReplacer;
	
	
	private String buttonName;
	
	protected ButtonTask(Tutorial parentTutorial, String elementName)
	{
		super(parentTutorial);
		this.buttonName = elementName;
	}
	
	public boolean start()
	{
		Node targetNode = TutorialControler.getNodeElement(buttonName);
		TutorialControler.getNodeElementRunnable(buttonName).run();
		
		TutorialControler.hideAllElementsExcept(null);
		
		showStandardWindow(targetNode, true);
		
		return(true);
	}
	
	public boolean finish(String buttonName)
	{
		return(this.buttonName.equalsIgnoreCase(buttonName));
	}
	
	public void finalizeText()
	{
		text = text.replace(nameReplacer, buttonName);
	}
	

	@Override
	protected int getType()
	{
		return(ButtonTaskType);
	}

	public String getButton()
	{
		return(this.buttonName);
	}

}
