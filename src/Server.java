import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
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
    private static final int MAJOR_VERSION = 0;
    private static final int MINOR_VERSION = 2;

    public Server() throws IOException
    {
        welcomeMessage();
        onlineClients = new HashMap<>();
        listen();
    }

    /**
     * prints a nice welcome message on startup
     */
    private void welcomeMessage()
    {
        System.out.println("┏━━━━━━━━━━━━━━━━━━━━━━━━┓");
        System.out.println("┃====== Welcome to ======┃");
        System.out.println("┃=== Remote Commander ===┃");
        System.out.println("┃===== Version: " + MAJOR_VERSION + "." + MINOR_VERSION + " =====┃");
        System.out.println("┗━━━━━━━━━━━━━━━━━━━━━━━━┛");
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
