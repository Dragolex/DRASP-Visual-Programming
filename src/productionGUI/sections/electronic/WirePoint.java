package productionGUI.sections.electronic;

import java.util.ArrayList;
import java.util.List;

import dataTypes.minor.GridLoc;
import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import main.electronic.Electronics;
import main.electronic.Pin;
import productionGUI.sections.electronic.parents.ElectronicsMouseDraggable;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import settings.GlobalSettings;
import staticHelpers.GuiMsgHelper;
import staticHelpers.KeyChecker;
import staticHelpers.OtherHelpers;

public class WirePoint extends ElectronicsMouseDraggable {
	
	static short IDcounter = 0;
	public short ID = IDcounter++; // unique ID
	
	public short tempID; // only used when saving and is not unique
	
	Pin associatedPin = null;
	private Label pinInfoLabel;	// only if having a pin
	VisualizedComponent surroundingComponent = null;
	
	List<WirePoint> connectedPoints = new ArrayList<>();
	
	Pane node;
	
	WirePoint newCreatedPoint;
	public GridLoc pressedLocation;
	
	private Label associatedLabel = null;
	private FadeTransition associatedLabelTransition = null;
	
	
	public WirePoint(Pin associatedPin, VisualizedComponent visualizedComponent)
	{
		super(new Pane(), GridLoc.fromScaled(visualizedComponent.getLocation().getScaledX() + associatedPin.getUnscaledXonBoard()*SCALE, visualizedComponent.getLocation().getScaledY() + associatedPin.getUnscaledYonBoard()*SCALE));
		
		surroundingComponent = visualizedComponent;
		
		this.associatedPin = associatedPin;
		
		setupInteraction();
	}
	
	public WirePoint(GridLoc loc)
	{
		super(new Pane(), loc);
		
		setupInteraction();
	}
	
	
	private void setupInteraction()
	{
		rotateable = false; // set for parent
		
		Electronics.registerWirePoint(this);
		
		node = (Pane) baseNode;
		node.setMinWidth(gridScale*SCALE);
		node.setMinHeight(gridScale*SCALE);
		
		//Label lb = new Label(Integer.toString(ID));
		//node.getChildren().add(lb);
		
		
		surroundingPane.getChildren().add(node);
		
		updatedPosition();
		
		
		node.setOnMousePressed((MouseEvent e) -> {
			if (isDragging() || (surroundingComponent != null && surroundingComponent.isDragging()) )
				return;
			
			//node.setBackground(clickBc);
			
			pressedLocation = Electronics.getMouseLocationCopy();
			
			if (e.getButton() == MouseButton.PRIMARY)
			{
				if (associatedPin != null) // has associated pin
				{
					ContentsSectionManager.getSelf().getMarkingRectangleControler().unmark();

					newCreatedPoint = new WirePoint(location.copy());
					newCreatedPoint.addConnectionTo(this, true);
					newCreatedPoint.startDragging(true);				
				}
				else
				{
					node.setEffect(GlobalSettings.clickObjectEffect);
					
					// If dragging this, start dragging all other too
					for (VisualizedComponent comp: Electronics.getVisualizedComponents())
					{
						if (comp.getPane().getEffect() == GlobalSettings.hoverObjectEffect)
							comp.startDragging(true);
					}
					for (WirePoint wp: Electronics.getWirePoints())
					{
						if (wp.getNode().getEffect() == GlobalSettings.hoverObjectEffect)
							wp.startDragging(true);
					}
					
					if (!currentlyDragged)
						startDragging(true);
					
				}
			}
		});
		
		
		node.setOnMouseReleased((MouseEvent e) -> {
			
			if ((surroundingComponent != null && surroundingComponent.isDragging()))
				return;
			
			if (Electronics.getMouseLocationCopy().equals(pressedLocation))
			{
				if (e.getButton() == MouseButton.PRIMARY)
				{
					if (newCreatedPoint != null)
						newCreatedPoint.destroy();
					
					stopDragging();
					
					// create a new point
					newCreatedPoint = new WirePoint(location.copy());
					newCreatedPoint.addConnectionTo(this, true);
					newCreatedPoint.startDragging(false); // also move without mouse release!
				}
				else
				if (KeyChecker.isDown(KeyCode.SHIFT)) // If shift down
				{
					destroy();
					System.out.println("------DESTROY!");
				}
				else
				{
					
				}
			
			}
			else
			{
			}
			
			pressedLocation = null;
			newCreatedPoint = null;
		});
		
		new Thread(()-> {
			while(true)
			{
				//for(WirePoint p: fixedConnectedPoints)
				//	System.out.println(ID + " connected to: " + p.ID);
				
				OtherHelpers.sleepNonException(1000);
			}
		}).start();
		
		
		
		
		
		node.setOnMouseEntered((MouseEvent) -> {
			
			if (associatedLabel != null)
			{
				associatedLabel.setVisible(true);
				associatedLabel.toFront();
				
				associatedLabelTransition.setDuration(GlobalSettings.attentionBlinkDurationFast);
				associatedLabelTransition.setFromValue(0.0);
				associatedLabelTransition.setToValue(1.0);
				associatedLabelTransition.playFromStart();
			}
			
			if ((surroundingComponent != null && surroundingComponent.isDragging()))
				return;
			
			node.toFront();
			node.setBackground(hoverBc);
			colorWholeNet(Color.LIME);
		});
		
		
		node.setOnMouseExited((MouseEvent) -> {
			node.toFront();
			
			if (associatedLabel != null)
			{
				associatedLabelTransition.setDuration(GlobalSettings.attentionBlinkDuration);
				
	        	if (associatedLabelTransition.getToValue() == 1.0)
	        		associatedLabelTransition.setFromValue(associatedLabel.getOpacity());
	        	else
	        		associatedLabelTransition.setFromValue(1.0);
	        	
	        	associatedLabelTransition.setToValue(0);
	        	associatedLabelTransition.playFromStart();
			}			
			
			if (!isSelected())
				if (associatedPin != null) // has associated pin
					unshow();
				else
					if (connectedPoints.size() < 2)
						node.setBackground(neutralBc);
					else
						unshow();
					//node.setBackground(onPinBc);
			colorWholeNet(Color.BLACK);
		});

		
		if (associatedPin == null) // has no associated pin
			node.setBackground(neutralBc);
		
		node.toFront();
		
		if (associatedPin != null)
			updatePinInfoPane();
	}	
	

