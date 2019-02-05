import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CardPath extends CardPlayable{
	
	public static final int PATH = 1;
	public static final int ROCK = -2;
	public static final int DEADEND = 0;
	
	/**
	 * The constructor used to create a path card
	 * @param cardNum the card's number
	 * @param cardType the card's type, from 1-2 where 1 means pathway and 2 mean dead-end 
	 * @param x2 the first property of the card whose value indicating the type of path on the north direction (use static final member of this class for the allowed values)
	 * @param x3 the second property of the card whose value indicating the type of path on the east direction (use static final member of this class for the allowed values)
	 * @param x4 the third property of the card whose value indicating the type of path on the south direction (use static final member of this class for the allowed values)
	 * @param x5 the forth property of the card whose value indicating the type of path on the west direction (use static final member of this class for the allowed values)
	 * @param hConstant a positive constant used to determine the heuristic value of a card
	 */
	public CardPath(int cardNum, int cardType, int x2, int x3, int x4, int x5, double hConstant) throws IOException
	{
		super(cardNum, cardType, x2, x3, x4, x5, hConstant, findOpenedImage(cardNum, cardType, x2, x3, x4, x5), findClosedImage());	
		
	}
	
	public CardPath(CardPath otherCard) throws IOException
	{
		this((int)otherCard.getAttributeVector()[0], (int)otherCard.getAttributeVector()[1], (int)otherCard.getAttributeVector()[2],
				(int)otherCard.getAttributeVector()[3], (int)otherCard.getAttributeVector()[4],(int)otherCard.getAttributeVector()[5],
				otherCard.getAttributeVector()[7]);
	}
			
	/**
	 * rotate the card and change the other states if it is a pathway / dead-end card
	 */
	public void rotate()
	{
		//change the state of the card
		super.rotate();
		
		//if a pathway/dead-end card, then change the other states
		double[] attributes = getAttributeVector();
		
		if((int)attributes[1] == 1 || (int)attributes[1] == 2)
		{
			//swap the value of x2 (top state) with x4 (bottom state)
			setX4((int)attributes[2]);			
			setX2((int)attributes[4]);
			//swap the value of x3 (right state) with x5 (left state)
			setX3((int)attributes[5]);
			setX5((int)attributes[3]);			
		}		
		
		setChanged();
		notifyObservers();
	}
	
	/**
	 * find the back image of a path card
	 * @return a BufferedImage containing the back image of a path card
	 * @throws IOException When the image cannot be found on the specified folder (folder "res" in the root folder of this program)
	 */
	private static BufferedImage findClosedImage() throws IOException
	{
		BufferedImage imgClosed = null;
		imgClosed = ImageIO.read(new File("res/ImgPath/BackCardPath.png"));
		return imgClosed;
	}
	/**
	 * find the front image of a path card based on the given properties
	 * @param cardType the card's type whose value defined by static constants in Card class
	 * @param x2 the existence of path on the north direction
	 * @param x3 the existence of path on the east direction
	 * @param x4 the existence of path on the south direction
	 * @param x5 the existence of path on the west direction
	 * @return a BufferedImage containing the front image of a path card
	 * @throws IOException When the image cannot be found on the specified folder (folder "res" in the root folder of this program)
	 */
	private static BufferedImage findOpenedImage(int cardNum, int cardType, int x2, int x3, int x4, int x5) throws IOException
	{
		BufferedImage imgOpened = null;
	
		//===============================
		//======start & goal cards=======
		//===============================
		//start card
		if(cardNum == 0)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardStart.png"));
		//stone goal card
		else if(cardNum == 68)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardGoalStone.png"));
		else if(cardNum == 69)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardGoalStone.png"));
		else if(cardNum == 70)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardGoalGold.png"));
		//===========================
		//======dead-end cards=======
		//===========================
		//all directions
		else if(cardType == DEADEND_CARD && x2 == DEADEND && x3 == DEADEND && x4 == DEADEND && x5 == DEADEND)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardDeadendAll.png"));
		//horizontal 
		else if(cardType == DEADEND_CARD && x2 == ROCK && x3 == DEADEND && x4 == ROCK && x5 == DEADEND)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardDeadendHorizontal.png"));
		//vertical 
		else if(cardType == DEADEND_CARD && x2 == DEADEND && x3 == ROCK && x4 == DEADEND && x5 == ROCK)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardDeadendVertical.png"));
		//turn-left 
		else if(cardType == DEADEND_CARD && x2 == ROCK && x3 == ROCK && x4 == DEADEND && x5 == DEADEND)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardDeadendLeft.png"));
		//turn-right 
		else if(cardType == DEADEND_CARD && x2 == ROCK && x3 == DEADEND && x4 == DEADEND && x5 == ROCK)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardDeadendRight.png"));
		
		//horizontal T
		else if(cardType == DEADEND_CARD && x2 == ROCK && x3 == DEADEND && x4 == DEADEND && x5 == DEADEND)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardDeadendHorizontalT.png"));
		//vertical T
		else if(cardType == DEADEND_CARD && x2 == DEADEND && x3 == ROCK && x4 == DEADEND && x5 == DEADEND)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardDeadendVerticalT.png"));

		//south (or north)
		else if(cardType == DEADEND_CARD && x2 == ROCK && x3 == ROCK && x4 == DEADEND && x5 == ROCK)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardDeadendSouth.png"));
		//west (or east)
		else if(cardType == DEADEND_CARD && x2 == ROCK && x3 == ROCK && x4 == ROCK && x5 == DEADEND)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardDeadendWest.png"));

		//==========================
		//======pathway cards=======
		//==========================
		//all directions
		else if(cardType == PATHWAY_CARD && x2 == PATH && x3 == PATH && x4 == PATH && x5 == PATH)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardPathAll.png"));
		//horizontal 
		else if(cardType == PATHWAY_CARD && x2 == ROCK && x3 == PATH && x4 == ROCK && x5 == PATH)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardPathHorizontal.png"));
		//vertical 
		else if(cardType == PATHWAY_CARD && x2 == PATH && x3 == ROCK && x4 == PATH && x5 == ROCK)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardPathVertical.png"));
		//turn-left 
		else if(cardType == PATHWAY_CARD && x2 == ROCK && x3 == ROCK && x4 == PATH && x5 == PATH)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardPathLeft.png"));
		//turn-right 
		else if(cardType == PATHWAY_CARD && x2 == ROCK && x3 == PATH && x4 == PATH && x5 == ROCK)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardPathRight.png"));
		
		//horizontal T
		else if(cardType == PATHWAY_CARD && x2 == ROCK && x3 == PATH && x4 == PATH && x5 == PATH)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardPathHorizontalT.png"));
		//vertical T
		else if(cardType == PATHWAY_CARD && x2 == PATH && x3 == ROCK && x4 == PATH && x5 == PATH)
			imgOpened = ImageIO.read(new File("res/ImgPath/CardPathVerticalT.png"));
			
		else
		{
			//System.out.println(cardType + " " + x2 + " " +x3 + " " +x4 + " " +x5);
			throw new IOException("Initialization error: a suitable path card image cannot be found for the given properties");
		}
		
		return imgOpened;
	}

	/**
	 * check whether this card path is the start card 
	 * @return true if this card is the start card, false otherwise
	 */
	public boolean isStartCard()
	{
		return ((int)getAttributeVector()[0] == 0);
	}
	
	/**
	 * check whether this card path is a goal card 
	 * @return true if this card is a goal card, false otherwise
	 */
	public boolean isGoalCard()
	{
		return ((int)getAttributeVector()[0] == 68 || (int)getAttributeVector()[0] == 69 || (int)getAttributeVector()[0] == 70);
	}
	
	/**
	 * check whether this card path is the goal card which contains gold
	 * @return true if this card is the goal card which contains gold, false otherwise
	 */
	public boolean isGoldGoalCard()
	{
		return ((int)getAttributeVector()[0] == 70);
	}
}
