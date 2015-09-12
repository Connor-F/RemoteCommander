package com.github.connorf.RemoteCommander;

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
            System.out.println("com.github.connorf.RemoteCommander.MainClient: Exception.");
            e.printStackTrace();
        }
    }
}
