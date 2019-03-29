package functionality.structures;

import java.util.Random;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.LabelString;
import dataTypes.contentValueRepresentations.TermTextVarCalc;
import dataTypes.contentValueRepresentations.TermValueVarCalc;
import dataTypes.contentValueRepresentations.TextOrValueOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.FuncLabel;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;
import settings.GlobalSettings;
import staticHelpers.StringHelpers;

public class Stru01_Variable extends Functionality {

	public static int POSITION = 1;
	public static String NAME = "Variables";
	public static String IDENTIFIER = "StruVariableNode";
	public static String DESCRIPTION = "Structures for 'Variables'.\nThose are defined as a certain piece of data\nwhich can be a number, text oder identifier for a more complex kind of data.\nVariables can be used as parameters by writing '#' in front of them.\nThe parameter-field will display the same color wherever the same variable is used.";
	
	
	public static ProgramElement create_StSetBinVar()
	{
		Object[] setBoolVar = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent("StSetBinVar",
				setBoolVar,
				() -> {}
				,
				() -> {
					if (content[0].getOptionalArgTrue(0))
						initVariableAndSet(setBoolVar[0], Variable.boolType, !((boolean)setBoolVar[1]));
					else
						initVariableAndSet(setBoolVar[0], Variable.boolType, (boolean)setBoolVar[1]);
				}));		
	}
	public static ProgramElement visualize_StSetBinVar(FunctionalityContent content)
	{
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "Set/Flip Boolean", "Set a boolean value in a variable.\nThat means true or false. Booleans can be used for operations.\nTip: Set the same variable for 'Value' and use the optional argument\nto flip the boolean: 'True' turns 'False' and vica versa.");
		StSetNumVar.setArgumentDescription(0, new VariableOnly(true, true), "Variable");
		StSetNumVar.setArgumentDescription(1, new BooleanOrVariable(), "Value");
		StSetNumVar.addOptionalParameter(0, new BooleanOrVariable(), "NOT", "If true, the boolean will be negated.");
		return(StSetNumVar);
	}

	

	
	public static ProgramElement create_StSetNumVar()
	{
		Object[] setNumVar = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent("StSetNumVar",
				setNumVar,
				() -> {}
				,
				() -> {					
					Term term = ((Term) setNumVar[1]);

					Variable var = (Variable) setNumVar[0];
					
					if (var.getType() != Variable.doubleType)
						var.toNewType(Variable.doubleType);
					
					initVariableAndSet(setNumVar[0], Variable.doubleType, term.applyTo(var) );
					
					if (cont[0].hasOptionalArgument(0))
						((Variable) setNumVar[0]).setMinClamp((double) cont[0].getOptionalArgumentValue(0));
					if (cont[0].hasOptionalArgument(1))
						((Variable) setNumVar[0]).setMaxClamp((double) cont[0].getOptionalArgumentValue(1));
					
				}));
	}
	public static ProgramElement visualize_StSetNumVar(FunctionalityContent content)
	{
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "Set Number", "Set a number value in a variable.\nA new variable for a number value can be created this way.\nAllowed are all letters and the underscore symbol.\nCapitalization does not matter.\nThe value allows a comma or period to separate decimals.Example: 1.5  or 42,84.\n\nCalculations based on one symbol before '=' are allowed as well, including variables.\nExamples: +=30  or  *=15.5  or -=myVariable\nFor evaluating entire terms, use 'Calculate Term'.\nSpaces are generally irrelevant.");
		StSetNumVar.setArgumentDescription(0, new VariableOnly(true, true), "Variable");
		StSetNumVar.setArgumentDescription(1, new TermValueVarCalc(), "Value");
		StSetNumVar.addOptionalParameter(0, new ValueOrVariable(String.valueOf(-Double.MAX_VALUE)), "Clamp Min", "The smallest value this variable can take.\nIf attempting to set a smaller value in any way later, it will be limited to this.\nNote that you can overwrite this any time.");
		StSetNumVar.addOptionalParameter(1, new ValueOrVariable(String.valueOf(Double.MAX_VALUE)), "Clamp Max", "The largest value this variable can take.\nIf attempting to set a larger value in any way later, it will be limited to this.\nNote that you can overwrite this any time.");
		return(StSetNumVar);		
	}
	

	
	public static ProgramElement create_StSetTexVar()
	{
		Object[] setTexVar = new Object[2];
		return( new FunctionalityContent("StSetTexVar",
				setTexVar,
				() -> {
						
						Term term = ((Term) setTexVar[1]);
						
						Variable var = (Variable) setTexVar[0];
						
						if (var.getType() != Variable.textType)
							var.toNewType(Variable.textType);
					
						((Variable) setTexVar[0]).set( term.applyTo(var) );
						
					}));
	}
	public static ProgramElement visualize_StSetTexVar(FunctionalityContent content)
	{	
		VisualizableProgramElement StSetTexVar;
		StSetTexVar = new VisualizableProgramElement(content, "Set/Concat Text", "Set a text in a variable.\nA new variable for text can be created this way.\nAllowed are all letters and the underscore symbol.\nCapitalization does not matter.");
		StSetTexVar.setArgumentDescription(0, new VariableOnly(true, true), "Variable");
		StSetTexVar.addParameter(1, new TermTextVarCalc(true), "Text", "Similar to the 'Set Number-Variable' structure you can use '+' as the first symbol\nto concat the new string (or variable) the first 'Variable'.\nTo actually put the '+' symbol as a text, use '#+' as the first symbols.");
		return(StSetTexVar);
	}
	
	public static ProgramElement create_StReplaceText()
	{
		Object[] param = new Object[4];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent("StReplaceText",
				param,
				() -> {
						String res = getSTRINGparam(param[0]);
						
						String newValue;
						if (param[2] instanceof String)
							newValue = (String) param[2];
						else
							newValue = String.valueOf(GlobalSettings.doubleFormatter.format((double) param[2]));
						
						if (param[1] == null)
						{
							Execution.setError("A replacer or index is needed!", false);
							return;
						}
						
						if (param[1] instanceof String) // replacer is text
						{
							if (((String) param[1]).isEmpty())
							{
								Execution.setError("A replacer or index is needed!", false);
								return;
							}

							String repl = (String) param[1];
							
							if (cont[0].getTotalOptionalOrExpandedArgumentsCount() <= 1) // no additional new-value has been given.
							{
								int ocInd = -1;
								if (cont[0].hasOptionalArgument(0))
									ocInd = (int) getDoubleVariable(cont[0].getOptionalArgumentValue(0)); // get the index of the occurrence to replace (if specified)
								
								if (ocInd >= 0)
									res = StringHelpers.replaceNth(res, repl, ocInd, newValue); // replace nth occurrence
								else
									res = res.replace(repl, newValue); // replace all 
								
							}
							else // additional new-values given
							{
								res = res.replaceFirst(repl, newValue);
								
								for(int i = 1; i < cont[0].getTotalOptionalOrExpandedArgumentsCount(); i++)
								{
									Object v = cont[0].getTotalOptionalOrExpandedArgumentsArray()[i];
									if (v instanceof String)
										newValue = (String) v;
									else
										newValue = String.valueOf(GlobalSettings.doubleFormatter.format((double) v));
									
									res = res.replaceFirst(repl, newValue); // replace the first occurrence and overwrite
								}
							}
						}
						else
						{
							StringBuilder str = new StringBuilder(res);
							str.insert(getINTparam(param[1]), newValue);
							res = str.toString();
						}
					
						((Variable) param[3]).initTypeAndSet(Variable.textType, res);
						
					}));
	}
	public static ProgramElement visualize_StReplaceText(FunctionalityContent content)
	{
		VisualizableProgramElement StSetTexVar;
		StSetTexVar = new VisualizableProgramElement(content, "Replace/Insert Text", "Replace parts of a text or insert text at a given position.");
		StSetTexVar.setArgumentDescription(0, new TextOrVariable(), "Input");
		StSetTexVar.addParameter(1, new TextOrValueOrVariable(), "Replacer/Index", "Either text to be searched within the input\nor a letter-index (position) to insert.");
		StSetTexVar.addParameter(2, new TextOrValueOrVariable(), "New", "New text or number to be replaced with or inserted.");
		StSetTexVar.addParameter(3, new VariableOnly(true, true), "Output", "Variable that will contain the resulting text.");
		
		StSetTexVar.addOptionalParameter(0, new ValueOrVariable("-1"), "Occurence Ind", "The n'th occurence to replace, or -1 to replace all.");
		StSetTexVar.setExpandableArgumentDescription(TextOrValueOrVariable.class, null, "New #", 32, "If you provide more than one 'New' text/value,\neach will replace one occurence of the Replacer-text.\nFor example replace '[X]' in\n'[X] Apples and [X] Bananas' nwith the values\n'2' and '3' will result in '2 Apples and 3 Bananas'.");
		return(StSetTexVar);
	}
	
	
	
	
	public static ProgramElement create_StSetRanVar()
	{
		Object[] input = new Object[1];
		
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent("StSetRanVar",
				input,
				() -> {
					
					Random rn = randomizer;
					if (content[0].hasOptionalArgument(3))
						rn = (Random) content[0].getOptionalArgumentValue(3);
					
					double res;
					
					if (!content[0].hasOptionalArgument(1))
						res = rn.nextDouble();
					else
					{
						double min = (double) content[0].getOptionalArgumentValue(0);
						double max = (double) content[0].getOptionalArgumentValue(1);
						
						if (content[0].getOptionalArgTrue(2))
							res = ((int) min) + rn.nextInt((int) Math.abs(max-min) +1);
						else
							res = min + rn.nextDouble() * Math.abs(max-min);
					}
					
					initVariableAndSet(input[0], Variable.doubleType, res);
						
					}));
	}
	public static ProgramElement visualize_StSetRanVar(FunctionalityContent content)
	{	
		VisualizableProgramElement vv;
		vv = new VisualizableProgramElement(content, "Set Random", "Creates a random factor either between 0 and 1\nor in the given range (including the limits).");
		vv.setArgumentDescription(0, new VariableOnly(true, true), "Variable");
		vv.addOptionalParameter(0, new ValueOrVariable("0"), "Lower Limit", "Needs the upper limit to have an effect.");
		vv.setOptionalArgument(1, new ValueOrVariable("1"), "Upper Limit");
		vv.addOptionalParameter(2, new BooleanOrVariable("False"), "Whole Number", "If true, only whole numbers like 0, 1, 2 are chosen.\nThe upper limit is included!");
		vv.addOptionalParameter(3, new VariableOnly(), "Randomizer Ident", "Identifier to a randomizer created with the corresponding action.\nThis makes sense if you want to set a fixed seed to the random-generator.");
		
		return(vv);
	}
	
	
	public static ProgramElement create_StSetLabelVar()
	{
		Object[] params = new Object[2];
		return( new FunctionalityContent("StSetLabelVar",
				params,
				() -> {
						Variable var = (Variable) params[0];
						var.initTypeAndSet(Variable.labelType, (FuncLabel) params[1]); 
					}));
	}
	public static ProgramElement visualize_StSetLabelVar(FunctionalityContent content)
	{	
		VisualizableProgramElement StSetLabelVar;
		StSetLabelVar = new VisualizableProgramElement(content, "Set Label Var", "Asign a Label to a variable.\nBy providing its name this structure\nenables you to reference a Label with a variable you provide.\nUse this to simply alternate between\n'Labeled Events' or 'Labeled Positions' you want to call.");
		StSetLabelVar.setArgumentDescription(0, new VariableOnly(true, true), "Variable");
		StSetLabelVar.addParameter(1, new LabelString(), "Label", "The label can have any name consisting of text\nor be another variable starting with '#'.");
		return(StSetLabelVar);
	}
	
	
	
	
	
	/*
	public static ProgrammElement create_StSetTermVar()
	{
		Object[] setTermVar = new Object[2];
		return( new ProgramContent("StSetTermVar",
				setTermVar,
				() -> {}
				,
				() -> {
					
					((Variable ) setNumVar[0]).set( ((Term) setNumVar[1]).applyTo( (Variable) setNumVar[0]) );

				}).markRealVariableIndex(0).markRealVariableIndex(1));		
	}
	public static ProgrammElement visualize_StSetTermVar(ProgramContent content)
	{		
		VisualizableProgrammElement StSetTermVar;
		StSetTermVar = new VisualizableProgrammElement(content, "Set Term-Variable", "Directly sets a term as the value of a variable instead of interpreting the variable.\nFor the meaning of a 'term' see the 'Set Number-Variable' functionality.\nUsing that function you can also make use of a term saved in a variable.");
		StSetTermVar.setArgumentDescription(0, new VariableOnly(), "Variable");
		StSetTermVar.setArgumentDescription(1, new TermValueVarCalc(), "Value"); // TODO
		return(StSetTermVar);		
	}
	*/
	
	
	
	
}


