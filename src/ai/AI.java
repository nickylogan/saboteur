package ai;

import model.Player;

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
 *   private Move makeDecision() {
 *     //
 *   }
 *
 *   &#64;Override
 *   public void onMovementPrompt() {
 *     Move move = makeDecision();
 *     try {
 *       // Play the decided move to the game
 *       state().playMove(move);
 *     } catch (GameException e) {
 *       System.out.println(e.getMessage());
 *     }
 *   }
 * }
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
  public AI(String name) { super(name); }
}
