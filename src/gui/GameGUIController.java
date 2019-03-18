package gui;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Window extends Stage {
  private Scene scene;
  private StackPane layers;

  public Window(GameWindow controller) {
    this.controller = controller;
    this.scene = new Scene(controller);
    setScene(this.scene);
  }
}
