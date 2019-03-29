package productionGUI.sections.elementManagers;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import execution.handlers.InfoErrorHandler;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import main.DataControler;
import main.functionality.FeatureLoader;
import main.functionality.JavaClassCompilerAndLoader;
import otherHelpers.DragAndDropHelper;
import productionGUI.ProductionGUI;
import productionGUI.sections.elements.ProgramElementOnGUI;
import settings.EnvironmentDataHandler;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;
import staticHelpers.GuiMsgHelper;
import staticHelpers.KeyChecker;

@SuppressWarnings("deprecation")
public abstract class StaticAbstractSectionManagerHelper
{
	public static List<FunctionalityContent> markedContents = new ArrayList<>();
	

	public static void removeCurrentMarkedElements()
	{		
		Class<?> source = getCurrentDragDropSourceClass();
		
		AbstractSectionManager sourceSection = null;
		if (source == null)
			sourceSection = ContentsSectionManager.getSelf();
		else
			try
			{
				Method getSelf = source.getMethod("getSelf");
				sourceSection = (AbstractSectionManager) getSelf.invoke(null);
			}
			catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {e.printStackTrace();}
		
		
		for(FunctionalityContent cont: markedContents)
		{
			sourceSection.removeElementFromTree(cont.getVisualization());
		}
	}
		
	
	protected static Class<?> getCurrentDragDropSourceClass()
	{
		try
		{
			return(Class.forName(DragAndDropHelper.getPayloadIdentifier()));
		}
		catch (ClassNotFoundException e)
		{
			//InfoErrorHandler.callBugError("Wrong payload identifier: " + DragAndDropHelper.getPayloadIdentifier());
		}
		
		return(null);
	}
	
	
	
	public static void copyElements()
	{
		markedNodesToClipboard();
	}
	
	public static void cutElements()
	{
		List<DataNode<ProgramElement>> rootNodes = markedNodesToClipboard();
		
		// Remove the nodes
		for(DataNode<ProgramElement> nd: rootNodes)
			nd.getParent().removeChildAlways(nd);
		
		ContentsSectionManager.getSelf().renewElementsRealizationFull();
		ContentsSectionManager.getSelf().renewElementsRealizationFull();
		
		ContentsSectionManager.getSelf().getMarkingRectangleControler().unmark();
	}
	
	public static List<DataNode<ProgramElement>> markedNodesToClipboard()
	{		
		DataNode<ProgramElement> newRoot = new DataNode<ProgramElement>(null);
		
		List<DataNode<ProgramElement>> rootNodes = new ArrayList<>();
		List<DataNode<ProgramElement>> parentNodes = new ArrayList<>();
		
		int baseDepth = -10;
		for(FunctionalityContent cont: markedContents)
		{
			if (baseDepth == -10)
				baseDepth = cont.getVisualization().getNode().getDepth();
			
			if (baseDepth == cont.getVisualization().getNode().getDepth())
			{
				rootNodes.add(cont.getVisualization().getNode());
				parentNodes.add(cont.getVisualization().getNode().getParent());
				
				newRoot.addChild(cont.getVisualization().getNode());
			}
		}
		
		List<String> lines = new ArrayList<>();
		DataControler.writeProgrammElementNodeTreeSection(newRoot, lines, "", true);
		
		
		StringBuilder nodesText = new StringBuilder();
		for(int i = 2; i < lines.size(); i++)
		{
			nodesText.append(lines.get(i));
			nodesText.append("\n");
		}
		
		ClipboardContent content = new ClipboardContent();
		content.putString(nodesText.toString());
		Clipboard.getSystemClipboard().setContent(content);
		

		newRoot.removeAllChildren();
		for(int i = 0; i < parentNodes.size(); i++)
		{
			rootNodes.get(i).setParent(parentNodes.get(i));
		}
		
		
		return(rootNodes);
	}
	
	public static void insertElements()
	{
		String content = (String) Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT);
		
		if (content == null || content.isEmpty())
			return;
		
		List<String> lines = new ArrayList<>();
		String[] linesStr = content.split("\\r?\\n|\\r");
		int depth = linesStr[0].chars().reduce(0, (a, c) -> a + (c == GlobalSettings.indentationSymbol ? 1 : 0));
		
		for(String line: linesStr)
		{
			for(int i = 0; i < depth; i++)
				line = line.replaceFirst("\\t", "");
			
			line.replaceAll("\\r?\\n|\\r", "");
			lines.add("\t" + line);
		}
		
		
		DataNode<ProgramElement> newRoot = new DataNode<ProgramElement>(null);
		
