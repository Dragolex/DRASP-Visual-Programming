package productionGUI.sections.elements;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.reactfx.util.FxTimer;

import dataTypes.ComponentContent;
import dataTypes.DataNode;
import dataTypes.DesignedImage;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.ProgramTutorialContent;
import dataTypes.contentValueRepresentations.AbstractContentValue;
import dataTypes.minor.Pair;
import dataTypes.minor.Quad;
import execution.handlers.InfoErrorHandler;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import main.functionality.FeatureLoader;
import main.functionality.Functionality;
import main.functionality.SharedComponents;
import productionGUI.ProductionGUI;
import productionGUI.sections.ElementToSectionAssociation;
import productionGUI.sections.elementManagers.AbstractSectionManager;
import settings.GlobalSettings;
import staticHelpers.FileHelpers;
import staticHelpers.LocationPreparator;
import staticHelpers.TemplateHandler;

@SuppressWarnings({"deprecation", "unchecked"})
public class VisualizableProgramElement extends ProgramElement
{
	private FunctionalityContent content;
	
	private boolean editable = true;
	private boolean draggable = true;
	private boolean isUserModifiableParentNode = false;
	private boolean copiedWithData = false;
	
	private ProgramElementOnGUI controlerOnGUI = null;
	
	private String name;
	private String toolTipMainText;
	
	private AbstractContentValue[] argumentsData;
	private String[] argumentDescriptions;
	//private String[] lastValidArgumentStrings;

	private AbstractSectionManager originSectionManager = null;
	private Class<AbstractSectionManager> originSectionManagerClass;

	DataNode<ProgramElement> node;
	List<DataNode<ProgramElement>> childrenToInsert;


	private Object specialContent = null;

	private AbstractContentValue[] additionalArgumentsData = new AbstractContentValue[0];
	private String [] additionalArgumentsDescr = new String[0];
	//private int additionalArgumentsCount = 0;
	private int currentlyAddedArguments = 0;
	
	private Class<?> expandableArgumentsType = null;
	private String expandableArgumentsNameBase = "";	
	private int expandableArgumentsCount, expandableArgumentsLimit;
	private String expandableArgumentsButtonName;
	
	private boolean passExpandableArgumentsAsVariable = false;
	private boolean expandableArgumentCanBeEditedByElement = false;
	private String expandableArgumentDefaultValueString = null;
	
	private Object[] defaultAddtionalArgumentsValues = new Object[0];
	
	private boolean forTutorial = false;
	
	private Pane rowPlaceHolder = null;
	
	private int basicArgumentsCount = 0;
	
	private boolean requiresRaspberry = false;
	
	List<Quad<String, String, String, Integer>> tooltipExtensions = new ArrayList<>();
	
	StackTraceElement creatorTrace = null;
	
	// Normal element
	public VisualizableProgramElement(FunctionalityContent content, String name, String toolTip)
	{
		this.content = (FunctionalityContent) content;
		this.content.setVisualization(this);
		this.name = name;
		this.toolTipMainText = toolTip;

		argumentsData = new AbstractContentValue[this.content.getArgumentValues().length];
		argumentDescriptions = new String[this.content.getArgumentValues().length];
		//lastValidArgumentStrings = new String[this.content.getArgumentValues().length];
		
		originSectionManager = ElementToSectionAssociation.getAssociation(content.getFunctionalityName());
		if (originSectionManager != null)
			originSectionManagerClass = (Class<AbstractSectionManager>) originSectionManager.getClass();
		
		
		creatorTrace = Thread.currentThread().getStackTrace()[2];
		
		if (!creatorTrace.getMethodName().endsWith(content.getFunctionalityName()))
			InfoErrorHandler.callPrecompilingError("The function name ('" +creatorTrace.getMethodName()+"') does not match with the correct element name: " + content.getFunctionalityName());
			
	}
	
