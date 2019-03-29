package main.functionality;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import productionGUI.sections.elements.VisualizableProgramElement;

public abstract class Structures extends UsedFunctionalityFlags {

	public static int nodeTreeStructureVersion = 113; // Increase when changing the tree! It's required for loading files correctly
	
	protected static int getVersion() {return(nodeTreeStructureVersion);};
	
	
	public static void applyStandardTree(DataNode<ProgramElement> structures)
	{
		// STRUCTURES
		
		/*
		
		DataNode<ProgrammElement> struProgNode = attachToNodeUndelatable(structures, create_StruProgNode());
		attachToNodeUndelatable(struProgNode, create_StComEv());
		attachToNodeUndelatable(struProgNode, create_StElse());
		attachToNodeUndelatable(struProgNode, create_StQuitEv());
		// ... more
		
		
		DataNode<ProgrammElement> struVariableNode = attachToNodeUndelatable(structures, create_StruVariableNode());				
		
		attachToNodeUndelatable(struVariableNode, create_StSetBinVar());
		attachToNodeUndelatable(struVariableNode, create_StSetNumVar());
		attachToNodeUndelatable(struVariableNode, create_StSetTexVar());
		attachToNodeUndelatable(struVariableNode, create_StReplaceText());
		attachToNodeUndelatable(struVariableNode, create_StSetRanVar());
		attachToNodeUndelatable(struVariableNode, create_StSetLabelVar());

		
		//attachToNodeUndelatable(struVariableNode, create_StSetTermVar());
		// ... more
		
		
		DataNode<ProgrammElement> struLabelNode = attachToNodeUndelatable(structures, create_StruLabelNode());
		
		attachToNodeUndelatable(struLabelNode, create_StLabel());
		attachToNodeUndelatable(struLabelNode, create_StLabelExecute());
		attachToNodeUndelatable(struLabelNode, create_StLabelJump());
		attachToNodeUndelatable(struLabelNode, create_StLabelAlarm());
		// ... more
		
		
		DataNode<ProgrammElement> actMathNode = attachToNodeUndelatable(structures, create_StruMathNode());
		attachToNodeUndelatable(actMathNode, create_StCalcTerm());
		attachToNodeUndelatable(actMathNode, create_StSum());
		attachToNodeUndelatable(actMathNode, create_StAverage());
		attachToNodeUndelatable(actMathNode, create_StMedian());
		attachToNodeUndelatable(actMathNode, create_StRound());
		attachToNodeUndelatable(actMathNode, create_StAND());
		attachToNodeUndelatable(actMathNode, create_StOR());
		attachToNodeUndelatable(actMathNode, create_StModulo());

		// ... more



		DataNode<ProgrammElement> struLoopNode = attachToNodeUndelatable(structures, create_StruLoopNode());
		
		attachToNodeUndelatable(struLoopNode, create_StRepEv());
		attachToNodeUndelatable(struLoopNode, create_StForLoop());
		attachToNodeUndelatable(struLoopNode, create_StXLoop());
		attachToNodeUndelatable(struLoopNode, create_StFadeLoop());
		attachToNodeUndelatable(struLoopNode, create_StListLoop());
		attachToNodeUndelatable(struLoopNode, create_StQuitLoop());
		// ... more

		
		
		
		DataNode<ProgrammElement> struListNode = attachToNodeUndelatable(structures, create_StruListNode());				

		attachToNodeUndelatable(struListNode, create_StSetListNum());
		attachToNodeUndelatable(struListNode, create_StSetListText());
		attachToNodeUndelatable(struListNode, create_StRemList());
		attachToNodeUndelatable(struListNode, create_StGetList());
		attachToNodeUndelatable(struListNode, create_StGetListSize());
		attachToNodeUndelatable(struListNode, create_StListToText());
		// ... more

		
		DataNode<ProgrammElement> struGraphNode = attachToNodeUndelatable(structures, create_StruSplineNode());

		attachToNodeUndelatable(struGraphNode, create_StCreateSpline());
		//attachToNodeUndelatable(struGraphNode, create_StSplineNodeDir());
		attachToNodeUndelatable(struGraphNode, create_StSplineNodeVar());
		attachToNodeUndelatable(struGraphNode, create_StSplineGetLimits());
		//attachToNodeUndelatable(struGraphNode, create_StEditSpline());
		attachToNodeUndelatable(struGraphNode, create_StSplineCrop());
		//attachToNodeUndelatable(struGraphNode, create_StSplineCopy()); // Enable to copy from one to another as well to merge		
		// ... more

		

		DataNode<ProgrammElement> struAdvancedNode = attachToNodeUndelatable(structures, create_StruAdvancedNode());				
		
		attachToNodeUndelatable(struAdvancedNode, create_StRepIf());
		attachToNodeUndelatable(struAdvancedNode, create_StVirtualEvent());
		//attachToNodeUndelatable(struAdvancedNode, create_StEndEvCond());
		
		// ... more
		
		
		
		//DataNode<ProgrammElement> struCustomNode = 
				attachToNodeUndelatable(structures, create_StruCustomNode());
		
		
		//////////
		
		*/
	}
	
	

