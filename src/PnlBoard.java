import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

/**
 * This class observes: Cell (to update the cells when a card is put on)
 * @author DATACLIM-PIKU2
 *
 */
public class PnlBoard extends JPanel implements Observer{
	private PnlCell[][] cells;
	Board board;
	
	/**
	 * 
	 * @param width
	 * @param height
	 */
	public PnlBoard(Board board, int width, int height, GameModel model)
	{
		//set the layout to gridlayout
		super(new GridLayout(5,9));
		
		//initialize the board
		this.board = board;
		
		//initialize the panels for each cell
		cells = new PnlCell[Board.WIDTH][Board.HEIGHT];
		for(int i = 0; i<cells[0].length; i++)
		{
			for(int ii=0; ii<cells.length; ii++)
			{
				cells[ii][i] = new PnlCell(ii, i, FrmMain.PNL_CARD_WIDTH, FrmMain.PNL_CARD_HEIGHT, model);
				add(cells[ii][i]);
				
				//add observers to the board's cell which are this panel (pnlBoard) and panel for each card (pnlCard)
				board.addObserver(this);
				//board.addObserverToCell(this, ii, i);
				board.addObserverToCell(cells[ii][i], ii, i);
				
			}
		}
		
		
		//initialize the panel for start card 
		cells[0][2].putCard(board.getCell(0, 2).getCard());
		//initialize the panel for goal cards
		cells[Board.WIDTH-1][0].putCard(board.getGoalCells().get(Board.TOP_GOAL).getCard());
		cells[Board.WIDTH-1][2].putCard(board.getGoalCells().get(Board.MIDDLE_GOAL).getCard());
		cells[Board.WIDTH-1][4].putCard(board.getGoalCells().get(Board.BOTTOM_GOAL).getCard());
				
		setVisible(true);
		repaint();
		revalidate();
	}
	
	public PnlCell[][] getCells(){
		return cells;
	}
	
	public Dimension getPreferredSize()
	{
		return new Dimension(Board.WIDTH*FrmMain.PNL_CARD_WIDTH, Board.HEIGHT*FrmMain.PNL_CARD_HEIGHT);
	}
	
	
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		repaint();
		revalidate();
	}
}