	public void addConnectionTo(WirePoint wirePoint, boolean addBack)
	{
		preUpdatedPosition();
		if (wirePoint != this)
		{
			if (!connectedPoints.contains(wirePoint))
				connectedPoints.add(wirePoint);
			if (addBack)
				wirePoint.addConnectionTo(this, false);
		}
		updatedPosition();
	}
	public void removeConnectionTo(WirePoint wirePoint, boolean removeBack)
	{
		preUpdatedPosition();
		
		if (connectedPoints.contains(wirePoint))
			connectedPoints.remove(wirePoint);
		if (removeBack)
			wirePoint.removeConnectionTo(this, false);

		updatedPosition();
	}
	
	public boolean isConnectedTo(WirePoint wirePoint)
	{
		return(connectedPoints.contains(wirePoint));
	}
	
	
	public void updatePositionByBoard(VisualizedComponent visualizedComponent)
	{
		preUpdatedPosition();
		
		/*
		location.setFromScaled(visualizedComponent.getLocation().getScaledX()
				//+ associatedPin.getUnscaledXonBoard()*SCALE
				+ visualizedComponent.content.getDesign().getAlignmentOffsetX()*SCALE
				+ visualizedComponent.rotateRelativeUnscaledPointX(associatedPin.getUnscaledXonBoard(), associatedPin.getUnscaledYonBoard())*SCALE
				//- gridScale*SCALE
				,
				visualizedComponent.getLocation().getScaledY()
				//+ associatedPin.getUnscaledYonBoard()*SCALE
				+ visualizedComponent.content.getDesign().getAlignmentOffsetY()*SCALE
				+ visualizedComponent.rotateRelativeUnscaledPointY(associatedPin.getUnscaledXonBoard(), associatedPin.getUnscaledYonBoard())*SCALE
				//- gridScale*SCALE
				);
		*/
		
		//double offsX = + visualizedComponent.content.getDesign().getAlignmentOffsetX()*SCALE;
		//double offsY = + visualizedComponent.content.getDesign().getAlignmentOffsetY()*SCALE;
		double offsX = -gridScale*0.5*SCALE;
		double offsY = -gridScale*0.5*SCALE;
		
		location.setFromScaled(visualizedComponent.rotateAbsoluteUnscaledPointX(associatedPin.getUnscaledXonBoard()*SCALE, associatedPin.getUnscaledYonBoard()*SCALE, offsX, offsY) + offsX
				//- gridScale*SCALE
				,
				visualizedComponent.rotateAbsoluteUnscaledPointY(associatedPin.getUnscaledXonBoard()*SCALE, associatedPin.getUnscaledYonBoard()*SCALE) + offsY
				//- gridScale*SCALE
				);

		
		node.toFront();
		
		updatedPosition();
		
		for(WirePoint pins: connectedPoints)
			pins.updateLines(true);
	}
	
	
	@Override
	protected void droppedAfterDragging()
	{
		WirePoint onSpot = Electronics.getWirepointOnPosition(this);
		if (onSpot != null)
		{
			removeLineOffGrid();
			
			for (WirePoint p: connectedPoints)
				onSpot.addConnectionTo(p, true);
			
			onSpot.updatedPosition();
			

			//if (onSpot.associatedPin != null)
			//	for(WirePoint cp: connectedPins)
			//		onSpot.addPinConnectionTo(cp, true); // should happen through rePropagateInNet already

			
			//rePropagateInNet(this);

			destroy();			
		}
		else
		if (wireGrid[location.getIndX()][location.getIndY()] > 0) // placed on a line
		{
			List<WirePoint> newToConnect = Electronics.getWirePointConnectedLinesByWirePoint(this);
			
			if (newToConnect == null || newToConnect.isEmpty())
				return;
			
			boolean inCircuit = false;
			for(WirePoint potentialNewConnection: newToConnect)
				if (potentialNewConnection.isConnectedTo(this))
					inCircuit = true;
			
			if (inCircuit) // if already connected to this
				if (1 != GuiMsgHelper.askQuestionDirect("The circuit you want to connect to, already contains the origin of your wire.\nAre you sure that you want to build a circle with this wire?\nThat can be suboptimal due to electromagnetic effects."))
					return;
			
			reconnectAllTo(newToConnect);
			
		}
		
	}
	
	
	public void reconnectAllTo(List<WirePoint> newToConnect)
	{
		for(WirePoint potentialNewConnectionA: newToConnect)
			for(WirePoint potentialNewConnectionB: newToConnect)
				potentialNewConnectionA.removeConnectionTo(potentialNewConnectionB, false);

		for(WirePoint potentialNewConnection: newToConnect)
		{
			potentialNewConnection.addConnectionTo(this, true); // connect all to this new point
			
			//potentialNewConnection.node.setBackground(new Background(bad));
		}
		
		//rePropagateInNet(this);
	}

