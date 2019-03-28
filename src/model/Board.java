package model;

import model.cards.PathCard;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * The {@link Board} class represents a Saboteur game board.
 * It contains all the necessary methods for game board logic.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Board {
  /** The {@link GoalPosition} enum represents every valid goal position in the game */
  public enum GoalPosition {TOP, MIDDLE, BOTTOM}

  /** The default board width specified in the game rule */
  public static final int DEFAULT_WIDTH = 9;
  /** The default board height specified in the game rule */
  public static final int DEFAULT_HEIGHT = 5;

  /** The width of the board */
  private final int width;
  /** The height of the board */
  private final int height;
  /** Cells in the board */
  private final Cell[][] cells;
  /** The top goal card */
  private GoalType topGoal;
  /** The middle goal card */
  private GoalType middleGoal;
  /** The bottom goal card */
  private GoalType bottomGoal;
  /** Marks the top goal as opened */
  private boolean topGoalOpened;

  /**
   * Creates a {@link Board} object.
   * <p>
   * The newly created {@link Board} contains a grid of empty cells the
   * size of <code>DEFAULT_WIDTH</code> &times; <code>DEFAULT_HEIGHT</code>
   * </p>
   */
  public Board() {
    // Initialize board dimension
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
    // Initialize cells
    this.cells = new Cell[this.width][this.height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        cells[x][y] = new Cell(x, y);
      }
    }
  }

  /**
   * Initialize the board by placing goal cards
   *
   * @param topGoal the top goal
   * @param midGoal the middle goal
   * @param botGoal the bottom goal
   */
  final void initialize(GoalType topGoal, GoalType midGoal, GoalType botGoal) {
    // Open starting cell sides
    this.cells[0][this.height / 2].openAllSides();

    // Open goal cell sides
    this.cells[this.width - 1][0].openAllSides();
    this.cells[this.width - 1][this.height / 2].openAllSides();
    this.cells[this.width - 1][this.height - 1].openAllSides();

    // Set goals
    this.topGoal = topGoal;
    this.middleGoal = midGoal;
    this.bottomGoal = botGoal;
  }

  /**
   * Attempts to place a path card on the specified <code>(x, y)</code> position
   *
   * @param card the path card to be placed
   * @param x    the x position to be placed on
   * @param y    the y position to be placed on
   * @throws GameException when card is not placeable at the specified position
   */
  final void placePathCardAt(PathCard card, int x, int y) throws GameException {
    if (!isInBoard(x, y)) {
      String msgFormat = "Position (%d, %d) is out of bounds";
      throw new GameException(msgFormat, x, y);
    }
    if (!isCardPlaceableAt(card, x, y)) {
      String msgFormat = "Card cannot be placed at position (%d, %d)";
      throw new GameException(msgFormat, x, y);
    }

    this.cells[x][y].placePathCard(card);
  }

  /**
   * Attempts to place a path card on the specified <code>position</code>
   *
   * @param card   the path card to be placed
   * @param target the position to be placed on
   * @throws GameException when card is not placeable at the specified position
   */
  final void placePathCardAt(PathCard card, Position target) throws GameException {
    this.placePathCardAt(card, target.x, target.y);
  }

  /**
   * Removes a card from the specified <code>(x, y)</code> target
   *
   * @param x the targeted x position
   * @param y the targeted y position
   * @throws GameException when position is out of bounds, equals start/goal, or has empty cell
   */
  final void removeCardAt(int x, int y) throws GameException {
    this.removeCardAt(new Position(x, y));
  }

  /**
   * Removes a card from the specified <code>target</code>
   *
   * @param target the targeted position
   * @throws GameException when position is out of bounds, equals start/goal, or has empty cell
   */
  final void removeCardAt(Position target) throws GameException {
    // Check if not out of bounds
    if (!isInBoard(target)) {
      String msgFormat = "Position (%d, %d) is out of bounds";
      throw new GameException(msgFormat, target.x, target.y);
    }
    // Make sure not equal to start
    if (target.equals(startPosition())) {
      throw new GameException("Cannot remove starting card");
    }
    // Make sure not equal to any of the goal cards
    if (target.equals(topGoalPosition())
        || target.equals(middleGoalPosition())
        || target.equals(bottomGoalPosition())) {
      throw new GameException("Cannot remove any of the goal cards");
    }
    // Check if contains card
    if (!cellAt(target).hasCard()) {
      String msgFormat = "Cell at (%d, %d) is empty";
      throw new GameException(msgFormat, target.x, target.y);
    }

    this.cells[target.x][target.y].removeCard();
  }

  /**
   * Checks whether the specified position is inside the board
   *
   * @param x the x position to be checked
   * @param y the y position to be checked
   * @return a boolean representing if (x,y) is in the board
   */
  public final boolean isInBoard(int x, int y) {
    return x >= 0 && x < width && y >= 0 && y < height;
  }

  /**
   * Checks whether the specified position is inside the board
   *
   * @param pos the position to be checked
   * @return a boolean representing if (x,y) is in the board
   */
  public final boolean isInBoard(Position pos) {
    return pos != null && this.isInBoard(pos.x, pos.y);
  }

  /**
   * Checks whether the gold card is reached
   *
   * @return a boolean indicating if the gold is reached
   */
  public final boolean isGoldReached() {
    return (isReachable(topGoalPosition()) && topGoal == GoalType.GOLD)
           || (isReachable(middleGoalPosition()) && middleGoal == GoalType.GOLD)
           || (isReachable(bottomGoalPosition()) && bottomGoal == GoalType.GOLD);
  }

  /**
   * Checks whether the specified <code>(x, y)</code> position is reachable from
   * the board's starting position
   *
   * @param x the targeted x position
   * @param y the targeted y position
   * @return a boolean representing whether the targeted position is reachable
   */
  public final boolean isReachable(int x, int y) {
    return this.isReachable(new Position(x, y));
  }

  /**
   * Checks whether the specified <code>target</code> is reachable from
   * the board's starting position
   *
   * @param target the targeted position
   * @return a boolean representing whether the targeted position is reachable
   */
  public final boolean isReachable(Position target) {
    // Check if position is not null
    if (target == null) return false;
    // Check is position is in board
    if (!isInBoard(target)) return false;

    Set<Position> reachable = getReachable();
    return reachable.contains(target);
  }

  /**
   * Checks whether the specified <code>(x, y)</code> position is destroyable
   *
   * @param x the targeted x position
   * @param y the targeted y position
   * @return a boolean representing whether the targeted position is destroyable
   */
  public final boolean isDestroyable(int x, int y){
    return this.isDestroyable(new Position(x, y));
  }

  /**
   * Checks whether the specified <code>target</code> is destroyable
   *
   * @param target the targeted position
   * @return a boolean representing whether the targeted position is destroyable
   */
  public final boolean isDestroyable(Position target) {
    return !target.equals(startPosition())
           && !target.equals(topGoalPosition())
           && !target.equals(middleGoalPosition())
           && !target.equals(bottomGoalPosition())
           && cellAt(target).hasCard();
  }

  /**
   * Checks whether the specified path card is placeable at the specified
   * <code>(x, y)</code> position
   *
   * @param card the checked path card
   * @param x    the target x position
   * @param y    the target y position
   * @return a boolean representing a card's placeability
   */
  public final boolean isCardPlaceableAt(PathCard card, int x, int y) {
    return this.isCardPlaceableAt(card, new Position(x, y));
  }

  /**
   * Checks whether the specified path card is placeable at the specified target
   *
   * @param card   the checked path card
   * @param target the target position
   * @return a boolean representing a card's placeability
   */
  public final boolean isCardPlaceableAt(PathCard card, Position target) {
    // Check target is not null
    if (target == null) return false;
    // Check if target is in board
    if (!isInBoard(target)) {
      System.out.println(target + " out of bounds");
      return false;
    }
    // Check if cell contains a card
    if (cellAt(target).hasCard()) {
      System.out.println(target + " not empty");
      System.out.println(Arrays.toString(cellAt(target).sides()));
      return false;
    }
    // Check if reachable
    if (!isReachable(target)) {
      System.out.println(target + " unreachable");
      System.out.println(getReachable());
      return false;
    }

    if (!checkTouchingSides(card, target)) {
      System.out.println(target + " invalid sides");
    }
    return checkTouchingSides(card, target);
  }

  /**
   * Gets all reachable positions from the starting cell
   *
   * @return a set containing all reachable positions
   */
  public final Set<Position> getReachable() {
    // Do depth-first search from start position
    Set<Position> visited = new HashSet<>();
    Position start = startPosition();
    Stack<Position> s = new Stack<>();
    s.push(start);
    while (!s.empty()) {
      Position curr = s.pop();
      if (visited.contains(curr)) continue;
      visited.add(curr);
      Cell currCell = cellAt(curr);

      if (!currCell.hasCard()) { continue; }

      if (isInBoard(curr.top()) && currCell.topSide() == Cell.Side.PATH) {
        s.push(curr.top());
      }
      if (isInBoard(curr.right()) && currCell.rightSide() == Cell.Side.PATH) {
        s.push(curr.right());
      }
      if (isInBoard(curr.bottom()) && currCell.bottomSide() == Cell.Side.PATH) {
        s.push(curr.bottom());
      }
      if (isInBoard(curr.left()) && currCell.leftSide() == Cell.Side.PATH) {
        s.push(curr.left());
      }
    }
    return visited;
  }

  /**
   * Gets all placeable positions of the specified path card
   *
   * @param card the path card to be placed
   * @return a set containing all placeable positions
   */
  public final Set<Position> getPlaceable(PathCard card) {
    Set<Position> reachable = getReachable();
    return getReachable().stream()
      .filter(p ->
        !(p.equals(startPosition())
          || p.equals(topGoalPosition())
          || p.equals(middleGoalPosition())
          || p.equals(bottomGoalPosition())
          || cellAt(p).hasCard())
        && checkTouchingSides(card, p))
      .collect(Collectors.toSet());
  }

  /**
   * Gets all destroyable path cards on the board
   *
   * @return a set containing all destroyable positions
   */
  public final Set<Position> getDestroyable() {
    Set<Position> destroyable = new HashSet<>();
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        Position p = new Position(x, y);
        if (p.equals(startPosition())
            || p.equals(topGoalPosition())
            || p.equals(middleGoalPosition())
            || p.equals(bottomGoalPosition())
            || !cellAt(p).hasCard()
        ) continue;
        destroyable.add(p);
      }
    }
    return destroyable;
  }

  /**
   * Checks the touching sides of the specified path card on the specified target
   *
   * @param card   the checked path card
   * @param target the target position
   * @return a boolean representing a card's placeability
   */
  private boolean checkTouchingSides(PathCard card, Position target) {
    System.out.println(card);
    // Check target is not null
    if (target == null) return false;
    // Check if target is in board
    if (!isInBoard(target)) return false;
    // Check touching sides
    if (
      isInBoard(target.top())
      && cellAt(target.top()).bottomSide() != Cell.Side.EMPTY
      && cellAt(target.top()).bottomSide().val() != card.topSide().val()
      && cellAt(target.top()).bottomSide().val() + card.topSide().val() < 0
    ) {
      System.out.printf("cell(bottom: %s) vs card(top: %s)\n", cellAt(target.top()).bottomSide(), card.topSide());
      return false;
    }
    if (
      isInBoard(target.right())
      && cellAt(target.right()).leftSide() != Cell.Side.EMPTY
      && cellAt(target.right()).leftSide().val() != card.rightSide().val()
      && cellAt(target.right()).leftSide().val() + card.rightSide().val() < 0
    ) {
      System.out.printf("cell(left: %s) vs card(right: %s)\n", cellAt(target.right()).leftSide(), card.rightSide());
      return false;
    }
    if (
      isInBoard(target.bottom())
      && cellAt(target.bottom()).topSide() != Cell.Side.EMPTY
      && cellAt(target.bottom()).topSide().val() != card.bottomSide().val()
      && cellAt(target.bottom()).topSide().val() + card.bottomSide().val() < 0
    ) {
      System.out.printf("cell(top: %s) vs card(bottom: %s)\n", cellAt(target.bottom()).topSide(), card.bottomSide());
      return false;
    }
    if (
      isInBoard(target.left())
      && cellAt(target.left()).rightSide() != Cell.Side.EMPTY
      && cellAt(target.left()).rightSide().val() != card.leftSide().val()
      && cellAt(target.left()).rightSide().val() + card.leftSide().val() < 0
    ) {
      System.out.printf("cell(right: %s) vs card(left: %s)\n", cellAt(target.left()).rightSide(), card.leftSide());
      return false;
    }

    // Return true as default
    return true;
  }

  /**
   * Returns a {@link Position} representing the starting cell position
   *
   * @return the position of the starting cell
   */
  public final Position startPosition() { return new Position(0, this.height / 2); }

  /**
   * Returns a {@link Position} representing the top goal's position
   *
   * @return the position of the top goal
   */
  public final Position topGoalPosition() { return new Position(this.width - 1, 0); }

  /**
   * Returns a {@link Position} representing the middle goal's position
   *
   * @return the position of the middle goal
   */
  public final Position middleGoalPosition() { return new Position(this.width - 1, this.height / 2); }

  /**
   * Returns a {@link Position} representing the bottom goal's position
   *
   * @return the position of the bottom goal
   */
  public final Position bottomGoalPosition() { return new Position(this.width - 1, this.height - 1); }

  /**
   * Returns the cell at a given <code>(x, y)</code> position
   *
   * @param x the x position
   * @param y the y position
   * @return the cell at <code>(x, y)</code>
   */
  public final Cell cellAt(int x, int y) {
    return !isInBoard(x, y) ? null : this.cells[x][y].copy();
  }

  /**
   * Returns the cell at a given <code>position</code>
   *
   * @param position the specified position
   * @return the cell at <code>position</code>
   */
  public final Cell cellAt(Position position) {
    if (position == null) return null;
    Cell cell = this.cellAt(position.x, position.y);
    return cell == null ? null : cell.copy();
  }

  /**
   * Returns a copy of all the cells in the board
   *
   * @return the board's cells
   */
  public final Cell[][] cells() {
    Cell[][] clone = new Cell[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        clone[x][y] = cells[x][y].copy();
      }
    }
    return clone;
  }

  /**
   * Returns the width of the board
   *
   * @return the board's width
   */
  public final int width() { return width; }

  /**
   * Returns the height of the board
   *
   * @return the board's height
   */
  public final int height() { return height; }

  /**
   * Returns the goal type on the specified goal position
   *
   * @param goalPosition the goal position
   * @return the goal type
   */
  final GoalType peekGoal(GoalPosition goalPosition) {
    switch (goalPosition) {
      case TOP:
        return topGoal;
      case MIDDLE:
        return middleGoal;
      case BOTTOM:
        return bottomGoal;
      default:
        return null;
    }
  }
}
