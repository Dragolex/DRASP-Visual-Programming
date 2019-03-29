package productionGUI.additionalWindows;

import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.ToggleSwitch;
import org.reactfx.util.FxTimer;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import otherHelpers.DragAndDropHelper;
import productionGUI.ProductionGUI;
import productionGUI.tutorialElements.Task;
import settings.GlobalSettings;
import staticHelpers.GuiMsgHelper;
import staticHelpers.InlineFeatures;

public class OverlayMenu
{
	
	Pane basePane;
	GridPane grid;
	
	double parentX, parentY;
	double sourceW = 0, sourceH = 0;
	boolean Xorientation = false;
	boolean Yorientation = false;
	
	boolean fullBelow, fading = false;
	
	Runnable removeAction;
	
	Rectangle attentionRectangle;
	FadeTransition attentionRectangleTransition;
	
	boolean popup = false;
	
	boolean useMinimumSize;
	
	boolean hideOnAnyButton = false;
	
	
	public OverlayMenu(Node source, Runnable removeAction, boolean fadeoutAtClick, boolean fullBelow)
	{		
		this.removeAction = removeAction;
		this.fullBelow = fullBelow;
		
		create(source, fadeoutAtClick, false);
	}
	
	public OverlayMenu(Node source, Runnable removeAction, boolean fadeoutAtClick, boolean fullBelow, boolean useMinimumSize)
	{		
		this.removeAction = removeAction;
		this.fullBelow = fullBelow;
		this.useMinimumSize = useMinimumSize;
	
		create(source, fadeoutAtClick, false);
	}
	
	/*
	public OverlayMenu(Node source, Runnable removeAction, boolean fadeoutAtClick, boolean fullBelow, boolean enforceBottomRight)
	{		
		this.removeAction = removeAction;
		this.fullBelow = fullBelow;
	
		create(source, fadeoutAtClick, enforceBottomRight);
	}
	*/
	
