package main.functionality.helperControlers.screen;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

import org.reactfx.util.FxTimer;

import execution.Execution;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import settings.GlobalSettings;
import staticHelpers.KeyChecker;
import staticHelpers.OtherHelpers;


public class JFXwindow extends Application
{	
	static int windows = 0;
	
	public static int countExistingWindows()
	{
		return(windows);
	}

	
	static boolean finishedlaunch = false;
	
	static private int tempWidth, tempHeight;
	static private String tempTitleText;
	
	
	static public void tempConstruct(double width, double height, String titleText)
	{
		tempWidth = (int) width;
		tempHeight = (int) height;
		tempTitleText = titleText;
		
		finishedlaunch = false;
	}
	
	
	static JFXwindow newWindow;
	
	public static JFXwindow getTheNewWindow()
	{
		return(newWindow);
	}

	public static boolean hasFinishedLaunch()
	{
		return(finishedlaunch);
	}
	
	
	
	
	
	public Stage primaryStage;
	public Scene scene;
	
	private int width, height;
	private String titleText;
	
	private Pane root;
	
	volatile private boolean open = true;
	
	
	//private Map<Integer, Pane> lastOfLayer = new HashMap<>();
	private Map<Integer, AnchorPane> layers = new HashMap<>();

	
	public JFXwindow()
	{
		this.width = tempWidth;
		this.height = tempHeight;
		this.titleText = tempTitleText;
		
		root = new AnchorPane();
		
		newWindow = this;
		finishedlaunch = false;
	}
	
	public JFXwindow(double width, double height, String titleText)
	{
		this.width = (int) width;
		this.height = (int) height;
		this.titleText = titleText;
		
		root = new AnchorPane();
		
		newWindow = this;
		finishedlaunch = false;
	}
	
	
	
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		windows++;
		
		this.primaryStage = primaryStage;
		
		Execution.addActiveExternalStages(primaryStage, true);
		primaryStage.setOnCloseRequest((WindowEvent) -> {windows--; open = false; Execution.removeActiveExternalStages(primaryStage); });
		
		KeyChecker.initForStage(primaryStage);
		
		if (width < 0 || height < 0)
		{
			scene = new Scene(root);
			primaryStage.setFullScreen(true);
		}
		else
			scene = new Scene(root, width, height);
		
		
		primaryStage.setTitle(titleText);
		
		primaryStage.setScene(scene);
		primaryStage.show();
		
		
		FxTimer.runLater(
		        GlobalSettings.windowToFrontDelay.dividedBy(4),
		        () -> primaryStage.toFront());
		FxTimer.runLater(
		        GlobalSettings.windowToFrontDelay,
		        () -> primaryStage.toFront());
		
		
		finishedlaunch = true;
	}

	
	public void applyItem(JFXitem item, boolean show, int layer)
	{
		OtherHelpers.perform(new FutureTask<Object>(() -> {
			
			if (show)
				item.show(this, layer);
			else
				item.hide();
			
			return(null);
		}));
	}
	
	
	protected void add(Pane element, int layer)
	{
		if (layers.containsKey(layer)) // if the layer already exists
			layers.get(layer).getChildren().add(element); // add to this layer
		else
		{
			AnchorPane newLayer = new AnchorPane();
			
			AnchorPane.setLeftAnchor(newLayer, 0.0);
			AnchorPane.setRightAnchor(newLayer, 0.0);
			AnchorPane.setBottomAnchor(newLayer, 0.0);
			AnchorPane.setTopAnchor(newLayer, 0.0);
			
			
			int ind = 0;
			for(int otherlayers: layers.keySet())
			{
				if (otherlayers > layer)
					break;
				ind++;
			}

			root.getChildren().add(ind, newLayer);
			
			newLayer.getChildren().add(element);
			layers.put(layer, newLayer);
		}
		
	}
	
	protected void remove(Pane element, int layer)
	{
		layers.get(layer).getChildren().remove(element);
	}

	public boolean isOpen()
	{
		return(open);
	}
	
	public void setPositionIfValid(double x, double y)
	{
		if (x >= 0)
			primaryStage.setX(x);
		if (y >= 0)
			primaryStage.setY(y);
	}
	
	public void close()
	{
		if (open)
		{
			windows--;
			open = false;
			Execution.removeActiveExternalStages(primaryStage);
			
			if (!Platform.isFxApplicationThread())
				Platform.runLater(() -> primaryStage.close());
		}
	}


	// For debug
	@Override
	public String toString()
	{
		return("Size: " + scene.getWidth() + " x " + scene.getHeight());
	}




	
	
}
