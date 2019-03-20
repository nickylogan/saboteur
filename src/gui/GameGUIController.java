package gui;

import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.*;
import model.cards.Card;
import model.cards.PlayerActionCard;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The {@link GameGUIController} class controls the main game GUI
 */
public class GameGUIController extends Stage {
  /**
   * The {@link GUIGameObserver} class allows GameGUIController to
   * observe the {@link GameGUIController#game}
   */
  public class GUIGameObserver extends GameObserver {
    @Override
    protected void onGoalOpen(Board.GoalPosition position, GoalType goalType, boolean permanent) {
      if (permanent) GameGUIController.this.handleGoalOpen(position);
    }

    @Override
    public GameLogicController game() {
      return super.game();
    }

    @Override
    protected void onNextTurn(int player, ArrayList<Card> hand) {
      GameGUIController.this.handleNextTurn(player, hand);
    }

    @Override
    protected void onGameFinished(Player.Role role, int lastPlayer) {
      GameGUIController.this.handleEndGame(role, lastPlayer);
    }

    @Override
    protected void onGameStart() {
      GameGUIController.this.handleStartGame();
    }

    @Override
    protected void onPlayerMove(Move move, Card newCard) {
      GameGUIController.this.handleMove(move, newCard);
    }
  }

  //=========================
  // GUI components
  //=========================
  /** A reference to the current scene */
  private Scene scene;
  /** GUI layers */
  private AnchorPane layers;
  /** The move history sidebar panel */
  private HistorySidebar log;
  /** The player info sidebar panel */
  private PlayerSidebar playerSidebar;
  /** The player hand panel */
  private PlayerCardsPane playerCardsPane;
  /** The card animation overlay */
  private CardAnimationOverlay animationOverlay;
  /** The next move button */
  private NextButton nextButton;
  /** The board pane */
  BoardPane boardPane;
  /** The background music */
  private static MediaPlayer player;

  //=========================
  // Game logic components
  //=========================
  /** Stores a reference to game logic controller */
  private GameLogicController game;
  /** Stores a reference to game state */
  private GameState state;
  /** Allows the window to be an observer of the game */
  private GUIGameObserver observer;
  /** Stores a reference to the last played move */
  private Move lastMove;
  /** Stores a reference to the last taken card from the deck */
  private Card lastCard;
  /** Stores a reference to the last player hand */
  private ArrayList<Card> lastHand;

  /**
   * Creates a game window
   *
   * @param players the players joining the game
   */
  public static GameGUIController NewGameSession(Player... players) throws GameException {
    GameGUIController window = new GameGUIController(players);
    window.setFullScreen(true);
    window.setResizable(false);
    window.show();

    window.boardPane.focusAt(0, 2);

    return window;
  }

