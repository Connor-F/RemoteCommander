package com.github.connorf.RemoteCommander;

import java.awt.*;
import java.io.IOException;

public class MainClient
{
    public static void main(String[] args)
    {
        try
        {
            new Client();
        }
        catch(IOException ioe)
        {
            System.err.println("Error: Failed to send/recieve data from a stream.");
            ioe.printStackTrace();
        }
        catch(AWTException awte)
        {
            System.err.println("Error: Robot encountered a fatal error.");
            awte.printStackTrace();
        }
        catch(UnknownOperatingSystemException uose)
        {
            System.err.println("Error: Unrecognised host operating system.");
            uose.printStackTrace();
        }
        catch(Exception e)
        {
            System.err.println("Error: Unknown error has occurred.");
            e.printStackTrace();
        }
    }
}
