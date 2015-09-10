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
     */
    public void playSound(File soundFile)
    {
        int BUFFER_SIZE = (int)soundFile.length();
        AudioInputStream audioStream = null;
        AudioFormat audioFormat;
        SourceDataLine sourceLine = null;

        try
        {
            audioStream = AudioSystem.getAudioInputStream(soundFile);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1); // todo: don't exit
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
            System.exit(1);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1);
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
            }
            if(nBytesRead >= 0)
            {
                @SuppressWarnings("unused")
                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
            }
        }

        sourceLine.drain();
        sourceLine.close();
    }
}