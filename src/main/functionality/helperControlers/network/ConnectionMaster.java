package main.functionality.helperControlers.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataTypes.ProgramEventContent;
import execution.Execution;
import main.functionality.helperControlers.HelperParent;
import main.functionality.helperControlers.network.TCP.TCPconnection;
import settings.GlobalSettings;
import staticHelpers.OtherHelpers;

public class ConnectionMaster extends HelperParent
{

	public final String globalCRYPT = null; // TODO: Use this

	public final char msgSep = '|';
	public final char identQ = '?';
	public final String identQstr = String.valueOf(identQ);

	
	public final String argSep = "|%|%|";

	public final String evPrefix = "|EV%EV|";
	public final String closedPrefix = "|CLOSED%CLOSED|";
	public final String newConPrefix = "|NEW%NEW|";	

	
	public final String successConfirmation = "Connected Successfully";	
	public final String connectionAttempt = "Want to connect";
	
	public String deviceIdentifier;
	
	
	public List<ProgramEventContent> events = new ArrayList<>(); // attached events
	
	public List<String> identifiers = new ArrayList<>();
	public List<ConnectionInterface> connections = new ArrayList<>();

	
	
	public void setDeviceIdentifier(String deviceIdentifier)
	{
		this.deviceIdentifier = deviceIdentifier;
	}
	
	
	public void passEventExecutor(ProgramEventContent eventContent)
	{
		events.add(eventContent);
	}

	
	@Override
	protected void start()
	{
	}
	
	
	
	public void send(String data, String toDeviceIdentifier) throws IOException
	{
		boolean sent = false;
		
		int qInd = -1;
		if (toDeviceIdentifier != null)
			qInd = toDeviceIdentifier.indexOf(identQ);
		
		if (qInd == -1)
		{
			int ind = 0;
			for(String ident: identifiers)
			{
				if ((toDeviceIdentifier == null) || ident.startsWith(toDeviceIdentifier))
				{
					connections.get(ind).out(data, toDeviceIdentifier);
					sent = true;
				}
				ind++;
			}
		}
		else
		{
			String subComp = toDeviceIdentifier.substring(0, qInd);
			
			int ind = 0;
			for(String ident: identifiers)
			{
				if (ident.startsWith(subComp))
				{
					connections.get(ind).out(data, toDeviceIdentifier);
					sent = true;
				}
				ind++;
			}
		}
		
		if (!sent)
			throw new IOException("A device matching the identifier '" + toDeviceIdentifier + "' does not exist!");
	}
	
	
	public void sendFile(String dataFile, String toDeviceIdentifier) throws IOException
	{
		/*
		if (toDeviceIdentifier == null)
		{
			for(ConnectionInterface con: connections)
				con.fileOut(dataFile);
			return;
		}
		
		int ind = identifiers.indexOf(toDeviceIdentifier);
		if (ind > -1)
			connections.get(ind).fileOut(dataFile);
		else
			System.out.println("A device with the identifier '" + toDeviceIdentifier + "' does not exist.");
		*/
	}
	
	
	private void receive(String from, String data)
	{
		Object[] dat = {from, data};
		
		for(ProgramEventContent cont: events)
			cont.triggerExternally(dat); // pass the input value and trigger
	}
	
	private void extOpen(String from)
	{
		Object[] dat = {from, newConPrefix};
		
		for(ProgramEventContent cont: events)
			cont.triggerExternally(dat); // pass the input value and trigger
	}
	
	private synchronized void extClosed(String from, ConnectionInterface conn)
	{
		int ind = connections.indexOf(conn);
		if (ind != -1)
		{		
			Object[] dat = {from, closedPrefix};
			
			try {
				conn.close();
			} catch (IOException e) {}
			
			identifiers.remove(ind);
			connections.remove(ind);
			
			for(ProgramEventContent cont: events)
				cont.triggerExternally(dat); // pass the input value and trigger
		}
	}

	

