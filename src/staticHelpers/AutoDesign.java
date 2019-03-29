package staticHelpers;

import javafx.scene.input.KeyCode;

public class AutoDesign {
	
	public static double[] val = new double[10];
	public static String[] name = new String[10];
		
	private static int currentChosen = 0;
	
	public static void apply(String nameA, double valA, String nameB, double valB, String nameC, double valC, double incrementStep, Runnable task)
	{
		val[0] = valA;
		val[1] = valB;
		val[2] = valC;

		name[0] = nameA;
		name[1] = nameB;
		name[2] = nameC;
		
		start(3, incrementStep, task);
	}
	
	public static void apply(String nameA, double valA, String nameB, double valB, String nameC, double valC, String nameD, double valD, String nameE, double valE, double incrementStep, Runnable task)
	{
		val[0] = valA;
		val[1] = valB;
		val[2] = valC;
		val[3] = valD;
		val[4] = valE;

		name[0] = nameA;
		name[1] = nameB;
		name[2] = nameC;
		name[3] = nameD;
		name[4] = nameE;
		
		start(5, incrementStep, task);
	}


	private static void start(int elements, double incrementStep, Runnable task)
	{
		currentChosen = 0;
				
		int index = -1;
		for(KeyCode key: KeyCode.values())
		{
			if ((key == KeyCode.A) || (index >= 0))
			{
				index++;
				
				if (index > elements)
				{
					index = -1;
					return;
				}
				
				int nInd = index;
				KeyChecker.addPressedHook(key, () -> {
					currentChosen = nInd;
					System.out.println("AutoDesign - Editing: " + name[currentChosen] + " Current value: " + val[currentChosen]);
				});
			}	
		}
		
		KeyChecker.addPressedHook(KeyCode.RIGHT, () -> {
			currentChosen = Math.max(0, Math.min(elements-1, currentChosen+1));
			
			System.out.println("AutoDesign - " + name[currentChosen] + ": " + val[currentChosen]);			
		});
		
		KeyChecker.addPressedHook(KeyCode.LEFT, () -> {
			currentChosen = Math.max(0, Math.min(elements-1, currentChosen-1));
			
			System.out.println("AutoDesign - " + name[currentChosen] + ": " + val[currentChosen]);			
		});

		
		KeyChecker.addPressedHook(KeyCode.UP, () -> {
			val[currentChosen] += incrementStep;
			val[currentChosen] = (double)Math.round(val[currentChosen] * 10000d) / 10000d; // round on 4 decimals
			
			System.out.println("AutoDesign - " + name[currentChosen] + ": " + val[currentChosen]);			
			task.run();
		});
		
		KeyChecker.addPressedHook(KeyCode.DOWN, () -> {
			val[currentChosen] -= incrementStep;			
			val[currentChosen] = (double)Math.round(val[currentChosen] * 10000d) / 10000d; // round on four decimals

			System.out.println("AutoDesign - " + name[currentChosen] + ": " + val[currentChosen]);			
			task.run();
		});

		
		task.run();
	}
	
	
}
