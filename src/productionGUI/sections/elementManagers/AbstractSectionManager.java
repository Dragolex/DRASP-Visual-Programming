package productionGUI.sections.elementManagers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.reactfx.util.FxTimer;

import dataTypes.ComponentContent;
import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import main.electronic.Electronics;
import main.functionality.FeatureLoader;
import main.functionality.Functionality;
import main.functionality.Structures;
import otherHelpers.DragAndDropHelper;
import otherHelpers.MarkingRectangleHelper;
import productionGUI.ProductionGUI;
import productionGUI.additionalWindows.OverlayMenu;
import productionGUI.controlers.UndoRedoControler;
import productionGUI.sections.electronic.VisualizedComponent;
import productionGUI.sections.electronic.WirePoint;
import productionGUI.sections.electronic.parents.ElectronicsMouseDraggable;
import productionGUI.sections.elements.ProgramElementOnGUI;
import productionGUI.sections.elements.VisualizableProgramElement;
import productionGUI.sections.subelements.SubElement;
import productionGUI.tutorialElements.TutorialControler;
import productionGUI.tutorialElements.TutorialReaderAndMaker;
import settings.EnvironmentDataHandler;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;
import staticHelpers.GuiMsgHelper;
import staticHelpers.OtherHelpers;

@SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
public abstract class AbstractSectionManager extends StaticAbstractSectionManagerHelper
{
	protected Class childIdentitiyClass;	
	
	protected VBox sectionBox;
	protected Pane contentPane;
	
	protected GeneralSectionManager sectionManager;
	
	protected String dataPage = "";
	
	protected DataNode<ProgramElement> rootElementNode;	
	protected List<ProgramElementOnGUI> realizedNodes = new ArrayList<>();
    
    private int contentWidthSum = 0;
	
	private int elementHeight = 0;
	private int sectionOriginX = -1000;
	private int sectionOriginY = -1000;
	private boolean alreadyInitializedDragAction = false;
	
	private int currentDragDropMode;
	
	protected boolean onlyOneChildLevel;
	protected boolean newBaseNodesAllowed;
	
	private int reVisualisationIndex = 0;
	
	// For Drag and Drop optimization
	private List<Node> contentPaneChilds;
	private int pulledIndex = 10000;
	Pane currentDummy;
	Pane currentTop;
	
	long currentDragDropFrame = -1;

	//Pane currentBasePane;
	int currentMaxNodes;

	static List<AbstractSectionManager> allManagers = new ArrayList<>();
	
	//private boolean isContentParent = false;
	
	
	
	static final Color YellowCol = Color.rgb(220, 220, 0, 0.4);
	static final Color GreenCol = Color.rgb(0, 200, 100, 0.4);
	
	
	List<ProgramElement> elementSnapshot = new ArrayList<>();
	List<Integer> elementDepthSnapshot = new ArrayList<>();
	
	int lastInsertionIndex = -1000;
	
	List<Integer> possibleParentPositionsIndices = new ArrayList<>();
	List<Integer> possibleInsertionDepths = new ArrayList<>();
	
	
	List<Rectangle> previewRects = new ArrayList<>();
	
	boolean allowedAsRoot = false;

	int insertionGapX, insertionGapY;
	
	int lastTargInd = -100;
	
	int minInsertionIndex = 1000;
	
	
	int currentInsertionParentIndex = -10000;
	int lastCurrentInsertionParentIndex = -2;
	int currentInsertionInsideParentIndex = -1;
	
	
	private MarkingRectangleHelper markingRectangleHelper;
	
	
	
	
	Label multiDragSignal;
	
	
	public AbstractSectionManager(GeneralSectionManager sectionManager, String page)
	{
		this.sectionManager = sectionManager;
		this.dataPage = page;
		
		/*
		ProductionGUI.addFinalizationEvent(() -> {
			sectionOriginX = (int) sectionBox.localToScene(sectionBox.getBoundsInLocal()).getMinX();
			sectionOriginY = (int) sectionBox.localToScene(sectionBox.getBoundsInLocal()).getMinY();
			
			//UndoRedoControler.getSelf().appliedChange(dataPage);
		});
		*/
		
		childIdentitiyClass = getClass();
		
		allManagers.add(this);
		
		multiDragSignal = new Label();
		multiDragSignal.setStyle(
				"-fx-font-size: 15;"
				+ "-fx-font-weight: bold;"
				+ "-fx-background-color: rgba(0, 90, 204, 0.6);"
				+ "-fx-padding: 3; -fx-margin: 0; "
				+ "-fx-fill: white; "
				+ "-fx-text-fill: white; "
				+ "-fx-border-color: elementBorderColor; "
				+ "-fx-border-width: 0.5; "
				+ "-fx-background-radius: 5; "
				+ "-fx-border-radius: 5;"
				);
		//multiDragSignal.setManaged(false);
	}
	
	public static void applyForAll(AbstractSectionManager[] abstr, Runnable task)
	{
		for(AbstractSectionManager ab: allManagers)
		{
			abstr[0] = ab;
			task.run();
		}
	}
	public static AbstractSectionManager getSpecificSelf(Class<?> originSectionManagerClass)
	{
		for (AbstractSectionManager mng: allManagers)
			if (mng.getClass() == originSectionManagerClass)
				return(mng);
		return(null);
	}
	
	
	public void switchDataPage(String page, boolean reinit)
	{
		dataPage = page;
		
		if (reinit)
			reinitialize();
		else
		{
			if (this instanceof ContentsSectionManager)
				rootElementNode = ProductionGUI.getVisualizedProgram().getPageRoot(dataPage);
			else
				rootElementNode = Functionality.getFeaturesProgram().getPageRoot(dataPage);

			Functionality.visualizeAllNodes(rootElementNode, false);
			
			ProgramElement[] thisElement = new ProgramElement[1];
			rootElementNode.applyToChildrenTotal(thisElement, () -> { ((VisualizableProgramElement) thisElement[0]).setOriginSection(this); }, true);
		}
		
	}
	
	
	OverlayMenu rightclickMenu = null;

