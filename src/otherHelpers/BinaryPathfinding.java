package otherHelpers;

import java.util.ArrayList;
import java.util.List;

public class BinaryPathfinding {
	
	byte[][] parGrid;
	byte[][] grid;
	boolean preferNonVertical;
	
	int x, y;
	
	int goalx;
	int goaly;
	
	byte BLOCKED = 5;
	byte VISITED_OFFS = 16;
	
	
	public BinaryPathfinding(byte[][] grid, boolean preferNonVertical)
	{
		this.parGrid = grid;
		this.preferNonVertical = preferNonVertical;
		
	}
	
	public int[] findPath(int xx, int yy, int goalx, int goaly)
	{				
		x = xx;
		y = yy;
		
		this.goalx = goalx;
		this.goaly = goaly;
		
		
		int[] posPos = new int[300*2];
		int posInd = 0;
		

		int length = this.parGrid.length;
		int height = this.parGrid[0].length;
	    grid = new byte[length][height];
	    for (int i = 0; i < length; i++)
		    for (int j = 0; j < height; j++)
		    	grid[i][j] = (this.parGrid[i][j] == 1) ? BLOCKED : 0;
		    	//System.arraycopy(this.parGrid[i], 0, grid[i], 0, this.parGrid[i].length);
		
		//System.out.println("STARTED");
		
		while(x != goalx || y != goaly)
		{
			x = Math.max(1, x);
			y = Math.max(1, y);
			
			if (posInd >= 590)
				return(null);
			

			if (grid[x][y] == BLOCKED || grid[x][y] >= VISITED_OFFS)
			{
				posInd -= 2;
				if (posInd < 0)
					posInd = 0;
					//return(posPos);
				
				x = posPos[posInd];
				y = posPos[posInd+1];
			}
			else
			{
				posPos[posInd] = x;
				posPos[posInd+1] = y;
				
				posInd += 2;
			}
			
			
			int dir = getDir(x,y)+8;
			
			int optA = 1;
			int optB = -1;
			
			double dirAcc = getDir2(x,y)+8;

			
			if ((((double) dir % 8) + 8) > ((dirAcc % 8) + 8))
			{
				optA = -1;
				optB = 1;
			}
			
			
			if (tryDir(x,y, dir))
				continue;
			
			if (tryDir(x,y, dir+optA))
				continue;
			if (tryDir(x,y, dir+optB))
				continue;
			if (tryDir(x,y, dir+2*optA))
				continue;
			if (tryDir(x,y, dir+2*optB))
				continue;
			if (tryDir(x,y, dir+3*optA))
				continue;
			if (tryDir(x,y, dir+3*optB))
				continue;
			if (tryDir(x,y, dir-4))
				continue;

			
			posInd -= 2;
			if (posInd < 0)
				posInd = 0;
			x = posPos[posInd];
			y = posPos[posInd+1];
		
		}

		//System.out.println("END");
		
		int trueSize = posInd;

			for (int i = 0; i < posInd; i+=2)
			{
				
				for (int j = posInd-2; j > i; j-=2)
				{
					if ((posPos[i] == posPos[j]) && (posPos[i+1] == posPos[j+1])) // same node crossed twice.
					{
						for (int k = i; k < j+2; k+=2)
						{
							posPos[k] = posPos[i];
							posPos[k+1] = posPos[i+1];
							trueSize -= 2;
						}
						trueSize += 2;


						i = j;
					}	
				}
				
			}
			
		int[] retArr = new int[trueSize+2];
		int lx = Integer.MIN_VALUE, ly = Integer.MIN_VALUE;
		int ind = 0;
		for (int i = 0; i < posInd-1; i+=2)
		{
			
			if (posPos[i] != lx || posPos[i+1] != ly)
			{

				retArr[ind] = posPos[i];
				retArr[ind+1] = posPos[i+1];
			
				lx = posPos[i];
				ly = posPos[i+1];
							
				ind += 2;
			}
			
		}
		
		retArr[trueSize] = goalx;
		retArr[trueSize+1] = goaly;
		
		
		return(retArr);
		
		
		/*
		int[] retArr2 = new int[trueSize];
		int in = 0;
		for (int i = 2; i < trueSize+2; i+=2)
		{
			retArr2[in] = retArr[i];
			retArr2[in+1] = retArr[i+1];
			in += 2;
		}
		
		return(retArr2);
		*/
		
	}


