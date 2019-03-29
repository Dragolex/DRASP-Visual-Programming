package productionGUI.controlers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.reactfx.util.FxTimer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;

public class StageOrderControler
{
	private static List<Stage> baseStages = new ArrayList<Stage>();
	private static List<Stage> additionalStages = new ArrayList<Stage>();
	private static List<Stage> popupStages = new ArrayList<Stage>();
	
	private static List<Stage> realOrder = new ArrayList<Stage>();
	
	
	public static void addBaseStage(Stage stage)
	{
		addStage(stage);
		
		if (!baseStages.contains(stage))
			baseStages.add(stage);
	}
	public static void addAdditionalStage(Stage stage)
	{
		addStage(stage);
		
		if (!additionalStages.contains(stage))
			additionalStages.add(stage);
	}
	public static void addPopupStages(Stage stage)
	{
		addStage(stage);
		
		if (!popupStages.contains(stage))
			popupStages.add(stage);
	}
	
	private static void addStage(Stage stage)//, int type)
	{
		stage.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				int oldInd = realOrder.indexOf(stage);
				
				//if (oldInd = 
				
				realOrder.remove(stage);
				realOrder.add(stage);
				
				rearangeStages();
			}
		});
		
		
		
	}
	
	public static void removeStage(Stage stage)
	{
		/*
		baseStages.remove(stage);
		additionalStages.remove(stage);
		popupStages.remove(stage);
		*/
	}
	
	
	private static boolean rearange = false;
	
	public static void rearangeStages()
	{
		if (true)
		return;
		//if (rearange) return;
		
		rearange = true;
		
		FxTimer.runLater(
		        Duration.ofMillis(500),
		        () -> rearange = false);
		
		/* // DEACTIVATED
		for(Stage stage: baseStages)
			stage.toFront();		
		for(Stage stage: additionalStages)			
			stage.toFront();		
		for(Stage stage: popupStages)
			stage.toFront();		
			*/
	}
	
}
