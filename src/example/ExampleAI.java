package example;

import ai.AI;
import model.*;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.ArrayList;
import java.util.Set;

public class ExampleAI extends AI {
  public ExampleAI(String name) {
    super(name);
  }

  private void foo() {
    // role() is used to get your current role (either SABOTEUR/GOLD_MINER)
    Role role = role();

    // history() is used to get the game move history
    History history = history();

    // discarded() is used to see cards you have previously discarded
    ArrayList<Card> discarded = discarded();

    // isSabotaged() is used to check if a player is sabotaged
    boolean sabotaged = isSabotaged();
  }

  @Override
  protected Move makeDecision() {
    // The index is always required to create a move.
    // It is used to identify yourself.
    // Access from calling index()
    int myIndex = index();

    // Prepare move to be returned
    Move move = null;

    // hand() is used to get the card at your hand.
    ArrayList<Card> cards = hand();

    // state() is used to get the game state
    GameLogicController state = state();

    // Iterate through hand
    for (int cardIndex = 0; cardIndex < cards.size(); ++cardIndex) {
      // Get reference of card at hand
      Card card = cards.get(cardIndex);

      // Example of placing a path card
      if (card.type() == Card.Type.PATHWAY) {
        PathCard pCard = ((PathCard) card);
        pCard.rotate();
        Set<Position> placeable = state.board().getPlaceable(pCard);
        Position target = placeable.toArray(new Position[0])[0];
        move = Move.NewPathMove(myIndex, cardIndex, target.x, target.y, true);
        break;
      }

      // Example of blocking another player
      if (card.type() == Card.Type.BLOCK) {
        PlayerActionCard pCard = ((PlayerActionCard) card);
        int targetPlayer = 1;
        Player p = state.playerAt(targetPlayer);
        Tool tool = pCard.effects()[0];
        if (p.isSabotageable(tool))
          move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
        break;
      }

      // Example of repairing another player
      if (card.type() == Card.Type.REPAIR) {
        PlayerActionCard pCard = ((PlayerActionCard) card);
        int targetPlayer = 2;
        Player p = state.playerAt(targetPlayer);
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
        Set<Position> destroyable = state.board().getDestroyable();
        Position target = destroyable.toArray(new Position[0])[1];
        move = Move.NewRockfallMove(myIndex, cardIndex, target.x, target.y);
        break;
      }

      // Example of discarding a card
      move = Move.NewDiscardMove(myIndex, cardIndex);
    }
    return move;
  }
}
