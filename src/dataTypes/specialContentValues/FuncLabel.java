package dataTypes.specialContentValues;

import execution.EventInstance;
import execution.handlers.LabelHandler;

public class FuncLabel {
	
	String labelName;
	EventInstance associatedEvent = null;
	Integer labelPositionIndex = null;
	
	
	public FuncLabel()
	{
		labelName = LabelHandler.getNewAutocreatedName(); // create a unique name
		LabelHandler.saveLabel(this); // save into a map
	}
	
	public FuncLabel(String labelName)
	{
		this.labelName = labelName; // assign the name
		LabelHandler.saveLabel(this); // save into a map
	}
	
	
	
	public String getName()
	{
		return(labelName);
	}

	public boolean hasBeenPrepared()
	{
		return((associatedEvent != null) || (labelPositionIndex != null));
	}
	
	public void setEvent(EventInstance associatedEvent)
	{
		this.associatedEvent = associatedEvent;		
	}
	public void setPositionIndex(int labelPositionIndex)
	{
		this.labelPositionIndex = labelPositionIndex;		
	}

	public EventInstance getEvent()
	{
		return(associatedEvent);
	}

	public Integer getPositionIndex()
	{
		return(labelPositionIndex);
	}
	
	@Override
	public String toString()
	{
		return(labelName);
	}

	public void reset()
	{	
		associatedEvent = null;
		labelPositionIndex = null;
	}
}
