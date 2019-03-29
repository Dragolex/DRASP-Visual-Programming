package main.functionality;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dataTypes.DataNode;
import dataTypes.ProgramElement;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import execution.Execution;
import execution.Program;
import execution.handlers.InfoErrorHandler;
import otherHelpers.Multithreader;
import productionGUI.sections.elementManagers.ActionsSectionManager;
import productionGUI.sections.elementManagers.ConditionsSectionManager;
import productionGUI.sections.elementManagers.EventsSectionManager;
import productionGUI.sections.elementManagers.StructuresSectionManager;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.FileHelpers;
import staticHelpers.LocationPreparator;

@SuppressWarnings("deprecation")
public class Functionality extends UsedFunctionalityFlags
{
	
	// CONSTANTS
	
	// General
	
	public static final int Normal = -1;
	public static final int StoppedAction = -2;
	
	
	// Structures
	
	public static final int EventRepeater = 1;
	public static final int ElseClause = 2;
	public static final int ForLoop = 3;
	public static final int LabeledPosition = 4;
	public static final int LabelExecute = 5;
	public static final int LabelJump = 6;
	public static final int EventQuitter = 7;
	public static final int ClauseRepeater = 8;
	public static final int JumpOutOf = 9;
	public static final int Comment = 10;
	public static final int ListLoop = 11;
	public static final int LoopQuitter = 12;
	public static final int ExtraDefinedLoop = 13;
	public static final int LabelAlarm = 14;
	
	
	
	// Events
	
	// Constants // Not using ENUM to be able to split into multiple pages
	public static final int InitEvent = 101;
	public static final int LabeledEvent = 102;
	public static final int RhythmStepEvent = 103;
	public static final int DefinedInternallyTriggeredEvent = 999;
	
	//public static final int KeyboardPressedEvent = 104;
	
	
	
	public static List<ProgramEventContent> keyPressedEventContents = new ArrayList<ProgramEventContent>();
	public static List<ProgramEventContent> GPIOchangedEventContents = new ArrayList<ProgramEventContent>();
	public static List<ProgramEventContent> buttonPressedEventContents = new ArrayList<ProgramEventContent>();
	public static List<ProgramEventContent> buttonHoverStateEventContents = new ArrayList<ProgramEventContent>();
	
	
	// Needed to clear
	public static void clearSpecialEventContents()
	{
		keyPressedEventContents.clear();
		GPIOchangedEventContents.clear();
		buttonPressedEventContents.clear();
		buttonHoverStateEventContents.clear();
	}
	
	
	
	
	private final static Program featuresProgram = new Program();
	
	private static final DataNode<ProgramElement> actions = new DataNode<ProgramElement>(null);
	private static final DataNode<ProgramElement> conditions = new DataNode<ProgramElement>(null);
	private static final DataNode<ProgramElement> events = new DataNode<ProgramElement>(null);
	private static final DataNode<ProgramElement> structures = new DataNode<ProgramElement>(null);


	private static final DataNode<ProgramElement> basicComponents = new DataNode<ProgramElement>(null);
	private static final DataNode<ProgramElement> controlerBoards = new DataNode<ProgramElement>(null);
	private static final DataNode<ProgramElement> sensorsActors = new DataNode<ProgramElement>(null);
	private static final DataNode<ProgramElement> otherComponents = new DataNode<ProgramElement>(null);

	
	
