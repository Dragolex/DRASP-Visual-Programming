package main.functionality;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.sound.sampled.TargetDataLine;

//import org.telegram.telegrambots.api.objects.Message;

import dataTypes.DataNode;
import dataTypes.ProgramElement;
import dataTypes.exceptions.AccessUnsetVariableException;
import dataTypes.exceptions.NonExistingPinException;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import execution.handlers.VariableHandler;
import main.functionality.helperControlers.HelperParent;
import main.functionality.helperControlers.NativeProgramHelper;
import main.functionality.helperControlers.hardware.GPIOcontrol;
import main.functionality.helperControlers.hardware.MPR121control;
import main.functionality.helperControlers.hardware.PWMboard.PWMcontrol;
import main.functionality.helperControlers.network.ConnectionMaster;
import main.functionality.helperControlers.network.TelegramControl;
//import main.functionality.helperControlers.network.TelegramControl;
import productionGUI.targetConnection.ConnectedExternalLinux;
import productionGUI.targetConnection.TargetConnection;
import productionGUI.tutorialElements.TutorialControler;
import settings.GlobalSettings;

public abstract class SharedComponents
{	
	public static final Runtime runtime = Runtime.getRuntime();
	
	// To be set by settings or launch parameters. For default values look into "GlobalSettings"!
	public static boolean DEBUG = false;
	protected static boolean SIMULATED = false;
	
	protected static boolean currentlyLoadingIDEelements = false;
	
	//Message k = new Message();
	
	// Add required global objects here. The constructors may not cause actual Initialization process or access the hardware!
	// Use the parent "HelperParent" and define constructing data and memmory in "@Override start()".
	// Then always call the startIfNeeded() function inside all functions which can be used externally.
	// This ensures that start() is only executed once
	
	public static final GPIOcontrol GPIOctrl = new GPIOcontrol();
	protected static final PWMcontrol PWMctrl = new PWMcontrol();
	protected static final TelegramControl TGRMctrl = new TelegramControl(); // TELEGRAM DEACTIVATED
	//protected static final OneWireControl OneWctrl = new OneWireControl();
	protected static final ConnectionMaster CONctrl = new ConnectionMaster();
	
	private static final HelperParent[] controlers = new HelperParent[] {GPIOctrl, PWMctrl, TGRMctrl, CONctrl}; // Add all controlers here
	
	
	// Other global objects
	
	protected static final Random randomizer = new Random(); // The global, independant randomizer	
	protected static final TargetDataLine[] recorderLine = new TargetDataLine[1];
	public final static Map<Random, Long> randomizerSeeds = new HashMap<>();
	
	
	// Reused description texts
	//protected static String Pi4JlayoutDescription = "If true, the Pi4J/Java Pin Layout will be used to interpret the 'Pin' number.";
	public static String gpioPinReferenceText = "\nClick into the field to see an interactive overview of possible pins.\nHint: If you store this number in a variable first,\nyou will see the pin-variable-name in the debug window!";
	public static String keyboardKeyDescription = "The 'Key Name' can be the letter to check, a key-word, or a combination of those.\\nAvailable Keywords: ANY, ANY_LETTER, ANY_UPPER, ANY_LOWER, ANY_NUMBER, ANY_ALTERING.\nNote that special keys like SPACE, ALT and CONTROL are available as well.\nTo combine multiple letters or keywords, use ',' as separators.\n\nTip: Search online for 'JavaFX KeyCode' for a list of all possibile special keys.";
	public static String hexadecimalColorDescription = "Color format is hexadecimal.\nThat means two hex values for red, green, blue and optional transparency.\nEvery hey value is represented by two numbers or letters.\nGoogle 'hex color'. It has an integrated generator :)";

	
	static protected int defaultDebounce = 30;
	static public boolean defaultUniqueExecEvent = true;
	static public int defaultMinWaitTimeEvent = 50;
	
	

	
	
	public static void resetGlobalObjects()
	{
		for(HelperParent ctrl: controlers)
			ctrl.doReset();
		
		Functionality.clearSpecialEventContents();
		
		MPR121control.reset();
		
		NativeProgramHelper.resetAll();
	}
	
	public static void quitGlobalObjects()
	{
		for(HelperParent ctrl: controlers)
			ctrl.doQuit();
		
		NativeProgramHelper.quitAll();
	}
	
	
	
	public static void setFunctionalityDebugAndSimulated(boolean debug, boolean simulated)
	{
		SharedComponents.DEBUG = debug;
		SharedComponents.SIMULATED = simulated;		
	}
	
