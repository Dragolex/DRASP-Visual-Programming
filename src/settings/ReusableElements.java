package settings;

import dataTypes.minor.FieldHighlight;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class ReusableElements {
	
    static public Background markedElementsBackground = new Background(new BackgroundFill(
    		new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
	        		new Stop[] { new Stop(0, new Color(0, 0, 0, 0)), new Stop(0.5, new Color(0, 0, 0.7, 0.7)), new Stop(2, new Color(0, 0, 0.7, 0.7))}
	        		)
    		, new CornerRadii(5), Insets.EMPTY));
    
    
	static public FieldHighlight alteringVariableHighlight = new FieldHighlight(Color.FUCHSIA.darker().darker().darker(), "Element might alter this variable value.");
	
	
}
