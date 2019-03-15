package model;

import java.util.Objects;

/**
 * The {@link Position} class represents a position in the board.
 * <p>
 * The top-left most position is (0,0). <code>x</code> increases to the right
 * and <code>y</code> increases to the bottom
 * </p>
 */
@SuppressWarnings("WeakerAccess")
public class Position {
  public final int x, y;

  /**
   * Creates a <code>Position</code> object representing the specified
   * <code>x</code> and <code>y</code> position
   *
   * @param x the x position
   * @param y the y position
   */
  public Position(int x, int y) { this.x = x; this.y = y; }

  /**
   * Returns a position above the current one
   *
   * @return the top position
   */
  public final Position top() { return new Position(x, y - 1); }

  /**
   * Returns a position on the right of the current one
   *
   * @return the position on the right
   */
  public final Position right() { return new Position(x + 1, y); }

  /**
   * Returns a position below the current one
   *
   * @return the position on the bottom
   */
  public final Position bottom() { return new Position(x, y + 1); }

  /**
   * Returns a position on the left of the current one
   *
   * @return the position on the left
   */
  public final Position left() { return new Position(x - 1, y); }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Position position = (Position) o;
    return x == position.x && y == position.y;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(x, y);
  }
}
