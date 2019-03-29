package productionGUI.sections.electronic;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import org.reactfx.util.FxTimer;

import dataTypes.ComponentContent;
import dataTypes.minor.GridLoc;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import main.electronic.Electronics;
import main.electronic.Pin;
import main.electronic.attributes.ComponentAttribute;
import main.electronic.attributes.SpecifiedAttribute;
import main.electronic.attributes.TextAttribute;
import otherHelpers.SeparatableString;
import productionGUI.additionalWindows.OverlayMenu;
import productionGUI.sections.electronic.parents.ElectronicsMouseDraggable;
import settings.GlobalSettings;
import staticHelpers.GuiMsgHelper;
import staticHelpers.OtherHelpers;

public class VisualizedComponent extends ElectronicsMouseDraggable {
	
	ComponentContent content;
	
	Pane ownPane, canvasPane;
	
	Rotate rotationTransform = new Rotate();
	
	int current_left_side, current_right_side, current_top_side, current_bottom_side;
	
	List<ComponentAttribute> attributes = new ArrayList<>();
	
	
	private Canvas canvas = null;
	private GraphicsContext gc;
	
	private List<Label> rotationRequiringLabels = new ArrayList<>();
	private List<String> rotationRequiringLabelsStyle = new ArrayList<>();
	private List<FadeTransition> rotationRequiringLabelsAnimations = new ArrayList<>();
	
	
	
