package ai;

import model.GameException;
import model.Move;
import model.MoveResult;
import model.Player;
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
  /** The result of the last move */
  protected MoveResult lastResult;

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

  @Override
  protected final void onMovementPrompt() {
    FutureTask<Move> task = new FutureTask<>(this::makeDecision);
    try {
      new Thread(task).start();
      Move move = task.get(5, TimeUnit.SECONDS);
      lastResult = state().playMove(move);
    } catch (InterruptedException e) {
      System.out.println("Decision making interrupted");
    } catch (TimeoutException e) {
      System.out.println("Decision timeout");
    } catch (GameException | ExecutionException e) {
      System.out.println(e.getMessage());
    }
  }

  @Override
  protected final void onPlayerMove(Move move, Card newCard) {}

  @Override
  protected final void onGameStart() { }

  @Override
  protected final void onNextTurn(int player, ArrayList<Card> hand) { }
}
