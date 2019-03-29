package main.functionality.helperControlers.sound;

import java.io.File;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class Mp3Sound extends PlayableSound
{
	boolean paused = false;
	double volume = 1;
	
	MediaPlayer mp3Sound;
	
	public Mp3Sound(String path)
	{
		super();
		
		Media hit = new Media(new File(path).toURI().toString());
		mp3Sound = new MediaPlayer(hit);
	}

	@Override
	public void start(boolean loop) {
		mp3Sound.play();
		mp3Sound.setAutoPlay(loop);
		mp3Sound.setVolume(volume);
	}

	@Override
	public void stop(boolean paused) {
		if (paused)
		{
			mp3Sound.pause();
			paused = true;
		}
		else
		{
			mp3Sound.stop();		
			paused = false;
		}
	}

	@Override
	public void setPosition(double seconds)
	{
		mp3Sound.seek(new Duration(seconds*1000));
	}
	
	@Override
	public void setVolume(double volume) {
		mp3Sound.setVolume(volume);
	}

	/*
	@Override
	public void getLength() {
		// TODO Auto-generated method stub
		
	}*/

	@Override
	public double getPosition() {
		return(0); // TODO
	}

}
