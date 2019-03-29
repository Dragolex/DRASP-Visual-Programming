package functionality.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.Message;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import dataTypes.contentValueRepresentations.TextOnly;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import main.functionality.Functionality;
import main.functionality.helperControlers.network.TelegramControl;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Ev03_Telegram extends Functionality {

	public static int POSITION = 3;
	public static String NAME = "Telegram Bot";
	public static String IDENTIFIER = "EvenTeleNode";
	public static String DESCRIPTION = "Events related to a Telegram Bot.";

	//public static boolean DEACTIVATED = true;
	
	
	public static FunctionalityContent create_EvTeleCommand()
	{
		Object[] params = new Object[3];
		
		Integer[] index = new Integer[1];
		index[0] = -1;
		
		String[] actualCommand = new String[1];
		
		ProgramEventContent[] content = new ProgramEventContent[1];
		
		
		return(content[0] = new ProgramEventContent("EvTeleCommand",
				params,
				() -> {
					index[0] = -1;
				},
				() -> {
						TelegramControl controler = TGRMctrl;//((BotControler) params[0]); TODO: Use BotControler directly by moving all fucntions from TelegramControl
						
						
						if (index[0] == -1)
						{
							actualCommand[0] = (String) params[2];
							
							if (!content[0].hasOptionalArgument(0))	
								index[0] = controler.addCommand((actualCommand[0]).split("\\|"));
							else
							{
								List<String> commands = new ArrayList<>();
								
								commands.addAll( Arrays.asList( (actualCommand[0]).split("\\|") ) );
								
								int len = content[0].getTotalOptionalOrExpandedArgumentsCount();
								
								
								for(int i = 2; i < len; i++) // Ignore the default standard events
									commands.addAll( Arrays.asList( ((String) content[0].getTotalOptionalOrExpandedArgumentsArray()[i]).split("\\|") ));
								
								index[0] = controler.addCommand(commands);
							}
						}
						else
						{
							if (!((String) params[2]).equalsIgnoreCase(actualCommand[0]))
							{
								controler.clearCommands(index[0]);
								index[0] = -1;
								return(false);
							}
							
							Message resp = controler.hasReceivedCommand(index[0]);
							if (resp != null)
								initVariableAndSet(params[1], Variable.teleChatMsg, resp);
							
							return(resp != null);
						}
						
						return(false);
					}));
	}
	public static ProgramElement visualize_EvTeleCommand(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Simple Command", "Launches the event if one of the given command texts have been sent to the bot by an user.\nNote: While the bot runs, messages are never missed (as long as internet connection is available).\nIf the command is sent multiple times, the event will always be executed as often one after another.");
		ev.addParameter(0, new VariableOnly(), "Bot Identifier", "Bot initialized by the corresponding action.");
		ev.addParameter(1, new VariableOnly(true, true), "Message Ident", "The new message will be stored here by an identifier.\nUse the corresponding actions to retrieve the actual text or the username.\nAnswering into the chat of the message is possible too (it won't quote).");
		ev.addParameter(2, new TextOrVariable(), "Command", "Possible command text to compare with. Note that the comparison is not case sensitive.\nThere are two ways to concat multiple variants of the command which should trigger this event:\n1. Using the optional argumenst via the '+' button.\n2. Separating them by '|'. For example: 'Lights on|Lamps on'.\nNote that therefore the '|' symbol is not allowed anywhere else in the text.");
		ev.setExpandableArgumentDescription(TextOnly.class, null, "Command #", 16, "Additional possible commands. To avoid excess computation, variables are not allowed in this case.");
		
		return(ev);
	}
	
	
	
	public static ProgramElement create_EvTeleCommandExt()
	{
		Object[] params = new Object[3];
		
		Integer[] index = new Integer[1];
		index[0] = -1;
		
		String[] actualCommand = new String[1];
		
		ProgramEventContent[] content = new ProgramEventContent[1];		
		
		return(content[0] = new ProgramEventContent("EvTeleCommandExt",
				params,
				() -> {
					index[0] = -1;
				}
				,
				() -> {
						TelegramControl controler = TGRMctrl;//(TelegramControl) params[0];
						
						if (index[0] == -1)
						{
							actualCommand[0] = (String) params[2];
							
							index[0] = controler.addCommand((actualCommand[0]).split("\\|"));
						}
						else
						{
							if (!((String) params[2]).equalsIgnoreCase(actualCommand[0]))
							{
								index[0] = -1;
								return(false);
							}
							
							Message resp = controler.checkReceivedCommandAndSetvariables(index[0], 2, content[0].getTotalOptionalOrExpandedArgumentsArray());
							
							if (resp != null)
								initVariableAndSet(params[1], Variable.teleChatMsg, resp);
							
							return(resp != null);
						}
						
						return(false);
					}));
	}
	public static ProgramElement visualize_EvTeleCommandExt(FunctionalityContent content)
	{
		VisualizableProgramElement ev;
		ev = new VisualizableProgramElement(content, "Argumented Command", "Launches the event if one of the given command texts\noptionally containing arguments,\nhave been sent to the bot by an user.\nNote: While the bot runs, messages are never missed (as long as internet connection is available).\nIf the command is sent multiple times, the event will always be executed as often one after another.");
		ev.addParameter(0, new VariableOnly(), "Bot Identifier", "Bot initialized by the corresponding action.");
		ev.addParameter(1, new VariableOnly(true, true), "Message Ident", "The new message will be stored here by an identifier.\nUse the corresponding actions to retrieve the actual text or the username.\nAnswering into the chat of the message is possible too (it won't quote).");
		ev.addParameter(2, new TextOrVariable(), "Command", "Command text to compare with. Note that the comparison is not case sensitive.\nArguments are allowed.\nThat means the command may contain placeholders in the form '[A#]'.\n'A' has to be the letter 'N' for a number argument or 'T' for a text argument.\n# has to be 0 or a positive number (can have multiple digits) and describes the index of the result variable.\nWhatever word or number the user writes for the palceholders, will be stored in the corresponding variable.\n\nThere are two ways to concat multiple variants of the command which should trigger this event:\n1. Using the optional arguments via the '+' button.\n2. Separating them by '|'. For example: 'Lights on|Lamps on'.\nNote that therefore the '|' symbol\nis not allowed anywhere else in the text.");
		ev.setExpandableArgumentDescription(VariableOnly.class, null, true, true, "Parameter #", 16, "Variables where the parameters will be stored in.");
		
		return(ev);
	}
	
	
	
	
}


