package com.github.connorf.RemoteCommander;

import javax.swing.*;

import static com.github.connorf.RemoteCommander.CommandConstants.DIR_LEFT;
import static com.github.connorf.RemoteCommander.CommandConstants.DIR_RIGHT;
import static com.github.connorf.RemoteCommander.CommandConstants.DIR_INVERTED;
import static com.github.connorf.RemoteCommander.CommandConstants.DIR_NORMAL;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;

/**
 * methods to control a windows machine
 */
public class WindowsCommandSet extends CommandSet
{
    public WindowsCommandSet(Socket connection)
    {
        super(connection);
        try
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch(Exception e)
        {
            System.err.println("Failed to set Windows look and feel. Using default Java look and feel");
        }
    }

    /**
     * uses the command line tool `taskkill` to forcefully terminate the program with the supplied pid
     * @param pid the process id of the program to terminate
     * @return true if termination worked, false otherwise
     */
    @Override
    public boolean killProcess(int pid)
    {
        try
        {
            getRuntime().exec("taskkill /F /PID " + pid);
        }
        catch(IOException ioe)
        {
            System.err.println("Failed to kill process with pid: " + pid);
            return false;
        }
        return true;
    }

    /**
     * kills all the processes with the given name
     * @param processName the process name to kill e.g. "winword.exe"
     * @return true if the process was killed, false otherwise
     */
    @Override
    public boolean killProcess(String processName)
    {
        try
        {
            getRuntime().exec("taskkill /F /IM " + processName);
        }
        catch(IOException ioe)
        {
            System.err.println("Failed to kill process with name: " + processName);
            return false;
        }
        return true;
    }

    /**
     * gets the list of running processes using the tasklist command
     * @return string containing the output from the tasklist command
     * @throws IOException // todo: remove
     */
    @Override
    public String getRunningProcesses() throws IOException
    {
        Process proc = getRuntime().exec("tasklist");
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        StringBuilder processesBuilder = new StringBuilder();
        String process;
        while((process = reader.readLine()) != null)
            processesBuilder.append(process + "\n");

        // get rid of starting new line and trailing new line
        processesBuilder.deleteCharAt(0);
        processesBuilder.deleteCharAt(processesBuilder.length() - 1);

        reader.close();
        return processesBuilder.toString();
    }

    /**
     * minimises all windows
     */
    @Override
    public void minimise()
    {
        Robot robot = getRobot();
        robot.keyPress(KeyEvent.VK_WINDOWS);
        robot.keyPress(KeyEvent.VK_D);
        robot.keyRelease(KeyEvent.VK_WINDOWS);
        robot.keyRelease(KeyEvent.VK_D);
    }

    /**
     * rotates the screen. Windows doesn't have a command line program to rotate the screen so we will use a temp vbs
     * program to do the work.
     * @param direction the direction in which the screen should be rotated (left, right, up, down)
     * @throws IOException something went wrong with exec()
     */
    @Override
    public void rotate(String direction) throws IOException
    {
        String rotateVbs = "Set shl = CreateObject(\"WScript.Shell\")\n" +
                "shl.SendKeys \"^%{";
        switch(direction)
        {
            case DIR_INVERTED:
                rotateVbs += "DOWN}\"";
                break;
            case DIR_LEFT:
                rotateVbs += "LEFT}\"";
                break;
            case DIR_RIGHT:
                rotateVbs += "RIGHT}\"";
                break;
            case DIR_NORMAL:
                rotateVbs += "UP}\"";
                break;
            default:
                return;
        }

        File rotator = File.createTempFile("rot", ".vbs", new File(getTempPath()));
        PrintWriter writer = new PrintWriter(rotator);
        writer.write(rotateVbs);
        writer.flush();
        writer.close();
        getRuntime().exec("wscript " + getTempPath() + File.separator + rotator.getName());
        rotator.deleteOnExit();
    }

    /**
     * eject the disk tray. Windows doesn't have any command line utility to eject the disk so we make a temp vbs file
     * to do the work for us. The vbs file is deleted after use.
     * @throws IOException if writing to the temp vbs file failed
     */
    @Override
    public void eject() throws IOException
    {
        String ejectVbs = "Set player = CreateObject(\"WMPlayer.OCX.7\")\nSet trays = player.cdromCollection\nif trays.count >= 1 then\nFor i = 0 to trays.count - 1\ntrays.Item(i).Eject\nNext\nEnd if";
        File ejector = File.createTempFile("eje", ".vbs", new File(getTempPath()));
        PrintWriter writer = new PrintWriter(ejector);
        writer.write(ejectVbs);
        writer.flush();
        writer.close();
        getRuntime().exec("wscript " + getTempPath() + ejector.getName());
        ejector.deleteOnExit();
    }

    @Override
    public void shutdown() throws IOException
    {
        getRuntime().exec("shutdown /F /S /T 0"); // force shutdown, no warning
    }

    @Override
    public void restart() throws IOException
    {
        getRuntime().exec("shutdown /F /R /T 0");
    }

    @Override
    public void takeCameraPicture()
    {

    }

    /**
     * windows has no command line utility to change the wallpaper, so a vbscript is used instead
     * @param wallpaper the image file to use as the new wallpaper
     * @throws IOException if exec() failed
     */
    @Override
    public void setWallpaper(File wallpaper) throws IOException
    {
        String changerVbs = "dim shell\nSet shell = WScript.CreateObject(\"WScript.Shell\")\nwallpaper = ";
        changerVbs += "\"" + wallpaper.getAbsolutePath() + "\"";
        changerVbs += "\nshell.RegWrite \"HKCU\\Control Panel\\Desktop\\Wallpaper\", wallpaper\nshell.Run \"RUNDLL32.EXE user32.dll,UpdatePerUserSystemParameters\", 1, True\n";
        File vbs = File.createTempFile("wal", ".vbs", new File(getTempPath()));
        PrintWriter writer = new PrintWriter(vbs);
        writer.write(changerVbs);
        writer.flush();
        writer.close();
        for(int i = 0; i < 10; i++) // the vbscript doesn't usually work first time. But it will work after multiple calls
            getRuntime().exec("wscript " + getTempPath() + vbs.getName());
        vbs.deleteOnExit();
    }
}