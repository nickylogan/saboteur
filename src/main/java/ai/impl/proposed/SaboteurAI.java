/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package ai.impl.proposed;

import ai.AI;
import ai.impl.proposed.utils.DoubleUtils;
import ai.utils.Log;
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
import java.util.stream.Collectors;

public class SaboteurAI extends AI {
  public static int playAsMiner = 0;
  public static int playAsSaboteur = 0;
  public static int winAsMiner = 0;
  public static int winAsSaboteur = 0;
  static final double EPS = 1e-6;
  static final double MIN_HEURISTIC = -.5;

  private RolePredictor rolePredictor;
  private PathMovePredictor pathMovePredictor;
  private RockfallMovePredictor rockfallMovePredictor;
  private PlayerActionMovePredictor playerActionMovePredictor;
  private MapMovePredictor mapMovePredictor;
  private BoardPredictor boardPredictor;
  private MoveValidator moveValidator;
  private GoalKnowledge goalKnowledge;

  /**
   * Creates an {@link AI} object representing an AI for the game
   */
  public SaboteurAI(String name) {
    super(name);
  }

  @Override
  public void initialize() {
    DoubleUtils.setEpsilon(EPS);

    goalKnowledge =
        new GoalKnowledge(knownGoals());
    boardPredictor =
        new BoardPredictor(role(), goalKnowledge);
    pathMovePredictor =
        new PathMovePredictor(index(), role(), boardPredictor);
    rolePredictor =
        new RolePredictor(game(), index(), role(), boardPredictor);
    rockfallMovePredictor =
        new RockfallMovePredictor(game(), index(), role(), boardPredictor);
    playerActionMovePredictor =
        new PlayerActionMovePredictor(game(), index(), rolePredictor);
    mapMovePredictor =
        new MapMovePredictor(index(), goalKnowledge);
    moveValidator =
        new MoveValidator(game(), this);

    updatePlayStats();
  }

  @Override
  protected Move makeDecision() {
    List<MoveHeuristic> possibleMoves = generateSortedMoves();
    Log.debugln(
        possibleMoves.stream()
            .map(MoveHeuristic::toString)
            .collect(Collectors.joining("\n"))
    );

    Move move = null;
    for (MoveHeuristic mh : possibleMoves) {
      // ignore move with low heuristic
      if (mh.heuristic < MIN_HEURISTIC) continue;

      Card card = hand().get(mh.move.handIndex()).copy();
      if (moveValidator.isLegal(mh.move, card)) {
        move = mh.move;
        break;
      }
    }

    // no good moves, discard worst card.
    if (move == null) {
      MoveHeuristic worst = possibleMoves.get(possibleMoves.size() - 1);
      move = Move.NewDiscardMove(index(), worst.move.handIndex());
    }

    return move;
  }

  private List<MoveHeuristic> generateSortedMoves() {
    List<MoveHeuristic> heuristics = new ArrayList<>();

    for (int i = 0; i < hand().size(); i++) {
      MoveHeuristic moveHeuristic = null;

      Card card = hand().get(i);
      switch (card.type()) {
        case PATHWAY:
        case DEADEND:
          moveHeuristic = pathMovePredictor.generatePathHeuristic(game().board(), i, (PathCard) card);
          break;
        case MAP:
          moveHeuristic = mapMovePredictor.generateMapHeuristic(i);
          break;
        case BLOCK:
          moveHeuristic = playerActionMovePredictor.generateBlockHeuristic(i, (PlayerActionCard) card);
          break;
        case REPAIR:
          moveHeuristic = playerActionMovePredictor.generateRepairHeuristic(i, (PlayerActionCard) card);
          break;
        case ROCKFALL:
          moveHeuristic = rockfallMovePredictor.generateRockfallHeuristic(i);
          break;
      }

      if (moveHeuristic == null) continue;

      heuristics.add(moveHeuristic);
    }

    // Shuffle and sort moves from best to worst
    Collections.shuffle(heuristics);
    heuristics.sort(Collections.reverseOrder(Comparator.comparingDouble(MoveHeuristic::heuristic)));

    return heuristics;
  }


  @Override
  protected void onOtherPlayerMove(Move move) {
    rolePredictor.updateFromMove(move);
    playerActionMovePredictor.update();
  }

  @Override
  protected void onGoalOpen(Board.GoalPosition position, GoalType goalType, boolean permanent) {
    if (permanent) {
      // TODO: add knowledge about other players
      rolePredictor.updateKnownGoals(position, goalType);
    }

    goalKnowledge.infer();
  }

  @Override
  protected void onGameFinished(Role role, int lastPlayer) {
    updateWinStats(role);
  }

  private void updatePlayStats() {
    playAsSaboteur += role() == Role.SABOTEUR ? 1 : 0;
    playAsMiner += role() == Role.GOLD_MINER ? 1 : 0;
  }

  private void updateWinStats(Role role) {
    if (role != role()) return;

    switch (role) {
      case SABOTEUR:
        ++winAsSaboteur;
        break;
      case GOLD_MINER:
        ++winAsMiner;
        break;
    }
  }
}
