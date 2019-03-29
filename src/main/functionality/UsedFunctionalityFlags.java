package main.functionality;

import java.util.ArrayList;
import java.util.List;

import execution.Execution;
import productionGUI.additionalWindows.WaitPopup;
import settings.HaveDoneFileHandler;
import staticHelpers.FileHelpers;
import staticHelpers.LocationPreparator;

public class UsedFunctionalityFlags extends SharedComponents
{
	static private boolean usingHardware = false;
	static private boolean usingOpenCV = false;
	static private boolean usingExternalJFX = false;
	static private boolean usingOtherExternalExecutables = false;

	// The functions in this class help keeping track of what functionality is actually needed for the program.
	// That information is used to load and extract resources only when needed
	
	protected static void useHardware()
	{
		if (!currentlyLoadingIDEelements)
			usingHardware = true;
	}/*
	static boolean isUsingHardware()
	{
		return(usingHardware);
	}*/

	protected static void useExternalJFX()
	{
		if (!currentlyLoadingIDEelements)
			usingExternalJFX = true;
	}/*
	static boolean isUsingExternalJFX()
	{
		return(usingExternalJFX);
	}*/
	
	protected static void useOpenCV()
	{
		if (!currentlyLoadingIDEelements)
			usingOpenCV = true;
	}/*
	static boolean isUsingOpenCV()
	{
		return(usingOpenCV);
	}*/
	
	protected static void useOtherExternalExecutables()
	{
		if (!currentlyLoadingIDEelements)
			usingOtherExternalExecutables = true;
	}
	
	
	public static void letExtractRequiredResources()
	{
		if (!LocationPreparator.isCompiled())
			return; // skip if not running as jar
		
		List<String> resourcesToLoad = new ArrayList<>();
		
		if (usingExternalJFX && !LocationPreparator.isWindows() ) // TODO add a check whether JFX actually works already
		{
			resourcesToLoad.add("integratedJFX");
		}
		if (usingOtherExternalExecutables)
		{
			if (LocationPreparator.isWindows())
				resourcesToLoad.add("winDLL");
			else
				resourcesToLoad.add("RPIcpp");
		}
		if (usingOpenCV)
		{
			if (LocationPreparator.isWindows())
				resourcesToLoad.add(FileHelpers.addSubfile("OpenCV", "win"));
			else
				resourcesToLoad.add(FileHelpers.addSubfile("OpenCV", "rpi"));
		}
		
		
		
		if (!resourcesToLoad.isEmpty())
		{
			String msg = "EXTRACTING ADDITIONAL RESOURCES.\nThis can take up to a few minutes.\nOnly needed on the first launch in a new directory.";
			
			WaitPopup waitPopup = null;
			
			if (Execution.isRunningInGUI())
				waitPopup = new WaitPopup(msg);
			
			Execution.print(msg);
			
			
			for(String res: resourcesToLoad)
				if (!HaveDoneFileHandler.haveDone(res, false)) // if not already extracted
					LocationPreparator.extractOptionalResource(res); // extracting the resources
			
			for(String res: resourcesToLoad)
				if (!HaveDoneFileHandler.haveDone(res, false))
					HaveDoneFileHandler.haveDone(res, true); // mark that resource has been extracted
			
			
			if (waitPopup != null)
				waitPopup.close();
			
			Execution.print("FINISHED EXTRACTING.");
		}
		
	}
	
	
	
}
