package main;

import customAI.nicky.AINicky;
import gui.GameGUIController;
import javafx.application.Application;
import javafx.stage.Stage;

import static gui.GameGUIController.GUIGamePlayer;

public class Main extends Application {
  private static GameGUIController window;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    AINicky aiNicky = new AINicky();
    TestAI testAI2 = new TestAI("Bob");
    TestAI testAI3 = new TestAI("Charlie");
    TestAI testAI4 = new TestAI("Daisy");
    // GUIGamePlayer testAI5 = new GUIGamePlayer("Eve");
    setUserAgentStylesheet(STYLESHEET_MODENA);
    window = GameGUIController.NewGameSession(testAI2, testAI3, testAI4, aiNicky);
  }
}
