package main.functionality.helperControlers.hardware;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinShutdown;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import dataTypes.ProgramEventContent;
import dataTypes.exceptions.NonExistingPinException;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import main.functionality.Functionality;
import main.functionality.helperControlers.HelperParent;
import staticHelpers.DebugMsgHelper;
import staticHelpers.GuiMsgHelper;
import staticHelpers.KeyChecker;
import staticHelpers.LocationPreparator;

public class GPIOcontrol extends HelperParent {

	public GpioController gpio;
	
	
	private Pin[] pinMap;
	
	private GpioPinDigitalOutput[] outputPinsJava;
	private GpioPinDigitalOutput[] outputPinsStandard;
	
	volatile byte[] pinCheckingStatus;
	private GpioProvider[] providers;
	
	private static GpioPinDigitalOutput simulatedOffOutput, simulatedOnOutput;
	
	
	@Override
	protected void start()
	{		
		if (SIMULATED)
		{
			simulatedOffOutput = new OnOffSimulatorDummy();
			simulatedOnOutput = new OnOffSimulatorDummy();
		}
		else
		if (LocationPreparator.isUnix())
			gpio = GpioFactory.getInstance();

		
		pinMap = new Pin[32];
		outputPinsJava = new GpioPinDigitalOutput[32];
		outputPinsStandard = new GpioPinDigitalOutput[32];
		pinCheckingStatus = new byte[28];		
	    providers = new GpioProvider[32];

		if (!SIMULATED)
			providers[0] = GpioFactory.getDefaultProvider();
		
		for(int i = 0; i < 32; i++)
		{
			if (!SIMULATED)
				providers[i] = providers[0]; // set all default GPIOs their default provider
			
			try {
				pinMap[i] = RaspiPin.getPinByName("GPIO " + getPinForJava(i)); // TODO: Do not rely on exception!
			} catch (NonExistingPinException e)
			{
				pinMap[i] = null;
				
				//TODO: Better error
				//e.printStackTrace();
			}
		}
		/*
		//private Pin[] standardPinMap = new Pin[28];
		standardPinMap[2] = RaspiPin.GPIO_08;
		standardPinMap[3] = RaspiPin.GPIO_09;
		standardPinMap[4] = RaspiPin.GPIO_07;
		standardPinMap[17] = RaspiPin.GPIO_00;
		standardPinMap[27] = RaspiPin.GPIO_02;
		standardPinMap[22] = RaspiPin.GPIO_03;
		standardPinMap[10] = RaspiPin.GPIO_12;
		standardPinMap[9] = RaspiPin.GPIO_13;
		standardPinMap[11] = RaspiPin.GPIO_14;
		standardPinMap[5] = RaspiPin.GPIO_21;
		standardPinMap[6] = RaspiPin.GPIO_22;
		standardPinMap[13] = RaspiPin.GPIO_23;
		standardPinMap[19] = RaspiPin.GPIO_24;
		standardPinMap[26] = RaspiPin.GPIO_25;
		standardPinMap[14] = RaspiPin.GPIO_15;
		standardPinMap[15] = RaspiPin.GPIO_16;
		standardPinMap[18] = RaspiPin.GPIO_01;
		standardPinMap[23] = RaspiPin.GPIO_04;
		standardPinMap[24] = RaspiPin.GPIO_05;
		standardPinMap[25] = RaspiPin.GPIO_06;
		standardPinMap[8] = RaspiPin.GPIO_10;
		standardPinMap[7] = RaspiPin.GPIO_11;
		standardPinMap[12] = RaspiPin.GPIO_26;
		standardPinMap[16] = RaspiPin.GPIO_27;
		standardPinMap[20] = RaspiPin.GPIO_28;
		standardPinMap[21] = RaspiPin.GPIO_29;*/
				
		for(int i = 0; i < 28; i++)
			pinCheckingStatus[i] = 0;
	}
	
	
	
	
	public GpioPinDigitalOutput setOutputPin(int pinInd, boolean on) throws NonExistingPinException
	{	
		startIfNeeded();
		
		Pin pin = pinMap[ pinInd ];
				
		if (DEBUG) DebugMsgHelper.setPinStateDebug(pinInd, true, on);
		
		if (SIMULATED || (gpio == null)) return(null); // Do not actually set if in a simulation or gpio is not available
		
		
		if (outputPinsStandard[pinInd] == null)
			outputPinsStandard[pinInd] = gpio.provisionDigitalOutputPin(providers[ pinInd ], pin, on ? PinState.HIGH : PinState.LOW);
		else
			if (on)
				outputPinsStandard[pinInd].high();
			else
				outputPinsStandard[pinInd].low();
		
		return(outputPinsStandard[pinInd]);
		
	}
	
	
	public void toggleOutputPin(int pinInd) throws NonExistingPinException
	{
		startIfNeeded();
		
		if (SIMULATED || (gpio == null))
		{
				if (outputPinsStandard[pinInd] == null)
				{
					setOutputPin(pinInd, false);
					outputPinsStandard[pinInd] = simulatedOffOutput;
				}
				else
				{
					outputPinsStandard[pinInd] = (outputPinsStandard[pinInd] == simulatedOffOutput) ? simulatedOnOutput : simulatedOffOutput;
					if (DEBUG) DebugMsgHelper.setPinStateDebug(pinInd, true, outputPinsStandard[pinInd] != simulatedOffOutput);
				}
			
			return;
		}
	
		if (outputPinsStandard[pinInd] == null)
			outputPinsStandard[pinInd] = setOutputPin(pinInd, true);
		else
		{
			outputPinsStandard[pinInd].toggle();
			if (DEBUG) DebugMsgHelper.setPinStateDebug(pinInd, true, outputPinsStandard[pinInd].getState() == PinState.HIGH);
		}
		
	}
	
	
	public GpioPinDigitalOutput getOutputPin(int pin)
	{
		return(outputPinsStandard[pin]);
	}
	
	
	public boolean checkInputPin(int pinInd, boolean pullUpRes, int debounceDelay) throws NonExistingPinException
	{
		startIfNeeded();
		
		
		if (pinCheckingStatus[pinInd] == 0)
		{
		    pinCheckingStatus[pinInd] = 1;
		    		    
			if (pinInd > 40)
				return(checkNonstandardPin(pinInd));
		    
	    	if (DEBUG) DebugMsgHelper.setCheckPinDebug(pinInd, () -> {
		    		pinCheckingStatus[pinInd] = (byte) ((pinCheckingStatus[pinInd] == 2) ? 1 : 2);
		    		
					if (!Execution.isPaused())
					if(Execution.isRunning())
						{
							Object[] dat = {pinInd, pinCheckingStatus[pinInd] == 2};
							
							for(ProgramEventContent cont: Functionality.GPIOchangedEventContents) // loop through all GPIO-changed events
								cont.triggerExternally(dat); // pass the input value and trigger
						}
		    		
					DebugMsgHelper.setPinStateDebug(pinInd, false, pinCheckingStatus[pinInd] == 2);	
		    	});
		    
			
			if (SIMULATED || (gpio == null)) return(false);
			
		    final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(providers[pinInd], pinMap[ pinInd ], pullUpRes ? PinPullResistance.PULL_UP : PinPullResistance.PULL_DOWN);
		    
		    if (debounceDelay > 0)
		    	myButton.setDebounce(debounceDelay);
		    
		    myButton.removeAllListeners();
		    
	        myButton.setShutdownOptions(true);
	        myButton.addListener(new GpioPinListenerDigital() {
	            @Override
	            public synchronized void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
	            {
					if (DEBUG)
						if (pullUpRes)
							DebugMsgHelper.setPinStateDebug((int) (double) pinInd, false, (event.getState() == PinState.LOW));
						else
							DebugMsgHelper.setPinStateDebug((int) (double) pinInd, false, (event.getState() == PinState.HIGH));
					
					if (pullUpRes)
						pinCheckingStatus[pinInd] = (byte) ((event.getState() == PinState.LOW) ? 2 : 1);
					else
						pinCheckingStatus[pinInd] = (byte) ((event.getState() == PinState.HIGH) ? 2 : 1);
					
					if (!Execution.isPaused())
					if(Execution.isRunning())
						{
							Object[] dat = {pinInd, pinCheckingStatus[pinInd] == 2};
							
							for(ProgramEventContent cont: Functionality.GPIOchangedEventContents) // loop through all GPIO-changed events
								cont.triggerExternally(dat); // pass the input value and trigger
						}

	            }
	        });
	        
	        return(pullUpRes ? myButton.isLow() : myButton.isHigh());
		}
		else
			return((pinCheckingStatus[pinInd] == 2));
	}	
	
	
	Map<Integer, Callable<Boolean>> nonstandardPins = new HashMap<>();
	public void registerNonstandardInputPin(int pinInd, Callable<Boolean> check)
	{
		startIfNeeded();    	
		
		if ((pinInd < 0) || (pinInd >= pinMap.length))
			Execution.setError("The pin '" + pinInd + "' is already provided by the system or negative! Use a number larger than 40.", false);
		else
			nonstandardPins.put(pinInd, check);
	}
	
