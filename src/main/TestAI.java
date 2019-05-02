package main;

import ai.AI;
import model.Board;
import model.Move;
import model.Player;
import model.Position;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class TestAI extends AI {
  public TestAI(String name) {
    super(name);
  }

  private ArrayList<Move> generatePossiblePaths(int cardIndex, PathCard card) {
    ArrayList<Move> possibleMoves = new ArrayList<>();

    card.setRotated(false);
    Set<Position> posNormal = game().board().getPlaceable(card);
    posNormal.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardIndex, p.x, p.y, false)));

    card.setRotated(true);
    Set<Position> posRotated = game().board().getPlaceable(card);
    posRotated.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardIndex, p.x, p.y, true)));

    // System.out.print(card + " => ");
    // System.out.print(posNormal);
    // System.out.println(posRotated);
    return possibleMoves;
  }

  private ArrayList<Move> generatePossiblePlayerActions(int cardIndex, PlayerActionCard card) {
    ArrayList<Move> possibleMoves = new ArrayList<>();
    int numPlayers = game().numPlayers();
    for (int i = 0; i < numPlayers; ++i) {
      if (i == index()) continue;
      Player p = game().playerAt(i);
      if (card.type() == Card.Type.REPAIR && p.isRepairable(card.effects())) {
        possibleMoves.add(Move.NewPlayerActionMove(index(), cardIndex, i));
      } else if (card.type() == Card.Type.BLOCK && p.isSabotageable(card.effects()[0])) {
        possibleMoves.add(Move.NewPlayerActionMove(index(), cardIndex, i));
      }
    }
    return possibleMoves;
  }

  private ArrayList<Move> generatePossibleRockfall(int cardIndex) {
    ArrayList<Move> possibleMoves = new ArrayList<>();
    Set<Position> positions = game().board().getDestroyable();
    positions.forEach(p -> possibleMoves.add(Move.NewRockfallMove(index(), cardIndex, p.x, p.y)));
    return possibleMoves;
  }

  private ArrayList<Move> generatePossibleMap(int cardIndex) {
    ArrayList<Move> possibleMoves = new ArrayList<>();
    possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.TOP));
    possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.MIDDLE));
    possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.BOTTOM));
    return possibleMoves;
  }

  @Override
  protected Move makeDecision() {
    ArrayList<Move> pathMoves = new ArrayList<>();
    ArrayList<Move> playMoves = new ArrayList<>();
    ArrayList<Move> discardMoves = new ArrayList<>();
    int len = hand().size();
    for (int i = 0; i < len; ++i) {
      Card c = hand().get(i);
      if (c instanceof PathCard && !isSabotaged()) {
        pathMoves.addAll(generatePossiblePaths(i, (PathCard) c));
      }
      if (c instanceof PlayerActionCard) {
        playMoves.addAll(generatePossiblePlayerActions(i, (PlayerActionCard) c));
      }
      if (c.type() == Card.Type.MAP) {
        playMoves.addAll(generatePossibleMap(i));
      }
      if (c.type() == Card.Type.ROCKFALL) {
        playMoves.addAll(generatePossibleRockfall(i));
      }
      discardMoves.add(Move.NewDiscardMove(index(), i));
    }
//    System.out.println(hand());
//    System.out.println(pathMoves);
    pathMoves.addAll(playMoves);
    pathMoves.addAll(discardMoves);
    Collections.shuffle(pathMoves);
    Collections.shuffle(pathMoves);
    Collections.shuffle(pathMoves);
    return pathMoves.get(0);
    // if (new Random().nextInt(10) < 5 && !pathMoves.isEmpty()) {
    //   Collections.shuffle(pathMoves);
    //   return pathMoves.get(0);
    // } else if (new Random().nextInt(10) < 7 && !playMoves.isEmpty()) {
    //   Collections.shuffle(playMoves);
    //   return playMoves.get(0);
    // } else {
    //   Collections.shuffle(discardMoves);
    //   return discardMoves.get(0);
    // }
  }
}
