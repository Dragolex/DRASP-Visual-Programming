package productionGUI.sections.subelements;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.reactfx.util.FxTimer;

import dataTypes.DataNode;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramEventContent;
import dataTypes.contentValueRepresentations.AbstractContentValue;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.SelectableType;
import dataTypes.contentValueRepresentations.TermValueVarCalc;
import dataTypes.contentValueRepresentations.TermValueVarComp;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.minor.FieldHighlight;
import dataTypes.minor.Pair;
import execution.Execution;
import execution.handlers.VariableHandler;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import main.functionality.Functionality;
import main.functionality.Structures;
import productionGUI.ProductionGUI;
import productionGUI.controlers.UndoRedoControler;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.elements.ProgramElementOnGUI;
import productionGUI.tutorialElements.TutorialControler;
import settings.GlobalSettings;
import staticHelpers.FastFadeTransitionHelper;
import staticHelpers.KeyChecker;
import staticHelpers.StringHelpers;

@SuppressWarnings({"deprecation", "rawtypes"})
public class SubElementField extends SubElementName //SubElement
{	
	static private SubElementField globalLastField;
	
	
	AbstractContentValue currentValueRepresentation = null;
	
	boolean editable = false;
	
	boolean hasChanged = true;
	
	boolean showingField = false;
	boolean focusedField = false;

	PopOver infoPop, specialPop;
	Pane contentNodeImage;
	Label infoPopLabel;
	
	ProgramElementOnGUI elOnGui;
	
	SubElementField nextField;
	
	boolean doubleClick = false;
	
	
	String lastValidArgumentString = "";
	boolean neverSet = true;
	
	Label commentTextLabel;
	private Integer[] tooltipState = {0};//new Integer[1];
	
	boolean notLayouted = true;
	
	boolean alwaysPropose = false;
	
	
	public volatile static SubElementField currentlyActive;
	
	
	AutoCompletionTextFieldBinding autocompleteBinding;
	
	
	// Create sub-element for a described value
	public SubElementField(AbstractContentValue currentValue, boolean editable, ProgramElementOnGUI elOnGui)
	{
		super("", false);
		
		this.currentValueRepresentation = currentValue;
		this.editable = editable;
		this.elOnGui = elOnGui;
		
		infoPopLabel = new Label();
		
		infoPop = new PopOver();
		infoPop.setDetachable(false);
		
		infoPop.setContentNode(infoPopLabel);
		infoPopLabel.setStyle(GlobalSettings.tooltipStyle);
		infoPop.setAnimated(true);
		
		infoPop.setArrowLocation(PopOver.ArrowLocation.BOTTOM_LEFT);
		
		KeyChecker.addKeyToCheck(KeyCode.ESCAPE);
		
		//infoPop.setOnHiding((WindowEvent)  -> {if (showingField) hideField(false, true);});
		//infoPop.getOwnerWindow().addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent) -> {System.out.println("SDLKSHFDKUUHDSKLSD"); if (contentField.isFocused()) if (showingField) hideField(false, true);});
		
		if (currentValue.getSepcialTooltipLoad() != null)
		{
			specialPop = new PopOver();
			specialPop.setDetachable(false);
			
			contentNodeImage = currentValue.getSepcialTooltipLoad();
			contentNodeImage.setStyle(GlobalSettings.tooltipStyle);
			
			//specialPop.setContentNode(img);
			
			specialPop.setAnimated(true);
			specialPop.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
			
			specialPop.setAutoFix(false);
		}
		
		
		revisualized();
	}
	
	
	@SuppressWarnings("unchecked")
	@FXML
	protected void initialize()
	{
		update(true);
		
		contentField.setManaged(false);
		contentField.setVisible(false);
		
		//contentField.minWidthProperty().bind(contentLabel.minWidthProperty());
		//contentField.maxWidthProperty().bind(contentLabel.maxWidthProperty());
		contentField.minWidthProperty().bind(contentLabel.widthProperty());
		contentField.maxWidthProperty().bind(contentLabel.widthProperty());

		
		
		List<String> additionalProposedVals = new ArrayList<String>();
		
		

		if (currentValueRepresentation instanceof BooleanOrVariable)
		{
			additionalProposedVals.add("True");
			additionalProposedVals.add("False");
			additionalProposedVals.add("#");
			
			alwaysPropose = true;
		}
		
		if (currentValueRepresentation instanceof VariableOnly)
			alwaysPropose = true;

		
		boolean proposeVariables;
		
		if (currentValueRepresentation instanceof SelectableType)
		{
			alwaysPropose = true;
			proposeVariables = false;
			additionalProposedVals = ((SelectableType) currentValueRepresentation).getValueList();
			if ((contentField.getText() == null) || (contentField.getText() == ""))
				contentField.setText(" ");
		}
		else
			proposeVariables = true;
		
		final List<String> finalAdditionalProposedVals = new ArrayList<>(additionalProposedVals);
		
		//@SuppressWarnings("unchecked")
		autocompleteBinding = new AutoCompletionTextFieldBinding(contentField, new Callback<AutoCompletionBinding.ISuggestionRequest, Collection>() {
		    @Override
		    public Collection call(AutoCompletionBinding.ISuggestionRequest param)
		    {
		    	Collection res = (Collection) (finalAdditionalProposedVals.stream().collect(Collectors.toList()));

		    	if (proposeVariables)
		    	{
			    	Collection proposal = ((Collection) (VariableHandler.getVariableNames().stream()
							.filter(s -> s.startsWith(contentField.getText()))
							.filter(( s -> VariableHandler.getVariableOccurences(s)>0 ))
							.filter(( s -> !s.equals(contentField.getText()) ))
							.sorted()
							.collect(Collectors.toList()) ));
			    	
			    	res.addAll(proposal);
		    	}			    	
		    	
		    	return(res);
		    }
		});
		autocompleteBinding.setDelay(0);
		
		
		
		showField();
		hideField(true, false);
		
		initEvents();		
	}
	
	
	public void revisualized()
	{
		if (globalLastField != null)
			globalLastField.setNextField(this);
		
		globalLastField = this;
	}
	private void setNextField(SubElementField nextField)
	{
		this.nextField = nextField;
	}