	private boolean checkNonstandardPin(int pinInd)
	{		
		Callable<Boolean> call = nonstandardPins.getOrDefault(pinInd, null);
		if (call == null)
		{
			//Execution.setError("The additional pin with the index '" + pinInd + "' does not exist!\nNote that only pins < 40 are regular raspberry pins.", false);
		}
		else
			try {
				return(call.call());
			} catch (Exception e) { InfoErrorHandler.callBugError("Unallowed error!"); } // should never happen
		return(false);
	}


	
	
	public void registerAdditionalPin(int pinInd, GpioProvider provider, Pin pin)
	{
		if (nonstandardPins.containsKey(pinInd))
			Execution.setError("The pin index '" + pinInd + "' is already in use!", false);
		
		if (pinInd >= providers.length)
		{
			providers = java.util.Arrays.copyOf(providers, pinInd+1);
			pinMap = java.util.Arrays.copyOf(pinMap, pinInd+1);
			outputPinsStandard = java.util.Arrays.copyOf(outputPinsStandard, pinInd+1);
		}
		else
		{
			if (pinMap[pinInd] != null)
				Execution.setError("The pin index '" + pinInd + "' is already in use!", false);
		}
		
		providers[pinInd] = provider;
		pinMap[pinInd] = pin;
		outputPinsStandard[pinInd] = null;
	}
	
	

