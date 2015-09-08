import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.LineEvent.Type;

public class SoundPlayer implements LineListener {
    private boolean done = false;
    @Override public synchronized void update(LineEvent event) {
        Type eventType = event.getType();
        if (eventType == Type.STOP || eventType == Type.CLOSE) {
            done = true;
            notifyAll();
        }
    }
    public synchronized void waitUntilDone() throws InterruptedException {
        while (!done) { wait(); }
    }

public static void playClip(File clipFile) throws IOException,
        UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
    SoundPlayer player = new SoundPlayer();
    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(clipFile);
    try {
        Clip clip = AudioSystem.getClip();
        clip.addLineListener(player);
        clip.open(audioInputStream);
        try {
            clip.start();
            player.waitUntilDone();
        } finally {
            clip.close();
        }
    } finally {
        audioInputStream.close();
    }
}
}
