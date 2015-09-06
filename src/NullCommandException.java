/**
 * Created by connor on 06/09/15.
 */
public class NullCommandException extends Exception
{
    private String msg;

    public NullCommandException(String msg)
    {
        this.msg = msg;
    }

    @Override
    public String getMessage()
    {
        return msg;
    }
}
