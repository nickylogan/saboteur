package main;

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
    TestAI testAI1 = new TestAI("Alice");
    TestAI testAI2 = new TestAI("Bob");
    TestAI testAI3 = new TestAI("Charlie");
    TestAI testAI4 = new TestAI("Daisy");
    TestAI testAI5 = new TestAI("Eve");
    setUserAgentStylesheet(STYLESHEET_MODENA);
    window = GameGUIController.NewGameSession(testAI1, testAI2, testAI3, testAI4, testAI5);
  }
}
