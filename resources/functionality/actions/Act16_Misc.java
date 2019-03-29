package functionality.actions;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import com.pi4j.gpio.extension.pcf.PCF8574GpioProvider;
import com.pi4j.gpio.extension.pcf.PCF8574Pin;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.exceptions.NonExistingPinException;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.helperControlers.hardware.MPR121control;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.LocationPreparator;
import staticHelpers.OtherHelpers;

public class Act16_Misc extends Functionality {

	public static int POSITION = 16;
	public static String NAME = "Miscellaneous";
	public static String IDENTIFIER = "ActMiscNode";
	public static String DESCRIPTION = "Other kind of actions.";
	
	
	
	public static ProgramElement create_ElCurRuntime()
	{
		Object[] params = new Object[1];
		return(new FunctionalityContent( "ElCurRuntime",
				params,
				() -> {
					
						initVariableAndSet(params[0], Variable.doubleType, (double) (System.currentTimeMillis()));
						
					}));
	}
	public static ProgramElement visualize_ElCurRuntime(FunctionalityContent content)
	{
		VisualizableProgramElement elPwmSetPeriodAB;
		elPwmSetPeriodAB = new VisualizableProgramElement(content, "Get Current Time", "Get the current system time in millisconds.\nThe result will be set to the given variable.");
		elPwmSetPeriodAB.setArgumentDescription(0, new VariableOnly(true, true), "Variable");
		return(elPwmSetPeriodAB);
	}
	


