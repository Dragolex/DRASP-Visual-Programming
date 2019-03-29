package productionGUI.additionalWindows;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

import dataTypes.DataNode;
import dataTypes.ProgramElement;
import dataTypes.ProgramTutorialContent;
import execution.handlers.InfoErrorHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import otherHelpers.VisualLineConnection;
import productionGUI.ProductionGUI;
import productionGUI.controlers.StageOrderControler;
import productionGUI.sections.elements.ProgramElementOnGUI;
import productionGUI.sections.elements.VisualizableProgramElement;
import productionGUI.tutorialElements.ButtonTask;
import productionGUI.tutorialElements.ElementTask;
import productionGUI.tutorialElements.ParameterTask;
import productionGUI.tutorialElements.Task;
import productionGUI.tutorialElements.TutExercise;
import productionGUI.tutorialElements.Tutorial;
import productionGUI.tutorialElements.TutorialControler;
import settings.GlobalSettings;
import settings.HaveDoneFileHandler;
import staticHelpers.FileHelpers;
import staticHelpers.GuiMsgHelper;
import staticHelpers.InlineFeatures;
import staticHelpers.KeyChecker;
import staticHelpers.LocationPreparator;
import staticHelpers.OtherHelpers;

public class MainInfoScreen extends Application
{
	private static final int maxEmptyLines = 5;

	private static Stage stage;
	
	volatile private boolean isReady = false;
	private static boolean visible = false;
	
	
	@FXML ScrollPane scrollPane;
	@FXML GridPane mainPane;
	
	@FXML VBox contentPane;
	@FXML Label titleText;
	String title = "Info and Tutorials";
	
	static Pane root;
	
	private static MainInfoScreen self;
	public static MainInfoScreen getSelf()
	{
		return(self);
	}
	
	static Map<VBox, VBox> subBoxes = new HashMap<>();
	