	/*
	// Goes through the whole net to detect pin connections
	public void rePropagateInNet(WirePoint wirePoint)
	{
		//List<WirePoint> traversedPins = new ArrayList<WirePoint>();
		List<WirePoint> pins = new ArrayList<WirePoint>();
		
		traversePins(this, wirePoint, new ArrayList<WirePoint>(), pins); // collect all pins
		
		for(WirePoint pinPoint: pins)
			pinPoint.connectedPins.clear(); // clear all connections
	
		for(WirePoint pinPoint: pins)
		{
			if (pinPoint != this)
				pinPoint.addPinConnectionsTo(pins, true); // re-establish all connections
		}
	}
	

	private void traversePins(WirePoint currentPoint, WirePoint nextPoint, List<WirePoint> traversedPins, List<WirePoint> pins)
	{
		traversedPins.add(nextPoint);
		for (WirePoint p: nextPoint.connectedPoints)
		{
			if (p != currentPoint)
			{
				if (traversedPins.contains(p))
				{
					System.out.println("CYCLE DETECTED!");
				}
				else
				{
					if (p.associatedPin != null) // has an associated pin
						pins.add(p);					
					traversePins(nextPoint, p, traversedPins, pins);
				}
			}	
		}
	}
	*/

	public List<WirePoint> getDirectlyConnectedPoints()
	{
		return(connectedPoints);
	}

	
	public List<WirePoint> getAllConnectedPoints(boolean pinsOnly)
	{
		List<WirePoint> traversedPoints = new ArrayList<>();
		List<WirePoint> traversedPins = new ArrayList<>();
		
		traversePoints(this, this, traversedPoints, traversedPins);
		
		if (pinsOnly)
			return(traversedPins);
		else
			return(traversedPoints);
	}
	
	private void traversePoints(WirePoint currentPoint, WirePoint nextPoint, List<WirePoint> traversedPoints, List<WirePoint> traversedPins)
	{
		traversedPoints.add(nextPoint);
		for (WirePoint p: nextPoint.connectedPoints)
			if (p != currentPoint)
			{
				if (!traversedPoints.contains(p))
				{
					if (p.associatedPin != null)
						traversedPins.add(p);
					traversePoints(nextPoint, p, traversedPoints, traversedPins);
				}
			}
	}
	
