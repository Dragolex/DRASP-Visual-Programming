package main.electronic;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import execution.handlers.InfoErrorHandler;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import main.electronic.attributes.ColorAttribute;
import main.functionality.FeatureLoader;
import main.functionality.Functionality;
import otherHelpers.BinaryPathfinding;
import productionGUI.additionalWindows.OverlayMenu;
import productionGUI.sections.electronic.VisualizedComponent;
import productionGUI.sections.electronic.WirePoint;
import productionGUI.sections.electronic.parents.ElectronicsMouseDraggable;
import productionGUI.sections.elementManagers.ActionsSectionManager;
import productionGUI.sections.elementManagers.ConditionsSectionManager;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.elementManagers.EventsSectionManager;
import productionGUI.sections.elementManagers.StructuresSectionManager;
import staticHelpers.GuiMsgHelper;
import staticHelpers.KeyChecker;

import dataTypes.ComponentContent;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.Scheme;
import dataTypes.minor.GridLoc;

public class Electronics extends ProgramElement {

	protected static final double PIN_LABEL_SIZE = 2.25;
	
	protected static ColorAttribute chipColor = new ColorAttribute(Color.rgb(35, 35, 35));
	protected static ColorAttribute copperColor = new ColorAttribute(Color.rgb(184, 115, 51));
	protected static ColorAttribute holeColor = new ColorAttribute(Color.TRANSPARENT); //Color.rgba(0, 0, 0, 0);
	protected static ColorAttribute pcbColor = new ColorAttribute(Color.rgb(34,139,34, 0.75));

	
	protected static Color selectedLime = Color.rgb(192,245,0, 1);
	protected static Color recocomendedGreen = Color.rgb(152,215,0, 1);
	protected static Color identicalBlue = Color.rgb(0,35,225, 1);
	//protected static Color loopOrange = Color.rgb(243,106,2, 1);
	
	
	protected static final Background hoverBc = new Background(new BackgroundFill(Color.rgb(255, 255, 0, 1), new CornerRadii(25, true), Insets.EMPTY)); // TODO: Make those static variables to reuse
	//static final Background clickBc = new Background(new BackgroundFill(Color.rgb(227, 255, 0, 0.5), new CornerRadii(25, true), Insets.EMPTY));
	protected static final Background neutralBc = new Background(new BackgroundFill(Color.rgb(0, 0, 0, 1), new CornerRadii(25, true), Insets.EMPTY));
	protected static final Background badBc = new Background(new BackgroundFill(Color.rgb(255, 0, 0, 1), new CornerRadii(25, true), Insets.EMPTY));

	protected static final Background selectedLimeBc = new Background(new BackgroundFill(selectedLime, new CornerRadii(25, true), Insets.EMPTY));
	protected static final Background recocomendedGreenBc = new Background(new BackgroundFill(recocomendedGreen, new CornerRadii(25, true), Insets.EMPTY));
	protected static final Background identicalBlueBc = new Background(new BackgroundFill(identicalBlue, new CornerRadii(25, true), Insets.EMPTY));	

	
	static String ElectronicsHints = "Drag electronic components here to plan your project\n\n" + 
			"Connect pins with wires by pressing the <Left Mouse Button>\n" +
			"Left-Click onto wires to create joints.\n" +		
			"Rightclick onto wires and joints to remove them\n" + 
			"Rightclick onto elements to rotate (if there is sufficient space)\n" + 
			"Doubleclick components for further info and tasks\n" + 
			"";
	
	
	
	//private static boolean drawFreeBlockedGrid = true;
	//private static Canvas backgroundCanvas;
	private static PixelWriter freeGridCanvasPixelWriter;
	
	public static double SCALE = 5;
	public static int GRID_W = 200;
	public static int GRID_H = 300;
	
	public static double gridScale = 2.54;
	
	
	//private static Color colG = Color.rgb(0, 200, 0, 0.6);
	private static Color colR = Color.rgb(200, 0, 0, 0.6);
	private static Color colY = Color.rgb(200, 200, 0, 0.6);
	private static Color colF = Color.rgb(200, 0, 200, 0.6);
	private static Color colE = Color.rgb(0, 0, 0, 0);
	
	
	
