package model;

import model.cards.Card;

import java.util.Arrays;

/**
 * The {@link Move} class represents a player movement.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Move {
  /**
   * The {@link Move.Type} enum represents all valid player movement types
   */
  public enum Type {
    PLAY_PATH,
    PLAY_MAP,
    PLAY_ROCKFALL,
    PLAY_PLAYER,
    DISCARD
  }

  /** The movement type */
  private final Type type;
  /** The playing player */
  private final int playerIndex;
  /** The played card index */
  private final int handIndex;
  /**
   * <code>args</code> depends on the player movement:
   * <ul>
   * <li>When a card is discarded, <code>args</code> is empty</li>
   * <li>When a path card is played, <code>args</code> contains the <code>x, y</code> position</li>,
   * along with <code> 0</code> or <code>1</code>, marking if the card is rotated
   * <li>When a map card is played, <code>args</code> only contains the goal position <code>(0, 1, or 2)</code></li>
   * <li>When a rockfall card is played, <code>args</code> contains the target <code>x, y</code> position</li>
   * <li>When a block/repair card is played, <code>args</code> contains the target player index</li>
   * </ul>
   */
  private int[] args;
  /** The played card, if any */
  private Card card;
  /** The game state change, if any */
  private StateDelta delta;

  /**
   * Creates a {@link Move} object based on the specified parameters. Do not use this constructor
   * to create a Move object. Instead, use either {@link Move#NewPathMove}, {@link Move#NewMapMove},
   * {@link Move#NewRockfallMove}, {@link Move#NewPlayerActionMove}, or {@link Move#NewDiscardMove}.
   *
   * @param type        the move type
   * @param playerIndex the playing player index
   * @param handIndex   the played card index
   * @param args        see {@link Move#args}
   */
  private Move(Type type, int playerIndex, int handIndex, int... args) {
    this.type = type;
    this.playerIndex = playerIndex;
    this.handIndex = handIndex;
    this.args = args;
  }

  /**
   * Creates a {@link Move} object representing a path card move
   *
   * @param playerIndex the playing player index
   * @param handIndex   the index from the player's hand
   * @param x           the target x position
   * @param y           the target y position
   * @param rotated     flag marking card is supposed to be rotated
   * @return a {@link Move} object representing the move
   */
  public static Move NewPathMove(int playerIndex, int handIndex, int x, int y, boolean rotated) {
    return new Move(Type.PLAY_PATH, playerIndex, handIndex, x, y, rotated ? 1 : 0);
  }

  /**
   * Creates a {@link Move} object representing a map card move
   *
   * @param playerIndex the playing player index
   * @param handIndex   the index from the player's hand
   * @param goalPos     the target goal position, which is either <code>0</code>, <code>1</code>,
   *                    or <code>2</code>
   * @return a {@link Move} object representing the move
   */
  public static Move NewMapMove(int playerIndex, int handIndex, Board.GoalPosition goalPos) {
    return new Move(Type.PLAY_MAP, playerIndex, handIndex, goalPos.ordinal());
  }

  /**
   * Creates a {@link Move} object representing a rockfall card move
   *
   * @param playerIndex the playing player index
   * @param handIndex   the index from the player's hand
   * @param x           the target x position
   * @param y           the target y position
   * @return a {@link Move} object representing the move
   */
  public static Move NewRockfallMove(int playerIndex, int handIndex, int x, int y) {
    return new Move(Type.PLAY_ROCKFALL, playerIndex, handIndex, x, y);
  }

  /**
   * Creates a {@link Move} object representing a player-action card move
   *
   * @param playerIndex       the playing player index
   * @param handIndex         the index from the player's hand
   * @param targetPlayerIndex the targeted player index
   * @return a {@link Move} object representing the move
   */
  public static Move NewPlayerActionMove(int playerIndex, int handIndex, int targetPlayerIndex) {
    return new Move(Type.PLAY_PLAYER, playerIndex, handIndex, targetPlayerIndex);
  }

  /**
   * Creates a {@link Move} object representing a discard move
   *
   * @param playerIndex the playing player index
   * @param handIndex   the index from the player's hand
   * @return a {@link Move} object representing the move
   */
  public static Move NewDiscardMove(int playerIndex, int handIndex) {
    return new Move(Type.DISCARD, playerIndex, handIndex);
  }

  /**
   * Sets the referenced card
   */
  final void setCard(Card card) { this.card = card.copy(); }

  /**
   * Sets the changes to the game state
   *
   * @param delta the game state change
   */
  final void setDelta(StateDelta delta) { this.delta = delta; }

  /**
   * Returns the movement type
   *
   * @return the movement type
   */
  public final Type type() { return this.type; }

  /**
   * Returns the playing player index
   *
   * @return the playing player index
   */
  public final int playerIndex() { return this.playerIndex; }

  /**
   * Returns the player hand index
   *
   * @return the player hand index
   */
  public final int handIndex() { return this.handIndex; }

  /**
   * Returns the movement args
   *
   * @return the movement args
   */
  public final int[] args() { return this.args.clone(); }

  /**
   * Returns the played card
   *
   * @return the played card
   */
  public final Card card() { return this.card; }

  /**
   * Returns the state delta from the move
   *
   * @return the state delta
   */
  public final StateDelta delta() { return this.delta; }

  @Override
  public String toString() {
    return "Move{" +
           "type=" + type +
           ", playerIndex=" + playerIndex +
           ", handIndex=" + handIndex +
           ", args=" + Arrays.toString(args) +
           ", card=" + card +
           '}';
  }
}
