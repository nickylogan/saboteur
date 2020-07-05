/*
 * Authors:
 * Jasson
 * Peter
 */

package ai.impl.jasson;

import ai.AI;
import model.*;
import model.cards.*;

import java.util.*;


public class Jasson extends AI {
    public Jasson(String name) {
        super(name);
    }

    private void foo() {
        Role role = role();
        History history = history();
        ArrayList<Card> discarded = discarded();
        boolean sabotaged = isSabotaged();
        Map<Board.GoalPosition, GoalType> knownGoals = knownGoals();
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


    //make best decision

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
        if (role() == Role.SABOTEUR) {
            Random randz = new Random();
            int mode = randz.nextInt(2);
            if (mode == 1) {
                Random rand = new Random();
                int yoggMode = rand.nextInt(2);
                if (yoggMode == 0 && !pathMoves.isEmpty()) {
                    Collections.shuffle(pathMoves);
                    return pathMoves.get(0);
                } else if (yoggMode == 1 && !playMoves.isEmpty()) {
                    Collections.shuffle(playMoves);
                    return playMoves.get(0);
                } else {
                    Collections.shuffle(discardMoves);
                    return discardMoves.get(0);
                }
            } else {

                int myIndex = index();
                Move move = null;
                ArrayList<Card> cards = hand();
                GameLogicController game = game();
                for (int cardIndex = 0; cardIndex < cards.size(); ++cardIndex) {
                    Card card = cards.get(cardIndex);
                    if (card.type() == Card.Type.ROCKFALL) {
                        Set<Position> destroyable = game.board().getDestroyable();
                        Position target = destroyable.toArray(new Position[0])[1];
                        move = Move.NewRockfallMove(myIndex, cardIndex, target.x, target.y);
                        break;
                    }
                    if (card.type() == Card.Type.BLOCK) {
                        PlayerActionCard pCard = ((PlayerActionCard) card);
                        int targetPlayer = 1;
                        Player p = game.playerAt(targetPlayer);
                        Tool tool = pCard.effects()[0];
                        if (p.isSabotageable(tool))
                            move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
                        break;
                    }
                    if (card.type() == Card.Type.PATHWAY) {
                        PathCard pCard = ((PathCard) card);
                        pCard.rotate();
                        Set<Position> placeable = game.board().getPlaceable(pCard);
                        Position target = placeable.toArray(new Position[0])[0];
                        move = Move.NewPathMove(myIndex, cardIndex, target.x, target.y, true);
                        break;
                    } else {
                        Random rand = new Random();
                        int yoggMode = rand.nextInt(2);
                        if (yoggMode == 0 && !pathMoves.isEmpty()) {
                            Collections.shuffle(pathMoves);
                            return pathMoves.get(0);
                        } else if (yoggMode == 1 && !playMoves.isEmpty()) {
                            Collections.shuffle(playMoves);
                            return playMoves.get(0);
                        } else {
                            Collections.shuffle(discardMoves);
                            return discardMoves.get(0);
                        }

                    }
                }
            }
        } else {
            Random rand = new Random();
            int yoggMode = rand.nextInt(2);
            if (yoggMode == 0 && !pathMoves.isEmpty()) {
                Collections.shuffle(pathMoves);
                return pathMoves.get(0);
            } else if (yoggMode == 1 && !playMoves.isEmpty()) {
                Collections.shuffle(playMoves);
                return playMoves.get(0);
            } else {
                Collections.shuffle(discardMoves);
                return discardMoves.get(0);
            }
        }
        Random rand = new Random();
        int yoggMode = rand.nextInt(2);
        if (yoggMode == 0 && !pathMoves.isEmpty()) {
            Collections.shuffle(pathMoves);
            return pathMoves.get(0);
        } else if (yoggMode == 1 && !playMoves.isEmpty()) {
            Collections.shuffle(playMoves);
            return playMoves.get(0);
        } else {
            Collections.shuffle(discardMoves);
            return discardMoves.get(0);
        }
    }

}







    //perdicting
