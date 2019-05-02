package main;

import ai.AI;
import customAI.nn.CustomAI;
import customAI.paper.PaperAI;
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
      CustomAI sai1 = new CustomAI("sai1");
      CustomAI sai2 = new CustomAI("sai2");
      CustomAI mai1 = new CustomAI("mai1");
      CustomAI mai2 = new CustomAI("mai2");
      CustomAI mai3 = new CustomAI("mai3");
      CustomAI mai4 = new CustomAI("mai4");
      CustomAI mai5 = new CustomAI("mai5");
      CustomAI mai6 = new CustomAI("mai6");
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
