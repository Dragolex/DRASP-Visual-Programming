package dataTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import execution.handlers.InfoErrorHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import productionGUI.sections.subelements.SubElementField;
import staticHelpers.OtherHelpers;

public class DesignedImage
{
	static Map<String, Pane> images = new HashMap<>();
	
	
		public static Pane getOrCreateByName(String type)
	{
		type = type.toLowerCase();
		
		if(images.containsKey(type))
			return(images.get(type));
		
		
		switch(type)
		{
		case "gpiooverview":
				images.put(type, createGpioOverview());
				break;
								
		default: InfoErrorHandler.callBugError("Trying to use the non-existing DesignedImage named '" + type +"'.");
			return(null);
		}
		
		return(getOrCreateByName(type));		
	}
	
	
	private static Pane createGpioOverview()
	{
		/*
		String[] jPins = {
				"3.3V", "5.0V",
				"*GPIO 8*", "5.0V",
				"*GPIO 9*", "Ground",
				"GPIO 7", "*GPIO 15*",
				"Ground", "*GPIO 16*",
				"GPIO 0", "GPIO 1",
				"GPIO 2", "Ground",
				"GPIO 3", "GPIO 4",
				"3.3V", "GPIO 5",
				"*GPIO 12*", "Ground",
				"*GPIO 13*", "GPIO 6",
				"*GPIO 14*", "*GPIO 10*",
				"Ground", "*GPIO 11*",
				"Used", "Used",
				"GPIO 21", "Ground",
				"GPIO 22", "*GPIO 26*",
				"*GPIO 23*", "Ground",
				"*GPIO 24*", "*GPIO 27*",
				"GPIO 25", "*GPIO 28*",
				"Ground", "*GPIO 29*"	
		};
		*/
		
		String[] sPins = {
				"3.3V", "5.0V",
				"*GPIO 2*", "5.0V",
				"*GPIO 3*", "Ground",
				"GPIO 4", "*GPIO 14*",
				"Ground", "*GPIO 15*",
				"GPIO 17", "GPIO 18",
				"GPIO 27", "Ground",
				"GPIO 22", "GPIO 23",
				"3.3V", "GPIO 24",
				"*GPIO 10*", "Ground",
				"*GPIO 9*", "GPIO 25",
				"*GPIO 11*", "*GPIO 8*",
				"Ground", "*GPIO 7*",
				"Used", "Used",
				"GPIO 5", "Ground",
				"GPIO 6", "*GPIO 12*",
				"*GPIO 13*", "Ground",
				"*GPIO 19*", "*GPIO 16*",
				"GPIO 26", "*GPIO 20*",
				"Ground", "*GPIO 21*"	
		};
		
		String[] specialPins = {
				"3.3V VDC", "5.0V VDC",
				"I²C: SDA1", "5.0V VDC",
				"I²C: SCL1", "Ground",
				"GPCLK 0", "Uart: TxD",
				"Ground", "Uart: RxD",
				"GPIO", "SPI 1: CS0",
				"GPIO", "Ground",
				"GPIO", "GPIO",
				"3.3V", "GPIO",
				"SPI 0: MOSI", "Ground",
				"SPI 0: MISO", "GPIO",
				"SPI 0: SCLK", "SPI 0: CS0",
				"Ground", "SPI 0: CS1",
				"BCM 0", "BCM 1",
				"GPIO", "Ground",
				"GPIO", "PWM 0",
				"PWM 1", "Ground",
				"SPI 1: MISO", "SPI 1: CS2",
				"GPIO", "SPI 1: MOSI",
				"Ground", "SPI 1: SCLK"
				
		};
		

		
		
		BorderPane base = new BorderPane();
		
		BorderPane standardGpio = createGpioOverview(sPins, "Standard GPIO");
		//BorderPane javaGpio = createGpioOverview(jPins, "Java GPIO");
		BorderPane specialGpio = createGpioOverview(specialPins, "Special Pins");
		
		base.setLeft(standardGpio);
		//base.setCenter(javaGpio);
		base.setRight(specialGpio);
		
		BorderPane.setAlignment(standardGpio, Pos.CENTER);
		//BorderPane.setAlignment(javaGpio, Pos.CENTER);
		BorderPane.setAlignment(specialGpio, Pos.CENTER);
		
		
		Label top = new Label("GPIO Overview");
		top.setStyle("-fx-font-size: 15; -fx-fill: white; -fx-text-fill: white;");
		

		
		Hyperlink linkA = OtherHelpers.createActivateableHyperLink("http://raspberrypi.ws");
		
		//Hyperlink linkB = OtherHelpers.createActivateableHyperLink("http://pi4j.com/pins/model-3b-rev1.html");
		
		Label lb = new Label("Source: ");
		lb.setStyle("-fx-font-size: 13; -fx-fill: white; -fx-text-fill: white;");
		linkA.setStyle("-fx-font-size: 13; -fx-fill: white; -fx-text-fill: white;");
		//linkB.setStyle("-fx-font-size: 13; -fx-fill: white; -fx-text-fill: white;");
		
		
		Label hint = new Label("Hints:\n- Underlined GPIOs have special purposes and might be already in use.\n- Note that this is the official Pin-Layout. Not the 'Java (Pi4J) Layout'!");
		hint.setStyle("-fx-font-size: 13; -fx-fill: white; -fx-text-fill: white;");
		
		VBox mbottom = new VBox();
		HBox bottom = new HBox();
		VBox tbottom = new VBox();
		mbottom.getChildren().add(hint);
		mbottom.getChildren().add(bottom);
		bottom.getChildren().add(lb);
		bottom.getChildren().add(tbottom);
		tbottom.getChildren().add(linkA);
		//tbottom.getChildren().add(linkB);
		
		//lb.setTranslateY(22);
		linkA.setTranslateY(7);
		lb.setTranslateY(11);
		
		
		mbottom.setStyle("-fx-padding: 16;");
		
		base.setTop(top);
		base.setBottom(mbottom);
		
		BorderPane.setAlignment(top, Pos.CENTER);
		BorderPane.setAlignment(mbottom, Pos.CENTER);
		
		return(base);
	}



