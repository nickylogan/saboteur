/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.proposed;

import ai.proposed.utils.RandomUtils;
import model.*;
import model.cards.PlayerActionCard;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("DuplicatedCode")
class PlayerActionMovePredictor {
  private static class TargetValue {
    private final Set<Integer> targets;
    private final double value;

    private TargetValue(Set<Integer> targets, double value) {
      this.targets = targets;
      this.value = value;
    }
  }
  static final double BASE_BLOCK_HEURISTIC = PathMovePredictor.MAX_PATH_HEURISTIC - .1;
  static final double BASE_REPAIR_HEURISTIC = PathMovePredictor.MAX_PATH_HEURISTIC - .1;
  static final double H_REPAIR_INCREMENT = 0.01;

  private final GameLogicController game;
  private final RolePredictor rolePredictor;
  private final int playerIndex;

  private final double blockHeuristic;
  private double repairHeuristic;

  PlayerActionMovePredictor(GameLogicController game, int playerIndex, RolePredictor rolePredictor) {
    this.playerIndex = playerIndex;
    this.game = game;
    this.rolePredictor = rolePredictor;
    this.blockHeuristic = BASE_BLOCK_HEURISTIC;
    this.repairHeuristic = BASE_REPAIR_HEURISTIC;
  }

  MoveHeuristic generateBlockHeuristic(int cardIndex, PlayerActionCard card) {
    Tool tool = card.effects()[0];

    Set<Integer> sabotageable = rolePredictor.getEnemies().stream()
        .filter(i -> game.playerAt(i).isSabotageable(tool))
        .collect(Collectors.toSet());
    if (sabotageable.isEmpty()) {
      return getDefaultBlockMove(cardIndex);
    }

    TargetValue best = getBestBlocks(sabotageable);
    int target = RandomUtils.choose(best.targets).orElse(-1);

    Move move = Move.NewPlayerActionMove(playerIndex, cardIndex, target);
    double heuristic = best.value > 1 ? .5 * blockHeuristic : blockHeuristic;

    return new MoveHeuristic(move, heuristic);
  }

  MoveHeuristic generateRepairHeuristic(int cardIndex, PlayerActionCard card) {
    Tool[] tools = card.effects();

    Set<Integer> repairable = rolePredictor.getFriends().stream()
        .filter(i -> game.playerAt(i).isRepairable(tools))
        .collect(Collectors.toSet());
    if (repairable.isEmpty()) {
      return getDefaultRepairMove(cardIndex);
    }

    TargetValue best = getBestRepairs(repairable, card);
    int target = RandomUtils.choose(best.targets).orElse(-1);

    Move move = Move.NewPlayerActionMove(playerIndex, cardIndex, target);
    double heuristic = best.value > 1 ? 2 * repairHeuristic : .5 * repairHeuristic;

    return new MoveHeuristic(move, heuristic);
  }

  private MoveHeuristic getDefaultBlockMove(int cardIndex) {
    int target = getDefaultTarget();
    Move move = Move.NewPlayerActionMove(playerIndex, cardIndex, target);
    return new MoveHeuristic(move, blockHeuristic / 2);
  }

  private MoveHeuristic getDefaultRepairMove(int cardIndex) {
    int target = getDefaultTarget();
    Move move = Move.NewPlayerActionMove(playerIndex, cardIndex, target);
    return new MoveHeuristic(move, repairHeuristic / 2);
  }

  private TargetValue getBestBlocks(Set<Integer> sabotageable) {
    Set<Integer> choices = new HashSet<>();
    double maxValue = -1;
    for (Integer pIndex : sabotageable) {
      Player p = game.playerAt(pIndex);
      double val = getBlockValueOf(p);
      if (val < maxValue) continue;

      if (val > maxValue) {
        choices.clear();
        maxValue = val;
      }
      choices.add(pIndex);
    }

    return new TargetValue(choices, maxValue);
  }

  private TargetValue getBestRepairs(Set<Integer> repairable, PlayerActionCard card) {
    Set<Integer> choices = new HashSet<>();
    double maxValue = -1;
    for (Integer pIndex : repairable) {
      Player p = game.playerAt(pIndex);
      double val = getRepairValueOf(p, card);
      if (val < maxValue) continue;

      if (val > maxValue) {
        choices.clear();
        maxValue = val;
      }
      choices.add(pIndex);
    }

    return new TargetValue(choices, maxValue);
  }

  private double getBlockValueOf(Player p) {
    return p.sabotaged().length;
  }

  private double getRepairValueOf(Player p, PlayerActionCard card) {
    double val = Stream.of(card.effects())
        .filter(p::isRepairable)
        .count();

    if (isSelf(p)) val *= 2;
    return val;
  }

  private boolean isSelf(Player p) {
    return p.index() == playerIndex;
  }

  private int getDefaultTarget() {
    return -1;
  }

  void update() {
    this.repairHeuristic += H_REPAIR_INCREMENT;
  }
}
