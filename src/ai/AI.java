package ai;

import model.*;
import model.cards.Card;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The {@link AI} class represents an AI class. Extend this to implement
 * your own <code>AI</code> class.
 *
 * <p>Example implementation:</p>
 * <pre><code>
 * package ai;
 *
 * import model.GameException;
 * import model.Move;
 * import model.Player;
 *
 * public class ExampleAI extends AI {
 *   public ExampleAI(String name) {
 *     super(name);
 *   }
 *
 *   &#64;Override
 *   private Move makeDecision() {
 *     //
 *   }
 * </code></pre>
 */
@SuppressWarnings("unused")
public abstract class AI extends Player {
  /**
   * Creates an {@link AI} object representing an AI for the game
   *
   * @param name the {@link Player}'s name
   * @see Player
   */
  public AI(String name) {
    super(name);
  }

  /**
   * Creates a decision based on the game state.
   *
   * @return a {@link Move} representing the AI's decision
   */
  protected abstract Move makeDecision();

  /**
   * Implement this to do something when the game is first initialized
   */
  public void initialize() {}

  @Override
  protected final void onMovementPrompt() {
    FutureTask<Move> task = new FutureTask<>(this::makeDecision);
    Move move = null;
    try {
      new Thread(task).start();
      move = task.get(5, TimeUnit.SECONDS);
      game().playMove(move);
    } catch (InterruptedException e) {
      // System.out.println("Decision making interrupted");
    } catch (TimeoutException e) {
      // System.out.println("Decision timeout. Defaulting to discarding the first card");
      move = Move.NewDiscardMove(index(), 0);
      try { game().playMove(move); } catch (GameException ignored) {}
    } catch (GameException | ExecutionException e) {
      // e.printStackTrace();
      // System.out.println("Unallowed decision: " + e.getMessage());
      if (move != null) {
        if (move.handIndex() < 0 || move.handIndex() >= handSize()) {
          // System.out.println("Defaulting to discarding the first card");
          move = Move.NewDiscardMove(index(), 0);
        } else {
          // System.out.println("Defaulting to discarding the played card");
          move = Move.NewDiscardMove(index(), move.handIndex());
        }
      } else {
        // System.out.println("Defaulting to discarding the first card");
        move = Move.NewDiscardMove(index(), 0);
      }
      try { game().playMove(move); } catch (GameException ignored) {}
    }
  }

  /**
   * Implement this to do something when another player moves
   *
   * @param move the played move
   */
  protected void onOtherPlayerMove(Move move) { }

  @Override
  protected final void onPlayerMove(Move move, Card newCard) {
    if (move.playerIndex() != index()) {
      try {
        onOtherPlayerMove(move);
      } catch (Exception ignored) {}
    }
  }

  @Override
  protected final void onGameStart() { }

  @Override
  protected final void onNextTurn(int player, Role role, ArrayList<Card> hand) { }
}
