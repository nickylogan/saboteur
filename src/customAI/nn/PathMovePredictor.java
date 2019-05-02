/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package customAI.nn;

import javafx.util.Pair;
import model.*;
import model.cards.Card;
import model.cards.PathCard;

import java.util.*;

import static customAI.nn.CustomAI.EPS;
import static customAI.nn.CustomAI.VERBOSE;

@SuppressWarnings("Duplicates")
class PathMovePredictor {
  static final double MAX_PATH_HEURISTIC = .5;
  static final double PATH_MULTIPLIER = 50;

  private static Map<PathCard.Type, Double> BASE_HEURISTIC = new HashMap<>();

  static {
    BASE_HEURISTIC.put(PathCard.Type.CROSSROAD_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.HORIZONTAL_T_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.VERTICAL_T_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.HORIZONTAL_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.VERTICAL_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.LEFT_TURN_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.RIGHT_TURN_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.CROSSROAD_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.HORIZONTAL_T_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.VERTICAL_T_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.BOTH_HORIZONTAL_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.BOTH_VERTICAL_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.SINGLE_HORIZONTAL_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.SINGLE_VERTICAL_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.LEFT_TURN_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTIC.put(PathCard.Type.RIGHT_TURN_DEADEND, MAX_PATH_HEURISTIC);
  }

  private final GameLogicController game;
  private final int playerIndex;
  private final Player.Role role;
  private final BoardPredictor boardPredictor;

  private final Map<PathCard.Type, Double> pathHeuristic;

  private double pathMultiplier;

  PathMovePredictor(GameLogicController game, int playerIndex, Player.Role role, BoardPredictor boardPredictor) {
    this.game = game;
    this.playerIndex = playerIndex;
    this.role = role;
    this.pathHeuristic = BASE_HEURISTIC;
    this.boardPredictor = boardPredictor;
    this.pathMultiplier = PATH_MULTIPLIER;
  }

  MoveHeuristic generatePathHeuristic(
    int cardIndex, PathCard card,
    Board board, Map<Board.GoalPosition, GoalType> knownGoals
  ) {
    if (VERBOSE > 1) System.out.println(card);
    if (VERBOSE > 1) System.out.println("not rotated:");

    Map<Position, Double> nonRotatedCellHeuristics = generatePlacementHeuristics(card, false, board, knownGoals);

    if (VERBOSE > 1) System.out.println(nonRotatedCellHeuristics);
    if (VERBOSE > 1) System.out.println("rotated:");

    Map<Position, Double> rotatedCellHeuristics = generatePlacementHeuristics(card, true, board, knownGoals);

    if (VERBOSE > 1) System.out.println(rotatedCellHeuristics);
    if (VERBOSE > 1) System.out.println();

    if (nonRotatedCellHeuristics.isEmpty() && rotatedCellHeuristics.isEmpty()) {
      Move move = Move.NewPathMove(playerIndex, cardIndex, -1, -1, false);
      double heuristic = pathHeuristic.get(card.pathType());
      return new MoveHeuristic(move, (role == Player.Role.GOLD_MINER ? heuristic : 1 - heuristic) / 2);
    }

    Map<Pair<Position, Boolean>, Double> possibleChoice = new HashMap<>();

    possibleChoice.put(new Pair<>(new Position(-1, -1), false), Double.NEGATIVE_INFINITY);

    boolean path = card.type() == Card.Type.PATHWAY;
    boolean bRole = role == Player.Role.GOLD_MINER;

    for (Map.Entry<Position, Double> e : nonRotatedCellHeuristics.entrySet()) {
      double best = path == bRole ?
        Collections.max(possibleChoice.values()) :
        Collections.min(possibleChoice.values());
      double val = e.getValue();
      if (path == bRole && val - best > EPS) possibleChoice.clear();
      if (path != bRole && best - val > EPS) possibleChoice.clear();
      if (path == bRole && val - best >= -EPS)
        possibleChoice.put(new Pair<>(e.getKey(), false), e.getValue());
      if (path != bRole && best - val >= -EPS)
        possibleChoice.put(new Pair<>(e.getKey(), false), e.getValue());
    }

    for (Map.Entry<Position, Double> e : rotatedCellHeuristics.entrySet()) {
      double best = path == bRole ?
        Collections.max(possibleChoice.values()) :
        Collections.min(possibleChoice.values());
      double val = e.getValue();
      if (path == bRole && val - best > EPS) possibleChoice.clear();
      if (path != bRole && best - val > EPS) possibleChoice.clear();
      if (path == bRole && val - best >= -EPS)
        possibleChoice.put(new Pair<>(e.getKey(), true), e.getValue());
      if (path != bRole && best - val >= -EPS)
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

  Map<Position, Double> generatePlacementHeuristics(
    PathCard card, boolean rotated,
    Board board, Map<Board.GoalPosition, GoalType> knownGoals
  ) {
    Map<Position, Double> cellHeuristics = new HashMap<>();
    card.setRotated(rotated);
    Set<Position> placeable = board.getPlaceable(card);
    placeable.forEach(p -> {
      double h = calculatePlacementHeuristic(board, card, p, knownGoals);
      cellHeuristics.put(p, h);
    });
    return cellHeuristics;
  }


  double calculatePlacementHeuristic(Board board, PathCard card, Position p, Map<Board.GoalPosition, GoalType> knownGoals) {
    Board simulated = board.simulatePlaceCardAt(card, p.x, p.y);
    if (simulated == null) return 0;
    if (VERBOSE > 1) System.out.println("p=" + p);
    if (VERBOSE > 1) System.out.println("old: ");
    double oldMax = boardPredictor.calculateMaxValue(board, knownGoals);
    double oldAvg = boardPredictor.calculateAverageValue(board, knownGoals);
    if (VERBOSE > 1) System.out.println("new: ");
    double simMax = boardPredictor.calculateMaxValue(simulated, knownGoals);
    double simAvg = boardPredictor.calculateAverageValue(simulated, knownGoals);
    if (Math.abs(simMax - oldMax) <= EPS) {
      return (simAvg - oldAvg) * pathMultiplier;
    } else {
      return (simMax - oldMax) * pathMultiplier;
    }
  }
}
