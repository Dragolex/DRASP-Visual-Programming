package functionality.actions;

import main.functionality.Functionality;

public class Act03_LightEffects extends Functionality {

	public static int POSITION = 3;
	public static String NAME = "Light Effects";
	public static String IDENTIFIER = "ActLightEffectsNode";
	public static String DESCRIPTION = "Actions for light effects and using IC-LED stripes.";
	
	public static boolean DEACTIVATED = true;
	
	/*

	/// SETUP

	create_ElAddWSpixels()
	"Connect WS LEDs"

	vis.setParameter(0, new VariableOnly(true, true), "LEDs Controler", "Variable that will hold the identifier for those LEDs.")
	vis.setParameter(1, new SelectableType(new String[] {"PWM, SPI"} ), "Connected by ", "The type of connection to the WS LEDs.")



	create_ElGroupWsLEDs
	"Group WS LEDs"

	vis.setParameter(0, new VariableOnly(true, true), "LEDs Group", "Variable that will hold the identifier for those LEDs.")
	vis.setParameter(1, new VariableOnly(), "LEDs Controler", "Controler created with the corresponding action.")
	vis.setParameter(2, new ValueOrVariable("-1"), "First Pixel", "Index of the first pixel on the stripe to group or '-1' to use the next pixel after the last call of this element.")
	vis.setParameter(3, new ValueOrVariable(), "Group Length", "Number of LEDs to assign to this group.")
	vis.setOptionalParameter(0, new ValueOrVariable("0"), "Per Row", "If you provide a number for this group, a twodimensional group will be created by cutting the group after the given amount of LEDs.")


	create_ElGroupOutputs
	"Group GPIO LEDs"

	vis.setParameter(0, new VariableOnly(true, true), "LEDs Group", "Variable that will hold the identifier for this LED group.")
	vis.setParameter(1, new ValueOrVariable(), "GPIO", "GPIO where the LED is attached to.")
	// Note that this should binarily switch on and off only



	create_ElGroupPWMoutputs
	"Group GPIO LEDs"

	vis.setParameter(0, new VariableOnly(true, true), "LEDs Group", "Variable that will hold the identifier for this LED group.")
	vis.setParameter(1, new BooleanOrVariable("False"), "RGB", "If true, every three PWM channels are used together to form one RGB output like an LED.")
	vis.setParameter(2, new ValueOrVariable("-1"), "First PWM index", "Index of the first GPIO to add to this group.")
	vis.setParameter(3, new ValueOrVariable(), "Group Length", "Number of PWMs to assign to this group.\nNote that if you chosed RGB, this needs to be dividable by 3.")
	vis.setOptionalParameter(0, new ValueOrVariable("0"), "Per Row", "If you provide a number for this group, a twodimensional group will be created by cutting the group after the given amount of PWMs.")




	create_ElAddLEDRow
	"Add Pixel Row"

	vis.setParameter(0, new VariableOnly(true, true), "ADD LEDs Group", "Identifier variable of the group to add the row to.")
	vis.setParameter(1, new ValueOrVariable(), "As New Row", "If false, that means just appending."); 
	vis.setParameter(2, new ValueOrVariable("-1"), "First Pixel", "Index of the first pixel on the stripe to group or '-1' to use the next pixel after the last call of this element.")
	vis.setParameter(3, new ValueOrVariable(), "Row Length", "Number of LEDs to assign to this group.")
	vis.setOptionalParameter(0, new ValueOrVariable("0"), "Offset", "If not 0, this row fill be filled with so many nonexistent dummy LEDs to match the largest row.")





	create_ElInsertGPIOasLED
	create_ElInsertPWMasLED
	"Turns a simple GPIO or 1-3 PWMs to behave like an RGB pixel and inserts this into a group"



	TODO:
	Perhaps have a function that allows to create a group from a special text.
	For example:
	WS 0|WS 1|GP 9|GP 16|PW 3|WS 8
	To make a group for three WS LEDs, two GPIOs and 2 PWM outputs.
	Maybe also enable a multiline system or load this matrix from a file (auto adapting if the first symol is '/').
	Also enable dummy LEDs with a symbol for the 2D system.

	TODO: Make a system that simulates all outputs and visualizes them on a screen to debug.
	Maybe activatable as an item for a JFX window.
	Maybe even add the feature to move the positions of the LEDs and overlap them onto an image to achieve the possibility to design an object with LEDs (like a suit for example).

	///////////


	/// USAGE OF LIGHT EFFECTS

	create_ElLightPulseGrowing

	create_ElLightWave

	create_ElLightSideToSide

	create_ElLightByMusic

	create_ElLightBySplineLinear
	create_ElLightBySplineUnique (with Versatz/offset parameter between LEDs to achieve rainbow effects if applying a sine-spline onto the HUE channel for every LED but with a slight offset. )

	create_ElLightSpeedIncreasingPulses (flashes)

	*/
	
	
}
