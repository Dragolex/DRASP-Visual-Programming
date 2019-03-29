package productionGUI.sections.elementManagers;

import productionGUI.ProductionGUI;

public class StructuresSectionManager extends AbstractSectionManager
{
	static String sectionName = "Structures/Data";
	static String tooltip = "'Structures/Data' are providing functionality\n"
			+ "for handling data and for influencing the execution-order"
			+ "of program elements inside events.\n\n"
			+ "The tooltips and tutorials will provide you more information.";

	public StructuresSectionManager(GeneralSectionManager topManager)
	{
		super(topManager, sectionName);
		
		self = this;
				
		ProductionGUI.addFinalizationEvent(() -> topManager.finalize(sectionName, tooltip));
		
		onlyOneChildLevel = true;
		newBaseNodesAllowed = false;
	}

	
	private static StructuresSectionManager self;
	
	public static StructuresSectionManager getSelf()
	{
		return(self);
	}
	
	
	public static String getName()
	{
		return(sectionName);
	}
}
