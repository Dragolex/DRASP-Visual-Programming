package functionality.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import dataTypes.FunctionalityContent;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import main.functionality.Functionality;
import main.functionality.helperControlers.screen.JFXwindow;
import productionGUI.guiEffectsSystem.JFXinputTools;
import productionGUI.sections.elements.VisualizableProgramElement;
import settings.ExecutionSettings;
import staticHelpers.GuiMsgHelper;
import staticHelpers.LocationPreparator;
import staticHelpers.OtherHelpers;
import staticHelpers.StringHelpers;

public class Act01_InOutText extends Functionality {

	public static int POSITION = 1;
	public static String NAME = "Input/Output Text";
	public static String IDENTIFIER = "ActInputOutput";
	public static String DESCRIPTION = "Actions for simple console-printing or popup-dialogs.";
	

	

	// Show message
	static public FunctionalityContent create_ElShowMessage()
	{
		
		Object[] showTextMess = new Object[1];
		return( new FunctionalityContent( "ElShowMessage",
				showTextMess,
				() -> {
						if (ExecutionSettings.ignorePopUps)
							InfoErrorHandler.printDirectMessage("POPUP MESSAGE: " + StringHelpers.resolveArgNewline(showTextMess[0].toString()));
						else
							GuiMsgHelper.showMessageNonblockingUI((String) showTextMess[0]);
					}));
		
	}
	static public VisualizableProgramElement visualize_ElShowMessage(FunctionalityContent content)
	{
		VisualizableProgramElement elShowMessage;
		elShowMessage = new VisualizableProgramElement(content, "Show Text Popup", "Shows a text message in a popup.\nThe event will halt until the popup is closed.\nAttention, on Linux this will only work if a JFX window has been opened beforehand!");
		elShowMessage.setArgumentDescription(0, new TextOrVariable(), "Text");
		return(elShowMessage);
	}
	
	
	// Show variable
	static public FunctionalityContent create_ElShowVariable()
	{
		Object[] showNumMessVars = new Object[2];
		return( new FunctionalityContent( "ElShowVariable",
				showNumMessVars,
				() -> {
						if (showNumMessVars[1] == null)
						{
							Execution.setError("The variable has never been set before!", false);
							return;
						}
						
						String tx = (String) ((Variable) showNumMessVars[1]).getConvertedValue(Variable.textType);
						
						if (showNumMessVars[0] != null)
							tx = (String)showNumMessVars[0] + tx;
						
						if (ExecutionSettings.ignorePopUps)
							InfoErrorHandler.printDirectMessage("POPUP MESSAGE: " + tx);
						else
							GuiMsgHelper.showMessageNonblockingUI(tx);
					}));
	}
	static public VisualizableProgramElement visualize_ElShowVariable(FunctionalityContent content)
	{
		VisualizableProgramElement elShowVariable;
		elShowVariable = new VisualizableProgramElement(content, "Show Variable Popup", "Shows a variable (any readable type) together with a given text in a popup.\nThe event will halt until the popup is closed.\\nAttention, on Linux this will only work if a JFX window has been opened beforehand!");
		elShowVariable.setArgumentDescription(0, new TextOrVariable(), "Text");
		elShowVariable.setArgumentDescription(1, new VariableOnly(true, false), "Variable");
		return(elShowVariable);
	}
	
	
	
	
	
