package main.functionality.helperControlers.network.TCP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import execution.Execution;
import main.functionality.SharedComponents;
import main.functionality.helperControlers.network.ConnectionInterface;
import main.functionality.helperControlers.network.TransmissionMessage;
import settings.GlobalSettings;
import staticHelpers.OtherHelpers;


public class TCPconnection extends SharedComponents implements ConnectionInterface
{
	static final int port = 80;//6789+1;
	
	
	static public void openForConnection() throws IOException
	{
		ServerSocket inputSocket = new ServerSocket(port);
		CONctrl.addDummyConnection(new TCPconnection(inputSocket));
		
		while (Execution.isRunning())
		{
			Socket connectionSocket;
			try
			{
				connectionSocket = inputSocket.accept();
			}
			catch(IOException e)
			{
				OtherHelpers.sleepNonException(GlobalSettings.constantCheckDelay);
				inputSocket = new ServerSocket(port);
				CONctrl.addDummyConnection(new TCPconnection(inputSocket));
				continue;
			}
			
			ConnectionInterface connection = new TCPconnection(connectionSocket, CONctrl.deviceIdentifier);
			
			CONctrl.openFromExernal(connection);
		}
		
		if (!inputSocket.isClosed())
			inputSocket.close();
		
		/*
		}
		catch(IOException e)
		{
			Execution.setError("Offering or maintaining a TCP connection failed. Error: " + e.getMessage(), false);
		}
		*/
	}
	
	static public String establishConnection(String targetAddress, String toDeviceIdentifier, boolean biDirectional) throws IOException, UnknownHostException
	{
		if (CONctrl.connectionExists(toDeviceIdentifier, true))
			return(null); // if a connection already exists, abort
		
		Socket clientSocket = new Socket(targetAddress, port);
		
		TCPconnection connection = new TCPconnection(clientSocket, CONctrl.deviceIdentifier);	    
		
		return(CONctrl.connect(connection, toDeviceIdentifier, biDirectional));		
	}
	
	
	
	Socket socket;
	ServerSocket serverSocketForClosing;
	
	DataInputStream reader;
	DataOutputStream writer;
	
	String deviceIdentifier;
	int deviceIdentifierLength;
	
	// Device identifier + Event identifier
	// Allow parameters
	
	
	private TCPconnection(Socket socket, String deviceIdentifier) throws IOException
	{
		this.socket = socket;
		this.deviceIdentifier = deviceIdentifier;
		this.deviceIdentifierLength = deviceIdentifier.length();
		
		reader = new DataInputStream(socket.getInputStream());
		writer = new DataOutputStream(socket.getOutputStream());
	}	
	
	private TCPconnection(ServerSocket serverSocketForClosing)
	{
		this.serverSocketForClosing = serverSocketForClosing;
	}
	
	public void out(String data, String toDeviceIdentifier) throws IOException
	{
		//writer.writeInt(data.length());// .writeBytes(data);
		/*
		if(sendOwnIdentifier)
			writer.writeUTF(deviceIdentifier + data);// .writeChars(data);
		else
		*/
		String sendText = TransmissionMessage.getAsMessage(deviceIdentifier, toDeviceIdentifier, data);
		writer.writeUTF(sendText);
	}
	
	public void outPure(String data) throws IOException
	{
		writer.writeUTF(data);
	}
	
	public void fileOut(String fileName) throws IOException
	{
		writer.writeUTF(fileName);// .writeChars(data);
		// TODO
		//writer.writeByte(v); // write the file
	}
	
	public TransmissionMessage in(boolean verifySource) throws IOException
	{
		String inText = reader.readUTF();
		TransmissionMessage msg = new TransmissionMessage(inText);
		//System.out.println("MSG: " + msg.toString());
		
		if (verifySource)
		{
			//System.out.println("Verifying own identifier: '"+deviceIdentifier + "' with received target: '" + msg.getToIdentifier() + "'.");  
			if (!msg.testToIdent(deviceIdentifier)) // test whether it is actually meant for our device
				return(null);
		}
		
		return(msg);
		
		
		/*
		if (msg.hasIdentifier())
		{
			String sender = msg.getIdentifier();
			
			if (sender.equals(deviceIdentifier))
			
		}
		else
			return(msg);
		
		
		String possibleIdent = data.substring(0, deviceIdentifierLength);
		int qInd = possibleIdent.indexOf("?");
		if (qInd == -1)
		{
			qInd = deviceIdentifierLength;
			
			if (!data.startsWith(deviceIdentifier))
				return(null);
			return(data.substring(deviceIdentifierLength));
		}
		
		if (!data.startsWith(data.substring(qInd)))
			return(null);
		
		int sepInd = data.indexOf("|");
		if (sepInd == -1)
			return(data);
		return(data.substring(sepInd));
		*/
		/*
		int length = reader.readInt();

		System.out.println("Received length: " +length);

		
		byte[] data = new byte[length];
		reader.read(data);
		
		
		return(String.valueOf(data));*/
		//return( reader.read(data));//  .readLine());
	}
	

	public void fileIn() throws IOException
	{
		String data = reader.readUTF();
		if (!data.startsWith(deviceIdentifier))
			return;
		
		// read file now and save it
		
		
		/*
		int length = reader.readInt();

		byte[] data = new byte[length];
		reader.readFully(data);
		
		return(String.valueOf(data));
		//return( reader.read(data));//  .readLine());	
		*/
	}
	
	
	public void close() throws IOException
	{
		if (serverSocketForClosing != null)
		{
			if (!serverSocketForClosing.isClosed())
				serverSocketForClosing.close();
			return;
		}

		
		try {
			reader.close();
			writer.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if (!socket.isClosed())
			socket.close();		
	}

	@Override
	public boolean isClosedOrInterrupted()
	{
		if (socket.isClosed())
			return(true);		
		return(false);
	}

	
}