	public MainInfoScreen()
	{
		self = this;
		
        FXMLLoader loader = new FXMLLoader(SettingsMenu.class.getResource("/productionGUI/additionalWindows/ScrollableWindow.fxml"));
        loader.setController(this);
        root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
        stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root, GlobalSettings.productionWindowWidth/1.1, ProductionGUI.getPrimaryScreenBounds().getHeight()/2));
        stage.setX(50);
        stage.setY(200);
        stage.show();
        
        
		KeyChecker.initForStage(stage);
        
		stage.setOnCloseRequest((event) -> close());
		
		//stage.setAlwaysOnTop(true);
		stage.toFront();
		
		visible = true;
		
		contentPane.setMinWidth(GlobalSettings.productionWindowWidth/1.1-120);
		
		root.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle (MouseEvent me) {
		    	/*
		        System.out.println("Clicked on: " + me.getTarget().getClass().getName());
		        */
		    }
		});
		
		StageOrderControler.addAdditionalStage(stage);
		
	}
	
	
	@Override
	public void start(Stage theStage) {}
	
	
	final String tutButtonStyle = "-fx-padding: 0 0 0 10";
	
	
	String tutorialFileName = "InfoPageAndTutorials.txt";
	
	static GridPane tutorialGrid = new GridPane();
	
	
	protected static final String tutMark = "[TUT";

	protected static final String tutInfoKnow = "[KNOW]";
	protected static final String tutInfoGoal = "[GOAL]";
	protected static final String tutInfoLearn = "[LEARN]";
	protected static final String tutInfoHard = "[HARD]";
	
	protected static final String tutUnavailable = "[UNAVAILABLE]";
	
	protected static final String tutNewElementTask = "[ELEMENT";
	protected static final String tutNewParamTask = "[PARAMETER";
	protected static final String tutTaskParamContent = "[CONTENT";
	protected static final String tutPressButtonTask = "[BUTTON";
	protected static final String tutTaskTarget = "[TARGET";
	protected static final String tutTaskParentTarget = "[PARENTTARGET";
	protected static final String tutStepEnd = "[ENDSTEP]";
	protected static final String tutConclusion = "[CONCLUSION]";
	protected static final String tutExerciseStart = "[EXERCISE";
	protected static final String tutEnd = "[ENDTUT]";
	
	protected static final String startStr = "[";
	protected static final String endStr = "]";

	protected final static String commentSymbol = "%";
	protected final static String multilineCommentStart = "/%";
	protected final static String multilineCommentEnd = "\\%";
	
	protected static final String varSymbolReplacer = Pattern.quote("[#]");
	
	
	private int ind = -1;
	int chapterCount = 0, subChapterCount = 0;
	Tutorial currentTutorial = null;
	Task currentTask = null;
	TutExercise currentExercise = null;
	
	
	int errorLine = -1;
	
	Text lastText = null;
	int lastSimpleLine = 1;
	
	int specialLine = -1;
	
	int emptyLines = 0;
	
	boolean inMultilineComment = false;
	
	int skips = 0;
	
	List<String> allLines = null;
	
	
	static int endOfAllTutorials = -1;
	
	
	@FXML
    public void initialize()
    {
		titleText.setText(title);
		
		File tutorialFile = new File( FileHelpers.addSubfile(LocationPreparator.getExternalDirectory(), tutorialFileName) );
		
		contentPane.prefWidthProperty().bind(mainPane.widthProperty().subtract(20));
		contentPane.prefHeightProperty().bind(mainPane.heightProperty().subtract(20));
		
		contentPane.setSpacing(0);
		
		
		finalizations.clear();
		
		
		
		KeyChecker.addKeyToCheck(KeyCode.R);
		if (KeyChecker.isDown(KeyCode.R)) // If R is pressed, the tutorial will be reloaded completely
			endOfAllTutorials = -1;
		

		if (!tutorialFile.exists())
		{
			//close();
			Text tx = new Text("The tutorial file in the external directory could not be found!\nFilename: " + tutorialFileName); 
			tx.getStyleClass().add("titleText"); // Normal line
			contentPane.getChildren().add(tx);
			//GuiMsgHelper.showInfoMessage("The file '" + tutorialFileName + "' is missing from the directory of the runner!" );
			return;
		}
		
		
		new Thread(() -> {
		
			
			allLines = FileHelpers.readAllLines(tutorialFile);
			if (allLines == null)
				return;
			
			
			final String tittleMark = "[T]";
			final String tittleStyle = "";
			
			final String chapterMark = "[C]";
			final String subChapterMark = "[S]";
			
			final String chapterStyle = "-fx-padding: 5 5 5 15";
			final String subChapterStyle = "-fx-padding: 5 5 5 15";
			final String standardStyle = "-fx-padding: 0 0 0 44";
			
			final String imgMark = "[IMG";
			final String imgCenteredMark = "[IMGCENTERED";
			
			
			ElementTask.nameReplacer = "[ELNAME]";
			ParameterTask.nameReplacer = "[ARGNAME]";
			ParameterTask.contentReplacer = "[FIELDCONTENT]";
			ButtonTask.nameReplacer = "[BTNAME]";
			
			
			
			final int conclusionLine = 1;
			final int knowLine = 2;
			final int goalLine = 3;
			final int learnLine = 4;
			final int hardwareLine = 5;
			final int exerciseLine = 6;
			
			
			boolean loadTutorialGrid = tutorialGrid.getChildren().isEmpty() || (endOfAllTutorials == -1);
			
			if (endOfAllTutorials == -1)
			if (!tutorialGrid.getChildren().isEmpty())
			{
				controlersOnGUI.clear();
				associatedRowPlaceHolder.clear();
				includedTutorials.clear();
				
				subBoxes.clear();
				
				if (tutorialGrid.getParent() != null)
					((VBox) tutorialGrid.getParent()).getChildren().remove(tutorialGrid);
			}
				
			
			Label loadingLabel = new Label("LOADING...");
			
			
			if (loadTutorialGrid)
			{			
				tutorialGrid.getChildren().clear();
				tutorialGrid.setHgap(GlobalSettings.elementTreeHgap);
				tutorialGrid.setVgap(GlobalSettings.elementsVgapWhole*2);
				
				tutorialGrid.setStyle(standardStyle);
				
				tutorialGrid.setManaged(false);
				tutorialGrid.setVisible(false);
				
				loadingLabel.getStyleClass().add("titleText");
				loadingLabel.setStyle("-fx-padding: 0 0 0 44; -fx-text-fill: blue; -fx-fill: blue;");
			}
			else
			{
				if (tutorialGrid.getParent() != null)
					((VBox) tutorialGrid.getParent()).getChildren().remove(tutorialGrid);
			}
	
			int linesCount = allLines.size();
			
			List<Integer> skipsTo = new ArrayList<>();
			
			
			ind = 0;
			
			skips = 0;
			
			if (loadTutorialGrid)
			{
				boolean outsideExercises = true;
				boolean inComment = false;
				for(String lineA: allLines)
				{
					if (lineA.startsWith(multilineCommentEnd))
						inComment = false;
					if (lineA.startsWith(multilineCommentStart))
						inComment = true;
					
					if (inComment)
					{
						ind++;
						continue;
					}
					
					/*
					if (outsideExercises)
						if (lineA.startsWith(tutExerciseStart))
							{
								skipsTo.add(ind);
								outsideExercises = false;
							}*/
							
					
					if(lineA.equals(tutConclusion))
					{
						if (outsideExercises)
							skipsTo.add(ind);
						
						outsideExercises = false;
					}
					else
					if(lineA.equals(tutEnd))
					{
						if (outsideExercises)
							skipsTo.add(ind);
						else
							outsideExercises = true;
					}
					
			
					ind++;
				}
			}
			
			
			ind = -1;

			for(; ind < linesCount-1;)
			{					
				Platform.runLater(() -> {
						
						for(int k = 0; k < Math.min(linesCount-ind-1, 30); k++)
						{						
							String line = allLines.get(ind+1);
									
							ind++;
							lastSimpleLine--;
							
							String upperLine = line.toUpperCase();
							
							if (upperLine.startsWith(multilineCommentStart))
								inMultilineComment = true;
							if (upperLine.startsWith(multilineCommentEnd))
							{
								inMultilineComment = false;
								lastSimpleLine++;
								continue;
							}
							if (upperLine.startsWith(commentSymbol) || inMultilineComment)
							{
								lastSimpleLine++;
								continue;
							}
							if (upperLine.startsWith(tittleMark))
							{
								Text tx = new Text(line.substring(tittleMark.length()));
								tx.setTextAlignment(TextAlignment.CENTER);
								tx.setStyle(tittleStyle);
								tx.getStyleClass().add("titleText");
								
								contentPane.getChildren().add(new StackPane(tx));
							}
							else
							if (upperLine.startsWith(chapterMark))
							{
								chapterCount++;
								subChapterCount = 0;
								
								Text tx = new Text(chapterCount + ". " + line.substring(chapterMark.length()));
								tx.getStyleClass().add("chapterText");
								
								HBox bx = new HBox(tx);
								bx.setStyle(chapterStyle);
								
								contentPane.getChildren().add(bx);
							}
							else
							if (upperLine.startsWith(subChapterMark))
							{
								subChapterCount++;
								
								Text tx = new Text(chapterCount + "." + subChapterCount + ". " + line.substring(subChapterMark.length()));
								tx.setStyle(subChapterStyle);
								tx.getStyleClass().add("subChapterText");
								
								HBox bx = new HBox(tx);
								bx.setStyle(subChapterStyle);
				
								contentPane.getChildren().add(bx);
							}
							else
							if (upperLine.startsWith(imgMark))
							{
								String filePath = line.substring(imgMark.length(), line.indexOf(endStr)).trim();
								
								Image img = new Image(filePath);
								ImageView imgV = new ImageView(img);
								
								contentPane.getChildren().add(imgV);
							}
							else
							if (upperLine.startsWith(imgCenteredMark))
							{
								String filePath = line.substring(imgCenteredMark.length(), line.indexOf(endStr)).trim();
								
								Image img = new Image(filePath);
								ImageView imgV = new ImageView(img);
								
								contentPane.getChildren().add(new StackPane(imgV));
							}
							else
							if (upperLine.startsWith(tutMark))
							{
								if (loadTutorialGrid)
								{
									if (tutorialGrid.getParent() == null)
									{
										Label lbA = new Label("BASIC");
										lbA.getStyleClass().add("chapterText");
										StackPane pnA = new StackPane(lbA);
										pnA.setAlignment(Pos.CENTER_LEFT);
										Label lbB = new Label("INTERMEDIATE");
										StackPane pnB = new StackPane(lbB);
										lbB.getStyleClass().add("chapterText");
										pnB.setAlignment(Pos.CENTER_LEFT);
										Label lbC = new Label("ADVANCED");
										StackPane pnC = new StackPane(lbC);
										lbC.getStyleClass().add("chapterText");
										pnC.setAlignment(Pos.CENTER_LEFT);
										
										int offs = 0;//120;
										
										pnA.setTranslateX(-offs);
										pnB.setTranslateX(-offs);
										pnC.setTranslateX(-offs);
										
										tutorialGrid.add(pnA, 0, 0);
										tutorialGrid.add(pnB, 1, 0);
										tutorialGrid.add(pnC, 2, 0);
										
										//pn.setMinWidth(1);
										tutorialGrid.setTranslateX(offs);
										contentPane.getChildren().add(tutorialGrid);
										
										contentPane.getChildren().add(new Label(" "));
										contentPane.getChildren().add(loadingLabel);
										contentPane.getChildren().add(new Label(" "));
										contentPane.getChildren().add(new Label(" "));
										
									}
									
									
									line = line.replaceAll(varSymbolReplacer, GlobalSettings.varSymbol);
									
									specialLine = -1;
									String name = line.substring(tutMark.length(), line.indexOf(endStr)).trim();
									
									int column = Integer.valueOf(line.substring(line.indexOf(endStr)+1).trim());
									
									currentTutorial = TutorialControler.createTutorial(name);
					
									addTutorialField(currentTutorial, column);
								}
								else
								{
									if (tutorialGrid.getParent() == null)
									{
										contentPane.getChildren().add(tutorialGrid);
										contentPane.getChildren().add(new Label(" "));
									}
									
									if (endOfAllTutorials >= 0)
									{
										ind = endOfAllTutorials;
										break;
									}
								}

								
							}
							else
							if (currentTask == null && upperLine.contains(InlineFeatures.greenRect))
							{
								Node nd = InlineFeatures.insertSpecialInline(line, InlineFeatures.greenRect, upperLine.indexOf(InlineFeatures.greenRect), "biggerMediumText", 20);
								nd.setStyle(standardStyle);
								contentPane.getChildren().add(nd);
							}
							else
							if (currentTask == null && upperLine.contains(InlineFeatures.yellowRect))
							{
								Node nd = InlineFeatures.insertSpecialInline(line, InlineFeatures.yellowRect, upperLine.indexOf(InlineFeatures.yellowRect), "biggerMediumText", 20);
								nd.setStyle(standardStyle);
								contentPane.getChildren().add(nd);
							}
							else
							if (currentTask == null && upperLine.contains(InlineFeatures.redRect))
							{
								Node nd = InlineFeatures.insertSpecialInline(line, InlineFeatures.redRect, upperLine.indexOf(InlineFeatures.redRect), "biggerMediumText", 20);
								nd.setStyle(standardStyle);
								contentPane.getChildren().add(nd);
							}
							else
							if (upperLine.startsWith(tutInfoKnow))
							{
								specialLine = knowLine;
							}
							else
							if (upperLine.startsWith(tutInfoGoal))
							{
								specialLine = goalLine;
							}
							else
							if (upperLine.startsWith(tutInfoLearn))
							{
								specialLine = learnLine;
							}
							else
							if (upperLine.startsWith(tutInfoHard))
							{
								specialLine = hardwareLine;
							}
							else
							if (upperLine.startsWith(tutUnavailable) && loadTutorialGrid)
							{
								if (currentTutorial != null)
								{
									currentTutorial.setAvailable(false);
									
									int endind = skipsTo.get(skips);
									currentTutorial.setInnerLineContent(allLines.subList(ind, endind));
									ind = endind-1;
									skips++;
								}
							}
							else
							/*
							if (upperLine.startsWith(tutNewElementTask))
							{
								line = line.replaceAll(varSymbolReplacer, GlobalSettings.varSymbol);
								specialLine = -1;
								String elName = line.substring(tutNewElementTask.length(), line.indexOf(endStr)).trim();
								currentTask = TutorialControler.createElementTask(currentTutorial, elName);
							}
							else
							if (upperLine.startsWith(tutPressButtonTask))
							{
								specialLine = -1;
								String buttonName = line.substring(tutPressButtonTask.length(), line.indexOf(endStr)).trim();
								currentTask = TutorialControler.createButtonTask(currentTutorial, buttonName);
							}
							else
							if (upperLine.startsWith(tutNewParamTask))
							{
								specialLine = -1;
								Integer paramIndex = OtherHelpers.convNumber( line.substring(tutNewParamTask.length(), line.indexOf(endStr)).trim() );
								if (paramIndex == null)
									errorLine = ind;
								
								currentTask = TutorialControler.createParameterTask(currentTutorial, paramIndex);
							}
							else
							if (upperLine.startsWith(tutTaskTarget))
							{
								specialLine = -1;
								Integer targetVal = OtherHelpers.convNumber( line.substring(tutTaskTarget.length(), line.indexOf(endStr)).trim() );
								if (targetVal == null)
									errorLine = ind;
								currentTask.setTargetOnContent(targetVal);
							}
							else
							if (upperLine.startsWith(tutTaskParentTarget))
							{
								specialLine = -1;
								Integer targetVal = OtherHelpers.convNumber( line.substring(tutTaskParentTarget.length(), line.indexOf(endStr)).trim() );
								if (targetVal == null)
									errorLine = ind;
								try
								{
									((ElementTask) currentTask).setTargetParent(targetVal);
								}
								catch(ClassCastException e)
								{
									errorLine = ind;
									//errorLine = "Trying to set a 'PARENTTARGET' for something else than a drag and drop element!";
								}
							}
							else
							if (upperLine.startsWith(tutTaskParamContent))
							{
								specialLine = -1;
								String content = line.substring(tutTaskParamContent.length(), line.indexOf(endStr)).trim();
								if (!(currentTask instanceof ParameterTask))
									errorLine = ind;
								else
									((ParameterTask) currentTask).setDesiredContent(content);
							}
							else
							*/
							if (upperLine.startsWith(tutConclusion))
							{
								currentTask = null;
								specialLine = conclusionLine;
							}
							else
							if (upperLine.startsWith(tutExerciseStart) && loadTutorialGrid)
							{
								currentTask = null;
								String name = line.substring(tutExerciseStart.length(), line.indexOf(endStr)).trim();
								currentExercise = new TutExercise(name);
								currentTutorial.addExercise(currentExercise);
								specialLine = exerciseLine;
							}
							else
							if (upperLine.startsWith(tutStepEnd))
							{
								currentTask = null;
								specialLine = -1;
							}
							else
							if (upperLine.startsWith(tutEnd))
							{
								currentTask = null;
								specialLine = -1;
								
								currentTutorial = null;
								currentExercise = null;
								
								endOfAllTutorials = ind;
							}
							else
							if (upperLine.startsWith(startStr) && !upperLine.equals("[E]"))
							{
								specialLine = -1;
								
								if (currentTutorial != null)
								{
									int endind = skipsTo.get(skips);
									currentTutorial.setInnerLineContent(allLines.subList(ind, endind));
									ind = endind-1;
									skips++;
								}
							}
							else
							{
								line = line.replaceAll(varSymbolReplacer, GlobalSettings.varSymbol);
								
								if (currentTask != null)
									currentTask.addLine(line);
								else
								{
									if ((currentTutorial != null) && loadTutorialGrid)
									{
										switch(specialLine)
										{
										case conclusionLine:
											currentTutorial.addConlusionLine(line);
											break;						
										case knowLine:
											currentTutorial.addKnowLine(line);
											break;
										case goalLine:
											currentTutorial.addGoalLine(line);
											break;
										case learnLine:
											currentTutorial.addLearnLine(line);
											break;
										case hardwareLine:
											currentTutorial.addHardLine(line);
											break;
										case exerciseLine:
											currentExercise.addDescriptionLine(line);
											break;
											
										case -1:
											break;
										}
									}
									else
									{
										if (line.isEmpty())
											emptyLines++;
										else
											emptyLines = 0;
				
										if (emptyLines < maxEmptyLines)
											if ((lastSimpleLine == 0) && (lastText != null))
													lastText.setText(lastText.getText()+"\n" + line);
											else
											{
												lastText = new Text(line); 
												lastText.getStyleClass().add("biggerMediumText"); // Normal line
												
												HBox bx = new HBox(lastText);
												bx.setStyle(standardStyle);
												
												contentPane.getChildren().add(bx);
											}
								}
									
								lastSimpleLine = 1;
							}
						}
				
					}
				});
				
				OtherHelpers.sleepNonException(125); // Give the platform time to execute the runLater-blocks.
			}
			
			
			if (errorLine >= 0)
				GuiMsgHelper.showInfoMessage("ERROR LOADING TUTORIAL!\nLine: " + errorLine);
			
			if (loadTutorialGrid)
				TutorialControler.prepareSpecialColors();
			
			/*
			FxTimer.runLater(
			        Duration.ofMillis(100),
			        () -> scrollPane.setVvalue(0));
			scrollPane.setVvalue(0);
			*/
			
			for(Runnable runnable: finalizations)
			{
				Platform.runLater(() -> runnable.run());
				OtherHelpers.sleepNonException(225);
			}
			
			
			isReady = true;
			
			Platform.runLater(() -> {
				if (contentPane.getChildren().contains(loadingLabel))
					contentPane.getChildren().remove(loadingLabel);
				});
			
			tutorialGrid.setManaged(true);
			tutorialGrid.setVisible(true);
			
			InfoErrorHandler.printDirectMessage("FINISHED LOADING INFO AND TUTORIALS!");
			
			
			//TutorialControler.printKnowings();
			//TutorialControler.printLearnings();
			
			//TutorialControler.printTutorials();

			
		}).start();
	

		
		
		
		/*
		FxTimer.runLater(
		        Duration.ofMillis(3500),
		        () -> {
		        	scrollPane.setVvalue(0);
		        	addConnectionLines(contentPane);
		        	//for(ProgramElementOnGUI ele: controlersOnGUI)
		        		//ele.getBasePane().toFront();
		        });
		 */
		
		

		
		

    	
		
		
				
		
		
		
    }
	
	


	
	
	
	
	
	int[] tutCollumns = new int[] {0, 0, 0};
	Pane[] rowPlaceHolders = new Pane[20];
	static List<ProgramElementOnGUI> controlersOnGUI = new ArrayList<>();
	static List<Pane> associatedRowPlaceHolder = new ArrayList<>();
	static List<Tutorial> includedTutorials = new ArrayList<>();
	
	static List<Runnable> finalizations = new ArrayList<>();
	
	
	private void addTutorialField(Tutorial thisTut, int x)
	{
		VBox tutBox = new VBox();
		tutBox.setSpacing(GlobalSettings.elementsVgapWhole);
		
		
		tutBox.setStyle(tutButtonStyle);
		StackPane pn = new StackPane(tutBox);
		pn.setMaxWidth(400);
		pn.setMinWidth(400);
		
		int row = tutCollumns[x]++;
		if (rowPlaceHolders[row] == null)
		{
			rowPlaceHolders[row] = new Pane();
			rowPlaceHolders[row].setMouseTransparent(true);
			tutorialGrid.add(rowPlaceHolders[row], 5, row+1);
		}
		
		tutorialGrid.add(pn, x, row+1);
		
		finalizations.add(() -> visualizeTutorial(thisTut, tutBox, rowPlaceHolders[row], pn));
	}
	

	
	private void visualizeTutorial(Tutorial thisTut, VBox tutBox, Pane rowPlaceHolder, StackPane pn)
	{
		//OtherHelpers.applyOptimizations(pn);
		
		if (!thisTut.isAvailable())
			pn.setOpacity(0.5);
		
		ProgramTutorialContent content;
		
		VBox tutSubBox = new VBox();
		
		content = new ProgramTutorialContent("tutorialBase", thisTut, thisTut.getName());//, thisTut.getGoal() + " ");
		DataNode<ProgramElement> baseNode = new DataNode<ProgramElement>(null);		
		baseNode.hideChildren();
		VisualizableProgramElement element = new VisualizableProgramElement(content, baseNode, tutBox, rowPlaceHolder, tutSubBox);
		element.getControlerOnGUI().forceCollapse(true);
		element.getControlerOnGUI().getBasePane().setMaxWidth(260);
		element.getControlerOnGUI().getBasePane().setMinWidth(260);
		
		
		tutSubBox.setSpacing(GlobalSettings.elementsVgapWhole);
		tutSubBox.setMinHeight(0);
		
		subBoxes.put(tutSubBox, tutBox);
		
		
		
		DataNode<ProgramElement> innerNode;
		
			List<String> goal = new ArrayList<String>();
			goal.add(thisTut.getGoal());
			content = new ProgramTutorialContent("tutorialSub", thisTut, "Goal", goal);
			DataNode<ProgramElement> goalNode = new DataNode<ProgramElement>(null);
			new VisualizableProgramElement(content, goalNode, tutSubBox, null, null);
			baseNode.addChild(goalNode);
			
			
			content = new ProgramTutorialContent("tutorialSub", thisTut, "Required Knowledge", thisTut.getKnowLines());
			DataNode<ProgramElement> reqNode = new DataNode<ProgramElement>(null);
			new VisualizableProgramElement(content, reqNode, tutSubBox, null, null);
			baseNode.addChild(reqNode);
			
			if (!thisTut.getHardwareLines().isEmpty())
			{
				content = new ProgramTutorialContent("tutorialSub", thisTut, "Required Hardware", thisTut.getHardwareLines());
				DataNode<ProgramElement> hardNode = new DataNode<ProgramElement>(null);
				new VisualizableProgramElement(content, hardNode, tutSubBox, null, null);
				baseNode.addChild(hardNode);
			}
			
			content = new ProgramTutorialContent("tutorialSub", thisTut, "You will Learn", thisTut.getLearnLines());
			DataNode<ProgramElement> learnNode = new DataNode<ProgramElement>(null);		
			new VisualizableProgramElement(content, learnNode, tutSubBox, null, null);
			baseNode.addChild(learnNode);
			
			content = new ProgramTutorialContent("tutorialStart", thisTut, "START");
			DataNode<ProgramElement> start = new DataNode<ProgramElement>(null);
			new VisualizableProgramElement(content, start, tutSubBox, null, null);
			baseNode.addChild(start);
			
			
			controlersOnGUI.add(element.getControlerOnGUI());
			associatedRowPlaceHolder.add(rowPlaceHolder);
			includedTutorials.add(thisTut);
	}
	
	public static void hideSubTutBox(VBox tutSubBox)
	{
		subBoxes.get(tutSubBox).getChildren().remove(tutSubBox);
	}
	public static void showSubTutBox(VBox tutSubBox)
	{
		if (!subBoxes.get(tutSubBox).getChildren().contains(tutSubBox))
			subBoxes.get(tutSubBox).getChildren().add(tutSubBox);
	}

	
	//List<VisualLineConnection> lines = new ArrayList<>();
	static Map<VisualLineConnection, Tutorial> associatedArrowsTo = new HashMap<>();
	static Map<VisualLineConnection, Tutorial> associatedArrowsFrom = new HashMap<>();
	
	private void addConnectionLines(VBox pane)
	{		
		int tuts = includedTutorials.size();
		
		for(int i = 0; i < tuts; i++)
		{
			
			for(String learned: includedTutorials.get(i).getLearnLines())
			{
				for(int j = 0; j < tuts; j++)
				{
					for(String knows: includedTutorials.get(j).getKnowLines())
					{
						if (learned.equals(knows))
						{
							//System.out.println("Drawing line from '" + includedTutorials.get(i).getName() + "' to '" + includedTutorials.get(j).getName() + "'.");
							VisualLineConnection line = new VisualLineConnection(pane, controlersOnGUI.get(i), controlersOnGUI.get(j), TutorialControler.getEquivalentColor(learned), false);
							associatedArrowsFrom.put(line, includedTutorials.get(i));
							associatedArrowsTo.put(line, includedTutorials.get(j));
						}
					}
				}
				
			}
			
		}		
	}
	
	public static void forceCloseAllTutorialInfosOnThisRowExcept(ProgramElementOnGUI thi)
	{
		int i = 0;
		for(ProgramElementOnGUI contr: controlersOnGUI)
		{
			if(contr != thi)
				if (associatedRowPlaceHolder.get(i) == thi.getElement().getTutorialTreeRowPlaceHolder())
				contr.pressedWhole(true, true);
			i++;
		}
	}
	
	
	public static void enteredTutElement(Tutorial tut)
	{
		if (blendIn != null)
		{
			blendIn.stop();
			blendIn = null;
		}
		
		for(Pane p: associatedRowPlaceHolder)
			if (p.getHeight() > 50)
				return;
		
		
		for(ProgramElementOnGUI ele: controlersOnGUI)
			if (((ProgramTutorialContent) ele.getContent()).getTut() == tut)
				ele.getBasePane().setOpacity(1);
			else
				ele.getBasePane().setOpacity(0.5);

		
		int ind = 0;
		for(Tutorial otherTut: includedTutorials)
		{
			for(String knows: tut.getKnowLines())
				if (otherTut.getLearnLines().contains(knows))
				{
					controlersOnGUI.get(ind).getBasePane().setOpacity(1);
					controlersOnGUI.get(ind).markAsExecuting();
				}

			for(String learn: tut.getLearnLines())
				if (otherTut.getKnowLines().contains(learn))
				{
					controlersOnGUI.get(ind).getBasePane().setOpacity(1);
					controlersOnGUI.get(ind).markAsCaution();
				}
			
			ind++;
		}		
		
		if (false)
		{
		for(Entry<VisualLineConnection, Tutorial> dat: associatedArrowsFrom.entrySet())
			if (dat.getValue() == tut)
				dat.getKey().show();
		for(Entry<VisualLineConnection, Tutorial> dat: associatedArrowsTo.entrySet())
			if (dat.getValue() == tut)
				dat.getKey().show();
		}
		
	}
	
	static Timer blendIn;
	
	public static void exitedTutElement(Tutorial tut)
	{
		for(ProgramElementOnGUI ele: controlersOnGUI)
			ele.quitAllMarking();
		
		if (blendIn != null)
			blendIn.stop();
		
		blendIn = FxTimer.runLater(
		        Duration.ofMillis(500),
		        () -> {
		    		for(ProgramElementOnGUI ele: controlersOnGUI)
		    			ele.getBasePane().setOpacity(1);
		        	blendIn = null;
		        });

		
		for(Entry<VisualLineConnection, Tutorial> dat: associatedArrowsFrom.entrySet())
			dat.getKey().hide();
		for(Entry<VisualLineConnection, Tutorial> dat: associatedArrowsTo.entrySet())
			dat.getKey().hide();
	}

	

	public void onScroll()
	{
	}
	
	
	public void close()
	{
		if (!HaveDoneFileHandler.haveDone("Closed Info Window", true))
			GuiMsgHelper.showInfoMessage("Note that you can reopen this info window\nwith the Question-mark button on the main window!");
		
		visible = false;
		StageOrderControler.removeStage(stage);
		stage.close();		
	}

	public void reset()
	{
		stage.toFront();
	}
	
	
	
	public boolean isReady()
	{
		return(isReady);
	}
	
	public static boolean isOpen()
	{
		return(visible);
	}
	
	

	public static void toFront()
	{
		stage.toFront();
	}


	public static Stage getStage()
	{
		return(stage);
	}

	public static Pane getRoot()
	{
		return(root);
	}
	


	static public void resolveTutorial(Tutorial currentTutorial, List<String> lines)
	{
		Task currentTask = null;
		int specialLine = -1;
		int errorLine = -1;
		final int conclusionLine = 15;
		int ind = 0;
		boolean inComment = false;
		
		for(String line: lines)
		{
			String upperLine = line.toUpperCase();
			
			if (upperLine.startsWith(multilineCommentEnd))
			{
				inComment = false;
				continue;
			}
			else
			if (upperLine.startsWith(multilineCommentStart) || inComment)
			{
				inComment = true;
				continue;
			}
			else
			if (upperLine.startsWith(tutNewElementTask))
			{
				line = line.replaceAll(varSymbolReplacer, GlobalSettings.varSymbol);
				specialLine = -1;
				String elName = line.substring(tutNewElementTask.length(), line.indexOf(endStr)).trim();
				currentTask = TutorialControler.createElementTask(currentTutorial, elName);
			}
			else
			if (upperLine.startsWith(tutPressButtonTask))
			{
				specialLine = -1;
				String buttonName = line.substring(tutPressButtonTask.length(), line.indexOf(endStr)).trim();
				currentTask = TutorialControler.createButtonTask(currentTutorial, buttonName);
			}
			else
			if (upperLine.startsWith(tutNewParamTask))
			{
				specialLine = -1;
				Integer paramIndex = OtherHelpers.convNumber( line.substring(tutNewParamTask.length(), line.indexOf(endStr)).trim() );
				if (paramIndex == null)
					errorLine = ind;
				
				currentTask = TutorialControler.createParameterTask(currentTutorial, paramIndex);
			}
			else
			if (upperLine.startsWith(tutTaskTarget))
			{
				specialLine = -1;
				Integer targetVal = OtherHelpers.convNumber( line.substring(tutTaskTarget.length(), line.indexOf(endStr)).trim() );
				if (targetVal == null)
					errorLine = ind;
				currentTask.setTargetOnContent(targetVal);
			}
			else
			if (upperLine.startsWith(tutTaskParentTarget))
			{
				specialLine = -1;
				Integer targetVal = OtherHelpers.convNumber( line.substring(tutTaskParentTarget.length(), line.indexOf(endStr)).trim() );
				if (targetVal == null)
					errorLine = ind;
				try
				{
					((ElementTask) currentTask).setTargetParent(targetVal);
				}
				catch(ClassCastException e)
				{
					errorLine = ind;
					//errorLine = "Trying to set a 'PARENTTARGET' for something else than a drag and drop element!";
				}
			}
			else
			if (upperLine.startsWith(tutTaskParamContent))
			{
				specialLine = -1;
				String content = line.substring(tutTaskParamContent.length(), line.indexOf(endStr)).trim();
				if (!(currentTask instanceof ParameterTask))
					errorLine = ind;
				else
					((ParameterTask) currentTask).setDesiredContent(content);
			}
			/*
			else
			if (upperLine.startsWith(tutConclusion))
			{
				currentTask = null;
				specialLine = conclusionLine;
			}*/
			else
			{
				line = line.replaceAll(varSymbolReplacer, GlobalSettings.varSymbol);
				
				if (currentTask != null)
					currentTask.addLine(line);
				/*
				else
				{
					if (currentTutorial != null)
					{						
						switch(specialLine)
						{
							case conclusionLine:
								currentTutorial.addConlusionLine(line);
							break;
						}
					}
				}
				*/
			}
		
			
			ind++;
		}
		
		if (errorLine != -1)
			GuiMsgHelper.showInfoMessage("Error evaluating tutorial '" +currentTutorial.getName() + "'at line: " + errorLine);
		
	}

	
}
