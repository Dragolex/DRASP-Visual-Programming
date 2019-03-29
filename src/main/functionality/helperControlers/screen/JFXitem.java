package main.functionality.helperControlers.screen;

import java.util.List;
import java.util.concurrent.FutureTask;

import dataTypes.ProgramEventContent;
import execution.Execution;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import main.functionality.Functionality;
import settings.GlobalSettings;
import staticHelpers.OtherHelpers;

public abstract class JFXitem
{
	JFXwindow window;
	Pane element;
	int posX = 0, posY = 0, offX = 0, offY = 0;
	int originalWidth, originalHeight;
	boolean stretchX = true, stretchY = true;
	int layer = 0;
	JFXwindow showingOnWindow = null;
	
	JFXitem()
	{
		element = new Pane();
	}
	
	protected void show(JFXwindow window, int layer)
	{
		if (this.showingOnWindow != null)
			window.remove(element, layer);
		
		this.layer = layer;
		this.window = window;
		window.add(element, layer);
		this.showingOnWindow = window;
	}

	protected void hide()
	{
		window.remove(element, layer);
		showingOnWindow = null;
	}
	
	
	protected abstract void prepare();
	protected abstract void update();
	
	
	
	
	
	// Public
	
	public JFXwindow getWindow()
	{
		return(showingOnWindow);
	}
	public int getLayer()
	{
		return(layer);
	}
	
	
	public void setOpacity(double opacity)
	{
		element.setOpacity(opacity);
	}
	
	public double getOpacity()
	{
		return(element.getOpacity());
	}
	
	public void setSize(int width, int height)
	{
		stretchX = (width < 0);
		stretchY = (height < 0);
		
		OtherHelpers.perform(new FutureTask<Object>(() -> {
			
			element.setMinWidth(width);
			element.setMaxWidth(width);
			element.setMinHeight(height);
			element.setMaxHeight(height);
			
			update();
		
			return(null);
		}));
	}
	
	
	public double getRotation()
	{
		return(element.getRotate());
	}
	public void setRotation(double angle)
	{
		element.setRotate(angle);
	}
	
	
	public void setScale(double scaleX, double scaleY)
	{
		setSize((int) (scaleX * originalWidth), (int) (scaleY * originalHeight));
	}

	
	public double getScaleX()
	{
		if (stretchX)
			return(1);
		else
			return(element.getWidth()/originalWidth);
	}
	public double getScaleY()
	{
		if (stretchY)
			return(1);
		else
			return(element.getHeight()/originalHeight);
	}
	
	
	public double getWidth()
	{
		return(element.getWidth());
	}
	public double getHeight()
	{
		return(element.getHeight());
	}
	
	
	
	public void setOffset(int offX, int offY)
	{
		this.offX = offX;
		this.offY = offY;
		
		setPosition(posX, posY);
	}
	
	public int getPositionX()
	{
		if (showingOnWindow != null)
			return (int) (element.getTranslateX()+offX);
		return(posX);
	}
	public int getPositionY()
	{
		if (showingOnWindow != null)
			return (int) (element.getTranslateY()+offY);
		return(posY);
	}
	
	public void setPosition(int x, int y)
	{
		posX = x;
		posY = y;
		
		OtherHelpers.perform(new FutureTask<Object>(() -> {
			element.setTranslateX(x-offX);
			element.setTranslateY(y-offY);
			return(null);
		}));
	}
		
	
	
	public void animateOpacity(double endOpacity, double duration, boolean hideAfterwards, int mode)
	{
		// {"One go", "Repeat", "Reverse Once", "Reverse and Repeat"}
		
		OtherHelpers.perform(new FutureTask<Object>(() -> {
		
		    FadeTransition ft = new FadeTransition(Duration.millis(duration), element);
	    	ft.setFromValue(getOpacity());
		    ft.setToValue(endOpacity);
		    
	    	ft.setAutoReverse(false);
	    	ft.setCycleCount(1);

		    switch (mode)
		    {
		    case 1:
		    	ft.setCycleCount(FadeTransition.INDEFINITE);
		    	break;
		    case 2:
		    	ft.setAutoReverse(true);
		    case 3:
		    	ft.setCycleCount(FadeTransition.INDEFINITE);
		    	ft.setAutoReverse(true);

		    }
		    
		    if (hideAfterwards)
		    	ft.setOnFinished((Event) -> hide());
		    
		    ft.play();
		    
		    return(null);
		}));
	}
	
