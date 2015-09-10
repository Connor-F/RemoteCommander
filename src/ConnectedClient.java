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
        outToClient = new PrintWriter(connection.getOutputStream(), true);
    }

    /**
     * sends the command to the client
     * @param command the string to send to the client so it can then act upon it
     */
    public void sendCommand(String command)
    {
        System.out.println("Connected client sending cmd: " + command);
        outToClient.flush();
        outToClient.println(command);
        outToClient.flush();
    }

    /**
     * sends a file to the client and stores it in our temp dir
     * @param toSend the file to send
     * @throws IOException something went wrong create file input stream
     */
    public void sendFile(File toSend, int size) throws IOException
    {
        byte[] buffer = new byte[size];
        sendCommand("" + size);
        InputStream in = new FileInputStream(toSend);
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());

        int count;
        int sentBytes = 0;
        while(sentBytes != size && (count = in.read(buffer)) > 0)
        {
            sentBytes += count;
            System.out.println("Bytes sent: " + sentBytes + " / " + size);
            out.write(buffer, 0, count);
            out.flush();
        }

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