	public boolean hasLoop()
	{
		List<WirePoint> traversedPoints = new ArrayList<>();
		return(hasLoopRecurs(traversedPoints, this));
	}
	private boolean hasLoopRecurs(List<WirePoint> traversedPoints, WirePoint lastPoint)
	{
		if (traversedPoints.contains(this))
			return(true);

		for (WirePoint p: connectedPoints)
			if (p != lastPoint)
				if (p.hasLoopRecurs(traversedPoints, this))
					return(true);
		
		return(false);
	}
	

	/*
	byte onOtherPin = 0;
	byte onOtherPinB = 0;
	int lastXpos = -1;
	int lastYpos = -1;
	*/
	
	@Override
	public void preUpdatedPosition()
	{
		if (isDragging())
		{
			occupatedGrid[location.getIndX()][location.getIndY()] = 0;
			Electronics.updateFreeGridPoint(location.getIndX(), location.getIndY());
		}
	}
	

	
	@Override
	public void updatedPosition()
	{
		if (pressedLocation != null)
			if (!pressedLocation.equals(location))
				pressedLocation = null;
		
		node.setLayoutX(location.getScaledMiddleX(1)-gridScale*SCALE);
		node.setLayoutY(location.getScaledMiddleY(1)-gridScale*SCALE);
		

		if (false)
		if (associatedPin != null)
		if (pinInfoLabel != null)
		{
			//pinInfoLabel.setLayoutX(location.getScaledMiddleX(1)-gridScale*SCALE + associatedPin.getTextOffsetXunscaled(surroundingComponent.getOrientation()) * SCALE);
			//pinInfoLabel.setLayoutY(location.getScaledMiddleY(1)-gridScale*SCALE + associatedPin.getTextOffsetYunscaled(surroundingComponent.getOrientation()) * SCALE);
			pinInfoLabel.toFront();
		}

		
		
		if (associatedPin != null)
		{
			switch(associatedPin.getEscapeDirection(surroundingComponent.getOrientation()))
			{
			case Pin.ESCAPE_RIGHT:
				escapeTowards(location.getIndX(), location.getIndY(), 1, 0);
				break;
			case Pin.ESCAPE_UP:
				escapeTowards(location.getIndX(), location.getIndY(), 0, -1);
				break;
			case Pin.ESCAPE_LEFT:
				escapeTowards(location.getIndX(), location.getIndY(), -1, 0);
				break;
			case Pin.ESCAPE_DOWN:
				escapeTowards(location.getIndX(), location.getIndY(), 0, 1);
				break;
			}
		}

		occupatedGrid[location.getIndX()][location.getIndY()] = 2;
		Electronics.updateFreeGridPoint(location.getIndX(), location.getIndY());
		
		updateLines(true);
		
	}

	
	private void setGridValueAt(int indX, int indY, byte newVal, byte ifNot)
	{
		indX = Math.min(surroundingComponent.current_right_side, Math.max(surroundingComponent.current_left_side , indX));
		indY = Math.min(surroundingComponent.current_bottom_side, Math.max(surroundingComponent.current_top_side , indY));
		
		if (occupatedGrid[indX][indY] != ifNot)
			occupatedGrid[indX][indY] = newVal;
		
		Electronics.updateFreeGridPoint(indX, indY);
	}

	private void escapeTowards(int indX, int indY, int xinc, int yinc)
	{		
		int xx = indX+xinc, yy = indY+yinc;
		
		while(xx >= surroundingComponent.current_left_side && yy >= surroundingComponent.current_top_side && xx < surroundingComponent.current_right_side && yy < surroundingComponent.current_bottom_side/* && (occupatedGrid[xx][yy] != 1)*/)
		{
			if (occupatedGrid[xx][yy] != 2)
			{
				occupatedGrid[xx][yy] = 0;
				Electronics.updateFreeGridPoint(xx, yy);
			}
			
			xx += xinc;
			yy += yinc;
		}
	}


	List<Line> airWireLines = new ArrayList<Line>();
	public List<Line> wireLines = new ArrayList<>();
	public List<Line> wireLinesTraverses = new ArrayList<>();
	
