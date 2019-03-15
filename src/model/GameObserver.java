package model;

import com.sun.istack.internal.NotNull;

/**
 * The {@link GameObserver} interface represents an observer for
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class GameObserver {
  /** A reference to the game state */
  private GameState state;
  /** The move history */
  private final History history = new History();

  /**
   * Sets the game state reference of the player
   *
   * @param state the game state
   */
  final void setState(GameState state) { this.state = state; }

  /**
   * Returns the game state reference
   *
   * @return the game state
   */
  protected GameState state() { return this.state; }

  /**
   * Implement this to do something when prompted with movement
   */
  public void onMovementPrompt() {}

  /**
   * Implement this to do something when the game finishes
   *
   * @param role       the winning role
   * @param lastPlayer the last player to move
   */
  public void onGameFinished(Player.Role role, int lastPlayer) {}

  /**
   * Implement this to do something when the game starts
   */
  public void onGameStart() {}

  /**
   * Implement this to do something when the game state changes
   */
  public void onGameStateChanged() {}

  /**
   * Implement this to do something when a player moves
   *
   * @param move the player move
   */
  public void onPlayerMove(@NotNull Move move) {}

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
   * @param move the player move
   */
  final void notifyPlayerMove(@NotNull Move move) {
    history.appendMove(move);
    onPlayerMove(move);
  }

  /**
   * Notifies the observer that the game state has changed
   */
  final void notifyGameStateChanged() {
    onGameStateChanged();
  }
}
