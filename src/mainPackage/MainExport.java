package mainPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.Message;

import dataTypes.FunctionalityConditionContent;
import dataTypes.ProgramElement;
import main.functionality.helperControlers.hardware.GPIOcontrol;
import main.functionality.helperControlers.hardware.PWMboard.PWMcontrol;
import main.functionality.helperControlers.network.ConnectionMaster;
import main.functionality.helperControlers.network.TelegramControl;


/*
 * If started in Eclipse, this project exports itself as if using the "Export Runnable Jar" dialog with the Setting "Package libraries" activated.
 * That means .jar files from the filepath that are palced in the given directory (default is "libs") are not unpacked but just copied into the new .jar.
 * A copy of Eclipse's "Jar in Jar Loader" is added to the project and activated with a corresponding manifest file.
 * 
 * The only thing needed to do to make a project compatible with this code is to add the "ExportHelpers" directory into the libs directory!
 */


public class MainExport {
	
	public final static char LF  = (char) 0x0A;
	


	public static final GPIOcontrol GPIOctrl = new GPIOcontrol();
	protected static final PWMcontrol PWMctrl = new PWMcontrol();
	protected static final TelegramControl TGRMctrl = new TelegramControl();
	//protected static final OneWireControl OneWctrl = new OneWireControl();
	protected static final ConnectionMaster CONctrl = new ConnectionMaster();
	
	
	

	public static void main(String[] args)
	{
		System.out.println("EXECUTING EXPORTER! Exporting in...");

		
		Message a = new Message();
		
		
		FunctionalityConditionContent fnc = new FunctionalityConditionContent();
		
		ProgramElement el = (ProgramElement) fnc;
		
		el.getContent();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("DONE!");
		
	}
	
}