	public void reinitialize()
	{
		markedContents.clear();
		
		//isContentParent = (this instanceof ContentsSectionManager);
		
		ProductionGUI.setLoadedFile(false);
		
		if (this instanceof ContentsSectionManager)
			rootElementNode = ProductionGUI.getVisualizedProgram().getPageRoot(dataPage);
		else
			rootElementNode = Functionality.getFeaturesProgram().getPageRoot(dataPage);

		Functionality.visualizeAllNodes(rootElementNode, false);
		
		
		ProgramElement[] thisElement = new ProgramElement[1];
		rootElementNode.applyToChildrenTotal(thisElement, () -> { ((VisualizableProgramElement) thisElement[0]).setOriginSection(this); }, true);
		
		
		sectionBox = sectionManager.getSectionBox();
		contentPane = sectionManager.getContentPane();

		realizeElements(rootElementNode);
		
		adjustSubelementsSize();
		adjustContainerSize();


		ProductionGUI.setLoadedFile(true);
		
		boolean onlyOneUnmovableLeaf = (childIdentitiyClass != ContentsSectionManager.class);

		
		if (markingRectangleHelper == null)
		{			
	        //Background reusableBc = new Background(new BackgroundFill(new Color(0, 0, 0, 0.25), new CornerRadii(5), Insets.EMPTY));
			
			Double[] corners = new Double[4];
			markingRectangleHelper = new MarkingRectangleHelper(sectionManager.getScrollPane(), corners, () -> {
			ProgramElement[] ele = new ProgramElement[1];
			
			markedContents.clear();
			
			int[] minDepth = new int[1];
			minDepth[0] = -2;
			
			if (!Electronics.isActive())
				rootElementNode.applyToChildrenTotal(ele,
					// MOUSE DRAG EVENT	
					() -> {
					
					ProgramElementOnGUI onGui = ele[0].getContent().getVisualization().getControlerOnGUI();
	
					if (ele[0].getContent().getVisualization().isDraggable())
					{
						boolean in = (onGui.getBasePane().localToScene(onGui.getBasePane().getBoundsInLocal()).intersects(corners[0], corners[1], corners[2], corners[3]));
						
						
						boolean markOnlyOne = false;
						
						if(onlyOneUnmovableLeaf)
						{
							markOnlyOne = true;
							
							if(ele[0].getContent().getVisualization().isDraggable())
							{
								DataNode<ProgramElement> parent = ele[0].getContent().getVisualization().getNode().getParent();
								do
								{
									if (((VisualizableProgramElement) parent.getData()).getIsUserModifiableParentNode())
										markOnlyOne = false;
									parent = ((VisualizableProgramElement) parent.getData()).getNode().getParent();
								}
								while(!parent.isRoot());						
							}
						}
									
						
						if (in || (minDepth[0] > -2))
						{
							if(minDepth[0] == -2)
								minDepth[0] = onGui.getDepth();
							
							if (onGui.getDepth() < minDepth[0])
								minDepth[0] = -3;
							
							if (!in)
								if (onGui.getDepth() <= minDepth[0])
									minDepth[0] = -3;
							
							if (minDepth[0] == -3)
								onGui.visualizeMarking(false);
							else
							{
								markedContents.add(ele[0].getContent());
								onGui.visualizeMarking(true);
								
								if (markOnlyOne) // allow only once
									minDepth[0] = -3;
							}
						}
						else
							onGui.visualizeMarking(false);
					}
					else
						onGui.visualizeMarking(false);
	
					
				}, false);
			
			if (Electronics.isActive())
			{
				for (VisualizedComponent comp: Electronics.getVisualizedComponents())
				{
					comp.setSelected( comp.getCanvas().localToScene(comp.getCanvas().getBoundsInLocal()).intersects(corners[0], corners[1], corners[2], corners[3]));
				}
				
				for (WirePoint wp: Electronics.getWirePoints())
				{
					if (wp.getAssociatedPin() == null)					
						if (wp.getNode().localToScene(wp.getNode().getBoundsInLocal()).intersects(corners[0], corners[1], corners[2], corners[3]))
						{
							wp.show();
							wp.setSelected(true);
						}
						else
						{
							wp.setSelected(false);
							wp.unshow();
						}
				}
			}
			
			},
				// RIGHTCLICK EVENT
				() -> {
					
					if (rightclickMenu != null) return; // quit if already open
					if (ElectronicsMouseDraggable.isActive()) return; // quit if in electroncis stuff. TODO
				
					
					rightclickMenu = new OverlayMenu(ProductionGUI.getScene().getRoot(), () -> rightclickMenu = null, true, false);
					rightclickMenu.applyHideOnAnyButton();
					

					FunctionalityContent firstContent = null;
					Button state;
					boolean allowCutAndInsert = true;
					
					if (!markedContents.isEmpty())
					{
						firstContent = markedContents.get(0);
						
						if (firstContent.isUndeletable())
							allowCutAndInsert = false; // the undeletable elements cannot be cut or inserted
					}

					
					
					Button cut = rightclickMenu.addButton("Cut (Ctrl + X)", () -> cutElements(), false);
					Button copy = rightclickMenu.addButton("Copy (Ctrl + C)", () -> copyElements(), false); // Todo: dynamically use the letter defined in GlobalSettings.copyElementsKey
					Button insert = rightclickMenu.addButton("Insert (Ctrl + V)", () -> insertElements(), false);

					rightclickMenu.addSeparator("rgb(255, 255, 255);");
					
					
					if ((firstContent != null) && firstContent.isOutcommented())
						state = rightclickMenu.addButton("Enable", () -> outcommentElements(null, false), false);
					else
						state = rightclickMenu.addButton("Disable", () -> outcommentElements(null, true), false);

					
					if (firstContent != null)
					{
						rightclickMenu.addSeparator("rgb(255, 255, 255);");
										
						FunctionalityContent fc = firstContent;
						// Edit code 
						rightclickMenu.addButton("EDIT JAVA CODE", () -> openJavaCode(fc), false);
					}
					else // nothing is marked
					{
						// Make copy and cut invalid
						OverlayMenu.setElementState(cut, false);
						OverlayMenu.setElementState(copy, false);
						OverlayMenu.setElementState(state, false);
					}
					if (!allowCutAndInsert)
					{
						OverlayMenu.setElementState(cut, false);
						OverlayMenu.setElementState(insert, false);
					}
					
					

					
					rightclickMenu.setPosition((int) (double) corners[0], (int) (double) corners[1]);
				});
		}

		
		//FxTimer.runLater(Duration.ofMillis(2000),() -> adjustContainerSize());
		
		
		for(int i = 0; i < 10; i++)
		{

			Rectangle rec = new Rectangle(0, 0, GlobalSettings.elementTreeHgap-GlobalSettings.elementsVgapWhole, 1);//desiredWidth, 1);
			rec.setArcHeight(10);
			rec.setArcWidth(10);
			rec.setMouseTransparent(true);
			
			rec.setStroke(Color.BLACK);
			rec.setStrokeWidth(0.5);
			
			rec.setManaged(false);
			
			OtherHelpers.applyOptimizations(rec);
			
			previewRects.add(rec);
		}
		
		
		FxTimer.runLater(
		        Duration.ofMillis(150),
		        () -> {
		
		        	int W = 0;
		
					for (ProgramElementOnGUI el: realizedNodes)
						W = (int) Math.max(W, el.getContainer().localToScene(el.getContainer().getBoundsInLocal()).getWidth());
			
					if (W != 0)
						contentPane.setMinWidth(W + GlobalSettings.elementTreeHgap);
		        });

		
		// Set as drag-and-drop target

		Runnable hoverEvent = new Runnable()
		{
			@Override
			public void run()
			{	
				if (currentDragDropFrame == DragAndDropHelper.getCurrentFrame())
					return;
				currentDragDropFrame = DragAndDropHelper.getCurrentFrame();
				
				
				if ((childIdentitiyClass != ContentsSectionManager.class) || (!Electronics.isActive())) // outside of electroncis mode
				{
				
					boolean removeOriginal = !DragAndDropHelper.getAttribute(GlobalSettings.noOrigRemovalAttribute);
					
					
					if (!alreadyInitializedDragAction)
					{
						currentDummy = (Pane) DragAndDropHelper.getDragInstance();
						currentTop = DragAndDropHelper.getTopPane();				
	
						VisualizableProgramElement origElement = (VisualizableProgramElement) DragAndDropHelper.getPayload();
						currentMaxNodes = realizedNodes.size();
						
						// Add the clicked element to the marked ones
						if (!markedContents.contains(origElement.getContent()))
						{
							markingRectangleHelper.unmark();
							markedContents.add(origElement.getContent());
						}
						
						
						// Add possible child elements to the marked ones
						ProgramElement[] eleDat = new ProgramElement[1];
						origElement.getNode().applyToChildrenTotal(eleDat, () -> {
							
							if (!markedContents.contains(eleDat[0].getContent()))
								markedContents.add(eleDat[0].getContent());						
							
						}, false);
						
						
						
						currentDragDropMode = DragAndDropHelper.getDragDropMode(getCurrentDragDropSourceClass(), childIdentitiyClass);
						
						
						if (origElement.getControlerOnGUI().isExceptionalRemovable())
						{
							for(FunctionalityContent cont: markedContents)
							{
								cont.getVisualization().getControlerOnGUI().getBasePane().setVisible(false);
								cont.getVisualization().getControlerOnGUI().getBasePane().setManaged(false);
							}
						}
						else
						{
							for(FunctionalityContent cont: markedContents)
							{
								cont.getVisualization().getControlerOnGUI().getBasePane().setVisible(true);
								cont.getVisualization().getControlerOnGUI().getBasePane().setManaged(true);
							}
						}
						
						
						if (currentDragDropMode == 0)
							currentDummy.setOpacity(0.5);
						
	
						contentPaneChilds = contentPane.getChildrenUnmodifiable();
						
						pulledIndex = 10000;
						if ((currentDragDropMode == 2) || (currentDragDropMode == 3))
							pulledIndex = origElement.getControlerOnGUI().getBasePane().getParent().getChildrenUnmodifiable().indexOf(origElement.getControlerOnGUI().getBasePane());										
						
						
						if (!removeOriginal)
							pulledIndex = 10000;
						
	
						alreadyInitializedDragAction = true;
						
						
						if (markedContents.size() > 1)
						{						
							if (!currentDummy.getChildren().contains(multiDragSignal))
								currentDummy.getChildren().add(multiDragSignal);
							
							multiDragSignal.setVisible(true);
							multiDragSignal.setText("+ " + (markedContents.size()-1) + " Element"+ GuiMsgHelper.sIfPlur((markedContents.size()-1)));
						}
						else
							multiDragSignal.setVisible(false);
						
						
						currentInsertionParentIndex = -10000;
						lastCurrentInsertionParentIndex = -2;
						
						
						possibleParentPositionsIndices.clear();
						possibleInsertionDepths.clear();
						currentTop.getChildren().removeAll(previewRects);
						
						
						updateElementSnapshot(elementSnapshot, elementDepthSnapshot);
						if (pulledIndex < 10000)
						{						
							for(FunctionalityContent cont: markedContents)
							{
								int ind = elementSnapshot.indexOf(cont.getVisualization());
								//if (ind >= 0)
								{
									elementSnapshot.remove(ind);
									elementDepthSnapshot.remove(ind);
								}
							}
						}
						lastInsertionIndex = -1000;
						
						allowedAsRoot = true;//origElement.isEvent() || origElement.getContent().isSpecial(Functionality.Comment);
						
						
						sectionOriginX = (int) sectionBox.localToScene(sectionBox.getBoundsInLocal()).getMinX() + GlobalSettings.elementsVgapWhole + 13;
						sectionOriginY = (int) sectionBox.localToScene(sectionBox.getBoundsInLocal()).getMinY() + 1;
						
						elementHeight = (int) ((Pane)((Pane) DragAndDropHelper.getOrigDragInstance()).getChildren().get(0)).getBoundsInLocal().getHeight();					
						
						multiDragSignal.setTranslateX(currentDummy.getChildren().get(0).getBoundsInParent().getWidth());
						multiDragSignal.setTranslateY(elementHeight/2 - GlobalSettings.elementsVgapWhole + GlobalSettings.elementsVgap);
					}
					
					//System. out.println("CURRENT DROP MODE: " + currentDragDropMode);
	
					
					
					switch(currentDragDropMode)
					{
						case 0: 
							currentDummy.setOpacity(0.5);
						break;
						case 2:
						case 3:
							if (removeOriginal)
							{
								for(FunctionalityContent cont: markedContents)
								{
									cont.getVisualization().getControlerOnGUI().getBasePane().setVisible(false);
									cont.getVisualization().getControlerOnGUI().getBasePane().setManaged(false);
								}
							}						
						case 1:
						case 4:
							currentDummy.setOpacity(1);
						break;
					}
					
									
					// Compute the index of the position where the object belongs to
					int Ybase = (int) DragAndDropHelper.getMouseDragY()  -  (int) sectionBox.localToScene(sectionBox.getBoundsInLocal()).getMinY() + GlobalSettings.elementDragOffElementDistance;
					
					int insertionIndex = computeInsertionIndex(elementHeight, Ybase, pulledIndex, currentMaxNodes, removeOriginal);
					
					
					//System.out.println("Insertion index: " + insertionIndex);
					
					
					if (insertionIndex != lastInsertionIndex)
					{
						for(Rectangle rect: previewRects)
							rect.setVisible(true);
						
						if (rootElementNode.isLeaf())
						{
							insertionGapX = (int) (sectionOriginX-16 + GlobalSettings.elementsVgapWhole - GlobalSettings.elementsVgap);
							insertionGapY = (int) (sectionOriginY + 2*GlobalSettings.elementsVgapWhole - GlobalSettings.elementsVgap);					
						}
						else
						{
							insertionGapX = (int) (sectionOriginX + sectionManager.getScrollPane().getHvalue()*(sectionManager.getContentPane().getWidth()-(sectionManager.getScrollPane().getViewportBounds().getWidth()-66 + GlobalSettings.elementsVgap )));				
							insertionGapY = (int) (sectionOriginY + (insertionIndex) * (elementHeight + GlobalSettings.elementsVgap) + GlobalSettings.elementsVgapWhole);					
						}
	
						
						if (!newBaseNodesAllowed)
							insertionGapY -= 0.75*rootElementNode.getChildrenAlways().size();
							
						for(int i = 0; i < possibleParentPositionsIndices.size(); i++)
							currentTop.getChildren().remove( previewRects.get(i) );
						
						possibleParentPositionsIndices.clear();
						possibleInsertionDepths.clear();
						
						
						ProgramElement above = null;
						int aboveDepth = -1;
						if ((insertionIndex > 0) && (elementSnapshot.size() > (insertionIndex-1)))
						{
							above = elementSnapshot.get(insertionIndex-1);
							aboveDepth = elementDepthSnapshot.get(insertionIndex-1)-1;
						}
						
						if (above == null)
							addPreviewPos(-1, 0 ); // Add as a root-child
						else
						{
							addPreviewPos(insertionIndex-1, aboveDepth+1);
							
							
							int belowDepth = 0;
							if (insertionIndex < elementSnapshot.size())
								belowDepth = elementDepthSnapshot.get(insertionIndex)-1;
							
							if (belowDepth <= aboveDepth)
							{
								int ind = -1;
								for(int i = insertionIndex-1; i >= 0; i--)
								{
									if ((elementDepthSnapshot.get(i)-1) == (aboveDepth-1))
									{
										ind = i;
										break;
									}
								}
								
								addPreviewPos(ind, aboveDepth);
							}
	
							if (belowDepth < aboveDepth)
							{
								for(int j = aboveDepth-1; j >= belowDepth; j--)
								{
									int ind = -1;
									for(int i = insertionIndex-1; i >= 0; i--)
									{
										if ((elementDepthSnapshot.get(i)-1) == (j-1))
										{
											ind = i;
											break;
										}
									}
									
									addPreviewPos(ind, j);
								}
							}
									
						}
						
						
						lastInsertionIndex = insertionIndex;
						
						for(ProgramElement ele: elementSnapshot)
							ele.getContent().getVisualization().getControlerOnGUI().fadeoutMarking();
						
						
						minInsertionIndex = 1000;
						for(int ind: possibleInsertionDepths)
						{
							if (ind < minInsertionIndex)
								minInsertionIndex = ind;
						}
						
						lastTargInd = -100;
					}
					
					
					int mouseXdif = Math.max(minInsertionIndex-1, (DragAndDropHelper.getMouseDragX() - insertionGapX) / GlobalSettings.elementTreeHgap);
					int targInd = -10;
					for(int ind: possibleInsertionDepths)
					{
						if (ind > targInd)
						if ((ind-1) <= mouseXdif)
							targInd = ind;
					}
					
					
					if (((Math.abs((mouseXdif) - (targInd)) < 3) && Math.abs(insertionGapY-DragAndDropHelper.getMouseDragY()) < (4*GlobalSettings.elementTreeHgap)) && (insertionIndex == lastInsertionIndex) )
					{
						if (targInd != lastTargInd)
						{
							DragAndDropHelper.setMouseAttachedDrag(false);
							currentDummy.setTranslateX(insertionGapX + targInd*GlobalSettings.elementTreeHgap - 12);
							currentDummy.setTranslateY(insertionGapY - elementHeight/2 + GlobalSettings.elementsVgapWhole + GlobalSettings.elementsVgap);
							
							for(Rectangle rect: previewRects)
								rect.setVisible(true);
							
							int inn = possibleInsertionDepths.indexOf((Integer) targInd);
							previewRects.get(inn).setVisible(false);
							
							for(int i = 0; i < possibleInsertionDepths.size(); i++)
								if (possibleInsertionDepths.get(i) > targInd)
									previewRects.get(i).setVisible(false);
	
							
							currentInsertionParentIndex = possibleParentPositionsIndices.get(inn);
							currentInsertionInsideParentIndex = insertionIndex - currentInsertionParentIndex;
							
							if (currentInsertionParentIndex != lastCurrentInsertionParentIndex)
							for(ProgramElement ele: elementSnapshot)
								ele.getContent().getVisualization().getControlerOnGUI().fadeoutMarking();
							
							lastCurrentInsertionParentIndex = currentInsertionParentIndex;
							
							if (currentInsertionParentIndex >= 0)
								elementSnapshot.get(currentInsertionParentIndex).getContent().getVisualization().getControlerOnGUI().markAsExecuting();
							else
								currentInsertionInsideParentIndex-=1;
	
							
							lastTargInd = targInd;
						}
					}
					else
						if ((mouseXdif > 1) || (Math.abs(insertionGapY-DragAndDropHelper.getMouseDragY()) < (3*GlobalSettings.elementTreeHgap)) || (insertionIndex == lastInsertionIndex))
						{
							DragAndDropHelper.setMouseAttachedDrag(true);
							for(Rectangle rect: previewRects)
								rect.setVisible(true);
								
							for(ProgramElement ele: elementSnapshot)
								ele.getContent().getVisualization().getControlerOnGUI().fadeoutMarking();
							
							currentInsertionParentIndex = -10000;
							currentInsertionInsideParentIndex = -10000;
							
							lastTargInd = -100;
						}
					
					
					if (pulledIndex < insertionIndex)
						insertionIndex += markedContents.size();
					
					int nodes = contentPaneChilds.size();
					if (nodes > 0)
					{	
						double yval;
						float h = (float) (((Pane)contentPaneChilds.get(0)).getHeight() + GlobalSettings.elementsVgapWhole);
						float fc = (float) DragAndDropHelper.getCurrentFactor();
						float ft = GlobalSettings.smoothDragTransition ? 5 : 1;
						
						int i = 0;
						
						if (!possibleParentPositionsIndices.isEmpty())
						{						
							for(; i < insertionIndex; i++)
							{
								yval = contentPaneChilds.get(i).getTranslateY();							
								if (Math.abs(yval) > 0.5)
									contentPaneChilds.get(i).setTranslateY( Math.max(0, yval -fc*(yval/ft)) );
							}
							
							for(; i < nodes; i++)
							{
								yval = contentPaneChilds.get(i).getTranslateY();							
								if (Math.abs(yval-h) > 0.5)
									contentPaneChilds.get(i).setTranslateY( Math.min(h, yval + fc*((h-yval)/ft)) );
							}
						}
						else
							for(; i < nodes; i++)
							{
								yval = contentPaneChilds.get(i).getTranslateY();							
								if (Math.abs(yval) > 0.5)
									contentPaneChilds.get(i).setTranslateY( Math.max(0, yval -fc*(yval/ft)) );
							}
					}
				}
				else
				{
					/*
					// ELECTRONICS MODE
					
					DragAndDropHelper.abortDragging();
					VisualizableProgramElement origElement = (VisualizableProgramElement) DragAndDropHelper.getPayload();
					ComponentContent content = (ComponentContent) origElement.getSpecialContent();
					
					String name = content.getFunctionalityName();
					
					
					FxTimer.runLater(Duration.ofMillis(300),() -> Platform.runLater(() -> Electronics.visualizeComponentContent(name)));
					*/
										
				}

				
				
			}

			private void addPreviewPos(int parentInd, int depth)
			{
				if(parentInd < elementSnapshot.size() && parentInd >= 0)
				if (!elementSnapshot.get(parentInd).getContent().canHaveUserAddedChildElements())
					return;
				
				if (parentInd == -1 && !newBaseNodesAllowed)
					return;
				
				Rectangle rect = previewRects.get(possibleParentPositionsIndices.size());
				currentTop.getChildren().add(1, rect);
				rect.setTranslateX(insertionGapX + depth*GlobalSettings.elementTreeHgap);
				rect.setTranslateY(insertionGapY);
				
				rect.setHeight(elementHeight-3);
				
				if ((depth == 0) && !allowedAsRoot)
					rect.setFill(YellowCol);
				else
					rect.setFill(GreenCol);
				
				possibleParentPositionsIndices.add(parentInd);
				
				possibleInsertionDepths.add(depth);
			}
		
		};
		
		Runnable dropEvent = new Runnable()
		{
			@Override
			public void run()
			{
				TutorialControler.setWindowTransparent(false);
				
				if ((childIdentitiyClass != ContentsSectionManager.class) || (!Electronics.isActive())) // outside of electroncis mode
				{
					
					// Get the element to insert
					VisualizableProgramElement origElement = (VisualizableProgramElement) DragAndDropHelper.getPayload();
					
					Pane basePane = DragAndDropHelper.getSavedPane();
					basePane.setStyle("");				
									
					
					currentDragDropMode = DragAndDropHelper.getDragDropMode(getCurrentDragDropSourceClass(), childIdentitiyClass);
					//InfoErrorHandler.printEnvironmentInfoMessage("CURRENT DROP MODE: " + currentDragDropMode);
					
					
					/*
					int pulledIndex = 10000;
					if ((currentDragDropMode == 2) || (currentDragDropMode == 3))
						pulledIndex = basePane.getParent().getChildrenUnmodifiable().indexOf(basePane);
						*/
					
					boolean removeOriginal = !DragAndDropHelper.getAttribute(GlobalSettings.noOrigRemovalAttribute);
					
					
					/*
					 *  Modes:
					 *  
					 *  0: Not possible
					 *  1: Move and copy recreating (keep in source and target)
					 *  2: Move and keep the data (delete from source)
					 *  3: Only delete from source
					 *  4: Move and copy and keep the data (keep in source and target)
					 */
					
					
					
					
					Platform.runLater(() -> {
					for(ProgramElement ele: elementSnapshot)
						ele.getContent().getVisualization().getControlerOnGUI().fadeoutMarking();
					});
					
					
					if (currentInsertionInsideParentIndex < -100)
						currentDragDropMode = 0;
					
					
					if (!removeOriginal)
						if (currentDragDropMode > 0)
							currentDragDropMode = 4;
					
					if (currentDragDropMode != 4)
						if (TutorialControler.tutorialRunning())
							currentDragDropMode = 0;
					
									
					if (Execution.isRunning() || Execution.isRunningDeployed())
						currentDragDropMode = 0;
					
					
					if (currentDragDropMode > 0)
						UndoRedoControler.getSelf().appliedChange(dataPage, false); // Enable the redo stop
					
					
					DragAndDropHelper.clearAttribute(GlobalSettings.noOrigRemovalAttribute);
					
					
					
					System.out.println("DROP TYPE: " + currentDragDropMode + " Apllying: " + markedContents.size() + " elements.");
					
					switch(currentDragDropMode)
					{
					case 0:
						
						insertMarkedElements(true, false, false, false);
						
						return; // No drag and drop possible -> restore
						
					case 1: // Copy to the new position
						
						insertMarkedElements(false, false, false, true);
						
						renewElementsRealization();
					break;
					
					case 2: // Move to the new position
						removeCurrentMarkedElements();
						
						
						insertMarkedElements(true, true, false, false);
						
						
						renewElementsRealization(true);
					break;
						
					case 3:
						removeCurrentMarkedElements();
					break;
					
					case 4: // Move and copy and keep the data (keep in source and target)
						
						VisualizableProgramElement tempCreated = insertMarkedElements(true, false, true, false);
						
						if (TutorialControler.tutorialRunning() || TutorialReaderAndMaker.isRecording())
						if (!TutorialControler.handleElementDrag(rootElementNode, tempCreated, origElement.getContent().getFunctionalityName()))
						{
							rootElementNode.removeAnywhereInTree(tempCreated);
							currentDragDropMode = 0;
						}
						
						renewElementsRealization();
						renewElementsRealization(); // TODO: Find out why renewing twice is required to work...
					break;
					}
					
					
					
					//if (childIdentitiyCLass == ContentsSectionManager.class) // if it's the contentsmanager
					if (currentDragDropMode > 0)
						UndoRedoControler.getSelf().appliedChange(dataPage, true); // Enable the finalization redo step
					
					
					adjustContainerSize();
					
					markedContents.clear();
				}
				else
				{
					// ELECTRONICS MODE
					
					DragAndDropHelper.abortDragging();
					VisualizableProgramElement origElement = (VisualizableProgramElement) DragAndDropHelper.getPayload();
					ComponentContent content = (ComponentContent) origElement.getSpecialContent();
					
					Electronics.visualizeComponentContent(content.getFunctionalityName());
				}
				
			}
			
			private VisualizableProgramElement insertMarkedElements(boolean revertOriginal, boolean justInsert, boolean duplicateWithData, boolean duplicateWithoutData)
			{
				// Insert at the right position
				
				VisualizableProgramElement tempCreated = null;
				
				Stack<Integer> oldPos = new Stack<Integer>();
				
				int lastInsDepth = markedContents.get(0).getVisualization().getControlerOnGUI().getDepth();
				int newElements = markedContents.size();
				for(int subInd = 0; subInd < newElements; subInd++)
				{
					FunctionalityContent cont = markedContents.get(subInd);
					
					if (revertOriginal)
					{
						cont.getVisualization().getControlerOnGUI().getBasePane().setStyle("");
						cont.getVisualization().getControlerOnGUI().getBasePane().setManaged(true);
						cont.getVisualization().getControlerOnGUI().getBasePane().setVisible(true);
					}
					
					int newInsDepth = cont.getVisualization().getControlerOnGUI().getDepth();
					if (newInsDepth != lastInsDepth) // new depth
					{
						if (newInsDepth > lastInsDepth) // new one is a child
						{
							oldPos.push(currentInsertionParentIndex);
							oldPos.push(currentInsertionInsideParentIndex-1);
							
							if (currentInsertionParentIndex == -1)
								currentInsertionParentIndex = 0;
							currentInsertionParentIndex += currentInsertionInsideParentIndex-1;
							currentInsertionInsideParentIndex = 1;
						}
						
						if (newInsDepth < lastInsDepth) // new one is parent again
						{
							currentInsertionInsideParentIndex += oldPos.pop();
							currentInsertionParentIndex = oldPos.pop();
						}
					}
					
					
					InfoErrorHandler.printDirectMessage("Inserting '" + cont.getFunctionalityName() + "' into parent '" + currentInsertionInsideParentIndex + "'.");
					
					
					if (justInsert)
					{
						if(cont.getVisualization().getNode().hasChildrenHidden()) // Copy hidden children
						{
							List<DataNode<ProgramElement>> children = (ArrayList<DataNode<ProgramElement>>) ((ArrayList<DataNode<ProgramElement>>) cont.getVisualization().getNode().getChildrenAlways()).clone();
							cont.getVisualization().addChildrenToInsert(children);
						}
						
						insertElementIntoParentByIndices(cont.getVisualization(), currentInsertionParentIndex, currentInsertionInsideParentIndex);	
					}
					else
					if (duplicateWithData)
						insertElementIntoParentByIndices(tempCreated = cont.getVisualization().duplicateWithData(), currentInsertionParentIndex, currentInsertionInsideParentIndex);
					else
					if (duplicateWithoutData)
						insertElementIntoParentByIndices(cont.getVisualization().duplicateWithoutData(), currentInsertionParentIndex, currentInsertionInsideParentIndex);
					
					
					currentInsertionInsideParentIndex++;
					
					lastInsDepth = newInsDepth;						
				}
				
				return(tempCreated);
			}

			private void insertElementIntoParentByIndices(VisualizableProgramElement newElement, int parentIndex, int subIndex)
			{
				ProgramElement[] dat = new ProgramElement[1];
				DataNode[] node = new DataNode[1];
				DataNode[] parent = new DataNode[1];
				
				Integer[] subInd = new Integer[1];

				parent[0] = null;
				subInd[0] = 0;
				
				// Find the corresponding parent-element in the tree and save it to parent[0]				
				if (parentIndex == -1) // if the parent is the root
				{
					parent[0] = rootElementNode; // root is the parent
					subIndex++;
				}
				else
				rootElementNode.applyToChildrenTotal(dat, node, () -> {
					if (subInd[0] == parentIndex)
						parent[0] = node[0]; // this is the parent
					
					subInd[0]++; // counter
				}, false);
				
				
				
				int innerIndex = 0;
				subInd[0] = subIndex;
				
				DataNode<ProgramElement> parentNode = parent[0];
				
				for(DataNode subNode: parentNode.getChildrenAlways())
				{
					subNode.applyToChildrenTotal(node, () -> {subInd[0]--;}, false);
					
					if (subInd[0] <= 0)
					{
						parentNode.addChildAt(innerIndex, newElement);
						innerIndex = -100000;
						break;
					}

					innerIndex++;					
				}
				
				if (innerIndex > -100) // has not been inserted yet
					parentNode.addChild(newElement);
				
				
			}};

		Runnable outsideEvent = new Runnable()
		{
			@Override
			public void run()
			{
				if (alreadyInitializedDragAction)
				{
					DragAndDropHelper.getTopPane().getChildren().removeAll(previewRects);
					
					for(ProgramElement ele: elementSnapshot)
						ele.getContent().getVisualization().getControlerOnGUI().fadeoutMarking();
				}
				alreadyInitializedDragAction = false;
				
				
				List<Node> childs = contentPane.getChildrenUnmodifiable();
				
				for(Node p: childs)
					p.setTranslateY(0);
			}};
			
			
		DragAndDropHelper.asignDragTarget(sectionBox, null, "-fx-opacity: 1", "-fx-opacity: 0.8", "", "-fx-background-color: positiveFocusCol;", true, hoverEvent, dropEvent, outsideEvent);			
		

		
	}
	