	static List<VisualizedComponent> components = new ArrayList<>();
	protected static List<WirePoint> globalWirePoints = new ArrayList<>();
	
	static Canvas freeGridCanvas = null;
	
	protected static BinaryPathfinding wirePathfinder;
	volatile protected static byte[][] occupatedGrid;
	volatile protected static byte[][] wireGrid;
	//volatile protected static WirePoint[][] wirePointGrid;
	
	
	protected static Pane surroundingPane = new Pane();

	private static VBox infoPane = new VBox();
	private static GridPane netInfoGrid = new GridPane();
	private static Label infoMessageLabel = new Label();

	private static boolean active = false;
	
	private static Circle circleForAddingWirePoint = new Circle();
	
	private static WirePoint justCreatedWirePoint = null;
	private static GridLoc justCreatedWirePointPressedLocation = null;
	
	private static boolean firstLoad = true;
	
	private static boolean alwaysShowingFreeGridCanvas = false;
	
	
	
	
	private static void redraw()
	{
		//surroundingPane.getChildren().clear();
		
		System.out.println("Components count: " + components.size());
		
		ObservableList<Node> lis = surroundingPane.getChildren();
		
		//for (VisualizedComponent comp: components)
		//	surroundingPane.getChildren().add(comp.drawContent(true));
		for (VisualizedComponent comp: components)
		{
			Pane pn = comp.drawContent(true);
			if (!lis.contains(pn))
				lis.add(pn);
		}
		
		freeGridCanvas.toFront();

	}
	
	private static void addComponentContent(ComponentContent content)
	{
		//VisualizedComponent vis = new VisualizedComponent(content, DragAndDropHelper.getMouseX(), DragAndDropHelper.getMouseY(), true);
		VisualizedComponent vis = new VisualizedComponent(content, lastMouseMoveGridIndX*gridScale*SCALE, lastMouseMoveGridIndY*gridScale*SCALE, true);
		vis.dragOffsX = -(content.getMaxWidth()/SCALE)*0.5;
		vis.dragOffsY = -(content.getMaxHeight()/SCALE)*0.5;
		
		surroundingPane.getChildren().add(vis.drawContent(true));
		
		freeGridCanvas.toFront();
		
		addComponent(vis);
	}
	
	public static int snapToGrid(double val)
	{
		return (int) (Math.round(val / (gridScale*SCALE))*(gridScale*SCALE));
	}
	public static int getGridIndex(double val)
	{
		return (int) (Math.round(val / (gridScale*SCALE)));
	}
	
	
	
	public static void addComponent(VisualizedComponent vis)
	{
		components.add(vis);
	}
	
	protected static void removeComponent(VisualizedComponent vis)
	{
		components.remove(vis);
	}
	
	public static void clearAllComponents()
	{
		for (VisualizedComponent comp: components)
			comp.destroy();
		
		components.clear();
	}

	

	public static boolean isActive()
	{
		return(active);
	}


	static private List<ElectronicsMouseDraggable> thingsAttachedToMouse = new ArrayList<>();

	
	static private List<Node> tempList = new ArrayList<>();
	
	
	static int lastMouseMoveGridIndX = -1;
	static int lastMouseMoveGridIndY = -1;
	static boolean currentlyOnLine = false;
	
	static private List<WirePoint> currentlyTouchedWireOriginPoints = new ArrayList<>();

