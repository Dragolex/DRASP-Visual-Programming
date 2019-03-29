package productionGUI.tutorialElements;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import productionGUI.additionalWindows.OverlayMenu;

public abstract class Task
{
	public static final int ElementOpenTaskType = 0;
	public static final int ElementDragTaskType = 1;
	public static final int ParameterTaskType = 2;
	public static final int ButtonTaskType = 3;
	
	
	public static final int tutMainTextSize = 16;
	public static final int tutSubTextSize = 14;
	
	public static final String otherTutStepCol = "rgb(150, 250, 150)";
	
	
	Tutorial parentTutorial;
	String topText = ""; 
	String text = "";
	
	public Task(Tutorial parentTutorial)
	{
		this.parentTutorial = parentTutorial;
		this.topText = parentTutorial.getName() + " - Step " + (parentTutorial.tasks.size()+1);
	}
	
	public void addLine(String addText)
	{
		if (text.isEmpty())
			text = addText;
		else
			text = text+"\n"+addText;
	}
	
	protected abstract boolean start();

	protected abstract int getType();

	protected int onContentTarget = -10;
	
	public void setTargetOnContent(int onContentTarget)
	{
		this.onContentTarget = onContentTarget;
	}

	public String getTopText()
	{
		return(topText);
	}

	public String getMidText()
	{
		int split = text.indexOf(Tutorial.textSeparator);
		
		if (split == -1)
			return(text);
		else
			return(text.substring(0, split));
	}

	public String getBottomText()
	{
		int split = text.indexOf(Tutorial.textSeparator);
		
		if (split == -1)
			return(null);
		else
			return(text.substring(split+4));
	}
	
	public void showStandardWindow(Node node, boolean forceBelow)
	{
		showStandardWindow(node, topText, null, forceBelow);
	}
	
	public void showStandardWindow(Node node, String top, String msg, boolean forceBelow)
	{
		Platform.runLater(() ->
		{
			String mid = null, bottom = null;
			
			if (msg != null)
			{
				int split = msg.indexOf(Tutorial.textSeparator);
				
				if (split == -1)
					mid = msg;
				else
				{
					mid = msg.substring(0, split);
					bottom = msg.substring(split+4);
				}
			}
			else
			{
				finalizeText();
				mid = getMidText();
				bottom = getBottomText();
			}
			
			OverlayMenu message = new OverlayMenu(node,  null, false, forceBelow);
						
			Label topTextLabel = message.addTextDirect(top, false);
			message.addSeparator("blue");
			//message.addCenteredText("TASK:");
			VBox midTextBox = message.addText(mid, false);
			
			VBox bottomTextBox[] = new VBox[1];
			bottomTextBox[0] = null;
			
			if (bottom != null && !bottom.isEmpty())
			{
				message.addSeparator("lime");
				//message.addCenteredText("INFO:");
				bottomTextBox[0] = message.addText(bottom, true);
			}
			message.addSeparator("yellow");
			
			message.addCornerButton("End Tutorial", () -> TutorialControler.endTutorial(true), 0,  "/guiGraphics/if_close.png", "/guiGraphics/if_close_hover.png", true);
			message.addCornerButton(null, () -> {}, 1,  "/guiGraphics/if_directions.png", "/guiGraphics/if_directions_hover.png", true);
			
			message.makeDraggable();
			
			String otherTutStepStyle = "-fx-text-fill: "+ otherTutStepCol +"; -fx-fill: " + otherTutStepCol + ";";
			String baseStyle = topTextLabel.getStyle();
			
			int ind = TutorialControler.currentTut.tasks.indexOf(TutorialControler.currentTask);
			
			Integer[] peekIndex = new Integer[1];
			peekIndex[0] = ind;
			
			message.addCornerButton("Next Step", () -> {
				peekIndex[0]++;
				
				TutorialControler.currentTut.tasks.get(peekIndex[0]).finalizeText();
				
				topTextLabel.setText(TutorialControler.currentTut.tasks.get(peekIndex[0]).getTopText());
				if (peekIndex[0] != ind)
					topTextLabel.setStyle(baseStyle + otherTutStepStyle);
				else
					topTextLabel.setStyle(baseStyle);
				
				message.replaceText(midTextBox, TutorialControler.currentTut.tasks.get(peekIndex[0]).getMidText(), false, (peekIndex[0] != ind) ? otherTutStepStyle : "");
				message.replaceText(bottomTextBox[0], TutorialControler.currentTut.tasks.get(peekIndex[0]).getBottomText(), true, (peekIndex[0] != ind) ? otherTutStepStyle : "");
				
				message.setCornerButtonState(2,	(peekIndex[0] < ind));
				message.setCornerButtonState(3,	(peekIndex[0] > 0));
			}, -2,  "/guiGraphics/if_arrow_right.png", "/guiGraphics/if_arrow_right_hover.png", false);
			
			
			message.addCornerButton("Last Step", () -> {
				peekIndex[0]--;
				
				TutorialControler.currentTut.tasks.get(peekIndex[0]).finalizeText();
				
				topTextLabel.setText(TutorialControler.currentTut.tasks.get(peekIndex[0]).getTopText());
				if (peekIndex[0] != ind)
					topTextLabel.setStyle(baseStyle + otherTutStepStyle);
				else
					topTextLabel.setStyle(baseStyle);
				
				
				message.replaceText(midTextBox, TutorialControler.currentTut.tasks.get(peekIndex[0]).getMidText(), false, (peekIndex[0] != ind) ? otherTutStepStyle : "");
				message.replaceText(bottomTextBox[0], TutorialControler.currentTut.tasks.get(peekIndex[0]).getBottomText(), true, (peekIndex[0] != ind) ? otherTutStepStyle : "");
				
				message.setCornerButtonState(2,	(peekIndex[0] < ind));
				message.setCornerButtonState(3,	(peekIndex[0] > 0));
			}, -1,  "/guiGraphics/if_arrow_left.png", "/guiGraphics/if_arrow_left_hover.png", (ind > 0));
			
			
			
			
			/*
			if (buttons)
			{
				if (ind > 0)
					message.addButton("Peek Last", () -> {});
				if (ind < currentTut.tasks.size()-1)
					message.addButton("Peek Next", () -> {});
			}
			*/
			
			TutorialControler.setWindow(message);
		});
	}
	
	public abstract void finalizeText();
}
