package productionGUI.sections.elementManagers;

import productionGUI.ProductionGUI;

public class EventsSectionManager extends AbstractSectionManager
{
	static String sectionName = "Events";
	static String tooltip = "'Events' are the foundation of every program\n"
			+ "and the only elements making sense to be placed directly into the frame as roots.\n"
			+ "'Actions', 'Conditions' and 'Structures' can be their nested children-elements forming a tree-structure\n"
			+ "which will be executed as soon as the internal condition of the event has been evaluated positively.\n\n"
			+ "Note that events run independently from each other (and can profit from multi-core hardware).\n"
			+ "Variables which are named pieces of data like a number or text for example, can be used.\n"
			+ "to transmit data between events and influence each other. They are threadsafe.\n\n"
			+ "Most 'Events' have parameters (sometimes called arguments) which can be filled by values, texts or variables.\n"
			+ "The tooltips and tutorials will provide you more information.";
	
	public EventsSectionManager(GeneralSectionManager topManager)
	{
		super(topManager, sectionName);
		
		self = this;
		
		ProductionGUI.addFinalizationEvent(() -> topManager.finalize(sectionName, tooltip));
		
		onlyOneChildLevel = true;
		newBaseNodesAllowed = false;
	}
	
	
	private static EventsSectionManager self;
	
	public static EventsSectionManager getSelf()
	{
		return(self);
	}
	
	
	public static String getName()
	{
		return(sectionName);
	}
}
