package otherHelpers;

import java.time.Duration;

import org.reactfx.util.FxTimer;

import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import settings.GlobalSettings;
import staticHelpers.OtherHelpers;

public class MarkingRectangleHelper {
	
	boolean currentlyMarking = false;
	double markingStartX, markingStartY;
	Rectangle markingRect;
	Double[] corners;
	Runnable toCheck;
	long startDragTime;
	Runnable rightclickEvent;
	boolean enabled = true;
	
	public MarkingRectangleHelper(Node target, Double[] corners, Runnable toCheck, Runnable rightclickEvent)
	{
		this.corners = corners;
		this.toCheck = toCheck;
		
		this.rightclickEvent = rightclickEvent;
		
		markingRect = new Rectangle(0,0, 1,1);
		markingRect.setFill(Color.WHITESMOKE);
		markingRect.setStroke(Color.BLACK);
		markingRect.setStrokeDashOffset(5);
		markingRect.setOpacity(0.35);
		
		markingRect.setManaged(false);
		
		OtherHelpers.applyOptimizations(markingRect);
		
		target.addEventHandler(MouseEvent.MOUSE_PRESSED, (ev) -> {
			
			if (ev.getButton() == MouseButton.PRIMARY)
			{
				startDragTime = System.currentTimeMillis();
				
				currentlyMarking = true;
				markingStartX = ev.getSceneX();
				corners[0] = markingStartX;
				
				markingStartY = ev.getSceneY();
				corners[1] = markingStartY;
				
				markingRect.setX(markingStartX);
				markingRect.setY(markingStartY);
				
				markingRect.setWidth(1);
				markingRect.setHeight(1);
				
				markingRect.setVisible(true);
				
				FxTimer.runLater(Duration.ofMillis(250),
				() -> {
				if (!DragAndDropHelper.getTopPane().getChildren().contains(markingRect))
					DragAndDropHelper.getTopPane().getChildren().add(markingRect);
				markingRect.toFront();
				});
			}
			
			});
		
		target.addEventHandler(MouseEvent.MOUSE_RELEASED, (ev) -> {
			
			if ((System.currentTimeMillis()-startDragTime) < GlobalSettings.doubleClickTime)
				unmark();
			else
			if (ev.getButton() == MouseButton.SECONDARY)
			{
				corners[0] = ev.getSceneX();
				corners[1] = ev.getSceneY();
				rightclickEvent.run();
			}
			
			markingRect.setVisible(false);
			
			currentlyMarking = false;
			if (DragAndDropHelper.getTopPane().getChildren().contains(markingRect))
				DragAndDropHelper.getTopPane().getChildren().remove(markingRect);
			});


		target.addEventFilter(MouseEvent.MOUSE_DRAGGED, (ev) -> {
			if (currentlyMarking)
			{
				if (!enabled)
				{
					unmark();
					currentlyMarking = false;
					return;
				}
				
				double w = Math.abs(ev.getSceneX() - markingStartX);			
				double h = Math.abs(ev.getSceneY() - markingStartY);
				
				markingRect.setWidth(w);
				markingRect.setHeight(h);
				
				if (ev.getSceneX() < markingStartX)
				{
					corners[0] = ev.getSceneX();
					markingRect.setX(corners[0]);
				}
				if (ev.getSceneY() < markingStartY)
				{
					corners[1] = ev.getSceneY();
					markingRect.setY(ev.getSceneY());
				}
				
				corners[2] = w;
				corners[3] = h;
				
				toCheck.run();
			}
			});
	}
	
	public void triggerExternally(double x, double y)
	{
		corners[0] = x;
		corners[1] = y;
		rightclickEvent.run();
	}

	public void unmark()
	{
		corners[0] = -1.0;
		corners[1] = -1.0;
		corners[2] = -1.0;
		corners[3] = -1.0;
		toCheck.run();
		
		markingRect.setVisible(false);
	}

	public void disable()
	{
		enabled = false;
	}
	public void enable()
	{
		enabled = true;
	}

}