	private synchronized void updateLines(boolean updateReturn)
	{
		ObservableList<Node> surr = surroundingPane.getChildren();
		
		// Airwire lines
		
		/*
		int len = airWireLines.size();
		int ind = 0;
		for(WirePoint con: connectedPins)
		if (location.isSmallerThan(con.location) || !con.connectedPins.contains(this)) // ensures that only one line is drawn although the points are in each others lists
		{
			Line l;
			if (ind < len)
			{
				l = airWireLines.get(ind);
				l.setVisible(true);
			}
			else
			{
				l = new Line();
				airWireLines.add(l);
				surr.add(l);
				
				l.setStroke(Color.YELLOW.darker());
				l.setStrokeWidth(3);
				l.setOpacity(0.65);
				
				l.setOnMouseEntered((MouseEvent) -> {
					l.setOpacity(1);
					l.setStrokeWidth(5);
				});
				l.setOnMouseExited((MouseEvent) -> {
					l.setOpacity(0.65);
					l.setStrokeWidth(3);
				});
				
				/*
				l.setOnMouseReleased((MouseEvent e) -> {
					if (e.getButton() != MouseButton.PRIMARY)
					if (!connectionToIsFixed(con)) // only remove if the connection is fixed
					{
						removeConnectionTo(con, true);
						removePinConnectionTo(con, true);
					}
					rePropagateInNet(con);
				});
				*//*
				
			}
			
			l.setStartX(location.getScaledX());
			l.setStartY(location.getScaledY());
			
			l.setEndX(con.getLocation().getScaledX());
			l.setEndY(con.getLocation().getScaledY());
			
			ind++;
		}

		for (int i = ind; i < len; i++)
			airWireLines.get(i).setVisible(false);
			//surr.remove(airWireLines.get(i));
		*/
		
		// Wire lines
		
		int len = wireLines.size();
		int ind = 0;

		for (Line l: wireLines)
		{			
			int xv = Electronics.getGridIndex(l.getStartX());
			int yv = Electronics.getGridIndex(l.getStartY());
			
			if (l.isVisible())
			{
				if (wireGrid[xv][yv] == 2)
					wireGrid[xv][yv] = 1;
				else
					wireGrid[xv][yv] = 0;
				Electronics.updateFreeGridPoint(xv, yv);
			}
			
			ind++;
		}
		
		
		ind = 0;
		
		// System.out.println("Length: " + connectedPoints.size() + " for: " + this);
		
		for(Line l: wireLinesTraverses)
			surr.remove(l);
		wireLinesTraverses.clear();
		

		for(Line l: airWireLines)
			l.setVisible(false);
		
		int usedAirWires = 0;
		
		for(WirePoint con: connectedPoints)
		if (location.isSmallerThan(con.location) || !con.connectedPoints.contains(this))
		{
			int[] positions = wirePathfinder.findPath(location.getIndX(), location.getIndY(), con.location.getIndX(), con.location.getIndY());
			if (positions == null || positions.length == 0)
			{
				Line l;
				if (usedAirWires < airWireLines.size())
				{
					l = airWireLines.get(usedAirWires);
					l.setVisible(true);
					l.setOpacity(1);
				}
				else
				{
					l = new Line();
					airWireLines.add(l);
					surr.add(l);
					
					l.setStroke(Color.YELLOW);
				}

				l.setStrokeWidth(3);
				l.setUserData(new WirePoint[] {this, con});

				l.setOnMouseEntered((MouseEvent) -> {
					l.setOpacity(1);
					l.setStrokeWidth(5);
					Electronics.currentlyTouchedAirWire = l;
				});
				l.setOnMouseExited((MouseEvent) -> {
					l.setOpacity(0.65);
					l.setStrokeWidth(3);
					Electronics.currentlyTouchedAirWire = null;
				});
				
				l.setStartX(location.getScaledX());
				l.setStartY(location.getScaledY());
				l.setEndX(con.location.getScaledX());
				l.setEndY(con.location.getScaledY());
				
				usedAirWires++;
				continue;
			}
			
			Line lastL = null;
			
			int eles = positions.length;

			double lastX = positions[0]*gridScale*SCALE, lastY = positions[1]*gridScale*SCALE;

			for(int i = 2; i < eles; i+=2)
			{
				Line l;
				if (ind < len)
				{
					l = wireLines.get(ind);
					l.setVisible(true);
					l.setOpacity(1);
				}
				else
				{
					l = new Line();
					wireLines.add(l);
					surr.add(l);
					l.setOpacity(1);
				}

				l.setStroke(Color.BLACK);
				l.setStrokeWidth(2);
				l.setUserData(con);
				
				
				int xv = Electronics.getGridIndex(lastX);
				int yv = Electronics.getGridIndex(lastY);
				if (wireGrid[xv][yv] == 1)
				{
					wireGrid[xv][yv] = 2;
				}
				
				
				l.setStartX(lastX);
				l.setStartY(lastY);
				
				//double sX = lastX;
				//double sY = lastY;
				
				lastX = positions[i]*gridScale*SCALE;
				lastY = positions[i+1]*gridScale*SCALE;
				
				
				/*
				byte ang = 0;
				if (lastY > sY)
				{
					if (lastX > sX)
						wireLinesAngles[i/2] = 1;
					else
					if (lastX < sX)
						wireLinesAngles[i/2] = 3;
					else
						wireLinesAngles[i/2] = 2;
				}
				else
				if (lastY < sY)
				{
					if (lastX > sX)
						wireLinesAngles[i/2] = -1;
					else
					if (lastX < sX)
						wireLinesAngles[i/2] = -3;
					else
						wireLinesAngles[i/2] = -2;
				}
				else
					if (lastX > sX)
						wireLinesAngles[i/2] = 0;
					else
					if (lastX < sX)
						wireLinesAngles[i/2] = 4;
					else
						System.err.println("--------- ON THE SAME POSTION!");
				
				if (i > 2)
				if (Electronics.isOnWirePointConnectedLinesByGridPosition(xv, yv, this))
				{
					int currentAngle = wireLinesAngles[i/2];
					int lastAngle = wireLinesAngles[i/2-2];
					
					int angle = -lastAngle*45;
					int angLength = 180-(currentAngle-lastAngle)*45;
					
					Arc arc = new Arc();
					arc.setCenterX(xv*gridScale*SCALE);
					arc.setCenterY(yv*gridScale*SCALE);
					arc.setRadiusX(gridScale*SCALE*0.5);
					arc.setRadiusY(gridScale*SCALE*0.5);
					arc.setFill(Color.TRANSPARENT);
					arc.setStroke(Color.BLACK);
					arc.setStrokeWidth(2);
					
					
					System.out.println("Angle: " + angle + " Length: " + angLength);
					
					arc.setStartAngle(angle);
					arc.setLength(angLength);

					wireJumpArcs.add(arc);					
					surr.add(arc);
					
					//lastLine.setOpacity(0);
					l.setOpacity(0);
					
					
				}*/
				
				l.setEndX(lastX);
				l.setEndY(lastY);

				
				if (ind > 0)
				if (lastL != null)
				if (Electronics.isOnWirePointConnectedLinesByGridPositionAndNotLineThick(xv, yv, this, 3))
				{
					/*
					lastL.setStrokeWidth(3);
					l.setStrokeWidth(3);
					

					double _lx = lastL.getStartX();
					double _ly = lastL.getStartY();
					double _endLx = lastL.getEndX();
					double _endLy = lastL.getEndY();

					double _x = l.getStartX();
					double _y = l.getStartY();
					double _endx = l.getEndX();
					double _endy = l.getEndY();
					
					Line L1 = new Line(_lx+(_endLx-_lx)/1.5, _ly+(_endLy-_ly)/1.5, _endLx, _endLy);
					Line L2 = new Line(_x, _y, _endx+(_x-_endx)/1.5, _endy+(_y-_endy)/1.5);
					//Line L1 = new Line(_lx+(_endLx-_lx)/1.5, _ly+(_endLy-_ly)/1.5, _endLx, _endLy);
					//Line L2 = new Line(_x, _y, _endx+(_x-_endx)/1.5, _endy+(_y-_endy)/1.5);
					//Line L1 = new Line(_lx+(_endLx-_lx), _ly+(_endLy-_ly), _endLx - 0.5*(_endLx-_lx), _endLy - 0.5*(_endLy-_ly));
					//Line L2 = new Line(_x + 0.5*(_endx-_x), _y+ 0.5*(_endy-_y), _endx+(_x-_endx), _endy+(_y-_endy));
					
					L1.setStrokeWidth(5);
					L2.setStrokeWidth(5);
					
					//L1.setStroke(Color.WHITE);
					//L1.setOpacity(0.75);
					//L2.setStroke(Color.WHITE);
					//L2.setOpacity(0.75);
					
					L1.toFront();
					L2.toFront();
					
					surr.add(L1);
					surr.add(L2);
					wireLinesTraverses.add(L1);
					wireLinesTraverses.add(L2);

					*/
					
					
					/*
					double _lx = lastL.getStartX();
					double _ly = lastL.getStartY();
					double _endLx = lastL.getEndX();
					double _endLy = lastL.getEndY();

					double _x = l.getStartX();
					double _y = l.getStartY();
					double _endx = l.getEndX();
					double _endy = l.getEndY();
					
					Line L1 = new Line(_lx+(_endLx-_lx)/1.5, _ly+(_endLy-_ly)/1.5, _endLx, _endLy);
					Line L2 = new Line(_x, _y, _endx+(_x-_endx)/1.5, _endy+(_y-_endy)/1.5);
					
					L1.setStrokeWidth(5);
					L2.setStrokeWidth(5);
					
					surr.add(L1);
					surr.add(L2);
					wireLinesTraverses.add(L1);
					wireLinesTraverses.add(L2);*/
					
				}

				lastL = l;
				
				
				ind++;
			}
		}
		
		
		for (int i = ind; i < len; i++)
			wireLines.get(i).setVisible(false);

		

		ind = 0;
		for (Line l: wireLines)
		{			
			int xv = Electronics.getGridIndex(l.getStartX());
			int yv = Electronics.getGridIndex(l.getStartY());
			
			if (l.isVisible())
			{
				if (wireGrid[xv][yv] != 2)
					wireGrid[xv][yv] = 1;
				
				Electronics.updateFreeGridPoint(xv, yv);
			}
			
			ind++;
		}
		
		if (updateReturn)
		{
			for(WirePoint p: connectedPoints)
				p.updateLines(false);
		}
		
		node.toFront();
	}
	
	
	/*
	private boolean connectionToIsFixed(WirePoint target)
	{
		List<WirePoint> visitedPoints = new ArrayList<>();
		
		visitedPoints.add(this);
		
		return(connectionToIsFixed(target, visitedPoints));
	}
	
	private boolean connectionToIsFixed(WirePoint target, List<WirePoint> visitedPoints)
	{
		visitedPoints.add(this);

		if (this == target)
			return(true);
		
		for(WirePoint p: fixedConnectedPoints)
			if (p != this)
				if (!visitedPoints.contains(p))
					if (p.connectionToIsFixed(target, visitedPoints))
						return(true);
		
		return(false);
	}
	*/

