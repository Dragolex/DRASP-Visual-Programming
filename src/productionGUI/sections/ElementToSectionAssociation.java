package productionGUI.sections;

import java.util.HashMap;
import java.util.Map;

import productionGUI.sections.elementManagers.AbstractSectionManager;

public class ElementToSectionAssociation
{
	static private Map<String, AbstractSectionManager> associations = new HashMap<String, AbstractSectionManager>();
	
	static public void addAssociation(String functionalityName, AbstractSectionManager assoc)
	{
		if (!associations.containsKey(functionalityName))
			associations.put(functionalityName, assoc);		
	}
	
	static public AbstractSectionManager getAssociation(String functionalityName)
	{
		if (!associations.containsKey(functionalityName))
			return(null);
		
		return(associations.get(functionalityName));	
	}
	
}
