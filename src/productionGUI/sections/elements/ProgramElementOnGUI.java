package productionGUI.sections.elements;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.reactfx.util.FxTimer;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramTutorialContent;
import dataTypes.contentValueRepresentations.AbstractContentValue;
import dataTypes.minor.FloatSingleton;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import main.functionality.Functionality;
import main.functionality.Structures;
import otherHelpers.DragAndDropHelper;
import otherHelpers.PropertyTransition;
import productionGUI.ProductionGUI;
import productionGUI.additionalWindows.MainInfoScreen;
import productionGUI.additionalWindows.WaitPopup;
import productionGUI.controlers.UndoRedoControler;
import productionGUI.guiEffectsSystem.EffectsClient;
import productionGUI.sections.elementManagers.AbstractSectionManager;
import productionGUI.sections.elementManagers.ActionsSectionManager;
import productionGUI.sections.elementManagers.ConditionsSectionManager;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.elementManagers.EventsSectionManager;
import productionGUI.sections.elementManagers.StructuresSectionManager;
import productionGUI.sections.subelements.SubElement;
import productionGUI.sections.subelements.SubElementField;
import productionGUI.sections.subelements.SubElementName;
import productionGUI.tutorialElements.TutorialControler;
import settings.GlobalSettings;
import settings.ReusableElements;
import staticHelpers.FastFadeTransitionHelper;
import staticHelpers.GuiMsgHelper;
import staticHelpers.KeyChecker;
import staticHelpers.OtherHelpers;
import staticHelpers.TemplateHandler;


@SuppressWarnings("deprecation")
public class ProgramElementOnGUI extends EffectsClient
{
	
	// Static method to realize on the GUI (it creates an instance of this class)
	public static ProgramElementOnGUI realizeOnGUI(VisualizableProgramElement element, AbstractSectionManager targetSectionManager, int listIndex, int depth, DataNode<ProgramElement> node)
	{
		Pane contentPane = targetSectionManager.getContentPane();
		
		ProgramElementOnGUI controler = new ProgramElementOnGUI(element, targetSectionManager, listIndex, depth, node);
		
		TemplateHandler.injectTemplate("/productionGUI/sections/elements/ProgramElementOnGUItemplate.fxml", controler, contentPane);
		
		return(controler);
	}



	@FXML private Pane basePane;
	@FXML private SplitPane elementsSplitPane;
	@FXML private HBox container;
	@FXML private Label childsLabel;
	
	private int listIndex = -1;
	
	private int codeLineIndex = 0;
	
	private int elementHeightWithGap;

	VisualizableProgramElement visualizedElement;
	FunctionalityContent content;
	
	private boolean hiddenChildren = false;
	
	private boolean dragDummy = false;
	private int depth;
	private boolean locked = false;
	private boolean dragAndDropEnabled = false;
	
	private volatile DataNode<ProgramElement> node, nodeRoot;
	
	private List<SubElement> subElements = new ArrayList<>();
	
	private AbstractSectionManager parentSectionManager;
	
	
	private Rectangle attentionRectangle;
	private FadeTransition attentionRectangleTransition;
	
	
	private Tooltip tooltip;
	private Integer[] tooltipState = new Integer[1];
	
	private boolean inContent;
	
	private boolean forTutorial = false;
	private boolean tutTop = false;
	private VBox tutSubBox;
	
	private Button plusOptionalParamButton = null, minusOptionalParamButton = null;
	private Runnable optionalParamPlusButtonTask = null;
	
	private volatile boolean currentlyHidingOrUnhiding = false;
	private ScaleTransition scaleTransition;
	private TranslateTransition positionTransition;
	private ParallelTransition parallelTransition;
	
	
	private Line parentLineA = new Line();
	private Line parentLineB = new Line();
	private Text optionalORifParEvent;
	
	
	private List<Line> additionalParentLines = new ArrayList<>();
	
	private boolean exceptionalRemovable = false;
	
	//private Background savedBackground = null;	
	
	
	private ImageView executionArrow = new ImageView(new Image("/guiGraphics/if_arrow_right_green.png"));
	private FloatSingleton exectionArrowAlpha = new FloatSingleton(1);
	
	
	
	public ProgramElementOnGUI(VisualizableProgramElement visualizedElement, AbstractSectionManager parentSectionManager, int listIndex, int depth, DataNode<ProgramElement> node)
	{
		this.visualizedElement = visualizedElement;
		this.listIndex = listIndex;
		this.content = visualizedElement.getContent();
		this.depth = depth;
		this.node = node;
		//visualizedElement.setItsNode(node);
		
		nodeRoot = node.getRoot();
		codeLineIndex = getIndexInRoot();
		
		this.parentSectionManager = parentSectionManager;
		
		inContent = (parentSectionManager == ContentsSectionManager.getSelf());
		
		if (inContent) /*|| (visualizedElement.getCopiedWithData()))*/  // If the element has been placed in the contents manager
			visualizedElement.setEditable(true);
		else
			visualizedElement.setEditable(false);
	}
	
	
	
	// Constructor for a drag and drop dummy
	public ProgramElementOnGUI(Object element, boolean dragDummy)
	{
		this.visualizedElement = (VisualizableProgramElement) element;
		this.dragDummy = dragDummy;
	}
	
