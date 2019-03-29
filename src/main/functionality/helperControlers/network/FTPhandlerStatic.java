package main.functionality.helperControlers.network;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;


public class FTPhandlerStatic {
	

	public static String download(String hostname, String username, String password, String target, String remote)
	{
		String error = "";
        FTPClient ftpClient = null;
        try {	
        	ftpClient = new FTPClient();
        	
            ftpClient.connect(hostname, 21);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
        	
        	// Download via stream
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(target));
            boolean success = ftpClient.retrieveFile(remote, outputStream1);
            outputStream1.close();
            
            
            if (!success)
            	error = "Downloading from FTP was not successful!\nFile missing?";
 
        } catch (IOException ex) {
        	error = "Downloading from FTP was not successful!\nError: " + ex.getMessage();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (Exception ex) {
            	error = "Quitting after downloading from FTP was not successful!\nError: " + ex.getMessage();
            }
        }
		
        return(error);
	}

	public static String upload(String hostname, String username, String password, String source, String remote)
	{
		String error = "";
		
		FTPClient ftpClient = null;
        try {
        	ftpClient = new FTPClient();
        	
            ftpClient.connect(hostname, 21);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 

            InputStream inputStream = new FileInputStream(new File(source));
 
            boolean done = ftpClient.storeFile(remote, inputStream);
            inputStream.close();
            
            if (!done)
            	error = "Uploading to FTP was not successful!\nFile missing?";
            
            
        } catch (IOException ex) {
        	error = "Uploading to FTP was not successful!\nError: " + ex.getMessage();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
            	error = "Quitting after uploading to FTP was not successful!\nError: " + ex.getMessage();
            }
        }
        
        return(error);
	}
	
	

}
