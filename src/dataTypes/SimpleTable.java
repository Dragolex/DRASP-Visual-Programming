package dataTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.functionality.SharedComponents;
import settings.GlobalSettings;
import staticHelpers.GuiMsgHelper;
import staticHelpers.KeyChecker;

public class SimpleTable<T> {

	public static final int normal = 0;
	public static final int PinInType = 1;
	public static final int PinOutType = 2;
	
	private static Image switchClosedImg = new Image("/guiGraphics/switch_closed.png");
	private static Image switchOpenImg = new Image("/guiGraphics/switch_open.png");
	private static Image lampOnImg = new Image("/guiGraphics/lamp_on.png");
	private static Image lampOffImg = new Image("/guiGraphics/lamp_off.png");
	
	
	List<String> headers = new ArrayList<String>();
	private List<List<T>> columns = new ArrayList<List<T>>();
	
	private List<VisibleRow> dataRows = new ArrayList<VisibleRow>();
	
	private boolean visualized = false;
	
	
	public List<T> getColumnList(int index)
	{
		return(columns.get(index));
	}
	
	public void addColumn(String header, List<T> list)
	{
		headers.add(header);
		columns.add(list);		
	}
	public void addColumn(String header, T[] list)
	{
		headers.add(header);
		columns.add(Arrays.asList(list));
	}
	
	// Empty column
	public void addColumn(String header)
	{
		headers.add(header);
		columns.add(new ArrayList<T>());
	}
	
	
	public void addOrUpdateRow(T[] values, String additionalClass, Runnable onClick, boolean highlight, int specialType, boolean hasButton)
	{
		VisibleRow row = findCorrespondingRow(values[0]);
				
		if (row == null)
			row = addRow(values, additionalClass, onClick, specialType, hasButton);
		else
			updateRow(row, values);
		
		if (highlight)
			row.highlight();
	}

	public VisibleRow addRow(T[] values, String additionalClass, Runnable onClick, int specialType, boolean hasButton)
	{
		int i = 0;
		for(T val: values)
		{
			columns.get(i).add(val);
			i++;
		}
		
		if (visualized)
		{
			VisibleRow row = visualizeRow(columns.get(0).size()-1, specialType, hasButton);
			
			row.applyClickFunction(onClick);
			if (additionalClass != null)
				row.getSplitPane().getStyleClass().add(additionalClass);
			return(row);
		}
		return(null);
	}
	
	/*
	public void addRow(List<T> values, String additionalClass, Runnable onClick)
	{
		int i = 0;
		for(T val: values)
		{
			columns.get(i).add(val);
			i++;
		}
		
		if (visualized)
		{
			VisibleRow row = visualizeRow(columns.get(0).size()-1);
			if (additionalClass != null)
				row.getNode().getStyleClass().add(additionalClass);
		}
	}
	*/
	
	
	private void updateRow(VisibleRow row, T[] newValues)
	{
		int ind = 0;
		for(T val: newValues)
			row.set(ind++, val);
	}
	
	private VisibleRow findCorrespondingRow(T target)
	{
		int ind = 0;
		
		for(T val: columns.get(0))
		{
			if (val.equals(target))
				return(dataRows.get(ind));
			ind++;
		}
		
		return(null);
	}
	
	
	
	public boolean isVisualized()
	{
		return(visualized);
	}
	

	public void deVisualize()
	{
		visualized = false;
	}
	
	public void clear()
	{
		for(List<T> col: columns)
			col.clear();
		
		dataRows.clear();
		
		if (visualized)
			mainBox.getChildren().remove(1, mainBox.getChildren().size());
	}
	
	
	
	
	
	
	
	

	
	private String headerStyleClass, rowStyleClass, headerLabelClass, labelStyleClass;
	
	private VBox mainBox;
	private VisibleRow headerRow;
	
	public Pane visualizeTable(String boxStyleClass, String headerStyleClass, String rowStyleClass, String headerLabelClass, String labelStyleClass)
	{
		this.headerStyleClass = headerStyleClass;		
		this.rowStyleClass = rowStyleClass;
		
		this.headerLabelClass = headerLabelClass;
		this.labelStyleClass = labelStyleClass;
		
		
		mainBox = new VBox();
		mainBox.setMaxWidth(Double.MAX_VALUE);
		
		mainBox.getStyleClass().add(boxStyleClass);
		
		
		// Header row
		visualizeHeaderRow(headers);
		
		
		int rows = columns.get(0).size();
		
		for(int row = 0; row < rows; row++)
			visualizeRow(row);	
		
		visualized = true;
		
		return(mainBox);
	}
	
	private void visualizeHeaderRow(List<String> headers)
	{
		VisibleRow newRow = new VisibleRow(headerStyleClass);
		
		if (headerRow == null)
			headerRow = newRow;
		
		for(String header: headers)
			newRow.add(0, header, headerLabelClass);
		
		newRow.apply(mainBox);
	}

