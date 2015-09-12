package com.github.connorf.RemoteCommander;

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
            System.out.println("com.github.connorf.RemoteCommander.MainServer: Exception");
            e.printStackTrace();
        }
    }
}
