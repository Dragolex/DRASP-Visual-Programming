package main.functionality;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import productionGUI.sections.elements.VisualizableProgramElement;

public abstract class Actions extends UsedFunctionalityFlags {
	
	private static int nodeTreeStructureVersion = 138; // Increase when changing the tree! It's required for loading files correctly
	
	protected static int getVersion() {return(nodeTreeStructureVersion);};
	
	
	public static void applyStandardTree(DataNode<ProgramElement> actions)
	{
		// ACTIONS
		
		/*
		DataNode<ProgrammElement> actBasicNode = attachToNodeUndelatable(actions, create_ActBasicNode());

		
		attachToNodeUndelatable(actBasicNode, create_ElDelay() );
		attachToNodeUndelatable(actBasicNode, create_ElSetIO() );
		attachToNodeUndelatable(actBasicNode, create_ElToggleIO() );
		attachToNodeUndelatable(actBasicNode, create_ElQuitProgram() );
		// ... More basic actions
		
		
		DataNode<ProgrammElement> actIO = attachToNodeUndelatable(actions, create_ActInputOutput());
		
		attachToNodeUndelatable(actIO, create_ElGetInputTx() );
		attachToNodeUndelatable(actIO, create_ElGetInputNum() );

		attachToNodeUndelatable(actIO, create_ElWriteMessage() );
		attachToNodeUndelatable(actIO, create_ElWriteVariable() );
		attachToNodeUndelatable(actIO, create_ElShowMessage() );
		attachToNodeUndelatable(actIO, create_ElShowVariable() );
		
		attachToNodeUndelatable(actIO, create_ElGetFilePath() );
		attachToNodeUndelatable(actIO, create_ElGetColor() );
		
		
	
		DataNode<ProgrammElement> actPWMnode = attachToNodeUndelatable(actions, create_ActPWMnode());

		attachToNodeUndelatable(actPWMnode, create_ElSetupPinPwm());
		attachToNodeUndelatable(actPWMnode, create_ElPrepPwm());
		attachToNodeUndelatable(actPWMnode, create_ElPwmSet());
		attachToNodeUndelatable(actPWMnode, create_ElPwmSetExt());
		attachToNodeUndelatable(actPWMnode, create_ElPwmSetPeriodA());
		attachToNodeUndelatable(actPWMnode, create_ElPwmSetPeriodAB());
		attachToNodeUndelatable(actPWMnode, create_ElPwmSetBySpline());
		// ... More PWM actions

		
		DataNode<ProgrammElement> actLightsNode = attachToNodeUndelatable(actions, create_ActLightEffectsNode());
		
		//attachToNodeUndelatable(actLightsNode, create_ElsetupWSstripe());
		// ... More PWM actions
		

		
		DataNode<ProgrammElement> actShellNetworkNode = attachToNodeUndelatable(actions, create_ActShellNetworkNode());
		attachToNodeUndelatable(actShellNetworkNode, create_ElExecCommand());
		attachToNodeUndelatable(actShellNetworkNode, create_ElExternalDevice());
		attachToNodeUndelatable(actShellNetworkNode, create_ElExecRemoteEvent());
		attachToNodeUndelatable(actShellNetworkNode, create_ElDownloadUrl());
		attachToNodeUndelatable(actShellNetworkNode, create_ElDownloadFTP());
		attachToNodeUndelatable(actShellNetworkNode, create_ElUploadFTP());
		

		

		DataNode<ProgrammElement> actWindowNode = attachToNodeUndelatable(actions, create_ActWindowNode());
		
		attachToNodeUndelatable(actWindowNode, create_ElWindCreate());
		attachToNodeUndelatable(actWindowNode, create_ElWindClose());
		attachToNodeUndelatable(actWindowNode, create_ElWindBckgr());
		attachToNodeUndelatable(actWindowNode, create_ElWindSprite());
		attachToNodeUndelatable(actWindowNode, create_ElWindShape());
		attachToNodeUndelatable(actWindowNode, create_ElWindText());
		attachToNodeUndelatable(actWindowNode, create_ElPlaceltem());
		attachToNodeUndelatable(actWindowNode, create_ElWindItemFade());
		attachToNodeUndelatable(actWindowNode, create_ElWindItemRotate());
		attachToNodeUndelatable(actWindowNode, create_ElWindItemScale());
		attachToNodeUndelatable(actWindowNode, create_ElWindSwapItem());
		attachToNodeUndelatable(actWindowNode, create_ElBtnItem());
		attachToNodeUndelatable(actWindowNode, create_ElWindItemEffect());
		attachToNodeUndelatable(actWindowNode, create_ElWindApplItemEffect());
		attachToNodeUndelatable(actWindowNode, create_ElWindApplItemCSS());
		
		attachToNodeUndelatable(actWindowNode, create_ElNewGraph());
		attachToNodeUndelatable(actWindowNode, create_ElCoordAxis());
		
		attachToNodeUndelatable(actWindowNode, create_ElExtSprite());
		
		// ... more Window actions
		
		
		
		DataNode<ProgrammElement> actAnalogNode = attachToNodeUndelatable(actions, create_ActAnalogNode());
		attachToNodeUndelatable(actAnalogNode, create_ElScanI2C());
		attachToNodeUndelatable(actAnalogNode, create_ElScanOneWire());
		
		attachToNodeUndelatable(actAnalogNode, create_ElCreateAnalogADSDevice());
		attachToNodeUndelatable(actAnalogNode, create_ElCreateAnalogMCPDevice());
		attachToNodeUndelatable(actAnalogNode, create_ElCreateBMP280Device());
		attachToNodeUndelatable(actAnalogNode, create_ElCreateLSM303DDevice());
		attachToNodeUndelatable(actAnalogNode, create_ElCreateBH1750Device());
		attachToNodeUndelatable(actAnalogNode, create_ElCreateINA219Device());
		attachToNodeUndelatable(actAnalogNode, create_ElCreateDS18B20Device());
		
		attachToNodeUndelatable(actAnalogNode, create_ElAdjustAnalogDevice());
		attachToNodeUndelatable(actAnalogNode, create_ElCreateRotaryDevice());
		attachToNodeUndelatable(actAnalogNode, create_ElGetAnalogInput());
		attachToNodeUndelatable(actAnalogNode, create_ElGetAnalogAsJoystick());
		
		// ... more

		
		
		DataNode<ProgrammElement> actStepperNode = attachToNodeUndelatable(actions, create_ActStepperNode());
		attachToNodeUndelatable(actStepperNode, create_ElCreateStepper());
		attachToNodeUndelatable(actStepperNode, create_ElMoveStepper());
		attachToNodeUndelatable(actStepperNode, create_ElMoveStepperContin());
		attachToNodeUndelatable(actStepperNode, create_ElMoveStepperBySpline());

		
		
		
		DataNode<ProgrammElement> actDispNode = attachToNodeUndelatable(actions, create_ActDisplayNode());
		
		attachToNodeUndelatable(actDispNode, create_ElI2CDisplayCreate());
		attachToNodeUndelatable(actDispNode, create_ElDispTextCreate());
		attachToNodeUndelatable(actDispNode, create_ElDispLineCreate());
		attachToNodeUndelatable(actDispNode, create_ElDispShapeCreate());

		
		
		
		DataNode<ProgrammElement> actTeleNode = attachToNodeUndelatable(actions, create_ActTeleNode());
		
		attachToNodeUndelatable(actTeleNode, create_ElTeleBot());
		attachToNodeUndelatable(actTeleNode, create_ElTeleGetName());
		attachToNodeUndelatable(actTeleNode, create_ElTeleGetCont());
		attachToNodeUndelatable(actTeleNode, create_ElTeleSend());
		// ... More Telegram actions
		
		
		DataNode<ProgrammElement> actCameraNode = attachToNodeUndelatable(actions, create_ActCameraNode());

		attachToNodeUndelatable(actCameraNode, create_ElOpenCameraStream());
		attachToNodeUndelatable(actCameraNode, create_ElOpenVideoStream());
		attachToNodeUndelatable(actCameraNode, create_ElOpenIPStream());
		attachToNodeUndelatable(actCameraNode, create_ElStreamToFile());
		attachToNodeUndelatable(actCameraNode, create_ElCloseStream());
		
		

		

		DataNode<ProgrammElement> actConnectivNode = attachToNodeUndelatable(actions, create_ActConnectivityNode());
		attachToNodeUndelatable(actConnectivNode, create_ElDefineDeviceName());
		attachToNodeUndelatable(actConnectivNode, create_ElConnectNetworkDevice());
		attachToNodeUndelatable(actConnectivNode, create_ElConnectUARTdevice());
		attachToNodeUndelatable(actConnectivNode, create_ElSendDeviceMessage());
		attachToNodeUndelatable(actConnectivNode, create_ElCallRemoteEvent());
		attachToNodeUndelatable(actConnectivNode, create_ElCloseCon());
		
		attachToNodeUndelatable(actConnectivNode, create_ElSendLowMhzValue());
		
		// ... more

		
		DataNode<ProgrammElement> actFilesNode = attachToNodeUndelatable(actions, create_ActFilesNode());
		attachToNodeUndelatable(actFilesNode, create_ElCopyFile());
		attachToNodeUndelatable(actFilesNode, create_ElCreateFile());
		attachToNodeUndelatable(actFilesNode, create_ElDeleteFile());
		attachToNodeUndelatable(actFilesNode, create_ElFileLoadDirContents());
		attachToNodeUndelatable(actFilesNode, create_ElFileLoadDirs());
		attachToNodeUndelatable(actFilesNode, create_ElSaveVar());
		attachToNodeUndelatable(actFilesNode, create_ElLoadVar());
		attachToNodeUndelatable(actFilesNode, create_ElReadFile());
		attachToNodeUndelatable(actFilesNode, create_ElWriteFile());

		
		// ... more

		
		
		DataNode<ProgrammElement> actRegulationNode = attachToNodeUndelatable(actions, create_ActRegulationNode());
		attachToNodeUndelatable(actRegulationNode, create_ElCreateRegulator());
		attachToNodeUndelatable(actRegulationNode, create_ElChangeTargetValue());		
		
		
		DataNode<ProgrammElement> actSoundNode = attachToNodeUndelatable(actions, create_ActSoundNode());
		attachToNodeUndelatable(actSoundNode, create_ElLoadSound());
		attachToNodeUndelatable(actSoundNode, create_ElPlaySound());
		attachToNodeUndelatable(actSoundNode, create_ElSetVolume());
		attachToNodeUndelatable(actSoundNode, create_ElSetPosition());
		attachToNodeUndelatable(actSoundNode, create_ElStopSound());
		attachToNodeUndelatable(actSoundNode, create_ElStartRecording());
		attachToNodeUndelatable(actSoundNode, create_ElStopRecording());
		
		
		// TODO: Move some of this to structures. Perhaps into "basic".
		DataNode<ProgrammElement> actMiscNode = attachToNodeUndelatable(actions, create_ActMiscNode());
		attachToNodeUndelatable(actMiscNode, create_ElSyncBarrierSet());
		attachToNodeUndelatable(actMiscNode, create_ElSyncBarrier());
		attachToNodeUndelatable(actMiscNode, create_ElCurRuntime());
		attachToNodeUndelatable(actMiscNode, create_ElRandomizer());
		attachToNodeUndelatable(actMiscNode, create_ElSetSeed());
		attachToNodeUndelatable(actMiscNode, create_ElMpr121());
		attachToNodeUndelatable(actMiscNode, create_ElPCF8574());
		*/
		
		
		// ... more

		
		
		//DataNode<ProgrammElement> actCustomNode = 
				attachToNodeUndelatable(actions, create_ActCustomNode());
		//attachToNode(actCustomNode, create_Booltest());
		

		//////////
	}
	
	
	

