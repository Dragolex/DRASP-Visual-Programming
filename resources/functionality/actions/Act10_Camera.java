package functionality.actions;

import dataTypes.FunctionalityContent;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.helperControlers.openCVrelated.ImageInput;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Act10_Camera extends Functionality {

	public static int POSITION = 10;
	public static String NAME = "Camera/Video";
	public static String IDENTIFIER = "ActCameraNode";
	public static String DESCRIPTION = "Actions related to cameras and video input, including IP-Streams.";
	

	
	
	static public FunctionalityContent create_ElOpenCameraStream()
	{
		useOpenCV();
		
		Object[] params = new Object[2];
		return(	new FunctionalityContent( "ElOpenCameraStream",
				params,
				() -> {
					int index = getINTparam(params[1]);
					
					if (index < 0)
					{
						Integer[] camIndices = ImageInput.getExistingCameraIndices();
						if (camIndices.length == 0)
						{
							Execution.setError("No camera found!", false);
							return;
						}
						index = camIndices[0];
					}
					
					ImageInput inp = new ImageInput(index);
					
					initVariableAndSet(params[0], Variable.streamType, inp);
					}));
	}
	static public VisualizableProgramElement visualize_ElOpenCameraStream(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Stream Camera", "Creates a new 'Stream' from a given camera.\nIt can be used to save the camera feed to a file or to display it in a 'Window'.");
		elDelay.addParameter(0, new VariableOnly(true, true), "Stream Identifier", "Identifier variable for this video stream.");
		elDelay.addParameter(1, new ValueOrVariable("-1"), "Index", "Small number defining the camera.\nUsually 0 is the first camera.\nIf you attach only one camera (like the standard raspberry cam),\nyou can use the value -1 to chose the first available camera.");
		
		return(elDelay);
	}
	
	
	static public FunctionalityContent create_ElOpenVideoStream()
	{
		useOpenCV();
		
		Object[] params = new Object[2];
		return(	new FunctionalityContent( "ElOpenVideoStream",
				params,
				() -> {
					
					String file = getSTRINGparam(params[1]);
					ImageInput inp = new ImageInput(null);
					inp.startVideoFromFile(file);
					
					initVariableAndSet(params[0], Variable.streamType, inp);
					}));
	}
	static public VisualizableProgramElement visualize_ElOpenVideoStream(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Stream Video File", "Creates a new 'Stream' from a given file.\nIt can be used to save it entirely or partially to a file or to display it in a 'Window'.");
		elDelay.addParameter(0, new VariableOnly(true, true), "Stream Identifier", "Identifier variable for this video stream.");
		elDelay.addParameter(1, new TextOrVariable(), "Video File Path", "Path to the video file you want to open.\nSeveral standard file formats are supported.");		
		return(elDelay);
	}
	
	
	static public FunctionalityContent create_ElOpenIPStream()
	{
		useOpenCV();
		
		Object[] params = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElOpenIPStream",
				params,
				() -> {
					
					String url = getSTRINGparam(params[1]);
					ImageInput inp = new ImageInput(null);
					
					if (cont[0].hasOptionalArgument(1))
						inp.openIPcamera(url,
								getSTRINGparam(cont[0].getOptionalArgumentValue(0)),
								getSTRINGparam(cont[0].getOptionalArgumentValue(1)));
					else
						inp.openIPcamera(url);
					
					initVariableAndSet(params[0], Variable.streamType, inp);
					}));
	}
	static public VisualizableProgramElement visualize_ElOpenIPStream(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Stream IP Cam", "Creates a video stream to an IP camera using a link/URL.\nIt can be used to save it entirely or partially to a file or to display it in a 'Window'.");
		elDelay.addParameter(0, new VariableOnly(true, true), "Stream Identifier", "Identifier variable for this video stream.");
		elDelay.addParameter(1, new TextOrVariable(), "Camera URL", "URL to the target camera.");
		elDelay.addOptionalParameter(0, new TextOrVariable(), "Username", "Username for the optional login verification.");
		elDelay.addOptionalParameter(1, new TextOrVariable(), "Password", "Password for the optional login verification.");
		return(elDelay);
	}
	
	
	static public FunctionalityContent create_ElStreamToFile()
	{
		useOpenCV();
		
		Object[] params = new Object[2];
		return(	new FunctionalityContent( "ElStreamToFile",
				params,
				() -> {
					
					ImageInput inp = (ImageInput) params[0];
					String file = getSTRINGparam(params[1]);
					
					inp.saveVideoToFile(file);					
					}));
	}
	static public VisualizableProgramElement visualize_ElStreamToFile(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Stream into File", "Directs a stream into a video file to save it.");
		elDelay.addParameter(0, new VariableOnly(false, false), "Stream Identifier", "Identifier variable with the already opened\nvideo stream to save in the file.");
		elDelay.addParameter(1, new TextOrVariable(), "Video File Path", "Path to the video file you want to save into.\nSeveral standard file formats are supported.\nIf the file exists, it will be overwritten.");
		return(elDelay);
	}
	
	
	static public FunctionalityContent create_ElCloseStream()
	{
		useOpenCV();
		
		Object[] params = new Object[1];
		return(	new FunctionalityContent( "ElCloseStream",
				params,
				() -> {
					
					ImageInput inp = (ImageInput) params[0];
					inp.stopStream();
					}));
	}
	static public VisualizableProgramElement visualize_ElCloseStream(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Stop Stream", "Stops and closes all kinds of streams and related tasks.");
		elDelay.addParameter(0, new VariableOnly(false, false), "Stream Identifier", "Identifier variable with the already opened\nvideo stream to stop.");
		return(elDelay);
	}
	
	
	
}
