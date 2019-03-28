package model;

import model.cards.BoardActionCard;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.*;

/**
 * The {@link GameLogicController} class contains all the necessary logic
 * and flow of the game. It provides a way to control a {@link GameState} from the
 * given methods
 */
public class GameLogicController {
  /** The default minimum players allowed in the game rules */
  private static final int MIN_PLAYER = 4;
  /** The default maximum players allowed in the game rules */
  private static final int MAX_PLAYER = 10;
  /** The default card deck amount */
  public static HashMap<String, Integer> CARD_COMPOSITION = new HashMap<>();

  /** Marks the top goal as opened */
  private boolean topGoalOpened;
  /** Marks the top goal as opened */
  private boolean middleGoalOpened;
  /** Marks the top goal as opened */
  private boolean bottomGoalOpened;

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

  /** The controlled game state */
  private final GameState game;
  /** The non player observers of the game */
  private ArrayList<GameObserver> nonPlayerObservers;

  /**
   * Creates a {@link GameLogicController} object from the available players
   *
   * @param state   the game state
   * @param players all players in the game
   * @throws GameException when invalid range
   */
  public GameLogicController(GameState state, Player... players) throws GameException {
    if (players.length < MIN_PLAYER || players.length > MAX_PLAYER) {
      String msgFormat = "Invalid player amount, expected range: %d-%d, found: %d";
      throw new GameException(msgFormat, MIN_PLAYER, MAX_PLAYER, players.length);
    }

    game = state;

    ArrayList<Player> playerList = new ArrayList<>(Arrays.asList(players));
    playerList.forEach(p -> p.setGame(this));
    game.setPlayers(playerList);
    this.nonPlayerObservers = new ArrayList<>();
  }

  /**
   * Initializes a game round
   *
   * @throws GameException when game is running
   */
  public final void initializeRound() throws GameException {
    if (game.started()) {
      throw new GameException("Cannot reinitialize a running game");
    }

    // Create new board and initialize goals
    game.setBoard(new Board());
    List<GoalType> goals = Arrays.asList(GoalType.GOLD, GoalType.ROCK, GoalType.ROCK);
    Collections.shuffle(goals);
    game.board().initialize(goals.get(0), goals.get(1), goals.get(2));

    // Shuffle roles
    int numPlayers = game.numPlayers();
    int numSaboteurs = deriveNumSaboteurs(numPlayers);
    ArrayList<Player.Role> roles = new ArrayList<>();
    for (int i = 0; i < numSaboteurs; ++i) roles.add(Player.Role.SABOTEUR);
    for (int i = 0; i < numPlayers - numSaboteurs; ++i) roles.add(Player.Role.GOLD_MINER);
    Collections.shuffle(roles);

    // Shuffle cards
    Stack<Card> deck = generateDeck();
    Collections.shuffle(deck);
    game.setDeck(deck);

    // Distribute roles and cards among players
    int cardsPerPlayer = deriveNumCardsPerPlayer(numPlayers);
    ArrayList<ArrayList<Card>> cardDistribution = new ArrayList<>();
    for (int i = 0; i < numPlayers; ++i)
      cardDistribution.add(new ArrayList<>());
    for (int i = 0; i < cardsPerPlayer; ++i)
      cardDistribution.forEach(h -> h.add(deck.pop()));
    for (int i = 0; i < numPlayers; ++i)
      game.playerAt(i).initialize(i, roles.get(i), cardDistribution.get(i));
  }

  /**
   * Starts the game and randomizes the starting player
   *
   * @throws GameException when game already running
   */
  public final void startRound() throws GameException {
    if (game.started()) {
      throw new GameException("Game already started");
    }
    int numPlayers = game.numPlayers();
    game.setCurrentPlayerIndex(new Random().nextInt(numPlayers));
    game.setStarted(true);
    broadcastGameStarted();
    broadcastNextTurn();
    currentPlayer().notifyPromptMovement();
  }

  /**
   * Finalize the current turn
   */
  public final void finalizeTurn() {
    Player.Role winner = this.checkEndGame();
    if (board().isReachable(board().topGoalPosition()) && !topGoalOpened) {
      broadcastGoalOpened(Board.GoalPosition.TOP);
      topGoalOpened = true;
    }
    if (board().isReachable(board().middleGoalPosition()) && !middleGoalOpened) {
      broadcastGoalOpened(Board.GoalPosition.MIDDLE);
      middleGoalOpened = true;
    }
    if (board().isReachable(board().bottomGoalPosition()) && !bottomGoalOpened) {
      broadcastGoalOpened(Board.GoalPosition.BOTTOM);
      bottomGoalOpened = true;
    }
    if (winner != null) {
      game.setFinished(true);
      game.setStarted(false);
      broadcastGameFinished(winner);
      return;
    }
    currentPlayer().hand().forEach(c -> {
      if (c instanceof PathCard) ((PathCard) c).setRotated(false);
    });
    game.incrementPlayerIndex();
    broadcastStateChanged();
    broadcastNextTurn();
    currentPlayer().notifyPromptMovement();
  }

