package electronic.basicComponents;

import dataTypes.ComponentContent;
import javafx.scene.paint.Color;
import main.electronic.Electronics;
import main.electronic.Pin;
import main.electronic.PinRow;
import main.electronic.attributes.ColorAttribute;
import main.electronic.attributes.ComponentAttribute;
import main.electronic.attributes.BigDecimalAttribute;
import main.electronic.attributes.TextAttribute;
import staticHelpers.OtherHelpers;

public class Basic01_Passive extends Electronics {

	public static int POSITION = 1;
	public static String NAME = "Passive Components";
	public static String IDENTIFIER = "BasicPassive";
	public static String DESCRIPTION = "Passive components like resistors, capacitors etc.";
	
	
	static public ComponentContent create_BasicResistor()
	{
		ComponentContent board = new ComponentContent("Resistor", 12.16, 2);
		
		board.addRoundrectAbs(3, 0, -3, 2, 2, Color.HONEYDEW);
		
		board.setScalable();
		
		// Pin connections
		board.addLine(1, 1, 3, 1, 2);
		board.addLine(-3, 1, -1, 1, 2);
		
		board.addAttributeWrittenOnPart("Resistance", "", new BigDecimalAttribute(1000, "\u2126"), 6.08, 1.65, 1.9, Color.BLACK, 0);
		
		board.addPin(new Pin(1, 1, Pin.PASSIVE, Pin.TEXT_LEFT_HORIZ, ""));
		board.addPin(new Pin(-1, 1, Pin.PASSIVE, Pin.TEXT_RIGHT_HORIZ, ""));
		
		return(board);
	}
	
	static public ComponentContent create_BasicDiode()
	{
		ComponentContent board = new ComponentContent("Diode", 12.16, 2);
		
		board.addRectAbs(3, 0, -3, 2, Color.SANDYBROWN);
		
		board.setScalable();

		board.addLine(3, 0, -3, 1);
		board.addLine(3, 2, -3, 1);

		// Pin connections
		board.addLine(1, 1, 3, 1, 2);
		board.addLine(-3, 1, -1, 1, 2);
		

		board.addPin(new Pin(1, 1, Pin.IN, Pin.TEXT_LEFT_HORIZ, "Anode")); // alligned pos!
		board.addPin(new Pin(-1, 1, Pin.OUT, Pin.TEXT_RIGHT_HORIZ, "Kathode"));		
		
		return(board);
	}

	
	
	static public ComponentContent create_BasicElectrolyt()
	{
		ComponentContent board = new ComponentContent("Electrolyte Capacitor", 6.54, 15);
		
		board.setScalable();
		
		// Pin connections
		board.addLine(2, -1, 2, -7, 2);
		board.addLine(-2, -3.54, -2, -7, 2);
		
		
		board.addEllipseAbs(0.27, 0, -0.27, 4.5, Color.DARKBLUE);
		board.addRoundrectAbs(0.27, 3, -0.27, -6.5, 1.5, Color.DARKBLUE);
		board.addEllipseAbs(1, 0.63, -1, 3.5, Color.SILVER);

		board.addRectAbs(-3, 4, -1.5, -7, Color.DARKGRAY);
		
		
		
		// Cross inside the circle at the top
		double rx = OtherHelpers.lengthdirX(35, 2);
		double ry = OtherHelpers.lengthdirY(35, 2);
		double rx2 = OtherHelpers.lengthdirX(145, 2);
		double ry2 = OtherHelpers.lengthdirY(145, 2);
		
		board.addLine(3.27-rx, 2.065-ry, 3.27+rx, 2.065+ry);
		board.addLine(3.27-rx2, 2.065-ry2, 3.27+rx2, 2.065+ry2);
		
		
		
		board.addPin(new Pin(2, -1, Pin.IN, Pin.TEXT_LEFT_HORIZ, "Anode")); // align on grid
		board.addPin(new Pin(-2, -3.54, Pin.OUT, Pin.TEXT_RIGHT_HORIZ, "Kathode"));		
		
		
		board.setRotationOffset(1, 0, 1, 1);
		
		//board.addAttributeWrittenOnPart("Capacity", "The capacity.", new BigDecimalAttribute(100, "uF", 0.000001f), 4.27, 5.0, 2, Color.WHITE, 90);
		board.addAttributeWrittenOnPart("Capacity", "The capacity.", new BigDecimalAttribute(100, "uF"), 4.27, 5.0, 2, Color.WHITE, 90);
		
		return(board);
	}
	
	
	static public ComponentContent create_BasicCeramicCapacitor()
	{
		ComponentContent board = new ComponentContent("Ceramic Capacitor", 6.54, 10);
		
		board.setScalable();
		
		// Pin connections
		board.addLine(2, -1, 2, -4, 2);
		board.addLine(-2, -1, -2, -4, 2);

		
		board.addRectAbs(2-0.5, -6, 2+0.5, -4, Color.SADDLEBROWN);
		board.addRectAbs(-2-0.5, -6, -2+0.5, -4, Color.SADDLEBROWN);
		board.addCircle(3.27, 3, 4.5, Color.SADDLEBROWN);

		
		board.addPin(new Pin(2, -1, Pin.PASSIVE, Pin.TEXT_LEFT_HORIZ, "")); // align on grid
		board.addPin(new Pin(-2, -1, Pin.PASSIVE, Pin.TEXT_RIGHT_HORIZ, ""));		
		
		
		//board.addAttributeWrittenOutsidePart("Resistance", "", new BigDecimalAttribute(0.1, "F", 0.000001f), 3.27, -5, 2, 0);
		board.addAttributeWrittenOutsidePart("Resistance", "", new BigDecimalAttribute(100, "nF"), -1.5, -5, 2, 0);
		
		
		board.setRotationOffset(1, 0, 1, 1);
		
		return(board);
	}
	