	// STRUCTURES root nodes
	
	public static ProgramElement create_StruProgNode()
	{
		return(new FunctionalityContent("StruProgNode"));
	}
	static public VisualizableProgramElement visualize_StruProgNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Programmatical", "Structures for programmatical functionalities.\nDrag into the queue and provide the desired parameters.", false));
	}

	
	public static ProgramElement create_StruLoopNode()
	{
		return(new FunctionalityContent("StruLoopNode"));
	}
	static public VisualizableProgramElement visualize_StruLoopNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Loops", "Structures to repeat a section of code.\nDrag into the queue and provide the desired parameters.\n\nDrag children elements ontop when available.", false));
	}

		
	public static ProgramElement create_StruLabelNode()
	{
		return(new FunctionalityContent("StruLabelNode"));
	}
	static public VisualizableProgramElement visualize_StruLabelNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Labels", "Structures to work with labels. Those are named positions in the action queue defiend by a text.\nDrag into the queue and provide the desired parameters.", false));
	}
	
	static public FunctionalityContent create_StruMathNode()
	{
		return(new FunctionalityContent("StruMathNode"));
	}
	static public VisualizableProgramElement visualize_StruMathNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Mathematical", "Mathematical and logical operations on variables.", false));
	}
		
	public static ProgramElement create_StruVariableNode()
	{
		return(new FunctionalityContent("StruVariableNode"));
	}
	static public VisualizableProgramElement visualize_StruVariableNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Variables", "Structures for 'Variables'\nThose are defined as a certain piece of data\nwhich can be a number, text oder identifier for a more complex kind of data.\nVariables can be used as parameters by writing '#' in front of them.\nThe parameter-field will display the same color wherever the same variable is used.\n\nDrag into the queue and provide the desired parameters.", false));
	}
	
	
	public static ProgramElement create_StruListNode()
	{
		return(new FunctionalityContent("StruListNode"));
	}
	static public VisualizableProgramElement visualize_StruListNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Lists", "Structures for 'Lists'.\nThose are defined by an identifier and can contain a range of values\naccessible by an index (aka the row on the list).\nDrag into the queue and provide the desired parameters.", false));
	}
	

	public static ProgramElement create_StruSplineNode()
	{
		return(new FunctionalityContent("StruSplineNode"));
	}
	static public VisualizableProgramElement visualize_StruSplineNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Splines", "Structures for 'Splines'.\nSplines are a more sophiscated type of lists with numbers.\nThey basically associate an index with a number but whenever you set an index\nvalues are interpolated to close the gap to the last index!\nThose graphs can be used to effectively display the data from a sensor\nor smoothly animate lights if using a PWM board.\n\nDrag into the queue and provide the desired parameters.", false));
	}
	
	
	
	public static ProgramElement create_StruAdvancedNode()
	{
		return(new FunctionalityContent("StruAdvancedNode"));
	}
	static public VisualizableProgramElement visualize_StruAdvancedNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Advanced", "Attention! Using those structures can make a program harder to comprehend!\nPlease only if when they truly bring a benefit.", false));
	}
	
				
	public static ProgramElement create_StruCustomNode()
	{
		return(new FunctionalityContent("StruCustomNode"));
	}
	static public VisualizableProgramElement visualize_StruCustomNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Custom with Values", "Structures with defined values.\nDrag here from the queue to save for re-use.\nKeep <CTRL> pressed when dragging to delete from here!", true));
	}
		
	
	
	
	
	
}
