package functionality.actions;

import dataTypes.FunctionalityContent;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.exceptions.NonExistingPinException;
import execution.Execution;
import main.functionality.Functionality;
import productionGUI.additionalWindows.WaitPopup;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.DebugMsgHelper;

public class Act00_Basic extends Functionality {

	public static int POSITION = 0;
	public static String NAME = "Basic";
	public static String IDENTIFIER = "ActBasicNode";
	public static String DESCRIPTION = "Actions for basic functionalities.";
	
	
	
	// Simple time delay
	static public FunctionalityContent create_ElDelay()
	{
		Object[] variable = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(	content[0] = new FunctionalityContent( "ElDelay",
				variable,
				() -> {
						if (content[0].hasOptionalArgument(0)) // variant with popup
						{
							String msg = (String) content[0].getOptionalArgumentValue(0); // restore newlines
							
							long length = (long) ((double) variable[0]);
							
							new Thread(() -> {
								WaitPopup waitPopup = new WaitPopup(msg);
								waitPopup.showTimerByReplacing("§", (int) length/1000);
								Execution.checkedSleep(length);
								waitPopup.close();
							}).start();
							
							Execution.checkedSleep(length);
						}
						else
							Execution.checkedSleep((long) ((double) variable[0])); // simple variant
					}));
	}
	static public VisualizableProgramElement visualize_ElDelay(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Delay", "Delays the execution by a given number of milliseconds.");
		elDelay.setArgumentDescription(0, new ValueOrVariable(), "Period (ms)");
		elDelay.addOptionalParameter(0, new TextOrVariable("Please Wait... \n§ seconds remaining."), "Popup Msg", "If this argument is provided, \na popup screen will be shown during the waiting period.\nIf you place the '$' symbol somewhere\nit will be automatically replaced by the remaining seconds\nand also updated automatically!");
		return(elDelay);
	}
	
	
	// Set IO
	static public FunctionalityContent create_ElSetIO()
	{
		Object[] variable = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElSetIO",
				variable, /*() -> { GPIOctrl.init(); },*/
				() -> {
						try
						{
							if(DEBUG) DebugMsgHelper.associateGPIOvariable(content[0], 0);
							
							GPIOctrl.setOutputPin(getINTparam( variable[0] ), (boolean) variable[1]);
						}
						catch (NonExistingPinException e)
						{
							e.callException();
						}
					});
		return(content[0]);
	}
	static public VisualizableProgramElement visualize_ElSetIO(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Set GPIO", "Sets the GPIO state of a corresponding pin.\nThis means that between ground and the given pin, either power of 3.3V is ON ('True') or OFF ('True').\nAttention: Even if both gpio-layouts (see below) are listing many pins,\nnot all are available. An error will be shown for inaccessible pins.\nYouc an also look into the 'Tools' menu for helpful functions.");
		
		vis.addParameter(0, new ValueOrVariable(), "GPIO Pin", "Pin on the raspberry to set." + gpioPinReferenceText);
		vis.addParameter(1, new BooleanOrVariable(), "Power", "'True' for 'ON' and 'False' for 'OFF'.");
		
		return(vis.setRequiringRaspberry());
	}
	

	static public FunctionalityContent create_ElToggleIO()
	{
		Object[] variable = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElToggleIO",
				variable, /*() -> { GPIOctrl.init(); },*/
				() -> {
						try
						{
							if(DEBUG) DebugMsgHelper.associateGPIOvariable(content[0], 0);
							
							GPIOctrl.toggleOutputPin(getINTparam( variable[0] ));
						}
						catch (NonExistingPinException e)
						{
							e.callException();
						}
					});
		return(content[0]);
	}
	static public VisualizableProgramElement visualize_ElToggleIO(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Toggle GPIO", "Flips the GPIO state of a corresponding pin.\nIf it has been OFF, it will be ON and the other way around.");
		vis.addParameter(0, new ValueOrVariable(), "GPIO Pin", "Pin on the raspberry to toggle." + gpioPinReferenceText);
		return(vis.setRequiringRaspberry());
	}

	
	
	
	// Close the entire program 
	static public FunctionalityContent create_ElQuitProgram()
	{
		Object[] args = new Object[0];
		return( new FunctionalityContent( "ElQuitProgram",
				args,
				() -> {
						new Thread(() -> Execution.stop()).start();
					}));
	}
	static public VisualizableProgramElement visualize_ElQuitProgram(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Quit Program", "Note, the current event might continue after this event!\nUse the 'Exit Block' structure after this action to prevent that.\nIf the programming GUI is not in use \nand an event cannot be closed directly\nit will be force-killed."));
	}
	
	
	
}
