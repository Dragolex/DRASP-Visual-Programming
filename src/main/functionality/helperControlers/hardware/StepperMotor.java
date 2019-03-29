package main.functionality.helperControlers.hardware;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import dataTypes.exceptions.NonExistingPinException;
import execution.Execution;
import main.functionality.SharedComponents;
import main.functionality.helperControlers.spline.DataSpline;

public class StepperMotor extends SharedComponents
{
	static private boolean calcHalfStep(int coil, int pos, int coils)
	{
		pos = pos % (coils*2);
		return(((coil*2 == pos) || (coil*2 == ((pos+1) % (coils*2) ) ) || (((coil*2) % (coils*2)) == pos-1)));
	}
	
	static private boolean calcFullStep(int coil, int pos, int coils)
	{
		pos = pos % coils;
		return(((coil == pos) || (((coil+1) % coils) == pos)));
	}
	
	/*
	static boolean stepForA[] = {true, true, false, false, false, false, false, true};
	static boolean stepForB[] = {false, false, false, true, true, true, false, false};
	static boolean stepForC[] = {false, false, false, false, false, true, true, true};
	static boolean stepForD[] = {false, true, true, true, false, false, false, false};
	
	static boolean fullStepForA[] = {true, false, false, true};
	static boolean fullStepForB[] = {true, true, false, false};
	static boolean fullStepForC[] = {false, true, true, false};
	static boolean fullStepForD[] = {false, false, true, true};
	*/	
	
	long stepTime;
	boolean fullStep = false;
	
	volatile private int step = -1;
	
	GpioPinDigitalOutput[] pinSetter;
	
	
	volatile double speed = 0;
	Thread continuousMovement = null;
	volatile double subPos = 0;
	
	double degreesQuotient = 1;
	
	String pinStr = "";
	
	boolean[][] pinStates;
	
	int stepsPerRound;
	
	public StepperMotor(int[] pins, double stepTime, boolean fullStep)
	{
		int coils = pins.length;
		stepsPerRound = fullStep ? coils : 2*coils; // if using halfStep, the numebr of steps is twice the coils
		
		pinStates = new boolean[coils][stepsPerRound];
		
		for(int y = 0; y < stepsPerRound; y++)
			for(int x = 0; x < coils; x++)
				if(fullStep)
					pinStates[x][y] = StepperMotor.calcFullStep(x, y, coils);
				else
					pinStates[x][y] = StepperMotor.calcHalfStep(x, y, coils);
		
		
		this.stepTime = (long) stepTime;
		this.fullStep = fullStep;
		
		pinSetter = new GpioPinDigitalOutput[coils];
		
		try
		{
			for(int x = 0; x < coils; x++)
				pinSetter[x] = GPIOctrl.setOutputPin(pins[x], false);
		} catch (NonExistingPinException e)
		{
			e.callException();
		}
		
		if(DEBUG)
		{
			pinStr = "Pins: ";
			int i = 1;
			for(int p : pins)
				pinStr += String.valueOf(((int) 'A')+i++) + ": " + p;
			
			pinStr += " Step: ";
			//A: " + pinA  + " B: " + pinB  + " C: " + pinC  + " D: " + pinD + " Step: ";
		}
	}
	
	
	private synchronized void forward()
	{		
		step = (step+1) % stepsPerRound;
		
		int c = 0;
		for(GpioPinDigitalOutput p: pinSetter)
			p.setState(pinStates[c++][step]);
		
		//pinAext.setState(stepForA[step]);
		//pinBext.setState(stepForB[step]);
		//pinCext.setState(stepForC[step]);
		//pinDext.setState(stepForD[step]);
		
		Execution.checkedSleep(stepTime);
	}
	
	private synchronized void backward()
	{
		if (step<0)
			step = stepsPerRound-1;
		else
			step--;
		
		int c = 0;
		for(GpioPinDigitalOutput p: pinSetter)
			p.setState(pinStates[c++][step]);
		
		//pinAext.setState(stepForA[step]);
		//pinBext.setState(stepForB[step]);
		//pinCext.setState(stepForC[step]);
		//pinDext.setState(stepForD[step]);
		
		Execution.checkedSleep(stepTime);
	}
	
	
	public void byDegrees(double degreesQuotient)
	{
		this.degreesQuotient = degreesQuotient;
	}
	
	
	public void move(double ammount, boolean locked)
	{
		speed = 0;
		
		ammount = Math.round(ammount / degreesQuotient);
		if (ammount > 0)
			for(; ammount > 0; ammount--)
				forward();
		else
			for(; ammount < 0; ammount++)
				forward();
		
		if (!locked)
			stop();
	}
	
	
	public void moveBySpline(DataSpline dataSpline, long xDelay, double yFactor)
	{
		float[] data = dataSpline.getData();
		int size = data.length;
		
		for(int pos = 0; pos < size; pos++)
		{
			move(data[pos]*yFactor, true);
			if (xDelay > 0)
			try
			{
				Thread.sleep(xDelay);
			} catch (InterruptedException e) {}
		}
	}
	
	
	
	private void stop()
	{
		for(GpioPinDigitalOutput p: pinSetter)
			p.setState(false);
	}
	
	
	public void setSpeed(double newspeed)
	{
		this.speed = Math.max(-1, Math.min(1, newspeed));
		if (continuousMovement == null)
		{
			continuousMovement = new Thread(()-> {
				while(speed != 0)
				{
					subPos += speed;
					
					if (subPos > 1)
					{
						subPos -= 1;
						forward();
					}
					else
					if (subPos < -1)
					{
						subPos += 1;
						backward();
					}
					else
						Execution.checkedSleep(stepTime);
				}
				continuousMovement = null;
			});
			continuousMovement.start();
		}
	}
	
	
	@Override
	public String toString()
	{
		return(pinStr + step);
	}


	
}
