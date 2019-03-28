package model;

import model.cards.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link GameObserver} interface represents an observer for
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class GameObserver {
  /** A reference to the game state */
  private GameLogicController game;
  /** The move history */
  private final History history = new History();
  /** Known goal cards */
  private final Map<Board.GoalPosition, GoalType> knownGoals = new HashMap<>();

  /**
   * Sets the game state reference of the player
   *
   * @param state the game state
   */
  final void setGame(GameLogicController state) { this.game = state; }

  /**
   * Returns the game state reference
   *
   * @return the game state
   */
  protected GameLogicController game() { return this.game; }

  /**
   * Returns the game move history
   *
   * @return the game move history
   */
  protected History history() { return this.history; }

  /**
   * Returns the known goals
   *
   * @return the known goals
   */
  protected Map<Board.GoalPosition, GoalType> knownGoals() {
    return knownGoals;
  }

  /**
   * Implement this to do something when prompted with movement
   */
  protected void onMovementPrompt() {}

  /**
   * Implement this to do something when the game finishes
   *
   * @param role       the winning role
   * @param lastPlayer the last player to move
   */
  protected void onGameFinished(Player.Role role, int lastPlayer) {}

  /**
   * Implement this to do something when the game starts
   */
  protected void onGameStart() {}

  /**
   * Implement this to do something when the game state changes
   */
  protected void onGameStateChanged() {}

  /**
   * Implement this to do something when a player moves
   *
   * @param move the player move
   */
  protected void onPlayerMove(Move move,  Card newCard) {}

  /**
   * Implement this to do something when it is the next turn
   *
   * @param player the next player
   * @param hand   the next player's hand
   */
  protected void onNextTurn(int player, ArrayList<Card> hand) { }

  /**
   * Implement this to do something when a goal card is opened
   *
   * @param position  the goal position
   * @param goalType  the opened goal card
   * @param permanent marks if the goal card is opened permanently
   */
  protected void onGoalOpen(Board.GoalPosition position, GoalType goalType, boolean permanent) { }

  /**
   * Notifies the observer of a movement prompt
   */
  final void notifyPromptMovement() { onMovementPrompt(); }

  /**
   * Notifies the observer that the game has started
   */
  final void notifyGameStarted() { history.clear(); onGameStart(); }

  /**
   * Notifies the observer that the game is finished
   */
  final void notifyGameFinished(Player.Role role, int lastPlayer) { onGameFinished(role, lastPlayer); }

  /**
   * Notifies the observer of a new player move
   *
   * @param move    the player move
   * @param newCard the player's new card
   */
  final void notifyPlayerMove(Move move,  Card newCard) {
    history.appendMove(move);
    onPlayerMove(move, newCard);
  }

  /**
   * Notifies the observer that the game state has changed
   */
  final void notifyGameStateChanged() {
    onGameStateChanged();
  }

  /**
   * Notifies the observer that it is the next player's turn
   */
  final void notifyNextPlayer(int player, ArrayList<Card> hand) { onNextTurn(player, hand); }

  /**
   * Notifies the observer of an open goal card
   *
   * @param pos       the goal position
   * @param goalType  the opened goal card
   * @param permanent marks if the goal card is opened permanently
   */
  final void notifyGoalOpen(Board.GoalPosition pos, GoalType goalType, boolean permanent) {
    this.knownGoals.put(pos, goalType);
    onGoalOpen(pos, goalType, permanent);
  }
}
