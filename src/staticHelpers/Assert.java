package staticHelpers;

public class Assert {
	
	public static void checkNull(Object isNull) throws NullPointerException
	{
		if (isNull == null)
			throw new NullPointerException();
	}

}