	public VisualizedComponent(ComponentContent content, double scaledX, double scaledY, boolean attachThisToMouse)
	{
		super(new Pane(), GridLoc.fromScaled(scaledX, scaledY));
		
		this.content = content;
		
		if (attachThisToMouse)
			startDragging(true);
		
		ownPane = (Pane) baseNode;
		ownPane.getTransforms().add(rotationTransform);
		rotationTransform.setPivotX((getWidthOnGrid()/2)*gridScale*SCALE);
		rotationTransform.setPivotY((getHeightOnGrid()/2)*gridScale*SCALE);
		
		canvasPane = new Pane();
		ownPane.getChildren().add(canvasPane);
		
		ownPane.setPickOnBounds(false);
		
		canvasPane.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {
			
			if (e.getButton() == MouseButton.PRIMARY) // left click
			{
				canvas.setEffect(GlobalSettings.clickObjectEffect);
				
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
			else // rightclick
			if (!currentlyDragged)
			{
				OverlayMenu infMenu = new OverlayMenu(ownPane, () -> {}, true, true);
				for(SpecifiedAttribute attribute: content.getSpecifiedAttributes())
				{
					String nm = attribute.getName()+": " + attribute.getAttribute().getAsText();
										
					infMenu.addButton(nm, () -> {
						
						Object userOut;
						
						if (attribute.getAttribute().isTextType())
							userOut = GuiMsgHelper.getTextDirect("Change value of the attribute: " + attribute.getName(), "Attribute description:\n"+attribute.getDescription(), attribute.getAttribute().get().toString());
						else
							userOut = GuiMsgHelper.getBigDecimalDirect("Change value of the attribute: " + attribute.getName(),"Attribute description:\n"+attribute.getDescription(), (BigDecimal) attribute.getAttribute().get());
						
						if (userOut != null)
							attribute.getAttribute().set(userOut);
						
						infMenu.fade(false);
						
						drawContent(true);						
					
					}, !false);
				}

				
			}
		});
		
		canvasPane.addEventFilter(MouseEvent.MOUSE_RELEASED, (MouseEvent e) -> {
			canvas.setEffect(null);
		});
			
		canvasPane.addEventFilter(MouseEvent.MOUSE_ENTERED, (MouseEvent e) -> {
			if (!currentlyDragged)
			{
				canvas.setEffect(GlobalSettings.hoverObjectEffect);
				int ind = 0;
				for(Label lbl: rotationRequiringLabels)
				{
					lbl.setVisible(true);
					lbl.toFront();
					FadeTransition tr = rotationRequiringLabelsAnimations.get(ind++);
					tr.setDuration(GlobalSettings.attentionBlinkDurationFast);
					tr.setFromValue(0.0);
					tr.setToValue(1.0);
					tr.playFromStart();
				}
			}
		});
		canvasPane.addEventFilter(MouseEvent.MOUSE_EXITED, (MouseEvent e) -> {
			if (!currentlyDragged)
			{
				canvas.setEffect(null);
				int ind = 0;
				for(Label lbl: rotationRequiringLabels)
				{
					FadeTransition tr = rotationRequiringLabelsAnimations.get(ind++);
					tr.setDuration(GlobalSettings.attentionBlinkDuration);
					
		        	if (tr.getToValue() == 1.0)
						tr.setFromValue(lbl.getOpacity());
		        	else
		        		tr.setFromValue(1.0);
		        	
					tr.setToValue(0);
					tr.playFromStart();
				}
			}
		});
	}
	

	
	public Pane drawContent(boolean redraw)
	{		
		double maxWidth = content.getMaxWidth();
		double maxHeight = content.getMaxHeight();

		//ownPane.getChildren().clear();
		
		if (canvas == null)
		{
			canvas = new Canvas(maxWidth*SCALE+1, maxHeight*SCALE+1);
			gc = canvas.getGraphicsContext2D();
			
			canvas.setLayoutX(content.getAlignmentOffsetX()*SCALE);
			canvas.setLayoutY(content.getAlignmentOffsetY()*SCALE);
			canvasPane.getChildren().add(canvas);

			gc.clearRect(0, 0, maxWidth*SCALE+1, maxHeight*SCALE+1);
			content.drawDesign(gc, this);
		}
		else
		if (canvas.getWidth() != maxWidth*SCALE+1)
		{
			canvas.setWidth(maxWidth*SCALE+1);
			canvas.setHeight(maxHeight*SCALE+1);

			redraw = true;
		}	
		
		
		if (redraw)
		{
			ownPane.getChildren().clear();
			ownPane.getChildren().add(canvasPane);			
			gc.clearRect(0, 0, maxWidth*SCALE+1, maxHeight*SCALE+1);
			content.drawDesign(gc, this);		
		}
		
		
		rotationRequiringLabels.clear();
		
		for (Pin associatedPin: content.getPins())
		{
			Label pinInfoLabel = makeAndAddRotationRequiringLabel(associatedPin.toString(), associatedPin.textIsVertical(getOrientation()), Electronics.PIN_LABEL_SIZE);
			
			associatedPin.getAssociatedWirePoint(this).setAssociatedLabel(pinInfoLabel, rotationRequiringLabelsAnimations.get(rotationRequiringLabelsAnimations.size()-1));
			
			FxTimer.runLater(
			        Duration.ofMillis(200),
			        () -> {
						double ww = pinInfoLabel.getLayoutBounds().getWidth();
						double hh = pinInfoLabel.getLayoutBounds().getHeight();
				
						/*
						pinInfoLabel.setLayoutX(associatedPin.getUnscaledXonBoard()*SCALE+ associatedPin.getTextOffsetXunscaled(getOrientation(), ww, hh+3*SCALE) - gridScale*SCALE*0.5+1
								- content.getAlignmentOffsetX()*SCALE);
						pinInfoLabel.setLayoutY(associatedPin.getUnscaledYonBoard()*SCALE+ associatedPin.getTextOffsetYunscaled(getOrientation(), ww+3*SCALE, hh) - gridScale*SCALE*0.5+1
								- content.getAlignmentOffsetY()*SCALE);*/

						
						pinInfoLabel.setLayoutX(associatedPin.getUnscaledXonBoard()*SCALE 
								+ associatedPin.getTextOffsetXunscaled(getOrientation(), ww, hh+3*SCALE)
								//- SCALE/2
								//- gridScale*SCALE*0.5+1
								+ content.getAlignmentOffsetX()*SCALE);
						pinInfoLabel.setLayoutY(associatedPin.getUnscaledYonBoard()*SCALE 
								+ associatedPin.getTextOffsetYunscaled(getOrientation(), ww+3*SCALE, hh)
								+ gridScale*SCALE*0.5
								+ content.getAlignmentOffsetY()*SCALE);

						pinInfoLabel.setVisible(true);
						
						pinInfoLabel.toFront();
			        });	
		}
		
		for (SpecifiedAttribute attr: content.getSpecifiedAttributes())
			if (attr.isExternallyWritten())
			{
				Label attrLabel = makeAndAddRotationRequiringLabel(attr.getAttribute().getAsText(), (Math.floor((getOrientation() + attr.rotation)*2) % 2) == 1, attr.fontSize);
				
				attrLabel.setLayoutX(attr.locatedAtX*SCALE);
				attrLabel.setLayoutY(attr.locatedAtY*SCALE);
				
				attrLabel.setVisible(true);
				
				attrLabel.toFront();
			}
		
		
		updatedPosition();
			
		return(ownPane);
	}
	
	
	private Label makeAndAddRotationRequiringLabel(String string, boolean textIsVertical, double fontSize)
	{
		Label pinInfoLabel = new Label(string);
		
		pinInfoLabel.setMouseTransparent(true);
		pinInfoLabel.getStyleClass().add("smallText");
		
		if (textIsVertical)
		{
			pinInfoLabel.setStyle("-fx-rotate: -90; -fx-font-size: "+((int)(fontSize*SCALE))+";");
			pinInfoLabel.setUserData((Boolean) true);
		}
		else
		{
			pinInfoLabel.setStyle("-fx-font-size: "+((int)(fontSize*SCALE))+";");
			pinInfoLabel.setUserData((Boolean) false);
		}
		
		ownPane.getChildren().add(pinInfoLabel);
		
		
		pinInfoLabel.setVisible(false);
		
		rotationRequiringLabels.add(pinInfoLabel);
		rotationRequiringLabelsStyle.add("-fx-font-size: "+((int)(fontSize*SCALE))+";");
		
		FadeTransition transition = new FadeTransition(GlobalSettings.attentionBlinkDuration);
		transition.setNode(pinInfoLabel);
		transition.setCycleCount(1);
		transition.setAutoReverse(false);
		transition.setFromValue(0.0);
		transition.setToValue(1.0);
		
		transition.playFromStart();
		
		rotationRequiringLabelsAnimations.add(transition);
		
		return(pinInfoLabel);
	}



