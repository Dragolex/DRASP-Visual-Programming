package dataTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import main.electronic.Electronics;
import main.electronic.Pin;
import main.electronic.PinRow;
import main.electronic.attributes.ColorAttribute;
import main.electronic.attributes.ComponentAttribute;
import main.electronic.attributes.SpecifiedAttribute;
import main.electronic.attributes.TextAttribute;
import main.functionality.SharedComponents;
import productionGUI.sections.electronic.VisualizedComponent;
import productionGUI.sections.electronic.WirePoint;

public class ComponentContent extends Electronics {
	
	static Map<Double, Font> sizedFonts = new HashMap<>();

	
	String displayName = "";
	String componentName = null;
	

	List<Runnable> drawTasks = new ArrayList<>();
	List<Pin> pins = new ArrayList<>();
	
	List<SpecifiedAttribute> specifiedAttributes = new ArrayList<>();
	
	double baseWidth;
	double baseHeight;

	double maxWidth = 0;
	double maxHeight = 0;

	int rotationOffsX90 = 0;
	int rotationOffsY90 = 0;
	int rotationOffsX180 = 0;
	int rotationOffsY180 = 0;

	
	Double alignmentOffsetX = null;
	Double alignmentOffsetY = null;
	
	boolean scaleable = false;
	
	String tooltip = "TODO: TOOLTIP";
	
	
	public ComponentContent(String name, String componentName)
	{
		this.displayName = name;
		
		this.componentName = componentName;
	}
	public ComponentContent(String name, double baseWidth, double baseHeight)
	{
		this.displayName = name;
		this.baseWidth = baseWidth;
		this.baseHeight = baseHeight;
		
		resetCreatorName();
	}
	public ComponentContent(String name, double baseWidth, double baseHeight, boolean draw_rect)
	{
		this.displayName = name;
		this.baseWidth = baseWidth;
		this.baseHeight = baseHeight;

		addRect(0, 0, baseWidth, baseHeight, pcbColor);
		resetCreatorName();
	}
	public ComponentContent(String name, double baseWidth, double baseHeight, boolean draw_rect, double rounding)
	{
		this.displayName = name;
		this.baseWidth = baseWidth;
		this.baseHeight = baseHeight;

		addRoundrect(0, 0, baseWidth, baseHeight, rounding, pcbColor);
		resetCreatorName();
	}

	public void resetCreatorName()
	{		
		if (componentName == null)
			componentName = SharedComponents.extractFunctionalityName(3);
	}
	
	// Corrects the limits taking into account that negative X and Y values are always relative to the right edge of the base roundrect
	private void correctLimits(double x, double y, double w, double h)
	{
		if (x >= 0)
			maxWidth = Math.max(maxWidth, x+w);
		else
			maxWidth = Math.max(maxWidth, baseWidth+x+w);

		if (y >= 0)
			maxHeight = Math.max(maxHeight, y+h);
		else
			maxHeight = Math.max(maxHeight, baseHeight+y+h);
	}
	private void correctLimits(double x, double y)
	{
		if (x >= 0)
			maxWidth = Math.max(maxWidth, x);
		else
			maxWidth = Math.max(maxWidth, baseWidth+x);

		if (y >= 0)
			maxHeight = Math.max(maxHeight, y);
		else
			maxHeight = Math.max(maxHeight, baseHeight+y);
	}
	
	
	
	public void addRoundrect(double x, double y, double width, double height, double rounding)
	{
		addRoundrect(x, y, width, height, rounding, (ColorAttribute) null);
	}
	public void addRoundrect(double x, double y, double width, double height, double rounding, Color col)
	{
		addRoundrect(x, y, width, height, rounding, new ColorAttribute(col));
	}
	public void addRoundrect(double x, double y, double width, double height, double rounding, ColorAttribute col)
	{
		correctLimits(x, y, width, height);
		double xx = adaptX(x); double yy = adaptY(y);
		
		drawTasks.add(() -> {
			
			gc.setStroke(Color.BLACK);

			gc.strokeRoundRect(xx*SCALE, yy*SCALE, width*SCALE, height*SCALE, rounding*SCALE, rounding*SCALE);
			if (col != null)
			{
				gc.setFill(col.get());
				gc.fillRoundRect(xx*SCALE, yy*SCALE, width*SCALE, height*SCALE, rounding*SCALE, rounding*SCALE);
			}
			
		});		
	}
	
