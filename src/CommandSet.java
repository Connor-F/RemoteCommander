import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/**
 * contains the definitions and some implementations (if they are not OS specific) of the features of the program
 */
public abstract class CommandSet
{
    /** the runtime environment for the host OS, used for OS specific commands */
    private Runtime runtime = Runtime.getRuntime();

    public abstract void eject() throws IOException;
    public abstract void playSound();
    public abstract void shutdown();
    public abstract void restart();
    public abstract void takeCameraPicture();
    public abstract void setWallpaper(Image newWallpaper);

    /**
     * shows a message box on the clients screen
     * @param message the msg to be displayed in the msg box
     */
    public void showMessage(String message)
    {
        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * takes a screenshot of the clients monitor
     * @return a BufferedImage that is the screenshot
     * @throws AWTException if something went wrong when creating the robot
     */
    public BufferedImage takeScreenshot() throws AWTException
    {
        Robot robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return robot.createScreenCapture(new Rectangle(screenSize));
    }

    public Runtime getRuntime()
    {
        return runtime;
    }

}
