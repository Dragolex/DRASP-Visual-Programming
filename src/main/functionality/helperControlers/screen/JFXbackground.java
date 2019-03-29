package main.functionality.helperControlers.screen;

import java.util.concurrent.FutureTask;

import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import staticHelpers.OtherHelpers;

public class JFXbackground extends JFXimageItem
{
	
	public JFXbackground(String imagePath, int type)
	{
		super(imagePath);

		OtherHelpers.perform(new FutureTask<Object>(() -> {
			BackgroundImage bc = createNewBackgroundImage(image, type);
			element.setBackground(new Background(bc));
			update();
			return(null);
		}));
	}

	@Override
	public void prepare()
	{
		prepared = true;
	}
	
	
	@Override
	protected void update()
	{
		// Stretch to fit
		
		if (stretchX)
		{
			AnchorPane.setLeftAnchor(element, 0.0);
			AnchorPane.setRightAnchor(element, 0.0);
		}
		else
		{
			AnchorPane.setLeftAnchor(element, null);
			AnchorPane.setRightAnchor(element, null);
		}

		if (stretchY)
		{
			AnchorPane.setBottomAnchor(element, 0.0);
			AnchorPane.setTopAnchor(element, 0.0);
		}
		else
		{
			AnchorPane.setBottomAnchor(element, null);
			AnchorPane.setTopAnchor(element, null);
		}
	}
	
	
	

	
	
	private static BackgroundImage createNewBackgroundImage(Image img, int type)
	{		
		BackgroundImage bc = null;
		
		switch(type)
		{
		case 0: bc = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(img.getWidth(), img.getHeight(), false, false, false, false));
		break;
		case 1: bc = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(1, 1, true, true, true, true));
		break;
		case 2: bc = new BackgroundImage(img, BackgroundRepeat.REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, //BackgroundSize.DEFAULT);
				new BackgroundSize(img.getWidth(), 1, false, true, false, false));
		break;
		case 3: bc = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(1, img.getHeight(), true, false, false, false));
		break;
		case 4: bc = new BackgroundImage(img, BackgroundRepeat.REPEAT,
				BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
		break;
		}

		return(bc);
	}
	
	
}
