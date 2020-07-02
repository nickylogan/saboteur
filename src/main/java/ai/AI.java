package ai;

import ai.utils.Log;
import model.GameException;
import model.Move;
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
  private static final int TIMEOUT_SECONDS = 5;

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
      move = task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      game().playMove(move);
    } catch (InterruptedException e) {
      Log.errorln("Decision making interrupted");
      System.exit(1);
    } catch (TimeoutException e) {
      Log.errorf("'%s' took too long to make a decision", this.name());
      fallbackAndLog();
    } catch (GameException | ExecutionException e) {
      e.printStackTrace();
      Log.errorf("'%s' made an illegal move: %s", this.name(), e.getMessage());
      fallbackAndLog(move);
    }
  }

  private void fallbackAndLog(Move move) {
    Move fallback;
    if (move != null) {
      Log.warnln("Falling back to discarding the played card.");
      fallback = Move.NewDiscardMove(index(), move.handIndex());
    } else {
      Log.warnln("Falling back to discarding the first card.");
      fallback = Move.NewDiscardMove(index(), 0);
    }

    try {
      game().playMove(fallback);
    } catch (GameException e) {
      Log.errorf("Fallback failed: %s", e.getMessage());
    }
  }

  private void fallbackAndLog() {
    this.fallbackAndLog(null);
  }

  /**
   * Implement this to do something when another player moves
   *
   * @param move the played move
   */
  protected void onOtherPlayerMove(Move move) { }

  @Override
  protected final void onPlayerMove(Move move, Card newCard) {
    if (move.playerIndex() == index()) return;

    try {
      onOtherPlayerMove(move);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected final void onGameStart() { }

  @Override
  protected final void onNextTurn(int player, Role role, ArrayList<Card> hand) { }
}
