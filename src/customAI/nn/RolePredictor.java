/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package customAI.nn;

import customAI.paper.PaperAI;
import model.*;
import model.cards.Card;
import model.cards.PathCard;

import java.util.*;

import static customAI.nn.CustomAI.EPS;

class RolePredictor {
  private final static double INITIAL_TRUST = 5 * PathMovePredictor.MAX_PATH_HEURISTIC;
  private final static double BLOCK_CONSTANT = INITIAL_TRUST + .1;
  private final static double REPAIR_CONSTANT = .5;
  private final static double DISCARD_CONSTANT = .5;

  private final GameLogicController game;
  private Map<Integer, Double> playerTrust;
  private List<Set<Board.GoalPosition>> goalKnowledge;
  private final int playerIndex;
  private final Player.Role role;
  private final CustomAI ai;

  RolePredictor(GameLogicController game, int playerIndex, Player.Role role, CustomAI ai) {
    this.game = game;
    this.playerTrust = new HashMap<>();
    this.goalKnowledge = new ArrayList<>();
    this.playerIndex = playerIndex;
    this.role = role;
    this.ai = ai;

    int n = game.numPlayers();
    for (int i = 0; i < n; ++i) {
      goalKnowledge.add(new HashSet<>());
      if (i == playerIndex) continue;
      playerTrust.put(i, INITIAL_TRUST);
    }
  }

  void updateFromPathMove(Move move, Map<Board.GoalPosition, GoalType> knownGoals) {
    BoardDelta delta = (BoardDelta) move.delta();

    // TODO: intersect knownGoals

    double oldMax = ai.boardPredictor.calculateMaxValue(delta.boardBefore(), knownGoals);
    double oldAvg = ai.boardPredictor.calculateAverageValue(delta.boardBefore(), knownGoals);
    double newMax = ai.boardPredictor.calculateMaxValue(delta.boardAfter(), knownGoals);
    double newAvg = ai.boardPredictor.calculateAverageValue(delta.boardAfter(), knownGoals);

    double value;
    if (Math.abs(newMax - oldMax) <= EPS) {
      value = newAvg - oldAvg;
    } else {
      value = newMax - oldMax;
    }
    // trust += value;
    double trust = playerTrust.get(move.playerIndex());
    playerTrust.put(move.playerIndex(), trust + value);
    // BoardDelta delta = (BoardDelta) move.delta();
    // double z1 = ai.pathMovePredictor.generatePathHeuristic(-1, (PathCard) move.card(), delta.boardBefore(), knownGoals).heuristic;
    // double z2 = ai.pathMovePredictor.calculatePlacementHeuristic(delta.boardBefore(), (PathCard) move.card(), new Position(move.args()[0], move.args()[1]), knownGoals);
    // double trust = playerTrust.get(move.playerIndex());
    // if (Math.abs(z1 - z2) <= EPS) {
    //   trust += role == Player.Role.GOLD_MINER ? z1 : -z1;
    // } else if (z1 - z2 > EPS) {
    //   trust -= role == Player.Role.GOLD_MINER ? (z1 - z2) : -(z1 - z2);
    // }
    // playerTrust.put(move.playerIndex(), trust);
  }

  void updateFromRockfallMove(Move move, Map<Board.GoalPosition, GoalType> knownGoals) {
    BoardDelta delta = (BoardDelta) move.delta();
    double mx1 = ai.boardPredictor.calculateMaxValue(delta.boardBefore(), knownGoals);
    double mx2 = ai.boardPredictor.calculateMaxValue(delta.boardAfter(), knownGoals);
    double avg1 = ai.boardPredictor.calculateAverageValue(delta.boardBefore(), knownGoals);
    double avg2 = ai.boardPredictor.calculateAverageValue(delta.boardAfter(), knownGoals);

    double trust = playerTrust.get(move.playerIndex());
    if (Math.abs(mx2 - mx1) <= EPS) {
      trust += avg2 - avg1;
    } else {
      trust += mx2 - mx1;
    }
    playerTrust.put(move.playerIndex(), trust);

    // if (delta.before().type() == Card.Type.PATHWAY) {
    //   playerTrust.put(move.playerIndex(), 0.0);
    // } else if (delta.before().type() == Card.Type.DEADEND) {
    //   playerTrust.put(move.playerIndex(), Collections.max(playerTrust.values()));
    // }
  }

