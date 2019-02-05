import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
//	private JPanel pnlDeck;
	private PnlBoard pnlBoard;
	JPanel pnlGame;
//	private PnlCard pnlPlayableDeck;
//	private PnlCard pnlGoldDeck;
	private ScrollablePanel pnlPlayers;	
	private ArrayList<PnlPlayer> pnlPlayersDetail;
	private JScrollPane scrPanePlayers;	
	private GameSimulator gameSimulator;
	private GameModel model;
	private JButton nextTurn;
	private int action = 0;
	
	//need to store all the possible targets
	
	public static final int PNL_CARD_WIDTH = 60;
	public static final int PNL_CARD_HEIGHT = 100;
	
	public static final Font FONT_STATUS = new Font("Times New Romans", Font.PLAIN, 28);
	
	public FrmMain()
	{
		//set the main frame of the game
		super("Saboteur Game");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyCode() == KeyEvent.VK_SPACE){
					if(action == 0){
						runGame(action);
						System.out.println("SPACE" + action);
						++action;
					}else if(action == 1){
						runGame(action);
						System.out.println("SPACE" + action);
						++action;
					}else{
						model.nextTurn();
						System.out.println("SPACE" + action);
						action = 0;
						model.getConsole().setText(model.getConsole().getText().toString().concat("\n"));
						System.out.println(model.getIdxCurrentPlayer());
					}
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		setFocusable(true);
		setResizable(false);
		setSize(1280,720);
		setLayout(new BorderLayout());
		
		//initialize the model first, then 
		int ttlPlayers = 0;
		while(ttlPlayers == 0)
		{
			ttlPlayers = 4;
			try {
				model = new GameModel(ttlPlayers);
				model.addObserver(this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			try{
//				ttlPlayers = Integer.parseInt(JOptionPane.showInputDialog("Please enter the total number of players in this game"));
//				model = new GameModel(ttlPlayers);
//				model.addObserver(this);				
//			}catch(NumberFormatException ex){
//				JOptionPane.showMessageDialog(this, "Error In Initializing Players", "Please input a number for total number of players!", JOptionPane.ERROR_MESSAGE);
//			} catch (Exception e) {
//				
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		//set the txt which shown all players movement
		txtPlayersMovement = new JTextArea();		
		txtPlayersMovement.setEditable(false);
		txtPlayersMovement.setRows(7);	
		txtPlayersMovement.setFont(FONT_STATUS);
		
		JScrollPane scroll = new JScrollPane (txtPlayersMovement, 
				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(scroll, BorderLayout.SOUTH);
		//set the panel for the deck
//		pnlDeck = new JPanel(new FlowLayout());
				
		//set the panel for the board
		pnlBoard = new PnlBoard(model.getBoard(), Board.WIDTH, Board.HEIGHT, model); 
		model.getBoard().addObserver(pnlBoard);		
		//set the panel for each players
		ArrayList<Player> players = model.getPlayers();
		pnlPlayersDetail = new ArrayList<PnlPlayer>();
		for(int i=0; i<ttlPlayers; i++)		
		{
			pnlPlayersDetail.add(new PnlPlayer(players.get(i), model));
			players.get(i).addObserver(pnlPlayersDetail.get(i));			
		}
		players = null;
		
		//set the panel for the players
		pnlPlayers = new ScrollablePanel();		
		pnlPlayers.setLayout(new BoxLayout(pnlPlayers, BoxLayout.PAGE_AXIS));
		pnlPlayers.setScrollableHeight(ScrollablePanel.ScrollableSizeHint.NONE);
		pnlPlayers.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.NONE);	
		for(int i=0; i<pnlPlayersDetail.size(); i++)
			pnlPlayers.add(pnlPlayersDetail.get(i));
		scrPanePlayers = new JScrollPane(pnlPlayers, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		//add the board panel to the frame (by wrapping it with another panel so that it could be drawn with center-alignment of the y-axis
		pnlGame = new JPanel();
		BoxLayout layout = new BoxLayout(pnlGame, BoxLayout.PAGE_AXIS);
		pnlGame.setLayout(layout);
		pnlGame.add(Box.createVerticalGlue());
		pnlGame.add(pnlBoard);
		pnlGame.add(Box.createVerticalGlue());
		add(pnlGame, BorderLayout.EAST);
		
		add(scrPanePlayers, BorderLayout.CENTER);
		
		
		gameSimulator = new GameSimulator(model,pnlPlayersDetail,pnlBoard, model.getPlayersVisibleStatus());
		model.setConsole(txtPlayersMovement);
		setVisible(true);
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		runSimulation();
		
	}
	
	public void runSimulation(){
//		if(rounds){
//			
//		}
//		model.nextTurn();
		gameSimulator.startGame(action);
		++action;
//		model.nextTurn();
	}
	
	public void runGame(int action){
		try {
			gameSimulator.runGame(action);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		FrmMain main = new FrmMain();		
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

}