	// Write message
	static public FunctionalityContent create_ElWriteMessage()
	{
		
		Object[] showTextMess = new Object[1];
		return( new FunctionalityContent( "ElWriteMessage",
				showTextMess,
				() -> {
						Execution.print((String) showTextMess[0]);
					}));
	}
	static public VisualizableProgramElement visualize_ElWriteMessage(FunctionalityContent content)
	{
		VisualizableProgramElement elShowMessage;
		elShowMessage = new VisualizableProgramElement(content, "Output Text", "Shows a text in the 'console'.\nIf you run the program here in the IDE,\nthe console will be a dedicated window.");
		elShowMessage.setArgumentDescription(0, new TextOrVariable(), "Text");
		return(elShowMessage);
	}
	
	
	// Show variable
	static public FunctionalityContent create_ElWriteVariable()
	{
		Object[] showNumMessVars = new Object[2];
		return( new FunctionalityContent( "ElWriteVariable",
				showNumMessVars,
				() -> {
						if (showNumMessVars[1] == null)
						{	
							Execution.setError("The variable has never been set before!", false);
							return;
						}
						
						if (showNumMessVars[0] == null)
							Execution.print((String) ((Variable) showNumMessVars[1]).getConvertedValue(Variable.textType));
						else
							Execution.print(((String)showNumMessVars[0]) + (String) (((Variable) showNumMessVars[1]).getConvertedValue(Variable.textType)));
					}));
	}
	static public VisualizableProgramElement visualize_ElWriteVariable(FunctionalityContent content)
	{
		VisualizableProgramElement elShowVariable;
		elShowVariable = new VisualizableProgramElement(content, "Output Variable", "Shows a variable (any readable type) together with a given text in the 'console'.\nIf you run the program here in the IDE,\nthe console will be a dedicated window.");
		elShowVariable.setArgumentDescription(0, new TextOrVariable(), "Text");
		elShowVariable.setArgumentDescription(1, new VariableOnly(true, false), "Variable");
		return(elShowVariable);
	}
	
	
	
	
	static public FunctionalityContent create_ElGetInputTx()
	{
		Object[] params = new Object[2];
		return(	new FunctionalityContent( "ElGetInputTx",
				params,
				() -> {
					
						String sample = "";
						Variable var = (Variable) params[1];
						if (var.isType(Variable.textType))
							sample = (String) var.getUnchecked();
						
						String res = GuiMsgHelper.getTextNonblockingUI((String) params[0], sample, false);
						if (res == null)
							res = sample;
						initVariableAndSet(params[1], Variable.textType, res);
						
					}));
	}
	static public VisualizableProgramElement visualize_ElGetInputTx(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Input Text", "Shows a simple window allowing the user to type a text.\nThe provided text will be placed in the given variable 'Output'.\nNote that this function only works on Linux/Raspberry when windows are allowed.\n\nTip: If the 'Output' variable already contains text,\nit will be used as a sample-text in the window.");
		elDelay.setArgumentDescription(0, new TextOrVariable(), "Text");
		elDelay.setArgumentDescription(1, new VariableOnly(true, true), "Output");
		return(elDelay);
	}

