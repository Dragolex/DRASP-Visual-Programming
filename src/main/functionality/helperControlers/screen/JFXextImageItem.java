package main.functionality.helperControlers.screen;

import main.functionality.helperControlers.openCVrelated.ImageInput;

public class JFXextImageItem extends JFXimageItem
{

	public JFXextImageItem(ImageInput inp)
	{
		super(null);
		
		inp.startStreaming();
		
		setImage(inp.getImage());		
	}

}
