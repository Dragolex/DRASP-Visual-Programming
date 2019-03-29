package productionGUI.sections.elementManagers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.controlsfx.control.ToggleSwitch;
import org.reactfx.util.FxTimer;

import dataTypes.DataNode;
import dataTypes.ProgramElement;
import execution.handlers.InfoErrorHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import main.electronic.Electronics;
import productionGUI.ProductionGUI;
import productionGUI.additionalWindows.OverlayMenu;
import productionGUI.controlers.UndoRedoControler;
import settings.GlobalSettings;
import staticHelpers.FastFadeTransitionHelper;
import staticHelpers.GuiMsgHelper;
import staticHelpers.OtherHelpers;


public class ContentsSectionManager extends AbstractSectionManager
{
	static String sectionName = "Program";
	
	HBox topBox, outerTopBox;
	int pages = 0;
	String currentPage = "";
	
	Button addPageButton, removePageButton;
	Button pagesMenuButton;
	
	Map<String, Button> pageButtons = new LinkedHashMap<>();
	Map<String, Boolean> pagesActive = new LinkedHashMap<>();

	Map<String, Node> newPagesFlashingRectangle = new LinkedHashMap<>();
	
	static String startPage = "Main";
	
	static String doubleClickPage = "";
	
	boolean pagesNotClickable = false;

	Button switchToEleAndBackButton;

	
	char ch = 'A';
	int ascA = (int) ch;
	
	

	static String headerTooltip = "This is the main 'Program' section which displays the current pages of elements.\n"
			+ "Drag elements from the surrounding sections 'Actions', 'Events', 'Structures' and 'Conditions'\n"
			+ "to achieve the desired behavior of your program.\n\n"
			+ "A program can consist of multiple pages. Add and remove with '+' and '-'.\n\n"
			+ "Right click on a page to temporarily exempt it from executing.\n"
			+ "This also works with program elements inside the page.\n\n"
			+ "The tooltips and tutorials will provide you more information.";

	
	public ContentsSectionManager(GeneralSectionManager topManager)
	{
		super(topManager, startPage);
		
		self = this;
		
		ProductionGUI.addFinalizationEvent(() -> {
			topManager.finalize(sectionName + " - Pages: ", headerTooltip);
			initPageButtons(topManager);
		} );
		
		
		onlyOneChildLevel = false;
		newBaseNodesAllowed = true;
		
	}
	
	
	private static ContentsSectionManager self;
	
	public static ContentsSectionManager getSelf()
	{
		return(self);
	}
	
	
	public static String getName()
	{
		return(sectionName);
	}
	
	
	OverlayMenu pagesMenu = null;
	
