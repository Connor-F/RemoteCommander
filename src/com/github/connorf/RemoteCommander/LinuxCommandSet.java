package com.github.connorf.RemoteCommander;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

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
     * @throws IOException if exec() failed
     */
    @Override
    public void rotate(String direction) throws IOException
    {
        getRuntime().exec("xrandr --output default --rotate " + direction);
    }

    @Override
    public void eject() throws IOException
    {
        getRuntime().exec("eject");
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

    @Override
    public void takeCameraPicture()
    {

    }

    /**
     * tries to set the wallpaper. Only supports gnome systems
     * @param wallpaper the image file we want to use as the new wallpaper
     * @throws IOException if exec() failed
     */
    @Override
    public void setWallpaper(File wallpaper) throws IOException
    {
        getRuntime().exec("gsettings set org.gnome.desktop.background picture-uri file:///" + wallpaper.getAbsolutePath());
    }
}