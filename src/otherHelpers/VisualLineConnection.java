package otherHelpers;

import javafx.geometry.Bounds;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import productionGUI.sections.elements.ProgramElementOnGUI;
import staticHelpers.OtherHelpers;

public class VisualLineConnection
{
	Line line;
	Polygon arrow;

	public VisualLineConnection(VBox pane, ProgramElementOnGUI origin,
			ProgramElementOnGUI target, String color, boolean visible)
	{
		//Bounds originBounds = origin.getBasePane().getBoundsInParent();
		//Bounds targetBounds = target.getBasePane().getBoundsInParent();
		
		
		Bounds originBounds = origin.getContainer().localToScene(origin.getContainer().getBoundsInLocal());
		Bounds targetBounds = target.getContainer().localToScene(target.getContainer().getBoundsInLocal());
		
		
		double x1, x2, y1, y2;
		

		y1 = originBounds.getMinY()+originBounds.getHeight()/2;
		y2 = targetBounds.getMinY()+targetBounds.getHeight()/2;

		if (targetBounds.getMinX() > originBounds.getMinX())
		{
			x1 = originBounds.getMaxX();
			x2 = targetBounds.getMinX()+7;
		}
		else
		if (targetBounds.getMinX() < originBounds.getMinX())
		{
			x1 = originBounds.getMinX();
			x2 = targetBounds.getMaxX();
		}
		else
		{
			x1 = originBounds.getMaxX();
			x2 = targetBounds.getMaxX();
		}
		
		
		line = new Line(x1, y1, x2, y2);
		arrow = OtherHelpers.createPolygonArrow(x1, y1, x2, y2, 6);
		line.setStrokeWidth(6);
		
		line.setStyle(color);
		arrow.setStyle(color);
		
		line.setStrokeLineCap(StrokeLineCap.ROUND);
		
		
		line.setManaged(false);
		arrow.setManaged(false);
		
		line.setTranslateX(-22);
		line.setTranslateY(-46);
		
		arrow.setTranslateX(-22);
		arrow.setTranslateY(-46);

		
		pane.getChildren().add(line);
		pane.getChildren().add(arrow);
		//MainInfoScreen.getRoot().getChildren().add(0, line);
		
		
		line.setVisible(visible);
		arrow.setVisible(visible);
	}

	public void show()
	{
		line.setVisible(true);
		arrow.setVisible(true);
	}
	
	public void hide()
	{
		line.setVisible(false);
		arrow.setVisible(false);
	}
	
}
