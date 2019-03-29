package functionality.events;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.SelectableType;
import dataTypes.contentValueRepresentations.VariableOnly;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.helperControlers.OnlyOnceHelper;
import main.functionality.helperControlers.screen.JFXeffect;
import main.functionality.helperControlers.screen.JFXitem;
import main.functionality.helperControlers.screen.JFXwindow;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Ev04_WindowGraphics extends Functionality {

	public static int POSITION = 4;
	public static String NAME = "Graphics on Screen";
	public static String IDENTIFIER = "EvenWindNode";
	public static String DESCRIPTION = "Events related to graphical windows on screen created by the corresponding actions.";
	
	
	// Window and button
	
	public static ProgramElement create_EvWindClosed()
	{
		Object[] params = new Object[1];
		
		return(new ProgramEventContent( "EvWindClosed",
				params,
				() -> {
					}
				,
				() -> {
						if (params[0] == null)
							return(false);
						
						Execution.checkedSleep(100);
						
						JFXwindow window = (JFXwindow) params[0];
						return(!window.isOpen());						
						
					}));
	}
	public static ProgramElement visualize_EvWindClosed(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Closed Window", "Launches when the given window has been closed.");
		ev.setArgumentDescription(0, new VariableOnly(), "Window Identifier");
		return(ev);
	}
	
	
	
	
	public static ProgramElement create_EvWindButtonPressed()
	{
		Object[] input = new Object[1];
		Object[] params = new Object[1];
		
		ProgramEventContent[] cont = new ProgramEventContent[1];		
		
		cont[0] = new ProgramEventContent("EvWindButtonPressed",
				input, params,
				() -> {
					buttonPressedEventContents.add(cont[0]);
				},
				() -> {
					
					return(params[0] == input[0]);
					
					/*
					if (params[0] != input[0]) // if not the right button for this event, abort
						return(false);
					
					if ((boolean) input[1]) // released
						return(cont[0].getOptionalArgTrue(0)); // trigger only when release desired
				
					return(true);
					*/
					
				});
		
		return(cont[0]);
	}
	public static ProgramElement visualize_EvWindButtonPressed(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Button Pressed", "Launches when a given button has been pressed.\nCreate buttons with the corresponding elements.");
		ev.setArgumentDescription(0, new VariableOnly(), "Button Identifier");
		//ev.setOptionalArgument(0, new BooleanOrVariable("False"), "On Release","If true, the the event will only trigger when the klick has been released.");
		return(ev);
	}
	
	
	public static ProgramElement create_EvWindItemPressed()
	{
		Object[] params = new Object[2];
		ProgramEventContent[] cont = new ProgramEventContent[1];		

		return(cont[0] = new ProgramEventContent("EvWindItemPressed",
				params,
				() -> {
					JFXitem item = (JFXitem) params[0];
					
					item.attachPressedEvent(cont[0], (boolean) params[1],
							(JFXeffect) cont[0].getOptionalArgumentValueOR(0, null),
							(JFXeffect) cont[0].getOptionalArgumentValueOR(1, null));
					
					return(item.beenPressed());
				}));
	}
	public static ProgramElement visualize_EvWindItemPressed(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Item Pressed", "Launches when a given item has been pressed.\nIt is possible to achieve graphical effects when the mouse hovers/pressed.");
		ev.setArgumentDescription(0, new VariableOnly(), "Item Identifier");
		ev.addParameter(1, new BooleanOrVariable("True"), "Button Effect","If true, the item will behave like a button and change brightness when hovering and when pressed!");
		ev.addOptionalParameter(0, new VariableOnly(), "Hover Effect", "An effect that will be applied to the button when hovering over the item.\nA default effect is used if this optional argument is not provided.");
		ev.addOptionalParameter(1, new VariableOnly(), "Pressed Effect", "An effect that will be applied to the button while the mouse is pressed onto the item.\nA default effect is used if this optional argument is not provided.");
		return(ev);
	}
	
	
	
	public static ProgramElement create_EvWindItemHover()
	{
		Object[] input = new Object[3];
		Object[] params = new Object[2];
		
		ProgramEventContent[] cont = new ProgramEventContent[1];	

		OnlyOnceHelper enabled = new OnlyOnceHelper();
		Boolean[] currently_continuous = new Boolean[1];
		currently_continuous[0] = false;
		
		cont[0] = new ProgramEventContent("EvWindItemHover",
				input, params,
				() -> {
					buttonHoverStateEventContents.add(cont[0]);
				},
				() -> {
					if (enabled.do_it())
					{
						JFXitem item = (JFXitem) params[0];
						item.enableHoveringEvents((int) params[1], buttonHoverStateEventContents);
					}
					
					if (currently_continuous[0])
						return(true);
					return(false);
				},
				() -> {
					JFXitem item = (JFXitem) params[0];
					
					if (item != input[0]) // not targeted at this event
						return(false);
					
					if (params[1] != input[1]) // not this type of event
						return(false);
					
					if ((int) input[1] >= 3) // if continuous while inside or while mouse down
						currently_continuous[0] = (boolean) input[2];
					
					return(true);
					
				});
		
		return(cont[0]);
	}
	public static ProgramElement visualize_EvWindItemHover(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Item Enter/Leave", "Launches when a cursor enters, leaves or hovers on an item.");
		ev.setArgumentDescription(0, new VariableOnly(), "Item Identifier");
		ev.setArgumentDescription(1, new SelectableType(new String[] {"Enter", "Leave", "When Moving", "Continuous while Inside", "Continuous while Mouse Pressed"}), "When the event should fire.");
		return(ev);
	}
	
	
	
}