  void updateFromBlockMove(Move move) {
    int targetIndex = move.args()[0];
    int playerIndex = move.playerIndex();
    // System.out.println(targetIndex);
    // System.out.println(playerIndex);
    double targetTrust = 0.0;
    double playerTrust = 0.0;
    try {
      targetTrust = this.playerTrust.get(targetIndex);
      playerTrust = this.playerTrust.get(playerIndex);
    } catch (NullPointerException e) {
      //
    }
    if (getFriends().contains(targetIndex)) {
      playerTrust -= BLOCK_CONSTANT;
      targetTrust += BLOCK_CONSTANT;
    } else if (getEnemies().contains(targetIndex)) {
      playerTrust += BLOCK_CONSTANT;
      targetTrust -= BLOCK_CONSTANT;
    }
    this.playerTrust.put(targetIndex, targetTrust);
    this.playerTrust.put(playerIndex, playerTrust);
  }

  void updateFromRepairMove(Move move) {
    int targetIndex = move.args()[0];
    int playerIndex = move.playerIndex();
    double targetTrust = 0.0;
    double playerTrust = 0.0;
    try {
      targetTrust = this.playerTrust.get(targetIndex);
      playerTrust = this.playerTrust.get(playerIndex);
    } catch (NullPointerException e) {
      //
    }
    if (getFriends().contains(targetIndex)) {
      playerTrust += REPAIR_CONSTANT;
      targetTrust += REPAIR_CONSTANT;
    } else if (getEnemies().contains(targetIndex)) {
      playerTrust -= REPAIR_CONSTANT;
      targetTrust -= REPAIR_CONSTANT;
    }
    this.playerTrust.put(targetIndex, targetTrust);
    this.playerTrust.put(playerIndex, playerTrust);
  }

  void updateFromMapMove(Move move) {
    Board.GoalPosition pos = Board.GoalPosition.values()[move.args()[0]];
    goalKnowledge.get(move.playerIndex()).add(pos);
  }

  void updateFromDiscardMove(Move move) {
    // double trust = playerTrust.get(move.playerIndex());
    // trust -= role == Player.Role.GOLD_MINER ? DISCARD_CONSTANT : -DISCARD_CONSTANT;
    // playerTrust.put(move.playerIndex(), trust);
  }

  Set<Integer> getFriends() {
    int numFriends = role == Player.Role.GOLD_MINER
      ? game.numPlayers() - game.numSaboteurs()
      : game.numSaboteurs();
    numFriends -= 1; // exclude self

    List<Map.Entry<Integer, Double>> trustArray = new ArrayList<>(playerTrust.entrySet());
    Collections.shuffle(trustArray);
    trustArray.sort(Collections.reverseOrder(Comparator.comparingDouble(Map.Entry::getValue)));

    Set<Integer> friends = new HashSet<>();
    for (int i = 0; i < numFriends; ++i) friends.add(trustArray.get(i).getKey());
    friends.add(playerIndex);
    return friends;
  }

  Set<Integer> getEnemies() {
    int numEnemies = role == Player.Role.SABOTEUR
      ? game.numPlayers() - game.numSaboteurs()
      : game.numSaboteurs();
    List<Map.Entry<Integer, Double>> trustArray = new ArrayList<>(playerTrust.entrySet());
    Collections.shuffle(trustArray);
    trustArray.sort(Comparator.comparingDouble(Map.Entry::getValue));

    Set<Integer> enemies = new HashSet<>();
    for (int i = 0; i < numEnemies; ++i) enemies.add(trustArray.get(i).getKey());
    return enemies;
  }

  Map<Integer, Double> playerTrust() {
    return playerTrust;
  }

  public List<Set<Board.GoalPosition>> goalKnowledge() {
    return goalKnowledge;
  }

  void updateKnownGoals(Board.GoalPosition pos, GoalType type) {
    if (type == GoalType.GOLD) {
      goalKnowledge.forEach(s -> s.addAll(Arrays.asList(Board.GoalPosition.values())));
    } else {
      goalKnowledge.forEach(s -> s.add(pos));
    }
  }
}
