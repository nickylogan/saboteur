import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class GameSimulator{
	
	public static int PATH_CARD = 0;
	public static int MAP_CARD = 1;
	public static int ROCKFALL_CARD = 2;
	public static int BLOCK_CARD = 3;
	public static int REPAIR_CARD = 4;
	public static int DISCARD = 5;
	
	public static int CARD_TYPE = 0;
	public static int CARD_INDEX = 1;
	public static int IS_ROTATED = 2;
	public static int PATH_X = 3;
	public static int PATH_Y = 4;
	public static int ROCKFALL_X = 2;
	public static int ROCKFALL_Y = 3;
	public static int GOLD_INDEX_OR_TARGET_PLAYER = 2;
	
	
	/*
	 * index 0 = playerNumber
	 * index 1 = playerMove
	 * index 2 = coordinate X if not board card -1
	 * index 3 = coordinate Y if not board card -1
	 * index 4 = target Player if not action card -1
	 * 
	 * -1 means no value
	 * 
	 * How to read log :
	 * [0, 1, 8, 0, -1]
	 * player 0 plays MAP card on cell 8,0 (8,0 is top goal card)
	 * [0, 2, 2, 4, -1]
	 * player 0 plays ROCKFALL card on cell 8,0 (8,0 is top goal card)
	 * [0, 0, 1, 2, -1]
	 * player 0 plays PATH card on cell 1,2
	 * [0, 3, -1, -1, 2]
	 * player 0 plays BLOCK card on player 2
	 */
	public ArrayList<int[]> logs = new ArrayList<>();

	private int DELAY = 500;
	
	private int player1 ;
	private int player2 ;
	private int player3 ;
	private int player4 ;
	
	private GameModel model;
	
	private ArrayList<AI> AIs;
	private ArrayList<PnlPlayer> pnlPlayersDetail;
	private ArrayList<PlayerVisibleStatus> playersVisibleStatus;
	private PnlBoard pnlBoard;
	private boolean running;
	
	private MyNewAI ai; 
	private AIJo ai2;
	private AiFelix ai3;
	private AIWL ai4;



	
	public GameSimulator(GameModel model, ArrayList<PnlPlayer> pnlPlayersDetail, PnlBoard pnlBoard, ArrayList<PlayerVisibleStatus> playersVisibleStatus){
		this.pnlBoard = pnlBoard;
		this.pnlPlayersDetail = pnlPlayersDetail;
		this.model = model;
		this.running = false;
		this.playersVisibleStatus = playersVisibleStatus;
		player1 = 0;
		player2 = 1;
		player3 = 2;    
		player4 = 3;
	}
	
	
	public void startGame(int action){
		
		model.nextTurn();

		// ai = new MyNewAI(model.getPlayers().get(player4),model.getBoard(),playersVisibleStatus, logs);
		// ai2 = new AIJo(model.getPlayers().get(player2),model.getBoard(),playersVisibleStatus, logs);
		// ai3 = new AiFelix(model.getPlayers().get(player3),model.getBoard(),playersVisibleStatus, logs);
		// ai4 = new AIWL(model.getPlayers().get(player1),model.getBoard(),playersVisibleStatus, logs);


		if(!model.getIsFinished()){
			running = true;
		}
		try {
			runGame(action);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
	
	public void runGame(int action) throws InterruptedException{
//		AI ai = new AI(model.getCurrentPlayer(),model.getBoard());
		//tinggal tambah current AI
		System.out.println(action);
		
		if(!model.getIsFinished()){
			
			if(model.getIdxCurrentPlayer()==player1){
				//Kelompok izzy
				//System.out.println("Izzy");
				update(ai,action);
			}else if(model.getIdxCurrentPlayer()==player2){

				//System.out.println("Felix");
				//Kelompok felix
				update(ai2,action);
			}else if(model.getIdxCurrentPlayer()==player3){
				//Kelompok kejo
				//System.out.println("Kejo");
				update(ai3,action);
			}else if(model.getIdxCurrentPlayer()==player4){
				//Kelompok senpai

				//System.out.println("Senpai");
				update(ai4,action);
			}
		}else{
			running = false;
		}
//		while(running){
//			
//			
//		}
	}
	
	private void update(AI ai,int action){
		for(int i=0; i<model.getPlayers().size(); ++i){
			playersVisibleStatus.get(i).ttlBlockCart = model.getPlayers().get(i).ttlBlockCart;
			playersVisibleStatus.get(i).ttlBlockLight = model.getPlayers().get(i).ttlBlockLight;
			playersVisibleStatus.get(i).ttlBlockPickaxe = model.getPlayers().get(i).ttlBlockPickaxe;
		}
		
		ai.playAlgorithm(model.getCurrentPlayer(), model.getBoard(), playersVisibleStatus, logs);
		
		Decision decision = ai.getDecision();
		int move = decision.getMoveType();
		int[] log= new int[5];
		log[0] = ai.getPlayer().number;
		log[1] = decision.getMoveType();
		if (decision.getX() != 0) {
		log[2] = decision.getX();
		}else {
			log[2]=-1;
		}
		if (decision.getY()!= 0 ) {
			log[3] = decision.getY();
		}else {
			log[3]=-1;
		}
		if (decision.getTargetPlayer() !=0) {
			log[4] = decision.getTargetPlayer();
		}else {
			log[4]=-1;
		}
		
//		System.out.println("Log : "+log);
		if (log != null) {
			logs.add(log);
			
		}
		if(action == 0){
			try {
				decideCard(ai);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			try {
				if(move == PATH_CARD){
					playPathCard(ai);
				}else if(move == MAP_CARD){
					playMapCard(ai);
				}else if(move == ROCKFALL_CARD){
					playRockFallCard(ai);
				}else if(move == BLOCK_CARD){
					playBlockCard(ai);
				}else if(move == REPAIR_CARD){
					playRepairCard(ai);
				}else if(move == DISCARD){
					discardCard(ai);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		model.nextTurn();
	}
	
	
	private void decideCard(AI ai) throws InterruptedException{
//		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[ai.getDecision()[CARD_INDEX]].borderHovered();
//		model.setSelectedCard(model.getPlayers().get(model.getIdxCurrentPlayer()).getCard(ai.getDecision()[CARD_INDEX]));
		int index = ai.getDecision().getIndexCard();

		
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderSelectable();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderSelected();
		
	}
	
	private void playPathCard(AI ai) throws InterruptedException{
		int index = ai.getDecision().getIndexCard();
		int x = ai.getDecision().getX();
		int y = ai.getDecision().getY();
		boolean isRotate = ai.getDecision().isRotate();
		isRotate =false;
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderSelectable();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderHovered();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		if(isRotate){
			model.getCurrentPlayer().getCard(index).rotate();
		}
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		model.setSelectedCard(model.getCurrentPlayer().getCard(index));
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlBoard.getCells()[x][y].putCardOnCell(x, y);
	}
	
	private void playMapCard(AI ai) throws InterruptedException{
		int index = ai.getDecision().getIndexCard();
		int x = model.getBoard().WIDTH-1;
		int y = ai.getDecision().getY();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderSelectable();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderHovered();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		model.setSelectedCard(model.getCurrentPlayer().getCard(index));
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlBoard.getCells()[x][y].putCardOnCell(x, y);
//		TimeUnit.MILLISECONDS.sleep(DELAY);
//		System.out.println("etwet");
	}
	
	private void playRockFallCard(AI ai) throws InterruptedException{
		int index = ai.getDecision().getIndexCard();
		int x = ai.getDecision().getX();
		int y = ai.getDecision().getY();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderSelectable();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderHovered();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		model.setSelectedCard(model.getCurrentPlayer().getCard(index));
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlBoard.getCells()[x][y].putCardOnCell(x, y);
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		
	}
	
	private void playBlockCard(AI ai) throws InterruptedException{
		int index = ai.getDecision().getIndexCard();
		int target = ai.getDecision().getTargetPlayer();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderSelectable();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderHovered();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		model.setSelectedCard(model.getCurrentPlayer().getCard(index));
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(target).blockOrRepairTarget(model.getPlayers().get(target));
//		TimeUnit.MILLISECONDS.sleep(DELAY);
	}
	
	private void playRepairCard(AI ai) throws InterruptedException{
		int index = ai.getDecision().getIndexCard();
		int target = ai.getDecision().getTargetPlayer();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderSelectable();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderHovered();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		model.setSelectedCard(model.getCurrentPlayer().getCard(index));
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(target).blockOrRepairTarget(model.getPlayers().get(target));
	}
	
	private void discardCard(AI ai) throws InterruptedException{
		int index = ai.getDecision().getIndexCard();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderSelectable();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		pnlPlayersDetail.get(model.getIdxCurrentPlayer()).pnlCards[index].borderHovered();
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		model.setSelectedCard(model.getCurrentPlayer().getCard(index));
//		TimeUnit.MILLISECONDS.sleep(DELAY);
		try {
			model.discardCard(model.getCurrentPlayer().getCard(index));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		TimeUnit.MILLISECONDS.sleep(DELAY);
	}

	public ArrayList<PlayerVisibleStatus> getPlayersVisibleStatus() {
		return playersVisibleStatus;
	}
	
	public void setRunning(boolean running){
		this.running = running;
	}

	public void setPlayersVisibleStatus(ArrayList<PlayerVisibleStatus> playersVisibleStatus) {
		this.playersVisibleStatus = playersVisibleStatus;
	}
	
}
