package main.electronic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataTypes.MultiMap;
import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import productionGUI.sections.electronic.VisualizedComponent;
import productionGUI.sections.electronic.WirePoint;

public class Pin {
	
	public static final int GND = 0;
	public static final int PWR_IN = 1;
	public static final int PWR_OUT = 2;
	public static final int IO = 3;
	public static final int IN = 4;
	public static final int OUT = 5;
	public static final int PASSIVE = 6;
	
	// Buses
	public static final int I2C_SDA = 100;
	public static final int I2C_SCL = 101;
	public static final int SPI_MISO = 102;
	public static final int SPI_MOSI = 103;
	public static final int SPI_SCLK = 104;
	public static final int SPI_CS = 105;
	public static final int UART_TX = 106;
	public static final int UART_RX = 107;

	// Active components
	public static final int GATE_NEG = 200;
	public static final int GATE_POS = 201;
	public static final int SOURCE_POS = 202;
	public static final int SOURCE_NEG = 203;
	public static final int DRAIN_NEG = 204;
	public static final int DRAIN_POS = 205;
	
	
	public static String GET_TYPE_STR(int type)
	{
		switch(type)
		{
		case 0: return("GND");
		case 1: return("PWR_IN (VCC)");
		case 2: return("PWR_OUT (VCC)");
		case 3: return("IO");
		case 4: return("IN");
		case 5: return("OUT ");
		case 6: return("PASSIVE");
		
		case 100: return("I2C_SDA");
		case 101: return("I2C_SCL");
		case 102: return("SPI_MISO");
		case 103: return("SPI_MOSI");
		case 104: return("SPI_SCLK");
		case 105: return("SPI_CS");
		case 106: return("UART_TX");
		case 107: return("UART_RX");

		case 200: return("GATE_NEG");
		case 201: return("GATE_POS");
		case 202: return("SOURCE_NEG");
		case 203: return("SOURCE_POS");
		case 204: return("DRAIN_NEG");	
		case 205: return("DRAIN_POS");	
		}
		
		return("MISSING TYPE! ID: " + type);
	}
	
	
	private static MultiMap<Integer> reccomended_connections = new MultiMap<>();
	
	public static void init()
	{
		// corresponding connections for transistors and basic power/GND
		reccomended_connections.addBidirectional(GND, GND, DRAIN_NEG, GATE_NEG);
		reccomended_connections.addBidirectional(PWR_OUT, PWR_IN, DRAIN_POS, GATE_POS);

		// i2c and the SPI clock, CS are connectible with themselves
		reccomended_connections.addBidirectional(I2C_SDA, I2C_SDA);
		reccomended_connections.addBidirectional(I2C_SCL, I2C_SCL);
		reccomended_connections.addBidirectional(SPI_SCLK, SPI_SCLK);
		reccomended_connections.addBidirectional(SPI_CS, SPI_CS);
		
		// Other SPI wires are cross-connectable just like UART TX and RX
		reccomended_connections.addBidirectional(SPI_MISO, SPI_MOSI);
		reccomended_connections.addBidirectional(UART_TX, UART_RX);

		reccomended_connections.addBidirectional(IO, IN, OUT, GATE_NEG, GATE_POS);
		reccomended_connections.addBidirectional(OUT, IN, GATE_NEG, GATE_POS);
		
	}
	
	
	public static boolean Connection_Reccomended(Integer typeA, Double valA, Integer typeB, Double valB)
	{
		if (valA != valB)
			return(false);
		
		return(reccomended_connections.contains(typeA, typeB));
	}

	
	
	
	// Escape directions that determine in what direction locations on the grid are freed up so the pin is connectible at all
	public static final int NO_ESCAPE = -1;
	public static final int ESCAPE_RIGHT = 0;
	public static final int ESCAPE_UP = 1;
	public static final int ESCAPE_LEFT = 2;
	public static final int ESCAPE_DOWN = 3;
	
	// Determines where the name text will be displayed
	public static final int TEXT_ABOVE_HORIZ = 0;
	public static final int TEXT_ABOVE_VERTIC = 1;
	public static final int TEXT_LEFT_HORIZ = 2;
	public static final int TEXT_LEFT_VERTIC = 3;
	public static final int TEXT_BELOW_HORIZ = 4;
	public static final int TEXT_BELOW_VERTIC = 5;
	public static final int TEXT_RIGHT_HORIZ = 6;
	public static final int TEXT_RIGHT_VERTIC = 7;
	
	
	
	public double x = 0;
	public double y = 0;
	protected List<Integer> types = new ArrayList<>();
	public List<String> names = new ArrayList<>();
	protected List<Double> values = new ArrayList<>(); // Can contain nulls if not provided!
	private int escapeDirection = -1;
	private int textPlacement = 0;
	private WirePoint associatedWirePoint;
	
	private String nameText = "";
	
	public Pin(double x, double y, int type, String name, Double value)
	{
		this.x = x;
		this.y = y;
		types.add(type);
		names.add(" " + name + " ");
		values.add(value);	
		prepareNameText();
	}

	public Pin(double x, double y, int type, int textPlacement, String name, Double value)
	{
		this.x = x;
		this.y = y;
		this.textPlacement = textPlacement;
		types.add(type);
		names.add(" " + name + " ");
		values.add(value);
		prepareNameText();
	}
	
	public Pin(double x, double y, int type, String name)
	{
		this.x = x;
		this.y = y;
		types.add(type);
		names.add(" " + name + " ");
		values.add(null);
		prepareNameText();
	}
	public Pin(double x, double y, int type, int textPlacement, String name)
	{
		this.x = x;
		this.y = y;
		this.textPlacement = textPlacement;
		types.add(type);
		names.add(" " + name + " ");
		values.add(null);
		prepareNameText();
	}
	
