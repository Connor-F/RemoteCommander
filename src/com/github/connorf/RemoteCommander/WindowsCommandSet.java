package com.github.connorf.RemoteCommander;

import static com.github.connorf.RemoteCommander.CommandConstants.DIR_LEFT;
import static com.github.connorf.RemoteCommander.CommandConstants.DIR_RIGHT;
import static com.github.connorf.RemoteCommander.CommandConstants.DIR_INVERTED;
import static com.github.connorf.RemoteCommander.CommandConstants.DIR_NORMAL;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by connor on 06/09/15.
 */
public class WindowsCommandSet extends CommandSet
{
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

    @Override
    public void setWallpaper(File wallpaper) throws IOException
    {
        String changerVbs = "dim shell\ndim user\nSet shell = WScript.CreateObject(\"WScript.Shell\")\nuser = shell.ExpandEnvironmentStrings(\"%USERNAME\")\nSet fso = CreateObject(\"Scripting.FileSystemObject\")\nsysDir = fso.GetSpecialFolder(1)\nwallpaper = ";
        changerVbs += wallpaper.getAbsolutePath();
        changerVbs += "\nshell.RegWrite \"HKCU\\Control Panel\\Desktop\\Wallpaper\", wallpaper\nshell.Run \"%windowsDir%\\System32\\RUNDLL32.EXE user32.dll,UpdatePerUserSystemParameters\", 1, True\n";
        File vbs = File.createTempFile("wal", ".vbs", new File(getTempPath()));
        PrintWriter writer = new PrintWriter(vbs);
        writer.write(changerVbs);
        writer.flush();
        writer.close();
        getRuntime().exec("wscript " + getTempPath() + vbs.getName());
        vbs.deleteOnExit();
    }
}