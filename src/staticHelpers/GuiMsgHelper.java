package staticHelpers;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.function.UnaryOperator;

import org.reactfx.util.FxTimer;

import dataTypes.SimpleTable;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import productionGUI.ProductionGUI;
import settings.GlobalSettings;

/**
 * This class defines a couple of JavaFX dialog boxes.
 *
 * @author Alexander Georgescu
 */
public class GuiMsgHelper
{
	
	final static int keyCodeType = 105;
	
	
	/*
	 * Show dialogue to ask question.
	 *
	 * @param text Question text.
	 * @param options Array with options.
	 * @return Index of option chosen or -1 if aborted.
	 */
	public static int askQuestion(String text, Object[] options, boolean includeCancel)
	{
		return((int) OtherHelpers.perform(new FutureTask<Object>(() -> 
		{
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Chose");
			alert.setHeaderText(null);
			alert.setContentText(StringHelpers.resolveArgNewline(text)+"\n");
			alert.getButtonTypes().clear();
			
			ButtonType[] buttons = new ButtonType[options.length];
	
			int i = 0;
			for(Object str: options)
			{
				buttons[i] = new ButtonType((String) str);
				alert.getButtonTypes().add(buttons[i++]);
			}
			if (includeCancel)
				alert.getButtonTypes().add(new ButtonType("Cancel", ButtonData.CANCEL_CLOSE));
	
	
			styleAlert(alert);

			Optional<ButtonType> result = alert.showAndWait();
			
			ProductionGUI.toFront();
	
			int j = 0;
			for(ButtonType bt: buttons)
			{
				if (result.get() == bt)
					return(j);
				j++;
			}
	
			return(-1);
		})));
	}
	

	/**
	 * Get an user input
	 *
	 * @param message
	 * @return
	 *//*
	public static String getText(String message)
	{
		return(getText(message, ""));
	}
*/
	/**
	 * Get an user input
	 *
	 * @param message
	 * @param initvalue
	 * @return
	 */
	/*
	public static String getText(String message, String initvalue)
	{
		TextInputDialog dialog = new TextInputDialog(initvalue);

		dialog.setTitle("Input text");
		dialog.setHeaderText(message);
		dialog.setContentText("");

		Optional<String> result = dialog.showAndWait();

		if (result.isPresent())
		   return(result.get());

		return(null);
	}
	*/

	/**
	 * Show a message
	 *
	 * @param message
	 */
	/*
	public static void showMessage(String message)
	{
		OtherHelpers.perform(new FutureTask<Object>(() -> {
			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("");
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
			
			styleAlert(alert);

			return(true);
		}));			
	}
	*/
	
	
	public static void showInfoMessage(String message)
	{
		InfoErrorHandler.printDirectMessage("DISPLAY INFO MESSAGE: " + message);
		
		if (!Execution.JFXgraphicsAvailable())
			return;
		
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("");
		alert.setHeaderText(null);
		alert.setContentText(StringHelpers.resolveArgNewline(message));
		styleAlert(alert);
		alert.showAndWait();
		
		ProductionGUI.toFront();
	}
	
	

	static private int openedMessages = 0;
	
	
	public static void showMessageNonblockingUI(String message)
	{
		showNonblockingUI(message, -1, null, null);
	}
	
	
	public static Double getNumberNonblockingUI(String message, Double sampleValue, boolean showCancel)
	{
		return((Double) showNonblockingUI(message, Variable.doubleType, sampleValue, null, showCancel));
	}
	public static Double getNumberNonblockingUI(String message, Double sampleValue)
	{
		return((Double) showNonblockingUI(message, Variable.doubleType, sampleValue, null, true));
	}
	public static Double getNumberNonblockingUI(String message, int sampleValue)
	{
		return((Double) showNonblockingUI(message, Variable.doubleType, Double.valueOf(sampleValue), null));
	}


	public static String getTextNonblockingUI(String message, String sampleValue, boolean showCancel)
	{
		return((String) showNonblockingUI(message, Variable.textType, sampleValue, null, showCancel));
	}
	public static String getTextNonblockingUI(String message, String sampleValue)
	{
		return((String) showNonblockingUI(message, Variable.textType, sampleValue, null, true));
	}
	
	
	private final static int staticTableMessageType = 101;
	private final static int dynamicTableMessageType = 102;
	
	
	public static int showNonblockingUIWithTable(String msg, SimpleTable variableTable, String[] results)
	{
		Object res = showNonblockingUI(msg, staticTableMessageType, variableTable, results);		
		if (res == null) return(-1);
		return((int) res);
	}
	public static int showNonblockingUIWithTableDynamic(String msg, SimpleTable variableTable, String[] results)
	{
		Object res = showNonblockingUI(msg, dynamicTableMessageType, variableTable, results);		
		if (res == null) return(-1);
		return((int) res);
	}
	
