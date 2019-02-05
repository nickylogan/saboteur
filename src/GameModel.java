import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 * 
 * @author DATACLIM-PIKU2
 * Observe the following objects: <p>
 * - players (to update their properties regarding goal cards)<p>
 * - card (to update the selectedCard attribute)
 */
public class GameModel extends Observable implements Observer
{
	/** The players in this game */
	private ArrayList<Player> players;
	private ArrayList<PlayerVisibleStatus> playersVisibleStatus;
	/** The player's index currently in turn*/
	private int idxCurrentPlayer;
	/** The card selected to be played by the player currently in turn */
	private CardPlayable selectedCard;
	/** The deck of playable cards used in the game */
	private ArrayList<CardPlayable> deck;
	/** The deck of gold cards used in the game */
	private ArrayList<CardGold> goldDeck;
	/** The board of the game containing 4x8 cells */
	private Board board;
	/** The boolean value indicating whether the game is finished or not*/
	private boolean isFinished;
	/** */
	private ArrayList<SelectableObject> target;
	
	private JTextArea console;
	
	boolean useThis = false;
	
	public final double H_CONSTANT_PATH = 1;
	public final double H_CONSTANT_MAP = 1;
	public final double H_CONSTANT_ROCKFALL = 1;
	public final double H_CONSTANT_BLOCK = 1;
	public final double H_CONSTANT_REPAIR = 1;
	
	public static final double k0 = 0.5;
	public static final double k1 = k0 + 2.5;
	public static final double k2 = k0 - 0.1;
	public static final double k3 = k2;
	public static final double k4 = k2;
	public static final double k5 = k2/2;
	public static final double c1 = 5*k0;
	public static final double c2 = c1 + 0.1;
	public static final double c3 = 0.5;
	public static final double c4 = c3;
	
	public GameModel(int ttlPlayers) throws Exception
	{
		//============================
		//initialize the deck of cards
		//============================
		initalizeDeck();
		/*for(int i=0; i<deck.size(); i++)
			deck.get(i).addObserver(this);
		for(int i=0; i<deck.size(); i++)
			goldDeck.get(i).addObserver(this);*/	
		
		//======================
		//initialize the players
		//======================				
		//determine the total number of players
		if(ttlPlayers < 4)
		{
			ttlPlayers = 4;
			JOptionPane.showMessageDialog(null, "Minimum number of players is 4.\n4 players will be generated.", "Warning In Number Of Players", JOptionPane.INFORMATION_MESSAGE);
		}
		else if(ttlPlayers > 10)
		{
			ttlPlayers = 10;
			JOptionPane.showMessageDialog(null, "Maximum number of players is 10.\n10 players will be generated.", "Warning In Number Of Players", JOptionPane.INFORMATION_MESSAGE);
		}
		//initialize the player randomly
		initializePlayer(ttlPlayers);
		
		//distribute cards from the deck to the players
		int cardsPerPlayer = getPlayerTtlCards(ttlPlayers);		
		CardPlayable tempCard;
		for(int i=0; i<cardsPerPlayer; i++)
		{
			for(int ii = 0; ii<ttlPlayers; ii++)
			{
				tempCard = deck.get(0);
				deck.remove(0);
				players.get(ii).addCard(tempCard);				
			}
		}
		tempCard = null;
						
		//====================
		//initialize the board
		//====================
		//add start card and three goal cards				
		CardPath startCard = new CardPath(0, 1, CardPath.PATH, CardPath.PATH, CardPath.PATH, CardPath.PATH, H_CONSTANT_PATH);
		ArrayList<CardPath> tempGoalCards = new ArrayList<CardPath>();		
		tempGoalCards.add(new CardPath(68, 1, CardPath.PATH, CardPath.PATH, CardPath.PATH, CardPath.PATH, H_CONSTANT_PATH));
		tempGoalCards.add(new CardPath(69, 1, CardPath.PATH, CardPath.PATH, CardPath.PATH, CardPath.PATH, H_CONSTANT_PATH));
		tempGoalCards.get(tempGoalCards.size()-1).rotate();
		tempGoalCards.add(new CardPath(70, 1, CardPath.PATH, CardPath.PATH, CardPath.PATH, CardPath.PATH, H_CONSTANT_PATH));
		Collections.shuffle(tempGoalCards);
		board = new Board(startCard, tempGoalCards.get(0), tempGoalCards.get(1), tempGoalCards.get(2));
		isFinished = false;
		
		//initialize the target (Cell / Player) to put/apply a card
		target = new ArrayList<SelectableObject>();
		
		//determine the first player to move
		idxCurrentPlayer = (int)(Math.random()*ttlPlayers);	
//		idxCurrentPlayer = 0;
//		System.out.println(idxCurrentPlayer);
		if(useThis){
			nextTurn();
		}
		
//		System.out.println(idxCurrentPlayer);
	}
	