	protected void updateElementSnapshot(List<ProgramElement> elementSnapshot, List<Integer> elementDepthSnapshot)
	{
		elementSnapshot.clear();
		elementDepthSnapshot.clear();
		
		ProgramElement[] dat = new ProgramElement[1];
		Integer[] depth = new Integer[1];
		rootElementNode.applyToChildrenTotal(dat, depth , () -> {
			elementSnapshot.add(dat[0]);
			elementDepthSnapshot.add(depth[0]);
		}, false);	
		
	}

	public void resetPreviewPos()
	{
		alreadyInitializedDragAction = false;
	}
	
	protected void replacePageName(String oldPageName, String newPageName)
	{
		if (dataPage.equals(oldPageName))
			dataPage = newPageName;
	}
	

	public void adjustContainerSize()
	{	
		VisualizableProgramElement[] ele = new VisualizableProgramElement[1];
		Integer[] depth = new Integer[1];
		Integer[] maxDepth = new Integer[1];
		maxDepth[0] = 0;
		rootElementNode.applyToChildrenTotal(ele, depth,
			() -> {
				maxDepth[0] = Math.max(maxDepth[0], depth[0]);
			}, false);
		

		Runnable task = () -> {
    		if (realizedNodes.size() > 0 )
    		{
				ProgramElementOnGUI el = realizedNodes.get(0);
				int H = (int) el.getContainer().localToScene(el.getContainer().getBoundsInLocal()).getHeight();
				
				contentPane.setPrefHeight((realizedNodes.size()+2)*(H + GlobalSettings.elementsVgap));
				sectionBox.setPrefHeight((realizedNodes.size()+2)*(H + GlobalSettings.elementsVgap));
				
				contentPane.setMinWidth(maxDepth[0] * GlobalSettings.elementTreeHgap + contentWidthSum*1.35);
				sectionBox.setPrefWidth(maxDepth[0] * GlobalSettings.elementTreeHgap + contentWidthSum*1.35);
    		}
        };
        
        task.run();
		
		FxTimer.runLater(Duration.ofMillis(250),task);
		FxTimer.runLater(Duration.ofMillis(1250),task);
		
	}