		String page = ContentsSectionManager.getSelf().getCurrentPage();
		DataControler.readProgrammElementNodeTree(lines, newRoot, true, page); // read string lines into node
		
		
		if (markedContents.isEmpty())
		{
			for (DataNode<ProgramElement> nd: newRoot.getChildrenAlways())
			{
				ContentsSectionManager.getSelf().getRootElementNode().addChild(nd); // append to the current nodes
			}
		}
		else
		{
			DataNode<ProgramElement> aboveInsert = markedContents.get(markedContents.size()-1).getVisualization().getNode();
			
			int pos = aboveInsert.getParent().getChildrenAlways().indexOf(aboveInsert)+1;
			for (DataNode<ProgramElement> nd: newRoot.getChildrenAlways())
			{
				aboveInsert.getParent().addChildAt(pos++, nd);
			}
		}
		
		ContentsSectionManager.getSelf().renewElementsRealizationFull();
		ContentsSectionManager.getSelf().renewElementsRealizationFull();
		
		ContentsSectionManager.getSelf().getMarkingRectangleControler().unmark();
	}
	
	
	public static void outcommentElements(ProgramElementOnGUI clickedOne, boolean newState)
	{
		for(FunctionalityContent cont: markedContents)
		{
			if ((clickedOne == null) || (cont != clickedOne.getContent()))
				if (cont.getVisualization().getControlerOnGUI().commentStateChangeAllowed())
				{
					cont.setOutcommented(newState);
					cont.getVisualization().getControlerOnGUI().updateOutcommentedState(true);	
				}	
		}
		
		ContentsSectionManager.getSelf().getMarkingRectangleControler().unmark();
	}
	
	
	public static void openJavaCode(FunctionalityContent firstContent)
	{
		ContentsSectionManager.getSelf().getMarkingRectangleControler().unmark();
		
		//String associatedJavaFile = firstContent.getVisualization().getSurroundingJavaFile();
		String compilableJavaFile = firstContent.getVisualization().getSurroundingJavaFile("");
		String editingJavaFile = firstContent.getVisualization().getSurroundingJavaFile("_editing_");
		String originalJavaFile = firstContent.getVisualization().getSurroundingJavaFile("_original_");
		
		
		// copy to create the editing file
		try {
			FileHelpers.copy(compilableJavaFile, editingJavaFile);
			if (!FileHelpers.fileExists(originalJavaFile)) // if no original file exists yet
				// Create the original file
				FileHelpers.copy(compilableJavaFile, originalJavaFile);
		} catch (IOException e)
		{
			InfoErrorHandler.callEnvironmentError("Handling the JAVA file failed! Does the program have access rights?");
			return;
		}

		
		
		String func = "create_"+firstContent.getFunctionalityName()+"(";
		
		int JavaFileCodeLine = 0;
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(editingJavaFile));
			String line = reader.readLine();
			int lineInd = 0;
			while (line != null) {
				
				if (line.contains(func))
				if (line.trim().startsWith("static public") || line.trim().startsWith("public static"))
				{
					JavaFileCodeLine = lineInd+1;
					break;
				}
				
				lineInd++;
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {}			
		}
		
		InfoErrorHandler.printEnvironmentInfoMessage("Opening JAVA file: " + editingJavaFile + "\nat line: " + JavaFileCodeLine);

		
		if (EnvironmentDataHandler.getFileEditorType().equals("NOTSET") || KeyChecker.isDown(KeyCode.E)) // no runnable yet
		{
			int res = GuiMsgHelper.askQuestion("How do you want to open the Java code for this functionality?\nNote that 'Eclipse' and 'Notepad++' will require you to provide their launch files once.", new String[] {"Eclipse",  "Notepad++", "System Default"}, true);
			if (res < 0)
				return;
			
			if (res == 2)
				EnvironmentDataHandler.setFileEditorRunnable("", "System Default");
			else
			{
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Chose the launcher (e.g. .EXE) for " + (res == 0 ? "ECLIPSE" : "NOTEPAD++") + " to edit .java files");
				
				File javaFileEditorPath = fileChooser.showOpenDialog(ProductionGUI.getStage());
				if (javaFileEditorPath == null)
					return;
				EnvironmentDataHandler.setFileEditorRunnable(javaFileEditorPath.toString(), (res == 0 ? "ECLIPSE" : "NOTEPAD++"));
			}
		}
		
		
		
		ProcessBuilder builder = null;
		
		switch(EnvironmentDataHandler.getFileEditorType())
		{
		case "ECLIPSE":
			builder = new ProcessBuilder(
					EnvironmentDataHandler.getFileEditorRunnable(),
					"--launcher.openFile",
					editingJavaFile+"+"+JavaFileCodeLine);
			handleBuilder(builder, editingJavaFile);
			break;
		case "NOTEPAD++":
			builder = new ProcessBuilder(
					EnvironmentDataHandler.getFileEditorRunnable(),
					editingJavaFile,
					"-n"+JavaFileCodeLine);
				handleBuilder(builder, editingJavaFile);
			break;
		case "System Default":
			try {
				Desktop.getDesktop().open(new File(editingJavaFile)); // just attempt to launch the file with the systems default editor
			} catch (IOException e1) {
				InfoErrorHandler.callEnvironmentError("The attempt to open the file failed!\nError: " + e1.getMessage());
				EnvironmentDataHandler.setFileEditorRunnable("", "NOTSET");
				FileHelpers.fileDeleteChecked(editingJavaFile);
			}
			break;
		}
		
		String className = FeatureLoader.creatingMethods.get(firstContent.getFunctionalityName()).getDeclaringClass().getName();
		
		askForRecompile(firstContent.getFunctionalityName(), compilableJavaFile, editingJavaFile, originalJavaFile, className, firstContent);
	}


	private static void handleBuilder(ProcessBuilder builder, String editingJavaFile)
	{
		if (builder == null)
		{
			InfoErrorHandler.callBugError("Corrupted settings-persistence file!\nIncorrect parameter: " + EnvironmentDataHandler.getFileEditorType());
			return;
		}
		
		builder.redirectErrorStream(true);
		
		try {
			Process process = builder.start();
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        while ((input.readLine()) != null) {}					        
        } catch (IOException e) {
			InfoErrorHandler.callEnvironmentError("The attempt to open the file failed!\nError: " + e.getMessage());
			EnvironmentDataHandler.setFileEditorRunnable("", "NOTSET");
			FileHelpers.fileDeleteChecked(editingJavaFile);
        }
	}


	private static void askForRecompile(String functionalityName, String compilableJavaFile, String editingJavaFile, String originalJavaFile, String className, ProgramElement elementToReplace)
	{
		int ret = GuiMsgHelper.askQuestion("ATTENTION: THIS FUNCTIONALITY IS NOT IMPLEMENTED COMPLITELLY YET (TODO): THE RECOMPILATION IS NOT APPLIED CORRECTLY WITHOUT A RESTART OF DRASP.\n\n\nYou can edit the JAVA code in the file that has just opened now.\n"
				+ "Look for the two functions 'create_" + functionalityName + "' and 'visualize_" + functionalityName + "' and see the documentation for how to alter the functionality.\n\n"
				+ "When you are done, click on 'Recompile' to apply the changes. If compilation fails, you will see an error message"
				+ "and be able to retry or abort to revert to the original code.", new String[] {"Recompile", "Keep Last Successful", "Revert to Original"}, false);
		
		
		boolean retry = true;
		
		while(retry)
		{
			
			if (ret == 0)
			{
				FileHelpers.copyChecked(editingJavaFile, compilableJavaFile);
			}
			if (ret == 1)
			{
				FileHelpers.fileDeleteChecked(editingJavaFile);
			}
			if (ret == 2)
			{
				FileHelpers.fileDeleteChecked(editingJavaFile);
				FileHelpers.copyChecked(originalJavaFile, compilableJavaFile);
			}
			
			try
			{
				JavaClassCompilerAndLoader.recompile(compilableJavaFile);
				Class<?> loadedClass = JavaClassCompilerAndLoader.reloadClass(className);

				/*
				 * TODO!
				 * 
				DataNode<ProgramElement> newParentNode = FeatureLoader.applyVisualizableFeature(loadedClass, compilableJavaFile, null, true, elementToReplace.getContent().getVisualization().getOriginSectionClass());
				
				List<DataNode<ProgramElement>> rootChilds = elementToReplace.getContent().getVisualization().getOriginSection().getRootElementNode().getChildren();
				int ind = rootChilds.indexOf(elementToReplace.getContent().getVisualization().getNode().getParent());
				System.out.println("INDDDD: " + ind);
				rootChilds.set(ind, newParentNode);
				
				elementToReplace.getContent().getVisualization().getOriginSection().renewElementsRealizationFull();
				elementToReplace.getContent().getVisualization().getOriginSection().renewElementsRealizationFull();
				*/
				
				//eatureLoader.applyFeature(loadedClass); // TODO: Find out why this doesn't have the right effect
				
				retry = false;
				
				InfoErrorHandler.printEnvironmentInfoMessage("Recompilation has been succesful!");
			}
			catch(Exception e)
			{
				ret = GuiMsgHelper.askQuestion("Recompilation failed! You can still edit the JAVA code in the file and retry.\n\n---------------\nERROR:\n" + e.getMessage()+"\n---------------\n\n"
					+ "When you are done, click on 'Recompile' to apply the changes. If compilation fails, you will see an error message\n"
					+ "and be able to retry or abort to revert to the original code.", new String[] {"Recompile", "Keep Last Successful", "Revert to Original"}, false);
				retry = true;
			}
			
		}
		
	}
	
}
