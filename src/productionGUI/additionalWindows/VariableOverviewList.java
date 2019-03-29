package productionGUI.additionalWindows;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import dataTypes.SimpleTable;
import dataTypes.specialContentValues.DataList;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.handlers.VariableHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import productionGUI.ProductionGUI;
import productionGUI.sections.elementManagers.EventsSectionManager;
import settings.GlobalSettings;
import staticHelpers.DebugMsgHelper;
import staticHelpers.GuiMsgHelper;

public class VariableOverviewList extends PopableWindow
{
	private static VariableOverviewList self;
	private static volatile boolean isReady = false;
	
	private static Timer updateOffsTimer;

	
	public VariableOverviewList()
	{
		super("Variables/Info",
				GlobalSettings.extraWindowWidth, GlobalSettings.extraWindowHeight/2,
				 (int) ProductionGUI.getScene().getWidth() - (int) EventsSectionManager.getSelf().getSectionManager().getScrollPane().getViewportBounds().getWidth()-64 - GlobalSettings.extraWindowWidth, (int) ProductionGUI.getScene().getHeight() - (int) (GlobalSettings.extraWindowHeight/2)/2 - 54,
				"/productionGUI/additionalWindows/VariableOverviewList.fxml");
		
		reset();
		
		
		updateOffsTimer = new Timer();
		updateOffsTimer.schedule(
			    new TimerTask() {
			        @Override
			        public void run() {
			        	updatedThisSec.clear();
			        	VariableHandler.handleExternal();
			        }
			    }, 0, 50);
		
	}
	
	protected void end()
	{
		updateOffsTimer.cancel();
	}
	


	@FXML Label titleText, titleText2;
	//@FXML private VBox contentA, contentB, contentC, contentD, contentE, contentF;
	@FXML private SplitPane elementsSplitPane, elementsSplitPane2;
	
	@FXML GridPane mainPane;
	@FXML VBox sectionBox;
	@FXML ScrollPane scrollPane;
	
	
	private static List<String> updatedThisSec = new ArrayList<String>();
	//private static ObservableList<Node> nodesA, nodesB, nodesC, nodesD, nodesE, nodesF;
	
	//private static SplitPane stElementsSplitPane, stElementsSplitPane2;
	
	private static HBox stTitle, stTitle2;
	
	
	private static SimpleTable<String> variablesTable;
	private static SimpleTable<String> infoTable;
	
	
	@FXML
    public void initialize()
    {
		self = this;
		
		
		sectionBox.getChildren().clear();		
		
		
		
		variablesTable = new SimpleTable<String>();
		
		variablesTable.addColumn("Name");
		variablesTable.addColumn("Type");
		variablesTable.addColumn("Value");
		
		infoTable = new SimpleTable<String>();
		
		infoTable.addColumn("Name");
		infoTable.addColumn("Type");
		infoTable.addColumn("Value");
		
		
		
		sectionBox.minHeightProperty().bind(scrollPane.heightProperty().subtract(4));
		
		
		scrollPane.getStyleClass().remove("sectionBorder");
		scrollPane.getStyleClass().add("sectionBorderFull");
		
		
		isReady = true;
		
		
		titleText.setText("Variables");
		titleText2.setText("Function Info");
		
		stTitle = (HBox) titleText.getParent();
		stTitle2 = (HBox) titleText2.getParent();
		stTitle.setVisible(false);
		stTitle2.setVisible(false);
		
		elementsSplitPane.setVisible(false);
		elementsSplitPane2.setVisible(false);
		//stElementsSplitPane = elementsSplitPane;
		//stElementsSplitPane2 = elementsSplitPane2;
    }
	

