package otherHelpers;

import java.util.ArrayList;
import java.util.List;

public class BinaryPathfinding2 {
	
	byte[][] parGrid;
	byte[][] grid;
	boolean preferNonVertical;
	
	int x, y;
	
	int goalx;
	int goaly;
	
	byte BLOCKED = 5;
	byte VISITED_OFFS = 6;
	
	
	public BinaryPathfinding2(byte[][] grid, boolean preferNonVertical)
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
		
		
		int[] posPos = new int[200*2];
		int posInd = 0;
		

		int length = this.parGrid.length;
	    grid = new byte[length][this.parGrid[0].length];
	    for (int i = 0; i < length; i++) {
	        System.arraycopy(this.parGrid[i], 0, grid[i], 0, this.parGrid[i].length);
	    }
		
		
		System.out.println("STARTED");
		
		while(x != goalx || y != goaly)
		{
			x = Math.max(1, x);
			y = Math.max(1, y);
			
			if (posInd >= 390)
			{
				System.out.println("Pos too high!");
				return(posPos);
			}
			
			//System.out.println(grid[x][y]);
			//System.out.println("Pos ind: " + posInd);
			
			
			if (grid[x][y] == BLOCKED)// || grid[x][y] >= VISITED_OFFS)
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
			
			
			int dir = getDir(x,y)+8; // get the optimal direction
			
			//System.out.println("DIR: " + dir);
			
			if (tryDir(x,y, dir))
				continue;
			
			if (tryDir(x,y, dir+1))
				continue;
			if (tryDir(x,y, dir-1))
				continue;
			if (tryDir(x,y, dir+2))
				continue;
			if (tryDir(x,y, dir-2))
				continue;
			if (tryDir(x,y, dir+3))
				continue;
			if (tryDir(x,y, dir-3))
				continue;
			if (tryDir(x,y, dir-4))
				continue;

			
			x = posPos[posInd-2];
			y = posPos[posInd-2];
		
		}
		System.out.println("ENDED");

		return(posPos);
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
