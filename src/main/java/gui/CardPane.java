package gui;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import model.cards.Card;

import java.util.ArrayList;

public class CardPane extends ImageView {
  private static final ArrayList<String> imagePaths = new ArrayList<>();
  public static double HEIGHT = 140 - 32;
  public static double WIDTH = 120.0 / 195 * (140 - 32);

  private boolean selected = false;
  private boolean rotated = false;

  static {
    imagePaths.add("path_crossroad_1");
    imagePaths.add("path_crossroad_2");
    imagePaths.add("path_crossroad_3");
    imagePaths.add("path_crossroad_4");
    imagePaths.add("path_crossroad_5");
    imagePaths.add("path_horizontal_t_1");
    imagePaths.add("path_horizontal_t_2");
    imagePaths.add("path_horizontal_t_3");
    imagePaths.add("path_horizontal_t_4");
    imagePaths.add("path_horizontal_t_5");
    imagePaths.add("path_vertical_t_1");
    imagePaths.add("path_vertical_t_2");
    imagePaths.add("path_vertical_t_3");
    imagePaths.add("path_vertical_t_4");
    imagePaths.add("path_vertical_t_5");
    imagePaths.add("path_horizontal_1");
    imagePaths.add("path_horizontal_2");
    imagePaths.add("path_horizontal_3");
    imagePaths.add("path_vertical_1");
    imagePaths.add("path_vertical_2");
    imagePaths.add("path_vertical_3");
    imagePaths.add("path_vertical_4");
    imagePaths.add("path_left_1");
    imagePaths.add("path_left_2");
    imagePaths.add("path_left_3");
    imagePaths.add("path_left_4");
    imagePaths.add("path_left_5");
    imagePaths.add("path_right_1");
    imagePaths.add("path_right_2");
    imagePaths.add("path_right_3");
    imagePaths.add("path_right_4");
    imagePaths.add("deadend_crossroad");
    imagePaths.add("deadend_horizontal_t");
    imagePaths.add("deadend_vertical_t");
    imagePaths.add("deadend_both_horizontal");
    imagePaths.add("deadend_both_vertical");
    imagePaths.add("deadend_left");
    imagePaths.add("deadend_right");
    imagePaths.add("deadend_single_horizontal");
    imagePaths.add("deadend_single_vertical");
    imagePaths.add("map");
    imagePaths.add("map");
    imagePaths.add("map");
    imagePaths.add("map");
    imagePaths.add("map");
    imagePaths.add("map");
    imagePaths.add("rockfall");
    imagePaths.add("rockfall");
    imagePaths.add("rockfall");
    imagePaths.add("repair_cart");
    imagePaths.add("repair_cart");
    imagePaths.add("repair_lantern");
    imagePaths.add("repair_lantern");
    imagePaths.add("repair_pickaxe");
    imagePaths.add("repair_pickaxe");
    imagePaths.add("repair_cart_lantern");
    imagePaths.add("repair_cart_pickaxe");
    imagePaths.add("repair_lantern_pickaxe");
    imagePaths.add("block_cart");
    imagePaths.add("block_cart");
    imagePaths.add("block_cart");
    imagePaths.add("block_lantern");
    imagePaths.add("block_lantern");
    imagePaths.add("block_lantern");
    imagePaths.add("block_pickaxe");
    imagePaths.add("block_pickaxe");
    imagePaths.add("block_pickaxe");
  }

  private Card card;

  public CardPane(Card card) {
    this.card = card;
    String pathFmt = "/img/cards/%s.png";
    String path = String.format(pathFmt, imagePaths.get(card.id() - 1));
    setImage(new Image(getClass().getResource(path).toExternalForm()));
    setFitHeight(HEIGHT);
    setFitWidth(WIDTH);
  }

  public CardPane(String name) {
    String pathFmt = "/img/cards/%s.png";
    String path = String.format(pathFmt, name);
    setImage(new Image(getClass().getResource(path).toExternalForm()));
    setFitHeight(HEIGHT);
    setFitWidth(WIDTH);
  }

  Card card() { return this.card; }

  void setSelected(boolean selected) {
    if (selected != this.selected) {
      TranslateTransition t = new TranslateTransition();
      t.setByY(selected ? -15 : 15);
      t.setDuration(Duration.millis(250));
      t.setInterpolator(Interpolator.EASE_IN);
      t.setNode(this);
      t.play();
    }
    this.selected = selected;
  }

  void rotate() {
    RotateTransition r1 = new RotateTransition();
    r1.setToAngle(rotated ? 10: 190);
    r1.setDuration(Duration.millis(125));
    r1.setInterpolator(Interpolator.EASE_OUT);

    RotateTransition r2 = new RotateTransition();
    r2.setToAngle(rotated ? 0: 180);
    r2.setDuration(Duration.millis(50));
    r2.setInterpolator(Interpolator.EASE_BOTH);

    SequentialTransition s = new SequentialTransition(r1, r2);
    s.setNode(this);
    s.play();
    rotated = !rotated;
  }
}
