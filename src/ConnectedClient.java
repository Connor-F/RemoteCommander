import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * represents a client that has connected to our server
 */
public class ConnectedClient
{
    /** the connection between the client and server */
    private Socket connection;
    /** the address of the client */
    private InetAddress address;
    /** output to the client */
    private PrintWriter outToClient;

    public ConnectedClient(Socket connection, InetAddress address) throws IOException
    {
        this.connection = connection;
        this.address = address;
        outToClient = new PrintWriter(connection.getOutputStream());
    }

    public void sendCommand(String command)
    {
        outToClient.println(command);
    }

    public Socket getConnection()
    {
        return connection;
    }

    public InetAddress getAddress()
    {
        return address;
    }
}
