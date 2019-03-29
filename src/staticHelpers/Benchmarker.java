package staticHelpers;

public class Benchmarker
{
	static long lastTime = 0;
	static long maxDif = 0;
	static String maxStr = "";
	
	public static void bench(String name)
	{
		if (lastTime != 0)
		{
			long dif = (System.nanoTime()-lastTime)/1000;
			
			if (dif >= maxDif)
			{
				maxDif = dif;
				maxStr = name;
			}
			
			System.out.println("New max: " + maxStr + " - Time: " + ((double)maxDif/1000) + "ms");
		}
		
		lastTime = System.nanoTime();
	}
	
	public static void clear()
	{
		lastTime = 0;
		maxDif = 0;
		maxStr = "";
	}

}
