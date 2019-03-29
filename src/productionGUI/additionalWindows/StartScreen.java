package productionGUI.additionalWindows;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import settings.GlobalSettings;

public class StartScreen extends Application
{
	static Stage primaryStage;
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		StartScreen.primaryStage = primaryStage;
		
		BorderPane content = new BorderPane();
		content.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-background-color: rgba(0, 90, 204, 0.6); -fx-padding: 7; -fx-margin: 0; -fx-fill: white; -fx-text-fill: white; -fx-padding: 50;");
		
		Label topText = new Label("LOADING...");
		topText.setStyle("-fx-font-size: 22; -fx-fill: white; -fx-text-fill: white;");
		BorderPane.setAlignment(topText, Pos.CENTER);
		content.setTop(topText);
		
		Scene scene = new Scene(content);
		
		primaryStage.setTitle(GlobalSettings.titleLineBaseString);

		primaryStage.setScene(scene);
		primaryStage.show();
		
		primaryStage.toFront();
		primaryStage.setAlwaysOnTop(true);
	}
	
	
	public void close()
	{
		primaryStage.close();
	}
	
}
