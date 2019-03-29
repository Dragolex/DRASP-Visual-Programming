package main.functionality.helperControlers.network;

import java.io.IOException;

public interface ConnectionInterface {
	
	abstract public void out(String data, String toDeviceIdentifier) throws IOException;
	
	abstract public void outPure(String data) throws IOException;
	
	abstract public void fileOut(String fileName) throws IOException;
	
	abstract public TransmissionMessage in(boolean verifySource) throws IOException;
	
	abstract public void fileIn() throws IOException;
	
	abstract public void close() throws IOException;
	
	abstract public boolean isClosedOrInterrupted();	
}
