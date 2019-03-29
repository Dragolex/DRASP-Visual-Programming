package execution.handlers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;

import execution.Execution;
import settings.GlobalSettings;

public class InfoErrorHandler
{
	
	public static void printDirectMessage(String message)
	{
		Execution.print(message);
	}
	
	public static void printEnvironmentInfoMessage(String message)
	{
		Execution.print(GlobalSettings.envSymbol + "ENVIRONMENT INFO: " + message);
	}
	
	public static void printExecutionInfoMessage(String message)
	{
		Execution.print(GlobalSettings.execInfoSymbol + "EXECUTION INFO: " + message);
	}
	
	public static void printMinorExecutionErrorMessage(String message)
	{
		Execution.print(GlobalSettings.minorErrorSymbol + "EXECUTION ERROR: " + message);
	}
	
	public static void printExecutionErrorMessage(String message)
	{
		Execution.print(GlobalSettings.errorSymbol + "EXECUTION ERROR: " + message);
	}
	

	
	// Errors
	
	public static void callGUIerror(String error)
	{
		System.err.println(GlobalSettings.errorSymbol + "------\nGUI ERROR: " + error + "\n------");
	}
	
	
	public static void callPrecompilingError(String error)
	{
		System.err.println(GlobalSettings.errorSymbol + "------\nPrecompilation ERROR: " + error + "\n------");
	}
	
	/*
	public static void callExecutionError(String error)
	{
		System.err.println("Execution ERROR: " + error);
	}
	*/

	public static void callEnvironmentError(String error)
	{
		System.err.println(GlobalSettings.errorSymbol +"------\nEnvironment ERROR: " + error + "\n------");
	}

	public static void callFileLoadError(String error)
	{
		System.err.println(GlobalSettings.errorSymbol +"------\nFile load ERROR: " + error + "\n------");
	}

	
	public static void callBugError(String error)
	{
		StringWriter sw = new StringWriter();
		new Throwable("").printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();
		
		System.err.println(GlobalSettings.errorSymbol +"------\nInternal bug ERROR. Please be so kind and notify the developer of this software about the circumstances\nthis error has occured and transmit the following message which has also been copied to your clipboard: " + error + "\n\nSTACK TRACE: \n" + stackTrace + "\n------");
		
		StringSelection stringSelection = new StringSelection(error);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}


	/*
	public static void showProblemMessage(String message)
	{
		OtherHelpers.perform(new FutureTask<Object>(() -> {			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("");
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
			return(true);
		}));
	}
	*/
	
	
}
