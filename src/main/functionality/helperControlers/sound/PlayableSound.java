package main.functionality.helperControlers.sound;

public abstract class PlayableSound {
	
	abstract public void start(boolean loop);
	abstract public void stop(boolean pause);
	abstract public void setVolume(double volume);
	abstract public void setPosition(double seconds);
	abstract public double getPosition();

	//abstract public void getLength();
	
}