	// ACTIONS root nodes
	
	
	static public FunctionalityContent create_ActBasicNode()
	{
		return(new FunctionalityContent("ActBasicNode"));
	}
	static public VisualizableProgramElement visualize_ActBasicNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Basic", "Actions for basic functionalities.\nDrag into the queue and provide the desired parameters.", false));
	}
	
	static public FunctionalityContent create_ActInputOutput()
	{
		return(new FunctionalityContent("ActInputOutput"));
	}
	static public VisualizableProgramElement visualize_ActInputOutput(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Input/Output Text", "Actions for simple console-printing or popup-messages.\nDrag into the queue and provide the desired parameters.", false));
	}	
	
	
	
	/*
	static public FunctionalityContent create_ActInputnode()
	{
		return(new FunctionalityContent("ActInputnode"));
	}
	static public VisualizableProgrammElement visualize_ActInputnode(FunctionalityContent content)
	{
		return(new VisualizableProgrammElement(content, "Hardware Input", "Actions related to hardware input like keyboards or GPIO.\nDrag into the queue and provide the desired parameters.", false));
	}
	*/
	
	
	static public FunctionalityContent create_ActPWMnode()
	{
		return(new FunctionalityContent("ActPWMnode"));
	}
	static public VisualizableProgramElement visualize_ActPWMnode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "PWM Output", "Actions related to a PWM Board.\nDrag into the queue and provide the desired parameters.", false).setRequiringRaspberry());
	}
	
	static public FunctionalityContent create_ActStepperNode()
	{
		return(new FunctionalityContent("ActStepperNode"));
	}
	static public VisualizableProgramElement visualize_ActStepperNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Stepper Motor", "Actions related to a stepper motor controled via 4 GPIOs each.\nDrag into the queue and provide the desired parameters.", false).setRequiringRaspberry());
	}
	

	static public FunctionalityContent create_ActTeleNode()
	{
		return(new FunctionalityContent("ActTeleNode"));
	}
	static public VisualizableProgramElement visualize_ActTeleNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Telegram Bot", "Actions related to telegram bots.\nDrag into the queue and provide the desired parameters.", false));
	}
	
	
	static public FunctionalityContent create_ActWindowNode()
	{
		return(new FunctionalityContent("ActWindowNode"));
	}
	static public VisualizableProgramElement visualize_ActWindowNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Graphics on Screen", "Actions enabling to show a window with background, text and sprite-images onto an attached screen.\nDrag into the queue and provide the desired parameters.", false));
	}
	
	
	
	
	
	static public FunctionalityContent create_ActLightEffectsNode()
	{
		return(new FunctionalityContent("ActLightEffectsNode"));
	}
	static public VisualizableProgramElement visualize_ActLightEffectsNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Light Effects", "Actions for light effects and using IC-LED stripes.\nDrag into the queue and provide the desired parameters.", false));
	}
	
	
	static public FunctionalityContent create_ActDisplayNode()
	{
		return(new FunctionalityContent("ActDisplayNode"));
	}
	static public VisualizableProgramElement visualize_ActDisplayNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Mini/LED Displays", "Actions enabling to draw onto attached SPI and I2C mini displays\nor onto displays made of pixels using individual LEDs.\nDrag into the queue and provide the desired parameters.", false));
	}
	


	
	
	static public FunctionalityContent create_ActCameraNode()
	{
		return(new FunctionalityContent("ActCameraNode"));
	}
	static public VisualizableProgramElement visualize_ActCameraNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Camera/Video", "Actions related to cameras and video input, including IP-Streams.", false));
	}
	
	
	
	static public FunctionalityContent create_ActFileNode()
	{
		return(new FunctionalityContent("ActFileNode"));
	}
	static public VisualizableProgramElement visualize_ActFileNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Files", "Actions related to manipulating and finding files and directories.\nDrag into the queue and provide the desired parameters.", false));
	}
	

	static public FunctionalityContent create_ActTimeNode()
	{
		return(new FunctionalityContent("ActTimeNode"));
	}
	static public VisualizableProgramElement visualize_ActTimeNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Time", "Actions related to time.\nDrag into the queue and provide the desired parameters.", false));
	}
	

	static public FunctionalityContent create_ActFilesNode()
	{
		return(new FunctionalityContent("ActFilesNode"));
	}
	static public VisualizableProgramElement visualize_ActFilesNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Files", "Functionalities involving files and directories.", false));
	}

	static public FunctionalityContent create_ActRegulationNode()
	{
		return(new FunctionalityContent("ActRegulationNode"));
	}
	static public VisualizableProgramElement visualize_ActRegulationNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Regulator", "The Regulator is a system which attempts to achieve a\n"
																	+ "desired hardware value by influencing a different value.\n"
																	+ "For example you can regulate the speed of a fan based on a temperature sensor\n"
																	+ "to maintain a constant target-temperature.", false));
	}

	
	static public FunctionalityContent create_ActSoundNode()
	{
		return(new FunctionalityContent("ActSoundNode"));
	}
	static public VisualizableProgramElement visualize_ActSoundNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Sound", "Functionalities involving playing sounds.", false));
	}
	
	
	
	static public FunctionalityContent create_ActAnalogNode()
	{
		return(new FunctionalityContent("ActAnalogNode"));
	}
	static public VisualizableProgramElement visualize_ActAnalogNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Sensor Input", "Actions to enable the input of analog values from sensors.", false).setRequiringRaspberry());
	}

	static public FunctionalityContent create_ActShellNetworkNode()
	{
		return(new FunctionalityContent("ActShellNetworkNode"));
	}
	static public VisualizableProgramElement visualize_ActShellNetworkNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Shell/FTP", "Actions to call shell commands locally or via network and access FTP servers.", false).setRequiringRaspberry());
	}
	
	
	static public FunctionalityContent create_ActConnectivityNode()
	{
		return(new FunctionalityContent("ActConnectivityNode"));
	}
	static public VisualizableProgramElement visualize_ActConnectivityNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "TCP, UART, Radio Connectivity", "Actions to connect with other microcontrolers or computers via various certain protocols.", false).setRequiringRaspberry());
	}

	
	
	static public FunctionalityContent create_ActMiscNode()
	{
		return(new FunctionalityContent("ActMiscNode"));
	}
	static public VisualizableProgramElement visualize_ActMiscNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Miscellaneous", "Other kind of actions.\nDrag into the queue and provide the desired parameters.", false));
	}
	
	
	static public FunctionalityContent create_ActCustomNode()
	{
		return(new FunctionalityContent("ActCustomNode"));
	}
	static public VisualizableProgramElement visualize_ActCustomNode(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Custom with Values", "Actions with defined values.\nDrag here from the queue to save for re-use.\nKeep <CTRL> pressed when dragging to delete from here!", true));
	}
	

	/////////
	
	
	
	
	
	
	// Normal ACTIONS
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	

	
	
	// Misc
	
	
	
	
	

	
	
	
	
	
	
}
