/*
 * Authors:
 * Cen (https://github.com/Bongcen)
 * Gabriel (https://github.com/gabrieldejan17)
 */

package ai.impl.cen;

import ai.AI;
import model.*;
import model.cards.*;

import java.util.ArrayList;
import java.util.Set;

public class botGDCN extends AI {

    boolean isMiner;
    int[] goalData = {0,0,0}; // 0 Unknown, -1 Rock, 1 Gold (Top, Mid, Bot)
    Heuristic hc;

    ArrayList<Card> currHand = new ArrayList<>();
    rPredictr rp;

    ArrayList<mHeuristic> cardsHeu = new ArrayList<>();



    GameLogicController game;

    public botGDCN(String name) {
        super(name);
    }

    @Override
    protected Move makeDecision() {
        //Updates the rp.potFriends and rp.potFoes
        rp.updateListOfRoles();
        calcHandHeu();

        Move m;


       m = hc.maxHeu(cardsHeu).m;
       if(m == null) {
       //    System.out.println("Move empty!!!");
           return Move.NewDiscardMove(index(), 0);
       } else {
           return m;
       }

    }

    private void calcHandHeu(){
        ArrayList<Move> tempMoves;
        currHand = hand();
        cardsHeu.clear();

       // System.out.print("List of Cards: ");

        for (int cardIndex = 0; cardIndex < currHand.size(); ++cardIndex) {

            tempMoves = new ArrayList<>();

            Card c = currHand.get(cardIndex);
            if(c instanceof PathCard && !isSabotaged()) {
              //  System.out.print("Path ");
                //Check Possible Card Location
                tempMoves.addAll(generatePossiblePaths(cardIndex,(PathCard) c));

                // Only If card can be placed
                if(!tempMoves.isEmpty()) {
//                    cardsHeu.add(hc.calcHeuCardPath((PathCard)c, tempMoves, isMiner));
                    cardsHeu.add(hc.calcHeuCardPathNEW((PathCard)c, tempMoves, isMiner, goalData));
                }

            }

            if(c instanceof PlayerActionCard && c.type() == Card.Type.BLOCK) {
              //  System.out.print("Block ");

                tempMoves.addAll(generatePossibleBlocks(cardIndex, (PlayerActionCard) c));

                // Only If card can be placed
                if(!tempMoves.isEmpty()) {
                    mHeuristic temp = hc.calcHeuCardBlock(cardIndex, tempMoves, game(), rp.potFoes);
                    if(temp.m != null) {
                        cardsHeu.add(temp);
                    }
                }
            }

            if(c instanceof PlayerActionCard && c.type() == Card.Type.REPAIR) {
              //  System.out.print("Repair ");

                mHeuristic selfRepair = useRepairToSelf(cardIndex, (PlayerActionCard) c);

                if(selfRepair.m != null) {
                    cardsHeu.add(selfRepair);
                } else {
                    tempMoves.addAll(generatePossibleRepairs(cardIndex,(PlayerActionCard) c));
                    // Only If card can be placed
                    if(!tempMoves.isEmpty()) {
                        mHeuristic temp = hc.calcHeuCardRepair(cardIndex, tempMoves, game(), rp.potFriends);
                        if(temp.m != null) {
                            cardsHeu.add(temp);
                        }
                    }
                }

            }

            if(c.type() == Card.Type.MAP) {
               // System.out.print("Map ");
                cardsHeu.add(hc.calcHeuCardMap(index(),cardIndex, goalData));

            }

            if(c.type() == Card.Type.ROCKFALL) {
              //  System.out.print("Rockfall ");

                Board board = game().board();

                tempMoves.addAll(generatePossibleRockfall(cardIndex,game().board()));
                if(!tempMoves.isEmpty()) {
                    cardsHeu.add(hc.calcHeuCardRockFall(tempMoves, isMiner, board, goalData));
                }

            }

            if(c.type() == Card.Type.DEADEND && !isSabotaged()){
              //  discarded(Card.Type.DEADEND);
                Move.NewDiscardMove(index(), cardIndex);

            }

        }
       // System.out.println("");
       // System.out.println("CardsHeu size() = " + cardsHeu.size());

        if(!cardsHeu.isEmpty()) {
            for (mHeuristic temp: cardsHeu) {

                Move x = temp.m;

                int index = x.handIndex();
                Card card = currHand.get(index);
                Card.Type tipe = card.type();

//            currHand.get(temp.m.handIndex()).type()
//                System.out.println("Card Index: "+tipe+" ("+temp.heu+")");
             //   System.out.print("Card "+tipe+": ("+temp.heu +") ");

                if(tipe == Card.Type.PATHWAY || tipe == Card.Type.DEADEND) {
             //       System.out.println("at "+x.args()[0]+","+x.args()[1]+" rotated="+x.args()[2]);
                } else if(tipe == Card.Type.MAP && x.type() == Move.Type.DISCARD) {
             //       System.out.println("discarded" );
                } else if(tipe == Card.Type.MAP) {
            //        System.out.println("to "+x.args()[0]);
                } else if(tipe == Card.Type.ROCKFALL) {
            //        System.out.println("at "+x.args()[0]+","+x.args()[1]);
                }  else if(tipe == Card.Type.BLOCK) {
             //       System.out.println("to Player "+x.args()[0]);
                }  else if(tipe == Card.Type.REPAIR) {
            //        System.out.println("to Player "+x.args()[0]);
                }
            }

        }

     //   System.out.println("");
    }