	// TODO:
	/*
	DONE - Enable Pin-Debug controlling
	DONE - getBoolean/int/double(object variable) static in sharedFunctionality which directly interprets variable (taking an object).
	DONE - Restructure the event-adding of the first two arguments
	DONE - Enable an optional argument for all "conditions" to allow an output ( therefore enable the expandable argument system to have varying possibilities and datatypes )
	DONE - Implement a dropdown menu in SIMULATION with options: Activate breakpoints (Shift plus click); Run Step by Step
	DONE - Add SET binary variables
	DONE - Cause that ".0" values are not displayed as such.
	DONE - Prevent Inititalization event from having the optional arguments.
	DONE but could be done differently - Restructure the access to functions from ProgramContent only the functionality-creator should be able to access
	DONE - Implement Breakpoints.
	DONE - Make source elements default collapsed.
	DONE - Make the debug window prettier
	DONE - Implement de-bouncing for GPIO
	DONE - Create an input action
	DONE - Redesign how optional arguments are shown in the tooltip
	DONE - Complete the ComputationHelper!
	DONE - Switch the PWM stuff from "percentage" to "factor 0-1"
	DONE - Create binary operations section (and, or, xor usw.)
	DONE - Add tooltips in SettingsMenu (line 200++)!
	DONE - Add tutorial return back and forth
	DONE - Add Pause function
	DONE - Create Tool-System
	DONE - Add the "tracked" possibility for regular use as well in the options dropdown
	DONE - Swap argument positions below the element name on mouse click (Currently activateable with CONTROL pressed)
	DONE - Create Alarm element with Overwriteable (and perhaps Overwrite) argument. Mention this in the labeled event tutorial.
	DONE - Make that the functionality windows are having a max width when launching
	DONE - Load tutorials only when actually needed
	DONE - Create a tutorial for stacked events, instead of the events-synchronization
	DONE - Make pwm work with numbers >1 or < 0
	DONE - Make elements of a page transparent when the page is disabled.
	DONE - Change the message for no-set-arguments warning to show in multiple lines and include which argument caused the problem.
	DONE - Ensure that the debug and cmd windows cannot be ontop of messages!
	DONE - Make the drag motion simpler by showing a grid to drag into!
	DONE - FIX DRAG AND DROP
	DONE - Transform the start tutorial button to make it properly visible
	DONE - Add checkboxes in pages-buttons (with on-off. Perhaps also add the on-off text to all check box sliders)
	DONE - Remove the "java pin" stuff"!
	DONE - Class only handling debug-messages
	DONE - Prevent that leaving a text field always requires two clicks	
	DONE - Add a remark that Java-Pin Layout exists (perhaps in the pin-overview)
	DONE - ENABLE UNDO FOR VALUES
	DONE - Fix ProgramEventControler l.48
	DONE - Problem when removing an element with children.
	DONE - The info menus don't disappear on left click (only rightclick)
	DONE - Print when initializatione vents started and ended
	DONE - Find a place to call up the pin-overview (perhaps as a Sub-button for the info button)
	DONE - Copy and move non-collapsed blocks and perhaps mark blocks to copy (just show simple menu consisting of three elements: Copy; Move; Delete)
	DONE - Add fading arrow next to execution element	
	DONE - Add a Alarm Event feature (by calling a labeled event with delay)
	DONE - Allow that Labels are automatically created if a variable is used!
	DONE - Enable that the variable used for a pin is displayed correctly
	DONE - Instead of auto-switching the True/False parameter fields, always show a true/false proposal in a dropdown field (perhaps proposing # too)
	DONE - Add "visual" argument to "wait". Allow for newline with '|'
	DONE - Implement a system to define flags what functions are used in a prgram. Base what resources need to be extracted, on that.
	DONE - Instead of "apply item": "Place item" and include the coordinates. Make "layer" optional.
	DONE - Element for Split text to list
	DONE - Read file to text, Write text to file, Act: Replace text in text, Act: Get File/Dir path, Test "Set List Text" with Splitter, "List to Text"
	DONE - Create button system
	DONE - Test ConConnExists
	DONE - Text ElCloseCon
	DONE - Create the "enable wlan" tool
	DONE - Launch-Remote-Drasp event which also allows downloading files (perhaps create a separate download block)
	DONE - Enable that network links can be directly used in the file path
	DONE - When copying blocks, transform them into text to be able to transfer it into chats etc and share. Use this also for moving multiblocks.
	DONE - Clamp block (or (advanced) variable feature)!
	DONE - Add an apply (term)-feature for splines
	DONE - Steppermotors with more than 4 pins (allow as optional args)
	DONE - Fullstep mode for steppers: http://www.tuf-ev.de/workshop/schrittmotor/vollschr.htm
	DONE - Remove spline.recompute(); for PWMcontrol
	DONE - Create modulo element
	DONE - Implement: https://github.com/ControlEverythingCommunity/LSM303DLHC/tree/master/Java
	DONE - Allow to rotate items
	DONE - More file-operators like copy and delete
	DONE - Support power and voltage chip: https://github.com/gsteckman/rpi-ina219
	DONE - Support air pressure sensor: https://github.com/ControlEverythingCommunity/BMP280/blob/master/Java/BMP280.java
	DONE - Allow variables to be inserted into strings
	DONE - Local variables in all non-initialization events (associate variabels with their first called event)
	DONE - Splash screen whilst loading
	DONE - Multiple-Choice parameter type
	DONE - Remote-Execute commands (send commands to a SSH connectable device)
	DONE - Support a mini display
	DONE - Play sounds
	DONE - Record sounds
	DONE - Support (port expander per i2c): https://www.reichelt.de/PCF-I-C-Bus-Controller/PCF-8574-AN/3/index.html?ACTION=3&LA=446&ARTICLE=216403&GROUPID=2942&artnr=PCF+8574+AN&SEARCH=PCF8574&trstct=pos_2
	DONE - Split up the Actions, Conditions, Events, Structures into one file for every category

	
	ON HOLD - Restructure the event system to allow the an event to be performed from any point (like the GPIO interrupt event)
	ON HOLD - Investigate whether it makes sense (and is faster) to catch attention to execution not by alpha blinking but scaling from left to right.
	
	
	TODO - Remove String visualizerName from ProgramContent and repalce by automated reading
	
	
	
	Keywords to implement: "clojure", "scope" Call the "pair": "cons"
	
	
	TODO: Use an additional pane onto every component for things like floating text (like pin-names) and the part-values. that pane should be rotated just like the main pane.
	
	TODO: Rightclick onto source-elements shall have the option: "Open java code". Perhaps even offer reloading the function afterwards but also keep an "orig" file and allow to revert to it!
	
	
	--- TODO ---
	
	TODO: Implement a simple resource manager
	
	TODO: Finish implementing saving and loading of schemes
	
	TODO: Frequency detector based on input (for rotation detection with magnet sensor. Also include a parameter for multi-pulse per rotation!).
	TODO: Support Phasenanschnittsteuerung and PSM (pulse skip modulation) of AC viat triac board!
	
	TODO: Fill SYMBOL_PATH for some functionalities
	
	TODO: Move the "constant get value" feature from the read_value element to each sensor-accessing calls.
	Best would be to put those additional optional parameters into a function and just apply it to all analog elements.
	
	TODO: Fix the GPIO KEY system in SimpleTable line 401
	
	TODO: Make rotated Text on components possible!
	
	TODO: Verify whether keyboard events are not performable while initialization events are still running (think that is not the case)

	TODO: Implement moving average (see mobing average mail). Perhaps int wo variants, the datasaving one and the full one.
	
	TODO: Individual scale per components
	
	TODO: Add support for: https://tutorials-raspberrypi.de/raspberry-pi-rfid-rc522-tueroeffner-nfc/
	
	TODO: Add "Drag into the queue and provide the desired parameters." to all category descriptions

	TODO: optional argument : "Autoread Delay" for get analog value
	
	TODO: Ensure that there's some visual distinction sign when it is determined (via what's connected to it) that a GPIO is used as an input or as an output
		  Perhaps just a small arrow.
	TODO: Perhaps generally have a toggle whether to draw power-flow direction or not.
	
	TODO: rightclick option for pins that have multiple purpose to define their purpose! Like GPIO -> output onl. Or if GPIO+SPI, so only SPI is used.
	
	
	TODO: Create Virtual Event advanced structure that allows to hook a labeled event to any type of virtual, normal event and provide indices.
	Use this to dyanmically create sprites for example and assign each of it a virtual mouse-clicked event.
	When the particular event fires, the labeled event is executed instead and it gets the given id and/or a variable that has been given to the "Virtual Event" element.

	https://www.codejava.net/coding/capture-and-record-sound-into-wav-file-with-java-sound-api
	

	TODO: Implement logarithmic fading/mapping for PWM to achieve linear brightness change
	Perhaps realize this with a spline_to_lookup_table function that can then be applied o PWM outputs efficiently
	
	TODO: Buy and experiment with this: https://www.ebay.de/itm/WS2812B-100pcs-Matrix-Round-Dots-Lamp-Beads-LED-White-Board-DIY-For-Arduino-D0/263857931407?_trkparms=aid%3D555018%26algo%3DPL.SIM%26ao%3D1%26asc%3D20140117130753%26meid%3Dfd4bcc9fad624e858950dbd4f34067b0%26pid%3D100005%26rk%3D6%26rkt%3D12%26sd%3D272284442740%26itm%3D263857931407&_trksid=p2047675.c100005.m1851
	Would also be ideal to place light dots below scales!
	Maybe forming a collier with them.
	
	TODO: Add verification whetehr ANT is installed
	
	TODO: Cable-length exporter
	TODO: Auto-Routing
	
	TODO: Tracking of PWRs (in/out)
	TODO: Take care how a diode in and out needs to be
	
	TOOD: Setup led: https://github.com/jgarff/rpi_ws281x via https://github.com/jgarff/rpi_ws281x/issues/205
	
	And PWM via: https://github.com/Pi4J/pi4j/blob/master/pi4j-example/src/main/java/PwmExample.java
	https://github.com/Pi4J/pi4j/blob/master/pi4j-example/src/main/java/SoftPwmExample.java
	
	For WS 2801 (separate chip):
	https://github.com/novoda/spikes/blob/master/androidthings-ws2801-driver/ws2801-driver/src/main/java/com/xrigau/driver/ws2801/Ws2801.java
	
	
	TODO: "Boarder" featurew with comment for the Electronics window.
	Useful for designing lochraster Platinen and for forming named sections of components.
	
	TODO: Enable the user to give a name to any elements and also provide a comment that shows up like a popup. 
	
	TODO: Test from the "external deploy" testfile directory
	
	TODO: IDEA: Use 0.5 scaled versions of PCBs as icons!
	
	TODO: Create a "note" component for the electronics editor
	
	TODO: Finish Regulator system (Make a self-learning Regler (maybe keyword: PI(D) Regler) which learns how to approach the target
	values at best by providing a maximum factor and a minimum factor. Will need start-learning and stop learning blocks. The regulator can be saved in a file)
	Look at: https://github.com/tekdemo/MiniPID-Java
	
	TODO: Add Hysterese condition (also available for splines)
	
	TODO: Add a few transformation and copy features for Splines (optionally merge)
	
	TODO: Implement https://github.com/davef21370/EasyDriver/blob/master/easydriver.py
	
	TODO: Implement https://github.com/Poduzov/HX711-Pi4j that is very commonly used for weight cells! Possibly temperature compensation is needed!
	
	TODO: Read I2C Tool

	TODO: Read/Set GPIO Tool
	
	TODO: Make some components mirrorable
	
	
	TODO: Ensure that ToolsDatabase.getI2Cconfig() is used everywhere where i2c is accessed!
	TODO: Ensure that something similar works for SPI, OneWire and UART!
	TODO: Ensure that this only happens in a raspberry!
	TODO: Perhaps for debug, add a "revert" function that reverts all those setting changes. Perhaps make this an option for Deploy: "Revert Raspi Settings"
	
	
	TODO - Add Item-Click event

	
	TODO: Auto-test tutorials with: https://stackoverflow.com/questions/11552176/generating-a-mouseevent-in-javafx
	Maybe also just demonstrate drag and drop
	
	
	TODO!!! Test all hardware sensors!
	
	
	TODO: Offer the use of the Raspberry Watchdog
	
	TODO: Tool using Etcher Cli to burn SD cards: https://etcher.io/cli/
	Therefore create command: Burn SD and Backup SD (Needs a different program than Etcher
	
	
	TODO: Create "Get Sound Loudness" action.
	
		
	TODO: Enable tha variable clamping to go over borders and reenter in the other side. Like when borders are: 5 and 20. When the value is set to 22, it shall be 7 instead.
	
	
	TODO: idee: Block named "Manipulate argument" to edit an existing element. Use this for auto-range script for analog sensors!
	
	
	TODO: Use https://github.com/JorenSix/TarsosDSP for controlling sounds with music and perhaps to create a clap-event block. Also use this to save audio files.
	
	
	TODO: Add Q/A
	Q: Why do events seem to get lost sometimes?
	A: Ensure that you have unique on false and the min wait on 0!
	
	Q: Adding external functionality does not work
	A: Ensure that you are using a JDK, or as a simpler fix, copy the lib/tools.jar file from an JDK to the lib directory of your JRE!
	
	
	TODO: test the sound stuff
   	IDEE: Graph aus Audio bytes generieren lassen als Demo!
	
	TODO fix bug: When Program is running and an element is dragged into window, the element remains transparent as if outcommented!
	
	
	Idea instead of pop-up: when hovering an element, blend out the elements on another side (or in the middle) to have room for the description.
	Perhaps have it only activateable with a click on the popup.
	
	Perhaps use https://aubio.org/download for beat-detection!
	
	TODO: Fix bug that creating a new program can happen while a program is running!
	
	TODO: Describe all parameters of "Set Graph Axis"
	
	TODO: Ensure that the program does not shut down ebcause "no event running" when the init even started a looping "Labeled Event" and then ended!
	
	TODO: Remove all \n from tootltips and let the system auto-wrap.
	TODO: Idea: Have the tooltips only show single lines. With possibility to expand by pressing "F1 or doubleclicking right mouse")
	// Use l1.setWrapText(true); l1.setTextAlignment(TextAlignment.JUSTIFY);
	
	
	TODO: Add RETURN concept for callable events. Perhaps create a "Execute Returnable Label" Action/Structure
	
	
	TODO: An output instructions-list for the components editor that includes the length of cables!

	
	TODO: Add heat cam: https://shop.pimoroni.com/products/mlx90640-thermal-camera-breakout
	
	
	TODO: Perhaps add an on/off text next (before) to the lamp image


	TODO: Provide a C++ based PWM-controler for playing the splines

	
	
	TODO: Implement ht16k33 input and output: https://github.com/androidthings/contrib-drivers/blob/master/ht16k33/src/main/java/com/google/android/things/contrib/driver/ht16k33/Ht16k33.java
	https://github.com/androidthings/drivers-samples/blob/master/ht16k33/src/main/java/com/example/androidthings/driversamples/SegmentDisplayActivity.java
	
	
	TODO: Draw an "OR" next to an event that is the direct child of another event
	
	
	TODO: Warn if auto-initializing variables!
	
	
	
	TODO: Shutdown PWM by disabling the value
	

	TODO: See whether the update-variable still uses transitions!
	
	
	
	Another idea: Display full info of an element in a fixed window at the bottom of the screen!
	
	
	
	IDEA: Create a "Hardware Mode" where the user can drag hardware components and wiring is shown, as well as access to create coresponding events and actions.
	// Can be based on the tutorial helper
	To create the connections between components, use a "nears-free-point-auto-connection system. Left click creates or moves points and the closest element snaps to it. This allows simple, intuitive routing
	When objects are moved so lines are not straight anymore, show warning.
	When creating a point while there's another, unused point, delete that last one (only relevant when there are no components).
	Rightclick-menu on lines: Delete all points of this line | Name this line
	 
	
	
	--- TODO FEATURES ---

	

	
	TODO: Support (port expander by register shifter): https://www.ebay.de/itm/Raspberry-Pi-Infinity-Cascade-RPI-GPIO-Expansion-Board-IO-Extend-Adapter-Modul/172263936333?hash=item281bbb114d:g:MJIAAOSwEjFXehMt

	
	TODO: Create elements for handling usb relais.
	
	
	Another serial example: https://github.com/Pi4J/pi4j/blob/master/pi4j-gpio-extension/src/main/java/com/pi4j/gpio/extension/olimex/OlimexAVRIOGpioProvider.java
	
	
	TODO - Create Tool: Read all I2C devices
	TODO - Create Tool: Read all 1 Wire devices
	
	TODO - Create Tool: Read Pins
	
	
	TODO - Create "Auto Cap" variable which automatically caps a variable or loops it back to the start value
	
	TODO - Create a "Synchronized" block "Elements inside a Synchronizer Block are only executed once at a time."
	 		Provide an argument: "Skip Possible". Having an else clause is allowed!
	
		
	

	
	
	TODO - Enable the ! symbol for terms
	
		
	TODO - Add more images and schemes for certain functionalities
	
	
	
	
	TODO - Add possible connection of stepper motor and rotary encoders (to maintain position for example)
	
	TODO: Add more 1-wire sensors (perhaps also an ADC)
	
	TODO: Load functionality from file in directory




	
	TODO: Make tool to chose color (reuse that for changing GUI color layout)
	
	
	
	
	
	
	TODO: Animate the swap argument positions below the element name on mouse click (Currently activateable with CONTROL pressed)
	
	TODO: Implement an AND system for events. Perhaps by just merging them together. Executes when both conditions are true. Possibly with another optional argument: "IF WITHIN (ms)" to launch even if there's a delay between them
	
	
	TODO: Add a new-user entered-event for telegram or an automated start-message.
	
	TODO: More telegram features like sending files/camera photos
	
	TODO: Adjust the telegram bot tutorial to mention how to register a bot!
	
	
	


	
	
	
	TODO: Distance-meassurement and positioning with beacon: https://thingtype.com/blog/using-a-dwm1000-module-with-a-raspberry-pi-and-python/

	
	TODO: 
	
	Look at modifying stepper motor: http://www.jangeox.be/2013/10/change-unipolar-28byj-48-to-bipolar.html?m=1
	
	IR: https://indibit.de/raspberry-pi-mit-lirc-infrarot-befehle-senden-irsend/
	
	PID links:
	https://www.csimn.com/CSI_pages/PIDforDummies.html
	http://robotsforroboticists.com/pid-control/
	
	SUPPORT: 
		PCF8591: https://github.com/mattjlewis/diozero/blob/master/diozero-core/src/main/java/com/diozero/devices/PCF8591.java
		ADS1115: https://github.com/ControlEverythingCommunity/ADS1115/blob/master/Java/ADS1115.java
	
	
	--- TODO TESTS ---
		
		Unit-Test system
		
		Test the PWM controler
		
		Test the JFX system on the raspi zero
		-> If not working: Swing an option?
		
		
		Accurately test breakpoints	
		Accurately test telegram server
		Accurately test one wire interface	
		
		Test the ComputationHelper
		
		Test stepper motor drivers
		
		Test rotary encoder 
		
		Test external tracking
		
		
		TODO: Allow something like "-#var" for all fields
	
	
	Further tutorials/Examples:
		Use Regulator
		Drive Stepper-Motor
		Send and retriever 433Mhz
		Work with UART
		SPI LED control: https://www.raspberrypi.org/forums/viewtopic.php?t=196399 or with WS2812B which uses a PWM output. Note: Vorwiderstand von 470 Ohma uf der Datenleitung empfohlen.
		
		Implement:
		https://github.com/OlivierLD/raspberry-pi4j-samples/blob/master/I2C.SPI/src/i2c/sensor/VL53L0X.java
		https://github.com/OlivierLD/raspberry-pi4j-samples/blob/master/I2C.SPI/src/i2c/sensor/VL53L0X_v1.java
		
		
	
	
	TODO LATER:

	- Queued labeled event calls
	- Make it possible that multiple programs can be executed in parallel (helpful for testing)
	- Replaceable elements (When dragging to overlap exactly)
	- Peformance/limit improvements for loops (See the Speed and limit example)
	- Implement touch-support for the drag and drop feature
	- Use sockets to transmit data to client to change variables etc.
	- Include flashing tool in DRASP

	
	Functionality to implement:
	
	- Camera access
	- Sockets/Network communication (perhaps with videostreaming)
	- Play sounds
	
	
	
	BUGS:
		Variable info for pins when executed remote
		
		In breakpoints screen, the table columns are swapped
	
		When deploying localy, the message never disappears
		
		When doubleclicking comment block, it collapses children nevertheless
		
		If copying/dragging an element into an outcommented block, it appears not-commented!
		
		Under unknown circumstance, stopping a deployed program blocks the host program and nothing happens.
		
		Sometimes when doing a parameter task in a tutorial, the fadeout is red. Sometimes the tutorial blocks. 
		
	
		
		
		Tutorial to setup SD card:
		
		1. Get WinDiskManager
		2. Download the most actual OS
		3. Flash the card with WindDIskManager ("Write")
		4. Open the "boot" drive which should have appeared
		5. Create a simple termination-less and empty file named "ssh"
		6. Create or edit (if existing) a file named "wpa_supplicant.conf"
		7. Write into the file the following by replacing your WLAN password and name.
		You can also chose your country's shortcut apropiately as that decides the keybaord layout.
		use the 2-letter codes here: https://www.iso.org/obp/ui/#search/code/ Examples: US for USA and DE for Germany.
		
		country=DE
		ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
		update_config=1
		network={
		       ssid="wlan-bezeichnung"
		       psk="passwort"
		       key_mgmt=WPA-PSK
		}
		
		
		Set GPU memory to 256!
		
		Installing JFX: https://stackoverflow.com/questions/38359076/how-can-i-get-javafx-working-on-raspberry-pi-3?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
		
		https://www.raspberrypi.org/forums/viewtopic.php?t=21632
		
	*/
	
