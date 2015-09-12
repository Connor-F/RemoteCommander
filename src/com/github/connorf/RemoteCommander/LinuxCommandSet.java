package com.github.connorf.RemoteCommander;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * methods to control a linux machine. xrandr and gnome required
 * for all the methods to work
 */
public class LinuxCommandSet extends CommandSet
{
    public LinuxCommandSet()
    {
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