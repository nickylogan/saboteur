package model;

import java.util.ArrayList;

@SuppressWarnings({"unused", "WeakerAccess"})
public class History {
  private final ArrayList<Move> moves;

  public History() { moves = new ArrayList<>(); }

  final void appendMove(Move move) { moves.add(move); }

  final void clear() { moves.clear(); }

  public final ArrayList<Move> moves() { return new ArrayList<>(moves); }
}