	protected static int getINTparam(Object input)
	{
		try {
			return((int) (double) input);
		}
		catch (ClassCastException e)
		{
			Execution.setError("An argument needs to be a numeric value (Double)\nbut had the type: " + input.getClass().getName(), false);
		}
		return(0);
	}
	
	protected static double getDOUBLEparam(Object input)
	{
		try {
			return((double) input);
		}
		catch (ClassCastException e)
		{
			Execution.setError("An argument needs to be a numeric value (Double)\nbut had the type: " + input.getClass().getName(), false);
		}
		return(0);
	}
	
	protected static float getFLOATparam(Object input)
	{
		try {
			return((float) (double) input);
		}
		catch (ClassCastException e)
		{
			Execution.setError("An argument needs to be a numeric value (Double)\nbut had the type: " + input.getClass().getName(), false);
		}
		return(0);
	}
	
	
	
	protected static String getSTRINGparam(Object input)
	{
		try {
			return((String) input);
		}
		catch (ClassCastException e)
		{
			Execution.setError("An argument needs to be a text (string)\nbut had the type: " + input.getClass().getName(), false);
		}
		return(null);
	}
	
	
	

	protected static boolean getBooleanVariable(Object variable)
	{
		try {
			
			return((boolean) ((Variable) variable).get()); // Casting the variable correctly and casting its contents into the desired type
			
		} catch (AccessUnsetVariableException e)
		{
			Execution.setError("Trying to get the value of a variable which has never been initialized!", false);
		}
		catch (ClassCastException e)
		{
			if (variable instanceof Variable)
			{
				Execution.setError("Trying to use the contents of a variable as BOOOLEAN (true\\false)\nwhile instead it contains: " + Variable.getDebugTypeName(((Variable) variable).getType()), false);
			}
			else
				funcError(variable);
		}
		return(false);
	}
	
