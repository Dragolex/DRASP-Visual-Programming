package main.functionality.helperControlers.network;

// For Telegram Bot
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;



public class BotControler extends TelegramLongPollingBot
{
	private String botName = "";
	private String botToken = "";
	
	private TelegramControl controler;
	private BotSession apiBot;
	
	public BotControler(String botName, String botToken, TelegramControl controler)
	{
		this.botName = botName;
		this.botToken = botToken;
		this.controler = controler;
		
		System.out.println("BOT NAME: " + botName);
		System.out.println("BOT TOKEN: " + botToken);
	}
	
	public void setNewControler(TelegramControl controler)
	{
		this.controler = controler;
	}

	@Override
	public String getBotUsername()
	{
		return(botName);
	}
	

	@Override
	public String getBotToken()
	{
		return(botToken);
	}
	
	

	@Override
	public void onUpdateReceived(Update update)
	{
		Message message = update.getMessage();
		
		System.out.println("Received message from: " + message.getFrom().getFirstName() +"\nContent: " + message.getText());
		
		controler.interpretCommand(message);
		
		//String response = controler.handleCommand(text);	
		//sendMessage(response, update.getMessage().getChatId() );
	}
	
	public void sendMessageDirect(String text, Long chatId)
	{
		SendMessage sendMessage = new SendMessage().setChatId( chatId );
		sendMessage.setText(text);
		
		try
		{
			execute(sendMessage);			
			// Old version: sendMessage(sendMessage);
		}
		catch (TelegramApiException e)
		{
			System.out.println("Sending the response failed!");
		}		
		
	}
	
	@Override
	public String toString()
	{
		return("BOT:"+botName+":"+botToken);
	}

	public void setApiBot(BotSession apiBot)
	{
		this.apiBot = apiBot;
	}
	public BotSession getApiBot()
	{
		return(apiBot);
	}

	
	
	
	

}
