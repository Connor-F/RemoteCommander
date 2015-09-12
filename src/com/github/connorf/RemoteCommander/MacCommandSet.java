package com.github.connorf.RemoteCommander;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by connor on 06/09/15.
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

    @Override
    public void setWallpaper(File wallpaper) throws IOException
    {

    }
}
