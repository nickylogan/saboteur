package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Player;
import model.Tool;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PlayerSidebar extends ScrollPane implements Initializable {
  @FXML
  private ScrollPane scroll;
  @FXML
  private VBox container;

  private GameGUIController controller;

  private ArrayList<Player> players;
  private ArrayList<PlayerStatusPane> playerStatusPanes;

  public PlayerSidebar(GameGUIController controller, Player... players) {
    this.controller = controller;
    this.players = new ArrayList<>();
    this.players.addAll(Arrays.asList(players));

    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layouts/player-sidebar.fxml"));
    loader.setRoot(this);
    loader.setController(this);
    try {
      loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AnchorPane.setTopAnchor(this, 16.0);
    AnchorPane.setRightAnchor(this, 16.0);
    AnchorPane.setBottomAnchor(this, 132.0);

    // Initialize dimensions
    setMaxHeight(Double.MAX_VALUE);
    setPrefWidth(300);
    container.prefWidthProperty().bind(scroll.prefViewportWidthProperty());

    // Populate children
    playerStatusPanes = new ArrayList<>();
    players.forEach(p -> playerStatusPanes.add(new PlayerStatusPane(p)));
    container.getChildren().addAll(playerStatusPanes);

    DropShadow shadow = new DropShadow();
    shadow.setBlurType(BlurType.GAUSSIAN);
    shadow.setColor(new Color(0,0,0,0.5));
    shadow.setHeight(72);
    shadow.setWidth(72);
    shadow.setRadius(72);
    shadow.setSpread(0);

    setEffect(shadow);
  }

  void repairTools(int playerIndex, Tool... tools) {
    playerStatusPanes.get(playerIndex).repairTools(tools);
  }

  void blockTools(int playerIndex, Tool... tools) {
    playerStatusPanes.get(playerIndex).blockTools(tools);
  }

  void setCurrent(int playerIndex) {
    clearCurrent();
    playerStatusPanes.get(playerIndex).getStyleClass().add(PlayerStatusPane.CURRENT_CLASS);
  }

  private void clearCurrent() {
    playerStatusPanes.forEach(p -> p.getStyleClass().remove(PlayerStatusPane.CURRENT_CLASS));
  }

  void targetPlayer(int playerIndex) {
    clearTarget();
    playerStatusPanes.get(playerIndex).getStyleClass().add(PlayerStatusPane.TARGETED_CLASS);
  }

  void clearTarget() {
    playerStatusPanes.forEach(p -> p.getStyleClass().remove(PlayerStatusPane.TARGETED_CLASS));
  }

  Bounds getPlayerPaneBounds(int index) {
    PlayerStatusPane p = playerStatusPanes.get(index);
    return p.localToScreen(p.getBoundsInLocal());
  }
}
