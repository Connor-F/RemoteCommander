package com.github.connorf.remotecommander;

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
            System.out.println("com.github.connorf.remotecommander.MainClient: Exception.");
            e.printStackTrace();
        }
    }
}