	public void resetCheckingInputPin(int pinInd)
	{
		startIfNeeded();
		
		pinCheckingStatus[pinInd] = 0;
	}
	
	
	protected void reset()
	{		
		lowAllPin();
		
		nonstandardPins.clear();
		
		for(int i = 0; i < 28; i++)
			pinCheckingStatus[i] = 0;
	}
	
	protected void quit()
	{
		nonstandardPins.clear();
		
		if (gpio != null)
		{
			lowAllPin();
			gpio.shutdown();
		}
		gpio = null;
	}
	
	private void lowAllPin()
	{
		if (gpio != null)
		{
			for(GpioPinDigitalOutput out: outputPinsJava)
				if (out != null) out.low();
			for(GpioPinDigitalOutput out: outputPinsStandard)
				if (out != null) out.low();
		}
	}



	Map<Integer, KeyCode> hookedKeys = new HashMap<>();
	
	public void clickedKeyButton(Button bt, String keyText)
	{
		String pinNum = "";
		
		keyText = keyText.trim();
		
		for(int i = keyText.length()-1; i > 0; i--)
		{
			if (keyText.substring(i).startsWith(" "))
			{
				pinNum = keyText.substring(i+1);
				break;
			}
		}
		
		int pin = Integer.valueOf(pinNum);

		KeyCode oldKey = hookedKeys.getOrDefault(pin, null);
		KeyCode cd = GuiMsgHelper.getAnyKeyNonblockingUI("Press the desired key on your keyboard to associate this GPIO with.\nWhen you press that key afterwards, the pin connection will simulated.\n\nNote: This does not affect the program when deployed!\nThe purpose is debugging only.", oldKey);
		
		if (cd != oldKey)
		{
			KeyChecker.addPressedHook(oldKey, () -> {});
			KeyChecker.addReleasedHook(oldKey, () -> {});
			hookedKeys.remove(pin);
		}
		
		if (cd == KeyCode.DEAD_BREVE)
		{
			KeyChecker.addPressedHook(oldKey, () -> {});
			KeyChecker.addReleasedHook(oldKey, () -> {});
			
			hookedKeys.remove(pin);
			Platform.runLater(() -> bt.setText("Key"));
			
			return;
		}
		if (cd == null)
		{
			return;
		}
		
		
		int res = GuiMsgHelper.askQuestion("You chosed the following key: " + cd.getName() + "\n\nDo you want to simulate a button?\n->The GPIO is connected as long as the key is down.\n\nOr a switch?\n->Every press switches to connected or diconnected.", new String[] {"Button", "Inverted Button", "Switch"}, true);
		
		
		switch(res)
		{
		case 0:
			KeyChecker.addPressedHook(cd, () -> {pinCheckingStatus[pin] = 2;}); // Todo: Add the debug messages
			KeyChecker.addReleasedHook(cd, () -> {pinCheckingStatus[pin] = 1;});
			break;
		case 1:
			KeyChecker.addPressedHook(cd, () -> {pinCheckingStatus[pin] = 1;});
			KeyChecker.addReleasedHook(cd, () -> {pinCheckingStatus[pin] = 2;});
			break;
		case 2:
			KeyChecker.addPressedHook(cd, () -> {pinCheckingStatus[pin] = (byte) ((pinCheckingStatus[pin]==2) ? 1 : 2);});
			break;
		case -1:
			return;
		}
		
		hookedKeys.put(pin, cd);
		
		
		Platform.runLater(() -> bt.setText("Key: " + cd.getName()));
	}


	
	public Pin getPin(int pin) throws NonExistingPinException
	{
		if ((pin < 0) || (pin >= pinMap.length))
			throw new NonExistingPinException(pin);
		else
			return(pinMap[pin]);
	}
	
