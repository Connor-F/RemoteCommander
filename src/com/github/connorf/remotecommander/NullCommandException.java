package com.github.connorf.remotecommander;

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
