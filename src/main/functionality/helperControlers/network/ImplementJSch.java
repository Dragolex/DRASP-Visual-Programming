package main.functionality.helperControlers.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import execution.handlers.InfoErrorHandler;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;

public class ImplementJSch
{
	JSch jsch;
	
	String user;
	String host;
	String pass;

	
	Session session;
	Channel channel;
	
	
	public ImplementJSch(String username, String hostname, String password)
	{
		jsch = new JSch();
		
		user = username;
		host = hostname;
		pass = password;
	}
	
	
	public String connectSession()
	{
		// Session
		try {
			session = jsch.getSession(user, host, 22);
			
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(pass);
			session.connect();
		}
		catch (JSchException e)
		{
			if (e.getCause() instanceof ConnectException)
				return("The attempt to connect to the target timed out! Possibly wrong host or is down?");
			else
			if (e.getMessage().equalsIgnoreCase("Auth fail"))
				return("Authentification failed! Possibly wrong username or password?");
			else
				return("Connecting to the target failed for an untypical reason.\nCheck whether SSH is enabled on the target!\nError message: " + e.getMessage());
		}
		
		if (!session.isConnected())
		{
			return("Connecting to the target failed.\nCheck whether the target is online and SSH is enabled!");
		}
		
		return("");
	}
	
	public boolean executeCommand(String command)
	{
		InfoErrorHandler.printEnvironmentInfoMessage("Externally executing the following SSH command:\n" + command);
		
		try {
			// Channel
			channel = session.openChannel("exec");
		    ((ChannelExec)channel).setCommand(command);
		    
		    channel.setInputStream(null);
		    
		    channel.connect();		    
		    
		    return(true);
		}
		catch (JSchException e)
		{
			if (e.getMessage().contains("session is down"))
				System.out.println("The connected session to the target is down!\nCheck whether it is online and SSH is enabled!");
			else
				System.out.println("Executing the command failed for the following reason: " + e.getMessage());
		}
		
		return(false);
	}
	
	public InputStream getStandardStream()
	{
		try {
			return(channel.getInputStream());
		} catch (IOException e) { e.printStackTrace(); }
		return(null);
	}

	public InputStream getErrorStream()
	{
		try {
			return(channel.getExtInputStream());
		} catch (IOException e) { e.printStackTrace(); }
		return(null);
	}
	
	
	public String executeCommandAndRetrieve(String command)
	{
		if (!executeCommand(command))
			return(null);
		
	    String completeRes = "";
		try {
			completeRes = retrieveDoubleStream(channel.getInputStream(), channel.getExtInputStream(), channel);
		} catch (IOException e) {return(null);}
		
		completeRes = completeRes.replaceAll("\n\n", "\n");
		
		channel.disconnect();
		
		return(completeRes);
	}
	
	public Channel executeCommandAndGetChannel(String command)
	{
		if (!executeCommand(command))
			return(null);
		
		return(channel);
	}


	/*
	public static String retrieveDoubleStream(InputStream in1, InputStream in2, Process process)
	{
		return(retrieveDoubleStream(in1,in2,null, process));
	}*/
	public static String retrieveDoubleStream(InputStream in1, InputStream in2, Channel channel)
	{
		return(retrieveDoubleStream(in1,in2,channel, null));
	}

	public static String retrieveDoubleStream(InputStream in1, InputStream in2, Channel channel, Process process)
	{
		StringBuilder output = new StringBuilder();
		
		try
	    {
			byte[] tmp=new byte[1024];
			boolean lastWasText1 = false, lastWasText2 = false;
		    while(true)
		    {
		        while(in1.available()>0 || in2.available()>0)
		        {
		        
		        	if (in1.available()>0)
		        	{
				        int i=in1.read(tmp, 0, 1024);
				        if(i<0)break;
				          
				        String str = new String(tmp, 0, i);
				        if (str.length() > 0)
				        {
				        	if (lastWasText1)
				        		output.append("\n");
				        	lastWasText1 = true;
				        	output.append(str);
				        }
				        else lastWasText1 = false;
		        	}
	
		        	if (in2.available()>0)
		        	{
				        int i=in2.read(tmp, 0, 1024);
				        if(i<0)break;
				          
				        String str = new String(tmp, 0, i);
				        if (str.length() > 0)
				        {
				        	if (lastWasText2)
				        		output.append("\n");
				        	lastWasText2 = true;
				        	output.append(str);
				        }
				        else lastWasText2 = false;
		        	}
	
		          
		        }
		        
		        if ((channel == null) && (!process.isAlive()))
		        	break;
		        
		        if (channel != null)
		        if(channel.isClosed())
		        {
		        	if(in1.available()>0) continue; 
			        if(in2.available()>0) continue; 
			        
			        break;
		        }
		        try{Thread.sleep(100);}catch(Exception ee){}
		    }
		    
	    }
	    catch (IOException e){ return("");}
		
		String completeRes = output.toString().replaceAll("\n\n", "\n");
		if (completeRes.endsWith("\n"))
			completeRes = completeRes.substring(0, completeRes.length()-1);
		
		return(completeRes);
	}
	