	public void adjustSubelementsSize()
	{
		if (!GlobalSettings.dynamicElementWidth)
		{
			contentWidthSum = 0;
			int mx = 0;
			
			for(int i = 0; i < realizedNodes.size(); i++)
			{
				int subCount = realizedNodes.get(i).getSubelementsCount();
				if (subCount > mx) mx = subCount;
			}
			contentWidthSum = mx * (GlobalSettings.optimalSubelementFieldWidth+10);
			
			return;
		}
		
		
		boolean hasMore;
		int pos = 0;
		contentWidthSum = 0;
		int comWidth = 0;
		int comWidthCount = 0;
		
		do
		{
			hasMore = false;
			
			for(int i = 0; i < realizedNodes.size(); i++)
			{
				if (realizedNodes.get(i).getSubelementsCount() > pos)
					hasMore = true;
			}
			
			if (hasMore)
			{
				int max = GlobalSettings.minSubelementFieldWidth;
				
				for(int i = 0; i < realizedNodes.size(); i++)
				{
					SubElement sub = realizedNodes.get(i).getSubelement(pos);
					if (sub != null)
						max = Math.max(max, sub.getContentWidth());
				}
				
				max = Math.min(GlobalSettings.maxSubelementFieldWidth, max);
				
				for(int i = 0; i < realizedNodes.size(); i++)
				{
					SubElement sub = realizedNodes.get(i).getSubelement(pos);
					if (sub != null)
						sub.setWidth(max+20);
				}
				
				contentWidthSum += max;
				if (comWidthCount++ < 2)
					comWidth += max;
			}
			
			pos++;
		}
		while(hasMore);
		
		for(int i = 0; i < realizedNodes.size(); i++)
		{
			if (realizedNodes.get(i).getElement().getContent().isSpecial(Functionality.Comment))
				if (!realizedNodes.get(i).getElement().getContent().isUndeletable())
					realizedNodes.get(i).getSubelement(2).setWidth(comWidth);
		}

	}
	