	protected static int getIntegerVariable(Object variable)
	{
		try {
			
			return((int) (double) ((Variable) variable).get()); // Casting the variable correctly and casting its contents into the desired type
			
		} catch (AccessUnsetVariableException e)
		{
			Execution.setError("Trying to get the value of a variable which has never been initialized!", false);
		}
		catch (ClassCastException e)
		{
			if (variable instanceof Variable)
			{
				Execution.setError("Trying to use the contents of a variable as INTEGER (simple number)\nwhile instead it contains: " + Variable.getDebugTypeName(((Variable) variable).getType()), false);
			}
			else
				funcError(variable);
		}
		
		return(0);
	}
	
	protected static double getDoubleVariable(Object variable)
	{
		try {
			
			return((double) ((Variable) variable).get()); // Casting the variable correctly and casting its contents into the desired type
			
		} catch (AccessUnsetVariableException e)
		{
			Execution.setError("Trying to get the value of a variable which has never been initialized!", false);
		}
		catch (ClassCastException e)
		{
			if (variable instanceof Variable)
			{
				Execution.setError("Trying to use the contents of a variable as DOUBLE (number)\nwhile instead it contains: " + Variable.getDebugTypeName(((Variable) variable).getType()), false);
			}
			else
				funcError(variable);
		}
		
		return(0);
	}
	
