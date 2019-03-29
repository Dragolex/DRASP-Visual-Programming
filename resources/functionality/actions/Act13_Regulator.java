package functionality.actions;

import dataTypes.FunctionalityContent;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.helperControlers.Regulator;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Act13_Regulator extends Functionality {

	public static int POSITION = 13;
	public static String NAME = "Regulator";
	public static String IDENTIFIER = "ActRegulationNode";
	public static String DESCRIPTION = "The Regulator is a system which attempts to achieve a\n"
									 + "desired hardware value by influencing a different value.\n"
									 + "For example you can regulate the speed of a fan based on a temperature sensor\n"
									 + "to maintain a constant target-temperature.";
	
	
	
	// Regulators
	
	static public FunctionalityContent create_ElCreateRegulator()
	{
		Object[] params = new Object[3];
		return(	new FunctionalityContent( "ElCreateRegulator",
				params,
				() -> {
					initVariableAndSet(params[0], Variable.regulatorType, new Regulator((Variable) params[1], params[2], 1, 1, 1));
					}));
	}
	static public VisualizableProgramElement visualize_ElCreateRegulator(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Create Regulator", "Creates a new 'Regulator'.\nThe regulator is a system which attempts to achieve a\ndesired hardware value by influencing a different value.\nBoth will be given as variables.For example you can regulate the speed of a fan based on a temperature sensor\nto maintain a constant target-temperature.\nWhen running, the regulator will perform in the background,\nnot blocking any event. However you have to constantly update the input variable in an event!\nYou should use this regulator together with the\n'Regulate' event to apply the change somewhere.");
		elDelay.addParameter(0, new VariableOnly(true, true), "Reg. Identifier", "Identifier variable for this regulator.");
		elDelay.addParameter(1, new VariableOnly(true, false), "Regulator Input", "Variable which will provide the input for the regulation.\nFor example this can be a variable\nconstantly updated with the temperature from a sensor.");
		elDelay.addParameter(2, new ValueOrVariable(), "Regulator Target", "Value to achieve.\nFor example the temperature to reach and maintain.");

		// ADD the regulation mechanism!
		
		return(elDelay);
	}

	static public FunctionalityContent create_ElChangeTargetValue()
	{
		Object[] params = new Object[2];
		return(	new FunctionalityContent( "ElChangeTargetValue",
				params,
				() -> {
						if (params[0] == null)
						{
							Execution.setError("The Regulator does not exist!", false);
							return;
						}
						((Regulator) params[0]).setTarget(getDoubleVariable(params[2]));
					}));
	}
	static public VisualizableProgramElement visualize_ElChangeTargetValue(FunctionalityContent content)
	{
		VisualizableProgramElement elDelay;
		elDelay = new VisualizableProgramElement(content, "Change Target Value", "TODO");
		elDelay.addParameter(0, new VariableOnly(), "Reg. Identifier", "Identifier variable for this regulator.");
		elDelay.addParameter(1, new ValueOrVariable(), "Regulator Target", "Value to achieve.\nFor example the temperature to reach and maintain.");
		
		return(elDelay);
	}	
	

	
}
