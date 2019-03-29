package functionality.actions;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.TextOrValueOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.helperControlers.sound.Mp3Sound;
import main.functionality.helperControlers.sound.PlayableSound;
import main.functionality.helperControlers.sound.WavSound;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.FileHelpers;
import staticHelpers.OtherHelpers;

public class Act14_Sound extends Functionality {

	public static int POSITION = 14;
	public static String NAME = "Sound";
	public static String IDENTIFIER = "ActSoundNode";
	public static String DESCRIPTION = "Functionalities involving playing sounds.";
	
	

	public static ProgramElement create_ElLoadSound()
	{
		Object[] params = new Object[3];
		return(new FunctionalityContent( "ElLoadSound",
				params,
				() -> {
						String path = FileHelpers.resolveUniversalFilePath((String) params[1]);
						
						if (path.endsWith(".wav"))
							initVariableAndSet(params[0], Variable.soundType, new WavSound(path, (boolean) params[2]));
						else
							initVariableAndSet(params[0], Variable.soundType, new Mp3Sound(path));
						
					}));
	}
	public static ProgramElement visualize_ElLoadSound(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Load Sound", "Loads a sound file to play with 'Play Sound'.");
		vis.addParameter(0, new VariableOnly(true, true), "Sound Identifier", "Identifier for this sound to use in 'Play Sound'.");
		vis.addParameter(1, new TextOrValueOrVariable(), "Sound File", "Absolute or relative path to a sound file (wav and mp3 are supported).\nTip: You can use a res directory to\nautomatically deploy it onto the target device!");
		vis.addParameter(2, new BooleanOrVariable("False"), "Parallel Playable", "If true, the file is a paralell playable one.\nThat means you can play it again and again while still running.\nThe downside is that you cannot use any of the other elements to alter this sound at runtime!");

		return(vis);
	}
	
	public static ProgramElement create_ElPlaySound()
	{
		Object[] params = new Object[2];
		return(new FunctionalityContent( "ElPlaySound",
				params,
				() -> {
						PlayableSound sound = (PlayableSound) params[0];
						sound.start((boolean) params[1]);
					}));
	}
	public static ProgramElement visualize_ElPlaySound(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Start Sound", "Start an alread loaded sound.");
		vis.addParameter(0, new VariableOnly(true, true), "Sound Identifier", "Identifier for this sound. Created by 'Play Sound'");
		vis.addParameter(1, new BooleanOrVariable("False"), "Loop", "If true, the sound will restart after the end.");
		return(vis);
	}
	
	public static ProgramElement create_ElSetVolume()
	{
		Object[] params = new Object[2];
		return(new FunctionalityContent( "ElSetVolume",
				params,
				() -> {
						PlayableSound sound = (PlayableSound) params[0];
						sound.setVolume((double) params[1]);
					}));
	}
	public static ProgramElement visualize_ElSetVolume(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Set Volume", "Set the volume of a sound\nNote that you can set this after or also before having started.");
		vis.addParameter(0, new VariableOnly(true, true), "Sound Identifier", "Identifier for this sound. Created by 'Play Sound'");
		vis.addParameter(1, new ValueOrVariable(), "Volume", "Volume value. TODO: Range");

		return(vis);
	}

	public static ProgramElement create_ElSetPosition()
	{
		Object[] params = new Object[2];
		return(new FunctionalityContent( "ElSetPosition",
				params,
				() -> {
						PlayableSound sound = (PlayableSound) params[0];
						sound.setPosition((double) params[1]);
					}));
	}
	public static ProgramElement visualize_ElSetPosition(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Set Position", "Set the position of a sound.");
		vis.addParameter(0, new VariableOnly(true, true), "Sound Identifier", "Identifier for this sound. Created by 'Play Sound'");
		vis.addParameter(1, new ValueOrVariable(), "Position (s)", "The position in seconds.");

		return(vis);
	}
	
