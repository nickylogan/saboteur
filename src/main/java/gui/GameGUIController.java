package gui;

import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.*;
import model.cards.BoardActionCard;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.*;

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
    protected void onNextTurn(int player, Player.Role role, ArrayList<Card> hand) {
      GameGUIController.this.handleNextTurn(player, role, hand);
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

  /**
   * The {@link GUIGamePlayer} class allows a human player to
   * interact with the game
   */
  public static class GUIGamePlayer extends Player {
    public GUIGamePlayer(String name) {
      super(name);
    }
  }

  //=========================
  // GUI components
  //=========================
  /** A reference to the current scene */
  private final Scene scene;
  /** GUI layers */
  private final AnchorPane layers;
  /** The move history sidebar panel */
  private final HistorySidebar log;
  /** The player info sidebar panel */
  private final PlayerSidebar playerSidebar;
  /** The current player info */
  private final PlayerInfoPane playerInfoPane;
  /** The player hand panel */
  private final PlayerCardsPane playerCardsPane;
  /** The card animation overlay */
  private final CardAnimationOverlay animationOverlay;
  /** The next move button */
  private final NextButton nextButton;
  /** The board pane */
  private BoardPane boardPane;
  /** The background music */
  private static MediaPlayer player;
  /** List of audio clips */
  private static final Map<String, AudioClip> audioClips = new HashMap<>() {{
    put("block_cart",
        new AudioClip(GameGUIController.class.getResource("/audio/block_cart.wav").toExternalForm())
    );
    put("block_lantern",
        new AudioClip(GameGUIController.class.getResource("/audio/block_lantern.wav").toExternalForm())
    );
    put("block_pick",
        new AudioClip(GameGUIController.class.getResource("/audio/block_pick.wav").toExternalForm())
    );
    put("card",
        new AudioClip(GameGUIController.class.getResource("/audio/card.wav").toExternalForm())
    );
    put("discard",
        new AudioClip(GameGUIController.class.getResource("/audio/discard.wav").toExternalForm())
    );
    put("error",
        new AudioClip(GameGUIController.class.getResource("/audio/error.wav").toExternalForm())
    );
    put("gold",
        new AudioClip(GameGUIController.class.getResource("/audio/gold.wav").toExternalForm())
    );
    put("map",
        new AudioClip(GameGUIController.class.getResource("/audio/map.wav").toExternalForm())
    );
    put("start",
        new AudioClip(GameGUIController.class.getResource("/audio/start.wav").toExternalForm())
    );
    put("pass_turn",
        new AudioClip(GameGUIController.class.getResource("/audio/pass_turn.wav").toExternalForm())
    );
    put("path",
        new AudioClip(GameGUIController.class.getResource("/audio/path.wav").toExternalForm())
    );
    put("repair_cart",
        new AudioClip(GameGUIController.class.getResource("/audio/repair_cart.wav").toExternalForm())
    );
    put("repair_lantern",
        new AudioClip(GameGUIController.class.getResource("/audio/repair_lantern.wav").toExternalForm())
    );
    put("repair_pick",
        new AudioClip(GameGUIController.class.getResource("/audio/repair_pick.wav").toExternalForm())
    );
    put("rockfall",
        new AudioClip(GameGUIController.class.getResource("/audio/rockfall.wav").toExternalForm())
    );
    put("select_card",
        new AudioClip(GameGUIController.class.getResource("/audio/select_card.wav").toExternalForm())
    );
    put("win",
        new AudioClip(GameGUIController.class.getResource("/audio/win.wav").toExternalForm())
    );
  }};


  //=========================
  // Game logic components
  //=========================
  /** Stores a reference to game logic controller */
  private final GameLogicController game;
  /** Stores a reference to game state */
  private final GameState state;
  /** Allows the window to be an observer of the game */
  private final GUIGameObserver observer;
  /** Stores a reference to the last played move */
  private Move lastMove;
  /** Stores a reference to the last taken card from the deck */
  private Card lastCard;
  /** Stores a reference to the last player hand */
  private ArrayList<Card> lastHand;

  private Card selectedCard;
  private int selectedIndex;

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

    window.boardPane.focusAt(4, 2);

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
    Font.loadFont(getClass().getResourceAsStream("/fonts/nevis.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("/fonts/WorkSans-Black.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("/fonts/WorkSans-Bold.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("/fonts/WorkSans-ExtraBold.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("/fonts/WorkSans-ExtraLight.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("/fonts/WorkSans-Light.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("/fonts/WorkSans-Medium.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("/fonts/WorkSans-Regular.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("/fonts/WorkSans-SemiBold.ttf"), 120);
    Font.loadFont(getClass().getResourceAsStream("/fonts/WorkSans-Thin.ttf"), 120);

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
    playerInfoPane = new PlayerInfoPane(this);
    animationOverlay = new CardAnimationOverlay(this);
    log = new HistorySidebar(this);
    nextButton = new NextButton();
    nextButton.setOnMouseClicked(e -> nextPlayer());
    layers.getChildren().addAll(boardPane, playerCardsPane, playerSidebar, playerInfoPane, animationOverlay, log, nextButton);

    // Add event handlers
    this.scene.setOnKeyReleased(this::handleKeyReleased);
    this.scene.setOnMouseMoved(this::handleMouseMoved);

    // Start audio
    startBackgroundMusic();
  }

  /**
   * Commits the played move to the GUI
   */
  private void applyMoveToGUI() {
    // Resets overlays
    boardPane.resetOverlays();
    playerSidebar.resetAvailable();

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
   * @param role   the role of the new player
   * @param hand   the respective player's card
   * @see GUIGameObserver#onNextTurn(int, Player.Role, ArrayList)
   */
  private void handleNextTurn(int player, Player.Role role, ArrayList<Card> hand) {
    String fmt = "It is %s's turn";
    Player p = state.playerAt(player);
    String msg = String.format(fmt, p.name());
    log.appendMessage(msg, HistorySidebar.TYPE_INFO, false);

    lastHand = hand;
    playerSidebar.setCurrent(player);
    playerSidebar.clearTarget();
    playerInfoPane.change(role, p.sabotaged());

    // Reset selected card
    selectedCard = null;
    selectedIndex = -1;

    if (game.playerAt(player) instanceof GUIGamePlayer) {
      playerCardsPane.playHideAnimation(
        () -> {
          boardPane.focusAt(4, 2);
          return null;
        }, e -> {
          // TODO: add alert if current player is not an AI
          // new alert
          // show and wait
          playerCardsPane.playShowAnimation(p.name(), hand,null, e2 -> {
            playerCardsPane.setInteractive(true);
            boardPane.sabotaged = state.playerAt(player).isSabotaged();
          });
        }
      );
      nextButton.setDisable(true);
    } else {
      playerCardsPane.setInteractive(false);
      playerCardsPane.playChangeHandAnimation(state.playerAt(player).name(), hand,null, e -> applyMoveToGUI());
    }
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
    // System.out.println(move);
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

  /**
   * Handles path cards
   *
   * @param move    the played path move
   * @param newCard the card taken from the deck
   */
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

  /**
   * Handles map cards
   *
   * @param move    the played map move
   * @param newCard the card taken from the deck
   */
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

  /**
   * Handles rockfall cards
   *
   * @param move    the played rockfall move
   * @param newCard the card taken from the deck
   */
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
    nextButton.setDisable(true);
    nextButton.setText("FINISHED");
  }

  private void handleGoalOpen(Board.GoalPosition pos) {
    GoalType goal = boardPane.openGoalCard(pos);
    playSound("card");
    if (goal == GoalType.GOLD) playSound("gold");
  }

  void handleCardSelected(int index) {
    // mark a card as selected
    selectedIndex = index;
    // set selected card variable
    selectedCard = lastHand.get(index);
    // reset all panes' overlays
    boardPane.resetOverlays();
    playerSidebar.resetAvailable();
    // apply overlay to card animation overlay
    // delegate to appropriate handler
    if (selectedCard instanceof PathCard || selectedCard instanceof BoardActionCard) {
      boardPane.highlightAvailable(selectedCard);
    } else if (selectedCard instanceof PlayerActionCard) {
      playerSidebar.highlightAvailable((PlayerActionCard) selectedCard);
    }
  }

  void applyManualPathMove(int x, int y) {
    nextButton.setDisable(false);
    playerCardsPane.setInteractive(false);
    PathCard path = (PathCard) selectedCard;
    Move move = Move.NewPathMove(game.currentPlayerIndex(), selectedIndex, x, y, path.rotated());
    try {
      game.playMove(move);
    } catch (GameException e) {
      System.out.println("Manual: " + e.getMessage());
    }
    applyMoveToGUI();
  }

  void applyManualRockfallMove(int x, int y) {
    nextButton.setDisable(false);
    playerCardsPane.setInteractive(false);
    Move move = Move.NewRockfallMove(game.currentPlayerIndex(), selectedIndex, x, y);
    try {
      game.playMove(move);
    } catch (GameException e) {
      System.out.println("Manual: " + e.getMessage());
    }
    applyMoveToGUI();
  }

  void applyManualMapMove(Board.GoalPosition pos) {
    nextButton.setDisable(false);
    playerCardsPane.setInteractive(false);
    Move move = Move.NewMapMove(game.currentPlayerIndex(), selectedIndex, pos);
    try {
      game.playMove(move);
    } catch (GameException e) {
      System.out.println("Manual: " + e.getMessage());
    }
    applyMoveToGUI();
  }

  void applyManualPlayerActionMove(int targetPlayer) {
    nextButton.setDisable(false);
    playerCardsPane.setInteractive(false);
    Move move = Move.NewPlayerActionMove(game.currentPlayerIndex(), selectedIndex, targetPlayer);
    try {
      game.playMove(move);
    } catch (GameException e) {
      System.out.println("Manual: " + e.getMessage());
    }
    applyMoveToGUI();
  }

  private void applyManualDiscardMove(int index) {
    nextButton.setDisable(false);
    playerCardsPane.setInteractive(false);
    Move move = Move.NewDiscardMove(game.currentPlayerIndex(), index);
    try {
      game.playMove(move);
    } catch (GameException e) {
      System.out.println("Manual: " + e.getMessage());
    }
    applyMoveToGUI();
  }

  private void nextPlayer() {
    if (!game.started()) {
      playSound("start");
      try {
        game.initializeRound();
        game.startRound();
        nextButton.setStarted();
      } catch (GameException ignored) {}
    } else {
      playSound("pass_turn");
      game.finalizeTurn();
    }
  }

  private void handleMouseMoved(MouseEvent e) {
    if (!playerCardsPane.interactive) return;
    if (!(selectedCard instanceof PlayerActionCard)) {
      boardPane.handleMoved(e);
    }
  }

  private void handleKeyReleased(KeyEvent e) {
    if (!playerCardsPane.interactive) return;
    // press R to rotate
    // press delete or backspace to discard
    if (e.getCode() == KeyCode.R && selectedCard instanceof PathCard) {
      playerCardsPane.rotateCardAt(selectedIndex);
      boardPane.resetCellOverlays();
      boardPane.rotateOverlay();
      ((PathCard) selectedCard).rotate();
      boardPane.highlightAvailable(selectedCard);
    }
    if (e.getCode() == KeyCode.D && selectedIndex != -1) {
      applyManualDiscardMove(selectedIndex);
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
    Media media = new Media(GameGUIController.class.getResource("/audio/background.wav").toExternalForm());
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
  static void playSound(String name) {
    AudioClip clip = audioClips.get(name);
    if (clip != null) clip.play(1f);
  }
}
