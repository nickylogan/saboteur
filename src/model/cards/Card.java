package model.cards;

/**
 * Card represents a playable card in a game. These are to be
 * handed out to players when a game round starts. Card should NOT include
 * start, goal, and gold cards
 */
public abstract class Card {
  /** The general type of the card */
  public enum Type {
    PATHWAY, DEADEND, MAP, ROCKFALL, BLOCK, REPAIR
  }

  /** The unique identifier of the card */
  private final int id;

  /**
   * Constructor for the abstract Card class
   *
   * @param id the unique identifier for a card
   */
  public Card(int id) {
    this.id = id;
  }

  /**
   * Returns the id of the card
   *
   * @return the card's id
   */
  public final int id() { return this.id; }

  /**
   * Returns the name of the card
   *
   * @return the card's name
   */
  public abstract String name();

  /**
   * Returns a {@link Card.Type} representing the general type of the card
   *
   * @return the card's general type
   */
  public abstract Type type();

  /**
   * Creates a deep copy {@link Card} object of the current card
   * @return the card copy
   */
  public abstract Card copy();
}