	@Override
	public boolean checkRotatedOrientation(int anglePos, boolean update_grid)
	{
		if (update_grid)
			updateOccupyGrid(0);
		int o = componentAngle;
		componentAngle = anglePos;
		boolean res = updateOccupyGrid(0, true);
		componentAngle = o;
		if (update_grid)
			updateOccupyGrid(1);
		
		return(res);
	}
	
	private void updateOccupyGrid(int val)
	{
		updateOccupyGrid(val, false);
	}	
	private boolean updateOccupyGrid(int val, boolean checkOnly)
	{
		int sww = (int) (Math.round((content.getWidth()/2) / (gridScale*SCALE)));
		int shh = (int) (Math.round((content.getHeight()/2) / (gridScale*SCALE)));
		
		int sx = location.getIndX();
		int sy = location.getIndY();
		int sw = sx+getWidthOnGrid();
		int sh = sy+getHeightOnGrid();
		
		switch(componentAngle)
		{
		case 1:
			sx -= shh-sww - content.getRotationOffsX(1);
			sy += shh-sww + content.getRotationOffsY(1);
			sw = sx+getHeightOnGrid();
			sh = sy+getWidthOnGrid();
			break;
		case 2:
			sx += content.getRotationOffsX(2);
			sy += content.getRotationOffsY(2);
			sw = sx+getWidthOnGrid();
			sh = sy+getHeightOnGrid();
			break;
			
		case 3:
			sx -= shh-sww - content.getRotationOffsX(3);
			sy += shh-sww + content.getRotationOffsY(3);
			sw = sx+getHeightOnGrid();
			sh = sy+getWidthOnGrid();
			break;
		}
		
		
		if (checkOnly)
		{
			if (sx < 0)
				return(false);
			if (sy < 0)
				return(false);
			if (sw > GRID_W)
				return(false);
			if (sh > GRID_H)
				return(false);
			
			byte b = (byte) val;

			for(int yy = sy; yy < sh; yy++)
				for(int xx = sx; xx < sw; xx++)
				{
					if (occupatedGrid[xx][yy] != b)
						return(false);
				}
		}
		else
		{
			byte b = (byte) val;
			
			for(int yy = sy; yy < sh; yy++)
				for(int xx = sx; xx < sw; xx++)
				{
					occupatedGrid[xx][yy] = b;
					Electronics.updateFreeGridPoint(xx, yy);
				}

			current_left_side = sx;
			current_top_side = sy;
			current_right_side = sw;
			current_bottom_side = sh;
		}
		
		return(true);
	}
	
	
	