	private static BorderPane createGpioOverview(String[] pins, String text)
	{
		List<Label> allEles = new ArrayList<>();
		
		BorderPane pane = new BorderPane();
		
		Label top = new Label(text);
		pane.setTop(top);
		
		pane.setStyle("-fx-padding: 16;");
		
		top.setStyle("-fx-font-size: 13; -fx-fill: white; -fx-text-fill: white; -fx-padding: 0 0 48 0");
		
		VBox left = new VBox();
		VBox center = new VBox();
		VBox right = new VBox();
		
		left.setStyle("-fx-padding: 3;");
		center.setStyle("-fx-padding: 3;");
		right.setStyle("-fx-padding: 3;");
		
		BorderPane.setAlignment(top, Pos.CENTER);
		
		BorderPane.setAlignment(left, Pos.CENTER_RIGHT);
		BorderPane.setAlignment(center, Pos.CENTER);
		BorderPane.setAlignment(right, Pos.CENTER_LEFT);
		
		Image img = new Image("/otherGraphics/roundPin.png");
		Image imgRect = new Image("/otherGraphics/squarePin.png");
		Image raspiFrame = new Image("/otherGraphics/raspiFrame.png");
		ImageView raspiImg = new ImageView(raspiFrame);
		
		left.setSpacing(0);
		center.setSpacing(0);
		right.setSpacing(0);
		
		raspiImg.setManaged(false);
		
		raspiImg.setTranslateX(-7);
		raspiImg.setTranslateY(-44);
		center.getChildren().add(raspiImg);
		
		int baseSize = 12;
		
		for(int i = 0; i < 40;)
		{
			Label lb1 = new Label(pins[i]);
			if (pins[i].startsWith("*"))
			{
				lb1.setText(pins[i].substring(1, pins[i].length()-1));
				lb1.setStyle("-fx-underline: true; -fx-font-size: "+baseSize+"; -fx-fill: rgb(235, 235, 235); -fx-text-fill: rgb(235, 235, 235);");
			}
			else
				lb1.setStyle("-fx-font-size: "+baseSize+"; -fx-fill: white; -fx-text-fill: white;");
			allEles.add(lb1);
			
			lb1.setOnMouseEntered((event) -> enteredPinLabel(lb1, allEles) );
			lb1.setOnMouseExited((event) -> exitedPinLabel(lb1, allEles) );
			lb1.setOnMousePressed((event) -> clickedPinLabel(lb1) );
			
			left.getChildren().add(lb1);
			i++;
			
			Label lb2 = new Label(pins[i]);
			if (pins[i].startsWith("*"))
			{
				lb2.setText(pins[i].substring(1, pins[i].length()-1));
				lb2.setStyle("-fx-underline: true; -fx-font-size: "+baseSize+"; -fx-fill: rgb(235, 235, 235); -fx-text-fill: rgb(235, 235, 235);");
			}
			else
				lb2.setStyle("-fx-font-size: "+baseSize+"; -fx-fill: white; -fx-text-fill: white;");
			
			lb2.setOnMouseEntered((event) -> enteredPinLabel(lb2, allEles) );
			lb2.setOnMouseExited((event) -> exitedPinLabel(lb2, allEles) );
			lb2.setOnMousePressed((event) -> clickedPinLabel(lb2) );
			
			allEles.add(lb2);
			
			right.getChildren().add(lb2);
			i++;
			
			
			ImageView imgl = new ImageView((i == 0) ? imgRect : img);
			ImageView imgr = new ImageView(img);
			Label lb = new Label(String.valueOf( (int)(i/2) ));
			lb.setStyle("-fx-font-size: "+baseSize+"; -fx-fill: white; -fx-text-fill: white;");
			
			BorderPane pn = new BorderPane();
			pn.setLeft(imgl);
			pn.setCenter(lb);
			pn.setRight(imgr);
			BorderPane.setAlignment(imgl, Pos.CENTER_LEFT);
			BorderPane.setAlignment(lb, Pos.CENTER);
			BorderPane.setAlignment(imgr, Pos.CENTER_RIGHT);
			center.getChildren().add(pn);
		}
		
		pane.setLeft(left);
		pane.setCenter(center);
		pane.setRight(right);
		
		return(pane);
	}



	private static void enteredPinLabel(Label label, List<Label> allEles)
	{
		String sub = label.getText();
		int ind = sub.indexOf(" ");
		if (ind > 0)
			sub = sub.substring(0, ind);
		
		boolean spec = label.getStyle().startsWith("-fx-un");
		
		for(Label lb: allEles)
			if ((!lb.getText().startsWith(sub)) || (spec && !lb.getStyle().startsWith("-fx-un")) || (!spec && lb.getStyle().startsWith("-fx-un")) )
				lb.setOpacity(0.45);
	}
	private static void exitedPinLabel(Label label, List<Label> allEles)
	{
		for(Label lb: allEles)
			lb.setOpacity(1);
	}
	
	private static void clickedPinLabel(Label label)
	{
		String str = "GPIO";
		
		String st = "";
		if (label.getText().startsWith(str))
			st = label.getText().substring(str.length()).trim();
		
		if (!st.isEmpty())
		{
			int ind = Integer.valueOf(st);
			
			if (SubElementField.currentlyActive != null)
				SubElementField.currentlyActive.applyString(String.valueOf(ind));
		}
	}

}
