package com.github.connorf.RemoteCommander;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import static com.github.connorf.RemoteCommander.CommandConstants.*;

/**
 * represents a client that has connected to our server
 */
public class ConnectedClient
{
    /**
     * the connection between the client and server
     */
    private Socket connection;
    /**
     * the address of the client
     */
    private InetAddress address;

    /**
     * output to the client
     */
    private DataOutputStream outToClient;

    /** stream from the client to server */
    private DataInputStream inFromClient;

    public ConnectedClient(Socket connection, InetAddress address) throws IOException
    {
        this.connection = connection;
        this.address = address;
        outToClient = new DataOutputStream(connection.getOutputStream());
        inFromClient = new DataInputStream(connection.getInputStream());
    }

    /**
     * allows the server user to control a remote shell ON the clients computer!
     */
    public void controlRemoteShell()
    {
        try
        {
            String username = System.getProperty("user.name");
            String ipAddress = connection.getInetAddress().toString().replace("/", ""); // for nice console output
            String inputCommand;
            String workingDirectory = getStringFromClient();
            System.out.print(username + "@" + ipAddress + " ~ " + workingDirectory + "> ");
            Scanner input = new Scanner(System.in);

            while(!(inputCommand = input.nextLine()).equals(REMOTE_SHELL_TERMINATE))
            {
                sendCommandPart(inputCommand); // protocol
                workingDirectory = getStringFromClient();
                String result = getStringFromClient();
                if(result.equals(REMOTE_SHELL_INDICATE_STDERR)) // get extra info if the command produced an output
                    System.out.print("[!] " + getStringFromClient());
                else if(result.equals(REMOTE_SHELL_INDICATE_STDOUT))
                    System.out.print(getStringFromClient());
                // otherwise server send REMOTE_SHELL_INDICATE_END meaning the command worked as expected so we continue

                System.out.print(username + "@" + ipAddress + " ~ " + workingDirectory + "> ");
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * sends part of the command to the client
     * @param command the part of the command to send
     */
    public void sendCommandPart(String command)
    {
        try
        {
            outToClient.writeInt(command.length()); // client will read this many bytes on its side
            outToClient.write(command.getBytes(), 0, command.length());
            outToClient.flush();
        }
        catch(IOException ioe)
        {
            System.err.println("Failed trying to send a command part to the client: " + connection.getInetAddress());
            ioe.printStackTrace();
        }
    }

    /**
     * sends a file to the client and stores it in our temp dir
     *
     * @param toSend the file to send
     * @throws IOException something went wrong create file input stream
     */
    public void sendFile(File toSend, int size) throws IOException
    {
        byte[] buffer = new byte[size];
        sendCommandPart("" + size); // send the size in bytes
        String extension = toSend.getName().substring(toSend.getName().lastIndexOf("."));
        sendCommandPart(extension); // send file type/extension over
        InputStream in = new FileInputStream(toSend);

        int count;
        int sentBytes = 0;
        while(sentBytes != size && (count = in.read(buffer)) > 0)
        {
            sentBytes += count;
            outToClient.write(buffer, 0, count);
        }

        outToClient.flush();
        in.close();
    }

    /**
     * starts the file retrieval process, where clients files are sent to the server
     * @throws IOException if reading from the clients stream failed
     */
    public void retrieve() throws IOException
    {
        int numberOfFiles = inFromClient.readInt(); // need to know the amount of files the client is going to send over
        new Thread(new Retriever(connection, numberOfFiles)).start();
    }

    /**
     * reads a string from the client. This may be the list of running processes, or system info etc.
     * @return the string read in from the client
     * @throws IOException if something went wrong reading the string from the stream
     */
    public String getStringFromClient() throws IOException
    {
        int strLen = inFromClient.readInt();
        byte[] buffer = new byte[strLen];
        inFromClient.readFully(buffer, 0, strLen);
        return new String(buffer);
    }

    public InetAddress getAddress()
    {
        return address;
    }
}
