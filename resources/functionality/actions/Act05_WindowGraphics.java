package functionality.actions;

import java.util.concurrent.FutureTask;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.SelectableType;
import dataTypes.contentValueRepresentations.TermValueVarCalc;
import dataTypes.contentValueRepresentations.TextOrValueOrVariable;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import javafx.application.Platform;
import javafx.stage.Stage;
import main.functionality.Actions;
import main.functionality.Functionality;
import main.functionality.helperControlers.openCVrelated.ImageInput;
import main.functionality.helperControlers.screen.JFXbackground;
import main.functionality.helperControlers.screen.JFXbuttonItem;
import main.functionality.helperControlers.screen.JFXeffect;
import main.functionality.helperControlers.screen.JFXextImageItem;
import main.functionality.helperControlers.screen.JFXgraph;
import main.functionality.helperControlers.screen.JFXimageItem;
import main.functionality.helperControlers.screen.JFXitem;
import main.functionality.helperControlers.screen.JFXshape;
import main.functionality.helperControlers.screen.JFXtext;
import main.functionality.helperControlers.screen.JFXwindow;
import main.functionality.helperControlers.spline.DataSpline;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.OtherHelpers;

public class Act05_WindowGraphics extends Functionality {

	public static int POSITION = 5;
	public static String NAME = "Graphics on Screen";
	public static String IDENTIFIER = "ActWindowNode";
	public static String DESCRIPTION = "Actions enabling to show a window with background,\ntext and sprite-images onto an attached HDMI or AV screen.";
	
	
	
	// Window functions	
	
	public static ProgramElement create_ElWindCreate()
	{
		useExternalJFX(); // signalize that JFX is needed
		
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		Object[] input = new Object[3];
		return(cont[0] = new FunctionalityContent( "ElWindCreate",
				input,
				() -> {
					
					JFXwindow window;
					double winX = (double) cont[0].getOptionalArgumentValueOR(1, -1.0);
					double winY = (double) cont[0].getOptionalArgumentValueOR(2, -1.0);
					
					if (!Execution.isRunningInGUI() && (JFXwindow.countExistingWindows() == 0)) // No window yet
					{
						synchronized(Actions.class)
						{
							JFXwindow.tempConstruct((double) input[1], (double) input[2], (String) cont[0].getOptionalArgumentValueOR(0, "") ); // The tempConstruct sets the arguments so they can be used by the argument-less constructor of JFXwindow (which is called by JavaFX)
							
							
							new Thread( new Runnable() {
							    @Override
							    public void run() {
									javafx.application.Application.launch(JFXwindow.class); // Launch the window in its own thread					    	
							    }
							}).start();
							
							
							while(!JFXwindow.hasFinishedLaunch()) {try {
								Thread.sleep(10); } catch (InterruptedException e) {}; } // busy wait till finish launch
							
							window = JFXwindow.getTheNewWindow();
							initVariableAndSet(input[0], Variable.JFXwindowType, window);
							
						}
					}
					else // At least one window already exists
					{
						synchronized(Actions.class)
						{
							window = new JFXwindow((double) input[1], (double) input[2], (String) cont[0].getOptionalArgumentValueOR(0, ""));
							
							OtherHelpers.perform(new FutureTask<Object>(() -> {
								try { window.start(new Stage()); } catch (Exception e) { e.printStackTrace(); }
								return(null);
							}));
							
							initVariableAndSet(input[0], Variable.JFXwindowType, JFXwindow.getTheNewWindow());
						}
					}
					
					if (window != null)
						window.setPositionIfValid(winX, winY);						
					
					}));
	}
	public static ProgramElement visualize_ElWindCreate(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Open Window", "Create a window to display things onto the screen.\nA handle to be used in other actions will be saved to the given variable.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Window Identifier");
		vis.addParameter(1, new ValueOrVariable("-1"), "Window Width", "Width of the window or negative value for fullscreen.");
		vis.addParameter(2, new ValueOrVariable("-1"), "Window Height", "Width of the window or negative value for fullscreen.");
		vis.addOptionalParameter(0, new TextOrVariable(), "Title-Bar Text", "Text in the title bar.");
		vis.addOptionalParameter(1, new ValueOrVariable("-1"), "Window X", "X position of the window. Negative value to center.");
		vis.addOptionalParameter(2, new ValueOrVariable("-1"), "Window Y", "Y position of the window. Negative value to center.");
		return(vis);
	}
	
	
	public static ProgramElement create_ElWindClose()
	{
		useExternalJFX();
		
		Object[] input = new Object[1];
		return(new FunctionalityContent( "ElWindClose",
				input,
				() -> {
					}
				,
				() -> {
					
					//if (!((Variable ) input[0]).isType(Variable.JFXwindowType))
					//	Execution.setError("The window identifier you attempt to close does not exist.", true);
					//else
					//{
					((JFXwindow) (input[0])).close();
					
					}));
	}
	public static ProgramElement visualize_ElWindClose(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Close Window", "Close a window opened by 'Open Window'");
		vis.setArgumentDescription(0, new VariableOnly(), "Window Identifier");
		return(vis);
	}
	
	
	
	
	
