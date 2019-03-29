package main.functionality.helperControlers.hardware.RFID;

import staticHelpers.OtherHelpers;

public class RC522tester {
	
	public RC522tester()
	{
	    NFCReader nfc;
	    RaspRC522 rsp;
	    
	    rsp = new RaspRC522();
	    
	    OtherHelpers.sleepNonException(1000);
	    
	    rsp.RC522_Init();
	    
	    OtherHelpers.sleepNonException(1000);
	    
	    nfc = new NFCReader();
	    
	    OtherHelpers.sleepNonException(1000);
	    
        try {
			System.out.println(nfc.readBGMNum());
		} catch (InterruptedException e)
        {
			System.out.println("Error at reading BGM");
		}
    
	}
	

}
