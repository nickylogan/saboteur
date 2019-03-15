package model;

import com.sun.istack.internal.NotNull;
import model.cards.Card;
import model.cards.PathCard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** The {@link Cell} class represents a game board cell */
public class Cell implements Cloneable {
  /** The {@link Cell.Side} enum represents every possible side of a cell */
  public enum Side {
    ROCK(-2), EMPTY(-1), DEADEND(0), PATH(1);
    private final int val;

    Side(int val) { this.val = val; }

    public int val() { return val; }
  }

  /** The x position of the cell */
  private final int x;
  /** The y position of the cell */
  private final int y;
  /** The card placed on this cell */
  private Card card;
  /** An array of {@link Cell.Side} representing the top, right, bottom, and left sides respectively */
  private Side[] sides;

  /**
   * Creates a {@link Cell} object representing a game board cell
   *
   * @param x the x position of the cell
   * @param y the y position of the cell
   */
  public Cell(int x, int y) {
    this.x = x;
    this.y = y;
    this.sides = new Side[]{Side.EMPTY, Side.EMPTY, Side.EMPTY, Side.EMPTY};
  }

  /**
   * Creates a {@link Cell} copy of the specified cell object
   *
   * @param cell the cell to be copied
   */
  private Cell(Cell cell) {
    this(cell.x, cell.y);
    this.sides = cell.sides.clone();
    this.card = cell.card;
  }

  /**
   * Places a {@link PathCard} on the cell
   *
   * @param card the {@link PathCard} to be placed
   */
  final void placePathCard(@NotNull PathCard card) {
    PathCard cardCopy = card.copy();
    List<Side> newSides;
    if (cardCopy.type() == Card.Type.PATHWAY) {
      newSides = Arrays.stream(cardCopy.sides())
        .map(val -> val == PathCard.Side.PATH ? Side.PATH : Side.ROCK)
        .collect(Collectors.toList());
    } else { // DEADEND
      newSides = Arrays.stream(cardCopy.sides())
        .map(val -> val == PathCard.Side.DEADEND ? Side.DEADEND : Side.ROCK)
        .collect(Collectors.toList());
    }
    if (cardCopy.rotated()) {
      Collections.rotate(newSides, 2);
    }
    this.sides = (Side[]) newSides.toArray();
  }

  /**
   * Opens the cell's sides, making it available to be connected on any side
   */
  final void openAllSides() {
    this.sides = new Side[]{Side.PATH, Side.PATH, Side.PATH, Side.PATH};
  }

  /**
   * Removes the card contained in the cell, if any
   */
  final void removeCard() {
    this.card = null;
    this.sides = new Side[]{Side.EMPTY, Side.EMPTY, Side.EMPTY, Side.EMPTY};
  }

  /**
   * Checks whether a card is contained in this cell
   *
   * @return a boolean marking whether the cell contains a card
   */
  public final boolean hasCard() {
    return this.sides[0] != Side.EMPTY
           && this.sides[1] != Side.EMPTY
           && this.sides[2] != Side.EMPTY
           && this.sides[3] != Side.EMPTY;
  }

  /**
   * Returns an array of {@link Cell.Side} representing the top, right, bottom, and left sides respectively
   *
   * @return the cell's sides
   */
  public final Side[] sides() { return this.sides.clone(); }

  /**
   * Returns the top side value
   *
   * @return the top side value
   */
  public final Side topSide() { return sides[0]; }

  /**
   * Returns the right side value
   *
   * @return the right side value
   */
  public final Side rightSide() { return sides[1]; }

  /**
   * Returns the bottom side value
   *
   * @return the bottom side value
   */
  public final Side bottomSide() { return sides[2]; }

  /**
   * Returns the left side value
   *
   * @return the left side value
   */
  public final Side leftSide() { return sides[3]; }

  /**
   * Returns the card contained in this cell
   *
   * @return the cell's contained card
   */
  public final Card card() { return this.card; }

  /**
   * Returns the x position of the cell
   *
   * @return the cell's x position
   */
  public final int x() { return this.x; }

  /**
   * Returns the y position of the cell
   *
   * @return the cell's y position
   */
  public final int y() { return this.y; }

  /**
   * Returns a {@link Position} representing the cell's position
   *
   * @return the cell's position
   */
  public final Position pos() { return new Position(x, y); }

  /**
   * Creates a deep-copy {@link Cell} object of this cell
   * @return the {@link Cell} copy
   */
  public final Cell copy() {
    return new Cell(this);
  }
}
