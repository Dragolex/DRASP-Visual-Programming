package functionality.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import dataTypes.FunctionalityConditionContent;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TextOrValueOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.exceptions.AccessUnsetVariableException;
import dataTypes.specialContentValues.DataList;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.handlers.VariableHandler;
import main.functionality.Actions;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;

public class Act12_Files extends Functionality {

	public static int POSITION = 12;
	public static String NAME = "Files";
	public static String IDENTIFIER = "ActFilesNode";
	public static String DESCRIPTION = "Functionalities involving files and directories.";
	
	
	public static ProgramElement create_ElFileLoadDirContents()
	{
		Object[] input = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElFileLoadDirContents",
				input,
				() -> {
					}
				,
				() -> {					
						String folder = (String) input[1];
						
						folder = FileHelpers.convertIfExternal(folder);
						folder = FileHelpers.resolveUniversalFilePath(folder);
						
						List<File> files = new ArrayList<File>();
						
						if (!FileHelpers.directoryExists(folder))
						{
							Execution.setError("The directory path does not exist!\nGiven path: " + folder, false);
							return;
						}
						
						if (content[0].hasOptionalArgument(1))
							FileHelpers.listFiles(folder, files, (boolean) input[2], (String) content[0].getArgumentValue(0), content[0].getOptionalArgTrue(0));
						else
							FileHelpers.listFiles(folder, files, (boolean) input[2], content[0].getOptionalArgTrue(0));

						
						int fileCount = files.size();
						
						DataList list;
						if (((Variable) input[0]).isType(Variable.dataListType)) // list already exists
						{
							if (files.isEmpty())
								return;
							
							list = (DataList) ((Variable) input[0]).getUnchecked();
							int ind = list.getSize();
							
							for(int i = 0; i < fileCount; i++)
								list.set(ind+i, files.get(i).getAbsolutePath());							
						}
						else
						{
							if (files.isEmpty())
							{
								(new DataList()).initType(Variable.textType);
								return;
							}
							
							String file = files.get(0).getAbsolutePath();
							list = DataList.externalSet((Variable) input[0], 0, new Term(0, file, Variable.textType), Variable.textType);
							
							for(int i = 1; i < fileCount; i++)
								list.set(i, files.get(i).getAbsolutePath());
						}
						
						/*
						String file = files.get(0).getAbsolutePath();
						DataList list = DataList.externalSet((Variable) input[0], 0, new Term(0, file, Variable.textType), Variable.textType);
						
						int fileCount = files.size();
						for(int i = 1; i < fileCount; i++)
							list.set(i, files.get(i).getAbsolutePath());*/
					}));
	}
	public static ProgramElement visualize_ElFileLoadDirContents(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "List Files", "Add all files found in a given directory into a list.\nIf the list does not exist yet, it will be created.\nIf 'Incl. Subdir' is true, the contents of all subdirectories will be added as well.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "List Identifier");
		vis.setArgumentDescription(1, new TextOrVariable(), "Directory"); // stretch, repeat etc
		vis.setArgumentDescription(2, new BooleanOrVariable(), "Incl. Subdir");
		vis.addOptionalParameter(0, new BooleanOrVariable("False"), "Incl. Hidden", "If true, hidden files are included as well.\nDefault is false.");
		vis.addOptionalParameter(1, new TextOrVariable(), "Ending With", "Define file endings which should be included. For example '.jpg'.\nThe ending is case insensitive.\nInclude multiple variations by separating them with this symbol: '|'.");
		
		return(vis);
	}
	
	
	public static ProgramElement create_ElFileLoadDirs()
	{
		Object[] input = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElFileLoadDirs",
				input,
				() -> {
					}
				,
				() -> {					
						String folder = (String) input[1];
						
						folder = FileHelpers.convertIfExternal(folder);
						folder = FileHelpers.resolveUniversalFilePath(folder);
						
						if (!FileHelpers.directoryExists(folder))
						{
							Execution.setError("The directory path does not exist!\nGiven path: " + folder, false);
							return;
						}
						
						List<File> files = new ArrayList<File>();
						
						FileHelpers.listDirectories(folder, files, (boolean) input[2], content[0].getOptionalArgTrue(0));
						
						int fileCount = files.size();						
						
						DataList list;
						if (((Variable) input[0]).isType(Variable.dataListType)) // list already exists
						{
							if (files.isEmpty())
								return;
							
							list = (DataList) ((Variable) input[0]).getUnchecked();
							int ind = list.getSize();
							
							for(int i = 0; i < fileCount; i++)
								list.set(ind+i, files.get(i).getAbsolutePath());
						}
						else
						{
							if (files.isEmpty())
							{
								(new DataList()).initType(Variable.textType);
								return;
							}
							
							String file = files.get(0).getAbsolutePath();
							list = DataList.externalSet((Variable) input[0], 0, new Term(0, file, Variable.textType), Variable.textType);
							
							for(int i = 1; i < fileCount; i++)
								list.set(i, files.get(i).getAbsolutePath());
						}
						
					}));
	}
	public static ProgramElement visualize_ElFileLoadDirs(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "List Directories", "Add all directories found inside a given directory into a list.\nIf the list does not exist yet, it will be created.\nIf 'Incl. Subdir' is true, the contents of all subdirectories will be added as well.");
		vis.addParameter(0, new VariableOnly(true, true), "List Identifier", "List where the files will be placed in.");
		vis.setArgumentDescription(1, new TextOrVariable(), "Directory");
		vis.setArgumentDescription(2, new BooleanOrVariable(), "Incl. Subdir");
		vis.addOptionalParameter(0, new BooleanOrVariable("False"), "Incl. Hidden", "If true, hidden files are included as well.\nDefault is false.");
		
		return(vis);
	}
	

	
	public static ProgramElement create_ElSaveVar()
	{
		Object[] input = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElSaveVar",
				input,
				() -> {
					try
					{
						synchronized(Actions.class)
						{
							String filePath = (String) input[0];
							
							filePath = FileHelpers.convertIfExternal(filePath);
							filePath = FileHelpers.resolveUniversalFilePath(filePath);
							
					        List<String> existingVarsInFile = new ArrayList<>(); 
							
							if (FileHelpers.fileExists(filePath))
							{
								FileReader fileIn = new FileReader(filePath);
						        BufferedReader reader = new BufferedReader(fileIn);
								
						        
						        String line = "";
						        String current = "";
						        while((line = reader.readLine()) != null)
						        {
						        	if (line.startsWith(GlobalSettings.varTypeStarter))
						        	{
						        		if (!current.isEmpty())
							        		existingVarsInFile.add(current);
						        		current = line;
						        	}
						        	else
						        		current += "\n"+line;
						        }
				        		if (!current.isEmpty())
					        		existingVarsInFile.add(current);
				        		
				        		reader.close();
				        		fileIn.close();
							}
							
	
							
							
							StringBuilder data = new StringBuilder();
							
							for(Object vari: content[0].getTotalOptionalOrExpandedArgumentsArray())
							{
								Variable var = (Variable) vari;
								String starter = GlobalSettings.varTypeStarter + VariableHandler.getVariableName(var);
								
								data.setLength(0);
								data.append(starter);
								data.append("\n");
								data.append(String.valueOf(var.getType()));
								data.append("\n");
								data.append(var.toPersistableString());
								data.append("\n");
								
								boolean write = true;
								for(int i = 0; i < existingVarsInFile.size(); i++)
								{
									if (existingVarsInFile.get(i).startsWith(starter))
									{
										existingVarsInFile.set(i, data.toString());
										write = false;
									}
								}
								
								if (write)
									existingVarsInFile.add(data.toString());
								
							}
							
							
							FileHelpers.writeLineListToFile(filePath, existingVarsInFile, "Could not write the file to: " + filePath);
						}
					}
					catch (IOException e)
					{
						Execution.setError("Writing variables to a file failed!\nReason: " + e.getMessage(), false);
					}
					catch (AccessUnsetVariableException e)
					{
						Execution.setError("Trying to write down a variable which has never been initialized!\nVariable: " + VariableHandler.getVariableName(e.getSourceVariable()), false);
					}
					}));
	}
	public static ProgramElement visualize_ElSaveVar(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Save Variables", "Saves one or more variables to a file.\nThey may contain values, booleans, texts, lists (of said types) or splines\nand may be saved and loaded from the file at any time.\nRelative and absolute file paths are allowed.\nYou may overwrite the same variable into one file\nbut note that this may get slow if many large datatypes\nlike Splines or Lists have been saved to the same file!");
		vis.setArgumentDescription(0, new TextOrVariable("DataFile.dat"), "File");
		vis.setExpandableArgumentDescription(VariableOnly.class, null, true, false, "Variable #", 32, "Up to 32 variables to write at once.");
		return(vis);
	}
	
	public static ProgramElement create_ElLoadVar()
	{		
		Object[] input = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityConditionContent( "ElLoadVar",
				input,
				() -> {
						try
						{
							String filePath = (String) input[0];
							
							filePath = FileHelpers.convertIfExternal(filePath);
							filePath = FileHelpers.resolveUniversalFilePath(filePath);
							
							if (!FileHelpers.fileExists(filePath))
								return(true);
							
					        FileReader fileIn = new FileReader(filePath);
					        BufferedReader reader = new BufferedReader(fileIn);
	
				        	boolean fullTry = true;
					        String searchFor = "";
							for(Object var: content[0].getTotalOptionalOrExpandedArgumentsArray())
							{
								searchFor = GlobalSettings.varTypeStarter+VariableHandler.getVariableName((Variable) var);
								
								boolean retry = true;
								
								while(retry)
								{
							        String line;
							        while((line = reader.readLine()) != null)
							        {
							        	if (line.startsWith(searchFor))
							        	{
							        		int type = Integer.valueOf(reader.readLine());
							        		StringBuilder data = new StringBuilder();
									        while(((line = reader.readLine()) != null) && !line.startsWith(GlobalSettings.varTypeStarter))
									        	data.append(line).append("\n");								        
									        ((Variable) var).fromPersitableString(type, data.toString());
									        
									        retry = false;
								        	fullTry = false;
									        
							        		break;
							        	}
							        }
								
							        if (retry)
							        {
							        	if (fullTry)
							        	{
							        		reader.close();
											return(true);
							        	}
							        	
							        	reader.close();
							        	reader = new BufferedReader(fileIn);
							        	fullTry = true;
							        }
								}
							}
							
							reader.close();
							fileIn.close();
						}
						catch(IOException e)
						{
							Execution.setError("Reading variables from a file failed!\nReason: " + e.getMessage(), false);
						}
						return(false);
					}).removeFixedOptionalArguments());
	}
	public static ProgramElement visualize_ElLoadVar(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Load Variables", "Loads one or more variables from a file written by 'Write Variable'.\nIf any of the variables is not inside the file, the child elements of this block is executed.\nThis allows to smoothly initialize the variables manually.");
		vis.setArgumentDescription(0, new TextOrVariable("DataFile.dat"), "File");
		vis.setExpandableArgumentDescription(VariableOnly.class, null, true, true, "Variable #", 32, "Up to 32 variables to read at once.");
		return(vis);
	}
	
	
	
	static public FunctionalityContent create_ElReadFile()
	{
		Object[] params = new Object[2];
		return(	new FunctionalityContent( "ElReadFile",
				params,
				() -> {					
					
					String filePath = (String) params[0];
					
					filePath = FileHelpers.convertIfExternal(filePath);
					filePath = FileHelpers.resolveUniversalFilePath(filePath);
					
					File f = new File(filePath);
					if (!f.exists())
					{
						Execution.setError("The file with the following path does not exist:\n"+f.getAbsolutePath(), false);
						return;
					}
					try {
						String fileContent = new String(Files.readAllBytes(Paths.get(f.getPath())));
						initVariableAndSet(params[1], Variable.textType, fileContent);
					} catch (IOException e)
					{
						Execution.setError("Error reading file with the following path:\n"+f.getPath(), false);
					}
					
					
					}));
	}
	static public VisualizableProgramElement visualize_ElReadFile(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Read File", "Reads the entire content of a file into a variable.");
		elDelay.addParameter(0, new TextOrVariable(), "File Path", "May be absolute or relative to the DRASP Executor.");
		elDelay.addParameter(1, new VariableOnly(true, true), "Output Variable", "Variable which will contain the text.");
		
		return(elDelay);
	}
	
	static public FunctionalityContent create_ElWriteFile()
	{		
		Object[] params = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElWriteFile",
				params,
				() -> {
					
					String input = String.valueOf(params[1]);
					
					String filePath = (String) params[0];
					
					filePath = FileHelpers.convertIfExternal(filePath);
					filePath = FileHelpers.resolveUniversalFilePath(filePath);
					
					File f = new File(filePath);
					
					try
					{
						if (f.exists())						
							Files.write(Paths.get(f.getPath()), input.getBytes(), cont[0].getOptionalArgTrue(0) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
						else
						    Files.write(Paths.get(f.getPath()), input.getBytes(), StandardOpenOption.CREATE);
					}
					catch (IOException e)
					{
						Execution.setError("Error writing to file with the following path:\n"+f.getAbsolutePath(), false);
					}
					}));
	}
	static public VisualizableProgramElement visualize_ElWriteFile(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Write File", "Writes the content of a text or value variable to a file.\nDepending on the optional argument,\nthe file will either be overwritten or the text appended to it.\nNote: To write a series of elements to the same file, use 'Write Variables'\nor generate a text through the 'List to Text' Structure.");
		elDelay.addParameter(0, new TextOrVariable(), "File Path", "May be absolute or relative to the DRASP Executor.");
		elDelay.addParameter(1, new TextOrValueOrVariable(), "Input", "Text to write down.");
		elDelay.addOptionalParameter(0, new BooleanOrVariable("False"), "Append", "If true, the input will be added\nto the file if it already exists.");
		
		return(elDelay);
	}
	
	
	
	static public FunctionalityContent create_ElCopyFile()
	{		
		Object[] params = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElCopyFile",
				params,
				() -> {
					
					File source = new File(FileHelpers.resolveUniversalFilePath(FileHelpers.convertIfExternal((String) params[0])));
					File target = new File(FileHelpers.resolveUniversalFilePath(FileHelpers.convertIfExternal((String) params[1])));
					
					try
					{
						if (!source.exists())
						{
							Execution.setError("The following file could not be found:\n"+source.getAbsolutePath(), false);
							return;
						}
						else
						{
							if (!cont[0].getOptionalArgTrue(1)) // just copy
								checkedCopy(source, target);
							else // copy into directory
							{
								if (target.isDirectory())
									checkedCopy(source, new File(FileHelpers.addSubfile(target.getAbsolutePath(), source.getName())));
								else
									checkedCopy(source, new File(FileHelpers.addSubfile(target.getParentFile().getAbsolutePath(), source.getName())));
							}
							
							if (cont[0].getOptionalArgTrue(0)) // delete source
								Files.deleteIfExists(source.toPath());
						}
					}
					catch (IOException e)
					{
						Execution.setError("Error copy the following file or directory:\n"+source.getAbsolutePath()+"\nReason: " + e.getMessage(), false);
					}
					}));
	}
	static public VisualizableProgramElement visualize_ElCopyFile(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Copy/Move File", "Copies or moves a file or directory.");
		elDelay.addParameter(0, new TextOrVariable(), "Source Path", "May be absolute or relative to the DRASP Executor.\nDirectories are allowed too.");
		elDelay.addParameter(1, new TextOrVariable(), "Target Path", "May be absolute or relative to the DRASP Executor.");
		elDelay.addOptionalParameter(0, new BooleanOrVariable("False"), "Delete Source", "Delete the original file after copying.");
		elDelay.addOptionalParameter(1, new BooleanOrVariable("False"), "Into Directory", "If true, the source will be placed\ninto the directory of the target.");
		
		return(elDelay);
	}
	
	private static void checkedCopy(File source, File target)
	{
		try
		{
			Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e)
		{
			Execution.setError("Error copy the following file or directory:\n"+source.getAbsolutePath()+"\nTo:\n"+target.getAbsolutePath(), false);
		}
	}
	
	static public FunctionalityContent create_ElDeleteFile()
	{		
		Object[] params = new Object[1];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElDeleteFile",
				params,
				() -> {
					
					File file = new File(FileHelpers.resolveUniversalFilePath(FileHelpers.convertIfExternal((String) params[0])));
					
					try
					{
						if (file.exists() || cont[0].getOptionalArgTrue(0) || !cont[0].hasOptionalArgument(0))
							Files.deleteIfExists(file.toPath());
						else
							Execution.setError("The following file could not be found:\n"+file.getAbsolutePath(), false);
					}
					catch (IOException e)
					{
						Execution.setError("Error deleting the following file or directory:\n"+file.getAbsolutePath(), false);
					}
					}));
	}
	static public VisualizableProgramElement visualize_ElDeleteFile(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Delete File", "Deletes a file or directory.");
		elDelay.addParameter(0, new TextOrVariable(), "Source Path", "May be absolute or relative to the DRASP Executor.\nDirectories are allowed too.");
		elDelay.addOptionalParameter(0, new BooleanOrVariable("True"), "Ign. Error", "Ignore if the file does not exist.\nOtherwise throw error.");
		
		return(elDelay);
	}
	
	static public FunctionalityContent create_ElCreateFile()
	{		
		Object[] params = new Object[1];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElCreateFile",
				params,
				() -> {
					
					File target = new File(FileHelpers.resolveUniversalFilePath(FileHelpers.convertIfExternal((String) params[0])));
					
					if (target.exists())
						return;
					
					try
					{
						if (target.isDirectory())
							Files.createDirectories(target.toPath());
						else
							Files.createFile(target.toPath());		
					}
					catch (IOException e)
					{
						Execution.setError("Error creating the following file or directory:\n"+target.getAbsolutePath()+"\nReason: " + e.getMessage(), false);
					}
						
					}));
	}
	static public VisualizableProgramElement visualize_ElCreateFile(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Create File/Dir", "Creates an empty file or directory.\nNote that you can also directly write to a file with the corresponding element.");
		elDelay.addParameter(0, new TextOrVariable(), "Target Path", "May be absolute or relative to the DRASP Executor.\nDirectories are allowed too (in that case end with '/').");
		
		return(elDelay);
	}
	
	
}
