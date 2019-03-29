package main;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.apache.commons.codec.binary.Base64;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import dataTypes.contentValueRepresentations.AbstractContentValue;
import execution.Execution;
import execution.Program;
import execution.handlers.InfoErrorHandler;
import execution.handlers.VariableHandler;
import main.functionality.Functionality;
import productionGUI.ProductionGUI;
import productionGUI.controlers.ButtonsRegionControl;
import productionGUI.controlers.UndoRedoControler;
import productionGUI.sections.elementManagers.AbstractSectionManager;
import productionGUI.sections.elementManagers.ActionsSectionManager;
import productionGUI.sections.elementManagers.ConditionsSectionManager;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.elementManagers.EventsSectionManager;
import productionGUI.sections.elementManagers.StructuresSectionManager;
import productionGUI.sections.elements.VisualizableProgramElement;
import settings.EnvironmentDataHandler;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;
import staticHelpers.GuiMsgHelper;

@SuppressWarnings("deprecation")
public class DataControler
{	
	static String[] depthIndentationLookup;	
	
	public static void init()
	{
		computeIndentationLookup(20);		
	}
	
	public static DataNode<ProgramElement> getPageNodeForAdd(String page)
	{
		if (ProductionGUI.getVisualizedProgram().containsPage(page))
			return(ProductionGUI.getVisualizedProgram().getPageRoot(page));
		
		return(getModifiableNode(Functionality.getFeaturesProgram().getPageRoot(page)));
	}
	
	
	public static void replaceData(String page, DataNode<ProgramElement> newNode)
	{
		if (ProductionGUI.getVisualizedProgram().containsPage(page))
			ProductionGUI.getVisualizedProgram().replaceData(page, newNode);
		else
			Functionality.getFeaturesProgram().replaceData(page, newNode);
	}
	

	
	
	
	
	
	public static Program loadProgramFile(String filePath, boolean clearFirst, boolean visualize)
	{
		return(loadProgramFile(filePath, clearFirst, visualize, null));
	}
	public static Program loadProgramFile(String filePath, boolean clearFirst, boolean visualize, String enforcedPageName) // the enforcedPageName is used to load exercises for tutorials
	{
		InfoErrorHandler.printEnvironmentInfoMessage("Loading file: " + filePath);
		
		if (visualize)
			Execution.stop(); // if vizualized, allow only one execution
		
		
		if (clearFirst)
		{
			VariableHandler.clear();
			
			ProductionGUI.getVisualizedProgram().clear();
			

			if (ActionsSectionManager.getSelf() != null)
			{
				//// Delete modifiable nodes only
				if (visualize)
				{
						ActionsSectionManager.getSelf().clearNodes(true);
						ConditionsSectionManager.getSelf().clearNodes(true);
						EventsSectionManager.getSelf().clearNodes(true);
						StructuresSectionManager.getSelf().clearNodes(true);
				}
				////
				
				ContentsSectionManager.getSelf().clearNodes(false); // Delete all contents
			}
		}
		
		
		Program program;
		if (visualize)
			program = ProductionGUI.getVisualizedProgram(); // apply to the existing, visualized program
		else
			program = new Program();
			
		try
		{
			List<String> allLines = FileHelpers.readAllLines(new File(filePath));
			
			if ((!allLines.get(0).equals(GlobalSettings.rightTypeIndicator)) && (!allLines.get(0).substring(1).equals(GlobalSettings.rightTypeIndicator))) // It's not a right file
			{
				failedLoading("The program file does not have the right content! Path: " + filePath, "", null, false);
				return(null);
			}
			
			boolean actualFileVersion = false;
			if (allLines.get(1).equals(GlobalSettings.treeVersionLine))
			{
				actualFileVersion = (Double.valueOf(allLines.get(2)).equals( (Double.valueOf((double) Functionality.getNodeTreeVersion()))));
			}
			
			
			// Read all pages
			
			int programPagesCount = 0;
			for(List<String> pageLines: findNodeLinesDataPages(allLines, !visualize))
			{
				String pageName = pageLines.get(0);
				
				if (pageLines.size() <= 1)
					continue;
				
				if (enforcedPageName != null)
					pageName = enforcedPageName;
								
				DataNode<ProgramElement> rootNode;
				
				boolean programPage = false;

				if (GlobalSettings.customElementsSectionStartString.equals(pageLines.get(1))) // Contents of the custom nodes
				{
					switch(pageName)
					{
					case "Conditions":
						List<DataNode<ProgramElement>> ch = ConditionsSectionManager.getSelf().getRootElementNode().getChildrenAlways();
						rootNode = ch.get(ch.size()-1);
						break;
					case "Actions":
						ch = ActionsSectionManager.getSelf().getRootElementNode().getChildrenAlways();
						rootNode = ch.get(ch.size()-1);
						break;
					case "Events":
						ch = EventsSectionManager.getSelf().getRootElementNode().getChildrenAlways();
						rootNode = ch.get(ch.size()-1);
						break;
					case "Structures/Data":
						ch = StructuresSectionManager.getSelf().getRootElementNode().getChildrenAlways();
						rootNode = ch.get(ch.size()-1);
						break;
						
					default:
						InfoErrorHandler.callFileLoadError("A file seems to have an invalid type of custom nodes: " + pageName);
						rootNode = null;
					}
				}
				else // Normal program page
				{
					if (program.containsPage(pageName))
					{
						rootNode = program.getPageRoot(pageName); // Get the node
	
						int res = GuiMsgHelper.askQuestionDirect("The file contains another content page with the name '" + pageName + "'","Do you want to rename this page (yes) to keep both\nor add the contents to the end of the existing page (no)?\nIf you abort, the page will be skipped.");
							
						if (res == -1)
							continue;
						if (res == 1)
						{
							int n = 0;
							do { n++; }
								while(program.containsPage(pageName+"_"+n));
							pageName = pageName+"_"+n;
							rootNode = new DataNode<ProgramElement>(null);
						}
					}
					else
						rootNode = new DataNode<ProgramElement>(null);
	
					programPagesCount++;
					programPage = true;
					
					program.addPage(pageName, rootNode);
				}
				
				
				boolean isActive = (!pageLines.get(programPage ? 1 : 2).equals(GlobalSettings.inactiveKeyword));
				
				if (isActive)
				{
					if (visualize && programPage)
						UndoRedoControler.getSelf().appliedChange(pageName, false);
					
					readProgrammElementNodeTree(pageLines.subList(programPage ? 2 : 3, pageLines.size()), rootNode, visualize, pageName);

					if (visualize && programPage)
						UndoRedoControler.getSelf().appliedChange(pageName, true);
				}
				if (visualize && !isActive)
					rootNode.hideChildren();

				
			}
			
			
			if (programPagesCount == 0)
			{
				if (visualize)
					Functionality.visualizeAllNodes(ProductionGUI.getOrCreateMainPage(), true);
			}
			
			
			if (visualize && clearFirst)
			{
				int ind = allLines.indexOf(GlobalSettings.collapsedNodesString);
				if (ind > -1)
				{
					String bitset = allLines.get(ind+1);
					applyCollapseDataAndBitSet(BitSet.valueOf(Base64.decodeBase64(bitset.getBytes(StandardCharsets.UTF_8))), actualFileVersion);
				}
			}
			
		}
		catch (Exception e)
		{
			failedLoading("Opening the following program file failed: " + filePath, "Error: " + e.getMessage(), e, true);
			return(null);
		}
		
		
		if (visualize)
		{	
			if (ActionsSectionManager.getSelf() != null)
			{
				try {
				
					ActionsSectionManager.getSelf().renewElementsRealizationFull();
					ConditionsSectionManager.getSelf().renewElementsRealizationFull();
					EventsSectionManager.getSelf().renewElementsRealizationFull();
					StructuresSectionManager.getSelf().renewElementsRealizationFull();
							
					ContentsSectionManager.getSelf().renewElementsRealizationFull();
					
					ContentsSectionManager.getSelf().reinitializePagesButtons();
				}
				catch(Exception e)
				{
					failedLoading("Loading the program: " + filePath + " failed!\nThe cause could be a corrupted file or that it has been made in a version of DRASP where functionality parameters are defined differently.",
							"Error: " + e.getClass().getName() + ": " + e.getMessage(),
							e, true);
					
					return(null);
				}
				
				finalizeCollapseSetting();
			}
			
			ProductionGUI.setCurrentFile(filePath);
			ProductionGUI.setLoadedFile(true);
		}

		
		return(program);
	}
	
