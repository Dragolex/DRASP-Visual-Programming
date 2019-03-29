package otherHelpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.reactfx.util.FxTimer;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import productionGUI.ProductionGUI;
import settings.GlobalSettings;
import staticHelpers.KeyChecker;
import staticHelpers.TemplateHandler;


/**
 * This class provides functions to easily realize Drag and Drop with a visual dummy, a copy of the originally dragged element.
 *
 * @author Alexander Georgescu
 */
public class DragAndDropHelper {

	//private static Object currentlyDraggingObject = null;
	private static Pane topPane;
	volatile private static Parent dragInstance, origDragInstance;
	private static Object transferPayload;
	private static Dragboard currentDB;
	private static int mouseOffsetX, mouseOffsetY;
	//private static int mouseX = 0, mouseY = 0, mouseScreenX = 0, mouseScreenY = 0;
	private static int mouseX = 0, mouseY = 0, relativeMouseX = 0, relativeMouseY = 0;
	private static int lastMouseX, lastMouseY;
	volatile private static boolean mouseDown = false;
	volatile private static float mouseDir;
	volatile private static boolean lastMouseVDir = false;
	volatile private static boolean mouseVDirCompensated = false;
	private static Pane savedPane;
	private static boolean mouseOutside;
	private static long currentDragDropFrame = -1;
	
	private static boolean currently_running = false;
	
    private static long frameDur = 0;
    private static long lastFrameTime = 0;
    private static volatile long currentFrame = 0;
    private static double currentFactor = 1;

	private static List<DragDropInteraction> dragDropInteractions = new ArrayList<>();
	
	private static SnapshotParameters dragInstanceSnapshotParams = null;
	private static ColorAdjust colorAdjust;
	
    private static final long[] frameTimes = new long[60];
    private static int frameTimeIndex = 0;
    private static boolean arrayFilled = false;
    
    private static boolean mouseAttachedDrag = true;
    
