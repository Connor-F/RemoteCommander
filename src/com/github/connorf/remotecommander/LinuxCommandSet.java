package com.github.connorf.remotecommander;

import java.awt.*;
import java.io.IOException;

public class LinuxCommandSet extends CommandSet
{
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

    @Override
    public void setWallpaper(Image newWallpaper)
    {

    }
}