	private static void failedLoading(String message, String errorStr, Exception e, boolean printTrace)
	{	
		InfoErrorHandler.callPrecompilingError(message+"\n"+errorStr);
		
		GuiMsgHelper.showErrorAlwaysPopup(message, errorStr, "New Program");
		
		if (printTrace)
			e.printStackTrace();
		
		ButtonsRegionControl.getSelf().New();
	}
	
	
	private static List<List<String>> findNodeLinesDataPages(List<String> lines, boolean programOnly)
	{
		List<List<String>> resultLists = new ArrayList<>(); 
		
		
		List<String> currentSubList = null;
		
		for(String line: lines)
		{
			if (!line.isEmpty()) // ignore empty lines
			if (line.toUpperCase().startsWith(GlobalSettings.programPageSectionStartString))
			{
				if (currentSubList != null)
					resultLists.add(currentSubList);
				
				currentSubList = new ArrayList<>();
				currentSubList.add( line.substring(GlobalSettings.programPageSectionStartString.length()+2, line.length()) );
			}
			else
			if ( line.toUpperCase().startsWith(GlobalSettings.customElementsSectionStartString) )
			{
				if (currentSubList != null)
					resultLists.add(currentSubList);
				
				if (programOnly)
					currentSubList = null;
				else
				{
					currentSubList = new ArrayList<>();
					currentSubList.add(line.substring(GlobalSettings.customElementsSectionStartString.length()+2, line.length()) );
					currentSubList.add(GlobalSettings.customElementsSectionStartString);
				}
			}
			else
			if (line.toUpperCase().startsWith(GlobalSettings.collapsedNodesString))
				break;
			else
				if (currentSubList != null)
					currentSubList.add(line);
		}
		
		if (currentSubList != null)
			resultLists.add(currentSubList);
		
		
		return(resultLists);
	}
	
	
	
	
	