	static public FunctionalityContent create_ElGetInputNum()
	{
		Object[] params = new Object[2];
		return(	new FunctionalityContent( "ElGetInputNum",
				params,
				() -> {
					
						double sample = 0;
						Variable var = (Variable) params[1];
						if (var.isType(Variable.doubleType))
							sample = (Double) var.getUnchecked();
						
						Double res = GuiMsgHelper.getNumberNonblockingUI((String) params[0], sample, false);
						if (res == null)
							res = sample;
						initVariableAndSet(params[1], Variable.doubleType, res);
						
					}));
	}
	static public VisualizableProgramElement visualize_ElGetInputNum(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Input Number", "Shows a simple window allowing the user to type a number.\nThe provided number will be placed in the given variable 'Output'.\nNote that this function only works on Linux/Raspberry when windows are allowed.\n\nTip: If the 'Output' variable already contains a number,\nit will be used as a sample-text in the window.");
		elDelay.setArgumentDescription(0, new TextOrVariable(), "Text");
		elDelay.setArgumentDescription(1, new VariableOnly(true, true), "Output");
		return(elDelay);
	}
	
	
	static public FunctionalityContent create_ElGetFilePath()
	{
		Object[] params = new Object[3];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] =	new FunctionalityContent( "ElGetFilePath",
				params,
				() -> {
					
						File initPath;
						String initPathStr = "";
						try
						{
							initPathStr = (String) cont[0].getOptionalArgumentValueOR(2, LocationPreparator.getRunnerFileDirectory()); // get the start path for the dialog
							initPath = new File(initPathStr);
						}
						catch(Exception e)
						{
							Execution.setError("The given start-path is not valid!\nPath: " + initPathStr, false);
							return;
						}
						
						if (!checkParam(params[1], "A window is needed!"))
							return;
						
						
						File output = null;
						
						
						if (cont[0].getOptionalArgTrue(0)) // directories only
						{
							DirectoryChooser dirChooser = new DirectoryChooser();
							dirChooser.setTitle((String) params[2]);
							
							dirChooser.setInitialDirectory(initPath);
							
							while(output == null)
								output = (File) OtherHelpers.perform(new FutureTask<Object>(() -> {return(dirChooser.showDialog(((JFXwindow) params[1]).primaryStage));}));
						}
						else
						{
							FileChooser fileChooser = new FileChooser(); // files dialog
							fileChooser.setTitle((String) params[2]);
							
							String fileTypes = "";
							if(cont[0].hasOptionalArgument(1))
								fileTypes = (String) cont[0].getOptionalArgumentValue(1);
							
							if (fileTypes.contains("\\|"))
							{
								String[] types = fileTypes.split("\\|");
								
								List<ExtensionFilter> extFilter = new ArrayList<ExtensionFilter>();
								
								if (types.length > 1)
								{							
									for(int i = 0; i < types.length; i += 2)
										extFilter.add(new FileChooser.ExtensionFilter(types[i].trim(), "*." + types[+1].trim()));
									
									fileChooser.getExtensionFilters().addAll(extFilter);
								}
							}
							
							fileChooser.setInitialDirectory(initPath);
							
							
							while(output == null)
								output = (File) OtherHelpers.perform(new FutureTask<Object>(() -> {return(fileChooser.showOpenDialog(((JFXwindow) params[1]).primaryStage));}));							
						}
						
						
						initVariableAndSet(params[0], Variable.textType, output.getPath());
						
					}));
	}
	static public VisualizableProgramElement visualize_ElGetFilePath(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Input File", "Shows a simple window allowing the user to chose a path\nto a file or directory.\nFiltering is available through optional arguments.^");
		elDelay.setArgumentDescription(0, new VariableOnly(true, true), "Output");
		elDelay.addParameter(1, new VariableOnly(), "Window Ident", "For technical reason, an existing window is needed.");
		elDelay.addParameter(2, new TextOrVariable(), "Title", "Title at the top of the choser-window.");
		elDelay.addOptionalParameter(0, new BooleanOrVariable("False"), "Directory Only", "If true, only diretories can be chosen.");
		elDelay.addOptionalParameter(1, new TextOrVariable(), "Filetypes", "Shows only files that have given filetypes.\nUse the following pattern: ScreenName|termination.\nYou can also concat multiple ones right after another.\nFor example: Jpg Image|jpg|Png Image|png\nTo allow everything, write anything without the '|' symbol.");
		elDelay.addOptionalParameter(2, new TextOrVariable(), "Start Path", "A directory path where the window\nwill propose chosing a file/subdirectory first.\nIf not set, the directory\nof the DRASP Executor will be used.");
		return(elDelay);
	}

	
	static public FunctionalityContent create_ElGetColor()
	{
		
		Object[] params = new Object[2];
		return( new FunctionalityContent( "ElGetColor",
				params,
				() -> {
					Variable out = (Variable) params[1];
					String outCol = "";
					if (out.isType(Variable.textType))
						outCol = (String) out.getUnchecked();
					
					Color col = Color.BLACK;
					
					if (outCol.length() == 6 || outCol.length() == 8)
						col = OtherHelpers.makeRGBAColorFromRawHexString(outCol);
						
					
					col = JFXinputTools.showColorDialog((String) params[0], col);
					
					outCol = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
					
					System.out.println("OUT COL: " + outCol);
					
					initVariableAndSet(params[1], Variable.textType, outCol);
					
					}));
		
	}
	static public VisualizableProgramElement visualize_ElGetColor(FunctionalityContent content)
	{
		VisualizableProgramElement elShowMessage;
		elShowMessage = new VisualizableProgramElement(content, "Show Color Dialog", "Shows a dialog that enables the user to provide a color.\nThat color is provided as a hex-color string in the given variable.\nFor example AABBCC.");
		elShowMessage.addParameter(0, new TextOrVariable(), "Title", "Text written at the top of the window.");
		elShowMessage.addParameter(1, new VariableOnly(true, true), "Output", "Note that if this variable already holds a color, it will be used as preselection for the color choser.");
		return(elShowMessage);
	}
	
	

	
	

	
}
