import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.Observable;

import javax.swing.border.LineBorder;

public class PnlCardPlayer extends PnlCard {

	/** the variable used to indicate whether this panel is selected to be played*/
	private boolean isSelected;
	
	/** the model of the game*/
	private GameModel model;
	
	private final LineBorder BORDER_SELECTED = new LineBorder(Color.WHITE, 5);
			
	public PnlCardPlayer(CardPlayable card, int width, int height, GameModel model) {
		super(card, width, height);
		setSelected(false);
		this.model = model;		
	}	
	public PnlCardPlayer(int width, int height, GameModel model) {
		this(null, width, height, model);		
	}
	
	public void update(Observable arg0, Object arg1) 
	{
		super.update(arg0, arg1);
		
		//observing whether a playable card has been selected
		if(arg0 instanceof CardPlayable)
		{						
			//if the one triggering the update
			setSelectable(getCard().isSelectable());
			setSelected(getCard().isSelected());			
		}
	}
	
	/**
	 * set whether this panel is currently selected or not
	 * @param isSelected a boolean value indicating whether this panel is currently selected (true) or not (false)
	 */
	public void setSelected(boolean isSelected)
	{
		this.isSelected = isSelected;
		if(isSelected)
			setBorder(BORDER_SELECTED);
		else if(isSelectable())
			setBorder(BORDER_SELECTABLE);
		else
			setBorder(BORDER_NORMAL);
	}
	/**
	 * get whether this panel is currently selected or not
	 * @return true if this panel is currently selected, false otherwise
	 */
	public boolean isSelected()
	{
		return isSelected;		
	}
	
	
	
	
//	public void rotateCard(){
//		
//	}
//	
	@Override	
	public void mouseClicked(MouseEvent arg0) 
	{				
		if(isSelectable())
		{
			if(arg0.getButton() == MouseEvent.BUTTON1)
			{
				if(arg0.getClickCount() >= 2)
					try {
						model.discardCard((CardPlayable)getCard());
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				else
					model.setSelectedCard((CardPlayable)getCard());
			}
			else if(arg0.getButton() == MouseEvent.BUTTON3)
				if(getCard() instanceof CardPath)
					getCard().rotate();
		}
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(isSelectable() && !isSelected())
		{
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			borderHovered();
		}
	}	
	@Override
	public void mouseExited(MouseEvent arg0)
	{
		if(isSelectable() && !isSelected())
		{
			borderSelectable();
		}		
	}
	
	public void borderHovered(){
		this.setBorder(BORDER_HOVERED);
	}
	
	public void borderSelectable(){
		setBorder(BORDER_SELECTABLE);
	}
	
	public void borderSelected(){
		setBorder(BORDER_SELECTED);
	}
}