	public static Line currentlyTouchedAirWire = null;
	
	
	public static void switchOnElectronics()
	{
		
		if (firstLoad)
		{
			firstLoad = false;
			
		
			circleForAddingWirePoint.setVisible(false);
			circleForAddingWirePoint.setRadius(gridScale*SCALE*0.75);
			circleForAddingWirePoint.setStroke(Color.YELLOW);
			circleForAddingWirePoint.setStrokeWidth(2);
			circleForAddingWirePoint.setFill(Color.TRANSPARENT);
			circleForAddingWirePoint.setMouseTransparent(true);
			
			
			surroundingPane.setOnMouseDragged((MouseEvent e) -> {
				int indX = getGridIndex(e.getX());
				int indY = getGridIndex(e.getY());
				
				
				if ((lastMouseMoveGridIndX != indX) || (lastMouseMoveGridIndY != indY))
				{
					for (ElectronicsMouseDraggable thing: thingsAttachedToMouse)
					{
						thing.repositionOnGrid((int) (indX+thing.dragOffsX), (int) (indY+thing.dragOffsY));
						if (thing instanceof WirePoint)
							((WirePoint) thing).colorWholeNet(Color.LIME);
					}
					
					lastMouseMoveGridIndX = indX;
					lastMouseMoveGridIndY = indY;
					
					currentlyOnLine = false;
					circleForAddingWirePoint.setVisible(false);
				}
	
			} );
			
			surroundingPane.setOnMouseMoved((MouseEvent e) -> {
				int indX = getGridIndex(e.getX());
				int indY = getGridIndex(e.getY());
				
				if ((lastMouseMoveGridIndX != indX) || (lastMouseMoveGridIndY != indY))
					movedFreeMouse(indX, indY);
	
			} );
			
			surroundingPane.setOnMouseReleased((MouseEvent e) -> {
				if (e.getButton() != MouseButton.PRIMARY) return; // left click
	
				if ((justCreatedWirePoint!=null) && (justCreatedWirePointPressedLocation.equals(justCreatedWirePoint.getLocation())))
				{
					justCreatedWirePoint.stopDragging();
						
					// create a new point
					WirePoint newCreatedPoint = new WirePoint(justCreatedWirePointPressedLocation);
					newCreatedPoint.addConnectionTo(justCreatedWirePoint, true);
					newCreatedPoint.startDragging(!false); // also move without mouse release!
				}
				else
				{
					List<ElectronicsMouseDraggable> tempToRemove = new ArrayList<>(thingsAttachedToMouse.size());
					for (ElectronicsMouseDraggable thing: thingsAttachedToMouse)
					{
						if (thing.getStopDragingWhenReleaseMouse())
							tempToRemove.add(thing);
						else
							thing.setStopDragingWhenReleaseMouse();
		
						if (thing instanceof WirePoint)
							((WirePoint) thing).colorWholeNet(Color.BLACK);
					}
					
					for (ElectronicsMouseDraggable thing: tempToRemove)
						thing.stopDragging();
				}
				
				justCreatedWirePoint = null;
			} );
			
			surroundingPane.setOnMousePressed((MouseEvent e) -> {
				if (e.getButton() == MouseButton.SECONDARY) // right click
				{
					if (thingsAttachedToMouse.isEmpty()) // nothing dragging currently
					{
						
						if (currentlyOnLine)
						{
							List<WirePoint> potentialConnections = getWirePointConnectedLinesByPosition(e.getX(), e.getY());					
							List<WirePoint> affectedPoints = new ArrayList<>();
							
							for(WirePoint p: potentialConnections)
								affectedPoints.addAll(p.getAllConnectedPoints(false));
							
							
							while(!potentialConnections.isEmpty())
							{
								WirePoint p = potentialConnections.get(0);
								for(int i = 1; i < potentialConnections.size(); i++)
								{
									p.removeConnectionTo(potentialConnections.get(i), true);
									
									//for(WirePoint pp: potentialConnections)
									//	p.rePropagateInNet(pp);
								}
								
								potentialConnections = getWirePointConnectedLinesByPosition(e.getX(), e.getY());
							}
	
							for(WirePoint p: affectedPoints)
							{
								p.colorWholeNet(Color.BLACK);
								//p.rePropagateInNet(p);
							}
							
							if (!KeyChecker.isDown(KeyCode.SHIFT)) // If shift is not down
							{
								for(WirePoint p: affectedPoints)
									if (p.getDirectlyConnectedPoints().isEmpty())
										p.destroy();
							}
							for(WirePoint p: affectedPoints)
							{
								if (p.getDirectlyConnectedPoints().size() < 2)
								if (!p.isOnPin())
									p.show(true);
							}
						}
						else
						{
							
							// RIGHTCLICK MENU
							
						}
					}
					else	
					{
						for (ElectronicsMouseDraggable thing: thingsAttachedToMouse)
							if (thing.checkRotatedOrientation(thing.getOrientation()+1, true))
								thing.setOrientation(thing.getOrientation()+1);
					}
				}
				else // left click
				{
					if (currentlyOnLine)
					if (thingsAttachedToMouse.isEmpty()) // nothing dragging currently
					{
						List<WirePoint> potentialConnections = getWirePointConnectedLinesByPosition(e.getX(), e.getY());
						if (!potentialConnections.isEmpty())
						{
							WirePoint wp = new WirePoint(GridLoc.fromMouse(e));
							wp.reconnectAllTo(potentialConnections);
							wp.show();
							wp.startDragging(true);
							justCreatedWirePoint = wp;
							justCreatedWirePointPressedLocation = wp.getLocation().copy();
						}
					}
				}
				
			} );

			
			netInfoGrid.getChildren().clear();
	
			Rectangle A = new Rectangle(0,0, 20, 20);
			A.setFill(selectedLime);
			Rectangle B = new Rectangle(0,0, 20, 20);
			B.setFill(recocomendedGreen);
			Rectangle C = new Rectangle(0,0, 20, 20);
			C.setFill(identicalBlue);
			
			netInfoGrid.add(A, 0, 0);
			Label lA = new Label(" Connected Pins");
			lA.getStyleClass().add("standardBoldText");
			netInfoGrid.add(lA, 1, 0);
	
			netInfoGrid.add(B, 0, 1);
			Label lB = new Label(" Suggested Pins");
			lB.getStyleClass().add("standardBoldText");
			netInfoGrid.add(lB, 1, 1);
	
			netInfoGrid.add(C, 0, 2);
			Label lC = new Label(" Same Type Pins");
			lC.getStyleClass().add("standardBoldText");
			netInfoGrid.add(lC, 1, 2);
			
			//netInfoGrid.add(C, 0, 2);
			//netInfoGrid.add(new Label("CCC"), 1, 2);
			
			infoPane.getChildren().add(netInfoGrid);
			infoMessageLabel.getStyleClass().add("mediumTextBold");
			infoPane.getChildren().add(infoMessageLabel);
			

			
			/*		
			addComponentContent(Cont00_Rasp.create_ContRaspi());
			
			VisualizedComponent resistor = new VisualizedComponent(Basic00_Passive.create_BasicResistor(), snapToGrid(60), snapToGrid(30), false);
			addComponent(resistor);
			
			VisualizedComponent diode = new VisualizedComponent(Basic00_Passive.create_BasicDiode(), snapToGrid(200), snapToGrid(40), false);
			addComponent(diode);
			
			VisualizedComponent electrolyt = new VisualizedComponent(Basic00_Passive.create_BasicElectrolyt(), snapToGrid(300), snapToGrid(50), false);
			addComponent(electrolyt);
	
			VisualizedComponent ceramic = new VisualizedComponent(Basic00_Passive.create_BasicCeramicCapacitor(), snapToGrid(60), snapToGrid(120), false);
			addComponent(ceramic);
			*/
			//VisualizedComponent LED = new VisualizedComponent(Basic00_Passive.create_BasicLED(), snapToGrid(200), snapToGrid(120), false);
			//addComponent(LED);
			/*
			
			VisualizedComponent bc = new VisualizedComponent(Basic00_Passive.create_BasicTransistorBC(), snapToGrid(300), snapToGrid(160), false);
			addComponent(bc);
			
	
			
			VisualizedComponent i2c = new VisualizedComponent(SenAct00_I2C.create_I2CSample(), snapToGrid(300), snapToGrid(260), false);
			addComponent(i2c);
			*/
			
			
			
			
			//int w = (int) (2000 / (gridScale * SCALE));
			occupatedGrid = new byte[GRID_W][GRID_H];
			wireGrid = new byte[GRID_W][GRID_H];
			//wirePointGrid = new WirePoint[w][w];
			
			wirePathfinder = new BinaryPathfinding(occupatedGrid, true);
			//wireGrid = new Grid2d(grid, !true);
			
			
			freeGridCanvas = new Canvas(GRID_W*2, GRID_H*2);
			freeGridCanvasPixelWriter = freeGridCanvas.getGraphicsContext2D().getPixelWriter();
			
			
			updateDrawMasks();
			
			
			
		}
		
		Pane p = ContentsSectionManager.getSelf().getContentPane();
		
		tempList.clear();
		tempList.addAll(p.getChildren()); // save the elements of the current program
		
		p.getChildren().clear();
		p.getChildren().add(surroundingPane);

			
		redraw();
		
		

		
		ObservableList<Node> ch = surroundingPane.getChildren();
		
		//infoPane.setManaged(false);
		infoPane.translateXProperty().bind(ContentsSectionManager.getSelf().getSectionBox().widthProperty().subtract(30).subtract(infoPane.widthProperty()));
		infoPane.setTranslateY(30);
		ch.add(infoPane);
		
		ch.add(circleForAddingWirePoint);
		
		
		Label hintsLabel = new Label(ElectronicsHints);
		hintsLabel.getStyleClass().add("standardBoldText");
		hintsLabel.getStyleClass().add("tenPadding");
		hintsLabel.setOpacity(0.8);
		ch.add(hintsLabel);
		
		
		updateBasicPaneStuff();
		
		
		ContentsSectionManager.getSelf().removePagesButtons();
		
		ContentsSectionManager.getSelf().addTopbarButton(0, "Export Image", () -> {});
		ContentsSectionManager.getSelf().addTopbarButton(1, "Make Program", () -> {}); // Should offer a button to create a list of wiring instructions
		ContentsSectionManager.getSelf().addTopbarCheckbox(2, "Names", () -> {}, () -> {}, true, true);
		ContentsSectionManager.getSelf().addTopbarCheckbox(3, "Values", () -> {}, () -> {}, true, true);
		ContentsSectionManager.getSelf().addTopbarCheckbox(4, "Blocked\nGrid", () -> {alwaysShowingFreeGridCanvas = true; updateDrawMasks();}, () -> {alwaysShowingFreeGridCanvas = false; updateDrawMasks();}, false, false);

		Node[] extrasButton = new Node[1];
		extrasButton[0] = ContentsSectionManager.getSelf().addTopbarButton(5, "Extras", () -> {
			OverlayMenu extrasMenu = new OverlayMenu(extrasButton[0], () -> {}, true, true);
			extrasMenu.addButton("Get components list", () -> {GuiMsgHelper.showInfoMessage("Not implemented yet :(");}, false);
			
			extrasMenu.addButton("Construction instructions", () -> {GuiMsgHelper.showInfoMessage("Not implemented yet :(");}, false);
		}); // Shows a message that informs about shortcuts. Also display thsoe in the area though
		

		// inside shortcutesContentsSectionManager.getSelf().addTopbarButton(2, "Create Base Program", () -> {});

		
		
		ActionsSectionManager.getSelf().setRootElementNode(Functionality.getControlerBoards());
		StructuresSectionManager.getSelf().setRootElementNode(Functionality.getBasicComponents());
		EventsSectionManager.getSelf().setRootElementNode(Functionality.getSensorsActors());
		ConditionsSectionManager.getSelf().setRootElementNode(Functionality.getOtherComponents());
		
		ConditionsSectionManager.getSelf().getSectionManager().showEmptyRootInfoLabel(false);

		
		
		active = true;		
	}


