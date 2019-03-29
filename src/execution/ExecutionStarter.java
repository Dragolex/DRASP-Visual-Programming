package execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataTypes.DataNode;
import dataTypes.ProgramElement;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.minor.Pair;
import execution.handlers.ExecutionErrorHandler;
import execution.handlers.InfoErrorHandler;
import execution.handlers.LabelHandler;
import execution.handlers.VariableHandler;
import main.functionality.SharedComponents;
import main.functionality.UsedFunctionalityFlags;
import main.functionality.helperControlers.AlarmControler;
import productionGUI.ProductionGUI;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.elements.VisualizableProgramElement;


@SuppressWarnings("deprecation")
public class ExecutionStarter
{
	volatile private static Map<String, FunctionalityContent> programContents = new HashMap<String, FunctionalityContent>();
	volatile private static Map<FunctionalityContent, String> programIndices = new HashMap<FunctionalityContent, String>();
	

	public static void startLoadedProgram(boolean simulated, boolean visualizedData, boolean tracked)
	{
		startProgram(ProductionGUI.getVisualizedProgram(), simulated, visualizedData, tracked);
	}
	
	public static void startProgram(Program program, boolean simulated, boolean visualizedData, boolean tracked)
	{
		Execution.setSimulated(simulated);
		Execution.setRunningInGUI(visualizedData);
		Execution.setTracked(tracked);
		
		UsedFunctionalityFlags.letExtractRequiredResources();
		
		
		if (visualizedData)
			Execution.prepareConsole("Program Output (Console)"); // Start the console using the local input
		
		prepareDebugProgramIndices();
		
		LabelHandler.clearLabels();
		VariableHandler.clearVariables();
		AlarmControler.clearAlarms();
		
		SharedComponents.resetGlobalObjects();
		
		boolean foundContent = false;
		//List<ProgramElementOnGUI> incompleteLines = new ArrayList<>();
		List<Pair<VisualizableProgramElement, Integer>> missingParameters = new ArrayList<>();
		
		VisualizableProgramElement[] element = new VisualizableProgramElement[1]; // This array of size 1 is used to transfer data across multiple Runnables (Lambdas)
																				  // as used by the "applyToChildrenTotal" function below. However those are only relevant when visualizedData is true
		
		
		List<DataNode<ProgramElement>> activePageRoots = new ArrayList<>(); // List of rootnodes to execute
		
		
		// Loop through all content pages
		//for(String page: DataControler.getProgramPages())
		for(String page: program.getPages())
		{
			//DataNode<ProgrammElement> rootNode = DataControler.getPageRoot(page); // Get the root-node for every page
			DataNode<ProgramElement> rootNode = program.getPageRoot(page);
			
			if (!rootNode.hasChildrenHidden() && ((ContentsSectionManager.getSelf()==null) || ContentsSectionManager.getSelf().pageIsActive(page))) // if the page is active and not deactivated
			{
				InfoErrorHandler.printEnvironmentInfoMessage("VERIFYING PROGRAM PAGE: " + page);
				
				activePageRoots.add(rootNode);
				
				if (!rootNode.isLeaf()) foundContent = true; // if there's any content, mark that
				
				
				if (visualizedData)
				{
					//ProgrammElement[] elemHolder = new ProgrammElement[1];
					//rootNode.selfreplaceTotalNodes(elemHolder, () -> { return(elemHolder[0].recreateContent());}, true);
					
					
					rootNode.applyToChildrenTotal(element, () -> // RUNNABLE LAMBDA executed for all children nodes and children's children nodes etc.
						{
							if (!element[0].getContent().isOutcommented())
							{
								element[0].getContent().setBeenTraversed(false);
								element[0].getContent().clearSetArguments();
								
								element[0].getContent().setPreparedSubActions(null);
								
								element[0].getControlerOnGUI().stopMarking();
								
								element[0].applyGUIargumentData(missingParameters); // Apply the GUI variables and make "content" (ProgramContent) ready.
							}
							
						}, true);
				}
			}
		}

		
		// Abort if no data
		if (!foundContent) // If the root has no children elements
		{
			InfoErrorHandler.callPrecompilingError("Program does not have any elements to run.");
			return;
		}
		
		
		// Show whether some lines are incomplete (means variables are not set)
		if (!missingParameters.isEmpty())
		{
			boolean contin = ExecutionErrorHandler.showMissingArgumentsMessage(missingParameters);
			
			if (!contin)
			{
				InfoErrorHandler.printExecutionInfoMessage("START ABORTED!");
				return;
			}
			else
			{
				for(Pair<VisualizableProgramElement, Integer> dat: missingParameters)
				{
					VisualizableProgramElement ele = dat.first;
					ele.getControlerOnGUI().stopMarking();
				}
			}
			
		}
		
		
		// Loop and execute all pages
		InfoErrorHandler.printEnvironmentInfoMessage("COMPILING PROGRAM");
		
		ProgramElement[] elementPr = new ProgramElement[1]; // same counts as for 'element' above and also for 'eventsCount' below
		
		Integer[] eventsCount = new Integer[1];
		eventsCount[0] = 0;
		
		for(DataNode<ProgramElement> root: activePageRoots)
		{
			root.applyToChildrenTotal(elementPr, () ->
				{
					//elementPr[0].getContent().resolveConstantArguments(); // Resolve all constant arguments
					if (elementPr[0].isEvent())
					if (!elementPr[0].getContent().isOutcommented())
						eventsCount[0]++;
				},
				true);
		}
		
		
		InfoErrorHandler.printEnvironmentInfoMessage("LAUNCHING EXECUTION");
		program.prepareAndStart(activePageRoots, eventsCount[0]);
		//new CoreExecution(activePageRoots, eventsCount[0]).start();
		
	}
	
	
	public static void prepareDebugProgramIndices()
	{
		programContents.clear();
		programIndices.clear();
		
		ProgramElement[] element = new ProgramElement[1];
		
		int[] index = new int[1];
		index[0] = 0;
		
		// Loop through all content pages
		for(String page: ProductionGUI.getVisualizedProgram().getPages())
		{
			DataNode<ProgramElement> rootNode = ProductionGUI.getVisualizedProgram().getPageRoot(page); // Get the root-node for every page
			
			if (!rootNode.hasChildrenHidden() && ((ContentsSectionManager.getSelf()==null) || ContentsSectionManager.getSelf().pageIsActive(page))) // if the page is active and not deactivated
			{
				rootNode.applyToChildrenTotal(element, () -> // RUNNABLE LAMBDA executed for all children nodes and children's children nodes etc.
						{
							element[0].getContent().setCodePageName(page);
							
							String str = page+":"+index[0]++;
							
							programContents.put(str, element[0].getContent());
							programIndices.put(element[0].getContent(), str);							
						}, true);
			}
		}
	}
	
	public static FunctionalityContent getProgramContent(String identifier)
	{
		return(programContents.getOrDefault(identifier, null));
	}
	public static String getProgramIndex(FunctionalityContent content)
	{
		return(programIndices.getOrDefault(content, null));
	}
	
	
	public static void applyToAllcontent(FunctionalityContent[] cont, Runnable func)
	{
		for(FunctionalityContent content: programIndices.keySet())
		{
			cont[0] = content;
			func.run();
		}
	}
	
	
	
	
	

}
