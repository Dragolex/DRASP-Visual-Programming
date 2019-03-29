package productionGUI.sections.elementManagers;

import dataTypes.DataNode;
import dataTypes.ProgramElement;
import productionGUI.ProductionGUI;

public class ActionsSectionManager extends AbstractSectionManager
{
	static String sectionName = "Actions";
	static String tooltip = "'Actions' are the main program elements.\n"
			+ "Place them as children of 'Events' by draggining them inside and they will be executed, performing certain tasks.\n\n"
			+ "Most 'Actions' have parameters (sometimes called arguments) which can be filled by values, texts or variables.\n"
			+ "The tooltips and tutorials will provide you more information.";
	
	
	public ActionsSectionManager(GeneralSectionManager topManager)
	{
		super(topManager, sectionName);
		
		self = this;
		
		ProductionGUI.addFinalizationEvent(() -> topManager.finalize(sectionName, tooltip));
		
		sectionBox = topManager.getSectionBox();

		onlyOneChildLevel = true;
		newBaseNodesAllowed = false;
	}
	
	
	private static ActionsSectionManager self;
	
	public static ActionsSectionManager getSelf()
	{
		return(self);
	}
	
	
	public static String getName()
	{
		return(sectionName);
	}

	
}
