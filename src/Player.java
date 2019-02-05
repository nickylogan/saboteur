import java.util.ArrayList;
import java.util.Observable;

public class Player extends SelectableObject{
	private ArrayList<CardGold> goldCards;
	private ArrayList<CardPlayable> cards;
	private int number;
	private int role;
	private int ttlBlockCart;
	private int ttlBlockLight;
	private int ttlBlockPickaxe;
	private int statGoal1;
	private int statGoal2;
	private int statGoal3;	
	private Prediction prediction[];
	
	private boolean isAi;
	
	public static final int SABOTEUR = 0;
	public static final int GOLD_MINER = 1;
	
	/**
	 * initialize the player according to the given player's number and role
	 * @param number the player's number
	 * @param role the player's role, 0 means a saboteur, 1 means a goldminer
	 */
	public Player(int number, int role)
	{
		//initialize the number and role of the player based on 
		this.number = number;
		this.role = role;
				
		//initialize other states to default value
		isAi = false;
		ttlBlockCart = 0;
		ttlBlockLight = 0;
		ttlBlockPickaxe = 0;
		statGoal1 = 0;
		statGoal2 = 0;
		statGoal3 = 0;
		cards = new ArrayList<CardPlayable>();
		goldCards = new ArrayList<CardGold>();
	}
	
	/**
	 * set this player to be controlled by ai
	 * @param allPlayers all players participating in the game
	 */
	public void setAi(ArrayList<Player> allPlayers)
	{
		//initialize prediction
		prediction = new Prediction[allPlayers.size()-1];
		for(int i=0; i<allPlayers.size() ;i++)
		{
			if(!equals(allPlayers.get(i)))
				prediction[i] = new Prediction(allPlayers.get(i));
		}				
		isAi = true;
		setChanged();
		notifyObservers(this);
	}

	/**
	 * get whether this player has been set to be controlled by computer or not
	 * @return true if this player has been set to be controlled by computer (by calling setAi()), false otherwise
	 */
	public boolean isAi()
	{	
		return isAi;
	}
	
	/**
	 * set the parameter of this player regarding whether this player knows that the top goal card contains gold / stone
	 * @param isGold a boolean value indicating whether the top goal card (located at cell (7,0)) contains gold or not 
	 */
	public void setTopGoal(boolean isGold)
	{
		statGoal1 = (isGold)?1:-1;
		//notify observer
		setChanged();
		notifyObservers(this);
	}
	/**
	 * set the parameter of this player regarding whether this player knows that the middle goal card contains gold / stone
	 * @param isGold a boolean value indicating whether the middle goal card (located at cell (7,2)) contains gold or not 
	 */
	public void setMiddleGoal(boolean isGold)
	{
		statGoal2 = (isGold)?1:-1;

		//notify observer
		setChanged();
		notifyObservers(this);
	}
	/**
	 * set the parameter of this player regarding whether this player knows that the bottom goal card contains gold / stone
	 * @param isGold a boolean value indicating whether the bottom goal card (located at cell (7,4)) contains gold or not 
	 */
	public void setBottomGoal(boolean isGold)
	{
		statGoal3 = (isGold)?1:-1;
		
		//notify observer
		setChanged();
		notifyObservers(this);
	}
	
	/**
	 * add a card into the collection owned by a player
	 * @param card the card to be added into the player's collection
	 */
	public void addCard(CardPlayable card)
	{
		cards.add(card);
		setChanged();
		notifyObservers(card);
	}
	
	/**
	 * play the card on the player's hand at the selected index 
	 * which will remove that card from the hand
	 * @param index the index of the card to be played on the player's hand
	 * @return the card at the selected index
	 * @throws Exception when the index is smaller than 0 or larger than the total number of cards in hand 
	 */
	public Card playCard(int index) throws Exception
	{
		//check the index
		if(index < 0 || index >= cards.size())
			throw new Exception("Action Error: the index given is not among the cards in the player's hand.");
		
		//get the card, then remove it from the player's hand
		Card temp = cards.get(index);
		cards.remove(index);
		setChanged();
		notifyObservers(temp);
		return temp;
	}
	
	/**
	 * play the given card on the player's hand  
	 * which will remove that card from the hand
	 * @param index the index of the card to be played on the player's hand
	 * @return the card at the selected index
	 * @throws Exception when the index is smaller than 0 or larger than the total number of cards in hand 
	 */
	public Card playCard(CardPlayable card) throws Exception
	{
		//check the index
		if(!cards.contains(card))
			throw new Exception("Action Error: the card is not in the player's hand.");
		
		//get the card, then remove it from the player's hand		
		cards.remove(card);
		setChanged();
		notifyObservers(card);
		return card;
	}
	
