package model;

import com.sun.istack.internal.NotNull;
import model.cards.BoardActionCard;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The {@link GameState} class represents a game.
 * It contains all necessary methods to control the game flow and logic.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class GameState {
  /** The default minimum players allowed in the game rules */
  private static final int MIN_PLAYER = 4;
  /** The default maximum players allowed in the game rules */
  private static final int MAX_PLAYER = 10;

  /** The default card deck amount */
  public static HashMap<String, Integer> CARD_COMPOSITION = new HashMap<>();

  /* initialize {@link GameState#CARD_COMPOSITION} */
  static {
    CARD_COMPOSITION.put(PathCard.Type.CROSSROAD_PATH.name(), 5);
    CARD_COMPOSITION.put(PathCard.Type.HORIZONTAL_T_PATH.name(), 5);
    CARD_COMPOSITION.put(PathCard.Type.VERTICAL_T_PATH.name(), 5);
    CARD_COMPOSITION.put(PathCard.Type.HORIZONTAL_PATH.name(), 3);
    CARD_COMPOSITION.put(PathCard.Type.VERTICAL_PATH.name(), 4);
    CARD_COMPOSITION.put(PathCard.Type.LEFT_TURN_PATH.name(), 5);
    CARD_COMPOSITION.put(PathCard.Type.RIGHT_TURN_PATH.name(), 4);
    CARD_COMPOSITION.put(PathCard.Type.CROSSROAD_DEADEND.name(), 1);
    CARD_COMPOSITION.put(PathCard.Type.HORIZONTAL_T_DEADEND.name(), 1);
    CARD_COMPOSITION.put(PathCard.Type.VERTICAL_T_DEADEND.name(), 1);
    CARD_COMPOSITION.put(PathCard.Type.BOTH_HORIZONTAL_DEADEND.name(), 1);
    CARD_COMPOSITION.put(PathCard.Type.BOTH_VERTICAL_DEADEND.name(), 1);
    CARD_COMPOSITION.put(PathCard.Type.SINGLE_HORIZONTAL_DEADEND.name(), 1);
    CARD_COMPOSITION.put(PathCard.Type.SINGLE_VERTICAL_DEADEND.name(), 1);
    CARD_COMPOSITION.put(PathCard.Type.LEFT_TURN_DEADEND.name(), 1);
    CARD_COMPOSITION.put(PathCard.Type.RIGHT_TURN_DEADEND.name(), 1);
    CARD_COMPOSITION.put(BoardActionCard.Type.MAP.name(), 6);
    CARD_COMPOSITION.put(BoardActionCard.Type.ROCKFALL.name(), 3);
    CARD_COMPOSITION.put(PlayerActionCard.Type.BLOCK_CART.name(), 3);
    CARD_COMPOSITION.put(PlayerActionCard.Type.BLOCK_LANTERN.name(), 3);
    CARD_COMPOSITION.put(PlayerActionCard.Type.BLOCK_PICKAXE.name(), 3);
    CARD_COMPOSITION.put(PlayerActionCard.Type.REPAIR_CART.name(), 2);
    CARD_COMPOSITION.put(PlayerActionCard.Type.REPAIR_LANTERN.name(), 2);
    CARD_COMPOSITION.put(PlayerActionCard.Type.REPAIR_PICKAXE.name(), 2);
    CARD_COMPOSITION.put(PlayerActionCard.Type.REPAIR_CART_LANTERN.name(), 1);
    CARD_COMPOSITION.put(PlayerActionCard.Type.REPAIR_CART_PICKAXE.name(), 1);
    CARD_COMPOSITION.put(PlayerActionCard.Type.REPAIR_LANTERN_PICKAXE.name(), 1);
  }

  /** Number of players in the game */
  private int numPlayers;
  /** All players that joined the game */
  private ArrayList<Player> players;
  /** The current game board */
  private Board board;
  /** Marks the game as started */
  private boolean started;
  /** Marks the game as finished */
  private boolean finished;
  /** The current card deck */
  private Stack<Card> deck;
  /** The current turn */
  private int currentPlayerIndex;
  /** The non player observers of the game */
  private ArrayList<GameObserver> nonPlayerObservers;

  /**
   * Creates a {@link GameState} object from the available players
   *
   * @param players all players in the game
   * @throws GameException when invalid range
   */
  public GameState(@NotNull Player... players) throws GameException {
    if (players.length < MIN_PLAYER || players.length > MAX_PLAYER) {
      String msgFormat = "Invalid player amount, expected range: %d-%d, found: %d";
      throw new GameException(msgFormat, MIN_PLAYER, MAX_PLAYER, players.length);
    }

    this.numPlayers = players.length;
    this.players = (ArrayList<Player>) Arrays.asList(players);
    this.deck = new Stack<>();
    this.nonPlayerObservers = new ArrayList<>();
  }

  /**
   * Initializes a game round
   *
   * @throws GameException when game is running
   */
  public final void initializeRound() throws GameException {
    if (started) {
      throw new GameException("Cannot reinitialize a running game");
    }

    // Create new board and initialize goals
    this.board = new Board();
    List<GoalType> goals = Arrays.asList(GoalType.GOLD, GoalType.ROCK, GoalType.ROCK);
    Collections.shuffle(goals);
    board.initialize(goals.get(0), goals.get(1), goals.get(2));

    // Shuffle roles
    int numSaboteurs = deriveNumSaboteurs(numPlayers);
    ArrayList<Player.Role> roles = new ArrayList<>();
    for (int i = 0; i < numSaboteurs; ++i) roles.add(Player.Role.SABOTEUR);
    for (int i = 0; i < numPlayers - numSaboteurs; ++i) roles.add(Player.Role.GOLD_MINER);
    Collections.shuffle(roles);

    // Shuffle cards
    this.deck = generateDeck();
    Collections.shuffle(deck);

    // Distribute roles and cards among players
    int cardsPerPlayer = deriveNumCardsPerPlayer(numPlayers);
    ArrayList<ArrayList<Card>> cardDistribution = new ArrayList<>();
    for (int i = 0; i < numPlayers; ++i)
      cardDistribution.add(new ArrayList<>());
    for (int i = 0; i < cardsPerPlayer; ++i)
      cardDistribution.get(i).add(deck.pop());
    for (int i = 0; i < numPlayers; ++i)
      players.get(i).initialize(roles.get(i), cardDistribution.get(i));
  }

  /**
   * Starts the game and randomizes the starting player
   *
   * @throws GameException when game already running
   */
  public final void startRound() throws GameException {
    if (started) {
      throw new GameException("Game already started");
    }
    currentPlayerIndex = new Random().nextInt(numPlayers);
    started = true;
    broadcastGameStarted();
    currentPlayer().notifyPromptMovement();
  }

  /**
   * Finalize the current turn
   */
  public final void finalizeTurn() {
    Player.Role winner = this.checkEndGame();
    if (winner != null) {
      finished = true;
      started = false;
      broadcastGameFinished(winner);
      return;
    }
    currentPlayerIndex = (currentPlayerIndex + 1) % numPlayers;
    broadcastStateChanged();
    currentPlayer().notifyPromptMovement();
  }

  /**
   * Applies a {@link Move} object, which represents a player's move on their turn
   *
   * @param move the move to be played
   * @return result if any, e.g. peeking a goal card
   * @throws GameException when an invalid move is applied
   */
  public final MoveResult playMove(@NotNull Move move) throws GameException {
    if (move.type() == Move.Type.DISCARD) {
      discardCard(move.playerIndex(), move.handIndex(), true);
      broadcastPlayerMove(move);
      return null;
    }
    MoveResult result = playCard(move); // Move.Type.PLAY
    broadcastPlayerMove(move);
    return result;
  }

  /**
   * Discards a card from the specified player and replaces the discarded card with a new one
   *
   * @param playerIndex the playing player
   * @param handIndex   the player hand index
   * @param move        marks whether it is purely the move
   * @throws GameException when an invalid discard is invoked
   */
  private void discardCard(int playerIndex, int handIndex, boolean move) throws GameException {
    // Check if invoker is correct
    if (playerIndex != currentPlayerIndex) {
      String name = players.get(playerIndex).name();
      throw new GameException("It is not %s's turn", name);
    }
    // Take and give card from player
    Player p = players.get(playerIndex);
    Card discarded = p.takeCardAt(handIndex);
    if (move) p.addDiscard(discarded);
    p.giveCard(takeCardFromDeck());
  }

  /**
   * Plays a card
   *
   * @param move the move to be played
   * @return result if any, e.g. peeking a goal card
   * @throws GameException when an invalid move is applied
   */
  private MoveResult playCard(Move move) throws GameException {
    int playerIndex = move.playerIndex();
    int handIndex = move.handIndex();
    int[] args = move.args();
    // Check if invoker is correct
    if (playerIndex != currentPlayerIndex) {
      String name = players.get(playerIndex).name();
      throw new GameException("It is not %s's turn", name);
    }
    // Get player's card
    Card card = players.get(playerIndex).peekCardAt(handIndex);
    MoveResult result = null;
    if (card instanceof PathCard) {
      playPathCard(playerIndex, (PathCard) card, args[0], args[1]);
    } else if (card instanceof PlayerActionCard) {
      playPlayerActionCard(playerIndex, (PlayerActionCard) card, args[0]);
    } else if (card.type() == Card.Type.MAP) {
      Board.GoalPosition pos = Board.GoalPosition.values()[args[0]];
      result = playMapCard(pos);
    } else if (card.type() == Card.Type.ROCKFALL) {
      playRockfallCard(args[0], args[1]);
    } else {
      throw new GameException("Unknown type of card: %s", card);
    }
    // Set move card reference
    move.setCard(card.copy());
    // Discard the played card
    discardCard(playerIndex, handIndex, false);

    return result;
  }

  /**
   * Places a path card on the specified position
   *
   * @param playerIndex the playing player
   * @param card        the path card to be placed
   * @param x           the target x position
   * @param y           the target y position
   * @throws GameException when an invalid move is played
   */
  private void playPathCard(int playerIndex, PathCard card, int x, int y) throws GameException {
    Player p = this.players.get(playerIndex);
    if (p.isSabotaged()) {
      String name = p.name();
      throw new GameException("%s is sabotaged and cannot place a path card", name);
    }
    board.placePathCardAt(card, x, y);
  }

  /**
   * Plays the specified player-action card to the targeted player
   *
   * @param playerIndex the playing player
   * @param card        the player-action card
   * @param targetIndex the targeted player
   * @throws GameException when an invalid move is applied
   */
  private void playPlayerActionCard(int playerIndex, PlayerActionCard card, int targetIndex)
    throws GameException {
    Player p = this.players.get(playerIndex);
    if (card.type() == Card.Type.BLOCK) {
      if (playerIndex == targetIndex) {
        throw new GameException("Cannot sabotage self");
      }
      p.sabotageTool(card.effects()[0]);
    } else if (card.type() == Card.Type.REPAIR) {
      p.repairTool(card.effects());
    }
  }

  /**
   * Plays a map card on the specified goal position
   *
   * @param goalPos the goal position
   * @return a {@link MoveResult} containing the goal type
   */
  private MoveResult playMapCard(Board.GoalPosition goalPos) {
    GoalType type = board.peekGoal(goalPos);
    return new MoveResult(type);
  }

  /**
   * Plays a rockfall card on the specified position
   *
   * @param x the targeted x position
   * @param y the targeted y position
   * @throws GameException when an invalid move is applied
   */
  private void playRockfallCard(int x, int y) throws GameException {
    board.removeCardAt(x, y);
  }

  /**
   * Returns the winning role if endgame condition is reached.
   * Otherwise, it will return <code>null</code>
   *
   * @return the winning role or <code>null</code>
   */
  public final Player.Role checkEndGame() {
    if (board.isGoldReached())
      return Player.Role.GOLD_MINER;
    if (deck.isEmpty() && players.stream().allMatch(player -> player.hand().isEmpty()))
      return Player.Role.SABOTEUR;
    return null;
  }

  /**
   * Takes a card from the deck
   *
   * @return the topmost card if deck is not empty.
   * Otherwise, <code>null</code> is returned
   */
  private Card takeCardFromDeck() {
    return deck.isEmpty() ? null : deck.pop();
  }

  /**
   * Adds a game observer
   *
   * @param observer the observer to be added
   */
  public void addObserver(@NotNull GameObserver observer) {
    observer.setState(this);
    nonPlayerObservers.add(observer);
  }

  /**
   * Removes a game observer
   *
   * @param observer the observer to be removed
   */
  public void removeObserver(@NotNull GameObserver observer) {
    nonPlayerObservers.remove(observer);
  }

  /**
   * Returns the current player index
   *
   * @return the current player index
   */
  public final int currentPlayerIndex() {
    return this.currentPlayerIndex;
  }

  /**
   * Broadcast to all players and observers of a new player move
   */
  private void broadcastPlayerMove(Move move) {
    players.forEach(p -> p.notifyPlayerMove(move));
    nonPlayerObservers.forEach(o -> o.notifyPlayerMove(move));
  }

  /**
   * Broadcast to all players and observers that the game state has updated
   */
  private void broadcastStateChanged() {
    players.forEach(GameObserver::notifyGameStateChanged);
    nonPlayerObservers.forEach(GameObserver::notifyGameStateChanged);
  }

  /**
   * Broadcast to all players and observers that the game has finished
   */
  private void broadcastGameFinished(Player.Role role) {
    players.forEach(p -> p.notifyGameFinished(role, currentPlayerIndex));
    nonPlayerObservers.forEach(o -> o.notifyGameFinished(role, currentPlayerIndex));
  }

  /**
   * Broadcast to all players and observers that
   */
  private void broadcastGameStarted() {
    players.forEach(GameObserver::notifyGameStarted);
    nonPlayerObservers.forEach(GameObserver::notifyGameStarted);
  }

  /**
   * Returns a reference to the current {@link Player}
   *
   * @return the current player
   */
  private Player currentPlayer() { return players.get(currentPlayerIndex); }

  /**
   * Returns the game's started flag
   *
   * @return a boolean representing the game's started flag
   */
  public final boolean started() { return this.started; }

  /**
   * Returns finished flag of game
   *
   * @return a boolean representing the game's finished flag
   */
  public final boolean finished() { return this.finished; }

  /**
   * Returns the total number of saboteurs in the current game
   *
   * @return number of saboteurs in the current game
   */
  public final int numSaboteurs() { return deriveNumSaboteurs(this.numPlayers); }

  /**
   * Get the total number of cards per player for a given total players
   *
   * @param numPlayers the total number of players in this game
   * @return the total number of cards per player
   */
  private static int deriveNumCardsPerPlayer(int numPlayers) {
    if (numPlayers <= 5) return 6;
    if (numPlayers <= 7) return 5;
    return 4;
  }

  /**
   * Get the number of saboteurs allowed in the game based on the specified number of players
   *
   * @param numPlayers total number of players in the game
   * @return the allowed number of saboteurs
   */
  private static int deriveNumSaboteurs(int numPlayers) {
    if (numPlayers == 4) return 1;
    if (numPlayers <= 6) return 2;
    if (numPlayers <= 10) return 3;
    return 0;
  }

  /**
   * Generates the whole playable card deck
   *
   * @return deck of cards
   */
  public static Stack<Card> generateDeck() {
    // Initialize new empty card stack
    Stack<Card> deck = new Stack<>();
    AtomicInteger cardIndex = new AtomicInteger(1);

    // Get all enum string values
    List<String> pathTypes =
      Arrays.stream(PathCard.Type.values()).map(Enum::name).collect(Collectors.toList());
    List<String> boardActionTypes =
      Arrays.stream(BoardActionCard.Type.values()).map(Enum::name).collect(Collectors.toList());
    List<String> playerActionTypes =
      Arrays.stream(PlayerActionCard.Type.values()).map(Enum::name).collect(Collectors.toList());

    // Iterate through amount data
    CARD_COMPOSITION.forEach((type, amt) -> {
      for (int i = 0; i < amt; ++i) {
        if (pathTypes.contains(type)) {
          deck.push(new PathCard(cardIndex.getAndIncrement(), PathCard.Type.valueOf(type)));
        } else if (boardActionTypes.contains(type)) {
          deck.push(new BoardActionCard(cardIndex.getAndIncrement(), BoardActionCard.Type.valueOf(type)));
        } else if (playerActionTypes.contains(type)) {
          deck.push(new PlayerActionCard(cardIndex.getAndIncrement(), PlayerActionCard.Type.valueOf(type)));
        }
      }
    });

    return deck;
  }
}
