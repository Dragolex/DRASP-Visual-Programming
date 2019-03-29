package main.functionality.helperControlers.spline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import dataTypes.exceptions.AccessUnsetVariableException;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.handlers.VariableHandler;
import main.functionality.helperControlers.screen.JFXgraph;
import settings.GlobalSettings;
import staticHelpers.OtherHelpers;

public class DataSpline
{
	boolean interpolate = true;
	boolean smoothen = true;
	
	boolean fixedMaximumValues = false;
	int maximumValues = 110;
	int autoCropAfterX = -1;
	int autoCropAfterCount = -1;
	
	float autoCropShift = 0;
	
	public double overallScaleFactor = 1;
	
	Random ran = new Random();
	
	
	SplineCreator localSpline;
	
	
	JFXgraph associatedGraph = null;
	
	float minX = Integer.MAX_VALUE;
	float maxX = Integer.MIN_VALUE;
	float minY = Float.MAX_VALUE;
	float maxY = Float.MIN_VALUE;

	
	float upperDistLimit = 15; // 35
	float lowerDistLimit = 4;
	
	
	public DataSpline(boolean interpolate, boolean smoothen)
	{
		this.interpolate = interpolate;
		this.smoothen = smoothen;
		create();
	}

	public DataSpline(boolean interpolate, boolean smoothen, int maximumValues, int cropByX, int cropByCount)
	{
		this.interpolate = interpolate;
		this.smoothen = smoothen;
		if (maximumValues > 0)
		{
			this.maximumValues = maximumValues;
			fixedMaximumValues = true;
		}
		this.autoCropAfterX = cropByX;
		this.autoCropAfterCount = cropByCount;
		
		create();
	}
	
	public void associateToGraph(JFXgraph associatedGraph)
	{
		this.associatedGraph = associatedGraph;
	}
	
	
	
	float[] interpolatedData;
	float[] interpolatedDataDirComp;
	
	private Variable identifierVariable;
	
	int interpolatedDataSize;
	
	//Map<Float, Float> xval = new TreeMap<>(); // treemap orders automatically by key
	
	public List<Float> xValues = new ArrayList<Float>(); // make private!
	public List<Float> yValues = new ArrayList<Float>();
	
	Map<Float, Variable> sourcedVariables = new HashMap<>();

	
	
	private void create()
	{
		interpolatedDataSize = maximumValues;
		interpolatedData = new float[interpolatedDataSize];
		
		interpolatedDataDirComp = new float[interpolatedDataSize];
	}

	
	
	float lastLastX, lastLastY, lastX, lastY;
	
	
	public float appendValue(float xdif, double yvalue)
	{
		if (xValues.isEmpty())
			minX = xdif;
		
		float lx = xValues.get(xValues.size()-1); // get the last element
		float xpos = lx + xdif;
		
		setValue(xpos, yvalue);
		
		return(xpos);
	}

	public synchronized void setValue(float xpos, double yvalue)
	{	
		float lim = Math.max(lowerDistLimit, upperDistLimit);
		
		xpos = xpos-autoCropShift;
		
		int lp = xValues.size()-1;
		if (lp > 2)
		{
			int llp = xValues.size()-2;
			if ((xValues.get(lp) - xValues.get(llp)) < lim)
			{
				xValues.remove(lp);
				yValues.remove(lp);
			}
		}
		
		if (autoCropAfterX != -1)
		if (xpos > 2*autoCropAfterX)
		{
			autoCropShift += autoCropAfterX;
			xpos -= autoCropAfterX;
			
			// shift the x values back
			int len = xValues.size();
			
			int j = 0;
			for (int i = 0; i < len; i++)
				if (xValues.get(j) < autoCropAfterX)
				{
					xValues.remove(j);
					yValues.remove(j);
				}
				else
					j++;
			
			len = xValues.size();
			for (int i = 0; i < len; i++)
				xValues.set(i, xValues.get(i)-autoCropAfterX);
			
			maxX = Integer.MIN_VALUE;
		}
		
		if (xpos*overallScaleFactor > interpolatedDataSize)
			doubleSize(xpos);
		
		minX = Math.min(minX, xpos);
		maxX = (float) (Math.max(maxX, xpos) * overallScaleFactor);
		minY = Math.min(minY, (float)yvalue);
		maxY = Math.max(maxY, (float)yvalue);

		
		if (autoCropAfterX != -1)
			minX = (int) Math.max(0, maxX-autoCropAfterX);
		
		
		int ind = OtherHelpers.addSorted(xValues, (float) (xpos * overallScaleFactor));
		if (ind >= 0)
			yValues.add(ind, (float) yvalue);
		else
			yValues.set(-ind, (float) yvalue);
		
		
		
		float div = ((float) xValues.size()) / interpolatedDataSize;
		
		float lastx = -Float.MAX_VALUE;
		int len = xValues.size();		
		for (int i = 0; i < len; i++)
		{
			float nx = xValues.get(i);
			if (((nx - lastx) < lim) && (ran.nextDouble() < div*(1-(((float)i)/len))))
			{
				xValues.set(i-1, (lastx + xValues.get(i)) / 2);
				yValues.set(i-1, yValues.get(i));
				
				xValues.remove(i);
				yValues.remove(i);
				len--;
				i--;
			}
			else
				lastx = nx;
		}

		if (interpolate)
			if ((ind >= 0) && (Math.abs(ind) < 3))
				interpolateAllValues();
			else			
				if (Math.abs(ind) > xValues.size()-3)
					interpolateValuesNearEnd();
				else
					interpolateInternalValues(-ind);
		else
			if (associatedGraph != null)
				associatedGraph.redraw();
	}
	