	/**
	 * get the card object (shallow copy) held by the player at the specified index 
	 * @param index the index of the card in the player's hand
	 * @return the card object (shallow copy) held by the player at the specified index (if index is between 0 and total cards in hand), null otherwise
	 */
	public CardPlayable getCard(int index)
	{
		if(index >= 0 && index < cards.size())
			return cards.get(index);
		else
			return null;
	}
	
	/**
	 * get the total number of cards held by this player
	 * @return the total number of cards held by this player
	 */
	public int getTtlCards()
	{
		return cards.size();
	}
	
	/**
	 * apply a non-board card to a player (either block or repair card)
	 * @param card the non-board card object to be played against this character
	 */
	public void applyCard(CardAction nonBoardCard)
	{
		double[] cardAttributes = nonBoardCard.getAttributeVector();
		ttlBlockCart += cardAttributes[2];
		ttlBlockLight += cardAttributes[3];
		ttlBlockPickaxe += cardAttributes[4];
		ttlBlockCart = (ttlBlockCart < 0)? 0 : ttlBlockCart;
		ttlBlockLight = (ttlBlockLight < 0)? 0 : ttlBlockLight;
		ttlBlockPickaxe = (ttlBlockPickaxe < 0)? 0 : ttlBlockPickaxe;
		
		//notify observer
		setChanged();
		notifyObservers(this);
	}	
	
	/**
	 * get the total number of block ore-cart cards played against this player
	 * @return the total number of block ore-cart cards played against this player
	 */
	public int getTtlBlockOreCart()
	{
		return ttlBlockCart;
	}
	/**
	 * get the total number of block light cards played against this player
	 * @return the total number of block light cards played against this player
	 */
	public int getTtlBlockLight()
	{
		return ttlBlockLight;
	}
	/**
	 * get the total number of block pickaxe cards played against this player
	 * @return the total number of block pickaxe cards played against this player
	 */
	public int getTtlBlockPickaxe()
	{
		return ttlBlockPickaxe;
	}
	/**
	 * get the total number of block cards played against this player
	 * @return the total number of block cards played against this player
	 */
	public int getTtlBlocks()
	{
		return getTtlBlockOreCart() + getTtlBlockLight() + getTtlBlockPickaxe();
	}
	
	/**
	 * get the total number of golds owned by this player by summing the number of golds in each gold card owned
	 * @return an integer showing the total number of golds owned by this player
	 */
	public int getTtlGolds()
	{
		int ttlGolds = 0;
		if(!goldCards.isEmpty())
		{
			for(int i=0; i<goldCards.size(); i++)
				ttlGolds += goldCards.get(i).getTtlGolds();
		}
		return ttlGolds;
	}
	
	/**
	 * return the attributes of a player in row vector format with the following orders: <p>
	 * 0: the player's number<p>
	 * 1: the role of the player, 0 being a saboteur and 1 being a goldminer<p>
	 * 2: the number of ore-cart block cards on this player<p>
	 * 3: the number of light block cards on this player<p>
	 * 4: the number of pickaxe block cards on this player<p>
	 * 5: the state of the goal card at cell (8,0)<p>
	 * 6: the state of the goal card at cell (8,2)<p>
	 * 7: the state of the goal card at cell (8,4)<p>
	 * for goal card's state, -1 means it's a rock, 1 a gold, and 0 means it's not known yet<p>
	 * @return a row vector containing all the attributes of a player as stated above
	 */
	public int[] getAttributes()
	{
		int[] attributes = new int[8];
		attributes[0] = number;		
		attributes[1] = role;
		attributes[2] = ttlBlockCart;
		attributes[3] = ttlBlockLight;
		attributes[4] = ttlBlockPickaxe;
		attributes[5] = statGoal1;
		attributes[6] = statGoal2;
		attributes[7] = statGoal3;
		return attributes;
	}

	/**
	 * get the total number this player peeked at the top goal card
	 * @return the total number this player peeked at the top goal card
	 */
	public int getTtlTopGoalPeeked()
	{
		return statGoal1;
	}
	/**
	 * get the total number this player peeked at the middle goal card
	 * @return the total number this player peeked at the middle goal card
	 */
	public int getTtlMiddleGoalPeeked()
	{
		return statGoal2;
	}
	/**
	 * get the total number this player peeked at the bottom goal card
	 * @return the total number this player peeked at the bottom goal card
	 */
	public int getTtlBottomGoalPeeked()
	{
		return statGoal3;
	}
	
	@Override
	/**
	 * Determine whether this player equals the given player based on their numbers
	 */
	public boolean equals(Object otherPlayer)
	{
		if(otherPlayer == null)
			return false;
		else if(!(otherPlayer instanceof Player))
			return false;
		else
		{
			Player temp = (Player) otherPlayer;
			return this.number == temp.number;
		}
	}
}
