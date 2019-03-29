package dataTypes.minor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * This is basically a list but which can take multiple objects for every position.
 */
public class MultiPositionList<T> implements Iterable<T> {

	List<List<T>> data;
	int totalSize = 0;	
	List<T> endList;
	
	public MultiPositionList()
	{
		data = new ArrayList<List<T>>();
		endList = new ArrayList<T>();
		totalSize = 0;
	}
	
	public void place(int pos, T obj)
	{
		totalSize++;
		
		if (pos < 0)
		{
			endList.add(obj);
			return;
		}
		
		while(pos >= data.size())
			data.add(new ArrayList<T>());
		
		data.get(pos).add(obj);
	}

    public Iterator<T> iterator() {

        return new MultiPositionListIterator<T>();
    }

    public class MultiPositionListIterator<TT> implements Iterator<T> {

        private int index;
        private int subIndex;
        private int totalIndex;
        
        public MultiPositionListIterator() {

            index = 0;
            subIndex = 0;
            totalIndex = 0;
        }

        @Override
        public boolean hasNext()
        {
            return totalIndex < totalSize;
        }

        @Override
        public T next()
        {
        	while(index < data.size())
        	{
        		if (subIndex < data.get(index).size())
	        	{
	        		subIndex++;
	            	totalIndex++;

	        		return(data.get(index).get(subIndex-1));
	        	}
	        	else
	        	{
	        		subIndex = 0;
	        		index++;        		
	        	}
        	}
    		if (subIndex < endList.size())
    		{
        		subIndex++;
            	totalIndex++;

        		return (endList.get(subIndex-1));
    		}
        	return(null);
        }
    } 
}
