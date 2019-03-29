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

public class Basic00_PwrSources extends Electronics {

	public static int POSITION = 0;
	public static String NAME = "Power Sources";
	public static String IDENTIFIER = "BasicPower";
	public static String DESCRIPTION = "Power sources and batteries";
	
	
	
	static public ComponentContent create_ConstVolt()
	{
		ComponentContent board = new ComponentContent("Constant Voltage", 22.32, 8);
		
		// Pin connections
		board.addLine(1, 4, 3, 4, 2);
		board.addLine(-3, 4, -1, 4, 2);
		
		board.addRoundrectAbs(3, 0, -3, 8, 2, Color.HONEYDEW.darker());

		board.addText("Const. Voltage", 11.3, 3.2, 2.2, Color.BLACK, 0);
		
		board.addAttributeWrittenOnPart("Voltage", "", new BigDecimalAttribute(3.3, "V"), 11.16, 6.2, 2.2, Color.BLACK, 0);
		
		board.addPin(new Pin(1, 4, Pin.GND, Pin.TEXT_LEFT_HORIZ, "GND"));
		board.addPin(new Pin(-1, 4, Pin.PWR_OUT, Pin.TEXT_RIGHT_HORIZ, "PWR"));
		
		board.setRotationOffset(1, -1, 0, 0);
		
		return(board);
	}
	
	
	static public ComponentContent create_ConstCurrent()
	{
		ComponentContent board = new ComponentContent("Constant Current", 22.32, 8);
		
		board.addRoundrectAbs(3, 0, -3, 8, 2, Color.HONEYDEW.darker());
		
		board.setScalable();
		
		// Pin connections
		board.addLine(1, 4, 3, 4, 2);
		board.addLine(-3, 4, -1, 4, 2);
		
		board.addText("Const. Current", 11.3, 3.2, 2.2, Color.BLACK, 0);
		
		board.addAttributeWrittenOnPart("Current", "", new BigDecimalAttribute(1, "A"), 11.16, 6.2, 2.2, Color.BLACK, 0);
		
		board.addPin(new Pin(1, 4, Pin.GND, Pin.TEXT_LEFT_HORIZ, "GND"));
		board.addPin(new Pin(-1, 4, Pin.PWR_OUT, Pin.TEXT_RIGHT_HORIZ, "PWR"));
		
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
	

	static public ComponentContent create_BasicPowerRegBC()
	{
		ComponentContent design = TO92Case("Power Regulator", new TextAttribute("PWR"), Pin.PWR_IN, "V IN", Pin.GND, "GND", Pin.PWR_OUT, "V OUT");
		design.resetCreatorName(); // very important: This has to be called after the ComponentContent IF (and only if) it is called from a function that is not final one starting with "create_"

		design.addAttributeWrittenOnPart("Voltage", "", new BigDecimalAttribute(3.3, "V"), 3.54, 5.4, 2, Color.BLACK, 0);			
		
		return(design);
	}
	
	
	
}
