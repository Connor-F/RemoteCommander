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
            getImageFilesFromClient();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private void getImageFilesFromClient() throws IOException
    {
        byte[] buffer = new byte[128 * 1024];
        InputStream in = toClient.getInputStream();

        String clientsPath = "/home/connor/Desktop/clients/" + toClient.getInetAddress().toString().replace("/", "");
        new File(clientsPath).mkdir();

        while(in.read() != -1)
        {
            FileOutputStream fos = new FileOutputStream(File.createTempFile("img_", ".jpg", new File(clientsPath + "/")));
            int count;
            while ((count = in.read(buffer)) > 0)
                fos.write(buffer, 0, count);

            fos.flush();
            fos.close();
        }
    }
}