	public void setConsole(JTextArea console){
		this.console = console;
	}
	
	public JTextArea getConsole(){
		return console;
	}
	
	/**
	 * get the player currently in turn
	 * @return a shallow copy of the player object currently in turn
	 */
	public Player getCurrentPlayer()
	{
		return players.get(idxCurrentPlayer);
	}
	/**
	 * get the player's index current in turn
	 * @return an integer which is the index of the player currently in turn
	 */
	public int getIdxCurrentPlayer()
	{
		return idxCurrentPlayer;
	}
	
	/**
	 * get the total number of cards per player for a given total players
	 * @param ttlPlayers the total number of players in this game
	 * @return the total number of cards per player
	 */
	private int getPlayerTtlCards(int ttlPlayers)
	{
		switch(ttlPlayers)
		{
		case 3: case 4: case 5: 
			return 6;
		case 6: case 7:
			return 5;
		case 8: case 9: case 10:
			return 4;
		default:
			return 6;
		}
	}
	
	/**
	 * initialize all players and their role randomly
	 * @param ttlPlayers total number of players in the game
	 */
	public void initializePlayer(int ttlPlayers)
	{
		players = new ArrayList<Player>();
		setPlayersVisibleStatus(new ArrayList<PlayerVisibleStatus>());
		//determine the number of saboteurs and goldminers
		int ttlSaboteur = getTtlSaboteur(ttlPlayers);
		int ttlGoldminer = ttlPlayers - ttlSaboteur;
		
		//generate the players randomly
		Random rand = new Random();
		while(players.size() < ttlPlayers)
		{
			//determine the role randomly
			//role = 0 --> saboteur, role = 1 --> goldminer
			int role = Player.SABOTEUR; 
			if(ttlSaboteur > 0 && ttlGoldminer > 0)
			{
				role = rand.nextInt(2);
				if(role == 0)
					role = Player.SABOTEUR;
				else
					role = Player.GOLD_MINER;
			}
			else if(ttlSaboteur == 0)
				role = Player.GOLD_MINER;
			else
				role = Player.SABOTEUR;
			
			//add the player
			players.add(new Player(players.size(), role));
			if(role == Player.SABOTEUR)					
				ttlSaboteur--;							
			else		
				ttlGoldminer--;			
		}
		int index = 0;
		while(playersVisibleStatus.size() < ttlPlayers){
			playersVisibleStatus.add(new PlayerVisibleStatus(players.get(index).number));
			++index;
		}
	}
	/**
	 * get the number of saboteurs allowed in the game based on the number of players
	 * @param ttlPlayers total number of players in the game
	 * @return the number of saboteurs allowed according to the rules and the number of players
	 */
	private int getTtlSaboteur(int ttlPlayers)
	{
		if(ttlPlayers == 4)
			return 1;
		else if(ttlPlayers <= 6)
			return 2;
		else if(ttlPlayers <= 10)
			return 3;
		else 
			return 0;
	}	
	
	/**
	 * get a shallow copy of the board used in this model
	 * @return a shallow copy of the board in this model
	 */
	public Board getBoard(){
		return board;
	}
	
	/**
	 * get a shallow copy of all the players in this model
	 * @return an ArrayList object containing shallow copies of all the players in this model
	 */
	public ArrayList<Player> getPlayers()
	{
		return players;
	}
	
