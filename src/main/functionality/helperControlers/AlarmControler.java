package main.functionality.helperControlers;

import java.util.ArrayList;
import java.util.List;

import dataTypes.minor.Tripple;
import dataTypes.specialContentValues.FuncLabel;

public class AlarmControler
{
	static volatile List<Long> alarmTime = new ArrayList<>();
	static volatile List<FuncLabel> alarmLabel = new ArrayList<>();
	static volatile List<Tripple<Object[], Integer, Integer>> alarmArguments = new ArrayList<>();
	static volatile int alarms = 0;
	
	public static void setAlarm(FuncLabel label, double delay, boolean overwrite,
			Object[] callerLocalArgs, int offsetIndex, int argumentsNumber)
	{
		if (overwrite)
		{
			int ind = alarmLabel.indexOf(label);
			if (ind > -1)
			{
				if (delay < 0)
				{
					alarmTime.remove(ind);
					alarmLabel.remove(ind);
					alarmArguments.remove(ind);
					alarms--;
					return;
				}
				
				alarmTime.set(ind, (long) (System.currentTimeMillis()+Math.round(delay)));
				alarmArguments.set(ind, new Tripple<>(callerLocalArgs,offsetIndex,argumentsNumber) );
				return;
			}
			if (delay < 0)
				return;
		}
		
		alarmTime.add((long) (System.currentTimeMillis()+Math.round(delay)));
		alarmLabel.add(label);
		alarmArguments.add(new Tripple<>(callerLocalArgs,offsetIndex,argumentsNumber));
		alarms++;
	}

	public static void checkAlarms(long currentTime)
	{
		for(int i = 0; i < alarms; i++)
		{
			if (alarmTime.get(i) < currentTime)
			{
				System.out.println("ALARM TRIGGERED");
				//LabelHandler.executeLabelThreaded(alarmLabel.get(i), alarmArguments.get(i));
				((FuncLabel) alarmLabel.get(i)).getEvent()
					.externallyActivateAndPerform(((FuncLabel) alarmLabel.get(i)).getPositionIndex(), alarmArguments.get(i).first, alarmArguments.get(i).second, alarmArguments.get(i).third);
				
				alarmTime.remove(i);
				alarmLabel.remove(i);
				alarmArguments.remove(i);
				alarms--;
				i--;
			}
		}
	}
	
	public static boolean hasAlarms()
	{
		return(alarms > 0);
	}
	
	public static void clearAlarms()
	{
		alarms = 0;
		alarmTime.clear();
		alarmLabel.clear();
		alarmArguments.clear();
	}
	
}
