package otherHelpers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class InitWindow extends Application
{	
	static boolean finishedlaunch = false;

	public static boolean hasFinishedLaunch()
	{
		return(finishedlaunch);
	}
	
	public static Stage primaryStage;
	
	@Override
	public void start(Stage primaryStage) throws Exception // Inivsible window only used for initializing JavaFX to allow the usage of popup-messages. Only used on Windows because it covers the render buffer on the smaller linux versions like Raspbian.
	{
		InitWindow.primaryStage = primaryStage;
		
		primaryStage.setScene(new Scene(new Pane(), 1, 1));
		
		primaryStage.setX(Double.MAX_VALUE);
		primaryStage.setY(Double.MAX_VALUE);
		primaryStage.initStyle(StageStyle.UTILITY);
		
		primaryStage.show();
		
		finishedlaunch = true;
	}
	
	
	public static void close()
	{
		if (!Platform.isFxApplicationThread())
			Platform.runLater(() -> primaryStage.close());
	}












	
	
}
