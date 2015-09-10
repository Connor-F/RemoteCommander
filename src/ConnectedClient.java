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
    private DataOutputStream outToClient;
//    private PrintWriter outToClient;

    public ConnectedClient(Socket connection, InetAddress address) throws IOException
    {
        this.connection = connection;
        this.address = address;
        //outToClient = new PrintWriter(connection.getOutputStream(), true);
        outToClient = new DataOutputStream(connection.getOutputStream());
    }

    /**
     * sends part of the command to the client
     * @param command the part of the command to send
     */
    public void sendCommandPart(String command)
    {
        try
        {
                outToClient.writeInt(command.length()); // client will read this many bytes on its side
                outToClient.write(command.getBytes(), 0, command.length());
                outToClient.flush();
        }
        catch(IOException ioe)
        {
            System.err.println("Failed trying to send a command part to the client");
            ioe.printStackTrace();
        }
    }

    /**
     * sends a file to the client and stores it in our temp dir
     * @param toSend the file to send
     * @throws IOException something went wrong create file input stream
     */
    public void sendFile(File toSend, int size) throws IOException
    {
        byte[] buffer = new byte[size];
        sendCommandPart("" + size);
        InputStream in = new FileInputStream(toSend);
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());

        int count;
        int sentBytes = 0;
        while(sentBytes != size && (count = in.read(buffer)) > 0)
        {
            sentBytes += count;
            System.out.println("Bytes sent: " + sentBytes + " / " + size);
            out.write(buffer, 0, count);
        }

        out.flush();
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
