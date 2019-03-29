package main.functionality.helperControlers.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;

public class UartDevice
{
	protected static final String identSymbolA = "|§|";
	protected static final String identSymbolB = "|$|";
	
	private Map<String, Stack<String>> data = new HashMap<>();
	private Stack<String> mainData = new Stack<String>();
	
	private Serial serial;
	
	public UartDevice()
	{
		
	}
	
	public void enableSender()
	{
		if (serial == null)
			serial = SerialFactory.createInstance();
		
		
		SerialConfig config = new SerialConfig();

        // set default serial settings (device, baud rate, flow control, etc)
        //
        // by default, use the DEFAULT com port on the Raspberry Pi (exposed on GPIO header)
        // NOTE: this utility method will determine the default serial port for the
        //       detected platform and board/model.  For all Raspberry Pi models
        //       except the 3B, it will return "/dev/ttyAMA0".  For Raspberry Pi
        //       model 3B may return "/dev/ttyS0" or "/dev/ttyAMA0" depending on
        //       environment configuration.
        try {
			config.device(SerialPort.getDefaultPort())
			      .baud(Baud._38400)
			      .dataBits(DataBits._8)
			      .parity(Parity.NONE)
			      .stopBits(StopBits._1)
			      .flowControl(FlowControl.NONE);
		} catch (UnsupportedBoardType | IOException | InterruptedException e1)
        {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        // parse optional command argument options to override the default serial settings.
        //if(args.length > 0){
          //  config = CommandArgumentParser.getSerialConfig(config, args);
        //}
        
        // open the default serial device/port with the configuration settings
        try
        {
			serial.open(config);
		}
        catch (IOException e)
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void enableListener()
	{
		if (serial == null)
			serial = SerialFactory.createInstance();
		
		mainData = new Stack<String>();
		data.put("", mainData);
		

        // create and register the serial data listener
        serial.addListener(new SerialDataEventListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {

                // NOTE! - It is extremely important to read the data received from the
                // serial port.  If it does not get read from the receive buffer, the
                // buffer will continue to grow and consume memory.
            	
                // print out the data received to the console
                try {
                    
                	System.out.println("Retrieved Internally Header: " + event.getHexByteString());
                    String str = event.getAsciiString();
                    System.out.println("Retrieved Internally Main: " + str);
                    
                    if (str.startsWith(identSymbolA))
                    {
                    	int identInd = str.indexOf(identSymbolB);
                    	if (identInd > 0)
                    	{
                    		String ident = str.substring(3, identInd-3);
                    		str = str.substring(identInd+3);
                    		System.out.println("Indent is: " + ident);
                    		if (!data.containsKey(ident))
                    		{
                    			Stack<String> st = new Stack<String>();
               					st.add(str);
                    			data.put(ident, st);
                    		}
                    		else
                    			data.get(ident).add(str);
                    	}
                    }
                    else
                    	mainData.add(str);
                    
                    
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
		
		
	}
	

	public String getNewText()
	{
		if (mainData.isEmpty())
			return(null);
		return(mainData.pop());
	}
	public String getNewText(String identifier)
	{
		Stack<String> st = data.get(identifier);
		if (st != null)
			if (!st.isEmpty())
				return(st.pop());
		return(null);
	}
	
	
	public void sendText(String text, boolean newLine)
	{
		try
		{
			if (newLine)
				serial.writeln(text);
			else
				serial.write(text);
			
		} catch (IllegalStateException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendText(String string, String identifier, boolean newLine)
	{
		sendText(identSymbolA + string + identSymbolB, newLine);
	}

}
