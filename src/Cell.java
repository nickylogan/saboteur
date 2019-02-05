import java.io.IOException;
import java.util.Observable;

/**
 * 
 * Observer of this class: Board, PnlCard
 * @author DATACLIM-PIKU2
 *
 */
public class Cell extends SelectableObject{
	private CardPath card;
	private int x;
	private int y;
	private boolean isFilled;
	private int topState;
	private int rightState;	
	private int bottomState;
	private int leftState;	
	
	public static final int CONNECTED_NEIGHBOR = 2;
	public static final int EMPTY_NEIGHBOR = -1;
	
	/**
	 * Constructor to initialize a cell to its default state
	 */
	public Cell(int x, int y)
	{
		this.x = x;
		this.y = y;
		card = null;
		isFilled = false;
		topState = EMPTY_NEIGHBOR;
		rightState = EMPTY_NEIGHBOR;
		bottomState = EMPTY_NEIGHBOR;
		leftState = EMPTY_NEIGHBOR;
	}
	
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	public int getTopState()
	{
		return topState;
	}
	public int getBottomState()
	{
		return bottomState;
	}
	public int getLeftState()
	{
		return leftState;
	}
	public int getRightState()
	{
		return rightState;
	}
	public boolean isFilled()
	{
		return isFilled;
	}
	
	/**
	 * get all attributes of this cell in a vector form. 
	 * @return an array of integer consisting all the attributes of this cell in the following order <p>
	 * 0: the x-coordinate of this cell on its respective board <p>
	 * 1: the y-coordinate of this cell on its respective board <p>
	 * 2: the existence (state) of pathway on the top of this cell <p>
	 * 3: the existence (state) of pathway on the right of this cell <p>
	 * 4: the existence (state) of pathway on the bottom of this cell <p>
	 * 5: the existence (state) of pathway on the left of this cell <p>
	 * 6: 1 if there is a card on this cell, 0 otherwise <p>
	 */
	public int[] getAttributes()
	{
		int[] attributes = new int[7];
		attributes[0] = x;
		attributes[1] = y;
		attributes[2] = topState;
		attributes[3] = rightState;
		attributes[4] = bottomState;
		attributes[5] = leftState;
		attributes[6] = (isFilled)? 1 : 0;
		return attributes;
	}
	
	/**
	 * check whether this cell is available for the player to put a card on
	 * @return true if this cell is available for the player to put a card on, false otherwise
	 */
	public boolean isReachable()
	{
		if((getTopState() == CardPath.PATH || getTopState() == CardPath.DEADEND) && !isFilled())
			return true;
		else if((getRightState() == CardPath.PATH || getRightState() == CardPath.DEADEND) && !isFilled())
			return true;
		else if((getTopState() == CardPath.PATH || getTopState() == CardPath.DEADEND) && !isFilled())
			return true;
		else if((getTopState() == CardPath.PATH || getTopState() == CardPath.DEADEND) && !isFilled())
			return true;
		else return false;
	}
		
	/**
	 * get a shallow copy of the path card put on this cell
	 * @return a shallow copy of the path card on this cell, null if no card has been put on
	 */
	public CardPath getCard()	
	{		
		return this.card;		
	}
	
	/**
	 * open the card on this cell (if there exist)
	 */
	public void openCard()
	{
		if(isFilled)
			card.open();
	}
	
	/**
	 * put a car on this cell <p>
	 * give null value to remove the card on this cell
	 * @param card a card object to be placed on this cell, or null to remove the card on this panel
	 * @throws Exception when try to put a card to an already-filled cell
	 */
	public void putCard(CardPath card) throws Exception
	{			
		//case where a card is removed from this cell
		if(card == null) 
		{
			
			//check whether the cell is filled
			if(!isFilled)
				throw new Exception("Action error: cell is empty thus can't be destroyed");		
			
		}
		//case where a card is put on this cell
		else 
		{
			//check whether the cell is empty
			if(isFilled)
				throw new Exception("Action error: cell is already filled with a card!");
			
			//check whether the given card can be put on this cell or not
			int[] cellState = getAttributes();
			int[] cardState = {(int)card.getAttributeVector()[0], (int)card.getAttributeVector()[1], (int)card.getAttributeVector()[2], (int)card.getAttributeVector()[3], 
					(int)card.getAttributeVector()[4], (int)card.getAttributeVector()[5], (int)card.getAttributeVector()[6], (int)card.getAttributeVector()[7]};
			for(int i = 2; i <= 6; i++)
			{
				if(!((cellState[i] + cardState[i] == -4) || cellState[i] == -1 || (cellState[i]+cardState[i] >= 0)))
					throw new Exception("Action error: the path card can't be put on the selected cells. <p>Their side(s) didn't match !");
			}									
		}	
		//put the card
		this.card = card;		
		isFilled = (card != null);
		setChanged();
		notifyObservers(card);
	}	
	/**
	 * update the states of the cell according to the values given (which is determined by the board)
	 */
	public void updateStates(int topState, int rightState, int bottomState, int leftState) throws Exception
	{
		if(!isStateValid(topState) || !isStateValid(rightState) || !isStateValid(bottomState) || !isStateValid(leftState))
			throw new Exception("Assignment error: the value given for the cells' states are not valid!");
		this.topState = topState;
		this.bottomState = bottomState;
		this.rightState = rightState;
		this.leftState = leftState;
	}
	/**
	 * check the validity of the value given as a state of this cell 
	 * @param state the value representing the state of this cell
	 * @return true if the value is valid, false otherwise
	 */
	private boolean isStateValid(int state)
	{
		switch(state)
		{
		case CONNECTED_NEIGHBOR: case EMPTY_NEIGHBOR: case CardPath.ROCK: case CardPath.PATH: case CardPath.DEADEND:
			return true;
		default:
			return false;
				
		}
	}
			
	@Override
	/**
	 * Determine whether this cell equals the given cell based on their x-y positions
	 */
	public boolean equals(Object otherCell)
	{
		if(otherCell == null)
			return false;
		else if(!(otherCell instanceof Cell))
			return false;
		else
		{
			Cell temp = (Cell) otherCell;
			return (this.x == temp.x && this.y == temp.y);
		}
	}
}
