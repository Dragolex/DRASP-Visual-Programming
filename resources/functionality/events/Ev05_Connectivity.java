package functionality.events;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import main.functionality.Functionality;
import main.functionality.helperControlers.network.TransmissionMessage;
import main.functionality.helperControlers.network.UartDevice;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Ev05_Connectivity extends Functionality {

	public static int POSITION = 5;
	public static String NAME = "Connectivity";
	public static String IDENTIFIER = "EvenNetNode";
	public static String DESCRIPTION = "Provides connectivity events based on (W)lan networks and UART.\nSee the corresponding action-category.";
	
	

	

	public static ProgramElement create_EvNetMsgReceived()
	{
		Object[] input = new Object[2];
		Object[] params = new Object[2];
		
		ProgramEventContent[] cont = new ProgramEventContent[1];
		
		cont[0] = new ProgramEventContent("EvNetMsgReceived",
				input, params,
				() -> {
				},
				() -> {
					
					String sender = (String) input[0];
					String msg = (String) input[1];
					
					if (msg.startsWith(CONctrl.evPrefix)
							|| msg.startsWith(CONctrl.newConPrefix)
							|| msg.startsWith(CONctrl.closedPrefix)) // if not a simple message -> close
						return(false);
					
					String receiver = (String) params[1];
					
					if (sender != null)
					{
						if (!TransmissionMessage.matchIdentifiers(receiver, sender))
							return(false);
						
						if (cont[0].hasOptionalArgument(0))
							((Variable) cont[0].getOptionalArgumentValue(0)).initTypeAndSet(Variable.textType, sender);
					}
					
					((Variable) params[0]).initTypeAndSet(Variable.textType, msg);					
					
					return(true);
				});
		
		if (!currentlyLoadingIDEelements)
			CONctrl.passEventExecutor(cont[0]);
		
		return(cont[0]);
		
	}
	public static ProgramElement visualize_EvNetMsgReceived(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Receive Message", "Receive the message from a network or UART device.");
		vis.addParameter(0, new VariableOnly(true, true), "Message", "Variable which will contain the received text.");
		vis.addParameter(1, new TextOrVariable(CONctrl.identQstr), "Sender Filter", "A name identifier of the target device so the event is only executed when the message comes from this certain device.\nUse '?' to allow any device or 'PREFIX?' to receive from a selection of senders.\nYou can always retrieve the full name with the optional parameter.");
		vis.addOptionalParameter(0, new VariableOnly(true, true), "Sender Name", "Variable which will contain the full name of the sender device.\nIt will be the entire name also if\nyou use a prefix and '?' for 'Sender Filter'.");
		return(vis);
	}
	
	
	

	public static ProgramElement create_EvNetEvent()
	{
		Object[] input = new Object[2];
		Object[] params = new Object[1];
		
		ProgramEventContent[] cont = new ProgramEventContent[1];
		
		cont[0] = new ProgramEventContent("EvNetEvent",
				input, params,
				() -> {
				},
				() -> {
					
					String sender = (String) input[0];
					String msg = (String) input[1];
					
					String[] dataFragments = msg.split(CONctrl.argSep);
					
					if (!dataFragments[0].equals(CONctrl.evPrefix))
						return(false);
					
					if (!TransmissionMessage.matchIdentifiers((String) params[0], dataFragments[1])) // match the event name
						return(false);					
					
					if (cont[0].hasOptionalArgument(0))
					{
						String receiver = (String) cont[0].getOptionalArgumentValue(0);
						
						if (!TransmissionMessage.matchIdentifiers(receiver, sender))
							return(false);
						
						if (cont[0].hasOptionalArgument(1))
							((Variable) cont[0].getOptionalArgumentValue(0)).initTypeAndSet(Variable.textType, sender);
						
						
						if (cont[0].hasOptionalArgument(2))
						{							
							int len = dataFragments.length;
							int indArg = 2;
							for(int ind = 2; ind < len; ind+=2)
							{
								((Variable) cont[0].getOptionalArgumentValue(indArg)).fromPersitableString(
									Integer.valueOf(dataFragments[ind]),// type
									dataFragments[ind+1]); // content string
								indArg++;
							}
						}
							
					}
					
					return(true);
				});
		
		if (!currentlyLoadingIDEelements)
			CONctrl.passEventExecutor(cont[0]);
		
		return(cont[0]);
		
	}
	public static ProgramElement visualize_EvNetEvent(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Remote Event", "This event can be called remotely through the network.\nuse the function 'Call Remote Event'.");
		vis.addParameter(0, new TextOrVariable(), "Event Name", "Name of the event to call.\nSimilarly to the optional 'Sender Name' you can use the ? symbol to call multiple events at once!");
		vis.addOptionalParameter(0, new TextOrVariable(CONctrl.identQstr), "Sender Filter", "A name identifier of the target device so the event is only executed when the message comes from this certain device.\nUse '?' to allow any device or 'PREFIX?' to receive from a selection of senders.\\nYou can always retrieve the full name with the optional parameter.");
		vis.addOptionalParameter(1, new VariableOnly(true, true), "Sender Name", "Variable which will contain the full name of the sender device.\nIt will be the entire name also if\nyou use a prefix and '?' for 'Sender Filter'.");
		vis.setExpandableArgumentDescription(VariableOnly.class, null, true, true, "Arg #", 16, "Up to 16 variables which will contain the passed values.");
		return(vis);
	}
	
	
	
	
	
	
	public static ProgramElement create_EvUartReceived()
	{
		Object[] params = new Object[2];
		ProgramEventContent[] content = new ProgramEventContent[1];
		
		return(content[0] = new ProgramEventContent( "EvUartReceived",
				params,
				() -> {}
				,
				() -> {
					UartDevice device = (UartDevice) params[0];
					
					String res = "";
					if (content[0].hasOptionalArgument(0))
						res = device.getNewText((String) content[0].getOptionalArgumentValue(0));
					else
						res = device.getNewText();
					
					if (res != null)
					{
						initVariableAndSet(params[3], Variable.textType, res);
						return(true);
					}
					
					return(false);
				}));
	}
	public static ProgramElement visualize_EvUartReceived(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Receive UART", "Executes when a message has been received through UART.\nThe identifier is taken into account.");
		vis.addParameter(0, new VariableOnly(), "UART Device", "Device providing the UART input.");
		vis.addParameter(1, new VariableOnly(true, true), "Value Var", "Variable which will contain the received text.");
		vis.addOptionalParameter(0, new TextOrVariable(), "Identifier", "UART allows to wire many devices together at once. When something is sent, all will receive the message.\nTo target a certain device, use the 'Identifier'.\nIf a text is provided, only the other devices having events with the same identifier, will receive the message.");
		return(vis);
	}
	
	
	
	
	public static ProgramElement create_EvNewExtCon()
	{
		Object[] input = new Object[2];
		Object[] params = new Object[1];
		
		ProgramEventContent[] cont = new ProgramEventContent[1];
		
		cont[0] = new ProgramEventContent("EvNewExtCon",
				input, params,
				() -> {
				},
				() -> {
					
					String sender = (String) input[0];
					String msg = (String) input[1];
					
					
					if (!msg.equals(CONctrl.newConPrefix))
						return(false);
					
					
					String receiver = (String) params[0];
										
					if (!TransmissionMessage.matchIdentifiers(receiver, sender))
						return(false);
					
					if (cont[0].hasOptionalArgument(0))
						((Variable) cont[0].getOptionalArgumentValue(0)).initTypeAndSet(Variable.textType, sender);
					
					return(true);
				});
		
		if (!currentlyLoadingIDEelements)
			CONctrl.passEventExecutor(cont[0]);
		
		return(cont[0]);
		
	}
	public static ProgramElement visualize_EvNewExtCon(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Connection Opened", "Executes when a device has connected to this device.");
		vis.addParameter(0, new TextOrVariable(CONctrl.identQstr), "Opener Filter", "A name identifier of the target device so the event is only executed when the connection opens from this certain device.\nUse '?' to allow any device or 'PREFIX?' to receive from a selection of senders.\nYou can always retrieve the full name with the optional parameter.");
		vis.addOptionalParameter(0, new VariableOnly(true, true), "Opener Name", "Variable which will contain the full name of the closed device.\nIt will be the entire name also if\nyou use a prefix and '?' for 'Opener Filter'.");
		return(vis);
	}

	
	public static ProgramElement create_EvConClosed()
	{
		Object[] input = new Object[2];
		Object[] params = new Object[1];
		
		ProgramEventContent[] cont = new ProgramEventContent[1];
		
		cont[0] = new ProgramEventContent("EvConClosed",
				input, params,
				() -> {
				},
				() -> {
					
					String sender = (String) input[0];
					String msg = (String) input[1];
					
					if (!msg.equals(CONctrl.closedPrefix))
						return(false);
					
					String receiver = (String) params[0];
					
					if (!TransmissionMessage.matchIdentifiers(receiver, sender))
						return(false);
					
					if (cont[0].hasOptionalArgument(0))
						((Variable) cont[0].getOptionalArgumentValue(0)).initTypeAndSet(Variable.textType, sender);
					
					return(true);
				});
		
		if (!currentlyLoadingIDEelements)
			CONctrl.passEventExecutor(cont[0]);
		
		return(cont[0]);
		
	}
	public static ProgramElement visualize_EvConClosed(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Connection Closed", "Executes when a device has closed its connection.");
		vis.addParameter(0, new TextOrVariable(CONctrl.identQstr), "Closer Filter", "A name identifier of the target device so the vent is only executed when the connection closes for this certain device.\nUse '?' to allow any device or 'PREFIX?' to receive from a selection of senders.\nYou can always retrieve the full name with the optional parameter.");
		vis.addOptionalParameter(0, new VariableOnly(true, true), "Closer Name", "Full name of the closed device.\nIt will be the entire name also if\nyou use a prefix and '?' for 'Closer Filter'.");
		return(vis);
	}

	
	
	
	
}


