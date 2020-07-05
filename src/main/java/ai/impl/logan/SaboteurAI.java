package ai.impl.logan;

import ai.AI;
import ai.impl.logan.utils.DoubleUtils;
import ai.utils.Log;
import model.*;
import model.cards.*;

import java.util.*;
import java.util.stream.Collectors;

public class SaboteurAI extends AI {
  private static final Stats stats = new Stats();
  private static final double EPS = 1e-6;
  private static final double MIN_HEURISTIC = -.5;

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

    stats.incrementPlay(role());
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
      Card card = hand().get(i);
      MoveHeuristic moveHeuristic = switch (card.type()) {
        case PATHWAY, DEADEND ->
            pathMovePredictor.generatePathHeuristic(game().board(), i, (PathCard) card);
        case MAP ->
            mapMovePredictor.generateMapHeuristic(i);
        case BLOCK ->
            playerActionMovePredictor.generateBlockHeuristic(i, (PlayerActionCard) card);
        case REPAIR ->
            playerActionMovePredictor.generateRepairHeuristic(i, (PlayerActionCard) card);
        case ROCKFALL ->
            rockfallMovePredictor.generateRockfallHeuristic(i);
      };

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

  private void updateWinStats(Role role) {
    if (role != role()) return;

    stats.incrementWin(role);
  }

  public static Stats getStats() {
    return stats;
  }
}
