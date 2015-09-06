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