	private void create(Node source, boolean fadeoutAtClick, boolean enforceBottomRight)
	{
		basePane = new AnchorPane();
		basePane.getStyleClass().add("overlayMenuBackground");
		basePane.getStyleClass().add("standardBoldText");
		
		
		if (source != null)
		{
			Bounds sourceBounds = source.localToScene(source.getBoundsInLocal());
					
			sourceW = Math.min(300, sourceBounds.getWidth())/2;
			sourceH = Math.min(300, sourceBounds.getHeight())/2;
			
			parentX = sourceBounds.getMinX()+sourceW;
			parentY = sourceBounds.getMinY()+sourceH;
		
			
			if (parentX < ProductionGUI.getPrimaryScreenBounds().getWidth()/2)
				Xorientation = true;
			else
				Xorientation = false;
			
			if (parentY < ProductionGUI.getPrimaryScreenBounds().getHeight()/2)
				Yorientation = true;
			else
				Yorientation = false;
			
			if (enforceBottomRight)
			{
				Xorientation = true;
				parentY = sourceBounds.getMinY()-2*sourceH + 200;
			}
		}
		else
			popup = true;
		
		
		
		attentionRectangle = new Rectangle(0, 0, 1, 1);
		attentionRectangle.setArcHeight(5);
		attentionRectangle.setArcWidth(5);
		attentionRectangle.setManaged(false);
		attentionRectangle.setMouseTransparent(true);
		
		attentionRectangle.setOpacity(0);		
		
		attentionRectangleTransition = new FadeTransition(GlobalSettings.attentionBlinkDuration, attentionRectangle);
		attentionRectangleTransition.setFromValue(0);
		attentionRectangleTransition.setToValue(GlobalSettings.attentionRectangleMaxAlpha*0.75);
		attentionRectangleTransition.setCycleCount(Timeline.INDEFINITE);
		attentionRectangleTransition.setAutoReverse(true);
		
		
		attentionRectangle.widthProperty().bind(basePane.widthProperty());
		attentionRectangle.heightProperty().bind(basePane.heightProperty());
		
		attentionRectangle.setVisible(false);
		basePane.getChildren().add(0, attentionRectangle);
		
		
		
		basePane.setMaxWidth(100);
		
		
		DragAndDropHelper.getTopPane().getChildren().add(basePane);
		
    	if (fadeoutAtClick)
		FxTimer.runLater(
		        java.time.Duration.ofMillis(250),
		        () -> {
		        	Platform.runLater(() -> {
		        		
		        	ProductionGUI.getStage().requestFocus();
		        		
		    		DragAndDropHelper.getTopPane().addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
		    			if (!basePane.localToScene(basePane.getLayoutBounds()).intersects(evt.getSceneX(), evt.getSceneY(), 1, 1))
			    	    	fade(false);
			    	    
		    	        //if (!GuiMsgHelper.inHierarchy(evt.getPickResult().getIntersectedNode(), basePane))
			    	    //if (!GuiMsgHelper.inHierarchy(evt.getPickResult().getIntersectedNode(), source))
			    	    	//fade(false);
		    	    });	
		    		DragAndDropHelper.getTopPane().addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
		    			if (!basePane.localToScene(basePane.getLayoutBounds()).intersects(evt.getSceneX(), evt.getSceneY(), 1, 1))
			    	    	fade(false);
		    	    });	
		    		DragAndDropHelper.getTopPane().addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
		    			if (!basePane.localToScene(basePane.getLayoutBounds()).intersects(evt.getSceneX(), evt.getSceneY(), 1, 1))
			    	    	fade(false);
		    	    });	
		    		
		        	});
		        });


    	/*
		basePane.setOnDragEntered(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent arg0)
			{
				basePane.setOpacity(0.25);
			}			
		});
		basePane.setOnDragExited(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent arg0)
			{
				basePane.setOpacity(1);
				//basePane.setMouseTransparent(false);
			}			
		});
		*/
    	
    	/*
    	if (false)
    	if (!fadeoutAtClick)
    		if (!popup)
    			basePane.setMouseTransparent(true);
    	*/
		
		
		grid = new GridPane();
		grid.setHgap(30);
		grid.setVgap(8);
		
		grid.setStyle("-fx-padding: 10");
				
		basePane.getChildren().add(grid);
		
		basePane.setOpacity(0);
		fade(true);
		
		
    	basePane.setOnMousePressed((event) -> basePane.toFront());
	}
	
	public void setXorientation(boolean Xorientation)
	{
		this.Xorientation = Xorientation;
	}
	
	public void setTransparent(boolean transparent)
	{
		basePane.setOpacity(transparent ? 0.35 : 1);
		basePane.setMouseTransparent(transparent);
	}
	
	
	Timeline fadeout = null;
	
	public void fade(boolean fadeIn)
	{		
		if (!fadeIn)
			fading = true;		
		
		double frac = 1.0/30.0;
		
		if (fadeout != null)
			fadeout.stop();
		
		fadeout = new Timeline(new KeyFrame(Duration.millis(10),
				new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event)
		    {
		    	if (fadeIn)
		    	{
			    	if (basePane.getOpacity() < 1)
			    		basePane.setOpacity(Math.max(0, basePane.getOpacity() +frac));
		    		
			    	if (basePane.getOpacity() >= 1)
			    	{
			    		basePane.setOpacity(1);
			    		fadeout = null;
			    		return;
			    	}
		    	}
		    	else
		    	{
			    	if (basePane.getOpacity() > 0)
			    		basePane.setOpacity(Math.max(0, basePane.getOpacity() -frac));
		    		
			    	if (basePane.getOpacity() <= 0)
			    	{
			    		closeInstantly();
			    		
			    		fadeout = null;
			    	}
		    	}
		    }
		}));
		fadeout.setCycleCount(33);
		fadeout.play();
	}
	
	public boolean isFading()
	{
		return(fading);
	}
	

	List<Integer> rowCounts = new ArrayList<>();
	
	
	public void addElement(Node element, int column)
	{
		addElement(element, column, false, false);
	}
	
	public void addElement(Node element, int column, boolean rightOriented, boolean directAdd)
	{
		while(rowCounts.size() <= column)
		{
			rowCounts.add(0);
		}
		
		int row = rowCounts.get(column);
		rowCounts.set(column, row+1);
		
		if (rightOriented)
			GridPane.setHalignment(element, HPos.RIGHT);

		element.setStyle("-fx-fill: white;" +
				"	 -fx-text-fill: white; " +
				"    -fx-font-family: \"Arial\";" + 
				"    -fx-font-size: 16;" + 
				"    -fx-font-weight: bold;" + 
				"    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5) , 6, 0, 0, 1 );");
		
		grid.add(element, column, row);
		

		
		if (!directAdd)
		{
			grid.applyCss();
			grid.layout();
	
			basePane.applyCss();
			basePane.layout();
		}
		
		
		double w = 1.15*grid.getWidth();		
		basePane.setMaxHeight(0);
		
		//Pane cont = DragAndDropHelper.getTopPane();
		int originY = (int) (-ProductionGUI.getScene().getHeight()/2 );// + sectionBox.localToScene(sectionBox.getBoundsInLocal()).getMinY() + (sectionBox.getChildren().size()-1)*GlobalSettings.elementsVgapWhole+3);	

		if (popup)
		{
			basePane.setTranslateX(ProductionGUI.getPrimaryScreenBounds().getWidth()/2 - grid.getWidth()/1.25);
			basePane.setTranslateY(originY + ProductionGUI.getPrimaryScreenBounds().getHeight()/2 - grid.getHeight()/2 +100);
		}
		else
		{
			if (Xorientation)
				basePane.setTranslateX(parentX + (!fullBelow ? sourceW : 0));
			else
				basePane.setTranslateX(parentX - (!fullBelow ? sourceW : 0) - w);
					
			if (Yorientation)
				basePane.setTranslateY(originY + parentY - sourceH + (fullBelow ? (2*sourceH + grid.getHeight()/2) : 0));// + grid.getHeight()/4);
			else
				basePane.setTranslateY(originY + parentY - sourceH - (fullBelow ? grid.getHeight()/2 : 0));
			
			basePane.setTranslateY(Math.min(ProductionGUI.getPrimaryScreenBounds().getMaxY()-grid.getHeight()*1.15f, basePane.getTranslateY()));
		}
	}
	
	public void addStandardToggle(String name, boolean startValue, String toolTip, Runnable runFalse, Runnable runTrue)
	{
		addElement(new Text(name), 0);
		ToggleSwitch toggle = new ToggleSwitch();
		toggle.setSelected(startValue);
		
		toggle.setScaleX(GlobalSettings.ToggleScale);
		toggle.setScaleY(GlobalSettings.ToggleScale);
		
		toggle.selectedProperty().addListener(new ChangeListener < Boolean > ()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldval, Boolean newval)
			{
				if (oldval)
					if(!newval)
						runFalse.run();
				if (!oldval)
					if(newval)
						runTrue.run();
			} 
        });
		
		addElement(toggle, 1, true, false);
		
		Node img = GuiMsgHelper.createSizedImage("/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", 20, toolTip);
		img.setTranslateY(4);
		addElement(img, 2);
	}
	
	
	public void addButtonToggle(String name, boolean startValue, String toolTip, Runnable runFalse, Runnable runTrue, String[] buttonsImages, String[] buttonsTooltips, Runnable[] buttonsActions)
	{
		addElement(new Text(name), 0);
		ToggleSwitch toggle = new ToggleSwitch();
		toggle.setSelected(startValue);
		
		toggle.setScaleX(GlobalSettings.ToggleScale);
		toggle.setScaleY(GlobalSettings.ToggleScale);
		
		toggle.selectedProperty().addListener(new ChangeListener < Boolean > ()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldval, Boolean newval)
			{
				if (oldval)
					if(!newval)
						runFalse.run();
				if (!oldval)
					if(newval)
						runTrue.run();
			} 
        });
		
		addElement(toggle, 1, true, false);
		
		int column = 2;
		
		for(int i = 0; i < buttonsActions.length; i++)
		{
			Node img = GuiMsgHelper.createSizedImage(buttonsImages[i*2],buttonsImages[i*2+1], 20, buttonsTooltips[i], buttonsActions[i]);
			img.setTranslateY(4);
			addElement(img, column);
			
			column++;
		}
		
		if (toolTip != null && !toolTip.isEmpty())
		{
			Node img = GuiMsgHelper.createSizedImage("/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", 20, toolTip);
			img.setTranslateY(4);
			addElement(img, column);
		}
	}
	
	

	
	public Label addTextDirect(String text, boolean small)
	{
		Label lb = new Label(text);
		lb.getStyleClass().add(small ? "mediumText" : "standardBoldText");
		//lb.setAlignment(Pos.CENTER);
		lb.setAlignment(Pos.CENTER_LEFT);
		addElement(new StackPane(lb), 0);
		
		if (!small)
			lb.setStyle("-fx-padding: 0 75 0 75 0;");
		
		return(lb);
	}
	
	
	
	public VBox addText(String text, boolean small)
	{
		VBox box = new VBox();
		for(String line: text.split("\\n"))
		{
			Node lb = InlineFeatures.insertSpecialInline(line, small ? "mediumText" : "standardBoldText", small ? Task.tutSubTextSize : Task.tutMainTextSize);
			//lb.setAlignment(Pos.CENTER);
			box.getChildren().add(lb);
		}
		StackPane pn = new StackPane(box);
		pn.setAlignment(Pos.CENTER);
		addElement(pn, 0);
		

		
		//pn.setMinWidth(350);
		
		return(box);
		
		/*
		Label lb = new Label(text);
		lb.getStyleClass().add("standardBoldText");
		lb.setAlignment(Pos.CENTER);
		addElement(new StackPane(lb), 0);
		*/
	}
	
	public void replaceText(VBox box, String text, boolean small, String style)
	{
		if (box == null) return;
		
		box.getChildren().clear();
		
		if (text == null)
			return;
		
		for(String line: text.split("\\n"))
		{
			Node lb = InlineFeatures.insertSpecialInline(line, small ? "mediumText" : "standardBoldText", small ? Task.tutSubTextSize : Task.tutMainTextSize, style);
			
			//lb.setAlignment(Pos.CENTER);
			box.getChildren().add(lb);
		}			
		
	}
	
	
	HBox buttonsHBox;
	public void addButton(String name, Runnable exec)
	{
		addButton(name, exec, true);
	}
	public Button addButton(String name, Runnable exec, boolean inSingleBox)
	{
		if (inSingleBox)
		if (buttonsHBox == null)
		{
			buttonsHBox = new HBox();
			buttonsHBox.setSpacing(10);
			
			int r = rowCounts.size();
			GridPane.setColumnSpan(buttonsHBox, Math.max(1, r));
			
			addElement(buttonsHBox, 0);
		}
		
		Button bt = new Button(name);
		bt.getStyleClass().add("mainButtons");
		bt.getStyleClass().add("standardBoldText");
		bt.setMaxWidth(Double.MAX_VALUE);
		bt.setOnAction((ActionEvent) -> { if (hideOnAnyButton) fade(false); exec.run();});
		
		if (inSingleBox)
		{
			buttonsHBox.getChildren().add(bt);
			HBox.setHgrow(bt, Priority.ALWAYS);
		}
		else
			addElement(bt, 0);
		
		return(bt);
	}
	
	
	
	public void addSeparator(String color)
	{
		Separator sep = new Separator();
		sep.setOrientation(Orientation.HORIZONTAL);
		
		int r = rowCounts.size();
		GridPane.setColumnSpan(sep, Math.max(1, r));
		
		for(int i = 1; i < r; i++)
		{
			int row = rowCounts.get(i);
			rowCounts.set(i, row+1);
		}
		
		addElement(sep, 0);
		
		sep.getChildrenUnmodifiable().get(0).setStyle("-fx-border-color: " + color);
	}
	
	
	public void highlightAndFadeout()
	{
		attentionRectangle.setVisible(true);
		
		attentionRectangle.setFill(Color.LIME);
		
		attentionRectangleTransition.stop();
		attentionRectangleTransition.setDuration(GlobalSettings.attentionBlinkDurationFast);
		attentionRectangleTransition.setCycleCount(2);
		attentionRectangleTransition.play();
		
		FxTimer.runLater(
		        java.time.Duration.ofMillis(350),
		        () -> fade(false));
		
		
		//attentionRectangleTransition.setOnFinished((ActionEvent event) -> fade(false));
	}
	
	
	public void highlightBad()
	{
		attentionRectangle.setVisible(true);
		
		attentionRectangle.setFill(Color.RED);
		
		attentionRectangleTransition.stop();
		attentionRectangleTransition.setDuration(GlobalSettings.attentionBlinkDurationFast);
		attentionRectangleTransition.setCycleCount(4);
		attentionRectangleTransition.play();
		
		attentionRectangleTransition.setOnFinished((ActionEvent event) -> attentionRectangle.setVisible(false));
	}
	
	
	private List<Node> cornerButtons = new ArrayList<>();
	
	public void addCornerButton(String text, Runnable task, int indexFromRight, String img1, String img2, boolean visible)
	{
		Node bt = GuiMsgHelper.createSizedImage(img1, img2, 25, text);
		bt.setOnMouseClicked((MouseEvent) -> task.run());

		basePane.getChildren().add(bt);
		if (indexFromRight < 0)
			bt.setTranslateX(9-(indexFromRight+1)*35);
		else
			bt.translateXProperty().bind(basePane.widthProperty().subtract(35+indexFromRight*35));
		bt.setTranslateY(10);
		
		
		cornerButtons.add(bt);
		
		bt.setMouseTransparent(!visible);
		bt.setOpacity(visible ? 1 : 0.5);

		
		/*
		Button bt = new Button("", GuiMsgHelper.createSizedImage("/guiGraphics/if_close.png", "/guiGraphics/if_close_hover.png", 25, text));
		
		bt.setOnAction((ActionEvent) -> task.run());
		
		basePane.getChildren().add(bt);
		//bt.setManaged(false);
		bt.translateXProperty().bind(basePane.widthProperty().subtract(20));
		bt.setTranslateY(20);
		*/
	}
	
	public void setCornerButtonState(int indexFromRight, boolean visible)
	{
		setElementState(cornerButtons.get(indexFromRight), visible);
	}

	public void fixWidth(int width)
	{
		basePane.setPrefWidth(width);
		basePane.setMinWidth(width);
		basePane.setMaxWidth(width);
	}
	
	
	double dragOrigX = 0;
	double dragOrigY = 0;
	double dragOrigOffsX = 0;
	double dragOrigOffsY = 0;
	Timeline draggingUpdater = null;
	
	int border = 65;
	
	public void makeDraggable()
	{
		basePane.setOnMousePressed((event) ->{
			
			basePane.toFront();

			if (event.getY() < 40)
			{
				dragOrigOffsX = basePane.getTranslateX();
				dragOrigOffsY = basePane.getTranslateY();
				dragOrigX = DragAndDropHelper.getMouseX();// event.getX();
				dragOrigY = DragAndDropHelper.getMouseY();// event.getY();
				
				if (draggingUpdater == null)
				{
					draggingUpdater = new Timeline(new KeyFrame(Duration.millis(16), new EventHandler<ActionEvent>() {
	
					    @Override
					    public void handle(ActionEvent event) {

								int targX = DragAndDropHelper.getMouseX();
								int targY = DragAndDropHelper.getMouseY();
								
								if ((targX > border) && (targX < ProductionGUI.getStage().getWidth()-border))
									basePane.setTranslateX(dragOrigOffsX + (targX - dragOrigX) );
									
								if ((targY > border) && (targY < ProductionGUI.getStage().getHeight()-border))
									basePane.setTranslateY(dragOrigOffsY + (targY - dragOrigY) );
								
					    }
					}));
					draggingUpdater.setCycleCount(Timeline.INDEFINITE);
					draggingUpdater.play();
				}
				else
					draggingUpdater.play();

			}});
		basePane.setOnMouseReleased((event) ->{if (draggingUpdater != null) draggingUpdater.stop();});

	}
	public void setRemoveAction(Runnable removeAction)
	{
		this.removeAction = removeAction;
	}
	
	boolean resT = false;
	boolean resR = false;
	boolean resB = false;
	boolean resL = false;
	int refMouse = 0;
	
	public void makeResizable(int borderSize, Pane root)
	{
		basePane.setOnMouseDragged((event) -> {
			
			if (resT)
			{
				
			}
			
			if (resL)
			{
				
			}
			
			if (resR)
			{
				grid.setMaxWidth(grid.getWidth() - (refMouse-event.getX()));
				basePane.setMaxWidth(grid.getWidth() - (refMouse-event.getX()));  // TODO
				
				root.setMaxWidth(grid.getWidth() - (refMouse-event.getX())-20);
			}

			if (resB)
				grid.setMaxHeight(grid.getHeight() - (refMouse-event.getY()));
			
			
		});
		
		
		basePane.setOnMouseMoved((event) -> {
			
			ProductionGUI.getScene().setCursor(Cursor.DEFAULT);
			
			if (event.getX() < borderSize)
				ProductionGUI.getScene().setCursor(Cursor.W_RESIZE);
			if (event.getY() < borderSize)
				ProductionGUI.getScene().setCursor(Cursor.N_RESIZE);
			if (event.getX() > basePane.getWidth()-borderSize)
				ProductionGUI.getScene().setCursor(Cursor.E_RESIZE);
			if (event.getY() > basePane.getHeight()-borderSize)
				ProductionGUI.getScene().setCursor(Cursor.S_RESIZE);
			
		});
		

		basePane.addEventFilter(MouseEvent.MOUSE_PRESSED, (event) -> {
			
			ProductionGUI.getScene().setCursor(Cursor.DEFAULT);
			
			if (event.getX() < borderSize)
			{
				if(!resL)
					refMouse = (int) event.getY();
				resL = true;
			}
			else
			if (event.getY() < borderSize)
			{
				if (!resT)
					refMouse = (int) event.getY();
				resT = true;
			}
			else
			if (event.getX() > basePane.getWidth()-borderSize)
			{
				if(!resR)
					refMouse = (int) event.getX();
				resR = true;
			}
			else
			if (event.getY() > basePane.getHeight()-borderSize)
			{
				if(!resB)
					refMouse = (int) event.getY();
				resB = true;
			}
			else
			{
				resT = false;
				resR = false;
				resB = false;
				resL = false;
			}
			
			
		});
		
		basePane.setOnMouseExited((event)-> ProductionGUI.getScene().setCursor(Cursor.DEFAULT));
		
	}
	
	
	boolean minimized = false;
	double origWidth;
	
	public void flipMinimize(Pane root, int minWidth)
	{
		if (minimized)
		{
			deMinimize(root);
		}
		else
		{
			origWidth = basePane.getMinWidth();
			basePane.setMinWidth(minWidth);
			root.setManaged(false);
			root.setVisible(false);
			basePane.setTranslateY(basePane.getTranslateY()-root.getHeight()/2);
			
			minimized = true;
		}		
	}

	public void deMinimize(Pane root)
	{
		if (minimized)
		{
			basePane.setMinWidth(origWidth);
			root.setManaged(true);
			root.setVisible(true);
			
			basePane.setTranslateY(basePane.getTranslateY()+root.getHeight()/2);
		}
		
		minimized = false;
	}

	public void shiftPosition(double x, double y)
	{
		basePane.setTranslateX(basePane.getTranslateX() + x);
		basePane.setTranslateY(basePane.getTranslateY() + y);
	}

	public void setPosition(int windowX, int windowY)
	{
		basePane.setTranslateX(windowX);
		basePane.setTranslateY(windowY -ProductionGUI.getScene().getHeight()/2);
	}

	public void closeInstantly()
	{
		if (DragAndDropHelper.getTopPane().getChildren().contains(basePane))
			DragAndDropHelper.getTopPane().getChildren().remove(basePane);

    	if (removeAction != null)
    	{
    		removeAction.run();
    		removeAction = null;
    	}		
	}
	
	public void setOpacity(double opacity)
	{
		basePane.setOpacity(opacity);
	}

	public static void setElementState(Node node, boolean active)
	{
		node.setMouseTransparent(!active);
		node.setOpacity(active ? 1 : 0.5);		
	}

	public void applyHideOnAnyButton()
	{
		hideOnAnyButton = true;
	}
}