	/*
	private static Object __showNonblockingUI(String message, int type, Object sampleValue, String[] results)
	{
		if ((!Execution.JFXgraphicsAvailable() && !Execution.isRunningInGUI()) || !Execution.moreActiveStagesPossible() || (openedMessages > GlobalSettings.maxPossibleExternalStages))
		{
			InfoErrorHandler.printDirectMessage("POPUP MESSAGE: \n" + message + "\nPOPUP MESSAGE END");
			return(null);
		}
		
		Execution.print("Popup text: " + message);		
		
		openedMessages++;
		
		int[] state = new int[1];
		state[0] = 0;
		
		Stage[] stage = new Stage[1];
		
		TextField field = new TextField("");
		if (sampleValue != null)
			field.setText(sampleValue.toString()); // output field (not always needed)
		
		
		boolean[] canceled = new boolean[1];
		canceled[0] = true;
		
		Object[] resultCarrier = new Object[1];
		resultCarrier[0] = -1;
		
		
		OtherHelpers.perform(new FutureTask<Object>(() -> {
			
			/*
			stage[0] = new Stage();
		    //stage[0].setTitle(title);
		    stage[0].setScene(new Scene(root, ProductionGUI.getPrimaryScreenBounds().getWidth()/2, ProductionGUI.getPrimaryScreenBounds().getHeight()/2));
		    //stage[0].setX(50);
		    //stage[0].setY(200);
		    stage[0].show();
		    * /
			
		    String title = "";//"PopUp Message";
		    
			
			StackPane contentPane = new StackPane();
			
			//contentPane.getStyleClass().add("messageBackground");
			
			
			GridPane grid = new GridPane();
			contentPane.getChildren().add(grid);
			grid.setMaxWidth(Double.MAX_VALUE);
			grid.setMaxHeight(Double.MAX_VALUE);
			
			//contentPane.minWidthProperty().bind(grid.widthProperty());
			
			//grid.prefWidthProperty().bind(contentPane.widthProperty());
			//grid.prefHeightProperty().bind(contentPane.heightProperty());
			
			
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(10));
			
			
			Label text = new Label("ABCD\nsddsds\nsakljasdl"); //message);
			text.setTextAlignment(TextAlignment.CENTER);
			text.getStyleClass().add("standardBoldText");
			
			Button bnOK = newButton("OK");
			
			
			switch(type)
			{
			case -1:
				grid.add(text, 0, 0);
				grid.add(bnOK, 0, 2);
				
				break;
				
			case Variable.doubleType:
				
				UnaryOperator<Change> integerFilter = change -> {
				    String newText = change.getControlNewText();
				    //if (newText.matches("-?([1-9][0-9]*)?")) { // INT
					if (newText.matches("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?"))				    	
				        return change;
				    return null;
				};

				field.setTextFormatter(
				    new TextFormatter<Double>(new DoubleStringConverter(), Double.valueOf(field.getText()), integerFilter)); // Enable the filter for doubles
				// No break statement
				
			case Variable.textType:				
				Button bnC = newButton("Cancel");
				
				grid.add(text, 0, 0, 2, 1);
				grid.add(field, 0, 1, 2, 1);
				grid.add(bnOK, 0, 3, 1, 1);
				grid.add(bnC, 1, 3, 1, 1);
				
				bnC.minWidthProperty().bind(bnOK.minWidthProperty());
				
				bnC.setOnAction((e)-> {
					stage[0].close();
				});
				
				break;
				
			case tableMessageQuestion:
				SimpleTable<String> table = (SimpleTable<String>) sampleValue;
								
				int resCount = Math.max(1, results.length);
				
				//Label textLb = new Label(text.getText());
				grid.add(text, 0, 0, resCount, 1);
				
				Pane tablePane = table.visualizeTable("msgTableBox", "msgHeaderBackground", "msgElementBackground", "tableElementsLabelHeader", "tableElementsLabel");
				tablePane.setMaxWidth(Double.MAX_VALUE);
				
				tablePane.prefWidthProperty().bind(contentPane.widthProperty().subtract(20));
				
				grid.add(tablePane, 0, 1, resCount, 1);
				
				
				ColumnConstraints cc = new ColumnConstraints();
				cc.setPercentWidth(100.0/results.length);
				
				if (results.length == 0)
					grid.add(bnOK, 0, 2, resCount, 1);
				else
				{
					int[] ind = new int[1];
					ind[0] = 0;
					for(String str: results)
					{
						Button bt = newButton(str);
						bt.setMaxWidth(Double.MAX_VALUE);
						grid.add(bt, ind[0], 2, 1, 1);		
						
						grid.getColumnConstraints().add(cc);
						
						int i = ind[0];
						bt.setOnAction((e)-> {
							resultCarrier[0] = i;
							canceled[0] = false;
							stage[0].close();
						});
						ind[0]++;
					}
				}
				break;
			}
			
			
			
			//grid.setMinWidth(200);
			bnOK.setMinWidth(100);
			
			GridPane.setHalignment(bnOK, HPos.CENTER);
			GridPane.setHalignment(text, HPos.CENTER);
			GridPane.setValignment(bnOK, VPos.CENTER);
			GridPane.setValignment(text, VPos.CENTER);
			
			
			stage[0] = new Stage();
			
		    FXMLLoader loader = new FXMLLoader(SettingsMenu.class.getResource("/productionGUI/additionalWindows/ScrollableWindow.fxml"));
		    loader.setController(new ScrollingWindowEmptyControler(stage[0], contentPane, title));
		    Pane root = null;
			try {
				root = loader.load();
			} catch (IOException e) {e.printStackTrace();}
		    
			
			
			if (!TestOS.isWindows())
				root.setBorder(new Border(new BorderStroke(Color.BLACK, 
						BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			
			
			root.setMaxHeight(GlobalSettings.maxMessageHeight);
			
			Scene dialog = new Scene(root, 500, 250);
			
			stage[0].setScene(dialog);
			
			bnOK.setOnAction((e)-> {
				canceled[0] = false;
				stage[0].close();
			});



			stage[0].show();
			stage[0].toFront();
			state[0] = 1;
			
			return(true);
		}));
		
		
		while(true)
		{
			if (state[0] == 1)
			{
				if (Execution.isRunning())
					Execution.addActiveExternalStages(stage[0], false);
				
				
				while(stage[0].isShowing()) {try { Thread.sleep(GlobalSettings.constantCheckDelay);} catch (InterruptedException e) {}}

				
				Execution.removeActiveExternalStages(stage[0]);
				
				openedMessages--;

				
				String tx = field.getText();
				
				if (canceled[0])
					return(null);
				
				switch(type)
				{
				case -1:
					return(null);
					
				case Variable.doubleType:
					if (tx.isEmpty())
						return(0);
					else
						return(Double.valueOf(tx));
					
				case Variable.textType:
					return(tx);
					
				case tableMessageQuestion:
					return(resultCarrier[0]);
				}
			}
			
			try { Thread.sleep(GlobalSettings.constantCheckDelay);} catch (InterruptedException e) {}
			
		}
	}
	*/
	