	// Constructor for tutorial
	public ProgramElementOnGUI(VisualizableProgramElement element, DataNode<ProgramElement> node, VBox tutSubBox)
	{
		this.visualizedElement = (VisualizableProgramElement) element;
		this.content = visualizedElement.getContent();
		this.node = node;
		this.tutSubBox = tutSubBox;
		nodeRoot = node.getRoot();
		depth = node.getDepth();
		forTutorial = true;
		visualizedElement.setEditable(false);
		
		tutTop = content.getFunctionalityName().equals("tutorialBase");
	}
	
	

	
	
	
	public void reVisualize(AbstractSectionManager parentSectionManager, int listIndex, int depth, DataNode<ProgramElement> node)
	{
		this.parentSectionManager = parentSectionManager;
		this.listIndex = listIndex;
		this.node = node;
		this.depth = depth;
		
		//visualizedElement.setItsNode(node);

		
		getBasePane().setStyle("");
		getBasePane().setManaged(true);
		getBasePane().setVisible(true);
		
		
		nodeRoot = node.getRoot();
		codeLineIndex = getIndexInRoot();
		
		
		if (hiddenChildren)
			node.hideChildren();
		
		
		if (parentSectionManager.getContentPane().getChildren().contains(basePane)) // if already inserted
		{
			basePane.setManaged(true); // make visible
			basePane.setVisible(true);
		}
		else
			parentSectionManager.getContentPane().getChildren().add(listIndex, basePane); // else add anew
		
		
		visualizedElement.updateOrigin();
		basePane.setTranslateX(depth*GlobalSettings.elementTreeHgap);
		
		
		if (depth > 0)
		{
			
			executionArrow.setManaged(false);
			if (!basePane.getChildren().contains(executionArrow))
				basePane.getChildren().add(executionArrow);
			
			executionArrow.setScaleX(0.3);
			executionArrow.setScaleY(0.35);
			executionArrow.setTranslateX(-78);
			executionArrow.setTranslateY(-26);
			
			exectionArrowAlpha.value = 0;
			executionArrow.setOpacity(0);

		}
		
		
		if (!dragDummy && !forTutorial)
		{
			unmarkAsChildrenParent();
			
			if (!forTutorial)
				updateParentLines();
			
			if (visualizedElement.isDraggable()) //  ((node.isLeaf() || node.hasChildrenHidden()) && (visualizedElement.isDraggable()))
			{
				if (!dragAndDropEnabled)
					initDragAndDrop();			
			}
			else
				if (dragAndDropEnabled)
					disableDragAndDrop();
			
			if (!node.getChildrenAlways().isEmpty())
				if (node.hasChildrenHidden())
					markAsChildrenParent();
			
			for(SubElement ele: subElements)
				if (ele instanceof SubElementField)
					((SubElementField) ele).revisualized();
			
			/*
			if (depth == 0)
				if (!visualizedElement.isEvent())
				if (!visualizedElement.getIsUserModifiableNode())
					visualizedElement.getContent().setOutcommented(true);*/
			
			updateOutcommentedState(false);
		}
	}
	

	
	@FXML
	protected void initialize()
	{	
	    scaleTransition = new ScaleTransition(javafx.util.Duration.millis(GlobalSettings.smoothDragTransition ? GlobalSettings.collapseDuration : 1), getBasePane());
	    positionTransition = new TranslateTransition(javafx.util.Duration.millis(GlobalSettings.smoothDragTransition ? GlobalSettings.collapseDuration : 1), getBasePane());
	    TranslateTransition additional = new TranslateTransition(javafx.util.Duration.millis(GlobalSettings.smoothDragTransition ? GlobalSettings.collapseDuration : 1), getBasePane());
	    getBasePane().setUserData(additional);
	    
	    scaleTransition.setAutoReverse(false);
	    positionTransition.setAutoReverse(false);
	    positionTransition.setAutoReverse(false);
	    
	    parallelTransition = new ParallelTransition();
	    parallelTransition.getChildren().addAll(scaleTransition, positionTransition);
	    parallelTransition.setAutoReverse(false);
	    
		
		OtherHelpers.applyOptimizations(container);
		
		unmarkAsChildrenParent();
		
		visualizedElement.updateOrigin();
		
		basePane.setTranslateX(depth*GlobalSettings.elementTreeHgap);
		
		if (forTutorial)
		{
			basePane.setTranslateX(GlobalSettings.elementTreeHgap);
			
		if (!tutTop && !content.getFunctionalityName().equals("tutorialStart"))
			basePane.setMouseTransparent(true);
		}
		
		if (!dragDummy)
		{			
			updateOutcommentedState(false);

			
			basePane.getChildren().add(parentLineA);
			basePane.getChildren().add(parentLineB);			
			if (content.isEvent())
			{
				optionalORifParEvent = new Text("OR");
				optionalORifParEvent.getStyleClass().add("reallySmallText");
				optionalORifParEvent.setFill(Color.BLACK); // TODO: Find the right tyle class to sue the variable
				optionalORifParEvent.setMouseTransparent(true);
				optionalORifParEvent.setManaged(false);
			}
			
			parentLineA.setMouseTransparent(true);
			parentLineB.setMouseTransparent(true);
			
			parentLineA.setManaged(false);
			parentLineB.setManaged(false);
			
			
			if (!forTutorial)
				updateParentLines();
			
			
			tooltip = visualizedElement.makeToolTip();
			
	    	tooltipState[0] = 0;
	    	
	    		    	
	    	
	    	if (!forTutorial)
			container.setOnMouseEntered(new EventHandler<MouseEvent>() {
				
			    @Override
			    public void handle(MouseEvent event)
			    {			    	
			    	tooltipState[0] = 1;
			    	FxTimer.runLater(
			    	        Duration.ofMillis(GlobalSettings.tooltipDelay),
			    	        () -> {
			    	        	if ((tooltipState[0] > 0) && ProductionGUI.getStage().isFocused())
			    	        	{
			    	        		if (tooltip != null)
			    	        		{
			    	        			
								        Point2D p = basePane.localToScreen(0, basePane.getLayoutBounds().getMaxY());
								        double x = event.getScreenX()+20;
								        double y = p.getY();
								        
								        if ((x + tooltip.getWidth()) > ProductionGUI.getPrimaryScreenBounds().getWidth())
									        x = x - 40 - tooltip.getWidth();
								        
								        tooltip.show(basePane, x, y);
								        if ((p.getY() + tooltip.getHeight()) > ProductionGUI.getPrimaryScreenBounds().getHeight())
								        	tooltip.setY(p.getY() - tooltip.getHeight() - 30);
			    	        			
			    	        			/*
								        Point2D p = basePane.localToScreen(0, basePane.getLayoutBounds().getMaxY());
								        tooltip.show(basePane, event.getScreenX(), p.getY());
								        if ((p.getY() + tooltip.getHeight()) > ProductionGUI.getPrimaryScreenBounds().getHeight())
								        	tooltip.setY(p.getY() - tooltip.getHeight() - 30);
								        	*/
								        	
			    	        		}
			    	        		
							        tooltipState[0] = 0;
			    	        	}
			    	        });
			    }
			});
	    	else
			if (tutTop)
			container.setOnMouseEntered((MouseEvent) -> MainInfoScreen.enteredTutElement(((ProgramTutorialContent)visualizedElement.getContent()).getTut()));
	    	
	    	if (tutTop)
	    		container.setOnMouseExited((MouseEvent) -> MainInfoScreen.exitedTutElement(((ProgramTutorialContent)visualizedElement.getContent()).getTut()));
	    	else
	    		container.setOnMouseExited((MouseEvent event) -> hideToolTip());
	    	
			
	    	if (!forTutorial || tutTop)
			elementsSplitPane.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent e)
				{
					if (GlobalSettings.guiDebug)
						InfoErrorHandler.printDirectMessage("Info for '" + visualizedElement.getName() +" ("+getContent().getFunctionalityName() + ")': " + visualizedElement.getEclipseCodeLink());
					
					if (tutTop)
						MainInfoScreen.exitedTutElement(((ProgramTutorialContent)visualizedElement.getContent()).getTut());
					
					// LEFT CLICK
			    	if (e.getButton() == MouseButton.PRIMARY)
			    	{
						boolean focused = false;
						for(SubElement subel: subElements)
							if (subel.isMarked())
								focused = true;
						
						if (!focused)
							pressedWhole();
			    	}
			    	else
			    	if (e.getButton() == MouseButton.MIDDLE)
			    		swapOrientation();
			    	
			    	// RIGHT CLICK
			    	else // MouseButton.SECONDARY; RIGHT CLICK
			    	{
			    		//if (!ContentsSectionManager.getSelf().markedContents.isEmpty())
		    			if (KeyChecker.isDown(KeyCode.SHIFT))
		    			{
		    				content.setBreakpoint(!content.isBreakpoint());
		    				updateBreakpointState();
		    			}
		    			else
		    			{
		    				if (!ContentsSectionManager.markedContents.contains(getContent()))
		    					ContentsSectionManager.markedContents.add(getContent());
		    				if (visualizedElement.getOriginSection() != null) // TODO: Try without in the Electronics field
		    				if (!content.isUndeletable() || visualizedElement.getNode().getChildrenAlways().isEmpty())
		    					visualizedElement.getOriginSection().getMarkingRectangleControler().triggerExternally(e.getSceneX(), e.getSceneY());
		    			}
			    		
			    		/*
			    		else
			    		if (commentStateChangeAllowed())
			    		{
			    			if (!KeyChecker.isDown(KeyCode.SHIFT))
			    			{
			    				boolean isOut = content.isOutcommented();
				    			content.setOutcommented(!isOut); // shift not pressed -> outcomment
				    			StaticAbstractSectionManagerHelper.outcommentElements(visualizedElement.getControlerOnGUI(), !isOut);
				    			updateOutcommentedState(true);	
			    			}
			    			else
			    			{
			    				content.setBreakpoint(!content.isBreakpoint());
			    				updateBreakpointState();
			    			}
			    			
			    		}
			    		*/
			    	}
			    	
				}
			});
	    	
	    	/*
	    	if (forTutorial && content.getFunctionalityName().equals("tutorialStart"))
				elementsSplitPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
	
						@Override
						public void handle(MouseEvent e)
						{
							ProductionGUI.toFront();
							WaitPopup pop = new WaitPopup("Loading Tutorial: \n" + ((ProgramTutorialContent) content).getTut().getName());
							
							TutorialControler.endTutorial(false);
							
							FxTimer.runLater(
							        Duration.ofMillis(450),
							        () -> {
							        	Platform.runLater(() ->
							        	{
								        	((ProgramTutorialContent) content).getTut().resolve();
								        	TutorialControler.launchTutorial(((ProgramTutorialContent) content).getTut());
								        	pop.close();
							        	});
							        });
						}
				});
			*/
			
		}
		
		
		boolean noDarkenRoot = (parentSectionManager == ContentsSectionManager.getSelf());
	
		// Set color based on the origin
		if (visualizedElement.getOriginSectionClass() == ActionsSectionManager.class)
			container.getStyleClass().add((depth>0 || noDarkenRoot) ? "actionColors" : "actionColorsDarkened");
		else
		if (visualizedElement.getOriginSectionClass() == ConditionsSectionManager.class)
			container.getStyleClass().add((depth>0 || noDarkenRoot) ? "conditionColors" : "conditionColorsDarkened");
		else
		if (visualizedElement.getOriginSectionClass() == EventsSectionManager.class)
			container.getStyleClass().add((depth>0 || noDarkenRoot) ? "eventColors" : "eventColorsDarkened");
		else
		if (visualizedElement.getOriginSectionClass() == StructuresSectionManager.class)
			container.getStyleClass().add((depth>0 || noDarkenRoot) ? "variableColors" : "variableColorsDarkened");
		
		if ((content != null && (content.getSpecial() == Functionality.Comment)) || forTutorial)
			container.getStyleClass().add("commentColors");


		
		subElements.add( SubElement.realizeOnGUI(elementsSplitPane, visualizedElement.getName(), visualizedElement.getRequiresRaspberry()));		
		
		
		if (forTutorial)
    		tutInit();
		
		if (!dragDummy)
		{
		
			FxTimer.runLater(Duration.ofMillis(600), () -> {
				//elementHeight = (int) basePane.getHeight();
				elementHeightWithGap = (int) basePane.getHeight()+GlobalSettings.elementsVgapWhole;
				
				
				attentionRectangle = new Rectangle(1, 1, container.getWidth()-2, container.getHeight()-2);
				attentionRectangle.setArcHeight(5);
				attentionRectangle.setArcWidth(5);
				attentionRectangle.setManaged(false);
				attentionRectangle.setMouseTransparent(true);
				
				attentionRectangle.widthProperty().bind(container.widthProperty().subtract(2));
				attentionRectangle.heightProperty().bind(container.heightProperty().subtract(2));
				
				
				attentionRectangleTransition = new FadeTransition(GlobalSettings.attentionBlinkDuration, attentionRectangle);
				attentionRectangleTransition.setFromValue(GlobalSettings.attentionRectangleMinAlpha);
				attentionRectangleTransition.setToValue(GlobalSettings.attentionRectangleMaxAlpha);
				attentionRectangleTransition.setCycleCount(Timeline.INDEFINITE);
				attentionRectangleTransition.setAutoReverse(true);
			});
			
			
			
			
			// End if the element is !draggable, meaning it is only existing for visual structure and doesn't represent an ElementContent. 
			if (!visualizedElement.isDraggable())
			{
				updateDashedBottom();
				return;
			}

			
			createSubelements();
			
			handleSpecialElements();
			
			
			if (visualizedElement.expandableArgumentsPossible())
			{
				HBox outerBox = new HBox();
				
				Button btp = new Button("+");
				btp.getStyleClass().add("programPageButton");
				if (!visualizedElement.canExpand())
					btp.setDisable(true);
				HBox box = new HBox(btp);
				box.setTranslateY(2);
				outerBox.getChildren().add(box);
				//container.getChildren().add(box);
				
				plusOptionalParamButton = btp;
				
				Button btm = new Button("–");
				btm.getStyleClass().add("programPageButton");
				btm.setTranslateY(2);
				if (!content.hasOptionalArgument(0))
					btm.setDisable(true);
				//container.getChildren().add(btm);
				outerBox.getChildren().add(btm);
				
				container.getChildren().add(outerBox);
				
				
				minusOptionalParamButton = btm;
				
				btp.setOnAction(new EventHandler<ActionEvent>() {
				    @Override
				    public void handle(ActionEvent e)
				    {
				    	if (Execution.isRunning())
				        {
				    		new Thread(() -> GuiMsgHelper.showMessageNonblockingUI("Please stop the program first!")).start();
				    		return;
				        }
				    	
				        if (!visualizedElement.expandArgument(true))
				        	btp.setDisable(true);
				        
				        createSubelements();
				        handleSpecialElements();
				        
				        btm.setDisable(false);
				        
				        if (optionalParamPlusButtonTask != null)
				        {
				        	optionalParamPlusButtonTask.run();
				        	optionalParamPlusButtonTask = null;
				        	
				        	HBox par = ((HBox) plusOptionalParamButton.getParent());
				        	
				        	for(Node node: par.getChildren())
				        		if (node instanceof Rectangle)
				        			FastFadeTransitionHelper.stopInstantly(node);
				        	
				        	par.getChildren().clear();
				        	par.getChildren().add(plusOptionalParamButton);
				        }
				        
				        parentSectionManager.adjustSubelementsSize();
				        parentSectionManager.adjustContainerSize();
				        
				        renewElementSplitPane();
				        
				    }
				});
				
				
				btm.setOnAction(new EventHandler<ActionEvent>() {
				    @Override public void handle(ActionEvent e) {
				    	
				    	if (Execution.isRunning())
				        {
				    		new Thread(() -> GuiMsgHelper.showMessageNonblockingUI("Please stop the program first!")).start();
				    		return;
				        }
				    	
				        if (!visualizedElement.removeExpandedArgument())
				        	btm.setDisable(true);
				        
				        createSubelements();
				        handleSpecialElements();
				        

				        btp.setDisable(false);
				        
				        parentSectionManager.adjustSubelementsSize();
				        parentSectionManager.adjustContainerSize();
				        
				        renewElementSplitPane();
				    }
				});
			
			}

			
			//if (node.isLeaf() || node.hasChildrenHidden())
				initDragAndDrop();			
			
			if (node.hasChildrenHidden())
				markAsChildrenParent();
			
			basePane.setVisible(false);
			boolean hasLoaded = ProductionGUI.hasLoadedFile();
			FxTimer.runLater(Duration.ofMillis(75), () -> { if (hasLoaded) {parentSectionManager.adjustSubelementsSize(); parentSectionManager.adjustContainerSize();} basePane.setVisible(true);});
			
			
			if (depth == 0)
				if (!visualizedElement.isEvent())
				if (!visualizedElement.getIsUserModifiableParentNode())
				{
					//visualizedElement.getContent().setOutcommented(true);
					updateOutcommentedState(false);
				}
			
			updateBreakpointState();
		}
		else
		{
			subElements.get(0).getContainer().getChildren().get(0).getStyleClass().removeAll("elementContentText");
			subElements.get(0).getContainer().getChildren().get(0).getStyleClass().add("elementContentTextLarge");
			subElements.get(0).setWidth(subElements.get(0).getContentWidth()+20);
		}

		
		
		updateDashedBottom();
	}
	
	
	
	
	public boolean commentStateChangeAllowed()
	{
		if ((node.getParent()).getData() != null)
		{
			if (node.getParent().getData().getContent().isOutcommented())
				return(false);
			
			if (node.getParent().getData().getContent().isUndeletable())
				return(false);
		}
		if (content.isUndeletable())
			return(false);

		return(true);
	}



	private void renewElementSplitPane()
	{
		elementsSplitPane.getItems().remove(1, elementsSplitPane.getItems().size());
		
		boolean hasValueElements = visualizedElement.getEditable() || visualizedElement.getCopiedWithData();
		
		if (elementsSplitPane.getOrientation() == Orientation.VERTICAL)
		{
			for(int i = 1; i < subElements.size(); i += hasValueElements ? 2 : 1)
			{
				HBox b = new HBox();
				b.getChildren().add(subElements.get(i).getContainer());
				if (hasValueElements)
					b.getChildren().add(subElements.get(i+1).getContainer());							
				
				if (!GlobalSettings.dynamicElementWidth)
				if (hasValueElements)
					subElements.get(i+1).setLimitedWidth(GlobalSettings.optimalSubelementFieldWidth, GlobalSettings.optimalSubelementFieldWidth*6);
					//subElements.get(i+1).setWidth(Label.USE_COMPUTED_SIZE); //GlobalSettings.optimalSubelementFieldWidth*2);
				
				elementsSplitPane.getItems().add(b);
			}
			
			if (minusOptionalParamButton != null)
				if (subElements.size() > 1)
				{
					//minusOptionalParamButton.setManaged(false);
					//minusOptionalParamButton.setTranslateX(plusOptionalParamButton.getLayoutX()+plusOptionalParamButton.getWidth()+1);
					//.setTranslateY(-23);
				}
		}
		else
		{
			for(int i = 1; i < subElements.size(); i++)
			{
				elementsSplitPane.getItems().add(subElements.get(i).getContainer());
				if (!GlobalSettings.dynamicElementWidth)
					subElements.get(i).setWidth(GlobalSettings.optimalSubelementFieldWidth);
			}
			
			if (minusOptionalParamButton != null)
			{
				//minusOptionalParamButton.setManaged(true);
				//minusOptionalParamButton.setTranslateX(0);
			}
		}
		
		// Required for some reason to achieve constant positioning
        for(Divider div: elementsSplitPane.getDividers())
        {
        	div.setPosition(div.getPosition()+0.1);
        	div.setPosition(div.getPosition());
        }
	}



	private void createSubelements()
	{
		// Clear
		while(subElements.size()>1)
			subElements.remove(1);
		while(elementsSplitPane.getItems().size()>1)
			elementsSplitPane.getItems().remove(1);
		
		AbstractContentValue[] argumentsData = visualizedElement.getArgumentsData();
		String[] argumentDescr = visualizedElement.getArgumentDescriptions();
		
		
		int argCount = argumentDescr.length;
		
		if (argCount != argumentsData.length)
			InfoErrorHandler.callPrecompilingError("The visualization of " + visualizedElement.getName() + " has a different number of argument-descriptions than arguments!");
		
		
		for(int i = 0; i<argCount; i++)
		{
			if (argumentsData[i] == null)
				if (argumentDescr[i] != null)
					InfoErrorHandler.callPrecompilingError("Functionality problem for '" + content.getFunctionalityName() + "'\nThe argument '" + argumentDescr[i] + "' is missing!");
				else
					InfoErrorHandler.callPrecompilingError("Functionality problem for '" + content.getFunctionalityName() + "'\nThe argument number '" + i + "' is missing!\nPossibly reserved more argument space by the object array, than applied for visualisation?");
			
			// Show the description of the field
			SubElement el = SubElement.realizeOnGUI(elementsSplitPane, argumentDescr[i], false);
			if (visualizedElement.getEditable() || visualizedElement.getCopiedWithData())
				el.alignToRight(true);
			else
				el.alignToCenter();
			subElements.add(el);
			
			
			// Show the content of the field
			if (visualizedElement.getEditable() || visualizedElement.getCopiedWithData())
			{
				SubElementField field = SubElement.realizeOnGUI(elementsSplitPane, argumentsData[i], visualizedElement.getEditable(), this);
				
				if(argumentsData[i].canBeEditedByElement())
					field.applyHighlightedType(ReusableElements.alteringVariableHighlight, true);
				
				subElements.add( field );
			}
		}
		
		if (!GlobalSettings.dynamicElementWidth)
		{
			for(SubElement sub: subElements)
				sub.setWidth(GlobalSettings.optimalSubelementFieldWidth);
			subElements.get(0).setWidth(GlobalSettings.maxSubelementFieldWidth);
		}
		subElements.get(0).getContainer().getChildren().get(0).getStyleClass().removeAll("elementContentText");
		subElements.get(0).getContainer().getChildren().get(0).getStyleClass().add("elementContentTextLarge");
		
		
		// Required for some reason to achieve constant positioning
        for(Divider div: elementsSplitPane.getDividers())
        {
        	div.setPosition(div.getPosition()+0.1);
        	div.setPosition(div.getPosition());
        }
	}


	public int getSubelementsCount()
	{
		return(subElements.size());
	}
	
	public SubElement getSubelement(int index)
	{
		if (index >= getSubelementsCount())
			return(null);
		else
			return(subElements.get(index));
	}
	public int getSubelementIndex(SubElement sub)
	{
		return(subElements.indexOf(sub));
	}
	
	
	public void hideToolTip()
	{
        if (tooltip != null) tooltip.hide();
        tooltipState[0] = 0;
	}
	
	
	
	
	
	private void handleSpecialElements()
	{
		if (content != null)
		switch(content.getSpecial())
		{
		case Functionality.Comment:
			if (!content.isUndeletable() || dragDummy)
			{
				elementsSplitPane.getItems().remove(subElements.get(0).getContainer());
				if (!visualizedElement.getArgumentsData()[0].hasContent())
					((SubElementField) subElements.get(2)).applyString(visualizedElement.getArgumentDescriptions()[0]);
				subElements.get(2).alignToCenter();

				((SubElementField) subElements.get(2)).setWidth(GlobalSettings.commentFieldWidth);
				
				((SubElementField) subElements.get(2)).getContainer().getChildren().get(0).getStyleClass().removeAll("elementContentText");
				((SubElementField) subElements.get(2)).getContainer().getChildren().get(0).getStyleClass().add("elementContentTextLarge");
			}			
			elementsSplitPane.getItems().remove(subElements.get(1).getContainer());
			
		break;
		
		}
		
	}

	
	
	
	
	private void initDragAndDrop()
	{
		
		String identifier = "";	
		
		if (parentSectionManager == ContentsSectionManager.getSelf())
			identifier = ContentsSectionManager.getSelf().getClass().getName();

		if (parentSectionManager == ActionsSectionManager.getSelf())
			identifier = ActionsSectionManager.getSelf().getClass().getName();
		if (parentSectionManager == ConditionsSectionManager.getSelf())
			identifier = ConditionsSectionManager.getSelf().getClass().getName();
		if (parentSectionManager == EventsSectionManager.getSelf())
			identifier = EventsSectionManager.getSelf().getClass().getName();
		if (parentSectionManager == StructuresSectionManager.getSelf())
			identifier = StructuresSectionManager.getSelf().getClass().getName();
		
		
		Runnable dragEvent = new Runnable()
		{
			@Override
			public void run()
			{
				parentSectionManager.getSectionManager().getScrollPane().setHvalue(0);	
				
				DragAndDropHelper.setSavedPane(basePane);
				//fadeoutMarking();
				
				TutorialControler.setWindowTransparent(true);
				
				if (KeyChecker.isDown(KeyCode.SHIFT)) // move without removing the old -> copy
				{
					KeyChecker.cancelDown(KeyCode.SHIFT);
					DragAndDropHelper.setAttribute(GlobalSettings.noOrigRemovalAttribute, true);
				}
				else
				if (KeyChecker.isDown(KeyCode.CONTROL))
				{
					exceptionalRemovable = true;
					KeyChecker.cancelDown(KeyCode.CONTROL);						
				}
				else
					exceptionalRemovable = false;
				
				if (!visualizedElement.getContent().isUndeletable())
				{
					basePane.setStyle("-fx-opacity: 0.5");
					for(FunctionalityContent cont: AbstractSectionManager.markedContents)
						cont.getVisualization().getControlerOnGUI().getBasePane().setStyle("-fx-opacity: 0.5");
					
					for(DataNode<ProgramElement> nd: node.getChildren())
						nd.getData().getContent().getVisualization().getControlerOnGUI().getBasePane().setStyle("-fx-opacity: 0.5");
				}
				
				
				AbstractSectionManager[] abstr = new AbstractSectionManager[1];
				AbstractSectionManager.applyForAll(abstr , () -> {
					
					ProgramElement[] nodeV = new ProgramElement[1];
					abstr[0].getRootElementNode().applyToChildrenTotal(nodeV , () -> {
						((VisualizableProgramElement) nodeV[0]).getControlerOnGUI().normalizeOrientation();
					}, true);					
				});
				
			}
		};
		
		Runnable endEvent = new Runnable()
		{
			@Override
			public void run()
			{
				basePane.setStyle("");
				basePane.setManaged(true);
				basePane.setVisible(true);
				
				TutorialControler.setWindowTransparent(false);
				
				
				// Deleting the element(s)!
				if ((((visualizedElement.getEditable() || (exceptionalRemovable)) && !visualizedElement.getContent().isUndeletable())) && !DragAndDropHelper.getAttribute(GlobalSettings.noOrigRemovalAttribute))
				{	
					UndoRedoControler.getSelf().appliedChange(content.getCodePageName(), false);
					
					AbstractSectionManager.removeCurrentMarkedElements();
					
					parentSectionManager.renewElementsRealization();
					parentSectionManager.adjustSubelementsSize();
					parentSectionManager.adjustContainerSize();
					
					UndoRedoControler.getSelf().appliedChange(content.getCodePageName(), true);
				}
				else
					parentSectionManager.renewElementsRealization();				
				
				FxTimer.runLater(
				        Duration.ofMillis(250),
				        () -> DragAndDropHelper.clearAttribute(GlobalSettings.noOrigRemovalAttribute));
				
				parentSectionManager.resetPreviewPos();
			}
		};

		
		DragAndDropHelper.asignDragSource(elementsSplitPane, identifier, visualizedElement, "/productionGUI/sections/elements/ProgramElementOnGUItemplate.fxml", ProgramElementOnGUI.class, TransferMode.MOVE, dragEvent, endEvent);

		
		dragAndDropEnabled = true;
	}
	
	
	protected void disableDragAndDrop()
	{
		DragAndDropHelper.deasignDragSource(elementsSplitPane);
		
		dragAndDropEnabled = false;
	}
	
	private void swapOrientation()
	{
		elementsSplitPane.setOrientation((elementsSplitPane.getOrientation() == Orientation.VERTICAL) ? Orientation.HORIZONTAL : Orientation.VERTICAL);			
		renewElementSplitPane();		
	}
	
	private void normalizeOrientation()
	{
		if (elementsSplitPane.getOrientation() == Orientation.VERTICAL)
		{
			elementsSplitPane.setOrientation(Orientation.HORIZONTAL);			
			renewElementSplitPane();
		}
	}
	
	
	
	PropertyTransition min, max;
	
	private void pressedWhole()
	{
		if (KeyChecker.isDown(KeyCode.SHIFT))
		{
			swapOrientation();
			return;
		}
		
		pressedWhole(false, false);
	}
	public void pressedWhole(boolean closeOnly, boolean excludeForTut)
	{
		if (scaleTransition != null && scaleTransition.getCurrentRate() != 0) return;
		if (positionTransition != null && positionTransition.getCurrentRate() != 0) return;		
		
		if (currentlyHidingOrUnhiding)
			return;
		
		
		ObservableList<Node> nd = null;
		if (!forTutorial)
			nd = parentSectionManager.getContentPane().getChildren();
		final ObservableList<Node> sectionChilds = nd;
		
		
		int childCount = 0;
		
		elementHeightWithGap = (int) basePane.getHeight() + GlobalSettings.elementsVgapWhole;
		
		if (!node.isLeaf())
			if (node.hasChildrenHidden())
			{
				if (closeOnly) return;
				
				node.unhideChildren();
				
				if ((parentSectionManager != ContentsSectionManager.getSelf()) && (!TutorialControler.handleElementOpen(visualizedElement)))
				{
					node.hideChildren();
					return;
				}
				
				fadeoutMarking();
				
				
				hiddenChildren = false;
				
				unmarkAsChildrenParent();
				node.unhideChildren();
				
				if (forTutorial)
				{
					if (tutSubBox != null)
						MainInfoScreen.showSubTutBox(tutSubBox);
				}
				
				
				List<VisualizableProgramElement> childrenElements = getChildrenElements();
				childCount = childrenElements.size();
				
				if (!forTutorial)
				{
					parentSectionManager.renewElementsRealization();
					parentSectionManager.adjustSubelementsSize();
					currentlyHidingOrUnhiding = true;
				}
				
				
				double ind = 0.5;
				for (VisualizableProgramElement ele: childrenElements)
				{
					ele.getControlerOnGUI().transitExternally(0, 1, -ind*elementHeightWithGap, 0);
					ind++;
				    
				    if (content.isOutcommented())
				    	ele.getContent().setOutcommented(true);
				    ele.getControlerOnGUI().updateOutcommentedState(true);
				}
				
				
				if (forTutorial && !excludeForTut)
				{
					MainInfoScreen.forceCloseAllTutorialInfosOnThisRowExcept(this);
					
					if (visualizedElement.getTutorialTreeRowPlaceHolder().minHeightProperty().get() < 2*elementHeightWithGap)
					{
						//int h = (childrenElements.size()+1) * elementHeightWithGap;
						int h = 6 * elementHeightWithGap;
						
						if (min != null)
						{
							min.stop();
							max.stop();
						}
						
						min = new PropertyTransition(javafx.util.Duration.millis(GlobalSettings.smoothDragTransition ? GlobalSettings.collapseDuration : 1), visualizedElement.getTutorialTreeRowPlaceHolder().minHeightProperty());
						min.setFrom(elementHeightWithGap);
						min.setTo(h);
						min.play();
					    
						max = new PropertyTransition(javafx.util.Duration.millis(GlobalSettings.smoothDragTransition ? GlobalSettings.collapseDuration : 1), visualizedElement.getTutorialTreeRowPlaceHolder().maxHeightProperty());
						min.setFrom(elementHeightWithGap);
						min.setTo(h);
						max.play();
					}
				}
				
				if (!forTutorial)
				for(int i = listIndex + childCount +1; i < sectionChilds.size(); i++)
				{
					TranslateTransition tran = (TranslateTransition) sectionChilds.get(i).getUserData();
					tran.setFromY(-childCount * elementHeightWithGap);
					tran.setToY(0);
					tran.setAutoReverse(false);
					tran.play();
				}
				
				
				int childCountF = childCount;
				
				if (!forTutorial)
				FxTimer.runLater(Duration.ofMillis(GlobalSettings.smoothDragTransition ? GlobalSettings.collapseDuration : 1),() -> {
					
					for(int i = listIndex + childCountF +1; i < sectionChilds.size(); i++)
					{
						sectionChilds.get(i).setTranslateY(0);
					}
					
					parentSectionManager.renewElementsRealization();
					parentSectionManager.adjustSubelementsSize();
					
					currentlyHidingOrUnhiding = false;
				});
				
				
			}
			else
			{					
				if (TutorialControler.tutorialRunning() && (parentSectionManager != ContentsSectionManager.getSelf()))
					return;
				
				hiddenChildren = true;
				
				List<VisualizableProgramElement> childrenElements = getChildrenElements();
				childCount = childrenElements.size();

				markAsChildrenParent();
				node.hideChildren();
				
				if(!forTutorial)
					currentlyHidingOrUnhiding = true;
				
				
				double ind = 0.5;
				for (VisualizableProgramElement ele: childrenElements)
				{
					ele.getControlerOnGUI().transitExternally(1, 0, 0, -ind * elementHeightWithGap);					
					ind++;
				}
				
				
				if (forTutorial && !excludeForTut)
				{
					//int h = (childrenElements.size()+1) * elementHeightWithGap;
					int h = 6 * elementHeightWithGap;

					
					if (min != null)
					{
						min.stop();
						max.stop();
					}
					
					min = new PropertyTransition(javafx.util.Duration.millis(GlobalSettings.smoothDragTransition ? GlobalSettings.collapseDuration : 1), visualizedElement.getTutorialTreeRowPlaceHolder().minHeightProperty());
					min.setFrom(h);
					min.setTo(elementHeightWithGap - GlobalSettings.elementsVgapWhole);
					min.play();
					
					max = new PropertyTransition(javafx.util.Duration.millis(GlobalSettings.smoothDragTransition ? GlobalSettings.collapseDuration : 1), visualizedElement.getTutorialTreeRowPlaceHolder().maxHeightProperty());
					min.setFrom(h);
					min.setTo(elementHeightWithGap - GlobalSettings.elementsVgapWhole);
					max.play();
				}
				
				
				if (!forTutorial)
				for(int i = listIndex + childCount+1; i < sectionChilds.size(); i++)
				{
					TranslateTransition tran = (TranslateTransition) sectionChilds.get(i).getUserData();
					tran.setFromY(0);
					tran.setToY(-childCount * elementHeightWithGap);
					tran.setAutoReverse(false);
					tran.play();
				}
				
				
				int childCountF = childCount;
				
				FxTimer.runLater(Duration.ofMillis(GlobalSettings.smoothDragTransition ? GlobalSettings.collapseDuration : 1),() ->
				{
					if (forTutorial)
					{
						if (tutSubBox != null)
							MainInfoScreen.hideSubTutBox(tutSubBox);
					}
					else
					{					
						for(int i = listIndex + childCountF+1; i < sectionChilds.size(); i++)
						{
							sectionChilds.get(i).setTranslateY(0);
						}
						
						parentSectionManager.renewElementsRealization();
						parentSectionManager.adjustSubelementsSize();
						
						currentlyHidingOrUnhiding = false;
					}
				
				});
				
			}
	
		updateDashedBottom();
	}
	
	private void transitExternally(double fromScale, double toScale, double fromPos, double toPos)
	{
		parallelTransition.stop();
		
		if (!GlobalSettings.smoothDragTransition)
		{
			getBasePane().setScaleY(toScale);
			getBasePane().setTranslateY(toPos);			
			return;
		}
		
		scaleTransition.setFromY(fromScale);
		scaleTransition.setToY(toScale);

	    positionTransition.setFromY(fromPos);
	    positionTransition.setToY(toPos);
	    
	    parallelTransition.play();
	}


	private void updateDashedBottom()
	{
		if (node != null)
		{
			if (node.hasChildrenHidden())
				container.getStyleClass().removeAll("dashedBottom");
			else
				if (content.canHaveChildElements())
					container.getStyleClass().add("dashedBottom");
			
			if (!forTutorial || tutTop)
				if (!visualizedElement.isDraggable())
					if (!node.hasChildrenHidden())
						container.getStyleClass().add("dashedBottom");
		}

	}
	
	/*
	public void forceCollapse()
	{
		forceCollapse(false);
	}*/
	public void forceCollapse(boolean alwaysForce)
	{
		forceCollapse(alwaysForce, false);
	}
	public void forceCollapse(boolean alwaysForce, boolean skipExternalUpdate)
	{
		if (!alwaysForce)
		if (content.isCollapsedInitialized())
			content.setCollapsedInitialized(false);
		else return;
		
		
		elementHeightWithGap = (int) basePane.getHeight()+GlobalSettings.elementsVgapWhole;
		
		ObservableList<Node> sectionChilds = null;
		if (!forTutorial)
			sectionChilds = parentSectionManager.getContentPane().getChildren();
		hiddenChildren = true;
		
		List<VisualizableProgramElement> childrenElements = getChildrenElements();
		int childCount = childrenElements.size();

		markAsChildrenParent();
		node.hideChildren();
		
		
		double ind = 0.5;
		for (VisualizableProgramElement ele: childrenElements)
		{
		    ele.getControlerOnGUI().getBasePane().setScaleY(0);
		    ele.getControlerOnGUI().getBasePane().setTranslateY(-ind * elementHeightWithGap);
		    ind++;
		}
		
		
		if (!forTutorial)		
		for(int i = listIndex + childCount+1; i < sectionChilds.size(); i++)
		{
			sectionChilds.get(i).setTranslateY(-childCount * elementHeightWithGap);
		}
		
		
		int childCountF = childCount;
		
		if (!forTutorial)
		{
			for(int i = listIndex + childCountF+1; i < sectionChilds.size(); i++)
				sectionChilds.get(i).setTranslateY(0);
			
			if (!skipExternalUpdate)
			{
				parentSectionManager.renewElementsRealization();
				parentSectionManager.adjustSubelementsSize();
			}
		}
		
		updateDashedBottom();
	}
	
	
	private List<VisualizableProgramElement> getChildrenElements()
	{
		List<VisualizableProgramElement> childrenElements = new ArrayList<>();

		VisualizableProgramElement[] dat = new VisualizableProgramElement[1];
		node.applyToChildrenTotal(dat, () -> {
			
			if (dat[0] != visualizedElement)
				childrenElements.add(dat[0]);
		    
		}, false);
		
		return(childrenElements);
	}


	private void unmarkAsChildrenParent()
	{
		//childsLabel.setText("   ");
		
		if (codeLineIndex < 10)
			childsLabel.setText("  " + codeLineIndex);
		else
		if (codeLineIndex < 100)
			childsLabel.setText(" " + codeLineIndex);
		else
			childsLabel.setText(String.valueOf(codeLineIndex));
		
		
		container.setStyle("");
		elementsSplitPane.setStyle("");
				
		if (!subElements.isEmpty())
		((SubElementName) subElements.get(0)).setExtraWidth((int) childsLabel.getWidth());
		
		
		if (forTutorial)
		{
			childsLabel.setText("");
			childsLabel.setStyle("-fx-margin: 0; -fx-padding: 0;");
		}
	}
	
	private void markAsChildrenParent()
	{
		int childCount = getChildrenNumber();
		
		
		if (childCount < 10)
			childsLabel.setText("  +\n  " + childCount);
		else
		if (childCount < 100)
			childsLabel.setText(" +\n " + childCount);
		else
			childsLabel.setText("+\n" + childCount);
		
		
		((SubElementName) subElements.get(0)).setExtraWidth((int) childsLabel.getWidth());
			
		/*
		container.setStyle("-fx-border-width: 0.75 0.75 2.5 0.75;");
		elementsSplitPane.setStyle("-fx-padding: 2 6 0.25 -4");
		*/
		container.setStyle("-fx-border-width: 0.75 0.75 3.5 0.75;");
		elementsSplitPane.setStyle("-fx-padding: 2 6 -0.75 -4");
		
		if (forTutorial)
		{
			childsLabel.setText("");
			childsLabel.setStyle("-fx-margin: 0; -fx-padding: 0;");
		}

	}
	
	
	private int getChildrenNumber()
	{
		ProgramElement[] dat = new ProgramElement[1];
		int[] count = new int[1];
		count[0] = -1;
		
		node.applyToChildrenTotal(dat, () -> {count[0] += 1;}, true);
		
		return(count[0]);
	}
	
	
	private int getIndexInRoot()
	{
		//VisualizableProgramElement[] dat = new VisualizableProgramElement[1];
		ProgramElement[] dat = new ProgramElement[1];
		
		DataNode<ProgramElement> root = node.getRoot();
		int[] count = new int[1];
		count[0] = 0;
		
		int[] codeLineIndex = new int[1];
		codeLineIndex[0] = 0;
		
		
		root.applyToChildrenTotal(dat, () -> {
			
			if (dat[0] == visualizedElement)
				codeLineIndex[0] = count[0];
				
			count[0]++;
		}, true);
		
		
		return(codeLineIndex[0]);
	}



	public int getDepth()
	{
		return(depth);
	}

	public HBox getContainer()
	{
		return(container);
	}

	public SplitPane getElementsSplitPane()
	{
		return(elementsSplitPane);
	}

	

	public Pane getBasePane()
	{
		return(basePane);
	}
	
	public void setLocked(boolean locked)
	{
		this.locked = locked;
	}
	public boolean getLocked()
	{
		return(locked);
	}
	
	
	public void destroy(boolean realDestroy)
	{
		if (realDestroy)
			for(AbstractContentValue cont: visualizedElement.getArgumentsData())
				cont.destroy();
		
		if (DragAndDropHelper.getSavedPane() != basePane)
		{
			Pane fp = (Pane) basePane.getParent();
			if (fp != null)
				fp.getChildren().remove(basePane);
		}
		else
		{
			if (basePane == null)
				return;
			basePane.setManaged(false);
			basePane.setVisible(false);
		}
	}

	
	public int getCodeLineIndex()
	{
		return(codeLineIndex);
	}
	
	
	public VisualizableProgramElement getElement()
	{
		return(visualizedElement);
	}
	
	
	public void markAsCaution()
	{
		if (!isVisualized() && !forTutorial) return;
		
		if (attentionRectangle == null) return;
		
		Platform.runLater(() -> { // Run later to enable this to work from any thread
			attentionRectangle.setFill(Color.YELLOW);
			container.getChildren().remove(attentionRectangle);
			container.getChildren().add(attentionRectangle);
			
			if (forTutorial)
				attentionRectangle.setOpacity(GlobalSettings.tutorialAttentionAlpha);
			else
			{
				attentionRectangleTransition.setCycleCount(Timeline.INDEFINITE);
				
				if (GlobalSettings.fastFade)
					FastFadeTransitionHelper.fade(attentionRectangle, GlobalSettings.attentionRectangleMinAlpha*0.75, GlobalSettings.attentionRectangleMaxAlpha*0.75, (long) GlobalSettings.attentionBlinkDuration.toMillis());
				else
				{
					attentionRectangleTransition.stop();
					attentionRectangleTransition.setDuration(GlobalSettings.attentionBlinkDuration);
					attentionRectangleTransition.play();
					attentionRectangleTransition.setOnFinished(null);
				}				
			}
		});
	}
	public void markAsErrorcause()
	{
		if (!isVisualized()) return;
	
		Platform.runLater(() -> { // Run later to enable this to work from any thread
			attentionRectangle.setFill(Color.RED.brighter());
			container.getChildren().remove(attentionRectangle);
			container.getChildren().add(attentionRectangle);
			
			attentionRectangleTransition.setCycleCount(Timeline.INDEFINITE);
			
			if (GlobalSettings.fastFade)
				FastFadeTransitionHelper.fade(attentionRectangle, GlobalSettings.attentionRectangleMinAlpha*0.75, GlobalSettings.attentionRectangleMaxAlpha*0.75, (long) GlobalSettings.attentionBlinkDuration.toMillis());
			else
			{
				attentionRectangleTransition.stop();
				attentionRectangleTransition.setDuration(GlobalSettings.attentionBlinkDuration);
				attentionRectangleTransition.play();
				attentionRectangleTransition.setOnFinished(null);
			}
		});
	}
	
	public void markAsExecuting()
	{		
		if (!isVisualized() && inContent && !forTutorial) return;
		
		exectionArrowAlpha.value = 1;
		//addEffectsVariable(exectionArrowAlpha, GlobalSettings.attentionBlinkDurationVal);
		addEffectsVariable(exectionArrowAlpha, 999999);
		
		
		if (attentionRectangleTransition != null)		
		if (GlobalSettings.fastFade && (FastFadeTransitionHelper.isRunning(attentionRectangle)))
			FastFadeTransitionHelper.abortFadeout(attentionRectangle);
		
		//attentionRectangleTransition.setOnFinished((ActionEvent event) -> attentionRectangleTransition.play());
		if (attentionRectangleTransition != null)
		//if (( (GlobalSettings.fastFade && (!FastFadeTransitionHelper.isRunning(attentionRectangle))) || ( (!GlobalSettings.fastFade) && (attentionRectangleTransition.getStatus() != Animation.Status.RUNNING)))
		if (( (GlobalSettings.fastFade && (!FastFadeTransitionHelper.isRunning(attentionRectangle))) || !GlobalSettings.fastFade)
			|| (attentionRectangle.getOpacity() > GlobalSettings.attentionRectangleMinAlpha + 0.75*(GlobalSettings.attentionRectangleMaxAlpha - GlobalSettings.attentionRectangleMinAlpha)  ))
		{
			Platform.runLater(() -> { // Run later to enable this to work from any thread
				
				//if (content.isEvent())
					//attentionRectangle.setFill(Color.BISQUE);
				//else
				attentionRectangle.setFill(Color.LIME);
		
				//container.getChildren().remove(attentionRectangle);
				if (!container.getChildren().contains(attentionRectangle))
					container.getChildren().add(0, attentionRectangle);
				
				if (forTutorial)
					attentionRectangle.setOpacity(GlobalSettings.tutorialAttentionAlpha);
				else
				{
					attentionRectangleTransition.setCycleCount(2);

					if (GlobalSettings.fastFade)
						FastFadeTransitionHelper.fade(attentionRectangle, GlobalSettings.attentionRectangleMinAlpha*0.75, GlobalSettings.attentionRectangleMaxAlpha*0.75, (long) GlobalSettings.attentionBlinkDurationFast.toMillis());
					else
					{
						attentionRectangleTransition.stop();
						attentionRectangleTransition.setDuration(GlobalSettings.attentionBlinkDurationFast);
						attentionRectangleTransition.setCycleCount(2);
						attentionRectangleTransition.play();
						attentionRectangleTransition.setOnFinished((ActionEvent event) -> attentionRectangleTransition.play());
					}
				}
				
			});
		}
		/*
		else
		{
			System. out.println("ELSE FOR MARK AS EXEC: " + content.getFunctionalityName());
			attentionRectangleTransition.jumpTo(javafx.util.Duration.seconds(-1));
		}
		*/
		
		
	}
	
	public void fadeoutMarking()
	{
		fadeoutMarking(false);
	}
	public void fadeoutMarking(boolean quitErrorAsWell)
	{	
		if (!isVisualized() && !forTutorial) return;
		
		if (attentionRectangleTransition == null)
			return;
		
		addEffectsVariable(exectionArrowAlpha, GlobalSettings.attentionBlinkDurationVal);		
		
		Runnable rn = () -> { // Run later to enable this to work from any thread
			
			if (GlobalSettings.fastFade)
			{
				if (attentionRectangleTransition.getCycleCount() == Timeline.INDEFINITE)
				{
					if (quitErrorAsWell)
					{
						FastFadeTransitionHelper.fadeout(attentionRectangle, () -> stopMarking());
						attentionRectangleTransition.setCycleCount(2);
					}
				}
				else
					FastFadeTransitionHelper.fadeout(attentionRectangle, () -> stopMarking());
			}
			else
			{
				if (quitErrorAsWell)
					if (attentionRectangleTransition.getCycleCount() == Timeline.INDEFINITE)
						attentionRectangleTransition.setCycleCount(2);
				attentionRectangleTransition.setOnFinished((ActionEvent event) -> stopMarking());		
			}
		};
		
		
		if (Platform.isFxApplicationThread())
			rn.run();
		else
			Platform.runLater(rn);
	}
	
	
	public void stopMarking()
	{
		if (!isVisualized() && !forTutorial) return;
		if (attentionRectangleTransition == null && ( !FastFadeTransitionHelper.isRunning(attentionRectangle) || !GlobalSettings.fastFade )) return;
		
		Platform.runLater(() -> { // Run later to enable this to work from any thread
			attentionRectangle.setOpacity(0);
			
			attentionRectangleTransition.setCycleCount(0);
			attentionRectangleTransition.stop();
			attentionRectangleTransition.setOnFinished(null);	
			container.getChildren().remove(attentionRectangle);
			
			if (GlobalSettings.fastFade)
				FastFadeTransitionHelper.stopInstantly(attentionRectangle);
		});
	}
	
	public void quitAllMarking()
	{
		if ((attentionRectangleTransition == null) && !FastFadeTransitionHelper.isRunning(attentionRectangle))
			return;
		Platform.runLater(() -> { // Run later to enable this to work from any thread
			attentionRectangleTransition.setCycleCount(0);
			attentionRectangleTransition.stop();
			attentionRectangleTransition.setOnFinished(null);	
			container.getChildren().remove(attentionRectangle);
			
			if (GlobalSettings.fastFade)
				FastFadeTransitionHelper.stopInstantly(attentionRectangle);
		});
	}
	
	
	public void updateEffects()
	{
		executionArrow.setOpacity(exectionArrowAlpha.value);
	}
	
	
	public boolean isVisualized()
	{
		if (node.isRoot()) return(!nodeRoot.hasChildrenHidden());
		
		if (node.getParent().hasChildrenHidden() || nodeRoot.hasChildrenHidden())
			return(false);
		else
			return(true);
	}
	
	
	
	public String getCodePageName()
	{
		return(ProductionGUI.getVisualizedProgram().getPageNamebyRoot(nodeRoot));
	}
	
	
	public void updateOutcommentedState(boolean loopChildren)
	{
		if (loopChildren)
		for(VisualizableProgramElement child: getChildrenElements())
		{
			child.getContent().setOutcommented(content.isOutcommented());
			if (child.getControlerOnGUI() != null)
				child.getControlerOnGUI().updateOutcommentedState(loopChildren);
		}
		
		
		//basePane.setOpacity(content.isOutcommented() ? 0.5 : 1);
		container.setOpacity(content.isOutcommented() ? 0.5 : 1);
		
		if (depth == 0)
			if (!visualizedElement.isEvent())
				if (!content.isSpecial(Functionality.Comment))
					if (parentSectionManager == ContentsSectionManager.getSelf())
						container.setOpacity(0.5); // Display as if outcommented if it is a content with the depth of 0
	}
	
	
	private void updateBreakpointState()
	{
		if (content.isBreakpoint())
			childsLabel.setStyle("-fx-background-color: red; -fx-background-radius: 6;");
		else
			childsLabel.setStyle("");
	}
	
	public FunctionalityContent getContent()
	{
		return(content);
	}
	

	public void tutInit()
	{
		if (node.getRoot().hasChildrenHidden() && !node.isRoot())
			basePane.setScaleY(0);
		basePane.setTranslateX(node.getDepth()*GlobalSettings.elementTreeHgap);
		depth = node.getDepth();
		
		subElements.get(0).alignToCenter();
		subElements.get(0).getContainer().setStyle("-fx-padding: 3 13 3 3;");
		
		ProgramTutorialContent tutContent = (ProgramTutorialContent)content;
		
		
	    if (!tutTop)
	    	basePane.setTranslateX(GlobalSettings.elementTreeHgap);

		
		
		if (!content.getFunctionalityName().equals("tutorialBase"))
			subElements.get(0).setWidth(160);
		
		
		if(!tutContent.getSubtexts().isEmpty())
		{
			for(String sub: tutContent.getSubtexts())
			{
				SubElement el = SubElement.realizeOnGUI(elementsSplitPane, sub, visualizedElement.getRequiresRaspberry());
				el.setWidth(12+sub.length()*7);
				
				el.setSpecialStyle(TutorialControler.getEquivalentColor(sub));
				
				//el.alignToCenter();
				
				//el.getContainer().getChildren().get(0).getStyleClass().add("elementContentTextLarge");
			}
			
	        for(Divider div: elementsSplitPane.getDividers())
	        {
	        	div.setPosition(div.getPosition()+0.1);
	        	div.setPosition(div.getPosition());
	        }
		}
		
		/*
		if (!tutContent.getGoal().isEmpty())
		{
			subElements.get(0).getContainer().getChildren().get(0).getStyleClass().add("elementContentTextLarge");
			subElements.get(0).getContainer().setStyle("-fx-padding: 3 13 3 3;");
			subElements.get(0).setWidth(180);
			
			SubElement el = SubElement.realizeOnGUI(elementsSplitPane, tutContent.getGoal());
			el.setWidth(350);
			
			el.alignToCenter();
			
			el.getContainer().getChildren().get(0).getStyleClass().add("elementContentTextLarge");
			
	        for(Divider div: elementsSplitPane.getDividers())
	        {
	        	div.setPosition(div.getPosition()+0.1);
	        	div.setPosition(div.getPosition());
	        }
		}
		*/
		subElements.get(0).getContainer().getChildren().get(0).getStyleClass().add("elementContentTextLarge");
		
		
		if (content.getFunctionalityName().equals("tutorialStart"))
		{
			HBox box = new HBox();
			
			if (!tutContent.getTut().getExercises().isEmpty())
			{
				Button exc = new Button("Exercise");
				exc.setOnMouseClicked((event) -> {ProductionGUI.toFront(); if (TutorialControler.endTutorial(true)) TutorialControler.startExercises(tutContent.getTut().getExercises());});
				exc.getStyleClass().add("mainButtons");
				exc.getStyleClass().add("mediumTextBold");
				
				exc.setMinWidth(100);
				
				box.getChildren().add(exc);
			}
			
			Button con = new Button("Conclusion");
			con.setOnMouseClicked((event) -> {ProductionGUI.toFront(); if (TutorialControler.endTutorial(true)) tutContent.getTut().showConclusion();});

			box.getChildren().add(con);
			box.setSpacing(8);
			
			con.getStyleClass().add("mainButtons");
			con.getStyleClass().add("mediumTextBold");
			
			con.setMinWidth(100);
			
			box.setStyle("-fx-padding: 0 0 0 6;");
				
			elementsSplitPane.getItems().add(box);
			
			
			
			
			HBox box2 = new HBox();
			box2.setSpacing(8);
			box2.setStyle("-fx-padding: 0 6 0 0; -fx-margin: 0 6 0 0;");
			
			Button exc = new Button("START");
			exc.setOnMouseClicked((event) -> {
				ProductionGUI.toFront();
				WaitPopup pop = new WaitPopup("Loading Tutorial: \n" + ((ProgramTutorialContent) content).getTut().getName());
				
				TutorialControler.endTutorial(false);
				
				FxTimer.runLater(
				        Duration.ofMillis(450),
				        () -> {
				        	Platform.runLater(() ->
				        	{
					        	((ProgramTutorialContent) content).getTut().resolve();
					        	TutorialControler.launchTutorial(((ProgramTutorialContent) content).getTut());
					        	pop.close();
				        	});
				        });				
				});
			exc.getStyleClass().add("mainButtons");
			exc.getStyleClass().add("mediumTextBold");
			
			exc.setMaxWidth(Double.MAX_VALUE);
			exc.setMinWidth(GlobalSettings.maxSubelementFieldWidth+10);
			
			elementsSplitPane.getItems().set(0, box2);
			box2.getChildren().add(exc);
			
			
			container.getStyleClass().remove("commentColors");
			container.getStyleClass().add("ignoreCommentHovering");
		}
		
		
		//if (content.getFunctionalityName().equals("tutorialStart") || content.getFunctionalityName().equals("tutorialBase"))
			//subElements.get(0).getContainer().getChildren().get(0).getStyleClass().add("elementContentTextLarge");
		
	}



	public void hookOnPlusOptionalParamButton(Runnable task)
	{
		optionalParamPlusButtonTask  = task;
	}

	
	
	private void updateParentLines()
	{
		
		for(Line line: additionalParentLines)
			basePane.getChildren().remove(line);
		additionalParentLines.clear();
		
		
		int cornerX = -GlobalSettings.elementTreeHgap/2;
		//int cornerY = (int) ((basePane.getBoundsInLocal().getHeight() + GlobalSettings.elementsVgapWhole)/2) + GlobalSettings.elementsVgap;
		//int cornerY = (int) ((basePane.getBoundsInLocal().getHeight() + GlobalSettings.elementsVgapWhole)) - GlobalSettings.elementsVgap;
		int cornerY = (int) ((12 + GlobalSettings.elementsVgapWhole)) - GlobalSettings.elementsVgap;
		
		parentLineA.setStartX(0);
		parentLineA.setStartY(cornerY);

		parentLineA.setEndX(cornerX);
		parentLineA.setEndY(cornerY);
		
		
		parentLineB.setStartX(cornerX);
		parentLineB.setStartY(0);
		
		


		
		/*
		int treeParentDistance = 0;
		if (!node.getParent().isRoot())
			treeParentDistance = getCodeLineIndex() - node.getParent().getData().getContent().getCodeLineIndex() - 1;
		*/
		
		if (!node.getParent().isRoot())
		{
			if (content.isEvent() && node.getParent().getData().getContent().isEvent())
			{
				optionalORifParEvent.setTranslateX(cornerX+2);
				optionalORifParEvent.setTranslateY(cornerY-3);
				if (!basePane.getChildren().contains(optionalORifParEvent))
					basePane.getChildren().add(optionalORifParEvent);
			}
			else
			if (optionalORifParEvent != null)
			{
				if (basePane.getChildren().contains(optionalORifParEvent))
					basePane.getChildren().remove(optionalORifParEvent);
			}
			
			
			VisualizableProgramElement[] dat = new VisualizableProgramElement[1];
			
			Boolean[] notf = new Boolean[1];
			notf[0] = false;
			
			
			
			int ind = node.getParent().getChildrenAlways().indexOf(node)+1;
			
			if (ind >= node.getParent().getChildrenAlways().size())
			node.getParent().applyToChildrenTotal(dat, () -> {
				
				if (dat[0] == node.getParent().getData())
					return;
				
				if (dat[0] == visualizedElement)
					notf[0] = true;

				if (notf[0]) return;
				
				if (dat[0].getControlerOnGUI() != null)
					if (dat[0].getControlerOnGUI().getDepth() != depth)
						dat[0].getControlerOnGUI().addLowerParentLine(depth);
				
			}, true);
			
		}
		
		parentLineB.setEndX(cornerX);

		
		boolean connectDown = node.getParent().getChildren().indexOf(node) < node.getParent().getChildren().size()-1;
		if (connectDown)
			parentLineB.setStartY(2*cornerY + 2*GlobalSettings.elementsVgap);
		else
			parentLineB.setStartY(cornerY + GlobalSettings.elementsVgapWhole + GlobalSettings.elementsVgap);
			
		parentLineB.setTranslateY(-GlobalSettings.elementsVgapWhole - GlobalSettings.elementsVgap);
	}



	private void addLowerParentLine(int depth2)
	{
		Line line = new Line();
		additionalParentLines.add(line);
		
		line.setMouseTransparent(true);
		line.setManaged(false);
		
		basePane.getChildren().add(line);
		
		
		int cornerX = -GlobalSettings.elementTreeHgap/2 - (depth-depth2) * GlobalSettings.elementTreeHgap;
		int cornerY = (int) ((12 + GlobalSettings.elementsVgapWhole)) - GlobalSettings.elementsVgap;
		
		
		
		line.setStartX(cornerX);
		line.setEndX(cornerX);
		
		line.setStartY(2*cornerY + 2*GlobalSettings.elementsVgap);
			
		line.setTranslateY(-GlobalSettings.elementsVgapWhole - GlobalSettings.elementsVgap);
		
	}



	public Node getAndHighlightPlusOptionalParamButton()
	{
		if (plusOptionalParamButton != null)
		{
			Rectangle attentionRectangle = new Rectangle(1, 1, 1, 1);
			attentionRectangle.setArcHeight(4);
			attentionRectangle.setArcWidth(4);
			attentionRectangle.setManaged(false);
			attentionRectangle.setMouseTransparent(true);
			
			attentionRectangle.widthProperty().bind(plusOptionalParamButton.widthProperty().subtract(2));
			attentionRectangle.heightProperty().bind(plusOptionalParamButton.heightProperty().subtract(2));

			attentionRectangle.setFill(Color.LIME);
			
			
			FastFadeTransitionHelper.fade(attentionRectangle, GlobalSettings.attentionRectangleMinAlpha*0.75, GlobalSettings.attentionRectangleMaxAlpha*0.75, (long) GlobalSettings.attentionBlinkDuration.toMillis());
			
			/*
			FadeTransition attentionRectangleTransition = new FadeTransition(GlobalSettings.attentionBlinkDuration, attentionRectangle);
			attentionRectangleTransition.setFromValue(GlobalSettings.attentionRectangleMinAlpha*0.75);
			attentionRectangleTransition.setToValue(GlobalSettings.attentionRectangleMaxAlpha*0.75);
			attentionRectangleTransition.setCycleCount(Timeline.INDEFINITE);
			attentionRectangleTransition.setAutoReverse(true);			
			
			attentionRectangleTransition.stop();
			attentionRectangleTransition.setDuration(GlobalSettings.attentionBlinkDurationFast);
			attentionRectangleTransition.setCycleCount(2);
			attentionRectangleTransition.play();
					
			attentionRectangleTransition.setOnFinished((ActionEvent event) -> attentionRectangleTransition.play());
			*/

			
			Platform.runLater(() -> ((HBox) plusOptionalParamButton.getParent()).getChildren().add(attentionRectangle));
		}
		return(plusOptionalParamButton);
	}



	public boolean isHidden()
	{
		if (node.getParent().hasChildrenHidden())
			return(true);
		else
			if (node.getParent().isRoot())
				return(false);
			else
				return(((ProgramElement) node.getParent().getData()).getContent().getVisualization().getControlerOnGUI().isHidden());
	}



	public boolean hidesChildren()
	{
		return(hiddenChildren);
	}



	public boolean isExceptionalRemovable()
	{
		return(exceptionalRemovable);
	}
	
	public void visualizeMarking(boolean mark)
	{		
		if (mark)
		{
			if (visualizedElement.getOriginSectionClass() == ActionsSectionManager.class)
				container.getStyleClass().add("actionColorsMarked");
			else
			if (visualizedElement.getOriginSectionClass() == ConditionsSectionManager.class)
				container.getStyleClass().add("conditionColorsMarked");
			else
			if (visualizedElement.getOriginSectionClass() == EventsSectionManager.class)
				container.getStyleClass().add("eventColorsMarked");
			else
			if (visualizedElement.getOriginSectionClass() == StructuresSectionManager.class)
				container.getStyleClass().add("variableColorsMarked");		
				
			if ((content != null && (content.getSpecial() == Functionality.Comment)) || forTutorial)
				container.getStyleClass().add("commentColorsMarked");
		}
		else
		{
			if (visualizedElement.getOriginSectionClass() == ActionsSectionManager.class)
				container.getStyleClass().removeAll("actionColorsMarked");
			else
			if (visualizedElement.getOriginSectionClass() == ConditionsSectionManager.class)
				container.getStyleClass().removeAll("conditionColorsMarked");
			else
			if (visualizedElement.getOriginSectionClass() == EventsSectionManager.class)
				container.getStyleClass().removeAll("eventColorsMarked");
			else
			if (visualizedElement.getOriginSectionClass() == StructuresSectionManager.class)
				container.getStyleClass().removeAll("variableColorsMarked");		
				
			if ((content != null && (content.getSpecial() == Functionality.Comment)) || forTutorial)
				container.getStyleClass().removeAll("commentColorsMarked");
		}


	}

	
}