	private void openPagesMenu()
	{
		openPagesMenu(false);
	}
	private void openPagesMenu(boolean skipFading)
	{
		if (pagesMenu != null) return;
		
		//pagesMenu = new OverlayMenu((Node) pagesMenuButton, () -> pagesMenu = null, true, true);
		pagesMenu = new OverlayMenu((Node) pagesMenuButton, () -> pagesMenu = null, true, true);
		if (skipFading)
			pagesMenu.setOpacity(1);
		
		pagesMenu.setXorientation(false); // Enforce on right side
		
		String[] buttonsImages = {"/guiGraphics/if_edit.png", "/guiGraphics/if_edit_hover.png", "/guiGraphics/if_close.png", "/guiGraphics/if_close_hover.png"};
		String[] buttonsTooltips = {"Click to rename this page.", "Click to delete this page."};
		
		pagesMenu.addElement(new Label("Page"), 0);
		pagesMenu.addElement(new Label("Active"), 1);
		pagesMenu.addElement(new Label("Rename"), 2);
		pagesMenu.addElement(new Label("Delete"), 3);

		pagesMenu.addSeparator("rgb(255, 255, 255);");
		
		for(String pageName: pageButtons.keySet())
		{
			Runnable[] buttonsActions = {() -> new Thread(() -> renamePage(pageName)).start(), () -> new Thread(() -> removePage(pageName)).start()};
			pagesMenu.addButtonToggle(pageName, pagesActive.get(pageName), "", () -> setPageState(pageName, false), () -> setPageState(pageName, true), buttonsImages, buttonsTooltips, buttonsActions);
		}
		
		pagesMenu.addSeparator("rgb(255, 255, 255);");
		pagesMenu.addButton("New Page", () -> {pagesMenu.fade(false); addNewPage();});
		
		//pagesMenu.shiftPosition(-27, 18);
		pagesMenu.shiftPosition(300, 18);
	}
	
	
	public void initPageButtons(GeneralSectionManager topManager)
	{
		/* Add the multi page mechanism */
		
		outerTopBox = (HBox)topManager.getFXtitleText().getParent();
		
		/*
		addPageButton = new Button("+");
		addPageButton.getStyleClass().add("programPageButton");
		addPageButton.getStyleClass().add("addProgramPageButton");
		
		//addPageButton.setOnMousePressed((MouseEvent event) -> {addNewPage(); } );
		addPageButton.setOnMouseReleased((MouseEvent event) -> {addNewPage(); } );
		
		
		removePageButton = new Button("–");
		removePageButton.getStyleClass().add("programPageButton");
		removePageButton.getStyleClass().add("addProgramPageButton");
		
		removePageButton.setOnMouseReleased((MouseEvent event) -> {removeCurrentPage(); } );
		*/
		
		outerTopBox.setAlignment(Pos.CENTER_LEFT);
		
		topBox = new HBox();
		topBox.setAlignment(Pos.CENTER);
		//topBox.setMinWidth(600);
		topBox.setMaxWidth(Double.MAX_VALUE);

		
		while(outerTopBox.getChildren().size() > 0)
		{
			Node f = outerTopBox.getChildren().get(0);
			outerTopBox.getChildren().remove(0);
			topBox.getChildren().add(f);
		}		

		
		switchToEleAndBackButton = new Button("Construction");
		switchToEleAndBackButton.getStyleClass().add("mediumTextBold");
		switchToEleAndBackButton.getStyleClass().add("mainButtons");
		
		switchToEleAndBackButton.setOnAction((ActionEvent) -> {
			if (Electronics.isActive())
			{
				switchToEleAndBackButton.setText("Construction");
				Electronics.switchOffElectronics();
			}
			else
			{
				switchToEleAndBackButton.setText("Programming");
				Electronics.switchOnElectronics();
			}
			});

		outerTopBox.getChildren().add(switchToEleAndBackButton);
		outerTopBox.getChildren().add(topBox);
		
		HBox.setHgrow(topBox, Priority.ALWAYS);
		
		
		
		pagesMenuButton = new Button();
		ImageView img = new ImageView(new Image("/guiGraphics/if_dropdown_white.png"));
		img.setFitHeight(10);
		img.setPreserveRatio(true);
		pagesMenuButton.setGraphic(img);

		pagesMenuButton.getStyleClass().add("programPageButton");
		pagesMenuButton.getStyleClass().add("addProgramPageButton");
		
		
		pagesMenuButton.setOnMouseClicked((MouseEvent) -> openPagesMenu());
		
	}
	
	public void removePagesButtons()
	{
		if (outerTopBox.getChildren().contains(topBox))
			outerTopBox.getChildren().remove(topBox);
		
		System.out.println("SPACING: " + outerTopBox.getSpacing());
		outerTopBox.setSpacing(10);
	}
	
	public Button addTopbarButton(int pos, String name, Runnable clicktask)
	{
		Button bt = new Button(name);
		bt.getStyleClass().add("mediumTextBold");
		bt.getStyleClass().add("mainButtons");
		bt.setOnAction((ActionEvent) -> clicktask.run());
		outerTopBox.getChildren().add(pos+1, bt);
		
		return(bt);
	}
	
