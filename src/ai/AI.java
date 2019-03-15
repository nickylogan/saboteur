package ai;

import com.sun.istack.internal.NotNull;
import model.GameException;
import model.Move;
import model.Player;

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
   * Implement this method to get a decision
   *
   * @return Move decided move
   */
  abstract Move makeDecision();

  @Override
  public final void onMovementPrompt() {
    FutureTask<Move> task = new FutureTask<>(this::makeDecision);
    try {
      Move move = task.get(5, TimeUnit.SECONDS);
      state().playMove(move);
    } catch (InterruptedException e) {
      System.out.println("Decision making interrupted");
    } catch (TimeoutException e) {
      System.out.println("Decision timeout");
    } catch (GameException | ExecutionException e) {
      System.out.println(e.getMessage());
    }
  }
}
