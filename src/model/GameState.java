package model;

import model.cards.Card;

import java.util.ArrayList;
import java.util.Stack;

/**
 * The {@link GameState} class represents a game state. It doesn't contain any game
 * flow or logic. Rather, it only stores the state of the current running game.
 *
 * @see GameLogicController for the logic
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class GameState {

  /** Number of players in the game */
  private int numPlayers;
  /** All players that joined the game */
  private ArrayList<Player> players;
  /** The current game board */
  private Board board;
  /** Marks the game as started */
  private boolean started;
  /** Marks the game as finished */
  private boolean finished;
  /** The current card deck */
  private Stack<Card> deck;
  /** The current turn */
  private int currentPlayerIndex;
  /** The non player observers of the game */
  private ArrayList<GameObserver> nonPlayerObservers;

  /**
   * Sets the players joining the game
   *
   * @param players the players
   */
  public final void setPlayers(ArrayList<Player> players) {
    this.players = players; this.numPlayers = players.size();
  }

  /**
   * Sets the deck
   *
   * @param deck the deck
   */
  public final void setDeck(Stack<Card> deck) {
    this.deck = deck;
  }

  /**
   * Sets the board
   *
   * @param board the board
   */
  public final void setBoard(Board board) {
    this.board = board;
  }

  /**
   * Sets the current player index
   *
   * @param currentPlayerIndex the index
   */
  public final void setCurrentPlayerIndex(int currentPlayerIndex) {
    this.currentPlayerIndex = currentPlayerIndex;
  }

  /**
   * Set the game's started status from the specified flag
   *
   * @param started the finished flag
   */
  public final void setStarted(boolean started) {
    this.started = started;
  }

  /**
   * Set the game's finished status from the specified flag
   *
   * @param finished the finished flag
   */
  public void setFinished(boolean finished) {
    this.finished = finished;
  }

  /**
   * Increments the current player index, and wraps around <code>numPlayers</code>
   */
  public final void incrementPlayerIndex() {
    int i = 0;
    do {
      currentPlayerIndex = (currentPlayerIndex + 1) % numPlayers;
    } while (currentPlayer().handSize() == 0 && ++i < numPlayers);
  }

  /**
   * Returns the current player index
   *
   * @return the current player index
   */
  public final int currentPlayerIndex() {
    return this.currentPlayerIndex;
  }

  /**
   * Returns a reference to the current {@link Player}
   *
   * @return the current player
   */
  public final Player currentPlayer() { return players.get(currentPlayerIndex); }

  /**
   * Returns a player of the specified index
   *
   * @param index the specified index
   * @return the requested player
   */
  public final Player playerAt(int index) {
    return players.get(index);
  }

  /**
   * Returns the game's started flag
   *
   * @return a boolean representing the game's started flag
   */
  public final boolean started() { return this.started; }

  /**
   * Returns finished flag of game
   *
   * @return a boolean representing the game's finished flag
   */
  public final boolean finished() { return this.finished; }

  /**
   * Returns the total number of players in the current game
   *
   * @return number of players in the current game
   */
  public final int numPlayers() { return this.numPlayers; }

  /**
   * Returns the total number of saboteurs in the current game
   *
   * @return number of saboteurs in the current game
   */
  public final int numSaboteurs() {
    return (int) this.players.stream().filter(p -> p.role() == Player.Role.SABOTEUR).count();
  }

  /**
   * Returns the list of players in the game
   *
   * @return the requested player
   */
  public final ArrayList<Player> players() {
    return this.players;
  }

  /**
   * Returns the current card deck
   *
   * @return the card deck
   */
  public final Stack<Card> deck() { return this.deck; }

  /**
   * Returns the current game board
   *
   * @return the current game board
   */
  public final Board board() { return board; }
}