	// Electronics element
	public VisualizableProgramElement(ComponentContent compContent, String name, String toolTip, Class<?> originSectionManagerClass)
	{
		this.content = new FunctionalityContent(compContent.getComponentName());
		this.specialContent = compContent;
		this.content.setVisualization(this);
		this.name = name;
		this.toolTipMainText = toolTip;

		argumentsData = new AbstractContentValue[0];
		argumentDescriptions = new String[0];
		
		
		this.originSectionManagerClass = (Class<AbstractSectionManager>) originSectionManagerClass;
		
		FxTimer.runLater(
		        Duration.ofMillis(1500),
		        () -> {
		        	this.originSectionManager = AbstractSectionManager.getSpecificSelf(this.originSectionManagerClass);
		        });
	}
	
	
	// Non-draggable element
	public VisualizableProgramElement(FunctionalityContent content, String name, String toolTip, boolean userModifiableParentNode)
	{		
		this.content = content;
		this.content.setVisualization(this);
		this.name = name;
		this.toolTipMainText = toolTip;
		
		argumentsData = new AbstractContentValue[0];
		
		this.draggable = false;
		this.isUserModifiableParentNode = userModifiableParentNode;
		
		originSectionManager = ElementToSectionAssociation.getAssociation(content.getFunctionalityName());
		if (originSectionManager != null)
			originSectionManagerClass = (Class<AbstractSectionManager>) originSectionManager.getClass();
	}
	public VisualizableProgramElement(ComponentContent compContent, String name, String toolTip, boolean userModifiableParentNode, Class<?> originSectionManagerClass) // for electronics
	{
		this.content = new FunctionalityContent(compContent.getComponentName());
		this.content.setVisualization(this);
		this.name = name;
		this.toolTipMainText = toolTip;
		
		argumentsData = new AbstractContentValue[0];
		
		this.draggable = false;
		this.isUserModifiableParentNode = userModifiableParentNode;
		
		this.originSectionManagerClass = (Class<AbstractSectionManager>) originSectionManagerClass;
		
		Platform.runLater(() -> {
		FxTimer.runLater(
	        Duration.ofMillis(1500),
	        () -> {
	        	this.originSectionManager = AbstractSectionManager.getSpecificSelf(this.originSectionManagerClass);
	        });
		});
	}
	
	
	// very simple element used only for the tutorial buttons
	public VisualizableProgramElement(ProgramTutorialContent content, DataNode<ProgramElement> parentNode, Pane contentPane, Pane rowPlaceHolder, VBox tutSubBox)
	{
		if (tutSubBox != null)
		if (content.getTut().getHardwareLines().contains("A Raspberry PI"))
			setRequiringRaspberry();
		
		this.content = content;
		this.name = content.getText();
		this.content.setVisualization(this);
		this.rowPlaceHolder = rowPlaceHolder;
		
		if (name.startsWith("- "))
			this.name = name.substring(2);
		
		this.node = parentNode;
		
		forTutorial = true;
		
		parentNode.setData(this);
		
		controlerOnGUI = new ProgramElementOnGUI(this, parentNode, tutSubBox);
		
		TemplateHandler.injectTemplate("/productionGUI/sections/elements/ProgramElementOnGUItemplate.fxml", controlerOnGUI, contentPane);
		
		controlerOnGUI.getBasePane().setMinWidth(250);
		controlerOnGUI.getBasePane().setMaxWidth(250);

		
		if (!content.getGoal().isEmpty())
			argumentDescriptions = new String[] {content.getGoal()};
		

		/*
		FxTimer.runLater(
		        Duration.ofMillis(1000), () -> {
		        	controlerOnGUI.tutInit();
		});
		*/
		
	}
	


