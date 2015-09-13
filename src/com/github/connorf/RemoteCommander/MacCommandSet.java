package com.github.connorf.RemoteCommander;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * methods for controlling a mac os machine
 * Unfortuantly all the Mac commands have not been tested
 */
public class MacCommandSet extends CommandSet
{
    public MacCommandSet(Socket connection)
    {
        super(connection);
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

    @Override
    public boolean killProcess(int pid)
    {
        return false;
    }

    @Override
    public boolean killProcess(String processName)
    {
        return false;
    }

    /**
     * minimises all open windows: COMMAND+ALT+H+M
     */
    @Override
    public void minimise()
    {
        Robot robot = getRobot();
        robot.keyPress(KeyEvent.VK_META);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_H);
        robot.keyPress(KeyEvent.VK_M);
        robot.keyRelease(KeyEvent.VK_META);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_H);
        robot.keyRelease(KeyEvent.VK_M);
    }

    @Override
    public void rotate(String direction)
    {

    }

    @Override
    public void eject() throws IOException
    {
        getRuntime().exec("drutil eject internal");
    }

    @Override
    public void shutdown() throws IOException
    {
        getRuntime().exec("osascript -e 'tell app \"System Events\" to shut down'");
    }

    @Override
    public void restart() throws IOException
    {
        getRuntime().exec("osascript -e 'tell app \"System Events\" to restart'");
    }

    @Override
    public void takeCameraPicture()
    {

    }

    /**
     * sets the wallpaper on the system
     * @param wallpaper the file that will be used as the wallpaper
     */
    @Override
    public boolean setWallpaper(File wallpaper)
    {
        try
        {
            getRuntime().exec("osascript -e 'tell application \"Finder\" to set desktop picture to POSIX file \"" + wallpaper.getAbsolutePath() + "\"'");
        }
        catch(IOException ioe)
        {
            System.err.println("Failed to set wallpaper");
            ioe.printStackTrace();
        }
        return true; // todo: find what the cmd failure msg is
    }
}
