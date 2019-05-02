/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package customAI.nn;

import model.*;
import model.cards.Card;
import model.cards.PathCard;

import java.util.*;

import static customAI.nn.CustomAI.EPS;

public class RockfallMovePredictor {
  static final double BASE_MULTIPLIER = 50;

  private final GameLogicController game;
  private final int playerIndex;
  private final Player.Role role;
  private double rockfallMultiplier;
  private final CustomAI ai;

  RockfallMovePredictor(GameLogicController game, int playerIndex, Player.Role role, CustomAI ai) {
    this.game = game;
    this.playerIndex = playerIndex;
    this.role = role;
    this.ai = ai;
    this.rockfallMultiplier = BASE_MULTIPLIER;
  }

  MoveHeuristic generateRockfallHeuristic(int cardIndex, Map<Board.GoalPosition, GoalType> knownGoals, ArrayList<Card> hand) {
    // System.out.println("ROCKFALL");
    Map<Position, Double> cellHeuristics = new HashMap<>();
    Set<Position> destroyable = game.board().getDestroyable();
    destroyable.forEach(p -> {
      Board old = game.board();
      Board simulated = game.board().simulateRemoveCardAt(p.x, p.y);
      if (simulated == null) return;

      // Calculate present value
      double oldMax = ai.boardPredictor.calculateMaxValue(old, knownGoals);
      double oldAvg = ai.boardPredictor.calculateAverageValue(old, knownGoals);
      double simMax = ai.boardPredictor.calculateMaxValue(simulated, knownGoals);
      double simAvg = ai.boardPredictor.calculateAverageValue(simulated, knownGoals);
      double presentValue;
      if (Math.abs(simMax - oldMax) <= EPS) {
        presentValue = (simAvg - oldAvg) * rockfallMultiplier;
      } else {
        presentValue = (simMax - oldMax) * rockfallMultiplier;
      }

      if (role == Player.Role.SABOTEUR) {
        cellHeuristics.put(p, presentValue);
        return;
      }

      // Calculate future value
      double futureValue = Double.NEGATIVE_INFINITY;
      for (Card card : hand) {
        if (!(card instanceof PathCard)) continue;
        PathCard path = (PathCard) card;
        MoveHeuristic mh = ai.pathMovePredictor.generatePathHeuristic(-1, path, simulated, knownGoals);
        futureValue = Math.max(mh.heuristic, futureValue);
      }

      cellHeuristics.put(p, Math.max(futureValue, presentValue));
    });
    if (destroyable.isEmpty()) {
      Move move = Move.NewRockfallMove(playerIndex, cardIndex, -1, -1);
      return new MoveHeuristic(move, PathMovePredictor.MAX_PATH_HEURISTIC / 2);
    }
    // System.out.println(cellHeuristics);
    Map<Position, Double> possibleChoice = new HashMap<>();
    possibleChoice.put(
      new Position(-1, -1),
      role == Player.Role.SABOTEUR ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY
    );
    for (Map.Entry<Position, Double> e : cellHeuristics.entrySet()) {
      double curr = role == Player.Role.GOLD_MINER
        ? Collections.max(possibleChoice.values())
        : Collections.min(possibleChoice.values());
      if (e.getValue() - curr > EPS) possibleChoice.clear();
      if (e.getValue() - curr >= -EPS) possibleChoice.put(e.getKey(), e.getValue());
    }
    List<Position> choiceArray = new ArrayList<>(possibleChoice.keySet());
    Random r = new Random();
    Position p = choiceArray.get(r.nextInt(choiceArray.size()));
    double heuristic = Math.abs(possibleChoice.get(p));

    Move move = Move.NewRockfallMove(playerIndex, cardIndex, p.x, p.y);
    return new MoveHeuristic(move, heuristic);
  }
}
