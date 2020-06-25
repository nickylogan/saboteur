/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.proposed;

import model.GameLogicController;
import model.Move;
import model.Player;
import model.Tool;
import model.cards.PlayerActionCard;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerActionMovePredictor {
  static final double BASE_BLOCK_HEURISTIC = PathMovePredictor.MAX_PATH_HEURISTIC - .1;
  static final double BASE_REPAIR_HEURISTIC = PathMovePredictor.MAX_PATH_HEURISTIC - .1;

  private final GameLogicController game;
  private final SaboteurAI ai;
  private final int playerIndex;

  private double blockHeuristic;
  private double repairHeuristic;

  PlayerActionMovePredictor(GameLogicController game, int playerIndex, SaboteurAI ai) {
    this.playerIndex = playerIndex;
    this.game = game;
    this.ai = ai;
    this.blockHeuristic = BASE_BLOCK_HEURISTIC;
    this.repairHeuristic = BASE_REPAIR_HEURISTIC;
  }

  MoveHeuristic generateBlockHeuristic(int cardIndex, PlayerActionCard card) {
    Set<Integer> enemies = ai.rolePredictor.getEnemies();
    Tool tool = card.effects()[0];
    enemies = enemies.stream().filter(i -> game.playerAt(i).isSabotageable(tool)).collect(Collectors.toSet());
    if (enemies.isEmpty()) {
      Move move = Move.NewPlayerActionMove(playerIndex, cardIndex, -1);
      return new MoveHeuristic(move, blockHeuristic / 2);
    }

    Set<Player> enemyChoices = new HashSet<>();
    int maxBlocked = -1;
    for (Integer i : enemies) {
      Player p = game.playerAt(i);
      if (p.sabotaged().length > maxBlocked) {
        enemyChoices.clear();
        maxBlocked = p.sabotaged().length;
      }
      if (p.sabotaged().length >= maxBlocked) {
        enemyChoices.add(p);
      }
    }

    List<Player> choiceArray = new ArrayList<>(enemyChoices);
    Random r = new Random();
    int target = choiceArray.get(r.nextInt(choiceArray.size())).index();
    Move move = Move.NewPlayerActionMove(playerIndex, cardIndex, target);
    double heuristic = maxBlocked > 1 ? .5 * blockHeuristic : blockHeuristic;

    return new MoveHeuristic(move, heuristic);
  }

  MoveHeuristic generateRepairHeuristic(int cardIndex, PlayerActionCard card) {
    Set<Integer> friends = ai.rolePredictor.getFriends();
    Tool[] tools = card.effects();
    friends = friends.stream().filter(i -> game.playerAt(i).isRepairable(tools)).collect(Collectors.toSet());
    if (friends.isEmpty()) {
      Move move = Move.NewPlayerActionMove(playerIndex, cardIndex, -1);
      return new MoveHeuristic(move, repairHeuristic / 2);
    }

    Set<Player> friendChoices = new HashSet<>();
    int maxRepairable = -1;
    for (Integer i : friends) {
      Player p = game.playerAt(i);
      int repairable = 0;
      for (Tool tool : card.effects()) {
        repairable += p.isRepairable(tool) ? 1 : 0;
      }
      repairable *= i == playerIndex ? 2 : 1;
      if (repairable > maxRepairable) {
        friendChoices.clear();
        maxRepairable = repairable;
      }
      if (repairable >= maxRepairable) {
        friendChoices.add(p);
      }
    }

    List<Player> choiceArray = new ArrayList<>(friendChoices);
    Random r = new Random();
    int target = choiceArray.get(r.nextInt(choiceArray.size())).index();
    Move move = Move.NewPlayerActionMove(playerIndex, cardIndex, target);
    double heuristic = maxRepairable > 1 ? 2 * repairHeuristic : .5 * repairHeuristic;

    return new MoveHeuristic(move, heuristic);
  }

  void setRepairHeuristic(double repairHeuristic) {
    this.repairHeuristic = repairHeuristic;
  }

  double getRepairHeuristic() {
    return repairHeuristic;
  }
}
