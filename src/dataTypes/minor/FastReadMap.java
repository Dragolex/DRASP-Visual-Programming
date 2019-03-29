package dataTypes.minor;

public class FastReadMap<dataType>
{
	/*
	Map that writes acceptably slow but reads relatively fast
	
	// TODO: Compare speed of this and HashMap
	*/
	
	int indices[] = new int[1];
	Object data[] = new Object[1];
	int currentSize = 1;
	int currentIndex = 0;
	
	
	public void add(int ind, dataType dat)
	{
		if (currentIndex >= currentSize)
		{
			
			int[] newIndices = new int[currentSize*2];
			Object[] newData = new Object[currentSize*2];
			System.arraycopy(indices, 0, newIndices, 0, currentSize);
			System.arraycopy(data, 0, newData, 0, currentSize);
			
			indices = newIndices;
			data = newData;
			
			currentSize *=2;
		}
		
		indices[currentIndex] = ind;
		data[currentIndex] = dat;
		
		currentIndex++;
	}
	
	public boolean has(int ind)
	{
		for(int i = 0; i < currentIndex; i++)
			if (indices[i] == ind) return(true);
		
		return(false);
	}

	public dataType get(int ind)
	{
		for(int i = 0; i < currentIndex; i++)
			if (indices[i] == ind) return((dataType) (data[i]));
		
		return(null);
	}

	
}
