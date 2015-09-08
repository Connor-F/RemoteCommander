import java.io.*;
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

    /**
     * sends the command to the client
     * @param command the string to send to the client so it can then act upon it
     */
    public void sendCommand(String command)
    {
        try
        {
            outToClient.println(command);
            outToClient.flush();
        }
        catch(Exception ioe)
        {
            System.err.println("Failed to create PrintWriter to the client.");
            ioe.printStackTrace();
        }
    }

    public void sendFile(File toSend) throws IOException
    {
        byte[] buffer = new byte[(int)toSend.length()]; // todo: better way of sizing
        InputStream in = new FileInputStream(toSend);
        OutputStream out = connection.getOutputStream();

        int count;
        while ((count = in.read(buffer)) > 0)
            out.write(buffer, 0, count);

        out.flush();
        //out.close();
        in.close();
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
