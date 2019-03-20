package gui;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import model.Player;
import model.Tool;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PlayerStatusPane extends AnchorPane implements Initializable {
  @FXML
  private Label name;
  @FXML
  private ImageView pickIcon;
  @FXML
  private ImageView lanternIcon;
  @FXML
  private ImageView cartIcon;

  private Player player;

  static final String TARGETED_CLASS = "targeted";
  static final String CURRENT_CLASS = "current";

  PlayerStatusPane(Player player) {
    this.player = player;

    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layouts/player-status-pane.fxml"));
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
    name.setText(player.name());
  }

  private void animateTools(Tool... tools) {
    for (Tool tool : tools) {
      ScaleTransition s = new ScaleTransition();
      s.setFromX(2.0);
      s.setFromY(2.0);
      s.setToX(1.0);
      s.setToY(1.0);
      s.setDuration(Duration.millis(500));
      s.setInterpolator(Interpolator.EASE_OUT);
      switch (tool) {
        case PICKAXE:
          s.setNode(pickIcon);
          break;
        case LANTERN:
          s.setNode(lanternIcon);
          break;
        case CART:
          s.setNode(cartIcon);
          break;
      }
      s.play();
    }
  }

  /**
   * Applies GUI changes for block move
   *
   * @param tools the blocked tools
   */
  void blockTools(Tool... tools) {
    for (Tool tool : tools) {
      switch (tool) {
        case PICKAXE:
          pickIcon.setImage(new Image(getClass().getResource("../img/icons/pick_blocked.png").toExternalForm()));
          break;
        case LANTERN:
          lanternIcon.setImage(new Image(getClass().getResource("../img/icons/lantern_blocked.png").toExternalForm()));
          break;
        case CART:
          cartIcon.setImage(new Image(getClass().getResource("../img/icons/cart_blocked.png").toExternalForm()));
          break;
      }
    }
    animateTools(tools);
  }

  /**
   * Applies GUI changes for repair move
   *
   * @param tools the repaired tools
   */
  void repairTools(Tool... tools) {
    for (Tool tool : tools) {
      switch (tool) {
        case PICKAXE:
          pickIcon.setImage(new Image(getClass().getResource("../img/icons/pick_intact.png").toExternalForm()));
          break;
        case LANTERN:
          lanternIcon.setImage(new Image(getClass().getResource("../img/icons/lantern_intact.png").toExternalForm()));
          break;
        case CART:
          cartIcon.setImage(new Image(getClass().getResource("../img/icons/cart_intact.png").toExternalForm()));
          break;
      }
    }
    animateTools(tools);
  }
}
