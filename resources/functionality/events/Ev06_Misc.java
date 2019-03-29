package functionality.events;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import main.functionality.Functionality;
import main.functionality.helperControlers.NativeProgramHelper;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.LocationPreparator;

public class Ev06_Misc extends Functionality {

	public static int POSITION = 6;
	public static String NAME = "Miscellaneous";
	public static String IDENTIFIER = "EvenMiscNode";
	public static String DESCRIPTION = "Various more events not belonging into other categories.";
	
	
	// Regulators
	
	public static ProgramElement create_EvRegulatorApply()
	{
		Object[] params = new Object[1];
		
		return(new ProgramEventContent( "EvRegulatorApply",
				params,
				() -> {
					}
				,
				() -> {
						if (params[0] == null)
							return(false);
						
						// TODO
						
						return(true);
					}));
	}
	public static ProgramElement visualize_EvRegulatorApply(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Apply Regulator", "Allows to apply a regulator output.\nThe 'Output' variable will contain the value to forward to the hardware.\nIn the example, this could be the power of the fan or the heater.\nThe event will execute whenever needed, unless the optional argument enforces to wait.\nThe required output will always be computed afetr the delay.");
		ev.addParameter(0, new VariableOnly(), "Reg. Identifier", "Identifier variable for this regulator (created by the 'Create Regulator' action).");
		
		// TODO: Perhaps create two events: "Regulate UP" and "Regulate DOWN".
		
		return(ev);
	}
	

	
	public static ProgramElement create_EvClapDetector()
	{
		Object[] params = new Object[4];
		
		return(new ProgramEventContent("EvClapDetector",
				params,
				() -> {
					}
				,
				() -> {
						if (params[0] == null)
							return(false);
						
						// TODO
						
						return(true);
					}));
	}
	public static ProgramElement visualize_EvClapDetector(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Clap Detected", "This event uses an attached sound input (for example USB microphone) to detect a clapping or any short, loud sound.\nThe main principle is that it analyses sound intensity and picks all peaks above a threshold ('sensivity').\nIt counts the number of peaks in a given time ('period')\nand if it is between two given thresholds (min and 'max count'), it triggers the event.");
		ev.addParameter(0, new ValueOrVariable("40"), "Sensivity", "Sensivity for peaks (between 0 and 1 or 0 and 100).");
		ev.addParameter(1, new ValueOrVariable("8"), "Period", "Period of milliseconds to search for peaks. Small values of 4-16 seem to work best.");
		ev.addParameter(2, new ValueOrVariable("4"), "Min Count", "Minimum peaks to find in the period.");
		ev.addParameter(3, new ValueOrVariable("30"), "Max Count", "Maximum peaks to find in the period (more peaks mean a constant noise).");
		ev.addOptionalParameter(4, new ValueOrVariable("1"), "Number of claps", "Number of claps to detect for a trigger.");
		ev.addOptionalParameter(5, new ValueOrVariable("700"), "Clap delay", "Max number of milliseconds between two claps (if waiting for more than one)");
		
		return(ev);
	}

	public static ProgramElement create_EvRhythmChanged()
	{
		Object[] params = new Object[1];
		
		return(new ProgramEventContent("EvRhythmChanged",
				params,
				() -> {
					}
				,
				() -> {
						if (params[0] == null)
							return(false);
						
						// TODO
						
						return(true);
					}));
	}
	public static ProgramElement visualize_EvRhythmChanged(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Clap Detected", "This event uses an attached sound input (for example USB microphone) to detect a clapping or any short, loud sound.\nThe main principle is that it analyses sound intensity and picks all peaks above a threshold ('sensivity').\nIt counts the number of peaks in a given time ('period')\nand if it is between two given thresholds (min and 'max count'), it triggers the event.");
		ev.addParameter(0, new ValueOrVariable("40"), "Sensivity", "Sensivity for peaks (between 0 and 1 or 0 and 100).");
		
		return(ev);
	}

