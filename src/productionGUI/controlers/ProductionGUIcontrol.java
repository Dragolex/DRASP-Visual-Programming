package productionGUI.controlers;

import java.io.IOException;

import execution.handlers.InfoErrorHandler;
import javafx.fxml.FXML;
import javafx.fxml.LoadException;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import productionGUI.ProductionGUI;
import productionGUI.sections.elementManagers.ActionsSectionManager;
import productionGUI.sections.elementManagers.ConditionsSectionManager;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.elementManagers.EventsSectionManager;
import productionGUI.sections.elementManagers.GeneralSectionManager;
import productionGUI.sections.elementManagers.StructuresSectionManager;
import settings.GlobalSettings;
import staticHelpers.Assert;
import staticHelpers.OtherHelpers;
import staticHelpers.TemplateHandler;

/**
 * This is the control class for the MainInterface.fxml
 * It constructs the basic of the interface by creating the sections for machines, variables and histories.
 *
 * @author Alexander Georgescu
 */
public class ProductionGUIcontrol {

	@FXML private Pane rootPane;
	@FXML private Pane mainPane;
	@FXML public static Pane topPane;
	
	private static ProductionGUIcontrol self;

	private boolean alreadyInitializedOnce = false;
	
	// Section controlers
	GeneralSectionManager actMng, condMng, evMng, contMng, strMng;
	ButtonsRegionControl btCtrl;
	
	
	@FXML
	protected void initialize() throws IOException
	{
		self = this;
		
		// Create the sections
		
		actMng = new GeneralSectionManager(ActionsSectionManager.class);
		condMng = new GeneralSectionManager(ConditionsSectionManager.class);
		evMng = new GeneralSectionManager(EventsSectionManager.class);
		contMng = new GeneralSectionManager(ContentsSectionManager.class);
		strMng = new GeneralSectionManager(StructuresSectionManager.class);
		
		btCtrl = new ButtonsRegionControl();
		
		//OtherHelpers.applyOptimizations(rootPane); // Does ratehr hamper performance if done for the root node!
		
		updateLayoutType();		
	}
		
	
	public void updateLayoutType()
	{
		mainPane.getChildren().clear();

		try {
			
				
			if (GlobalSettings.alternativeLayout)
			{
				
				SplitPane vSplit = (SplitPane)TemplateHandler.injectTemplateDirect("/globalTemplates/SplitterTemplate.fxml", null, mainPane);
				vSplit.setOrientation(Orientation.VERTICAL);
				vSplit.setDividerPosition(0, 0.015);
				
				
				Assert.checkNull(TemplateHandler.injectTemplate("/productionGUI/ButtonsRegion.fxml", btCtrl, vSplit));
				
				
				SplitPane hSplit = (SplitPane)TemplateHandler.injectTemplate("/globalTemplates/SplitterTemplate.fxml", null, vSplit);
				hSplit.setOrientation(Orientation.HORIZONTAL);
				hSplit.setDividerPosition(0, 0.225);
				hSplit.setDividerPosition(1, 0.775);
				
				Runnable setPos = () -> {
					OtherHelpers.setAbsoluteDividerPosition(hSplit, 0, 300, false);
					OtherHelpers.setAbsoluteDividerPosition(hSplit, 1, 300, true);
				};
				
				if (alreadyInitializedOnce)
					setPos.run();
				else
					ProductionGUI.addFinalizationEvent(setPos);

				
				SplitPane vSplitL = (SplitPane)TemplateHandler.injectTemplate("/globalTemplates/SplitterTemplate.fxml", null, hSplit);
				vSplitL.setOrientation(Orientation.VERTICAL);
				vSplitL.setDividerPosition(0, 0.5);
				
				
				Assert.checkNull(TemplateHandler.injectTemplate("/globalTemplates/SectionTemplate.fxml", contMng, hSplit));
				
				
				SplitPane vSplitR = (SplitPane)TemplateHandler.injectTemplate("/globalTemplates/SplitterTemplate.fxml", null, hSplit);
				vSplitR.setOrientation(Orientation.VERTICAL);
				vSplitR.setDividerPosition(0, 0.5);
	
				
				Assert.checkNull(TemplateHandler.injectTemplate("/globalTemplates/SectionTemplate.fxml", actMng, vSplitL));
				Assert.checkNull(TemplateHandler.injectTemplate("/globalTemplates/SectionTemplate.fxml", strMng, vSplitL));
	
				Assert.checkNull(TemplateHandler.injectTemplate("/globalTemplates/SectionTemplate.fxml", evMng, vSplitR));
				Assert.checkNull(TemplateHandler.injectTemplate("/globalTemplates/SectionTemplate.fxml", condMng, vSplitR));
				
				
				if (alreadyInitializedOnce)
				{
					ContentsSectionManager.getSelf().initPageButtons(contMng);
					ContentsSectionManager.getSelf().reinitializePagesButtons();
				}
				
	
			}
			else
			{
				
				SplitPane vSplit = (SplitPane)TemplateHandler.injectTemplateDirect("/globalTemplates/SplitterTemplate.fxml", null, mainPane);
				vSplit.setOrientation(Orientation.VERTICAL);
				vSplit.setDividerPosition(0, 0.425);
		
				SplitPane hSplit = (SplitPane)TemplateHandler.injectTemplate("/globalTemplates/SplitterTemplate.fxml", null, vSplit);
				hSplit.setOrientation(Orientation.HORIZONTAL);
				hSplit.setDividerPosition(0, 0.25);
				hSplit.setDividerPosition(1, 0.5);
				hSplit.setDividerPosition(2, 0.75);
				
		
				SplitPane hSplit2 = (SplitPane)TemplateHandler.injectTemplate("/globalTemplates/SplitterTemplate.fxml", null, hSplit);
				hSplit2.setOrientation(Orientation.HORIZONTAL);
				hSplit2.setDividerPosition(0, 0.25);
				
				SplitPane hSplit3 = (SplitPane)TemplateHandler.injectTemplate("/globalTemplates/SplitterTemplate.fxml", null, vSplit);
				hSplit3.setOrientation(Orientation.HORIZONTAL);
				hSplit3.setDividerPosition(0, 0.9);
		
		
		
				Assert.checkNull(TemplateHandler.injectTemplate("/globalTemplates/SectionTemplate.fxml", actMng, hSplit2));
				Assert.checkNull(TemplateHandler.injectTemplate("/globalTemplates/SectionTemplate.fxml", condMng, hSplit));	
				Assert.checkNull(TemplateHandler.injectTemplate("/globalTemplates/SectionTemplate.fxml", evMng, hSplit));
				Assert.checkNull(TemplateHandler.injectTemplate("/globalTemplates/SectionTemplate.fxml", strMng, hSplit));
				Assert.checkNull(TemplateHandler.injectTemplate("/globalTemplates/SectionTemplate.fxml", contMng, hSplit3));
				
				Assert.checkNull(TemplateHandler.injectTemplate("/productionGUI/ButtonsRegion.fxml", btCtrl, hSplit3));
				
				
				if (alreadyInitializedOnce)
				{
					ContentsSectionManager.getSelf().initPageButtons(contMng);
					ContentsSectionManager.getSelf().reinitializePagesButtons();
				}
			}
	
		
		} catch(NullPointerException e)
		{
			InfoErrorHandler.callBugError("Apparently random FXML-Loading error. Please restart the program. Sorry for the inconvenience...\nError message: " + e.getMessage());
			Runtime.getRuntime().exit(1);
		}
		
		alreadyInitializedOnce = true;
	}
	
	
	// Return instance of itself
	public static ProductionGUIcontrol getSelf()
	{
		return(self);
	}

}