	private int getDir(int xx, int yy)
	{
		float angle = (float) Math.toDegrees(Math.atan2(goaly - yy, goalx - xx));
		int res = Math.round(angle /= 45);
		
		res += 16;
		res = res % 8;
		res = 8-res;
		if (res == 8)
			return(0);
		return(res);
		
		
		
		
		
		
		/*
		float angle = (float) Math.toDegrees(Math.atan2(goaly - yy, goalx - xx));
		

		System.out.println("Angle: " + angle);

		
		double resAcc = angle /= 45;

		
		int res = Math.round(angle /= 45);
		System.out.println("iner res: " + res);

		
		res += 16;
		res = res % 8;
		res = 8-res;
		
		
		if (res == 8)
			return(0);

		System.out.println("Res acc: " + resAcc);
		
		if (resAcc > res)
			return(res+0.25);
		else
			return(res-0.25);
		*/
		//return(res);
	}
	
	private double getDir2(int xx, int yy)
	{
		float angle = (float) Math.toDegrees(Math.atan2(goaly - yy, goalx - xx));
		
		double resAcc = angle /= 45;
		
		resAcc += 16;
		resAcc = resAcc % 8;
		resAcc = 8-resAcc;
		//if (res == 8)
			//return(0);
		return(resAcc);
		
		

		/*
		
		

		
		int res = Math.round(angle /= 45);
		System.out.println("iner res: " + res);

		
		res += 16;
		res = res % 8;
		res = 8-res;
		
		
		if (res == 8)
			return(0);

		System.out.println("Res acc: " + resAcc);
		
		if (resAcc > res)
			return(res+0.25);
		else
			return(res-0.25);
		*/
		//return(res);
	}

	