	private static void updateBasicPaneStuff()
	{
		Color lineCol = Color.rgb(255, 255, 255, 0.25);
		
		ObservableList<Node> ch = surroundingPane.getChildren();

		// The background grid
		double offs = -(gridScale*SCALE)/2;
		for(int x = 0; x < occupatedGrid.length; x++)
		{
			Line l = new Line(offs + x*gridScale*SCALE, offs, offs + x*gridScale*SCALE, offs + GRID_H*gridScale*SCALE);
			ch.add(l);
			//l.setCache(true);
			l.setMouseTransparent(true);
			l.toBack();
			l.setStroke(lineCol);
		}
		for(int y = 0; y < occupatedGrid[0].length; y++)
		{
			Line l = new Line(offs, offs + y*gridScale*SCALE, offs + GRID_W*gridScale*SCALE, offs + y*gridScale*SCALE);
			ch.add(l);
			//l.setCache(true);
			l.setMouseTransparent(true);
			l.toBack();
			l.setStroke(lineCol);
		}

		

		ch.add(freeGridCanvas);
		
		freeGridCanvas.setScaleX(gridScale*SCALE*0.5);
		freeGridCanvas.setScaleY(gridScale*SCALE*0.5);
		freeGridCanvas.setLayoutX(offs + ( GRID_W*gridScale*SCALE ) / 2 - GRID_W);
		freeGridCanvas.setLayoutY(offs + ( GRID_H*gridScale*SCALE ) / 2 - GRID_H);
		
		freeGridCanvas.setMouseTransparent(true);
		
		
		surroundingPane.setMinWidth(GRID_W*gridScale*SCALE);
		surroundingPane.setMaxWidth(GRID_W*gridScale*SCALE);
		surroundingPane.setMinHeight(GRID_H*gridScale*SCALE);
		surroundingPane.setMaxHeight(GRID_H*gridScale*SCALE);
	}

