package productionGUI.additionalWindows;

import org.controlsfx.control.PopOver;

import execution.Execution;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import otherHelpers.DragAndDropHelper;
import productionGUI.ProductionGUI;
import settings.GlobalSettings;
import staticHelpers.OtherHelpers;
import staticHelpers.StringHelpers;

public class WaitPopup {
	
	BorderPane content;
	
	PopOver infoPop;
	
	String originalMsg;
	Label topText;
	
	public WaitPopup(String text)
	{
		text = StringHelpers.resolveArgNewline(text);
		originalMsg = text;
		
		Platform.runLater(() -> {
			
			content = new BorderPane();
			content.setStyle(GlobalSettings.tooltipStyle +"\n-fx-padding: 50;");
			
			topText = new Label(originalMsg);
			topText.setStyle("-fx-font-size: 22; -fx-fill: white; -fx-text-fill: white;");
			BorderPane.setAlignment(topText, Pos.CENTER);
			content.setTop(topText);
			
			
			infoPop = new PopOver();
			infoPop.setDetachable(false);
			infoPop.setContentNode(content);
			
			infoPop.setAnimated(true);
			
			infoPop.setArrowSize(0);
			
			infoPop.setAutoHide(false);
			infoPop.setHideOnEscape(false);
			
			infoPop.show(DragAndDropHelper.getTopPane(), ProductionGUI.getStage().getWidth()/2, ProductionGUI.getStage().getHeight()/2);
		
		});

	}
	
	public void showTimerByReplacing(String symbol, int seconds)
	{
		boolean onlyIfRunning = Execution.isRunning();
		new Thread(() -> {
			for(int i = seconds-1; i >= 0; i--)
			{
				int sec = i;
				Platform.runLater(() -> {
					topText.setText( originalMsg.replaceAll(symbol, String.valueOf(sec+1)));
				});
				
				if (onlyIfRunning)
				{
					Execution.checkedSleep(1000);
					if (!Execution.isRunning())
						return;
				}
				else
					OtherHelpers.sleepNonException(1000);
			}		
		}).start();
	}

	public void showTimer(String text, int seconds)
	{
		String subMsg = StringHelpers.resolveArgNewline(text);
		
		boolean onlyIfRunning = Execution.isRunning();
		
		Platform.runLater(() -> {
			Label centerText = new Label(subMsg+ String.valueOf(seconds));
			centerText.setStyle("-fx-font-size: 22; -fx-fill: white; -fx-text-fill: white;");
			BorderPane.setAlignment(centerText, Pos.CENTER);
			content.setCenter(centerText);
			
			new Thread(() -> {
				for(int i = seconds-1; i > 0; i--)
				{
					int sec = i;
					Platform.runLater(() -> {
						centerText.setText(subMsg+ String.valueOf(sec));
					});
					
					if (onlyIfRunning)
					{
						Execution.checkedSleep(1000);
						if (!Execution.isRunning())
							return;
					}
					else
						OtherHelpers.sleepNonException(1000);
				}			
			}).start();
			
		});
	}

	public void close()
	{
		Platform.runLater(() -> {
			infoPop.hide();			
		});
		
	}
	
	/*
	public void toFront()
	{
		content.toFront();
	}
	*/

}
