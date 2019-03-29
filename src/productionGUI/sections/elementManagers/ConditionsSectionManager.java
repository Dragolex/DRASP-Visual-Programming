package productionGUI.sections.elementManagers;

import productionGUI.ProductionGUI;

public class ConditionsSectionManager extends AbstractSectionManager
{
	static String sectionName = "Conditions";
	static String tooltip = "'Conditions' are the 'if' statements from many other programming languages.\n"
			+ "Place them as children of 'Events' just like 'Actions' by draggining them inside and"
			+ "they will be evaluated when executed.\n"
			+ "Depending on their result, either their nested 'Actions' are executed next,"
			+ "or the ones inside an 'ELSE' statement.\n\n"
			+ "Most 'Conditions' have parameters (sometimes called arguments) which can be filled by values, texts or variables.\n"
			+ "The tooltips and tutorials will provide you more information.";
	
	public ConditionsSectionManager(GeneralSectionManager topManager)
	{
		super(topManager, sectionName);
		
		self = this;
		
		ProductionGUI.addFinalizationEvent(() -> topManager.finalize(sectionName, tooltip));
		
		sectionBox = topManager.getSectionBox();

		onlyOneChildLevel = true;
		newBaseNodesAllowed = false;
	}

	
	private static ConditionsSectionManager self;
	
	public static ConditionsSectionManager getSelf()
	{
		return(self);
	}
	
	
	public static String getName()
	{
		return(sectionName);
	}
	
}