	private void removeLineOffGrid()
	{
		for (Line l: wireLines)
		{			
			int xv = Electronics.getGridIndex(l.getStartX());
			int yv = Electronics.getGridIndex(l.getStartY());
			
			if (l.isVisible())
			{
				if (wireGrid[xv][yv] != 2)
					wireGrid[xv][yv] = 0;
				Electronics.updateFreeGridPoint(xv, yv);
			}
		}
	}
	
	public void destroy()
	{
		if (associatedPin != null)
		{
			while(!connectedPoints.isEmpty())
				connectedPoints.get(0).removeConnectionTo(this, true);

			updatedPosition();
			
			return;
		}
		
		
		ObservableList<Node> surr = surroundingPane.getChildren();
		
		surr.remove(node);
		
		Electronics.deAttachFromMouse(this, false);
		ContentsSectionManager.getSelf().getMarkingRectangleControler().enable();
		
		for (Line l: wireLines)
		{			
			int xv = Electronics.getGridIndex(l.getStartX());
			int yv = Electronics.getGridIndex(l.getStartY());
			
			if (l.isVisible())
			{
				if (wireGrid[xv][yv] != 2)
					wireGrid[xv][yv] = 0;
				Electronics.updateFreeGridPoint(xv, yv);
			}
			
			surr.remove(l);
		}
		
		for (Line l: airWireLines)
			surr.remove(l);
		
		
		while(!connectedPoints.isEmpty())
			connectedPoints.get(0).removeConnectionTo(this, true);		
		
		wireLines.clear();
		airWireLines.clear();
		
		occupatedGrid[location.getIndX()][location.getIndY()] = 0;
		Electronics.updateFreeGridPoint(location.getIndX(), location.getIndY());
		
		Electronics.unregisterWirePoint(this);
	}
	
	

