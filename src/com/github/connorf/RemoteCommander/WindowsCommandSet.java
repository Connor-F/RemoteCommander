package com.github.connorf.RemoteCommander;

import javax.swing.*;

import static com.github.connorf.RemoteCommander.CommandConstants.DIR_LEFT;
import static com.github.connorf.RemoteCommander.CommandConstants.DIR_RIGHT;
import static com.github.connorf.RemoteCommander.CommandConstants.DIR_INVERTED;
import static com.github.connorf.RemoteCommander.CommandConstants.DIR_NORMAL;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.IntUnaryOperator;

/**
 * methods to control a windows machine
 */
public class WindowsCommandSet extends CommandSet
{
    public WindowsCommandSet()
    {
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
        getRuntime().exec("shutdown /f /s /t 0"); // force shutdown, no warning
    }

    @Override
    public void restart() throws IOException
    {
        getRuntime().exec("shutdown /f /r /t 0");
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
        for(int i = 0; i < 15; i++) // the vbscript doesn't usually work first time. But it will work after multiple calls
        {
            getRuntime().exec("wscript " + getTempPath() + vbs.getName());
            try
            {
                Thread.sleep(100);
            }
            catch(InterruptedException ie)
            {
                System.err.println("Thread.sleep() failed in setWallpaper");
            }
        }
        vbs.deleteOnExit();
    }
}