	public void clearElementsRealization()
	{
		for (int i = 0; i < realizedNodes.size(); i++)
		{
			realizedNodes.get(i).destroy(false);
			realizedNodes.get(i).getBasePane().setBackground(null);
		}
		realizedNodes.clear();
		
		for(FunctionalityContent cont: markedContents)
			cont.getVisualization().getControlerOnGUI().getBasePane().setBackground(null);
		markedContents.clear();
	}

	public void realizeElements(DataNode<ProgramElement> element, boolean onlyPassSubchilds)
	{
		reVisualisationIndex = 0;
		realizeElements(element, -1, onlyPassSubchilds);
	}
	
	
	public void realizeElements(DataNode<ProgramElement> element)
	{
		reVisualisationIndex = 0;
		realizeElements(element, -1, false);
	}

	
	private void realizeElements(DataNode<ProgramElement> node, int depth, boolean onlyPassSubchilds)
	{
		if (node.isRoot())
		{
			if (childIdentitiyClass == ContentsSectionManager.class)
				sectionManager.showEmptyRootInfoLabel(node.isLeaf()); // if the contents section is empty: show the info label
			
			
			for(DataNode<ProgramElement> subnode: node.getChildren())
			{
				realizeElements(subnode, depth+1, onlyPassSubchilds);
			}
				
			return;
		}
		
		VisualizableProgramElement elVis = (VisualizableProgramElement) node.getData();
		
		ProgramElementOnGUI onGuiEl = null;
		
		if (elVis != null)
			onGuiEl = elVis.realizeOnGUI(contentPane, this, depth, node, onlyPassSubchilds, reVisualisationIndex); // realize on gui
		reVisualisationIndex++;
		
		if (onGuiEl != null)
			realizedNodes.add(onGuiEl);
		
		// Apply to children as well
		if (!node.isLeaf() && !node.hasChildrenHidden())
		{
			for(DataNode<ProgramElement> subnode: node.getChildren())
			{
				realizeElements(subnode, depth+1, onlyPassSubchilds);
			}
			
		}
		
		
	}
	
	
	protected int computeInsertionIndex(int height, int Y, int pulledIndex, int maxNodes, boolean removeOriginal)
	{
		int insertionIndex = Math.max(0, (Y / (height+GlobalSettings.elementsVgap)));
		if (pulledIndex < 10000)
			insertionIndex = Math.max(0, Math.min(insertionIndex, maxNodes-markedContents.size()));
		else
			insertionIndex = Math.max(0, Math.min(insertionIndex, maxNodes));
		
		return(insertionIndex);
	}		

	
	
