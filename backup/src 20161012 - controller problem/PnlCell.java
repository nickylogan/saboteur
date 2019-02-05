import java.awt.event.MouseEvent;
import java.util.Observable;

public class PnlCell extends PnlCard {

	public PnlCell(Card card, int width, int height) {
		super(card, width, height);
	}
	public PnlCell(int width, int height)
	{
		super(null, width, height);		
	}
	
	@Override	
	public void mouseClicked(MouseEvent arg0) 
	{		
		if(isSelectable())
		{
			this.setBorder(BORDER_HOVERED);		
		}
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		//observing whether a cell has been filled with a card
		//put the given object (which is a card) 
		if(arg0 instanceof Cell)
		{
			if(arg1  == null || arg1 instanceof Card)
				putCard((Card)arg1);
		}		
		repaint();
		revalidate();
	}	
}