	public static ProgramElement create_ElStopSound()
	{
		Object[] params = new Object[2];
		return(new FunctionalityContent( "ElStopSound",
				params,
				() -> {
						PlayableSound sound = (PlayableSound) params[0];
						sound.stop((boolean) params[1]);
					}));
	}
	public static ProgramElement visualize_ElStopSound(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Stop Sound", "Stop a sound that is currently playing.");
		vis.addParameter(0, new VariableOnly(true, true), "Sound Identifier", "Identifier for this sound. Created by 'Play Sound'");
		vis.addParameter(1, new BooleanOrVariable(), "Keep Position", "If true, the position of the sound will be held\nand when starting the sound again, it will continue fromt here.\nThis is practically a pause function.");

		return(vis);
	}
	
	
	
	
	
	public static ProgramElement create_ElStartRecording()
	{
		Object[] params = new Object[1];
		return(new FunctionalityContent( "ElStartRecording",
				params,
				() -> {
		            if (recorderLine[0] != null)
		            {
		            	recorderLine[0].stop();
		            	recorderLine[0].close();
		            	recorderLine[0] = null;
		            }
				},
				() -> {
					String wavFile = FileHelpers.resolveUniversalFilePath((String) params[1]);
					if (!wavFile.toLowerCase().endsWith(".wav"))
						wavFile += ".wav";
						
				    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;				 
				    
			        try {
			        	
				        //AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                        //								       channels, signed, bigEndian);
			        	AudioFormat format = new AudioFormat(16000, 8, 2, true, true);
			            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format );
			            
			        	if (!AudioSystem.isLineSupported(info)) {
			                System.out.println("Line not supported");
			                return;
			            }
			            
			            
			            if (recorderLine[0] != null)
			            {
			            	recorderLine[0].stop();
			            	recorderLine[0].close();
			            	recorderLine[0] = null;
			            }
			            
			            recorderLine[0] = (TargetDataLine) AudioSystem.getLine(info);
			            recorderLine[0].open(format);
			            recorderLine[0].start();   // start capturing
			            
			            AudioInputStream ais = new AudioInputStream(recorderLine[0]);
			            
			            File file = new File(wavFile);
			            
			            new Thread(() -> {
							try {
								AudioSystem.write(ais, fileType, file);
							} catch (IOException er)
							{
								Execution.setError("Problem at saving sound record to file. Error: " + er.getMessage(), false);
							}
						}).start();
			            
			            // Stop if the program is stopped
			            new Thread(() -> {
			            	while(Execution.isRunning())
			            		OtherHelpers.sleepNonException(200);
			            	
				            if (recorderLine[0] != null)
				            {
				            	recorderLine[0].stop();
				            	recorderLine[0].close();
				            	recorderLine[0] = null;
				            }
			            }).start();
			 
			        } catch (LineUnavailableException er) {
			        	Execution.setError("Problem at starting sound recording. Error: " + er.getMessage(), false);
			        }
			        
					}));
	}
	public static ProgramElement visualize_ElStartRecording(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Start Recording", "Start recording the sound input to a file.");
		vis.addParameter(0, new TextOrValueOrVariable(), "Sound File", "Absolute or relative path to a .wav");

		return(vis);
	}
	
	public static ProgramElement create_ElStopRecording()
	{
		Object[] params = new Object[0];
		return(new FunctionalityContent( "ElStopRecording",
				params,
				() -> {
					if (recorderLine[0] != null)
					{
					    recorderLine[0].stop();
			            recorderLine[0].close();
			            recorderLine[0] = null;
					}
					}));
	}
	public static ProgramElement visualize_ElStopRecording(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Stop Recording", "Stop recording of sound input to a file.");
		return(vis);
	}
	
	
	
	public static ProgramElement create_ElGetInputLoudness()
	{
		Object[] params = new Object[2];
		return(new FunctionalityContent( "ElGetInputLoudness",
				params,
				() -> {
					if (recorderLine[0] != null)
					{
					    recorderLine[0].stop();
			            recorderLine[0].close();
			            recorderLine[0] = null;
					}
					}));
	}
	public static ProgramElement visualize_ElGetInputLoudness(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Input Loudness", "Provides the peak of loudness within a given time-period provided by an attached microphone.");
		vis.addParameter(0, new ValueOrVariable("100"), "Period", "Period of milliseconds to search for peaks.");
		vis.addParameter(1, new VariableOnly(), "Output", "Value between 0 (silent) and 1 (loud).");
		return(vis);
	}
	
	
	
}
