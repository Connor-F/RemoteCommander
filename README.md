#Remote Commander

A multi-platform, client server program to remotely command other computers.

##Remote Commands

These commands are executed from the server and cause something to happen on the clients machine. More info can be found about their
usage by checking the `Help` and `CommandConstants` classes. `HOST` can either be a specified IPv4 address of a client or the word "all" to 
send the command to every online client ("all" applies to certain commands only).

- `talk HOST "MSG HERE"` -> make the clients computer talk to them 
- `screenshot HOST` -> take a screenshot of the clients computer and save it
- `sysinfo HOST` -> return useful system information about the clients machine
- `retrieve HOST` -> transfers all screenshots taken of the clients machine to the server
- `sound HOST /local/path/to/sound/file.wav` -> transfers the sound file to the client and plays the file on the clients machine
- `lsprocs HOST` -> returns all the running processes on the clients machine
- `upload HOST /local/path/to/file/to/upload` -> uploads the file from the server to the clients temp dir
- `type HOST "MSG HERE"` -> type out the message provided on the clients machine
- `msg HOST "TITLE" "MSG HERE" TYPE` -> show a message box on the clients machine
- `shutdown HOST` -> force shutdown the clients machine
- `restart HOST` -> force restart the clients machine
- `wallpaper HOST /local/path/to/new/wallpaper.jpg` -> set the desktop wallpaper on the clients machine
- `rotate HOST DIRECTION` -> rotate the clients screen orientation
- `eject HOST` -> eject the clients disk tray
- `kill HOST TYPE ARG` -> kill any process on the clients machine via its name or PID
- `mini HOST` -> minimise all open windows
- `chaos HOST DURATION INTERVAL` -> causes the clients machine to randomly type and move the mouse

- `shell HOST` -> start a remote shell session on the clients machine to allow for complete control

When a shell session is started the following inbuilt commands become available...
- `exit_shell` -> terminates the remote shell session
- `get_file FILE` -> transfers the file from the clients machine to the server (puts the file into the clients dir)

##Local Commands

These commands only run on the server. Clients are not needed for these commands to run.

- `online` -> prints out geolocation info about each online client
- `count` -> returns the number of online clients
- `help COMMAND` -> prints out help info relating to the supplied command. Leaving the command option out will print brief help of all commands


###How it Works

Each client tries to connect to the main server. Once connected the server can issue commands to individual clients 
(based on IP addresses) or all of the connected clients. Some commands require the client to use the command line of the 
host OS (e.g. eject disk). Since the clients can be using different operating systems, the
client code checks the hosts OS and executes the appropriate commands for that OS.

**todo**
- way for client to tell server its going offline, then remove that client from the online clients map
- need to store wallpaper somewhere, otherwise it gets deleted in temp folder and then when the sys restarts the wallpaper is blank
- exit server command

- if there > 1 connection, sending an un-transmitted command (count, online) then trying to send a transmitted command causes the clients to recieve no data at all
  - > 1 connection and sending multiple sound commands causes the clients to hang, even though they both recieve the file
  
####Client Requirements

#####Linux

These programs are needed for some of the commands. Ubuntu based distros have them installed by default. Other distros
have not been tested but the source code can easily be modified to support them.

- `xrandr` to allow the screen to be rotated
- `espeak` to allow 

#####Windows and Mac

All programs needed should be installed already.