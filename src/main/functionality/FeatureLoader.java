package main.functionality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

import dataTypes.ComponentContent;
import dataTypes.DataNode;
import dataTypes.ProgramElement;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.minor.MultiPositionList;
import execution.handlers.InfoErrorHandler;
import productionGUI.sections.elementManagers.AbstractSectionManager;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.FileHelpers;
import staticHelpers.LocationPreparator;


public class FeatureLoader extends SharedComponents {

	public static final Map<String, Method> creatingMethods = new HashMap<>();
	protected static final Map<String, Method> visualizingMethods = new HashMap<>();
	
	public static void loadFeatureType(String dirBase, String typeBase, String type, DataNode<ProgramElement> rootNode, boolean visualizable)
	{
		loadFeatureType(dirBase, typeBase, type, rootNode, visualizable, true, null);
	}
	public static void loadFeatureType(String dirBase, String typeBase, String type, DataNode<ProgramElement> rootNode, boolean visualizable, boolean isFunctionality, Class<?> associatedSection)
	{
		MultiPositionList<DataNode<ProgramElement>> parentNodes = new MultiPositionList<>();				
		
		String rootDir = dirBase + type + File.separator;
		
		List<File> files = new ArrayList<File>();
		FileHelpers.listFiles(rootDir, files, true);		
		

		for(File file: files)
		{
			String name = file.getName();
			String filePath = file.getPath();

			if (name.startsWith("_")) // skip disabled files
				continue;
			
			if (!name.endsWith(".java")) // skip other files but .java
				continue;
			else
			{
				
				name = name.substring(0, file.getName().length()-5);
				String className = typeBase + type + '.' + name;
				
				Boolean ret = JavaClassCompilerAndLoader.recompileIfNeeded(rootDir + name, filePath);
				
				if (ret == null)
					continue;
				
				if (ret)
					InfoErrorHandler.printEnvironmentInfoMessage("Recompiled class '"+ className +"'.");

				InfoErrorHandler.printEnvironmentInfoMessage("Loading class '"+ className +"'.");
				Class<?> loadedClass = JavaClassCompilerAndLoader.loadClass(className);
				
				if (loadedClass == null)
					continue;
				
				
				if (visualizable)
					applyVisualizableFeature(loadedClass, filePath, parentNodes, isFunctionality, associatedSection);
				else
					applyFeature(loadedClass);
			}
			

		}
		
		if (visualizable && isFunctionality)
		{
			// Add the custom node
			DataNode<ProgramElement> parentNode = attachToNodeUndelatable(null, new VisualizableProgramElement(new FunctionalityContent(type+"CustomNode"), "Custom with Values", "Conditions with defined values.\nDrag here from the queue to save for re-use.\nKeep <CTRL> pressed when dragging to delete from here!", true));				
			parentNodes.place(-1, parentNode);
		}			
	

		
		
		// Attach the root nodes to the correct subtree
		for (DataNode<ProgramElement> node: parentNodes)
		{
			rootNode.addChild(node);
		}
		
	}
	
	
	public static void applyFeature(Class<?> featureClass)
	{
		try {			
			
			for (Method method: featureClass.getDeclaredMethods())
			{
				String mthName = method.getName();

				synchronized(FeatureLoader.class)
				{
					if (mthName.startsWith("create_"))
					{
						String contentName = mthName.substring(7);
						if (null != creatingMethods.put(contentName, method)) // if not null, that means the key has already been set!
							InfoErrorHandler.callPrecompilingError("A feature named '" + contentName + "' exists twice! First occurence will be overwritten.");
					}
				}
			}
		
		} catch (IllegalArgumentException | SecurityException e) {
			InfoErrorHandler.callPrecompilingError("Loading the feature '" +featureClass.getSimpleName() + "' failed!\nDid you provide the public static POSITION, NAME and DESCRIPTION variables?\nError: "+ e);
		}
	}
	