	public static void readProgrammElementNodeTree(List<String> lines, DataNode<ProgramElement> rootNode, boolean visualize, String pageName)
	{
		if (lines == null) return;
		
		
		int codeLineIndex = 0;
		
		int pos = 0;
		DataNode<ProgramElement> parent = rootNode;
		DataNode<ProgramElement> lastNode = rootNode;

			
		Stack<DataNode<ProgramElement>> subchildStack = new Stack<>();
		
		int depth = 1, lastDepth = 1;
		int linesCount = lines.size();
		
		
		while((pos) < linesCount)
		{
			String elementName = lines.get(pos);
			
			depth = elementName.chars().reduce(0, (a, c) -> a + (c == GlobalSettings.indentationSymbol ? 1 : 0));
			
			
			while(depth > lastDepth)
			{
				subchildStack.push(parent);
				parent = lastNode;
				lastDepth++;
			}
			while(depth < lastDepth)
			{
				parent = subchildStack.pop();
				lastDepth--;
			}
			
			
			
			String contentName = elementName.substring(depth);
			
			boolean outcommented = false;
			if (contentName.startsWith(GlobalSettings.outcommentedSymbol)) // is outcommented!
			{
				contentName = contentName.substring(1);
				outcommented = true;
			}
			
			boolean breakpoint = false;
			if (contentName.startsWith(GlobalSettings.breakpointSymbol)) // is a breakpoint!
			{
				contentName = contentName.substring(1);				
				breakpoint = true;
			}
			
			boolean collapsed = false;
			if (contentName.startsWith(GlobalSettings.collapsedElementSymbol)) // is collapsed!
			{
				contentName = contentName.substring(1);
				collapsed = true;
			}
			
			
			
			// Call the corresponding create-content function from Functionality
			FunctionalityContent content = (FunctionalityContent) Functionality.createProgramContent(contentName);
			if (content == null) return; // if fail
			
			content.setOutcommented(outcommented);
			content.setBreakpoint(breakpoint);
			content.setCollapsedInitialized(collapsed);
			
			content.setCodeLineIndex(codeLineIndex++);
			content.setCodePageName(pageName);
			
			if (content.isEvent())
				if (!(content instanceof ProgramEventContent))
					InfoErrorHandler.callPrecompilingError("The content type of event named '" + contentName + "' IS NOT 'ProgramEventContent' like it has to be!");
			
			AbstractContentValue argument;
			
			int i = 1;
			
			if (pos+i != linesCount)
			{
				String argLine = lines.get(pos + i);
				
				while(argLine.charAt(depth) == GlobalSettings.argumentElementSymbolChar)
				{
					int subPos = 1;
					
					boolean realVar = false;
					boolean eleEditVar = false;
					
					char argCharAtOne = argLine.charAt(depth+1);
					
					if ((argCharAtOne == GlobalSettings.passesAsRealVarSymbolChar) || (argCharAtOne == GlobalSettings.passesAsRealVarAndEleEditVarSymbolChar))
					{
						if ((argCharAtOne == GlobalSettings.passesAsRealVarAndEleEditVarSymbolChar))
							eleEditVar = true;
						
						realVar = true;
						if (argLine.charAt(depth+2) == GlobalSettings.fixedArgumentSymbolChar)
						{
							subPos = 3;
							content.setSpecialOptionalArgIndex();
						}
						else subPos = 2;
					}
					else
						if (argCharAtOne == GlobalSettings.fixedArgumentSymbolChar)
						{
							subPos = 2;
							content.setSpecialOptionalArgIndex();
						}
						else subPos = 1;
					
					
					argument = AbstractContentValue.interpretLine(argLine.substring(depth+subPos), realVar, eleEditVar); // Interpret and create the argument
					
					
					
					content.setArgumentDirect(i-1, argument.getOutputValue(), argument.passesAsRealVar());
					argument.destroy();
					
					
					i++;
					if ((pos + i) >= lines.size()) break;
					argLine = lines.get(pos + i);
				}
			}
			
					
			DataNode<ProgramElement> newNode = new DataNode<ProgramElement>(content);
			parent.addChild(newNode); // Add as a new child
			
			
			// Increase pos: One for the content name and one for every argument
			lastNode = newNode;
			pos += i;
				
		}
		
		
		if (visualize)
			Functionality.visualizeAllNodes(rootNode, true);
		
		if (rootNode.getChildrenAlways().size() > 0)
		{
			InfoErrorHandler.printEnvironmentInfoMessage("Loaded the page '" + pageName + "' with the following program tree:");
			rootNode.printAll();
		}
			
	}

