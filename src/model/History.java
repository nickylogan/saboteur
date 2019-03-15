package model;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;

public class History {
  private final ArrayList<Move> moves;

  public History() { moves = new ArrayList<>(); }

  final void appendMove(@NotNull Move move) { moves.add(move); }

  final void clear() { moves.clear(); }

  public final ArrayList<Move> moves() { return new ArrayList<>(moves); }
}
