package functionality.conditions;

import java.util.Random;

import dataTypes.FunctionalityConditionContent;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.GuiMsgHelper;

public class Cond00_Basic extends Functionality {

	public static int POSITION = 0;
	public static String NAME = "Basic";
	public static String IDENTIFIER = "CondMiscNode";
	public static String DESCRIPTION = "Various conditions for general purposes.";
	
	
	public static ProgramElement create_StElse()
	{
		return(new FunctionalityContent("StElse", ElseClause, 0));	
	}
	public static ProgramElement visualize_StElse(FunctionalityContent content)
	{	
		return(new VisualizableProgramElement(content, "Else", "Continue here if the last If condition returned false."));
	}
		
	static public FunctionalityContent create_ConAskUser()
	{
		Object[] params = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityConditionContent( "ConAskUser",
				params,
				() -> {
						if (content[0].hasOptionalArgument(0))
						{
							if (content[0].getTotalOptionalOrExpandedArgumentsCount()>1)
							{
								String[] buttons = new String[content[0].getTotalOptionalOrExpandedArgumentsCount()-1];
								for(int i = 1; i < content[0].getTotalOptionalOrExpandedArgumentsCount(); i++)
									buttons[i-1] = (String) content[0].getTotalOptionalOrExpandedArgumentsArray()[i];
								
								double res = GuiMsgHelper.askQuestion((String) params[0], buttons, false);
								initVariableAndSet((Variable) content[0].getOptionalArgumentValue(0), Variable.doubleType, res);
								return(true);
							}
							else
							{								
								double res = GuiMsgHelper.askQuestion((String) params[0], new String[]{"Yes", "No"}, true);
								if (res == -1) res = 2;
								initVariableAndSet((Variable) content[0].getOptionalArgumentValue(0), Variable.doubleType, res);
								return(true);
							}
						}
						else
							return(0 == GuiMsgHelper.askQuestion((String) params[0], new String[]{"Yes", "No"}, false));
						
					}).removeFixedOptionalArguments();
		return(content[0]);
	}
	static public VisualizableProgramElement visualize_ConAskUser(FunctionalityContent content)
	{
		VisualizableProgramElement vv;
		vv = new VisualizableProgramElement(content, "Ask Question", "Shows a popup message asking the user a given question.\nDefault are binary questions. If the user presses 'Yes',\nthe child-element-block will be executed.\nOtherwise the Else-block will.\n\nUsing the optional parameters\nyou can define your own results\nand they will be placed in a variable.");
		vv.addParameter(0, new TextOrVariable("Yes or no?"), "Question", "The question Text. Use '\\n' as a new-line symbol.");
		vv.addOptionalParameter(0, new VariableOnly(true, true), "Response Index", "Index of the button pressed by the user placed in a variable.\nThe child-elements will always be executed 8and never the else block)\nbut you can react accordingly using the value from this variable.\nNote if you provide this argument but no additional buttons,\nthe default will be 'Yes', 'No', 'Cancel'");
		vv.setExpandableArgumentDescription(TextOrVariable.class, null, false, false, "Button #", 8, "Up to 8 buttons which will be shown in the popup.");
		return(vv);
	}
	
	
	// Random based
	static public FunctionalityContent create_ConIfLucky()
	{
		Object[] params = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityConditionContent( "ConIfLucky",
				params,
				() -> {
						Random rn = randomizer;
						if (content[0].hasOptionalArgument(3))
							rn = (Random) content[0].getOptionalArgumentValue(0);
					
						return(rn.nextDouble() > (((double) params[0])/100));
					});
		return(content[0]);
	}
	static public VisualizableProgramElement visualize_ConIfLucky(FunctionalityContent content)
	{
		VisualizableProgramElement vv;
		vv = new VisualizableProgramElement(content, "Lucky", "Executes nested actions with a given probability.\nFor example if using the value '50'\nit will execute every second time.\nYou can use the ELSE structure element with this too.");
		vv.addParameter(0, new ValueOrVariable(), "Probability (%)", "Probability between 0 to 100.");
		vv.addOptionalParameter(0, new VariableOnly(), "Randomizer Ident", "Identifier to a randomizer created with the corresponding action.\nThis makes sense if you want to set a fixed seed to the random-generator.");
		return(vv);
	}
	
	
}