	/*
	private String retrieveStream(InputStream in)
	{
		StringBuilder output = new StringBuilder();
		
		try
	    {
			byte[] tmp=new byte[1024];
			boolean lastWasText = false;
		    while(true)
		    {
		        while(in.available()>0)
		        {
		          int i=in.read(tmp, 0, 1024);
		          if(i<0)break;
		          
		          String str = new String(tmp, 0, i);
		          if (str.length() > 0)
		          {
		        	  if (lastWasText)
		        		  output.append("\n");
		        	  lastWasText = true;
			          output.append(str);
		          }
		          else lastWasText = false;
		        }
		        if(channel.isClosed()){
		          if(in.available()>0) continue; 
		          break;
		        }
		        try{Thread.sleep(1000);}catch(Exception ee){}
		    }
		    
	    }
	    catch (IOException e){ return("");}
		
		return(output.toString());
	}
	*/
	
	
	public boolean checkCommandRunning(boolean quitSessionAfterwards)
	{
		if (channel == null)
			return(false);
		if(channel.isClosed())
		{
		    channel.disconnect();
		    if (quitSessionAfterwards)
		    	session.disconnect();
		    return(false);
		}
		return(true);
	}


	public void close()
	{
		if (channel != null)
			channel.disconnect();
		if (session != null)
			session.disconnect();
	}


	public void activateDirectPrint(InputStream in)
	{
    	byte[] tmp=new byte[1024];

	    try
	    {	
		    while(true)
		    {
		        while(in.available()>0)
		        {
		          int i=in.read(tmp, 0, 1024);
		          if(i<0)break;
		          System.out.print(new String(tmp, 0, i));
		        }
		        if(channel.isClosed()){
		          if(in.available()>0) continue; 
		          System.out.println("exit-status: "+channel.getExitStatus());
		          break;
		        }
		        try{Thread.sleep(1000);}catch(Exception ee){}
		    }
	    }
	    catch (IOException e){}
	    
	    channel.disconnect();
	}
	
	
	public void transferFile(String origFile, String destFile)
	{
		System.out.println("Transfering file from: " + origFile + "\nTo: " + destFile);
		
		origFile = FileHelpers.adaptPathForLinux(origFile);
		
		String dr = FileHelpers.adaptPathForLinux(new File(destFile).getParent());
		executeCommandAndRetrieve("mkdir -p \"" + dr + "\"");
		
		destFile = FileHelpers.adaptPathForLinux(destFile);
		
		
		try
		{
			boolean keepAttributes = true;
	
		     // exec 'scp -t rfile' remotely
		    String command="scp " + (keepAttributes ? "-p" :"") +" -t \""+ destFile +"\"";
		    
		    Channel channel=session.openChannel("exec");
		    ((ChannelExec)channel).setCommand(command);
	
		    // get I/O streams for remote scp
		    OutputStream out=channel.getOutputStream();
		    InputStream in=channel.getInputStream();
	
		    channel.connect();
		    
		    if(checkAck(in)!=0)
		    	System.exit(0);
		    
		    File lfile = new File(origFile);
		    
		    if(keepAttributes)
		    {
		    	command="T"+(lfile.lastModified()/1000)+" 0";
		        // The access time should be sent here,
		        // but it is not accessible with JavaAPI ;-<
		        command+=(" "+(lfile.lastModified()/1000)+" 0\n"); 
		       
		        out.write(command.getBytes()); out.flush();
		        
		        if(checkAck(in)!=0)
		        	System.exit(0);
		    }
		    
		    // send "C0644 filesize filename", where filename should not include '/'
		    long filesize=lfile.length();
		    command="C0644 "+filesize+" ";
		    
		    if(origFile.lastIndexOf('/')>0)
		        command+=origFile.substring(origFile.lastIndexOf('/')+1);
		    else
		        command+=origFile;
	
		    command+="\n";
		    out.write(command.getBytes()); out.flush();
		    
		    if(checkAck(in)!=0)
		    	System.exit(0);
		    
	
		    // send a content of origFile
		    FileInputStream fis=new FileInputStream(origFile);
		    byte[] buf=new byte[1024];
		    
		    while(true)
		    {
		        int len=fis.read(buf, 0, buf.length);
		        if(len<=0) break;
		        out.write(buf, 0, len); //out.flush();
		    }
		    
		    fis.close();
		    fis=null;
		    
		    // send '\0'
		    buf[0]=0; out.write(buf, 0, 1); out.flush();
		    
		    if(checkAck(in)!=0)
		    	System.exit(0);
		      
		    out.close();
	
		    channel.disconnect();
		    
		}
		catch(Exception e) {}
		
	}
	
