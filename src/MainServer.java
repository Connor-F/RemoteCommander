import java.io.IOException;

/**
 * Created by connor on 06/09/15.
 */
public class MainServer
{
    public static void main(String[] args)
    {
        try
        {
            new Server();
        }
        catch(IOException e)
        {
            System.out.println("MainServer: Exception");
            e.printStackTrace();
        }
    }
}
