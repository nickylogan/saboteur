/*
 * Authors:
 * Andreas
 * Thompson
 */

package customAI.tom;
//This edit pls

import ai.AI;
import model.*;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.*;

@SuppressWarnings("ALL")
public class AndreTomAI extends AI {
    public static int playAsMiner = 0;
    public static int playAsSaboteur = 0;
    public static int winAsMiner = 0;
    public static int winAsSaboteur = 0;

    Board.GoalPosition goldPosition;
    Boolean searchedTop = false;
    Boolean searchedMiddle = false;
    Boolean searchedBottom = false;
    Boolean goldFound = false;
    Random rand = new Random();

    ArrayList<PathCard.Type> topOptimalCardTypesUnrotated = new ArrayList<>();
    ArrayList<PathCard.Type> topOptimalCardTypesRotated = new ArrayList<>();

    ArrayList<PathCard.Type> middleOptimalCardTypesUnrotated = new ArrayList<>();
    ArrayList<PathCard.Type> middleOptimalCardTypesRotated = new ArrayList<>();

    ArrayList<PathCard.Type> bottomOptimalCardTypesUnrotated = new ArrayList<>();
    ArrayList<PathCard.Type> bottomOptimalCardTypesRotated = new ArrayList<>();

    public AndreTomAI(String name) {
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
        // System.out.println("myindex = " + myIndex);
        // Prepare move to be returned
        Move move = null;

        // hand() is used to get all cards currently in your hand
        ArrayList<Card> cards = hand();

        // game() is used to get the game state
        GameLogicController game = game();

        //List [baru]
        ArrayList<Move> possiblePathMoves = new ArrayList<>();
        // Iterate through hand
        for (int cardIndex = 0; cardIndex < cards.size(); ++cardIndex) {
            // Get reference of card at hand
            Card card = cards.get(cardIndex);

            //Place a path card
            if (card.type() == Card.Type.PATHWAY) {
                PathCard pCard = ((PathCard) card);

                possiblePathMoves.add(GenerateCardBestMove(myIndex, cardIndex, pCard));//add a path move in the array
            }

            // When the AI gets a goal card
            //It will check from the top > middle > bottom
            //if the goal is already found, it will discard the current map card.
            if (card.type() == Card.Type.MAP) {
                if (searchedTop == false && goldFound == false) {
                    Board.GoalPosition target = Board.GoalPosition.TOP;
                    searchedTop = true;
                    move = Move.NewMapMove(myIndex, cardIndex, target);
                    // System.out.println("searched top");
                } else if (searchedMiddle == false && goldFound == false) {
                    Board.GoalPosition target = Board.GoalPosition.MIDDLE;
                    searchedMiddle = true;
                    move = Move.NewMapMove(myIndex, cardIndex, target);
                    // System.out.println("searched middle");
                } else if (searchedBottom == false && goldFound == false) {
                    Board.GoalPosition target = Board.GoalPosition.BOTTOM;
                    searchedBottom = true;
                    move = Move.NewMapMove(myIndex, cardIndex, target);
                    // System.out.println("searched bottom");
                } else if (goldFound == true) {
                    move = move.NewDiscardMove(myIndex, cardIndex);
                    // System.out.println("Map card discarded because gold already found");
                }
                // Goal type is stored in this.lastResult
                break;
            }
            // Blocking another player
            if (card.type() == Card.Type.BLOCK) {
                PlayerActionCard pCard = ((PlayerActionCard) card);
                //randomizes the block to block a random player except itself
                int targetPlayer = getRandomWithExclusion(rand, 0, game.numPlayers() - 1, myIndex);
                Player p = game.playerAt(targetPlayer);
                Tool tool = pCard.effects()[0];
                if (p.isSabotageable(tool))
                    move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
                // System.out.println("i call block!");
                break;
            }
            // Repairing another player
            if (card.type() == Card.Type.REPAIR) {
                PlayerActionCard pCard = ((PlayerActionCard) card);

                int targetPlayer;
                if (isSabotaged()) {//if the current player is broken then fix self first
                    targetPlayer = myIndex;
                } else {
                    //randomizes the repair to a random player except itself
                    targetPlayer = getRandomWithExclusion(rand, 0, game.numPlayers() - 1, myIndex);
                }
                Player p = game.playerAt(targetPlayer);
                Tool tool = pCard.effects()[0];
                if (p.isRepairable(tool))
                    move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
                break;

            }

            // Rockfall Functions
            /*if (card.type() == Card.Type.ROCKFALL) {
                Set<Position> destroyable = game.board().getDestroyable();
                Position temporaryDestroyable;
                Position bestDestroyable = null; //best destroyable position in the board

                if (role().equals(Role.SABOTEUR)) {
                    //CHECK DESTROYABLE X position if it is higher then destroy for saboteur
                    bestDestroyable = DestroyMostRightPathway(destroyable, bestDestroyable);
                }
                if (role().equals(Role.GOLD_MINER)) {
                    bestDestroyable = FindMostRightDeadEnd(game, destroyable, bestDestroyable);
                    if (bestDestroyable == null) { //if no dead end found in the most right then destroy the current rockfall card
                        System.out.println("no deadends found -- revert to destroy card instead");
                        move = Move.NewDiscardMove(myIndex, cardIndex); //destroy current rockfall card (if no dead end found as miner)
                        break;
                    }
                }
                System.out.println("final best destroyable: " + bestDestroyable);
                System.out.println("---destroyable AFTER: " + destroyable);
                move = Move.NewRockfallMove(myIndex, cardIndex, bestDestroyable.x, bestDestroyable.y);
                break;
            }*/

/*            // Example of placing a path card
            if (card.type() == Card.Type.PATHWAY) {
                PathCard pCard = ((PathCard) card);
                pCard.rotate();
                Set<Position> placeable = game.board().getPlaceable(pCard);
                Position target = placeable.toArray(new Position[0])[0];
                System.out.println("targetPathway: "+target);
                move = Move.NewPathMove(myIndex, cardIndex, target.x, target.y, true);
                System.out.println("move it move it!");
                break;
            }

            // Example of discarding a card
            move = Move.NewDiscardMove(myIndex, cardIndex);*/


        }

        if (possiblePathMoves.size() > 1) {//if the array of possible path moves contains more than one moves then
            // System.out.println("Size moves: " + possiblePathMoves.size());
            Move decidedMove = GeneratePathMoveDecision(possiblePathMoves);
            return decidedMove;
            /*if (decidedMove == null) {
                //System.out.println("karena null decided move nya, jadinya kita discard handIndex:1");
//                return null;
                //return Move.NewDiscardMove(myIndex, 1);*/
        }
        else if(possiblePathMoves.size() == 1){
            // System.out.println("The only best move : " + possiblePathMoves.get(0));
            return possiblePathMoves.get(0);
            //return decidedMove;
        }

        // System.out.println("movenya: " + move);
        if (move == null) {//Kalau movenya null daripada ngulang yang sebelumnya, paksa aja utk remove kartu yang pertama
            // System.out.println("Paksa discard index 2");
            //return null;

            //DISCARD sstuff -james
            if (role().equals(Role.SABOTEUR)) {
                for (Card temp : cards) {
                    // System.out.println("curent card id: " + temp.id());
                    if (temp.type().equals(Card.Type.PATHWAY)) {
                        move = Move.NewDiscardMove(myIndex, temp.id());
                        // System.out.println("saboteur discard card pathway id " + temp.id());
                    }
                }
            }
            if (role().equals(Role.GOLD_MINER)) {
                for (Card temp : cards) {
                    // System.out.println("curent card id: " + temp.id());
                    if (temp.type().equals(Card.Type.DEADEND)) {
                        move = Move.NewDiscardMove(myIndex, temp.id());
                        // System.out.println("gold miner discard deadend card");
                    }

                }


            }

            // //-----
            //
            // //search card on deck to discard
            // for (int cardIndex = 0; cardIndex < cards.size(); ++cardIndex) {
            //     // Get reference of card at hand
            //     Card card = cards.get(cardIndex);
            //
            //     //Discard useless for miners
            //     if (role().equals(Role.GOLD_MINER)){
            //         if (card.type() == Card.Type.DEADEND){
            //             return Move.NewDiscardMove(myIndex, cardIndex);
            //         }
            //     }
            //
            //     //Discard useless for saboteur
            //     else {
            //         if (card.type() == Card.Type.PATHWAY) {
            //             if (((PathCard) card).pathType().equals(PathCard.Type.HORIZONTAL_PATH)||
            //                     ((PathCard) card).pathType().equals(PathCard.Type.HORIZONTAL_T_PATH)||
            //                     ((PathCard) card).pathType().equals(PathCard.Type.CROSSROAD_PATH)||
            //                     ((PathCard) card).pathType().equals(PathCard.Type.RIGHT_TURN_PATH)||
            //                     ((PathCard) card).pathType().equals(PathCard.Type.LEFT_TURN_PATH)||
            //                     ((PathCard) card).pathType().equals(PathCard.Type.VERTICAL_PATH)||
            //                     ((PathCard) card).pathType().equals(PathCard.Type.VERTICAL_T_PATH)
            //             ){
            //                 return Move.NewDiscardMove(myIndex, cardIndex);
            //             }
            //         }
            //     }
            //
            //
            // }
    
            //move = Move.NewDiscardMove(myIndex, 2);
        }

        return move; //return nya ga boleh null, kalau null dia jadi ngulang yang tadi
        //return move;//yang ori
    }

