import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
/**
 * This class observes: Player (to update the player's status when an action card is applied on this player)
 * @author DATACLIM-PIKU2
 *
 */
public class PnlPlayer extends SelectablePanel implements Observer{

	private Player player;
	private GameModel model;
	
	public PnlCardPlayer[] pnlCards;
	private JPanel pnlStatus;
	private JLabel lblCart;
	private JLabel lblLight;
	private JLabel lblPickaxe;
	private JLabel lblPlayer;
	private JLabel lblTopGoal;
	private JLabel lblMiddleGoal;
	private JLabel lblBottomGoal;
	private static String SABOTEUR = "Saboteur";
	private static String GOLD_MINER = "Gold Miner";
	
	private String status;
	
	private final int TTL_CARDS = 6;
	private final Font FONT_STATUS = new Font("Times New Roman", Font.BOLD, 20);
	
	public PnlPlayer(Player player, GameModel model)
	{
		//initialize the panel
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));		
		
		//initialize the player
		this.player = player;
		this.model = model;
		setSize(TTL_CARDS * FrmMain.PNL_CARD_WIDTH, TTL_CARDS * FrmMain.PNL_CARD_HEIGHT);
		
		//initialize the panels for placing cards panels
		//==============================================
		//initialize the card panels and add observers
		pnlCards = new PnlCardPlayer[TTL_CARDS];
		for(int i=0; i<TTL_CARDS; i++)
		{
			pnlCards[i] = new PnlCardPlayer(this.player.getCard(i), FrmMain.PNL_CARD_WIDTH, FrmMain.PNL_CARD_HEIGHT, model);
			if(this.player.getCard(i) != null)
				this.player.getCard(i).addObserver(pnlCards[i]);
		}
		
		if(player.getRole()==0){
			status = "Player " + player.getAttributes()[0] +" \n "+SABOTEUR;
		}else{
			 status = "Player " + player.getAttributes()[0] +" \n "+ GOLD_MINER;
		}
		//initialize the panels for player's status
		//=========================================		
		lblPlayer = new JLabel();									
		lblCart = new JLabel(new ImageIcon("res/IconBlockCart1.png"));
		lblCart.setFont(FONT_STATUS);
		lblLight = new JLabel(new ImageIcon("res/IconBlockLight1.png"));
		lblLight.setFont(FONT_STATUS);
		lblPickaxe = new JLabel(new ImageIcon("res/IconBlockPickaxe1.png"));
		lblPickaxe .setFont(FONT_STATUS);
		lblTopGoal = new JLabel(new ImageIcon("res/IconTopGoal1.png"));
		lblTopGoal .setFont(FONT_STATUS);
		lblMiddleGoal = new JLabel(new ImageIcon("res/IconMiddleGoal1.png"));
		lblMiddleGoal .setFont(FONT_STATUS);
		lblBottomGoal = new JLabel(new ImageIcon("res/IconBottomGoal1.png"));
		lblBottomGoal .setFont(FONT_STATUS);
		updatePlayerStatus();
		pnlStatus = new JPanel();
		pnlStatus.setLayout(new GridLayout(2, 3));
		pnlStatus.add(lblCart);
		pnlStatus.add(lblLight);
		pnlStatus.add(lblPickaxe);
		pnlStatus.add(lblTopGoal);
		pnlStatus.add(lblMiddleGoal);
		pnlStatus.add(lblBottomGoal);
		
		//add all components to panel
		//===========================		
		add(lblPlayer);
		for(int i=0; i<pnlCards.length; i++)
			add(pnlCards[i]);
		add(pnlStatus);
		
		setVisible(true);
	}
	
	public String getStatus(){
		return status;
	}
	
	/**
	 * update the player's status 
	 */
	private void updatePlayerStatus()
	{
		//update player's name
		if(!player.isAi())
			lblPlayer.setText(status);
		else
			lblPlayer.setText("Computer " + player.getAttributes()[0]);
		
		//update player's block status
		lblCart.setText(Integer.toString(player.getTtlBlockOreCart()));
		lblLight.setText(Integer.toString(player.getTtlBlockLight()));
		lblPickaxe.setText(Integer.toString(player.getTtlBlockPickaxe()));
		
		//update player's history of peeking goal cards
		lblTopGoal.setText(Integer.toString(player.getTtlTopGoalPeeked()));
		lblMiddleGoal.setText(Integer.toString(player.getTtlMiddleGoalPeeked()));
		lblBottomGoal.setText(Integer.toString(player.getTtlBottomGoalPeeked()));
	}
	/**
	 * update the player's cards 
	 */
	private void updatePlayerCards()
	{
		for(int i=0; i<pnlCards.length; i++)
		{
			if(pnlCards[i].getCard() != null)
				pnlCards[i].getCard().deleteObserver(pnlCards[i]);
			pnlCards[i].putCard(player.getCard(i));
			if(player.getCard(i) != null)
				player.getCard(i).addObserver(pnlCards[i]);
		}
	}
	
	
	public void blockOrRepairTarget(Player player){
		if(!model.getSelectedCard().isBoardCard())
		{
			try
			{
				
//				System.out.println("tes1");
				int currentPlayer = 0;
				if(model.getIdxCurrentPlayer()-1>=0){
					currentPlayer = (model.getIdxCurrentPlayer()-1);
				}else if(model.getIdxCurrentPlayer()-1<0){
					currentPlayer = 3;					
				}
				if(model.getSelectedCard().getAttributeVector()[1]==5){
					model.getConsole().setText(model.getConsole().getText().toString().concat("Player "+ model.getIdxCurrentPlayer() + " is blocking Player " + player.getAttributes()[0]+"\n"));
				}else if(model.getSelectedCard().getAttributeVector()[1]==6){
					model.getConsole().setText(model.getConsole().getText().toString().concat("Player "+ model.getIdxCurrentPlayer() + " is repairing Player " + player.getAttributes()[0]+"\n"));
				}
				model.playPlayerCard((CardAction)model.getSelectedCard(), player);
			}
			catch(Exception ex)
			{ex.printStackTrace();}
		}
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		if(arg0 instanceof Player)
		{	
			//if any states of this player is updated
			if(arg1 instanceof Player)
			{
				updatePlayerStatus();
				setSelectable(player.isSelectable());
			}			
			//a playable card is given to this player / a card is played (removed) by this player			
			else if(arg1 instanceof CardPlayable)
			{				
				updatePlayerCards();
			}
			repaint();
			revalidate();			
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(isSelectable())
		{
			//this player is set to be the target
			blockOrRepairTarget(player);
		}
		else if(player.equals(model.getCurrentPlayer()))
		{
//			if(arg0.getClickCount() >= 2)
//				model.nextTurn();
		}
	}

}
