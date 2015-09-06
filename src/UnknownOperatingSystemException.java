public class UnknownOperatingSystemException extends Exception
{
    private String msg;

    public UnknownOperatingSystemException(String msg)
    {
        this.msg = msg;
    }

    @Override
    public String getMessage()
    {
        return msg;
    }
}
