import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
/**
 * A class specially made to display one card on the interface
 * This class observes:<p> 
 * - Cell (to update the cell's image when a card is put on)<p>
 * - Card (to update the cell's image when a card is rotated or opened)<p>
 * 
 * @author DATACLIM-PIKU2
 *
 */
public class PnlCard extends SelectablePanel implements Observer{
	/**card to be drawn in this panel */
	private Card card;	
		
	/**
	 * constructor that accepts a card to be displayed in this panel
	 * the size of the panel is defined by client, then the card will be
	 * resized accordingly without maintaining aspect ratio
	 * @param card the card to be shown in this panel
	 * @param width the width of the panel
	 * @param height the hight of the panel
	 */
	public PnlCard(Card card, int width, int height)
	{
		this.card = card;
		setSize(width, height);
		setBorder(new LineBorder(Color.BLACK));
	}
	
	/**
	 * constructor that will create an empty panel
	 * the size of the panel is defined by client, then the card will be
	 * resized accordingly without maintaining aspect ratio
	 * @param card the card to be shown in this panel
	 * @param width the width of the panel
	 * @param height the hight of the panel
	 */
	public PnlCard(int width, int height)
	{
		this(null, width, height);		
	}
	
	/**
	 * put a card on this panel, or remove it by giving null as the argument
	 * @param card the card to be put on this panel. Gives null to remove a card from this panel
	 */
	public void putCard(Card card)
	{
		this.card = card;
		repaint();
		revalidate();
	}	
	
		
	/**
	 * get whether this panel is filled with card or not
	 * @return true if there is a card put on this panel, false otherwise
	 */
	public boolean isFilled()
	{
		return (card != null);
	}
	
	/**
	 * paint the panel along with the card it contains
	 */
	public void paintComponent(Graphics g)
	{		
		//call parent's method
		super.paintComponents(g);
					
		//draw the card
		if(card != null)
			g.drawImage(card.getImage(), 0, 0, getSize().width, getSize().height, null);		
	}

	@Override
	/**
	 * get the preferred size of this panel, which is the same as this panel's normal size
	 */
	public Dimension getPreferredSize()
	{
		super.getPreferredSize();
		return getSize();
	}		
	
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub				
		repaint();
		revalidate();
	}	
}
