package main;

import customAI.angjoshel.Core;
import customAI.jasson.Jasson;
import customAI.jerry.AIJR;
import customAI.nn.CustomAI;
import customAI.yj.YJ_AI;
import customAI.cen.botGDCN;
import gui.GameGUIController;
import javafx.application.Application;
import javafx.stage.Stage;
import main.HeuristicsAI;
import model.GameLogicController;
import model.Player;

public class Main extends Application {
  private static GameGUIController window;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    // CustomAI.VERBOSE = 1;
    GameLogicController.COMPETITION_MODE = 0;

    // CustomAI customAi1 = new CustomAI("Nicky");
    // Perceptron customAi2 = new Perceptron("Wilbert");
    // AIPakSam customAi3 = new AIPakSam("Ricky");
    // FishAI customAi4 = new FishAI("Davis");
    // AndreTomAI customAi5 = new AndreTomAI("Thompson");
    // GUIGamePlayer testAI5 = new GUIGamePlayer("Eve");

    HeuristicsAI ray1 = new HeuristicsAI("Saboteur");
    HeuristicsAI ray2 = new HeuristicsAI("Miner 2");
    HeuristicsAI ray3 = new HeuristicsAI("Miner 3");
    HeuristicsAI ray4 = new HeuristicsAI("Miner 4");
    HeuristicsAI ray5 = new HeuristicsAI("Miner 5");

    Player[] players = new Player[] {
      ray1, ray2, ray3, ray4, ray5
    };

    setUserAgentStylesheet(STYLESHEET_MODENA);
    window = GameGUIController.NewGameSession(players);
  }
}
