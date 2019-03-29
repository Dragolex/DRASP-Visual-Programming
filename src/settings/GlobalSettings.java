package settings;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.scene.CacheHint;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import productionGUI.sections.elements.ProgramElementOnGUI;
import productionGUI.targetConnection.ConnectedLocal;
import productionGUI.targetConnection.TargetConnection;

public class GlobalSettings
{
	public static boolean guiDebug = false;
	
	public static final int productionWindowWidth = 1600;
	public static final int productionWindowHeight = 1024;
	
	public static final int extraWindowWidth = 500;
	public static final int extraWindowHeight = 400;
	public static final int maxMessageHeight = 600;
	
	public static final boolean menusAlwaysOnTop = true;
	
	public static final String standardProgramFileTermination = "dra";
	public static final String datFileTermination = "dat";
	public static final String titleLineBaseString = "DRASP - ";
	public static final String rightTypeIndicator = "DRASP Program";
	
	public static final String programPageSectionStartString = "PROGRAM ELEMENTS PAGE";
	public static final String customElementsSectionStartString = "CUSTOM ELEMENTS FOR";
	public static final String collapsedNodesString = "COLLAPSED NODES BITMAP";
	public static final String treeVersionLine = "STANDARD ELEMENTS VERSION";

	public static boolean dynamicElementWidth = false;
	
	public static boolean alternativeLayout = true;	
	public static boolean smoothDragTransition = true;


	public static final String argumentElementSymbol = "|";
	public static final char argumentElementSymbolChar = argumentElementSymbol.charAt(0);
	
	public static final char indentationSymbol = '\t';
	public static final String indentationSymbolStr = Character.toString(GlobalSettings.indentationSymbol);
	public static final String outcommentedSymbol = "-";
	public static final String breakpointSymbol = "§";
	public static final String collapsedElementSymbol = "°";
	public static final String fixedArgumentSymbol = "^";
	public static final char fixedArgumentSymbolChar = fixedArgumentSymbol.charAt(0);
	
	//public static final String firstExpandablArgSymbol = argumentElementSymbol + "E";
	
	public static final String passesAsRealVarSymbol = "~";
	public static final char passesAsRealVarSymbolChar = passesAsRealVarSymbol.charAt(0);
	public static final String passesAsRealVarAndEleEditVarSymbol = "&";
	public static char passesAsRealVarAndEleEditVarSymbolChar = passesAsRealVarAndEleEditVarSymbol.charAt(0);
	

	public static final String varSymbol = "#";
	
	public static final int doubleClickTime = 300;
	public static final int waitForEventsEndTime = 1500;
	public static final long maxTaskDurationTilCheck = waitForEventsEndTime / 2;

	public static int maxPossibleExternalStages = 20;

		
	public static final String activeKeyword = "Active";
	public static final String inactiveKeyword = "Deactivated";
	
	public static final int elementTreeHgap = 35;
	public static final int elementsVgap = 1;
	public static final int elementsVgapWhole = 5;
	public static final int elementDragOffElementDistance = 3 * elementsVgapWhole;
	
	public static final int textfieldLetterWidth = 8;
	public static final int minSubelementFieldWidth = 40;
	public static final int maxSubelementFieldWidth = 160;
	public static final int commentFieldWidth = 4 * maxSubelementFieldWidth - 28;
	public static final int optimalSubelementFieldWidth = 100;
	
	public static final int maxConsoleNodes = 300;
	
	public static final int constantCheckDelay = 8;
	
	public static final String tooltipStyle = "-fx-font-size: 15; -fx-font-weight: bold; -fx-background-color: rgba(0, 90, 204, 0.6); -fx-padding: 7; -fx-margin: 0; -fx-fill: white; -fx-text-fill: white; -fx-border-color: elementBorderColor; -fx-border-width: 0.5; -fx-background-radius: 5; -fx-border-radius: 5;";
	public static final String tooltipBackgroundStyle = "-fx-background-color: rgba(0, 90, 204, 0.6); -fx-padding: 7; -fx-margin: 0; -fx-text-fill: white; -fx-border-color: elementBorderColor; -fx-border-width: 0.5; -fx-background-radius: 5; -fx-border-radius: 5;";
	public static final long setupDelay = 500;
	public static final String emptyValueString = "";//"      ";
	public static final String variableDescriptionText = "Note that you can use a 'variable' to influence the behavior from elsewere in the program.\nTo achieve this type '"+varSymbol+"' and a text without special symbols behind.\nFor example " + GlobalSettings.varSymbol + "myVar.\nUsing the 'Variables' structures you can set the content of such a variable.";
	public static final String argumentSeparator = ": ";
	public static final String notSetSymbol = "$NOTSET$";
	

	public static final long tooltipDelay = 550;
	public static final int collapseDuration = 500;
	public static final int attentionBlinkDurationVal = 500;
	public static final double attentionRectangleMinAlpha = 0.2; // 0.15
	public static final double attentionRectangleMaxAlpha = 0.65; // 0.55
	public static final double tutorialAttentionAlpha = 0.25;

	
	public static final long runningCheckInterval = 800;
	
	public static final float scrollFractionLimit = 0.05f;
	public static double scrollHorizonallyFrom = 0.8;	
	
	
	public static final String deployedExecutorName = "DraspExecutor.jar";
	
	public static TargetConnection destination = null;
	public static TargetConnection localDestination = new ConnectedLocal("", "");
	
