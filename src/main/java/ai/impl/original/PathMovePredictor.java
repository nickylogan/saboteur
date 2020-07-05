/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 */

package ai.impl.original;

import javafx.util.Pair;
import model.*;
import model.cards.Card;
import model.cards.PathCard;

import java.util.*;

import static ai.impl.original.SaboteurAI.EPS;
import static ai.impl.original.SaboteurAI.k0;

@SuppressWarnings("Duplicates")
class PathMovePredictor {
  private static Map<PathCard.Type, Double> BASE_HEURISTIC = new HashMap<>();

  static {
    BASE_HEURISTIC.put(PathCard.Type.CROSSROAD_PATH, k0);
    BASE_HEURISTIC.put(PathCard.Type.HORIZONTAL_T_PATH, k0);
    BASE_HEURISTIC.put(PathCard.Type.VERTICAL_T_PATH, k0);
    BASE_HEURISTIC.put(PathCard.Type.HORIZONTAL_PATH, k0);
    BASE_HEURISTIC.put(PathCard.Type.VERTICAL_PATH, k0);
    BASE_HEURISTIC.put(PathCard.Type.LEFT_TURN_PATH, k0);
    BASE_HEURISTIC.put(PathCard.Type.RIGHT_TURN_PATH, k0);
    BASE_HEURISTIC.put(PathCard.Type.CROSSROAD_DEADEND, k0);
    BASE_HEURISTIC.put(PathCard.Type.HORIZONTAL_T_DEADEND, k0);
    BASE_HEURISTIC.put(PathCard.Type.VERTICAL_T_DEADEND, k0);
    BASE_HEURISTIC.put(PathCard.Type.BOTH_HORIZONTAL_DEADEND, k0);
    BASE_HEURISTIC.put(PathCard.Type.BOTH_VERTICAL_DEADEND, k0);
    BASE_HEURISTIC.put(PathCard.Type.SINGLE_HORIZONTAL_DEADEND, k0);
    BASE_HEURISTIC.put(PathCard.Type.SINGLE_VERTICAL_DEADEND, k0);
    BASE_HEURISTIC.put(PathCard.Type.LEFT_TURN_DEADEND, k0);
    BASE_HEURISTIC.put(PathCard.Type.RIGHT_TURN_DEADEND, k0);
  }

  private final GameLogicController game;
  private final int playerIndex;
  private final Player.Role role;

  private final Map<PathCard.Type, Double> pathHeuristic;

  PathMovePredictor(GameLogicController game, int playerIndex, Player.Role role) {
    this.game = game;
    this.playerIndex = playerIndex;
    this.role = role;
    this.pathHeuristic = BASE_HEURISTIC;
  }

  MoveHeuristic generatePathHeuristic(
    int cardIndex, PathCard card,
    Board board, Map<Board.GoalPosition, GoalType> knownGoals
  ) {
    Map<Position, Double> nonRotatedCellHeuristics = generatePlacementHeuristics(card, false, board, knownGoals);
    Map<Position, Double> rotatedCellHeuristics = generatePlacementHeuristics(card, true, board, knownGoals);

    if (nonRotatedCellHeuristics.isEmpty() && rotatedCellHeuristics.isEmpty()) {
      Move move = Move.NewPathMove(playerIndex, cardIndex, -1, -1, false);
      double heuristic = pathHeuristic.get(card.pathType());
      return new MoveHeuristic(move, (role == Player.Role.GOLD_MINER ? heuristic : 1 - heuristic) / 2);
    }

    Map<Pair<Position, Boolean>, Double> possibleChoice = new HashMap<>();
    possibleChoice.put(new Pair<>(new Position(-1, -1), false), Double.NEGATIVE_INFINITY);

    for (Map.Entry<Position, Double> e : nonRotatedCellHeuristics.entrySet()) {
      double best = Collections.max(possibleChoice.values());
      double val = e.getValue();
      if (val - best > EPS) possibleChoice.clear();
      if (val - best >= -EPS)
        possibleChoice.put(new Pair<>(e.getKey(), true), e.getValue());
    }

    for (Map.Entry<Position, Double> e : rotatedCellHeuristics.entrySet()) {
      double best = Collections.max(possibleChoice.values());
      double val = e.getValue();
      if (val - best > EPS) possibleChoice.clear();
      if (val - best >= -EPS)
        possibleChoice.put(new Pair<>(e.getKey(), true), e.getValue());
    }

    ArrayList<Pair<Position, Boolean>> choiceArray = new ArrayList<>(possibleChoice.keySet());
    Random r = new Random();
    Pair<Position, Boolean> chosen = choiceArray.get(r.nextInt(choiceArray.size()));
    Position pos = chosen.getKey();
    boolean willRotate = chosen.getValue();

    Move move = Move.NewPathMove(playerIndex, cardIndex, pos.x, pos.y, willRotate);
    double heuristic = possibleChoice.get(chosen);

    return new MoveHeuristic(move, heuristic);
  }

  private Map<Position, Double> generatePlacementHeuristics(
    PathCard card, boolean rotated,
    Board board, Map<Board.GoalPosition, GoalType> knownGoals
  ) {
    Map<Position, Double> cellHeuristics = new HashMap<>();
    card.setRotated(rotated);
    Set<Position> placeable = board.getPlaceable(card);
    placeable.forEach(p -> {
      double h = calculatePlacementHeuristic(card, p, knownGoals);
      cellHeuristics.put(p, h);
    });
    return cellHeuristics;
  }


  double calculatePlacementHeuristic(PathCard card, Position p, Map<Board.GoalPosition, GoalType> knownGoals) {
    GoalType top = knownGoals.get(Board.GoalPosition.TOP);
    GoalType mid = knownGoals.get(Board.GoalPosition.MIDDLE);
    GoalType bot = knownGoals.get(Board.GoalPosition.BOTTOM);

    int possibleGolds = 3;
    possibleGolds = (top == GoalType.GOLD ? 1 : top == GoalType.ROCK ? possibleGolds - 1 : possibleGolds);
    possibleGolds = (possibleGolds == 1 || mid == GoalType.GOLD ? 1 : mid == GoalType.ROCK ? possibleGolds - 1 : possibleGolds);
    possibleGolds = (possibleGolds == 1 || bot == GoalType.GOLD ? 1 : bot == GoalType.ROCK ? possibleGolds - 1 : possibleGolds);
    int E = possibleGolds;

    double b = 0.0;
    if (p.y == 0 && card.rightSide() == PathCard.Side.PATH && card.bottomSide() == PathCard.Side.PATH)
      b += 0.3 * (card.rightSide().val() + card.bottomSide().val());
    else if (1 <= p.y && p.y <= 3)
      b += 0.3 * (card.topSide().val() + card.rightSide().val() + card.bottomSide().val()) + 0.1;
    else if (p.y == 4 && card.rightSide() == PathCard.Side.PATH && card.topSide() == PathCard.Side.PATH)
      b += 0.3 * (card.rightSide().val() + card.topSide().val());

    if (card.type() == Card.Type.PATHWAY) {
      b += 0.1 * (role == Player.Role.GOLD_MINER ? (1.0 + p.x) / E : (9.0 - p.x) / E);
    } else if (card.type() == Card.Type.DEADEND) {
      b += 0.1 * (role == Player.Role.SABOTEUR ? (1.0 + p.x) / E : (9.0 - p.x) / E);
    }

    return pathHeuristic.get(card.pathType()) + b;
  }
}