	private void update()
	{
		update(false);
	}
	private void update(boolean defineAsLastValid)
	{
		update(defineAsLastValid, false);
	}
	private void update(boolean defineAsLastValid, boolean dontSaveForUndo)
	{
		if (currentValueRepresentation.hasContent())
		{			
			String str = currentValueRepresentation.getDisplayString();
			if (currentValueRepresentation instanceof SelectableType)
			
			if ((currentValueRepresentation instanceof ValueOrVariable)
				|| (currentValueRepresentation instanceof TermValueVarCalc)
				|| (currentValueRepresentation instanceof TermValueVarComp))
				if (!currentValueRepresentation.hasVariable())
				if (str.endsWith(".0"))
					str = str.substring(0, str.length()-2); // Remove unneeded ".0" )
			
						
			contentLabel.setText(str);
			if (defineAsLastValid)
				lastValidArgumentString = currentValueRepresentation.getDisplayString();
		}
		else
		{
			if (lastValidArgumentString.isEmpty())
				contentLabel.setText(GlobalSettings.emptyValueString);
			else
			{
				currentValueRepresentation.checkAndSetFromString(lastValidArgumentString);
				contentLabel.setText(lastValidArgumentString);
			}
		}
		
		VariableHandler.getVariableColorStyle(contentLabel.getText(), contentLabel);
	}
	
	public void replaceValue(String newValue)
	{
		currentValueRepresentation.checkAndSetFromString(newValue);
		update(true, true);
	}
	
