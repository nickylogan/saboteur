import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CardAction extends CardPlayable {
	
	public static final int BLOCK = 1;
	public static final int REPAIR = -1;
	public static final int NOT_USED = 0;
	
	/**
	 * Constructor to create an action card
	 * @param cardNum the card's number
	 * @param cardType the card's type, from 1-6 where 3 means map, 4 means rock-fall, 5 means block, and 6 means repair card 
	 * @param x2 must be NOT_USED for map and rock-fall card, BLOCK for block cart card, and REPAIR for repair card which can nullify block cart
	 * @param x3 must be NOT_USED for map and rock-fall card, BLOCK for block light card, and REPAIR for repair card which can nullify block light
	 * @param x4 must be NOT_USED for map and rock-fall card, BLOCK for block pickaxe card, and REPAIR for repair card which can nullify block pickaxe
	 * @param hConstant a positive constant used to determine the heuristic value of a card
	 * @param imgOpened a BufferedImage object containing the card's front image
	 * @param imgClosed a BufferedImage object containing the card's closed image
	 */
	public CardAction(int cardNum, int cardType, int x2, int x3, int x4, double hConstant) throws IOException
	{
		super(cardNum, cardType, x2, x3, x4, NOT_USED, hConstant, findOpenedImage(cardType, x2, x3, x4), findClosedImage());							
	}
	
	/**
	 * find the back image of a path card
	 * @return a BufferedImage containing the back image of a path card
	 * @throws IOException When the image cannot be found on the specified folder (folder "res" in the root folder of this program)
	 */
	private static BufferedImage findClosedImage() throws IOException
	{
		BufferedImage imgClosed = null;
		imgClosed = ImageIO.read(new File("res/ImgAction/BackCardAction.png"));
		return imgClosed;
	}
	/**
	 * find the front image of an action card based on the given properties
	 * @param cardType the card's type whose value defined by static constants in Card class
	 * @param x2 must be NOT_USED for map and rock-fall card, BLOCK for block cart card, and REPAIR for repair card which can nullify block cart
	 * @param x3 must be NOT_USED for map and rock-fall card, BLOCK for block light card, and REPAIR for repair card which can nullify block light
	 * @param x4 must be NOT_USED for map and rock-fall card, BLOCK for block pickaxe card, and REPAIR for repair card which can nullify block pickaxe
	 * @param x5 not used in action card, which means it will always have to be NOT_USED
	 * @return a BufferedImage containing the front image of a path card
	 * @throws IOException When the image cannot be found on the specified folder (folder "res" in the root folder of this program)
	 */
	private static BufferedImage findOpenedImage(int cardType, int x2, int x3, int x4) throws IOException
	{
		BufferedImage imgOpened = null;
	
		//===========================
		//======dead-end cards=======
		//===========================
		//map		
		if(cardType == MAP_CARD  && x2 == NOT_USED && x3 == NOT_USED && x4 == NOT_USED)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardMap.png"));		
		//rock-fall
		else if(cardType == ROCKFALL_CARD  && x2 == NOT_USED && x3 == NOT_USED && x4 == NOT_USED)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardRockfall.png"));
		//block cart 
		else if(cardType == BLOCK_CARD  && x2 == BLOCK && x3 == NOT_USED && x4 == NOT_USED)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardBlockCart.png"));	
		//block light
		else if(cardType == BLOCK_CARD  && x2 == NOT_USED && x3 == BLOCK && x4 == NOT_USED)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardBlockLight.png"));
		//block pickaxe
		else if(cardType == BLOCK_CARD  && x2 == NOT_USED && x3 == NOT_USED && x4 == BLOCK)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardBlockPickaxe.png"));
		
		//repair cart
		else if(cardType == REPAIR_CARD  && x2 == REPAIR && x3 == NOT_USED && x4 == NOT_USED)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardRepairCart.png"));		
		//repair light
		else if(cardType == REPAIR_CARD  && x2 == NOT_USED && x3 == REPAIR && x4 == NOT_USED)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardRepairLight.png"));
		//repair pickaxe
		else if(cardType == REPAIR_CARD  && x2 == NOT_USED && x3 == NOT_USED && x4 == REPAIR)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardRepairPickaxe.png"));
		
		//repair cart & light
		else if(cardType == REPAIR_CARD  && x2 == REPAIR && x3 == REPAIR && x4 == NOT_USED)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardRepairCartLight.png"));		
		//repair cart & pickaxe
		else if(cardType == REPAIR_CARD  && x2 == REPAIR && x3 == NOT_USED && x4 == REPAIR)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardRepairCartPickaxe.png"));		
		//repair light & pickaxe
		else if(cardType == REPAIR_CARD  && x2 == NOT_USED && x3 == REPAIR && x4 == REPAIR)
			imgOpened = ImageIO.read(new File("res/ImgAction/CardRepairLightPickaxe.png"));		
		
		else
			throw new IOException("Initialization error: a suitable action card image cannot be found for the given properties");
		
		return imgOpened;
	}
	
	
	
}
