package ai.impl.logan;

import ai.impl.logan.utils.DoubleUtils;
import ai.impl.logan.utils.RandomUtils;
import model.*;
import model.cards.PathCard;

import java.util.*;

@SuppressWarnings("Duplicates")
class PathMovePredictor {
  private static class Placement {
    private final Position pos;
    private final boolean rotated;

    Placement(Position pos, boolean rotated) {
      this.pos = pos;
      this.rotated = rotated;
    }
  }

  static final double MAX_PATH_HEURISTIC = .5;
  static final double PATH_MULTIPLIER = 50;

  private static final Map<PathCard.Type, Double> BASE_HEURISTICS = new HashMap<>();

  static {
    BASE_HEURISTICS.put(PathCard.Type.CROSSROAD_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.HORIZONTAL_T_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.VERTICAL_T_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.HORIZONTAL_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.VERTICAL_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.LEFT_TURN_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.RIGHT_TURN_PATH, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.CROSSROAD_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.HORIZONTAL_T_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.VERTICAL_T_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.BOTH_HORIZONTAL_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.BOTH_VERTICAL_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.SINGLE_HORIZONTAL_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.SINGLE_VERTICAL_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.LEFT_TURN_DEADEND, MAX_PATH_HEURISTIC);
    BASE_HEURISTICS.put(PathCard.Type.RIGHT_TURN_DEADEND, MAX_PATH_HEURISTIC);
  }

  private final int playerIndex;
  private final Player.Role role;
  private final BoardPredictor boardPredictor;

  private final Map<PathCard.Type, Double> pathHeuristics;
  private final double pathMultiplier;

  PathMovePredictor(int playerIndex, Player.Role role, BoardPredictor boardPredictor) {
    this.playerIndex = playerIndex;
    this.role = role;
    this.boardPredictor = boardPredictor;
    this.pathHeuristics = BASE_HEURISTICS;
    this.pathMultiplier = PATH_MULTIPLIER;
  }

  MoveHeuristic generatePathHeuristic(Board board, int cardIndex, PathCard card) {
    Map<Placement, Double> placementValues = getPlacementValues(board, card);
    if (placementValues.isEmpty()) {
      return getDefaultMove(cardIndex, card);
    }

    Map<Placement, Double> bestPlacements = extractBestPlacements(placementValues);
    Placement chosen = RandomUtils.choose(bestPlacements.keySet()).orElse(getDefaultPlacement());

    Move move = Move.NewPathMove(playerIndex, cardIndex, chosen.pos.x, chosen.pos.y, chosen.rotated);
    double heuristic = bestPlacements.get(chosen);

    return new MoveHeuristic(move, heuristic);
  }

  private Map<Placement, Double> getPlacementValues(Board board, PathCard card) {
    Map<Placement, Double> placementValues = new HashMap<>();

    card.setRotated(false);
    for (Position p : board.getPlaceable(card)) {
      placementValues.put(new Placement(p, false), valueOfPosition(board, card, p));
    }

    card.setRotated(true);
    for (Position p : board.getPlaceable(card)) {
      placementValues.put(new Placement(p, true), valueOfPosition(board, card, p));
    }

    return placementValues;
  }

  private Map<Placement, Double> extractBestPlacements(Map<Placement, Double> placementValues) {
    Map<Placement, Double> choices = new HashMap<>();
    choices.put(getDefaultPlacement(), Double.NEGATIVE_INFINITY);
    for (Map.Entry<Placement, Double> e : placementValues.entrySet()) {
      double best = Collections.max(choices.values());
      double val = e.getValue();
      if (DoubleUtils.compare(val, best) < 0) continue;

      if (DoubleUtils.compare(val, best) > 0)
        choices.clear();
      choices.put(e.getKey(), e.getValue());
    }

    return choices;
  }

  private double valueOfPosition(Board board, PathCard card, Position p) {
    Board simulated = board.simulatePlaceCardAt(card, p.x, p.y);
    if (simulated == null) {
      return 0.0;
    }

    double diff = boardPredictor.calcDiff(board, simulated);
    return diff * pathMultiplier;
  }

  private MoveHeuristic getDefaultMove(int cardIndex, PathCard card) {
    Placement def = getDefaultPlacement();
    Move move = Move.NewPathMove(playerIndex, cardIndex, def.pos.x, def.pos.y, def.rotated);

    double value = pathHeuristics.get(card.pathType());
    value = (role == Player.Role.GOLD_MINER ? value : 1 - value) / 2;

    return new MoveHeuristic(move, value);
  }

  private Placement getDefaultPlacement() {
    Position pos = new Position(-1, -1);
    return new Placement(pos, false);
  }
}
