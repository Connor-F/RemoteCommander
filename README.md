#Remote Commander

A multi-platform, client server program to remotely command other computers and send commands to them.

##Overview

###How it Works

Each client tries to connect to the main server. Once connected the server can issue commands to individual clients 
(based on IP addresses) or all of the connected clients. Some commands require the client to use the command line of the 
host OS (e.g. eject disk). Since the clients can be using different operating systems, the
client code checks the hosts OS and executes the appropriate commands for that OS.

**todo**
- document each command and args
- maybe: if com.github.connorf.RemoteCommander.MakeSound unmuted the systems mixer, then play the sound and mute it again
- way for client to tell server its going offline, then remove that client from the online clients map
- need to store wallpaper somewhere, otherwise it gets deleted in temp folder and then when the sys restarts the wallpaper is blank

- if there > 1 connection, sending an un-transmitted command (count, online) then trying to send a transmitted command causes the clients to recieve no data at all
  - > 1 connection and sending multiple sound commands causes the clients to hang, even though they both recieve the file

