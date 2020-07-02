package ai.impl.example;

import ai.AI;
import model.*;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ExampleAI extends AI {
  public ExampleAI(String name) {
    super(name);
  }

  private void foo() {
    // role() is used to get your current role (either SABOTEUR/GOLD_MINER)
    // see Player.Role in the model package
    Role role = role();

    // history() is used to get the game move history
    History history = history();

    // discarded() is used to see cards you have previously discarded
    ArrayList<Card> discarded = discarded();

    // isSabotaged() is used to check if your player is sabotaged
    boolean sabotaged = isSabotaged();

    // knownGoals() is used to get opened goals, whether by a map card, or path-reachable
    Map<Board.GoalPosition, GoalType> knownGoals = knownGoals();
  }

  @Override
  protected Move makeDecision() {
    // The index is always required to create a move.
    // It is used to identify yourself.
    // Access from calling index()
    int myIndex = index();

    // Prepare move to be returned
    Move move = null;

    // hand() is used to get all cards currently in your hand
    ArrayList<Card> cards = hand();

    // game() is used to get the game state
    GameLogicController game = game();

    // Iterate through hand
    for (int cardIndex = 0; cardIndex < cards.size(); ++cardIndex) {
      // Get reference of card at hand
      Card card = cards.get(cardIndex);

      // Example of placing a path card
      if (card.type() == Card.Type.PATHWAY) {
        PathCard pCard = ((PathCard) card);
        pCard.rotate();
        Set<Position> placeable = game.board().getPlaceable(pCard);
        Position target = placeable.toArray(new Position[0])[0];
        move = Move.NewPathMove(myIndex, cardIndex, target.x, target.y, true);
        break;
      }

      // Example of blocking another player
      if (card.type() == Card.Type.BLOCK) {
        PlayerActionCard pCard = ((PlayerActionCard) card);
        int targetPlayer = 1;
        Player p = game.playerAt(targetPlayer);
        Tool tool = pCard.effects()[0];
        if (p.isSabotageable(tool))
          move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
        break;
      }

      // Example of repairing another player
      if (card.type() == Card.Type.REPAIR) {
        PlayerActionCard pCard = ((PlayerActionCard) card);
        int targetPlayer = 2;
        Player p = game.playerAt(targetPlayer);
        Tool tool = pCard.effects()[0];
        if (p.isRepairable(tool))
          move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
        break;
      }

      // Example of opening a goal card
      if (card.type() == Card.Type.MAP) {
        Board.GoalPosition target = Board.GoalPosition.TOP;
        move = Move.NewMapMove(myIndex, cardIndex, target);
        // Goal type is stored in this.lastResult
        break;
      }

      // Example of destroying a path
      if (card.type() == Card.Type.ROCKFALL) {
        Set<Position> destroyable = game.board().getDestroyable();
        Position target = destroyable.toArray(new Position[0])[1];
        move = Move.NewRockfallMove(myIndex, cardIndex, target.x, target.y);
        break;
      }

      // Example of discarding a card
      move = Move.NewDiscardMove(myIndex, cardIndex);
    }
    return move;
  }

  /**
   * @param move the played move
   * @see AI#onOtherPlayerMove(Move)
   */
  @Override
  protected void onOtherPlayerMove(Move move) {
    // Implement this to do something when another player moves
  }

  /**
   * @param position  the goal position
   * @param goalType  the opened goal card
   * @param permanent marks if the goal card is opened permanently (a path connects to it)
   * @see GameObserver#onGoalOpen(Board.GoalPosition, GoalType, boolean)
   */
  @Override
  protected void onGoalOpen(Board.GoalPosition position, GoalType goalType, boolean permanent) {
    // Implement this to do something when a goal card is opened

    // This method is called with two conditions:
    // - you played a map card
    // - a path card is placed so that a goal card is reached
  }

  @Override
  public void initialize() {
    // Implement this to initialize the AI when the game starts

    // e.g. set this constant to some value, initialize all predictors
  }
}