	private synchronized void doubleSize(float xpos)
	{
		if (xpos <= maxX) return;
		
		if (!fixedMaximumValues)
		{
			// If not fixed, duplicate
			
			if (interpolate)
			{
				float[] newInterpolatedData = new float[interpolatedDataSize*2];
				System.arraycopy(interpolatedData, 0, newInterpolatedData, 0, interpolatedDataSize);
				interpolatedData = newInterpolatedData;
			}
			interpolatedDataSize *= 2;
		}
		else
		{
			int len = xValues.size();
			for (int i = 0; i < len; i++)
				xValues.set(i, xValues.get(i)*0.5f);
			
			overallScaleFactor *= 0.5;
			upperDistLimit *= 0.85;
			maxX *= overallScaleFactor;
		}
		
		if (interpolate)
			interpolateAllValues();
	}
	
	
	public void appendValue(float xdif, Variable variable) throws AccessUnsetVariableException
	{
		float xp = appendValue(xdif, (Double) variable.get());
		
		sourcedVariables.put(xp, variable);
		
		variable.addChangerHook(() -> {
			setValue(xp, (Double) variable.getUnchecked()); // update the value
		});
	}
	
	public void setValue(float xpos, Variable variable) throws AccessUnsetVariableException
	{
		setValue(xpos, (Double) variable.get());
		
		sourcedVariables.put(xpos, variable);
		
		variable.addChangerHook(() -> {
			setValue(xpos, (Double) variable.getUnchecked()); // update the value
		});
	}
	

	public double getMinX() {
		return minX;
	}
	
	public double getMaxX() {
		return maxX;
	}
	
	public double getMinY() {
		return minY;
	}
	
	public double getMaxY() {
		return maxY;
	}
	
	public void crop(double start_x, double end_x, boolean shiftToZero)
	{
		while(!xValues.isEmpty() && xValues.get(0) < start_x)
		{
			xValues.remove(0);
			yValues.remove(0);
		}
		
		int lastInd = xValues.size()-1;
		while(!xValues.isEmpty() && xValues.get(lastInd) > end_x)
		{
			xValues.remove(lastInd);
			yValues.remove(lastInd);
			lastInd--;
		}

		if (shiftToZero)
		{
			int len = xValues.size();
			for(int i = 0; i < len; i++)
				xValues.set(i, (float) (xValues.get(i)-start_x));
		}
		
	}
	
	public void applyTermX(Term term)
	{
		int len = xValues.size();
		for(int i = 0; i < len; i++)
			xValues.set(i, (float) (term.applyTo((double) xValues.get(i))));
	}
	public void applyTermY(Term term)
	{
		int len = yValues.size();
		for(int i = 0; i < len; i++)
			yValues.set(i, (float) (term.applyTo((double) yValues.get(i))));
	}
	
	
	
	private void interpolateAllValues()
	{
		if (smoothen)
		{
			localSpline = SplineCreator.createMonotoneCubicSpline(xValues, yValues);
			
			for(int i = 0; i < maximumValues; i++)
				interpolatedData[i] = localSpline.interpolate(i);			
		}
		else
		{
			int vals = xValues.size();
			if (vals == 0) return;
			
			double lastx = 0, lasty = 0;
			double curPos = 0;
			
			for(int i = 0; i < vals; i++)
			{
				float nx = xValues.get(i);
				float ny = yValues.get(i);
				double inc = (ny-lasty)/(nx-lastx);
				
				for(int j = (int) Math.floor(lastx); j < nx; j++)
				{
					curPos += inc;
					interpolatedData[j] = (float) curPos;
				}
				
				lastx = nx;
				lasty = ny;
			}
		}
		
		if (associatedGraph != null)
			associatedGraph.redraw();
	}
	
