package dataTypes.minor;

import javafx.scene.input.MouseEvent;
import main.electronic.Electronics;
import main.electronic.Pin;
import productionGUI.sections.electronic.WirePoint;
import staticHelpers.OtherHelpers;

public class GridLoc {
	
	int x, y;
		
	static public GridLoc fromMouse(MouseEvent e)
	{
		GridLoc loc = new GridLoc();
		loc.x = gridInd(e.getX());
		loc.y = gridInd(e.getY());
		loc.lim();
		return(loc);
	}
	
	static public GridLoc fromInd(int x, int y)
	{
		GridLoc loc = new GridLoc();
		loc.x = x;
		loc.y = y;
		loc.lim();
		return(loc);
	}
	
	static public GridLoc fromScaled(double x, double y)
	{
		GridLoc loc = new GridLoc();
		loc.x = gridInd(x);
		loc.y = gridInd(y);
		loc.lim();
		return(loc);
	}


	public int getIndX()
	{
		return(x);
	}
	public int getIndY()
	{
		return(y);
	}

	public double getScaledX()
	{
		return(x*(Electronics.gridScale*Electronics.SCALE));
	}
	public double getScaledY()
	{
		return(y*(Electronics.gridScale*Electronics.SCALE));
	}
	
	public double getScaledMiddleX(double fc)
	{
		return((x + 0.5*fc)*(Electronics.gridScale*Electronics.SCALE));
	}
	public double getScaledMiddleY(double fc)
	{
		return((y + 0.5*fc)*(Electronics.gridScale*Electronics.SCALE));
	}
	
	
	static private int gridInd(double val)
	{
		return (int) (Math.round(val / (Electronics.gridScale*Electronics.SCALE)));
	}

	public GridLoc shiftOnGrid(int indX, int indY)
	{
		x = Math.max(0, x+indX); // TODO: add upper limit!
		y = Math.max(0, y+indY); // TODO: add upper limit!
		lim();
		return(this);
	}

	public GridLoc copy()
	{
		GridLoc loc = new GridLoc();
		loc.x = x;
		loc.y = y;	
		loc.lim();
		return(loc);
	}

	public void setFromScaled(double x, double y)
	{
		this.x = gridInd(x);
		this.y = gridInd(y);
		lim();
	}

	public void setFromInd(int x, int y)
	{
		this.x = x;
		this.y = y;
		lim();
	}
	
	private void lim()
	{
		x = Math.max(0, x);
		y = Math.max(0, y);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return(false);
		
	    GridLoc loc = (GridLoc) other;
	    if (loc.x != x)
	    	return(false);
	    if (loc.y != y)
	    	return(false);
	    
	    return(true);
	}

	public boolean isSmallerThan(GridLoc con)
	{
		if (x+y < con.x+con.y)
			return(true);
		else
			if (x+y > con.x+con.y)
				return(false);
			else // both are equal
				if (x == con.x)
					return(y < con.y);
				else
					return(x < con.x);
	}
	

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder("Location(");
		b.append(x);
		b.append(',');
		b.append(y);
		b.append(')');
		return(b.toString());
	}
	
	
	public boolean isInsideScaledRect(double xa, double ya, double w, double h)
	{
		double xx = getScaledX();
		double yy = getScaledY();
		
		System.out.println("xa: " + xa + " ya: " + ya + " w: " + w + " h: " + h + " IN: " + xx +" "+yy);
		
		if (xx < xa)
			return(false);
		if (yy < ya)
			return(false);
		if (xx > xa+w)
			return(false);
		if (yy > ya+h)
			return(false);
		
		System.out.println("SELECTED!");
		
		return(true);
	}

}
