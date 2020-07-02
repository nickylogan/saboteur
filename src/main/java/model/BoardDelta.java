package model;

import model.cards.Card;

/**
 * The {@link BoardDelta} class represents changes made to the board.
 */
public class BoardDelta extends StateDelta {
  /** The affected position */
  private final Position affected;
  /** The previous card value of the given position */
  private final Card before;
  /** The new card value of the given position */
  private final Card after;
  /** The old board state */
  private final Board boardBefore;
  /** The new board state */
  private final Board boardAfter;

  BoardDelta(Position affected, Card before, Card after, Board bBefore, Board bAfter) {
    this.affected = affected;
    this.before = before;
    this.after = after;
    this.boardBefore = bBefore.copy();
    this.boardAfter = bAfter.copy();
  }

  /**
   * Returns the affected position
   *
   * @return the affected position
   */
  public final Position affected() {
    return affected;
  }

  /**
   * Returns the previous card value
   *
   * @return the previous card value
   */
  public final Card before() {
    return before;
  }

  /**
   * Returns the new card value
   *
   * @return the new card value
   */
  public final Card after() {
    return after;
  }

  /**
   * Returns the old board state
   *
   * @return the old board state
   */
  public Board boardBefore() {
    return boardBefore;
  }

  /**
   * Returns the new board state
   *
   * @return the new board state
   */
  public Board boardAfter() {
    return boardAfter;
  }
}
