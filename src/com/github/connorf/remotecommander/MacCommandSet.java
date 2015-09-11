package com.github.connorf.remotecommander;

import java.awt.*;
import java.io.IOException;

/**
 * Created by connor on 06/09/15.
 */
public class MacCommandSet extends CommandSet
{
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
    public void setWallpaper(Image newWallpaper)
    {

    }
}
