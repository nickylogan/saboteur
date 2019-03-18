package gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import model.*;
import model.cards.Card;
import model.cards.PlayerActionCard;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GameWindowController extends StackPane implements Initializable {
  public class GUIGameObserver extends GameObserver {
    @Override
    public GameLogicController state() {
      return super.state();
    }

    @Override
    protected void onNextTurn(int player, ArrayList<Card> hand) {
      GameWindowController.this.handleNextTurn(player, hand);
    }

    @Override
    protected void onGameFinished(Player.Role role, int lastPlayer) {
      GameWindowController.this.handleEndGame(role, lastPlayer);
    }

    @Override
    protected void onGameStart() {
      GameWindowController.this.handleStartGame();
    }

    @Override
    protected void onPlayerMove(Move move, Card newCard) {
      System.out.println(move);
      GameWindowController.this.handleMove(move, newCard);
    }
  }

  @FXML
  private Button nextButton;
  @FXML
  private VBox logPane;
  @FXML
  private ScrollPane logScroll;
  @FXML
  private VBox playerPane;
  @FXML
  private Pane overlay;

  private ArrayList<PlayerPaneController> playerPanes;

  private GUIGameObserver observer = new GUIGameObserver();

  /** Reference to game logic controller */
  private GameLogicController game;
  /** Reference to game state */
  private GameState state;

  public GameWindowController(Player... players) throws GameException {
    this.state = new GameState();
    this.game = new GameLogicController(state, players);

    game.addObserver(this.observer);
    startBackgroundMusic();

    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layouts/GameWindow.fxml"));
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
    setPickOnBounds(false);
    logPane.prefWidthProperty().bind(logScroll.prefWidthProperty());
    nextButton.setOnMouseClicked(e -> nextPlayer());
    playerPanes = new ArrayList<>();
    for (Player p : state.players()) {
      PlayerPaneController pane = new PlayerPaneController(p);
      playerPane.getChildren().add(pane);
      playerPanes.add(pane);
    }
  }

  private void handleStartGame() {
    appendLog("Game started");
  }

  private void startBackgroundMusic() {
    Media media = new Media(getClass().getResource("../audio/background.wav").toExternalForm());
    MediaPlayer player = new MediaPlayer(media);
    player.setVolume(0.5f);
    player.setCycleCount(MediaPlayer.INDEFINITE);
    player.play();
  }

  private void playSound(String name) {
    Thread thread = new Thread(() -> {
      AudioClip clip = new AudioClip(getClass().getResource(String.format("../audio/%s.wav", name)).toExternalForm());
      clip.play(1f);
    });
    thread.start();
  }

  private void handleNextTurn(int player, ArrayList<Card> hand) {
    String fmt = "It is %s's turn";
    appendLog(String.format(fmt, state.playerAt(player).name()));
  }

  private void handleMove(Move move, Card newCard) {
    Move.Type type = move.type();
    switch (type) {
      case DISCARD:
        handleDiscard(move, newCard);
        break;
      case PLAY_PATH:
        handlePath(move, newCard);
        break;
      case PLAY_MAP:
        handleMap(move, newCard);
        break;
      case PLAY_ROCKFALL:
        handleRockfall(move, newCard);
        break;
      case PLAY_PLAYER:
        handlePlayerAction(move, newCard);
        break;
    }

  }

  private void handleDiscard(Move move, Card newCard) {
    String logFormat = "%s discards a card";
    String moveLog = String.format(
      logFormat,
      state.playerAt(move.playerIndex()).name()
    );
    appendLog(moveLog);
    playSound("discard");
  }

  private void handlePath(Move move, Card newCard) {
    playSound("card");
    String logFormat = "%s digs a path at (%d, %d)";
    String moveLog = String.format(
      logFormat,
      state.playerAt(move.playerIndex()).name(),
      move.args()[0], move.args()[1]
    );
    appendLog(moveLog);
    playSound("path");
  }

  private void handleMap(Move move, Card newCard) {
    playSound("card");
    String logFormat = "%s opens the %s goal card";
    String moveLog = String.format(
      logFormat,
      state.playerAt(move.playerIndex()).name(),
      Board.GoalPosition.values()[move.args()[0]].name().toLowerCase()
    );
    appendLog(moveLog);
    playSound("map");
  }

  private void handleRockfall(Move move, Card newCard) {
    playSound("card");
    String logFormat = "%s destroys the path at (%d, %d)";
    String moveLog = String.format(
      logFormat,
      state.playerAt(move.playerIndex()).name(),
      move.args()[0], move.args()[1]
    );
    appendLog(moveLog);
    playSound("rockfall");
  }

  private void handlePlayerAction(Move move, Card newCard) {
    playSound("card");
    PlayerActionCard card = (PlayerActionCard) move.card();
    PlayerActionCard.Type type = card.playerActionType();
    String moveLog = "";
    if (card.type() == Card.Type.BLOCK) {
      playerPanes.get(move.args()[0]).blockTools(card.effects());
    } else if (card.type() == Card.Type.REPAIR) {
      playerPanes.get(move.args()[0]).repairTools(card.effects());
    }
    switch (type) {
      case BLOCK_CART:
        moveLog = String.format(
          "%s breaks %s's cart",
          state.playerAt(move.playerIndex()).name(),
          state.playerAt(move.args()[0]).name()
        );
        playSound("block_cart");
        break;
      case BLOCK_LANTERN:
        moveLog = String.format(
          "%s breaks %s's lantern",
          state.playerAt(move.playerIndex()).name(),
          state.playerAt(move.args()[0]).name()
        );
        playSound("block_lantern");
        break;
      case BLOCK_PICKAXE:
        moveLog = String.format(
          "%s destroys %s's pickaxe",
          state.playerAt(move.playerIndex()).name(),
          state.playerAt(move.args()[0]).name()
        );
        playSound("block_pick");
        break;
      case REPAIR_CART:
        moveLog = String.format(
          "%s repairs %s's cart",
          state.playerAt(move.playerIndex()).name(),
          state.playerAt(move.args()[0]).name()
        );
        playSound("repair_cart");
        break;
      case REPAIR_LANTERN:
        moveLog = String.format(
          "%s repairs %s's lantern",
          state.playerAt(move.playerIndex()).name(),
          state.playerAt(move.args()[0]).name()
        );
        playSound("repair_lantern");
        break;
      case REPAIR_PICKAXE:
        moveLog = String.format(
          "%s repairs %s's pickaxe",
          state.playerAt(move.playerIndex()).name(),
          state.playerAt(move.args()[0]).name()
        );
        playSound("repair_pick");
        break;
      case REPAIR_LANTERN_PICKAXE:
        moveLog = String.format(
          "%s repairs %s's lantern and pickaxe",
          state.playerAt(move.playerIndex()).name(),
          state.playerAt(move.args()[0]).name()
        );
        playSound("repair_lantern");
        playSound("repair_pick");
        break;
      case REPAIR_CART_PICKAXE:
        moveLog = String.format(
          "%s repairs %s's cart and pickaxe",
          state.playerAt(move.playerIndex()).name(),
          state.playerAt(move.args()[0]).name()
        );
        playSound("repair_cart");
        playSound("repair_pick");
        break;
      case REPAIR_CART_LANTERN:
        moveLog = String.format(
          "%s repairs %s's cart and lantern",
          state.playerAt(move.playerIndex()).name(),
          state.playerAt(move.args()[0]).name()
        );
        playSound("repair_cart");
        playSound("repair_lantern");
        break;
    }
    appendLog(moveLog);
  }

  private void handleEndGame(Player.Role role, int lastPlayer) {
    playSound("win");
    appendLog("The " + role.name().toLowerCase() + "s won the game");
  }

  private void animateNode(Node node, int targetX, int targetY, double scale) {
    overlay.getChildren().add(node);
    TranslateTransition transition = new TranslateTransition();
    transition.setByX(targetX);
    transition.setByY(targetY);
    transition.setDuration(Duration.millis(1000));
    transition.setNode();
  }

  private void appendLog(String log) {
    Label label = new Label(log);
    label.setTextFill(Color.WHITE);
    label.setFont(Font.font("Consolas", 12));
    label.setWrapText(true);
    label.setPrefWidth(USE_COMPUTED_SIZE);
    label.setMaxWidth(logPane.getWidth());
    logPane.getChildren().add(label);
    logScroll.setVvalue(logScroll.getVmax());
    System.out.println(log);
  }

  private void nextPlayer() {
    if (!game.started()) {
      try {
        game.initializeRound();
        game.startRound();
        nextButton.setText("NEXT TURN");
      } catch (GameException ignored) {}
    } else {
      game.finalizeTurn();
    }
  }
}