	public void toFront()
	{
		node.toFront();		
	}

	@Override
	protected int getWidthOnGrid()
	{
		return 1;
	}

	@Override
	protected int getHeightOnGrid()
	{
		return 1;
	}
	
	@Override
	public boolean checkRotatedOrientation(int anglePos, boolean update_grid)
	{
		return(true);
	}

	public boolean colorLines(Color col)
	{
		boolean coloredSomething = false;
		
		for (Line l: wireLines)
			if (l.isVisible())
			{
				l.setStroke(col); // revert color
				coloredSomething = true;
			}
		/*
		for (Line l: wireLinesTraverses)
			if (l.isVisible())
				//if (l.getStroke() != Color.WHITE)
					l.setStroke(col);
				*/
		
		return(coloredSomething);
	}
	
	public void colorWholeNet(Color col)
	{
		ArrayList<WirePoint> visitedPoints = new ArrayList<>();
		Integer[] depth = new Integer[1]; // Reason that this is needed: That's because the algorithm fails if a wirepoint is connected but no path has been found to form lines
		depth[0] = 500;

		if (colorLines(col))
			visitedPoints.add(this);
		
		for(WirePoint next: connectedPoints)
			next.colorWholeNet(col, visitedPoints, depth);
	}
	private void colorWholeNet(Color col, ArrayList<WirePoint> visitedPoints, Integer[] depth)
	{
		if (depth[0] <= 0)
			return;
		
		depth[0]--;
		
		if (visitedPoints.contains(this))
			return;
		
		if (colorLines(col))
			visitedPoints.add(this);
		
		for(WirePoint next: connectedPoints)
			next.colorWholeNet(col, visitedPoints, depth);
	}
	