	// Returns where found and removed
	int removeElementFromTree(VisualizableProgramElement elementToRemove)
	{
		int res = 0;
		for(int i = 0; i < realizedNodes.size(); i++)
		{
			if (realizedNodes.get(i).getElement() == elementToRemove)
			{
				realizedNodes.get(i).destroy(true);
				realizedNodes.remove(i);
				res = i;
				i = realizedNodes.size()+10;
			}
		}

		removeElementFromTreeNode(rootElementNode, elementToRemove);

		return(res);
	}
	
	private boolean removeElementFromTreeNode(DataNode<ProgramElement> node, VisualizableProgramElement elementToRemove)
	{
		if (node.isLeaf()) return(false);
		
		for(DataNode<ProgramElement> n: node.getChildren())
		{
			if (n.getData() == elementToRemove)
			{
				node.removeChild(n);
				return(true);
			}
				
			if (removeElementFromTreeNode(n, elementToRemove))
				return(true);
		}
		
		return(false);
	}
	

	
	public Pane getContentPane()
	{
		return(contentPane);
	}
	public VBox getSectionBox()
	{
		return(sectionBox);
	}
	
	public Class getChildIdentitiyClass()
	{
		return(childIdentitiyClass);
	}


	public void renewElementsRealization(boolean noteSubchilds)
	{
		if (noteSubchilds)
		{
			clearElementsRealization();
			realizeElements(rootElementNode, true);
			realizeElements(rootElementNode, false);			
		}
		else
			renewElementsRealization();
	}
	
