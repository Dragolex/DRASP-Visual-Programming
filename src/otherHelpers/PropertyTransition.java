package otherHelpers;

import javafx.beans.property.DoubleProperty;
import javafx.util.Duration;

public class PropertyTransition
{
	static int msPerStep = 33;
	
	private DoubleProperty property;	
	private double startTime, totalTime;
	
	private double startValue, targetValue;
	
	private volatile boolean stopped = false;
	
	
	public PropertyTransition(Duration millis, DoubleProperty property)
	{
		this.property = property;
		this.totalTime = millis.toMillis();
	}
	
	public void play()
	{
		startTime = System.currentTimeMillis();
		
		
		if (totalTime <= 1)
		{
			property.set(targetValue);
			return;
		}
		else
			property.set(startValue);
		
		
		new Thread(() -> {
			double durDif = totalTime;
			
			while(durDif > 0)
			{
				durDif = totalTime-(System.currentTimeMillis()-startTime);
				
				if (stopped)
					return;
				
				property.set(startValue + Math.min(1, ((totalTime-durDif) / totalTime)) * (targetValue-startValue) );
				
				try {
					Thread.sleep(msPerStep);
				} catch (InterruptedException e) {}
				
			}
			property.set(targetValue);
			
		}).start();
	}



	public void setFrom(double start)
	{
		startValue = start;
	}
	public void setTo(double target)
	{
		targetValue = target;
	}

	public void stop()
	{
		stopped = true;
	}

}
