package productionGUI.sections.elementManagers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import settings.GlobalSettings;
import staticHelpers.GuiMsgHelper;
import staticHelpers.OtherHelpers;

public class GeneralSectionManager
{
	@FXML protected VBox sectionBox;
	@FXML protected Pane contentPane;
	@FXML protected HBox titleBox;
	@FXML protected Label titleText;
	@FXML protected Label emptyRootInfoLabel;
	@FXML protected ScrollPane scrollPane;
	
	AbstractSectionManager submanager;
	
	private String thisName = "";
	
	public <T extends AbstractSectionManager> GeneralSectionManager(Class<T> submanagerClass)
	{
		try
		{
			Constructor<AbstractSectionManager> constr = (Constructor<AbstractSectionManager>) submanagerClass.getConstructor(GeneralSectionManager.class);
			submanager = constr.newInstance(this);
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
	
	boolean alreadyScr = false;
	boolean scrollHorizontally = false;
	
	
	@FXML
	public void initialize()
	{
		emptyRootInfoLabel.setText(GlobalSettings.ProgramHintsText);
		emptyRootInfoLabel.setOpacity(0.8);
		
		showEmptyRootInfoLabel(false);
		
		OtherHelpers.applyOptimizations(contentPane);
		
		
		scrollPane.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event)
			{				
				scrollHorizontally = ((double)event.getY() / (double)scrollPane.getHeight()) > GlobalSettings.scrollHorizonallyFrom;
			}
		});
		
		scrollPane.setOnScroll(
                new EventHandler<ScrollEvent>() {
                    @Override
                    public void handle(ScrollEvent event) {
                    	
                    	if (scrollHorizontally)
                    		scrollHorizontal(Math.min(GlobalSettings.scrollFractionLimit, Math.max(-GlobalSettings.scrollFractionLimit, -event.getDeltaY()/200)));
                    	
                        //event.consume();
                    }
                });

		
		
		scrollPane.vvalueProperty().addListener(new ChangeListener<Number>() 
        {
          @Override
          public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) 
          {
        	  if (alreadyScr || !scrollHorizontally)
        	  {
        		  alreadyScr = false;
        		  return;
        	  }
        	  alreadyScr = true;
        	  scrollPane.setVvalue((double)oldValue);
        	  
        	  scrollHorizontal((double) newValue - (double) oldValue);
          }
        });
		
		submanager.reinitialize();
		
		titleText.setText(thisName);
		
		Node quest = GuiMsgHelper.createSizedImage("/guiGraphics/if_question.png", "/guiGraphics/if_question_hover.png", 20, null);
		quest.setManaged(false);
		quest.translateXProperty().bind(titleBox.widthProperty().subtract(28));
		quest.setTranslateY(6);
		titleBox.getChildren().add( quest );
	}
	
	
	private void scrollHorizontal(double delta)
	{
		scrollPane.setHvalue(Math.min(1, Math.max(0, scrollPane.getHvalue()+delta)));
	}

	
	protected void finalize(String name, String tooltip)
	{
		thisName = name;
		
		titleText.setText(name);
		titleText.minWidthProperty().bind(titleText.widthProperty());
		
		GuiMsgHelper.applyStandardTooltip(titleBox, tooltip);
	}
	
	
	public Label getFXtitleText()
	{
		return(titleText);
	}
	
	public void showEmptyRootInfoLabel(boolean show)
	{
		if (show)
		{
			if (!sectionBox.getChildren().contains(emptyRootInfoLabel))
				sectionBox.getChildren().add(0, emptyRootInfoLabel);		
		}
		else
			sectionBox.getChildren().remove(emptyRootInfoLabel);
	}
	

	public Pane getContentPane() {
		return(contentPane);
	}

	public VBox getSectionBox() {
		return(sectionBox);
	}
	
	public ScrollPane getScrollPane()
	{
		return(scrollPane);
	}

}
