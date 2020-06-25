package main;

import customAI.nn.CustomAI;
import gui.GameGUIController;
import javafx.application.Application;
import javafx.stage.Stage;
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

    CustomAI customAi1 = new CustomAI("Alice");
    CustomAI customAi2 = new CustomAI("Bob");
    CustomAI customAi3 = new CustomAI("Charlie");
    CustomAI customAi4 = new CustomAI("Daisy");

    // HeuristicsAI ray1 = new HeuristicsAI("Saboteur");
    // HeuristicsAI ray2 = new HeuristicsAI("Miner 2");
    // HeuristicsAI ray3 = new HeuristicsAI("Miner 3");
    // HeuristicsAI ray4 = new HeuristicsAI("Miner 4");
    // HeuristicsAI ray5 = new HeuristicsAI("Miner 5");

    Player[] players = new Player[]{
        customAi1, customAi2, customAi3, customAi4
    };

    setUserAgentStylesheet(STYLESHEET_MODENA);
    window = GameGUIController.NewGameSession(players);
  }
}