	public static ProgramElement create_EvOnSoundRhythm()
	{
		Object[] params = new Object[1];
		
		return(new ProgramEventContent("EvRhythmChanged",
				params,
				() -> {
					}
				,
				() -> {
						if (params[0] == null)
							return(false);
						
						// TODO
						
						return(true);
					}));
	}
	public static ProgramElement visualize_EvOnSoundRhythm(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Clap Detected", "This event uses an attached sound input (for example USB microphone) to detect a clapping or any short, loud sound.\nThe main principle is that it analyses sound intensity and picks all peaks above a threshold ('sensivity').\nIt counts the number of peaks in a given time ('period')\nand if it is between two given thresholds (min and 'max count'), it triggers the event.");
		ev.addParameter(0, new ValueOrVariable("40"), "Sensivity", "Sensivity for peaks (between 0 and 1 or 0 and 100).");
		
		return(ev);
	}
	
	
	
	
	
	/*
	public static ProgrammElement create_EvOneWireValueChanged()
	{
		Object[] params = new Object[3];
		ProgramContent[] content = new ProgramContent[1];
		
		String[] lastValue = new String[1];
		lastValue[0] = null;
		
		return(content[0] = new ProgramEventContent( "EvOneWireValueChanged",
				params,
				() -> {
					
					String valueText;
					
					int delay;
					if (content[0].hasOptionalArgument(0))
						delay = getIntegerVariable(content[0].getOptionalArgumentValue(0));
					else
						delay = 1000;

					
					try
					{
						valueText = OneWctrl.getDeviceValueById(getStringVariable( params[1] ));
					}
					catch (IOException e)
					{
						Execution.setError("The ID to an 1 Wire device is not valid!\nThe sensor might not be connected.\nGiven ID: " + (String) params[1] + "\n\n" + e.getMessage(), false);
						return(false);
					}
					
					if (content[0].hasOptionalArgument(1))
						if (valueText.equalsIgnoreCase(getStringVariable( content[0].getOptionalArgumentValue(1) )))
						{
							OtherHelpers.sleepNonException(delay);
							return(false);
						}
					
					if (!valueText.equals(lastValue[0]))
					{
						
						OneWctrl.interpretAndSet((String) params[2], valueText, params[0]);
						
						lastValue[0] = valueText;
						
						return(true);
					}
					
					OtherHelpers.sleepNonException(delay);
					
					return(false);
					
					}));
	}
	public static ProgrammElement visualize_EvOneWireValueChanged(ProgramContent content)
	{
		VisualizableProgrammElement elSyncBarrier;
		elSyncBarrier = new VisualizableProgrammElement(content, "Read 1Wire Val", "Execute if the value read from a device connected through the '1-Wire-Protocol' has changed.\nMost prominent example of such sensors is the 'DS18B20' meassuring temperature.");
		elSyncBarrier.setArgumentDescription(0, new VariableOnly(true, true), "Target", "Variable where the result will be placed in.\nDepending on the 'Interpretation' it will be either a number or text.");
		elSyncBarrier.setArgumentDescription(1, new TextOrVariable(), "Device ID", "The Address of the data-source.\nLook into 'Tools' for analizing the available data-sources.");
		elSyncBarrier.setArgumentDescription(2, new TextOrVariable("DS18B20"), "Interpretation", "The mode how it will interpret the value (technically a text).\nUse one of the following keywords: 'DS18B20' -> Temperature sensor. Result will be a number.");
		elSyncBarrier.setOptionalArgument(0, new ValueOrVariable("1000"), "Delay", "Because the state of a '1-Wire-Device' is not meant to change extremly frequently,\n minimum wait time in milliseconds between two checks can be defined here.\nDefault delay is 1000ms.");
		elSyncBarrier.setOptionalArgument(1, new TextOrVariable(), "Not This", "Sometimes the '1-Wire-Protocol' fails to transmit a value on first try even if it is still figuring as connected.\nThe reason is not always clear or avoidable.\nTherefore many sensors provide a default value which is returned instead.\nIn case of the 'DS18B20' it is '80.0'.\nIf you provide such a default value here as the 'Not This' parameter,\nthe program will ignore this value and not trigger an event.");
		
		return(elSyncBarrier);
	}
	*/
	
	
	
	
	
	
	static Map<Integer, NativeProgramHelper> radioSniffers = new HashMap<>();
	
	
	public static ProgramElement create_EvSniffLowMhzModule()
	{
		Object[] params = new Object[2];
		ProgramEventContent[] content = new ProgramEventContent[1];
		
		
		
		return(content[0] = new ProgramEventContent( "EvSniffLowMhzModule",
				params,
				() -> {
					
					int pin = (int) (double) params[1];
					NativeProgramHelper prog = radioSniffers.getOrDefault((Integer) pin, null);
					
					if (prog == null)
					{
						radioSniffers.put(pin, new NativeProgramHelper(LocationPreparator.optResDir() + "RPIcpp" + File.separator + "RFSniffer", String.valueOf( getPinForJava( pin ))));
						return(false);
					}
					
					Double newValue = prog.getNewNumberResponse();
					
					if (newValue != null)
					{
						initVariableAndSet(params[1], Variable.doubleType, newValue);
						return(true);
					}
					
					return(false);
					
					}));
	}
	public static ProgramElement visualize_EvSniffLowMhzModule(FunctionalityContent content)
	{
		VisualizableProgramElement EvIfTexVar;
		EvIfTexVar = new VisualizableProgramElement(content, "Radio Sniffer", "This event executes when a message received by a radio transmitter is received.\nMost common type of receiver is the 433Mhz receivers which work together\nwith many cheap, wireless power sockets.\nNote that the sensivity of many of thsoe modules compatible with raspberry are limited to <1ft!\nHowever the senders are powerful,\nthus you can use this sniffing event to detect the codes of your remote,\nsave the value and thus reuse it later to mimic the remote control.");
		EvIfTexVar.addParameter(0, new ValueOrVariable(), "Data Pin", "GPIO pin where the receivers data line is connected to." + gpioPinReferenceText);
		EvIfTexVar.addParameter(1, new VariableOnly(true, true), "Target", "Variable to place the resulting value in.");
		return(EvIfTexVar);
	}
	
