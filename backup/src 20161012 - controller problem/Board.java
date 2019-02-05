import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

/**
 * 
 * This class observes: Cell (to update the cells when a card is put on)
 * @author DATACLIM-PIKU2
 *
 */
public class Board extends Observable implements Observer{
	/** the cells of the game consists of 4x8 cells */
	private Cell[][] cells;					
	/** the list of playable cells for which a card can be put */
	private ArrayList<Cell> playableCells;	
	/** the list of removable cells for which a card can be put */
	private ArrayList<Cell> filledCells;
	/** the array which stores information whether the goal card at the specified index has been reached*/
	private boolean isGoalReached[];	
	/** the boolean variable which stores the information whether the gold goal card has been reached and opened*/	
	private int idxGoldGoalCard;
	
	public static final int WIDTH = 8;
	public static final int HEIGHT = 5;
	
	public static final int TOP_GOAL = 0;
	public static final int MIDDLE_GOAL = 1;
	public static final int BOTTOM_GOAL = 2;
	
	/**
	 * 	
	 * @param startCard
	 * @param goalCardTop
	 * @param goalCardMiddle
	 * @param goalCardBottom
	 * @throws Exception
	 */
	public Board(CardPath startCard, CardPath goalCardTop, CardPath goalCardMiddle, CardPath goalCardBottom) throws Exception
	{		
		if(!startCard.isStartCard())
			throw new Exception("Initialization error: gives the start card in initializing the board!");
		
		if(!goalCardTop.isGoalCard() || !goalCardMiddle.isGoalCard() || !goalCardBottom.isGoalCard())
			throw new Exception("Initialization error: gives goal cards in initializing the board!");
			
		playableCells = new ArrayList<Cell>();
		filledCells = new ArrayList<Cell>();
	
		cells = new Cell[WIDTH][HEIGHT];
		for(int i=0; i<WIDTH; i++)
		{
			for(int ii=0; ii<HEIGHT; ii++)
			{
				cells[i][ii] = new Cell(i, ii);
				addObserverToCell(this, i, ii);
			}
		}
		
		isGoalReached = new boolean[3];
		isGoalReached[TOP_GOAL] = false;
		isGoalReached[MIDDLE_GOAL] = false;
		isGoalReached[BOTTOM_GOAL] = false;
		
		try{
			//place the start card			
			cells[0][2].putCard(startCard);
			cells[0][2].openCard();
			updateCells(startCard, 0, 2);
			updateAvailableCells(startCard, 0, 2);
			filledCells.remove(cells[0][2]);
			
			//place the goal card and updating the cell's state accordingly			
			cells[WIDTH-1][0].putCard(goalCardTop);
			cells[WIDTH-1][0].updateStates(-1, -1, 1, 1);
			cells[WIDTH-1][2].putCard(goalCardMiddle);
			cells[WIDTH-1][2].updateStates(1, -1, 1, 1);
			cells[WIDTH-1][4].putCard(goalCardBottom);
			cells[WIDTH-1][4].updateStates(1, -1, -1, 1);
			
			//updating the index of gold goal card
			if(goalCardTop.isGoldGoalCard())
				idxGoldGoalCard = TOP_GOAL;
			else if(goalCardMiddle.isGoldGoalCard())
				idxGoldGoalCard = MIDDLE_GOAL;
			else if(goalCardBottom.isGoldGoalCard())
				idxGoldGoalCard = BOTTOM_GOAL;
			
		}catch(Exception ex){}
	}
	
	/**
	 * get a shallow copy of the cell on the board at the specified xy-position
	 * @param x the x-position of the cell
	 * @param y the y-position of the cell
	 * @return a shallow copy of the cell object located at the specified xy-position
	 */
	public Cell getCell(int x, int y)
	{
		return cells[x][y];
	}
	
