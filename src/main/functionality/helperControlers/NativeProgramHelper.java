package main.functionality.helperControlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import main.functionality.SharedComponents;
import staticHelpers.LocationPreparator;

/*
 * 
 * This type can be used to implement functionality relying on an external (possibly compiled C/C++)) program and communication with the software via standard I/O
 * 
 */

public class NativeProgramHelper
{
	private static List<NativeProgramHelper> controlers = new ArrayList<>();
	
	
	
	volatile private Process process;
	volatile String newestResponse = null;
	volatile boolean running = false;
	
	
	public NativeProgramHelper(String filePathInExternal, String arguments)
	{
		
		try
		{
			String command = LocationPreparator.getExternalDirectory() + filePathInExternal;
			
			Process proc = SharedComponents.runtime.exec("chmod +x " + command); // Get the rights to execute this program
			try
			{
				proc.waitFor();
			}
			catch (InterruptedException e) {}
			
			
			process = SharedComponents.runtime.exec(command + " " + arguments);
			
			
			InputStream stream = process.getInputStream();
			
			
			new Thread() {
				public void run()
				{
			    	InputStreamReader inputStreamReader = new InputStreamReader(stream);
				    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				    
				    String line = null;
				    try
				    {
					    while(running)
						    //while ((line = bufferedReader.readLine()) != null)
						    {
					    		line = bufferedReader.readLine();
					    		System.out.println("READING!");
					    		if (line != null)
					    			newestResponse = bufferedReader.readLine();
						    }
				    } catch (IOException e) { e.printStackTrace(); }
				    
				}}.start();
			
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		
		
	}
	

	public String getNewTextResponse()
	{
		if (newestResponse == null)
			return(null);
		
		String r = newestResponse;		
		newestResponse = null;
		
		return(r);
	}

	public Double getNewNumberResponse()
	{
		if (newestResponse == null)
			return(null);
		
		Double r = Double.valueOf(newestResponse);
		
		newestResponse = null;
		
		return(r);
	}
	
	
	
	protected void reset() // Refers to when the DRASP software restarts a simulation. This should not restart the external program but quit it!
	{
		quit();
	}
	
	public void quit()
	{
		process.destroy();
	}

	public static void resetAll()
	{
		for (NativeProgramHelper controler: controlers)
		{
			controler.reset();
		}
	}
	public static void quitAll()
	{
		for (NativeProgramHelper controler: controlers)
		{
			controler.quit();
		}
	}




	
	
	
	
	
}