	/**
	 * find the cells where the given board card can be played on 
	 * @param boardCard the board card to be played
	 * @return a collection of cells where the given board card can be played on
	 * @exception when the boardCard given is not a board card (a Block / Repair Card which is played on a player)
	 */
	public ArrayList<Cell> findPossibleMovesOnBoard(CardPlayable boardCard) throws Exception
	{
		if(!boardCard.isBoardCard())
			throw new Exception("Action Error: the selected card cannot be played on the board card");		
		int cardType = (int)boardCard.getAttributeVector()[1];
		if(cardType == CardPlayable.MAP_CARD)
		{
			return board.getGoalCells();
		}
		else if(cardType == CardPlayable.ROCKFALL_CARD)
		{
			return board.getFilledCells();
		}
		else //if(cardType == CardPlayable.DEADEND_CARD || cardType == CardPlayable.PATHWAY_CARD) 
		{
			return board.getPlayableCells();
		}
		
	}
	
	
	/**
	 * find the players whom the given card can be played against with by the specified players
	 * @param player the player to play the given card
	 * @param playerCard the card to be played against
	 * @return the list of players whom the given card can be played against with
	 */
	public ArrayList<Player> findPossibleMovesOnPlayers(Player player, CardPlayable playerCard)
	{
		ArrayList<Player> possiblePlayers = new ArrayList<Player>();
		for(int i=0;i<players.size();i++)
		{
			if(players.get(i).getAttributes()[0] == player.getAttributes()[0] && (int)playerCard.getAttributeVector()[1] != CardPlayable.REPAIR_CARD)
				continue;
			//else if(players.get(i).getAttributes()[0] == player.getAttributes()[0] && (int)playerCard.getAttributeVector()[1] == CardPlayable.REPAIR_CARD)
			else
				possiblePlayers.add(players.get(i));
		}
		return possiblePlayers;
	}
	
	public double calculateHeuristicValue()
	{
		return 0;
	}
	
	/**
	 * 
	 * @param board
	 * @param cell
	 * @exception when giving non-board card to this function
	 */
	public void playCardOnBoard(CardPlayable boardCard, Cell cell) throws Exception
	{
		
		if(players.get(idxCurrentPlayer).getTtlBlocks()>0){
			console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" You're blocked!\n"));
//			JOptionPane.showMessageDialog(null, "You're blocked!");
			return;
		}
		
		//check the card
		if(!boardCard.isBoardCard())
			throw new Exception("Action Error: cannot place a non-board card on the board.");
		
		//get the available cells		
		if(!target.contains(cell))
			throw new Exception("Action Error: the selected card cannot be played on the selected cell.");
		
		double[] cardProperties = boardCard.getAttributeVector();
		//map card
		if((int)cardProperties[1] == CardPlayable.MAP_CARD)
		{
			//change the player's status who played the card, then based
			if(cell.getX() == Board.WIDTH-1 && cell.getY() == 0)
			{				
				if(board.peekGoal(Board.TOP_GOAL))
				{
//					JOptionPane.showMessageDialog(null, "You got the gold card!");
					players.get(idxCurrentPlayer).setTopGoal(true);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" checking top goal..."+"\n"));
					TimeUnit.MILLISECONDS.sleep(500);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" You got the gold card!"+"\n"));
				}
				else
				{
//					JOptionPane.showMessageDialog(null, "You got the rock card!");
					players.get(idxCurrentPlayer).setTopGoal(false);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" checking top goal..."+"\n"));
					TimeUnit.MILLISECONDS.sleep(500);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" You got the rock card!"+"\n"));

				}
			}
			else if(cell.getX() == Board.WIDTH-1 && cell.getY() == 2)
			{
				if(board.peekGoal(Board.MIDDLE_GOAL))
				{
//					JOptionPane.showMessageDialog(null, "You got the gold card!");
					players.get(idxCurrentPlayer).setMiddleGoal(true);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" checking middle goal..."+"\n"));
					TimeUnit.MILLISECONDS.sleep(500);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" You got the gold card!"+"\n"));
				}
				else
				{
//					JOptionPane.showMessageDialog(null, "You got the rock card!");
					players.get(idxCurrentPlayer).setMiddleGoal(false);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" checking middle goal..."+"\n"));
					TimeUnit.MILLISECONDS.sleep(500);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" You got the rock card!"+"\n"));
				}
			}
			else if(cell.getX() == Board.WIDTH-1 && cell.getY() == 4)
			{
				if(board.peekGoal(Board.BOTTOM_GOAL))
				{
//					JOptionPane.showMessageDialog(null, "You got the gold card!");
					players.get(idxCurrentPlayer).setBottomGoal(true);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" checking bottom goal..."+"\n"));
					TimeUnit.MILLISECONDS.sleep(500);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" You got the gold card!"+"\n"));
				}
				else
				{
//					JOptionPane.showMessageDialog(null, "You got the rock card!");
					players.get(idxCurrentPlayer).setBottomGoal(false);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" checking bottom goal..."+"\n"));
					TimeUnit.MILLISECONDS.sleep(500);
					console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" You got the rock card!"+"\n"));
				}
			}
			
		}						
		//rock-fall card
		else if((int)cardProperties[1] == CardPlayable.ROCKFALL_CARD){
//			System.out.println("tes");
			console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" is playing ROCKFALL card on ("+cell.getX()+" , "+cell.getY()+")"+"\n"));
			cell.putCard(null);		
		}
		//pathway card
		else{
//			System.out.println("path");
			console.setText(console.getText().toString().concat("Player "+ getCurrentPlayer().getAttributes()[0] +" is playing PATH card on ("+cell.getX()+" , "+cell.getY()+")"+"\n"));
			cell.putCard((CardPath)boardCard);	
		}
		
		//remove the card from the player's hand
		players.get(idxCurrentPlayer).playCard(boardCard);		
