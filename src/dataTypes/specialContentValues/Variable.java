package dataTypes.specialContentValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import dataTypes.exceptions.AccessUnsetVariableException;
import execution.Execution;
import execution.handlers.VariableHandler;
import main.functionality.SharedComponents;
import main.functionality.helperControlers.hardware.analog.SensorDevice;
import main.functionality.helperControlers.spline.DataSpline;
import productionGUI.additionalWindows.VariableOverviewList;
import settings.GlobalSettings;

public class Variable {
	
	/*
	public abstract void set(Object newValue);
	public abstract Object get();
	*/

	public static final int noType = -1;
	public static final int doubleType = 0;
	public static final int textType = 1;
	public static final int variableType = 2;
	public static final int JFXwindowType = 3;
	public static final int JFXitemType = 4;
	public static final int dataListType = 5;
	public static final int countDownLatchType = 6;
	public static final int boolType = 7;
	public static final int teleBot = 8;
	public static final int teleChatMsg = 9;
	public static final int splineType = 10;
	public static final int AnalogDevice = 11;
	public static final int UARTDevice = 12;
	public static final int rotaryDevice = 13;
	public static final int stepperMotor = 14;
	public static final int regulatorType = 15;
	public static final int randomizerType = 16;
	public static final int labelType = 17;
	public static final int streamType = 18;
	public static final int DestinationDeviceType = 19;
	public static final int miniDisplayType = 20;
	public static final int soundType = 21;
	public static final int JFXeffectType = 22;
	
	


	
	public static String getDebugTypeName(int type) // only used for displaying what type a variable has during debugging
	{
		switch(type)
		{
		case noType: return("No Type");
		case doubleType: return("Number");
		case textType: return("Text");
		case variableType: return("Variable");
		case JFXwindowType: return("JFX Window");
		case JFXitemType: return("JFX Item");
		case dataListType: return("Data List");
		case countDownLatchType: return("Sync Barrier");
		case boolType: return("Boolean");
		case teleBot: return("Telegram Bot");		
		case teleChatMsg: return("Telegram Chat");		
		case splineType: return("Data Graph");
		case AnalogDevice: return("Analog Device");
		case UARTDevice: return("UART Device");
		case rotaryDevice: return("Rotary Device");
		case stepperMotor: return("Stepper Motor");
		case regulatorType: return("Regulator");
		case randomizerType: return("Randomizer");
		case labelType: return("Label");
		case streamType: return("Video Stream");
		case DestinationDeviceType: return("Device Destination Data");
		case miniDisplayType: return("Mini Display");
		case soundType: return("Playable Soundfile");
		case JFXeffectType: return("JFX Graphical Effect");
		}
		return("Unknown Type");
	}
	
	public static boolean getDebugEditable(int type) // whether the variable type can be edited via the GUI at runtime
	{
		switch(type)
		{
		case boolType: return(true);
		case doubleType: return(true);
		case textType: return(true);
		//case variableType: return(true);
		case dataListType: return(true);
		case countDownLatchType: return(true);
		}
		return(false);
	}
	
	
	public static boolean getDebugTypeRequiringConstantUpdate(int type) // whether the variable type requires constant updating becuase of special value-strings
																		//	(should be all types which are handled specially by getDebugTypeValueString() )
	{
		switch(type)
		{
		case dataListType: return(true);
		case countDownLatchType: return(true);
		case JFXwindowType: return(true);
		case JFXitemType: return(true);
		case stepperMotor: return(true);
		case regulatorType: return(true);
		}
		return(false);
	}
	
	public static String getDebugTypeValueString(int type, Object value)
	{
		switch(type)
		{		
		case dataListType:
			DataList list = (DataList) value;
			return("Type: " + getDebugTypeName(list.getType()) + " Size: " + list.getSize());
			
		case countDownLatchType: return("Counter: " + ((CountDownLatch) value).getCount());
		
		case randomizerType: return("Seed: " + SharedComponents.randomizerSeeds.get((Random) value));
		
		case doubleType:
			return(String.valueOf(value));
			//return(String.valueOf(((Double) (double) Math.round((((Double) (double) value) * 100000)))/100000));
		}
		
		if (value == null)
			return("");
		
		return(value.toString());
	}
	
	// Add more conversions if they make sense
	public Object getConvertedValue(int newType)
	{
		Object ret = getUnchecked();
		
		if (newType == type)
			return(ret);
		
		if (newType == Variable.textType)
		{
			switch(getType())
			{
			case Variable.doubleType:
				ret = GlobalSettings.doubleFormatter.format(getUnchecked());
				break;
			case Variable.AnalogDevice:
				ret = GlobalSettings.doubleFormatter.format(((SensorDevice) getUnchecked()).getValue());
				break;
			}
		}
		else
		if (newType == Variable.doubleType)
		{
			switch(getType())
			{
			case Variable.textType:
				ret = Double.valueOf((String) getUnchecked());
				break;
			}
		}
		
		return(ret);
	}
	
	
	

	
	
	private static boolean signalizeSet = false;
	public static void enableSignalizeSet()
	{
		signalizeSet = true;
	}
	
	
	
	
	AtomicReference<Object> value = new AtomicReference<Object>();
	int type = noType;
	
	List<Runnable> hooks = new ArrayList<>();
	
	public void initType(int type)
	{
		this.type = type;
		switch(type)
		{
		case doubleType: value.set((double) 0); break;
		case textType: value.set(""); break;
		//case variableType: value.set(   ); break;
		
		default: value.set(null);
		}
		
		if(!hooks.isEmpty())
			for(Runnable r: hooks)
				r.run();			
		
		if (signalizeSet)
		{
			handleIfTrackedExecution(true);
			signalizeForDebug();
		}
	}
	