	public void addRoundrectAbs(double x, double y, double x2, double y2, double rounding)
	{
		addRoundrectAbs(x, y, x2, y2, rounding, (ColorAttribute) null);
	}
	public void addRoundrectAbs(double x, double y, double x2, double y2, double rounding, Color col)
	{
		addRoundrectAbs(x, y, x2, y2, rounding, new ColorAttribute(col));
	}
	public void addRoundrectAbs(double x, double y, double x2, double y2, double rounding, ColorAttribute col)
	{
		correctLimits(x, y);
		correctLimits(x2, y2);
		double xx = adaptX(x); double yy = adaptY(y);
		double xx2 = adaptX(x2); double yy2 = adaptY(y2);
		
		drawTasks.add(() -> {
			
			gc.setStroke(Color.BLACK);

			gc.strokeRoundRect(xx*SCALE, yy*SCALE, xx2*SCALE - xx*SCALE, yy2*SCALE - yy*SCALE, rounding*SCALE, rounding*SCALE);
			if (col != null)
			{
				gc.setFill(col.get());
				gc.fillRoundRect(xx*SCALE, yy*SCALE, xx2*SCALE - xx*SCALE, yy2*SCALE - yy*SCALE, rounding*SCALE, rounding*SCALE);
			}
			
		});		
	}

	


	public void addRect(double x, double y, double width, double height)
	{
		addRect(x, y, width, height, (ColorAttribute) null);
	}
	public void addRect(double x, double y, double width, double height, Color col)
	{
		addRect(x, y, width, height, new ColorAttribute(col));
	}
	public void addRect(double x, double y, double width, double height, ColorAttribute col)
	{
		correctLimits(x, y, width, height);
		double xx = adaptX(x); double yy = adaptY(y);
		
		drawTasks.add(() -> {
			
			gc.setStroke(Color.BLACK);
			gc.strokeRect(xx*SCALE, yy*SCALE, width*SCALE, height*SCALE);
			if (col != null)
			{
				gc.setFill(col.get());
				gc.fillRect(xx*SCALE, yy*SCALE, width*SCALE, height*SCALE);
			}

		});		
	}
	
	public void addRectAbs(double x, double y, double x2, double y2)
	{
		addRectAbs(x, y, x2, y2, (ColorAttribute) null);
	}
	public void addRectAbs(double x, double y, double x2, double y2, Color col)
	{
		addRectAbs(x, y, x2, y2, new ColorAttribute(col));
	}
	public void addRectAbs(double x, double y, double x2, double y2, ColorAttribute col)
	{
		correctLimits(x, y);
		correctLimits(x2, y2);
		double xx = adaptX(x); double yy = adaptY(y);
		double xx2 = adaptX(x2); double yy2 = adaptY(y2);
		
		drawTasks.add(() -> {
			
			gc.setStroke(Color.BLACK);
			gc.strokeRect(xx*SCALE, yy*SCALE, xx2*SCALE-xx*SCALE, yy2*SCALE-yy*SCALE);
			if (col != null)
			{
				gc.setFill(col.get());
				gc.fillRect(xx*SCALE, yy*SCALE, xx2*SCALE-xx*SCALE, yy2*SCALE-yy*SCALE);
			}

		});		
	}
	
	
	public void addQuadPoly(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		addQuadPoly(x1, y1, x2, y2, x3, y3, x4, y4, (ColorAttribute) null);
	}
	public void addQuadPoly(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, Color col)
	{
		addQuadPoly(x1, y1, x2, y2, x3, y3, x4, y4, new ColorAttribute(col));
	}
	public void addQuadPoly(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, ColorAttribute col)
	{
		correctLimits(x1, y1);
		correctLimits(x2, y2);
		correctLimits(x3, y3);
		correctLimits(x4, y4);
		double xx1 = adaptX(x1); double yy1 = adaptY(y1);
		double xx2 = adaptX(x2); double yy2 = adaptY(y2);
		double xx3 = adaptX(x3); double yy3 = adaptY(y3);
		double xx4 = adaptX(x4); double yy4 = adaptY(y4);
		
		drawTasks.add(() -> {
			
			gc.setStroke(Color.BLACK);			
			gc.strokePolygon(new double[] {xx1*SCALE, xx2*SCALE, xx3*SCALE, xx4*SCALE}, new double[] {yy1*SCALE, yy2*SCALE, yy3*SCALE, yy4*SCALE}, 4);
			
			if (col != null)
			{
				gc.setFill(col.get());
				gc.fillPolygon(new double[] {xx1*SCALE, xx2*SCALE, xx3*SCALE, xx4*SCALE}, new double[] {yy1*SCALE, yy2*SCALE, yy3*SCALE, yy4*SCALE}, 4);
			}
			
		});		
	}
	
	

