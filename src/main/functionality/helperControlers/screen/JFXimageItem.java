package main.functionality.helperControlers.screen;

import java.io.File;
import java.util.concurrent.FutureTask;

import execution.Execution;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import staticHelpers.FileHelpers;
import staticHelpers.OtherHelpers;

public class JFXimageItem extends JFXitem
{
	Image image;
	boolean prepared = false;
	String imageName = "";
	
	public JFXimageItem(String imagePath)
	{
		OtherHelpers.perform(new FutureTask<Object>(() -> {
			
			if (imagePath == null)
				return(null);
			
			String newImagePath = FileHelpers.resolveUniversalFilePath(FileHelpers.convertIfExternal(imagePath));
			
			Image tempImage;
			
			try
			{
				tempImage = new Image("file:" + newImagePath);
							
				if (tempImage.isError())
					tempImage = new Image(newImagePath);				
			}
			catch(IllegalArgumentException e)
			{
				Execution.setError("Loading the following image file failed: " + imagePath, false);
				return(null);
			}
			
			if (tempImage.isError())
			{
				Execution.setError("Loading the following image file failed: " + imagePath, false);
				return(null);
			}
			
			setImage(tempImage);
			
			
			imageName = (new File(newImagePath)).getName();
			
			return(null);
		}));
	}
	
	protected void setImage(Image image)
	{
		this.image = image;
		
		originalWidth = (int) image.getWidth();
		originalHeight = (int) image.getHeight();
		
		prepare();		
	}
	
	@Override
	protected void update() {} // nothing required
	
	@Override
	public void prepare()
	{
		if (!prepared)
		{
			element.getChildren().add(new ImageView(image));
			
			prepared = true;
		}
	}

	
	

	// Public functions
	
	public int getOriginalWidth()
	{
		return(originalWidth);
	}
	public int getOriginalHeight()
	{
		return(originalHeight);
	}
	
	
	
	@Override
	public String toStringSimple()
	{
		if (imageName.isEmpty())
			return("Image");
		else
			return("Image - File: " + imageName);
	}
	@Override
	public String toString()
	{
		return(toStringSimple() + " - X: " + getPositionX() + " Y: " + getPositionY() + " W: " + getWidth() + " H: " + getHeight());
	}


}