	/*
	public static void pinExists(int pin) throws NonExistingPinException
	{
		if ((pin < 0) || (pin >= pinMap.length))
			throw new NonExistingPinException(pin);
	}
	*/

	
	public GpioPinDigitalInput getInputPin(int pinInd, boolean pullUp)
	{
		return(gpio.provisionDigitalInputPin(providers[ pinInd ], pinMap[pinInd], pullUp ? PinPullResistance.PULL_UP : PinPullResistance.PULL_DOWN));
	}

	
	
	// Only for simulations!
	class OnOffSimulatorDummy implements GpioPinDigitalOutput
	{

		@Override
		public PinState getState() {
			return null;
		}

		@Override
		public boolean isHigh() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isLow() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isState(PinState arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void addListener(GpioPinListener... arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addListener(List<? extends GpioPinListener> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void clearProperties() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void export(PinMode arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void export(PinMode arg0, PinState arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Collection<GpioPinListener> getListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PinMode getMode() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Pin getPin() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, String> getProperties() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getProperty(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getProperty(String arg0, String arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GpioProvider getProvider() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PinPullResistance getPullResistance() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GpioPinShutdown getShutdownOptions() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getTag() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasListener(GpioPinListener... arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean hasProperty(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isExported() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isMode(PinMode arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isPullResistance(PinPullResistance arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeAllListeners() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeListener(GpioPinListener... arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeListener(List<? extends GpioPinListener> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeProperty(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setMode(PinMode arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setName(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setProperty(String arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setPullResistance(PinPullResistance arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setShutdownOptions(GpioPinShutdown arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setShutdownOptions(Boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setShutdownOptions(Boolean arg0, PinState arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setShutdownOptions(Boolean arg0, PinState arg1, PinPullResistance arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setShutdownOptions(Boolean arg0, PinState arg1, PinPullResistance arg2, PinMode arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setTag(Object arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void unexport() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Future<?> blink(long arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> blink(long arg0, PinState arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> blink(long arg0, long arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> blink(long arg0, long arg1, PinState arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void high() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void low() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Future<?> pulse(long arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, Callable<Void> arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, boolean arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, PinState arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, boolean arg1, Callable<Void> arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, PinState arg1, Callable<Void> arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, PinState arg1, boolean arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, PinState arg1, boolean arg2, Callable<Void> arg3) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setState(PinState arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setState(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void toggle() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Future<?> blink(long arg0, TimeUnit arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> blink(long arg0, PinState arg1, TimeUnit arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> blink(long arg0, long arg1, TimeUnit arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> blink(long arg0, long arg1, PinState arg2, TimeUnit arg3) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, TimeUnit arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, Callable<Void> arg1, TimeUnit arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, boolean arg1, TimeUnit arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, PinState arg1, TimeUnit arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, boolean arg1, Callable<Void> arg2, TimeUnit arg3) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, PinState arg1, Callable<Void> arg2, TimeUnit arg3) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, PinState arg1, boolean arg2, TimeUnit arg3) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulse(long arg0, PinState arg1, boolean arg2, Callable<Void> arg3, TimeUnit arg4) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}




	
}
