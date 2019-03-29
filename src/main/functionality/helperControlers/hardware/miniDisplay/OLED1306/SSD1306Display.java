package main.functionality.helperControlers.hardware.miniDisplay.OLED1306;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.i2c.I2CBus;

import main.functionality.helperControlers.hardware.miniDisplay.MiniDisplay;
import main.functionality.helperControlers.hardware.miniDisplay.OLED1306.font.CodePage1252;
import main.functionality.helperControlers.hardware.miniDisplay.OLED1306.font.CodePage437;
import main.functionality.helperControlers.hardware.miniDisplay.OLED1306.font.CodePage850;
import main.functionality.helperControlers.hardware.miniDisplay.OLED1306.font.Font;
import main.functionality.helperControlers.hardware.miniDisplay.OLED1306.impl.SSD1306I2CImpl;
import staticHelpers.LocationPreparator;


public class SSD1306Display implements MiniDisplay {

	SSD1306 ssd1306;
	Graphics graphics;
	Font currentCodepage;
	Font[] possibleCodepages = new Font[3];
	
	public SSD1306Display(int i2cAddress, GpioPinDigitalOutput resetPin)
	{
		ssd1306 = new SSD1306I2CImpl(128, 64, resetPin, LocationPreparator.i2c_bus_ind() == 1 ? I2CBus.BUS_1 : I2CBus.BUS_0, 0x3D);
		
		graphics = ssd1306.getGraphics();
		
		ssd1306.startup(false);
	}
	
	
	@Override
	public void drawText(int x, int y, String txt) {
		graphics.text(x, y, currentCodepage, txt);
	}
	

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		graphics.line(x1, y1, x2, y2);		
	}

	@Override
	public void drawArc(int x, int y, int r, int startAng, int endAng) {
		graphics.arc(x, y, r, startAng, endAng);
	}
	

	@Override
	public void drawRectangle(int x, int y, int width, int height, boolean filled) {
		graphics.rectangle(x, y, width, height, filled);			
	}

	@Override
	public void drawCircle(int x, int y, int r, boolean filled) {
		graphics.circle(x, y, r, filled);
	}


	@Override
	public String toString() {
		return("I2C Display 'SSD1306'");
	}


	@Override
	public void setCharacterSet(int index) {
		
		if (possibleCodepages[index] == null)
		{
			switch(index)
			{
			case 0: possibleCodepages[index] = new CodePage1252(); break;
			case 1: possibleCodepages[index] = new CodePage437(); break;
			case 2: possibleCodepages[index] = new CodePage850(); break;
			}
		}
		
		currentCodepage = possibleCodepages[index];
	}

}
