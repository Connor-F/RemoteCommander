package com.github.connorf.RemoteCommander;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 * allows the playing of sound files
 */
public class MakeSound implements Runnable
{
    private File sound;

    public MakeSound(File sound)
    {
        this.sound = sound;
    }

    @Override
    public void run()
    {
        playSound(sound);
    }

    /**
     * plays the sound file provided
     * @param soundFile the sound to play
     * @return true if everything worked, false if otherwise
     */
    public boolean playSound(File soundFile)
    {
        int BUFFER_SIZE = (int)soundFile.length();
        AudioInputStream audioStream = null;
        AudioFormat audioFormat;
        SourceDataLine sourceLine = null;
        soundFile.deleteOnExit(); // putting this here means if an exception occurs and we exit gracefully, the file will be deleted

        try
        {
            audioStream = AudioSystem.getAudioInputStream(soundFile);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }

        audioFormat = audioStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try
        {
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
        }
        catch(LineUnavailableException e)
        {
            e.printStackTrace();
            return false;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }

        // try to unmute systems mixer if its muted
        // doesn't always work (due to the source line itself) but worth keeping
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixInfo : infos)
        {
            Mixer mixer = AudioSystem.getMixer(mixInfo);
        }

        BooleanControl bc = (BooleanControl) sourceLine.getControl(BooleanControl.Type.MUTE);
        if (bc != null)
            bc.setValue(false); // true to mute the line, false to unmute

        sourceLine.start();

        int nBytesRead = 0;
        byte[] abData = new byte[BUFFER_SIZE];
        while(nBytesRead != -1)
        {
            try
            {
                nBytesRead = audioStream.read(abData, 0, abData.length);
            }
            catch(IOException e)
            {
                e.printStackTrace();
                return false;
            }
            if(nBytesRead >= 0)
            {
                @SuppressWarnings("unused")
                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
            }
        }

        sourceLine.drain();
        sourceLine.close();
        soundFile.delete();
        return true;
    }
}