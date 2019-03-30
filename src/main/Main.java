package main;

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
    TestAI testAI1 = new TestAI("Alice");
    GUIGamePlayer testAI2 = new GUIGamePlayer("Bob");
    GUIGamePlayer testAI3 = new GUIGamePlayer("Charlie");
    GUIGamePlayer testAI4 = new GUIGamePlayer("Daisy");
    GUIGamePlayer testAI5 = new GUIGamePlayer("Eve");
    setUserAgentStylesheet(STYLESHEET_MODENA);
    window = GameGUIController.NewGameSession(testAI1, testAI2, testAI3, testAI4, testAI5);
  }
}
