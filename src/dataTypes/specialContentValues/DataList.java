package dataTypes.specialContentValues;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import execution.Execution;
import main.functionality.helperControlers.spline.DataSpline;
import settings.GlobalSettings;

public class DataList
{	
	//private static List<DataList> dataLists = new ArrayList<>();
	
	
	public static DataList externalSet(Variable variable, int ind, Term term, int newType)
	{
		DataList list;
		
		if (variable.getType() == Variable.dataListType)
			list = (DataList) variable.getUnchecked();
		else
		{
			list = new DataList();
			list.initType(newType);
			variable.initTypeAndSet(Variable.dataListType, list);
		}
		
		
		if (!list.hasIndex(ind))
			list.set(ind, term.getRightSide());
		else
			list.set(ind, term.applyTo( list.get(ind), list.getType() ) );
		
		return(list);
	}
	
	
	

	/*
	public DataList()
	{
		dataLists.add(this);
	}
	*/
	

	private int type;
	//private LinkedList<Object> data = new LinkedList<>();
	private Map<Integer, Object> data = new LinkedHashMap<>();
	
	

	public void initType(int type)
	{
		this.type = type;
	}

	public int getType()
	{
		return(type);
	}
	
	
	
	
	public Object get(int ind)
	{
		if (!data.containsKey(ind))
			Execution.setError("The index '" + ind + "' inside the list has never been set!", false);
		else
			return(data.get(ind));
		
		return(null);
		
		/*
		if (ind < 0)
		{
			if (ind == -1)
				return(data.peekLast());
			return(data.peekFirst());
		}
		return(data.get(ind));
		*/
	}
	
	
	public void set(int ind, Object newData)
	{
		data.put(ind, newData);
	}
	
	private boolean hasIndex(int ind)
	{
		return(data.containsKey(ind));
	}	
	
	public void remove(int ind)
	{
		data.remove(ind);
	}

	public int getSize()
	{
		return(data.size());
	}

	public void clear()
	{
		data.clear();
	}

	
	public String printListForDebug()
	{
		StringBuilder listContent = new StringBuilder();
		for(Entry<Integer, Object> item: data.entrySet())
		{
			listContent.append(item.getKey());
			listContent.append(": ");
			listContent.append(Variable.getDebugTypeValueString(type, item.getValue()));			
			listContent.append("\n");			
		}
		return(listContent.toString());
	}
	
	static String separator = "|#--#|";
	
	public String toPersistibleString()
	{
		StringBuilder str = new StringBuilder();
		
		str.append(type);
		str.append(separator);
		
		for(Entry<Integer, Object> item: data.entrySet())
		{
			str.append(item.getKey());
			str.append(separator);
			
			switch(type)
			{
			case Variable.boolType:
				str.append(((Boolean) item.getValue()) ? "T" : "F");
				break;
				
			case Variable.doubleType:
				str.append( GlobalSettings.doubleFormatter.format((Double) item.getValue()) );
				break;
				
			case Variable.textType:
				str.append((String) item.getValue());
				break;
				
			case Variable.splineType:
				str.append( ((DataSpline) item.getValue()).toPersistibleString() );
				break;
			}
			
			str.append(separator);
		}
		return(str.toString());
	}

	public static DataList fromPersistibleString(String str)
	{
		String[] blocks = str.split(Pattern.quote(separator));

		DataList list = new DataList();
		int type = Integer.valueOf(blocks[0]);
		list.initType(type);
		
		int len = blocks.length;
		switch(type)
		{
			case Variable.boolType:
				for(int i = 1; i < len; i += 2)
					list.set(Integer.valueOf(blocks[i]), (Boolean) blocks[i+1].equals("T"));
				break;
				
			case Variable.doubleType:
				for(int i = 1; i < len; i += 2)
					list.set(Integer.valueOf(blocks[i]), Double.valueOf(blocks[i+1]));
				break;
				
			case Variable.textType:
				for(int i = 1; i < len; i += 2)
					list.set(Integer.valueOf(blocks[i]), blocks[i+1]);
				break;
				
			case Variable.splineType:
				for(int i = 1; i < len; i += 2)
					list.set(Integer.valueOf(blocks[i]), DataSpline.fromPersistibleString(blocks[i+1]));
				break;
		}
		
		return(list);
	}

	

}
