/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.proposed;

import ai.proposed.utils.DoubleUtils;
import ai.proposed.utils.RandomUtils;
import model.*;
import model.cards.Card;
import model.cards.PathCard;

import java.util.*;
import java.util.stream.Collectors;

import static ai.proposed.SaboteurAI.EPS;

public class RockfallMovePredictor {
  static final double BASE_MULTIPLIER = 50;

  private final GameLogicController game;
  private final int playerIndex;
  private final Player.Role role;
  private final double rockfallMultiplier;
  private final BoardPredictor boardPredictor;
  private final PathMovePredictor pathMovePredictor;

  RockfallMovePredictor(
      GameLogicController game,
      int playerIndex,
      Player.Role role,
      BoardPredictor boardPredictor,
      PathMovePredictor pathMovePredictor
  ) {
    this.game = game;
    this.playerIndex = playerIndex;
    this.role = role;
    this.boardPredictor = boardPredictor;
    this.pathMovePredictor = pathMovePredictor;
    this.rockfallMultiplier = BASE_MULTIPLIER;
  }

  MoveHeuristic generateRockfallHeuristic(int cardIndex, ArrayList<Card> hand) {
    Set<Position> destroyable = game.board().getDestroyable();
    if (destroyable.isEmpty()) {
      return getDefaultMove(cardIndex);
    }

    Map<Position, Double> choices = generateChoices(destroyable, hand);
    choices = extractBestChoices(choices);

    Position pos = RandomUtils.choose(choices.keySet()).orElse(new Position(-1, -1));
    double heuristic = Math.abs(choices.get(pos));

    Move move = Move.NewRockfallMove(playerIndex, cardIndex, pos.x, pos.y);
    return new MoveHeuristic(move, heuristic);
  }

  private Map<Position, Double> generateChoices(Set<Position> destroyable, List<Card> hand) {
    List<PathCard> pathCards = hand.stream()
        .filter(c -> c instanceof PathCard)
        .map(c -> (PathCard) c)
        .collect(Collectors.toList());

    Map<Position, Double> choices = new HashMap<>();
    for (Position pos : destroyable) {
      Optional<Double> value = getCellValue(game.board(), pos, pathCards);
      if (!value.isPresent()) continue;

      choices.put(pos, value.get());
    }

    return choices;
  }

  private Map<Position, Double> extractBestChoices(Map<Position, Double> choices) {
    Map<Position, Double> best = new HashMap<>();
    best.put(getDefaultPosition(), Double.NEGATIVE_INFINITY);

    for (Map.Entry<Position, Double> e : choices.entrySet()) {
      double currMax = Collections.max(best.values());

      if (DoubleUtils.compare(e.getValue(), currMax) == -1)
        continue;

      if (DoubleUtils.compare(e.getValue(), currMax) == 1)
        best.clear();

      best.put(e.getKey(), e.getValue());
    }

    return best;
  }

  private Optional<Double> getCellValue(Board board, Position pos, List<PathCard> pathCards) {
    Board simulated = board.simulateRemoveCardAt(pos.x, pos.y);
    if (simulated == null) return Optional.empty();

    double diff = boardPredictor.calcDiff(board, simulated);
    double valueIfDestroyed = diff * rockfallMultiplier;

    if (role == Player.Role.SABOTEUR) {
      return Optional.of(valueIfDestroyed);
    }

    double valueOfFuturePath = pathCards.stream()
        .map(c -> pathMovePredictor.generatePathHeuristic(simulated, -1, c).heuristic)
        .max(Double::compareTo)
        .orElse(Double.NEGATIVE_INFINITY);

    return Optional.of(Math.max(valueOfFuturePath, valueIfDestroyed));
  }

  private MoveHeuristic getDefaultMove(int cardIndex) {
    Position pos = getDefaultPosition();
    Move move = Move.NewRockfallMove(playerIndex, cardIndex, pos.x, pos.y);
    return new MoveHeuristic(move, PathMovePredictor.MAX_PATH_HEURISTIC / 2);
  }

  private Position getDefaultPosition() {
    return new Position(-1, -1);
  }
}