	private void prepareNameText()
	{
		StringBuilder nameStr = new StringBuilder();
		for (int i = 0; i < names.size(); i++)
		{
			if (i != 0)
				nameStr.append(" | ");
			
			Double val = values.get(i);
			if (val == null)
			{
				nameStr.append(names.get(i));
			}
			else
			{
				nameStr.append(names.get(i));
				nameStr.append(" (");
				nameStr.append(val);
				nameStr.append(")");
			}
		}
		
		nameText = nameStr.toString();
	}
	
	/*
	public Pin(double x2, double y2, List<Integer> types2, List<String> names2, List<Double> values2)
	{
		this.x = x2;
		this.y = y2;
		this.types = types2;
		this.names = names2;
		this.values = values2;
	}*/
	
	
	public void setPosition(double x, double y, int escapeDirection, int textPlacement)
	{
		this.x = x;
		this.y = y;
		this.escapeDirection = escapeDirection;
		this.textPlacement = textPlacement;
	}
	
	public void enforceEscapeDirection(int escape_direction)
	{
		this.escapeDirection = escape_direction;
	}
	
	
	// add another type for this same pin
	public Pin add(int additionalType, String additionalName, Double additionalValue)
	{
		types.add(additionalType);
		names.add(additionalName);
		values.add(additionalValue);
		prepareNameText();
		return(this);
	}
	// add another type for this same pin
	public Pin add(int additionalType, String additionalName)
	{
		return(add(additionalType, additionalName, null));
	}
	

	public double getUnscaledXonBoard()
	{
		return(x);
	}
	public double getUnscaledYonBoard()
	{
		return(y);
	}
	
	public int getEscapeDirection(int surrounding_orientation)
	{
		if (escapeDirection == -1)
			return(escapeDirection);
		
		return((escapeDirection-surrounding_orientation + 4) % 4);
	}
	
	public void computeEscapeDir(double component_w, double component_h)
	{
		if (escapeDirection == -1)
		{
			double rightdist = component_w-x;
			double bottomdist = component_h-y;
			
			double mini = Math.min(x, Math.min(y, Math.min(rightdist, bottomdist)));
			if (mini == y)
				escapeDirection = ESCAPE_UP;
			else
			if (mini == bottomdist)
				escapeDirection = ESCAPE_DOWN;
			else
			if (mini == x)
				escapeDirection = ESCAPE_LEFT;
			else
			if (mini == rightdist)
				escapeDirection = ESCAPE_RIGHT;
		}
	}

	public WirePoint getAssociatedWirePoint(VisualizedComponent component)
	{
		if (associatedWirePoint == null)
			associatedWirePoint = new WirePoint(this, component);
		
		return(associatedWirePoint);
	}

	public void associatePin(WirePoint wirePoint)
	{
		associatedWirePoint = wirePoint;
		wirePoint.associatePin(this);
	}
	
	public int getTextPlacement()
	{
		return(textPlacement);
	}


	public boolean textIsVertical(int orientation)
	{
		double res = Math.floor(textPlacement + orientation*2) % 2;
		return(res == 1);		
	}
	
	
	public Pos getTextOrientation(int outerOrientation)
	{
		switch((textPlacement+outerOrientation*2) % 8)
		{
		case TEXT_LEFT_VERTIC:
		case TEXT_ABOVE_HORIZ:
			return(Pos.BOTTOM_CENTER);
			
		case TEXT_ABOVE_VERTIC:
		case TEXT_RIGHT_HORIZ:
			return(Pos.CENTER_LEFT);

		case TEXT_BELOW_VERTIC:
		case TEXT_LEFT_HORIZ:
			return(Pos.CENTER_RIGHT);

		case TEXT_RIGHT_VERTIC:
		case TEXT_BELOW_HORIZ:
			return(Pos.TOP_CENTER);
		}
		
		return(null);
	}
	
	public double getTextOffsetXunscaled(int orientation, double w, double h)
	{
		double hh = h-3*Electronics.SCALE;
		
		switch((textPlacement+orientation*2) % 8)
		{
		case TEXT_ABOVE_HORIZ:
		case TEXT_BELOW_HORIZ:
			return(-w/2);

		case TEXT_ABOVE_VERTIC:
		case TEXT_BELOW_VERTIC:
			return(-w/2); // return(-w/2);
			

		case TEXT_LEFT_HORIZ:
			return(-w -hh/2);

		case TEXT_RIGHT_HORIZ:
			return(hh/2);
			

		case TEXT_LEFT_VERTIC:
			return(-w/2 -hh);

			
		case TEXT_RIGHT_VERTIC:
			return(-w/2 + hh);
		}
		return(0); // Todo: Error
	}
	public double getTextOffsetYunscaled(int orientation, double w, double h)
	{
		double ww = w-3*Electronics.SCALE;
		
		switch((textPlacement+orientation*2) % 8)
		{
		case TEXT_LEFT_VERTIC:
		case TEXT_LEFT_HORIZ:
			return(-h);
			
			
		case TEXT_ABOVE_VERTIC:
			return(-w/2-h);

		case TEXT_BELOW_VERTIC:
			return(w/2-h);
			

			
		case TEXT_ABOVE_HORIZ:
			return(-2*h);

		case TEXT_RIGHT_HORIZ:
		case TEXT_RIGHT_VERTIC:
			return(-h);

		case TEXT_BELOW_HORIZ:
			return(0);
		}
		return(0); // Todo: Error
	}

	
	/*
	public Pin copy()
	{
		return new Pin(x, y, types, names, values);
	}
	*/
	
	@Override 
	public String toString()
	{
		return(nameText);
	}
}
