package electronic.sensorsActors;

import dataTypes.ComponentContent;
import javafx.scene.paint.Color;
import main.electronic.Electronics;
import main.electronic.Pin;
import main.electronic.PinRow;
import main.electronic.attributes.BigDecimalAttribute;

public class SenAct00_Misc extends Electronics {

	public static int POSITION = 0;
	public static String NAME = "Misc";
	public static String IDENTIFIER = "SenActMisc";
	public static String DESCRIPTION = "Miscelaneous actors.";
	
	
	
	static public ComponentContent create_Speaker()
	{
		ComponentContent board = new ComponentContent("Speaker", 12, 26);
		
		
		double y1 = 9.19;
		double y2 = 16.81;
		
		board.addLine(1, y1, 3, y1, 2);
		board.addLine(1, y2, 3, y2, 2);

		
		board.addRect(3, 6, 4, 14, Color.DARKGRAY);
		
		
		board.addQuadPoly(7, 6,
						  7, -6,
						  12, 26,
						  12, 0, Color.DARKGRAY);
		

		board.addPin(new Pin(1, y1, Pin.IN, Pin.TEXT_LEFT_HORIZ, "SIG+"));
		board.addPin(new Pin(1, y2, Pin.PASSIVE, Pin.TEXT_LEFT_HORIZ, "GND"));
		

		board.addAttributeWrittenOnPart("Impedance", "The impedance of this speaker", new BigDecimalAttribute(6, "\u2126"), 5.1, 13.5, 2.2, Color.BLACK, 0);

		
		board.setRotationOffset(1, 0, 0, -1);

		
		return(board);
	}

	
	
	
	
}
