/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 */

package ai.impl.original;

import model.GameLogicController;
import model.Move;
import model.Player;
import model.Tool;
import model.cards.PlayerActionCard;

import java.util.*;
import java.util.stream.Collectors;

import static ai.impl.original.SaboteurAI.k3;
import static ai.impl.original.SaboteurAI.k4;

@SuppressWarnings("Duplicates")
public class PlayerActionMovePredictor {
  private static final double BASE_BLOCK_HEURISTIC = k3;
  private static final double BASE_REPAIR_HEURISTIC = k4;

  private final GameLogicController game;
  private final RolePredictor rolePredictor;
  private final int playerIndex;

  private double blockHeuristic;
  private double repairHeuristic;

  PlayerActionMovePredictor(GameLogicController game, int playerIndex, RolePredictor rolePredictor) {
    this.playerIndex = playerIndex;
    this.game = game;
    this.rolePredictor = rolePredictor;
    this.blockHeuristic = BASE_BLOCK_HEURISTIC;
    this.repairHeuristic = BASE_REPAIR_HEURISTIC;
  }

  MoveHeuristic generateBlockHeuristic(int cardIndex, PlayerActionCard card) {
    Set<Integer> enemies = rolePredictor.getEnemies();
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
    Set<Integer> friends = rolePredictor.getFriends();
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

  public void setRepairHeuristic(double repairHeuristic) {
    this.repairHeuristic = repairHeuristic;
  }

  public double getRepairHeuristic() {
    return repairHeuristic;
  }
}
