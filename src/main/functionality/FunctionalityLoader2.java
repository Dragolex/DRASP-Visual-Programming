package main.functionality;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.minor.MultiPositionList;
import execution.handlers.InfoErrorHandler;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.FileHelpers;
import staticHelpers.LocationPreparator;


public class FunctionalityLoader2 extends SharedComponents {

	protected static final Map<String, Method> creatingMethods = new HashMap<>();
	protected static final Map<String, Method> visualizingMethods = new HashMap<>();
	
	
	public static void loadFunctionalityType(String type, DataNode<ProgramElement> rootNode, boolean visualizable)
	{
		MultiPositionList<DataNode<ProgramElement>> parentNodes = new MultiPositionList<>();				
		
		
		List<File> files = new ArrayList<File>();
		String rootDir = FileHelpers.addSubfile(LocationPreparator.getFunctionalityDirectory(), type);
		FileHelpers.listFiles(rootDir, files, true);		
		
		try {
			// Compile source file.
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			
			if (compiler == null)
				InfoErrorHandler.callPrecompilingError("The Java Compiler could not be loaded therefore no external functionality has been loaded.\nIf you want that, ensure that you are using a JDK, or as a simpler fix, copy the lib/tools.jar file from an JDK to the lib directory of your JRE!");
			
			
			for(File file: files)
			{
				String name = file.getName();
				
				if (!LocationPreparator.isCompiled())
				{
					if (!name.endsWith(".java")) // skip other files but .java
						continue;
					else
						name = name.substring(0, file.getName().length()-5);
					
					if (visualizable || !FileHelpers.fileExists(rootDir+File.separator+name+".class"))
					{
						// Compile
						compiler.run(null, null, null, file.getPath());
					}
				}
				else
				{
					if (name.endsWith(".java")) // skip other files but .java
					{
						name = name.substring(0, file.getName().length()-5);
						
						if (visualizable || !FileHelpers.fileExists(rootDir+File.separator+name+".class"))
						{
							// Compile
							compiler.run(null, null, null, file.getPath());
						}
					}
					else
					if (name.endsWith(".class")) // skip other files but .java
						name = name.substring(0, file.getName().length()-6);
				}
				
				

				

				// Get the root
				File root = new File(LocationPreparator.getExternalDirectory());
				root = root.getParentFile();
				
				// Load the class
				URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
				Class<?> cls = Class.forName("functionality." + type + '.' + name, true, classLoader);
				
				if (visualizable)
					applyVisualizableFunctionality(cls, parentNodes);
				else
					applyFunctionality(cls);
			}
			
			if (visualizable)
			{
				// Add the custom node
				DataNode<ProgramElement> parentNode = attachToNodeUndelatable(null, new VisualizableProgramElement(new FunctionalityContent(type+"CustomNode"), "Custom with Values", "Conditions with defined values.\nDrag here from the queue to save for re-use.\nKeep <CTRL> pressed when dragging to delete from here!", true));
				parentNodes.place(-1, parentNode);
			}			
		
		}
		catch (Exception e)
		{
			System.out.println("Problem at loading external files! Error: " + e);
			e.printStackTrace();
		}
		
		
		
		// Attach the root nodes to the correct subtree
		for (DataNode<ProgramElement> node: parentNodes)
		{
			rootNode.addChild(node);
		}
				
	}
	
	public static void applyFunctionality(Class<?> functionalityClass)
	{
		try {			
			
			for (Method method: functionalityClass.getDeclaredMethods())
			{
				String mthName = method.getName();

				synchronized(FunctionalityLoader2.class)
				{
					if (mthName.startsWith("create_"))
					{
						String contentName = mthName.substring(7);
						if (null != creatingMethods.put(contentName, method)) // if not null, that means the key has already been set!
							InfoErrorHandler.callPrecompilingError("A functionality named '" + contentName + "' exists twice! First occurence will be overwritten.");
					}
				}
			}
		
		} catch (IllegalArgumentException | SecurityException e) {
			InfoErrorHandler.callPrecompilingError("Loading the functionality '" +functionalityClass.getSimpleName() + "' failed!\nDid you provide the public static POSITION, NAME and DESCRIPTION variables?\nError: "+ e);
		}
	}
	
	public static void applyVisualizableFunctionality(Class<?> functionalityClass, MultiPositionList<DataNode<ProgramElement>> parentNodes)
	{
		try {

			int position = -1;
			String name = null, description = "", identifier = null;
			
			for (Field field: functionalityClass.getDeclaredFields())
			{
				if (field.getName().equals("POSITION"))
					position = field.getInt(null);
				else
				if (field.getName().equals("NAME"))
					name = (String) field.get(null);
				else
				if (field.getName().equals("DESCRIPTION"))
					description = (String) field.get(null);
				else
				if (field.getName().equals("IDENTIFIER"))
					identifier = (String) field.get(null);
			}
			if (name == null)
			{
				InfoErrorHandler.callPrecompilingError("The public static 'NAME' is missing for the functionality '" +functionalityClass.getSimpleName() + "'!");
				name = "MISSING!";
			}

			if (identifier == null)
				identifier = name.replace(" ", "");
			
			
			DataNode<ProgramElement> parentNode = attachToNodeUndelatable(null, new VisualizableProgramElement(new FunctionalityContent(identifier), name, description, false));
			parentNodes.place(position, parentNode);
			
			
			for (Method method: functionalityClass.getDeclaredMethods())
			{
				String mthName = method.getName();

				synchronized(FunctionalityLoader2.class)
				{
					if (mthName.startsWith("create_"))
					{
						String contentName = mthName.substring(7);
						if (null != creatingMethods.put(contentName, method)) // if not null, that means the key has already been set!
							InfoErrorHandler.callPrecompilingError("A functionality named '" + contentName + "' exists twice! First occurence will be overwritten.");
						attachToNodeUndelatable(parentNode, Functionality.createProgramContent(contentName));
						
					}
					if (mthName.startsWith("visualize_"))
						visualizingMethods.put(mthName.substring(10), method);
				}
			}
		
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			InfoErrorHandler.callPrecompilingError("Loading the functionality '" +functionalityClass.getSimpleName() + "' failed!\nDid you provide the public static POSITION, NAME and DESCRIPTION variables?\nError: "+ e);
		}
	}
	
	
	
}
