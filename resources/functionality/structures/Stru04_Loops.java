package functionality.structures;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.SelectableType;
import dataTypes.contentValueRepresentations.TermValueVarCalc;
import dataTypes.contentValueRepresentations.TermValueVarComp;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Stru04_Loops extends Functionality {

	public static int POSITION = 4;
	public static String NAME = "Loops";
	public static String IDENTIFIER = "StruLoopNode";
	public static String DESCRIPTION = "Structures to repeat a section of code.\nDrag into the queue and provide the desired parameters.";
	
	

	// Repeat Event
	public static ProgramElement create_StRepEv()
	{
		return(new FunctionalityContent("StRepEv", EventRepeater, 0));
	}
	public static ProgramElement visualize_StRepEv(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Repeat Event", "Causes the program to continue with the top of the current event without checking its condition."));
	}
	
	

	
	// For loop
	public static ProgramElement create_StForLoop()
	{
		return(new FunctionalityContent("StForLoop", ForLoop, 4));
	}
	public static ProgramElement visualize_StForLoop(FunctionalityContent content)
	{	
		VisualizableProgramElement StForLoop;
		StForLoop = new VisualizableProgramElement(content, "For Loop", "A common structure from many programming languages and simplifies what could be achieved with Labels and condition repeats.");
		StForLoop.addParameter(0, new VariableOnly(true, true), "Index", "A variable to be used as the iteration-counter during the loop.");
		StForLoop.addParameter(1, new ValueOrVariable("0"), "Init Value", "The value the index will be set to at initialization.");
		StForLoop.addParameter(2, new TermValueVarComp(true), "Comparator", "A comparator term like in the 'Compare Number' condition\nwhich the variable will be checked against at every iteration.");
		StForLoop.addParameter(3, new TermValueVarCalc(true), "New Value", "A calculation term like in the 'Set Number' structure\nwhich will be applied to the variable after every iteration of the loop.\nA relative term like '+1' is recomended to avoid an endless loop.");
		return(StForLoop);
	}
	
	
	
	
	public static ProgramElement create_StXLoop()
	{
		Object[] variable = new Object[2];
		//LoopHelper loopHelper = new LoopHelper();
		int[] loopVars = new int[2];
		return(new FunctionalityContent( "StXLoop",
				variable,
				() -> {
					}
				,
				() -> {
						loopVars[0] = 0; // index
						loopVars[1] = getINTparam( variable[0] );
						initVariableAndSet(variable[1], Variable.doubleType, 0);
					},
				() -> {
						setVariable(variable[1], loopVars[0]);
						return(loopVars[0] < loopVars[1]);
					},
				() -> {
					loopVars[0]++;
				}
				
				
				/*
				() -> {
					}
				,
				() -> {
						loopHelper.reset();
					 	loopHelper.setCounter(getIntegerInput( variable[0]);
					 	initVariableAndSet(variable[1], Variable.doubleType, 0);
					},
				() -> {
						setVariable(variable[1], loopHelper.getIndex());
						return(loopHelper.canContinue());
					},
				() -> {
					loopHelper.next();
				} 
				 */
				
				));
	}
	public static ProgramElement visualize_StXLoop(FunctionalityContent content)
	{
		VisualizableProgramElement elExecCommand;
		elExecCommand = new VisualizableProgramElement(content, "Repeat X", "Repeat the child elements 'X times'\nThe 'Index Variable' will contain which iteration is currently executed.");
		elExecCommand.setArgumentDescription(0, new ValueOrVariable(), "X times");
		elExecCommand.setArgumentDescription(1, new VariableOnly(true, true), "Index Variable");
		return(elExecCommand);
	}
	
	
	public static ProgramElement create_StFadeLoop()
	{
		Object[] param = new Object[5];

		Boolean[] reversed = new Boolean[1];
		reversed[0] = false;
		
		Integer[] remaining_reverses = new Integer[1];
		Integer[] counter = new Integer[1];

		Double[] increment = new Double[1];
		
		
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "StFadeLoop",
				param,
				() -> {
					}
				,
				() -> {
						initVariableAndSet(param[4], Variable.doubleType, (double) param[0]);
						remaining_reverses[0] = cont[0].getOptionalArgumentValueOR(0, 0);
						
						if (cont[0].hasOptionalArgument(1))
							initVariableAndSet(cont[0].getOptionalArgumentValue(1), Variable.doubleType, (double) 0);
						
						reversed[0] = false;
						
						if ((int) param[2] == 0) // directly given increment
							increment[0] = Math.abs((double) param[3]);
						else
							increment[0] = Math.abs((double) param[0] - (double) param[1])/Math.abs((double) param[3]); // compute the increment needed to achieve the given number of steps
						
						counter[0] = 0;
					},
				() -> {
						if (counter[0] == -1000)
							return(false);
						
						counter[0] += 1;
						return(true);
					},
				() -> {
										
					double val = (double) ((Variable) param[4]).getUnchecked();
					
					if (cont[0].hasOptionalArgument(1))
						((Variable) cont[0].getOptionalArgumentValue(1)).set((double) counter[0]);
					
					boolean pastLimit = false;
					
					if ((double) param[reversed[0] ? 0 : 1] > (double) param[reversed[0] ? 1 : 0]) // end is larger than start
					{
						val += increment[0]; // add a positive increment
						
						if (val >= (double) param[reversed[0] ? 0 : 1]) // past the limit
							pastLimit = true;
					}
					else
					{
						val -= increment[0]; // subtract a positive increment
						
						if (val <= (double) param[reversed[0] ? 0 : 1]) // past the limit
							pastLimit = true;
					}
					if (pastLimit)
					{
						val = (double) param[reversed[0] ? 0 : 1];

						if (remaining_reverses[0] <= 0)
						{
							if (counter[0] >= 0)
								counter[0] = -1001;
						}
						else
						{
							remaining_reverses[0] -= 1;
							reversed[0] = !reversed[0];
							
							if ((int) param[2] == 1) // directly given increment
								increment[0] = Math.abs((double) param[0] - (double) param[1])/Math.abs((double) param[3]); // compute the increment needed to achieve the given number of steps
						}
					}
					
					setVariable(param[4], val);
				}
				
				));
	}
	public static ProgramElement visualize_StFadeLoop(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Fading Loop", "Similar to the For loop but lets you provide the start- and end-values and either an increment or steps directly instead of terms.\nPlus it allows to reverse the loop again.\nIt is prominently used to fade effects like lights up and down.\nNote that there is also a 'Fade Loop Event' that can be even more useful!");
		vis.setArgumentDescription(0, new ValueOrVariable(), "Start Value");
		vis.setArgumentDescription(1, new ValueOrVariable(), "End Value");
		vis.addParameter(2, new SelectableType(new String[] {"By Increment", "By Steps"}), "Iteration", "By Increment means you provide a value to increase on every step.\nBy step means you provide the number of steps and the increment\nto achieve so many steps will be computed automatically.");
		vis.addParameter(3, new ValueOrVariable(), "Incr/Steps", "May not be zero.\nNegative or positive increment does not matter\nas it will be adapted to reach the end value.\nNote that if you provide a variable for steps and change its value at runtime,\nthat will only take effect after reaching the next limit.");
		vis.addParameter(4, new VariableOnly(true, true), "Value Variable", "This variable will hold the new value during every iteration.\nNote that you can manipulate this value to skip iterations!");
		vis.addOptionalParameter(0, new ValueOrVariable("0"), "Reverses", "Number of reverses when reaching a limit.\nReverse of 2 for example means it fades the following: 0->1->0->1.");
		vis.addOptionalParameter(1, new VariableOnly(true, true), "Index Variable", "This variable will hold the index of the current iteration.");
		return(vis);
	}

	
	
	
	// List loop
	public static ProgramElement create_StListLoop()
	{
		return(new FunctionalityContent("StListLoop", ListLoop, 2));
	}
	public static ProgramElement visualize_StListLoop(FunctionalityContent content)
	{	
		VisualizableProgramElement StForLoop;
		StForLoop = new VisualizableProgramElement(content, "Loop a List", "Loops through a given list and places the output\nand optionally the coresponding index in the given variables.");
		StForLoop.setArgumentDescription(0, new VariableOnly(), "List Identifier");
		StForLoop.setArgumentDescription(1, new VariableOnly(true, true), "Output");
		StForLoop.setOptionalArgument(0, new VariableOnly(true, true), "Index");
		//.setArgumentDescription(2, new VariableOnly(), true, "Index");
		return(StForLoop);
	}
	

	// Loop quit
	public static ProgramElement create_StQuitLoop()
	{
		return(new FunctionalityContent("StQuitLoop", LoopQuitter, 0));
	}
	public static ProgramElement visualize_StQuitLoop(FunctionalityContent content)
	{	
		return(new VisualizableProgramElement(content, "Quit Loop", "Forces a 'for-' or 'list-loop' to quit instantly.\nDoes not apply to 'Repeat Event' or 'Repeat Block'."));
	}
	
	
	
	
	
}


