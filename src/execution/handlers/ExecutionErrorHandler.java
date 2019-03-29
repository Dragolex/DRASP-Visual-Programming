package execution.handlers;

import java.util.ArrayList;
import java.util.List;

import dataTypes.FunctionalityContent;
import dataTypes.SimpleTable;
import dataTypes.minor.Pair;
import execution.Execution;
import execution.ExecutionStarter;
import productionGUI.controlers.ButtonsRegionControl;
import productionGUI.sections.elements.VisualizableProgramElement;
import settings.GlobalSettings;
import staticHelpers.GuiMsgHelper;

public class ExecutionErrorHandler {
	
	
	/*
	public static void showError(String string, VisualizableProgrammElement problematicContent)
	{
		// TODO Auto-generated method stub
		
		boolean continuePressed = false;
		while(continuePressed) {} // Wait/sleep until the "continue" button is pressed
		
		problematicContent.applyGUIargumentData(); // Re-apply the arguments in case the user changed them in the meantime				 
	}
	*/
	
	public static boolean showError(String errormessage, Exception exc, FunctionalityContent problematicContent)
	{
		return(showError(errormessage, exc, problematicContent, false, false) == 1);
	}
	public static int showError(String errormessage, Exception exc, FunctionalityContent problematicContent, boolean allowSkip, boolean absoluteError)
	{
		String page = "";
		int codeLine = 0;
		
		Execution.signalizeError();
		
		if (problematicContent.isVisualized())
		{
			page = problematicContent.getVisualization().getControlerOnGUI().getCodePageName();
			codeLine = problematicContent.getVisualization().getControlerOnGUI().getCodeLineIndex();
			
			problematicContent.getVisualization().getControlerOnGUI().markAsErrorcause();
		}
		else
		{
			page = problematicContent.getCodePageName();
			codeLine = problematicContent.getCodeLineIndex();
		}
		
		if (Execution.isRunningInGUI())
			ButtonsRegionControl.getSelf().switchToRunningButtons(true);
		
		
		String errmsg = "Unknown Error\n\n";
		if (Execution.hasError())
		{
			errmsg = Execution.getError()+"\n\n";

			errmsg += "Program page: " + page + "\nLine: " + codeLine + "\n\n";
			
			if(exc != null)
				for(StackTraceElement st: exc.getStackTrace())
					errmsg += st.toString()+"\n";
			
			Execution.setError("", false);
		}
		else
		if(exc != null)
		{		
			//errmsg = "ERROR: " + exc.getClass().getSimpleName() + "\nMessage: " + exc.getLocalizedMessage() +"\n\n";
			
			if (exc instanceof NullPointerException)
				errmsg = "ERROR: Internal nullpointer exception. Is a variable missing or initialized incorectly?\n";
			else
				errmsg = "ERROR: " + exc.getClass().getSimpleName() + " - " + exc.getMessage() + "\n\n";
			
			errmsg += "Program page: " + page + "\nLine: " + codeLine;
			errmsg += "\n";
			
			for(StackTraceElement st: exc.getStackTrace())
				errmsg += st.toString()+"\n";
			
			errmsg += "\n\n";
		}
		
		
		
		Execution.print(GlobalSettings.errorSignal + ExecutionStarter.getProgramIndex(problematicContent));

		if (absoluteError)
		{
			GuiMsgHelper.showError(errormessage, errmsg, "\n\n" + "The execution will be stopped when you close this dialog.");	
			Execution.stop();
			return(-1);
		}
		if (!allowSkip)
			GuiMsgHelper.showError(errormessage, errmsg, "\n\n" + "The execution of the event has been halted.\nAfter closing this dialog you can change parameters\nin the program and press 'CONTINUE' or 'STOP' the program.\n");
		else
			if (GuiMsgHelper.showError(errormessage, errmsg, "\n\n" + "The execution of the event has been halted.\nAfter closing this dialog you can change parameters\nin the program and press 'CONTINUE' or 'STOP' the program.\nAlternatively skip the problematic element.", "Skip Element"))
			{
				if (Execution.isRunningInGUI())
					ButtonsRegionControl.getSelf().switchToRunningButtons(false);
				
				return(Execution.isRunningInGUI() ? 0 : -1);
			}
		
		if (!Execution.isRunningInGUI())
			return(-1);
		
		ButtonsRegionControl.getSelf().currentlyWaitingForButtons();
		
		boolean cont = false, sto = false;
		do
		{
			cont = ButtonsRegionControl.getSelf().hasPressedContinue();
			sto = ButtonsRegionControl.getSelf().hasPressedStop();
		}
		while(!cont && !sto);
		
		ButtonsRegionControl.getSelf().finishedWaitingForButtons();

		
		if (Execution.isRunningInGUI())
			ButtonsRegionControl.getSelf().switchToRunningButtons(false);

		if (cont)
			Execution.resetLastPerformingHadError();
		
		return(cont ? 1 : -1); // Return 1 if user wants to continue; 0 if action skipped and -1 if program stopped
	}
	
	
	
	
	
	public static boolean showMissingArgumentsMessage(List<Pair<VisualizableProgramElement, Integer>> missingParameters)
	{
		SimpleTable<String> variableTable = new SimpleTable<String>();
		
		List<String> pages = new ArrayList<String>();
		List<String> eles = new ArrayList<String>();
		List<String> params = new ArrayList<String>();
		
		for(Pair<VisualizableProgramElement, Integer> dat: missingParameters)
		{
			VisualizableProgramElement ele = dat.first;
			int ind = dat.second;
			
			pages.add(ele.getContent().getCodePageName());
			eles.add(ele.getContent().getCodeLineIndex()+ " - " + ele.getName());
			params.add(ind + " - " + ele.getArgumentDescriptions()[ind]);
		}
		
		variableTable.addColumn("Page", pages);
		variableTable.addColumn("Element", eles);
		variableTable.addColumn("Parameter", params);
		
		String msg = "";
		if (missingParameters.size() == 1)
			msg = "The following parameter has not been set:";
		else
			msg = "The following parameters have not been set:";
		
		int res = GuiMsgHelper.showNonblockingUIWithTable(msg,
				variableTable, new String[] {"Ignore", "Abort"});
		
		return(res == 0);
		
		/*
		
		
		Execution.signalizeError();
		
		//GuiMsgHelper.showMessageNonblockingUI(message);
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		if (!LocationPreparator.isWindows())
			alert.dialogPaneProperty().get().setBorder(new Border(new BorderStroke(Color.BLACK, 
		            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		alert.setTitle("Missing Parameters");
		alert.setHeaderText("Parameters for the following lines are not set:");
		alert.setContentText(linesStr);

		ButtonType buttonTypeOne = new ButtonType("Ignore and Continue");
		ButtonType buttonTypeCancel = new ButtonType("Abort", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);

		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.get() == buttonTypeOne)
			return(true);
		else
			return(false);
			
			*/
	}

	

}
