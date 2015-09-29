package com.github.connorf.RemoteCommander;

import static com.github.connorf.RemoteCommander.CommandConstants.LOCAL_STORAGE_PATH;

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

    /**
     * runs in the background and retrieved a file from the clients machine and then stores it onto the servers machine
     * in the clients own directory
     * @throws IOException if something went wrong reading from the stream
     */
    private void getFileFromClient() throws IOException
    {
        DataInputStream inFromClient = new DataInputStream(toClient.getInputStream());
        int fileSize = inFromClient.readInt(); // client sends file size
        int filenameLen = inFromClient.readInt(); // then filename length
        byte[] filenameBytes = new byte[filenameLen];
        inFromClient.readFully(filenameBytes, 0, filenameLen); // then filename itself
        String filename = new String(filenameBytes);

        byte[] buffer = new byte[fileSize];

        String clientsPath = LOCAL_STORAGE_PATH + toClient.getInetAddress().toString().replace("/", "") + "/";
        File clientsDir = new File(clientsPath);
        if(!clientsDir.exists())
            if(!clientsDir.mkdir())
                System.err.println("Failed to create directory for the client at: " + clientsPath);

        inFromClient.readFully(buffer, 0, fileSize);
        FileOutputStream outFile = new FileOutputStream(new File(clientsPath + File.separator + filename));
        outFile.write(buffer, 0, fileSize);
        outFile.flush();
        outFile.close();
    }
}
