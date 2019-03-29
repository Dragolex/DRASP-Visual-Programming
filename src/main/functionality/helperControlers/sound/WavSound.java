package main.functionality.helperControlers.sound;

import execution.Execution;
import externalLibs.tinysound.Music;
import externalLibs.tinysound.Sound;
import externalLibs.tinysound.TinySound;

public class WavSound extends PlayableSound
{
	static boolean initialized = false;
	
	Music wavMusic;	
	Sound wavSound;
	
	boolean paused = false;
	
	double volume = 1;
	
	public WavSound(String path, boolean multiplayable)
	{
		super();
		
		if (!initialized)
		{
			TinySound.init();
			initialized = true;
		}
		
		if (multiplayable)
			wavSound = TinySound.loadSound(path, true);
		else
			wavMusic = TinySound.loadMusic(path, true);
	}
	
	@Override
	public void start(boolean loop)
	{
		if (wavSound != null)
			wavSound.play(volume);
		else
		if (paused)
			wavMusic.resume();
		else
			wavMusic.play(loop, volume);
		
		paused = false;
	}

	@Override
	public void stop(boolean pause)
	{
		if (wavSound != null)
			Execution.setError("You cannot stop a multi-instance-sound!", false);
		else
		if (pause)
		{
			wavMusic.pause();
			paused = true;
		}
		else
		{
			wavMusic.stop();
			paused = false;
		}
	}

	@Override
	public void setPosition(double seconds) {
		wavMusic.setPosition((long) seconds);
		
	}
	
	@Override
	public double getPosition() {
		return(wavMusic.getPosition());
	}

	@Override
	public void setVolume(double volume) {
		if (wavSound != null)
			Execution.setError("You cannot change volume of a multi-instance-sound!", false);
		else
		{
			this.volume = volume;
			wavMusic.setVolume(volume);
		}
	}
	
	/*
	@Override
	public void getLength() {
		// TODO Auto-generated method stub
		wavMusic.setLoopPositionBySeconds(seconds);
	}*/



}
