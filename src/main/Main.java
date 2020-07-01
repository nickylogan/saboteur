package main;

import ai.proposed.SaboteurAI;
import ai.utils.Log;
import gui.GameGUIController;
import gui.GameGUIController.GUIGamePlayer;
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
    GameLogicController.COMPETITION_MODE = 0;
    Log.init(Log.Level.DEBUG);

    SaboteurAI saboteurAi1 = new SaboteurAI("Alice");
    SaboteurAI saboteurAi2 = new SaboteurAI("Bob");
    SaboteurAI saboteurAi3 = new SaboteurAI("Charlie");
    SaboteurAI saboteurAi4 = new SaboteurAI("Daisy");

    // HeuristicsAI ray1 = new HeuristicsAI("Saboteur");
    // HeuristicsAI ray2 = new HeuristicsAI("Miner 2");
    // HeuristicsAI ray3 = new HeuristicsAI("Miner 3");
    // HeuristicsAI ray4 = new HeuristicsAI("Miner 4");
    // HeuristicsAI ray5 = new HeuristicsAI("Miner 5");

    Player[] players = new Player[]{
        saboteurAi1, saboteurAi2, saboteurAi3, saboteurAi4
    };

    setUserAgentStylesheet(STYLESHEET_MODENA);
    window = GameGUIController.NewGameSession(players);
  }
}
