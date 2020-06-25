package main;

import ai.AI;
import ai.proposed.SaboteurAI;
import model.*;
import model.cards.Card;

import java.util.ArrayList;

public class CLIMain extends GameObserver {
  private static int saboteur = 0;
  private static int miner = 0;
  private static int i = 0;
  private static ArrayList<String> results = new ArrayList<>();

  @Override
  protected void onGameFinished(Player.Role role, int lastPlayer) {
    if (role == Player.Role.SABOTEUR)
      ++saboteur;
    else
      ++miner;

    System.out.printf("%d: %s (s=%d, m=%d)\n", ++i, role, saboteur, miner);

    if (i % 1000 == 0) {
      String result;
      if (saboteur >= miner)
        result = String.format("%d\t%d\t%.2f : 1", saboteur, miner, (float) saboteur / miner);
      else
        result = String.format("%d\t%d\t1 : %.2f", saboteur, miner, (float) miner / saboteur);
      results.add(result);
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
    for (int i = 0; i < 18000; ++i) {
      GameLogicController.COMPETITION_MODE = i / 3000;
      GameState state = new GameState();
      SaboteurAI sai1 = new SaboteurAI("sai1");
      SaboteurAI sai2 = new SaboteurAI("sai2");
      SaboteurAI mai1 = new SaboteurAI("mai1");
      SaboteurAI mai2 = new SaboteurAI("mai2");
      SaboteurAI mai3 = new SaboteurAI("mai3");
      SaboteurAI mai4 = new SaboteurAI("mai4");
      SaboteurAI mai5 = new SaboteurAI("mai5");
      SaboteurAI mai6 = new SaboteurAI("mai6");
      AI[][] players = new AI[][]{
        {sai1, mai1, mai2, mai3},
        {sai1, mai1, mai2, mai3, mai4},
        {sai1, sai2, mai1, mai2, mai3},
        {sai1, sai2, mai1, mai2, mai3, mai4},
        {sai1, sai2, mai1, mai2, mai3, mai4, mai5},
        {sai1, sai2, mai1, mai2, mai3, mai4, mai5, mai6},
      };
      GameLogicController game = new GameLogicController(state, players[i / 3000]);
      game.addObserver(new CLIMain());
      game.initializeRound();
      game.startRound();
    }

    for (int i = 0; i < results.size(); ++i) {
      if (i % 5 == 0) System.out.println();
      System.out.println(results.get(i));
    }
  }
}
