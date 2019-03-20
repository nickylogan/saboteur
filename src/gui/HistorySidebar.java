package gui;

import javafx.animation.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import model.Board;
import model.Move;
import model.Tool;
import model.cards.PlayerActionCard;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * The {@link HistorySidebar} class is a panel that shows the game log
 */
public class HistorySidebar extends GridPane implements Initializable {
  /** Message type primary */
  static final int TYPE_PRIMARY = 1;
  /** Message type secondary */
  static final int TYPE_SECONDARY = 2;
  /** Message type success */
  static final int TYPE_SUCCESS = 3;
  /** Message type danger */
  static final int TYPE_DANGER = 4;
  /** Message type warning */
  static final int TYPE_WARNING = 5;
  /** Message type primary */
  static final int TYPE_INFO = 6;

  @FXML
  private Button collapseButton;
  @FXML
  private ImageView collapseIcon;
  @FXML
  private ScrollPane scroll;
  @FXML
  private VBox pane;

  private GameGUIController controller;

  private SimpleBooleanProperty collapsed;

  public HistorySidebar(GameGUIController controller) {
    this.controller = controller;

    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layouts/history-sidebar.fxml"));
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
    AnchorPane.setBottomAnchor(this, 16.0);
    AnchorPane.setLeftAnchor(this, 16.0);

    pane.prefWidthProperty().bind(scroll.widthProperty());

    collapsed = new SimpleBooleanProperty(false);
    collapseButton.setOnMouseClicked(e -> toggle());

    DropShadow shadow = new DropShadow();
    shadow.setBlurType(BlurType.GAUSSIAN);
    shadow.setColor(new Color(0,0,0,0.5));
    shadow.setHeight(72);
    shadow.setWidth(72);
    shadow.setRadius(72);
    shadow.setSpread(0);

    setEffect(shadow);
    setPickOnBounds(false);
  }

  /**
   * Appends a move message to the log
   *
   * @param move the move to be appended
   */
  void appendMove(Move move) {
    Move.Type type = move.type();
    String name = controller.game().playerAt(move.playerIndex()).name();
    String message = "";
    switch (type) {
      case DISCARD:
        message = generateDiscardMessage(name);
        break;
      case PLAY_PATH:
        message = generatePathMessage(name, move.args()[0], move.args()[1]);
        break;
      case PLAY_ROCKFALL:
        message = generateRockfallMessage(name, move.args()[0], move.args()[1]);
        break;
      case PLAY_MAP:
        message = generateMapMessage(name, Board.GoalPosition.values()[move.args()[0]]);
        break;
      case PLAY_PLAYER:
        String target = controller.game().playerAt(move.args()[0]).name();
        message = generatePlayerActionMessage(name, (PlayerActionCard) move.card(), target);
        break;
    }
    appendMessage(message, TYPE_PRIMARY, true);
  }

  private String generateDiscardMessage(String name) {
    String fmt = "%s discards a card";
    return String.format(fmt, name);
  }

  private String generatePathMessage(String name, int x, int y) {
    String fmt = "%s digs a path at (%d, %d)";
    return String.format(fmt, name, x, y);
  }

  private String generateRockfallMessage(String name, int x, int y) {
    String fmt = "%s destroys the path at (%d, %d)";
    return String.format(fmt, name, x, y);
  }

  private String generateMapMessage(String name, Board.GoalPosition pos) {
    String fmt = "%s plays a map card on the %s goal";
    return String.format(fmt, name, pos.toString().toLowerCase());
  }

  private String generatePlayerActionMessage(String name, PlayerActionCard card, String target) {
    String fmt = "%s %s %s's %s";
    String type = card.type().toString().toLowerCase() + "s";
    Tool[] tools = card.effects();
    String toolSentence = "";
    if (tools.length == 1) {
      toolSentence = tools[0].name().toLowerCase();
    } else if (tools.length == 2) {
      toolSentence = tools[0].name().toLowerCase() + " and " + tools[1].name().toLowerCase();
    }
    return String.format(fmt, name, type, target, toolSentence);
  }

  /**
   * Appends a message string to the log
   *
   * @param message     the message to be logged
   * @param messageType the message type, can be of either
   *                    {@link HistorySidebar#TYPE_PRIMARY},
   *                    {@link HistorySidebar#TYPE_SECONDARY},
   *                    {@link HistorySidebar#TYPE_SUCCESS},
   *                    {@link HistorySidebar#TYPE_DANGER},
   *                    {@link HistorySidebar#TYPE_WARNING}, or
   *                    {@link HistorySidebar#TYPE_INFO}.
   * @param bold        marks the message as bold
   */
  void appendMessage(String message, int messageType, boolean bold) {
    Label label = new Label(message);
    label.getStyleClass().add("log-text");
    switch (messageType) {
      case TYPE_PRIMARY:
        label.getStyleClass().add("primary");
        break;
      case TYPE_SECONDARY:
        label.getStyleClass().add("secondary");
        break;
      case TYPE_SUCCESS:
        label.getStyleClass().add("success");
        break;
      case TYPE_DANGER:
        label.getStyleClass().add("danger");
        break;
      case TYPE_WARNING:
        label.getStyleClass().add("warning");
        break;
      case TYPE_INFO:
        label.getStyleClass().add("info");
        break;
    }
    if (bold) label.getStyleClass().add("bold");
    label.setMaxWidth(Double.MAX_VALUE);
    label.setWrapText(true);
    pane.getChildren().add(label);

    scroll.setVvalue(1.0);
  }

  private void toggle() {
    ParallelTransition expand = new ParallelTransition();

    // Translate position
    TranslateTransition t = new TranslateTransition();
    t.setByX(collapsed.get() ? 316 : -316);
    t.setInterpolator(Interpolator.EASE_BOTH);
    t.setDuration(Duration.millis(500));
    t.setNode(this);
    // Rotate icon
    RotateTransition r = new RotateTransition();
    r.setAxis(new Point3D(0, 1, 0));
    r.setByAngle(180);
    r.setDuration(Duration.millis(250));
    r.setNode(collapseIcon);

    expand.getChildren().addAll(t, r);
    expand.play();

    collapsed.set(!collapsed.get());
  }

}