	public void show()
	{
		show(false);
	}
	public void show(boolean dark)
	{
		node.setBackground(dark ? neutralBc : hoverBc);
		node.toFront();
	}
	public void unshow()
	{
		//if ((connectedPoints.size() > 1) || (associatedPin != null))
		if ((connectedPoints.size() == 2) || (associatedPin != null))
			node.setBackground(null);
		if (connectedPoints.size() > 2)
		if (associatedPin == null)
		{
			node.setBackground(neutralBc);

			node.setTranslateX(gridScale*SCALE*0.175);
			node.setTranslateY(gridScale*SCALE*0.175);
			
			node.setMinWidth(gridScale*SCALE*0.7);
			node.setMinHeight(gridScale*SCALE*0.7);
		}
	}
	
	private void updatePinInfoPane()
	{
		if (pinInfoLabel == null)
		{
			pinInfoLabel = new Label(associatedPin.toString());
			
			/*
			Pane m = new Pane(pinInfoLabel);
			m.setManaged(false);
			
			m.setMouseTransparent(true);
			*/
			pinInfoLabel.setMouseTransparent(true);
			
			//node.setMaxHeight(20);
			//pinInfoLabel.setVisible(true);
			
			pinInfoLabel.setAlignment(Pos.CENTER);
			
			//pinInfoLabel.setTranslateX(associatedPin.getTextOffsetXunscaled(surroundingComponent.getOrientation()) * SCALE);
			//pinInfoLabel.setTranslateY(associatedPin.getTextOffsetYunscaled(surroundingComponent.getOrientation()) * SCALE);
			
			if (associatedPin.textIsVertical(surroundingComponent.getOrientation()))
				pinInfoLabel.setStyle("-fx-rotate: -90;");
			else
				pinInfoLabel.setStyle("");
			
			//surroundingPane.getChildren().add(pinInfoLabel);
			
			
			//node.getChildren().add(m);
		}
		else
		{
			pinInfoLabel = new Label(associatedPin.toString());
			
			if (associatedPin.textIsVertical(surroundingComponent.getOrientation()))
				pinInfoLabel.setStyle("-fx-rotate: -90;");
			else
				pinInfoLabel.setStyle("");
			
			//pinInfoLabel.setTranslateX(associatedPin.getTextOffsetXunscaled(surroundingComponent.getOrientation()) * SCALE);
			//pinInfoLabel.setTranslateY(associatedPin.getTextOffsetYunscaled(surroundingComponent.getOrientation()) * SCALE);
		}
	}


	public boolean isOnPin()
	{
		return(associatedPin!=null);
	}
	
	public void associatePin(Pin pin)
	{
		associatedPin = pin;
		if (associatedPin != null)
			updatePinInfoPane();
	}
	
	public Pin getAssociatedPin()
	{
		return(associatedPin);
	}

	public Node getNode()
	{
		return(node);
	}

	public void applyColorBc(Background colBc)
	{
		node.setBackground(colBc);
	}

	public void setAssociatedLabel(Label pinInfoLabel, FadeTransition transition)
	{
		associatedLabel = pinInfoLabel;
		associatedLabelTransition = transition;
	}


	
}
