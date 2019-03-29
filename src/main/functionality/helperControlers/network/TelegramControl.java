package main.functionality.helperControlers.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotSession;


import dataTypes.minor.Tripple;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import main.functionality.helperControlers.HelperParent;

public class TelegramControl extends HelperParent
{
	static boolean oneTimeContextInitialized = false;
	
	private List<BotControler> bots;
	private List<BotSession> sessions;
	
	private static Map<String, BotControler> persistentBotSessions = new HashMap<>();
	
	
	
	private List<Pattern> commandPatterns;
	private List<List<Integer>> commandInnerIndices;
	private List<List<Integer>> commandInnerIndicesType;
	private List<Integer> activatedCommandsIndex;
	
	private int[] activatedCommands;
	private Message[] activatedMessage;
	private Object[][] activatedOutputValues;
	private Object[][] activatedOutputTypes;
	private int usedCommands = 0;
	
	
	private final int initCommandsSize = 8;
	private int commandsSize = initCommandsSize;
	private final int paramSize = 16;
	
	
	@Override
	protected void start()
	{
		ApiContextInitializer.init();
		
		bots = new ArrayList<BotControler>();
		sessions = new ArrayList<BotSession>();
		
		commandPatterns = new ArrayList<>();
		commandInnerIndices = new ArrayList<>();
		commandInnerIndicesType = new ArrayList<>();
		activatedCommandsIndex = new ArrayList<>();
		
	
		activatedCommands = new int[commandsSize];
		activatedMessage = new Message[commandsSize];
		activatedOutputValues = new Object[commandsSize][paramSize];
		activatedOutputTypes = new Object[commandsSize][paramSize];
	
		resizeCommandsStorage(commandsSize);
	}
	
	public void resizeCommandsStorage(int newSize)
	{
		if (newSize != commandsSize)
		{
			int[] newActivatedCommands = new int[newSize];
		    System.arraycopy(activatedCommands, 0, newActivatedCommands, 0, commandsSize);
			
		    Message[] newActivatedMessage = new Message[newSize];
		    System.arraycopy(activatedMessage, 0, newActivatedMessage, 0, commandsSize);
			
		    Object[][] newActivatedOutputValues = new Object[newSize][paramSize];
		    System.arraycopy(activatedOutputValues, 0, newActivatedOutputValues, 0, commandsSize);
	
		    Object[][] newActivatedOutputTypes = new Object[newSize][paramSize];
		    System.arraycopy(activatedOutputTypes, 0, newActivatedOutputTypes, 0, commandsSize);
	
			commandsSize = newSize;
		}
		
		for(int i = 0; i < commandsSize; i++)
			for(int j = 0; j < paramSize; j++)
				activatedOutputValues[i][j] = null;
	}
    
	
	
	
	// "myLedTestBot", "375391311:AAGWBLQULRGQ3vXJYXEtNc_uhNEd8-k5384"
	// new: "myLedTestBot", "375391311:AAGl5d5Ah9-8WoxQHE2UpHu7-X-Vf61I4Ng"
	
	public BotControler createBot(String name, String token)
	{
		startIfNeeded();
		BotControler bot = null;
		
		try
		{
			bot = new BotControler(name, token, this);
			
			if (!oneTimeContextInitialized)
				ApiContextInitializer.init();
			oneTimeContextInitialized = true;
			
			
			
			if (persistentBotSessions.containsKey(bot.toString()))
			{
				BotControler existingBot = persistentBotSessions.get(bot.toString());
				sessions.add(existingBot.getApiBot());
				existingBot.setNewControler(this);
				bots.add(bot);
			}
			else
			try
			{
				bots.add(bot);
				
				TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
				
				BotSession apiBot = telegramBotsApi.registerBot(bot);
				bot.setApiBot(apiBot);
				
				sessions.add(apiBot);
				persistentBotSessions.put(bot.toString(), bot);
			}
			catch (TelegramApiRequestException e)
			{
				Execution.setError("Registering the Telegram Bot '" + name + "'failed!\nGiven token: " + token + "\n\nTelegram error message:\n\n" + e.getMessage(), false);
			}
			
			
		}
		catch (Exception e)
		{
			Execution.setError("Registering the Telegram Bot '" + name + "'failed!\nGiven token: " + token + "\n\nTelegram error message:\n\n" + e.getMessage(), false);
		}
			
		
		for(int i = 0; i < activatedCommands.length; i++)
			activatedCommands[i] = 0;
		
		return(bot);
	}
	
	
	
