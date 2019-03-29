package productionGUI.sections.electronic.parents;

import java.time.Duration;

import org.reactfx.util.FxTimer;

import dataTypes.minor.GridLoc;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javassist.expr.Instanceof;
import main.electronic.Electronics;
import productionGUI.sections.electronic.WirePoint;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import settings.GlobalSettings;

public abstract class ElectronicsMouseDraggable extends Electronics
{
	protected GridLoc location;
	boolean stopDragingWhenReleaseMouse = false;
	protected boolean currentlyDragged = false;
	protected int componentAngle = 0;
	protected boolean rotateable = true;
	private boolean draggedAsSelected = false;
	
	protected Node baseNode = null;
	
	public ElectronicsMouseDraggable(Node baseNode, GridLoc location)
	{
		this.baseNode = baseNode;
		this.location = location;	
	}
	
	public GridLoc getLocation()
	{
		return(location);
	}
	
	public double dragOffsX, dragOffsY;
	
	public void startDragging(boolean stopWhenRelease)
	{
		currentlyDragged = true;
		stopDragingWhenReleaseMouse = stopWhenRelease;
		//if (nodeIfNeeded != null)
		//	nodeIfNeeded.setMouseTransparent(true);
		Electronics.attachToMouse(this);
		
		ContentsSectionManager.getSelf().getMarkingRectangleControler().disable();
		
		draggedAsSelected = (baseNode.getEffect() == GlobalSettings.hoverObjectEffect);

		Electronics.updateDrawMasks();
	}
	
	public boolean isDragging()
	{
		return(currentlyDragged);
	}
	
	public void stopDragging()
	{
		Electronics.deAttachFromMouse(this, false);
		currentlyDragged = false;
		stopDragingWhenReleaseMouse = false;
		
		droppedAfterDragging();
		
		/*
		if (nodeIfNeeded != null)
			FxTimer.runLater(
		        Duration.ofMillis(500),
		        () -> nodeIfNeeded.setMouseTransparent(false));
		        */
		
		ContentsSectionManager.getSelf().getMarkingRectangleControler().enable();
		if (draggedAsSelected)
			setSelected(true);
		
		Electronics.updateDrawMasks();
	}
	
	public void repositionOnGrid(int indX, int indY)
	{
		indX = Math.max(indX, 0);
		indY = Math.max(indY, 0);
				
		preUpdatedPosition();
		
		if (!freePosition(indX, indY, getWidthOnGrid(), getHeightOnGrid()))
		{
			if (freePosition(indX, location.getIndY(), getWidthOnGrid(), getHeightOnGrid()))
			{
				int ad = (indY > location.getIndY()) ? 1 : -1;

				if (freePosition(indX, location.getIndY()+ad, getWidthOnGrid(), getHeightOnGrid()))
					indY = location.getIndY()+ad;
				else
					indY = location.getIndY();
			}
			else
			if (freePosition(location.getIndX(), indY, getWidthOnGrid(), getHeightOnGrid()))
			{
				int ad = (indX > location.getIndX()) ? 1 : -1;

				if (freePosition(location.getIndX()+ad, indY, getWidthOnGrid(), getHeightOnGrid()))
					indX = location.getIndX()+ad;
				else
					indX = location.getIndX();
			}
			else
			{
				updatedPosition();
				return;
			}
		}

		
		location.setFromInd(indX, indY);
		updatedPosition();
	}
	
	public boolean isSelected()
	{
		return(baseNode.getEffect() == GlobalSettings.hoverObjectEffect);
	}
	public void setSelected(boolean select)
	{
		if (select)
			baseNode.setEffect(GlobalSettings.hoverObjectEffect);
		else
			baseNode.setEffect(null);
	}
	
	public boolean freePosition(int indX, int indY, int w, int h)
	{
		if (this instanceof WirePoint)
			return(occupatedGrid[indX][indY] != 1);
		else
		{
			int lastX = location.getIndX();
			int lastY = location.getIndY();
			location.setFromInd(indX, indY);
			boolean res = checkRotatedOrientation(componentAngle, false);
			location.setFromInd(lastX, lastY);
			return(res);
		}
		/*
		for(int yy = indY; yy < indY+h; yy++)
			for(int xx = indX; xx < indX+w; xx++)
			{
				if (occupatedGrid[xx][yy] != 0)
					return(false);
			}
		return(true);
		*/
	}
	
	
	public boolean getStopDragingWhenReleaseMouse()
	{
		return(stopDragingWhenReleaseMouse);
	}

	public void setStopDragingWhenReleaseMouse()
	{
		stopDragingWhenReleaseMouse = true; 
	}

	public int getOrientation()
	{
		return(componentAngle);
	}
	public void setOrientation(int angle)
	{
		if (!rotateable) return;
		
		preUpdatedPosition();

		componentAngle = angle % 4;
		
		updatedPosition();
	}
	
	
	// Abstract
	protected abstract void droppedAfterDragging();
	protected abstract void preUpdatedPosition();
	protected abstract void updatedPosition();

	protected abstract int getWidthOnGrid();
	protected abstract int getHeightOnGrid();

	public abstract boolean checkRotatedOrientation(int anglePos, boolean update_grid);	
	

	
}
