package main.functionality.helperControlers.screen;

import java.util.concurrent.FutureTask;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import staticHelpers.OtherHelpers;

public class JFXtext extends JFXitem
{
	Label label;
	
	public JFXtext(String text, int size, String color)
	{
		OtherHelpers.perform(new FutureTask<Object>(() -> {
			
			label = new Label(text);
			label.setFont(new Font("Arial", 30));
			
			label.setTextFill(Color.web("#" + color));
			
			//else
				//label.setTextFill(Color. .getColor(color));
			
			element.getChildren().add(label);
			
			return(null);
			
		}));
	}

	@Override
	protected void prepare()
	{
		
	}

	@Override
	protected void update()
	{
		
	}

	public void changeText(String newText)
	{
		Platform.runLater(() -> label.setText(newText));
	}
	
	public String getText()
	{
		return(label.getText());
	}
	
	
	@Override
	public String toStringSimple()
	{
		return("Text: " + getText());
	}
	@Override
	public String toString()
	{
		return(toStringSimple() + " - X: " + getPositionX() + " Y: " + getPositionY() + " W: " + getWidth() + " H: " + getHeight());
	}

}