	public static void saveProgramFile(String filePath, boolean saveHidden)
	{
		saveProgramFile(filePath, saveHidden, null);
	}
	public static void saveProgramFile(String filePath, boolean saveHidden, DataNode<ProgramElement> specialRoot)
	{
		List<String> allLines = new ArrayList<>();
		
		allLines.add(GlobalSettings.rightTypeIndicator);
		allLines.add(GlobalSettings.treeVersionLine);
		allLines.add(String.valueOf(Functionality.getNodeTreeVersion()));
		allLines.add(GlobalSettings.indentationSymbolStr);
		
		
		if (specialRoot == null)
		{
			// Write all program pages
			for(String page: ProductionGUI.getVisualizedProgram().getPages())
			{
				boolean isActive = ContentsSectionManager.getSelf().pageIsActive(page);
				
				if (saveHidden || isActive)
					writeProgrammElementNodeTreeSection(ProductionGUI.getVisualizedProgram().getPageRoot(page), allLines, GlobalSettings.programPageSectionStartString + ": " + page, isActive);
			}
		}
		else
		{
			// Write only the given node
			writeProgrammElementNodeTreeSection(specialRoot, allLines, GlobalSettings.programPageSectionStartString + ": Main", true);
		}
		
		
		//// Write the feature sections
		String nm = GlobalSettings.customElementsSectionStartString + ": ";
		writeProgrammElementNodeTreeSection(getModifiableNode(Functionality.getActions()), allLines, nm + ActionsSectionManager.getName(), true);
		writeProgrammElementNodeTreeSection(getModifiableNode(Functionality.getConditions()), allLines, nm + ConditionsSectionManager.getName(), true);
		writeProgrammElementNodeTreeSection(getModifiableNode(Functionality.getEvents()), allLines, nm + EventsSectionManager.getName(), true);
		writeProgrammElementNodeTreeSection(getModifiableNode(Functionality.getStructures()), allLines, nm + StructuresSectionManager.getName(), true);
		////
		
		
		allLines.add(GlobalSettings.collapsedNodesString);
		String str = new String(computeCollapseBitSet().toByteArray());
		allLines.add(new String(Base64.encodeBase64(str.getBytes(StandardCharsets.UTF_8))));
		
		
		// Write the list of lines to the file
		FileHelpers.writeLineListToFile(filePath, allLines, "Could not save the program file to: " + filePath);
		
	}
	
	
	
	
	public static void writeProgrammElementNodeTreeSection(DataNode<ProgramElement> rootNode, List<String> contentLines, String sectionName, boolean isActive)
	{
		if (rootNode == null || rootNode.getChildrenAlways().isEmpty())
			return; // Do not add anything if empty
		
		
		contentLines.add(sectionName);
		contentLines.add(isActive ? GlobalSettings.activeKeyword : GlobalSettings.inactiveKeyword);
		
		Integer[] lastDepth = new Integer[1];
		lastDepth[0] = rootNode.getDepth() + (rootNode.isRoot() ? 1 : 0);
		
		VisualizableProgramElement[] ele = new VisualizableProgramElement[1];
		Integer[] depth = new Integer[1];
		@SuppressWarnings("unchecked")
		DataNode<ProgramElement>[] node = new DataNode[1];
		
		StringBuilder infSymbols = new StringBuilder();
		StringBuilder argSymbols = new StringBuilder();
		
		rootNode.applyToChildrenTotal(ele, node, depth,
			() -> {
				boolean skip = false;
				if (ele[0] == rootNode.getData()) skip = true;
				
				if (!skip)
				{
					
					String contentName = ele[0].getFunctionalityName();
					String indent = depthIndentationLookup[depth[0]];
					
					infSymbols.setLength(0);
					
					if (ele[0].getContent().isOutcommented()) // if the action is outcomented
						infSymbols.append(GlobalSettings.outcommentedSymbol);
					
					if (ele[0].getContent().isBreakpoint()) // if the action is marked as breakpoint
						infSymbols.append(GlobalSettings.breakpointSymbol);
					
					if (node[0].hasChildrenHidden())
						infSymbols.append(GlobalSettings.collapsedElementSymbol);
						
					
					contentLines.add(indent + infSymbols.toString() + contentName);
					
					
										
					AbstractContentValue[] args = ele[0].getArgumentsData();
					int argCount = args.length;
					for (int i = 0; i < argCount; i++)
					{
						argSymbols.setLength(0);
						argSymbols.append(indent);
						argSymbols.append(GlobalSettings.argumentElementSymbol);
						if (args[i].passesAsRealVar())
							if (args[i].canBeEditedByElement())
								argSymbols.append(GlobalSettings.passesAsRealVarAndEleEditVarSymbol);
							else
								argSymbols.append(GlobalSettings.passesAsRealVarSymbol);
						if (ele[0].getContent().checkSpecialFixedArgument(i))
							argSymbols.append(GlobalSettings.fixedArgumentSymbol);
						
						argSymbols.append(args[i].toInterpretableString()); // Write the interpretable string
						
						contentLines.add(argSymbols.toString());
						
						//contentLines.add(indent + GlobalSettings.argumentElementSymbol + (args[i].passesAsRealVar() ? GlobalSettings.passesAsRealVarSymbol : "") + (ele[0].getContent().checkSpecialFixedArgument(i) ? GlobalSettings.fixedArgumentSymbol : "") +  args[i].toInterpretableString()); // Write the interpretable string
					}
					
				}
				
		}, true);
		
		contentLines.add("");
		
		return;
	}
	
	
	
	
	public static DataNode<ProgramElement> getModifiableNode(DataNode<ProgramElement> root)
	{
		if ((!root.isRoot()) && (((VisualizableProgramElement)root.getData()).getIsUserModifiableParentNode())) // if modifiable
			return(root);
		else
		if (root.isLeaf())
			return(null);
		else
		{
			DataNode<ProgramElement> nd;
			
			for(DataNode<ProgramElement> rootNode: root.getChildrenAlways())
			{
				nd = getModifiableNode(rootNode);
				if (nd != null)
					return(nd);
			}
		}
		return(null);
	}