    private static boolean frameRatePrinter = false;
    
	
	public static void initDragDummyArea(Scene scene)
	{
		if (dragInstanceSnapshotParams == null)
		{
		    dragInstanceSnapshotParams = new SnapshotParameters();
		    int height = 100;
		    //int height = (int) ((VisualizableProgrammElement) ActionsSectionManager.getSelf().getRootElementNode().getChildren().get(0).getData()).getControlerOnGUI().getBasePane().getBoundsInLocal().getHeight();
		    dragInstanceSnapshotParams.setFill(Color.TRANSPARENT);
		    dragInstanceSnapshotParams.setViewport(new Rectangle2D(0, 0, 200, height));
		    
			colorAdjust = new ColorAdjust();
			colorAdjust.setContrast(0.3);
			colorAdjust.setSaturation(-0.15);
		}
		
		
		topPane = (Pane) scene.getRoot();
		
		AnimationTimer backgroundCounter = new AnimationTimer() {

	            @Override
	            public void handle(long now) {
	            	frameDur = now-lastFrameTime;
	            	lastFrameTime = now;
	            	currentFactor = frameDur / 16666666f;
	            	currentFrame++;
	            	
	            	
	            	if (frameRatePrinter)
	            	{
		                long oldFrameTime = frameTimes[frameTimeIndex] ;
		                frameTimes[frameTimeIndex] = now;
		                frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length ;
		                if (frameTimeIndex == 0)
		                {
		                    arrayFilled = true;
		                    long elapsedNanos = now - oldFrameTime ;
		                    long elapsedNanosPerFrame = elapsedNanos / frameTimes.length ;
		                    double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame ;
		                    System.out.println("Current frame rate: " + frameRate);
		                }
	            	}
	            }
	        };
			
	        backgroundCounter.start();
	        
	    
	    KeyChecker.addPressedHook(KeyCode.F2, () -> frameRatePrinter = !frameRatePrinter);
	    
        
        new Timer().schedule(
        	    new TimerTask() {
        	        @Override
        	        public void run()
        	        {
        	        	if ((mouseDir<0) == lastMouseVDir)
        	        		mouseVDirCompensated = lastMouseVDir; // Smoothens the binary direction of the mouse
        	        	lastMouseVDir = mouseDir<0;
        	        }
        	    }, 0, 65);
        
        
		scene.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
			relativeMouseX = (int) event.getSceneX();
			relativeMouseY = (int) event.getSceneY();
		});
		scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, event ->{
			relativeMouseX = (int) event.getSceneX();
			relativeMouseY = (int) event.getSceneY();
		});
		
		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event-> mouseDown = true);
		scene.addEventFilter(MouseEvent.MOUSE_RELEASED, event-> mouseDown = false);
		
		topPane.setOnTouchPressed((TouchEvent) -> System.out.println("TOUCHED!!!"));
		
		scene.addEventFilter(DragEvent.ANY, event -> {
			relativeMouseX = (int) event.getSceneX();
			relativeMouseY = (int) event.getSceneY();
			
			if (dragInstance != null)
			{
				if (currentDragDropFrame == currentFrame)
					return;
				currentDragDropFrame = currentFrame;
				
				lastMouseX = mouseX;
				lastMouseY = mouseY;

				//mouseScreenX = (int) event.getScreenX();
				//mouseScreenY = (int) event.getScreenY();

				
				mouseOutside = ((relativeMouseX == 0) && (relativeMouseY == 0));
				
				mouseX = (int) (relativeMouseX+mouseOffsetX);
				mouseY = (int) (relativeMouseY+mouseOffsetY);
				
				
				if (!dragInstance.isVisible())
					dragInstance.setVisible(true);
				
				if (mouseAttachedDrag)
				if (!mouseOutside)
				{
					dragInstance.setTranslateX(Math.max(8, Math.min(ProductionGUI.getStage().getWidth()-205, mouseX)));
					dragInstance.setTranslateY(mouseY);
				}
				else
				{
					dragInstance.setTranslateX(-10000);
			        dragInstance.setTranslateY(-10000);					
				}
		        
				if ((mouseY != lastMouseY) || (mouseX != lastMouseX))
					mouseDir = (float)Math.toDegrees(Math.atan2(mouseY - lastMouseY, mouseX - lastMouseX));
			}
	    });
	}

	public static long getCurrentFrame()
	{
		return(currentFrame);
	}
	public static double getCurrentFactor()
	{
		return(currentFactor);
	}
	public static int getMouseDragX()
	{
		return(mouseX);
	}
	public static int getMouseDragY()
	{
		return(mouseY);
	}
	
	public static boolean mouseIsDown()
	{
		return(mouseDown);
	}
	
	public static int getMouseX()
	{
		return(relativeMouseX);
	}
	public static int getMouseY()
	{
		return(relativeMouseY);
	}
	
	/*
	public static int getScreenMouseX()
	{
		return(mouseScreenX);
	}
	public static int getScreenMouseY()
	{
		return(mouseScreenY);
	}
	*/
	
	
	/**
	 * Assign the drag source
	 *
	 * @param element
	 * @param indentifier
	 * @param payload
	 * @param templatePath
	 * @param templateControlerClass
	 * @param mode
	 * @param dragEvent
	 */
	public static void asignDragSource(Parent element, String indentifier, Object payload, String templatePath, Class<?> templateControlerClass, TransferMode mode, Runnable dragEvent, Runnable endEvent)
	{
		/**
		 * Drag start
		 */
		element.setOnDragDetected(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent event)
		    {
		    	currently_running = true;
		    	
		    	mouseOffsetX = (int) Math.max(Math.min((int) -event.getX(), GlobalSettings.maxDragDropOffsetX), GlobalSettings.minDragDropOffsetX);
		    	mouseOffsetY = (int) Math.max(Math.min((int) -event.getY(), GlobalSettings.maxDragDropOffsetY), GlobalSettings.minDragDropOffsetY) - GlobalSettings.elementsVgapWhole*3;
		    	
				Object templateControl = null;

				try
				{
					Constructor<?> constructor = templateControlerClass.getConstructor(Object.class, boolean.class);

					templateControl = constructor.newInstance(payload, true);
				}
				catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { e.printStackTrace(); }


				origDragInstance = TemplateHandler.injectTemplateDirect(templatePath, templateControl, topPane);
				origDragInstance.setMouseTransparent(true);
				
				topPane.applyCss();
				topPane.layout();
				
				int subWidth = (int) ((Pane)((Pane) origDragInstance).getChildren().get(0)).getWidth();
				
		        ////////
        	    dragInstanceSnapshotParams.setViewport(new Rectangle2D(0, 0, subWidth+16, 100));
        	    WritableImage snapImage = origDragInstance.snapshot(dragInstanceSnapshotParams, null);
				origDragInstance.setVisible(false);
								
        	    topPane.getChildren().remove(origDragInstance);
        	    ImageView img = new ImageView(snapImage);
        	    img.setEffect(colorAdjust);
        	    dragInstance = new Pane(img);
        	    dragInstance.setManaged(false);
        	    dragInstance.toFront();
        	    dragInstance.setMouseTransparent(true);
        	    dragInstance.setVisible(false);
        	    dragInstance.setManaged(false);
        	    topPane.getChildren().add(dragInstance);

                ////////
        	    
        	    mouseAttachedDrag = true;
		        
		        Dragboard db = element.startDragAndDrop(mode);

		        transferPayload = payload;
		        
		        
		        FxTimer.runLater(
		                Duration.ofMillis(500),
		                () -> KeyChecker.cancelAll());
		        

		        if (dragEvent != null)
		        	dragEvent.run();


		        ClipboardContent content = new ClipboardContent();
		        content.putString(indentifier);
		        db.setContent(content);


		        event.consume();
		    }
		});

		/**
		 * Drag end
		 */
		element.setOnDragDone(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event)
			{
				if (!currently_running)
					return;
				
				if (dragInstance != null)
				{
					if (endEvent != null)
						endEvent.run();
					
			        topPane.getChildren().remove(dragInstance);
			        dragInstance = null;
			        
			        mouseAttachedDrag = true;
				}
			}});
	}

	public static void deasignDragSource(Parent element)
	{
		element.setOnDragDetected(null);
		element.setOnDragDone(null);
	}
	

	/**
	 * Assign the drag target
	 *
	 * @param element
	 * @param indentifier
	 * @param targetNormalStyle
	 * @param targetHoverStyle
	 * @param hoverEvent
	 * @param droppedEvent
	 */
	public static void asignDragTarget(Pane element, String indentifier, String targetNormalStyle, String targetHoverStyle, Runnable hoverEvent, Runnable droppedEvent, Runnable outsideEvent)
	{
		asignDragTarget(element, indentifier, targetNormalStyle, targetHoverStyle, null, null, false, hoverEvent, droppedEvent, outsideEvent);
	}
	
	/**
	 * Assign the drag target
	 *
	 * @param element
	 * @param indentifier
	 * @param targetNormalStyle
	 * @param targetHoverStyle
 	 * @param dummyNormalStyle
 	 * @param dummyHoverStyle
 	 * @param applyToChildhoverEvent
	 * @param hoverEvent
	 * @param droppedEvent
	 * @param outsideEvent
	 */
	public static void asignDragTarget(Pane element, String indentifier, String targetNormalStyle, String targetHoverStyle, String dummyNormalStyle, String dummyHoverStyle, boolean applyToChildhoverEvent, Runnable hoverEvent, Runnable droppedEvent, Runnable outsideEvent)
	{
		if (GlobalSettings.applyDragAndDropStyle)
			element.setStyle(targetNormalStyle);
		

		element.setOnDragEntered(new EventHandler<DragEvent>() {
		    public void handle(DragEvent event)
		    {
				if (!currently_running)
					return;
		    	
		    	currentDB = event.getDragboard();
		        if (currentDB.hasString() && ((indentifier==null) || (currentDB.getString().equals(indentifier))))
		        {
		        	
		        	// Remove former style and perform the outside event once
		        	if (GlobalSettings.applyDragAndDropStyle)
		        		element.setStyle(targetNormalStyle);
	            	if (outsideEvent != null)
            			outsideEvent.run();
            			
	            	
		        	// Changes style of the target area
	            	if (GlobalSettings.applyDragAndDropStyle)
	            		element.setStyle(targetHoverStyle);
		        	
	            	// Changes style of the dragged dummy
	            	if (dummyHoverStyle != null)
	            		if (applyToChildhoverEvent)
	            			//dragInstance.getChildrenUnmodifiable().get(0).setStyle(dummyHoverStyle);
            				dragInstance.setStyle(dummyHoverStyle);
	            		else
	            			dragInstance.setStyle("");
	            	
	            	mouseAttachedDrag = true;
		        }
		        
	             //event.consume();
		    }
		});

		element.setOnDragOver(new EventHandler<DragEvent>() {
		    public void handle(DragEvent event)
		    {
				if (!currently_running)
					return;
		    	
		    	currentDB = event.getDragboard();
		        if (currentDB.hasString() && ((indentifier==null) || (currentDB.getString().equals(indentifier))))
		        {
			    	event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			    	if (hoverEvent != null)
			    		hoverEvent.run();
		        }
				//event.consume();
		    }
		});

		element.setOnDragExited(new EventHandler<DragEvent>() {
		    public void handle(DragEvent event)
		    {
				if (!currently_running)
					return;
		    	
		        Dragboard db = event.getDragboard();
		        if (db.hasString() && ((indentifier==null) || (db.getString().equals(indentifier))))
		        {
		        	// Changes style of the target area back
		        	if (GlobalSettings.applyDragAndDropStyle)
		        		element.setStyle(targetNormalStyle);
	            	if (outsideEvent != null)
            			outsideEvent.run();
	            	
	            	mouseAttachedDrag = true;
		        }
	            //event.consume();
		    }
		});
		

		element.setOnDragDropped(new EventHandler<DragEvent>() {
		    public void handle(DragEvent event)
		    {		    	
		    	currentDB = event.getDragboard();
		        if (currentDB.hasString() && ((indentifier==null) || (currentDB.getString().equals(indentifier))))
		        {
		        	if (droppedEvent != null)
		        		droppedEvent.run();
		        	
	            	if (outsideEvent != null)
            			outsideEvent.run();

		        	event.setDropCompleted(true);
		        }
		        else
		        	event.setDropCompleted(false);

		        //event.consume();

		        topPane.getChildren().remove(dragInstance);
		        dragInstance = null;
		    }
		});
	}


	/**
	 * Get the currently dragged instance
	 *
	 * @return
	 */
	public static Object getDragInstance()
	{
		return(dragInstance);
	}
	public static Object getOrigDragInstance()
	{
		return(origDragInstance);//dragInstance);
	}

	/**
	 * Get the payload provided when starting a drag
	 *
	 * @return
	 */
	public static Object getPayload()
	{
		return(transferPayload);
	}

	/**
	 * Get the currently active transfer modes
	 *
	 * @return
	 */
	public static Set<TransferMode> getTransferModes()
	{
		return(currentDB.getTransferModes());
	}
	
	public static Pane getTopPane()
	{
		return(topPane);
	}


	public static float getMouseMovDirection()
	{
		return(mouseDir);
	}
	public static boolean getMouseMovDirectionBinary()
	{
		return(mouseVDirCompensated);
	}
	
	
	public static void addDragDropInteraction(Class source, Class target, int mode)
	{
		dragDropInteractions.add(new DragDropInteraction(source, target, mode));
	}
	

	public static String getPayloadIdentifier()
	{
		if (currentDB == null)
			return("");
		return(currentDB.getString());
	}

	
	
	static class DragDropInteraction
	{
		public Class source;
		public Class target;
		public int mode;
		
		DragDropInteraction(Class source, Class target, int mode)
		{
			this.source = source;
			this.target = target;
			this.mode = mode;
		}
	}


	public static int getDragDropMode(Class source, Class target)
	{
		for(DragDropInteraction inter: dragDropInteractions)
			if (inter.source == source)
				if (inter.target == target)
					return(inter.mode);
		return(0);
	}
	
	
	public static void setSavedPane(Pane savedPane)
	{
		DragAndDropHelper.savedPane = savedPane;
	}
	
	public static Pane getSavedPane()
	{
		return(savedPane);
	}
	
	public static boolean getMouseOutside()
	{
		return(mouseOutside);
	}

	static Map<String, Boolean> attributes = new HashMap<>();
	public static void setAttribute(String ident, boolean val)
	{
		attributes.put(ident, val);
	}
	public static boolean getAttribute(String ident)
	{
		return(attributes.getOrDefault(ident, false));
	}
	public static void clearAttribute(String ident)
	{
		attributes.remove(ident);
	}
	
	public static void setMouseAttachedDrag(boolean attached)
	{
		mouseAttachedDrag = attached;
	}

	public static void abortDragging()
	{
		currently_running = false;
	}
	
}