	private static void movedFreeMouse(int indX, int indY)
	{
		/*
		for (WirePoint p: currentlyTouchedWireOriginPoints)
		{
			for (Line l: p.wireLines)
				if (l.isVisible())
					l.setStroke(Color.BLACK); // revert color
			for (Line l: p.wireLinesTraverses)
				if (l.isVisible())
					l.setStroke(Color.BLACK);
		}
		*/

		
		for (WirePoint wp: globalWirePoints) // revert color of all lines
			wp.colorLines(Color.BLACK);
		
		//if (!currentlyTouchedWireOriginPoints.isEmpty())
			//currentlyTouchedWireOriginPoints.get(0).colorWholeNet(Color.BLACK); // uncolor the last found net

		currentlyTouchedWireOriginPoints = getWirePointConnectedLinesByGrid(indX, indY, currentlyTouchedWireOriginPoints); // get the new net
			
		if (!currentlyTouchedWireOriginPoints.isEmpty())
		{
			currentlyOnLine = thingsAttachedToMouse.isEmpty();
			circleForAddingWirePoint.setCenterX(indX*gridScale*SCALE);
			circleForAddingWirePoint.setCenterY(indY*gridScale*SCALE);
			circleForAddingWirePoint.setVisible(true);
			
			StringBuilder str = new StringBuilder("Pin Types:\n");
			
			List<Integer> totalTypes = new ArrayList<>();
			List<Double> totalValues = new ArrayList<>();
			
			for (WirePoint wp: currentlyTouchedWireOriginPoints.get(0).getAllConnectedPoints(true))
			{
				Iterator<Integer> types = wp.getAssociatedPin().types.iterator();
				Iterator<Double> values = wp.getAssociatedPin().values.iterator();

				while (types.hasNext() && values.hasNext())
				{
					Integer type = types.next();
					Double val = values.next();
					
					totalTypes.add(type);
					totalValues.add(val);
					
					if (val == null)
						str.append( Pin.GET_TYPE_STR(type) );
					else
					{
						str.append( Pin.GET_TYPE_STR(type) );
						str.append( " (" + String.valueOf(val) + ")" );
					}	
					str.append("\n");
				}
				
				
			}
			
			if (currentlyTouchedWireOriginPoints.get(0).hasLoop())
				str.append("\nWire contains a loop!\nThis might be problematic\nfor electromagnetic reasons.");
			
			
			infoMessageLabel.setText(str.toString());
			
			currentlyTouchedWireOriginPoints.get(0).colorWholeNet(selectedLime);
			
			
			for (WirePoint wp: globalWirePoints)
			{
				if (wp.getAssociatedPin() != null)
					if (!currentlyTouchedWireOriginPoints.contains(wp)) // all pins not connected to this pin
					{
						Iterator<Integer> totTypes = totalTypes.iterator();
						Iterator<Double> totValues = totalValues.iterator();
						
						while (totTypes.hasNext() && totValues.hasNext())
						{
							Integer type = totTypes.next();
							Double val = totValues.next();
							
							
							Iterator<Integer> types = wp.getAssociatedPin().types.iterator();
							Iterator<Double> values = wp.getAssociatedPin().values.iterator();

							while (types.hasNext() && values.hasNext())
							{
								Integer tp = types.next();
								Double vl = values.next();
								
								if (Pin.Connection_Reccomended(type, val, tp, vl))
								{
									wp.applyColorBc(recocomendedGreenBc);
									wp.colorWholeNet(recocomendedGreen);
								}
								else
									if (type == tp && val == vl)
										wp.applyColorBc(identicalBlueBc);
							}
						}
					}
			}
			
			
			
			netInfoGrid.setVisible(true);
		}
		else
		{
			netInfoGrid.setVisible(false);
			
			infoMessageLabel.setText("");

			currentlyOnLine = false;
			circleForAddingWirePoint.setVisible(false);
		}
						
		for (ElectronicsMouseDraggable thing: thingsAttachedToMouse)
			thing.repositionOnGrid((int) (indX+thing.dragOffsX), (int) (indY+thing.dragOffsY));
		
		lastMouseMoveGridIndX = indX;
		lastMouseMoveGridIndY = indY;
	}

