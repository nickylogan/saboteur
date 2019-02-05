import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * 
 * @author DATACLIM-PIKU2
 * Observe the following objects: <p>
 * - model (when updating the selected card)
 */
public class FrmMain extends JFrame implements Observer{
	private JTextArea txtPlayersMovement;
	private JPanel pnlDeck;	
	private PnlBoard pnlBoard;
	private PnlCard pnlPlayableDeck;
	private PnlCard pnlGoldDeck;
	private ScrollablePanel pnlPlayers;	
	private ArrayList<PnlPlayer> pnlPlayersDetail;	
	private JScrollPane scrPanePlayers;	
	
	private GameModel model;
	
	//need to store all the possible targets
	
	public static final int PNL_CARD_WIDTH = 60;
	public static final int PNL_CARD_HEIGHT = 100;
	
	public static final Font FONT_STATUS = new Font("Times New Romans", Font.PLAIN, 16);
	
	public FrmMain()
	{
		//set the main frame of the game
		super("Saboteur Game");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	
		setResizable(false);
		setSize(1280,720);
		setLayout(new BorderLayout());
		
		//initialize the model first, then 
		int ttlPlayers = 0;
		while(ttlPlayers == 0)
		{
			try{
				ttlPlayers = Integer.parseInt(JOptionPane.showInputDialog("Please enter the total number of players in this game"));
				model = new GameModel(ttlPlayers);
			}catch(NumberFormatException ex){
				JOptionPane.showMessageDialog(this, "Error In Initializing Players", "Please input a number for total number of players!", JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}				
		
		//set the txt which shown all players movement
		txtPlayersMovement = new JTextArea();		
		txtPlayersMovement.setEditable(false);		
		txtPlayersMovement.setRows(7);		
		txtPlayersMovement.setFont(FONT_STATUS);
		add(txtPlayersMovement, BorderLayout.SOUTH);
		
		//set the panel for the deck
		pnlDeck = new JPanel(new FlowLayout());
				
		//set the panel for the board
		pnlBoard = new PnlBoard(model.getBoard(), Board.WIDTH, Board.HEIGHT); 
		model.getBoard().addObserver(pnlBoard);
		
		//set the panel for each players
		pnlPlayersDetail = new ArrayList<PnlPlayer>();
		for(int i=0; i<ttlPlayers; i++)		
		{
			pnlPlayersDetail.add(new PnlPlayer(model.getPlayers().get(i)));
			model.getPlayers().get(i).addObserver(pnlPlayersDetail.get(i));
		}
				
		//set the panel for the players
		pnlPlayers = new ScrollablePanel();		
		pnlPlayers.setLayout(new BoxLayout(pnlPlayers, BoxLayout.PAGE_AXIS));
		pnlPlayers.setScrollableHeight(ScrollablePanel.ScrollableSizeHint.NONE);
		pnlPlayers.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.NONE);	
		for(int i=0; i<pnlPlayersDetail.size(); i++)
			pnlPlayers.add(pnlPlayersDetail.get(i));
		scrPanePlayers = new JScrollPane(pnlPlayers, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);		
				
		//add the board panel to the frame (by wrapping it with another panel so that it could be drawn with center-alignment of the y-axis
		JPanel pnlGame = new JPanel();
		BoxLayout layout = new BoxLayout(pnlGame, BoxLayout.PAGE_AXIS);
		pnlGame.setLayout(layout);		
		pnlGame.add(Box.createVerticalGlue());
		pnlGame.add(pnlBoard);
		pnlGame.add(Box.createVerticalGlue());
		add(pnlGame, BorderLayout.EAST);
		
		add(scrPanePlayers, BorderLayout.CENTER);	
		setVisible(true);
	}
	
	/**
	 * method used to set the GUI for the player currently in turn
	 */
	private void changeTurn()
	{
		//turn off the interface for the player just move
		int idxCurrentPlayer = model.getIdxCurrentPlayer();
		pnlPlayersDetail.get(idxCurrentPlayer).setInTurn(false);
		
		//turn on the interface for the player to move
		idxCurrentPlayer = (idxCurrentPlayer+1) % pnlPlayersDetail.size();
		pnlPlayersDetail.get(idxCurrentPlayer).setInTurn(true);		
	}

	
	
	public static void main(String[] args)
	{
		FrmMain main = new FrmMain();		
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		if(arg0 instanceof GameModel)
		{
			//if the model is updating the player's turn 
			if(arg1 instanceof Player)
				changeTurn();
			//if the model is updating the possible moves
			if(arg1 instanceof ArrayList)
			{
				
			}
		}
	}
}
