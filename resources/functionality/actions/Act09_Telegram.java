package functionality.actions;

import org.telegram.telegrambots.meta.api.objects.Message;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import main.functionality.Functionality;
import main.functionality.helperControlers.network.BotControler;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Act09_Telegram extends Functionality {

	public static int POSITION = 9;
	public static String NAME = "Telegram Bot";
	public static String IDENTIFIER = "ActTeleNode";
	public static String DESCRIPTION = "Actions related to telegram bots.";
	
	//public static boolean DEACTIVATED = true;

	
	public static ProgramElement create_ElTeleBot()
	{
		// PWM value set over time between two values
		Object[] params = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElTeleBot",
				params,
				() -> {
					
						initVariableAndSet(params[0], Variable.teleBot, TGRMctrl.createBot((String) params[1], (String) params[2]) );
						
					});
		return(content[0]);
	}
	public static ProgramElement visualize_ElTeleBot(FunctionalityContent content)
	{
		VisualizableProgramElement elBotVis;
		elBotVis = new VisualizableProgramElement(content, "Connect Bot", "Connect a Telegram Bot.\nThe telegram bot is an automated\n'user' you can talk to via the messenegr app 'Telegram'.\nWhen you or anyone talks to this bot\nwhich will be found via the search function, ocne created,\nthe messages are sent to your DRASP software\nand youc an work with it.\nEvents interpreting the message are available\nand can be used for remote-controling whatever you want using the bot.\nTo register a bot (compeltely free) you have to\nfollow the little instruction on their homepage (see below).");
		elBotVis.addParameter(0, new VariableOnly(true, true), "Bot Ident", "A variable which will be used in other functions to refer to this bot.");
		elBotVis.addParameter(1, new TextOrVariable("___Bot"), "Bot Name", "How you named your bot when registering it.");
		elBotVis.addParameter(2, new TextOrVariable("375391311:AAGl5d5Ah9-8WoxQHE2UpHu7-X-Vf61I4Ng"), "Token", "Token created with those official instructions:\nhttps://core.telegram.org/bots#6-botfather");
		return(elBotVis);
	}
	
	
	
	public static ProgramElement create_ElTeleGetName()
	{
		// PWM value set over time between two values
		Object[] params = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElTeleGetName",
				params,
				() -> {
					
						initVariableAndSet(params[0], Variable.teleChatMsg, TGRMctrl.getChatUsername( (Message) params[1]) );
						
					});
		return(content[0]);
	}
	public static ProgramElement visualize_ElTeleGetName(FunctionalityContent content)
	{
		VisualizableProgramElement elBotVis;
		elBotVis = new VisualizableProgramElement(content, "Get Username", "Get the username of a chat-message\nprovided by a telegram bot event.");
		elBotVis.addParameter(0, new VariableOnly(true, true), "Username", "The name of the user will be assigned to this variable.");
		elBotVis.addParameter(1, new VariableOnly(), "Message Ident", "Identifier of the chat message gvien by the event.");
		return(elBotVis);
	}
	
	
	public static ProgramElement create_ElTeleGetCont()
	{
		// PWM value set over time between two values
		Object[] params = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElTeleGetCont",
				params,
				() -> {
					
						initVariableAndSet(params[0], Variable.textType, ((Message) params[1]).getText() );
						
					});
		return(content[0]);
	}
	public static ProgramElement visualize_ElTeleGetCont(FunctionalityContent content)
	{
		VisualizableProgramElement elBotVis;
		elBotVis = new VisualizableProgramElement(content, "Get Content", "Get the text which is contained by the given message provided by a telegram bot event.");
		elBotVis.addParameter(0, new VariableOnly(true, true), "Content", "The message text will be assigned to this variable.");
		elBotVis.addParameter(1, new VariableOnly(), "Message Ident", "Identifier of the chat message gvien by the event.");
		return(elBotVis);
	}
	
	
	
	public static ProgramElement create_ElTeleSend()
	{
		// PWM value set over time between two values
		Object[] params = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElTeleSend",
				params,
				() -> {
					
						TGRMctrl.sendResponseMessage( (BotControler) params[0], (Message) params[1], (String) params[2]);
						
					});
		return(content[0]);
	}
	public static ProgramElement visualize_ElTeleSend(FunctionalityContent content)
	{
		VisualizableProgramElement elBotVis;
		elBotVis = new VisualizableProgramElement(content, "Respond Message", "Send a response message to the person who has sent the given message to the bot.\nYou will receive messages through a telegram bot event.");
		elBotVis.addParameter(0, new VariableOnly(), "Bot Ident", "Identifier of the bot.");
		elBotVis.addParameter(1, new VariableOnly(), "Message Ident", "The message to respond to.");
		elBotVis.addParameter(2, new TextOrVariable(), "Response", "The text to respond with.");
		
		return(elBotVis);
	}
	
	
	
	
}
