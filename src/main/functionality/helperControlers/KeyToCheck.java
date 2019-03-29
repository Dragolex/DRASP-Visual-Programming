package main.functionality.helperControlers;

import java.util.ArrayList;
import java.util.List;

import execution.Execution;
import javafx.scene.input.KeyCode;
import staticHelpers.KeyChecker;

public class KeyToCheck
{
	List<KeyCode> keyCodes = new ArrayList<>();
	String checkerString = null;
	
	volatile boolean alreadyPressed = false;
	
	boolean ANY = false;
	boolean ANY_LETTER = false;
	boolean ANY_LOWER = false;
	boolean ANY_UPPER = false;
	boolean ANY_NUMBER = false;
	boolean ANY_SPECIAL = false;
	boolean ANY_ALTERING = false;
	
	

	
	public void updateIfChanged(String checkerString)
	{
		if (checkerString == null)
		{
			Execution.setError("A valid key representation is required!", false);
			return;
		}
		
		if ((this.checkerString == null) || (!this.checkerString.equals(checkerString.toUpperCase())))
			update(checkerString);
	}

	public void update(String newCheckerString)
	{
		if (newCheckerString == null)
		{
			Execution.setError("A valid key representation is required!", false);
			return;
		}
		
		checkerString = newCheckerString.toUpperCase();
		
		
		keyCodes.clear();
		
		ANY = false;
		ANY_LETTER = false;
		ANY_LOWER = false;
		ANY_UPPER = false;
		ANY_NUMBER = false;
		ANY_SPECIAL = false;
		ANY_ALTERING = false;
		
		if (checkerString.contains(","))
		{
			for(String possibility: checkerString.split(","))
				updatePossibility(possibility.trim());			
		}
		else
			updatePossibility(checkerString.trim());
		
	}
	
	private void updatePossibility(String possibility)
	{
		switch(possibility)
		{
		
		case "ANY": ANY = true;  break;
		case "ANY_LETTER": ANY_LETTER = true;  break;
		case "ANY_LOWER": ANY_LOWER = true;  break;
		case "ANY_UPPER": ANY_UPPER = true;  break;
		case "ANY_NUMBER": ANY_NUMBER = true; break;
		//case "ANY_SPECIAL": ANY_SPECIAL = true;  break;
		case "ANY_ALTERING": ANY_ALTERING = true; ; break;

		default:
			KeyCode key = KeyChecker.getKeyCode(possibility);
			
			if (key == null)
			{
				Execution.setError("The following text is not a valid key representation: " + possibility, false);
				return;
			}
			
			keyCodes.add(key);
								
			break;
		}
		
	}

	public boolean isPrepared()
	{
		return(checkerString != null);
	}
	

	
	/*
	 * key: keycode to check
	 * pressed: whetehr the key has been presse d(true) or released (false)
	 * checkingMode
	 */
	public boolean check(KeyCode key, boolean beenPressed, boolean shouldBePressed, boolean allowKeyRepeat)
	{
		if (shouldBePressed)
		{
			if(!allowKeyRepeat)
				if (beenPressed)
					if (alreadyPressed)
							return(false);
			
			if (!beenPressed)
				alreadyPressed = false;	
			else
				alreadyPressed = true;
		}
		else
			alreadyPressed = false;		
	

		
		if (beenPressed != shouldBePressed)
			return(false); // abort
		
		
		for(KeyCode k: keyCodes)
			if(k.equals(key))
				return(true);
		
		if (ANY)
			return(true);
		
		if(ANY_LETTER)
			if (key.isLetterKey() || key.isWhitespaceKey()) return(true);

		if(ANY_LOWER)
			if ((key.isLetterKey() && (!KeyChecker.isDown(KeyCode.SHIFT))) || key.isWhitespaceKey()) return(true);
		
		if(ANY_UPPER)
			if ((key.isLetterKey() && KeyChecker.isDown(KeyCode.SHIFT)) || key.isWhitespaceKey() ) return(true);
		
		if(ANY_NUMBER)
			if (key.isDigitKey() && !KeyChecker.isDown(KeyCode.CONTROL) && !KeyChecker.isDown(KeyCode.SHIFT)) return(true);

		/*
		if(ANY_SPECIAL)
			if (decide( key.isKeypadKey() .is .isModifierKey(), checkingMode)) return(true);*/
		
		if(ANY_ALTERING)
			return(key.isModifierKey());
		
		return(false);
	}
	
	/*
	private boolean decide(boolean res, int checkingMode)
	{
		switch(checkingMode)
		{
		case 0:
			
			System.out.println("Checking witH; lastState: " + lastState );
			
			if (lastState == null)
			{
				lastState = res;
				return(false);
			}
			else
				if (!lastState)
					return(lastState = res);
				else
					lastState = res;
			return(false);
		
			
		case 1:
			return(res);
			
			
		case 2:
			if (lastState == null)
			{
				lastState = res;
				return(false);
			}
			else
				if (lastState)
					return(!(lastState = res));
				else
					lastState = res;
			return(false);
			
		}
		
		
		return(false);
	}
	*/
	
	public void clear()
	{
		keyCodes.clear();
		checkerString = null;
		
		alreadyPressed = false;
		
		ANY = false;
		ANY_LETTER = false;
		ANY_LOWER = false;
		ANY_UPPER = false;
		ANY_NUMBER = false;
		ANY_SPECIAL = false;
		ANY_ALTERING = false;
	}
	
}