    private mHeuristic useRepairToSelf(int cardIndex, PlayerActionCard card) {
        Player me = game().playerAt(index());

        if (card.type() == Card.Type.REPAIR && me.isRepairable(card.effects())) {
            Move m = Move.NewPlayerActionMove(index(), cardIndex, index());
            return new mHeuristic(m,10.0f);
        }

        return new mHeuristic(null,0.0f);
    }

    private ArrayList<Move> generatePossiblePaths(int cardIndex, PathCard card) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        card.setRotated(false);
        Set<Position> posNormal = game().board().getPlaceable(card);
        posNormal.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardIndex, p.x, p.y, false)));

        card.setRotated(true);
        Set<Position> posRotated = game().board().getPlaceable(card);
        posRotated.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardIndex, p.x, p.y, true)));

        return possibleMoves;
    }

    private ArrayList<Move> generatePossiblePaths(int cardIndex, PathCard card, Board oldBoard) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        card.setRotated(false);
        Set<Position> posNormal = oldBoard.getPlaceable(card);
        posNormal.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardIndex, p.x, p.y, false)));

        card.setRotated(true);
        Set<Position> posRotated = oldBoard.getPlaceable(card);
        posRotated.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardIndex, p.x, p.y, true)));

        return possibleMoves;
    }

    private ArrayList<Move> generatePossibleRockfall(int cardIndex, Board board) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        Set<Position> positions = board.getDestroyable();
        positions.forEach(p -> possibleMoves.add(Move.NewRockfallMove(index(), cardIndex, p.x, p.y)));
        return possibleMoves;
    }

    private ArrayList<Move> generatePossibleBlocks(int cardIndex, PlayerActionCard card) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int numPlayers = game().numPlayers();

        for (int i = 0; i < numPlayers; ++i) {
            if (i == index()) continue;
            Player p = game().playerAt(i);

            if (card.type() == Card.Type.BLOCK && p.isSabotageable(card.effects()[0])) {
                possibleMoves.add(Move.NewPlayerActionMove(index(), cardIndex, i));
            }
        }
        return possibleMoves;
    }

    private ArrayList<Move> generatePossibleRepairs(int cardIndex, PlayerActionCard card) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int numPlayers = game().numPlayers();

        for (int i = 0; i < numPlayers; ++i) {
            if (i == index()) continue;
            Player p = game().playerAt(i);

            if (card.type() == Card.Type.REPAIR && p.isRepairable(card.effects())) {
                possibleMoves.add(Move.NewPlayerActionMove(index(), cardIndex, i));
            }
        }
        return possibleMoves;
    }

    protected void onOtherPlayerMove(Move move) {
        ArrayList<Float> heus;

        /************
         PATHCARD
         ************/

        if(move.type() == Move.Type.PLAY_PATH) {
       //     System.out.println("Player " + move.playerIndex() + " PATH");

            float z1, z2;
            int x,y,rotate;

            ArrayList<Move> m = new ArrayList<>();
            BoardDelta bd = (BoardDelta) move.delta();

            //Generate Move berdasarkan kondisi tadinya
            m.addAll(generatePossiblePaths(move.handIndex(),(PathCard) move.card(), bd.boardBefore()));

            //Find bestMove
            //mHeuristic bestMove = hc.calcHeuCardPath((PathCard) move.card(), m, isMiner);
            mHeuristic bestMove = hc.calcHeuCardPathNEW((PathCard) move.card(), m, isMiner, goalData);

            //Get all possible heus
            heus = hc.calcHeuCardPathFloatsNEW((PathCard) move.card(), m, isMiner, goalData);

        //    System.out.print("Heus: ");
            for (Float f: heus) {
                System.out.print(f+" ");
            }
         //   System.out.println("");

            x = bestMove.m.args()[0];
            y = bestMove.m.args()[1];
            rotate = bestMove.m.args()[2];

            z1 = bestMove.heu;
       //     System.out.println("Best Possible = "+z1 +" at ("+x+","+y+") rotate="+rotate);

            z2 = hc.calcHeuCellPathFloatsNEW((PathCard) move.card(), move, isMiner, goalData);
        //    System.out.println("Actual = "+z2 +" at ("+move.args()[0]+","+move.args()[1]+") rotate="+move.args()[2]);

            rp.updatePrediction(z2,heus,move.playerIndex());
        }

        /************
         ROCKFALL
         ************/
        if(move.type() == Move.Type.PLAY_ROCKFALL) {
       //     System.out.println("Player " + move.playerIndex() + " ROCKFALL");

            float z1, z2;
            int x,y;

            ArrayList<Move> m = new ArrayList<>();
            BoardDelta bd = (BoardDelta) move.delta();

            //Generate Move berdasarkan kondisi tadinya
            m.addAll(generatePossibleRockfall(move.handIndex(), bd.boardBefore()));

            //Find bestMove
            mHeuristic bestMove = hc.calcHeuCardRockFall(m,isMiner,bd.boardBefore(),goalData);

            //Get all possible heus
            heus = hc.calcHeuCardRockFallFloats(m, isMiner,bd.boardBefore(),goalData);


          //  System.out.print("Heus: ");
            for (Float f: heus) {
       //         System.out.print(f+" ");
            }
       //     System.out.println("");

            x = bestMove.m.args()[0];
            y = bestMove.m.args()[1];

            z1 = bestMove.heu;
       //     System.out.println("Best Possible = "+z1 +" at ("+x+","+y+")");

            z2 = hc.calcHeuCellRockFall(move, isMiner, bd.boardBefore(), goalData);
       //     System.out.println("Actual = "+z2 +" at ("+move.args()[0]+","+move.args()[1]+")");

            rp.updatePrediction(z2,heus,move.playerIndex());

        }

        if(move.type() == Move.Type.PLAY_PLAYER) {
//            Player p = game().playerAt(move.args()[0]);

            /***********
             BLOCK
             ***********/
            if(move.card().type() == Card.Type.BLOCK) {
        //        System.out.println("Player " + move.playerIndex() + " BLOCK");

                rp.updatePredictionBlock(move, index());

            /************
             REPAIR
             ************/
            } else if(move.card().type() == Card.Type.REPAIR) {
        //        System.out.println("Player " + move.playerIndex() + " REPAIR");

                rp.updatePredictionRepair(move, index());
            }
        }

        if(move.type() == Move.Type.PLAY_MAP) {
     //       System.out.println("Player " + move.playerIndex() + " MAP to " + move.args()[0]);
        }

        if(move.type() == Move.Type.DISCARD) {
      //      System.out.println("Player " + move.playerIndex() + " DISCARD");
        }
      //  System.out.println("");
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
//        System.out.println("ONGOALOPEN");

        if(goalType == GoalType.GOLD) {
            if(position == Board.GoalPosition.TOP) {
                goalData[0] = 1;
                goalData[1] = -1;
                goalData[2] = -1;
            }
            if(position == Board.GoalPosition.MIDDLE) {
                goalData[0] = -1;
                goalData[1] = 1;
                goalData[2] = -1;
            }
            if(position == Board.GoalPosition.BOTTOM) {
                goalData[0] = -1;
                goalData[1] = -1;
                goalData[2] = 1;
            }
        } else {
            if(position == Board.GoalPosition.TOP) {
                goalData[0] = -1;
            }
            if(position == Board.GoalPosition.MIDDLE) {
                goalData[1] = -1;
            }
            if(position == Board.GoalPosition.BOTTOM) {
                goalData[2] = -1;
            }
        }

        if(goalData[0] == -1 && goalData[1] == -1) goalData[2] = 1;
        if(goalData[1] == -1 && goalData[2] == -1) goalData[0] = 1;
        if(goalData[2] == -1 && goalData[0] == -1) goalData[1] = 1;
    }

    @Override
    public void initialize() {
        // Implement this to initialize the AI when the game starts
        // e.g. set this constant to some value, initialize all predictors
        if(role() == Role.GOLD_MINER) {
            isMiner = true;
        } else {
            isMiner = false;
        }

        hc = new Heuristic(5, 50,3,7,8,2,25,30,5,5);

        rp = new rPredictr(index(), game().numPlayers(), game().numSaboteurs(),2,1,0.5f,0.75f,isMiner);

        game = game();
    }
}
