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

    public Client() throws IOException
    {
        connectAndListen();
    }

    /**
     * connects to the command server and waits for commands
     * @throws IOException if something went wrong connecting to the command server
     */
    private void connectAndListen() throws IOException
    {
        Socket socket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        while(socket.isConnected())
        {
            String serverCommand = inFromServer.readLine();
        }
    }
}
