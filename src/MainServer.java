import java.io.IOException;

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