	public static void clearReset(boolean keepFeatures)
	{
		if (!keepFeatures)
		{
			ActionsSectionManager.getSelf().clearNodes(true);
			ConditionsSectionManager.getSelf().clearNodes(true);
			EventsSectionManager.getSelf().clearNodes(true);
			StructuresSectionManager.getSelf().clearNodes(true);
		}
		
		ContentsSectionManager.getSelf().clearNodes(false); // Delete all contents
		
		ProductionGUI.getVisualizedProgram().clear();
		ProductionGUI.createMainPage();
		
		
		if (!keepFeatures)
		{
			ActionsSectionManager.getSelf().renewElementsRealizationFull();
			ConditionsSectionManager.getSelf().renewElementsRealizationFull();
			EventsSectionManager.getSelf().renewElementsRealizationFull();
			StructuresSectionManager.getSelf().renewElementsRealizationFull();
		}
				
		ContentsSectionManager.getSelf().renewElementsRealizationFull();
		
		ContentsSectionManager.getSelf().reinitializePagesButtons();
	}


	public static boolean customFeaturesAdded()
	{
		for(DataNode<ProgramElement> dat: Functionality.getFeaturesProgram().getPageRoots())
			if (!getModifiableNode(dat).isLeaf())
				return(true);
				
		return(false);
	}


