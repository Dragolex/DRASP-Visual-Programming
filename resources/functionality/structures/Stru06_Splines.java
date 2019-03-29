package functionality.structures;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.SelectableType;
import dataTypes.contentValueRepresentations.TermTextVarCalc;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.exceptions.AccessUnsetVariableException;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.helperControlers.spline.DataSpline;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Stru06_Splines extends Functionality {

	public static int POSITION = 6;
	public static String NAME = "Splines";
	public static String IDENTIFIER = "StruSplineNode";
	public static String DESCRIPTION = "Structures for 'Splines'.\nSplines are a more sophiscated type of lists with numbers.\nThey basically associate an index with a number but whenever you set an index\nvalues are interpolated to close the gap to the last index!\nThose graphs can be used to effectively display the data from a sensor\nor smoothly animate lights if using a PWM board.";
	
	
	public static ProgramElement create_StCreateSpline()
	{
		Object[] input = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent("StCreateSpline",
				input,
				() -> {
					
					boolean interpolate = (boolean) input[1];
					boolean smoothen = (boolean) input[2];
					
				 	initVariableAndSet(input[0], Variable.splineType,
				 			new DataSpline(interpolate, smoothen,
				 					getINTparam( content[0].getOptionalArgumentValueOR(0, -1.0)),
				 					getINTparam( content[0].getOptionalArgumentValueOR(1, -1.0)),
				 					getINTparam( content[0].getOptionalArgumentValueOR(2, -1.0))));
					
					if (DEBUG)
						((DataSpline) ((Variable) input[0]).getUnchecked()).setIdentifierVariable(((Variable) input[0]));
					
				}));		
	}
	public static ProgramElement visualize_StCreateSpline(FunctionalityContent content)
	{		
		VisualizableProgramElement StAddVal;
		StAddVal = new VisualizableProgramElement(content, "Create Spline", "Creates the new Spline data structure.");
		StAddVal.setArgumentDescription(0, new VariableOnly(true, true), "Spline Identifier");
		StAddVal.addParameter(1, new BooleanOrVariable("true"), "Interpolate", "If true, the system will automatically interpolate the values between X-values when you add them.\nThat's recommended for animating lights or to visualize\ndata from sensors with a slow update rate.\nIf false, the nearest value will be used.");
		StAddVal.addParameter(2, new BooleanOrVariable("true"), "Smooth", "If true and interpolation is true as well,\nthe spline will form a smooth curve.\nOtherwise linear interpolation is used.\nThis requires less computation however it only needs\nto be recomputed when creating and changing the graph!");
		StAddVal.addOptionalParameter(0, new ValueOrVariable("1000"), "Maximum Vals", "Maximum values stored.\nThe more values, the more memmory the software requires.\nThe Spline will be scaled down automatically when the value nears the limit.\nDefault is 2000");
		StAddVal.addOptionalParameter(1, new ValueOrVariable("-1"), "Autocrop by X", "Automatically crop to the given X-Range.\nUse negative value to avoid cropping.");
		StAddVal.addOptionalParameter(2, new ValueOrVariable("-1"), "Autocrop by Count", "Automatically crop to the given number of last added values.\nUse negative values to avoid cropping.");
		
		return(StAddVal);		
	}

	

	public static ProgramElement create_StSplineNodeDir()
	{
		Object[] input = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent("StSplineNodeDir",
				input,
				() -> {
					DataSpline spline = (DataSpline) input[0];

					boolean addRelative = content[0].getOptionalArgTrue(0);
					
					if (addRelative)
						spline.appendValue(getINTparam(input[1]), (double) input[2]);
					else
						spline.setValue(getINTparam(input[1]), (double) input[2]);
					
					if (content[0].getTotalOptionalOrExpandedArgumentsCount() > 1)
					{
						int len = content[0].getTotalOptionalOrExpandedArgumentsCount();
						int dif = getINTparam(input[1]);
						
						Object[] values = content[0].getTotalOptionalOrExpandedArgumentsArray();
								
						for(int i = 1; i < len; i++)
							spline.appendValue(dif, (double) values[i]);
					}
				 	
				}));		
	}
	public static ProgramElement visualize_StSplineNodeDir(FunctionalityContent content)
	{		
		VisualizableProgramElement StAddVal;
		StAddVal = new VisualizableProgramElement(content, "Add Spline Value", "Add at least one new value directly to the Spline.");
		StAddVal.setArgumentDescription(0, new VariableOnly(), "Spline Identifier");
		StAddVal.setArgumentDescription(1, new ValueOrVariable(), "X Position");
		StAddVal.setArgumentDescription(2, new ValueOrVariable(), "Y Position");
		StAddVal.addOptionalParameter(0, new BooleanOrVariable("false"), "Append X", "If true, the given 'X Position' will be relative to the maximum 'X Postition'.\nTherefore the new 'Y Position' will be appended.\nIf more additional parameters are used, setting multiple Y-values\n within this single element, then those further values are always set relative\nwith the given 'X Postion' as a distance to the last.");
		StAddVal.setExpandableArgumentDescription(ValueOrVariable.class, null, "Y Position #", 32, "Add up to 32 more values at once at X positions with equal distance.");
		return(StAddVal);
	}	
	
	
	public static ProgramElement create_StSplineNodeVar()
	{
		Object[] input = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent("StSplineNodeVar",
				input,
				() -> {
					DataSpline spline = (DataSpline) input[0];
					
					boolean addRelative = content[0].getOptionalArgTrue(0);

					try {
						if (addRelative)
							spline.appendValue(getFLOATparam(input[1]), (Variable) input[2]);
						else
							spline.setValue(getFLOATparam(input[1]), (Variable) input[2]);
					
						
						if (content[0].getTotalOptionalOrExpandedArgumentsCount() > 1)
						{
							int len = content[0].getTotalOptionalOrExpandedArgumentsCount();
							int dif = getINTparam(input[1]);
							
							Object[] values = content[0].getTotalOptionalOrExpandedArgumentsArray();
									
							for(int i = 1; i < len; i++)
								spline.appendValue(dif, (Variable) values[i]);
						}
					} catch (AccessUnsetVariableException e)
					{
						Execution.setError("The variable needs to be initialized to a value!",false);
					}
				 	
				}));		
	}
	public static ProgramElement visualize_StSplineNodeVar(FunctionalityContent content)
	{
		VisualizableProgramElement StAddVal;
		StAddVal = new VisualizableProgramElement(content, "Add Spline Var", "Add at least one new value based on a variable to the Spline.\nThe variable(s) you pass as 'Y Position' are evaluated whenever the Spline data is used.\nThis way you can automatically influence a Spline defined one single time.\nNote that you are allowed to add fixed as well as dynamic values to the Spline.");
		StAddVal.setArgumentDescription(0, new VariableOnly(), "Spline Identifier");
		StAddVal.setArgumentDescription(1, new ValueOrVariable(), "X Position");
		StAddVal.setArgumentDescription(2, new VariableOnly(true, false), "Y Position");
		StAddVal.addOptionalParameter(0, new BooleanOrVariable("false"), "Append X", "If true, the given 'X Position' will be relative to the maximum 'X Postition'.\nTherefore the new 'Y Position' will be appended.\nIf more additional parameters are used, setting multiple Y-values\n within this single element, then those further values are always set relative\nwith the given 'X Postion' as a distance to the last.");
		StAddVal.setExpandableArgumentDescription(VariableOnly.class, null, true, false, "Y Position #", 32, "Up to 32 additional variables at X positions with equal distance.");
		return(StAddVal);
	}	
		
	
	public static ProgramElement create_StSplineGetLimits()
	{
		Object[] input = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent("StSplineGetLimits",
				input,
				() -> {
					
					DataSpline spline = (DataSpline) input[0];
					
					switch ((int) input[1])
					{
					case 0: initVariableAndSet(input[2], Variable.doubleType, (double) spline.getMinX());
	
					case 1: initVariableAndSet(input[2], Variable.doubleType, (double) spline.getMaxX());
	
					case 2: initVariableAndSet(input[2], Variable.doubleType, (double) spline.getMinY());
						
					case 3: initVariableAndSet(input[2], Variable.doubleType, (double) spline.getMaxY());
					}
				 	
				}));		
	}
	public static ProgramElement visualize_StSplineGetLimits(FunctionalityContent content)
	{		
		VisualizableProgramElement StAddVal;
		StAddVal = new VisualizableProgramElement(content, "Get Spline Limits", "Get one of the four limits of a Spline and store them in variable.");
		StAddVal.setArgumentDescription(0, new VariableOnly(), "Spline Identifier");
		StAddVal.addParameter(1, new SelectableType(new String[] {"Min X", "Max X", "Min Y", "Max Y"}), "Option", "The limit to retrieve.");
		StAddVal.addParameter(2, new VariableOnly(true, true), "Output", "Variable where the result will be placed in.");

		return(StAddVal);
	}

	
	public static ProgramElement create_StSplineCrop()
	{
		Object[] input = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent("StSplineCrop",
				input,
				() -> {
					
					DataSpline spline = (DataSpline) input[0];
					
					initVariableAndSet(input[1], Variable.doubleType, (double) spline.getMinX());
					
					spline.crop((double) input[1], (double) input[2], content[0].getOptionalArgTrue(0));
					
				}));		
	}
	public static ProgramElement visualize_StSplineCrop(FunctionalityContent content)
	{		
		VisualizableProgramElement StAddVal;
		StAddVal = new VisualizableProgramElement(content, "Crop Spline", "Crop out a part of a Spline by forcing the min and max X psoitions.\nIf you use 0 and 0, the Spline will be cleared.\nNote that the 'Create Spline' element enables you to set auto-crop.");
		StAddVal.setArgumentDescription(0, new VariableOnly(), "Spline Identifier");
		StAddVal.setArgumentDescription(1, new VariableOnly(), "Min X");
		StAddVal.setArgumentDescription(2, new VariableOnly(), "Max X");
		StAddVal.addOptionalParameter(0, new BooleanOrVariable("False"), "Shift to 0", "if true, the spline will be shifted by the minimum value.");
		return(StAddVal);
	}

	
	public static ProgramElement create_StEditSpline()
	{
		Object[] input = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent("StSplineCrop",
				input,
				() -> {
					
					DataSpline spline = (DataSpline) input[0];
					
					if ((int) input[1] == 0)
						spline.applyTermX((Term) input[2]);
					if ((int) input[1] == 1)
						spline.applyTermY((Term) input[2]);
					
				}));		
	}
	public static ProgramElement visualize_StEditSpline(FunctionalityContent content)
	{		
		VisualizableProgramElement StAddVal;
		StAddVal = new VisualizableProgramElement(content, "Edit Spline", "With this block you can modify the X axis or the y axis\nof a spline by applying a calculation to every value.\nFor example apply *=0.5 to the X axis to scale the spline horizontally.\nAdd +=5 to the Y axis to raise the whole spline.\nVariables are allowed too.");
		StAddVal.setArgumentDescription(0, new VariableOnly(), "Spline Identifier");
		StAddVal.setArgumentDescription(1, new SelectableType(new String[] {"X", "Y"}), "Axis");
		StAddVal.setArgumentDescription(2, new TermTextVarCalc(true), "Term");
		return(StAddVal);
	}

	
	
	
}


