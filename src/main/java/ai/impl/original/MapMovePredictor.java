/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 */

package ai.impl.original;

import model.Board;
import model.GameLogicController;
import model.GoalType;
import model.Move;

import java.util.*;

import static ai.impl.original.SaboteurAI.k1;

public class MapMovePredictor {
  static final double BASE_HEURISTIC = k1;

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
    double hTop = (1 + .5 * (top == null ? 0 : top == GoalType.GOLD ? 1 : -1)) * mapHeuristic;
    double hMid = (1 + .5 * (mid == null ? 0 : mid == GoalType.GOLD ? 1 : -1)) * mapHeuristic;
    double hBot = (1 + .5 * (bot == null ? 0 : bot == GoalType.GOLD ? 1 : -1)) * mapHeuristic;
    Map<Board.GoalPosition, Double> heuristics = new HashMap<>();
    heuristics.put(Board.GoalPosition.TOP, hTop);
    heuristics.put(Board.GoalPosition.MIDDLE, hMid);
    heuristics.put(Board.GoalPosition.BOTTOM, hBot);
    ArrayList<Board.GoalPosition> hArray = new ArrayList<>(heuristics.keySet());
    Collections.shuffle(hArray);
    hArray.sort(Collections.reverseOrder(Comparator.comparingDouble(heuristics::get)));

    Move move = Move.NewMapMove(playerIndex, cardIndex, hArray.get(0));
    double heuristic = heuristics.get(hArray.get(0));
    return new MoveHeuristic(move, heuristic);
  }
}
