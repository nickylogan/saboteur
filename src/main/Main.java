package main;

import customAI.CustomAI;
import gui.GameGUIController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
  private static GameGUIController window;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    CustomAI customAi1 = new CustomAI("Stupidity 1");
    CustomAI customAi2 = new CustomAI("Stupidity 2");
    CustomAI customAi3 = new CustomAI("Stupidity 3");
    CustomAI customAi4 = new CustomAI("Stupidity 4");
    // GUIGamePlayer testAI5 = new GUIGamePlayer("Eve");
    setUserAgentStylesheet(STYLESHEET_MODENA);
    window = GameGUIController.NewGameSession(customAi1, customAi2, customAi3, customAi4);
  }
}