  /**
   * Applies a {@link Move} object, which represents a player's move on their turn
   *
   * @param move the move to be played
   * @throws GameException when an invalid move is applied
   */
  public final void playMove(Move move) throws GameException {
    if (move == null) return;
    if (move.type() == Move.Type.DISCARD) {
      Card card = discardCard(move.playerIndex(), move.handIndex(), true);
      broadcastPlayerMove(move, card);
    }
    Card card = playCard(move);
    broadcastPlayerMove(move, card);
  }

  /**
   * Discards a card from the specified player and replaces the discarded card with a new one
   *
   * @param playerIndex the playing player
   * @param handIndex   the player hand index
   * @param move        marks whether it is purely the move
   * @return the new added card
   * @throws GameException when an invalid discard is invoked
   */
  private Card discardCard(int playerIndex, int handIndex, boolean move) throws GameException {
    // Check if invoker is correct
    if (playerIndex != game.currentPlayerIndex()) {
      String name = game.playerAt(playerIndex).name();
      throw new GameException("It is not %s's turn", name);
    }
    // Take and give card from player
    Player p = game.playerAt(playerIndex);
    Card discarded = p.takeCardAt(handIndex);
    if (move) p.addDiscard(discarded);
    Card card = takeCardFromDeck();
    p.giveCard(card);
    return card;
  }

  /**
   * Plays a card
   *
   * @param move the move to be played
   * @return the new card
   * @throws GameException when an invalid move is applied
   */
  private Card playCard(Move move) throws GameException {
    int playerIndex = move.playerIndex();
    int handIndex = move.handIndex();
    int[] args = move.args();
    // Check if invoker is correct
    if (playerIndex != game.currentPlayerIndex()) {
      String name = game.playerAt(playerIndex).name();
      throw new GameException("It is not %s's turn", name);
    }
    // Get player's card
    Card card = playerAt(playerIndex).peekCardAt(handIndex);
    switch (move.type()) {
      case PLAY_PATH:
        if (!(card instanceof PathCard)) {
          throw new GameException("Cannot create a path with a non path card");
        }
        ((PathCard) card).setRotated(args[2] == 1);
        playPathCard(playerIndex, (PathCard) card, args[0], args[1]);
        break;
      case PLAY_PLAYER:
        if (!(card instanceof PlayerActionCard)) {
          throw new GameException("Cannot block/repair another player with a non player-action card");
        }
        playPlayerActionCard(playerIndex, (PlayerActionCard) card, args[0]);
        break;
      case PLAY_MAP:
        if (card.type() != Card.Type.MAP) {
          throw new GameException("Cannot open a goal card with a non map card");
        }
        Board.GoalPosition pos = Board.GoalPosition.values()[args[0]];
        playMapCard(playerIndex, pos);
        break;
      case PLAY_ROCKFALL:
        if (card.type() != Card.Type.ROCKFALL) {
          throw new GameException("Cannot destroy a path with a non rockfall card");
        }
        playRockfallCard(args[0], args[1]);
        break;
      case DISCARD:
        break;
      default:
        throw new GameException("Unknown move type");
    }
    // Set move card reference
    move.setCard(card.copy());
    // Discard the played card
    return discardCard(playerIndex, handIndex, false);
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
    Player p = game.playerAt(playerIndex);
    if (p.isSabotaged()) {
      String name = p.name();
      throw new GameException("%s is sabotaged and cannot place a path card", name);
    }
    game.board().placePathCardAt(card, x, y);
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
    Player p = game.players().get(targetIndex);
    if (card.type() == Card.Type.BLOCK) {
      if (playerIndex == targetIndex) {
        throw new GameException("Cannot sabotage self");
      }
      p.sabotageTool(card.effects()[0]);
    } else if (card.type() == Card.Type.REPAIR) {
      p.repairTools(card.effects());
    }
  }

  /**
   * Plays a map card on the specified goal position
   *
   * @param playerIndex the playing player
   * @param goalPos     the goal position
   */
  private void playMapCard(int playerIndex, Board.GoalPosition goalPos) {
    sendGoalType(playerIndex, goalPos);
  }