	public static DataNode<ProgramElement> applyVisualizableFeature(Class<?> featureClass, String javaFile, MultiPositionList<DataNode<ProgramElement>> parentNodes, boolean hasVisualizableFuncs, Class<?> associatedSection)
	{
		DataNode<ProgramElement> parentNode = null;
		
		try {

			int position = -1;
			String name = null, description = "", identifier = null;
			
			// Loop through all fields
			for (Field field: featureClass.getDeclaredFields())
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
				else
				if (field.getName().equals("DEACTIVATED"))
					if ((boolean) field.get(null)) // if DEACTIVATED is true
						return(null); // Skip
			}
			
			if (name == null)
			{
				InfoErrorHandler.callPrecompilingError("The public static 'NAME' is missing for the feature '" +featureClass.getSimpleName() + "'!");
				name = "NAME MISSING!";
			}

			if (identifier == null)
				identifier = name.replace(" ", "");
			
			
			
			if (hasVisualizableFuncs)
				parentNode = attachToNodeUndelatable(null, new VisualizableProgramElement(new FunctionalityContent(identifier), name, description, false));
			else
				parentNode = attachToNodeUndelatable(null, new VisualizableProgramElement(new ComponentContent(identifier, identifier), name, description, false, associatedSection));
			
			if (parentNodes != null)
				parentNodes.place(position, parentNode);
			
			
			
			Method[] unOrderedMethods = featureClass.getDeclaredMethods();
			
			int len = unOrderedMethods.length;
			
			Method[] methods = new Method[len/(hasVisualizableFuncs ? 2 : 1)];
			String[] methodNames = new String[len/(hasVisualizableFuncs ? 2 : 1)];
			
			Method[] unorderedOtherMethods = new Method[len];
			
			int methodCount = 0, uo = 0;
			for(Method m: unOrderedMethods)
			{
				if (m.getName().startsWith("create_"))
				{
					methods[methodCount] = m;
					methodNames[methodCount] = m.getName();					
					methodCount++;
				}
				else
				{
					unorderedOtherMethods[uo] = m;
					uo++;
				}
			}

			Method[] orderedCreateMethods = new Method[methodCount * (hasVisualizableFuncs ? 2 : 1)];


			
			int orderIndex = 0;
			for(String line: FileHelpers.readAllLines(new File(javaFile)))
			{
				line = line.trim();
				
				if (line.startsWith("static public") || line.startsWith("public static"))
				{
					
					for(int j = 0; j < methodCount; j++)
					{
						if (methodNames[j] != null)
						if (line.contains(methodNames[j]))
						{
							orderedCreateMethods[orderIndex] = methods[j];
							
							methodNames[j] = null; // ensure that each method name is added only once
							
							orderIndex++;
							break;
						}
					}
				}
			}
			
			
			
			for (Method method: orderedCreateMethods)
			if (method != null)
			{
				String mthName = method.getName();
				
				synchronized(FeatureLoader.class)
				{
					String contentName = mthName.substring(7);
					if (null != creatingMethods.put(contentName, method)) // if not null, that means the key has already been set!
						InfoErrorHandler.callPrecompilingError("A feature named '" + contentName + "' exists twice! First occurence will be overwritten.");
					
					if (hasVisualizableFuncs)
						attachToNodeUndelatable(parentNode, Functionality.createProgramContent(contentName));
					else
					{
						ComponentContent cont = (ComponentContent) Functionality.createProgramContent(contentName);
						attachToNodeUndelatable(parentNode, new VisualizableProgramElement(cont, cont.getName(), cont.getTooltipText(), associatedSection));
					}						
				}
			}
			
			for (Method method: unorderedOtherMethods)
			if (method != null)
			{
				String mthName = method.getName();
				
				if (mthName.startsWith("visualize_"))
					visualizingMethods.put(mthName.substring(10), method);
			}

			
		
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			InfoErrorHandler.callPrecompilingError("Loading the feature '" +featureClass.getSimpleName() + "' failed!\nDid you provide the public static POSITION, NAME and DESCRIPTION variables?\nError: "+ e);
		}
		
		return(parentNode);
	}
	
	
	
}
