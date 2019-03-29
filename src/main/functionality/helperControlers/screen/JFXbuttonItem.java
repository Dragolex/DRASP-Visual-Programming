package main.functionality.helperControlers.screen;

import dataTypes.ProgramEventContent;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import javafx.scene.control.Button;
import main.functionality.Events;
import main.functionality.Functionality;

public class JFXbuttonItem extends JFXimageItem
{
	Button button;
	JFXitem contentItem;
	
	public JFXbuttonItem()
	{
		super(null);
		
		
		//Idee: Face recognitiona nd following in Open CV
		
		
		button = new Button();
		
		
		JFXbuttonItem th = this;
		
		button.setOnAction((ev) -> {				
			if (!Execution.isPaused())
			if(Execution.isRunning())
			{
				Object[] dat = {th};
						
				for(ProgramEventContent cont: Functionality.buttonPressedEventContents)
					cont.triggerExternally(dat); // pass the input value and trigger
			}
				
		});

		
		/*
		bt.setOnMouseReleased((ev) -> {				
			if (!Execution.isPaused())
			if(Execution.isRunning())
			{
				System.out.println("Released a button");

				
				Object[] dat = {th, true};
					
				for(ProgramEventContent cont: Events.buttonPressedEventContents)
					cont.triggerExternally(dat); // pass the input value and trigger
			}
				
		});
		*/
	}

	public void updateContent(String text)
	{
		element.getChildren().clear();
		element.getChildren().add(button);
		
		contentItem = new JFXtext(text, 20, "000000");
		button.setGraphic(contentItem.element);
	}
	
	public void updateContent(Variable contentVar)
	{
		element.getChildren().clear();
		element.getChildren().add(button);
		
		if (!contentVar.hasValue())
		{
			Execution.setError("Trying to set content to a button with an empty variable!", false);
			return;
		}	
		if (contentVar.isType(Variable.textType))
		{
			updateContent((String) contentVar.getUnchecked());
			return;
		}
		if (contentVar.getUnchecked() instanceof JFXitem)
		{
			contentItem = (JFXitem) contentVar.getUnchecked();
			button.setGraphic(contentItem.element);
		}
		else
		{
			Execution.setError("Trying to set an unsuported type of content to a button!", false);
			return;
		}
		
	}
	
	
	@Override
	public String toStringSimple()
	{
		if (contentItem == null)
			return("Button - Content: N/A");
		return("Button - Content: " + contentItem.toStringSimple());
	}
	@Override
	public String toString()
	{
		return(toStringSimple() + " - X: " + getPositionX() + " Y: " + getPositionY() + " W: " + getWidth() + " H: " + getHeight());
	}

}
