package staticHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Node;

public class FastFadeTransitionHelper {
	
	// This class exists because the standard opacity transition is relatively slow (on my system at least)
	
	static int nodeCount = 0;
	static List<Node> nodes = new ArrayList<>();
	static List<Double> minsMaxs = new ArrayList<>();
	static List<Long> times = new ArrayList<>();
	
	static Map<Node, Runnable> whenDoneTask = new HashMap<>();
	
	
	static boolean updating = true;
	
	static public void init()
	{
		AnimationTimer updater = new AnimationTimer() {

            @Override
            public void handle(long now) {
            	if (updating)
            	{
                	update();
            	}            	
            	updating = !updating;
            }
        };
        updater.start();
	}
	
	
	protected static void update()
	{
		long time = System.currentTimeMillis();
		
		try
		{
		for(int i = 0; i < nodeCount*2; i+=2)
		{
			
			double opacity = minsMaxs.get(i);
			long dif = (time-times.get(i));
			double period = Math.abs(times.get(i+1));
			
			if (opacity <= -100)
			{
				if (opacity <= -200)
					nodes.get(i/2).setOpacity(0);
				continue;
			}
			
			if (dif < period)
				opacity += (dif/period)*minsMaxs.get(i+1);
			else
   			if (dif < 2*period)
				opacity += (1-(dif-period)/period)*minsMaxs.get(i+1);
   			else
   			{
   				times.set(i, time);
   				if (times.get(i+1) < 0)
   				{
   					Node node = nodes.get(i/2);
   					Runnable whenDone = whenDoneTask.get(node);
   					nodes.get(i/2).setOpacity(opacity);
   					
   					stopInstantly(node);
   					
   					if(whenDone != null)
   						whenDone.run();
   					whenDoneTask.remove(node);
   					
       				continue;
   				}
   			}
			
			nodes.get(i/2).setOpacity(opacity);            			
		}
		}
		catch (IndexOutOfBoundsException ex)
		{
			nodeCount = Math.min(nodes.size(), Math.min(minsMaxs.size()/2, times.size()/2));
		}
	}
	public static synchronized void stopAll()
	{
		Platform.runLater(() ->
		{
			for (Node node: nodes)
			{
				node.setOpacity(0);
				Runnable whenDone = whenDoneTask.getOrDefault(node, null);
				
				if(whenDone != null)
						whenDone.run();
			}			
			
			nodeCount = 0;
			nodes.clear();
			minsMaxs.clear();
			times.clear();
			whenDoneTask.clear();
		});
	}

	// Start fading between two values
	public static synchronized void fade(Node node, double min, double max, long period)
	{
		if (nodes.contains(node))
		{
			int ind = nodes.indexOf(node)*2;
			minsMaxs.set(ind, min);
			minsMaxs.set(ind+1,max-min);
			times.set(ind, System.currentTimeMillis());
			times.set(ind+1, period);
			
			whenDoneTask.remove(node);
			
			update();
			
			return;
		}
		
		nodes.add(node);
		minsMaxs.add(min);
		minsMaxs.add(max-min);
		times.add(System.currentTimeMillis());
		times.add(period);
		
		if (!Platform.isFxApplicationThread())
			Platform.runLater(() -> node.setOpacity(min));
		else
			node.setOpacity(min);
		
		nodeCount++;
		
		update();
	}
	
	public static synchronized void fadeout(Node node, Runnable whenDone)
	{
		int ind = nodes.indexOf(node);
		if (ind != -1)
		{
			if (whenDoneTask.containsKey(nodes.get(ind)))
				return;
				
			times.set(ind*2+1, -times.get(ind*2+1));
			whenDoneTask.put(nodes.get(ind), whenDone);
			
			update();
		}
	}
	
	public static synchronized void abortFadeout(Node node)
	{
		if (whenDoneTask.containsKey(node))
		{
			int ind = nodes.indexOf(node)*2;
			times.set(ind+1, -times.get(ind+1));
			
			whenDoneTask.remove(node);
			
			update();
		}
	}
	
	
	public static synchronized void stopInstantly(Node node)
	{
		int ind = nodes.indexOf(node);
		if (ind != -1)
		{
			nodes.remove(ind);
			
			double min = minsMaxs.get(ind*2);
			
			minsMaxs.remove(ind*2+1);
			minsMaxs.remove(ind*2);
			
			times.remove(ind*2+1);
			times.remove(ind*2);
			
			Platform.runLater(() -> node.setOpacity(min));
			
			nodeCount--;
			
			update();
		}
	}

	public static boolean isRunning(Node node)
	{
		return(nodes.contains(node));
	}

	public static synchronized void pause(Node node, boolean makeDisappear)
	{
		int ind = nodes.indexOf(node);
		if (ind != -1)
		{
			minsMaxs.set(ind*2, -minsMaxs.get(ind*2) - (makeDisappear ? 200 : 100) );
			update();
		}
	}
	public static synchronized void contin(Node node)
	{
		int ind = nodes.indexOf(node);
		if (ind != -1)
		{
			double v = minsMaxs.get(ind*2);
			
			if (v < -150)
				minsMaxs.set(ind*2, -(minsMaxs.get(ind*2)+200));
			else
				minsMaxs.set(ind*2, -(minsMaxs.get(ind*2)+100));
			
			update();
		}
	}
	
	

}