	public static void switchOffElectronics()
	{
		Pane p = ContentsSectionManager.getSelf().getContentPane();
		p.getChildren().clear();
		p.getChildren().addAll(tempList);
		
		active = false;
				
		ActionsSectionManager.getSelf().setRootElementNode(Functionality.getEvents());
		
		ContentsSectionManager.getSelf().revertTopbarButtons();
		
		surroundingPane.getChildren().clear();
		
		Functionality.applyRootElementNodes();
		
		ContentsSectionManager.getSelf().renewElementsRealizationFull();
	}
	

	public static GridLoc getMouseLocationCopy()
	{
		return(GridLoc.fromInd(lastMouseMoveGridIndX, lastMouseMoveGridIndY));
	}
	
	
	static public void updateFreeGridPoint(int x, int y)
	{
		Color col = colE;
		switch(occupatedGrid[x][y])
		{
		case 1: col = colR; break;
		case 2: col = colF; break;
		//case 3: col = colY; break;
		//case 4: col = colY; break;
		}
		int xx = x*2;
		int yy = y*2;
		
		if (wireGrid[x][y] > 0)
			col = colY;
		
		if (freeGridCanvasPixelWriter == null) return;
		
		freeGridCanvasPixelWriter.setColor(xx, yy, col);
		freeGridCanvasPixelWriter.setColor(xx+1, yy, col);
		freeGridCanvasPixelWriter.setColor(xx, yy+1, col);
		freeGridCanvasPixelWriter.setColor(xx+1, yy+1, col);
	}
	
	
	public static void attachToMouse(ElectronicsMouseDraggable thing)
	{
		thingsAttachedToMouse.add(thing);
		thing.dragOffsX = thing.getLocation().getIndX()-lastMouseMoveGridIndX;
		thing.dragOffsY = thing.getLocation().getIndY()-lastMouseMoveGridIndY;
	}

