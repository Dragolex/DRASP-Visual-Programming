package productionGUI.controlers;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ScrollingWindowEmptyControler extends Application
{
	Stage stage;
	
	Pane contentPaneExt;
	
	@FXML VBox contentPane;
	@FXML HBox titleBox;
	@FXML Label titleText;
	@FXML ScrollPane scrollPane;
	@FXML GridPane mainPane;
	@FXML BorderPane borderPane;
	
	String title = "";
	
	public ScrollingWindowEmptyControler(Stage stage, Pane contentPaneExt, String title)
	{
		this.contentPaneExt = contentPaneExt;
		this.title = title;
		this.stage = stage;
	}
	
	
	@Override
	public void start(Stage stage) throws Exception
	{
	}
	
	@FXML
    public void initialize()
    {		
		if (!title.isEmpty())
			titleText.setText(title);
		else
		{
			titleBox.setVisible(false);
			titleBox.setManaged(false);
			
			scrollPane.getStyleClass().remove("sectionBorder");
			scrollPane.getStyleClass().add("sectionBorderFull");
			
			stage.setAlwaysOnTop(true);
		}
		
		
		contentPane.getChildren().add(contentPaneExt);
		
		
		contentPaneExt.prefWidthProperty().bind(mainPane.widthProperty().subtract(20));
		contentPaneExt.prefHeightProperty().bind(mainPane.heightProperty().subtract(20));
		
		
		//scrollPane.prefWidthProperty().bind(mainPane.widthProperty());
		
		/*
		borderPane.prefWidthProperty().bind(mainPane.widthProperty());
		contentPaneExt.prefWidthProperty().bind(scrollPane.widthProperty().subtract(93));

		
		borderPane.prefHeightProperty().bind(mainPane.heightProperty());
		contentPaneExt.prefHeightProperty().bind(scrollPane.heightProperty().subtract(140));
		*/
		
    }
	
	@FXML
	public void onScroll()
	{
	}

}
