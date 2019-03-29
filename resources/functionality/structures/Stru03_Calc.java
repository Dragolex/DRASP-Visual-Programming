package functionality.structures;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.exceptions.AccessUnsetVariableException;
import dataTypes.exceptions.MalformedTermException;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.handlers.VariableHandler;
import main.functionality.Functionality;
import main.functionality.helperControlers.ComputationHelper;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Stru03_Calc extends Functionality {

	public static int POSITION = 3;
	public static String NAME = "Calculations";
	public static String IDENTIFIER = "StruMathNode";
	public static String DESCRIPTION = "Mathematical and logical operations on variables.";
	
	
	public static ProgramElement create_StSum()
	{
		Object[] input = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "StSum",
				input,
				() -> {					
						double sum = 0;
						
						for(Object value: content[0].getTotalOptionalOrExpandedArgumentsArray())
							sum += (double) value;
						
						initVariableAndSet(input[0], Variable.doubleType, sum);
					});
		return(content[0]);
	}
	public static ProgramElement visualize_StSum(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Sum of Values", "Sets the sum of all expanded arguments to the Result Variable.\nVariables have to contain numbers!\nDoes not incldue any pre-existing value in the Result Variable.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Result Var");
		vis.setExpandableArgumentDescription(ValueOrVariable.class, null, "Value #", 16, "Up to 16 values.");
		return(vis);
	}
	


	
	public static ProgramElement create_StAverage()
	{
		Object[] input = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "StAverage",
				input,
				() -> {
						double avg = 0;
						
						for(Object value: content[0].getTotalOptionalOrExpandedArgumentsArray())
							avg += (double) value;
						
						avg /= content[0].getTotalOptionalOrExpandedArgumentsCount();
						
						initVariableAndSet(input[0], Variable.doubleType, avg);
					});
		return(content[0]);
	}
	public static ProgramElement visualize_StAverage(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Average of Values", "Sets the average (mean) value of all expanded arguments to the Result Variable.\nVariables have to contain numbers!\nDoes not incldue any pre-existing value in the Result Variable.\nExample: The average of 6 and 2 is 4.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Result Var");
		vis.setExpandableArgumentDescription(ValueOrVariable.class, null, "Value #", 16, "Up to 16 values.");
		return(vis);
	}
	
	
	public static ProgramElement create_StMedian()
	{
		Object[] input = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "StMedian",
				input,
				() -> {
						double mid = Double.MAX_VALUE;
						double minDif = Double.MAX_VALUE;
						for(Object value: content[0].getTotalOptionalOrExpandedArgumentsArray())
						{
							if (Math.abs(((double) value) - mid) < minDif)
							{
								mid = (double) value;
								minDif = Math.abs(((double) value) - mid);
							}
						}
						
						initVariableAndSet(input[0], Variable.doubleType, mid);
					});
		return(content[0]);
	}
	public static ProgramElement visualize_StMedian(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Median of Values", "Sets the middle (median) value of all expanded arguments to the Result Variable.\nVariables have to contain numbers!\nDoes not incldue any pre-existing value in the Result Variable.\nExample: If the variables contain: 12, 2, 6, then 6 will be the mean value.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Result Var");
		vis.setExpandableArgumentDescription(ValueOrVariable.class, null, "Value #", 16, "Up to 16 values.");
		return(vis);
	}
	
	
	public static ProgramElement create_StRound()
	{
		Object[] input = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "StRound",
				input,
				() -> {
						if (content[0].hasOptionalArgument(0))
						{
							int scale = (int) Math.pow(10, (double) content[0].getOptionalArgumentValue(0));
					        initVariableAndSet(input[0], Variable.doubleType, (double) Math.round(((double) input[1]) * scale) / scale);
						}
						else
							initVariableAndSet(input[0], Variable.doubleType, (double) Math.round((double) input[1]));
					});
		return(content[0]);
	}
	public static ProgramElement visualize_StRound(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Round", "Rounds a value either to the next full number, or the given decimal if the optional argument is used.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Result Var");
		vis.setArgumentDescription(1, new ValueOrVariable(), "Value");
		vis.addOptionalParameter(0, new ValueOrVariable(), "Decimals", "Example: Rounding PI to 3 decimals results in a value like 3.142.");
		return(vis);
	}
	
	
	
	
	public static ProgramElement create_StAND()
	{
		Object[] input = new Object[1];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "StAND",
				input,
				() -> {
					}
				,
				() -> {
					try
					{
						for(Object value: content[0].getTotalOptionalOrExpandedArgumentsArray())
						{
							if (!checkIfBoolVar((Variable) value))
							{
								initVariableAndSet(input[0], Variable.boolType, false);
								return;
							}
						}
						initVariableAndSet(input[0], Variable.boolType, true);
					}
					catch (AccessUnsetVariableException e)
					{
						Execution.setError("The variable '" + VariableHandler.getVariableName(e.getSourceVariable()) + "' has not been set to any value!", false);
					}
				});
		return(content[0]);
	}
	public static ProgramElement visualize_StAND(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Logic AND", "Places 'True' in the result variable if all arguments equal to true.\nThe variables can be of any type accepted by the condition 'Check True'.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Result Var");
		vis.setExpandableArgumentDescription(VariableOnly.class, null, true, false, "Var #", 16, "Up to 16 variables.");
		return(vis);
	}
	
	
	public static ProgramElement create_StOR()
	{
		Object[] input = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "StOR",
				input,
				() -> {
					}
				,
				() -> {
					boolean nor = (boolean) input[1];
					
					try
					{
						for(Object value: content[0].getTotalOptionalOrExpandedArgumentsArray())
						{
							if (checkIfBoolVar((Variable) value) == nor)
							{
								initVariableAndSet(input[0], Variable.boolType, true);
								return;
							}
						}
						initVariableAndSet(input[0], Variable.boolType, false);
					}
					catch (AccessUnsetVariableException e)
					{
						Execution.setError("The variable '" + VariableHandler.getVariableName(e.getSourceVariable()) + "' has not been set to any value!", false);
					}
				});
		return(content[0]);
	}
	public static ProgramElement visualize_StOR(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Logic (N)OR", "Places 'True' in the result variable if at least one argument equals to true.\nIf 'NOR' is true, it happens if at least one variable is 'False'.\nThe variables can be of any type accepted by the condition 'Check True'.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Result Var");
		vis.setArgumentDescription(1, new BooleanOrVariable("False"), "NOR");
		vis.setExpandableArgumentDescription(VariableOnly.class, null, true, false, "Var #", 16, "Up to 16 variables.");
		return(vis);
	}
	
	
	public static ProgramElement create_StCalcTerm()
	{
		Object[] param = new Object[2];
		return( new FunctionalityContent("StCalcTerm",
				param,
				() -> {}
				,
				() -> {
					
					try {
						initVariableAndSet(param[0], Variable.doubleType, ComputationHelper.compute((String) param[1]) );
					}
					catch(MalformedTermException e)
					{
						Execution.setError("The term could not be evaluated!\nProblem: " + e.getProblem(), false);
					}

				}));
	}
	public static ProgramElement visualize_StCalcTerm(FunctionalityContent content)
	{
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "Calculate Term", "Calculates a term consisting of numbers, variables, +,-,*,/ and brackets.\nStandard mathematical priority laws are followed.\nThe result is placed in the given variable.\n\nExample:\n2 * (10 + #myvar) + SIN(3*pi)\nYou mayb use binary variables too which will be handled as 1 or 0.\n\nNote: Since the term is handled like a text, you can use text stored in a variable as a term.\nThe following built-in fucntions can be used:\n ABS, ACOS, ASIN, ATAN, ATAN2, CEIL, COS, COSH, DEG, EXP, FLOOR, LN, LOG, MAX, MIN\nPOW, RAD, RND, ROUND, SIGN, SIN, SINH, SQRT, TAN, TANH.\npi and E are available.\nNote: Although a fast, external library ('Parsii') is used, this is a relatively slow functionality.\nUse only if the desired effect cannot be achieved differently.");
		StSetNumVar.setArgumentDescription(0, new VariableOnly(true, true), "Variable");
		StSetNumVar.setArgumentDescription(1, new TextOrVariable(), "Term");
		return(StSetNumVar);		
	}

	
	
	public static ProgramElement create_StModulo()
	{
		Object[] param = new Object[3];
		return( new FunctionalityContent("StModulo",
				param,
				() -> {}
				,
				() -> {
					
					initVariableAndSet(param[2], Variable.doubleType, (double) param[0] % (double) param[1]);

				}));
	}
	public static ProgramElement visualize_StModulo(FunctionalityContent content)
	{
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "Modulo", "Calculates the modulo between the two values.");
		StSetNumVar.setArgumentDescription(0, new ValueOrVariable(), "Input Value");
		StSetNumVar.setArgumentDescription(1, new ValueOrVariable(), "Modulo");
		StSetNumVar.setArgumentDescription(2, new VariableOnly(true, true), "Output Value");
		return(StSetNumVar);		
	}

	
	
	
}


