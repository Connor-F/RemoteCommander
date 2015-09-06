import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the command server which all the clients connect to
 */
public class Server
{
    /** the servers socket that clients will connect to */
    private ServerSocket serverSocket;
    private static final int SERVER_PORT = 0xbeef;
    /** contains all the clients that are currently online */
    private static Map<InetAddress, ConnectedClient> onlineClients;

    public Server() throws IOException
    {
        onlineClients = new HashMap<>();
        listen();
    }

    /**
     * infinitely loops waiting for clients to connect to the server
     * @throws IOException if something went wrong setting up the clients connection
     */
    private void listen() throws IOException
    {
        while(true)
        {
            accept();
            serverSocket.close(); // important otherwise no other clients will be able to connect
        }
    }

    /**
     * waits for a connection then adds it to the onlineClients list
     * @throws IOException if something went wrong setting up the connection
     */
    private void accept() throws IOException
    {
        serverSocket = new ServerSocket(SERVER_PORT);
        Socket clientSocket = serverSocket.accept();
        ConnectedClient connectedClient = new ConnectedClient(clientSocket, clientSocket.getInetAddress());
        onlineClients.put(clientSocket.getInetAddress(), connectedClient);
        System.out.println("Added connection: " + connectedClient.getAddress());
        new Thread(new ClientCommander()).start(); // start allowing input once we have at least 1 connection
    }

    public static Map<InetAddress, ConnectedClient> getOnlineClients()
    {
        return onlineClients;
    }
}