	// Main function which assigns all possible elements to the top lists for actions, events and structures 
	public static void loadElements(boolean loadIntoProductionGUI)
	{
		currentlyLoadingIDEelements = true;
		
		if (loadIntoProductionGUI)
		{
			// Remove in case they already exist (should not be the case)
			featuresProgram.clear();
			actions.removeAllChildren();
			conditions.removeAllChildren();
			events.removeAllChildren();
			structures.removeAllChildren();
			
			otherComponents.removeAllChildren();
			basicComponents.removeAllChildren();
			controlerBoards.removeAllChildren();
			sensorsActors.removeAllChildren();
			
			
			featuresProgram.addPage(ActionsSectionManager.getName(), actions);
			featuresProgram.addPage(ConditionsSectionManager.getName(), conditions);
			featuresProgram.addPage(EventsSectionManager.getName(), events);
			featuresProgram.addPage(StructuresSectionManager.getName(), structures);
		}
		
		String funcDir = LocationPreparator.getFunctionalityDirectory();
		
		Multithreader mt = new Multithreader();
		
		mt.add(() -> FeatureLoader.loadFeatureType(funcDir, "functionality.", "actions", actions, loadIntoProductionGUI));
		mt.add(() -> FeatureLoader.loadFeatureType(funcDir, "functionality.", "conditions", conditions, loadIntoProductionGUI));
		mt.add(() -> FeatureLoader.loadFeatureType(funcDir, "functionality.", "events", events, loadIntoProductionGUI));
		mt.add(() -> FeatureLoader.loadFeatureType(funcDir, "functionality.", "structures", structures, loadIntoProductionGUI));
			
		
		if (loadIntoProductionGUI)
		{
			String eleDir = LocationPreparator.getElectronicDirectory();

			// only needed if into production GUI
			mt.add(() -> FeatureLoader.loadFeatureType(eleDir, "electronic.", "controlerBoards", controlerBoards, true, false, ActionsSectionManager.class));
			mt.add(() -> FeatureLoader.loadFeatureType(eleDir, "electronic.", "basicComponents", basicComponents, true, false, StructuresSectionManager.class));
			mt.add(() -> FeatureLoader.loadFeatureType(eleDir, "electronic.", "sensorsActors", sensorsActors, true, false, EventsSectionManager.class));
			mt.add(() -> FeatureLoader.loadFeatureType(eleDir, "electronic.", "otherComponents", otherComponents, true, false, ConditionsSectionManager.class));
		}
		
		mt.runAll();
		
		
		currentlyLoadingIDEelements = false;
		

		/*
		// Length doesn't match. That means some visualization function is missing
		if (FunctionalityLoader.creatingMethods.size() != FunctionalityLoader.visualizingMethods.size())
		{
			Map<String, Method> cre = new HashMap<String, Method>(FunctionalityLoader.creatingMethods);
			Map<String, Method> vis = new HashMap<String, Method>(FunctionalityLoader.visualizingMethods);
			
			for (String func: cre.keySet())
			{
				if (!vis.containsKey(func))
				{
					InfoErrorHandler.callPrecompilingError("The functionality '" + func + "' is missing a corresponding VISUALIZATION function!\nEnsure that both 'create_" + func +"' as well as visualize_" + func + "' exist!");
					vis.remove(func);
				}					
			}
			for (String func: vis.keySet())
			{
				InfoErrorHandler.callPrecompilingError("The functionality '" + func + "' is missing a corresponding CREATE function!\nEnsure that both 'create_" + func +"' as well as visualize_" + func + "' exist!");
			}
		}
		*/
		
		//DataNode<ProgrammElement> actBasicNode = attachToNodeUndelatable(actions, create_ActBasicNode());
		//attachToNodeUndelatable(actBasicNode, create_ElDelay() );
		
		
		/*
		// Load all elements
		Actions.applyStandardTree(actions);
		verifyUsageOfAllFunctions(Actions.class, actions);
		
		Conditions.applyStandardTree(conditions);
		verifyUsageOfAllFunctions(Conditions.class, conditions);

		Events.applyStandardTree(events);
		verifyUsageOfAllFunctions(Events.class, events);

		Structures.applyStandardTree(structures);
		verifyUsageOfAllFunctions(Structures.class, structures);
		*/
		
		
	}
	
	
	private static void verifyUsageOfAllFunctions(Class funcClass, DataNode<ProgramElement> rootnode)
	{
		if(true)
			return;
		
		Method[] m = funcClass.getMethods();
		
		List<String> meths = new ArrayList<String>();
        for (Method method: m)
        {
        	String mth = method.getName();
        	
        	if (mth.startsWith("create_"))
        	{
            	System.out.println("Func: " + mth + " trns to: " + mth.substring(7));
            	meths.add(mth.substring(7));
        	}
        }        
        
        ProgramElement[] dat = new ProgramElement[1];
		rootnode.applyToChildrenTotal(dat , () -> {
			System.out.println("Searched Func Name: " + dat[0].getFunctionalityName());
			if (meths.contains(dat[0].getFunctionalityName()))
				meths.remove(dat[0].getFunctionalityName());			
		}, true);
		
		
		for (String str: meths)
		{
			InfoErrorHandler.callPrecompilingError("The " + funcClass.getSimpleName() + "functionality '" + str + "' has not been made available for the user!\nEnsure that 'create_" + str + "' is used somewhere inside the function 'applyStandardTree()'.");
		}
		
	}
	
	
	public static int getNodeTreeVersion()
	{
		return(
				Actions.getVersion()+
				Conditions.getVersion()+
				Events.getVersion()+
				Structures.getVersion()
				);
	}
	
	
	
