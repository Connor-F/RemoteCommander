/**
 * Created by connor on 06/09/15.
 */
public class MainClient
{
    public static void main(String[] args)
    {
        try
        {
            new Client();
        }
        catch(Exception e)
        {
            System.out.println("MainClient: Exception.");
            e.printStackTrace();
        }
    }
}