	// Returns the used index
	public synchronized int addCommand(String[] texts)
	{
		for(String text: texts)
			_addCommand(text, usedCommands);

		usedCommands++;
		return(usedCommands-1);
	}
	public synchronized int addCommand(List<String> texts)
	{
		for(String text: texts)
			_addCommand(text, usedCommands);

		usedCommands++;
		return(usedCommands-1);
	}
	
	public synchronized int addCommand(String text)
	{
		_addCommand(text, usedCommands);
		
		usedCommands++;		
		return(usedCommands-1);
	}
	

	public void clearCommands(int index)
	{
		boolean again = true;
		while(again)
		{
			again = false;
			
			for(int ind = 0; ind < activatedCommandsIndex.size(); ind++)
			{
				if (((int) activatedCommandsIndex.get(ind)) == index)
				{
					activatedCommandsIndex.remove(ind);
					commandPatterns.remove(ind);
					commandInnerIndices.remove(ind);
					commandInnerIndicesType.remove(ind);
					
					again = true;
					break;
				}
			}
		}
	}
	
	
	public synchronized Message hasReceivedCommand(int index)
	{
		if (activatedCommands[index] > 0)
		{
			activatedCommands[index]--;
			return(activatedMessage[index]);
		}
		return(null);
	}
	
	public synchronized Message checkReceivedCommandAndSetvariables(int index, int offset, Object[] variables)
	{
		if (activatedCommands[index] > 0)
		{
			activatedCommands[index]--;
			
			Object[] outputValues = activatedOutputValues[index];
			Object[] outputTypes = activatedOutputTypes[index];
			
			for(int i = 0; i < Math.min(paramSize, variables.length-offset); i++)
				if (outputValues[i] != null)
					initVariableAndSet(variables[i+offset], (int) outputTypes[i], outputValues[i]);	
			
			return(activatedMessage[index]);
		}
		return(null);
	}
	
	
	public String getChatUsername(Message message)
	{
		return(message.getFrom().getUserName());
	}
	
	public void sendResponseMessage(BotControler bot, Message oldMessage, String responseText)
	{
		bot.sendMessageDirect(responseText, oldMessage.getChatId());
	}
	
	
	
	
	private void _addCommand(String text, int index)
	{
		if (index >= commandsSize)
			resizeCommandsStorage(commandsSize * 2);
		
		Tripple<String, List<Integer>, List<Integer>> resolvingResult = resolveCommandText(text.toLowerCase().trim());

		commandPatterns.add(Pattern.compile(resolvingResult.first));
		commandInnerIndices.add(resolvingResult.second);
		commandInnerIndicesType.add(resolvingResult.third);
		
		activatedCommandsIndex.add(index);
	}
	
	
	Object[] tempStorage;// = new Object[paramSize];
	
