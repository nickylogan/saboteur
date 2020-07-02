/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.proposed;

import ai.proposed.utils.DoubleUtils;
import ai.proposed.utils.RandomUtils;
import model.*;
import model.Player.Role;

import java.util.*;

public class RockfallMovePredictor {
  static final double BASE_MULTIPLIER = 50;

  private final GameLogicController game;
  private final int playerIndex;
  private final Role role;
  private final double rockfallMultiplier;
  private final BoardPredictor boardPredictor;

  RockfallMovePredictor(
      GameLogicController game,
      int playerIndex,
      Role role,
      BoardPredictor boardPredictor
  ) {
    this.game = game;
    this.playerIndex = playerIndex;
    this.role = role;
    this.boardPredictor = boardPredictor;
    this.rockfallMultiplier = BASE_MULTIPLIER;
  }

  MoveHeuristic generateRockfallHeuristic(int cardIndex) {
    if (role == Role.GOLD_MINER) {
      return getMinerMove(cardIndex);
    }

    Set<Position> destroyable = game.board().getDestroyable();
    if (destroyable.isEmpty()) {
      return getDefaultMove(cardIndex);
    }

    Map<Position, Double> choices = generateChoices(destroyable);
    choices = extractBestChoices(choices);

    Position pos = RandomUtils.choose(choices.keySet()).orElse(getDefaultPosition());
    double heuristic = Math.abs(choices.get(pos));

    Move move = Move.NewRockfallMove(playerIndex, cardIndex, pos.x, pos.y);
    return new MoveHeuristic(move, heuristic);
  }

  private Map<Position, Double> generateChoices(Set<Position> destroyable) {
    Map<Position, Double> choices = new HashMap<>();
    for (Position pos : destroyable) {
      Optional<Double> value = getCellValue(game.board(), pos);
      if (!value.isPresent()) continue;

      choices.put(pos, value.get());
    }

    return choices;
  }

  private Map<Position, Double> extractBestChoices(Map<Position, Double> choices) {
    Map<Position, Double> bestSet = new HashMap<>();
    bestSet.put(getDefaultPosition(), Double.NEGATIVE_INFINITY);

    for (Map.Entry<Position, Double> e : choices.entrySet()) {
      double currBest = Collections.max(bestSet.values());

      if (DoubleUtils.compare(e.getValue(), currBest) == -1)
        continue;

      if (DoubleUtils.compare(e.getValue(), currBest) == 1)
        bestSet.clear();

      bestSet.put(e.getKey(), e.getValue());
    }

    return bestSet;
  }

  private Optional<Double> getCellValue(Board board, Position pos) {
    Board simulated = board.simulateRemoveCardAt(pos.x, pos.y);
    if (simulated == null) {
      return Optional.empty();
    }

    double diff = boardPredictor.calcDiff(board, simulated);
    double valueIfDestroyed = diff * rockfallMultiplier;

    return Optional.of(valueIfDestroyed);
  }

  private MoveHeuristic getDefaultMove(int cardIndex) {
    Position pos = getDefaultPosition();
    Move move = Move.NewRockfallMove(playerIndex, cardIndex, pos.x, pos.y);
    return new MoveHeuristic(move, PathMovePredictor.MAX_PATH_HEURISTIC / 2);
  }

  private MoveHeuristic getMinerMove(int cardIndex) {
    Position pos = getDefaultPosition();
    Move move = Move.NewRockfallMove(playerIndex, cardIndex, pos.x, pos.y);
    return new MoveHeuristic(move, Double.POSITIVE_INFINITY);
  }

  private Position getDefaultPosition() {
    return new Position(-1, -1);
  }
}
