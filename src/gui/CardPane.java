package gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.cards.Card;

import java.util.ArrayList;

public class CardPane extends ImageView {
  private static final ArrayList<String> imagePaths = new ArrayList<>();
  public static double HEIGHT = 140-32;
  public static double WIDTH = 120.0 / 195 * (140 - 32);

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
    String pathFmt = "../img/cards/%s.png";
    String path = String.format(pathFmt, imagePaths.get(card.id() - 1));
    setImage(new Image(getClass().getResource(path).toExternalForm()));
    setFitHeight(HEIGHT);
    setFitWidth(WIDTH);
  }

  public CardPane(String name) {
    String pathFmt = "../img/cards/%s.png";
    String path = String.format(pathFmt, name);
    setImage(new Image(getClass().getResource(path).toExternalForm()));
    setFitHeight(HEIGHT);
    setFitWidth(WIDTH);
  }

  Card card() { return this.card; }
}
