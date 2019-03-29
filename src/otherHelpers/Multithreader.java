package otherHelpers;

import java.util.ArrayList;
import java.util.List;

public class Multithreader {
	
	List<Thread> threads = new ArrayList<>();
	
	public void add(Runnable task)
	{
		threads.add(new Thread(task));
	}
	
	public void runAll()
	{
		for(Thread thread: threads)
			thread.start();

		try {
			for(Thread thread: threads)
				thread.join();
		} catch (InterruptedException e) {}
	}
}
