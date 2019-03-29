package dataTypes.minor;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import staticHelpers.StringHelpers;

public class FieldHighlight
{
	public Color col;
	public PopOver tipPop;
	public VBox tipNode;
	public Background bc;
	
	public FieldHighlight(Color col, String tooltipText)
	{
		this.col = col;
		
		tipPop = new PopOver();
		tipPop.setDetachable(false);
		
		Label txt = new Label(tooltipText);
		tipPop.setContentNode(tipNode = new VBox(txt));
		tipNode.setAlignment(Pos.CENTER);
		
		//tipNode.setStyle(GlobalSettings.tooltipStyle);
		tipNode.setStyle(
		"-fx-font-size: 15; -fx-font-weight: bold; -fx-background-color: rgba(" + StringHelpers.RGBcolorToInnerString(col) +", 0.6); -fx-padding: 7; -fx-margin: 0; -fx-fill: white; -fx-text-fill: white; -fx-border-color: elementBorderColor; -fx-border-width: 0.5; -fx-background-radius: 5; -fx-border-radius: 5;");
		
		txt.getStyleClass().add("elementContentTextLarge");
		
		tipNode.setMouseTransparent(true);			    	        			
		tipPop.setArrowLocation(ArrowLocation.BOTTOM_RIGHT);
		
		tipPop.setAutoHide(false);
		
		tipPop.setAnimated(true);
		
		tipPop.setAutoFix(false);
		
		
		double thickness = 0.04;
		
		Color bCol = col.brighter();
		
		bc = new Background(new BackgroundFill(
	    		new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
		        		new Stop[] {
		        				new Stop(0, Color.TRANSPARENT),
		        				new Stop(thickness, bCol),
		        				new Stop(1.5*thickness, Color.TRANSPARENT),
		        				new Stop(1-1.5*thickness, Color.TRANSPARENT),
	    						new Stop(1-thickness, bCol),
		        				new Stop(1, Color.TRANSPARENT)}
		        		)
	    		, new CornerRadii(5), Insets.EMPTY));

	}

}
