package main.functionality.helperControlers.screen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.FutureTask;

import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import main.functionality.helperControlers.spline.DataSpline;
import staticHelpers.OtherHelpers;

public class JFXgraph extends JFXitem
{
	DataSpline spline;
	
	Label label;
	Color color;
	GraphicsContext canv;
	Canvas coordinateCanvas;
	WritableImage coordinateImage = null;
	SnapshotParameters params;
	
	int width, height;
	
	private int cropMin = -1;
	private int cropMax = Integer.MAX_VALUE;
	
	double xScale, yScale;
	int minx, maxx, ybase;
	boolean editable = false;
	
	List<Integer> specialPositions = new ArrayList<>();
	List<String> specialPositionsDescr = new ArrayList<>();
	
	int minSpecialPointRadius = 3;
	int minSpecialPointDistance = 10;
	
	public JFXgraph(DataSpline spline, String color, int width, int height, boolean editable)
	{
		this.spline = spline;
		
		this.width = width;
		this.height = height;
		
		this.editable = editable;
		
		JFXgraph graph = this;
		
		OtherHelpers.perform(new FutureTask<Object>(() ->
		{
			this.color = OtherHelpers.makeRGBAColorFromRawHexString(color);
			
			element.prefWidth(width);
			element.prefHeight(height);
			
			Canvas canvas = new Canvas(width, height);
			
			canv = canvas.getGraphicsContext2D();
			
			update();
			
			element.getChildren().add(canvas);
			
			params = new SnapshotParameters();
			params.setFill(Color.TRANSPARENT);
			
			if (editable)
			{
				canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
				    public void handle(MouseEvent m)
				    {
						int tolerance = (int) Math.round(minSpecialPointDistance/xScale);
				    	spline.remove((int) Math.round(m.getX()/xScale), tolerance);
				    	spline.setValue((int) Math.round(m.getX()/xScale), (ybase-m.getY())/yScale);

				    	addSpecialPoint((int) Math.round(m.getX()), "");
				    }
				});
			}
			
			
			spline.associateToGraph(graph);
			
			redraw();
			
			return(null);
			
		}));
	}
	
	public void addSpecialPoint(int xPos, String descr)
	{
		Iterator<Integer> specPos = specialPositions.iterator();	
		Iterator<String> specPosDescr = specialPositionsDescr.iterator();	

		int tolerance = (int) Math.round(minSpecialPointDistance/xScale);		
		xPos = (int) Math.round(((int) Math.round(xPos/xScale))*xScale);
		
		
		while(specPos.hasNext())
		{
			int pointPos = specPos.next();
			specPosDescr.next();
		    
		    if (Math.abs(pointPos-xPos) < tolerance)
		    {
		    	specPos.remove();
		    	specPosDescr.remove();
		    }
		}
		
		specialPositions.add(xPos);
		specialPositionsDescr.add(descr);		
		
		redraw();
	}
	
	public void setCoordinatesData(int minCropX, int maxCropX, int minValX, int maxValX, int minValY, int maxValY)
	{
		OtherHelpers.perform(new FutureTask<Object>(() ->
		{

			if (coordinateCanvas == null)
			{
				coordinateCanvas = new Canvas(width, height);
				coordinateCanvas.widthProperty().bind(canv.getCanvas().widthProperty());
				coordinateCanvas.heightProperty().bind(canv.getCanvas().heightProperty());
			}
			
			cropMin = minCropX;
			cropMax = maxCropX;
			if (maxCropX < 0)
				cropMax = Integer.MAX_VALUE;
			
			calcLimitsAndScales();
			
			GraphicsContext bc = coordinateCanvas.getGraphicsContext2D();
			
			drawArrow(bc, Color.BLACK, 0, (int) ybase, width, (int) ybase);
			drawArrow(bc, Color.BLACK, 0, (int) height, 0, 0);			
			
			
			coordinateImage = coordinateCanvas.snapshot(params, coordinateImage);
			
			update();
			
			return(null);
		}));
		
	}
	
	@Override
	protected void prepare()
	{
		
	}

	@Override
	protected void update()
	{
		redraw();
	}
	
	
	public void redraw()
	{
		if (canv == null) return;
		canv.clearRect(0, 0, canv.getCanvas().getWidth(), canv.getCanvas().getHeight());
		
		if (coordinateImage != null)
			canv.drawImage(coordinateImage, 0, 0);
		
		
		calcLimitsAndScales();
		

		canv.setStroke(color);
		canv.setLineWidth(1);

		
		if (spline.isInterpolated())
		{
			float[] data = spline.getData();
			float pos = (float) xScale;
			
			if (data.length > 1)
			{
				if (minx < 10000)
				for(int i = minx+1; i < maxx; i++)
				{
					canv.strokeLine(pos-xScale, ybase-data[i-1]*yScale, pos, ybase-data[i]*yScale);
					pos += xScale;
				}
			}
			
			int ind = 0;
			for(Integer pointPos: specialPositions)
			{
				int pointInd = (int) Math.round(pointPos/xScale);
				canv.fillOval(pointPos-minSpecialPointRadius,
						ybase-data[pointInd]*yScale-minSpecialPointRadius,
						2*minSpecialPointRadius,
						2*minSpecialPointRadius);
				
				if ((specialPositionsDescr.get(ind)) != null)
					canv.fillText(pointPos + " : " + data[pointInd] +"\n" + specialPositionsDescr.get(ind), pointPos+minSpecialPointRadius*2, ybase-data[pointInd]*yScale);
			}
		}
		else
		{
			int len = spline.xValues.size();
			
			for(int i = 0; i < len; i++)
			{
				float xx = spline.xValues.get(i);
				float yy = spline.yValues.get(i);

				canv.fillOval((xx*xScale)-xScale, ybase-yy*yScale, 5, 5);				
			}
		}
			
	
		

	}

	
	private void calcLimitsAndScales()
	{
		
		if (cropMin < 0)
			minx = (int) spline.getMinX();
		else
			minx = Math.max(0, cropMin);
		
		
		//maxx = (int) Math.min(spline.getMaxX(), cropMax); // TODO USE THIS!
		maxx = (int) spline.getMaxX();
		
		xScale = ((double)width)/Math.abs(maxx-minx);
		yScale = ((double)height)/Math.abs(spline.getMaxY()-spline.getMinY());
		
		ybase = (int) Math.round(height + (spline.getMinY() * yScale) );
	}
	
	
	private void drawArrow(GraphicsContext gc, Color col, int x1, int y1, int x2, int y2)
	{
		int ARR_SIZE = 8;
		
	    gc.setFill(col);
	    
	    double dx = x2 - x1, dy = y2 - y1;
	    double angle = Math.atan2(dy, dx);
	    int len = (int) Math.sqrt(dx * dx + dy * dy);

	    Transform transform = Transform.translate(x1, y1);
	    transform = transform.createConcatenation(Transform.rotate(Math.toDegrees(angle), 0, 0));
	    gc.setTransform(new Affine(transform));

	    gc.strokeLine(0, 0, len, 0);
	    gc.fillPolygon(new double[]{len, len - ARR_SIZE, len - ARR_SIZE, len}, new double[]{0, -ARR_SIZE, ARR_SIZE, 0}, 4);
	}
	
	
	@Override
	public String toStringSimple()
	{
		return("Graph"); // TODO: Add some info about the graph
	}
	@Override
	public String toString()
	{
		return(toStringSimple() + " - X: " + getPositionX() + " Y: " + getPositionY() + " W: " + getWidth() + " H: " + getHeight());
	}

}
