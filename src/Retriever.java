import java.io.*;
import java.net.Socket;

/**
 * Created by connor on 08/09/15.
 */
public class Retriever implements Runnable
{
    private Socket toClient;

    public Retriever(Socket toClient)
    {
        this.toClient = toClient;
    }

    @Override
    public void run()
    {
        try
        {
            getFileFromClient();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private void getFileFromClient() throws IOException
    {
        DataInputStream inFromClient = new DataInputStream(toClient.getInputStream());
        int fileSize = inFromClient.readInt();
        byte[] buffer = new byte[fileSize];

        String clientsPath = "/home/connor/Desktop/clients/" + toClient.getInetAddress().toString().replace("/", "") + "/";
        boolean worked = new File(clientsPath).mkdir();
        if(!worked)
            System.err.println("Failed to create dir: " + clientsPath);

        inFromClient.readFully(buffer, 0, fileSize);
        FileOutputStream outFile = new FileOutputStream(File.createTempFile("file_", "_test.jpg", new File(clientsPath)));
        outFile.write(buffer, 0, fileSize);
        outFile.flush();
        outFile.close();
    }
}
