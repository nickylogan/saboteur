/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.proposed;

import ai.AI;
import model.Board;
import model.GoalType;
import model.Move;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static ai.proposed.Utils.getGoldPosition;

public class SaboteurAI extends AI {
  public static int VERBOSE = 0;
  public static int playAsMiner = 0;
  public static int playAsSaboteur = 0;
  public static int winAsMiner = 0;
  public static int winAsSaboteur = 0;
  static final double EPS = 1e-6;
  static final double DISCARD_HEURISTIC = RockfallMovePredictor.BASE_MULTIPLIER / 3;

  RolePredictor rolePredictor;
  PathMovePredictor pathMovePredictor;
  RockfallMovePredictor rockfallMovePredictor;
  PlayerActionMovePredictor playerActionMovePredictor;
  MapMovePredictor mapMovePredictor;
  BoardPredictor boardPredictor;
  private double discardHeuristic;

  /**
   * Creates an {@link AI} object representing an AI for the game
   */
  public SaboteurAI(String name) {
    super(name);
  }

  @Override
  protected Move makeDecision() {
    if (VERBOSE > 0) {
      System.out.println("############################################################");
      System.out.println(role());
      System.out.println(hand());
      System.out.println(knownGoals());
    }
    List<MoveHeuristic> heuristics = new ArrayList<>();
    ArrayList<Card> hand = hand();
    int len = hand.size();
    for (int i = 0; i < len; ++i) {
      MoveHeuristic moveHeuristic = null;
      Card card = hand.get(i);
      if (VERBOSE > 0) System.out.println(card);
      if (card instanceof PathCard) {
        moveHeuristic = pathMovePredictor.generatePathHeuristic(i, (PathCard) card, game().board(), knownGoals());
      } else if (card.type() == Card.Type.MAP) {
        moveHeuristic = mapMovePredictor.generateMapHeuristic(i, knownGoals());
      } else if (card.type() == Card.Type.BLOCK) {
        moveHeuristic = playerActionMovePredictor.generateBlockHeuristic(i, (PlayerActionCard) card);
      } else if (card.type() == Card.Type.REPAIR) {
        moveHeuristic = playerActionMovePredictor.generateRepairHeuristic(i, (PlayerActionCard) card);
      } else if (card.type() == Card.Type.ROCKFALL) {
        moveHeuristic = rockfallMovePredictor.generateRockfallHeuristic(i, knownGoals(), hand);
      }
      heuristics.add(moveHeuristic);
    }
    Collections.shuffle(heuristics);
    heuristics.sort(Collections.reverseOrder(Comparator.comparingDouble(MoveHeuristic::heuristic)));
    if (VERBOSE > 0) {
      for (MoveHeuristic mh : heuristics) {
        System.out.print(hand().get(mh.move.handIndex()) + " = ");
        System.out.println(mh);
      }
    }
    Move move = null;
    for (MoveHeuristic mh : heuristics) {
      if (mh.heuristic > -.5 && checkLegal(mh.move)) {
        move = mh.move;
        break;
      }
    }
    if (move == null) {
      move = Move.NewDiscardMove(index(), heuristics.get(heuristics.size() - 1).move.handIndex());
    }
    if (VERBOSE > 0) {
      System.out.println(move);
      System.out.println("############################################################");
    }
    // Scanner sc = new Scanner(System.in);
    // sc.nextLine();
    return move;
  }

  private boolean checkLegal(Move move) {
    if (move.type() == Move.Type.PLAY_PATH) {
      if (isSabotaged()) {
        return false;
      }
      PathCard card = (PathCard) hand().get(move.handIndex()).copy();
      card.setRotated(move.args()[2] == 1);
      return game().board().isCardPlaceableAt(card, move.args()[0], move.args()[1]);
    }
    if (move.type() == Move.Type.PLAY_PLAYER) {
      PlayerActionCard card = (PlayerActionCard) hand().get(move.handIndex());
      int target = move.args()[0];
      if (target < 0 || target >= game().numPlayers()) {
        return false;
      }
      if (card.type() == Card.Type.BLOCK && (target == index() || !game().playerAt(target).isSabotageable(card.effects()[0]))) {
        return false;
      }
      if (card.type() == Card.Type.REPAIR && !game().playerAt(target).isRepairable(card.effects())) {
        return false;
      }
    }
    if (move.type() == Move.Type.PLAY_ROCKFALL) {
      return game().board().isDestroyable(move.args()[0], move.args()[1]);
    }
    return true;
  }

  @Override
  public void initialize() {
    rolePredictor = new RolePredictor(game(), index(), role(), this);
    boardPredictor = new BoardPredictor(game(), role());
    pathMovePredictor = new PathMovePredictor(game(), index(), role(), boardPredictor);
    rockfallMovePredictor = new RockfallMovePredictor(game(), index(), role(), this);
    playerActionMovePredictor = new PlayerActionMovePredictor(game(), index(), this);
    mapMovePredictor = new MapMovePredictor(game(), index());
    discardHeuristic = DISCARD_HEURISTIC;
    playAsSaboteur += role() == Role.SABOTEUR ? 1 : 0;
    playAsMiner += role() == Role.GOLD_MINER ? 1 : 0;
  }

  @Override
  protected void onOtherPlayerMove(Move move) {
    if (VERBOSE > 0) System.out.println(name());
    // System.out.println("Other: " + move);
    switch (move.type()) {
      case PLAY_PATH:
        rolePredictor.updateFromPathMove(move, knownGoals());
        break;
      case PLAY_ROCKFALL:
        rolePredictor.updateFromRockfallMove(move, knownGoals());
        break;
      case PLAY_MAP:
        rolePredictor.updateFromMapMove(move);
        break;
      case PLAY_PLAYER:
        if (move.card().type() == Card.Type.BLOCK) {
          rolePredictor.updateFromBlockMove(move);
        } else {
          rolePredictor.updateFromRepairMove(move);
        }
        break;
      case DISCARD:
        rolePredictor.updateFromDiscardMove(move);
        break;
    }
    double rh = playerActionMovePredictor.getRepairHeuristic();
    playerActionMovePredictor.setRepairHeuristic(rh + .01);
    if (VERBOSE > 0) {
      System.out.println(rolePredictor.playerTrust());
      System.out.println(rolePredictor.goalKnowledge());
    }
  }

  @Override
  protected void onGoalOpen(Board.GoalPosition position, GoalType goalType, boolean permanent) {
    // TODO: add knowledge about other players
    if (permanent) rolePredictor.updateKnownGoals(position, goalType);
    if (knownGoals().size() == 3) {
      return;
    }
    if (knownGoals().values().contains(GoalType.GOLD)) {
      Board.GoalPosition pos = getGoldPosition(knownGoals());
      for (Board.GoalPosition p : Board.GoalPosition.values()) {
          if (p != pos) knownGoals().put(p, GoalType.ROCK);
      }
      return;
    }
    if (knownGoals().values().size() == 2) {
      for (Board.GoalPosition p : Board.GoalPosition.values()) {
        if (!knownGoals().keySet().contains(p)) knownGoals().put(p, GoalType.GOLD);
      }
    }
  }

  @Override
  protected void onGameFinished(Role role, int lastPlayer) {
    if (role == role() && role == Role.SABOTEUR) ++winAsSaboteur;
    if (role == role() && role == Role.GOLD_MINER) ++winAsMiner;
  }
}