    private Position FindMostRightDeadEnd(GameLogicController game, Set<Position> destroyable, Position bestDestroyable) {
        Position temporaryDestroyable;
        for (Position p : destroyable) {
            // System.out.println(game.board().cellAt(p).card().type().equals(Card.Type.DEADEND) + "----------- x nya: " + p.x + "y nya: " + p.y);
            if (game.board().cellAt(p).card().type().equals(Card.Type.DEADEND)) {
                if (bestDestroyable == null) {
                    bestDestroyable = p;
                } else {
                    temporaryDestroyable = p;

                    if (temporaryDestroyable.x > bestDestroyable.x) { //if the X is higher (closer to the right edge then
                        bestDestroyable = temporaryDestroyable;
                    }
                }
                // System.out.println("DeadEND Card FOund and set to bestDestroyable: ----------- x nya: " + p.x + "y nya: " + p.y);
            }
        }
        return bestDestroyable;
    }

    private Position DestroyMostRightPathway(Set<Position> destroyable, Position bestDestroyable) {
        Position temporaryDestroyable;
        // System.out.println("Destroying right most PATHWAY");
        for (Position p : destroyable) {
            // System.out.println("x nya: " + p.x);
            // System.out.println("y nya: " + p.y);
            if (bestDestroyable == null) {
                bestDestroyable = p;
            } else {
                temporaryDestroyable = p;

                if (temporaryDestroyable.x > bestDestroyable.x) { //if the X is higher (closer to the right edge then
                    bestDestroyable = temporaryDestroyable;
                }
            }
        }
        // System.out.println("final best destroyable: " + bestDestroyable);
        return bestDestroyable;
    }

