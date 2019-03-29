package dataTypes.minor;

public class ObjectHolder<T> {

	public T obj;
	
	public ObjectHolder()
	{
		this.obj = null;
	}

	public ObjectHolder(T obj)
	{
		this.obj = obj;
	}
	
	public void setObject(T obj)
	{
		this.obj = obj;
	}
	
	public T getObject()
	{
		return(obj);
	}
	
}
