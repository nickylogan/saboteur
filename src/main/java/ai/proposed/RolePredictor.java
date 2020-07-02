/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.proposed;

import ai.proposed.utils.RandomUtils;
import model.*;
import model.cards.Card;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
class RolePredictor {
  private final static double INITIAL_TRUST = 5 * PathMovePredictor.MAX_PATH_HEURISTIC;
  private final static double BLOCK_CONSTANT = INITIAL_TRUST + .1;
  private final static double REPAIR_CONSTANT = .5;
  private final static double DISCARD_CONSTANT = .5;

  private final GameLogicController game;
  private final Map<Integer, Double> playerTrust;
  private final List<Set<Board.GoalPosition>> othersGoalKnowledge;
  private final int playerIndex;
  private final Player.Role role;
  private final BoardPredictor boardPredictor;

  RolePredictor(GameLogicController game, int playerIndex, Player.Role role, BoardPredictor boardPredictor) {
    this.game = game;
    this.playerTrust = new HashMap<>();
    this.othersGoalKnowledge = new ArrayList<>();
    this.playerIndex = playerIndex;
    this.role = role;
    this.boardPredictor = boardPredictor;

    initTrust(game, playerIndex);
  }

  Set<Integer> getFriends() {
    List<Map.Entry<Integer, Double>> trustArray = new ArrayList<>(playerTrust.entrySet());
    RandomUtils.shuffleSort(trustArray, Collections.reverseOrder(Comparator.comparingDouble(Map.Entry::getValue)));

    Set<Integer> friends = trustArray.subList(0, getNumFriends()).stream()
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
    friends.add(playerIndex);

    return friends;
  }

  Set<Integer> getEnemies() {
    List<Map.Entry<Integer, Double>> trustArray = new ArrayList<>(playerTrust.entrySet());
    RandomUtils.shuffleSort(trustArray, Comparator.comparingDouble(Map.Entry::getValue));

    return trustArray.subList(0, getNumEnemies()).stream()
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  void updateFromMove(Move move) {
    switch (move.type()) {
      case PLAY_PATH:
        updateFromPathMove(move);
        break;
      case PLAY_ROCKFALL:
        updateFromRockfallMove(move);
        break;
      case PLAY_MAP:
        updateFromMapMove(move);
        break;
      case PLAY_PLAYER:
        if (move.card().type() == Card.Type.BLOCK) {
          updateFromBlockMove(move);
        } else {
          updateFromRepairMove(move);
        }
        break;
      case DISCARD:
        updateFromDiscardMove(move);
        break;
    }
  }

  private void updateFromPathMove(Move move) {
    BoardDelta delta = (BoardDelta) move.delta();

    double trust = playerTrust.get(move.playerIndex());
    double diff = boardPredictor.calcDiff(delta.boardBefore(), delta.boardAfter());

    playerTrust.put(move.playerIndex(), trust + diff);
  }

  private void updateFromRockfallMove(Move move) {
    BoardDelta delta = (BoardDelta) move.delta();

    double trust = playerTrust.get(move.playerIndex());
    double diff = boardPredictor.calcDiff(delta.boardBefore(), delta.boardAfter());

    playerTrust.put(move.playerIndex(), trust + diff);
  }

  private void updateFromBlockMove(Move move) {
    int targetIndex = move.args()[0];
    int playerIndex = move.playerIndex();
    double targetTrust = targetIndex != this.playerIndex ?
        this.playerTrust.get(targetIndex) :
        0.0;
    double playerTrust = this.playerTrust.get(playerIndex);

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

  private void updateFromRepairMove(Move move) {
    int targetIndex = move.args()[0];
    int playerIndex = move.playerIndex();
    double targetTrust = targetIndex != this.playerIndex ?
        this.playerTrust.get(targetIndex) :
        0.0;
    double playerTrust = this.playerTrust.get(playerIndex);

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

  private void updateFromMapMove(Move move) {
    Board.GoalPosition pos = Board.GoalPosition.values()[move.args()[0]];
    othersGoalKnowledge.get(move.playerIndex()).add(pos);
  }

  private void updateFromDiscardMove(Move move) {
    // TODO: no trust update for now
  }

  private int getNumEnemies() {
    return role == Player.Role.SABOTEUR
        ? game.numPlayers() - game.numSaboteurs()
        : game.numSaboteurs();
  }

  private int getNumFriends() {
    int numFriends = role == Player.Role.GOLD_MINER
        ? game.numPlayers() - game.numSaboteurs()
        : game.numSaboteurs();
    numFriends -= 1; // exclude self

    return numFriends;
  }

  void updateKnownGoals(Board.GoalPosition pos, GoalType type) {
    if (type == GoalType.GOLD) {
      othersGoalKnowledge.forEach(s -> s.addAll(Arrays.asList(Board.GoalPosition.values())));
    } else {
      othersGoalKnowledge.forEach(s -> s.add(pos));
    }
  }

  private void initTrust(GameLogicController game, int playerIndex) {
    int n = game.numPlayers();
    for (int i = 0; i < n; ++i) {
      othersGoalKnowledge.add(new HashSet<>());
      if (i == playerIndex) continue;
      playerTrust.put(i, INITIAL_TRUST);
    }
  }
}