	public void addTopbarCheckbox(int pos, String name, Runnable turnon, Runnable turnoff, boolean startValue, boolean performToInit)
	{
		Label text = new Label(name);
		text.getStyleClass().add("mediumTextBold");

		HBox box = new HBox();
		box.setAlignment(Pos.CENTER_LEFT);

		
		ToggleSwitch toggle = new ToggleSwitch();
		toggle.setSelected(startValue);
		
		if (performToInit)
			if (startValue)
				turnon.run();
			else
				turnoff.run();
		
		toggle.setScaleX(GlobalSettings.ToggleScale);
		toggle.setScaleY(GlobalSettings.ToggleScale);
		
		toggle.selectedProperty().addListener(new ChangeListener < Boolean > ()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldval, Boolean newval)
			{
				if (oldval)
					if(!newval)
						turnoff.run();
				if (!oldval)
					if(newval)
						turnon.run();
			} 
        });
		
		toggle.setStyle("-fx-fill: white;" +
				"	 -fx-text-fill: white; " +
				"    -fx-font-family: \"Arial\";" + 
				"    -fx-font-size: 16;" + 
				"    -fx-font-weight: bold;" + 
				"    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5) , 6, 0, 0, 1 );");
		
		
		box.getChildren().add(toggle);
		outerTopBox.getChildren().add(pos+1, box);
		box.getChildren().add(text);
		
		if (!name.contains("\n"))
			text.setTranslateY(-3);
	}

	
	public void revertTopbarButtons()
	{
		outerTopBox.getChildren().clear();
		outerTopBox.getChildren().add(switchToEleAndBackButton);
		outerTopBox.getChildren().add(topBox);
	}
	
	
	public void reinitializePagesButtons()
	{
		pages = 0;
		pageButtons.clear();
		pagesActive.clear();
		
		Label tx = (Label) topBox.getChildren().get(0);
		
		topBox.getChildren().clear();
		
		topBox.getChildren().add(tx);
		
		//topBox.getChildren().add(addPageButton);
		//topBox.getChildren().add(removePageButton);
		
		topBox.getChildren().add(pagesMenuButton);
		
		
		currentPage = "";
		
		for(String page: ProductionGUI.getVisualizedProgram().getPages())
		{
			addNewPage(page, true); // Add all existing pages (including the MAIN page which has been created by the DataControler
		}
		
	}
	
	
	
	
	private void addNewPage()
	{
		addNewPage(null, false);
	}
	public void addNewPage(String pageName, boolean nodeExists)
	{
		
		if ((pageName == null) || pageName.isEmpty())
		{
			pageName = askForPageName(pageName, true);
			
			if ((pageName == null) || pageName.isEmpty()) return;
		}
		
		
		//Button newPage = new Button(Character.toString((char) ( ascA+pages ) ));
		Button newPageButton = new Button(pageName);
		newPageButton.getStyleClass().add("programPageButton");
		newPageButton.getStyleClass().add("addProgramPageButton");
				
		
		newPageButton.setOnMousePressed((MouseEvent event) ->
			{
				String pName = getButtonName(newPageButton);
				if (!pName.equals(currentPage) || (event.getButton() == MouseButton.SECONDARY) || doubleClickPage.equals(pName))
					clickPage(pName, event.getButton() == MouseButton.SECONDARY);
				
				doubleClickPage = pName; FxTimer.runLater(Duration.ofMillis(GlobalSettings.doubleClickTime),() -> doubleClickPage = "");
			});
		
		newPageButton.setOnDragOver((DragEvent) -> {String pName = getButtonName(newPageButton); if (!pName.equals(currentPage)) clickPage(pName, false); } );
		
		
		pagesActive.put(pageName, true);
		pageButtons.put(pageName, newPageButton);
		if (!nodeExists)
			ProductionGUI.getVisualizedProgram().addPage(pageName, new DataNode<ProgramElement>(null));
		else
		{
			if (ProductionGUI.getVisualizedProgram().getPageRoot(pageName).hasChildrenHidden())
			{
				pagesActive.put(pageName, false);
				crossNode(pageButtons.get(pageName));				
				
				ProductionGUI.getVisualizedProgram().getPageRoot(pageName).unhideChildren();
			}
		}
		
		newPageButton.setTranslateY(2);
		
		topBox.getChildren().add(1+pages, new HBox(newPageButton));
		
		
		
		pages++;
		
		if (currentPage.isEmpty())
			currentPage = pageName;
		
		clickPage(pageName, false);
		
		refreshPagesMenu();
	}
	
	
	private String getButtonName(Button bt)
	{
		if (!bt.getText().isEmpty())
			return(bt.getText());
		else
		{
			HBox node = (HBox) bt.getGraphic();
			Label lb;
			if (node.getChildren().size() == 1)
				lb = (Label) node.getChildren().get(0);
			else
				lb = (Label) node.getChildren().get(2);
			return(lb.getText());
		}			
	}


	private void refreshPagesMenu()
	{
		if (pagesMenu != null)	
		{
			Platform.runLater(() ->
			{
				pagesMenu.closeInstantly();
				openPagesMenu(true);
			});
		}
	}


	private void crossNode(Button bt)
	{
		String tx = bt.getText();
		
		Label lb;
		if (tx == null || tx.isEmpty())
		{
			HBox node = (HBox) bt.getGraphic();
			if (node.getChildren().size() == 1)
				lb = (Label) node.getChildren().get(0);
			else
				lb = (Label) node.getChildren().get(2);
		}
		else
			lb = new Label(tx);
		
		HBox newNode = new HBox();
		newNode.getChildren().add(lb);

		int wA = 2;
		int wB = 3;

		//Color col = Color.RED.darker();
		Color colA = new Color(0.9,0,0,0.8);// .RED.darker();
		Color colB = new Color(0.9,0,0,0.15);// .RED.darker();

		Line lineA = new Line(0,0, 5,16);
		lineA.endXProperty().bind(bt.widthProperty().subtract(2));
		lineA.setManaged(false);
		lineA.setMouseTransparent(true);
		lineA.setStrokeWidth(wA);
		lineA.setStroke(colA);
		lineA.setTranslateX(-4);
		lineA.setTranslateY(2);
		
		Line lineB = new Line(0,16, 5,0);
		lineB.endXProperty().bind(bt.widthProperty().subtract(2));
		lineB.setManaged(false);
		lineB.setMouseTransparent(true);
		lineB.setStrokeWidth(wA);
		lineB.setStroke(colA);
		lineB.setTranslateX(-4);
		lineB.setTranslateY(2);

		
		Line lineC = new Line(0,0, 5,16);
		lineC.endXProperty().bind(bt.widthProperty().subtract(2));
		lineC.setManaged(false);
		lineC.setMouseTransparent(true);
		lineC.setStrokeWidth(wB);
		lineC.setStroke(colB);
		lineC.setTranslateX(-4);
		lineC.setTranslateY(2);
		
		Line lineD = new Line(0,16, 5,0);
		lineD.endXProperty().bind(bt.widthProperty().subtract(2));
		lineD.setManaged(false);
		lineD.setMouseTransparent(true);
		lineD.setStrokeWidth(wB);
		lineD.setStroke(colB);
		lineD.setTranslateX(-4);
		lineD.setTranslateY(2);
		
		
		
		
		newNode.getChildren().add(lineA);
		newNode.getChildren().add(lineB);
		
		lb.toFront();
		
		newNode.getChildren().add(lineC);
		newNode.getChildren().add(lineD);
		bt.setGraphic(newNode);
		
		bt.setText("");
		
		lb.setStyle("-fx-text-fill: white;");
	}
	
	private void uncrossNode(Button bt)
	{
		HBox node = (HBox) bt.getGraphic();
		Label lb = (Label) node.getChildren().get(2);
		node.getChildren().clear();
		node.getChildren().add(lb);
	}
	


	private String askForPageName(String pageName, boolean asNewPage)
	{
		boolean valid;
		do
		{
			valid = true;
			
			if (asNewPage)
				pageName = GuiMsgHelper.getTextDirect("Please type a name for the new page.", "A short name is recommended.\nDo not use an already existing name.", Character.toString((char) ( ascA+pages ) ));
			else
				pageName = GuiMsgHelper.getTextDirect("Please type a new name for the page.", "A short name is recomended.\nDo not use an already existing name.", pageName);
			
			if (ProductionGUI.getVisualizedProgram().containsPage(pageName)) valid = false;
		}
		while(!valid);
		
		return(pageName);
	}


	private void removePage(String pageName)
	{
		if (pages <= 1)
			GuiMsgHelper.showMessageNonblockingUI("You cannot delete the only page.");
		else
		if (GuiMsgHelper.askQuestionDirect("Are you sure you want to delete the page named '"+pageName+"'?") == 1)
		{			
			if (currentPage.equals(pageName))
			{
			
				String newCurrentPage = "";
				//Set<String>[] keys = (String[]) pageButtons.keySet();//.toArray();
				
				List<String> keys = new ArrayList<>();
				for(Entry<String, Button> entr: pageButtons.entrySet()) // For some reason, the toArray function doesn't work as supposed)
					keys.add(entr.getKey());
				
				for(String name: keys)
				{
					if (pageName.equals(name))
						break;
					newCurrentPage = name;
				}
				if (newCurrentPage.isEmpty())
					newCurrentPage = keys.get(1);
							
				currentPage = newCurrentPage;
				
				Platform.runLater(() -> clickPage(currentPage, false));
			}
			
			Platform.runLater(() -> {
				
				topBox.getChildren().remove( pageButtons.get(pageName).getParent() );
				pageButtons.remove(pageName);
				pagesActive.remove(pageName);
				ProductionGUI.getVisualizedProgram().removePage(pageName);
				
				pages--;
				
				refreshPagesMenu();
			});
			
		}
	}
	
	
	private void clickPage(String pageName, boolean rightclick)
	{
		if (!pagesNotClickable && newPagesFlashingRectangle.containsKey(pageName)) // Stop flashing
		{
			String pgn = pageName;
			
			FastFadeTransitionHelper.fadeout(newPagesFlashingRectangle.get(pageName), () -> {
				
				Button bt = pageButtons.get(pgn);
				
				if (((HBox) bt.getParent()).getChildren().contains(newPagesFlashingRectangle.get(pgn)))
					((HBox) bt.getParent()).getChildren().remove(newPagesFlashingRectangle.get(pgn));
				
				newPagesFlashingRectangle.remove(pgn);
				
			});
		}
		
		if (doubleClickPage.equals(pageName)) // if doubleClick -> rename
		{
			renamePage(pageName);
			
			return;
		}
		
		
		if (rightclick)
			setPageState(pageName, !pagesActive.get(pageName));
		else
		{
			setCurrentPage(pageName);
			
			switchDataPage(currentPage, true);
			renewElementsRealizationFull();	
		}
	}

	private void renamePage(String oldPageName)
	{		
		String newPageName = askForPageName(oldPageName, false);
			
		if ((newPageName == null) || newPageName.isEmpty()) return;
		
		
		if (currentPage.equals(oldPageName))
			currentPage = newPageName;
		OtherHelpers.replaceMapKeyMaintainingOrder(pageButtons, oldPageName, newPageName);
		OtherHelpers.replaceMapKeyMaintainingOrder(pagesActive, oldPageName, newPageName);
		OtherHelpers.replaceMapKeyMaintainingOrder(newPagesFlashingRectangle, oldPageName, newPageName);
		
		ProductionGUI.getVisualizedProgram().replacePageName(oldPageName, newPageName);
		UndoRedoControler.getSelf().replacePageName(oldPageName, newPageName);
		replacePageName(oldPageName, newPageName);
		
		Button bt = pageButtons.get(newPageName);
		Platform.runLater(() -> {
			
			if (!bt.getText().isEmpty())
				bt.setText(newPageName);
			else
			{
				HBox node = (HBox) bt.getGraphic();
				Label lb;
				if (node.getChildren().size() == 1)
					lb = (Label) node.getChildren().get(0);
				else
					lb = (Label) node.getChildren().get(2);
				lb.setText(newPageName);
			}			
		});
		
		refreshPagesMenu();
	}


	private void setPageState(String pageName, boolean newState)
	{
		if (pagesActive.get(pageName) != newState)
		{
			pagesActive.put(pageName, newState);

			if (newState)
				uncrossNode(pageButtons.get(pageName));				
			else
				crossNode(pageButtons.get(pageName));
		}
		
		if (pageName.equals(currentPage))
			sectionManager.contentPane.setOpacity(newState ? 1 : 0.5);
		
	}


	public void setCurrentPage(String newPage)
	{
		if (!ProductionGUI.getVisualizedProgram().containsPage(newPage))
			InfoErrorHandler.callBugError("Trying to set to a page which does not exist.");
		
		pageButtons.get(currentPage).setStyle("");
		currentPage = newPage;
		pageButtons.get(currentPage).setStyle("-fx-background-color: rgba(0, 100, 255, 0.4);");
		
		sectionManager.contentPane.setOpacity(pagesActive.get(currentPage) ? 1 : 0.5);
	}




	public boolean hasDeactivatedPages()
	{
		return(pagesActive.containsValue(false));		
	}
	public boolean pageIsActive(String page)
	{
		return(pagesActive.getOrDefault(page, true));
	}
	

	public int getPagesCount()
	{
		return(pagesActive.size());
	}
	
	public static String getStartPageName()
	{
		return(startPage);
	}


	public String getCurrentPage()
	{
		return(currentPage);
	}
	
	


	public void deactivateButFlashNewestPage()
	{
		String lastPage = null;
		String firstPage = null;
		
		for(Entry<String, Button> entry: pageButtons.entrySet())
		{
			if (firstPage == null)
				firstPage = entry.getKey();
			else
			{
				lastPage = entry.getKey();
				pagesActive.put(lastPage, false);
				crossNode(pageButtons.get(lastPage));
			}
		}
		
		pagesNotClickable = true;
		
		FxTimer.runLater(
		        Duration.ofMillis(2000),
		        () -> {
		        	pagesNotClickable = false;
		        });
		
		Button bt = pageButtons.get(lastPage);
		
		pagesActive.put(lastPage, false);
		crossNode(bt);
		

		Rectangle attentionRectangle = new Rectangle(1, 1, 1, 1);
		attentionRectangle.setArcHeight(12);
		attentionRectangle.setArcWidth(12);
		attentionRectangle.setManaged(false);
		attentionRectangle.setMouseTransparent(true);

		
		attentionRectangle.widthProperty().bind(bt.widthProperty().subtract(2));
		attentionRectangle.heightProperty().bind(bt.heightProperty().subtract(2));
		
		attentionRectangle.setFill(Color.LIME);
		OtherHelpers.applyOptimizations(attentionRectangle);
		
		
		/*FadeTransition attentionRectangleTransition = new FadeTransition(GlobalSettings.attentionBlinkDuration, attentionRectangle);
		attentionRectangleTransition.setFromValue(GlobalSettings.attentionRectangleMinAlpha);
		attentionRectangleTransition.setToValue(GlobalSettings.attentionRectangleMaxAlpha);
		attentionRectangleTransition.setCycleCount(Timeline.INDEFINITE);
		attentionRectangleTransition.setAutoReverse(true);
		
		
		//pagesFlashingRect.put(pageName, attentionRectangle);
		newPagesFlashingTransition.put(lastPage, attentionRectangleTransition);
		
		
		attentionRectangleTransition.setDuration(GlobalSettings.attentionBlinkDurationFast);
		attentionRectangleTransition.setCycleCount(2);
		attentionRectangleTransition.play();
				
		attentionRectangleTransition.setOnFinished((ActionEvent event) -> attentionRectangleTransition.play());
		*/
		
		
		newPagesFlashingRectangle.put(lastPage, attentionRectangle);
		FastFadeTransitionHelper.fade(attentionRectangle, GlobalSettings.attentionRectangleMinAlpha, GlobalSettings.attentionRectangleMaxAlpha, (long) GlobalSettings.attentionBlinkDuration.toMillis());
		

        
        
		
		
		
		if (!((HBox) bt.getParent()).getChildren().contains(attentionRectangle))
			((HBox) bt.getParent()).getChildren().add(attentionRectangle);
		
		
		clickPage(firstPage, false);
	}

	
	
}
