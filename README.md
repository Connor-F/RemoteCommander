#Remote Commander

A multi-platform, client server program to remotely command other computers and send commands to them.

##Overview

###How it Works

Each client tries to connect to the main server. Once connected the server can issue commands to individual clients 
(based on IP addresses) or all of the connected clients. Some commands require the client to use the command line of the 
host OS (e.g. eject disk). Since the clients can be using different operating systems, the
client code checks the hosts OS and executes the appropriate commands for that OS.

**todo**
- command line history (up arrow) -> difficult without using libs
- clear vars when an incorrect command is typed
- document each command and args
- find which keycodes are invalid -> or use clipboard
- check if File.createTempFile is the same as our specified temp dir

- sound causes strange errors: a sound will play after the server has been terminated but not whilst its running
  - may be best to remove feature IF the problem occurs when using streams to retrieve screenshots