	protected synchronized boolean interpretCommand(Message message)
	{
		String text = message.getText().toLowerCase();
		text.trim();
		
		
		// Go through all resolved commands and attempt to match
		for (int i = 0; i < commandPatterns.size(); i++)
		{
			Matcher m = commandPatterns.get(i).matcher(text);

			if (m.find() && (m.start()==0) && (m.end()==text.length())) // Full match found
			{
				boolean trueMatch = true;
				
				//System.out.println("Command found: " + text);
				
				int elements = m.groupCount();
				//System.out.println("The command has the following " + elements + " elements:");
				
				List<Integer> innerIndices = commandInnerIndices.get(i);
				List<Integer> innerIndicesType = commandInnerIndicesType.get(i);
				
				int outerInd = activatedCommandsIndex.get(i);
				
				
				Object[] tempStorage = Arrays.copyOf(activatedOutputValues[outerInd], paramSize);
				
				for(int j = 0; j < paramSize; j++)
					activatedOutputValues[outerInd][j] = null;
				
				
				for(int j = 0; j < elements; j++)
				{
					String element = m.group(j+1);
					
					int type = innerIndicesType.get(j);
					
					// Todo: Use a less dirty method to check whether number is parseable
					// using  NumberUtils.isCreatable from Apache.lang is an option but requries the extra library
					try  
					{  
						// If it is a parseable number
						double val = Double.parseDouble(element);
						
						if (type != Variable.doubleType)
						{
							//System.out.println("Expecting number, found text!");
							trueMatch = false;
						}
						else
						{
							activatedOutputValues[outerInd][innerIndices.get(j)] = val;
							activatedOutputTypes[outerInd][innerIndices.get(j)] = Variable.doubleType;
						}
						
						//System.out.println("Number: " + element);
					}  
					catch(NumberFormatException nfe) // otherwise add as is being a string (keyword)
					{
						if (type != Variable.textType)
						{
							//System.out.println("Expecting text, found number!");
							trueMatch = false;
						}
						else
						{
							activatedOutputValues[outerInd][innerIndices.get(j)] = element;
							activatedOutputTypes[outerInd][innerIndices.get(j)] = Variable.textType;
						}
						
						//System.out.println("Keyword: " + element);
					}
				}
				
				if (trueMatch)
				{
					activatedMessage[outerInd] = message;
					activatedCommands[outerInd]++;
					
					return(true);
				}
				else
					activatedOutputValues[outerInd] = tempStorage;

			}
		}
		
		return(false);
	}


	
	
	
	
	
	
	protected void reset()
	{
		commandPatterns.clear();
		commandInnerIndices.clear();
		commandInnerIndicesType.clear();
		activatedCommandsIndex.clear();

		for(int i = 0; i < activatedCommands.length; i++)
			activatedCommands[i] = 0;
		
		resizeCommandsStorage(initCommandsSize);
		
		try
		{
			// For an unknown reason (despite bug tracker) this can take up to 50 seconds
			//for(BotSession session: sessions)
			//	session.stop();
			
			for(BotControler bot: bots)
				bot.clearWebhook();
			
			if (!sessions.isEmpty() || !bots.isEmpty())
				InfoErrorHandler.printExecutionInfoMessage("Closing Telegram Bots.");
		}
		catch (TelegramApiRequestException e)
		{
			InfoErrorHandler.printExecutionErrorMessage("Closing a telegram session failed.\nException AP response:\n" + e.getApiResponse()+"\nException error:\n" + e.getMessage());
		}
		
		usedCommands = 0;
		
		sessions.clear();
		bots.clear();
	}

	protected void quit()
	{
		reset();
	}
	
	
	
	
	

	
	static char startChar = "[".charAt(0);
	static char endChar = "]".charAt(0);
	static String repStr = "(\\S+)";
	
	public static Tripple<String, List<Integer>, List<Integer>> resolveCommandText(String text)
	{
		String resText = "";
		List<Integer> contents = new ArrayList<>();
		List<Integer> contentTypes = new ArrayList<>();
		
		int lastEnd = 0;
		
		int i;
		for (i = 0; i < text.length(); i++)
		{
			if (text.charAt(i) == startChar)
			{
				resText = resText + text.substring(lastEnd, i) + repStr;
				lastEnd = i;
			}
			
			if (text.charAt(i) == endChar)
			{
				String substr = text.substring(lastEnd+1,i);
				
				contents.add(Integer.parseInt(substr.substring(1)));
				
				if (substr.startsWith("n"))
					contentTypes.add(Variable.doubleType);
				else
				if (substr.startsWith("t"))	
					contentTypes.add(Variable.textType);
				else
				{
					contentTypes.add(Variable.textType);
					Execution.setError("Trying to set a telegram command paameter with an invalid letter.\nOnly T(=Text) or N(=Number) are allowed after the '[' symbol.\nGiven command was: " + text, false);
				}
				
				lastEnd = i+1;
			}
						
		}
		
		resText = resText + text.substring(lastEnd, i);
		
		return(new Tripple<String, List<Integer>, List<Integer>>(resText, contents, contentTypes));
	}
	

}
