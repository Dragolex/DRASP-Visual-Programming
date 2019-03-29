package productionGUI.controlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import execution.Execution;
import execution.ExecutionStarter;
import execution.Program;
import execution.handlers.InfoErrorHandler;
import main.DataControler;
import productionGUI.ProductionGUI;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;
import staticHelpers.GuiMsgHelper;
import staticHelpers.LocationPreparator;

public class TestControler
{
	private static String testsDirName = "testPrograms";
	private static String testsDir = LocationPreparator.optResDir()+testsDirName+File.separator;
	
	private static List<File> programFiles;
	private static List<File> desiredResultFiles;
	volatile private static int currentTest;
	
	private static boolean showMessage = false;
	
	private static int firstTest = 0;
	private static int lastTest = Integer.MAX_VALUE;
	
	
	static public void startTests(boolean showErrorMessage)
	{
		showMessage = showErrorMessage;
		
		LocationPreparator.extractOptionalResource(testsDirName);
		
		String path = LocationPreparator.getExternalDirectory()+testsDir;
		
		programFiles = new ArrayList<>();
		desiredResultFiles = new ArrayList<>();
		
		FileHelpers.listFiles(path, programFiles, true, GlobalSettings.standardProgramFileTermination, true);
		FileHelpers.listFiles(path, desiredResultFiles, true, GlobalSettings.datFileTermination, true);
		
		
		currentTest = firstTest;
		if (programFiles.isEmpty() || programFiles.size() <= currentTest)
			return;
		lastTest = Math.min(lastTest, programFiles.size()-1);
		
		testFile(programFiles.get(currentTest), desiredResultFiles.get(currentTest));
	}

	private static void testFile(File programFile, File resultFile)
	{
		System.out.println("Starting test number " + currentTest + ". Name: " + resultFile.getName());
		
		
		Program program = DataControler.loadProgramFile(programFile.getPath(), true, false); // Load the test
		
		
		Execution.hookOntoFinish(() -> { // Create runnable for when the to-be-started test execution has finished
			
			try {
				List<String> desiredResult = FileHelpers.readAllLines(resultFile);
				
				List<String> currentResult = Execution.getTrackingResult();
				
				int index = 0;
				for(String line: desiredResult)
				{
					if (!line.equals(currentResult.get(index))) // If a line does not match
						signalizeAndSaveError(index, desiredResult, currentResult, resultFile); // Error!
					
					index++;
				}
				
				
			} catch (Exception e)
			{
				InfoErrorHandler.callEnvironmentError("The following test-result file cannot be loaded: " + resultFile.getAbsolutePath());
			}
			
			
			currentTest++;
			
			if (currentTest > lastTest)
			{
				Execution.setTracked(false);
				return;
			}
			
			testFile(programFiles.get(currentTest), desiredResultFiles.get(currentTest)); // Start next test
		});
		
		ExecutionStarter.startProgram(program, true, false, true); // Execute tracked
	}

	private static void signalizeAndSaveError(int index, List<String> desiredResult, List<String> currentResult, File resultFile)
	{
		String oldFilePath = resultFile.getPath();
		String newFilePath = oldFilePath.substring(0, oldFilePath.length()-GlobalSettings.datFileTermination.length()-1)+"_err_from_" + index + "." + GlobalSettings.datFileTermination;
		
		if (showMessage)
			GuiMsgHelper.showError("Test '" + resultFile.getName() + "' failed from line: " + index,					
					"Desired result: \n" + overviewLines(desiredResult, index, 5),
					"Current result: \n" + overviewLines(currentResult, index, 5)
					);
		
		FileHelpers.writeLineListToFile(newFilePath, currentResult, "Could not save error file to: " + newFilePath);

	}
	
	
	private static String overviewLines(List<String> totalLines, int index, int offset)
	{
		String res = "";
		
		int startIndex = Math.max(0, index-offset);
		
		while(startIndex < index)
			res += totalLines.get(startIndex++)+"\n";

		while(startIndex < Math.min(totalLines.size()-1, index+offset))
			res += totalLines.get(startIndex++)+"\n";
		
		return(res);
	}
	
	
	public static void saveCurrentProgramAsTest()
	{
		String name = GuiMsgHelper.getTextDirect("Please type the name of this test.", ProductionGUI.getCurrentDocumentName());
		if (name == null || name.isEmpty()) return;
		
		if (name.endsWith(GlobalSettings.standardProgramFileTermination))
			name = name.substring(0, name.length()-GlobalSettings.standardProgramFileTermination.length()-1);
		
		String saveFilePath = LocationPreparator.getExternalDirectory()+testsDir+name+"."+GlobalSettings.standardProgramFileTermination;
		String resultFilePath = LocationPreparator.getExternalDirectory()+testsDir+name+"."+GlobalSettings.datFileTermination;
		
		String saveName = name;
		
		Execution.hookOntoFinish(() -> {
			FileHelpers.writeLineListToFile(resultFilePath, Execution.getTrackingResult(), "Could not save the desired-result file to: " + resultFilePath);
			DataControler.saveProgramFile(saveFilePath, true);
			
			System.out.println("Successfully written down the test files. Name: " + saveName);
		});
		
		ExecutionStarter.startLoadedProgram(true, false, true);
	}
	
}
