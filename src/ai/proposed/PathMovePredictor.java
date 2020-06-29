/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.proposed;

import ai.proposed.utils.RandomUtils;
import model.Board;
import model.Move;
import model.Player;
import model.Position;
import model.cards.Card;
import model.cards.PathCard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static ai.proposed.SaboteurAI.EPS;

@SuppressWarnings("Duplicates")
class PathMovePredictor {
  private static class PathPlacement {
    private final Position pos;
    private final boolean rotated;

    PathPlacement(Position pos, boolean rotated) {
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
    Map<Position, Double> nonRotatedValues = getPositionValues(board, card, false);
    Map<Position, Double> rotatedValues = getPositionValues(board, card, true);

    if (nonRotatedValues.isEmpty() && rotatedValues.isEmpty()) {
      Move move = Move.NewPathMove(playerIndex, cardIndex, -1, -1, false);
      double base = pathHeuristics.get(card.pathType());
      double heuristic = (role == Player.Role.GOLD_MINER ? base : 1 - base) / 2;
      return new MoveHeuristic(move, heuristic);
    }

    Map<PathPlacement, Double> choices = new HashMap<>();
    choices.put(new PathPlacement(new Position(-1, -1), false), Double.NEGATIVE_INFINITY);

    boolean path = card.type() == Card.Type.PATHWAY;
    boolean bRole = role == Player.Role.GOLD_MINER;

    for (Map.Entry<Position, Double> e : nonRotatedValues.entrySet()) {
      double best = path == bRole ?
          Collections.max(choices.values()) :
          Collections.min(choices.values());
      double val = e.getValue();
      if (path == bRole && val - best > EPS)
        choices.clear();
      if (path != bRole && best - val > EPS)
        choices.clear();
      if (path == bRole && val - best >= -EPS)
        choices.put(new PathPlacement(e.getKey(), false), e.getValue());
      if (path != bRole && best - val >= -EPS)
        choices.put(new PathPlacement(e.getKey(), false), e.getValue());
    }

    for (Map.Entry<Position, Double> e : rotatedValues.entrySet()) {
      double best = path == bRole ?
          Collections.max(choices.values()) :
          Collections.min(choices.values());
      double val = e.getValue();
      if (path == bRole && val - best > EPS)
        choices.clear();
      if (path != bRole && best - val > EPS)
        choices.clear();
      if (path == bRole && val - best >= -EPS)
        choices.put(new PathPlacement(e.getKey(), true), e.getValue());
      if (path != bRole && best - val >= -EPS)
        choices.put(new PathPlacement(e.getKey(), true), e.getValue());
    }

    PathPlacement chosen = RandomUtils.choose(choices.keySet())
        .orElse(new PathPlacement(new Position(-1, -1), false));
    Position pos = chosen.pos;
    boolean willRotate = chosen.rotated;

    Move move = Move.NewPathMove(playerIndex, cardIndex, pos.x, pos.y, willRotate);
    double heuristic = choices.get(chosen);

    return new MoveHeuristic(move, heuristic);
  }

  private Map<Position, Double> getPositionValues(Board board, PathCard card, boolean rotated) {
    Map<Position, Double> cellHeuristics = new HashMap<>();
    card.setRotated(rotated);

    Set<Position> placeable = board.getPlaceable(card);
    for (Position p : placeable) {
      cellHeuristics.put(p, valueOfPosition(board, card, p));
    }

    return cellHeuristics;
  }


  private double valueOfPosition(Board board, PathCard card, Position p) {
    Board simulated = board.simulatePlaceCardAt(card, p.x, p.y);
    if (simulated == null) {
      return 0.0;
    }

    double diff = boardPredictor.calcDiff(board, simulated);
    return diff * pathMultiplier;
  }
}