	static int checkAck(InputStream in) throws IOException{
	    int b=in.read();
	    // b may be 0 for success,
	    //          1 for error,
	    //          2 for fatal error,
	    //          -1
	    if(b==0) return b;
	    if(b==-1) return b;

	    if(b==1 || b==2){
	      StringBuffer sb=new StringBuffer();
	      int c;
	      do {
		c=in.read();
		sb.append((char)c);
	      }
	      while(c!='\n');
	      if(b==1){ // error
		InfoErrorHandler.printExecutionErrorMessage(sb.toString());
	      }
	      if(b==2){ // fatal error
	    InfoErrorHandler.printExecutionErrorMessage(sb.toString());
	      }
	    }
	    return b;
	  }
	
	
	
	public void transferDirectory(String sourcePath, String targetPath)
	{
		List<File> files = new ArrayList<>();
		FileHelpers.listFiles(sourcePath, files, true);
		
		String[] basePathParts = sourcePath.split("\\"+File.separator);
		
		int baseLength = 0;
		for(String part: basePathParts)
		{
			baseLength += part.length()+1;
		}
		baseLength--;
		
		if (!targetPath.endsWith(File.separator))
			targetPath = targetPath + File.separator;
		
		for(File f: files)
		{
			String origpt = f.getPath();
			String pt = origpt.substring(baseLength);
			
			transferFile(origpt, targetPath + pt);
		}
	}
	
	

	public void listFiles(String destinationDirectory, List<String> files, String filterEnding, boolean includeSubs)
	{
		destinationDirectory = FileHelpers.adaptPathForLinux(destinationDirectory);
		
		String response = executeCommandAndRetrieve("find \"" + destinationDirectory + "\"" + (includeSubs ? "" : " -maxdepth 1") +" -type f"); // List all files
		
		String[] fileStrs = response.split("\n");
		
		if (!destinationDirectory.endsWith("/"))
			destinationDirectory += "/";
		
		for(String ff: fileStrs)
			if (ff.length()>0)
				if (filterEnding.isEmpty() || ff.endsWith(filterEnding)) // -> has the correct ending
					files.add(ff); // -> Add to the list
	}
	
	
	public long getFileSize(String filePath)
	{
		filePath = FileHelpers.adaptPathForLinux(filePath);
		String response = executeCommandAndRetrieve("ls -nl \"" + filePath + "\" | awk '{print $5}'");
		return(Long.valueOf(response.replaceAll("\n", "")));
	}
	
	
	public long getLastFileModif(String filePath)
	{
		filePath = FileHelpers.adaptPathForLinux(filePath);
		String response = executeCommandAndRetrieve("expr `date +%s` - `stat -c %Y \"" + filePath + "\"`");
		return(Long.valueOf(response.replaceAll("\n", "")));
	}


	public void killCommand()
	{
		if (channel != null)
		{
			try {
				channel.disconnect();
				
				// Channel
				channel = session.openChannel("exec");
			    ((ChannelExec)channel).setCommand("sudo pkill -f java");
			    
			    channel.connect();
			    
				
				while(!channel.isConnected())
					Thread.sleep(GlobalSettings.constantCheckDelay);
				
				session.disconnect();				
				
			} catch (Exception e) { e.printStackTrace(); }
		}		
	}




	
}