	public double rotateAbsoluteUnscaledPointX(double scaledX, double scaledY, double offsX, double offsY)
	{
		switch(componentAngle)
		{
		case 0:
			return(current_left_side*gridScale*SCALE+scaledX);
		case 1:
			return(current_right_side*gridScale*SCALE-scaledY); // + current_width*0.5*gridScale - current_height*0.5*gridScale);
		case 2:
			return(current_right_side*gridScale*SCALE-scaledX);
		case 3:
			return(current_left_side*gridScale*SCALE+scaledY);
		}
		return(0);
	}
	public double rotateAbsoluteUnscaledPointY(double scaledX, double scaledY)
	{
		switch(componentAngle)
		{
		case 0:
			return(current_top_side*gridScale*SCALE+scaledY);
		case 1:
			return(current_top_side*gridScale*SCALE+scaledX); // +current_width*0.5*gridScale + current_height*0.5*gridScale);
		case 2:
			return(current_bottom_side*gridScale*SCALE-scaledY);
		case 3:
			return(current_bottom_side*gridScale*SCALE-scaledX);
		}
		return(0);
	}
	
	@Override
	public void preUpdatedPosition()
	{
		updateOccupyGrid(0);
	}

	int offsss = 0;
	
	@Override
	protected void updatedPosition()
	{
		ownPane.setLayoutX(location.getScaledX());
		ownPane.setLayoutY(location.getScaledY());
		
		rotationTransform.setAngle(componentAngle*90);
		
				
		int ind = 0;
		for(Label lbl: rotationRequiringLabels)
		{
			String extStyle = rotationRequiringLabelsStyle.get(ind);
			
			if (!(boolean) lbl.getUserData()) // is horizontal
			{
				switch(componentAngle % 4)
				{
				case 0: lbl.setStyle("-fx-font-size: 14; "+extStyle); break;
				case 1: lbl.setStyle("-fx-rotate: -180; "+extStyle); break;
				case 2: lbl.setStyle("-fx-rotate: 180; "+extStyle); break;
				case 3: lbl.setStyle(""+extStyle); break;
				}
			}
			else
				switch(componentAngle % 4)
				{
				case 0: lbl.setStyle("-fx-rotate: -90; "+extStyle); break;
				case 1: lbl.setStyle("-fx-rotate: -90; "+extStyle); break;
				case 2: lbl.setStyle("-fx-rotate: +90; "+extStyle); break;
				case 3: lbl.setStyle("-fx-rotate: +90; "+extStyle); break;
				}
			
			ind++;
		}
		

		updateOccupyGrid(1);
		
		content.updateInteractiveElements(gc, ownPane, this);
		
		
		FxTimer.runLater(
		        Duration.ofMillis(150),
		        () -> {
		        	
					for (Pin pin: content.getPins())
						pin.getAssociatedWirePoint(this).toFront();
		        });
	}
	
	
	@Override
	protected void droppedAfterDragging()
	{
		int ind = 0;
		for(Label lbl: rotationRequiringLabels)
		{
			FadeTransition tr = rotationRequiringLabelsAnimations.get(ind++);
			tr.setDuration(GlobalSettings.attentionBlinkDuration);
			
        	if (tr.getToValue() == 1.0)
				tr.setFromValue(lbl.getOpacity());
        	else
        		tr.setFromValue(1.0);
        	
			tr.setToValue(0);
			tr.playFromStart();
		}
	}
	
	public Pane getPane()
	{
		return(ownPane);
	}
	
	public Canvas getCanvas()
	{
		return(canvas);
	}
	
	

	public ComponentContent getComponentContent()
	{
		return(content);
	}
	
	
	@Override
	protected int getWidthOnGrid() {
		return(Electronics.getGridIndex(content.getWidth()));
	}
	
	@Override
	protected int getHeightOnGrid() {
		return(Electronics.getGridIndex(content.getHeight()));
	}
	
	

	public void destroy()
	{
		Electronics.removeComponent(this);
		surroundingPane.getChildren().remove(ownPane);
		
		// Destroy also all pins
		for (Pin pin: content.getPins())
			pin.getAssociatedWirePoint(this).destroy();
	}

	public String toString()
	{
		SeparatableString str = new SeparatableString("&=&");
		str.append(content.getComponentName());
		str.append((int) getLocation().getIndX());
		str.append((int) getLocation().getIndY());
		
		for(ComponentAttribute attribute: attributes)
			str.append(attribute.toString());
		
		return(content.toString());
	}
	
	public static VisualizedComponent fromString(String str) {
		
		String[] eles = str.split("&=&");
	
		String componentName = eles[0];
		// TODO: Create by class lookup
		
		
		ComponentContent con = null; // TODO: Make by name:
		
		int len = eles.length;
		for (int i = 3; i < len; i++)
		{
			con.setAttributeValue(i-3, ComponentAttribute.fromString(eles[i]));
		}
		
		return(new VisualizedComponent(con, Integer.valueOf(eles[1]), Integer.valueOf(eles[2]), false));
	}

	
	
	
}
