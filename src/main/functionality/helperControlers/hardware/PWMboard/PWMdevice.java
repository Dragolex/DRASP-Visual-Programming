package main.functionality.helperControlers.hardware.PWMboard;

import java.io.IOException;


public interface PWMdevice {

	/*
	public abstract PWMdevice(int bus, int address) throws IOException, UnsupportedBusNumberException;

	public abstract PWMdevice();*/
	
	public abstract void setAllPWM(int on, int off) throws IOException;

	
	public abstract void setChannelPWM(int channelIndex, int on, int off) throws IOException;
	
}
