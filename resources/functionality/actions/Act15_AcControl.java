package functionality.actions;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Act15_AcControl extends Functionality {

	public static int POSITION = 15;
	public static String NAME = "Alternate Current";
	public static String IDENTIFIER = "ActAcCtrl";
	public static String DESCRIPTION = "Actions related to AC devices.";
	
	
	public static ProgramElement create_ElSetupPhaseFiredControl()
	{
		Object[] params = new Object[3];
		return(new FunctionalityContent( "ElSetupPhaseFiredControl",
				params,
				() -> {
					
						initVariableAndSet(params[0], Variable.doubleType, (double) (System.currentTimeMillis()));
						
					}));
	}
	public static ProgramElement visualize_ElSetupPhaseFiredControl(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Setup PFC", "Prepare the usage of 'phase fired control' of an AC power source.");
		vis.addParameter(0, new VariableOnly(true, true), "Identifier", "Identifier to set for the output.");
		vis.addParameter(1, new ValueOrVariable(), "Zero Cross Pin", "Needs to be attached to the zero-Cross sensor output.");
		vis.addParameter(2, new ValueOrVariable(), "Triac Trigger Pin", "Needs to be connected with the triac (at best through an optocoupler).");
		return(vis);
	}
	
	
	public static ProgramElement create_ElSetupModulationControl()
	{
		Object[] params = new Object[5];
		return(new FunctionalityContent( "ElSetupModulationControl",
				params,
				() -> {
					
						initVariableAndSet(params[0], Variable.doubleType, (double) (System.currentTimeMillis()));
						
					}));
	}
	public static ProgramElement visualize_ElSetupModulationControl(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Setup SINE PACKAGE MODULATION", "Prepare the usage of '' of an AC power source.");
		vis.addParameter(0, new VariableOnly(true, true), "Identifier", "Identifier to set for the output.");
		vis.addParameter(1, new ValueOrVariable(), "Zero Cross Pin", "Needs to be attached to the zero-Cross sensor output.");
		vis.addParameter(2, new ValueOrVariable(), "Triac Trigger Pin", "Needs to be connected with the triac (at best through an optocoupler).");
		vis.addParameter(3, new ValueOrVariable(), "Adjustion Steps", "Maximum sines for one alteration of power.");
		vis.addParameter(4, new BooleanOrVariable("False"), "Long Term Averaging", "If the value you provided does not fit exactly for skipping one sine,\nthe system can average the value on long term via many sines.\nThis can result in less continuous driving of the loads.");
		return(vis);
	}
	
	// TODO: Make this usable with all the SET PWM features!
	
}
