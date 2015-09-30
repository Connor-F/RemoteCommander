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
        catch(IOException ioe)
        {
            System.err.println("Error: Something went wrong sending/recieving from a data stream.");
            ioe.printStackTrace();
        }
    }
}