	public static final String errorSymbol = "|E|";
	public static final String execInfoSymbol = "|I|";
	public static final String envSymbol = "|V|";
	public static final String minorErrorSymbol = "|Y|";
	
	public static final String startExecutionSignal = "|SI_S|";
	public static final String errorSignal = "|SI_R|";
	public static final String endEventSignal = "|SI_E|";
	public static final String variableSetSignal = "|SI_V|";
	public static final String innerDataChangeSignal = "|SI_D|";

	
	public static final String deployTempStr = "_deploy_temp";

	
	public static final String keyCheckForRunning = "CHECKRUNNING";
	public static final String keyVisualDebug = "VISUALDEBUG";
	public static final String keyEventDebug = "EVENTDEBUG";
	public static final String keyFullDebug = "FULLDEBUG";
	public static final String keyExternalTracking = "TRACKIT";
	
	public static final String varTypeStarter = "||#||";
	
	public static boolean visualDebug = false;
	public static boolean eventDebug = false;
	public static boolean fullDebug = false;
	
	public static boolean showConsoleWindow = true;
	public static boolean showDebugWindow = true;
	
	public static String noOrigRemovalAttribute = "noOrigRemoval";


	
	public static final DecimalFormat doubleFormatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US); //new DecimalFormat("##.#########", null); //"##.#######"
	public static final String debugButtonStartSymbol = "|°|";
	
	public static final long backToFrontDelay = 300;
	
	public static final boolean cacheOptimizations = true;
	public static final boolean applyDragAndDropStyle = false;
	public static final boolean fastFade = true;

	public static final double ToggleScale = 0.7;


	
	public static final int maxDragDropOffsetX = -100;
	public static final int minDragDropOffsetX = -250;
	
	public static final int maxDragDropOffsetY = 20;
	public static final int minDragDropOffsetY = -20;
	
	
	
	public static KeyCode layoutAlternativesKey = KeyCode.F1;

	public static Modifier shortcutBaseKey = KeyCombination.CONTROL_DOWN;
	public static KeyCode directSaveKey = KeyCode.S;
	public static KeyCode copyElementsKey = KeyCode.C;
	public static KeyCode cutElementsKey = KeyCode.X;
	public static KeyCode insertElementsKey = KeyCode.V;

	
	
	public final static List<ExtensionFilter> extensionFilters = new ArrayList<ExtensionFilter>() {{ add(new FileChooser.ExtensionFilter(standardProgramFileTermination.toUpperCase(), "*." + standardProgramFileTermination.toLowerCase())); add(new FileChooser.ExtensionFilter(standardProgramFileTermination.toUpperCase(), "*." + standardProgramFileTermination.toUpperCase())); }};
	
	
	public final static Duration attentionBlinkDuration = Duration.millis(attentionBlinkDurationVal);
	public final static Duration attentionBlinkDurationFast = Duration.millis(350);
	
	public final static CacheHint chacheOptimizationType = CacheHint.SPEED;

	public static final String userusedNewlineSymbol = "\\n";
	
	
	public static final java.time.Duration windowToFrontDelay = java.time.Duration.ofMillis(500);
	

	public static DropShadow hoverObjectEffect = new DropShadow();
	public static ColorAdjust clickObjectEffect = new ColorAdjust(0, 0, 0.4, 0);

	
	public static String ProgramHintsText = "You can begin to design your program now\n\n" + 
			"1. Drag and drop an Event: Events -> Basic -> 'Initialization'\n" + 
			"2. Drag and drop an Action inside the Event: Actions -> Basic -> Show Message\n" + 
			"3. Provide a text parameter to the Action\n" + 
			"4. Press 'Simulate'\n\n\n"+ 
			"Shortcuts:\n\n" + 
			"- Hold <SHIFT> when dragging to copy\r\n" + 
			"- Right-click to disable an element\r\n" + 
			"- Hold <SHIFT> during right-click to enable or disable a break-point.\n" + 
			"- Hold <SHIFT> during left-click to change parameter view.\n" + 
			"";

	
	//public static String[] mainCSSsheets = {BaseCss.class.getResource("ThemeColors.css").toExternalForm(),
	//		BaseCss.class.getResource("CustomStyles.css").toExternalForm(),
	//		BaseCss.class.getResource("StaticStyles.css").toExternalForm()};
	
	
	public static void init()
	{
		doubleFormatter.setGroupingUsed(false);
		
		hoverObjectEffect.setColor(Color.rgb(255, 255, 255, 0.75));
		hoverObjectEffect.setOffsetX(0f);
		hoverObjectEffect.setOffsetY(0f);
		hoverObjectEffect.setRadius(2);
		hoverObjectEffect.setSpread(0.95);
		
		
		clickObjectEffect.setInput(hoverObjectEffect);
	}
	
	public static Map<String, String> getSaveableSettings()
	{
		Map<String, String> settings = new HashMap<>();
		
		settings.put("alternativeLayout", alternativeLayout ? "true" : "false");
		settings.put("smoothDragTransition", smoothDragTransition ? "true" : "false");
		
		return(settings);
	}
	
	
	public static void readSaveableSettings(Map<String, String> settings)
	{
		alternativeLayout = settings.getOrDefault("alternativeLayout", alternativeLayout ? "true" : "false").equalsIgnoreCase("true");
		smoothDragTransition = settings.getOrDefault("smoothDragTransition", smoothDragTransition ? "true" : "false").equalsIgnoreCase("true");
	}
	
}
