package functionality.actions;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import main.functionality.Functionality;
import main.functionality.helperControlers.hardware.StepperMotor;
import main.functionality.helperControlers.spline.DataSpline;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Act06_Stepper extends Functionality {

	public static int POSITION = 6;
	public static String NAME = "Stepper Motor";
	public static String IDENTIFIER = "ActStepperNode";
	public static String DESCRIPTION = "Actions related to a stepper motor controled via several GPIOs each.";
	public static String SYMBOL_PATH = ""; // TODO (For example symbol of the raspberry or of a stepper motor)
	
	
	
	// Stepper motor
	
	// Simple PWM channel value set
	public static ProgramElement create_ElCreateStepper()
	{
		Object[] params = new Object[6];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElCreateStepper",
				params,
				() -> {
					
					Object[] ext = content[0].getTotalOptionalOrExpandedArgumentsArray();

					int[] pins;
					if (ext == null || ext.length <= 2)
					{
						pins = new int[] {
								getINTparam( params[1] ),
								getINTparam( params[2] ),
								getINTparam( params[3] ),
								getINTparam( params[4] )};
					}
					else
					{
						pins = new int[4+ext.length-2];
						
						System.arraycopy(params, 1, pins, 0, 4);
						System.arraycopy(ext, 2, pins, 4, ext.length-2);
					}
					
					
					StepperMotor motor = new StepperMotor(pins,
							(double) params[5],
							content[0].getOptionalArgTrue(0));
					
					if (content[0].hasOptionalArgument(1))
						motor.byDegrees((double) content[0].getOptionalArgumentValue(1));
					initVariableAndSet(params[0], Variable.stepperMotor, motor);
					
					});
		return(content[0]);
	}
	public static ProgramElement visualize_ElCreateStepper(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Create Stepper", "Prepares a stepper motor to control through 4 or more (optional) pins.\nNote that the stepper usually requires a dedicated driver chip!\nTODO: Add scheme");
		vis.addParameter(0, new VariableOnly(true, true), "Identifier", "PWM channel to use.");
		vis.addParameter(1, new ValueOrVariable(), "Coil Pin 1", "Pin for coil 1." + gpioPinReferenceText);
		vis.addParameter(2, new ValueOrVariable(), "Coil Pin 2", "Pin for coil 2." + gpioPinReferenceText);
		vis.addParameter(3, new ValueOrVariable(), "Coil Pin 3", "Pin for coil 3." + gpioPinReferenceText);
		vis.addParameter(4, new ValueOrVariable(), "Coil Pin 4", "Pin for coil 4." + gpioPinReferenceText);
		vis.addParameter(5, new ValueOrVariable(), "Step Time", "Minimum duration between two (micro)steps in milliseconds. Depends on the motor.\nYou should chose the lowest value which\nstill securely causes the stepper to move\nbecause this also limits the maximum speed of the motor.\nYou often find this value in the data sheet.");
		vis.addOptionalParameter(0, new BooleanOrVariable("False"), "Use Full-Step", "If true, the stepper will use full steps. That means it only uses the main positions of the axis.\nThis makes movement less accurate but holding the position is stronger.\nAdditionally the risk of missing steps is lower.\nThis is also useful when the stepper motor moves fast.");
		vis.addOptionalParameter(1, new ValueOrVariable("1"), "Degrees/Step", "If you provide the number of degrees (0-359) moved in one step of the motor\nthis will be automatically applied to all\nfurther actions applied to this stepper\nand thus control it via degrees than via steps.\nYou will find this value in your step motors data sheet.\nNote that due to the fact that steps have to be used internally,\n values might be rounded.");
		vis.setExpandableArgumentDescription(ValueOrVariable.class, "", false, false, "Coil Pin 4+#", 16, "Additional pin if your stepper motor has more than 4 coils.");
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElMoveStepper()
	{
		Object[] params = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElMoveStepper",
				params,
				() -> {
						((StepperMotor) params[0]).move((double) params[1], (boolean) params[2]);						
					});
		return(content[0]);
	}
	public static ProgramElement visualize_ElMoveStepper(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Move Stepper", "Rotate the stepper motor by a certain number of steps or degrees.");
		vis.addParameter(0, new VariableOnly(), "Identifier", "Identifier created by 'Create Stepper'.");
		vis.addParameter(1, new ValueOrVariable(), "Ammount", "Either the number of steps to move or the degrees if the optional argument 'Degrees/Step'\nhas been set when the stepper has been created.\nNegative values move backwards.\nNumbers smaller than 1 are allowed but will sum up to\\nat least one full step before anything can happen physically.");
		vis.addParameter(2, new BooleanOrVariable("True"), "Keep Locked", "If true, the electromagnets will stay powered to maintain the current step.\nHowever this draws power,\nthus you can disable the motor by setting this parameter to false.");
		return(vis);
	}
	
	
	public static ProgramElement create_ElMoveStepperContin()
	{
		Object[] params = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		content[0] = new FunctionalityContent( "ElMoveStepperContin",
				params,
				() -> {
						((StepperMotor) params[0]).setSpeed((double) params[1]);
					});
		return(content[0]);
	}
	public static ProgramElement visualize_ElMoveStepperContin(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Continuous Stepper", "Set the stepper motor to rotate continuously with a certain speed factor.\nThe speed will be resetted by either of the other actions affecting the stepper.\nNote that stepper motors always work in steps.\nThat means at low speed, no continuous rotation can be achieved anymore.");
		vis.addParameter(0, new VariableOnly(), "Identifier", "Identifier created by 'Create Stepper'.");
		vis.addParameter(1, new ValueOrVariable(), "Speed Factor", "A speed of 1 is the maximum stepper speed and 0 means stopped.\nA negative value will rotate backwards.");
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElMoveStepperBySpline()
	{
		Object[] params = new Object[4];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElMoveStepperBySpline",
				params,
				() -> {
					
					if (content[0].getOptionalArgTrue(0))
						new Thread(() -> ((StepperMotor) params[0]).moveBySpline((DataSpline) params[1], (long) (double) params[2], (double) params[3])).start();
					else
						((StepperMotor) params[0]).moveBySpline((DataSpline) params[1], (long) (double) params[2], (double) params[3]);
						
					}));
	}
	public static ProgramElement visualize_ElMoveStepperBySpline(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Stepper by Spline", "This action traces every position on a given spline\nand applies the contained number of steps or degrees to the motor.\nAfter the given step, the next position on the X-Axis is taken and so on.");
		vis.addParameter(0, new VariableOnly(), "Identifier", "Identifier created by 'Create Stepper'.");
		vis.addParameter(1, new VariableOnly(), "Spline Identifier", "Spline to move given steps from.");
		vis.addParameter(2, new ValueOrVariable("0"), "X Delay", "Delay in milliseconds between two positions on the X-axis of the Spline.");
		vis.addParameter(3, new ValueOrVariable("1"), "Value Factor", "Factor applied to the Y-values of the Spline.");
		vis.addOptionalParameter(0, new BooleanOrVariable("False"), "No Wait", "If true, the current event will instantly continue execution.\nIf false (default), the event will wait until the transition has finished.");
		return(vis);
	}
	
	
	
	
}
