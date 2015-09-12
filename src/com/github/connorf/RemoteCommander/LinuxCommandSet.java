package com.github.connorf.RemoteCommander;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LinuxCommandSet extends CommandSet
{
    /**
     * rotates the screen. Linux version relies on xrandr being installed on the clients system
     * @param direction the orientation to rotate the screen (up, down, left, right)
     * @throws IOException if exec() failed
     */
    @Override
    public void rotate(String direction) throws IOException
    {
        System.out.println("Rotating: " + direction);
        //getRuntime().exec("xrandr --output default --rotate " + direction);
    }

    @Override
    public void eject() throws IOException
    {
        getRuntime().exec("eject");
        //todo remove
//        File temp = File.createTempFile("eject", ".vbs", new File(getTempPath()));
//        String writeMe = "i am the string";
//        PrintWriter writer = new PrintWriter(temp);
//        writer.println(writeMe);
//        writer.flush();
//        writer.close();
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