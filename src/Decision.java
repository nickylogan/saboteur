
public class Decision {

	
	private int moveType;
	private int indexCard;
	private int x;
	private int y;
	private boolean isRotate;
	private int targetPlayer;

	public Decision(int moveType, int indexCard, int x, int y) {
		super();
		this.moveType = moveType;
		this.indexCard = indexCard;
		this.x = x;
		this.y = y;
	}
	
	public Decision(int moveType, int indexCard, int x, int y, boolean isRotate) {
		super();
		this.moveType = moveType;
		this.indexCard = indexCard;
		this.x = x;
		this.y = y;
		this.isRotate = isRotate;
	}

	public Decision(int moveType, int indexCard, int targetPlayer) {
		super();
		this.moveType = moveType;
		this.indexCard = indexCard;
		this.targetPlayer = targetPlayer;
	}

	public Decision(int moveType, int indexCard) {
		super();
		this.moveType = moveType;
		this.indexCard = indexCard;
	}

	public int getMoveType() {
		return moveType;
	}
	public void setMoveType(int moveType) {
		this.moveType = moveType;
	}
	public int getIndexCard() {
		return indexCard;
	}
	public void setIndexCard(int indexCard) {
		this.indexCard = indexCard;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public boolean isRotate() {
		return isRotate;
	}
	public void setRotate(boolean isRotate) {
		this.isRotate = isRotate;
	}
	public int getTargetPlayer() {
		return targetPlayer;
	}
	public void setTargetPlayer(int targetPlayer) {
		this.targetPlayer = targetPlayer;
	}
	
}
