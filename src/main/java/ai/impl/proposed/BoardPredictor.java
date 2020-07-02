/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.impl.proposed;

import ai.impl.proposed.GoalKnowledge.Scenario;
import ai.impl.proposed.utils.DoubleUtils;
import model.Board;
import model.Cell;
import model.Player;
import model.Position;

import java.util.*;
import java.util.stream.Collectors;

public class BoardPredictor {
  private final GoalKnowledge goalKnowledge;
  private static final int K = 5;
  private final Player.Role role;

  BoardPredictor(Player.Role role, GoalKnowledge goalKnowledge) {
    this.role = role;
    this.goalKnowledge = goalKnowledge;
  }

  double calcDiff(Board before, Board after) {
    double maxBefore = this.calcMaxValue(before);
    double maxAfter = this.calcMaxValue(after);

    // prefer difference of max
    if (DoubleUtils.compare(maxAfter, maxBefore) != 0) {
      return maxAfter - maxBefore;
    }

    double avgBefore = this.calcAvgValue(before);
    double avgAfter = this.calcAvgValue(after);

    return avgAfter - avgBefore;
  }

  private double calcAvgValue(Board board) {
    Set<Position> reachable = getReachable(board);
    if (reachable.isEmpty()) return 0.0;

    int k = role == Player.Role.GOLD_MINER ?
        Math.min(reachable.size(), K) :
        reachable.size();

    List<Double> kMax = reachable.stream()
        .map(this::calcCellValue)
        .sorted(Comparator.reverseOrder())
        .collect(Collectors.toList())
        .subList(0, k);

    OptionalDouble average = kMax.stream()
        .mapToDouble(Double::doubleValue)
        .average();

    return average.orElse(0.0);
  }

  private double calcMaxValue(Board board) {
    Set<Position> reachable = getReachable(board);
    if (reachable.isEmpty()) return Double.NEGATIVE_INFINITY;

    Optional<Double> max = reachable.stream()
        .map(this::calcCellValue)
        .max(Double::compareTo);

    return max.get();
  }

  private double calcCellValue(Position p) {
    Scenario scenario = goalKnowledge.getScenario();

    double hy = calcYValue(p, scenario);
    double hx = calcXValue(p);

    return hx * hy;
  }

  private double calcYValue(Position p, Scenario scenario) {
    double hy = 1.0;

    switch (scenario) {
      case TOP_ROCK:
        hy = role == Player.Role.GOLD_MINER ?
            Math.min(0.5 * p.y, 1) :    // prefer mid-bottom area (miner)
            Math.max(1 - 0.5 * p.y, 0); // prefer top area (saboteur)
        break;
      case TOP_GOLD:
        hy = role == Player.Role.GOLD_MINER ?
            1 - .25 * p.y :       // prefer top area (miner)
            1 / 16.0 * p.y * p.y; // prefer bottom area (saboteur)
        break;
      case MID_GOLD:
        hy = role == Player.Role.GOLD_MINER ?
            1 - Math.abs(1 - .5 * p.y) : // prefer mid area (miner)
            Math.abs(1 - .5 * p.y);      // prefer top/bottom area (saboteur)
        break;
      case BOT_GOLD:
        hy = role == Player.Role.GOLD_MINER ?
            .25 * p.y :                       // prefer bottom area (miner)
            1 / 16.0 * (p.y - 4) * (p.y - 4); // prefer top area (saboteur)
        break;
    }

    return hy;
  }

  private double calcXValue(Position pos) {
    double baseValue = role == Player.Role.GOLD_MINER ?
        1 + pos.x : // prefer moving forward (miner)
        9 - pos.x;  // prefer moving backwards (saboteur)

    return 0.1 * baseValue;
  }

  private static Set<Position> getReachable(Board board) {
    Set<Position> reachable = board.getReachable();
    reachable.retainAll(getEmpty(board));

    if (board.isReachable(board.topGoalPosition()))
      reachable.add(board.topGoalPosition());
    if (board.isReachable(board.middleGoalPosition()))
      reachable.add(board.middleGoalPosition());
    if (board.isReachable(board.bottomGoalPosition()))
      reachable.add(board.bottomGoalPosition());

    return reachable;
  }

  private static Set<Position> getEmpty(Board board) {
    Set<Position> empty = new HashSet<>();
    for (int x = 0; x < board.width(); x++) {
      for (int y = 0; y < board.height(); y++) {
        Cell cell = board.cellAt(x, y);
        if (cell == null) continue;
        if (!cell.hasCard()) empty.add(new Position(x, y));
      }
    }
    return empty;
  }
}
