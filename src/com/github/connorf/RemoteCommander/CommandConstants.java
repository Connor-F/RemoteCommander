package com.github.connorf.RemoteCommander;

/**
 * represents all command keywords
 */
public final class CommandConstants
{
    // all commands
    public static final String CMD_ONLINE = "online";
    public static final String CMD_COUNT = "count";
    public static final String CMD_EJECT = "eject";
    public static final String CMD_SHUTDOWN = "shutdown";
    public static final String CMD_RESTART = "restart";
    public static final String CMD_SCREENSHOT = "screenshot";
    public static final String CMD_RETRIEVE = "retrieve";
    public static final String CMD_MSG = "msg";
    public static final String CMD_SOUND = "sound";
    public static final String CMD_CHAOS = "chaos";
    public static final String CMD_TYPE = "type";
    public static final String CMD_HELP = "help";
    public static final String CMD_SYSINFO = "sysinfo";
    public static final String CMD_ROTATE = "rotate";
    public static final String CMD_WALLPAPER = "wallpaper";
    public static final String CMD_MINIMISE = "mini";
    public static final String CMD_LIST_PROCESSES = "lsprocs";
    public static final String CMD_KILL_PROCESS = "killproc";

    public static final String HOST_ALL = "all";

    public static final String KILL_PID = "pid";
    public static final String KILL_NAME = "name";

    /** used to see if the process ran via Runtimes exec() method returned successfully or not */
    public static final int RETURN_SUCCESS = 0;

    public static final String DIR_LEFT = "left";
    public static final String DIR_RIGHT = "right";
    public static final String DIR_NORMAL = "normal";
    public static final String DIR_INVERTED = "inverted";

    public static final String MSG_TYPE_INFO = "info";
    public static final String MSG_TYPE_ERROR = "error";
    public static final String MSG_TYPE_QUESTION = "ques";
    public static final String MSG_TYPE_WARN = "warn";
    public static final String MSG_TYPE_NONE = "none";

    public static final String OS_LINUX = "Linux";
    public static final String OS_WINDOWS = "Windows";
    public static final String OS_MAC = "Mac";

    public static final String TYPE_WAV = ".wav";
}
