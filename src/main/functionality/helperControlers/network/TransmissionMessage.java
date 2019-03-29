package main.functionality.helperControlers.network;

import main.functionality.SharedComponents;

public class TransmissionMessage extends SharedComponents{
	
	String fromIdent = null;
	String toIdent = null;
	String content = null;
	
	public TransmissionMessage(String data)
	{
		int separatorPos1 = (int) data.charAt(0);
				
		if (data.charAt(separatorPos1) == CONctrl.msgSep) // the seperator has been found -> a sender has been used
		{
			int separatorPos2 = separatorPos1 + (int) data.charAt(separatorPos1+1) + 1;
			
			if (data.charAt(separatorPos2) == CONctrl.msgSep) // the seperator has been found -> a sender has been used
			{
				fromIdent = data.substring(1, separatorPos1);
				toIdent = data.substring(separatorPos1+2, separatorPos2);

				content = data.substring(separatorPos2+1);
				
				return;
			}
		}
		
		content = data;
	}
	
	
	public static String getAsMessage(String from, String to, String data)
	{
		StringBuilder str = new StringBuilder();
		
		int fromLen = from.length()+1;
		str.append((char) fromLen);
		str.append(from);
		str.append(CONctrl.msgSep);
		
		if (to != null)
		{
			int toLen = to.length()+1;
			str.append((char) toLen);
			str.append(to);
		}
		else
		{
			int toLen = 2;
			str.append((char) toLen);
			str.append(CONctrl.identQ);
		}
			
		str.append(CONctrl.msgSep);

		str.append(data);
		
		return(str.toString());
	}

	
	
	public boolean hasIdentifiers()
	{
		return(fromIdent != null);
	}
	
	public String getFromIdentifier()
	{
		return(fromIdent);
	}
	public String getToIdentifier()
	{
		return(toIdent);
	}
	
	public String getContent()
	{
		
		return(content);
	}

	public boolean testToIdent(String deviceIdentifier)
	{
		if (!hasIdentifiers())
			return(false);
		
		return(matchIdentifiers(toIdent, deviceIdentifier));
	}
	
	public static boolean matchIdentifiers(String matcher, String identifier)
	{		
		if (matcher == null || matcher.isEmpty())
			return(false);
		
		if (matcher.equals(identifier))
			return(true);
		
		int qInd = matcher.indexOf(CONctrl.identQ);
		
		if (qInd == 0) // everything allowed because matcher is just the ident symbol	
			return(true);
		
		if (qInd == -1)
			return(false);
		
		String sub = identifier.substring(0, qInd);
		return(identifier.startsWith(sub));
	}	

	
	
	public String toString()
	{
		return("From Ident: " + fromIdent + " To Ident: " + toIdent + " Content: " + content);
	}
}