	protected static String getStringVariable(Object variable)
	{
		try {
			
			return((String) ((Variable) variable).get()); // Casting the variable correctly and casting its contents into the desired type
			
		} catch (AccessUnsetVariableException e)
		{
			Execution.setError("Trying to get the value of a variable which has never been initialized!", false);
		}
		catch (ClassCastException e)
		{
			if (variable instanceof Variable)
			{
				Execution.setError("Trying to use the contents of a variable as TEXT (String)\nwhile instead it contains: " + Variable.getDebugTypeName(((Variable) variable).getType()), false);
			}
			else
				funcError(variable);
		}
		
		return("");
	}
	
	
	private static void funcError(Object variable)
	{
		Execution.setError("FUNCTIONALITY ERROR: Trying to handle an argument like a variable while it is not a variable!\nThe type is: " + variable.getClass().getName() +"\nIs the wrong argument index in use or have you forgotten to\ndeclare that the argument needs to be passed as a variable?", true);
	}
	
	
	
	protected static void initVariable(Object variable, int type)
	{
		((Variable) variable).initType(type);	
	}
	protected static void initVariableAndSet(Object variable, int type, Object value)
	{
		((Variable) variable).initTypeAndSet(type, value);
	}

	protected static void setVariable(Object variable, Object value)
	{
		((Variable) variable).set(value);
	}
	
	
	protected static boolean checkParam(Object val, String error) // throws error if null
	{
		if (val == null)
		{
			Execution.setError(error, false);
			return(false);
		}
		return(true);
	}
	
	
	protected static ConnectedExternalLinux getExternalTargetFromVar(Object var)
	{
		if (!(var instanceof Variable))
		{
			Execution.setError("The input is not a vairiable!", false);
			return(null);
		}

		
		String target_name = VariableHandler.getVariableName((Variable) var);
		
		if (target_name.equals("LOCAL"))
		{
			Execution.setError("LOCAL target computer is not allowed here!", false);
			return(null);
		}

		if (target_name.equals("DEPLOY"))
			if (GlobalSettings.destination instanceof ConnectedExternalLinux)
				return (ConnectedExternalLinux) (GlobalSettings.destination);
			else
			{
				Execution.setError("The deployment destination is local but this element only supports external computer targets!", false);
				return(null);
			}
		
		if (!((Variable) var).isType(Variable.DestinationDeviceType))
		{
			Execution.setError("The variable does not have the type of an 'External Computer'!\nIt has to be initialized with the same-named action.", false);
			return(null);
		}
		else
			return (ConnectedExternalLinux) (((Variable) var).getUnchecked());
	}
	
