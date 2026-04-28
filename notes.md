# My Notes

### -- PHASE 0 --
* Server module: program that handles network requests to create and play games, stores games persistently
in a database and sends out notifications to all the players of a game
* Client module: command line program that players use to create and play a game of chess, client communicates
with server over the network to play a game with other clients
* Shared module: code library that contains rules and representation of a chess game that both client and server
use to exercise and validate game play

**Client and Server modules use Websocket to communicate with each other

GENERAL NOTES:
- hashCode method basically presents a default solution for something. use @Override to get a different solution