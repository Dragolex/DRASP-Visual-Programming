package dataTypes.minor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class possibleIP {
	
	String ip, host;
	InetAddress addr;
	Boolean reachable, sshConnectable;
	
	public possibleIP(String ip)
	{
		this.ip = ip;
		try {
			addr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {}
	}
	
	public boolean isReachable()
	{
		if (addr == null)
			return(false);
		
		if (reachable == null)
		{
			reachable = false;
			try {
				reachable = addr.isReachable(1000);
			} catch (IOException e) { }
		}
		
		return(reachable);
	}
	
	public boolean isSSHconnectable()
	{
		if (addr == null)
			return(false);
		
		if (sshConnectable == null)
		{
			sshConnectable = false;
			Socket s = null;
			try
			{
				s = new Socket(ip, 22);
				sshConnectable = true;
		    }
		    catch (Exception e) {}
	        if(s != null)
	        	try {s.close();} catch(Exception e){}
			
		}
		
		return(sshConnectable);
	}


	public String getIP()
	{
		return(ip);
	}
	public String getHost()
	{
		if (addr == null)
			return("NOT RETRIEVABLE");
		
		if (host == null)
			host = addr.getCanonicalHostName();
		return(host);
	}
	
}
