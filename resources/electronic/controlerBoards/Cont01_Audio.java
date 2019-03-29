package electronic.controlerBoards;

import dataTypes.ComponentContent;
import javafx.scene.paint.Color;
import main.electronic.Electronics;
import main.electronic.Pin;
import main.electronic.PinRow;

public class Cont01_Audio extends Electronics {

	public static int POSITION = 1;
	public static String NAME = "Audio";
	public static String IDENTIFIER = "ContAudio";
	public static String DESCRIPTION = "Boards and ICs used for audio providing and processing.";
	
	

	static public ComponentContent create_FMBK1068()
	{
		ComponentContent board = new ComponentContent("FM Receiver BK1068", 16, 12, true, 3);
		
		// Chip
		board.addRect(4, 4.1, 8, 4, chipColor); // Todo: Positions

		
		board.addText("BK1068", 8, 6.6, 2, Color.WHITE, 0);
		
		PinRow topPins = new PinRow(2.54, PinRow.HORIZONTAL, Pin.ESCAPE_UP, Pin.TEXT_ABOVE_VERTIC);
		topPins.add(Pin.IN, "Scan");
		topPins.add(Pin.OUT, "Speaker +");
		topPins.add(Pin.IN, "Reset");
		topPins.add(Pin.PWR_IN, "3V");
		
		PinRow bottomPins = new PinRow(2.54, PinRow.HORIZONTAL, Pin.ESCAPE_DOWN, Pin.TEXT_BELOW_VERTIC);
		bottomPins.add(Pin.IN, "Vol");
		bottomPins.add(Pin.PASSIVE, "OFF");
		bottomPins.add(Pin.IN, "Ant");
		bottomPins.add(Pin.GND, "GND");
		
		
		board.addPinRow(2.92+0.5*2.54, 2.13, topPins);
		board.addPinRow(2.92+0.5*2.54, 9.75, bottomPins);
		
		
		board.setRotationOffset(0, 0, 1, 0);
		
		return(board);
	}
	
	
	
	
	
}