	// Additional required functions for implementing the functionalities
	
	
	public static ProgramElement createProgramContent(String featureName)
	{
		try
		{	
			// Call the corresponding creation function from Functionality
			return((ProgramElement) FeatureLoader.creatingMethods.get(featureName).invoke(null));
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NullPointerException e)
		{
			InfoErrorHandler.callPrecompilingError("Failed to create the following feature: " + featureName + "\nIf you ran this program directly, you have the wrong version for the file you loaded.\nIf you are working on the Java source code of project, please ensure that a class in 'main.functionality'\nhas the following public function: \ncreate_" + featureName + "()");
		}
		return(null);
	}
	
	/*
	public static ProgramContent createProgramContent(String functionalityName)
	{
		try
		{	
			// Call the corresponding creation function from Functionality
			return((ProgramContent) Functionality.class.getMethod("create_" + functionalityName).invoke(null));
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			InfoErrorHandler.callPrecompilingError("Failed to create the following functionality: " + functionalityName + "\nIf you ran this program directly, you have the wrong version for the file you loaded.\nIf you are working on the Java source code of project, please ensure that a class in 'main.functionality'\nhas the following public function: \ncreate_" + functionalityName + "()");
		}
		return(null);
	}
	*/
	
	
	
	public static VisualizableProgramElement visualizeElementContent(FunctionalityContent content)
	{
		try
		{	
			// Call the corresponding visualization function from Functionality
			VisualizableProgramElement visElement = (VisualizableProgramElement) FeatureLoader.visualizingMethods.get(content.getFunctionalityName()).invoke(null, content);
			//VisualizableProgrammElement visElement = (VisualizableProgrammElement) Functionality.class.getMethod("visualize_" + content.getFunctionalityName(), ProgramContent.class).invoke(null, content);
			visElement.getContent().addFixedOptionalArguments(visElement); // Create the fixed additional arguments if existing
			visElement.aquireOptionalArguments();
			return(visElement);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NullPointerException e)
		{
			InfoErrorHandler.callPrecompilingError("Failed to visualize the following functionality: " + content.getFunctionalityName() + "\nIf you ran this program directly, you have the wrong version for the file you loaded.\nIf you are working on the Java source code of project, please ensure that a class in a subdirectory of 'resources.functionality'\nhas the following public function: \nvisualize_" + content.getFunctionalityName() + "(ProgramContent content)");
			e.printStackTrace();
		}
		return(null);
	}
	
	
	public static void visualizeAllNodes(DataNode<ProgramElement> rootNode, boolean setCopiedWithData)
	{		
		ProgramElement[] ele = new ProgramElement[1];
		@SuppressWarnings("unchecked")
		DataNode<ProgramElement>[] node = new DataNode[1];
		
		rootNode.applyToChildrenTotal(ele, node,
			() -> {
				if (ele[0] instanceof FunctionalityContent ) // Only if not visualized already
				{
					VisualizableProgramElement visEl = visualizeElementContent((FunctionalityContent)ele[0]); // create the visualisation
					
					node[0].setData(visEl); // set to node
					
					if (setCopiedWithData)
						visEl.setCopiedWithData();
				}
		}, true);
	}


	public static DataNode<ProgramElement> getActions()
	{
		return(actions);
	}
	public static DataNode<ProgramElement> getEvents()
	{
		return(events);
	}
	public static DataNode<ProgramElement> getConditions()
	{
		return(conditions);
	}
	public static DataNode<ProgramElement> getStructures()
	{
		return(structures);
	}
	

	public static DataNode<ProgramElement> getOtherComponents()
	{
		return(otherComponents);
	}
	public static DataNode<ProgramElement> getBasicComponents()
	{
		return(basicComponents);
	}
	public static DataNode<ProgramElement> getControlerBoards()
	{
		return(controlerBoards);
	}
	public static DataNode<ProgramElement> getSensorsActors()
	{
		return(sensorsActors);
	}
	
	
	public static void applyRootElementNodes()
	{
		ActionsSectionManager.getSelf().setRootElementNode(getActions());
		StructuresSectionManager.getSelf().setRootElementNode(getStructures());
		EventsSectionManager.getSelf().setRootElementNode(getEvents());
		ConditionsSectionManager.getSelf().setRootElementNode(getConditions());		
	}

	
	

	public static Program getFeaturesProgram()
	{
		return(featuresProgram);
	}
	
	
	

	protected static int parseI2CAddress(String txtAddr)
	{
		int addr = -1;
		try {
			addr = Integer.parseInt(txtAddr, 16);
		}
		catch (NumberFormatException e)
		{
			addr = -1;
		}
		
		if (addr == -1)
			if (SIMULATED)
			{
				InfoErrorHandler.printExecutionErrorMessage("Warning: The I2C address (" + txtAddr + ") is invalid!");
				return(0);
			}
			else
			{
				Execution.setError("The I2C address (" + txtAddr + ") is invalid!", false);
				return(-1);
			}
		
		return(addr);
	}

}
