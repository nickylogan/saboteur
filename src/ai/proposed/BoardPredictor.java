/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.proposed;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static ai.proposed.SaboteurAI.VERBOSE;

public class BoardPredictor {
  private static final int K = 5;
  private final GameLogicController game;
  private final Player.Role role;

  BoardPredictor(GameLogicController game, Player.Role role) {
    this.game = game;
    this.role = role;
  }

  private double calculateCellValue(Position p, Map<Board.GoalPosition, GoalType> knownGoals) {
    int scenario = Utils.getGoalScenario(knownGoals);

    double hy = 1.0;
    if (role == Player.Role.GOLD_MINER) {
      switch (scenario) {
        case 0: hy = Math.min(0.5 * p.y, 1); break;
        case 1: hy = 1 - .25 * p.y; break;
        case 2: hy = 1 - Math.abs(1 - .5 * p.y); break;
        case 3: hy = .25 * p.y; break;
      }
    } else {
      switch (scenario) {
        case 0: hy = Math.max(1 - 0.5 * p.y, 0); break;
        case 1: hy = 1 / 16.0 * p.y * p.y; break;
        case 2: hy = Math.abs(1 - .5 * p.y); break;
        case 3: hy = 1 / 16.0 * (p.y - 4) * (p.y - 4); break;
      }
    }
    double hx = 0.1 * (role == Player.Role.GOLD_MINER ? 1 + p.x : 9 - p.x) / (scenario == 0 ? 2 : 1);
    double hxy = hx * hy;
    if (VERBOSE > 1)
      System.out.printf("h(%d)=%f, h(%d)=%f, h(%d,%d)=%f\n", p.x, hx, p.y, hy, p.x, p.y, hxy);
    return hxy;
  }

  double calculateAverageValue(Board board, Map<Board.GoalPosition, GoalType> knownGoals) {
    double sum = 0;
    Set<Position> reachable = board.getReachable();
    reachable.retainAll(Utils.getEmpty(board));
    if (board.isReachable(board.topGoalPosition())) reachable.add(board.topGoalPosition());
    if (board.isReachable(board.middleGoalPosition())) reachable.add(board.middleGoalPosition());
    if (board.isReachable(board.bottomGoalPosition())) reachable.add(board.bottomGoalPosition());

    ArrayList<Double> values = new ArrayList<>();
    for (Position ps : reachable) {
      values.add(calculateCellValue(ps, knownGoals));
    }
    values.sort(Collections.reverseOrder());
    int k = role == Player.Role.GOLD_MINER ? Math.min(values.size(), K) : values.size();
    for(int i = 0; i < k; ++i) {
      sum += values.get(i);
    }

    double value = reachable.isEmpty() ? 0 : sum / values.size();
    if (VERBOSE > 1) System.out.println("avg = " + value);
    return value;
  }

  double calculateMaxValue(Board board, Map<Board.GoalPosition, GoalType> knownGoals) {
    double max = Double.NEGATIVE_INFINITY;
    Set<Position> reachable = board.getReachable();
    reachable.retainAll(Utils.getEmpty(board));
    if (board.isReachable(board.topGoalPosition())) reachable.add(board.topGoalPosition());
    if (board.isReachable(board.middleGoalPosition())) reachable.add(board.middleGoalPosition());
    if (board.isReachable(board.bottomGoalPosition())) reachable.add(board.bottomGoalPosition());
    for (Position ps : reachable) {
      double val = calculateCellValue(ps, knownGoals);
      if (val - max > SaboteurAI.EPS) max = val;
    }
    if (VERBOSE > 1) System.out.println("max = " + max);
    return max;
  }
}
