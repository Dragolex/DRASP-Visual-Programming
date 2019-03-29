package staticHelpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import execution.handlers.InfoErrorHandler;
import execution.handlers.ToolsDatabase;
import main.MainControler;
import productionGUI.targetConnection.TargetConnection;
import settings.EnvironmentDataHandler;
import settings.HaveDoneFileHandler;

public class LocationPreparator {
	
	private static String OS;
	private static int OStype = -1;

	private static String hardwareName = "";
	private static boolean use_i2c_bus_1 = true;
	private static boolean onRaspberry = false;
	
	static private File originalRunnerFile;
	
	volatile private static String externalDir = "", functionalityDir = "", electronicDir = "", baseDir = "";
	
	
	private final static String optionalResourcesDirectory = "external" + File.separator + "optionalResources";
	
	static private JarFile openJarFile;
	
	public static void init()
	{
		OS = System.getProperty("os.name").toLowerCase();

		String os_name = "";
		
		if (OS.contains("win"))
		{
			OStype = 0;
			os_name = "Windows";
		}
		if (OS.contains("mac"))
		{
			OStype = 1;
			os_name = "Mac";
		}
		if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix") )
		{
			OStype = 2;
			os_name = "Unix";
		}
		if (OS.contains("sunos"))
		{
			OStype = 3;
			os_name = "Solaris";
		}

		InfoErrorHandler.printEnvironmentInfoMessage("Running on " + os_name +".");
		
		if (isUnix())
		{
			hardwareName = ToolsDatabase.execAndRetrieve("cat /proc/device-tree/model");
			
			InfoErrorHandler.printEnvironmentInfoMessage("Running on a " + hardwareName);
			
			hardwareName = hardwareName.toLowerCase();
			
			onRaspberry = hardwareName.contains("raspberry");
			
			if (hardwareName.contains("model a") && !hardwareName.contains("a+"))
				use_i2c_bus_1 = false;
		}
		
		if (isCompiled())
		{
			String basePath = getOriginalRunner().getParent();
			
			Enumeration<JarEntry> enumEntries = getJarEntries();
			
			BlockingQueue<JarEntry> packedFiles =  new LinkedBlockingQueue<>();
			
			/*
			String[] toIgnore = String[] {
				removeSeps(file.getName().startsWith(optionalResourcesDirectory)),
				removeSeps(file.getName().endsWith(TargetConnection.getFileName())),
				removeSeps(file.getName().endsWith(EnvironmentDataHandler.getFileName())),
				removeSeps(file.getName().endsWith(HaveDoneFileHandler.getFileName())),
			}
			*/
			
			while (enumEntries.hasMoreElements())
			{
			    JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
			    String fname = file.getName();
			    
			    String optionalResourcesDirectoryConv = optionalResourcesDirectory.replace(File.separator, "/");
			    
				if (fname.startsWith("external"))
				{
					if (file.isDirectory())
						new File(FileHelpers.addSubfile(basePath, file.getName())).mkdirs();
					else
					if (!fname.startsWith(optionalResourcesDirectoryConv))	
					if (!fname.endsWith(TargetConnection.getFileName())) // Ignore those because it should be recreated by the program locally
					if (!fname.endsWith(EnvironmentDataHandler.getFileName()))
					if (!fname.endsWith(HaveDoneFileHandler.getFileName()))
					{
						try {
							packedFiles.put(file);
						} catch (InterruptedException e) {e.printStackTrace();}
						
					}
				}
				else
				// Copy the functionalities and the electronic components files
				if (file.getName().startsWith("functionality") || file.getName().startsWith("electronic"))
				{					
					if (file.isDirectory())
						new File(FileHelpers.addSubfile(basePath, file.getName())).mkdirs();
					else
					{
						try {
							packedFiles.put(file);
						} catch (InterruptedException e) {e.printStackTrace();}
					}
				}
			}
			
			multiThreadedExtract(packedFiles);
			
			closeCurrentJar();
			
			externalDir = FileHelpers.addSubfile(basePath, "external" + File.separator);
			functionalityDir = FileHelpers.addSubfile(basePath, "functionality" + File.separator);
			electronicDir = FileHelpers.addSubfile(basePath, "electronic" + File.separator);
			baseDir = basePath;
		}
		else
		{
			externalDir = FileHelpers.addSubfile(getOriginalRunner().getParent(), "resources" + File.separator + "external" + File.separator);
			functionalityDir = FileHelpers.addSubfile(getOriginalRunner().getParent(), "resources" + File.separator + "functionality" + File.separator);
			electronicDir = FileHelpers.addSubfile(getOriginalRunner().getParent(), "resources" + File.separator + "electronic" + File.separator);
			baseDir = FileHelpers.addSubfile(getOriginalRunner().getParent(), "resources");
		}		
    }
	
