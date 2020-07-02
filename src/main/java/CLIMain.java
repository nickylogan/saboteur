import ai.AI;
import model.*;
import model.cards.Card;

public class CLIMain extends GameObserver {
  private static int saboteur = 0;
  private static int miner = 0;
  private static int i = 0;
  private static int epoch = 1000;

  @Override
  protected void onGameFinished(Player.Role role, int lastPlayer) {
    if (role == Player.Role.SABOTEUR)
      ++saboteur;
    else
      ++miner;

    System.out.printf("%d: %s (s=%d, m=%d)\r", ++i, role, saboteur, miner);

    if (i % epoch == 0) {
      String saboteurRate = String.format("%d (%.2f%%)", saboteur, 100.0 * saboteur / epoch);
      String minerRate = String.format("%d (%.2f%%)", miner, 100.0 * miner / epoch);
      String ratio;
      if (saboteur >= miner) {
        ratio = String.format("%.2f : 1", (float) saboteur / miner);
      } else {
        ratio = String.format("1 : %.2f", (float) miner / saboteur);
      }

      String result = String.format("%s\t%s\t%s", saboteurRate, minerRate, ratio);
      System.out.println(result);

      saboteur = 0;
      miner = 0;
    }
  }

  @Override
  protected void onPlayerMove(Move move, Card newCard) {
    game().finalizeTurn();
  }

  public static void main(String[] args) throws GameException {
    for (int i = 0; i < 6 * epoch; ++i) {
      GameLogicController.COMPETITION_MODE = i / epoch;
      GameState state = new GameState();
      ai.proposed.SaboteurAI sai1 = new ai.proposed.SaboteurAI("sai1");
      ai.proposed.SaboteurAI sai2 = new ai.proposed.SaboteurAI("sai2");
      ai.proposed.SaboteurAI mai1 = new ai.proposed.SaboteurAI("mai1");
      ai.proposed.SaboteurAI mai2 = new ai.proposed.SaboteurAI("mai2");
      ai.proposed.SaboteurAI mai3 = new ai.proposed.SaboteurAI("mai3");
      ai.proposed.SaboteurAI mai4 = new ai.proposed.SaboteurAI("mai4");
      ai.proposed.SaboteurAI mai5 = new ai.proposed.SaboteurAI("mai5");
      ai.proposed.SaboteurAI mai6 = new ai.proposed.SaboteurAI("mai6");
      AI[][] players = new AI[][]{
          {sai1, mai1, mai2, mai3},
          {sai1, mai1, mai2, mai3, mai4},
          {sai1, sai2, mai1, mai2, mai3},
          {sai1, sai2, mai1, mai2, mai3, mai4},
          {sai1, sai2, mai1, mai2, mai3, mai4, mai5},
          {sai1, sai2, mai1, mai2, mai3, mai4, mai5, mai6},
      };
      GameLogicController game = new GameLogicController(state, players[i / epoch]);
      game.addObserver(new CLIMain());
      game.initializeRound();
      game.startRound();
    }
  }
}
