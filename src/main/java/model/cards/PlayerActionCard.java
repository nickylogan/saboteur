package model.cards;

import model.Tool;

/**
 * The {@link PlayerActionCard} class represents a player-action card that can be played in the game.
 * Instances of this class are to be played on a player.
 */
@SuppressWarnings("unused")
public class PlayerActionCard extends Card {
  /**
   * The {@link PlayerActionCard.Type} enum represents every valid player-action
   * type in the game
   */
  public enum Type {
    BLOCK_CART(Card.Type.BLOCK, Tool.CART),
    BLOCK_LANTERN(Card.Type.BLOCK, Tool.LANTERN),
    BLOCK_PICKAXE(Card.Type.BLOCK, Tool.PICKAXE),
    REPAIR_CART(Card.Type.REPAIR, Tool.CART),
    REPAIR_LANTERN(Card.Type.REPAIR, Tool.LANTERN),
    REPAIR_PICKAXE(Card.Type.REPAIR, Tool.PICKAXE),
    REPAIR_CART_LANTERN(Card.Type.REPAIR, Tool.CART, Tool.LANTERN),
    REPAIR_CART_PICKAXE(Card.Type.REPAIR, Tool.CART, Tool.PICKAXE),
    REPAIR_LANTERN_PICKAXE(Card.Type.REPAIR, Tool.LANTERN, Tool.PICKAXE);
    Card.Type type;
    Tool[] effects;

    Type(Card.Type type, Tool... effects) {
      this.type = type;
      this.effects = effects;
    }
  }

  /** The specific type of the player-action card */
  private final Type playerActionType;

  /**
   * Creates a {@link PlayerActionCard} object representing the
   * specified {@link PlayerActionCard.Type}
   *
   * @param id               the unique identifier for the created card
   * @param playerActionType the player-action type
   */
  public PlayerActionCard(int id, Type playerActionType) {
    super(id);
    this.playerActionType = playerActionType;
  }

  /**
   * Creates a {@link PlayerActionCard} object copy based on another card
   *
   * @param card the card to be copied
   */
  private PlayerActionCard(PlayerActionCard card) {
    this(card.id(), card.playerActionType);
  }

  /**
   * Returns the player-action type of the card
   *
   * @return the card's player-action type
   */
  public final Type playerActionType() {
    return playerActionType;
  }

  /**
   * Returns an array of {@link Tool} representing the effects of
   * the player-action card
   *
   * @return the effects of the card
   */
  public final Tool[] effects() {
    return this.playerActionType.effects.clone();
  }

  @Override
  public final String name() {
    return this.playerActionType.name();
  }

  @Override
  public final Card.Type type() {
    return this.playerActionType.type;
  }

  @Override
  public final PlayerActionCard copy() {
    return new PlayerActionCard(this);
  }
}
