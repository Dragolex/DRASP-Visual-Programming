package productionGUI.tutorialElements;

import java.util.regex.Pattern;

import dataTypes.DataNode;
import dataTypes.ProgramElement;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import productionGUI.additionalWindows.MainInfoScreen;
import productionGUI.sections.elements.ProgramElementOnGUI;
import productionGUI.sections.elements.VisualizableProgramElement;
import productionGUI.sections.subelements.SubElementField;
import staticHelpers.GuiMsgHelper;

public class TutorialReaderAndMaker extends MainInfoScreen
{
	
	
	private static String tutText = "";
	
	public static void beginOrFinishMakingTutorial()
	{		
		if (!tutText.isEmpty())
		{
			tutText += "\n" + tutConclusion + "\n\n\n" + tutEnd;
			
			final Clipboard clipboard = Clipboard.getSystemClipboard();
	        final ClipboardContent content = new ClipboardContent();
	        content.putString(tutText);
	        clipboard.setContent(content);
	        
	        tutText = "";
			
			return;
		}
		
		String name = GuiMsgHelper.getTextDirect("Type the name of the new tutorial you want to create.", "Name");		
		tutText = tutMark+" " + name + endStr+" 0\n\n";
		
		ElementTask.nameReplacer = "[ELNAME]";
		ParameterTask.nameReplacer = "[ARGNAME]";
		ParameterTask.contentReplacer = "[FIELDCONTENT]";
		ButtonTask.nameReplacer = "[BTNAME]";
		
	}
	
	
	public static void handleElementDrag(DataNode<ProgramElement> root, VisualizableProgramElement newCreated,
			String contentType)
	{
		if (tutText.isEmpty()) return;
		
		tutText += "\n"+tutNewElementTask+" "+newCreated.getFunctionalityName()+endStr;
		tutText += "\n"+tutTaskTarget +" "+root.indexInTree(newCreated)+endStr;
		
		DataNode<ProgramElement> ele = root.getNodeOfInTree(newCreated);

		if (ele.getParent() != root)
			tutText += "\n"+tutTaskParentTarget +" "+root.indexInTree(ele.getParent().getData())+endStr;
		
		
		askForStepText(ElementTask.nameReplacer);
	}
	
	public static void handleElementOpen(VisualizableProgramElement element)
	{
		if (tutText.isEmpty()) return;
		
		tutText += "\n"+tutNewElementTask+" "+element.getFunctionalityName()+endStr;
		askForStepText(ElementTask.nameReplacer);
	}


	public static void handleButtonPress(String buttonText)
	{
		if (tutText.isEmpty()) return;
		
		tutText += "\n"+tutPressButtonTask+" "+buttonText.toUpperCase()+endStr;
		askForStepText(ButtonTask.nameReplacer);
	}
	
	public static void handleParameterFinish(SubElementField field, ProgramElementOnGUI elOnGui)
	{
		if (tutText.isEmpty()) return;
		
		Platform.runLater(() -> {			
		
			for(int i = 0; i < elOnGui.getSubelementsCount(); i++)
			{
				if (elOnGui.getSubelement(i) == field)
					tutText += "\n"+tutNewParamTask+" "+ ((i-1)/2) +endStr;
			}
	
			tutText += "\n"+tutTaskTarget+" "+ elOnGui.getCodeLineIndex() + endStr;
	
			Platform.runLater(() -> {				
				if (GuiMsgHelper.askQuestionDirect("Do you want to enforce this text?") == 1)
					tutText += "\n"+tutTaskParamContent+" "+field.getTextDirect()+endStr;
		
				askForStepText(ParameterTask.nameReplacer);
			});
		
		});
	}
	
	
	private static void askForStepText(String inner)
	{		
		Platform.runLater(() -> 
		{
			String A = GuiMsgHelper.getTextDirect("Main task text.", inner);
			A = A.replaceAll(Pattern.quote("|"), "\n");
			tutText+="\n"+A+"\n";
			
			Platform.runLater(() -> 
			{
				String B = GuiMsgHelper.getTextDirect("Detail text.", inner);
				B = B.replaceAll(Pattern.quote("|"), "\n");
				if (!B.isEmpty() && !B.equals(inner))
					tutText+=Tutorial.textSeparator+"\n"+B+"\n";
			});
		});

	}


	public static boolean isRecording()
	{
		return(!tutText.isEmpty());
	}
	
	
}