	public void animateRotation(double endAngle, double duration, int mode)
	{
		// {"One go", "Repeat", "Reverse Once", "Reverse and Repeat"}
		
		OtherHelpers.perform(new FutureTask<Object>(() -> {
		
			RotateTransition ft = new RotateTransition(Duration.millis(duration), element);
	    	ft.setFromAngle(getRotation());
		    ft.setToAngle(endAngle);
		    
	    	ft.setAutoReverse(false);
	    	ft.setCycleCount(1);

		    switch (mode)
		    {
		    case 1:
		    	ft.setCycleCount(FadeTransition.INDEFINITE);
		    	break;
		    case 2:
		    	ft.setAutoReverse(true);
		    	break;
		    case 3:
		    	ft.setCycleCount(FadeTransition.INDEFINITE);
		    	ft.setAutoReverse(true);
		    	break;
		    }
		    
		    ft.play();
		    
		    return(null);
		}));
	}
	
	
	
	
	public void animateScale(double newScaleX, double newScaleY, double duration, int mode, int axis)
	{
		OtherHelpers.perform(new FutureTask<Object>(() -> {
			
		    ScaleTransition ft = new ScaleTransition(Duration.millis(duration), element);
		    
		    switch(axis)
		    {
		    case 0:
		    	ft.setFromX(this.getScaleX());
			    ft.setToX(newScaleX);
			    stretchX = false;
		    	break;
		    case 2:
		    	ft.setFromX(this.getScaleX());
			    ft.setToX(newScaleX);
			    stretchX = false;
			    // no break!
		    case 1:
		    	ft.setFromY(this.getScaleY());
			    ft.setToY(newScaleY);
			    stretchY = false;
			    break;
		    }
		    
		    switch (mode)
		    {
		    case 1:
		    	ft.setCycleCount(FadeTransition.INDEFINITE);
		    	break;
		    case 2:
		    	ft.setAutoReverse(true);
		    	break;
		    case 3:
		    	ft.setCycleCount(FadeTransition.INDEFINITE);
		    	ft.setAutoReverse(true);
		    	break;
		    }
		    
		    ft.play();
		    
		    update();
		    
		    return(null);
		}));
	}
	
	
	public void animatePosition(int newPosX, int newPosY, double duration, int mode, int axis)
	{
		OtherHelpers.perform(new FutureTask<Object>(() -> {
		
		    TranslateTransition ft = new TranslateTransition(Duration.millis(duration), element);
		    
		    switch(axis)
		    {
		    case 0:
		    	ft.setFromX(this.getScaleX());
			    ft.setToX(newPosX);
		    	break;
		    case 2:
		    	ft.setFromX(this.getScaleX());
			    ft.setToX(newPosX);
			    // no break!
		    case 1:
		    	ft.setFromY(this.getScaleY());
			    ft.setToY(newPosY);
			    break;
		    }
		    
		    switch (mode)
		    {
		    case 1:
		    	ft.setCycleCount(FadeTransition.INDEFINITE);
		    	break;
		    case 2:
		    	ft.setAutoReverse(true);
		    	break;
		    case 3:
		    	ft.setCycleCount(FadeTransition.INDEFINITE);
		    	ft.setAutoReverse(true);
		    	break;
		    }
		    
		    ft.setOnFinished((Event) -> {posX = getPositionX(); posY = getPositionY();});
		    
		    ft.play();
		    
		    
		    update();
		    
		    return(null);
		}));
	}
	
	
	
	
	public void animateToOther(JFXitem otherItem, double duration, boolean animateOpacity,  boolean animatePosition, boolean animateRotation, boolean animateScale, int mode)
	{
		if (animateOpacity)
		{
			this.animateOpacity(otherItem.getOpacity(), duration, false, mode);
		}
		
		if (animatePosition)
		{
			this.animatePosition(otherItem.getPositionX(), otherItem.getPositionY(), duration, mode, 2);
		}
		
		if (animateRotation)
		{
			this.animateRotation(otherItem.getRotation(), duration, mode);
		}
		
		if (animateScale)
		{
			this.animateScale(otherItem.getScaleX(), otherItem.getScaleY(), duration, mode, 2);
		}
	}
	
	
	private long last_move = 0;
	
