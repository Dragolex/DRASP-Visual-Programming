package staticHelpers;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import settings.GlobalSettings;

public class InlineFeatures
{
	
	final public static String greenRect = "[GREENRECT]";
	final public static String yellowRect = "[YELLOWRECT]";
	final public static String redRect = "[REDRECT]";
	
	final static String innerImg = "[INNERIMG";
	
	
	public static Node insertSpecialInline(String line, String textClass, int height)
	{
		return(insertSpecialInline(line, textClass, height, ""));
	}
	
	public static Node insertSpecialInline(String line, String textClass, int height, String textStyle)
	{
		String upperLine = line.toUpperCase();
		
		int pos1 = upperLine.indexOf(greenRect);
		if (pos1 >= 0)
			return(insertSpecialInline(line, greenRect, pos1, textClass, height, textStyle));
		
		int pos2 = upperLine.indexOf(yellowRect);
		if (pos2 >= 0)
			return(insertSpecialInline(line, yellowRect, pos2, textClass, height, textStyle));
		
		int pos3 = upperLine.indexOf(redRect);		
		if (pos3 >= 0)
			return(insertSpecialInline(line, redRect, pos3, textClass, height, textStyle));

		int pos4 = upperLine.indexOf(innerImg);		
		if (pos4 >= 0)
			return(insertSpecialInline(line, innerImg, pos4, textClass, height, textStyle));

		
		Text tx = new Text(line);
		tx.getStyleClass().add(textClass);
		
		if (!textStyle.isEmpty())
			tx.setStyle(textStyle);
		
		return(new HBox(tx));
	}

	public static Node insertSpecialInline(String line, String type, int pos, String textClass, int height)
	{
		return(insertSpecialInline(line, type, pos, textClass, height, ""));
	}
	
	public static Node insertSpecialInline(String line, String type, int pos, String textClass, int height, String textStyle)
	{
		Node element = null;
		
		Rectangle rect = new Rectangle(0,0, 60, height);
		rect.setOpacity(GlobalSettings.tutorialAttentionAlpha*1.5);
		rect.setArcWidth(10);
		rect.setArcHeight(10);
		element = rect;
		
		int startPos = pos;
		
		HBox box = new HBox();
		
		switch(type)
		{
		case greenRect:
			rect.setFill(Color.LIME);
			break;
		case yellowRect:
			rect.setFill(Color.YELLOW);
			break;
		case redRect:
			rect.setFill(Color.RED.brighter());
			break;
		case innerImg:
			int endPos = line.indexOf("]");
			String file = line.substring(pos+type.length(), endPos).trim();
			pos = endPos-type.length()+1;
			if (LocationPreparator.usesSlashForPaths())
				file = FileHelpers.adaptPathForLinux(file);
			ImageView img = new ImageView(new Image(file));
			img.setFitHeight(height*2);
			img.setPreserveRatio(true);
			img.setTranslateY(-height/2.5);
			box.setStyle("-fx-padding: 10 0 -5 0;");
			element = img;
			break;
			
		}
		
		Text starterText = new Text(line.substring(0, startPos));
		starterText.getStyleClass().add(textClass); // Normal line
		if (!textStyle.isEmpty())
			starterText.setStyle(textStyle);

		Text endText = new Text(line.substring(pos+type.length()));
		endText.getStyleClass().add(textClass); // Normal line
		if (!textStyle.isEmpty())
			endText.setStyle(textStyle);
		
		
		
		box.getChildren().add(starterText);		
		box.getChildren().add(element);		
		box.getChildren().add(endText);
		
		return(box);
	}

	
	
	
	
}
