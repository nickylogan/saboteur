/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 */

package ai.impl.original;

import ai.AI;
import model.Board;
import model.GoalType;
import model.Move;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.*;

@SuppressWarnings("Duplicates")
public class SaboteurAI extends AI {
  public static int playAsMiner = 0;
  public static int playAsSaboteur = 0;
  public static int winAsMiner = 0;
  public static int winAsSaboteur = 0;

  static final double k0 = 0.5;
  static final double k1 = k0 + 2.5;
  static final double k2 = k0 - 0.1;
  static final double k3 = k2;
  static final double k4 = k2;
  static final double k5 = k2/2;
  static final double c1 = 5*k0;
  static final double c2 = c1 + 0.1;
  static final double c3 = 0.5;
  static final double c4 = c3;

  static final double EPS = 1e-6;
  private static final double DISCARD_HEURISTIC = RockfallMovePredictor.BASE_HEURISTIC / 3;

  private RolePredictor rolePredictor;
  private PathMovePredictor pathMovePredictor;
  private RockfallMovePredictor rockfallMovePredictor;
  private PlayerActionMovePredictor playerActionMovePredictor;
  private MapMovePredictor mapMovePredictor;
  private double discardHeuristic = k5;

  /**
   * Creates an {@link AI} object representing an AI for the game
   */
  public SaboteurAI(String name) {
    super(name);
  }

  @Override
  protected Move makeDecision() {
    List<MoveHeuristic> heuristics = new ArrayList<>();
    List<Card> hand = hand();
    int len = hand.size();
    for (int i = 0; i < len; ++i) {
      MoveHeuristic moveHeuristic = null;
      Card card = hand.get(i);
      if (card instanceof PathCard) {
        moveHeuristic = pathMovePredictor.generatePathHeuristic(i, (PathCard) card, game().board(), knownGoals());
      } else if (card.type() == Card.Type.MAP) {
        moveHeuristic = mapMovePredictor.generateMapHeuristic(i, knownGoals());
      } else if (card.type() == Card.Type.BLOCK) {
        moveHeuristic = playerActionMovePredictor.generateBlockHeuristic(i, (PlayerActionCard) card);
      } else if (card.type() == Card.Type.REPAIR) {
        moveHeuristic = playerActionMovePredictor.generateRepairHeuristic(i, (PlayerActionCard) card);
      } else if (card.type() == Card.Type.ROCKFALL) {
        moveHeuristic = rockfallMovePredictor.generateRockfallHeuristic(i, knownGoals());
      }
      heuristics.add(moveHeuristic);
    }
    Collections.shuffle(heuristics);
    heuristics.sort(Collections.reverseOrder(Comparator.comparingDouble(MoveHeuristic::heuristic)));
    Move move = null;
    for (MoveHeuristic mh : heuristics) {
      if (mh.heuristic > discardHeuristic && checkLegal(mh.move)) {
        move = mh.move;
        break;
      }
    }
    if (move == null) {
      move = Move.NewDiscardMove(index(), heuristics.get(heuristics.size() - 1).move.handIndex());
    }
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
    rolePredictor = new RolePredictor(game(), index(), role());
    pathMovePredictor = new PathMovePredictor(game(), index(), role());
    rockfallMovePredictor = new RockfallMovePredictor(game(), index(), role());
    playerActionMovePredictor = new PlayerActionMovePredictor(game(), index(), rolePredictor);
    mapMovePredictor = new MapMovePredictor(game(), index());
    discardHeuristic = DISCARD_HEURISTIC;

    playAsSaboteur += role() == Role.SABOTEUR ? 1 : 0;
    playAsMiner += role() == Role.GOLD_MINER ? 1 : 0;
  }

  @Override
  protected void onOtherPlayerMove(Move move) {
    switch (move.type()) {
      case PLAY_PATH:
        rolePredictor.updateFromPathMove(move, pathMovePredictor, knownGoals());
        break;
      case PLAY_ROCKFALL:
        rolePredictor.updateFromRockfallMove(move);
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
  }

  @Override
  protected void onGoalOpen(Board.GoalPosition position, GoalType goalType, boolean permanent) {
    if (permanent) rolePredictor.updateKnownGoals(position, goalType);
    if (knownGoals().size() == 3) {
      return;
    }
    if (knownGoals().values().contains(GoalType.GOLD)) {
      Board.GoalPosition pos = getGoalPosition(knownGoals());
      for (Board.GoalPosition p : Board.GoalPosition.values()) {
        if (p != pos) knownGoals().put(p, GoalType.ROCK);
      }
      return;
    }
    if (knownGoals().values().size() == 2) {
      for (Board.GoalPosition p : Board.GoalPosition.values()) {
        knownGoals().put(p, GoalType.GOLD);
      }
    }
  }

  private Board.GoalPosition getGoalPosition(Map<Board.GoalPosition, GoalType> knownGoals) {
    if (!knownGoals.values().contains(GoalType.GOLD)) {
      return null;
    }
    if (knownGoals.get(Board.GoalPosition.TOP) == GoalType.GOLD) {
      return Board.GoalPosition.TOP;
    } else if (knownGoals.get(Board.GoalPosition.MIDDLE) == GoalType.GOLD) {
      return Board.GoalPosition.MIDDLE;
    } else {
      return Board.GoalPosition.BOTTOM;
    }
  }

  @Override
  protected void onGameFinished(Role role, int lastPlayer) {
    if (role == role() && role == Role.SABOTEUR) ++winAsSaboteur;
    if (role == role() && role == Role.GOLD_MINER) ++winAsMiner;
  }
}
