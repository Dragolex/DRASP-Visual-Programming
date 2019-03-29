package productionGUI.controlers;

import java.util.ArrayList;
import java.util.List;

import dataTypes.DataNode;
import dataTypes.ProgramElement;
import main.DataControler;
import productionGUI.ProductionGUI;
import productionGUI.sections.elementManagers.ContentsSectionManager;
import productionGUI.sections.subelements.SubElementField;

public class UndoRedoControler {

	private static UndoRedoControler self;
	
	
	int cursor = 0;
	
	List<Object> historyNamesOrOrigNodes = new ArrayList<>();
	List<Object> historyNodes = new ArrayList<>();
	
	Object historyNamesOrOrigNodeNew;
	Object historyNodeNew;
	
	
	public UndoRedoControler()
	{
		self = this;
	}
	
	
	public static UndoRedoControler getSelf()
	{
		return(self);
	}


	public void undo()
	{
		if (cursor == 0) return;
		
		cursor--;
		
		loadHistoryPosition(historyNamesOrOrigNodes.get(cursor), historyNodes.get(cursor), false);
	}
	
	public void redo()
	{
		if (cursor >= historyNodes.size()) return;
		
		if (cursor == historyNodes.size()-1)
		{
			cursor++;
			
			loadHistoryPosition(historyNamesOrOrigNodeNew, historyNodeNew, true);
		}
		else
		{			
			cursor++;
			
			loadHistoryPosition(historyNamesOrOrigNodes.get(cursor), historyNodes.get(cursor), true);
		}
	}
	
	
	private void loadHistoryPosition(Object historyNamesOrOrigNode, Object historyNode, boolean toRedo)
	{	
		if (historyNode instanceof DataNode<?>) // if DataNode variant (means it contains a copy of the whole root)
		{
			DataControler.replaceData((String) historyNamesOrOrigNode, (DataNode<ProgramElement>) historyNode);
			ContentsSectionManager.getSelf().setCurrentPage((String) historyNamesOrOrigNode);
			ContentsSectionManager.getSelf().switchDataPage((String) historyNamesOrOrigNode, false); // TODO: APPLY TO ALL
			ContentsSectionManager.getSelf().renewElementsRealizationFull();
		}
		else
		{
			((SubElementField) historyNode).replaceValue(((String[]) historyNamesOrOrigNode)[toRedo ? 1 : 0]);
		}
	}
	
	
	public void appliedChange(SubElementField elementField, String oldText, String newText)
	{
		deleteUpperHistory();
		
		if (cursor <= historyNodes.size()-1)
		{
			historyNamesOrOrigNodes.set(cursor, new String[] {oldText, newText});
			historyNodes.set(cursor, elementField);
		}
		else
		{
			historyNamesOrOrigNodes.add(cursor, new String[] {oldText, newText});
			historyNodes.add(cursor, elementField);
		}
		
		cursor++;
		
		
		historyNodeNew = elementField;
		historyNamesOrOrigNodeNew = new String[] {oldText, newText};
		
		ProductionGUI.hasChangedContent();

	}
	
	
	public void appliedChange(String historyName, boolean isAfter)
	{
		DataNode<ProgramElement> origNode = DataControler.getPageNodeForAdd(historyName);
		
		DataNode<ProgramElement> newNode = new DataNode<ProgramElement>(null);
		baseClone(origNode, newNode);

		if (!isAfter)
		{
			deleteUpperHistory();
			
			if (cursor <= historyNodes.size()-1)
			{
				historyNamesOrOrigNodes.set(cursor, historyName);
				historyNodes.set(cursor, newNode);
			}
			else
			{
				historyNamesOrOrigNodes.add(cursor, historyName);
				historyNodes.add(cursor, newNode);
			}
			
			cursor++;
		}
		else
		{
			historyNamesOrOrigNodeNew = historyName;
			historyNodeNew = newNode;
		}
		
		if (ProductionGUI.isAvailable())
			ProductionGUI.hasChangedContent();
	}

	private void deleteUpperHistory()
	{
		int ind = cursor+1;
		while(ind < historyNamesOrOrigNodes.size())
		{
			historyNamesOrOrigNodes.remove(ind);
			historyNodes.remove(ind);
		}
	}


	public void baseClone(DataNode<ProgramElement> origNode, DataNode<ProgramElement> newNode)
	{
		newNode.setData(origNode.getData());
		
		for(DataNode<ProgramElement> child: origNode.getChildrenAlways())
			baseClone(child, (new DataNode<ProgramElement>(null)).setParent(newNode));
	}


	public void replacePageName(String oldPageName, String newPageName)
	{
		for (int i = 0; i < historyNamesOrOrigNodes.size(); i++)
		{
			if (historyNodes.get(i) instanceof DataNode<?>)
				if (((String)historyNamesOrOrigNodes.get(i)).equals(oldPageName))
					historyNamesOrOrigNodes.set(i, newPageName);
		}
		
		if (historyNodeNew instanceof DataNode<?>)
			if (((String)historyNamesOrOrigNodeNew).equals(oldPageName))
				historyNamesOrOrigNodeNew = newPageName;
	}
	
	
	

}