  /**
   * Creates a game window. Use {@link GameGUIController#NewGameSession(Player...)}
   * factory method instead.
   *
   * @param players the players joining the game
   * @see GameGUIController#NewGameSession(Player...)
   */
  private GameGUIController(Player... players) throws GameException {
    // Initialize fonts
    Font.loadFont(getClass().getResourceAsStream("../fonts/nevis.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("../fonts/WorkSans-Black.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("../fonts/WorkSans-Bold.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("../fonts/WorkSans-ExtraBold.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("../fonts/WorkSans-ExtraLight.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("../fonts/WorkSans-Light.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("../fonts/WorkSans-Medium.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("../fonts/WorkSans-Regular.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("../fonts/WorkSans-SemiBold.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("../fonts/WorkSans-Thin.ttf"), 120);

    // Initialize game state and logic
    this.state = new GameState();
    this.game = new GameLogicController(state, players);
    this.observer = new GUIGameObserver();
    // Add the instance's observer to the game logic
    this.game.addObserver(this.observer);

    // Initialize root GUI
    layers = new AnchorPane();
    layers.setPrefWidth(1280);
    layers.setPrefWidth(720);
    this.scene = new Scene(layers);
    setScene(this.scene);
    setFullScreen(true);
    setResizable(false);

    // Initialize GUI elements
    layers.getChildren().add(new BaseLayer());
    boardPane = new BoardPane(this);
    boardPane.initialize();
    playerCardsPane = new PlayerCardsPane(this);
    playerSidebar = new PlayerSidebar(this, players);
    animationOverlay = new CardAnimationOverlay(this);
    log = new HistorySidebar(this);
    nextButton = new NextButton();
    nextButton.setOnMouseClicked(e -> nextPlayer());
    layers.getChildren().addAll(boardPane, playerCardsPane, playerSidebar, animationOverlay, log, nextButton);

    // Start audio
    startBackgroundMusic();
  }

  /**
   * Commits the played move to the GUI
   */
  private void applyMoveToGUI() {
    // Delegates move to correct handler
    Move move = lastMove;
    Card newCard = lastCard;
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

    // Appends the played move to the log
    log.appendMove(move);
  }

  /**
   * Handles the game start event
   */
  private void handleStartGame() {
    log.appendMessage("GAME STARTED", HistorySidebar.TYPE_SUCCESS, true);
    // TODO: add popup dialog and sound effect
  }

  /**
   * Handles the next turn event
   *
   * @param player the new player index
   * @param hand   the respective player's card
   * @see GUIGameObserver#onNextTurn(int, ArrayList)
   */
  private void handleNextTurn(int player, ArrayList<Card> hand) {
    String fmt = "It is %s's turn";
    String msg = String.format(fmt, state.playerAt(player).name());
    log.appendMessage(msg, HistorySidebar.TYPE_INFO, false);

    // TODO: add alert
    // TODO: change callable to play shuffle
    playerSidebar.setCurrent(player);
    playerSidebar.clearTarget();
    playerCardsPane.playChangeHandAnimation(state.playerAt(player).name(), hand, null, e -> applyMoveToGUI());
  }

  /**
   * Handles the next move event
   *
   * @param move    the played move
   * @param newCard the card taken from the board
   * @see GUIGameObserver#onPlayerMove(Move, Card)
   */
  private void handleMove(Move move, Card newCard) {
    nextButton.setDisable(true);
    System.out.println(move);
    this.lastMove = move;
    this.lastCard = newCard;
  }

  /**
   * Handles the discard event
   *
   * @param move    the played discard move
   * @param newCard the card taken from the deck
   */
  private void handleDiscard(Move move, Card newCard) {
    playSound("discard");
    playerCardsPane.playDiscardAnimation(move.handIndex(), newCard, null,
      e -> nextButton.setDisable(false));
  }

  private void handlePath(Move move, Card newCard) {
    playSound("card");

    CardPane original = playerCardsPane.getCard(move.handIndex());
    CardPane pane = new CardPane(move.card());
    Bounds cardBounds = original.localToScreen(original.getBoundsInLocal());
    pane.setLayoutX(cardBounds.getMinX());
    pane.setLayoutY(cardBounds.getMinY());
    double deltaX = getWidth() / 2.0 - CardPane.WIDTH / 2.0 - cardBounds.getMinX();
    double deltaY = getHeight() / 2.0 - CardPane.HEIGHT / 2.0 - cardBounds.getMinY();

    animationOverlay.playCardMoveAnimation(
      pane, deltaX, deltaY, move.args()[2] == 1,
      () -> {
        playerCardsPane.playReplaceAnimation(move.handIndex(), newCard, null, null);
        boardPane.focusAt(move.args()[0], move.args()[1]);
        boardPane.placeCard(move.card(), move.args()[0], move.args()[1], move.args()[2] == 1);
        return null;
      },
      e -> {
        playSound("path");
        nextButton.setDisable(false);
      }
    );
  }

  private void handleMap(Move move, Card newCard) {
    playSound("card");

    CardPane original = playerCardsPane.getCard(move.handIndex());
    CardPane pane = new CardPane(move.card());
    Bounds cardBounds = original.localToScreen(original.getBoundsInLocal());
    pane.setLayoutX(cardBounds.getMinX());
    pane.setLayoutY(cardBounds.getMinY());
    double deltaX = getWidth() / 2.0 - CardPane.WIDTH / 2.0 - cardBounds.getMinX();
    double deltaY = getHeight() / 2.0 - CardPane.HEIGHT / 2.0 - cardBounds.getMinY();

    Board.GoalPosition pos = Board.GoalPosition.values()[move.args()[0]];

    animationOverlay.playCardMoveAnimation(
      pane, deltaX, deltaY,
      () -> {
        playerCardsPane.playReplaceAnimation(move.handIndex(), newCard, null, null);
        switch (pos) {
          case TOP:
            boardPane.focusAt(8, 0);
            break;
          case MIDDLE:
            boardPane.focusAt(8, 2);
            break;
          case BOTTOM:
            boardPane.focusAt(8, 4);
            break;
        }
        return null;
      },
      e -> {
        nextButton.setDisable(false);
        GoalType type = boardPane.peekGoalCard(pos);
        playSound("map");
        if (type == GoalType.GOLD) playSound("gold");
      }
    );
  }

  private void handleRockfall(Move move, Card newCard) {
    playSound("card");

    CardPane original = playerCardsPane.getCard(move.handIndex());
    CardPane pane = new CardPane(move.card());
    Bounds cardBounds = original.localToScreen(original.getBoundsInLocal());
    pane.setLayoutX(cardBounds.getMinX());
    pane.setLayoutY(cardBounds.getMinY());
    double deltaX = getWidth() / 2.0 - CardPane.WIDTH / 2.0 - cardBounds.getMinX();
    double deltaY = getHeight() / 2.0 - CardPane.HEIGHT / 2.0 - cardBounds.getMinY();

    animationOverlay.playCardMoveAnimation(
      pane, deltaX, deltaY,
      () -> {
        playerCardsPane.playReplaceAnimation(move.handIndex(), newCard, null, null);
        boardPane.focusAt(move.args()[0], move.args()[1]);
        boardPane.destroyCard(move.args()[0], move.args()[1]);
        return null;
      },
      e -> {
        nextButton.setDisable(false);
        playSound("rockfall");
      }
    );
  }

  /**
   * Handles player action move
   *
   * @param move    the played player action
   * @param newCard the card taken from the deck
   */
  private void handlePlayerAction(Move move, Card newCard) {
    playSound("card");
    PlayerActionCard actionCard = (PlayerActionCard) move.card();
    Card.Type type = actionCard.type();
    int targetPlayer = move.args()[0];
    Bounds targetBounds = playerSidebar.getPlayerPaneBounds(targetPlayer);

    CardPane original = playerCardsPane.getCard(move.handIndex());
    CardPane pane = new CardPane(actionCard);
    Bounds cardBounds = original.localToScreen(original.getBoundsInLocal());
    pane.setLayoutX(cardBounds.getMinX());
    pane.setLayoutY(cardBounds.getMinY());
    double deltaX = targetBounds.getMinX() - cardBounds.getMinX();
    double deltaY = targetBounds.getMinY() - cardBounds.getMinY();

    animationOverlay.playCardMoveAnimation(
      pane, deltaX, deltaY,
      () -> {
        playerSidebar.targetPlayer(targetPlayer);
        playerCardsPane.playReplaceAnimation(move.handIndex(), newCard, null, null);
        return null;
      },
      e -> {
        nextButton.setDisable(false);
        // Play sound effect
        Arrays.stream(actionCard.effects()).forEach(tool -> {
          switch (tool) {
            case PICKAXE:
              playSound(type == Card.Type.BLOCK ? "block_pick" : "repair_pick");
              break;
            case CART:
              playSound(type == Card.Type.BLOCK ? "block_cart" : "repair_cart");
              break;
            case LANTERN:
              playSound(type == Card.Type.BLOCK ? "block_lantern" : "repair_lantern");
              break;
          }
        });
        // Animate player pane
        if (type == Card.Type.BLOCK) {
          playerSidebar.blockTools(targetPlayer, actionCard.effects());
        } else if (type == Card.Type.REPAIR) {
          playerSidebar.repairTools(targetPlayer, actionCard.effects());
        }
      }
    );
  }

  /**
   * Handles end game event
   *
   * @param role       the winning role
   * @param lastPlayer the last player index to move
   */
  private void handleEndGame(Player.Role role, int lastPlayer) {
    playSound("win");
    // TODO: add alert and animation
    log.appendMessage("The " + role.name().toLowerCase() + "s won the game", HistorySidebar.TYPE_WARNING, true);
  }

  private void handleGoalOpen(Board.GoalPosition pos) {
    GoalType goal = boardPane.openGoalCard(pos);
    playSound("card");
    if (goal == GoalType.GOLD) playSound("gold");
  }

  private void nextPlayer() {
    if (!game.started()) {
      try {
        game.initializeRound();
        game.startRound();
        nextButton.setStarted();
      } catch (GameException ignored) {}
    } else {
      game.finalizeTurn();
    }
  }

  GameState state() {
    return this.state;
  }

  GameLogicController game() {
    return this.game;
  }

  /**
   * Plays the background music
   */
  private static void startBackgroundMusic() {
    Media media = new Media(GameGUIController.class.getResource("../audio/background.wav").toExternalForm());
    player = new MediaPlayer(media);
    player.setVolume(0.5f);
    player.setCycleCount(MediaPlayer.INDEFINITE);
    player.play();
  }

  /**
   * Utility function for playing a sound effect
   *
   * @param name the sound file name
   */
  private static void playSound(String name) {
    Thread thread = new Thread(() -> {
      AudioClip clip = new AudioClip(GameGUIController.class.getResource(String.format("../audio/%s.wav", name)).toExternalForm());
      clip.play(1f);
    });
    thread.start();
  }
}