	private void initEvents()
	{
		if (!elOnGui.getElement().getContent().isSpecial(Functionality.Comment))
		{
			container.addEventHandler(MouseEvent.MOUSE_ENTERED,
			new EventHandler<MouseEvent>()
			{
			    @Override
			    public void handle(MouseEvent e)
			    {
			    	if (!focusedField)
			    	{
			    		double h = container.getHeight();
			    		container.setMinHeight(h);
			    		container.setMaxHeight(h);
			    		container.setPrefHeight(h);
			    		
			    		showField();
			    	}
			    }
			});
			
			container.addEventHandler(MouseEvent.MOUSE_EXITED,
			new EventHandler<MouseEvent>()
			{
			    @Override
			    public void handle(MouseEvent e)
			    {

					if (currentHighlight != null)
					    if (currentHighlight.tipPop != null)
					       	currentHighlight.tipPop.hide();
					tooltipState[0] = 0; 
			    	
			    	if (!focusedField)
			    		hideField(false, false);
			    	
			    	e.consume();
			    }
			});
			
			
			container.setOnMouseEntered(new EventHandler<MouseEvent>() {
				
			    @Override
			    public void handle(MouseEvent event)
			    {
		    		tooltipState[0] = 1;
			    	FxTimer.runLater(
			    	        Duration.ofMillis(GlobalSettings.tooltipDelay),
			    	        () -> {
			    	        	if ((tooltipState[0] > 0) && ProductionGUI.getStage().isFocused())
			    	        	{
			    	        		if (currentHighlight != null)
			    	        		{
			    	        			if (notLayouted)
			    	        			{
			    	        				currentHighlight.tipNode.applyCss();
			    	        				currentHighlight.tipNode.layout();
				    	        			notLayouted = false;
			    	        			}
			    	        			
			    	    		        Point2D p = contentLabel.localToScreen(contentLabel.getLayoutBounds().getMinX()+contentLabel.getLayoutBounds().getWidth()/2, contentLabel.getLayoutBounds().getMinY());

			    	    		        double x = p.getX();
			    	    		        double y = p.getY()-20;
			    	    		        
			    	    		        if (!currentHighlight.tipPop.isShowing())
			    	    		        	currentHighlight.tipPop.show(container, x, y);			    	    			
			    	        		}
			    	        		
							        tooltipState[0] = 0;
			    	        	}
			    	        });
			    }
			});
			
		}
		else
		{
			container.addEventHandler(MouseEvent.MOUSE_RELEASED, (event) -> {if ((!focusedField) && (event.getButton() == MouseButton.PRIMARY)) { if (doubleClick) { Platform.runLater(() -> {showField(); requestFocus();}); /*showField(); requestFocus();*/ } doubleClick = true; FxTimer.runLater(Duration.ofMillis(GlobalSettings.doubleClickTime), () -> {doubleClick = false;}); }} );
		
		
			Tooltip commentTip = new Tooltip();
			
			AnchorPane box = new AnchorPane();
			VBox innerBox = new VBox();

			//"Comment: \n"
			
			
			innerBox.setSpacing(3);
			commentTextLabel = new Label(contentLabel.getText().equals("Comment Block") ? "" : contentLabel.getText());
			commentTextLabel.setMinWidth(10);
			commentTextLabel.setMaxWidth(250);
			commentTextLabel.setMinHeight(10);
	
			commentTextLabel.setWrapText(true);
			commentTextLabel.getStyleClass().add("mediumText");
			
			Label title = new Label("Comment:");
			title.getStyleClass().add("biggerMediumText");
			
			innerBox.getChildren().add(title);
			innerBox.getChildren().add(commentTextLabel);
			
			
			box.getChildren().add(innerBox);
			
			commentTip.setGraphic(box);
			
						
			box.setMouseTransparent(true);
			commentTip.setStyle(GlobalSettings.tooltipBackgroundStyle);			
			commentTip.setAutoHide(false);
			
			contentLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
				
				
			    @Override
			    public void handle(MouseEvent event)
			    {
			    	tooltipState[0] = 1;
			    	FxTimer.runLater(
			    	        Duration.ofMillis(GlobalSettings.tooltipDelay),
			    	        () -> {
			    	        	if ((tooltipState[0] > 0) && ProductionGUI.getStage().isFocused())
			    	        	{
			    	        		if (commentTip != null)
			    	        		{
			    	        			if(commentTextLabel.getText().isEmpty())
			    	        				return;
			    	        			
			    	        			if (notLayouted)
			    	        			{
				    	        			box.applyCss();
				    	        			box.layout();
				    	        			notLayouted = false;
			    	        			}
			    	        			
								        Point2D p = contentLabel.localToScreen(0, contentLabel.getLayoutBounds().getMaxY());
								        double x = event.getScreenX()-commentTip.getWidth()/2;
								        double y = p.getY() - commentTip.getHeight()-10;
								        
								        /*
								        if ((x + commentTip.getWidth()) > ProductionGUI.getPrimaryScreenBounds().getWidth())
									        x = x - 40 - commentTip.getWidth();
									        */
								        
								        commentTip.show(contentLabel, x, y);
								        if ((p.getY() + commentTip.getHeight()) > ProductionGUI.getPrimaryScreenBounds().getHeight())
								        	commentTip.setY(p.getY() - commentTip.getHeight() - 30);
								        	
			    	        		}			    	        		
			    	        		
							        tooltipState[0] = 0;
			    	        	}
			    	        });
			    }
			});
			
			
			contentLabel.setOnMouseExited((MouseEvent event) -> {
		        if (commentTip != null) commentTip.hide();
		        tooltipState[0] = 0;	
			});
		
		}

		
		
		
		contentField.addEventHandler(MouseEvent.MOUSE_ENTERED,
				new EventHandler<MouseEvent>()
				{
				    @Override
				    public void handle(MouseEvent e)
				    {
				    	unmarkIfOutside = false;
				    }
				});
		contentField.addEventHandler(MouseEvent.MOUSE_EXITED,
				new EventHandler<MouseEvent>()
				{
				    @Override
				    public void handle(MouseEvent e)
				    {
				    	unmarkIfOutside = true;
				    }
				});
		

		
		contentField.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
		    {		    	
		        if (newPropertyValue)
		        	onFocusField();
		        else
		        	outFocusField();
		    }
		});
		
		contentField.textProperty().addListener((observable, oldValue, newValue) -> {
			changedField();
		});


		contentField.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
	        if (KeyCode.ESCAPE == event.getCode())
	        	contentField.getParent().requestFocus();
	    });
		
		
		contentField.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
		    @Override
		    public void handle(KeyEvent keyEvent) {
		        if (keyEvent.getCode() == KeyCode.ENTER)
		        {
		        	changedField();
		        	outFocusField(true);
		        }
		        
		        if (keyEvent.getCode() == KeyCode.TAB)
		        {
		        	changedField();
		        	outFocusField(true);
		        	if (nextField != null)
		        	{
		        	nextField.showField();
		        	nextField.requestFocus();
		        	}
		        }

		    }
		});
		
		
		
	}


	protected void requestFocus()
	{
    	contentField.requestFocus();		
	}



	public void showField()
	{		
		update();	
		
		// Hide label and make field visible
		contentLabel.setManaged(false);
		contentLabel.setVisible(false);
		
		contentField.setManaged(true);
		contentField.setVisible(true);
				
		
		String str = contentLabel.getText();
		if (str.equals(GlobalSettings.emptyValueString))
			contentField.setText("");
		else
			contentField.setText(contentLabel.getText());
		
		showingField = true;
	}
	
	
	public void hideField(boolean defineAsLastValid, boolean byFocus)
	{		
		String last = lastValidArgumentString;
		
		update(true);
		
		// Remove modification
		contentField.setStyle("");
		
		infoPop.hide();
		
		
		// Hide field and make label visible
		contentLabel.setManaged(true);
		contentLabel.setVisible(true);
		
		contentField.setManaged(false);
		contentField.setVisible(false);
		
		if (contentField.isFocused())
			elOnGui.getContainer().requestFocus();
		
		
		showingField = false;
		

		
		if (hasChanged)
		if (Execution.isRunning())
		{
			elOnGui.getElement().getContent().getVisualization().applyGUIargumentData(); // Update even during execution
			FunctionalityContent cont = elOnGui.getElement().getContent();
			
			int argInd = (int) Math.floor((elOnGui.getSubelementIndex(this)-1)/2.0);
			
			Object newVal;
			if (argInd >= cont.getArgumentValues().length)
				newVal = cont.getOptionalArgumentValue(argInd-cont.getArgumentValues().length);
			else
				newVal = cont.getArgumentValue(argInd);
			
			Object[] currentLocalVars = null;
			
			DataNode<ProgramElement> par = elOnGui.getElement().getNode();
			while(!par.isRoot())
			{
				if (par.getData().getContent() instanceof ProgramEventContent)
				{
					for (Object[] lis: ((ProgramEventContent) par.getData().getContent()).associatedEventInstance.allListsssss)
					{
						if (currentLocalVars == null)
							currentLocalVars = lis;
						lis[cont.getPreparedArgumentsLocalOffset()+argInd] = newVal;
					}
					
					// ((ProgramEventContent) par.getData().getContent()).associatedEventInstance.latestListOfLocalArgs[cont.getPreparedArgumentsLocalOffset()+argInd] = newVal;
					((ProgramEventContent) par.getData().getContent()).associatedEventInstance.neededParameters.set(cont.getPreparedArgumentsLocalOffset()+argInd, newVal);
				}
				par = par.getParent();
			}
			
			if (currentLocalVars != null)
				elOnGui.getElement().getContent().resolveVariableArguments(currentLocalVars);

		}	
		
		
		if (specialPop != null)
		{
			specialPop.hide();
			specialPop.setOnHidden((ev) -> {specialPop.setContentNode(null);}); 			
		}
		
		
		if(!last.equals(contentLabel.getText()) && !last.equals(contentLabel.getText()+".0"))
		{
			UndoRedoControler.getSelf().appliedChange(this, last, contentLabel.getText());
			
			if(commentTextLabel != null)
				commentTextLabel.setText(contentLabel.getText());
		}
		
		
		
		if (!neverSet)
		if (byFocus && !TutorialControler.handleParameterFinish(this, elOnGui) && hasChanged)
		{
			contentLabel.setText(last);
			update();
		}
		else
			if (hasChanged) stopHighlight();
		neverSet = false;
		
		currentlyActive = null;
		
		hasChanged = false;
		
		
	}

	
	boolean unmarkIfOutside = false;
	boolean addOutsideClickEvent = true;
	
	private void changedField()
	{
		if (!TutorialControler.handleParameterChange(this))
		{
			contentLabel.setText(lastValidArgumentString);
			update();
			return;
		}
		
		
		String newStr = contentField.getText();
		
		currentValueRepresentation.checkAndSetFromString("");
		Pair<Boolean, String> resp = currentValueRepresentation.checkAndSetFromString(newStr);
		
		if (resp.first) // Value has been checked successfully with a positive result
		{
			contentField.setStyle("");
			if (!Execution.isRunning())
				update();			
		}
		else
			contentField.setStyle("-fx-background-color: shadowColorB;"); // Visualize that there is a problem
		
		
		hasChanged = (!newStr.equals(lastValidArgumentString));
		
		
		int res = resp.second.chars().reduce(0, (a, c) -> a + (c == '\n' ? 1 : 0));		
		switch(res)
		{
		case 0:
		case 1:
			resp.second = " \n"+resp.second+" \n ";
		}
		
		
		if (newStr.length()>7)
			ContentsSectionManager.getSelf().adjustSubelementsSize();
		
		
		if (!newStr.isEmpty())
		if (lastHighlight != null)
			applyHighlightedType(lastHighlight, false);
		
		
		
		boolean renew = StringHelpers.countLines(infoPopLabel.getText()) != StringHelpers.countLines(resp.second);
		
		infoPopLabel.setText(resp.second);
		//if (infoPop.isShowing())
			//infoPop.hide();
		if (!infoPop.isShowing() || renew )
		if (contentField.isManaged())
		{
			infoPop.show(contentField, -GlobalSettings.elementsVgapWhole);
			if (addOutsideClickEvent)
				infoPop.getOwnerWindow().addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent) -> {if (contentField.isFocused()) if (showingField) {if (unmarkIfOutside) hideField(false, true); /*doit = true;*/}});
			addOutsideClickEvent = false;
		}
		
		
		//VariableHandler.getVariableColorStyle(newStr, contentLabel);
		
		elOnGui.stopMarking();
	}
	
	
	
	public int getContentWidth()
	{		
		if (contentField.getWidth() < 30)
			return(contentLabel.getText().length()*GlobalSettings.textfieldLetterWidth);

		if (showingField)
		{
			return (int) (StringHelpers.computeTextWidth(contentField.getFont(),
					contentField.getText(), 0.0D));
		}
		else
		{
			return (int) (StringHelpers.computeTextWidth(contentLabel.getFont(),
					contentLabel.getText(), 0.0D));
		}
	}
	
	
	
	@FXML
	public void onFocusField()
	{
		elOnGui.hideToolTip();
		//doit = false;
		
		if (currentValueRepresentation instanceof BooleanOrVariable)
		{
			String str = contentField.getText();
			contentField.setText("");
			contentField.setText(str);
		}
		
		if (alwaysPropose)
			autocompleteBinding.setUserInput(" "); // enforce propose
		
		/*
		// Swap if boolean
		if (currentValue instanceof BooleanOrVariable)
			if (currentValue.hasContent())
				if (!currentValue.hasVariable())
				{
					currentValue.checkAndSetFromString( ((boolean)currentValue.getOutputValue()) ? "False" : "True" );
					update();
					contentField.setText(currentValue.getDisplayString());
				}
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		*/
		
		
		if (currentHighlight != null)
		{
	        if (currentHighlight.tipPop != null)
	        	currentHighlight.tipPop.hide();
	        tooltipState[0] = 0; 
		}

		
		focusedField = true;
		changedField();
		
		currentlyActive = this;
		
		if (specialPop != null)
		{
			//if (!specialPop.isShowing())
				//if (contentField.isManaged())
					{	
					specialPop.setContentNode(contentNodeImage);
					specialPop.show(contentField);
					}
		}

		
		if (hasHighlight)
			pauseHighlight();
	}
	

	@FXML
	public void outFocusField()
	{
		outFocusField(false);
	}
	
	public void outFocusField(boolean byKeyboard)
	{
		//doit = false;
		
		
		focusedField = false;
		
		currentlyActive = null;
		
		hideField(true, !byKeyboard);
		
		ContentsSectionManager.getSelf().adjustSubelementsSize();
		
		if (hasHighlight)
			continueHighlight();
	}
	
	
	
	
	
	@Override
	public boolean isMarked()
	{
		return(showingField || focusedField);
	}




	public void applyString(String string)
	{
		contentField.setText(string);
		outFocusField();
	}


	boolean hasHighlight = false;
	public void highlight()
	{		
		hasHighlight = true;

		Platform.runLater(() ->
		{			
			if (TutorialControler.attentionRectangle.getParent() != null)
				((HBox) TutorialControler.attentionRectangle.getParent()).getChildren().remove(TutorialControler.attentionRectangle);
			
			container.getChildren().add(TutorialControler.attentionRectangle);
			
			TutorialControler.attentionRectangle.widthProperty().bind(container.widthProperty().subtract(2));
			TutorialControler.attentionRectangle.heightProperty().bind(container.heightProperty().subtract(2));
			
			
			TutorialControler.attentionRectangle.setFill(Color.LIME);
			
			
			if (GlobalSettings.fastFade)
				FastFadeTransitionHelper.fade(TutorialControler.attentionRectangle, GlobalSettings.attentionRectangleMinAlpha, GlobalSettings.attentionRectangleMaxAlpha, (long) GlobalSettings.attentionBlinkDurationFast.toMillis());
			else
			{
				TutorialControler.attentionRectangleTransition.stop();
				TutorialControler.attentionRectangleTransition.setDuration(GlobalSettings.attentionBlinkDurationFast);
				TutorialControler.attentionRectangleTransition.setCycleCount(2);
				TutorialControler.attentionRectangleTransition.play();
				
				TutorialControler.attentionRectangleTransition.setOnFinished((ActionEvent event) -> TutorialControler.attentionRectangleTransition.play());
			}
			
			
			hasHighlight = true;
		});
	}
	private void stopHighlight()
	{
		if (GlobalSettings.fastFade)
		{
			if (FastFadeTransitionHelper.isRunning(TutorialControler.attentionRectangle))
				Platform.runLater(() -> FastFadeTransitionHelper.fadeout(TutorialControler.attentionRectangle, () -> {container.getChildren().remove(TutorialControler.attentionRectangle); hasHighlight = false;}));
		}
		else
		if (TutorialControler.attentionRectangleTransition != null)
			Platform.runLater(() -> TutorialControler.attentionRectangleTransition.setOnFinished((ActionEvent event) -> {container.getChildren().remove(TutorialControler.attentionRectangle); hasHighlight = false;}));
	}
	public void forceStopHighlight()
	{
		Platform.runLater(() -> {
			if (TutorialControler.attentionRectangleTransition != null)
				TutorialControler.attentionRectangleTransition.stop();
			
			if (GlobalSettings.fastFade)
				FastFadeTransitionHelper.stopInstantly(TutorialControler.attentionRectangle);
			
			container.getChildren().remove(TutorialControler.attentionRectangle);
			hasHighlight = false;});
	}

	private void pauseHighlight()
	{
		if (GlobalSettings.fastFade)
			FastFadeTransitionHelper.pause(TutorialControler.attentionRectangle, true);
		else
			Platform.runLater(() -> TutorialControler.attentionRectangleTransition.setOnFinished((ActionEvent event) -> {TutorialControler.attentionRectangleTransition.pause(); TutorialControler.attentionRectangle.setOpacity(0);}));
	}
	private void continueHighlight()
	{
		Platform.runLater(() -> {
			if (GlobalSettings.fastFade)
				FastFadeTransitionHelper.contin(TutorialControler.attentionRectangle);
			else
			{			
				TutorialControler.attentionRectangleTransition.setOnFinished((ActionEvent event) -> TutorialControler.attentionRectangleTransition.play());
				TutorialControler.attentionRectangleTransition.playFromStart();
			}
			});
	}
	
	
	public String getTextDirect()
	{
		return(currentValueRepresentation.getDisplayString());
	}
	
	public String getText()
	{
		hideField(false, false);
		return(contentLabel.getText());
	}



	FieldHighlight lastHighlight = null, currentHighlight = null;
	
	public void applyHighlightedType(FieldHighlight newHighlight, boolean revertIfEdited)
	{
		if (revertIfEdited)
			lastHighlight = currentHighlight;
		
		currentHighlight = newHighlight;
				
		container.setBackground(currentHighlight.bc);

		
		
		/*
		AutoDesign.apply("Thickness", 0.04, "Dist", 0.1, "X", 0.5, "Y", 0.5, "Radius", 1f, 0.005, () -> {
		
			double thickness = AutoDesign.val[0];
			
			Background bc = new Background(new BackgroundFill(
//		    		new RadialGradient(0, 0.1, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
		    		new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
			        		new Stop[] {
			        				new Stop(0, Color.TRANSPARENT),
			        				new Stop(thickness, currentHighlight.col),
			        				new Stop(1.5*thickness, Color.TRANSPARENT),
			        				new Stop(1-1.5*thickness, Color.TRANSPARENT),
		    						new Stop(1-thickness, currentHighlight.col),
			        				new Stop(1, Color.TRANSPARENT)}
			        		)
		    		, new CornerRadii(5), Insets.EMPTY));
	
			
			container.setBackground(bc);
		
		});*/

		
		/*
		Background bc = new Background(new BackgroundFill(
			    		new RadialGradient(0, 0.1, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
				        		new Stop[] { new Stop(0, currentHighlight.col), new Stop(0.8, currentHighlight.col), new Stop(1, Color.TRANSPARENT)}
				        		)
			    		, new CornerRadii(5), Insets.EMPTY));

				
		container.setBackground(bc);
		*/
		//container.setBackground(bc);
	}
	
	
}
