package functionality.actions;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.SelectableType;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.exceptions.NonExistingPinException;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.helperControlers.hardware.miniDisplay.MiniDisplay;
import main.functionality.helperControlers.hardware.miniDisplay.OLED1306.SSD1306Display;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.OtherHelpers;

public class Act08_MiniLEDdisplays extends Functionality {

	public static int POSITION = 8;
	public static String NAME = "Mini/LED Displays";
	public static String IDENTIFIER = "ActDisplayNode";
	public static String DESCRIPTION = "Actions enabling to draw onto attached SPI and I2C mini displays\nor onto displays made of pixels using individual LEDs.";
	
	
	public static ProgramElement create_ElI2CDisplayCreate()
	{
		
		Object[] input = new Object[4];
		return(new FunctionalityContent( "ElI2CDisplayCreate",
				input,
				() -> {
					
					int addr = parseI2CAddress((String) input[2]);
					if (addr < 0)
						return;

					try {
						switch((int) input[1])
						{
						case 0:
							initVariableAndSet(input[0], Variable.miniDisplayType, new SSD1306Display(addr, GPIOctrl.setOutputPin((int) (double) input[3], false)));
							break;
						}
					
					} catch (NonExistingPinException e)
					{
						Execution.setError("The pin '" + (int) (double) input[3] + "' is not available!", false);
					}

					
					}));
	}
	public static ProgramElement visualize_ElI2CDisplayCreate(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Create I2C Display", "Create an identifier for using an display attached with I2C.\nCurrently supported model is the 'SSD1306' with 128x64 pixels on 0.96 inches.");
		vis.addParameter(0, new VariableOnly(), "Display Identifier", "A variable to be used to refer to this display in other eactions.");
		vis.setArgumentDescription(1, new SelectableType(new String[] {"SSD1306"}), "Display Type");
		vis.addParameter(2, new TextOrVariable("0x3D"), "I2C Address", "The I2C address of this display (default is 0x3D).\nHowever look into the 'Tools' for some helpful features implemented in DRASP.\nAddress is in hexadecimal like it is also displayed by the system.");
		vis.addParameter(3, new ValueOrVariable(), "Reset Pin", "GPIO connected to the res/reset wire of the display." + gpioPinReferenceText);

		return(vis);
	}
	
	
	public static ProgramElement create_ElDispTextCreate()
	{
		Object[] input = new Object[4];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElDispTextCreate",
				input,
				() -> {
					MiniDisplay disp = (MiniDisplay) input[0];
					
					disp.setCharacterSet(cont[0].getOptionalArgumentValueOR(0, 0));
					disp.drawText(getINTparam(input[1]), getINTparam(input[2]), (String) input[2]);					
					
					}));
	}
	public static ProgramElement visualize_ElDispTextCreate(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Display Text", "Draw text onto an attached SPI or I2C display.");
		vis.setArgumentDescription(0, new VariableOnly(), "Display Identifier");
		vis.setArgumentDescription(1, new ValueOrVariable(), "X position");
		vis.setArgumentDescription(2, new ValueOrVariable(), "Y position");
		vis.setArgumentDescription(3, new TextOrVariable("Hello World"), "Text");
		vis.addOptionalParameter(0, new SelectableType(new String[] {"Windows-1252", "IBM PC", "MS-DOS"}), "Character Set", "Type of characters to draw.\nUnfortunately for now only those options are available.");

		return(vis);
	}
	
	
	
	public static ProgramElement create_ElDispShapeCreate()
	{
		Object[] input = new Object[6];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElDispShapeCreate",
				input,
				() -> {
					MiniDisplay disp = (MiniDisplay) input[0];
					
					
					switch((int) input[1])
					{
					case 0:
						disp.drawRectangle(getINTparam(input[2]), getINTparam(input[3]), getINTparam(input[4]), getINTparam(input[5]), false);
						break;
					case 1:
						disp.drawRectangle(getINTparam(input[2]), getINTparam(input[3]), getINTparam(input[4]), getINTparam(input[5]), true);
						break;
					case 2:
						disp.drawRectangle(getINTparam(input[2]), getINTparam(input[3]), getINTparam(input[4]), getINTparam(input[5]), true);
						break;
					case 3:
						disp.drawCircle(getINTparam(input[2]), getINTparam(input[3]), getINTparam(input[4]), false);
						break;
					case 4:
						disp.drawCircle(getINTparam(input[2]), getINTparam(input[3]), getINTparam(input[4]), true);
						break;
					case 5:
						disp.drawArc(getINTparam(input[2]), getINTparam(input[3]), getINTparam(input[4]), getINTparam(input[5]), cont[0].getOptionalArgumentValueOR(0, 60));
						break;
						
					}
					
					}));
	}
	public static ProgramElement visualize_ElDispShapeCreate(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Display Shape", "Draw text onto an attached SPI or I2C display.");
		vis.setArgumentDescription(0, new VariableOnly(), "Display Identifier");
		vis.setArgumentDescription(1, new SelectableType(new String[] {"Rectangle", "Filled Rectangle", "Circle", "Filled Circle", "Arc"}), "Shape");		
		vis.setArgumentDescription(2, new ValueOrVariable(), "X position");
		vis.setArgumentDescription(3, new ValueOrVariable(), "Y position");
		vis.setArgumentDescription(4, new ValueOrVariable(), "Width or Radius");
		vis.setArgumentDescription(5, new ValueOrVariable(), "Height or Arc Angle");
		vis.setOptionalArgument(0, new ValueOrVariable(), "Arc Range Angle");

		return(vis);
	}
	
	
	public static ProgramElement create_ElDispLineCreate()
	{
		Object[] input = new Object[6];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElDispLineCreate",
				input,
				() -> {
					MiniDisplay disp = (MiniDisplay) input[0];
					
					Object[] addit = cont[0].getTotalOptionalOrExpandedArgumentsArray();
					int len = addit.length;
					if ((len % 2) != 0)
					{
						Execution.setError("The number of additional arguments needs to be even!", false);
						return;
					}
					
					
					if ((int)input[3] == 0)
					{
						disp.drawLine(getINTparam(input[1]), getINTparam(input[2]), getINTparam(input[4]), getINTparam(input[5]));
						
						if (len != 0)
						{
							int lx = getINTparam(input[4]);
							int ly = getINTparam(input[5]);
							
							for(int i = 0; i < len; i += 2)
							{
								disp.drawLine(lx, ly, getINTparam(cont[0].getOptionalArgumentValue(i)), getINTparam(cont[0].getOptionalArgumentValue(i+1)));
								lx = getINTparam(cont[0].getOptionalArgumentValue(i));
								ly = getINTparam(cont[0].getOptionalArgumentValue(i+1));
							}
						}
					}
					else
					if ((int)input[3] == 0)
					{
						int x = getINTparam(input[1]);
						int y = getINTparam(input[2]);
						double dir = (double) input[4];
						double leng = (double) input[5];
						int lx = (int) (x + OtherHelpers.lengthdirX(dir, leng));
						int ly = (int) (y + OtherHelpers.lengthdirY(dir, leng));
						
						disp.drawLine(x, y, lx, ly);
						
						if (len != 0)
							for(int i = 0; i < len; i += 2)
							{
								x = (int) (lx + OtherHelpers.lengthdirX(dir, leng));
								y = (int) (ly + OtherHelpers.lengthdirY(dir, leng));
								
								disp.drawLine(lx, ly, x, y);
								
								lx = x;
								ly = y;
							}
					}
					
					}));
	}
	public static ProgramElement visualize_ElDispLineCreate(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Display Line", "Draw text onto an attached SPI or I2C display.");
		vis.setArgumentDescription(0, new VariableOnly(), "Display Identifier");
		vis.setArgumentDescription(1, new ValueOrVariable(), "X1");
		vis.setArgumentDescription(2, new ValueOrVariable(), "Y1");
		vis.setArgumentDescription(3, new SelectableType(new String[] {"By X2 and Y2 coordinates", "By direction and length"}), "Shape");
		vis.setArgumentDescription(4, new ValueOrVariable(), "X2 or Dir");
		vis.setArgumentDescription(5, new ValueOrVariable(), "Y2 or Len");
		vis.setExpandableArgumentDescription(ValueOrVariable.class, null, "Chain Val #", 64, "Optional additional sets of positions.\nDepending on what you selected up there,\neither additional X and Y points, or 'Dir' and 'Len'.\nIn total you can form a chain of lines this way.");

		return(vis);
	}
	
	
}
