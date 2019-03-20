package model;

import model.cards.Card;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The {@link MockGame} class is <strong>used for testing purposes.</strong> The class
 * represents a mock game with mock players
 */
public class MockGame extends GameLogicController {
  private static class MockPlayer extends Player {
    MockPlayer(String name) {
      super(name);
    }
  }

  private MockGame(GameState state, Player... players) throws GameException {
    super(state, players);
  }

  /**
   * Creates a mock game
   * @param observer observer for game
   * @return mock game
   */
  public static MockGame CreateMockGame(GameObserver observer) throws GameException {
    Player[] players = new Player[]{
      new MockPlayer("Player 1"),
      new MockPlayer("Player 2"),
      new MockPlayer("Player 3"),
      new MockPlayer("Player 4"),
      new MockPlayer("Player 5"),
    };
    GameState state = new GameState();
    MockGame game = new MockGame(state, players);
    game.addObserver(observer);

    // Create new board
    Board board = new Board();
    board.initialize(GoalType.ROCK, GoalType.GOLD, GoalType.ROCK);
    state.setBoard(board);

    // Distribute roles
    Player.Role[] roles = new Player.Role[]{
      Player.Role.SABOTEUR,
      Player.Role.SABOTEUR,
      Player.Role.GOLD_MINER,
      Player.Role.GOLD_MINER,
      Player.Role.GOLD_MINER
    };

    // Initialize cards
    new ArrayList<Integer>();
    int[] idxs = new int[]{
      0, 1, 2, 3, 4,        // Crossroads
      5, 6, 7, 8, 9,        // Vertical T path
      10, 11, 12, 13, 14,   // Horizontal T path
      58, 61, 64, 59, 62,   // Block: C, L, P, C, L
      49, 51, 53, 50, 52,   // Repair: C, L, P, C, L
      40, 41, 42, 46, 47,   // Map, Map, Map, Rockfall, Rockfall
    };
    List<Integer> removedIndices = Arrays.stream(idxs).boxed().collect(Collectors.toList());
    Stack<Card> deck = generateDeck();
    List<Card> dist = removedIndices.stream().map(deck::get).collect(Collectors.toList());
    removedIndices.sort(Collections.reverseOrder());
    removedIndices.forEach(deck::removeElementAt);
    state.setDeck(deck);

    // Distribute roles and cards among players
    int cardsPerPlayer = deriveNumCardsPerPlayer(players.length);
    ArrayList<ArrayList<Card>> cardDistribution = new ArrayList<>();
    for (int i = 0; i < players.length; ++i)
      cardDistribution.add(new ArrayList<>());
    for (int i = 0; i < cardsPerPlayer; ++i)
      cardDistribution.forEach(h -> h.add(dist.remove(0)));
    for (int i = 0; i < players.length; ++i)
      game.playerAt(i).initialize(i, roles[i], cardDistribution.get(i));

    // Intended role and card distribution
    // 0: + HT VT BC RC M
    // 1: + HT VT BL RL M
    // 2: + HT VT BP RP M
    // 3: + HT VT BC RC R
    // 4: + HT VT BL RL R

    state.setCurrentPlayerIndex(0);

    // Player 0 places T path to (1,2)
    System.out.println(game.currentPlayerIndex());
    game.playMove(Move.NewPathMove(0, 1, 1, 2, false));
    game.finalizeTurn();
    // Player 1 places + path to (2,2)
    System.out.println(game.currentPlayerIndex());
    game.playMove(Move.NewPathMove(1, 0, 2, 2, false));
    game.finalizeTurn();
    // Player 2 places rotated T path to (3,2)
    System.out.println(game.currentPlayerIndex());
    game.playMove(Move.NewPathMove(2, 1, 3, 2, true));
    game.finalizeTurn();
    // Player 3 places + path to (4,2)
    System.out.println(game.currentPlayerIndex());
    game.playMove(Move.NewPathMove(3, 0, 4, 2, false));
    game.finalizeTurn();
    // Player 4 places T path to (5,2)
    System.out.println(game.currentPlayerIndex());
    game.playMove(Move.NewPathMove(4, 1, 5, 2, false));
    game.finalizeTurn();
    // Player 0 places + path to (6,2)
    System.out.println(game.currentPlayerIndex());
    game.playMove(Move.NewPathMove(0, 0, 6, 2, false));
    game.finalizeTurn();

    // Card distribution after moves
    // 0: VT BC RC  M X X
    // 1: HT VT BL RL M X
    // 2:  + VT BP RP M X
    // 3: HT VT BC RC R X
    // 4:  + VT BL RL R X
    return game;
  }

  public void addMove(Move move) throws GameException {
    playMove(move);
    finalizeTurn();
  }
}