	public void addEllipse(double x, double y, double width, double height)
	{
		addEllipse(x, y, width, height, (ColorAttribute) null);
	}
	public void addEllipse(double x, double y, double width, double height, Color col)
	{
		addEllipse(x, y, width, height, new ColorAttribute(col));
	}
	public void addEllipse(double x, double y, double width, double height, ColorAttribute col)
	{
		correctLimits(x, y, width, height);
		double xx = adaptX(x); double yy = adaptY(y);
		
		drawTasks.add(() -> {
			
			gc.setStroke(Color.BLACK);
			gc.strokeOval(xx*SCALE, yy*SCALE, width*SCALE, height*SCALE);
			if (col != null)
			{
				gc.setFill(col.get());
				gc.fillOval(xx*SCALE, yy*SCALE, width*SCALE, height*SCALE);
			}

		});		
	}

	public void addEllipseAbs(double x, double y, double x2, double y2)
	{
		addEllipseAbs(x, y, x2, y2, (ColorAttribute) null);
	}
	public void addEllipseAbs(double x, double y, double x2, double y2, Color col)
	{
		addEllipseAbs(x, y, x2, y2, new ColorAttribute(col));
	}
	public void addEllipseAbs(double x, double y, double x2, double y2, ColorAttribute col)
	{
		correctLimits(x, y);
		correctLimits(x2, y2);
		double xx = adaptX(x); double yy = adaptY(y);
		double xx2 = adaptX(x2); double yy2 = adaptY(y2);
		
		drawTasks.add(() -> {
			
			gc.setStroke(Color.BLACK);
			gc.strokeOval(xx*SCALE, yy*SCALE, xx2*SCALE-xx*SCALE, yy2*SCALE-yy*SCALE);
			if (col != null)
			{
				gc.setFill(col.get());
				gc.fillOval(xx*SCALE, yy*SCALE, xx2*SCALE-xx*SCALE, yy2*SCALE-yy*SCALE);
			}

		});		
	}
	
	
	public void addText(ComponentAttribute text, double x, double y, double size, Color col, int rotation)
	{
		addText(text, x, y, size, new ColorAttribute(col), rotation);
	}
	public void addText(String text, double x, double y, double size, Color col, int rotation)
	{
		addText(new TextAttribute(text), x, y, size, new ColorAttribute(col), rotation);
	}
	public void addText(ComponentAttribute text, double x, double y, double size, ColorAttribute col, int rotation)
	{
		double xx = adaptX(x); double yy = adaptY(y);
		
		//gc.setFont(arg0);
		drawTasks.add(() -> {
			if (!sizedFonts.containsKey(size*SCALE))
				sizedFonts.put(size*SCALE, new Font(size*SCALE));
			gc.setFont(sizedFonts.get(size*SCALE));			

			gc.setTextAlign(TextAlignment.CENTER);
			gc.setFill(col.get());
			gc.rotate(rotation);
			gc.fillText(text.getAsText(), xx*SCALE, yy*SCALE);
			gc.restore();
		});
	}
	
	
	
	public void addLine(double x, double y, double x2, double y2)
	{
		addLine(x, y, x2, y2, (ColorAttribute) null, 1);
	}
	public void addLine(double x, double y, double x2, double y2, Color col)
	{
		addLine(x, y, x2, y2, new ColorAttribute(col), 1);
	}
	public void addLine(double x, double y, double x2, double y2, ColorAttribute col)
	{
		addLine(x, y, x2, y2, col, 1);
	}
	public void addLine(double x, double y, double x2, double y2, double width)
	{
		addLine(x, y, x2, y2, (ColorAttribute) null, width);
	}
	public void addLine(double x, double y, double x2, double y2, Color col, double width)
	{
		addLine(x, y, x2, y2, new ColorAttribute(col), width);
	}
	public void addLine(double x, double y, double x2, double y2, ColorAttribute col, double width)
	{
		correctLimits(x, y);
		correctLimits(x2, y2);
		double xx = adaptX(x); double yy = adaptY(y);
		double xx2 = adaptX(x2); double yy2 = adaptY(y2);
		
		drawTasks.add(() -> {
			
			if (width != 1)
				gc.setLineWidth(width);
			
			gc.setStroke(Color.BLACK);
			if (col != null)
				gc.setStroke(col.get());
			gc.strokeLine(xx*SCALE, yy*SCALE, xx2*SCALE, yy2*SCALE);
			gc.setStroke(Color.BLACK);

			if (width != 1)
				gc.setLineWidth(1);
			
		});		
	}



