package productionGUI.tutorialElements;

import productionGUI.sections.elements.VisualizableProgramElement;
import productionGUI.sections.subelements.SubElementField;

public class ParameterTask extends Task
{
	public static String nameReplacer;
	public static String contentReplacer;
	
	public String desiredContent = "";
	
	
	private int parameterIndex;
	private SubElementField targetField;
	
	
	protected ParameterTask(Tutorial parentTutorial, int parameterIndex)
	{
		super(parentTutorial);
		this.parameterIndex = parameterIndex;
	}
		
	public boolean start()
	{
		VisualizableProgramElement targetElement = TutorialControler.getVisualizedContentByIndex(onContentTarget);
		
		if (targetElement == null) // is expected to be set
		{
			// ERROR
			return(false);
		}
		
		TutorialControler.hideAllElementsExcept(null);
		
		
		targetField = (SubElementField) targetElement.getControlerOnGUI().getSubelement(1+parameterIndex*2+1);
		
		if (targetField == null)
		{
			targetElement.getControlerOnGUI().hookOnPlusOptionalParamButton(() -> {
				if (TutorialControler.tutorialRunning())
				{
					targetField = (SubElementField) targetElement.getControlerOnGUI().getSubelement(1+parameterIndex*2+1);
					targetField.highlight();
					
					TutorialControler.getCurrentWindow().shiftPosition(250, 0);
				}
			});
			
			showStandardWindow(targetElement.getControlerOnGUI().getAndHighlightPlusOptionalParamButton(), false);
		}
		else
		{
			targetField.highlight();
			showStandardWindow(targetField.getContainer(), false);
		}

		
		return(true);
	}
	
	public void finalizeText()
	{
		VisualizableProgramElement targetElement = TutorialControler.getVisualizedContentByIndex(onContentTarget);
		String parameterName = targetElement.getPotentialArgumentDescription(parameterIndex);
		//String parameterName = ((SubElementName) targetElement.getControlerOnGUI().getSubelement(1+parameterIndex*2)).getName();
		
		text = text.replace(nameReplacer, parameterName).replace(contentReplacer, desiredContent);
	}
	
	
	public boolean checkRightField(SubElementField field)
	{
		return(targetField == field);
	}

	
	public boolean finish(SubElementField field)
	{
		if ((targetField != field) || (field.getTextDirect().trim().isEmpty()))
			return(false);
		
		if (!desiredContent.isEmpty())
			if (!desiredContent.equals(field.getTextDirect()))
				return(false);
		
		targetField.forceStopHighlight();
		
		return(true);
	}

	public void setDesiredContent(String content)
	{
		desiredContent = content;
	}
	
	
	@Override
	protected int getType()
	{
		return(ParameterTaskType);
	}


}
