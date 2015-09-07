import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.model.IspResponse;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Scanner;

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
            String command = new Scanner(System.in).nextLine().toLowerCase(); // blocks
            if(commandValid(command.split("\\s+")[0]))
                parseAndSendCommand(command);
        }
    }

    /**
     * checks to see if the command exists
     * @param command the command to check
     * @return true if the command is valid, false otherwise
     */
    private boolean commandValid(String command)
    {
        return command.equals("count") || command.equals("online") || command.equals("eject") || command.equals("sound") || command.equals("shutdown") || command.equals("restart") || command.equals("screenshot") || command.equals("msg");
    }

    /**
     * uses the command input by the server user to execute the command on the
     * specified clients
     * @param fullCommand the users command input. Each command should have the format:
     *                      COMMAND HOST. Where COMMAND is the instruction e.g. poweroff and HOST is either "all" or
     *                      the IP address of the target host. The online command takes no host argument.
     * @throws UnknownHostException thrown if the host provided is incorrect
     */
    private void parseAndSendCommand(String fullCommand) throws NullCommandException, IOException
    {
        String[] commandTokens = fullCommand.split("\\s+");
        String command = commandTokens[0];

        // Zero argument commands: online, count
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

        // One argument commands: eject, shutdown, reboot, screenshot
        // e.g. eject 127.0.0.1
        String host = null;
        if(commandTokens.length > 1)
            host = commandTokens[1];
        if(host == null)
            throw new NullCommandException("Host not provided");

        ConnectedClient target = null;
        if(host.equals("all")) // 2 tokens means a COMMAND and HOST has been provided
            sendCommandAll(command);
        else // find the specified client
            target = connectedClients.get(InetAddress.getByName(host));

        if(target == null && !host.equals("all"))
            throw new UnknownHostException(host + " isn't online or doesn't exist");

        if(!host.equals("all"))
            target.sendCommand(command); // send the cmd to the specified connected client

        // Two argument commands: msg, sound
        // e.g. msg 127.0.0.1 "No place like home"
        String argument = null;
        if(commandTokens.length == 3)
        {
            argument = commandTokens[2];
            if(host.equals("all")) // command has already been sent, just need to send argument now
                sendCommandAll(argument);
            else
                target.sendCommand(argument); // if the client recieves the msg command it knows to read the next line of input (the msg itself)
        }
    }

    /**
     * iterates through each connected client and issues the command
     * @param command the command(s) to send to each client
     */
    private void sendCommandAll(String command)
    {
        for (Map.Entry<InetAddress, ConnectedClient> client : connectedClients.entrySet())
        {
            client.getValue().sendCommand(command);
        }
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
