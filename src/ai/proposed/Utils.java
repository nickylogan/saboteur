/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.proposed;

import model.Board;
import model.Cell;
import model.GoalType;
import model.Position;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Utils {
  static Board.GoalPosition getGoldPosition(Map<Board.GoalPosition, GoalType> knownGoals) {
    if (!knownGoals.values().contains(GoalType.GOLD)) {
      return null;
    }
    if (knownGoals.get(Board.GoalPosition.TOP) == GoalType.GOLD) {
      return Board.GoalPosition.TOP;
    } else if (knownGoals.get(Board.GoalPosition.MIDDLE) == GoalType.GOLD) {
      return Board.GoalPosition.MIDDLE;
    } else {
      return Board.GoalPosition.BOTTOM;
    }
  }

  static int getGoalScenario(Map<Board.GoalPosition, GoalType> knownGoals) {
    if (!knownGoals.values().contains(GoalType.GOLD)) {
      return knownGoals.get(Board.GoalPosition.TOP) != null ? 0 : -1;
    }
    if (knownGoals.get(Board.GoalPosition.TOP) == GoalType.GOLD) {
      return 1;
    } else if (knownGoals.get(Board.GoalPosition.MIDDLE) == GoalType.GOLD) {
      return 2;
    } else {
      return 3;
    }
  }

  static Set<Position> getEmpty(Board board) {
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

  static long factorial(long n) {
    long res = 1;
    for (long i = 2; i < n; ++i) res *= i;
    return res;
  }
}