	public void addRingedHole(double x, double y, double holeDiam, double outerDiam, Color ringColor)
	{
		addRingedHole(x, y, holeDiam, outerDiam, new ColorAttribute(ringColor));
	}
	public void addRingedHole(double x, double y, double holeDiam, double outerDiam, ColorAttribute ringColor)
	{
		correctLimits(x, y, outerDiam/2, outerDiam/2);
		double xx = adaptX(x); double yy = adaptY(y);
		
		drawTasks.add(() -> {
			
			double th = (outerDiam-holeDiam)/2;
			
			gc.clearRect((xx-holeDiam/2)*SCALE, (yy-holeDiam/2)*SCALE, holeDiam*SCALE, holeDiam*SCALE);
			
			double w = gc.getLineWidth();
			gc.setLineWidth(th*SCALE);
	        gc.setStroke(ringColor.get());
	        gc.setLineCap(StrokeLineCap.BUTT);
	        gc.strokeArc((xx-holeDiam/2-th/2)*SCALE, (yy-holeDiam/2-th/2)*SCALE, (holeDiam+th)*SCALE, (holeDiam+th)*SCALE, 0, 360, ArcType.OPEN);
			gc.setLineWidth(w);
	        
		});
		
		addCircle(x, y, holeDiam);
		addCircle(x, y, outerDiam);
	}
	
	public void addCircle(double x, double y, double outerDiam)
	{
		addCircle(x, y, outerDiam, (ColorAttribute) null);
	}
	public void addCircle(double x, double y, double outerDiam, Color col)
	{
		addCircle(x, y, outerDiam, new ColorAttribute(col));		
	}
	public void addCircle(double x, double y, double outerDiam, ColorAttribute col)
	{
		correctLimits(x, y, outerDiam/2, outerDiam/2);
		double xx = adaptX(x); double yy = adaptY(y);
		
		drawTasks.add(() -> {
			gc.setStroke(Color.BLACK);
			gc.strokeOval((xx-outerDiam/2)*SCALE, (yy-outerDiam/2)*SCALE, outerDiam*SCALE, outerDiam*SCALE);
			if (col != null)
			{
				gc.setFill(col.get());
				gc.fillOval((xx-outerDiam/2)*SCALE, (yy-outerDiam/2)*SCALE, outerDiam*SCALE, outerDiam*SCALE);
			}
		});
	}
	

	// Note that unless you set it to 1, the width will be scaled with all elements!
	public void setLineWidth(double width)
	{
		if (width == 1)
			gc.setLineWidth(width);
		else
			gc.setLineWidth(width*SCALE);		
	}
	
	
	public void addPinRow(double x, double y, PinRow pinRow)
	{
		double xx = adaptX(x); double yy = adaptY(y);
		
		//for (Pin pin: pinRow.getPlacedPins(xx+0.52, yy+0.52)) // offset of 0.52 because pins are smaller than their footprint (2.54x2.54)
		for (Pin pin: pinRow.getPlacedPins(xx, yy)) // offset of 0.52 because pins are smaller than their footprint (2.54x2.54)
			addPin(pin);
		
	}
	
	public void addPin(Pin pin)
	{
		if (pin.x < 0)
			pin.x = baseWidth+pin.x;
		if (pin.y < 0)
			pin.y = baseHeight+pin.y;
		
		if (alignmentOffsetX == null)
		{
			alignmentOffsetX = -(pin.x % gridScale);
			alignmentOffsetY = -(pin.y % gridScale);
		}
		else
		{
			double tX = -(pin.x % gridScale);
			double tY = -(pin.y % gridScale);
			if (Math.abs(tX - alignmentOffsetX) > 0.00001 )
				System.out.println("UNALIGEND PIN! Ind: " + pins.size() +" X difference: " + alignmentOffsetX +  " -> " + tX);
			if (Math.abs(tY - alignmentOffsetY) > 0.00001 )
				System.out.println("UNALIGEND PIN! Ind: " + pins.size() +" Y difference: " + alignmentOffsetY +  " -> " + tY);
			
		}
		
		addRoundrectAbs(pin.x-0.75, pin.y-0.75, pin.x+0.75, pin.y+0.75, 0.75, chipColor);
		//addRectAbs(pin.x-0.25, pin.y-0.25, pin.x+0.25, pin.y+0.25, new ColorAttribute(Color.WHITE));
		addRectAbs(pin.x-0.35, pin.y-0.35, pin.x+0.35, pin.y+0.35, new ColorAttribute(Color.WHITE));
		//addRoundrect(pin.x-0.75, pin.y-0.75, 1.5, 1.5, 0.25, chipColor);
		//addRect(pin.x-0.25, pin.y-0.25, 0.5, 0.5, Color.WHITE);
		
		pin.computeEscapeDir(maxWidth, maxHeight);
		
		pins.add(pin);				
	}
	
	
	
