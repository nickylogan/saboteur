import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Observable;

/**
 * 
 * @author DATACLIM-PIKU2
 *
 */
public abstract class Card extends Observable{
	/** the card's front image */
	private BufferedImage imgOpened;		 	
	/** a boolean value indicating whether the card is being rotated or not*/
	private boolean isRotated;
	/** a boolean value indicating whether the card is already opened or not*/
	private boolean isOpened;
	/** the card's back image*/
	private BufferedImage imgClosed;
	/** the variable used to indicate whether this card is selected to be played*/
	private boolean isSelected;
	
	
	/**
	 * Create a card with the given image
	 * @param imgOpened a BufferedImage object containing the card's front image
	 * @param imgClosed a BufferedImage object containing the card's closed image
	 */
	public Card(BufferedImage imgOpened, BufferedImage imgClosed)
	{
		this.imgOpened = imgOpened;
		this.imgClosed = imgClosed;
		isRotated = false;
		isOpened = false;
		
		//set the attributes of isSelected
		isSelected = false;
	}	
	
	/**
	 * Open the card
	 */
	public void open()
	{
		isOpened = true;
		
		//notify observers
		setChanged();
		notifyObservers();
	}

	/**
	 * Close the card
	 */
	public void close()
	{
		isOpened = false;
		
		//notify observers
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Rotate the card
	 */
	public void rotate()
	{
		isRotated = !isRotated;
		AffineTransform transform = new AffineTransform();
		transform.rotate(Math.PI, getImage().getWidth()/2, getImage().getHeight()/2);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		imgOpened = op.filter(imgOpened, null);
		imgClosed = op.filter(imgClosed, null);
		
		//notify observers
		setChanged();
		notifyObservers();
	}
	
	/**
	 * get the image of the card which depends on whether the card is in opened/closed condition
	 * @return a BufferedImage object containing the card's front image if the card is in opened condition, the card's back image otherwise
	 */
	public BufferedImage getImage()
	{
		if(isOpened)
			return imgOpened;
		else
			return imgClosed;
	}
	/**
	 * get the front image of the card 
	 * @return a BufferedImage object containing the card's front image
	 */
	public BufferedImage getOpenedImage()
	{
		return imgOpened;
	}
	/**
	 * get the back image of the card 
	 * @return a BufferedImage object containing the card's back image
	 */
	public BufferedImage getClosedImage()
	{
		return imgClosed;
	}
	/**
	 * get information about the orientation of this card
	 * @return true is it's rotated from its original orientation, false otherwise
	 */
	public boolean isRotated()
	{
		
		return isRotated;
	}
	/**
	 * get information whether this card is opened already or not 
	 * @return true if the card is opened, false otherwise
	 */
	public boolean isOpened()
	{
		return isOpened;
	}


	/**
	 * set the isSelected attribute to select or unselect this card
	 * @param isSelected true if this card is selected, false otherwise
	 */
	public void setSelected(boolean isSelected)
	{
		this.isSelected = isSelected;
		setChanged();
		notifyObservers();
	}	
	/**
	 * get whether this card is currently selected or not
	 * @return true if this card is currently selected, false otherwise
	 */
	public boolean isSelected()
	{
		return isSelected;
	}
	
}