	protected static TargetConnection getExternalTargetFromVarOrLocal(Object var)
	{
		if (!(var instanceof Variable))
		{
			Execution.setError("The input is not a vairiable!", false);
			return(null);
		}
		
		String target_name = VariableHandler.getVariableName((Variable) var);
		
		if (target_name.equals("LOCAL"))
			return(GlobalSettings.localDestination);
		

		if (target_name.equals("DEPLOY"))
			if (GlobalSettings.destination instanceof ConnectedExternalLinux)
				return (ConnectedExternalLinux) (GlobalSettings.destination);
			else
			{
				Execution.setError("The deployment destination is local but this element only supports external computer targets!", false);
				return(null);
			}
		
		if (!((Variable) var).isType(Variable.DestinationDeviceType))
		{
			Execution.setError("The variable does not have the type of an 'External Computer'!\nIt has to be initialized with the same-named action.", false);
			return(null);
		}
		else
			return (ConnectedExternalLinux) (((Variable) var).getUnchecked());
	}
	
	
	protected static boolean assertVal(boolean check, String err)
	{
		if (!check)
			Execution.setError(err, false);
		
		return(!check);
	}
	protected static boolean assertVal(boolean checkA, boolean checkB, String err)
	{
		if ((!checkA) || (!checkB))
			Execution.setError(err, false);
		
		return((!checkA) || (!checkB));
	}
	
