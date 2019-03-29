package main.functionality.helperControlers.screen;

import java.util.concurrent.FutureTask;

import execution.Execution;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import staticHelpers.OtherHelpers;

public class JFXshape extends JFXitem
{
	Shape shape;

	public JFXshape(int type, double x2_w_dir_r, double y2_h_len, String line_col,
			String inner_col, double thickness, Double roundness)
	{
		OtherHelpers.perform(new FutureTask<Object>(() -> {

			shape = null;
			
			double x1 = 0;
			double y1 = 0;
			
			//"Line by X/Y", "Line by Direction", "Rectangle by Width/Height", "Ellipse by Box", "Circle by Radius"
			switch(type)
			{
			case 0: // "Line by x, y"
			case 1: // "arrow by x, y"
				Line line = new Line();
				line.setStartX(x1);
				line.setStartY(y1);
				line.setEndX(x2_w_dir_r);
				line.setEndY(y2_h_len);
				shape = line;
			break;
			case 2: // "Line by dir"
				line = new Line();
				line.setStartX(x1);
				line.setStartY(y1);
				line.setEndX(x1+OtherHelpers.lengthdirX(x2_w_dir_r, y2_h_len));
				line.setEndY(y1+OtherHelpers.lengthdirY(x2_w_dir_r, y2_h_len));
				shape = line;
			break;
			case 3: // "Rectangle by size"
				Rectangle rect = new Rectangle();
				rect.setX(x1);
				rect.setY(y1);
				rect.setWidth(x2_w_dir_r);
				rect.setHeight(y2_h_len);
				if (roundness != null)
				{
					rect.setArcWidth(roundness);
					rect.setArcHeight(roundness);
				}		
				shape = rect;
			break;
			case 4: // "Ellipse by box"
				Ellipse ellipse = new Ellipse();
				ellipse.setCenterX(x1+(x2_w_dir_r-x1)/2);
				ellipse.setCenterY(y1+(y2_h_len-y1)/2);
				ellipse.setRadiusX((x2_w_dir_r-x1)/2);
				ellipse.setRadiusY((y2_h_len-y1)/2);
				shape = ellipse;
			break;
			case 5: // "Circle by radius"
				Circle circle = new Circle();
				circle.setCenterX(x1);
				circle.setCenterY(y1);
				circle.setRadius(x2_w_dir_r);
				shape = circle;
			break;
			case 6: // "Arc/Pie"
				Arc arc = new Arc();
				arc.setCenterX(x1);
				arc.setCenterY(y1);
				arc.setRadiusX(x2_w_dir_r);
				arc.setRadiusY(x2_w_dir_r);
				arc.setStartAngle(y2_h_len);
				if (roundness != null)
					arc.setLength(roundness);
				else
					Execution.setError("Note that you need to set the optional parameter 'Roundness/Arc-Len' to draw an arc or pie!", false);
				shape = arc;
			break;

			}
			
			shape.setStrokeWidth(thickness);
			shape.setStroke(OtherHelpers.makeRGBAColorFromRawHexString(line_col));
			shape.setFill(OtherHelpers.makeRGBAColorFromRawHexString(inner_col));
			
			element.getChildren().add(shape);
			
			if (type == 1) // arrow
			{
				double pdir = 0;
				
				Line lineA = new Line();
				lineA.setStartX(x2_w_dir_r);
				lineA.setStartY(y2_h_len);				
				lineA.setEndX(x2_w_dir_r+OtherHelpers.lengthdirX(pdir+135, 5));
				lineA.setEndY(y2_h_len+OtherHelpers.lengthdirY(pdir+135, 5));
				
				lineA.setStrokeWidth(thickness);
				lineA.setStroke(OtherHelpers.makeRGBAColorFromRawHexString(line_col));
				
				element.getChildren().add(lineA);
				
				
				Line lineB = new Line();
				lineB.setStartX(x2_w_dir_r);
				lineB.setStartY(y2_h_len);				
				lineB.setEndX(x2_w_dir_r+OtherHelpers.lengthdirX(pdir-135, 5));
				lineB.setEndY(y2_h_len+OtherHelpers.lengthdirY(pdir-135, 5));

				lineB.setStrokeWidth(thickness);
				lineB.setStroke(OtherHelpers.makeRGBAColorFromRawHexString(line_col));
				
				element.getChildren().add(lineB);
			}
			
			
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
		
	}
	
	
	@Override
	public String toStringSimple()
	{
		if (shape == null)
			return("Shape");
		else
			return("Shape: " + shape.getClass().getSimpleName());
	}
	@Override
	public String toString()
	{
		return(toStringSimple() + " - X: " + getPositionX() + " Y: " + getPositionY() + " W: " + getWidth() + " H: " + getHeight());
	}

}