  /**
   * Plays a rockfall card on the specified position
   *
   * @param x the targeted x position
   * @param y the targeted y position
   * @throws GameException when an invalid move is applied
   */
  private void playRockfallCard(int x, int y) throws GameException {
    game.board().removeCardAt(x, y);
  }

  /**
   * Returns the winning role if endgame condition is reached.
   * Otherwise, it will return <code>null</code>
   *
   * @return the winning role or <code>null</code>
   */
  public final Player.Role checkEndGame() {
    if (game.board().isGoldReached())
      return Player.Role.GOLD_MINER;
    if (game.deck().isEmpty() && game.players().stream().allMatch(player -> player.hand().isEmpty()))
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
    return game.deck().isEmpty() ? null : game.deck().pop();
  }

  /**
   * Adds a game observer
   *
   * @param observer the observer to be added
   */
  public void addObserver(GameObserver observer) {
    observer.setGame(this);
    nonPlayerObservers.add(observer);
  }

  /**
   * Removes a game observer
   *
   * @param observer the observer to be removed
   */
  public void removeObserver(GameObserver observer) {
    nonPlayerObservers.remove(observer);
  }

  /**
   * Returns the current player index
   *
   * @return the current player index
   */
  public final int currentPlayerIndex() {
    return game.currentPlayerIndex();
  }

  /**
   * Returns a reference to the current {@link Player}
   *
   * @return the current player
   */
  private Player currentPlayer() { return game.players().get(currentPlayerIndex()); }

  /**
   * Returns a player of the specified index
   *
   * @param index the specified index
   * @return the requested player
   */
  public final Player playerAt(int index) {
    return game.players().get(index);
  }

  /**
   * Broadcast to all observers that it is the next turn
   */
  private void broadcastNextTurn() {
    ArrayList<Card> hand = new ArrayList<>(currentPlayer().hand());
    nonPlayerObservers.forEach(o -> o.notifyNextPlayer(currentPlayerIndex(), hand));
  }

  /**
   * Broadcast to all players and observers of a new player move
   */
  private void broadcastPlayerMove(Move move, Card newCard) {
    game.players().forEach(p -> p.notifyPlayerMove(move, newCard));
    nonPlayerObservers.forEach(o -> o.notifyPlayerMove(move, newCard));
  }

  /**
   * Broadcast to all players and observers that the game state has updated
   */
  private void broadcastStateChanged() {
    game.players().forEach(GameObserver::notifyGameStateChanged);
    nonPlayerObservers.forEach(GameObserver::notifyGameStateChanged);
  }

  /**
   * Broadcast to all players and observers that the game has finished
   */
  private void broadcastGameFinished(Player.Role role) {
    game.players().forEach(p -> p.notifyGameFinished(role, currentPlayerIndex()));
    nonPlayerObservers.forEach(o -> o.notifyGameFinished(role, currentPlayerIndex()));
  }

  /**
   * Broadcast to all players and observers that the game has started
   */
  private void broadcastGameStarted() {
    game.players().forEach(GameObserver::notifyGameStarted);
    nonPlayerObservers.forEach(GameObserver::notifyGameStarted);
  }

  /**
   * Broadcast to all players and observers that a goal card was opened
   *
   * @param position the opened goal position
   */
  private void broadcastGoalOpened(Board.GoalPosition position) {
    GoalType type = board().peekGoal(position);
    game.players().forEach(p -> p.notifyGoalOpen(position, type, true));
    nonPlayerObservers.forEach(o -> o.notifyGoalOpen(position, type, true));
  }

  /**
   * Sends the specified playerIndex, and all observers the specified goal
   *
   * @param playerIndex recipient player
   * @param position    the opened goal position
   */
  private void sendGoalType(int playerIndex, Board.GoalPosition position) {
    GoalType type = board().peekGoal(position);
    playerAt(playerIndex).notifyGoalOpen(position, type, false);
    nonPlayerObservers.forEach(o -> o.notifyGoalOpen(position, type, false));
  }

  /**
   * Returns the game's started flag
   *
   * @return a boolean representing the game's started flag
   */
  public final boolean started() { return game.started(); }

  /**
   * Returns finished flag of game
   *
   * @return a boolean representing the game's finished flag
   */
  public final boolean finished() { return game.finished(); }

  /**
   * Returns the total number of players in the current game
   *
   * @return number of players in the current game
   */
  public final int numPlayers() { return game.numPlayers(); }

  /**
   * Returns the total number of saboteurs in the current game
   *
   * @return number of saboteurs in the current game
   */
  public final int numSaboteurs() { return game.numSaboteurs(); }

