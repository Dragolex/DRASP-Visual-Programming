package productionGUI.additionalWindows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import execution.Execution;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import productionGUI.ProductionGUI;
import productionGUI.sections.elementManagers.ActionsSectionManager;
import settings.GlobalSettings;

public class DataConsole extends PopableWindow
{
	private static DataConsole self;
	private static volatile boolean isReady = false;
	
	private boolean lockedToBottom = true;
	private double lastvPos = 0;
	private int lastNodesSize = 0;
	
	
	public DataConsole(String title)
	{
		super(title, (int) (GlobalSettings.extraWindowWidth*1.5), (int) (GlobalSettings.extraWindowHeight/1),
				(int) ActionsSectionManager.getSelf().getSectionManager().getScrollPane().getViewportBounds().getWidth()+30, (int) ProductionGUI.getScene().getHeight() - (int) (GlobalSettings.extraWindowHeight/1)/2 - 64,
				"/productionGUI/additionalWindows/ScrollableWindow.fxml");
		
		
        scrollPane.setVvalue(scrollPane.getVmax());
        scrollPane.vvalueProperty().addListener(new ChangeListener<Number>() 
        {
          @Override
          public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) 
          {
        	  
        	  double v = scrollPane.getVvalue();
        	  if (v == 1) return;        	  
        	  
        	  if (nodes.size()==lastNodesSize)
        	  if (v < lastvPos)
        		  lockedToBottom = false;
        	  
        	  lastNodesSize = nodes.size();
        	  
        	  lastvPos = v;
          }
        });
        
	}
	
	
	public void onScroll()
	{
  	  if (scrollPane.getVvalue() > 0)
		  lockedToBottom = true;
	}
	
	
	
	
	@FXML GridPane mainPane;
	@FXML Label titleText;
	@FXML ScrollPane scrollPane;
	@FXML VBox contentPane;
	
	private ObservableList<Node> nodes;
	
	@FXML
    public void initialize()
    {
		self = this;
		
		nodes = contentPane.getChildren();
		
		
		contentPane.prefWidthProperty().bind(mainPane.widthProperty().subtract(20));
		contentPane.prefHeightProperty().bind(mainPane.heightProperty().subtract(20));

		
		isReady = true;
    }
	
	public void addConstantStreamLines(InputStream stream, String style)
	{
		//if (stream == null) return;
		
		new Thread() {
			public void run()
			{
		    	InputStreamReader inputStreamReader = new InputStreamReader(stream);
			    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			    
			    String line = null;
			    try
			    {
				    while ((line = bufferedReader.readLine()) != null)
				    {
				    	if (Execution.specialInterpretLine(line))
					    	if (style.isEmpty())
					    		externallyAddLine(line);
					    	else
					    		externallyAddLine(line).setStyle(style);
				    }
			    } catch (IOException e) { e.printStackTrace(); }
			    
			}}.start();
	}
	
	
	public Text externallyAddLine(String message)
	{		
		if (!isVisible()) return(new Text(message));
		
    	Text tx = new Text(message);
    	tx.getStyleClass().add("standardBoldText");
    	
    	if (message.length() > 3)
    	{
    		switch(message.substring(0, 3))
    		{
    		case GlobalSettings.errorSymbol: tx.setStyle("-fx-fill: CRIMSON;"); tx.setText(tx.getText().substring(3)); break;
    		case GlobalSettings.execInfoSymbol: tx.setStyle("-fx-fill: CHARTREUSE;"); tx.setText(tx.getText().substring(3)); break;
    		case GlobalSettings.envSymbol: tx.setStyle("-fx-fill: DARKBLUE;"); tx.setText(tx.getText().substring(3)); break;
    		case GlobalSettings.minorErrorSymbol: tx.setStyle("-fx-fill: GOLD;"); tx.setText(tx.getText().substring(3)); break;
    		}
    	}
    	
    	Platform.runLater(() -> {
	        nodes.add(tx);
	    	if (nodes.size() > GlobalSettings.maxConsoleNodes)
	    		nodes.remove(0, GlobalSettings.maxConsoleNodes/2);
	    	
	    	if (lockedToBottom)
	    		Platform.runLater(() -> scrollPane.setVvalue(scrollPane.getVmax()));
    	});    	
    	
    	return(tx);
	}
	
	
	public void setSubtitle(String title)
	{
		titleText.setText(title);
	}

	
	public void reset()
	{
		toFront();
		nodes.clear();
	}
	
	
	
	public boolean isReady()
	{
		return(isReady);
	}

	
	public static DataConsole getSelf()
	{
		return(self);
	}


	
}
