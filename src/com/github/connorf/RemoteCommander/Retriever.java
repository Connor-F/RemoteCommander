package com.github.connorf.RemoteCommander;

import java.io.*;
import java.net.Socket;

/**
 * retrieves the specified number of files from the client machine
 */
public class Retriever implements Runnable
{
    private Socket toClient;
    private int numOfFiles;

    public Retriever(Socket toClient, int numOfFiles)
    {
        this.toClient = toClient;
        this.numOfFiles = numOfFiles;
    }

    @Override
    public void run()
    {
        try
        {
            for(int i = 0; i < numOfFiles; i++)
                getFileFromClient();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private void getFileFromClient() throws IOException
    {
        DataInputStream inFromClient = new DataInputStream(toClient.getInputStream());
        int fileSize = inFromClient.readInt();
        byte[] buffer = new byte[fileSize];

        String clientsPath = "/home/connor/Desktop/clients/" + toClient.getInetAddress().toString().replace("/", "") + "/";
        File clientsDir = new File(clientsPath);
        if(!clientsDir.exists())
            if(!clientsDir.mkdir())
                System.err.println("Failed to create directory for the client at: " + clientsPath);

        inFromClient.readFully(buffer, 0, fileSize);
        FileOutputStream outFile = new FileOutputStream(File.createTempFile("file_", "_test.jpg", new File(clientsPath)));
        outFile.write(buffer, 0, fileSize);
        outFile.flush();
        outFile.close();
    }
}
