package functionality.conditions;

import dataTypes.FunctionalityConditionContent;
import dataTypes.FunctionalityContent;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Cond03_Connectivity extends Functionality {

	public static int POSITION = 3;
	public static String NAME = "Connectivity";
	public static String IDENTIFIER = "CondConNode";
	public static String DESCRIPTION = "Various conditions related to connectity tasks.";
	
	
	static public FunctionalityContent create_ConConnExist()
	{
		Object[] params = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityConditionContent( "ConConnExist",
				params,
				() -> {
					
						return(CONctrl.connectionExists((String) params[0], false));
						
					});
		return(content[0]);
	}
	static public VisualizableProgramElement visualize_ConConnExist(FunctionalityContent content)
	{
		VisualizableProgramElement vv;
		vv = new VisualizableProgramElement(content, "Connection Exists", "Executes the child-elements if the device is connected\nto a device matching the given filter.\nUse this to check whether connecting to a device has been succesful.");
		vv.setArgumentDescription(0, new TextOrVariable(CONctrl.identQstr), "Device Filter");
		return(vv);
	}
	

	static public FunctionalityContent create_ConDeviceAccessible()
	{
		Object[] params = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityConditionContent( "ConDeviceAccessible",
				params,
				() -> {
					
						return(CONctrl.connectionExists((String) params[0], false));
						
					});
		return(content[0]);
	}
	static public VisualizableProgramElement visualize_ConDeviceAccessible(FunctionalityContent content)
	{
		VisualizableProgramElement vv;
		vv = new VisualizableProgramElement(content, "Computer Accessible", "Executes the child-elements if accessing a computer defined\nwith the Action 'External Computer' is possible.");
		vv.setArgumentDescription(0, new VariableOnly(), "Computer Identifier");
		return(vv);
	}

	
	
	
}


