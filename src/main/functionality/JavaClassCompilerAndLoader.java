package main.functionality;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import execution.handlers.InfoErrorHandler;
import staticHelpers.FileHelpers;
import staticHelpers.LocationPreparator;

/*
 * This class is used to compile .java files and optionally load the resulting .class file
 * 
 */

public class JavaClassCompilerAndLoader {
	
	static private JavaCompiler compiler;
	
	// Returns null if java compiler is missing. Returns true if compiled and false if cpompilation not needed
	static Boolean recompileIfNeeded(String fileNameWithoutTermination, String filePath)
	{
		boolean needsCompilation = !FileHelpers.fileExists(fileNameWithoutTermination+".class"); // compile in every case if the .class file is missing
		
		if (!needsCompilation)
		{
			// check whether the .java file is newer than the .class file
			// if the .java file is newer than the .class file that means that the java has to be recompiled.
		    if (new File(fileNameWithoutTermination+".java").lastModified() > new File(fileNameWithoutTermination+".class").lastModified()+1500)
		    	needsCompilation = true;						
		}
		
		if (needsCompilation) // || true)
		{
			return(recompile(filePath));
		}
		else
			return(false);
	}
	
	
	public static Boolean recompile(String filePath)
	{
		if (compiler == null)
		{
			// Compile source file.
			compiler = ToolProvider.getSystemJavaCompiler();
			
			if (compiler == null)
			{
				InfoErrorHandler.callPrecompilingError("The Java Compiler could not be loaded therefore the file could not be recompiled!\nEnsure that you are using a JDK as your default Java executor,\nor as a simpler fix, copy the lib/tools.jar file from an JDK to the lib directory of your JRE!");
				return(null);
			}
		}
		
		// Compile
		compiler.run(null, null, null, filePath);
		
		return(true);
	}


	static private URL[] root = null;
	
	static Class<?> loadClass(String className)
	{
		// Get the root
		try {
			return(reloadClass(className));
		}
		catch (MalformedURLException | ClassNotFoundException e)
		{
			InfoErrorHandler.callPrecompilingError("Error when loading class '" + className + "'.\nError: " + e.getMessage());
		}
		
		return(null);
	}


	public static Class<?> reloadClass(String className) throws MalformedURLException, ClassNotFoundException
	{
		if (root == null)
			root = new URL[] { new File(LocationPreparator.getExternalDirectory()).getParentFile().toURI().toURL() };
		
		// Load the class
		URLClassLoader classLoader = URLClassLoader.newInstance(root);
		
		return( Class.forName(className, true, classLoader) );
	}
	
	





}
