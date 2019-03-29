package dataTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiMap<T> {

	Map<T, List<T>> data = new HashMap<>();

	public void addBidirectional(T base, T A, T B, T C, T D)
	{
		addBidirectional(base, A);
		addBidirectional(base, B);
		addBidirectional(base, C);
		addBidirectional(base, D);
	}
	public void addBidirectional(T base, T A, T B, T C)
	{
		addBidirectional(base, A);
		addBidirectional(base, B);
		addBidirectional(base, C);		
	}
	public void addBidirectional(T base, T A, T B)
	{
		addBidirectional(base, A);
		addBidirectional(base, B);
	}
	public void addBidirectional(T base, T A)
	{
		add(base, A);
		add(A, base);
	}
		
	public void add(T base, T A)
	{
		if (data.containsKey(base))
			data.get(base).add(A);
		else // a new list is needed
		{
			List<T> list = new ArrayList<>();
			list.add(A);
			data.put(base, list); // add the new list
		}
	}
	
	public boolean contains(Integer base, Integer A)
	{
		if (!data.containsKey(base))
			return(false);
		
		return(data.get(base).contains(A));
	}
	
	

	
}
