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
import model.cards.Card;
import model.cards.PlayerActionCard;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class PlayerSidebar extends ScrollPane implements Initializable {
  @FXML
  private ScrollPane scroll;
  @FXML
  private VBox container;

  private GameGUIController controller;

  private ArrayList<Player> players;
  private ArrayList<PlayerStatusPane> playerStatusPanes;

  private int targeted;
  private int current;
  private List<Integer> available;

  private PlayerActionCard selected;

  public PlayerSidebar(GameGUIController controller, Player... players) {
    this.controller = controller;
    this.players = new ArrayList<>();
    this.players.addAll(Arrays.asList(players));

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/player-sidebar.fxml"));
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
    for (int i = 0; i < players.size(); ++i) {
      int index = i;
      Player p = players.get(i);
      PlayerStatusPane pane = new PlayerStatusPane(p);
      playerStatusPanes.add(pane);
      pane.setOnMouseClicked(e -> this.handleClick(index));
    }
    container.getChildren().addAll(playerStatusPanes);

    DropShadow shadow = new DropShadow();
    shadow.setBlurType(BlurType.GAUSSIAN);
    shadow.setColor(new Color(0, 0, 0, 0.5));
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

  /**
   * Highlights the specified player index as the current one
   *
   * @param playerIndex the current player index
   */
  void setCurrent(int playerIndex) {
    clearCurrent();
    playerStatusPanes.get(playerIndex).setCurrent(true);
    current = playerIndex;
  }

  /**
   * Clears the current player highlight
   */
  private void clearCurrent() {
    if (current != -1) {
      playerStatusPanes.get(current).setCurrent(false);
      current = -1;
    }
  }

  /**
   * Highlights the specified target player
   *
   * @param playerIndex the targeted player index
   */
  void targetPlayer(int playerIndex) {
    clearTarget();
    playerStatusPanes.get(playerIndex).setTargeted(true);
    targeted = playerIndex;
  }

  /**
   * Clears targeted player highlight
   */
  void clearTarget() {
    if (targeted != -1) {
      playerStatusPanes.get(targeted).setTargeted(false);
      targeted = -1;
    }
  }

  /**
   * Highlights the available players to be played on, based on the specified card
   *
   * @param card the player-action card to be played
   */
  void highlightAvailable(PlayerActionCard card) {
    selected = card;
    available = new ArrayList<>();
    for (int i = 0; i < players.size(); ++i) {
      if (card.type() == Card.Type.BLOCK && players.get(i).isSabotageable(card.effects()[0])) {
        if (i == controller.game().currentPlayerIndex()) continue;
        available.add(i);
      } else if (card.type() == Card.Type.REPAIR && players.get(i).isRepairable(card.effects())) {
        available.add(i);
      }
    }
    available.forEach(i -> playerStatusPanes.get(i).setAvailable(true));
  }

  /**
   * Resets all valid target highlights
   */
  void resetAvailable() {
    if (available != null) {
      for (Integer i : available) {
        playerStatusPanes.get(i).setAvailable(false);
      }
      available = null;
    }
  }

  private void handleClick(int index) {
    if (!playerStatusPanes.get(index).isAvailable()) {
      GameGUIController.playSound("error");
      return;
    }
    controller.applyManualPlayerActionMove(index);
  }

  /**
   * Returns the bounds of the specified playerPane index
   *
   * @param index the player index
   * @return the player pane's bounds
   */
  Bounds getPlayerPaneBounds(int index) {
    PlayerStatusPane p = playerStatusPanes.get(index);
    return p.localToScreen(p.getBoundsInLocal());
  }
}