	public static Enumeration<JarEntry> getJarEntries()
	{
		while (openJarFile != null) // wait until handling another file has been finished
			OtherHelpers.sleepNonException(10);
		
		try {
			openJarFile = new JarFile(getOriginalRunner());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return(openJarFile.entries());
	}
	public static void closeCurrentJar()
	{
		if (openJarFile == null) return;
		
		try {
			openJarFile.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		openJarFile = null;
	}

	public static void extractOptionalResource(String resDirectory)
	{
		if (!isCompiled())
			return;
		
		Enumeration<JarEntry> enumEntries = getJarEntries();
		
		BlockingQueue<JarEntry> packedFiles =  new LinkedBlockingQueue<>();
		
		String sourceDir = FileHelpers.addSubfile(optionalResourcesDirectory, resDirectory);
		
		while (enumEntries.hasMoreElements())
		{
		    JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
		    
			if (file.getName().startsWith(sourceDir))
			{
				if (file.isDirectory())
					new File(FileHelpers.addSubfile(sourceDir, file.getName())).mkdirs();
				else
					try {
						packedFiles.put(file);
					} catch (InterruptedException e) {e.printStackTrace();}
			}
		}
		
		multiThreadedExtract(packedFiles);
		
		closeCurrentJar();
	}
	
	
	private static void multiThreadedExtract(BlockingQueue<JarEntry> packedFiles)
	{
		String basePath = getOriginalRunner().getParent();
		int proc = Math.max(1, Runtime.getRuntime().availableProcessors()-1);
		
		List<Thread> threads = new ArrayList<>();
		
		for(int i = 0; i < proc; i++)
		{
			threads.add(new Thread( () -> {
			try {
				while (!packedFiles.isEmpty())
				{
					JarEntry file = packedFiles.take();
					String outFile = FileHelpers.addSubfile(basePath, file.getName());
					
					InputStream is = openJarFile.getInputStream(file); // get the input stream
				    FileOutputStream fos = new FileOutputStream(outFile);
				    
				    while (is.available() > 0)
				        fos.write(is.read());
				    
				    fos.close();
				    is.close();

	            }
				}
				catch (IOException | InterruptedException e)
				{
					e.printStackTrace();
				}
			}));
		}

		for(Thread thr: threads)
			thr.start();
		
		try {		
			for(Thread thr: threads)
				thr.join();
		} catch (InterruptedException e) { e.printStackTrace(); }
		
	}
	

    public static boolean isWindows() {
        return (OStype==0);
    }

    public static boolean isMac() {
        return (OStype==1);
    }

    public static boolean isUnix() {
        return (OStype==2);
    }

    public static boolean isSolaris() {
        return (OStype==3);
    }

    
    
    public static int i2c_bus_ind()
    {
    	return(use_i2c_bus_1 ? 1 : 0);
    }

	
	public static File getOriginalRunner()
	{		
		String surroundingJar = null;

		try {
			surroundingJar = MainControler.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {}
		
		
		
		/*
		String surroundingJar = null;
		
		// gets the path to the jar file if it exists; or the "bin" directory if calling from Eclipse
		String jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()).getAbsolutePath();

		// gets the "bin" directory if calling from eclipse or the name of the .jar file alone (without its path)
		String jarFileFromSys = System.getProperty("java.class.path").split(";")[0];
		
		// If both are equal that means it is running from an IDE like Eclipse
		if (jarFileFromSys.equals(jarDir))
		{
			System.out.println("RUNNING FROM IDE!");
			// The path to the jar is the "bin" directory in that case because there is no actual .jar file.
			surroundingJar = jarDir;
		}
		else
		{
			// Combining the path and the name of the .jar file to achieve the final result
			surroundingJar = jarDir + jarFileFromSys.substring(1);
		}

		System.out.println("JAR File: " + surroundingJar);
		*/
		
		
		originalRunnerFile = new File(surroundingJar);
		
		return(originalRunnerFile);
	}
	
	public static boolean isCompiled()
	{
		return(getOriginalRunner().getPath().endsWith(".jar"));
	}
	
	public static String getExternalDirectory()
	{
		if (externalDir.isEmpty())
			InfoErrorHandler.callBugError("Trying to access the external directory before it has been prepared!");
		
		return(externalDir);
	}
	
	public static String getFunctionalityDirectory()
	{
		if (functionalityDir.isEmpty())
			InfoErrorHandler.callBugError("Trying to access the functionality directory before it has been prepared!");
		
		return(functionalityDir);
	}
	
	public static String getElectronicDirectory()
	{
		if (electronicDir.isEmpty())
			InfoErrorHandler.callBugError("Trying to access the electronic directory before it has been prepared!");
		
		return(electronicDir);
	}
	
	public static String getBaseDirectory()
	{
		if (baseDir.isEmpty())
			InfoErrorHandler.callBugError("Trying to access the base directory before it has been prepared!");
		
		return(baseDir);
	}
	
	
	
	
	
	public static String getRunnerFileDirectory()
	{
		if (isCompiled())
			return(FileHelpers.addSubfile(getOriginalRunner().getParent(), ""));
		else
			return(FileHelpers.addSubfile(getOriginalRunner().getPath(), ""));
	}

	public static String getToolsDirectory()
	{
		return(FileHelpers.addSubfile(getExternalDirectory(), "Tools"));
		
	}

	public static boolean usesSlashForPaths()
	{
		return(!isWindows());
	}

	public static String optResDir()
	{
		return("optionalResoruces"+File.separator);
	}

	public static boolean isRaspberry()
	{
		return(onRaspberry);
	}


    
}
