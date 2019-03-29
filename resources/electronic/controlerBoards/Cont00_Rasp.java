package electronic.controlerBoards;

import dataTypes.ComponentContent;
import javafx.scene.paint.Color;
import main.electronic.Electronics;
import main.electronic.Pin;
import main.electronic.PinRow;

public class Cont00_Rasp extends Electronics {

	public static int POSITION = 0;
	public static String NAME = "Raspberry Pi";
	public static String IDENTIFIER = "ContRaspberry";
	public static String DESCRIPTION = "Raspberry PI boards.";
	
	
	static public ComponentContent create_ContRaspi()
	{		
		ComponentContent board = new ComponentContent("Raspberry Pi 3b", 85, 56, true, 5);
		
		board.addRingedHole(3.5, 3.5, 2.75, 6, copperColor);

		// Screwholes
		board.addRingedHole(3.5, 3.5, 2.75, 6, copperColor);
		board.addRingedHole(3.5, -3.5, 2.75, 6, copperColor);
		board.addRingedHole(58 + 3.5, 3.5, 2.75, 6, copperColor);
		board.addRingedHole(58 + 3.5, -3.5, 2.75, 6, copperColor);
		
		// Chip
		board.addRoundrect(26, 34, 12, 12, 2, chipColor); // Todo: Positions
		
		
		board.addRect(-15, 5, 18, 12, Color.SILVER); // Todo: Positions
		board.addRect(-15, 20, 18, 12, Color.SILVER); // Todo: Positions

		board.addRect(-18, 35, 21, 16, Color.SILVER); // Todo: Positions

		
		PinRow outerPins = new PinRow(2.54, PinRow.HORIZONTAL, Pin.ESCAPE_UP, Pin.TEXT_ABOVE_VERTIC);
		outerPins.add(Pin.PWR_OUT, "5.0V", 5.0);
		outerPins.add(Pin.PWR_OUT, "5.0V", 5.0);
		outerPins.add(Pin.GND, "Ground");
		outerPins.add(Pin.IO, "GPIO 14").add(Pin.UART_TX, "UART TX");
		outerPins.add(Pin.IO, "GPIO 15").add(Pin.UART_RX, "UART RX");
		outerPins.add(Pin.IO, "GPIO 18").add(Pin.SPI_CS, "SPI1: CS0", 1.0);
		outerPins.add(Pin.GND, "Ground");
		outerPins.add(Pin.IO, "GPIO 23");
		outerPins.add(Pin.IO, "GPIO 24");
		outerPins.add(Pin.GND, "Ground");
		outerPins.add(Pin.IO, "GPIO 25");		
		outerPins.add(Pin.IO, "GPIO 8").add(Pin.SPI_CS, "SPI0: CS0", 0.0);		
		outerPins.add(Pin.IO, "GPIO 7").add(Pin.SPI_CS, "SPI0: CS1", 0.0);
		outerPins.add(Pin.IO, "BCM 1");
		outerPins.add(Pin.GND, "Ground");
		outerPins.add(Pin.IO, "GPIO 12").add(Pin.OUT, "PWM 0");
		outerPins.add(Pin.GND, "Ground");
		outerPins.add(Pin.IO, "GPIO 16").add(Pin.SPI_CS, "SPI1: CS2", 1.0);
		outerPins.add(Pin.IO, "GPIO 20").add(Pin.SPI_MOSI, "SPI1: MOSI", 1.0);
		outerPins.add(Pin.IO, "GPIO 21").add(Pin.SPI_SCLK, "SPI1: SCLK", 1.0);
		
		
		PinRow innerPins = new PinRow(2.54, PinRow.HORIZONTAL, Pin.ESCAPE_UP, Pin.TEXT_BELOW_VERTIC);
		innerPins.add(Pin.PWR_OUT, "3.3V VCC", 3.3);
		innerPins.add(Pin.IO, "GPIO 2").add(Pin.I2C_SDA, "I²C: SDA");
		innerPins.add(Pin.IO, "GPIO 3").add(Pin.I2C_SCL, "I²C: SCL");
		innerPins.add(Pin.IO, "GPIO 4").add(Pin.OUT, "GPCLK 0");
		innerPins.add(Pin.GND, "Ground");
		innerPins.add(Pin.IO, "GPIO 17");
		innerPins.add(Pin.IO, "GPIO 27");
		innerPins.add(Pin.IO, "GPIO 22");
		innerPins.add(Pin.PWR_OUT, "3.3V VCC", 3.3);
		innerPins.add(Pin.IO, "GPIO 10").add(Pin.SPI_MOSI, "SPI0: MOSI", 0.0);
		innerPins.add(Pin.IO, "GPIO 9").add(Pin.SPI_MISO, "SPI0: MISO", 0.0);
		innerPins.add(Pin.IO, "GPIO 11").add(Pin.SPI_SCLK, "SPI0: SCLK", 0.0);
		innerPins.add(Pin.GND, "Ground");
		innerPins.add(Pin.IO, "BCM 0");
		innerPins.add(Pin.IO, "GPIO 5");
		innerPins.add(Pin.IO, "GPIO 6");
		innerPins.add(Pin.IO, "GPIO 13").add(Pin.OUT, "PWM 1");
		innerPins.add(Pin.IO, "GPIO 19").add(Pin.SPI_MISO, "SPI1: MISO", 1.0);
		innerPins.add(Pin.IO, "GPIO 26");
		innerPins.add(Pin.GND, "Ground");
		
		
		board.addPinRow(7.62+0.52, 1.54+0.52, outerPins);
		board.addPinRow(7.62+0.52, 4.08+0.52, innerPins);
		
		//board.addText("Raspberry Pi 3 Model B", 32, 12, 4, Color.BLACK, 0);
		board.addText("Raspberry Pi 3 Model B", 32, 22, 4, Color.BLACK, 0);
		
		
		board.setRotationOffset(1, 0, 0, 1);
		
		return(board);
	}
	
	
	
	
	
}