	public void setArgumentDescription(int index, AbstractContentValue startValue, String name)
	{
		if (index > argumentsData.length-1)
		{
			InfoErrorHandler.callPrecompilingError("There is an attempt to set the description for an argument with an index\noutside the reserved array size for the following functionality: " + content.getFunctionalityName() + "\nCheck whether the largest index used in the function 'visualize_" + content.getFunctionalityName() +"'\nmatches the size of the variable-array defined in 'create_"+ content.getFunctionalityName() +"'.");
			return;
		}

		if (argumentsData[index] != null)
		{
			InfoErrorHandler.callPrecompilingError("There is an attempt to set the description for an argument twice \nfor the following functionality: " + content.getFunctionalityName() + "\nCheck whether the indices used in the function 'visualize_" + content.getFunctionalityName() +"'.");
			return;
		}
			
		
		argumentsData[index] = startValue;
		aquireArgumentsDataFromContent(index);
		
		argumentDescriptions[index] = name;
	}
	
	public void addParameter(int index, AbstractContentValue startValue, String name, String detailTooltip)
	{
		addToTooltip(name, detailTooltip, startValue.getDisplayString(), false);
		
		if (detailTooltip.contains(SharedComponents.gpioPinReferenceText))
			startValue.setSpecialTooltipLoad(DesignedImage.getOrCreateByName("gpioOverview"));
		
		setArgumentDescription(index, startValue, name);
	}
	
	
	public int setOptionalArgument(int index, AbstractContentValue contentValue, String argumentName)
	{
		return(addOptionalParameter(index, contentValue, argumentName, null));
	}
	
	public int addOptionalParameter(int index, AbstractContentValue contentValue, String argumentName, String description)
	{
		/*
		if (expandableArgumentsType != null)
		{
			InfoErrorHandler.callPrecompilingError("There is an attempt add an optional argument after expandable arguments!\nIssue for the following functionality: " + content.getFunctionalityName());
			return(index);
		}
		*/
		
		if (description != null)
			addToTooltip(argumentName, description, contentValue.getDisplayString(), true);
		
		if (index < 0) // Index < 0 means that the argument should just be appended
			index = additionalArgumentsData.length;
		
		
		if (index >= additionalArgumentsData.length)
		{
			additionalArgumentsData = java.util.Arrays.copyOf(additionalArgumentsData, index + 1); // Resize
			additionalArgumentsDescr = java.util.Arrays.copyOf(additionalArgumentsDescr, index + 1); // Resize
			
			defaultAddtionalArgumentsValues = java.util.Arrays.copyOf(defaultAddtionalArgumentsValues, index + 1); // Resize
		}
		
		defaultAddtionalArgumentsValues[index] = contentValue.getOutputValue(); // save the default values

		
		
		additionalArgumentsData[index] = contentValue;
		additionalArgumentsDescr[index] = argumentName;
		//additionalArgumentsCount = additionalArgumentsData.length;
		
		return(index);
	}
	

	public void setExpandableArgumentDescription(Class<?> type, String defaultValueString, String numName, int maxArguments, String description)
	{
		addToTooltip(numName, description, defaultValueString, true);
		
		expandableArgumentsType = type;
		expandableArgumentsNameBase = numName;
		expandableArgumentsCount = 0;
		expandableArgumentsLimit = maxArguments;
		
		expandableArgumentDefaultValueString = defaultValueString;
	}

	
	public void setExpandableArgumentDescription(Class<?> type, String defaultValueString, boolean passExpandableArgumentsAsVariable, boolean canBeEditedByElement, String numName, int maxArguments, String description)
	{
		addToTooltip(numName, description, defaultValueString, true);
		
		expandableArgumentsType = type;
		expandableArgumentsNameBase = numName;
		expandableArgumentsCount = 0;
		expandableArgumentsLimit = maxArguments;
		
		this.passExpandableArgumentsAsVariable = passExpandableArgumentsAsVariable;
		this.expandableArgumentCanBeEditedByElement = canBeEditedByElement;
		expandableArgumentDefaultValueString = defaultValueString;
	}
	