	public static ProgramElement create_ElWindBckgr()
	{
		useExternalJFX();
		
		Object[] input = new Object[3];
		return(new FunctionalityContent( "ElWindBckgr",
				input,
				() -> {
						
						JFXbackground background = new JFXbackground((String) input[1], (int) input[2] );
						
						initVariableAndSet(input[0], Variable.JFXitemType, background);
						
					}));
	}
	public static ProgramElement visualize_ElWindBckgr(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "New Background", "Creates a background image based on an image.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Item Identifier");
		vis.addParameter(1, new TextOrVariable(), "Image Path", "Absolute or relative path to an image file.\nTip: You can use a res directory to\nautomatically deploy it onto the target device!");
		vis.addParameter(2, new SelectableType(new String[] {"Once in Center", "Stretch to Fit", "Repeat Horizontally", "Repeat Vertically", "Repeat Both Directions"}), "Type", "Describes how the image is used if it does not exactly fit the window.");
		return(vis);
	}
	
	

	/*
	double dur = (double) input[3];
	
	if (dur <= 0)
		Platform.runLater(() -> window.setBackground((String) input[1], (double) input[2]));
	else
	{
		Platform.runLater(() -> window.animateBackground((String) input[1], (double) input[2], (double) input[3]));
		try {
			Thread.sleep((int) dur);
		} catch (InterruptedException e) { e.printStackTrace(); }
	}

	*/

	
	
	public static ProgramElement create_ElWindSprite()
	{
		useExternalJFX();
		
		Object[] input = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElWindSprite",
				input,
				() -> {
					}
				,
				() -> {
					
						JFXimageItem sprite = new JFXimageItem((String) input[1]);
						
						setSpriteOrigin(sprite, cont[0], 0, 1);					
						
						initVariableAndSet(input[0], Variable.JFXitemType, sprite);
					
					}));
	}
	public static ProgramElement visualize_ElWindSprite(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "New Sprite", "Creates a 'Sprite' item to use on a window.\nThat is an image which can be placed at any position\nin the window, scaled, faded, etc.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Item Identifier");
		vis.setArgumentDescription(1, new TextOrVariable(), "Sprite File Path");
		vis.addOptionalParameter(0, new ValueOrVariable(), "X Origin", "X component of the origin. It is an offset always applied to the position of the image.\nDefault value is always the center of the sprite.");
		vis.addOptionalParameter(1, new ValueOrVariable(), "Y Origin", "Y component of the origin.");
		return(vis);
	}
	
	public static ProgramElement create_ElWindShape()
	{
		useExternalJFX();
		
		Object[] input = new Object[4];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElWindShape",
				input,
				() -> {
					}
				,
				() -> {
					
						JFXshape sprite = new JFXshape(
								(int) input[1],
								(double) input[2],
								(double) input[3],
								(String) cont[0].getOptionalArgumentValueOR(0, "000000FF"),
								(String) cont[0].getOptionalArgumentValueOR(1, "00000000"),
								(double) cont[0].getOptionalArgumentValueOR(2, 1.0),
								(Double) cont[0].getOptionalArgumentValueOR(3, null)
								);	
						
						initVariableAndSet(input[0], Variable.JFXitemType, sprite);
						
					}));
	}
	public static ProgramElement visualize_ElWindShape(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "New Shape", "Creates a 'Shape' item to use on a window.\nThose can be lines, rectangles, circles and arcs.. Note that have to place those items with 'Place Item' to provide a location.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Item Identifier");
		vis.addParameter(1, new SelectableType(new String[] {"Line to X/Y", "Arrow to X/Y", "Line to Direction", "Rectangle by Width/Height", "Ellipse by Box", "Circle by Radius", "Arc/Pie"}), "Type", "The type of shape to create.");
		vis.addParameter(2, new ValueOrVariable(), "X, W, Dir or R", "X-coordinate of a box, direction of a line, width of a rectangle or radius of a circle/arc/pie.");
		vis.addParameter(3, new ValueOrVariable(), "Y, H, Len or Angle", "Y-coordinate of a box, length of a line, height of a rectangle or the start-angle of an arc/pie.");
		vis.addOptionalParameter(0, new TextOrVariable("000000FF"), "Line color", "Color of the lines or contours. " + hexadecimalColorDescription);
		vis.addOptionalParameter(1, new TextOrVariable("00000000"), "Filling color", "Color of the filling (default is transparent). " + hexadecimalColorDescription);
		vis.addOptionalParameter(2, new ValueOrVariable(), "Line thickness", "");
		vis.addOptionalParameter(3, new ValueOrVariable(), "Roundness/Arc-Len", "Corner roundness of rectangles or the lngth (in degree) an arc/pie.");
		return(vis);
	}
	
	
	
	
	public static ProgramElement create_ElExtSprite()
	{
		useExternalJFX();
		
		Object[] input = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElExtSprite",
				input,
				() -> {
					}
				,
				() -> {
					
					ImageInput inp = (ImageInput) input[1];
					JFXimageItem sprite = new JFXextImageItem(inp);
					
					setSpriteOrigin(sprite, cont[0], 0, 1);					
					
					initVariableAndSet(input[0], Variable.JFXitemType, sprite);
					
					}));
	}
	public static ProgramElement visualize_ElExtSprite(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "New Stream/Anim", "Creates a 'Sprite' item based on a video stream source (see 'Camera/Video') or animation.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Item Identifier");
		vis.setArgumentDescription(1, new VariableOnly(false, false), "Video Identifier");
		vis.addOptionalParameter(0, new ValueOrVariable(), "X Origin", "X component of the origin. It is an offset always applied to the position of the image.\nDefault value is always the center of the sprite.");
		vis.addOptionalParameter(1, new ValueOrVariable(), "Y Origin", "Y component of the origin.");
		return(vis);
	}

	
	
	private static void setSpriteOrigin(JFXimageItem sprite, FunctionalityContent cont, int indx, int indy)
	{
		int xOrigin = sprite.getOriginalWidth()/2;
		int yOrigin = sprite.getOriginalHeight()/2;
		
		if(cont.hasOptionalArgument(indx))
			xOrigin = getINTparam( cont.getOptionalArgumentValue(indx) );
		if(cont.hasOptionalArgument(indy))
			yOrigin = getINTparam( cont.getOptionalArgumentValue(indy) );
		
		sprite.setOffset(xOrigin, yOrigin);
	}

	
	
	
	
	public static ProgramElement create_ElWindText()
	{
		useExternalJFX();
		
		Object[] input = new Object[4];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElWindText",
				input,
				() -> {
					}
				,
				() -> {
					
						JFXtext text = new JFXtext((String) input[1], getINTparam( input[2] ), (String) input[3] );
						
						if (content[0].hasOptionalArgument(0))
						{
							Variable var = (Variable) content[0].getOptionalArgumentValue(0);
							var.addChangerHook(() -> text.changeText((String) var.getConvertedValue(Variable.textType)));
						}
						
						initVariableAndSet(input[0], Variable.JFXitemType, text);
						
					}));
	}
	public static ProgramElement visualize_ElWindText(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "New Text Label", "Create an item consisting of text to show onto a window.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Item Identifier");
		vis.addParameter(1, new TextOrVariable(), "Text", "Text or variable containing the text to display.");
		vis.setArgumentDescription(2, new ValueOrVariable(), "Text Size");
		vis.addParameter(3, new TextOrVariable("000000FF"), "Text Color", "Color of the text. " + hexadecimalColorDescription);
		vis.addOptionalParameter(0, new VariableOnly(true, false), "Source Var", "If a variable is provided,\nits contents will always be displayed in this label.");
		
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElPlaceltem()
	{
		useExternalJFX();
		
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		Object[] input = new Object[4];
		return(cont[0] = new FunctionalityContent( "ElPlaceltem",
				input,
				() -> {
					}
				,
				() -> {
						JFXwindow window = (JFXwindow) input[0];
						JFXitem item = (JFXitem) input[1];
						
						if (cont[0].hasOptionalArgument(0))
							window.applyItem(item, true, getINTparam( cont[0].getOptionalArgumentValue(0) ));
						else
							window.applyItem(item, true, 0);
						
						Term xTerm = (Term) input[2];
						Term yTerm = (Term) input[3];
						
						item.setPosition(
							getINTparam( xTerm.applyTo((double) item.getPositionX()) ),
							getINTparam( yTerm.applyTo((double) item.getPositionY()) )
							);
					}));
	}
	public static ProgramElement visualize_ElPlaceltem(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Place Item", "Show an item like a sprite, button or text onto a window.\nNote that you can place an item only once at a time.\nIf it already exists on any layer, it will be just moved to the new place.\nTip: To remove an item, just position it outside the window (a position like x = -1000) or use the 'Fade Item' action.");
		vis.addParameter(0, new VariableOnly(), "Window Identifier", "Variable with the window created by the coresponding action.");
		vis.addParameter(1, new VariableOnly(), "Item Identifier", "Variable with the item created by the coresponding action.");
		vis.addParameter(2, new TermValueVarCalc(true), "X Position", "Initial or new X position for the item.\nYou can use terms here like +=10\nto shift by 10 pixels relative to the current position.");
		vis.addParameter(3, new TermValueVarCalc(true), "Y Position", "Initial or new Y position for the item.");
		vis.addOptionalParameter(0, new ValueOrVariable("0"), "Layer", "By using layers you can determine what will be drawn ontop of something else.\nHigher number means ontop.\nDefault (if not using this argument) is 0.");
		return(vis);
	}
	
	
	
	
	
	public static ProgramElement create_ElWindItemFade()
	{
		useExternalJFX();
		
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		Object[] input = new Object[2];
		return(cont[0] = new FunctionalityContent( "ElWindItemFade",
				input,
				() -> {
					}
				,
				() -> {
						JFXitem item = (JFXitem) input[0];
						
						if (cont[0].hasOptionalArgument(0)) // duration given
						{
							double startOpacity = (double) cont[0].getOptionalArgumentValueOR(1, -1.0);
							
							if (startOpacity > 0)
								item.setOpacity(startOpacity % 1);
							
							item.animateOpacity((double) ((Term) input[1]).getRightSide(), (double) cont[0].getOptionalArgumentValue(0), false, (int) cont[0].getOptionalArgumentValueOR(2, 0));
						}
						else
							item.setOpacity( (double) ((Term) input[1]).applyTo(item.getOpacity()));
						
					}));
	}
	public static ProgramElement visualize_ElWindItemFade(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Fade Item", "Set the transparency of an item.\nUsing the optional arguments you can also animate (fade) between two values.");
		vis.addParameter(0, new VariableOnly(), "Item Identifier", "Item to fade.");
		vis.addParameter(1, new TermValueVarCalc(true), "Target Alpha", "Alpha (transparency) to set or fade towards.");
		vis.addOptionalParameter(0, new ValueOrVariable(), "Fade Duration", "Duration of the fading in milliseconds.");
		vis.addOptionalParameter(1, new ValueOrVariable(), "Start Alpha", "Alpha value to begin with. Use -1 for starting with the current value.");
		vis.addOptionalParameter(2, new SelectableType(new String[] {"One go", "Repeat", "Reverse Once", "Reverse and Repeat"}), "Mode", "Default is One go");
		return(vis);
	}
	
	
	public static ProgramElement create_ElWindItemRotate()
	{
		useExternalJFX();
		
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		Object[] input = new Object[2];
		return(cont[0] = new FunctionalityContent( "ElWindItemRotate",
				input,
				() -> {
					}
				,
				() -> {
						JFXitem item = (JFXitem) input[0];

						if (cont[0].hasOptionalArgument(0)) // duration given
						{
							double startAngle = (double) cont[0].getOptionalArgumentValueOR(1, -370.0);
							
							if (startAngle >= -360)
								item.setRotation(startAngle % 360);
							
							
							item.animateRotation((double) ((Term) input[1]).getRightSide(), (double) cont[0].getOptionalArgumentValue(0), (int) cont[0].getOptionalArgumentValueOR(2, 0));
						}
						else
							item.setRotation( (double) ((Term) input[1]).applyTo(item.getRotation()));
						
					}));
	}
	public static ProgramElement visualize_ElWindItemRotate(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Rotate Item", "Set the rotation angle of an item.\nUsing the optional arguments you can also animate between two angles.");
		vis.addParameter(0, new VariableOnly(), "Item Identifier", "Item to rotate.");		
		vis.addParameter(1, new TermValueVarCalc(), "Target Angle", "Angle (in degrees) to set or fade towards.");
		vis.addOptionalParameter(0, new ValueOrVariable(), "Fade Duration", "Duration of the fading in milliseconds.");
		vis.addOptionalParameter(1, new ValueOrVariable(), "Start Angle", "Angle value to begin with. Use < -361 for starting with the current value.");
		vis.setOptionalArgument(2, new SelectableType(new String[] {"One go", "Repeat", "Reverse Once", "Reverse and Repeat"}), "Mode");
		return(vis);
	}
	
	
	public static ProgramElement create_ElWindItemScale()
	{
		useExternalJFX();
		
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		Object[] input = new Object[3];
		return(cont[0] = new FunctionalityContent( "ElWindItemScale",
				input,
				() -> {
					}
				,
				() -> {
						JFXitem item = (JFXitem) input[0];

						if (cont[0].hasOptionalArgument(0)) // duration given
						{
							double startScale = (double) cont[0].getOptionalArgumentValueOR(1, -1.1);
							
							if (startScale >= -1.1)
							{
								switch((int) input[1])
								{
								case 0: item.setScale(startScale, item.getScaleY()); break;
								case 1: item.setScale(item.getScaleX(), startScale); break;
								case 2: item.setScale(startScale, startScale); break;
								}
							}
							
							item.animateScale((double) ((Term) input[2]).getRightSide(), (double) ((Term) input[2]).getRightSide(), (double) cont[0].getOptionalArgumentValue(0), (int) cont[0].getOptionalArgumentValueOR(2, 0), (int) input[1]);
						}
						else
						{
							switch((int) input[1])
							{
							case 0: item.setScale((double) ((Term) input[2]).applyTo(item.getScaleX()) , item.getScaleY()); break;
							case 1: item.setScale(item.getScaleX(), (double) ((Term) input[2]).applyTo(item.getScaleY())); break;
							case 2: item.setScale((double) ((Term) input[2]).applyTo(item.getScaleX()), (double) ((Term) input[2]).applyTo(item.getScaleY())); break;
							}
						}
						
					}));
	}
	public static ProgramElement visualize_ElWindItemScale(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Scale Item", "Set the scale factors of an item.\nUsing the optional arguments you can also animate between two scales.\nNote that you can use negative values to mirror the item.");
		vis.addParameter(0, new VariableOnly(), "Item Identifier", "Item to rotate.");
		vis.setArgumentDescription(1, new SelectableType(new String[] {"X", "Y", "X and Y"}), "Axis");	
		vis.addParameter(2, new TermValueVarCalc(), "Target Scale", "Scale factor to set or transform towards.");
		vis.addOptionalParameter(0, new ValueOrVariable(), "Transit Duration", "Duration of the transformation in milliseconds.");
		vis.addOptionalParameter(1, new ValueOrVariable(), "Start Scale", "Angle value to begin with. Use -2 to start with the current value.");
		vis.setOptionalArgument(2, new SelectableType(new String[] {"One go", "Repeat", "Reverse Once", "Reverse and Repeat"}), "Mode");
		return(vis);
	}
	
	
	
	
	public static ProgramElement create_ElWindItemPosition()
	{
		useExternalJFX();
		
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		Object[] input = new Object[3];
		return(cont[0] = new FunctionalityContent( "ElWindItemPosition",
				input,
				() -> {
					}
				,
				() -> {
						JFXitem item = (JFXitem) input[0];

						if (cont[0].hasOptionalArgument(0)) // duration given
						{
							int startX = (int) (double) cont[0].getOptionalArgumentValueOR(1, -10000);
							int startY = (int) (double) cont[0].getOptionalArgumentValueOR(2, -10000);

							if (startX <= -10000) 
								startX = item.getPositionX();
							if (startY <= -10000) 
								startY = item.getPositionY();
							
							item.setPosition(startX, startY);

														
							item.animatePosition((int) (double) ((Term) input[1]).getRightSide(), (int) (double) ((Term) input[2]).getRightSide(), (double) cont[0].getOptionalArgumentValue(0), (int) cont[0].getOptionalArgumentValueOR(3, 0), 2);
						}
						else
							item.setPosition((int) ((Term) input[2]).applyTo((double) item.getPositionX()), (int) ((Term) input[2]).applyTo((double) item.getPositionY()));
					}));
	}
	public static ProgramElement visualize_ElWindItemPosition(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Move Item", "Set the position of an item that is already visible.\nUsing the optional arguments you can also animate between two positions.");
		vis.addParameter(0, new VariableOnly(), "Item Identifier", "Item to rotate.");
		vis.addParameter(1, new TermValueVarCalc(), "Target X", "New X position to set or move towards.");
		vis.addParameter(2, new TermValueVarCalc(), "Target Y", "New Y position to set or move towards.");
		vis.addOptionalParameter(0, new ValueOrVariable(), "Transit Duration", "Duration of the transformation in milliseconds.");
		vis.addOptionalParameter(1, new ValueOrVariable(), "Start X", "X value to begin with. Use < -10000 to start with the current value.");
		vis.addOptionalParameter(2, new ValueOrVariable(), "Start Y", "Y value to begin with. Use < -10000 to start with the current value.");
		vis.setOptionalArgument(3, new SelectableType(new String[] {"One go", "Repeat", "Reverse Once", "Reverse and Repeat"}), "Mode");
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElWindSwapItem()
	{
		useExternalJFX();
		
		Object[] input = new Object[3];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElWindSwapItem",
				input,
				() -> {
					}
				,
				() -> {
					
						JFXitem itemA = (JFXitem) input[0];
						JFXitem itemB = (JFXitem) input[1];
						boolean adapt = (boolean) input[2];
						
						JFXwindow window = itemA.getWindow();
						
						if (!itemB.isApplied())
						{
							if (adapt)
							{
								itemB.setOpacity(itemA.getOpacity());
								itemB.setPosition(itemA.getPositionX(), itemA.getPositionY());
								itemB.setScale(itemA.getScaleX(), itemA.getScaleY());
								itemB.setRotation(itemA.getRotation());
							}
							
							window.applyItem(itemB, true, itemA.getLayer());
						}
						
						if (cont[0].hasOptionalArgument(0))
						{
							double opacB = itemB.getOpacity();
							double rotB = itemB.getRotation();
							int posXB = itemB.getPositionX();
							int posYB = itemB.getPositionY();
							double scaleXB = itemB.getScaleX();
							double scaleYB = itemB.getScaleY();
							
							if (adapt)
							{
								itemB.setOpacity(itemA.getOpacity());
								itemB.setPosition(itemA.getPositionX(), itemA.getPositionY());
								itemB.setScale(itemA.getScaleX(), itemA.getScaleY());
								itemB.setRotation(itemA.getRotation());
							}
							
							itemA.setOpacity(opacB);
							itemA.setPosition(posXB, posYB);
							itemA.setScale(scaleXB, scaleYB);
							itemA.setRotation(rotB);
							
							window.applyItem(itemA, true, itemA.getLayer());
						}
						else
						{
							if (adapt)
								itemB.animateToOther(itemA, (double) cont[0].getOptionalArgumentValue(1), true, true, true, true, (int) cont[0].getOptionalArgumentValueOR(2, 0));
							itemA.animateToOther(itemB, (double) cont[0].getOptionalArgumentValue(1), true, true, true, true, (int) cont[0].getOptionalArgumentValueOR(2, 0));
						}
					
					}));
	}
	public static ProgramElement visualize_ElWindSwapItem(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Swap Item", "Swap two items including their attributes like scale, position, transparency and rotation.\nIf 'Item B' is not applied to a window yet, it will be applied to the position of 'Item A'.");
		vis.setArgumentDescription(0, new VariableOnly(), "Item Identifier A");
		vis.setArgumentDescription(1, new VariableOnly(), "Item Identifier B");
		vis.addParameter(2, new BooleanOrVariable(), "Adapt B To A", "Apply the attributes of B to A as well.");
		vis.addOptionalParameter(1, new ValueOrVariable(), "Transit Duration", "Duration of the transformation in milliseconds.");
		vis.setOptionalArgument(2, new SelectableType(new String[] {"One go", "Repeat", "Reverse Once", "Reverse and Repeat"}), "Mode");
		
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElBtnItem()
	{
		useExternalJFX();
		
		Object[] input = new Object[4];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElBtnItem",
				input,
				() -> {
					}
				,
				() -> {
					
						JFXbuttonItem btItem = new JFXbuttonItem();
						
						if (input[1] instanceof Variable)
						{
							btItem.updateContent((Variable) input[1]); // simple string
							
							if (cont[0].getOptionalArgTrue(0)) // auto update
								((Variable) input[1]).addChangerHook(() -> 
									Platform.runLater(() -> btItem.updateContent((Variable) input[1]))
								);
						}
						else
							btItem.updateContent((String) input[1]); // simple string
						
											
						btItem.setScale(getINTparam(input[2]), getINTparam(input[3]));
						setSpriteOrigin(btItem, cont[0], 1, 2);
						
						initVariableAndSet(input[0], Variable.JFXitemType, btItem);
					
					}));
	}
	public static ProgramElement visualize_ElBtnItem(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "New Button", "Creates a new Button or transform an existing item into a button.\nA button can be used as triggers for certain events.");
		vis.addParameter(0, new VariableOnly(true, true), "Item Identifier", "Identifier variable for this button.");
		vis.addParameter(1, new TextOrVariable(true, false), "Content", "Simple text or an identifier variable\nto a Text-Label or image to display\ncentered in the button.");
		vis.addParameter(2, new ValueOrVariable("-1"), "Width", "Width of the Button.\nIf you provide a negative value,\nthe button will scale based on the text.");
		vis.addParameter(3, new ValueOrVariable("-1"), "Height", "Height of the Button.\nIf you provide a negative value,\nthe button will scale based on the text.");
		vis.addOptionalParameter(0, new BooleanOrVariable("False"), "Auto Update", "If true and you use a variable (even one with simple text)\nfor the 'content' parameter, the button will always\ndisplay the content of that variable even if changed.");
		vis.addOptionalParameter(1, new ValueOrVariable(), "X Origin", "X component of the origin. It is an offset always applied to the position of the image.\nDefault value if not using this parameter\nis always the center of the sprite.");
		vis.addOptionalParameter(2, new ValueOrVariable(), "Y Origin", "Y component of the origin.");
		
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElWindItemEffect()
	{
		useExternalJFX();
		
		Object[] input = new Object[5];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElWindItemEffect",
				input,
				() -> {
					
						JFXeffect effect = (JFXeffect) input[0];
						if (effect == null) // if null, create and set
							initVariableAndSet(input[0], Variable.JFXeffectType, new JFXeffect((double) input[1], (double) input[2], (double) input[3], (double) input[4]));
						else
							effect.update((double) input[1], (double) input[2], (double) input[3], (double) input[4]);
						
					}));
	}
	public static ProgramElement visualize_ElWindItemEffect(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Item Effect", "Creates a graphical effect that can be applied to items.\nNote that you can change this effect anywhere without needing to reapply it to the item!");
		vis.addParameter(0, new VariableOnly(true, true), "Effect Identifier", "Identifier for this effect.");
		vis.addParameter(1, new ValueOrVariable("0"), "Contrast", "A small positive or negative factor for contrast. For example try 0.2");
		vis.addParameter(2, new ValueOrVariable("0"), "Hue", "A small positive or negative factor for shifting the HUE. For example try 0.2");
		vis.addParameter(3, new ValueOrVariable("0"), "Brightness", "A small positive or negative factor to adjust brightness. For example try 0.2");
		vis.addParameter(4, new ValueOrVariable("0"), "Saturation", "A small positive or negative factor for saturation. For example try 0.2");
		
		return(vis);
	}
	
	
	public static ProgramElement create_ElWindApplItemEffect()
	{
		useExternalJFX();
		
		Object[] input = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElWindApplItemEffect",
				input,
				() -> {
					
						JFXitem item = (JFXitem) input[0];		
						JFXeffect effect = (JFXeffect) input[1];
						item.applyEffect(effect);
						
					}));
	}
	public static ProgramElement visualize_ElWindApplItemEffect(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Apply Effect", "Applies an existing effect to an item.\nNot that if the item already has an effect, it will be replaced.");
		vis.addParameter(0, new VariableOnly(), "Item Identifier", "Identifier for this effect.");
		vis.addParameter(1, new VariableOnly(), "Effect Identifier", "Identifier for this effect.");
		
		return(vis);
	}
	

	public static ProgramElement create_ElWindApplItemCSS()
	{
		useExternalJFX();
		
		Object[] input = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent( "ElWindApplItemCSS",
				input,
				() -> {
					
						JFXitem item = (JFXitem) input[0];		
						item.applyCSS((String) input[1]);
						
					}));
	}
	public static ProgramElement visualize_ElWindApplItemCSS(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Apply CSS", "Applies css design to an item.\nThis is classical CSS used with Java FX. See the official documentation.");
		vis.addParameter(0, new VariableOnly(), "Item Identifier", "Identifier for the CSS.");
		vis.setArgumentDescription(1, new TextOrValueOrVariable(), "CSS");
		
		return(vis);
	}

	
	
	
	public static ProgramElement create_ElNewGraph()
	{
		useExternalJFX();
		
		Object[] input = new Object[4];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElNewGraph",
				input,
				() -> {
					DataSpline spline = (DataSpline) input[1];
					
					String color = (String) content[0].getOptionalArgumentValueOR(0, "000000FF");
					
					JFXgraph graph = new JFXgraph(spline, color, getINTparam( input[2] ), getINTparam( input[3] ), content[0].getOptionalArgTrue(1));
					
					initVariableAndSet(input[0], Variable.JFXitemType, graph);
						
					}));
	}
	public static ProgramElement visualize_ElNewGraph(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "New Graph", "Visualizes a spline in a coordinate system.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Graph Identifier");
		vis.setArgumentDescription(1, new VariableOnly(), "Spline Identifier");
		vis.setArgumentDescription(2, new ValueOrVariable(), "Width");
		vis.setArgumentDescription(3, new ValueOrVariable(), "Height");
		vis.addOptionalParameter(0, new TextOrVariable("000000FF"), "Color", "Color for the spline line. " + hexadecimalColorDescription);
		//vis.setOptionalArgument(1, new BooleanOrVariable("False"), "Mouse Editable", "If true, you can click with the cursor (or finger in case of a touchscreen)\n anywhere inside the graph to add new points.\nRightlick or long press to remove a point.");		
		return(vis);
	}
	
	
	public static ProgramElement create_ElCoordAxis()
	{
		useExternalJFX();
		
		Object[] input = new Object[7];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElCoordAxis",
				input,
				() -> {
					
					JFXgraph graph = (JFXgraph) input[0];
					
					graph.setCoordinatesData(
							getINTparam( input[1] ),
							getINTparam( input[2] ),
							getINTparam( input[3] ),
							getINTparam( input[4] ),
							getINTparam( input[5] ),
							getINTparam( input[6] ));
						
					}));
	}
	public static ProgramElement visualize_ElCoordAxis(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Set Graph Axis", "Sets how the axis displayed for a graph look like.\nAnd what fraction of the graph should be displayed.\nIf this function is not used, no axis will be shown.");
		vis.setArgumentDescription(0, new VariableOnly(), "Graph Identifier");
		vis.addParameter(1, new ValueOrVariable(), "Crop Min X", "Use -1 to show from the very first position.");
		vis.addParameter(2, new ValueOrVariable(), "Crop Max Y", "Use -1 to show to the very last position");
		vis.setArgumentDescription(3, new ValueOrVariable(), "X First Val");
		vis.setArgumentDescription(4, new ValueOrVariable(), "X Last Val");
		vis.setArgumentDescription(5, new ValueOrVariable(), "Y First Val");
		vis.setArgumentDescription(6, new ValueOrVariable(), "Y Last Val");

		return(vis);
	}
	
}