	public static KeyCode getAnyKeyNonblockingUI(String message, KeyCode keyCode)
	{
		return((KeyCode) showNonblockingUI(message, keyCodeType, keyCode, new String[] {""}, false));
	}
	
	
	private static Object showNonblockingUI(String message, int type, Object sampleValue, String[] results)
	{
		return(showNonblockingUI(message, type, sampleValue, results, true));
	}
	
	private static Object showNonblockingUI(String messageTxt, int type, Object sampleValue, String[] results, boolean showCancel)
	{
		String message = StringHelpers.resolveArgNewline(messageTxt);
		
		if ((!Execution.JFXgraphicsAvailable() && !Execution.isRunningInGUI()) || !Execution.moreActiveStagesPossible() || (openedMessages > GlobalSettings.maxPossibleExternalStages))
		{
			InfoErrorHandler.printDirectMessage("POPUP MESSAGE: \n" + message + "\nPOPUP MESSAGE END");
			return(null);
		}
		
		Execution.print("Popup text: " + message);		
		
		openedMessages++;
		
		int[] state = new int[1];
		state[0] = 0;
		
		Stage[] stage = new Stage[1];
		
		TextField field = new TextField("");
		if (sampleValue != null)
			field.setText(sampleValue.toString()); // output field (not always needed)
		
		
		boolean[] canceled = new boolean[1];
		canceled[0] = true;
		
		Object[] resultCarrier = new Object[1];
		resultCarrier[0] = -1;
		
		
		OtherHelpers.perform(new FutureTask<Object>(() -> {
			
			Pane contentPane = new Pane();
			
			contentPane.getStyleClass().add("messageBackground");
			
			
			
			if (!LocationPreparator.isWindows())
				contentPane.setBorder(new Border(new BorderStroke(Color.BLACK, 
			            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			
			GridPane grid = new GridPane();
			contentPane.getChildren().add(grid);
			
			
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(10));
			
			boolean okBound = false;
			
			Label text = new Label(message);
			text.getStyleClass().add("standardBoldText");
			
			Button bnOK = newButton("OK");
			
			boolean dynamic = false;
			
			switch(type)
			{
			case -1:
				grid.add(text, 0, 0);
				grid.add(bnOK, 0, 2);
				text.minWidthProperty().bind(bnOK.minWidthProperty());
				okBound = true;
				
				break;
				
			case Variable.doubleType:
				
				UnaryOperator<Change> integerFilter = change -> {
				    String newText = change.getControlNewText();
				    //if (newText.matches("-?([1-9][0-9]*)?")) { // INT
					if (newText.matches("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?"))				    	
				        return change;
				    return null;
				};

				field.setTextFormatter(
				    new TextFormatter<Double>(new DoubleStringConverter(), Double.valueOf(field.getText()), integerFilter)); // Enable the filter for doubles
				
				// No break statement!
				
			case Variable.textType:
				
				grid.add(text, 0, 0, 2, 1);
				grid.add(field, 0, 1, 2, 1);
				
				if (showCancel)
				{
					Button bnC = newButton("Cancel");

					grid.add(bnOK, 0, 3, 1, 1);
					grid.add(bnC, 1, 3, 1, 1);
					
					bnC.minWidthProperty().bind(bnOK.minWidthProperty());
					okBound = true;
					
					bnC.setOnAction((e)-> {
						stage[0].close();
					});
				}
				else
					grid.add(bnOK, 0, 3, 2, 1);
				
				break;
			
			case dynamicTableMessageType:
				dynamic = true;
				
			case staticTableMessageType:
				SimpleTable<String> table = (SimpleTable<String>) sampleValue;
								
				int resCount = Math.max(1, results.length);
				
				//Label textLb = new Label(text.getText());
				grid.add(text, 0, 0, resCount, 1);
				
				Pane tablePane = table.visualizeTable("msgTableBox", "msgHeaderBackground", "msgElementBackground", "tableElementsLabelHeader", "tableElementsLabel");
				tablePane.setMaxWidth(Double.MAX_VALUE);
				
				tablePane.prefWidthProperty().bind(contentPane.widthProperty().subtract(20));
				
				if (!dynamic)
					grid.add(tablePane, 0, 1, resCount, 1);
				else
				{
					ScrollPane scrl = new ScrollPane(tablePane);
					scrl.setMinHeight(120);
					scrl.setMaxHeight(120);
					scrl.getStyleClass().add("transparentScrollPane");
					scrl.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
					scrl.setFitToWidth(true);

					grid.add(scrl, 0, 1, resCount, 1);
				}
				
				
				ColumnConstraints cc = new ColumnConstraints();
				cc.setPercentWidth(100.0/results.length);
				
				if (results.length == 0)
					grid.add(bnOK, 0, 2, resCount, 1);
				else
				{
					int[] ind = new int[1];
					ind[0] = 0;
					for(String str: results)
					{
						Button bt = newButton(str);
						bt.setMaxWidth(Double.MAX_VALUE);
						grid.add(bt, ind[0], 2, 1, 1);		
						
						grid.getColumnConstraints().add(cc);
						
						int i = ind[0];
						bt.setOnAction((e)-> {
							resultCarrier[0] = i;
							canceled[0] = false;
							stage[0].close();
						});
						ind[0]++;
					}
				}
				break;
				
			case keyCodeType:
				
				KeyChecker.clearLastKey();
				
				resultCarrier[0] = sampleValue;
				
				grid.add(text, 0, 0, 3, 1);
				
				Label[] resTx = new Label[1];
				if (sampleValue == null)
					resTx[0] = new Label("Key: ");
				else
					resTx[0] = new Label("Key: " + ((KeyCode)sampleValue).getName());
				
				resTx[0].getStyleClass().add("standardBoldText");
				grid.add(resTx[0], 0, 1, 3, 1);

				Button bnCl = newButton("Clear");
				Button bnC = newButton("Cancel");
				
				grid.add(bnOK, 0, 3, 1, 1);
				grid.add(bnCl, 1, 3, 1, 1);
				grid.add(bnC, 2, 3, 1, 1);
				
				
				bnOK.minWidthProperty().bindBidirectional(bnCl.minWidthProperty());
				bnC.minWidthProperty().bindBidirectional(bnOK.minWidthProperty());
				okBound = true;
					
				bnCl.setOnAction((e)-> {
					canceled[0] = false;
					resultCarrier[0] = KeyCode.DEAD_BREVE;
					stage[0].close();
				});
				bnC.setOnAction((e)-> {
					canceled[0] = false;
					resultCarrier[0] = null;
					stage[0].close();
				});
				
				
				new Thread(() ->  {
					while((stage[0] == null) || stage[0].isShowing())
					{
						if (resultCarrier[0] != KeyChecker.getLastKey() && KeyChecker.getLastKey() != null)
						{
							resultCarrier[0] = KeyChecker.getLastKey();
							if (resultCarrier[0] != null)
								Platform.runLater(() -> resTx[0].setText("Key: " + ((KeyCode)resultCarrier[0]).getName()));
						}
						OtherHelpers.sleepNonException(20);
					}
				}).start();
				
				break;
				
			}
			
			
			grid.setMinWidth(200);
			if (!okBound)
				bnOK.setMinWidth(100);
			GridPane.setHalignment(bnOK, HPos.CENTER);

			
			Scene dialog = new Scene(contentPane);
			
			Node root = (Node) dialog.getRoot();
			

			stage[0] = new Stage();
			stage[0].setScene(dialog);
			
			
			stage[0].setAlwaysOnTop(true); ///////////
			
			
			bnOK.setOnAction((e)-> {
				canceled[0] = false;
				stage[0].close();
			});
			
			
			if (type == keyCodeType)
				KeyChecker.initForStage(stage[0]);
				
			stage[0].show();


			
			stage[0].toFront();
			
			state[0] = 1;
			
			return(true);
		}));
		
		
		while(true)
		{
			if (state[0] == 1)
			{
				if (Execution.isRunning())
					Execution.addActiveExternalStages(stage[0], false);
				
				
				while(stage[0].isShowing()) {try { Thread.sleep(GlobalSettings.constantCheckDelay);} catch (InterruptedException e) {}}

				
				Execution.removeActiveExternalStages(stage[0]);
				
				openedMessages--;
				
				
				if (!Execution.isRunning())
				if (Execution.countActiveExternalStages() > 1)
					ProductionGUI.toFront(); ////////////////
				
				
				String tx = field.getText();
				
				if (canceled[0])
					return(null);
				
				switch(type)
				{
				case -1:
					return(null);
					
				case Variable.doubleType:
					if (tx.isEmpty())
						return(0);
					else
						return(Double.valueOf(tx));
					
				case Variable.textType:
					return(tx);
					
				case staticTableMessageType:
				case keyCodeType:
					return(resultCarrier[0]);
				}
			}
			
			try { Thread.sleep(GlobalSettings.constantCheckDelay);} catch (InterruptedException e) {}
			
		}
	}
	
	
	
	
	private static Button newButton(String text)
	{
		Button bt = new Button(text);
		bt.getStyleClass().addAll("mainButtons", "standardBoldText");
		bt.setMaxWidth(Double.MAX_VALUE);
		return(bt);
	}
	
	
	
	

	/**
	 * Show a message with title
	 *
	 * @param title
	 * @param message
	 */
	/*
	public static void showMessage(String title, String message)
	{
		OtherHelpers.perform(new FutureTask<Object>(() -> {

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
			
			styleAlert(alert);

			return(true);
		
		}));
	}
	*/

	
	/**
	 * Shows a message
	 *
	 * @param message
	 */
	public static void showErrorAlwaysPopup(String message, String errorStr, String buttonText)
	{
		OtherHelpers.perform(new FutureTask<Object>(() -> {
		
			Alert alert = new Alert(AlertType.ERROR);
			if (!LocationPreparator.isWindows())
				alert.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
			            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			
			
			alert.setTitle("Error");
			alert.setHeaderText(message);
			alert.setContentText(errorStr +"\n\n ");
			
			ButtonType buttonCancel = new ButtonType(buttonText);
			
			alert.getButtonTypes().setAll(buttonCancel);
			
			styleAlert(alert);
			
			alert.showAndWait();
			
			ProductionGUI.toFront();
			
			return(true);
		}));
	}
	
	

	/**
	 * Shows a message
	 *
	 * @param message
	 */
	public static void showError(String message, String errorStr, String errorStrMsg)
	{
		InfoErrorHandler.printExecutionErrorMessage("------------------- ERROR OCCURRED -------------------");
		InfoErrorHandler.printExecutionErrorMessage(message);
		InfoErrorHandler.printExecutionErrorMessage("------------------- -------------- -------------------");
		InfoErrorHandler.printExecutionErrorMessage(errorStr);
		InfoErrorHandler.printExecutionErrorMessage("------------------- -------------- -------------------");
		
		if (!Execution.isRunningInGUI() && !GlobalSettings.visualDebug)
			return;
		
		OtherHelpers.perform(new FutureTask<Object>(() -> {
		
			Alert alert = new Alert(AlertType.ERROR);
			if (!LocationPreparator.isWindows())
				alert.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
			            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			
			
			alert.setTitle("Error");
			alert.setHeaderText(message);
			alert.setContentText(errorStr + errorStrMsg+"\n\n ");
			
			ButtonType buttonCancel = new ButtonType("Close Dialog");
			
			alert.getButtonTypes().setAll(buttonCancel);
			
			styleAlert(alert);
			
			alert.showAndWait();
			
			ProductionGUI.toFront();
			
			return(true);
		}));
	}
	
	
	
	
	
	public static boolean showError(String message, String errorStr, String errorStrMsg, String additionalButton)
	{
		InfoErrorHandler.printExecutionErrorMessage("------------------- ERROR OCCURRED -------------------");
		InfoErrorHandler.printExecutionErrorMessage(message);
		InfoErrorHandler.printExecutionErrorMessage("------------------- -------------- -------------------");
		InfoErrorHandler.printExecutionErrorMessage(errorStr);
		InfoErrorHandler.printExecutionErrorMessage("------------------- -------------- -------------------");
		
		if (!Execution.isRunningInGUI() && !GlobalSettings.visualDebug)
			return(false);
		
		return (boolean) (OtherHelpers.perform(new FutureTask<Object>(() -> {

			Alert alert = new Alert(AlertType.ERROR);
			if (!LocationPreparator.isWindows())
				alert.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
			            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			alert.setTitle("Error");
			alert.setHeaderText(message);
			alert.setContentText(errorStr + errorStrMsg+"\n\n ");
			
			
			ButtonType button = new ButtonType(additionalButton);
			ButtonType buttonCancel = new ButtonType("Close Dialog");
		
			alert.getButtonTypes().setAll(button, buttonCancel);
		
			styleAlert(alert);
			
			Optional<ButtonType> result = alert.showAndWait();
			
			ProductionGUI.toFront();
			
			if (result.get() == button)
				return(true);
			
		
			return(false);
			
		})));
	}
	
	private static void styleAlert(Alert alert)
	{
		DialogPane dialogPane = alert.getDialogPane();
		
		dialogPane.getStyleClass().add("messageBackgroundRed");
		dialogPane.getStyleClass().add("alertDialog");
		
		String text = alert.getContentText();
		if (!text.isEmpty())
			alert.setContentText(text+"\n ");
		
		//int buttonCount = dialogPane.getButtonTypes().size();
		for(ButtonType btType: dialogPane.getButtonTypes())
		{
			Button bt = ((Button) dialogPane.lookupButton(btType));
			bt.getStyleClass().add("mainButtons");
			bt.getStyleClass().add("standardBoldText");
		}
		
		((Stage) alert.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
	}


	/**
	 * Shows a message allowing to select from a list
	 *
	 * @param embeddedWindow
	 * @param questionText
	 * @param choices
	 * @return
	 */
	/*
	public static String getListChosenElement(String message, String questionText, List<String> choices)
	{
		return((String) OtherHelpers.perform(new FutureTask<Object>(() -> { 
			
			ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
			if (!LocationPreparator.isWindows())
				dialog.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
			            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			dialog.setTitle("");
			dialog.setHeaderText(message);
			dialog.setContentText(questionText);			
			
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent())
				return(result.get());
	
			return("");
			
		})));
	}
	*/
	
	
	
	
	public static int askQuestionDirect(String question)
	{
		return(askQuestionDirect(question, ""));
	}
	public static int askQuestionDirect(String questionTxt, String content)
	{
		String question = StringHelpers.resolveArgNewline(questionTxt);
		
		InfoErrorHandler.printExecutionInfoMessage("QUESTION: " + question + " CONTENT: " + content);
		
		return((int) OtherHelpers.perform(new FutureTask<Object>(() -> { 
					
			Alert alert = new Alert(AlertType.NONE);
			if (!LocationPreparator.isWindows())
				alert.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
			            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			alert.setTitle("Question");
			alert.setHeaderText(question);
			alert.setContentText(content);
			
			
			ButtonType buttonTypeYes = new ButtonType("Yes");
			ButtonType buttonTypeNo = new ButtonType("No");
			ButtonType buttonTypeCancel = new ButtonType("Cancel");
			
			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
			
			styleAlert(alert);
			
			Optional<ButtonType> result = alert.showAndWait();
			
			ProductionGUI.toFront();
			
			
			if (result.get() == buttonTypeYes)
				return(1);
			if (result.get() == buttonTypeNo)
				return(0);
				
			return(-1);
		
		})));
	}
	
	public static boolean askForContinue(String questionTxt)
	{
		String question = StringHelpers.resolveArgNewline(questionTxt);
		
		InfoErrorHandler.printExecutionInfoMessage("QUESTION: " + question);
		
		return((boolean) OtherHelpers.perform(new FutureTask<Object>(() -> { 
					
			Alert alert = new Alert(AlertType.NONE);
			if (!LocationPreparator.isWindows())
				alert.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
			            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			alert.setTitle("Question");
			alert.setHeaderText(question);
			
			ButtonType buttonTypeYes = new ButtonType("Continue");
			ButtonType buttonTypeCancel = new ButtonType("Cancel");
	
			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeCancel);
	
			styleAlert(alert);

			Optional<ButtonType> result = alert.showAndWait();
			
			ProductionGUI.toFront();
			
			if (result.get() == buttonTypeYes)
				return(true);
				
			return(false);
		
		})));
	}
	
	
	public static String getTextDirect(String question, String defaultStr)
	{
		return(getTextDirect(question, "", defaultStr));
	}
	public static String getTextDirect(String questionTxt, String content, String defaultStr)
	{
		String question = StringHelpers.resolveArgNewline(questionTxt);
		
		return((String) OtherHelpers.perform(new FutureTask<Object>(() -> { 
		
		TextInputDialog dialog = new TextInputDialog(defaultStr);
		if (!LocationPreparator.isWindows())
			dialog.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
		            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		dialog.setTitle("Input Text");
		dialog.setHeaderText(question);
		dialog.setContentText(content);
		
		
		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		
		ProductionGUI.toFront();
		
		
		if (result.isPresent())
			return(result.get());
		else return("");
		
		})));
	}
	
	
	public static Double getDoubleDirect(String questionTxt, String content, double defaultValue)
	{
		String question = StringHelpers.resolveArgNewline(questionTxt);
		
		return((Double) OtherHelpers.perform(new FutureTask<Object>(() -> { 
		
		TextInputDialog dialog = new  TextInputDialog( String.valueOf(defaultValue) );
		if (!LocationPreparator.isWindows())
			dialog.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
		            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		dialog.setTitle("Input Value");
		dialog.setHeaderText(question);
		dialog.setContentText(content);
		
		
		dialog.getEditor().textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		    	newValue = newValue.replace(',', '.');
		    	if (!newValue.matches("\\d*\\.*\\d*"))
		        	dialog.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
	        	else
		        	dialog.getEditor().setText(newValue);
		    }
		});
		
		
		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		
		ProductionGUI.toFront();
		
		
		if (result.isPresent())
			return(Double.valueOf(result.get()));
		else return(null);
		
		})));
	}
	
	public static BigDecimal getBigDecimalDirect(String questionTxt, String content, BigDecimal defaultValue)
	{
		String question = StringHelpers.resolveArgNewline(questionTxt);
		
		return((BigDecimal) OtherHelpers.perform(new FutureTask<Object>(() -> { 
		
		TextInputDialog dialog = new  TextInputDialog( String.valueOf(defaultValue) );
		if (!LocationPreparator.isWindows())
			dialog.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
		            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		dialog.setTitle("Input Value");
		dialog.setHeaderText(question);
		dialog.setContentText(content);
		
		
		dialog.getEditor().textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		    	newValue = newValue.replace(',', '.');
		        if (!newValue.matches("\\d*\\.*\\d*"))
		        	dialog.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
	        	else
		        	dialog.getEditor().setText(newValue);
		    }
		});
		
		
		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		ProductionGUI.toFront();
		
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setGroupingSeparator(',');
		symbols.setDecimalSeparator('.');
		String pattern = "#,##0.0#";
		DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
		decimalFormat.setParseBigDecimal(true);
		
		
		if (result.isPresent())
			return((BigDecimal) decimalFormat.parse(result.get()));
		else return(null);
		
		})));
	}
	
	
	public static Integer getIntDirect(String questionTxt, String content, Integer defaultValue)
	{
		String question = StringHelpers.resolveArgNewline(questionTxt);
		
		return((Integer) OtherHelpers.perform(new FutureTask<Object>(() -> { 
		
		TextInputDialog dialog = new  TextInputDialog( String.valueOf(defaultValue) );
		if (!LocationPreparator.isWindows())
			dialog.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
		            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		dialog.setTitle("Input Value");
		dialog.setHeaderText(question);
		dialog.setContentText(content);
		
		
		dialog.getEditor().textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	dialog.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		
		
		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		
		ProductionGUI.toFront();
		
		
		if (result.isPresent())
			return(Integer.valueOf(result.get()));
		else return(null);
		
		})));
	}
	

	
	public static void applyStandardTooltip(Node node, String toolTipText)
	{
		Tooltip tooltip = new Tooltip(toolTipText);
		tooltip.setStyle(GlobalSettings.tooltipStyle);
		
		boolean[] showTooltip = new boolean[1];
		showTooltip[0] = false;
		
		
		node.setOnMouseEntered(new EventHandler<MouseEvent>() {
			
		    @Override
		    public void handle(MouseEvent event)
		    {
		    	showTooltip[0] = true;
		    	FxTimer.runLater(
		    	        Duration.ofMillis(GlobalSettings.tooltipDelay),
		    	        () -> {
		    	        	if (showTooltip[0])
		    	        	{
						        Point2D p = node.localToScreen(0, node.getLayoutBounds().getMaxY());
						        tooltip.show(node, event.getScreenX(), p.getY());
						        if ((p.getY() + tooltip.getHeight()) > ProductionGUI.getPrimaryScreenBounds().getHeight())
						        	tooltip.setY(p.getY() - tooltip.getHeight() - 30);
						        
						        showTooltip[0] = false;
		    	        	}
		    	        });
		    }
		});
		node.setOnMouseExited((MouseEvent) -> {
			tooltip.hide();
			showTooltip[0] = false;
			});		
		
		
		
		
	}

	public static Node createSizedImage(String baseFile, String hoverFile, int size, String toolTipText)
	{
		return(createSizedImage(baseFile, hoverFile, size, toolTipText, null));
	}
	public static Node createSizedImage(String baseFile, String hoverFile, int size, String toolTipText, Runnable clickTask)
	{
		ImageView node = new ImageView(new Image(baseFile));
		node.setFitHeight(size);
		node.setPreserveRatio(true);
		
		return(createSizedImage(node, baseFile, hoverFile, toolTipText, clickTask));
	}
	public static Node createSizedImage(ImageView node, String baseFile, String hoverFile, String toolTipText)
	{
		return(createSizedImage(node, baseFile, hoverFile, toolTipText, null));
	}
	public static Node createSizedImage(ImageView node, String baseFile, String hoverFile, String toolTipText, Runnable clickTask)
	{
		Image base = new Image(baseFile);
		Image hover = new Image(hoverFile);
		
		
		Tooltip tooltip = new Tooltip(toolTipText);
		tooltip.setStyle(GlobalSettings.tooltipStyle);
		
		boolean[] showTooltip = new boolean[1];
		showTooltip[0] = false;
		
		Node surrounded;
		
		if (node.getParent() != null)
			surrounded = node;
		else
			surrounded = new Pane(node);
		
		if (clickTask == null)
			surrounded.setOnMouseClicked((event) -> {
		    	node.setImage(hover);
			    Point2D p = node.localToScreen(0, node.getLayoutBounds().getMaxY());
		        if (toolTipText!=null)
		        	tooltip.show(node, event.getScreenX(), p.getY());
				showTooltip[0] = false;
			});
		else
			surrounded.setOnMouseClicked((event) -> clickTask.run());
		
			
		surrounded.setOnMouseEntered((MouseEvent event) -> {
		    	node.setImage(hover);
		    	showTooltip[0] = true;
		    	FxTimer.runLater(
		    	        Duration.ofMillis(GlobalSettings.tooltipDelay),
		    	        () -> {
		    	        	if (showTooltip[0])
		    	        	{
						        Point2D p = node.localToScreen(0, node.getLayoutBounds().getMaxY());
						        if (toolTipText!=null)
						        	tooltip.show(node, event.getScreenX(), p.getY());
						        showTooltip[0] = false;
		    	        	}
		    	        });
		    });
		surrounded.setOnMouseExited((MouseEvent event) -> {
	        if (toolTipText!=null)
	        	tooltip.hide();
			showTooltip[0] = false;
			node.setImage(base);
			});		
		
		
		
		
		//v.setOnMouseEntered((MouseEvent) -> {v.setImage(hover); System.out.println("HOVER");} );	
		//v.setOnMouseExited((MouseEvent) -> v.setImage(base));	
		
		return(surrounded);
	}
	
	
	
	
	public static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
	    if (potentialHierarchyElement == null) {
	        return true;
	    }
	    while (node != null) {
	        if (node == potentialHierarchyElement) {
	            return true;
	        }
	        node = node.getParent();
	    }
	    return false;
	}




	public static String sIfPlur(int vars)
	{
		return((vars > 1) ? "s" : "");
	}

	
	


}