	public static void deAttachFromMouse(ElectronicsMouseDraggable thing, boolean callStopDragging)
	{
		if (callStopDragging)
			thing.stopDragging();
		if (thingsAttachedToMouse.contains(thing))
			thingsAttachedToMouse.remove(thing);
	}

	public static void addVisualElement(Rectangle ele)
	{
		surroundingPane.getChildren().add(ele);
	}

	
	
	public static void registerWirePoint(WirePoint wirePoint)
	{
		globalWirePoints.add(wirePoint);
	}

	public static void unregisterWirePoint(WirePoint wirePoint)
	{
		globalWirePoints.remove(wirePoint);
	}
	
	public static void clearAllWirePoints()
	{
		for (WirePoint p: globalWirePoints)
			p.destroy();
		
		globalWirePoints.clear();
	}

	public static WirePoint getWirepointOnPosition(WirePoint ignoreThis)
	{
		GridLoc loc = ignoreThis.getLocation();
		for (WirePoint p: globalWirePoints)
		{
			if (p != ignoreThis)
				if (p.getLocation().equals(loc))
					return(p);
		}
		
		return(null);
	}
	
	
	public static List<WirePoint> getWirePointConnectedLinesByWirePoint(WirePoint ignoreThis)
	{
		double gridX = ignoreThis.getLocation().getIndX();//*gridScale*SCALE;
		double gridY = ignoreThis.getLocation().getIndY();//*gridScale*SCALE;
		
		List<WirePoint> ret = new ArrayList<WirePoint>();
		
		if (currentlyTouchedAirWire != null)
		{
			ret.add( ((WirePoint[])(currentlyTouchedAirWire.getUserData()))[0]);
			ret.add( ((WirePoint[])(currentlyTouchedAirWire.getUserData()))[1]);
		}
		
		for (WirePoint p: globalWirePoints)
			if (p != ignoreThis)
				for(Line l: p.wireLines)
					if (l.isVisible())
						if (Math.round(l.getStartX()/(gridScale*SCALE)) == gridX && Math.round(l.getStartY()/(gridScale*SCALE)) == gridY)
						{
							if (!p.isConnectedTo(ignoreThis))
							{
								ret.add(p);
								ret.add((WirePoint) l.getUserData());
							}
						}
		
		return(ret);
	}
	public static List<WirePoint> getWirePointConnectedLinesByPosition(double x, double y)
	{
		int gridX = (int) Math.round(x/(gridScale*SCALE));
		int gridY = (int) Math.round(y/(gridScale*SCALE));
		
		List<WirePoint> ret = new ArrayList<WirePoint>();
		
		if (currentlyTouchedAirWire != null)
		{
			ret.add( ((WirePoint[])(currentlyTouchedAirWire.getUserData()))[0]);
			ret.add( ((WirePoint[])(currentlyTouchedAirWire.getUserData()))[1]);
		}
		
		for (WirePoint p: globalWirePoints)
			for(Line l: p.wireLines)
				if (l.isVisible())
					if (Math.round(l.getStartX()/(gridScale*SCALE)) == gridX && Math.round(l.getStartY()/(gridScale*SCALE)) == gridY)
					{
						ret.add(p);
						ret.add((WirePoint) l.getUserData());
					}
		
		return(ret);
	}
	public static List<WirePoint> getWirePointConnectedLinesByGrid(int gridX, int gridY, List<WirePoint> ret)
	{
		ret.clear();
		
		for (WirePoint p: globalWirePoints)
			for(Line l: p.wireLines)
				if (l.isVisible())
					if (Math.round(l.getStartX()/(gridScale*SCALE)) == gridX && Math.round(l.getStartY()/(gridScale*SCALE)) == gridY)
					{
						ret.add(p);
						ret.add((WirePoint) l.getUserData());
					}
		
		return(ret);
	}
	
