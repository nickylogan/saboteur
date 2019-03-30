package gui;

import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import model.Player;
import model.Tool;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PlayerInfoPane extends AnchorPane implements Initializable {
  private GameGUIController controller;

  @FXML
  ImageView roleCard;
  @FXML
  private ImageView pickIcon;
  @FXML
  private ImageView lanternIcon;
  @FXML
  private ImageView cartIcon;


  public PlayerInfoPane(GameGUIController controller) {
    this.controller = controller;

    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layouts/player-info.fxml"));
    loader.setRoot(this);
    loader.setController(this);
    try {
      loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void change(Player.Role role, Tool[] sabotaged) {
    TranslateTransition t1 = new TranslateTransition();
    t1.setByY(-156);
    t1.setDuration(Duration.millis(250));
    t1.setInterpolator(Interpolator.EASE_BOTH);
    t1.setNode(this);

    TranslateTransition t2 = new TranslateTransition();
    t2.setByY(156);
    t2.setDuration(Duration.millis(250));
    t2.setInterpolator(Interpolator.EASE_BOTH);
    t2.setNode(this);

    t1.setOnFinished(e -> {
      render(role, sabotaged);
      t2.play();
    });
    t1.play();
  }

  private void render(Player.Role role, Tool[] sabotaged) {
    if (role == Player.Role.GOLD_MINER) {
      roleCard.setImage(new Image(getClass().getResource("../img/cards/miner.png").toExternalForm()));
    } else {
      roleCard.setImage(new Image(getClass().getResource("../img/cards/saboteur.png").toExternalForm()));
    }
    pickIcon.setImage(new Image(getClass().getResource("../img/icons/pick_intact.png").toExternalForm()));
    lanternIcon.setImage(new Image(getClass().getResource("../img/icons/lantern_intact.png").toExternalForm()));
    cartIcon.setImage(new Image(getClass().getResource("../img/icons/cart_intact.png").toExternalForm()));
    for (Tool tool : sabotaged) {
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
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AnchorPane.setTopAnchor(this, 16.0);
    AnchorPane.setRightAnchor(this, 332.0);
  }
}
