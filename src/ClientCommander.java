import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Scanner;

/**
 * allows for the control of the clients from the server
 */
public class ClientCommander implements Runnable
{
    /** holds all the clients that are currently connected to the server */
    private Map<InetAddress, ConnectedClient> connectedClients;

    public ClientCommander()
    {
        connectedClients = Server.getOnlineClients();
    }

    public void run()
    {
        try
        {
            waitForCommand();
        }
        catch(UnknownHostException | NullCommandException excp)
        {
            System.out.println("run() clientcommander exception");
            excp.printStackTrace();
        }
    }

    /**
     * waits for the user of the server to type a command
     */
    private void waitForCommand() throws UnknownHostException, NullCommandException
    {
        while(true)
        {
            System.out.print("> ");
            String command = new Scanner(System.in).nextLine();
            parseAndSendCommand(command);
        }
    }

    /**
     * uses the command input by the server user to execute the command on the
     * specified clients
     * @param fullCommand the users command input. Each command should have the format:
     *                      COMMAND HOST. Where COMMAND is the instruction e.g. poweroff and HOST is either "all" or
     *                      the IP address of the target host
     * @throws UnknownHostException thrown if the host provided is incorrect
     */
    private void parseAndSendCommand(String fullCommand) throws UnknownHostException, NullCommandException
    {
        String[] commandTokens = fullCommand.split("\\s+");
        String command = commandTokens[0]; // Format: COMMAND HOST
        String host = commandTokens[1];
        if(command == null || host == null)
            throw new NullCommandException("Command and/or host not provided");

        ConnectedClient target = null;
        if(!host.equals("all")) // find the specified client
            target = connectedClients.get(InetAddress.getByName(host));
        if(target == null && !host.equals("all"))
            throw new UnknownHostException(host + " isn't online or doesn't exist");

        target.sendCommand(command); // send the cmd to the connected client
    }
}