	private void visualizeTable(String headerText, SimpleTable<String> table, Pane target)
	{
		Pane tablePane = table.visualizeTable("msgTableBox", "msgHeaderBackgroundB", "msgElementBackgroundB", "tableElementsLabelHeader", "tableElementsLabel");
		tablePane.setMaxWidth(Double.MAX_VALUE);
		
		tablePane.prefWidthProperty().bind(target.widthProperty().subtract(20));
		
		table.getHeader().setStyle("-fx-border-width: 0 0.75 0.75 0.75;");
	
		sectionBox.minHeightProperty().bind(scrollPane.heightProperty().subtract(3));
		
		Label lb = new Label(headerText);
		lb.getStyleClass().add("tableElementsLabelHeader");
		BorderPane header = new BorderPane(lb);
		header.getStyleClass().add("msgHeaderBackground");
		header.setStyle("-fx-padding: 2;");
		target.getChildren().add(header);
		
		target.getChildren().add(tablePane);
		
		
		scrollPane.getStyleClass().remove("sectionBorderFull");
		if (!scrollPane.getStyleClass().contains("sectionBorder"))
			scrollPane.getStyleClass().add("sectionBorder");
	}
	

	
	public static void updateVariable(String variableName, String typeName, String valueString, boolean isVariable, boolean editable)
	{
		if (self == null) return;
		self._updateVariable(variableName, typeName, valueString, isVariable, editable, ()-> offerVariableEdit(variableName), false);
	}
	public static void updateVariable(String variableName, String typeName, String valueString, boolean isVariable, boolean editable, boolean ignoreIfOften)
	{
		if (self == null) return;
		self._updateVariable(variableName, typeName, valueString, isVariable, editable, ()-> offerVariableEdit(variableName), ignoreIfOften);
	}
	public static void updateVariable(String variableName, String typeName, String valueString, boolean isVariable, boolean editable, Runnable changeFunc)
	{
		if (self == null) return;
		self._updateVariable(variableName, typeName, valueString, isVariable, editable, changeFunc, false);
	}
	private void _updateVariable(String variableName, String typeName, String valueString, boolean isVariable, boolean editable, Runnable changeFunc, boolean ignoreIfOften)
	{
		if (!isReady) return;
		
		Platform.runLater(() -> {
			synchronized(VariableOverviewList.class)
			{
				boolean highlight = !updatedThisSec.contains(variableName);
				if (highlight)
					updatedThisSec.add(variableName);
				
				if (ignoreIfOften && !highlight)
					return;
				
				
				if (isVariable)
				{
					if (!variablesTable.isVisualized())
						visualizeTable("Variables", variablesTable, sectionBox);
				}
				else
				{
					if (!infoTable.isVisualized())
						visualizeTable("Info / Hardware", infoTable, sectionBox);
				}
				
				
				String name = variableName;
				
				int specialType = SimpleTable.normal;
				boolean hasButton = false;
				
				// Special cases
				if (variableName.startsWith(DebugMsgHelper.PINSstr))
				{
					name = name.substring(2);
					if (typeName.equals(DebugMsgHelper.PINstrIN))
					{
						specialType = SimpleTable.PinInType;
						hasButton = true;
					}
					if (typeName.equals(DebugMsgHelper.PINstrOUT))
						specialType = SimpleTable.PinOutType;
					
					if (variableName.startsWith(DebugMsgHelper.PINSstrWithVar))
						name = name.substring(DebugMsgHelper.PINSstrWithVar.length()-2);
				}
				
				if (variableName.startsWith(GlobalSettings.debugButtonStartSymbol))
				{
					name = name.substring(GlobalSettings.debugButtonStartSymbol.length());
					hasButton = true;
				}
				
				///
				
				
				
				if (isVariable)
					variablesTable.addOrUpdateRow(new String[] {name, typeName, valueString}, editable ? "msgElementBackgroundClickable" : "", editable ? () -> new Thread(changeFunc).start() : null, highlight, specialType, hasButton);
				else
					infoTable.addOrUpdateRow(new String[] {name, typeName, valueString}, editable ? "msgElementBackgroundClickable" : "", editable ? () -> new Thread(changeFunc).start() : null, highlight, specialType, hasButton);
				
			}
		});
	}
	
	
	private static void offerVariableEdit(String variableName)
	{
		if (!Execution.isRunning())
			return;
		
		Variable var = VariableHandler.getOnlyIfExisting(variableName);
		if (var!=null)
		{
			switch(var.getType())
			{
			case Variable.boolType:
				var.set(!(boolean) var.getUnchecked());
				break;
			
			case Variable.doubleType:
				Double res = GuiMsgHelper.getNumberNonblockingUI("Type in the new value of the variable to change it instantly.", (Double) var.getUnchecked());
				if (res != null)
					var.set((double) res);
				break;
				
			case Variable.textType:
				String res2 = GuiMsgHelper.getTextNonblockingUI("Type in the new text to place in the variable '" + variableName + "'.", (String) var.getUnchecked() );
				if (res2 != null)
					var.set(res2);
				break;
				
			case Variable.dataListType:
				DataList list = (DataList) var.getUnchecked();
				String listContent = list.printListForDebug();
				
				if ((list.getType() != Variable.doubleType) && (list.getType() != Variable.textType)) // not editable
				{
					GuiMsgHelper.showMessageNonblockingUI("The list has the following content:\n---\n" + listContent.toString() +"---\nThe type is not editable at runtime.");
					return;
				}
				
				Double res4 = GuiMsgHelper.getNumberNonblockingUI("The list has the following content:\n---\n" + listContent.toString() +"---\nType the index number you want to change or delete.\nUse a larger number to expand the list.", 0);
				if (res4 != null)
				{
					int pos = (int) (double) res4;
					if (pos < list.getSize())
					switch(GuiMsgHelper.askQuestionDirect("Do you want to delete entry with index '" + pos + "'?"))
					{
					case 1: list.remove(pos); return;
					case -1: return;
					}
					
					switch(list.getType())
					{
					case Variable.doubleType:
						Double resB = GuiMsgHelper.getNumberNonblockingUI("Type in the new value of the list entry at position '" + pos + "'.", (Double) list.get(pos) );
						if (resB != null)
							list.set(pos, (double) resB);
						break;
						
					case Variable.textType:
						String res2B = GuiMsgHelper.getTextNonblockingUI("Type in the new text to place into the list at position '" + pos + "'.", list.get(pos).toString() );
						if (res2B != null)
							list.set(pos, res2B);
						break;
					}
					
				}
				break;
				
			case Variable.countDownLatchType:
				CountDownLatch latch = (CountDownLatch) var.getUnchecked();
				Double res5 = GuiMsgHelper.getNumberNonblockingUI("Type the new counter value.\nAttention: If the new number is higher than the current value,\nthe CountDownLatch will be re-created and thus threads (events) currently blocked\nwillinstantly be released.\nIf the number is lower, the current latch will be decreased corespondingly.\nType '0' to instantly release all blocked threads.", Double.valueOf(latch.getCount()));
				if (res5 != null)
				{
					int newCount = (int) (double) res5;
					if (newCount > latch.getCount())
					{
						CountDownLatch newLatch = new CountDownLatch(newCount);
						var.set(newLatch);
						while(latch.getCount() > 0)
							latch.countDown();
					}
					else
						while(newCount < latch.getCount())
							latch.countDown();
				}
				break;
				
			}
		}
	}
	
/*
	private static void highlightIndex(int index, boolean isVariable)
	{		
		Text[] targets = new Text[3];
		if (isVariable)
		{
			targets[0] = (Text) nodesA.get(index);
			targets[1] = (Text) nodesB.get(index);
			targets[2] = (Text) nodesC.get(index);			
		}
		else
		{
			targets[0] = (Text) nodesD.get(index);
			targets[1] = (Text) nodesE.get(index);
			targets[2] = (Text) nodesF.get(index);
		}
		
		if (!targets[0].getStyle().isEmpty())
			return;
		
		targets[0].setStyle("-fx-fill: rgb(255, 255, 255);");
		
		final int speed = 30;
		final Timer sh = new Timer();
		sh.schedule(
			    new TimerTask() {
			    	boolean revert = false;
			    	int alpha = 255;
			        @Override
			        public void run() {
			        	Platform.runLater(() -> {
			        		String style = "-fx-fill: rgb(" + alpha + ", 255, " + alpha + ");";
			        		targets[0].setStyle(style);	
			        		targets[1].setStyle(style);
			        		targets[2].setStyle(style);	
			        	
				        	alpha += revert ? speed : -speed;
				        	if (alpha < 0)
				        	{
				        		alpha = 0;
				        		revert = true;
				        	}
				        	if (alpha > 255)
				        	{
				        		alpha = 255;
				        		targets[0].setStyle("");
				        		targets[1].setStyle("");
				        		targets[2].setStyle("");
				        		sh.cancel();
				        	}
			        	});

			        }
			    }, 0, 32);
		
	}
*/

	public void reset()
	{
		toFront();
		
		updatedThisSec.clear();
		
		variablesTable.clear();
		infoTable.clear();
	}
	
	
	public boolean isReady()
	{
		return(isReady);
	}
	
	public static VariableOverviewList getSelf()
	{
		return(self);
	}
	
	
}
