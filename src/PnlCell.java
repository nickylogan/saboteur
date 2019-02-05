import java.awt.event.MouseEvent;
import java.util.Observable;

import javax.swing.JOptionPane;

public class PnlCell extends PnlCard {

	private int x;
	private int y;
	private GameModel model;
	
	public PnlCell(int x, int y, Card card, int width, int height, GameModel model) {
		super(card, width, height);
		this.x = x;
		this.y = y;
		this.model = model;
	}
	public PnlCell(int x, int y, int width, int height, GameModel model)
	{
		this(x, y, null, width, height, model);		
	}
	
	/*public void mouseEntered(MouseEvent arg0)
	{
		System.out.println(model.getBoard().getCell(x, y).countObservers());
		System.out.println(getCard());
	}*/
	
	@Override	
	public void mouseClicked(MouseEvent arg0) 
	{				
		if(isSelectable())
		{
			try
			{
				//if a board card has been selected
				CardPlayable selectedCard = model.getSelectedCard();
				if(selectedCard.isBoardCard())				
					model.playCardOnBoard(selectedCard, model.getBoard().getCell(x, y));					
				selectedCard = null;
			}catch(Exception ex)
			{
				JOptionPane.showMessageDialog(null, ex.getMessage(), "Action Error", JOptionPane.ERROR_MESSAGE);
				//ex.printStackTrace();
			}
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
			else if(arg1 instanceof Cell)
				setSelectable(((Cell) arg1).isSelectable());
		}
		
		
		repaint();
		revalidate();
	}	
}
