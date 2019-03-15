package model.cards;

/**
 * The {@link BoardActionCard} class represents a board-action card that can be played in the game.
 * Instances of this class are to be played on the board.
 */
public class BoardActionCard extends Card {
  /**
   * The {@link BoardActionCard.Type} enum represents every valid board-action
   * type in the game
   */
  public enum Type {
    MAP(Card.Type.MAP), ROCKFALL(Card.Type.ROCKFALL);
    Card.Type type;

    Type(Card.Type type) {
      this.type = type;
    }
  }

  /** The specific type of the board-action card */
  private Type boardActionType;

  /**
   * Creates a {@link BoardActionCard} object representing the
   * specified {@link BoardActionCard.Type}
   *
   * @param id              the unique identifier for the created card.
   * @param boardActionType the board-action type
   */
  public BoardActionCard(int id, Type boardActionType) {
    super(id);
    this.boardActionType = boardActionType;
  }

  /**
   * Creates a {@link BoardActionCard} object copy based on another card
   *
   * @param card the card to be copied
   */
  private BoardActionCard(BoardActionCard card) {
    this(card.id(), card.boardActionType);
  }

  /**
   * Returns the specific type of the board-action card
   *
   * @return the card's board-action type
   */
  public final Type boardActionType() {
    return boardActionType;
  }

  @Override
  public final Card.Type type() {
    return this.boardActionType.type;
  }

  @Override
  public final String name() {
    return this.boardActionType.name();
  }

  @Override
  public final BoardActionCard copy () {
    return new BoardActionCard(this);
  }
}