	static public ComponentContent create_BasicLED()
	{
		ComponentContent board = new ComponentContent("LED", 4.54, 13);
		
		board.setScalable();

		
		// Pin connections
		board.addLine(1, -1, 1, -7, 2);
		board.addLine(-1, -3.54, -1, -7, 2);
		
		ColorAttribute ledCol = new ColorAttribute(Color.WHITESMOKE);
		
		board.addCircle(2.27, 3, 4, ledCol);
		
		board.addRectAbs(0.27, 3, -0.27, -6.5, ledCol);
		board.addLine(0.27, 3, -0.27, 3, ledCol); // Overdraw the upper edge of the rect

		
		board.addRectAbs(0, -6.5-0.75, -0.001, -6.5, ledCol);
		board.addLine(0.27, -6.5-0.75, -0.27, -6.5-0.75, ledCol); // Overdraw the upper edge of the rect
		
		
		// Todo: Add wider base
		
		
		Pin p = new Pin(1, -1, Pin.IN, Pin.TEXT_LEFT_HORIZ, "Anode");
		p.enforceEscapeDirection(Pin.ESCAPE_DOWN);
		board.addPin(p);

		p = new Pin(-1, -3.54, Pin.OUT, Pin.TEXT_RIGHT_HORIZ, "Kathode");
		p.enforceEscapeDirection(Pin.ESCAPE_DOWN);
		board.addPin(p);
		
		
		board.addAttribute("Color", "The color of the LED.", ledCol);
		
		
		board.setRotationOffset(1, -1, 1, 0);
		
		return(board);
	}
	
	
	
	private static ComponentContent TO92Case(String name, TextAttribute onDesignText, int pin1, String pin1Name, int pin2, String pin2Name, int pin3, String pin3Name)
	{
		ComponentContent board = new ComponentContent(name, 7.08, 12);
		
		board.setScalable();

		
		board.addEllipseAbs(1.5, 0, -1.5, 5, Color.rgb(60, 60, 60));
		board.addRectAbs(1.5, 2.5, -1.5, 6, Color.rgb(30, 30, 30));
		
		board.addLine(2.27, 6, 1, -3.5, 2);
		board.addLine(4.81, 6, 6.08, -3.5, 2);
		
		board.addLine(1, -1, 1, -3.5, 2);
		board.addLine(3.54, -1, 3.54, 6, 2);
		board.addLine(6.08, -1, 6.08, -3.5, 2);
		
		board.addText(onDesignText, 3.54, 3.8, 1.5, Color.GRAY, 0);
		
		board.addPin(new Pin(1, -1, pin1, Pin.TEXT_LEFT_HORIZ, pin1Name)); // align on grid
		board.addPin(new Pin(3.54, -1, pin2, Pin.TEXT_BELOW_HORIZ, pin2Name)); // align on grid
		board.addPin(new Pin(6.08, -1, pin3, Pin.TEXT_RIGHT_HORIZ, pin3Name));
		
		return(board);
	}
	
	
	static public ComponentContent create_BasicTransistorBC()
	{	
		ComponentContent design = TO92Case("Transistor BC547", new TextAttribute("BC\n547"), Pin.IN, "Gate", Pin.PWR_IN, "Source", Pin.PWR_OUT, "Drain");
		design.resetCreatorName(); // very important: This has to be called after the ComponentContent IF (and only if) it is called from a function that is not final one starting with "create_"

		return(design);
	}


	
	
	
}
