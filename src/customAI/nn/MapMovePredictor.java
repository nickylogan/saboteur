/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package customAI.nn;

import model.Board;
import model.GameLogicController;
import model.GoalType;
import model.Move;

import java.util.Collection;
import java.util.Map;

public class MapMovePredictor {
  static final double BASE_HEURISTIC = PathMovePredictor.MAX_PATH_HEURISTIC + 2.5;

  private final GameLogicController game;
  private final int playerIndex;
  private double mapHeuristic;

  MapMovePredictor(GameLogicController game, int playerIndex) {
    this.game = game;
    this.playerIndex = playerIndex;
    this.mapHeuristic = BASE_HEURISTIC;
  }

  MoveHeuristic generateMapHeuristic(int cardIndex, Map<Board.GoalPosition, GoalType> knownGoals) {
    GoalType top = knownGoals.get(Board.GoalPosition.TOP);
    GoalType mid = knownGoals.get(Board.GoalPosition.MIDDLE);
    GoalType bot = knownGoals.get(Board.GoalPosition.BOTTOM);
    Collection<GoalType> goals = knownGoals.values();
    double htop = (1 + .5 * (goals.contains(GoalType.GOLD) || top != null ? -5 : 1)) * mapHeuristic;
    double hmid = (1 + .5 * (goals.contains(GoalType.GOLD) || mid != null ? -5 : 1)) * mapHeuristic;
    double hbot = (1 + .5 * (goals.contains(GoalType.GOLD) || bot != null ? -5 : 1)) * mapHeuristic;
    Board.GoalPosition pos;
    double heuristic;
    if (htop >= hmid && htop >= hbot) {
      pos = Board.GoalPosition.TOP;
      heuristic = htop;
    } else if (hmid >= htop && hmid >= hbot) {
      pos = Board.GoalPosition.MIDDLE;
      heuristic = hmid;
    } else {
      pos = Board.GoalPosition.BOTTOM;
      heuristic = hbot;
    }
    Move move = Move.NewMapMove(playerIndex, cardIndex, pos);
    return new MoveHeuristic(move, heuristic);
  }

  public void setMapHeuristic(double mapHeuristic) {
    this.mapHeuristic = mapHeuristic;
  }
}