	public void addImageToTooltip(String name, String link, String linkShort)
	{
		tooltipExtensions.add(new Quad<String, String, String, Integer>(name, linkShort, link, 2));
	}
	



	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void addToTooltip(String name, String description, String startValue, boolean optional)
	{
		tooltipExtensions.add(new Quad<String, String, String, Integer>(name, description, startValue, optional ? 1 : 0));
		//toolTip += "\n\n" + name + ":\n" + description;
	}	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public String getPotentialArgumentDescription(int index)
	{
		if (index < argumentDescriptions.length)
			return(argumentDescriptions[index]);
		
		
		int s = index-content.getArgumentValues().length;
		
		if (s < additionalArgumentsDescr.length)
			return(additionalArgumentsDescr[s]);
		else
		{
			s -= additionalArgumentsDescr.length;
			
			String descr = expandableArgumentsNameBase;
			if (descr.contains("#"))
				descr = descr.replace("#", String.valueOf(s));
			
			return(descr);
		}
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	private void aquireArgumentsDataFromContent(int index)
	{
		aquireArgumentsDataFromContent(index, 0);
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	private void aquireArgumentsDataFromContent(int index, int optArgs)
	{
		if (index < content.getArgumentValues().length)
		{
			if (content.getArgumentValues()[index] != null)
			{
				if (content.getArgumentValues()[index] instanceof AbstractContentValue)
					argumentsData[index] = (AbstractContentValue) content.getArgumentValues()[index];
				else
					argumentsData[index].checkAndSetForInitTop(content.getArgumentValues()[index]);			
			}
		}
		else
		{
			if (index < argumentsData.length+optArgs)
				return;
			
			int subindex = content.getTotalOptionalOrExpandedArgumentsCount()-1;
			
			expandArgument(false);
			
			if (content.getOptionalArgumentValue(subindex) != null)
			{

				if (content.getOptionalArgumentValue(subindex) instanceof AbstractContentValue)
					argumentsData[index] = (AbstractContentValue) content.getOptionalArgumentValue(subindex);
				else
					argumentsData[index].checkAndSetForInitTop(content.getOptionalArgumentValue(subindex));
			}
			else
				System.out.println("OPW AS NULL");
			
		}
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void aquireOptionalArguments()
	{
		int index = 0;
		if (content.getArgumentValues() != null)
			index = content.getArgumentValues().length;
		
		for(Object obj: content.getTotalOptionalOrExpandedArgumentsArray())
		{
			expandArgument(false);
			argumentsData[index].checkAndSetForInitTop(obj);
			index++;
		}
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	private void aquireOptionalArgumentDirectly(int subind)
	{
		int index = subind + content.getArgumentValues().length;

		if (content.getTotalOptionalOrExpandedArgumentsArray().length > subind)
		{
			expandArgument(false);
			
			if (content.getTotalOptionalOrExpandedArgumentsArray()[subind] != null)
				if (content.getTotalOptionalOrExpandedArgumentsArray()[subind] instanceof AbstractContentValue)
					argumentsData[index] = (AbstractContentValue) content.getTotalOptionalOrExpandedArgumentsArray()[subind];
				else
					argumentsData[index].checkAndSetForInitTop(content.getTotalOptionalOrExpandedArgumentsArray()[subind]);	
		}
		
	}	
	
	
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean canExpand()
	{
		if (currentlyAddedArguments >= additionalArgumentsData.length)
		if (expandableArgumentsType != null)
		{
			if (expandableArgumentsCount >= expandableArgumentsLimit)
				return(false);
		}
		else
			return(false);
		
		return(true);
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean expandArgument(boolean expandForContent)
	{		
		if (currentlyAddedArguments == 0)
			basicArgumentsCount = argumentsData.length;
		
		
		if (currentlyAddedArguments < additionalArgumentsData.length)
		{ // Added standard argument
			
			argumentsData = java.util.Arrays.copyOf(argumentsData, basicArgumentsCount + currentlyAddedArguments+1); // Resize
			argumentDescriptions = java.util.Arrays.copyOf(argumentDescriptions, basicArgumentsCount + currentlyAddedArguments+1); // Resize
	
			argumentsData[basicArgumentsCount + currentlyAddedArguments] = additionalArgumentsData[currentlyAddedArguments];
			argumentDescriptions[basicArgumentsCount + currentlyAddedArguments] = additionalArgumentsDescr[currentlyAddedArguments];
			
			if (defaultAddtionalArgumentsValues != null)
				if (currentlyAddedArguments < defaultAddtionalArgumentsValues.length)
					additionalArgumentsData[currentlyAddedArguments].checkAndSetForInitTop(	defaultAddtionalArgumentsValues[currentlyAddedArguments] );
			
			if (expandForContent)
				content.addAdditionalArgument();
			
			currentlyAddedArguments++;
			
			if (currentlyAddedArguments >= additionalArgumentsData.length)
				if (expandableArgumentsType == null)
					return(false);
		}
		else
		if (expandableArgumentsType != null)
		{ // attempt to add expandable argument
			currentlyAddedArguments++;
			
			return(expandContArgument(expandForContent));
		}
		else
			return(false);
		
		return(true);
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean expandContArgument(boolean expandForContent)
	{
		if (expandableArgumentsCount >= expandableArgumentsLimit)
			return(false);
		
		String descr = expandableArgumentsNameBase;
		if (descr.contains("#"))
			descr = descr.replace("#", String.valueOf(expandableArgumentsCount));
		
		
		try {
			
			// get the constructor
			AbstractContentValue startValue;
			
			startValue = (AbstractContentValue) expandableArgumentsType.newInstance();
			startValue.setSpecial(passExpandableArgumentsAsVariable, expandableArgumentCanBeEditedByElement);
			
			
			if (expandableArgumentDefaultValueString != null)
				startValue.checkAndSetFromString(expandableArgumentDefaultValueString);
			
			argumentsData = java.util.Arrays.copyOf(argumentsData, basicArgumentsCount + currentlyAddedArguments); // Resize
			argumentDescriptions = java.util.Arrays.copyOf(argumentDescriptions, basicArgumentsCount + currentlyAddedArguments); // Resize

			argumentsData[basicArgumentsCount + currentlyAddedArguments -1] = startValue;
			argumentDescriptions[basicArgumentsCount + currentlyAddedArguments -1] = descr;
			
			
			if (expandForContent)
				content.addAdditionalArgument();
						
			
			expandableArgumentsCount++;
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {e.printStackTrace();}
		
		if (expandableArgumentsCount == expandableArgumentsLimit)
			return(false);
		
		return(true);
	}
	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean removeExpandedArgument()
	{
		if (currentlyAddedArguments >= additionalArgumentsData.length)
		{
			currentlyAddedArguments--;
			
			if (expandableArgumentsType != null)
			{
				if (removeExpandedContArgument())
					if (currentlyAddedArguments <= 0)
						return(false);
					else
						return(true);
			}
		}
		else
			currentlyAddedArguments--;
		
		
		if (currentlyAddedArguments<0)
			return(false);
		
		argumentsData = java.util.Arrays.copyOf(argumentsData, basicArgumentsCount + currentlyAddedArguments); // Resize
		argumentDescriptions = java.util.Arrays.copyOf(argumentDescriptions, basicArgumentsCount + currentlyAddedArguments); // Resize
		
		content.removeExpandedArgument();
		
		if (currentlyAddedArguments == 0)
			return(false);
		
		return(true);
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean removeExpandedContArgument()
	{
		if (expandableArgumentsCount > 0)
		{
			argumentsData = java.util.Arrays.copyOf(argumentsData, basicArgumentsCount + currentlyAddedArguments); // Resize
			argumentDescriptions = java.util.Arrays.copyOf(argumentDescriptions, basicArgumentsCount + currentlyAddedArguments); // Resize
			
			content.removeExpandedArgument();
			
			expandableArgumentsCount--;
			return(true);
		}
		else
			return(false);
	}

	
	
	/*
	@Override
	public ProgramElement markAsEvent()
	{
		InfoErrorHandler.callPrecompilingError("Trying to mark a VisualizedProgramElement as an event.\nThis is only possible for a ProgramContent.");
		return(this);
	}
	*/
	

	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isEvent()
	{
		return(content.isEvent());
	}


	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void applyGUIargumentData()
	{
		int i = 0;
		for(AbstractContentValue value: argumentsData)
		{
			System.out.println("NAME of Inside: " + value + " withc ont: " + value.getOutputValue());
			content.setArgumentDirect(i++, value.getOutputValue(), value.passesAsRealVar());
		}
	}
	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void applyGUIargumentData(List<Pair<VisualizableProgramElement, Integer>> missingParameters)
	{		
		int i = 0;
		for(AbstractContentValue value: argumentsData)
		{
			if (!value.hasContent()) // If a variable has not been set
			{
				missingParameters.add(new Pair<VisualizableProgramElement, Integer>(this, i));
				
				if (getControlerOnGUI() != null)
					getControlerOnGUI().markAsCaution();
			}
			
			content.setArgumentDirect(i++, value.getOutputValue(), value.passesAsRealVar());
		}		
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void updateOrigin()
	{
		originSectionManager = ElementToSectionAssociation.getAssociation(content.getFunctionalityName());
		
		if (originSectionManager != null)
			originSectionManagerClass = (Class<AbstractSectionManager>) originSectionManager.getClass();
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public String getExpandableArgumentsButtonName()
	{
		return(expandableArgumentsButtonName);
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public String makeStringRepresentation()
	{
		return(content.makeStringRepresentation());
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean expandableArgumentsPossible()
	{
		return((expandableArgumentsType != null) || (additionalArgumentsData.length>0) );
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public String toString()
	{
		return(name);
	}


	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public ProgramElementOnGUI realizeOnGUI(Pane contentPane, AbstractSectionManager targetSectionManager, int depth, DataNode<ProgramElement> node, boolean onlyPassSubchilds, int reVisualisationIndex)
	{
		this.node = node;
		
		if (content != null)
		if ((content.getCodePageName() == null) || content.getCodePageName().isEmpty())
			content.setCodePageName(ProductionGUI.getVisualizedProgram().getPageNamebyRoot(node.getRoot()));
		
		if (childrenToInsert != null)
			node.setChildren(childrenToInsert);
		childrenToInsert = null;
		
		if (onlyPassSubchilds)
			return(null);
		
		if (controlerOnGUI != null)
			controlerOnGUI.reVisualize(targetSectionManager, reVisualisationIndex, depth, node);
		else
			controlerOnGUI = ProgramElementOnGUI.realizeOnGUI(this, targetSectionManager, reVisualisationIndex, depth, node);
		
		return(controlerOnGUI);
	}



	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public String getFunctionalityName()
	{
		return(content.getFunctionalityName());
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public String[] getArgumentDescriptions()
	{
		return(argumentDescriptions);
	}


	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public FunctionalityContent getContent()
	{
		return(content);
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public String getName()
	{
		return(name);
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean getEditable()
	{
		return(editable);
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isDraggable()
	{
		return(draggable && (!forTutorial));
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean getIsUserModifiableParentNode()
	{
		return(isUserModifiableParentNode);
	}


	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public AbstractContentValue[] getArgumentsData()
	{
		return(argumentsData);
	}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public Object[] getArgumentValues()
	{
		return(content.getArgumentValues());
	}


	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setOriginSection(AbstractSectionManager origin)
	{
		originSectionManager = origin;
		if (originSectionManager != null)
			originSectionManagerClass = (Class<AbstractSectionManager>) originSectionManager.getClass();
		
		ElementToSectionAssociation.addAssociation(content.getFunctionalityName(), origin);
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public AbstractSectionManager getOriginSection()
	{
		return(originSectionManager);
	}
	public Class<?> getOriginSectionClass()
	{
		return(originSectionManagerClass);
	}
	

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean canHaveChildElements()
	{
		return(content.canHaveChildElements() || isUserModifiableParentNode);
	}
	

	// Create a new instance
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public VisualizableProgramElement duplicateWithoutData()
	{
		FunctionalityContent newContent = (FunctionalityContent) Functionality.createProgramContent(content.getFunctionalityName());
		VisualizableProgramElement visElement = Functionality.visualizeElementContent(newContent);
		
		visElement.setOriginSection(originSectionManager); // set the origin
		
		return(visElement);
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public VisualizableProgramElement duplicateWithData()
	{
		VisualizableProgramElement visElement = duplicateWithoutData();
		
		List<DataNode<ProgramElement>> children = new ArrayList<DataNode<ProgramElement>>();
		
		
		for(DataNode<ProgramElement> subnode: node.getChildrenAlways())
		{
			VisualizableProgramElement newVis = ((VisualizableProgramElement) subnode.getData()).duplicateWithData();
			children.add(new DataNode<ProgramElement>(newVis));
		}

		if (!children.isEmpty())
			visElement.addChildrenToInsert(children);
		
		
		int ind = 0;
		for(AbstractContentValue arg: argumentsData)
		{
			arg = arg.cloneThis();
			visElement.getContent().setArgumentDirect(ind, arg, arg.passesAsRealVar()); // copy the arguments data
			
			visElement.aquireArgumentsDataFromContent(ind, additionalArgumentsData.length);
			
			ind++;
		}
		
		
		ind = 0;
		for(AbstractContentValue optionalArg: additionalArgumentsData)
		{
			visElement.aquireOptionalArgumentDirectly(ind);
			ind++;
		}

		
		visElement.setCopiedWithData();

		return(visElement);
	}
	
	


	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setCopiedWithData()
	{
		copiedWithData = true;
	}
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean getCopiedWithData()
	{
		return(copiedWithData);
	}


	@Override
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void setUndeletable(boolean undeletable)
	{
		if (content != null)
			content.setUndeletable(undeletable);
	}


	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public DataNode<ProgramElement> getNode()
	{
		return(node);
	}


	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void addChildrenToInsert(List<DataNode<ProgramElement>> childrenToInsert)
	{
		this.childrenToInsert = childrenToInsert;
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public ProgramElementOnGUI getControlerOnGUI()
	{
		return(controlerOnGUI);
	}


	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean isConditionalElement()
	{
		return(content.isConditionalElement());
	}

	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!	
	public Pane getTutorialTreeRowPlaceHolder()
	{
		return(rowPlaceHolder);
	}
	
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void resetIfPossible(int index) {}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public void reloadIfPossible(int index) {}
	
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public boolean getRequiresRaspberry()
	{
		return(requiresRaspberry);
	}
	
	
	
	public VisualizableProgramElement setRequiringRaspberry()
	{
		requiresRaspberry  = true;
		return(this);
	}

	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	public Tooltip makeToolTip()
	{
		if (toolTipMainText == null)
			return(null);
		
		String[] r = toolTipMainText.split("\\.");
		
		String tooltipTitle = toolTipMainText;
		String tooltipBody = "";
		if (r.length > 0)
		{
			tooltipTitle = r[0]+".";
			
			int pos = toolTipMainText.indexOf(".");
			if (pos >= 0)
				if (pos < (toolTipMainText.length()-1)) 
					tooltipBody = toolTipMainText.substring(pos+2);			
		}
		
		
		VBox box = new VBox();
		box.setSpacing(3);
		
		box.setMouseTransparent(true);
		
		Label title = new Label(tooltipTitle);
		title.getStyleClass().add("biggerMediumText");
		box.getChildren().add(title);
		
		if (!tooltipBody.isEmpty())
		{
			Label body = new Label(tooltipBody);
			body.getStyleClass().add("mediumText");
			body.setStyle("-fx-padding: 0 0 0 20");
			box.getChildren().add(body);
		}
		else
			if (!tooltipExtensions.isEmpty()) box.getChildren().add(new Label(" "));
		
		boolean addedOptional = false;
		
		if (!tooltipExtensions.isEmpty())
		{
			if (tooltipExtensions.get(0).fourth == 0)
			{
				box.getChildren().add(new Label(" "));
				Label par = new Label("PARAMETERS");
				par.getStyleClass().add("biggerMediumText");
				box.getChildren().add(par);
				box.getChildren().add(new Label(" "));
			}
			
			boolean deleteLast = false;
			
			for(Quad<String, String, String, Integer> dat: tooltipExtensions)
			{
				switch(dat.fourth)
				{
				case 1:
					if (!addedOptional)
					{
						box.getChildren().add(new Label(" "));
						Label opt = new Label("OPTIONAL PARAMETERS");
						opt.getStyleClass().add("biggerMediumText");
						box.getChildren().add(opt);
						box.getChildren().add(new Label(" "));
						addedOptional = true;
					}
					
				case 0:
					
					Label tx;
					
					if ((dat.third != null) && (!dat.third.isEmpty()))
						tx = new Label("     " + dat.first+" - Default value: " + dat.third);
					else
						tx = new Label("     " + dat.first+":");
					
					tx.getStyleClass().add("mediumTextBold");
					box.getChildren().add(tx);
					
					for(String str: dat.second.split("\\n"))
					{
						tx = new Label("     " + "     " + str);
						tx.getStyleClass().add("mediumText");
						box.getChildren().add(tx);
					}
					
					/*
					if ((dat.third != null) && (!dat.third.isEmpty()))
					{
						tx = new Label("     " + "Default:");
						tx.getStyleClass().add("mediumTextBold");
						box.getChildren().add(tx);
				
						tx = new Label("     " + "     " + dat.third);
						tx.getStyleClass().add("mediumText");
						box.getChildren().add(tx);
					}
					*/
					
					box.getChildren().add(new Label(" "));
					deleteLast = true;
					
					break;
					
					
				case 2: // image

					deleteLast = false;
					// TODO

					
					break;
				}
			}
			
			if (deleteLast)
				box.getChildren().remove(box.getChildren().size()-1);
		}
		
		
		box.setMouseTransparent(true);
		
		
		Tooltip tooltip = new Tooltip();
		tooltip.setGraphic(box);		
		
		tooltip.setStyle(GlobalSettings.tooltipBackgroundStyle);
		
		return(tooltip);
	}
	
	public String getEclipseCodeLink()
	{
		if (creatorTrace == null)
			return("");
		
		try {
			Class<?> cl = Class.forName(creatorTrace.getClassName());
			return(cl.getSimpleName() + ".java:" + creatorTrace.getLineNumber());
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return("");
	}
	
	
	public Object getSpecialContent()
	{
		return(specialContent);
	}
	
	
	
	public String getSurroundingJavaFile(String prestring)
	{
		String classFilePath = FeatureLoader.creatingMethods.get(getFunctionalityName()).getDeclaringClass().getName();

		String javaFile = LocationPreparator.getBaseDirectory();
		
		String[] pathParts = classFilePath.split("\\.");
		for(int i = 0; i < pathParts.length-1; i++)
		{
			javaFile = FileHelpers.addSubfile(javaFile, pathParts[i]); // add all aprts except for the last
		}
		
		javaFile = FileHelpers.addSubfile(javaFile, prestring + pathParts[pathParts.length-1] + ".java" ); // add the prestring, the class name and the file termination
		
		//classFilePath = classFilePath.replace(".", File.separator);
		//String javaFile = FileHelpers.addSubfile(LocationPreparator.getBaseDirectory(), classFilePath)+".java";
		
		return(javaFile);
	}



	
	
	/*
	@Deprecated // Marked as depreciated so one doesn't use it accidentally in Functionality-Classes!
	@Override
	public ProgramElement recreateContent()
	{
		content = (ProgramContent) content.recreateContent();
		return(this);
	}
	*/
	
}
