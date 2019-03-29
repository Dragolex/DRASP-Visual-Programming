package productionGUI.tutorialElements;

import dataTypes.FunctionalityContent;
import javafx.scene.Node;
import productionGUI.sections.elements.VisualizableProgramElement;

public class ElementTask extends Task
{
	public static String nameReplacer;
	
	private String elementName;
	
	private int inParentTarget = -1;
	
	protected ElementTask(Tutorial parentTutorial, String elementName)
	{
		super(parentTutorial);
		this.elementName = elementName;
	}
	
	public void setTargetParent(int inParentTarget)
	{
		this.inParentTarget = inParentTarget;
	}
	
	public boolean start()
	{
		FunctionalityContent target = TutorialControler.getProgramContentElement(elementName);
		
		if (target == null)
		{
			// EROROR
			return(false);
		}
		
		TutorialControler.hideAllElementsExcept(((FunctionalityContent) target));
		
		((FunctionalityContent) target).getVisualization().getControlerOnGUI().markAsExecuting();
		String internalName = ((FunctionalityContent) target).getVisualization().getName();

		
		//if (onContentTarget < -1) // the task had no target and therefore only required to open a category
			((FunctionalityContent) target).getVisualization().getOriginSection().scrollToVisualizeNode(((FunctionalityContent) target).getVisualization());
		
		
		Node node = ((FunctionalityContent) target).getVisualization().getControlerOnGUI().getContainer();
		showStandardWindow(node, false);
		
		return(true);
	}
	
	public void finalizeText()
	{
		FunctionalityContent target = TutorialControler.getProgramContentElement(elementName);
		String internalName = ((FunctionalityContent) target).getVisualization().getName();
		
		text = text.replace(nameReplacer, internalName);
	}
	
	public boolean finish(VisualizableProgramElement openedElement)
	{
		VisualizableProgramElement visualizedElement = (VisualizableProgramElement) openedElement;
		
		if (!visualizedElement.getContent().getFunctionalityName().equalsIgnoreCase(elementName))
			return(false); // wrong element!
		
		if (onContentTarget < -1) // the task had no target and therefore only required to open a category
		{
			if (visualizedElement.getNode().hasChildrenHidden()) // is still collapsed!
				return(false);
			
			return(true);
		}
		else
			return(false);		
	}
	
	
	public boolean finish(int insertedIndex, String contentType)
	{
		if (!elementName.equalsIgnoreCase(contentType))
			return(false); // wrong element!
		
		if (onContentTarget != insertedIndex)
			return(false);
		
		TutorialControler.getProgramContentElement(elementName).getVisualization().getControlerOnGUI().fadeoutMarking();
		
		return(true);
	}
	
	public boolean finish(int insertedIndex, int parentIndex, String contentType)
	{
		if (!elementName.equalsIgnoreCase(contentType))
			return(false); // wrong element!
		
		if (onContentTarget != insertedIndex)
			return(false);
		
		if (inParentTarget != parentIndex)
			return(false);
		
		TutorialControler.getProgramContentElement(elementName).getVisualization().getControlerOnGUI().fadeoutMarking();
		
		return(true);
	}

	

	@Override
	protected int getType()
	{
		if (onContentTarget < -1) // the task had no target and therefore only required to open a category
			return(ElementOpenTaskType);
		else
			return(ElementDragTaskType);		
	}	
	
	
	
	
}
