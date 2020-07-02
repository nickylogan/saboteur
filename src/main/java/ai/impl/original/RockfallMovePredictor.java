/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 */

package ai.impl.original;

import model.*;

import java.util.*;

import static ai.impl.original.SaboteurAI.EPS;
import static ai.impl.original.SaboteurAI.k2;

public class RockfallMovePredictor {
  static final double BASE_HEURISTIC = k2;

  private final GameLogicController game;
  private final int playerIndex;
  private final Player.Role role;
  private double rockfallHeuristic;

  RockfallMovePredictor(GameLogicController game, int playerIndex, Player.Role role) {
    this.game = game;
    this.playerIndex = playerIndex;
    this.role = role;
    this.rockfallHeuristic = BASE_HEURISTIC;
  }

  MoveHeuristic generateRockfallHeuristic(int cardIndex, Map<Board.GoalPosition, GoalType> knownGoals) {
    // System.out.println("ROCKFALL");
    Map<Position, Double> cellHeuristics = new HashMap<>();
    Set<Position> destroyable = game.board().getDestroyable();
    destroyable.forEach(p -> {
      double heuristic;
      if (p.x < 6) {
        heuristic = .5 * rockfallHeuristic;
      } else {
        int[] a = getSideValues(p);
        heuristic = a[0] + a[1] + a[2] + a[3];
      }
      cellHeuristics.put(p, heuristic);
    });
    if (destroyable.isEmpty()) {
      Move move = Move.NewRockfallMove(playerIndex, cardIndex, -1, -1);
      return new MoveHeuristic(move, k2 / 2);
    }
    // System.out.println(cellHeuristics);
    Map<Position, Double> possibleChoice = new HashMap<>();
    possibleChoice.put(
      new Position(-1, -1),
      role == Player.Role.SABOTEUR ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY
    );
    for (Map.Entry<Position, Double> e : cellHeuristics.entrySet()) {
      double curr = role == Player.Role.GOLD_MINER
        ? Collections.min(possibleChoice.values())
        : Collections.max(possibleChoice.values());
      if (role == Player.Role.GOLD_MINER) {
        if (e.getValue() - curr > EPS) possibleChoice.clear();
        if (e.getValue() - curr >= -EPS) possibleChoice.put(e.getKey(), e.getValue());
      }

    }
    List<Position> choiceArray = new ArrayList<>(possibleChoice.keySet());
    Random r = new Random();
    Position p = choiceArray.get(r.nextInt(choiceArray.size()));
    double heuristic = Math.abs(possibleChoice.get(p));

    Move move = Move.NewRockfallMove(playerIndex, cardIndex, p.x, p.y);
    return new MoveHeuristic(move, heuristic);
  }

  private int[] getSideValues(Position p) {
    int[] sides = new int[]{-1, -1, -1, -1};
    Board board = game.board();
    Cell cell = board.cellAt(p);
    if (cell == null) return sides;
    sides[0] = cell.topSide().val();
    sides[1] = cell.rightSide().val();
    sides[2] = cell.bottomSide().val();
    sides[3] = cell.leftSide().val();
    Cell top = board.cellAt(p.top());
    Cell right = board.cellAt(p.right());
    Cell bottom = board.cellAt(p.bottom());
    Cell left = board.cellAt(p.left());

    if (top != null && top.bottomSide() == Cell.Side.PATH && cell.topSide() == Cell.Side.PATH)
      sides[0] = 2;
    if (right != null && right.leftSide() == Cell.Side.PATH && cell.rightSide() == Cell.Side.PATH)
      sides[1] = 2;
    if (bottom != null && bottom.topSide() == Cell.Side.PATH && cell.bottomSide() == Cell.Side.PATH)
      sides[2] = 2;
    if (left != null && left.rightSide() == Cell.Side.PATH && cell.leftSide() == Cell.Side.PATH)
      sides[3] = 2;

    return sides;
  }
}