	private static void computeIndentationLookup(int size)
	{
		int start;
		
		if (depthIndentationLookup == null)
		{
			start = 0;
			depthIndentationLookup = new String[size];
		}
		else
		{
			start = depthIndentationLookup.length;
			String[] nn = new String[size];
			
			System.arraycopy(depthIndentationLookup, 0, nn, 0, start);
			depthIndentationLookup = nn;
		}
		
		
		for(; start < size; start++)
			depthIndentationLookup[start] = String.join("", Collections.nCopies(start, GlobalSettings.indentationSymbolStr));
	}
	
	
	
	
	private static BitSet computeCollapseBitSet()
	{
		BitSet data = new BitSet();
		Integer[] index = new Integer[1];
		index[0] = 0;
		
		VisualizableProgramElement[] element = new VisualizableProgramElement[1];
		@SuppressWarnings("unchecked")
		DataNode<ProgramElement>[] node = new DataNode[1];
		
		if (data != null)
		for(DataNode<ProgramElement> dat: Functionality.getFeaturesProgram().getPageRoots())
		{
			dat.applyToChildrenTotal(element, node, () ->
			{
				if (!node[0].getChildrenAlways().isEmpty())
				if (!element[0].getIsUserModifiableParentNode())
				{
					if (!node[0].hasChildrenHidden())
						data.set(index[0]);	
				
					index[0]++;
				}
			},true);	
		}
		
		return(data);
	}
	
	
	private static void applyCollapseDataAndBitSet(BitSet data, boolean applyToStandardElements)
	{
		Integer[] index = new Integer[1];
		index[0] = 0;
		VisualizableProgramElement[] element = new VisualizableProgramElement[1];
		@SuppressWarnings("unchecked")
		DataNode<ProgramElement>[] node = new DataNode[1];
		
		
		
		for(DataNode<ProgramElement> dat: Functionality.getFeaturesProgram().getPageRoots())
		{
			dat.applyToChildrenTotal(element, node, () ->
			{
				if (!node[0].getChildrenAlways().isEmpty())
				if (!element[0].getIsUserModifiableParentNode())
				{
					if (applyToStandardElements)
						element[0].getContent().setCollapsedInitialized(!data.get(index[0]));
					else
						element[0].getContent().setCollapsedInitialized(true);
					
					if (!element[0].getContent().isCollapsedInitialized())
					if (element[0].getContent().getVisualization().getControlerOnGUI() != null)
					if (element[0].getContent().getVisualization().getControlerOnGUI().hidesChildren())
						element[0].getContent().getVisualization().getControlerOnGUI().pressedWhole(false, true);
					
					index[0]++;
				}
			},true);
		}
		
		/*
		if (applyToStandardElements)
		for(Entry<String, DataNode<ProgrammElement>> dat: wholeData.entrySet())
		{
			dat.getValue().unhideChildren();
			
			dat.getValue().applyToChildrenTotal(element, node, () ->
				{	
					/*
					if (element[0].getContent().isCollapsedInitialized())
						node[0].hideChildren();
					else
						node[0].unhideChildren();
						*
				}, true);
		}		
		*/
	}
	
