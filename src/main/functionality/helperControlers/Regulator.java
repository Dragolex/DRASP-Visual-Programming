package main.functionality.helperControlers;

import java.util.concurrent.atomic.AtomicReference;

import dataTypes.specialContentValues.Variable;

public class Regulator
{
	private AtomicReference<Object> inputCont;
	private double goal;
	private AtomicReference<Object> goalVariable;
	
	
	
	
	
	/*working variables*/
	long lastTime;
	double current;
	double errSum, lastErr;
	double kp, ki, kd;
	
	  
	void SetTunings(double Kp, double Ki, double Kd)
	{
	   kp = Kp;
	   ki = Ki;
	   kd = Kd;
	}
	
	
	public double compute()
	{
		if (goalVariable != null)
			goal = (Double) this.goalVariable.get();
		
		
		long now = System.currentTimeMillis();
		double timeChange = (double)(now - lastTime);
		
		double error = goal - ((Double) inputCont.get());
		errSum += (error * timeChange);
		double dErr = (error - lastErr) / timeChange;
		
		
		lastErr = error;
		lastTime = now;
		
		return(kp * error + ki * errSum + kd * dErr);
	}
	
	
	public Regulator(Variable inputVar, Object target, double kp, double ki, double kd)
	{
		inputCont = inputVar.getInternalValueContainer();
		if (target instanceof Variable)
		{
			this.goalVariable = ((Variable) target).getInternalValueContainer();
			this.goal = (Double) this.goalVariable.get();
		}
		else
			this.goal = (Double) target;
	}
	
	public void setTarget(double goal)
	{
		this.goal = goal;		
	}

}