	/**
	 * get all the playable cells where a path card can be put
	 * @return a shallow copy of the collection of playable cells maintained by this board 
	 */
	public ArrayList<Cell> getPlayableCells()
	{
		return playableCells;
	}
	/**
	 * get all the cells which has been filled by a path card
	 * @return a shallow copy of the collection of filled cells maintained by this board
	 */
	public ArrayList<Cell> getFilledCells()
	{
		return filledCells;
	}	
	/**
	 * get all the goal cells on this board
	 * @return a shallow copy of the collection of goal cells on this board
	 */
	public ArrayList<Cell> getGoalCells()
	{
		ArrayList<Cell> goalCells = new ArrayList<Cell>();
		goalCells.add(cells[WIDTH-1][0]);
		goalCells.add(cells[WIDTH-1][2]);
		goalCells.add(cells[WIDTH-1][4]);
		return goalCells;
	}
	
	/**
	 * open the goal card specified by the index given, then return information whether the goal card contains gold or stone 
	 * @param goalCardIndex the index for the goal card (use static final member of this class: TOP_GOAL, MIDDLE_GOAL, and BOTTOM_GOAL for the top, middle, and bottom goal card respectively)
	 * @return true if the goal card opened contains gold, false otherwise
	 * @exception when trying to open a card other than goal cards 
	 */
	public boolean peekGoal(int goalCardIndex) throws Exception
	{
		if(goalCardIndex == TOP_GOAL)
			return cells[WIDTH-1][0].getCard().isGoldGoalCard();
		else if(goalCardIndex == MIDDLE_GOAL)
			return cells[WIDTH-1][2].getCard().isGoldGoalCard();
		else if(goalCardIndex == BOTTOM_GOAL)
			return cells[WIDTH-1][4].getCard().isGoldGoalCard();
		else 
			throw new Exception("Action Error: cannot open the specified card since it's not a goal card");		
	}
			
