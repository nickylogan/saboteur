/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.impl.proposed;

import model.Board.GoalPosition;
import model.GoalType;
import model.Move;

import java.util.Optional;

public class MapMovePredictor {
  private static class PositionValue {
    private final GoalPosition pos;
    private final double value;

    private PositionValue(GoalPosition pos, double value) {
      this.pos = pos;
      this.value = value;
    }
  }

  static final double BASE_HEURISTIC = PathMovePredictor.MAX_PATH_HEURISTIC + 2.5;

  private final int playerIndex;
  private final double mapHeuristic;
  private final GoalKnowledge goalKnowledge;

  MapMovePredictor(int playerIndex, GoalKnowledge goalKnowledge) {
    this.playerIndex = playerIndex;
    this.mapHeuristic = BASE_HEURISTIC;
    this.goalKnowledge = goalKnowledge;
  }

  MoveHeuristic generateMapHeuristic(int cardIndex) {
    double hTop, hMid, hBot;
    if (goalKnowledge.containsGold()) {
      hTop = hMid = hBot = (1 + .5 * -5) * mapHeuristic;
    } else {
      Optional<GoalType> top = goalKnowledge.top();
      hTop = (1 + .5 * (top.isPresent() ? -5 : 1)) * mapHeuristic;

      Optional<GoalType> mid = goalKnowledge.mid();
      hMid = (1 + .5 * (mid.isPresent() ? -5 : 1)) * mapHeuristic;

      Optional<GoalType> bot = goalKnowledge.bottom();
      hBot = (1 + .5 * (bot.isPresent() ? -5 : 1)) * mapHeuristic;
    }

    PositionValue best = getBestPosition(hTop, hMid, hBot);
    Move move = Move.NewMapMove(playerIndex, cardIndex, best.pos);
    return new MoveHeuristic(move, best.value);
  }

  private PositionValue getBestPosition(double hTop, double hMid, double hBot) {
    GoalPosition pos;
    double heuristic;
    if (hTop >= hMid && hTop >= hBot) {
      pos = GoalPosition.TOP;
      heuristic = hTop;
    } else if (hMid >= hTop && hMid >= hBot) {
      pos = GoalPosition.MIDDLE;
      heuristic = hMid;
    } else {
      pos = GoalPosition.BOTTOM;
      heuristic = hBot;
    }

    return new PositionValue(pos, heuristic);
  }
}
