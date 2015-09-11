package com.github.connorf.remotecommander;

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
            System.out.println("com.github.connorf.remotecommander.MainServer: Exception");
            e.printStackTrace();
        }
    }
}
