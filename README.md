# Dungeons-Online-MMORPG

## Game Explanation 
Dungeons Online is a multiplayer game where players can
collect treasures (e.g. spells, weapons, health/mana potions), 
fight minions and interact with other players. 

Each Actor (player/minion) has its own name, level, stats (e.g. health/mana/attack/defense points), weapon and spell.
Players in addition have their own experience and backpack (i.e. inventory) where they can store treasures.
Weapons and spells have their own power stats and level (in addition spells have mana cost). 
Equipping/Learning a weapon/spell with higher level than the player's is impossible.

A player can move around the map, interact with treasures they've found (use or collect to backpack), 
fight with other minions/players or trade treasures from their backpack with other players. 
A player can also open its backpack and perform commands on a choosen treasure from the backpack (e.g. use, drop).
Killing a minion grants experience to the killer. 
Killing another player forces the dead player to drop a treasure from their backpack (if there is any).


## Code Explanation
There are several main modules running the server part: Game Engine, Network Server, 
Map, Players Storage, Static Objects Storage, DungeonsOnline (Main) Server.


`Game Engine` performs calculations and the logic of simple game functionalities.

`Network Server` uses NIO to establish and maintain the communication between server and clients (players).

`Map` contains a matrix representing the game's map, shared among all players.

`Players Storage` contains information about the players and their connections.

`Static Objects Storage` mantains all static game objects (e.g. treasures, minions).

The `Main Server` combines all previous modules and manages the main logic of the game.

  
  
There is a decent amount of threads working on the server side in order to achieve a non-blocking behavior and faster performance.

The maximum number of players is currently set to 9.

The Server part has its own unit tests (JUnit 4, Mockito).



The Client part has nothing but a message receiver (listener)/sender used to communicate with the server.
The lack of any business game logic on the client part is intentional.


*Written in Java 15 using IntelliJ IDEA.*

