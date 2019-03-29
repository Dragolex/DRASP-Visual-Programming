package main.functionality.helperControlers.screen;

import javafx.scene.effect.ColorAdjust;
import main.functionality.SharedComponents;

public class JFXeffect extends SharedComponents {
	
	String effects = "";
	ColorAdjust adjust = new ColorAdjust();
	
	public JFXeffect(double contrast, double hue, double brightness, double saturation)
	{
		update(contrast, hue, brightness, saturation);
	}
	
	public void update(double contrast, double hue, double brightness, double saturation)
	{
		adjust.setContrast(contrast);
		adjust.setHue(hue);
		adjust.setBrightness(brightness);
		adjust.setSaturation(saturation);
		
		if (DEBUG)
		{
			effects = "";
			
			if (adjust.getContrast() != 0)
				effects +=  " Contr: " + adjust.getContrast();
			if (adjust.getHue() != 0)
				effects +=  " Hue: " + adjust.getHue();
			if (adjust.getBrightness() != 0)
				effects +=  " Bright: " + adjust.getBrightness();
			if (adjust.getSaturation() != 0)
				effects +=  " Satur: " + adjust.getSaturation();
		}
	}
	
	public ColorAdjust getAdjust()
	{
		return(adjust);
	}
	
	@Override
	public String toString()
	{
		return("Effect:" + effects);
	}
	
}