	private boolean tryDir(int xx, int yy, int dir)
	{
		dir = dir % 8;
		
		switch(dir)
		{
			case 0: xx +=1; break;
			case 1: xx +=1; yy -= 1; break;
			case 2: yy -= 1; break;
			case 3: xx -=1; yy -= 1; break;
			case 4: xx -=1; break;
			case 5: xx -=1; yy += 1; break;
			case 6: yy += 1; break;
			case 7: xx +=1; yy += 1; break;
		}
		
		if (grid[xx][yy] == BLOCKED || grid[xx][yy] >= VISITED_OFFS)
		{
			grid[x][y] = (byte) Math.max(BLOCKED+1, grid[x][y]+1);
			return(false);
		}
		else
		{
			x = xx;
			y = yy;
			return(true);
		}
	}
	
	
		
		
		/*
		public int tryStep(int startx, int starty, int depth)
		{
			
			if (grid[startx][starty] == 5)
				return(-1); // try different way

			if (grid[startx][starty] == 6)
				return(0); // try different way
			
					
			
			if (goalx == startx)
				if (goaly == starty)
				{
					path.add(startx);
					path.add(starty);
					return(2);
				}
			
			int res = 0;
			
			if (goalx == startx)
				if (goaly > starty)
					res = (tryStep(startx, starty+1, depth));
				else
					res = (tryStep(startx, starty-1, depth));
			else		
			if (goaly == starty)
				if (goalx > startx)
					res = (tryStep(startx+1, starty, depth));
				else
					res = (tryStep(startx-1, starty, depth));
			else
				if (goalx > startx)
					if (goaly > starty)
						res = (tryStep(startx+1, starty+1, depth));
					else
						res = (tryStep(startx+1, starty-1, depth));
				else
					if (goaly > starty)
						res = (tryStep(startx-1, starty+1, depth));
					else
						res = (tryStep(startx-1, starty-1, depth));
			
			if (res == 2)
			{
				path.add(startx);
				path.add(starty);
				return(2);
			}
			
			if (res == -1)
			{
				
				StepWorker worker = new StepWorker(path, startx, starty+1, goalx, goaly, 0);
				
				
				
				if (tryStep(startx, starty+1, depth) == 2)
					return(2);
				
				if (tryStep(startx, starty-1, depth) == 2)
					return(2);

				if (tryStep(startx+1, starty, depth) == 2)
					return(2);

				if (tryStep(startx-1, starty, depth) == 2)
					return(2);

				if (tryStep(startx+1, starty+1, depth) == 2)
					return(2);

				if (tryStep(startx+1, starty-1, depth) == 2)
					return(2);

				if (tryStep(startx-1, starty+1, depth) == 2)
					return(2);

				if (tryStep(startx-1, starty-1, depth) == 2)
					return(2);
				
				grid[startx][starty] = 6;
			}
			
			return(res);
			
			
			
		}
		
	}
	

	public List<Integer> findPath(int startx, int starty, int goalx, int goaly, boolean lockPath)
	{
		
		List<Integer> path = new ArrayList<>();
		
		StepWorker worker = new StepWorker(path, startx, starty, goalx, goaly, 0);
		
		worker.tryStep(startx, starty, 0);
		
		
		return(path);
	}
	
	*/
	
	
	/*
	public List<Integer> findPath(int startx, int starty, int goalx, int goaly, boolean lockPath)
	{
		Integer[] depth = new Integer[1];
		depth[0] = 0;
		List<Integer> res = tryDirectStep(startx, starty, startx, starty, goalx, goaly, depth);
		
		
		return(res);
	}
	
	
	
	private List<Integer> tryDirectStep(int lastx, int lasty, int startx, int starty, int goalx, int goaly, Integer[] depth)
	{
		return(tryDirectStep(lastx, lasty, startx, starty, goalx, goaly, depth, false));
	}
	
	private List<Integer> tryDirectStep(int lastx, int lasty, int startx, int starty, int goalx, int goaly, Integer[] depth, boolean oneAttempt)
	{
		//int dx = goalx-startx;
		//int dy = goaly-starty;
		
		List<Integer> path = new ArrayList<>();
		
		depth[0] += 1;
		if (depth[0] > 200)
			return(path);
		
		
		if (oneAttempt)
		if (grid[startx][starty] != 5)
			grid[startx][starty] = 6;
		
		
		if (grid[startx][starty] == 5) // the attempted spot is blocked
		{
			if (oneAttempt)
				return(path); // abort
			
			
			if (grid[lastx][lasty] == 6)
				return(path); // abort			
			

			Integer[] ndepth = new Integer[1];
			ndepth[0] = depth[0];
					
			List<Integer> UP = tryDirectStep(lastx, lasty, lastx, lasty+1, goalx, goaly, ndepth, true);
			List<Integer> DOWN = tryDirectStep(lastx, lasty, lastx, lasty-1, goalx, goaly, ndepth, true);
			List<Integer> RIGHT = tryDirectStep(lastx, lasty, lastx+1, lasty, goalx, goaly, ndepth, true);
			List<Integer> LEFT = tryDirectStep(lastx, lasty, lastx-1, lasty, goalx, goaly, ndepth, true);
			List<Integer> RIGHTUP = tryDirectStep(lastx, lasty, lastx+1, lasty+1, goalx, goaly, ndepth, true);
			List<Integer> RIGHTDOWN = tryDirectStep(lastx, lasty, lastx+1, lasty-1, goalx, goaly, ndepth, true);
			List<Integer> LEFTUP = tryDirectStep(lastx, lasty, lastx-1, lasty+1, goalx, goaly, ndepth, true);
			List<Integer> LEFTDOWN = tryDirectStep(lastx, lasty, lastx-1, lasty-1, goalx, goaly, ndepth, true);
			
			List<Integer> minList = null;
			int minLen = Integer.MAX_VALUE;

			int testLen = UP.size();
			if (testLen != 0 && testLen < minLen)
			if ((UP.get(testLen-2) == goalx) && (UP.get(testLen-1) == goaly) )
			{	minList = UP; minLen = testLen; }
			
			testLen = DOWN.size();
			if (testLen != 0 && testLen < minLen)
			if ((DOWN.get(testLen-2) == goalx) && (DOWN.get(testLen-1) == goaly) )
			{	minList = DOWN; minLen = testLen; }

			testLen = RIGHT.size();
			if (testLen != 0 && testLen < minLen)
			if ((RIGHT.get(testLen-2) == goalx) && (RIGHT.get(testLen-1) == goaly) )
			{	minList = RIGHT; minLen = testLen; }

			testLen = LEFT.size();
			if (testLen != 0 && testLen < minLen)
			if ((LEFT.get(testLen-2) == goalx) && (LEFT.get(testLen-1) == goaly) )
			{	minList = LEFT; minLen = testLen; }

			testLen = RIGHTUP.size();
			if (testLen != 0 && testLen < minLen)
			if ((RIGHTUP.get(testLen-2) == goalx) && (RIGHTUP.get(testLen-1) == goaly) )
			{	minList = RIGHTUP; minLen = testLen; }

			testLen = RIGHTDOWN.size();
			if (testLen != 0 && testLen < minLen)
			if ((RIGHTDOWN.get(testLen-2) == goalx) && (RIGHTDOWN.get(testLen-1) == goaly) )
			{	minList = RIGHTDOWN; minLen = testLen; }

			testLen = LEFTUP.size();
			if (testLen != 0 && testLen < minLen)
			if ((LEFTUP.get(testLen-2) == goalx) && (LEFTUP.get(testLen-1) == goaly) )
			{	minList = LEFTUP; minLen = testLen; }

			testLen = LEFTDOWN.size();
			if (testLen != 0 && testLen < minLen)
			if ((LEFTDOWN.get(testLen-2) == goalx) && (LEFTDOWN.get(testLen-1) == goaly) )
			{	minList = LEFTDOWN; minLen = testLen; }

			if (minList == null)
			{
				// No path possible!
				
				return(path);
			}
			else
			{
				System.out.println("Path found!");
			}
			
			path.add(startx);
			path.add(starty);
			
			path.addAll(minList);
					
			return(path);
			
			
		}
		
		
		
		path.add(startx);
		path.add(starty);
		
		if (goalx == startx)
			if (goaly == starty)
				return(path);
		
		if (goalx == startx)
			if (goaly > starty)
				path.addAll(tryDirectStep(startx, starty, startx, starty+1, goalx, goaly, depth));
			else
				path.addAll(tryDirectStep(startx, starty, startx, starty-1, goalx, goaly, depth));
		else		
		if (goaly == starty)
			if (goalx > startx)
				path.addAll(tryDirectStep(startx, starty, startx+1, starty, goalx, goaly, depth));
			else
				path.addAll(tryDirectStep(startx, starty, startx-1, starty, goalx, goaly, depth));
		else
			if (goalx > startx)
				if (goaly > starty)
					path.addAll(tryDirectStep(startx, starty, startx+1, starty+1, goalx, goaly, depth));
				else
					path.addAll(tryDirectStep(startx, starty, startx+1, starty-1, goalx, goaly, depth));
			else
				if (goaly > starty)
					path.addAll(tryDirectStep(startx, starty, startx-1, starty+1, goalx, goaly, depth));
				else
					path.addAll(tryDirectStep(startx, starty, startx-1, starty-1, goalx, goaly, depth));
		
		return(path);
			
	}
	*/
	

}
