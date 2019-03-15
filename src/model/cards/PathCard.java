package model.cards;

/**
 * The {@link PathCard} class represents a path card that can be played in the game.
 * Instances of this class are to be placed on the board.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PathCard extends Card {
  /** The {@link PathCard.Type} enum represents every valid path type in the game */
  public enum Type {
    CROSSROAD_PATH(
      Card.Type.PATHWAY,
      Side.PATH, Side.PATH, Side.PATH, Side.PATH),
    HORIZONTAL_T_PATH(
      Card.Type.PATHWAY,
      Side.ROCK, Side.PATH, Side.PATH, Side.PATH),
    VERTICAL_T_PATH(
      Card.Type.PATHWAY,
      Side.PATH, Side.ROCK, Side.PATH, Side.PATH),
    HORIZONTAL_PATH(
      Card.Type.PATHWAY,
      Side.ROCK, Side.PATH, Side.ROCK, Side.PATH),
    VERTICAL_PATH(
      Card.Type.PATHWAY,
      Side.PATH, Side.ROCK, Side.PATH, Side.ROCK),
    LEFT_TURN_PATH(
      Card.Type.PATHWAY,
      Side.ROCK, Side.ROCK, Side.PATH, Side.PATH),
    RIGHT_TURN_PATH(
      Card.Type.PATHWAY,
      Side.ROCK, Side.PATH, Side.PATH, Side.ROCK),
    CROSSROAD_DEADEND(
      Card.Type.DEADEND,
      Side.DEADEND, Side.DEADEND, Side.DEADEND, Side.DEADEND),
    HORIZONTAL_T_DEADEND(
      Card.Type.DEADEND,
      Side.ROCK, Side.DEADEND, Side.DEADEND, Side.DEADEND),
    VERTICAL_T_DEADEND(
      Card.Type.DEADEND,
      Side.DEADEND, Side.ROCK, Side.DEADEND, Side.DEADEND),
    BOTH_HORIZONTAL_DEADEND(
      Card.Type.DEADEND,
      Side.ROCK, Side.DEADEND, Side.ROCK, Side.DEADEND),
    BOTH_VERTICAL_DEADEND(
      Card.Type.DEADEND,
      Side.DEADEND, Side.ROCK, Side.DEADEND, Side.ROCK),
    SINGLE_HORIZONTAL_DEADEND(
      Card.Type.DEADEND,
      Side.ROCK, Side.ROCK, Side.ROCK, Side.DEADEND),
    SINGLE_VERTICAL_DEADEND(
      Card.Type.DEADEND,
      Side.ROCK, Side.ROCK, Side.DEADEND, Side.ROCK),
    LEFT_TURN_DEADEND(
      Card.Type.DEADEND,
      Side.ROCK, Side.ROCK, Side.DEADEND, Side.DEADEND),
    RIGHT_TURN_DEADEND(
      Card.Type.DEADEND,
      Side.ROCK, Side.DEADEND, Side.DEADEND, Side.ROCK);
    Card.Type type;
    Side[] sides;

    Type(Card.Type type, Side... sides) {
      this.type = type;
      this.sides = sides;
    }
  }

  /** The {@link PathCard.Side} enum represents every possible side value of a {@link PathCard} */
  public enum Side {
    ROCK(-2), DEADEND(0), PATH(1);
    private final int val;

    Side(int val) { this.val = val; }

    public int val() { return val; }
  }

  /** The specific type of the card */
  private final Type pathType;
  /** Marks whether the card is rotated from its default position */
  private boolean rotated;

  /**
   * Creates a {@link PathCard} object representing the specified
   * {@link PathCard.Type}.
   *
   * @param id       the unique identifier for the created card
   * @param pathType the path type
   */
  public PathCard(int id, Type pathType) {
    super(id);
    this.pathType = pathType;
  }

  /**
   * Creates a {@link PathCard} object copy based on another card
   *
   * @param card the card to be copied
   */
  private PathCard(PathCard card) {
    this(card.id(), card.pathType);
    if (card.rotated()) this.rotate();
  }

  /**
   * Toggles the rotation of the path card
   */
  public final void rotate() { rotated = !rotated; }

  /**
   * Returns the path type of the card
   *
   * @return the card's path type
   */
  public final Type pathType() { return pathType; }

  /**
   * Returns the rotation of the card
   *
   * @return the card's rotation
   */
  public final boolean rotated() { return this.rotated; }

  /**
   * Returns an array of {@link PathCard.Side} representing the card's
   * top, right, bottom, and left sides respectively
   *
   * @return the sides of the card
   */
  public final Side[] sides() {
    Side[] sides = pathType.sides.clone();
    if (rotated) {
      return new Side[]{sides[2], sides[3], sides[0], sides[1]};
    }
    return sides;
  }

  /**
   * Returns the top side value
   *
   * @return the top side value
   */
  public final Side topSide() { return pathType.sides[0]; }

  /**
   * Returns the right side value
   *
   * @return the right side value
   */
  public final Side rightSide() { return pathType.sides[1]; }

  /**
   * Returns the bottom side value
   *
   * @return the bottom side value
   */
  public final Side bottomSide() { return pathType.sides[2]; }

  /**
   * Returns the left side value
   *
   * @return the left side value
   */
  public final Side leftSide() { return pathType.sides[3]; }

  @Override
  public final String name() { return pathType.name(); }

  @Override
  public final Card.Type type() { return pathType.type; }

  @Override
  public final PathCard copy() {
    return new PathCard(this);
  }
}
