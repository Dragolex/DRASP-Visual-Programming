package productionGUI.guiEffectsSystem;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import com.sun.javafx.scene.control.skin.CustomColorDialog;

import dataTypes.minor.ObjectHolder;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import productionGUI.ProductionGUI;

public class JFXinputTools {
	
	public static Color showColorDialog(String title, Color initialColor)
	{

		CountDownLatch countDownLatch = new CountDownLatch(1);

		ObjectHolder<Color> selectedColorHolder = new ObjectHolder<Color>();

		Platform.runLater(new Runnable() {
		    @SuppressWarnings("restriction")
			@Override
		    public void run() {
		    try {
		        final CustomColorDialog customColorDialog = new CustomColorDialog(ProductionGUI.getScene().getWindow());
		        customColorDialog.setCurrentColor(initialColor);

		        // remove save button
		        VBox controllBox = (VBox) customColorDialog.getChildren().get(1);
		        HBox buttonBox = (HBox) controllBox.getChildren().get(2);
		        buttonBox.getChildren().remove(0);

		        Runnable saveUseRunnable = new Runnable() {
		        @Override
		        public void run() {
		            try {
		            Field customColorPropertyField = CustomColorDialog.class
		                .getDeclaredField("customColorProperty"); //$NON-NLS-1$
		            customColorPropertyField.setAccessible(true);
		            @SuppressWarnings("unchecked")
		            ObjectProperty<Color> customColorPropertyValue = (ObjectProperty<Color>) customColorPropertyField
		                .get(customColorDialog);
		            selectedColorHolder.setObject(customColorPropertyValue.getValue());
		            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e)
		            {
				    	selectedColorHolder.setObject(null);
				    }
		        }
		        };

		        customColorDialog.setOnUse(saveUseRunnable);

		        customColorDialog.setOnHidden(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent arg0) {
						countDownLatch.countDown();
		        }});

		        Field dialogField = CustomColorDialog.class.getDeclaredField("dialog"); //$NON-NLS-1$
		        dialogField.setAccessible(true);
		        Stage dialog = (Stage) dialogField.get(customColorDialog);

		        dialog.setTitle(title);
		        customColorDialog.show();
		        dialog.centerOnScreen();
		    } catch (Exception e) {
		    	selectedColorHolder.setObject(null);
		        countDownLatch.countDown();
		    }
		    }
		});

		try {
		    countDownLatch.await();
		} catch (InterruptedException e) {
		    return(null);
		}

		return selectedColorHolder.getObject();
	}

}
