package main.functionality;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import productionGUI.sections.elements.VisualizableProgramElement;


public abstract class Events extends UsedFunctionalityFlags {

	private static int nodeTreeStructureVersion = 112; // Increase when changing the tree! It's requried for loading files correctly
	
	protected static int getVersion() {return(nodeTreeStructureVersion);};
	
	
	public static void applyStandardTree(DataNode<ProgramElement> events)
	{
		
		/*
		
		DataNode<ProgrammElement> evenBaseNode = attachToNodeUndelatable(events, create_EvenBaseNode());
		
		attachToNodeUndelatable(evenBaseNode, create_EvInit());
		attachToNodeUndelatable(evenBaseNode, create_EvRhythmStep());
		attachToNodeUndelatable(evenBaseNode, create_EvStep());
		attachToNodeUndelatable(evenBaseNode, create_EvFade());
		attachToNodeUndelatable(evenBaseNode, create_EvLabeled());
		// ... More basic events
		
		
		
		DataNode<ProgrammElement> evenInputNode = attachToNodeUndelatable(events, create_EvenInputNode());
	
		attachToNodeUndelatable(evenInputNode, create_EvKeyPressed());
		attachToNodeUndelatable(evenInputNode, create_EvKeyDown());
		attachToNodeUndelatable(evenInputNode, create_EvKeyReleased());
		
		attachToNodeUndelatable(evenInputNode, create_EvGPIOchanged());
		attachToNodeUndelatable(evenInputNode, create_EvGPIOresp());
		
		attachToNodeUndelatable(evenInputNode, create_EvAnalogInputChanged());
		attachToNodeUndelatable(evenInputNode, create_EvAnalogInputBetween());
		
		attachToNodeUndelatable(evenInputNode, create_EvRotaryInc());
		attachToNodeUndelatable(evenInputNode, create_EvRotaryDec());
		
		// ... More hardware input events
		
		

		DataNode<ProgrammElement> evenVarNode = attachToNodeUndelatable(events, create_EvenVarNode());
		
		attachToNodeUndelatable(evenVarNode, create_EvIfNumVar());
		attachToNodeUndelatable(evenVarNode, create_EvIfBoolVar());
		attachToNodeUndelatable(evenVarNode, create_EvIfTexVar());
		attachToNodeUndelatable(evenVarNode, create_EvIfChVar());
		

		
		DataNode<ProgrammElement> evenTeleNode = attachToNodeUndelatable(events, create_EvenTeleNode());
		attachToNodeUndelatable(evenTeleNode, create_EvTeleCommand());
		attachToNodeUndelatable(evenTeleNode, create_EvTeleCommandExt());
		// ... More telegram events
		
		
		
		
		DataNode<ProgrammElement> evenWindNode = attachToNodeUndelatable(events, create_EvenWindNode());
		attachToNodeUndelatable(evenWindNode, create_EvWindClosed());
		attachToNodeUndelatable(evenWindNode, create_EvWindButtonPressed());
		attachToNodeUndelatable(evenWindNode, create_EvWindItemPressed());
		attachToNodeUndelatable(evenWindNode, create_EvWindItemHover());
		
		
		// ... More window events
		
		
		DataNode<ProgrammElement> evenNetNode = attachToNodeUndelatable(events, create_EvenNetNode());
		attachToNodeUndelatable(evenNetNode, create_EvNetMsgReceived());
		attachToNodeUndelatable(evenNetNode, create_EvNetEvent());
		attachToNodeUndelatable(evenNetNode, create_EvNewExtCon());
		attachToNodeUndelatable(evenNetNode, create_EvConClosed());
		//attachToNodeUndelatable(evenNetNode, create_EvUart_Received());

		
		DataNode<ProgrammElement> evenMiscNode = attachToNodeUndelatable(events, create_EvenMiscNode());
		attachToNodeUndelatable(evenMiscNode, create_EvClapDetector());
		attachToNodeUndelatable(evenMiscNode, create_EvRegulatorApply());
		//attachToNodeUndelatable(evenMiscNode, create_EvOneWireValueChanged());
		attachToNodeUndelatable(evenMiscNode, create_EvSniffLowMhzModule());
		attachToNodeUndelatable(evenMiscNode, create_EvGetLowMhzModule());
		

		
		
		//attachToNodeUndelatable(evenMiscNode, create_Ev433Mhz());
		
		
		
		
		//DataNode<ProgrammElement> evenCustomNode = 
				attachToNodeUndelatable(events, create_EvenCustomNode());
		
		
		 */
		
		//////////
	}
	

	
	// EVENTS root nodes
	
	

	static public FunctionalityContent create_EvenBaseNode()
	{
		return(new ProgramEventContent("EvenBaseNode"));
	}
	static public VisualizableProgramElement visualize_EvenBaseNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Basic", "Events with generic purposes.\nDrag into the queue and provide the desired parameters.\nWhen the condition is met, nested actions are executed.", false));
	}
	
	
	
	static public FunctionalityContent create_EvenInputNode()
	{
		return(new FunctionalityContent("EvenInputNode"));
	}
	static public VisualizableProgramElement visualize_EvenInputNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Hardware Input", "Events based on hardware input like a keyboard or GPIO.\nDrag into the queue and provide the desired parameters.\nWhen the condition is met, nested actions are executed.", false));
	}
	
	
	static public FunctionalityContent create_EvenVarNode()
	{
		return(new FunctionalityContent("EvenVarNode"));
	}
	static public VisualizableProgramElement visualize_EvenVarNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Variable Check", "Events based on variables.\nNote that those event conditions need to be checked constantly in the background\nand might have an impact on performance if too many are used.", false));
	}
	
	
	
	
	
	static public FunctionalityContent create_EvenTeleNode()
	{
		return(new FunctionalityContent("EvenTeleNode"));
	}
	static public VisualizableProgramElement visualize_EvenTeleNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Telegram Bot", "Events related to a Telegram Bot.", false));
	}
	
	
	
	static public FunctionalityContent create_EvenWindNode()
	{
		return(new FunctionalityContent("EvenWindNode"));
	}
	static public VisualizableProgramElement visualize_EvenWindNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Graphics Window", "Events related to graphical (JavaFX) windows created by the corresponding actions.", false));
	}
	
	
	static public FunctionalityContent create_EvenNetNode()
	{
		return(new FunctionalityContent("EvenNetNode"));
	}
	static public VisualizableProgramElement visualize_EvenNetNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Connectivity", "Provides connectivity events based on (W)lan networks and UART.\nSee the corresponding action-category.", false));
	}
	
	
	
	
	
	static public FunctionalityContent create_EvenMiscNode()
	{
		return(new FunctionalityContent("EvenMiscNode"));
	}
	static public VisualizableProgramElement visualize_EvenMiscNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Miscellaneous", "Various more events not belonging into other categories.", false));
	}

	
	
	
	static public FunctionalityContent create_EvenCustomNode()
	{
		return(new FunctionalityContent("EvenCustomNode"));
	}
	static public VisualizableProgramElement visualize_EvenCustomNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Custom with Values", "Events with defined values.\nDrag here from the queue to save for re-use.\nKeep <CTRL> pressed when dragging to delete from here!", true));
	}
	
	
	/////////
	
	
	
		



	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	


	
	
	
	
	
	
	
}
