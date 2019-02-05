import java.util.ArrayList;

public abstract class AI {
	
	protected Player player;
	// List for players status
	protected ArrayList<PlayerVisibleStatus> playersVisibleStatus;
	protected Board board;
	protected Decision decision;
	protected ArrayList<int[]> logs;
	
	public AI(Player player, Board board, ArrayList<PlayerVisibleStatus> playersVisibleStatus, ArrayList<int[]> logs){
		this.setPlayer(player);
		this.setBoard(board);
		this.playersVisibleStatus = playersVisibleStatus;
		this.logs = logs;
	}
	/*
	 * method untuk discard
	 * 
	 */
	
	protected void PlayCard(int moveType, int indexCard){
		decision = new Decision(moveType,indexCard);
	}
	
	/*
	 * method untuk mengeluarkan kartu tipe path
	 * 
	 */
	protected void PlayCard(int moveType, int indexCard,boolean isRotate, int x, int y)
	{
		decision = new Decision(moveType, indexCard, x, y, isRotate);
	}

	/*
	 * method untuk mengeluarkan kartu tipe action rockfall/map (map adalah targetnya)
	 * 
	 */
	protected void PlayCard(int moveType, int indexCard, int x, int y)
	{
		decision = new Decision(moveType, indexCard, x, y);
	}
	
	/*
	 * method untuk mengeluarkan kartu tipe action block/repair(player adalah targetnya)
	 * 
	 */
	protected void PlayCard(int moveType, int indexCard, int targetPlayer)
	{
		decision = new Decision(moveType, indexCard, targetPlayer);
	}
	
	public abstract void playAlgorithm(Player player, Board board, ArrayList<PlayerVisibleStatus> playersVisibleStatus, ArrayList<int[]> logs);
	
	protected void setLogs(ArrayList<int[]> logs){
		this.logs = logs;
	}
	
	public Decision getDecision()
	{
		return decision;
	}
	
	protected void setDecision(Decision decision)
	{
		this.decision = decision;
	}
	
	protected Player getPlayer() {
		return player;
	}
	
	
	protected void setPlayer(Player player) {
		this.player = player;
	}
	protected Board getBoard() {
		return board;
	}
	protected void setBoard(Board board) {
		this.board = board;
	}
	
	public ArrayList<PlayerVisibleStatus> getPlayersVisibleStatus() {
		return playersVisibleStatus;
	}

	public void setPlayersVisibleStatus(ArrayList<PlayerVisibleStatus> playersVisibleStatus) {
		this.playersVisibleStatus = playersVisibleStatus;
	}

	public ArrayList<int[]> getLogs() {
		return logs;
	}

}
