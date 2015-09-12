package com.github.connorf.RemoteCommander;

import java.io.File;
import java.io.IOException;

/**
 * methods for controlling a mac os machine
 */
public class MacCommandSet extends CommandSet
{
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