	public void initTypeAndSet(int type, Object val)
	{
		this.type = type;
		value.set(val);
		
		if(!hooks.isEmpty())
			for(Runnable r: hooks)
				r.run();
		
		if (signalizeSet)
		{
			handleIfTrackedExecution(false);
			signalizeForDebug();
		}
	}
	

	double clampMax = Double.MAX_VALUE;
	double clampMin = -Double.MAX_VALUE;
	
	
	public void set(Object newValue)
	{
		if (type == doubleType)
			newValue = new Double(Math.max(Math.min((double) newValue, clampMax), clampMin));
		
		value.set(newValue);
		
		if(!hooks.isEmpty())
			for(Runnable r: hooks)
				r.run();
		
		if (signalizeSet)
			if (type != noType)
			{
				handleIfTrackedExecution(false);
				signalizeForDebug();
			}
	}
	
	public Object get() throws AccessUnsetVariableException
	{
		if (type == noType)
			throw new AccessUnsetVariableException(this);
		
		if (type == doubleType)
			return( ( (double) Math.round( (((double) value.get()) * 100000)))/100000);
		
		return(value.get());
	}
	public Object getUnchecked() // Return without checking whether it has a type. SHould only be used when the type is checked manually beforehand (for example by testing "hasValue()")!
	{
		if (type == doubleType)
			return( ( (double) Math.round( (((double) value.get()) * 100000)))/100000);
		
		return(value.get());		
	}
	
	
	
	public boolean hasValue()
	{
		return(type != noType);
	}
	
	
	
	public String toString()
	{
		return(VariableHandler.getVariableName(this));
	}
	
	
	public int getType()
	{
		return(type);
	}
	
	public boolean isType(int testType)
	{
		return(testType == type);
	}
	
	
	public void signalizeForDebugExternally()
	{
		handleIfTrackedExecution(false);
		signalizeForDebug();
	}

	private void signalizeForDebug()
	{
		if (Variable.getDebugTypeRequiringConstantUpdate(getType()))
			VariableHandler.handleExternallyChangedVariable(this);
		
		if (GlobalSettings.fullDebug)
		{
			StringBuilder str = new StringBuilder(GlobalSettings.variableSetSignal);
			
			str.append(toString());			
			str.append("|");
			str.append(type);
			str.append("|");
			str.append(getDebugTypeValueString(type, value.get()));
			
			Execution.print(str.toString());
		}
		else
			if (Execution.isSimulated())
			{
				VariableOverviewList.updateVariable(toString(), getDebugTypeName(type), getDebugTypeValueString(type, value.get()), true, getDebugEditable(type));
			}
	}
	
	private void handleIfTrackedExecution(boolean initializedOnly)
	{
		if (!Execution.isTracked())
			return;
		
		StringBuilder str = new StringBuilder();
		
		str.append("Variable '");
		str.append(toString());
		str.append("' of type '");
		str.append(getDebugTypeName(type));
		if (initializedOnly)
			str.append("' initialized to default: ");
		else
			str.append("' updated to: ");
		str.append(getDebugTypeValueString(type, value.get()));
		
		Execution.print(str.toString());
		
	}

	
	public void addChangerHook(Runnable hook)
	{
		hooks.add(hook);
	}

	
	public void setMinClamp(double clampMin)
	{
		this.clampMin = clampMin;
	}
	public void setMaxClamp(double clampMax)
	{
		this.clampMax = clampMax;
	}
	
	
	
	public void forceReset()
	{
		value.set(null);
		type = noType;
		globalVariable = false;
	}
	
	
	public AtomicReference<Object> getInternalValueContainer()
	{
		return(value);
	}
	
	
	public Variable toNewType(int newType)
	{
		if (newType == type)
			return(this);
		
		if (!hasValue())
			initType(newType);
		
		initTypeAndSet(newType, getConvertedValue(newType));
		
		return(this);
	}	


	public String toPersistableString() throws AccessUnsetVariableException
	{
		switch(type)
		{
		case boolType:
			return( ((boolean) get()) ? "T" : "F");
			
		case doubleType:
	        return(GlobalSettings.doubleFormatter.format((Double) get()));
			
		case textType:
			return((String) get());
			
		case dataListType:
			return(((DataList) get()).toPersistibleString());

		case splineType:
			return(((DataSpline) get()).toPersistibleString());
		}
		
		Execution.setError("Trying to write down a variable of the type '" + getDebugTypeName(type) + "'.\nUnfortunately that is not implemented in DRASP yet! ", false);
		return("");
	}
	
	final String listSep = "|##|";
	
	public void fromPersitableString(int type, String str)
	{		
		switch(type)
		{
		case boolType:
			initTypeAndSet(type, (Boolean) str.equals("T"));
			break;
			
		case doubleType:
			initTypeAndSet(type, Double.valueOf(str));
			break;
			
		case textType:
			initTypeAndSet(type, str);
			break;
			
		case dataListType:
			initTypeAndSet(type, DataList.fromPersistibleString(str));
			break;

		case splineType:
			DataSpline spline = DataSpline.fromPersistibleString(str);
			initTypeAndSet(type, spline);
			spline.setIdentifierVariable(this);
			break;
		}
		
		// Note: If adding more here, it might make sense to add the new types to the Action "create_ElCallRemoteEvent" since it uses this function
	}
	
	private boolean globalVariable = false;
	public void markAsGlobal(boolean globalVariable)
	{
		this.globalVariable = globalVariable;
	}

	public boolean isMarkedGlobal()
	{
		return(globalVariable);
	}

	

	
	
}