	private double adaptX(double x)
	{
		if (x < 0)
			return(x+baseWidth+0.1);
		return(x+0.1);
	}
	private double adaptY(double y)
	{
		if (y < 0)
			return(y+baseHeight+0.1);
		return(y+0.1);
	}
	
	
	public double getMaxWidth()
	{
		return(maxWidth);
	}
	public double getMaxHeight()
	{
		return(maxHeight);
	}


	
	private GraphicsContext gc;
	// Only set for drawing one time
	public void drawDesign(GraphicsContext gc, VisualizedComponent component)
	{
		this.gc = gc;
		
		for (Runnable r: drawTasks)
		{
			r.run();
		}
		
		for (Pin pin: pins)
			pin.getAssociatedWirePoint(component).updatedPosition();
	}
	
	public double getAlignmentOffsetX()
	{
		return(alignmentOffsetX);
	}
	public double getAlignmentOffsetY()
	{
		return(alignmentOffsetY);		
	}


	public void updateInteractiveElements(GraphicsContext gc, Pane boardPane, VisualizedComponent component)
	{
		for (Pin pin: pins)
			pin.getAssociatedWirePoint(component).updatePositionByBoard(component);
	}
	public void setScalable()
	{
		scaleable = true;
	}
	
	
	public void addAttribute(String name, String description, ComponentAttribute attribute)
	{
		specifiedAttributes.add(new SpecifiedAttribute(name, description, attribute));
	}
	public void addAttributeWrittenOutsidePart(String name, String description, ComponentAttribute attribute, double xOffs, double yOffs, int fontSize, int rotation)
	{
		specifiedAttributes.add(new SpecifiedAttribute(name, description, attribute, xOffs, yOffs, fontSize, rotation));
	}
	public void addAttributeWrittenOnPart(String name, String description, ComponentAttribute attribute, double xOffs, double yOffs, double size, Color col, int rotation)
	{
		specifiedAttributes.add(new SpecifiedAttribute(name, description, attribute));
		
		addText(attribute, xOffs, yOffs, size, col, rotation);
	}
	
	
	// For setting when loading a scheme
	public void setAttributeValue(int index, ComponentAttribute attribute)
	{
		if (index < specifiedAttributes.size()-1)
			specifiedAttributes.get(index).replaceAttribute(attribute);
	}
	
	
	public List<Pin> getPins()
	{
		return(pins);
	}
	
	public void setRotationOffset(int xoffs, int yoffs)
	{
		rotationOffsX90 = xoffs;
		rotationOffsY90 = yoffs;
	}
	public void setRotationOffset(int xoffs90, int yoffs90, int xoffs180, int yoffs180)
	{
		rotationOffsX90 = xoffs90;
		rotationOffsY90 = yoffs90;
		rotationOffsX180 = xoffs180;
		rotationOffsY180 = yoffs180;
	}

	public int getRotationOffsX(int dir)
	{
		switch(dir)
		{
		case 1: return(rotationOffsX90);
		case 2: return(rotationOffsX180);
		case 3: return(-rotationOffsY180 + rotationOffsX90);
		}
		return(0);
	}
	public int getRotationOffsY(int dir)
	{
		switch(dir)
		{
		case 1: return(rotationOffsY90);
		case 2: return(rotationOffsY180);
		case 3: return(rotationOffsX180 + rotationOffsY90);
		}
		return(0);
	}

	public double getWidth()
	{
		return(getMaxWidth()*SCALE);
	}
	public double getHeight()
	{
		return(getMaxHeight()*SCALE);
	}
	
	
	public String getName()
	{
		return(displayName);
	}
	
	public String getComponentName()
	{
		return(componentName);
	}
	
	public String getTooltipText()
	{
		return(tooltip);
	}
	
	public List<SpecifiedAttribute> getSpecifiedAttributes()
	{
		return(specifiedAttributes);
	}
	
	
	@Override
	public String getFunctionalityName()
	{
		return(componentName);
	}




}
