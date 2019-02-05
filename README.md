# Saboteur_Game
Game for AI final project

# Instructions for AI class
- Extends AI class for your AI class
- Please use playAlgorithm method provided by AI class , because the method is used by GameSimulator to give your class input 
  on the current game situations
- Please use PlayCard methods to provide output for the GameSimulator
- playersVisibleStatus is used for getting information of other player status, ex : total cart blocked on a player, etc.
- MyAI class is an example class, please create 

How to read logs list in GameSimulator

- index 0 = playerNumber
- index 1 = playerMove
- index 2 = coordinate X if not board card -1
- index 3 = coordinate Y if not board card -1
- index 4 = target Player if not action card -1
	 
-1 means no value

Example on how to read log :
- [0, 1, 8, 0, -1] player 0 plays MAP card on cell 8,0 (8,0 is top goal card)
- [0, 2, 2, 4, -1] player 0 plays ROCKFALL card on cell 8,0 (8,0 is top goal card)
- [0, 0, 1, 2, -1] player 0 plays PATH card on cell 1,2
- [0, 3, -1, -1, 2] player 0 plays BLOCK card on player 2

