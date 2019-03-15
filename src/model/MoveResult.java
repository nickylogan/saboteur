package model;

import model.cards.BoardActionCard.Type;

/**
 * The {@link MoveResult} class represents the result from a player move. This is
 * usually when applying a {@link Move} of type {@link Type#MAP}
 */
@SuppressWarnings("unused")
public class MoveResult {
  /** The contained result */
  private final Object result;

  /**
   * Creates a {@link MoveResult} wrapper for the given result
   *
   * @param result the actual result
   */
  MoveResult(Object result) { this.result = result; }

  /**
   * Returns the contained result
   *
   * @return the contained result
   */
  public Object result() { return this.result; }
}