	public void renewElementsRealization()
	{
		clearElementsRealization();
		realizeElements(rootElementNode);
	}

	
	public void renewElementsRealizationFull()
	{
		clearElementsRealization();
		realizeElements(rootElementNode);
		
		adjustSubelementsSize();
		adjustContainerSize();
	}
	
	public DataNode<ProgramElement> getRootElementNode()
	{
		return(rootElementNode);
	}
	public void setRootElementNode(DataNode<ProgramElement> rootElementNode)
	{
		this.rootElementNode = rootElementNode;
		renewElementsRealizationFull();
	}

	public void clearNodes(boolean usermodifiableOnly)
	{
		clearElementsRealization();
		
		if (usermodifiableOnly)
		{
			for(DataNode<ProgramElement> rootNode: rootElementNode.getChildrenAlways())
				clearModifiableNodes(rootNode);
		}
		else
			rootElementNode.removeAllChildren();
	}
	
	public void clearModifiableNodes(DataNode<ProgramElement> root)
	{
		if (((VisualizableProgramElement)root.getData()).getIsUserModifiableParentNode()) // if modifiable
			root.removeAllChildren();
		else
		{
			for(DataNode<ProgramElement> rootNode: root.getChildrenAlways())
				clearModifiableNodes(rootNode);
		}
	}
	
	public void scrollToVisualizeNode(VisualizableProgramElement visualization)
	{
		Platform.runLater(() -> {
			sectionManager.getScrollPane().setHvalue(0);
			
			Integer[] posi = new Integer[1];
			ProgramElement[] node = new ProgramElement[1];
			Boolean[] found = new Boolean[1];
			found[0] = false;
			posi[0] = 0;
			visualization.getNode().getRoot().applyToChildrenTotal(node, () -> {if (node[0] == visualization) found[0] = true; if (!found[0]) posi[0]++; }, false);
			
			int pos = Math.max(0, posi[0] * 48 + 48);
			double scrollPos =  1-(sectionManager.getContentPane().getHeight()-(sectionManager.getScrollPane().getViewportBounds().getHeight()-66 + GlobalSettings.elementsVgap ))/pos;
			
			if (pos < 200)
				sectionManager.getScrollPane().setVvalue(0);
			else
				sectionManager.getScrollPane().setVvalue(scrollPos);
		});
		
	}


	public GeneralSectionManager getSectionManager()
	{
		return(sectionManager);
	}

	public MarkingRectangleHelper getMarkingRectangleControler()
	{
		return(markingRectangleHelper);
	}
	
	
}
