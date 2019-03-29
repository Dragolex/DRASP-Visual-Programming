package main.electronic;

import java.util.ArrayList;
import java.util.List;

public class PinRow {

	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	
	
	private double distance;
	private int orientation;
	
	List<Pin> pins = new ArrayList<>();
	int escapeDirection, textPlacement;
	
	public PinRow(double distance, int type, int escapeDirection, int textPlacement)
	{
		this.distance = distance;
		this.orientation = type;
		this.escapeDirection = escapeDirection;
		this.textPlacement = textPlacement;
	}
	
	public Pin add(int pinType, String name)
	{
		return(add(pinType, name, null));
	}
	
	public Pin add(int pinType, String name, Double value)
	{
		Pin pin = new Pin(0, 0, pinType, name, value); // actual position instead of 0,0 will be set later when placed on a board using getPlacedPins()
		pins.add(pin);
		return(pin);
	}
	
	
	public List<Pin> getPlacedPins(double offsx, double offsy)
	{
		for (Pin pin: pins)
		{
			pin.setPosition(offsx, offsy, escapeDirection, textPlacement);
			if (orientation == HORIZONTAL)
				offsx += distance;
			if (orientation == VERTICAL)
				offsy += distance;
		}
		return(pins);
	}

}
