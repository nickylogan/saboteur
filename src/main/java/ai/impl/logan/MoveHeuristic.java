package ai.impl.logan;

import model.Move;

class MoveHeuristic {
  final Move move;
  final double heuristic;

  MoveHeuristic(Move move, double heuristic) {
    this.move = move;
    this.heuristic = heuristic;
  }

  double heuristic() {
    return heuristic;
  }

  @Override
  public String toString() {
    return "{" +
           "m=" + move +
           ", h=" + heuristic +
           '}';
  }
}
