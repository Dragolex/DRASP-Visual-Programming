package main.functionality.helperControlers.hardware.miniDisplay;

public interface MiniDisplay {
	

	abstract public void drawLine(int x1, int y1, int x2, int y2);

	abstract public void drawArc(int x, int y, int r, int startAng, int endAng);

	abstract public void drawRectangle(int x, int y, int width, int height, boolean filled);

	abstract public void drawCircle(int x, int y, int r, boolean filled);

	abstract public void setCharacterSet(int index);
	abstract public void drawText(int x, int y, String txt);
	
	
	abstract public String toString();

}