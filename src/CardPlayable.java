import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class CardPlayable extends Card{
			
	/** a positive constant used in determining the heuristic value of a card */
	private double hConstant;				
	/** an integer indicating the card's number (ID)*/
	private int cardNum;			
	/** an integer indicating the card's type, where 1 means a pathway card, 2 means a dead-end card*, 3 means a map card, 4 means a rock-fall card, 5 means a block card, and 6 means a repair card*/
	private int cardType;
	/** the first property of this card whose value depends on the card's type*/
	private int x2;
	/** the second property of this card whose value depends on the card's type*/
	private int x3;
	/** the third property of this card whose value depends on the card's type*/
	private int x4;
	/** the forth property of this card whose value depends on the card's type*/
	private int x5;
	
	
	public static final int PATHWAY_CARD = 1;
	public static final int DEADEND_CARD = 2;
	public static final int MAP_CARD = 3;
	public static final int ROCKFALL_CARD = 4;
	public static final int BLOCK_CARD = 5;
	public static final int REPAIR_CARD = 6;	
	
	public void setX2(int x2)
	{
		this.x2 = x2;
	}
	public void setX3(int x3)
	{
		this.x3 = x3;
	}
	public void setX4(int x4)
	{
		this.x4 = x4;
	}
	public void setX5(int x5)
	{
		this.x5 = x5;
	}
	
	/**
	 * Constructor to set all attributes of a card
	 * @param cardNum the card's number
	 * @param cardType the card's type, from 1-6 where 1 means pathway, 2 mean dead-end, 3 means map, 4 means rock-fall, 5 means block, and 6 means repair card 
	 * @param x2 the first property of the card, whose value depends on the value of cardType
	 * @param x3 the second property of the card, whose value depends on the value of cardType
	 * @param x4 the third property of the card, whose value depends on the value of cardType
	 * @param x5 the forth property of the card, whose value depends on the value of cardType
	 * @param hConstant a positive constant used to determine the heuristic value of a card
	 * @param imgOpened a BufferedImage object containing the card's front image
	 * @param imgClosed a BufferedImage object containing the card's closed image
	 */
	public CardPlayable(int cardNum, int cardType, int x2, int x3, int x4, int x5, double hConstant, BufferedImage imgOpened, BufferedImage imgClosed)
	{		
		//load the image
		super(imgOpened, imgClosed);
		
		//set the attributes of the card based on the arguments given		
		this.cardNum = cardNum;
		this.cardType = cardType;
		this.x2 = x2;
		this.x3 = x3;
		this.x4 = x4;
		this.x5 = x5;		
		this.hConstant = hConstant;			
	}
	
	/**
	 * get information whether this card is one played on the board or on a player
	 * @return a boolean value indicating whether this card can be played on the board or not (on a player)
	 */
	public boolean isBoardCard()
	{
		if(cardType>=1 && cardType<=4)
			return true;
		else
			return false;
	}
	
	/**
	 * get all attributes of a card in a row vector format (a 1D array)
	 * @return a 1D array containing all the attributes of a card in the following order:<p>
	 * 0: the card's number<p>
	 * 1: the card's type<p>
	 * 2: the first property of the card<p> 
	 * 3: the second property of the card <p>
	 * 4: the third property of the card <p>
	 * 5: the fourth property of the card <p>
	 * 6: is the card rotated (1) or not (0)<p>
	 * 7: the heuristic value of this card<p>
	 */
	public double[] getAttributeVector()
	{
		//set the attributes
		double[] attributes = new double[8];
		
		//set the attributes of the card based on the arguments given		
		attributes[0] = cardNum;
		attributes[1] = cardType;
		attributes[2] = x2;
		attributes[3] = x3;
		attributes[4] = x4;
		attributes[5] = x5;
		attributes[6] = (isRotated())? 1:0;
		attributes[7] = hConstant;

		return attributes;
	}
	
	@Override
	/**
	 * Determine whether this card equals the given card based on their card numbers
	 */
	public boolean equals(Object otherCardPlayable)
	{
		if(otherCardPlayable == null)
			return false;
		else if(!(otherCardPlayable instanceof CardPlayable))
			return false;
		else
		{
			CardPlayable temp = (CardPlayable) otherCardPlayable;
			return this.cardNum == temp.cardNum;
		}
	}
}