    private Move GenerateCardBestMove(int myIndex, int cardIndex, PathCard card) {

        //Best move
        Move unrotated = null;
        Move rotated = null;

        //UN ROTATED
        card.setRotated(false);
        Set<Position> posNormal = game().board().getPlaceable(card);
        Set<Position> posProcessed = new HashSet<>();

        /*if (posNormal.isEmpty()) {
            System.out.println("posnormal empty");
            return null;
        }*/

        ArrayList<PathCard.Type> unrotatedOptimalCards = new ArrayList<>();
        ArrayList<PathCard.Type> rotatedOptimalCards = new ArrayList<>();;

        if(goldPosition.equals(Board.GoalPosition.TOP)){
            unrotatedOptimalCards = topOptimalCardTypesUnrotated;
            rotatedOptimalCards = topOptimalCardTypesRotated;
        }else if(goldPosition.equals(Board.GoalPosition.MIDDLE)){
            unrotatedOptimalCards = middleOptimalCardTypesUnrotated;
            rotatedOptimalCards = middleOptimalCardTypesRotated;
        }else if(goldPosition.equals(Board.GoalPosition.BOTTOM)){
            unrotatedOptimalCards = bottomOptimalCardTypesUnrotated;
            unrotatedOptimalCards = bottomOptimalCardTypesRotated;
        }

        if (unrotatedOptimalCards.contains(card.pathType())) {
            //check normal positions
            for (Position p : posNormal) {
                if(goldPosition.equals(Board.GoalPosition.TOP)){
                    if (card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 0 ||
                            card.pathType().equals(PathCard.Type.VERTICAL_PATH) && p.y == 0) {
                        //Don't add
                        // System.out.println("1 - Not adding Vertical T or Vertical Path");
                    } else if (card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH) && p.x == 8 && p.y == 1) {//if at (8,0) [right corner] and card is horizontal
                        //Don't add
                        // System.out.println("2 - Not adding Horizontal T Path - if at (8,0) [right corner] and card is horizontal");
                    } else if (p.x == 7 && p.y == 0 && !card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) &&
                            !card.pathType().equals(PathCard.Type.VERTICAL_PATH)) {//if at (7,0) [top right corner before target]  card isnt vertical T or vertical line then
                        // System.out.println("3 - if at (7,0) [top right corner before target]  card isnt vertical T or vertical line then");
                        Move.NewPathMove(myIndex, cardIndex, p.x, p.y, false);
                    } else if (p.x == 8 && p.y == 1 && !card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH)
                            && card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH)) {
                        // System.out.println("4 - ");
                        Move.NewPathMove(myIndex, cardIndex, p.x, p.y, true);//rotate to true so that it aims to the top one
                    } else if (p.y <= 2) {
                        // System.out.println("5 - y is less than 2");
                        posProcessed.add(p);
                    }
                }
                else if(goldPosition.equals(Board.GoalPosition.MIDDLE)){
                    if((card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 0)
                            || (card.pathType().equals(PathCard.Type.VERTICAL_PATH) && p.y == 0)
                            || (card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 4)
                            || (card.pathType().equals(PathCard.Type.VERTICAL_PATH) && p.y == 4)){
                        // Don't add
                    }
                    else if(p.x == 8 &&p.y == 1 &&
                            (card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH)
                                    || card.pathType().equals(PathCard.Type.HORIZONTAL_PATH))){
                        // Don't add
                    }
                    else if(p.x == 8 &&p.y == 3 &&
                            (card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH)
                                    || card.pathType().equals(PathCard.Type.HORIZONTAL_PATH))){
                        // Don't add
                    }
                    else if(card.pathType().equals(PathCard.Type.VERTICAL_PATH) && p.y == 2
                            || card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 2
                            || card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.y == 2
                            || card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.y == 2){
                        // Don't add
                    }
                    else if(card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.y == 4){
                        // Don't add
                    }
                    else if(p.x == 7 && (p.y == 0 || p.y == 4 || p.y == 2)
                            && (card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH)
                            || card.pathType().equals(PathCard.Type.HORIZONTAL_PATH))){
                        // Spesifik sudah membuka jalur ke goal
                        return Move.NewPathMove(myIndex, cardIndex, p.x, p.y, false);
                    }
                    else if(p.x == 8 && (p.y == 1 || p.y == 3)
                            && card.pathType().equals(PathCard.Type.VERTICAL_T_PATH)){
                        // Spesifik sudah membuka jalur ke goal
                        return Move.NewPathMove(myIndex, cardIndex, p.x, p.y, false);
                    }
                    else if(p.y < 3 && p.y > 1){
                        posProcessed.add(p);
                    }
                }
                else if(goldPosition.equals(Board.GoalPosition.BOTTOM)){
                    if(card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 4 ||
                            card.pathType().equals(PathCard.Type.VERTICAL_PATH) && p.y == 4 ||
                            card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.y == 4){
                        //Don't add
                    }
                    else if(p.y >= 2){
                        // System.out.println("5");
                        posProcessed.add(p);
                    }
                }
            }
        }

        Position temporaryPosition;
        Position bestPosition = null;

        //process position to get the best positions
        for (Position p : posProcessed) {
            if (bestPosition == null) {
                bestPosition = p;
            } else {
                temporaryPosition = p;

                if(goldPosition.equals(Board.GoalPosition.TOP)){
                    if (temporaryPosition.x > bestPosition.x && //x is further to the right (closer to the target)
                            temporaryPosition.y < bestPosition.y) { //y is smaller (closer to top edge)
                        bestPosition = temporaryPosition; //set best position to the temp one
                    }
                }
                else if(goldPosition.equals(Board.GoalPosition.MIDDLE)){
                    int temporaryDistanceToMiddle;
                    int selectedDistanceToMiddle;

                    if(temporaryPosition.y < 2){
                        temporaryDistanceToMiddle = 2 - temporaryPosition.y;
                    }
                    else if(temporaryPosition.y > 2){
                        temporaryDistanceToMiddle = temporaryPosition.y - 2;
                    }
                    else {
                        temporaryDistanceToMiddle = 0;
                    }

                    if(bestPosition.y < 2){
                        selectedDistanceToMiddle = 2 - bestPosition.y;
                    }
                    else if(bestPosition.y > 2){
                        selectedDistanceToMiddle = bestPosition.y - 2;
                    }
                    else {
                        selectedDistanceToMiddle = 0;
                    }

                    if(temporaryPosition.x > bestPosition.x && temporaryDistanceToMiddle < selectedDistanceToMiddle){
                        bestPosition = temporaryPosition;
                    }
                }
                else if(goldPosition.equals(Board.GoalPosition.BOTTOM)){
                    if(temporaryPosition.x > bestPosition.x && temporaryPosition.y > bestPosition.y){
                        bestPosition = temporaryPosition;
                    }
                }
            }
        }

        if(bestPosition != null){
            unrotated = Move.NewPathMove(myIndex, cardIndex, bestPosition.x, bestPosition.y, false);
        }

        //ROTATED CARD
        card.setRotated(true);
        Set<Position> posRotated = game().board().getPlaceable(card);

        if (rotatedOptimalCards.contains(card.pathType())) {
            //check rotated positions
            for (Position p : posRotated) {
                if(goldPosition.equals(Board.GoalPosition.TOP)){
                    if (card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.y == 0 || card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 0) {
                        //do nothing cuz they suck
                        // System.out.println("Kartu sesat:  card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.y == 0 || card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 0");
                    } else if (p.y < 2) {
                        posProcessed.add(p);
                    }
                }
                else if(goldPosition.equals(Board.GoalPosition.MIDDLE)){
                    if(card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.y == 0){
                        // Don't add
                    }
                    else if(card.pathType().equals(PathCard.Type.VERTICAL_PATH) && p.y == 2
                            || card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 2
                            || card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.y == 2
                            || card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.y == 2){
                        // Don't add
                    }
                    else {
                        posProcessed.add(p);
                    }
                }
                else if(goldPosition.equals(Board.GoalPosition.BOTTOM)){
                    if(card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.x == 7 && p.y == 4 ||
                            card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.x == 7 && p.y == 4){
                        return Move.NewPathMove(myIndex, cardIndex, p.x, p.y, true);
                    }
                    else if(card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 4){
                        //Dont add
                    }
                    else if(p.y >= 2){
                        posProcessed.add(p);
                    }
                }
            }
        }

        bestPosition = null;
        posProcessed = new HashSet<>();

        //process position to get the best positions
        for (Position p : posProcessed) {
            if (bestPosition == null) {
                bestPosition = p;
            } else {
                temporaryPosition = p;

                if(goldPosition.equals(Board.GoalPosition.TOP)){
                    if (temporaryPosition.x > bestPosition.x && //x is further to the right (closer to the target)
                            temporaryPosition.y < bestPosition.y) { //y is smaller (closer to top edge)
                        bestPosition = temporaryPosition; //set best position to the temp one
                    }
                }
                else if(goldPosition.equals(Board.GoalPosition.MIDDLE)){
                    int temporaryDistanceToMiddle;
                    int selectedDistanceToMiddle;

                    if(temporaryPosition.y < 2){
                        temporaryDistanceToMiddle = 2 - temporaryPosition.y;
                    }
                    else if(temporaryPosition.y > 2){
                        temporaryDistanceToMiddle = temporaryPosition.y - 2;
                    }
                    else {
                        temporaryDistanceToMiddle = 0;
                    }

                    if(bestPosition.y < 2){
                        selectedDistanceToMiddle = 2 - bestPosition.y;
                    }
                    else if(bestPosition.y > 2){
                        selectedDistanceToMiddle = bestPosition.y - 2;
                    }
                    else {
                        selectedDistanceToMiddle = 0;
                    }

                    if(temporaryPosition.x > bestPosition.x && temporaryDistanceToMiddle < selectedDistanceToMiddle){
                        bestPosition = temporaryPosition;
                    }
                }
                else if(goldPosition.equals(Board.GoalPosition.BOTTOM)){
                    if(temporaryPosition.x > bestPosition.x && temporaryPosition.y > bestPosition.y){
                        bestPosition = temporaryPosition;
                    }
                }
            }
        }

        if (bestPosition != null) {
            rotated = Move.NewPathMove(myIndex, cardIndex, bestPosition.x, bestPosition.y, true);
        }

        if(unrotated == null && rotated != null){
            return rotated;
        }
        else if(unrotated != null && rotated == null){
            return unrotated;
        }
        else{
            // System.out.println("Best move akhirnya null for card : " + card + "[" + cardIndex + "]");
            return null;
        }
    }

    private Move GeneratePathMoveDecision(ArrayList<Move> possibleMoves) {

        //Generates best to the top right position
        Move temporaryMove;
        Move bestMove = null; //set best move to null first

        for (Move m : possibleMoves) {
            if (bestMove != null) {
                temporaryMove = m;

                if(goldPosition.equals(Board.GoalPosition.TOP)){
                    int[] temporaryXY = temporaryMove.args();//returns the card
                    int[] bestXY = bestMove.args();//returns the card

                    if (temporaryXY[0] > bestXY[0] && //x lebih jauh &&
                            temporaryXY[1] < bestXY[1]) { //y lebih kecil (posisi lebih tinggi
                        bestMove = temporaryMove;
                    }
                }
                else if(goldPosition.equals(Board.GoalPosition.MIDDLE)){
                    int[] temporaryXY = temporaryMove.args();
                    int[] selectedXY = bestMove.args();

                    int temporaryXYDistanceToMiddle;
                    int selectedXYDistanceToMiddle;

                    if(temporaryXY[1] < 2){
                        temporaryXYDistanceToMiddle = 2 - temporaryXY[1];
                    }
                    else if(temporaryXY[1] > 2){
                        temporaryXYDistanceToMiddle = temporaryXY[1] - 2;
                    }
                    else {
                        temporaryXYDistanceToMiddle = 0;
                    }

                    if(selectedXY[1] < 2){
                        selectedXYDistanceToMiddle = 2 - selectedXY[1];
                    }
                    else if(selectedXY[1] > 2){
                        selectedXYDistanceToMiddle = selectedXY[1] - 2;
                    }
                    else {
                        selectedXYDistanceToMiddle = 0;
                    }

                    if(temporaryXY[0] > selectedXY[0] && temporaryXYDistanceToMiddle < selectedXYDistanceToMiddle){
                        bestMove = temporaryMove;
                    }
                }
                else if(goldPosition.equals(Board.GoalPosition.BOTTOM)){
                    int[] temporaryXY = temporaryMove.args();
                    int[] selectedXY = bestMove.args();

                    if(temporaryXY[0] > selectedXY[0] && temporaryXY[1] > selectedXY[1]){
                        bestMove = temporaryMove;
                    }
                }
            } else {
                bestMove = m;
            }
        }

        if(bestMove == null){
            bestMove = possibleMoves.get(0);
        }
        // System.out.println("best move terakhir: " + bestMove);

        return bestMove;

    }

    /**
     * @param move the played move
     * @see AI#onOtherPlayerMove(Move)
     */
    @Override
    protected void onOtherPlayerMove(Move move) {
        // Implement this to do something when another player moves
        //checkStatusJames();
        //System.out.println(jamesGoldPosition);
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
        // System.out.println("myindex: " + index() + "--figured out goal position: " + position + " goaltype: " + goalType);

        if (goalType.equals(GoalType.GOLD)) {
            // System.out.println("ini gold");
            goldPosition = position;
            goldFound = true;
        }
        if (goalType.equals(GoalType.ROCK)) {
            // System.out.println("ini sampah");
        }
        if (searchedTop == true && searchedMiddle == true && goldFound == false) {
            //If the first and second targets are searched and gold is not found yet then
            //set the gold to be in the bottom and then set the boolean to true
            goldPosition = Board.GoalPosition.BOTTOM;
            goldFound = true;
        }

        // This method is called with two conditions:
        // - you played a map card
        // - a path card is placed so that a goal card is reached
    }

    @Override
    public void initialize() {
        // Implement this to initialize the AI when the game starts
        init();
        // e.g. set this constant to some value, initialize all predictors
        playAsSaboteur += role() == Role.SABOTEUR ? 1 : 0;
        playAsMiner += role() == Role.GOLD_MINER ? 1 : 0;
    }


    //----------------------------*Tambahan James*----------------------------//
    private void init() {
        // System.out.println("Initialize!");

        ArrayList<Integer> randomFirstGoal = new ArrayList<>();
        randomFirstGoal.add(0);
        randomFirstGoal.add(1);
        randomFirstGoal.add(2);

        Collections.shuffle(randomFirstGoal);

        if(randomFirstGoal.get(0) == 0){
            goldPosition = Board.GoalPosition.TOP;
        }
        else if(randomFirstGoal.get(0) == 1 ){
            goldPosition = Board.GoalPosition.MIDDLE;
        }
        else{
            goldPosition = Board.GoalPosition.BOTTOM;
        }

        topOptimalCardTypesUnrotated.add(PathCard.Type.VERTICAL_T_PATH);
        topOptimalCardTypesUnrotated.add(PathCard.Type.VERTICAL_PATH);
        topOptimalCardTypesUnrotated.add(PathCard.Type.RIGHT_TURN_PATH);
        topOptimalCardTypesUnrotated.add(PathCard.Type.HORIZONTAL_T_PATH);
        topOptimalCardTypesUnrotated.add(PathCard.Type.CROSSROAD_PATH);
        topOptimalCardTypesUnrotated.add(PathCard.Type.HORIZONTAL_PATH);

        topOptimalCardTypesRotated.add(PathCard.Type.VERTICAL_T_PATH);
        topOptimalCardTypesRotated.add(PathCard.Type.RIGHT_TURN_PATH);
        topOptimalCardTypesRotated.add(PathCard.Type.HORIZONTAL_T_PATH);
        topOptimalCardTypesRotated.add(PathCard.Type.CROSSROAD_PATH);

        middleOptimalCardTypesUnrotated.add(PathCard.Type.VERTICAL_T_PATH);
        middleOptimalCardTypesUnrotated.add(PathCard.Type.VERTICAL_PATH);
        middleOptimalCardTypesUnrotated.add(PathCard.Type.CROSSROAD_PATH);
        middleOptimalCardTypesUnrotated.add(PathCard.Type.LEFT_TURN_PATH);
        middleOptimalCardTypesUnrotated.add(PathCard.Type.RIGHT_TURN_PATH);
        middleOptimalCardTypesUnrotated.add(PathCard.Type.HORIZONTAL_PATH);
        middleOptimalCardTypesUnrotated.add(PathCard.Type.HORIZONTAL_T_PATH);

        middleOptimalCardTypesRotated.add(PathCard.Type.VERTICAL_T_PATH);
        middleOptimalCardTypesRotated.add(PathCard.Type.HORIZONTAL_T_PATH);
        middleOptimalCardTypesRotated.add(PathCard.Type.RIGHT_TURN_PATH);
        middleOptimalCardTypesRotated.add(PathCard.Type.LEFT_TURN_PATH);

        bottomOptimalCardTypesUnrotated.add(PathCard.Type.VERTICAL_T_PATH);
        bottomOptimalCardTypesUnrotated.add(PathCard.Type.VERTICAL_PATH);
        bottomOptimalCardTypesUnrotated.add(PathCard.Type.LEFT_TURN_PATH);
        bottomOptimalCardTypesUnrotated.add(PathCard.Type.HORIZONTAL_T_PATH);
        bottomOptimalCardTypesUnrotated.add(PathCard.Type.CROSSROAD_PATH);
        bottomOptimalCardTypesUnrotated.add(PathCard.Type.HORIZONTAL_PATH);

        bottomOptimalCardTypesRotated.add(PathCard.Type.VERTICAL_T_PATH);
        bottomOptimalCardTypesRotated.add(PathCard.Type.LEFT_TURN_PATH);
        bottomOptimalCardTypesRotated.add(PathCard.Type.CROSSROAD_PATH);

        //checkStatusJames();
    }

    private void checkStatus() {
        //print role
        // System.out.print("role: ");
        // System.out.println(role());

        // history() is used to get the game move history
        // System.out.print("history: ");
        // System.out.println(history().moves());

        // discarded() is used to see cards you have previously discarded
        //ArrayList<Card> discarded = discarded();
        // System.out.print("discarded: ");
        // System.out.println(discarded());

        // isSabotaged() is used to check if your player is sabotaged
        // System.out.print("issabotaged: ");
        // System.out.println(isSabotaged());

        // knownGoals() is used to get opened goals, whether by a map card, or path-reachable
        //Map<Board.GoalPosition, GoalType> knownGoals = knownGoals();
        // System.out.print("knowngoals: ");
        // System.out.println(knownGoals());

        // System.out.print("hand: ");
        // System.out.println(hand());


    }

    public int getRandomWithExclusion(Random rnd, int start, int end, int... exclude) {
        int random = start + rnd.nextInt(end - start + 1 - exclude.length);
        for (int ex : exclude) {
            if (random < ex) {
                break;
            }
            random++;
        }
        return random;
    }

    @Override
    protected void onGameFinished(Role role, int lastPlayer) {
        if (role == role() && role == Role.SABOTEUR) ++winAsSaboteur;
        if (role == role() && role == Role.GOLD_MINER) ++winAsMiner;
    }
}
