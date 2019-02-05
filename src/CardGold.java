import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CardGold extends Card{
	/** the total number of golds contained in this card*/
	private int ttlGolds;
	
	/**
	 * 
	 * @param ttlGolds
	 * @throws Exception
	 * @throws IOException
	 */
	public CardGold(int ttlGolds) throws IOException
	{
		super(findOpenedImage(ttlGolds), findClosedImage());
		this.ttlGolds = ttlGolds;				
	}
	
	/**
	 * get the total number of golds contained in this card
	 * @return the total number of golds in this card
	 */
	public int getTtlGolds()
	{
		return ttlGolds;
	}
		
	/**
	 * find the back image of a gold card
	 * @return a BufferedImage containing the back image of a gold card
	 * @throws IOException When the image cannot be found on the specified folder (folder "res" in the root folder of this program)
	 */
	private static BufferedImage findClosedImage() throws IOException
	{
		BufferedImage imgClosed = null;
		imgClosed = ImageIO.read(new File("res/ImgGold/BackCardGold.png"));
		return imgClosed;
	}
	/**
	 * find the front image of a gold card based on the total number of golds given
	 * @param ttlGolds the total number of golds given
	 * @return a BufferedImage containing the front image of a gold card
	 * @throws IOException When the image cannot be found on the specified folder (folder "res" in the root folder of this program)
	 */
	private static BufferedImage findOpenedImage(int ttlGolds) throws IOException
	{
		BufferedImage imgOpened = null;
		switch(ttlGolds)
		{
		case 1:
			imgOpened = ImageIO.read(new File("res/ImgGold/CardGoldOne.png"));
			break;
		case 2:
			imgOpened = ImageIO.read(new File("res/ImgGold/CardGoldTwo.png"));
			break;
		case 3:
			imgOpened = ImageIO.read(new File("res/ImgGold/CardGoldThree.png"));
			break;
		default:
			throw new IOException("Initialization error: a suitable gold card image cannot be found for the given number of golds");
		}
		
		return imgOpened;
	}
}