	private static void finalizeCollapseSetting()
	{
		VisualizableProgramElement[] element = new VisualizableProgramElement[1];
		@SuppressWarnings("unchecked")
		DataNode<ProgramElement>[] node = new DataNode[1];
		
		for(DataNode<ProgramElement> dat: Functionality.getFeaturesProgram().getPageRoots())
		{
			dat.unhideChildren();
			dat.applyToChildrenTotal(element, node, () -> element[0].getControlerOnGUI().forceCollapse(false, true), true);
		}
		
		for(DataNode<ProgramElement> dat: ProductionGUI.getVisualizedProgram().getPageRoots())
		{
			dat.unhideChildren();
			dat.applyToChildrenTotal(element, node, () -> element[0].getControlerOnGUI().forceCollapse(false, true), true);
		}

				
		AbstractSectionManager[] abstr = new AbstractSectionManager[1];
		AbstractSectionManager.applyForAll(abstr,
				() -> {
					abstr[0].renewElementsRealization();
					abstr[0].adjustSubelementsSize();
					
					abstr[0].adjustContainerSize();
				});
	}
	
	
	/*
	private static BitSet computeCollapseBitSet()
	{
		BitSet data = new BitSet();
		Integer[] index = new Integer[1];
		index[0] = 0;
		
		VisualizableProgrammElement[] element = new VisualizableProgrammElement[1];
		DataNode<ProgrammElement>[] node = new DataNode[1];
		
		for(Entry<String, DataNode<ProgrammElement>> dat: wholeData.entrySet())
		{
			
			dat.getValue().applyToChildrenTotal(element, node, () ->
				{	
					if (node[0].hasChildrenHidden())
						data.set(index[0]);					
					index[0]++;
				}, true);
		}

		return(data);
	}
	
	private static void applyCollapseBitSet(BitSet data)
	{
		Integer[] index = new Integer[1];
		index[0] = 0;
		
		VisualizableProgrammElement[] element = new VisualizableProgrammElement[1];
		DataNode<ProgrammElement>[] node = new DataNode[1];
		
		
		for(Entry<String, DataNode<ProgrammElement>> dat: wholeData.entrySet())
		{
			dat.getValue().applyToChildrenTotal(element, node, () ->
				{	
					if (data.get(index[0]))
						node[0].hideChildren();
					else
						node[0].unhideChildren();
					index[0]++;
				}, true);
		}
	}
	*/

}

