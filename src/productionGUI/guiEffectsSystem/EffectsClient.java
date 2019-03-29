package productionGUI.guiEffectsSystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import dataTypes.minor.FloatSingleton;
import javafx.animation.AnimationTimer;

public abstract class EffectsClient {
	
	static List<EffectsClient> effectObjects = new ArrayList<>();
	static boolean updating = true;
	
	static final int STEP = 16;
	
	static Map<FloatSingleton, Double> data = new LinkedHashMap<>();
	static Set<FloatSingleton> values = data.keySet();
	
	
	
	static public void init()
	{
		AnimationTimer updater = new AnimationTimer() {

            @Override
            public void handle(long now) {
            	if (updating)
            	{
                	for(EffectsClient client: effectObjects)
                		client.updateEffects();
            	}            	
            	updating = !updating;
            }
        };
        updater.start();
        
        
		Timer timer = new Timer(); 
        TimerTask task = new TimerTask() {  
              @Override
               public void run() { 
            	
            	synchronized(EffectsClient.class)
            	{
	          		for(Entry<FloatSingleton, Double> v: data.entrySet())
	        		{
	        			v.getKey().value -= v.getValue();
	        			
	        			if (v.getKey().value < 0)
	        				v.getKey().value = 0;
	        		}
            	}
              }
        };
        timer.schedule(task,0,STEP);
	}
	
	public EffectsClient()
	{
		synchronized(EffectsClient.class)
		{
			effectObjects.add(this);
		}
	}
	
	abstract public void updateEffects();

	
	public void addEffectsVariable(FloatSingleton newData, int duration)
	{
		synchronized(EffectsClient.class)
		{
		//if (!values.contains(newData))
			data.put(newData, (double) (newData.value / (duration/STEP)));		
		}
	}
	

}