	private void interpolateValuesNearEnd()
	{
		if (smoothen)
			interpolateAllValues();
		else
		{
			int vals = xValues.size();
			double lastx = xValues.get(vals-4), lasty = yValues.get(vals-4);
			double curPos = yValues.get(vals-4);
			
			for(int i = vals-3; i < vals; i++)
			{
				float nx = xValues.get(i);
				float ny = yValues.get(i);
				double inc = (ny-lasty)/(nx-lastx);
				
				for(int j = (int) Math.floor(lastx); j < nx; j++)
				{
					curPos += inc;
					interpolatedData[j] = (float) curPos;
				}
				lastx = nx;
				lasty = ny;
			}
			
			if (associatedGraph != null)
				associatedGraph.redraw();
		}
	}
	
	
	private void interpolateInternalValues(int ind)
	{
		localSpline.updateAround(ind);
		
		int min = (int) Math.floor(xValues.get(ind-2));
		int max = (int) Math.floor(xValues.get(ind+2));
		
		for(int i = min; i < max; i++)
			interpolatedData[i] = localSpline.interpolate(i);
		
		if (associatedGraph != null)
			associatedGraph.redraw();
	}
	

	public float[] getData()
	{
		return(interpolatedData);
	}
	
	
	public void remove(int pointPos, int tolerance)
	{
		int len = xValues.size();
		for(int i = 0; i < len; i++)
		    if (Math.abs(pointPos-xValues.get(i)) < tolerance)
		    {
		    	xValues.remove(i);
		    	yValues.remove(i);
		    	i--;
		    	len--;
		    }
	}
	
	
	static String separator = "#";
	static String secSeparator = "#|~|#";

	public String toPersistibleString()
	{
		StringBuilder str = new StringBuilder();
		
		str.append(interpolate ? "T" : "F");
		str.append(secSeparator);
		str.append(smoothen ? "T" : "F");
		str.append(secSeparator);
		str.append(maximumValues);
		str.append(secSeparator);
		str.append(autoCropAfterX);
		str.append(secSeparator);
		str.append(autoCropAfterCount);
		str.append(secSeparator);
		
		
		int len = xValues.size();
		for(int i = 0; i < len; i++)
		{
			str.append( GlobalSettings.doubleFormatter.format((double) xValues.get(i)));
			str.append(separator);
			str.append( GlobalSettings.doubleFormatter.format((double) yValues.get(i)));
			str.append(separator);
		}
		
		str.append(secSeparator);

		for(Entry<Float, Variable> var: sourcedVariables.entrySet())
		{
			GlobalSettings.doubleFormatter.format(var.getKey());	
			str.append(separator);

			VariableHandler.getVariableName(var.getValue());
			str.append(separator);
			
			str.append( GlobalSettings.doubleFormatter.format((double) var.getValue().getUnchecked()));
			str.append(separator);
		}
		
		return(str.toString());
	}
	
	
	public void setIdentifierVariable(Variable variable)
	{
		identifierVariable = variable;
	}
	
	public String getIdentifierVariableName()
	{
		return(identifierVariable.toString());
	}

	public static DataSpline fromPersistibleString(String str)
	{
		String[] entries = str.split(secSeparator);

		DataSpline spline = new DataSpline(
				entries[0].equals("T"),
				entries[1].equals("T"),
				Integer.valueOf(entries[2]),
				Integer.valueOf(entries[3]),
				Integer.valueOf(entries[4]));
		
		String[] vals = entries[5].split(separator);
		String[] vars = entries[6].split(separator);
		
		int len = vals.length;
		for(int i = 0; i < len; i+=2)
			spline.setValue((float) (double) Double.valueOf(vals[i]), (float) (double) Double.valueOf(vals[i+1]));
		
		len = vars.length;
		try {
			for(int i = 0; i < len; i+=3)
			{
				Variable var = VariableHandler.getIfExistingOrCreateNew(vars[i+1]);
				if (!var.hasValue())
					var.initTypeAndSet(Variable.doubleType, (float) (double) Double.valueOf(vals[i+2]));
				
				spline.setValue((float) (double) Double.valueOf(vars[i]), var);
			}
		} catch (NumberFormatException | AccessUnsetVariableException e)
		{
			Execution.setError("Problem at reading a Spline from the file. Error: " + e.getMessage(), false);
			return(null);
		}

		
		return(spline);
	}

	public boolean isInterpolated()
	{
		return interpolate;
	}
	
}
