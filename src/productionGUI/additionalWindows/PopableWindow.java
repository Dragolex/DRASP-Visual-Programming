package productionGUI.additionalWindows;

import java.io.IOException;
import java.time.Duration;

import org.reactfx.util.FxTimer;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import productionGUI.controlers.StageOrderControler;
import settings.GlobalSettings;
import staticHelpers.KeyChecker;
import staticHelpers.OtherHelpers;

public abstract class PopableWindow
{
	private Stage stage;
	
	volatile private boolean visible;
	private boolean asStage = false;
	
	private OverlayMenu embeddedWindow = null;
	
	
	String title;
	int width, height, windowX, windowY;
	
	public PopableWindow(String title, int width, int height, int windowX, int windowY, String fxml)
	{
		this.title = title;
		this.width = width;
		this.height = height;
		
		this.windowX = windowX;
		this.windowY = windowY;
		
		
        FXMLLoader loader = new FXMLLoader(SettingsMenu.class.getResource(fxml));
        loader.setController(this);
        Pane root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		
		
		OtherHelpers.applyOptimizations(root);
		
		
		if (asStage)
		{
			toStage(root);
			
			FxTimer.runLater(
			        Duration.ofMillis(GlobalSettings.backToFrontDelay),
			        () -> stage.toFront());
		}
		else
		{
			toEmbedded(root);
		}

		visible = true;
		
	}
	
	
	
	
	private void toStage(Pane root)
	{
		if (embeddedWindow!=null)
		{
			embeddedWindow.setRemoveAction(() -> {
				
				 ((Pane) root.getParent()).getChildren().remove(root);
				 createStage(root);
				
			});
			
			embeddedWindow.fade(false);
		}
		else
			createStage(root);

		asStage = true;
	}

	
	private void createStage(Pane root)
	{
		stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root, width, height));	        
        
        stage.setX(windowX - width);
        stage.setY(windowY);
        stage.show();
        
        StageOrderControler.addAdditionalStage(stage);
		KeyChecker.initForStage(stage);
		
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent ev)
            {
            	close();
            }});
	}
	
	private void toEmbedded(Pane root)
	{
		embeddedWindow = new OverlayMenu(null, null , false, false);
		root.setPrefSize(width, height);
		embeddedWindow.addText(title, false);
		embeddedWindow.addElement(new Pane(root), 0, false, true);
		embeddedWindow.addCornerButton("Close Window", () -> close(), 0,  "/guiGraphics/if_close.png", "/guiGraphics/if_close_hover.png", true);
		embeddedWindow.addCornerButton("Pop-Out Window", () -> {if (asStage) toEmbedded(root); else {embeddedWindow.deMinimize(root); toStage(root);}}, 1,  "/guiGraphics/if_popup.png", "/guiGraphics/if_popup_hover.png", true);
		embeddedWindow.addCornerButton("Minimize", () -> {if (!asStage) embeddedWindow.flipMinimize(root, (int) (width/1.5));}, 2,  "/guiGraphics/if_minimize.png", "/guiGraphics/if_minimize_hover.png", true);
		embeddedWindow.makeDraggable();
		embeddedWindow.setPosition(windowX, windowY);
		//embeddedWindow.makeResizable(8, root);
		
		asStage = false;
	}
	
	
	
	public void close()
	{		
		if (asStage)
		{
			toEmbedded((Pane) stage.getScene().getRoot());
			
			stage.close();
			StageOrderControler.removeStage(stage);
		}
		else
		{
			end();
			visible = false;
			
			if (embeddedWindow != null)
			{
				embeddedWindow.fade(false);
				embeddedWindow.setRemoveAction(() -> embeddedWindow = null);
			}
		}
	}
	
	
	public boolean isVisible()
	{
		return(visible);
	}
	
	
	public void toFront()
	{
		if (asStage)
			stage.toFront();
	}
	
	public void setTransparent(boolean transparent)
	{
		if (!asStage)
			if (embeddedWindow != null)
				embeddedWindow.setTransparent(transparent);
	}
	
	protected void end() {}
	

}