//		System.out.println(players.get(idxCurrentPlayer).getGoals());
		//give one card to the player who just moved		
		takesCardFromDeck();
		
		//change the turn
		if(useThis){
			nextTurn();
		}
	}	
	/**
	 * play a non-board card to a player
	 * @param nonBoardCard
	 * @param player
	 * @throws Exception
	 */
	public void playPlayerCard(CardAction nonBoardCard, Player targetPlayer) throws Exception
	{		
		if(nonBoardCard.isBoardCard())
			throw new Exception("Error in placing a board card on a player");
		players.get(idxCurrentPlayer).playCard(nonBoardCard);
		targetPlayer.applyCard(nonBoardCard);
		
		//give the player just moved a card from the deck
		takesCardFromDeck();
		if(useThis){
			nextTurn();
		}
	}	
	/**
	 * 
	 * @param card
	 * @throws Exception
	 */
	public void discardCard(CardPlayable card) throws Exception
	{				
		players.get(idxCurrentPlayer).playCard(card);
		console.setText(console.getText().toString().concat("Player "+ getIdxCurrentPlayer() + " is discarding card\n"));
		//give the player just moved a card from the deck
		
		
		if(!deck.isEmpty())	{
			takesCardFromDeck();
			if(useThis){
				nextTurn();
			}
		}
		
		else if(deck.isEmpty()){
			if(useThis){
				nextTurn();
			}
		}
		
		
	}
	/**
	 * open all the cards on the player specified by the given index
	 * @param idxPlayer an integer indicating the player's index whose cards to be opened
	 */
	private void openCards(int idxPlayer)
	{		
		for(int i=0; i<players.get(idxCurrentPlayer).getTtlCards(); i++)
		{
			players.get(idxCurrentPlayer).getCard(i).open();
//			if(players.get(idxCurrentPlayer).getTtlBlocks() > 0)
//			{
//				if(!players.get(idxCurrentPlayer).getCard(i).isBoardCard() || (int)players.get(idxCurrentPlayer).getCard(i).getAttributeVector()[1]==3)
//					players.get(idxCurrentPlayer).getCard(i).setSelectable(true);
//			}
//			else
//				players.get(idxCurrentPlayer).getCard(i).setSelectable(true);
			players.get(idxCurrentPlayer).getCard(i).setSelectable(true);
		}
	}
	/**
	 * close all the cards on the player specified by the given index
	 * @param idxPlayer an integer indicating the player's index whose cards to be closed
	 */
	private void closeCards(int idxPlayer)
	{
		for(int i=0; i<players.get(idxCurrentPlayer).getTtlCards(); i++)
		{
			players.get(idxCurrentPlayer).getCard(i).setSelectable(false);
			players.get(idxCurrentPlayer).getCard(i).close();
		}
	}

	/**
	 * change the player in turn to the one next to the current player (index + 1), and check whether gold goal card has been reached or not in the previous turn
	 */
	public void nextTurn()
	{
		//disable all the targets
		for(int i=0; i<target.size(); i++)
			target.get(i).setSelectable(false);
		target.clear();
		
		int count =0;
		
		for(int x=0 ; x<players.size(); x++){
			if(players.get(x).getTtlCards()==0)
			{
				++count;
			}
		}
		//check whether any goal cards has been reached
		if(board.isGoldGoalReached())
			isFinished = true;
		
		//if the game is not finished, change the turn
		if(isFinished)
		{
			//close all the cards on this player
			closeCards(idxCurrentPlayer);
			
			//set the states of the targets 
			for(int i=0; i<target.size(); i++)
				target.get(i).setSelectable(false);
//			JOptionPane.showMessageDialog(null, "Gold Miner Wins!");
			console.setText(console.getText().toString().concat("Gold Miner Wins!\n"));
		}else if(count==players.size()){
			
			//close all the cards on this player
			closeCards(idxCurrentPlayer);
			
			//set the states of the targets 
			for(int i=0; i<target.size(); i++)
				target.get(i).setSelectable(false);
//			JOptionPane.showMessageDialog(null, "Saboteur Wins!");
			console.setText(console.getText().toString().concat("Saboteur Wins!\n"));
		}
		else
		{			
			//close all the cards on this player
			closeCards(idxCurrentPlayer);
			
			//determine the next player
			idxCurrentPlayer = (idxCurrentPlayer+1)%players.size();
			
			//open all the cards on this player
			openCards(idxCurrentPlayer);
					
			//notify the observer
			setChanged();
			notifyObservers(players.get(idxCurrentPlayer));
		}
	}
	/**
	 * give one card at the top of the deck to the player's currently in turn
	 */
	private void takesCardFromDeck()
	{
		
		if(deck.size()>0){
			//add the card at the top of the deck to the player's currently in turn
			players.get(idxCurrentPlayer).addCard(deck.get(0));
			//remove the card from the deck
			deck.remove(0);
		}
		
		
	}
	
	public void skipTurn()
	{
		
	}
	
	/**
	 * initialize the deck of the cards (re-shuffling the cards) at the beginning of the game
	 * @throws IOException 
	 */
	public void initalizeDeck() throws IOException
	{		
		initializePlayableDeck();
		initializeGoldDeck();				
	}
	/**
	 * initialize the deck of pathway cards (path and dead-end)
	 * @throws IOException when the image for the card cannot be found (possibly because of wrong parameters in initializing the cards)
	 */
	private void initializePlayableDeck() throws IOException
	{
		//initialize 67 cards
				deck = new ArrayList<CardPlayable>();
				int cardNum = 1, cardType = CardPlayable.PATHWAY_CARD, cardTtl = 5;
								
				//initialize cross-road pathway cards
				cardTtl = 5;
//				cardTtl = 30;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.PATH, CardPath.PATH, CardPath.PATH, CardPath.PATH, H_CONSTANT_PATH));
					cardNum++;
				}		
				//initialize horizontal T pathway cards
				cardTtl = 5;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.ROCK, CardPath.PATH, CardPath.PATH, CardPath.PATH, H_CONSTANT_PATH));
					cardNum++;
				}		
				//initialize vertical T pathway cards
				cardTtl = 5;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.PATH, CardPath.ROCK, CardPath.PATH, CardPath.PATH, H_CONSTANT_PATH));
					cardNum++;
				}
				//initialize horizontal pathway cards
				cardTtl = 3;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.ROCK, CardPath.PATH, CardPath.ROCK, CardPath.PATH, H_CONSTANT_PATH));
					cardNum++;
				}
				//initialize vertical pathway cards
				cardTtl = 4;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.PATH, CardPath.ROCK, CardPath.PATH, CardPath.ROCK, H_CONSTANT_PATH));
					cardNum++;
				}		
				//initialize turn left pathway cards
				cardTtl = 5;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.ROCK, CardPath.ROCK, CardPath.PATH, CardPath.PATH, H_CONSTANT_PATH));
					cardNum++;
				}
				//initialize turn right pathway cards
				cardTtl = 4;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.ROCK, CardPath.PATH, CardPath.PATH, CardPath.ROCK, H_CONSTANT_PATH));
					cardNum++;
				}
				
				//=====================================
				//initialize all dead-end pathway cards
				cardTtl = 1;
				cardType = CardPlayable.DEADEND_CARD;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.DEADEND, CardPath.DEADEND, CardPath.DEADEND, CardPath.DEADEND, H_CONSTANT_PATH));
					cardNum++;
				}		
				//initialize horizontal T dead-end pathway cards
				cardTtl = 1;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.ROCK, CardPath.DEADEND, CardPath.DEADEND, CardPath.DEADEND, H_CONSTANT_PATH));
					cardNum++;
				}
				//initialize vertical T dead-end pathway cards
				cardTtl = 1;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.DEADEND, CardPath.ROCK, CardPath.DEADEND, CardPath.DEADEND, H_CONSTANT_PATH));
					cardNum++;
				}
				//initialize horizontal dead-end pathway cards
				cardTtl = 1;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.ROCK, CardPath.DEADEND, CardPath.ROCK, CardPath.DEADEND, H_CONSTANT_PATH));
					cardNum++;
				}
				//initialize vertical dead-end pathway cards
				cardTtl = 1;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.DEADEND, CardPath.ROCK, CardPath.DEADEND, CardPath.ROCK, H_CONSTANT_PATH));
					cardNum++;
				}
				//initialize turn left dead-end pathway cards
				cardTtl = 1;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.ROCK, CardPath.ROCK, CardPath.DEADEND, CardPath.DEADEND, H_CONSTANT_PATH));
					cardNum++;
				}
				//initialize turn right dead-end pathway cards
				cardTtl = 1;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.ROCK, CardPath.DEADEND, CardPath.DEADEND, CardPath.ROCK, H_CONSTANT_PATH));
					cardNum++;
				}
				//initialize south (north) dead-end pathway cards
				cardTtl = 1;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.ROCK, CardPath.ROCK, CardPath.DEADEND, CardPath.ROCK, H_CONSTANT_PATH));
					cardNum++;
				}
				//initialize west (east) dead-end pathway cards
				cardTtl = 1;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardPath(cardNum, cardType, CardPath.ROCK, CardPath.ROCK, CardPath.ROCK, CardPath.DEADEND, H_CONSTANT_PATH));
					cardNum++;
				}
						
				//=====================================
				//initialize all map cards
				cardTtl = 6;
				cardType = CardPlayable.MAP_CARD;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.NOT_USED, CardAction.NOT_USED, CardAction.NOT_USED, H_CONSTANT_MAP));
					cardNum++;
				}
				//=====================================
				//initialize all rock-fall cards

				cardTtl = 3;
				cardType = CardPlayable.ROCKFALL_CARD;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.NOT_USED, CardAction.NOT_USED, CardAction.NOT_USED, H_CONSTANT_ROCKFALL));
					cardNum++;
				}
				//=====================================
				//initialize all repair cards
				cardTtl = 2;
				cardType = CardPlayable.REPAIR_CARD;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.REPAIR, CardAction.NOT_USED, CardAction.NOT_USED, H_CONSTANT_REPAIR));
					cardNum++;
				}
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.NOT_USED, CardAction.REPAIR, CardAction.NOT_USED, H_CONSTANT_REPAIR));
					cardNum++;
				}
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.NOT_USED, CardAction.NOT_USED, CardAction.REPAIR, H_CONSTANT_REPAIR));
					cardNum++;
				}
				cardTtl = 1;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.REPAIR, CardAction.REPAIR, CardAction.NOT_USED, H_CONSTANT_REPAIR));
					cardNum++;
				}
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.REPAIR, CardAction.NOT_USED, CardAction.REPAIR, H_CONSTANT_REPAIR));
					cardNum++;
				}
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.NOT_USED, CardAction.REPAIR, CardAction.REPAIR, H_CONSTANT_REPAIR));
					cardNum++;
				}
				//=====================================
				//initialize all block cards
				cardTtl = 3;
				cardType = CardPlayable.BLOCK_CARD;
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.BLOCK, CardAction.NOT_USED, CardAction.NOT_USED, H_CONSTANT_BLOCK));
					cardNum++;
				}
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.NOT_USED, CardAction.BLOCK, CardAction.NOT_USED, H_CONSTANT_BLOCK));
					cardNum++;
				}
				for(int i=0; i<cardTtl; i++)
				{
					deck.add(new CardAction(cardNum, cardType, CardAction.NOT_USED, CardAction.NOT_USED, CardAction.BLOCK, H_CONSTANT_BLOCK));
					cardNum++;
				}
				
				//shuffle the deck
				Collections.shuffle(deck);				
	}
	/**
	 * initialize the deck of gold cards
	 * @throws IOException when the image for the card cannot be found (possibly because of wrong parameters in initializing the cards)
	 */
	private void initializeGoldDeck() throws IOException
	{
		//initialize gold cards
		goldDeck = new ArrayList<CardGold>();				
		
		int ttlCards=4;
		for(int i=0; i<ttlCards; i++)		
			goldDeck.add(new CardGold(3));
		
		ttlCards = 8;
		for(int i=0; i<ttlCards; i++)		
			goldDeck.add(new CardGold(2));
		
		ttlCards = 16;
		for(int i=0; i<ttlCards; i++)		
			goldDeck.add(new CardGold(1));
		
		//shuffle the deck
		Collections.shuffle(goldDeck);
	}

	/**
	 * set the card to be played by the player currently in turn
	 */
	public void setSelectedCard(CardPlayable card)
	{
		//change the selected card
		if(selectedCard != null)
		{
			//if not null, there is a list of possible targets already. Change their state 
			try
			{				
				if(selectedCard.isBoardCard())								
					target.addAll(findPossibleMovesOnBoard(selectedCard));				
				else
					target.addAll(findPossibleMovesOnPlayers(players.get(idxCurrentPlayer), selectedCard));
				
				for(int i=0; i<target.size(); i++)
					target.get(i).setSelectable(false);	
				target.clear();
			}
			catch(Exception ex){
				ex.printStackTrace();}
			selectedCard.setSelected(false);
		}
		
		if(card != null)		
		{
			selectedCard = card;
			selectedCard.setSelected(true);							
			
			//send an update to its Observer which is the possible Target to be selected for the selected card
			try
			{						
				if(selectedCard.isBoardCard())								
					target.addAll(findPossibleMovesOnBoard(selectedCard));				
				else
					target.addAll(findPossibleMovesOnPlayers(players.get(idxCurrentPlayer), selectedCard));
				
				for(int i=0; i<target.size(); i++)
					target.get(i).setSelectable(true);
			}
			catch(Exception ex){
				ex.printStackTrace();}
		}			
	}
	
	public boolean getIsFinished(){
		return isFinished;
	}
	
	public void setIsFinished(boolean isFinished){
		this.isFinished = isFinished;
	}
	
	/**
	 * get the card currently selected to be played by the player currently in turn 
	 * @return a PlayableCard object to be played
	 */
	public CardPlayable getSelectedCard()
	{
		return selectedCard;
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {		
		//observing whether a playable card has been selected or not	
		
	}

	public ArrayList<PlayerVisibleStatus> getPlayersVisibleStatus() {
		return playersVisibleStatus;
	}

	public void setPlayersVisibleStatus(ArrayList<PlayerVisibleStatus> playersVisibleStatus) {
		this.playersVisibleStatus = playersVisibleStatus;
	}
}
