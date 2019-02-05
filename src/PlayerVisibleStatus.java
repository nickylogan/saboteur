
public class PlayerVisibleStatus extends Player{

	public PlayerVisibleStatus(int number) {
		super(number);
		// TODO Auto-generated constructor stub
		role = -1;
		ttlBlockCart = 0;
		ttlBlockLight = 0;
		ttlBlockPickaxe = 0;
		statGoal1 = 0;
		statGoal2 = 0;
		statGoal3 = 0;
	}
	
	public void setRole(int role){
		this.role = role;
	}

	public void setTopGoal(boolean isGold)
	{
		statGoal1 = (isGold)?1:-1;
	}
	
	public void setMiddleGoal(boolean isGold)
	{
		statGoal1 = (isGold)?1:-1;
	}
	
	public void setBottomGoal(boolean isGold)
	{
		statGoal1 = (isGold)?1:-1;
	}
	
}