	public static ProgramElement create_ElSyncBarrierSet()
	{
		Object[] params = new Object[2];
		return(new FunctionalityContent( "ElSyncBarrierSet",
				params,
				() -> {					
					
					CountDownLatch latch = new CountDownLatch(getINTparam( params[1] ));
					
					initVariableAndSet(params[0], Variable.countDownLatchType, latch);
					
					}));
	}
	public static ProgramElement visualize_ElSyncBarrierSet(FunctionalityContent content)
	{
		VisualizableProgramElement elSyncBarrierSet;
		elSyncBarrierSet = new VisualizableProgramElement(content, "Set Sync Barrier", "Creates or resets a synchronization barrier for threads.\nSince every event - including 'Labeled Events' are executed\nin individual system threads in parallel, synchronization might be needed.\nThe barrier serves this purpose by forming a countdown with the given start value.\nWhenever the 'Wait Sync Barrier' element is called somewhere\n the countdown is lowered by one and the corresponding thread waits\nuntil the counter has reached 0 (thanks to other threads).\nIn JAVA terms, the underlying system is the 'CountDownLatch' however the behavior is often called a 'Barrier'.");
		elSyncBarrierSet.setArgumentDescription(0, new VariableOnly(true, true), "Barrier Ident");
		elSyncBarrierSet.setArgumentDescription(1, new ValueOrVariable(), "Barrier Count");
		return(elSyncBarrierSet);
	}

	
	public static ProgramElement create_ElSyncBarrier()
	{
		Object[] params = new Object[1];
		return(new FunctionalityContent( "ElSyncBarrier",
				params,
				() -> {					
					
					try
					{
						CountDownLatch latch = (CountDownLatch) params[0];
						latch.countDown();
						try {
							latch.await();
						} catch (InterruptedException e) {e.printStackTrace();}
					}
					catch (ClassCastException e)
					{
						Execution.setError("Trying to use 'Wait Sync Barrier' with a 'Barrier identifier' which has not been initialized yet.", false);
					}
					
					}));
	}
	public static ProgramElement visualize_ElSyncBarrier(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Wait Sync Barrier", "Counterpart to 'Set Sync Barrier'.\nWhenever called somewhere the countdown is lowered by one and the corresponding thread waits\nuntil the counter has reached 0 (thanks to other threads).\nIn JAVA terms, the underlying system is the 'CountDownLatch' however the behavior is often called a 'Barrier'.");
		vis.setArgumentDescription(0, new VariableOnly(), "Barrier Ident");
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElRandomizer()
	{
		Object[] params = new Object[2];
		return(new FunctionalityContent( "ElRandomizer",
				params,
				() -> {
						Random rn = new Random();
						rn.setSeed((long) (double) params[1]);
						randomizerSeeds.put(rn, (Long) (long) (double) params[1]);
						initVariableAndSet(params[0], Variable.randomizerType, rn);
					}));
	}
	public static ProgramElement visualize_ElRandomizer(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Create Randomizer", "The 'Randomizer' can be used optionally in all elements which handle probability.\nNormally a default randomizer is used\nwhich will always give random results.\nHowever if you want to repeat the same row of\nnumbers any time the program starts, you can set a fixed seed here.");
		vis.addParameter(0, new VariableOnly(true, true), "Randomizer Ident", "Identifier variable for the new randomizer.");
		vis.addParameter(1, new ValueOrVariable(), "Seed", "A supposedly large number.\nTo create randomness every time the program starts, you could use the result from 'Get Current Time'.");

		return(vis);
	}
	
	public static ProgramElement create_ElSetSeed()
	{
		Object[] params = new Object[2];
		return(new FunctionalityContent( "ElSetSeed",
				params,
				() -> {
						((Random) params[1]).setSeed((long) (double) params[1]);
						randomizerSeeds.put((Random) params[1], (Long) (long) (double) params[1]);
					}));
	}
	public static ProgramElement visualize_ElSetSeed(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Set Seed", "Set a new seed for an existing 'Randomizer'.");
		vis.addParameter(0, new VariableOnly(), "Randomizer Ident", "Identifier variable for the existing randomizer.");
		vis.addParameter(1, new ValueOrVariable(), "Seed", "A supposedly large number.\nTo create randomness every time the program starts, you could use the result from 'Get Current Time'.");

		return(vis);
	}

	
	public static ProgramElement create_ElMpr121()
	{
		Object[] params = new Object[4];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElMpr121",
				params,
				() -> {
					
					int addr = parseI2CAddress((String) params[0]);
					if (addr < 0)
						return;
					
					byte touchThreshold = (byte) (((double) cont[0].getOptionalArgumentValueOR(0, 7.0))-128);
					byte releaseThreshold = (byte) (((double) cont[0].getOptionalArgumentValueOR(1, 4))-128);
					
					try {
						MPR121control.setupInput(addr, getINTparam(params[1]), getINTparam(params[2]), getINTparam(params[3]), touchThreshold, releaseThreshold);
					} catch (IOException | UnsupportedBusNumberException | NonExistingPinException e) {
						Execution.setError("Error at seting up a pin for an MPR 121 chip.\nError: " + e.getMessage(), false);
					}
					
					}));
	}
	public static ProgramElement visualize_ElMpr121(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Touch Input Mpr121", "Setup one of the 12 channels from an I2C MPR121 chip as a GPIO (usable by GPIO elements like events).\nThere are MPR121 boards for constructing your own touchpads and there are complete boards with number pads.");
		vis.addParameter(0, new ValueOrVariable(), "I2C Address", "");
		vis.addParameter(1, new ValueOrVariable(), "Channel", "0 - 11");
		vis.addParameter(2, new ValueOrVariable(), "IO Index", "Number to use as a GPIO.\nThe channel from the touch sensor will behave like a simple GPIO input\nwhich will be accessible under this number.\nNote that you should not define an existing number twice and also not overwrite a standard raspberry pin (0-39).\nIt's recomended to use easily memorable numbers like 100, 101, 102 and so on.\nOf coruse you can also store the numebr in a variable and reference the variable.");
		vis.addParameter(3, new ValueOrVariable(), "Interrupt GPIO", "The I2C MPR121 chip has one pin that has to be labeled with 'IRQ' or 'Interrupt'.\nThat pin has to be connected with the GPIO given here!");
		vis.addOptionalParameter(0, new ValueOrVariable("7"), "Touch Sensivity", "A value between 0 and 1 or 0 and 100 that tells at what charge level, the sensor channel detects a touch.\nSmall values often suffice.");
		vis.addOptionalParameter(1, new ValueOrVariable("4"), "Release Sensivity", "A value between 0 and 1 or 0 and 100 that tells at what charge level, the sensor channel stops detecting a touch anymore.\nIt should be lower than the touch sensivity. Small values often suffice.");
		
		return(vis);
	}

	
	public static ProgramElement create_ElPCF8574()
	{
		Object[] params = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElPCF8574",
				params,
				() -> {
					
					int addr = parseI2CAddress((String) params[0]);
					if (addr < 0)
						return;
					
					int pinInd = getINTparam(params[1]) + 7;
					
					if (pinInd < 40)
						Execution.setError("You cannot overwrite the standard raspberry pins! Use a number larger than 40.", false);
					else
						try {
							
							PCF8574GpioProvider provider = new PCF8574GpioProvider(LocationPreparator.i2c_bus_ind(), addr);
							for(int p = 7; p >= 0; p--)
								GPIOctrl.registerAdditionalPin(pinInd--, provider, PCF8574Pin.ALL[p]);						
							
						} catch (UnsupportedBusNumberException | IOException e)
						{
							Execution.setError("Accessing a PCF-8574 failed! Error: " + e.getMessage(), false);
						}
					
					
					}));
	}
	public static ProgramElement visualize_ElPCF8574(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Portexpander PCF-8574", "Adds 8 more pins to the device using a PCF-8574 portexpande attached via I2C.\nAfter this element executes you can use the pin just like any of the standard raspebrry GPIOs!\nUse the corresponding GPIO actions and events.");
		vis.addParameter(0, new ValueOrVariable(), "I2C Address", "");
		vis.addParameter(1, new ValueOrVariable(), "First Index", "The GPIO number you will be able to access the new pins provided by the port-expander.\nThe expander provides 8 pins. That means if you set this parameter to 50,\nthe new GPIOs will be available as 50, 51, 52. ... 57.\nNote that you may not overwrite standard raspberry pins (<40) and also not assign pins twice!");		
		return(vis);
	}
	
	
	
	
	public static ProgramElement create_ElSetupWatchdog()
	{
		Object[] params = new Object[1];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElSetupWatchdog",
				params,
				() -> {
					
					if (SIMULATED)
						return;
					
					if (LocationPreparator.isRaspberry())
					{
						boolean errorSensitive = (boolean) params[0];
						
						new Thread(() -> {
							
							// Enable the watchdog
							//sudo modprobe bcm2835_wdt“
							//cat > /dev/watchdog
							
							// https://github.com/binerry/RaspberryPi/blob/master/snippets/c/watchdog/wdt_test.c
							
							// TODO
							
							while(Execution.isRunning())
							{
								if (errorSensitive)
									if (Execution.hasError())
										return; // stop the thread without executing the watchdog
								
								
								// WATCHDOG_IOCTL_BASE == "W"
								
								OtherHelpers.sleepNonException(5000); // every five seconds								
							}
							
							if (!Execution.isRunning())
							{
								// Disable the watchdog
								// https://github.com/binerry/RaspberryPi/blob/master/snippets/c/watchdog/wdt_test.c
								// write(deviceHandle, "V", 1);
							}
							
						}).start();
						
					}
					else
						Execution.print("The watchdog is only usable on a Raspberry Pi!");
					
					}));
	}
	public static ProgramElement visualize_ElSetupWatchdog(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Setup Watchdog", "Activates the 'hardware watchdog' on a Raspberry Pi.\nThat is a hardware component that automatically restarts the whole system\nif the program which activated it, does not react anymore for 15 seconds.\nIf you want to use the Drasp program 24/7, you should activate this.\nNote that this only makes sense if the the Drasp program is on autostart on the Raspberry!");
		vis.addParameter(0, new ValueOrVariable(), "Error Sensitive", "Let the watchdog trigger if an error occurs anywhere in the program.\nNote that you should disable this while still debugging your software!");
		return(vis);
	}
	
	
}
