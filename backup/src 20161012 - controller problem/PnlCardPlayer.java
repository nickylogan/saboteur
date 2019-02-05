import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Observable;

import javax.swing.border.LineBorder;

public class PnlCardPlayer extends PnlCard {

	/** the variable used to indicate whether this panel is selected to be played*/
	private boolean isSelected;
		
	private final LineBorder BORDER_SELECTED = new LineBorder(Color.WHITE, 5);
	
	public PnlCardPlayer(Card card, int width, int height) {
		super(card, width, height);
		setSelected(false);
	}
	public PnlCardPlayer(int width, int height) {
		this(null, width, height);		
	}
	
	public void update(Observable arg0, Object arg1) 
	{
		super.update(arg0, arg1);
		//observing whether a playable card has been selected
				if(arg0 instanceof CardPlayable)
				{			
					if(((CardPlayable)arg1).isSelected())
					{
						
					}
					else
					{
						
					}
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
	
	@Override	
	public void mouseClicked(MouseEvent arg0) 
	{		
		if(isSelectable())
		{
			setSelected(true);
			
		}
	}
}
