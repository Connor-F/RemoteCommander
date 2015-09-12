package com.github.connorf.RemoteCommander;

import static com.github.connorf.RemoteCommander.CommandConstants.MSG_TYPE_ERROR;
import static com.github.connorf.RemoteCommander.CommandConstants.MSG_TYPE_INFO;
import static com.github.connorf.RemoteCommander.CommandConstants.MSG_TYPE_QUESTION;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * contains the definitions and some implementations (if they are not OS specific) of the features of the program
 */
public abstract class CommandSet implements ClipboardOwner
{
    /** the runtime environment for the host OS, used for OS specific commands */
    private Runtime runtime = Runtime.getRuntime();
    /** path to the OS specific temp directory (where we store transferred files etc.) */
    private String tempPath;

    public abstract void eject() throws IOException;
    public abstract void shutdown() throws IOException;
    public abstract void restart() throws IOException;
    public abstract void rotate(String direction) throws IOException;
    public abstract void takeCameraPicture();
    public abstract void setWallpaper(Image newWallpaper);

    public CommandSet()
    {
        createStorageDir();
    }

    /**
     * creates a temp directory to store transferred files (from the server) on the clients machine
     */
    private void createStorageDir()
    {
        tempPath = System.getProperty("java.io.tmpdir") + File.separator + "rc";
        new File(tempPath).mkdir();
    }

    public void retrieve()
    {
    }

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
        if(type.equals(MSG_TYPE_INFO))
            option = JOptionPane.INFORMATION_MESSAGE;
        else if(type.equals(MSG_TYPE_ERROR))
            option = JOptionPane.ERROR_MESSAGE;
        else if(type.equals(MSG_TYPE_QUESTION))
            option = JOptionPane.QUESTION_MESSAGE;
        JOptionPane.showMessageDialog(null, message, title, option);
    }

    /**
     * takes a screenshot of the clients screen and saves it in our temp directory
     * @throws AWTException if the robot failed to be created
     * @throws IOException if something went wrong created the image file from the bufferedimage
     */
    public void takeScreenshot() throws AWTException, IOException
    {
        Robot robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage screenImg = robot.createScreenCapture(new Rectangle(screenSize));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd@HH-mm-ss");
        File screenshot = new File(getTempPath() + "/scr_" + dateFormat.format(new Date()) + ".jpg");
        ImageIO.write(screenImg, "jpg", screenshot);
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

    public String getTempPath()
    {
        return tempPath;
    }

}
