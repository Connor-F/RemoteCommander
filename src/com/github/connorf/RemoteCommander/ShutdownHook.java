package com.github.connorf.RemoteCommander;

import java.io.DataOutputStream;
import java.io.IOException;

import static com.github.connorf.RemoteCommander.CommandConstants.CLIENT_TERMINATING;

/**
 * the server needs to know when a client is going offline. This class is used to send
 * a terminating message to the server when the client is about to go offline.
 */
public class ShutdownHook implements Runnable
{
    /** allows a string to be sent to the server */
    private DataOutputStream outToServer;

    /**
     * @param outToServer needed to send a string to the server
     */
    public ShutdownHook(DataOutputStream outToServer)
    {
        this.outToServer = outToServer;
    }

    @Override
    public void run()
    {
        try
        {
            outToServer.writeInt(CLIENT_TERMINATING.length());
            outToServer.write(CLIENT_TERMINATING.getBytes(), 0, CLIENT_TERMINATING.length());
        }
        catch(IOException ioe)
        {
            System.err.println("Error sending CLIENT_TERMINATING to the server.");
            ioe.printStackTrace();
        }
    }
}
