package staticHelpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import dataTypes.ProgramEventContent;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import main.functionality.Functionality;


public class KeyChecker
{
    static Map<KeyCode, Boolean> state = new Hashtable<>();
    static Set<KeyCode> stateSet = state.keySet();
    static KeyCode lastKey = null;
    
    static List<KeyCode> currentlyDownList = new ArrayList<>();
    
	public static void initForStage(Stage stage)
    {		
		stage.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke)
            {
            	KeyCode pressed = ke.getCode();
            	lastKey = pressed;
            	
            	
            	if (!state.getOrDefault(pressed, false)) // if not already down
            	{
                	Runnable runnable = pressedHooks.getOrDefault(pressed, null);
                	if (runnable != null) runnable.run();

                	for(KeyCode key: stateSet)
                		if (pressed == key)
                			state.put(key, true); // only puts if that entry already exists!
            		
                	currentlyDownList.add(pressed);
            	}
            	
            	signalizePossibleKeyEvents(pressed, true, false);

            }
		});
		
		stage.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke)
            {
            	KeyCode released = ke.getCode();

            	Runnable runnable = releasedHooks.getOrDefault(released, null);
            	if (runnable != null) runnable.run();
            	

            	for(KeyCode key: stateSet)
            		if (released == key)
            			state.put(key, false);
            	
            	currentlyDownList.removeAll(Collections.singleton(released));
            	
            	signalizePossibleKeyEvents(released, false, true);
            }
		});
		
		
		KeyChecker.addKeyToCheck(KeyCode.CONTROL);
		KeyChecker.addKeyToCheck(KeyCode.SHIFT);
    }	
	
	
    
    protected static void signalizePossibleKeyEvents(KeyCode key, boolean press, boolean release)
    {
		if (!Execution.isPaused())
		if(Execution.isRunning())
		{
			Object[] dat = {key, press};
			
			for(ProgramEventContent cont: Functionality.keyPressedEventContents)
				cont.triggerExternally(dat); // pass the input value and trigger
		}
	}



	public static void addKeyToCheck(KeyCode key)
    {
    	if (!stateSet.contains(key))
    		state.put(key, false);
    }

    public static boolean isDown(KeyCode key)
    {
        synchronized (KeyChecker.class)
        {
        	if (!state.containsKey(key))
        	{
        		InfoErrorHandler.callGUIerror("Trying to check a key which has not been marked to be tracked. Key: " + key.getName());
        		return(false);
        	}
        	
       		return(state.get(key));
        }
    }


	public static void cancelDown(KeyCode key)
	{
        synchronized (KeyChecker.class)
        {
        	if (!state.containsKey(key))
        		InfoErrorHandler.callGUIerror("Trying to cancel a key which has not been marked to be tracked. Key: " + key.getName());
        	else
        		state.put(key, false);
        }
	}
    
	
	public static void cancelAll()
	{
		for(Entry<KeyCode, Boolean> entry: state.entrySet())
			state.put(entry.getKey(), false);
	}
    
    private static Map<String, KeyCode> keyCodes = new Hashtable<>();
    
    
    public static KeyCode getKeyCode(String str)
    {
    	if (keyCodes.containsKey(str))
    		return(keyCodes.get(str));
    	else
    	{
    		KeyCode newKey = null;
    		try
    		{
    			newKey = KeyCode.valueOf(str);
    		} catch (IllegalArgumentException e) {return(null);};
    		
    		if (newKey == null)
    			return(null);
    		keyCodes.put(str, newKey);
    		return(newKey);
    	}
    }
    
    
    public static void clearLastKey()
    {
    	lastKey = null;
    }
    
    public static KeyCode getLastKey()
    {
    	return(lastKey);
    }
    
    
	
	// Hooks
	
    private static Map<KeyCode, Runnable> pressedHooks = new HashMap<>();
    private static Map<KeyCode, Runnable> releasedHooks = new HashMap<>();

    public static void addPressedHook(KeyCode cd, Runnable runnable)
	{
    	pressedHooks.put(cd, runnable);
	}
	public static void addReleasedHook(KeyCode cd, Runnable runnable)
	{
		releasedHooks.put(cd, runnable);
	}



	public static List<KeyCode> getCurrentlyDown()
	{
		return(currentlyDownList);
	}
   
}