	public static ProgramElement create_EvGetLowMhzModule()
	{
		Object[] params = new Object[2];
		ProgramEventContent[] content = new ProgramEventContent[1];
		
		
		return(content[0] = new ProgramEventContent( "EvGetLowMhzModule",
				params,
				() -> {}
				,
				() -> {
					
					int pin = (int) (double) params[1];
					NativeProgramHelper prog = radioSniffers.getOrDefault((Integer) pin, null);
					
					if (prog == null)
					{
						radioSniffers.put(pin, new NativeProgramHelper("RPIcpp" + File.separator + "RFSniffer", String.valueOf( getPinForJava( pin ))));
						return(false);
					}
					
					Double newValue = prog.getNewNumberResponse();
					
					if (newValue != null)
						return(newValue == (Double) params[1]);
					
					return(false);
					
					}));
	}
	public static ProgramElement visualize_EvGetLowMhzModule(FunctionalityContent content)
	{
		VisualizableProgramElement EvIfTexVar;
		EvIfTexVar = new VisualizableProgramElement(content, "Radio Receive", "This event executes when a certain message has been received\nby a radio transmitter is received.\nMost common type of receiver is the 433Mhz receivers which work together\nwith many cheap, wireless power sockets.\nNote that the sensivity of many of thsoe modules compatible with raspberry are limited to <1ft!\nHowever the senders are powerful,\nthus you can use this sniffing event to detect the codes of your remote,\nsave the value and thus reuse it later to mimic the remote control.");
		EvIfTexVar.addParameter(0, new ValueOrVariable(), "Data Pin", "GPIO pin where the receivers data line is connected to." + gpioPinReferenceText);
		EvIfTexVar.addParameter(1, new ValueOrVariable(), "Desired", "Numeric value to compare to.");
		return(EvIfTexVar);
	}
	
	
	
	
}


