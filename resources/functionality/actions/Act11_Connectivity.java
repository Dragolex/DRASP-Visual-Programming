package functionality.actions;

import java.io.IOException;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TextOrValueOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.exceptions.AccessUnsetVariableException;
import dataTypes.exceptions.NonExistingPinException;
import dataTypes.specialContentValues.DataList;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.helperControlers.hardware.LowMhzTransmitter;
import main.functionality.helperControlers.network.UartDevice;
import main.functionality.helperControlers.network.TCP.TCPconnection;
import main.functionality.helperControlers.spline.DataSpline;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Act11_Connectivity extends Functionality {

	public static int POSITION = 11;
	public static String NAME = "LAN/UART/Radio Connectivity";
	public static String IDENTIFIER = "ActConnectivityNode";
	public static String DESCRIPTION = "Actions to connect with other microcontrolers or computers via various certain protocols.";
	
	
	public static ProgramElement create_ElDefineDeviceName()
	{
		Object[] params = new Object[2];
		
		return(new FunctionalityContent( "ElDefineDeviceName",
				params,
				() -> {
						CONctrl.setDeviceIdentifier((String) params[0]);
						
						if ((boolean) params[1])
						{
							new Thread(() -> {
								try {
									TCPconnection.openForConnection();
									
									// UARTconnection.openForConnection(); TODO open for uart as well
	
								} catch (IOException e)
								{
									Execution.setError("Error at opening for new connections.\nError-Message: " + e.getMessage(), true);
								}
							}).start();
							
						}
						
					}));
	}
	public static ProgramElement visualize_ElDefineDeviceName(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Set Device Name", "All Networking with Drasp relies on a device name for identification.\nThat is similar to web addresses.\nCall this function prior any other networking functions.\nNote that principially multiple devices may have the same name\nbut you won't be able to distinguish between them in that case.");
		vis.setArgumentDescription(0, new TextOrVariable("My Device"), "Name");
		vis.addParameter(1, new BooleanOrVariable(), "Connectable", "If true, other devices can connect to this one.\nIf false, you can only use connections this program opens itself.");
		
		return(vis);
	}
	
	

	
	
	public static ProgramElement create_ElConnectNetworkDevice()
	{
		Object[] params = new Object[3];
		
		FunctionalityContent[] content = new FunctionalityContent[1];
		
		return(content[0] = new FunctionalityContent( "ElConnectNetworkDevice",
				params,
				() -> {						
						try {							
							if (content[0].hasOptionalArgument(0))
							{
								String from = TCPconnection.establishConnection((String) params[0], (String) params[1], true);
								if (content[0].hasOptionalArgument(1))
									initVariableAndSet(content[0].getOptionalArgumentValue(0) , Variable.textType, from);
							}
							else
							if (content[0].getOptionalArgTrue(1))
								TCPconnection.establishConnection((String) params[0], null, true);
							else
								TCPconnection.establishConnection((String) params[0], CONctrl.identQstr, true);
						
						} catch (IOException e)
						{
							if((boolean) params[2])
								Execution.setError("Error at establishing a connection.\nError: " + e.getMessage(), false);
						}
						
					}));
	}
	public static ProgramElement visualize_ElConnectNetworkDevice(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Connect Network", "Connects to a specific device in the local network.\nCommunication works based on a device name similar to a website address.");
		vis.addParameter(0, new TextOrVariable("127.0.0.1"), "Address", "Address to connect to.");
		vis.addParameter(1, new TextOrVariable(CONctrl.identQstr), "Target Filter", "A name identifier of the target device to ensure that\nthat only the device running a certain Drasp program will be connected.\nUse '?' to allow any device.\nIn that case you can retrieve the device name with the second optional parameter.");
		vis.addParameter(2, new BooleanOrVariable("True"), "Throw Error", "If true, a program error will be thrown if the connection failed.\nIf you put 'false', you should verify whether the connection has been established\nwith the corresponding 'Connection Exists' Structure.");
		vis.addOptionalParameter(0, new VariableOnly(true, true), "Name Variable", "If you set a filter with '?' for the 'Target name',\nthis variable will contain the name of the actually connected device.");
		vis.addOptionalParameter(1, new BooleanOrVariable("False"), "No Verify", "If using a target device filter it will be sent\ntogether with the message so the receiver can verify that it was meant for it.\nIn the case that the receiver is not a Drasp program (like some UART microcontroler or some other network device),\nyou can put this parameter to true and thus send only the message.");
		return(vis);
	}
	
	
	public static ProgramElement create_ElConnectUARTdevice()
	{
		Object[] params = new Object[1];
		
		return(new FunctionalityContent( "ElConnectUARTdevice",
				params,
				() -> {
						// TODO!!
						initVariableAndSet(params[0], Variable.UARTDevice, new UartDevice());
					}));
	}
	public static ProgramElement visualize_ElConnectUARTdevice(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Connect UART", "Enables the program to use an UART connection.\nThat is a bidirectional protocol enabling you to connect\nthe raspberry with microcontrolers or other raspberries.\nNote that using the identifier, you can even attach multiple devices.");
		vis.addParameter(0, new VariableOnly(true, true), "UART Device", "Device that will be created.\nUse this variable in the other elements related to UART.");
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElSendDeviceMessage()
	{
		Object[] params = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		
		return(content[0] = new FunctionalityContent( "ElSendDeviceMessage",
				params,
				() -> {
						try {
							
							if (content[0].hasOptionalArgument(0))
								CONctrl.send((String) params[0], (String) params[1]);
							else
								CONctrl.send((String) params[0], null);

						} catch (IOException e)
						{
							Execution.setError("Error at sending a message.\nError: " + e.getMessage(), false);
						}
						
						/*
						if (content[0].hasOptionalArgument(1))
							((UartDevice) params[0]).sendText((String) params[1], (String) content[0].getOptionalArgumentValue(1), content[0].getOptionalArgTrue(0));
						else
							((UartDevice) params[0]).sendText((String) params[1], content[0].getOptionalArgTrue(0));
							*/
					}));
	}
	public static ProgramElement visualize_ElSendDeviceMessage(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Direct Message", "Sends a value via a connection-device.\nDevices can be created with 'Connect Network' and 'Connect UART'.\nNote that normally the text will be sent to all connected devices.\nTo pic particular ones, chose a 'Target' name.");
		vis.addParameter(0, new TextOrVariable(), "Text", "Simple text to send.");
		vis.addParameter(1, new TextOrVariable(CONctrl.identQstr), "Target Filter", "A name identifier filter of the target device.\nThis allows to pick certain targets to send to.\nMulti-Selections are possible as well using the '?' symbol.\nExample:\nIf the following devices are accessible: 'ServiceAlpha', 'Hoster', 'ServiceBeta'\nThen using the device name 'Service?'\nwill only send to the two services but not to 'Hoster'.");
		vis.addOptionalParameter(0, new BooleanOrVariable("False"), "No Verify", "If using a target device name it will be sent\ntogether with the message so the receiver can verify that it was meant for it.\nIn the case that the receiver is not a Drasp program (like some UART microcontroler or some other network device),\nyou can put this parameter to true and thus send only the message.");
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElCallRemoteEvent()
	{
		Object[] params = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		
		return(content[0] = new FunctionalityContent( "ElCallRemoteEvent",
				params,
				() -> {
					
					StringBuilder argumentedString = new StringBuilder(CONctrl.evPrefix);
					argumentedString.append(CONctrl.argSep);
					argumentedString.append((String) params[0]);
					
					if (content[0].hasOptionalArgument(1))
					{
						int args = content[0].getTotalOptionalOrExpandedArgumentsCount();
						for(int i = 1; i < args; i++)
						{
							Object val = content[0].getTotalOptionalOrExpandedArgumentsArray()[i];
							Variable var = new Variable();
							
							if (val instanceof Double)
								var.initTypeAndSet(Variable.doubleType, (Double) val);
							if (val instanceof String)
								var.initTypeAndSet(Variable.textType, (String) val);
							if (val instanceof Boolean)
								var.initTypeAndSet(Variable.boolType, (Boolean) val);
							if (val instanceof DataList)
								var.initTypeAndSet(Variable.dataListType, (DataList) val);								
							if (val instanceof DataSpline)
								var.initTypeAndSet(Variable.splineType, (DataSpline) val);	
							
							
							argumentedString.append(CONctrl.argSep);
							argumentedString.append(var.getType());
							argumentedString.append(CONctrl.argSep);
							try {
								argumentedString.append(var.toPersistableString());
							} catch (AccessUnsetVariableException e) {
							Execution.setError("A parameter type is not supported! Parameter #" + (i-1)+".", false);
								return;
							}
						}
					}
					
					try {
									
						if (content[0].hasOptionalArgument(0))
							CONctrl.send(argumentedString.toString(), (String) content[0].getOptionalArgumentValue(0));
						else
							CONctrl.send(argumentedString.toString(), null);						
						
					} catch (IOException e)
					{
						Execution.setError("Error at sending a message.\nError: " + e.getMessage(), false);
					}
					
					
					}));
	}
	public static ProgramElement visualize_ElCallRemoteEvent(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Call Remote Event", "Sends a special message to a connected device to perform a given event.\nDevices can be asigned with 'Connect UART' and 'Connect Network'.\nNote that normally the text will be sent to all connected devices.\nTo pic particular ones, chose a 'Target' name.");
		vis.addParameter(0, new TextOrVariable(), "Event Name", "Name of the event to call.\nSimilarly to the optional 'Device Name' you can use the ? symbol to call multiple events at once!");
		vis.addOptionalParameter(0, new TextOrVariable(CONctrl.identQstr), "Target Filter", "A name identifier filter for the target device.\nThis allows to pick certain targets to send to.\nMulti-Selections are possible as well using the '?' symbol.\nExample:\nIf the following devices are accessible: 'ServiceAlpha', 'Hoster', 'ServiceBeta'\nThen using the device name 'Service?'\nwill only send to the two services but not to 'Hoster'.\nUse just '?' to target all.");
		vis.setExpandableArgumentDescription(TextOrValueOrVariable.class, null, true, false, "Arg #", 16, "Optional values can be expanded and their content will be transmitted to the target event\nEnsure to use the same number of 'arguments' as demanded by the target event!\nNote that only text, numbers, booelan, splines and lists\nconsisting of those types are allowed currently supported.");
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElCloseCon()
	{
		Object[] params = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		
		return(content[0] = new FunctionalityContent( "ElCloseCon",
				params,
				() -> {
						try {
						
							CONctrl.closeConnection((String) params[0], false);

						} catch (IOException e)
						{
							Execution.setError("Error when closing a connection.\nError-Message: " + e.getMessage(), false);
						}
					}));
	}
	public static ProgramElement visualize_ElCloseCon(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Close Connection", "Closes connections to all devices matching a given filter.\nNote that all connections will be closed anyways when the program terminates.\nBy the way, to stop accepting external connections, just close with the own device' identifier.");
		vis.addParameter(0, new TextOrVariable(CONctrl.identQstr), "Target Filter", "The filter identifying the target devices for closing connections.\nThis allows to pick certain targets to send to.\nMulti-Selections are possible as well using the '?' symbol.\nExample:\nIf the following devices are accessible: 'ServiceAlpha', 'Hoster', 'ServiceBeta'\nThen using the device name 'Service?'\nwill only send to the two services but not to 'Hoster'.");
		return(vis);
	}
	
	
	
	
	
	
	public static ProgramElement create_ElSendLowMhzValue()
	{
		Object[] params = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		
		return(content[0] = new FunctionalityContent( "ElSendLowMhzValue",
				params,
				() -> {
						try {
							LowMhzTransmitter.sendValue( (int) (double) params[0], (double) params[1]);
						} catch (NonExistingPinException e)
						{
							e.callException();
						}
					}));
	}
	public static ProgramElement visualize_ElSendLowMhzValue(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Radio Send", "Sends a value via a connected transmitter.\nSee the image for wiring instructions (TODO).");
		vis.addParameter(0, new ValueOrVariable(), "Data Pin", "GPIO pin where the transmitters data line is connected to." + gpioPinReferenceText);
		vis.addParameter(1, new ValueOrVariable(), "Value", "Value to send. TODO: Limit");
		return(vis);
	}
	
	
}