  /**
   * Returns the remaining deck size
   *
   * @return the remaining deck size
   */
  public final int drawPileSize() {
    return game.deck().size();
  }

  /**
   * Returns the current game board
   *
   * @return the current game board
   */
  public final Board board() { return game.board(); }

  /**
   * Get the total number of cards per player for a given total players
   *
   * @param numPlayers the total number of players in this game
   * @return the total number of cards per player
   */
  public static int deriveNumCardsPerPlayer(int numPlayers) {
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
  public static int deriveNumSaboteurs(int numPlayers) {
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
    int cardIndex = 1;

    //=====================================
    // initialize all pathway cards
    //=====================================
    for (int i = 0; i < CARD_COMPOSITION.get("CROSSROAD_PATH"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.CROSSROAD_PATH));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("HORIZONTAL_T_PATH"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.HORIZONTAL_T_PATH));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("VERTICAL_T_PATH"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.VERTICAL_T_PATH));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("HORIZONTAL_PATH"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.HORIZONTAL_PATH));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("VERTICAL_PATH"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.VERTICAL_PATH));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("LEFT_TURN_PATH"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.LEFT_TURN_PATH));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("RIGHT_TURN_PATH"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.RIGHT_TURN_PATH));
    }

    //=====================================
    // initialize all deadend cards
    //=====================================
    for (int i = 0; i < CARD_COMPOSITION.get("CROSSROAD_DEADEND"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.CROSSROAD_DEADEND));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("HORIZONTAL_T_DEADEND"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.HORIZONTAL_T_DEADEND));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("VERTICAL_T_DEADEND"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.VERTICAL_T_DEADEND));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("BOTH_HORIZONTAL_DEADEND"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.BOTH_HORIZONTAL_DEADEND));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("BOTH_VERTICAL_DEADEND"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.BOTH_VERTICAL_DEADEND));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("LEFT_TURN_DEADEND"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.LEFT_TURN_DEADEND));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("RIGHT_TURN_DEADEND"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.RIGHT_TURN_DEADEND));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("SINGLE_HORIZONTAL_DEADEND"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.SINGLE_HORIZONTAL_DEADEND));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("SINGLE_VERTICAL_DEADEND"); i++) {
      deck.push(new PathCard(cardIndex++, PathCard.Type.SINGLE_VERTICAL_DEADEND));
    }

    //=====================================
    // initialize all board action cards
    //=====================================
    for (int i = 0; i < CARD_COMPOSITION.get("MAP"); i++) {
      deck.push(new BoardActionCard(cardIndex++, BoardActionCard.Type.MAP));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("ROCKFALL"); i++) {
      deck.push(new BoardActionCard(cardIndex++, BoardActionCard.Type.ROCKFALL));
    }

    //=====================================
    // initialize all repair cards
    //=====================================
    for (int i = 0; i < CARD_COMPOSITION.get("REPAIR_CART"); i++) {
      deck.push(new PlayerActionCard(cardIndex++, PlayerActionCard.Type.REPAIR_CART));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("REPAIR_LANTERN"); i++) {
      deck.push(new PlayerActionCard(cardIndex++, PlayerActionCard.Type.REPAIR_LANTERN));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("REPAIR_PICKAXE"); i++) {
      deck.push(new PlayerActionCard(cardIndex++, PlayerActionCard.Type.REPAIR_PICKAXE));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("REPAIR_CART_LANTERN"); i++) {
      deck.push(new PlayerActionCard(cardIndex++, PlayerActionCard.Type.REPAIR_CART_LANTERN));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("REPAIR_CART_PICKAXE"); i++) {
      deck.push(new PlayerActionCard(cardIndex++, PlayerActionCard.Type.REPAIR_CART_PICKAXE));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("REPAIR_LANTERN_PICKAXE"); i++) {
      deck.push(new PlayerActionCard(cardIndex++, PlayerActionCard.Type.REPAIR_LANTERN_PICKAXE));
    }

    //=====================================
    // initialize all block cards
    //=====================================
    for (int i = 0; i < CARD_COMPOSITION.get("BLOCK_CART"); i++) {
      deck.push(new PlayerActionCard(cardIndex++, PlayerActionCard.Type.BLOCK_CART));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("BLOCK_LANTERN"); i++) {
      deck.push(new PlayerActionCard(cardIndex++, PlayerActionCard.Type.BLOCK_LANTERN));
    }
    for (int i = 0; i < CARD_COMPOSITION.get("BLOCK_PICKAXE"); i++) {
      deck.push(new PlayerActionCard(cardIndex++, PlayerActionCard.Type.BLOCK_PICKAXE));
    }

    return deck;
  }
}
