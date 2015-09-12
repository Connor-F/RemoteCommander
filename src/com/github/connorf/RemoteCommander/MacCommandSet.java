package com.github.connorf.RemoteCommander;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * methods for controlling a mac os machine
 */
public class MacCommandSet extends CommandSet
{
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
     * @throws IOException if something went wrong with exec()
     */
    @Override
    public void setWallpaper(File wallpaper) throws IOException
    {
        getRuntime().exec("osascript -e 'tell application \"Finder\" to set desktop picture to POSIX file \"" + wallpaper.getAbsolutePath() + "\"'");
    }
}
