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
- find which keycodes are invalid -> or use clipboard

- sound will only work once then get stuck reading into the buffer in getFile