	private VisibleRow visualizeRow(int row)
	{
		return(visualizeRow(row, normal, false));
	}
	
	private VisibleRow visualizeRow(int row, int specialType, boolean hasButton)
	{
		VisibleRow dataRow = new VisibleRow(rowStyleClass, specialType, hasButton);
		
		for(int col = 0; col < columns.size(); col++)
			dataRow.add(col, columns.get(col).get(row).toString(), labelStyleClass);
		
		dataRow.syncWith(headerRow);
		dataRows.add(dataRow);
		dataRow.apply(mainBox);
		
		return(dataRow);
	}
	
	
	class VisibleRow
	{
		SplitPane dataPane = new SplitPane();
		Pane base = new Pane(dataPane);
		FadeTransition attentionRectangleTransition;
		
		int type = normal;
		boolean hasButton = false;
		
		Runnable onClick;

		public VisibleRow(String rowStyleClass)
		{
			create(rowStyleClass);
		}
		
		public VisibleRow(String rowStyleClass, int specialType, boolean hasButton)
		{
			this.type = specialType;
			this.hasButton = hasButton;
			create(rowStyleClass);
		}
		
		private void create(String rowStyleClass)
		{
			//base.setMaxWidth(Double.MAX_VALUE);
			
			base.setMaxWidth(Double.MAX_VALUE);
			dataPane.setMaxWidth(Double.MAX_VALUE);
			dataPane.getStyleClass().add(rowStyleClass);
			
			dataPane.prefWidthProperty().bind(base.widthProperty());
			
			
			Rectangle attentionRectangle = new Rectangle(1, 1, dataPane.getWidth()-2, dataPane.getHeight()-2);
			attentionRectangle.setArcHeight(5);
			attentionRectangle.setArcWidth(5);
			attentionRectangle.setManaged(false);
			attentionRectangle.setMouseTransparent(true);
			
			attentionRectangle.widthProperty().bind(dataPane.widthProperty().subtract(2));
			attentionRectangle.heightProperty().bind(dataPane.heightProperty().subtract(2));
			
			attentionRectangle.setFill(Color.LIME);
			attentionRectangle.setOpacity(0);
			
			base.getChildren().add(attentionRectangle);
			
			attentionRectangleTransition = new FadeTransition(GlobalSettings.attentionBlinkDurationFast, attentionRectangle);
			attentionRectangleTransition.setFromValue(GlobalSettings.attentionRectangleMinAlpha);
			attentionRectangleTransition.setToValue(GlobalSettings.attentionRectangleMaxAlpha);

			attentionRectangleTransition.setDuration(GlobalSettings.attentionBlinkDuration);
			attentionRectangleTransition.setCycleCount(2);

			attentionRectangleTransition.setAutoReverse(true);
		}
		
		
		public void applyClickFunction(Runnable onClick)
		{
			this.onClick = onClick;
			
			dataPane.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent e)
				{
			    	if (e.getButton() == MouseButton.PRIMARY)
			    		if (onClick != null)
			    			onClick.run();
				}
			});
		}


		public void apply(VBox mainBox)
		{
			mainBox.getChildren().add(base);
			base.prefWidthProperty().bind(mainBox.widthProperty());
		}


		public void highlight()
		{
			attentionRectangleTransition.stop();
			attentionRectangleTransition.play();
		}
		

		public SplitPane getSplitPane()
		{
			return(dataPane);
		}
		

		public void syncWith(SimpleTable<T>.VisibleRow headerRow)
		{
			double sect = 1.0 / (double) dataPane.getItems().size();
						
			int ind = 0;
			for(Divider div: dataPane.getDividers())
			{
				headerRow.getSplitPane().getDividers().get(ind).positionProperty().bindBidirectional( div.positionProperty() );
				div.setPosition((ind+1)*sect);
				ind++;
			}
			
		}
		
		public Label getElement(int ind)
		{
			return (Label) (((Pane) dataPane.getItems().get(ind)).getChildren().get(0));
		}
		
		public void add(int col, String data, String labelStyleClass)
		{
			Pane ele = null;
			
			
			/// Special handling for the name of GPIO pin out
			if ((col == 0) && (type == PinOutType))
			{
				//System.out.println("D: " + data);
				
			}
				
			
			if (type == normal || (!(data.equalsIgnoreCase("true") || data.equalsIgnoreCase("false"))))
			{
				Label lb = new Label(data);
				lb.getStyleClass().add(labelStyleClass);
				ele = new Pane(lb);	
			}
			else
			switch(type)
			{
			case PinInType:
				ImageView img = new ImageView();
				img.setPreserveRatio(true);
				
				if (data.equalsIgnoreCase("true"))
					img.setImage(switchClosedImg);
				else
					img.setImage(switchOpenImg);
				
				Button bt = new Button("Key");
				bt.setVisible(false); bt.setManaged(false); bt.setMouseTransparent(true); // TODO and verify that it works!
				HBox box = createKeyBox(img, bt, (ev) -> { new Thread(() -> SharedComponents.GPIOctrl.clickedKeyButton(bt, ((Label)((Pane) dataPane.getItems().get(0)).getChildren().get(0)).getText())).start();});
				ele = new StackPane(box);
				StackPane.setAlignment(box, Pos.CENTER);
				
				hasButton = false;
				
				img.fitWidthProperty().bind(ele.widthProperty());
				img.setFitHeight(20);
				
				break;
				
			case PinOutType:
				
				ImageView img2 = new ImageView();
				img2.setPreserveRatio(true);
				
				if (data.equalsIgnoreCase("true"))
					img2.setImage(lampOnImg);
				else
					img2.setImage(lampOffImg);

				ele = new StackPane(img2);
				StackPane.setAlignment(img2, Pos.CENTER);
				
				img2.fitWidthProperty().bind(ele.widthProperty());
				img2.setFitHeight(20);
				
				break;			

			}
			
			if (hasButton && col == 2)
			{
				Label lb = new Label(data);
				lb.getStyleClass().add(labelStyleClass);
				
				Button bt = new Button("Key");
				HBox box = createKeyBox(lb, bt, (ev) -> { new Thread(() -> directExecuteButton(bt, onClick)).start();});
				ele = new StackPane(box);
				StackPane.setAlignment(box, Pos.CENTER);
			}
			
			
			ele.setMaxWidth(Double.MAX_VALUE);
			ele.setMinWidth(60);
			dataPane.getItems().add( ele );
		}
		
		
		public void set(int col, T dataEle)
		{
			String dataStr = (String) dataEle;
			Pane elePane = (Pane) dataPane.getItems().get(col);
			
			if ((type == normal || (!(dataStr.equalsIgnoreCase("true") || dataStr.equalsIgnoreCase("false")))) && (!hasButton))
			{
				Label baseL = (Label) elePane.getChildren().get(0);
				baseL.setText(dataStr);
			}
			else
			switch(type)
			{
				
			case PinInType:
				ImageView img = (ImageView) ((HBox) elePane.getChildren().get(0)).getChildren().get(0);
				
				if (dataStr.equalsIgnoreCase("true"))
					img.setImage(switchClosedImg);
				else
					img.setImage(switchOpenImg);
				break;
				
			case PinOutType:
				ImageView img2 = (ImageView) elePane.getChildren().get(0);
				
				if (dataStr.equalsIgnoreCase("true"))
					img2.setImage(lampOnImg);
				else
					img2.setImage(lampOffImg);
				break;			

			}

			
			
			
			
			//((Label)((Pane) (dataPane.getItems().get(ind))).getChildren().get(0)).setText((String) val);
		}
		
	}
	
	
	public SplitPane getHeader()
	{
		return(headerRow.getSplitPane());
	}
	
	
	static Map<Button, KeyCode> hookedKeys = new HashMap<>();
	
	public void directExecuteButton(Button bt, Runnable onClick)
	{
		KeyCode oldKey = hookedKeys.getOrDefault(bt, null);
		KeyCode cd = GuiMsgHelper.getAnyKeyNonblockingUI("Press the desired key on your keyboard to associate this debug shortcut with.\nWhen you press that key afterwards,\nthe same will happen as if you directly click onto this field.\n\nNote: This does not affect the program when deployed!\nThe purpose is debugging only.\nAlso Note: Using the direction keys is troublesome because the IDE window uses them.", oldKey);
		
		if (cd != oldKey)
		{
			KeyChecker.addPressedHook(oldKey, () -> {});
			KeyChecker.addReleasedHook(oldKey, () -> {});
			hookedKeys.remove(bt);
		}
		
		if (cd == KeyCode.DEAD_BREVE)
		{
			KeyChecker.addPressedHook(oldKey, () -> {});
			
			hookedKeys.remove(bt);
			Platform.runLater(() -> bt.setText("Key"));
			
			return;
		}
		if (cd == null)
		{
			return;
		}
		
		KeyChecker.addPressedHook(cd, onClick);
		hookedKeys.put(bt, cd);
		
		Platform.runLater(() -> bt.setText("Key: " + cd.getName()));
	}

	public HBox createKeyBox(Node node, Button bt, EventHandler<ActionEvent> task)
	{
		HBox box = new HBox(node);
		box.setSpacing(10);
		box.getChildren().add(bt);
		box.setAlignment(Pos.CENTER);
		bt.setOnAction(task);
		
		bt.setStyle("-fx-text-fill: black; "+
				"    -fx-font-family: \"Arial\"; "+ 
				"    -fx-font-size: 10;" + 
				"    -fx-font-weight: bold;");
		
		return(box);
	}

}