	// This has been taken outside the Callable so it can be reused by an "Event"!
	protected static boolean checkIfBoolVar(Variable input) throws AccessUnsetVariableException
	{
		Object res = input.get();
		switch(input.getType())
		{
		case Variable.boolType: 
			return((boolean)res);
		case Variable.doubleType:
			return(((double)res) >= 0.5);
		case Variable.textType:
			return(((String)res).equalsIgnoreCase("true"));
		}
		
		Execution.setError("Attempted to binarily check a variable of type: " + Variable.getDebugTypeName(input.getType()) +"\nThis is not possible.", false);
		
		return(false);
	}
	
	
	
	
	// A few functions required to form the node-trees for the GUI
	
	protected static DataNode<ProgramElement> attachToNodeUndelatable(DataNode<ProgramElement> node, ProgramElement element)
	{	
		DataNode<ProgramElement> newNode = new DataNode<ProgramElement>(element);
		element.setUndeletable(true);
		
		if (node != null)
			node.addChild(newNode);
		TutorialControler.addPossibleMark(element.getContent());
		
		return(newNode);
	}
	
	
	public static int getPinForJava(int pin) throws NonExistingPinException
	{
		/*
		if (givenAlreadyJavaPin)
		{
			if ((pin > 31) || (pin < 0))
				throw new NonExistingPinException(pin, givenAlreadyJavaPin);
			return(pin);
		}*/
		switch(pin)
		{
			case 2: return(8);
			case 3: return(9);
			case 4: return(7);
			case 17: return(0);
			case 27: return(2);
			case 22: return(3);
			case 10: return(12);
			case 9: return(13);
			case 11: return(14);
			case 5: return(21);
			case 6: return(22);
			case 13: return(23);
			case 19: return(24);
			case 26: return(25);
			case 14: return(15);
			case 15: return(16);
			case 18: return(1);
			case 23: return(4);
			case 24: return(5);
			case 25: return(6);
			case 8: return(10);
			case 7: return(11);
			case 12: return(26);
			case 16: return(27);
			case 20: return(28);
			case 21: return(29);
		}
		
		throw new NonExistingPinException(pin);
	}

	public static String extractFunctionalityName(int level)
	{
		String attempt = extractFunctionalityNameExact(level);
		
		if (attempt == null)
			attempt = extractFunctionalityNameExact(level+2);
		if (attempt == null)
			attempt = extractFunctionalityNameExact(level+1);

		if (attempt == null)
		{
			String eles = "";
			for (StackTraceElement strack: Thread.currentThread().getStackTrace())
				eles += "\n" +strack.getMethodName();
			
			InfoErrorHandler.callPrecompilingError("Invalid ProgramContent creation. Result for level '" + level + "': " + eles);// Thread.currentThread().getStackTrace()[level].getMethodName());
		}

		return(attempt);
	}

	private static String extractFunctionalityNameExact(int level)
	{
		String name = Thread.currentThread().getStackTrace()[level+1].getMethodName();
		if (name.startsWith("create_"))
			return(name.substring(7));
		return(null);
	}

	
	/*
	protected static DataNode<ProgrammElement> attachToNode(DataNode<ProgrammElement> node, ProgramContent element)
	{
		DataNode<ProgrammElement> newNode = new DataNode<ProgrammElement>(element);
		
		node.addChild(newNode);
		return(newNode);
	}
	*/
	
}