	/*
	public static boolean isOnWirePointConnectedLinesByGridPosition(int gridX, int gridY)
	{
		for (WirePoint p: wirePoints)
			for(Line l: p.wireLines)
				if (l.isVisible())
					if (Math.round(l.getStartX()/(gridScale*SCALE)) == gridX && Math.round(l.getStartY()/(gridScale*SCALE)) == gridY)
						return(true);
		
		return(false);
	}*/
	
	
	public static boolean isOnWirePointConnectedLinesByGridPositionAndNotLineThick(int gridX, int gridY, WirePoint ignoreThis,	int thickness)
	{
		for (WirePoint p: globalWirePoints)
			if (p != ignoreThis)
				for(Line l: p.wireLines)
					if (l.isVisible())
						if (Math.round(l.getStartX()/(gridScale*SCALE)) == gridX && Math.round(l.getStartY()/(gridScale*SCALE)) == gridY)
							if (l.getStrokeWidth() != thickness)
								return(true);
		
		return(false);
	}
	
	
	
	
	public static List<VisualizedComponent> getVisualizedComponents()
	{
		return(components);
	}

	
	
	public Scheme getCurrentSchemeStructure()
	{
		return(Scheme.FromVisualizedStructures(components, globalWirePoints));
	}

	public static List<WirePoint>getWirePoints()
	{
		return(globalWirePoints);
	}	

	public static void updateDrawMasks()
	{
		if (freeGridCanvas != null)
			freeGridCanvas.setVisible(!thingsAttachedToMouse.isEmpty() || alwaysShowingFreeGridCanvas);
	}

	
	
	public static void visualizeComponentContent(String name)
	{
		try
		{	
			ComponentContent visElement = (ComponentContent) FeatureLoader.creatingMethods.get(name).invoke(null);
			Electronics.addComponentContent(visElement);
			//redraw();
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NullPointerException e)
		{
			InfoErrorHandler.callPrecompilingError("Failed to visualize the following component: " + name + "\nIf you ran this program directly, you have the wrong version for the file you loaded.\nIf you are working on the Java source code of project, please ensure that a class in a subdirectory of 'resources/electronic'\nhas the following public function: \ncreate_" + name + "()");
			e.printStackTrace();
		}
	}
	
	

	@Override
	public boolean isEvent() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String getFunctionalityName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUndeletable(boolean undeletable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FunctionalityContent getContent() {
		// TODO Auto-generated method stub
		return null;
	}

}