	public void enableHoveringEvents(int type, List<ProgramEventContent> buttonHoverStateEventContents)
	{
		JFXitem th = this;
		
		switch(type)
		{
		case 0: // enter
			element.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
				for(ProgramEventContent cont: Functionality.buttonHoverStateEventContents)
					cont.triggerExternally(new Object[] {th, 0, false}); // pass the input value and trigger
			});
			break;
		case 1: // exited
			element.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
				for(ProgramEventContent cont: Functionality.buttonHoverStateEventContents)
					cont.triggerExternally(new Object[] {th, 1, false}); // pass the input value and trigger
			});
			break;
		case 2: // moving
			element.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
				if ((Execution.currentTimeMillisVague()-last_move) > GlobalSettings.constantCheckDelay)
				{
					for(ProgramEventContent cont: Functionality.buttonHoverStateEventContents)
						cont.triggerExternally(new Object[] {th, 2, false}); // pass the input value and trigger
					last_move = Execution.currentTimeMillisVague();
				}
			});
			break;

		case 3: // inside continuously
			element.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
				for(ProgramEventContent cont: Functionality.buttonHoverStateEventContents)
					cont.triggerExternally(new Object[] {th, 3, true}); // pass the input value and trigger
			});
			element.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
				for(ProgramEventContent cont: Functionality.buttonHoverStateEventContents)
					cont.triggerExternally(new Object[] {th, 3, false}); // pass the input value and trigger				
			});
			break;
		case 4: // pressed continuously
			element.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
				for(ProgramEventContent cont: Functionality.buttonHoverStateEventContents)
					cont.triggerExternally(new Object[] {th, 4, true}); // pass the input value and trigger
			});
			element.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
				for(ProgramEventContent cont: Functionality.buttonHoverStateEventContents)
					cont.triggerExternally(new Object[] {th, 4, false}); // pass the input value and trigger
			});
			break; // enter

		}
		
		
		
		
	}

	

	
	private volatile boolean currentlyPressed = false;
	
	static ColorAdjust default_item_hover_effect = new ColorAdjust(0, 0, -0.2, 0);
	static ColorAdjust default_item_pressed_effect = new ColorAdjust(0, 0, 0.2, 0);

	public void attachPressedEvent(ProgramEventContent progEv, boolean buttonize, JFXeffect hover_effect, JFXeffect pressed_effect)
	{		
		element.setOnMouseReleased((MouseEvent e) -> {
			currentlyPressed = true;
			element.setEffect(null);
		});
		
		
		if (buttonize)
		{
			if (hover_effect == null)
				element.setOnMouseEntered((MouseEvent e) -> element.setEffect(default_item_hover_effect));
			else
				element.setOnMouseEntered((MouseEvent e) -> element.setEffect(hover_effect.getAdjust()));
			
			if (hover_effect == null)
				element.setOnMousePressed((MouseEvent e) -> element.setEffect(default_item_pressed_effect));
			else
				element.setOnMousePressed((MouseEvent e) -> element.setEffect(pressed_effect.getAdjust()));

			
			element.setOnMouseExited((MouseEvent e) -> element.setEffect(null));
			element.setOnMouseReleased((MouseEvent e) -> {element.setEffect(null); currentlyPressed = true;});
		}
	}

	public boolean beenPressed() {
		if (currentlyPressed)
		{
			currentlyPressed = false;
			return(true);
		}
		return(false);
	}
	
	public void applyEffect(JFXeffect effect)
	{
		element.setEffect(effect.getAdjust());
	}
	public void applyCSS(String css)
	{
		element.setStyle(css);
	}
	

	public boolean isApplied()
	{
		return(showingOnWindow != null);
	}
	
	public abstract String toString();
	public abstract String toStringSimple();

	
	


	
}
