package dataTypes;

public abstract class ProgramElement {
	
	//public abstract String makeStringRepresentation();
	
	//public abstract Object[] getArgumentValues();
	
	//-public abstract ProgramElement markAsEvent();
	public abstract boolean isEvent();
	
	//public abstract boolean isConditionalElement();
	
	//public abstract boolean canHaveChildElements();

	public abstract String getFunctionalityName();

	public abstract void setUndeletable(boolean undeletable);

	public abstract FunctionalityContent getContent();

	//public abstract void resetIfPossible(int index);

	//public abstract void reloadIfPossible(int index);

	//-public abstract ProgrammElement recreateContent();

}
