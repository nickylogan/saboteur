package main;

import model.*;

import java.util.Arrays;
import java.util.Scanner;

public class Main extends GameObserver {
  private static Scanner sc = new Scanner(System.in);

  @Override
  protected void onGameStart() {
    System.out.println("Game started");
  }

  @Override
  protected void onPlayerMove(Move move) {
    System.out.println(move);
    System.out.println(state().board().getReachable());
    for (int i = 0; i < 5; ++i) {
      System.out.println(i + ": " + Arrays.toString(state().playerAt(i).sabotaged()));
    }
    System.out.print("Press ENTER to continue...");
    sc.nextLine();
  }

  @Override
  protected void onGameFinished(Player.Role role, int lastPlayer) {
    System.out.println("Winner: " + role);
  }

  public static void main(String[] args) throws GameException {
    MockGame game = MockGame.CreateMockGame(new Main());
    game.addMove(Move.NewPlayerActionMove(game.currentPlayerIndex(), 2, 2));
    game.addMove(Move.NewPathMove(game.currentPlayerIndex(), 0, 0, 0, false));
  }
}