	/**
	 * update the cell's parameters which is the condition of its four direct neighbors according to the card given
	 * @param card the card placed on the cells
	 * @param x the x position where the card is placed
	 * @param y the y position where the card is placed
	 */
	private void updateCells(CardPath card, int x, int y) 
	{
		//in the case where a card is removed from the cell at (x,y)
		if(card == null)
		{		
			//enclosed in try-catch because the card properties' validity have already been guaranteed	
			try
			{		
				//update the state of the cell on the west of cell (x, y)
				if(x > 0)
				{
					if(!cells[x-1][y].isFilled())
						cells[x-1][y].updateStates(cells[x-1][y].getTopState(), Cell.EMPTY_NEIGHBOR, cells[x-1][y].getBottomState(), cells[x-1][y].getLeftState());
					else if(cells[x-1][y].getRightState() == Cell.CONNECTED_NEIGHBOR )
						cells[x-1][y].updateStates(cells[x-1][y].getTopState(), CardPath.PATH, cells[x-1][y].getBottomState(), cells[x-1][y].getLeftState());					
				}
				
				//update the state of the cell on the north of cell (x, y)
				if(y > 0)
				{
					if(!cells[x][y-1].isFilled())
						cells[x][y-1].updateStates(cells[x][y-1].getTopState(), cells[x][y-1].getRightState(), Cell.EMPTY_NEIGHBOR, cells[x][y-1].getLeftState());
					else if (cells[x][y-1].getBottomState() == Cell.CONNECTED_NEIGHBOR)
						cells[x][y-1].updateStates(cells[x][y-1].getTopState(), cells[x][y-1].getRightState(), CardPath.PATH, cells[x][y-1].getLeftState());
				}
				
				//update the state of the cell on the south of cell (x, y)
				if(y < HEIGHT-1)
				{
					if(!cells[x][y+1].isFilled())
						cells[x][y+1].updateStates(Cell.EMPTY_NEIGHBOR, cells[x][y+1].getRightState(), cells[x][y+1].getBottomState(), cells[x][y+1].getLeftState());
					else if (cells[x][y+1].getTopState() == Cell.CONNECTED_NEIGHBOR)
						cells[x][y+1].updateStates(CardPath.PATH, cells[x][y+1].getRightState(), cells[x][y+1].getBottomState(), cells[x][y+1].getLeftState());
				}
				
				//update the state of the cell on the east of cell (x, y)
				if(x < WIDTH - 1)
				{
					if(!cells[x+1][y].isFilled())
						cells[x+1][y].updateStates(cells[x+1][y].getTopState(), cells[x+1][y].getRightState(), cells[x+1][y].getBottomState(), Cell.EMPTY_NEIGHBOR);
					else if (cells[x+1][y].getLeftState() == Cell.CONNECTED_NEIGHBOR)
						cells[x+1][y].updateStates(cells[x+1][y].getTopState(), cells[x+1][y].getRightState(), cells[x+1][y].getBottomState(), CardPath.PATH);
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			//update the state of cell (x, y)		
			try
			{
				//enclosed in try-catch because the card properties' validity have already been guaranteed  
				cells[x][y].updateStates(cells[x][y-1].getBottomState(), cells[x+1][y].getLeftState(), cells[x][y+1].getTopState(), cells[x-1][y].getRightState());
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			
			
		}
		//in the case where a card is put on the cell at (x,y)
		else
		{
			//update the state of cell (x, y)
			double[] cardProperties = card.getAttributeVector();		
			try
			{
				//enclosed in try-catch because the card properties' validity have already been guaranteed  
				cells[x][y].updateStates((int)cardProperties[2], (int)cardProperties[3], (int)cardProperties[4], (int)cardProperties[5]);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
						
			//enclosed in try-catch because the card properties' validity have already been guaranteed
			try
			{
				//update the state of the cell on the west of cell (x, y)				
				if(x > 0)
				{
					if(cells[x-1][y].getRightState() == Cell.EMPTY_NEIGHBOR)
						cells[x-1][y].updateStates(cells[x-1][y].getTopState(), (int)cardProperties[5], 
								cells[x-1][y].getBottomState(), cells[x-1][y].getLeftState());
					else if (cells[x-1][y].getRightState() == CardPath.PATH && (int)cardProperties[5] == CardPath.PATH )
						cells[x-1][y].updateStates(cells[x-1][y].getTopState(), Cell.CONNECTED_NEIGHBOR, 
								cells[x-1][y].getBottomState(), cells[x-1][y].getLeftState());
				}
				
				//update the state of the cell on the north of cell (x, y)
				if(y > 0)
				{
					if(cells[x][y-1].getBottomState() == Cell.EMPTY_NEIGHBOR)
						cells[x][y-1].updateStates(cells[x][y-1].getTopState(), cells[x][y-1].getRightState(), 
							(int)cardProperties[2], cells[x][y-1].getLeftState());
					else if (cells[x][y-1].getBottomState() == CardPath.PATH && (int)cardProperties[2] == CardPath.PATH )
						cells[x][y-1].updateStates(cells[x][y-1].getTopState(), cells[x][y-1].getRightState(), 
								Cell.CONNECTED_NEIGHBOR, cells[x][y-1].getLeftState());
				}
				
				//update the state of the cell on the south of cell (x, y)
				if(y < HEIGHT-1)
				{
					if(cells[x][y+1].getTopState() == Cell.EMPTY_NEIGHBOR)
						cells[x][y+1].updateStates((int)cardProperties[4], cells[x][y+1].getRightState(), 
							cells[x][y+1].getBottomState(), cells[x][y+1].getLeftState());
					else if (cells[x][y+1].getTopState() == CardPath.PATH && (int)cardProperties[4] == CardPath.PATH )
						cells[x][y+1].updateStates(Cell.CONNECTED_NEIGHBOR, cells[x][y+1].getRightState(), 
								cells[x][y+1].getBottomState(), cells[x][y+1].getLeftState());
				}
				
				//update the state of the cell on the east of cell (x, y)
				if(x < WIDTH - 1)
				{
					if(cells[x+1][y].getLeftState() == Cell.EMPTY_NEIGHBOR)
						cells[x+1][y].updateStates(cells[x+1][y].getTopState(), cells[x+1][y].getRightState(), 
								cells[x+1][y].getBottomState(), (int)cardProperties[3]);
					else if (cells[x+1][y].getLeftState() == CardPath.PATH && (int)cardProperties[3] == CardPath.PATH )
						cells[x+1][y].updateStates(cells[x+1][y].getTopState(), cells[x+1][y].getRightState(), 
								cells[x+1][y].getBottomState(), Cell.CONNECTED_NEIGHBOR);
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}	
		}			
	}
	/**
	 * update the available cells where a path card can be put on based on the card put on the specified cell (x,y)
	 * precondition: to ensure adding the right cells, call updateCells first before calling this method 
	 * @param card the card which is put on the specified cell (x,y)
	 * @param x the x-coordinate of the cell where a card is put on
	 * @param y the y-coordinate of the cell where a card is put on
	 */
	private void updateAvailableCells(CardPath card, int x, int y)
	{
		if(card == null)
		{
			//add the just-destroyed cell to the collection of playable cells
			if(!playableCells.contains(cells[x][y]))
				playableCells.add(cells[x][y]);
			
			//remove the just-destroyed cell from the collection of filled cells			
			filledCells.remove(cells[x][y]);
			
			//adjust the playable cells by considering the neighboring cells
			//the top cell
			if(y > 0 && !cells[x][y-1].isReachable())
				playableCells.remove(cells[x][y-1]);
			//the bottom cell
			if(y < HEIGHT - 1 && !cells[x][y+1].isReachable())						
				playableCells.remove(cells[x][y+1]);			
			//the left cell
			if(x > 0 && !cells[x-1][y].isReachable())			
				playableCells.remove(cells[x-1][y]);			
			//the right cell
			if(x < WIDTH - 1 && !cells[x+1][y].isReachable())			
				playableCells.remove(cells[x+1][y]);			
		}
		else
		{
			//try to remove the cell being filled
			if(!playableCells.isEmpty())
				playableCells.remove(cells[x][y]);
			
			//add this cell to the collection of filled cells
			if(!filledCells.contains(cells[x][y]))
				filledCells.add(cells[x][y]);
			
			//add neighboring cells of cell (x,y) if there is any path/dead-end leading to the neighboring cells			
			//the top cell
			if(y > 0)
			{
				if((cells[x][y].getTopState() == CardPath.PATH || cells[x][y].getTopState() == CardPath.DEADEND) && !cells[x][y-1].isFilled())
					if(!playableCells.contains(cells[x][y-1]))
						playableCells.add(cells[x][y-1]);
			}
			//the bottom cell
			if(y < HEIGHT - 1)
			{
				if((cells[x][y].getBottomState() == CardPath.PATH || cells[x][y].getBottomState() == CardPath.DEADEND) && !cells[x][y+1].isFilled())
					if(!playableCells.contains(cells[x][y+1]))
						playableCells.add(cells[x][y+1]);
			}
			//the left cell
			if(x > 0)
			{
				if((cells[x][y].getLeftState() == CardPath.PATH || cells[x][y].getLeftState() == CardPath.DEADEND) && !cells[x-1][y].isFilled())
					if(!playableCells.contains(cells[x-1][y]))
						playableCells.add(cells[x-1][y]);
			}
			//the right cell
			if(x < WIDTH - 1)
			{
				if((cells[x][y].getRightState() == CardPath.PATH || cells[x][y].getRightState() == CardPath.DEADEND) && !cells[x+1][y].isFilled())
					if(!playableCells.contains(cells[x+1][y]))
						playableCells.add(cells[x+1][y]);				
			}
		}				
	}
	
	/**
	 * check whether the cell given is connected to the start card using DFS
	 * @param currentCell the cell to be checked whether it is connected to the start card or not
	 */
	public boolean checkConnectedPath(Cell currentCell)
	{		
		//declare variables needed
		boolean[][] isChecked = new boolean[WIDTH][HEIGHT];
		boolean isFound = false;
		Stack<Cell> toBeChecked = new Stack<Cell>();
		
		//initialize the variables
		toBeChecked.push(currentCell);
		
		//for each neighboring cells, add it to the stack of cell to be checked
		do
		{			
			//check whether the cell given is the starting cell
			currentCell = toBeChecked.pop();
			isChecked[currentCell.getX()][currentCell.getY()] = true;
			if(currentCell.getX() == 0 && currentCell.getY() == 2)
				return true;											
			
			//check four neighboring cells of current cell		
			if(currentCell.getRightState() == Cell.CONNECTED_NEIGHBOR && !isChecked[currentCell.getY()][currentCell.getX()+1])
				toBeChecked.push(cells[currentCell.getY()][currentCell.getX()+1]);		
			if(currentCell.getBottomState() == Cell.CONNECTED_NEIGHBOR && !isChecked[currentCell.getY()+1][currentCell.getX()])				
				toBeChecked.push(cells[currentCell.getY()+1][currentCell.getX()]);
			if(currentCell.getTopState() == Cell.CONNECTED_NEIGHBOR && !isChecked[currentCell.getY()-1][currentCell.getX()])
				toBeChecked.push(cells[currentCell.getY()-1][currentCell.getX()]);
			if(currentCell.getLeftState() == Cell.CONNECTED_NEIGHBOR && !isChecked[currentCell.getY()][currentCell.getX()-1])
				toBeChecked.push(cells[currentCell.getY()][currentCell.getX()-1]);												
		}
		while(!toBeChecked.isEmpty() && !isFound);
		
		toBeChecked.clear();
		toBeChecked = null;
		return isFound;
	}
	
	/**
	 * get information whether the goal card containing the gold has already been opened (thus reached)
	 * @return true if the goal card containing the gold has already been opened (thus reached), false otherwise
	 */
	public boolean isGoldGoalReached()
	{		
		switch(idxGoldGoalCard)
		{
		case TOP_GOAL:
			return cells[WIDTH-1][0].getCard().isOpened();
		case MIDDLE_GOAL:
			return cells[WIDTH-1][2].getCard().isOpened();
		case BOTTOM_GOAL:
			return cells[WIDTH-1][4].getCard().isOpened();
		default:
			return false;
		}
		
	}
	
	/**
	 * update the board based on which cell is put a card on, and which card is put
	 */
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		if(o instanceof Cell)
		{
			Cell updatedCell = (Cell)o;
			//destroy the card at the specified cell
			if(arg == null)
			{
				updateCells(null, updatedCell.getX(), updatedCell.getY());
				updateAvailableCells(null, updatedCell.getX(), updatedCell.getY());								
			}
			//put a card at the specified cell
			else if(arg instanceof CardPath)
			{
				updateCells((CardPath)arg, updatedCell.getX(), updatedCell.getY());
				updateAvailableCells((CardPath)arg, updatedCell.getX(), updatedCell.getY());			
			}
			
			//check whether any goal card is reachable
			isGoalReached[TOP_GOAL] = (cells[WIDTH-2][0].isFilled() || cells[WIDTH-1][1].isFilled());
			isGoalReached[MIDDLE_GOAL] = (cells[WIDTH-2][2].isFilled() || cells[WIDTH-1][1].isFilled() || cells[WIDTH-1][3].isFilled());
			isGoalReached[BOTTOM_GOAL] = (cells[WIDTH-2][4].isFilled() || cells[WIDTH-1][3].isFilled());
			
			//if any goal card is reached and not yet opened
			if(isGoalReached[TOP_GOAL] && !cells[WIDTH-1][0].getCard().isOpened())				
				if(checkConnectedPath(cells[WIDTH-1][0]))
					cells[WIDTH-1][0].openCard();								
			else if(isGoalReached[MIDDLE_GOAL] && !cells[WIDTH-1][2].getCard().isOpened())			
				if(checkConnectedPath(cells[WIDTH-1][2]))				
					cells[WIDTH-1][2].openCard();								
			else if(isGoalReached[BOTTOM_GOAL] && !cells[WIDTH-1][4].getCard().isOpened())
				if(checkConnectedPath(cells[WIDTH-1][4]))
					cells[WIDTH-1][4].openCard();								
		}
	}
	
	/**
	 * add observer to cell (x,y) on the board
	 * @param observer the observer to observe the cell (x,y)
	 * @param x the x-position of the cell being observed
	 * @param y the y-position of the cell being observed
	 */
	public void addObserverToCell(Observer observer, int x, int y)
	{
		cells[x][y].addObserver(observer);
	}
	
	
}