	@Override
	protected void reset()
	{
		try {
			for(ConnectionInterface conn: connections)
				conn.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		events.clear();
		identifiers.clear();
		connections.clear();
	}
	
	
	@Override
	protected void quit() {
		reset();
	}


	public void openFromExernal(ConnectionInterface connection) throws IOException
	{
		startIfNeeded();
		
		TransmissionMessage msg = connection.in(true); // retrieve the identifier
		
		String fromIdentifier = msg.getFromIdentifier();
		
		
		if (CONctrl.identifiers.contains(fromIdentifier))
		{
			connection.close();
			return; // if a connection already exists, abort
		}
		
		if (!CONctrl.connectionAttempt.equals(msg.getContent()))
		{
			Execution.setError("A device tried to connect but sent a wrong first message. Expected '" + CONctrl.connectionAttempt + "'\nReceived: '" + msg.getContent() +"'.", false);
			return;
		}
		
		CONctrl.connections.add(connection);			
		CONctrl.identifiers.add(fromIdentifier);
		
		OtherHelpers.sleepNonException(250);
		
		connection.out(CONctrl.successConfirmation, fromIdentifier); // confirm connection
		
		extOpen(fromIdentifier);
		
		startReceiving(connection, fromIdentifier);
	}
	
	
	public String connect(ConnectionInterface connection, String toDeviceIdentifier, boolean biDirectional) throws IOException
	{
		startIfNeeded();
		
		if (toDeviceIdentifier == null)
			connection.outPure(connectionAttempt);
		else
			connection.out(connectionAttempt, toDeviceIdentifier); // send the own identifier
				
		if (toDeviceIdentifier != null)
		{
			TransmissionMessage msg = connection.in(true);
			
			if (msg == null)
				throw new IOException("Connecting to a device with identifier '" + toDeviceIdentifier + "' has not been successful.");
			
			if (successConfirmation.equals(msg.getContent()))
			{
				connections.add(connection);
				identifiers.add(msg.getFromIdentifier());
			}
			else
				throw new IOException("Connecting to a device with identifier '" + toDeviceIdentifier + "' has not been successful.\nThe response was: '" + msg.getContent() + "'.");
			
			if (biDirectional)
				startReceiving(connection, msg.getFromIdentifier());
			
			return(msg.getFromIdentifier());
		}
		
		if (biDirectional)
			startReceiving(connection, toDeviceIdentifier);
		
		return(null);
	}
	
	
	private void startReceiving(ConnectionInterface connection, String originIdentifier)
	{
		new Thread(() -> {
			while(true)
			{
				try {
					if(!Execution.isRunning())
						return;
					
					OtherHelpers.sleepNonException(GlobalSettings.constantCheckDelay);
					
					if (Execution.isPaused())
						continue;
					
					if (connection.isClosedOrInterrupted())
					{
						System.out.println("BEEN ITNERRUPTED AA");
						extClosed(originIdentifier, connection);
						return;
					}
					
					TransmissionMessage input = connection.in(true);
					
					if (input == null)
						continue;
					
					if (Execution.isPaused() || !Execution.isRunning())
						continue;
										
					receive(originIdentifier, input.getContent());
				}
				catch (IOException e)
				{
					System.out.println("BEEN ITNERRUPTED BB");

					extClosed(originIdentifier, connection);
				}
				
				System.out.println("TRHEAD ENDED AA");

			}
		}).start();
	}
	
	
	public boolean connectionExists(String matcher, boolean exactComparison)
	{
		if (exactComparison)
		{
			if (!String.valueOf(identQ).equals(matcher))
				if (identifiers.contains(matcher))
					return(true);
		}
		else
			for(String ide: identifiers)
			{
				if (ide != null && !ide.isEmpty() && !ide.equals(deviceIdentifier))
				if (TransmissionMessage.matchIdentifiers(matcher, ide))
					return(true);
				}
		
		return(false);
	}


	public void closeConnection(String matcher, boolean exactComparison) throws IOException
	{
		if (exactComparison)
		{
			int ind = identifiers.indexOf(matcher);
			if (ind != -1)
			{
				connections.get(ind).close();
				connections.remove(ind);
				identifiers.remove(ind);
			}
			
			return;
		}
		
		List<String> toClose = new ArrayList<>();
		for(String ide: identifiers)
			if (TransmissionMessage.matchIdentifiers(matcher, ide))
				toClose.add(ide);
		
		for(String ide: toClose)
			closeConnection(ide, true);
	}
	
	
	public void addDummyConnection(TCPconnection con)
	{
		startIfNeeded();
		
		connections.add(con);
		identifiers.add(deviceIdentifier);
	}


}
