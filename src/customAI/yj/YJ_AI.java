/*
 * Created By:
 * Christopher Yefta / 00000026157 (https://github.com/ChrisYef)
 * James Adhitthana / 00000021759 (https://github.com/jamesadhitthana)
 */

package customAI.yj;

import ai.AI;
import model.*;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.*;

public class YJ_AI extends AI {

    Board.GoalPosition goldPosition;
    Boolean searchedTop = false;
    Boolean searchedMiddle = false;
    Boolean searchedBottom = false;
    Boolean goldFound = false;
    Random rand = new Random();

    int[] enemy;
    int enemyIndex = 99;


    public YJ_AI(String name) {
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

    private Position FindMostRightDeadEnd(GameLogicController game, Set<Position> destroyable, Position bestDestroyable) {
        Position temporaryDestroyable;
        for (Position p : destroyable) {
            //System.out.println(game.board().cellAt(p).card().type().equals(Card.Type.DEADEND) + "----------- x nya: " + p.x + "y nya: " + p.y);
            if (game.board().cellAt(p).card().type().equals(Card.Type.DEADEND)) {
                if (bestDestroyable == null) {
                    bestDestroyable = p;
                } else {
                    temporaryDestroyable = p;

                    if (temporaryDestroyable.x > bestDestroyable.x) { //if the X is higher (closer to the right edge then
                        bestDestroyable = temporaryDestroyable;
                    }
                }
                //System.out.println("DeadEND Card FOund and set to bestDestroyable: ----------- x nya: " + p.x + "y nya: " + p.y);
            }
        }
        return bestDestroyable;
    }


    private Position DestroyMostRightPathway(GameLogicController game, Set<Position> destroyable, Position bestDestroyable) {
        Position temporaryDestroyable;
        //System.out.println("Destroying right most PATHWAY");

        //TODO: if it is a dead end, dont destroy it
        for (Position p : destroyable) {
            //System.out.println(game.board().cellAt(p).card().type().equals(Card.Type.DEADEND) + "KETEMU DEADEND NIH!!----------- x nya: " + p.x + "y nya: " + p.y); //fixme
            //TODO: if it founds a deadend, then dont do the for loop below
            if (!game.board().cellAt(p).card().type().equals(Card.Type.DEADEND)) {
                //If the card is not a deadend
                //System.out.println("x nya: " + p.x);
                //System.out.println("y nya: " + p.y);
                if (bestDestroyable == null) {
                    bestDestroyable = p;
                } else {
                    temporaryDestroyable = p;

                    if (temporaryDestroyable.x > bestDestroyable.x) { //if the X is higher (closer to the right edge then
                        bestDestroyable = temporaryDestroyable;
                    }
                }
            }
        }
        //System.out.println("final best destroyable: " + bestDestroyable);
        return bestDestroyable;
    }

    private Move BestTopMove(int myIndex, int cardIndex, Set<Position> posProcessed, boolean isRotated) {

        Position temporaryPosition;
        Position selectedPosition = null;
        int diff = 0;
        int tempdiff = 0;

        /** PosProcessed **/
        for (Position p : posProcessed) {
            if (selectedPosition == null) {
                selectedPosition = p;
                diff = (8 - selectedPosition.x) / 2 + (selectedPosition.y);
            } else {
                temporaryPosition = p;
                tempdiff = (8 - temporaryPosition.x) / 2 + (temporaryPosition.y);

                if (tempdiff < diff) {
                    selectedPosition = temporaryPosition;
                    diff = tempdiff;
                }
            }
        }

        /** return selected position **/
        if (selectedPosition != null) {
            return Move.NewPathMove(myIndex, cardIndex, selectedPosition.x, selectedPosition.y, isRotated);
        } else {
            return null;
        }
    }

    private Move BestBottomMove(int myIndex, int cardIndex, Set<Position> posProcessed, boolean isRotated) {

        Position temporaryPosition;
        Position selectedPosition = null;
        int diff = 0;
        int tempdiff = 0;

        /** PosProcessed **/
        for (Position p : posProcessed) {
            if (selectedPosition == null) {
                selectedPosition = p;
                diff = (8 - selectedPosition.x) / 2 + (4 - selectedPosition.y);
            } else {
                temporaryPosition = p;
                tempdiff = (8 - temporaryPosition.x) / 2 + (4 - temporaryPosition.y);

                if (tempdiff < diff) {
                    selectedPosition = temporaryPosition;
                    diff = tempdiff;
                }
            }
        }

        /** return selected position **/
        if (selectedPosition != null) {
            return Move.NewPathMove(myIndex, cardIndex, selectedPosition.x, selectedPosition.y, isRotated);
        } else {
            return null;
        }
    }

    private Move BestMiddleMove(int myIndex, int cardIndex, Set<Position> posProcessed, boolean isRotated) {

        Position temporaryPosition;
        Position selectedPosition = null;

        /** PosProcessed **/
        for (Position p : posProcessed) {
            if (selectedPosition == null) {
                selectedPosition = p;
            } else {
                temporaryPosition = p;

                int temporaryDistanceToMiddle;
                int selectedDistanceToMiddle;

                if (temporaryPosition.y < 2) {
                    temporaryDistanceToMiddle = 2 - temporaryPosition.y;
                } else if (temporaryPosition.y > 2) {
                    temporaryDistanceToMiddle = temporaryPosition.y - 2;
                } else {
                    temporaryDistanceToMiddle = 0;
                }

                if (selectedPosition.y < 2) {
                    selectedDistanceToMiddle = 2 - selectedPosition.y;
                } else if (selectedPosition.y > 2) {
                    selectedDistanceToMiddle = selectedPosition.y - 2;
                } else {
                    selectedDistanceToMiddle = 0;
                }

                if (temporaryPosition.x > selectedPosition.x && temporaryDistanceToMiddle < selectedDistanceToMiddle) {
                    selectedPosition = temporaryPosition;
                }
            }
        }

        /** return selected position **/
        if (selectedPosition != null) {
            return Move.NewPathMove(myIndex, cardIndex, selectedPosition.x, selectedPosition.y, isRotated);
        } else {
            return null;
        }
    }

    private Move BestNeutralMove(int myIndex, int cardIndex, Set<Position> posProcessed, boolean isRotated) {

        Position temporaryPosition;
        Position selectedPosition = null;

        /** PosProcessed **/
        for (Position p : posProcessed) {
            if (selectedPosition == null) {
                selectedPosition = p;
            } else {
                temporaryPosition = p;

                if (temporaryPosition.x > selectedPosition.x) {
                    selectedPosition = temporaryPosition;
                }
            }
        }

        /** return selected position **/
        if (selectedPosition != null) {
            return Move.NewPathMove(myIndex, cardIndex, selectedPosition.x, selectedPosition.y, isRotated);
        } else {
            return null;
        }
    }

    private Move GenerateDiggerPathMove(int myIndex, int cardIndex, PathCard card) {

        /** UN ROTATED CARD **/

        card.setRotated(false);

        Set<Position> posNormal = game().board().getPlaceable(card);
        Set<Position> posProcessed = new HashSet<>(); // isi semua kemungkinan posisi dari suatu kartu

        Move unrotatedMove = null;
        Move rotatedMove = null;

        // Kalau kartunya gak bisa ditaruh di mana-mana, maka return null
        if (posNormal.isEmpty()) {
            Move temp = null;
            return temp;
        }

        // Cek jenis kartu pathway-nya, kalau deadway, maka return null
        if (card.pathType().equals(PathCard.Type.VERTICAL_PATH)
                || card.pathType().equals(PathCard.Type.CROSSROAD_PATH)
                || card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH)
                || card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH)
                || card.pathType().equals(PathCard.Type.HORIZONTAL_PATH)) {
            // Cek setiap possible lokasi kartunya bisa ditaruh
            for (Position p : posNormal) {

                if ((card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 0)
                        || (card.pathType().equals(PathCard.Type.VERTICAL_PATH) && p.y == 0)
                        || (card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.y == 0)
                        || (card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.y == 4)
                        || (card.pathType().equals(PathCard.Type.VERTICAL_PATH) && p.y == 4)
                        || (card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH) && p.y == 4)
                        || (card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.y == 4)
                        || (card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.y > 1)
                        || (card.pathType().equals(PathCard.Type.VERTICAL_PATH) && p.y == 2)
                        || (card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.y == 2)
                        || (card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH) && p.x == 0)
                        || (card.pathType().equals(PathCard.Type.HORIZONTAL_PATH) && p.x == 0)
                        || (card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.x == 0)
                        || (card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.x == 0)
                        || (card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH) && p.x == 8)
                        || (card.pathType().equals(PathCard.Type.HORIZONTAL_PATH) && p.x == 8)
                        || (card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.x == 8)
                        || (card.pathType().equals(PathCard.Type.VERTICAL_T_PATH) && p.x == 7)
                        || (card.pathType().equals(PathCard.Type.VERTICAL_PATH) && p.x == 7)
                        || (card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.x == 7)
                ) {
                    // Don't add
                } else {
                    posProcessed.add(p);
                }
            }
        }

        if (goldFound) {
            if (goldPosition == Board.GoalPosition.TOP) {
                unrotatedMove = BestTopMove(myIndex, cardIndex, posProcessed, false);
            } else if (goldPosition == Board.GoalPosition.MIDDLE) {
                unrotatedMove = BestMiddleMove(myIndex, cardIndex, posProcessed, false);
            } else if (goldPosition == Board.GoalPosition.BOTTOM) {
                unrotatedMove = BestBottomMove(myIndex, cardIndex, posProcessed, false);
            }
        } else {
            unrotatedMove = BestNeutralMove(myIndex, cardIndex, posProcessed, false);
        }


        /** ROTATED CARD **/

        card.setRotated(true);
        Set<Position> posRotated = game().board().getPlaceable(card);

        if (card.pathType().equals(PathCard.Type.VERTICAL_T_PATH)
                || card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH)
                || card.pathType().equals(PathCard.Type.LEFT_TURN_PATH)) {
            for (Position p : posRotated) {
                if ((card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.y == 0)
                        || (card.pathType().equals(PathCard.Type.LEFT_TURN_PATH) && p.y < 3)
                        || (card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH) && p.y == 0)
                        || (card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.y == 2)
                        || (card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.x == 0)
                        || (card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH) && p.x == 0)
                        || (card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.x == 8)
                        || (card.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH) && p.x == 8)
                        || (card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.x == 7)
                        || (card.pathType().equals(PathCard.Type.RIGHT_TURN_PATH) && p.y == 4)
                ) {
                    // Don't add
                } else {
                    posProcessed.add(p);
                }
            }
        }

        if (goldFound) {
            if (goldPosition == Board.GoalPosition.TOP) {
                rotatedMove = BestTopMove(myIndex, cardIndex, posProcessed, true);
            } else if (goldPosition == Board.GoalPosition.MIDDLE) {
                rotatedMove = BestMiddleMove(myIndex, cardIndex, posProcessed, true);
            } else if (goldPosition == Board.GoalPosition.BOTTOM) {
                rotatedMove = BestBottomMove(myIndex, cardIndex, posProcessed, true);
            }
        } else {
            rotatedMove = BestNeutralMove(myIndex, cardIndex, posProcessed, true);
        }


        /** Compare unrotated move and rotated move**/
        if (unrotatedMove == null && rotatedMove == null) {
            Move temp = null;
            return temp;
        } else if (unrotatedMove == null && rotatedMove != null) {
            return rotatedMove;
        } else if (unrotatedMove != null && rotatedMove == null) {
            return unrotatedMove;
        } else {
            int[] unrotatedXY = unrotatedMove.args();
            int[] rotatedXY = rotatedMove.args();

            // indeks 0 = x
            // indeks 1 = y
            if (rotatedXY[0] > unrotatedXY[0]) {
                return rotatedMove;
            } else {
                return unrotatedMove;
            }
        }
    }

    private Move GenerateSaboteurPathMove(int myIndex, int cardIndex, PathCard card) {

        Set<Position> posProcessed = new HashSet<>(); // isi semua kemungkinan posisi dari suatu kartu

        Move unrotatedMove = null;
        Move rotatedMove = null;

        /** UN ROTATED CARD **/

        card.setRotated(false);
        Set<Position> posNormal = game().board().getPlaceable(card);

        for (Position p : posNormal) {

            //System.out.println("p: " + p);
            posProcessed.add(p);

        }

        if (goldFound) {
            if (goldPosition == Board.GoalPosition.TOP) {
                unrotatedMove = BestTopMove(myIndex, cardIndex, posProcessed, false);
            } else if (goldPosition == Board.GoalPosition.MIDDLE) {
                unrotatedMove = BestMiddleMove(myIndex, cardIndex, posProcessed, false);
            } else if (goldPosition == Board.GoalPosition.BOTTOM) {
                unrotatedMove = BestBottomMove(myIndex, cardIndex, posProcessed, false);
            }
        } else {
            unrotatedMove = BestNeutralMove(myIndex, cardIndex, posProcessed, false);
        }

        /** ROTATED CARD **/

        card.setRotated(true);
        Set<Position> posRotated = game().board().getPlaceable(card);

        for (Position p : posRotated) {

            posProcessed.add(p);

        }

        if (goldFound) {
            if (goldPosition == Board.GoalPosition.TOP) {
                rotatedMove = BestTopMove(myIndex, cardIndex, posProcessed, true);
            } else if (goldPosition == Board.GoalPosition.MIDDLE) {
                rotatedMove = BestMiddleMove(myIndex, cardIndex, posProcessed, true);
            } else if (goldPosition == Board.GoalPosition.BOTTOM) {
                rotatedMove = BestBottomMove(myIndex, cardIndex, posProcessed, true);
            }
        } else {
            rotatedMove = BestNeutralMove(myIndex, cardIndex, posProcessed, true);
        }


        /** Compare unrotated move and rotated move**/
        if (unrotatedMove == null && rotatedMove == null) {
            Move temp = null;
            return temp;
        } else if (unrotatedMove == null && rotatedMove != null) {
            return rotatedMove;
        } else if (unrotatedMove != null && rotatedMove == null) {
            return unrotatedMove;
        } else {
            int[] unrotatedXY = unrotatedMove.args();
            int[] rotatedXY = rotatedMove.args();

            // indeks 0 = x
            // indeks 1 = y
            if (rotatedXY[0] > unrotatedXY[0]) {
                return rotatedMove;
            } else {
                return unrotatedMove;
            }
        }

    }

    private Move ChooseBestPathMove(ArrayList<Move> possibleMoves) {

        Move temporaryMove;
        Move selectedMove = null;

        for (Move p : possibleMoves) {
            if (p != null) {
                if (selectedMove == null) {
                    selectedMove = p;
                } else {
                    temporaryMove = p;

                    int[] temporaryXY = temporaryMove.args();
                    int[] selectedXY = selectedMove.args();

                    // indeks 0 = x
                    // indeks 1 = y
                    if (temporaryXY[0] > selectedXY[0]) {
                        selectedMove = temporaryMove;
                    }
                }
            }
        }

        return selectedMove;
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

        //List
        ArrayList<Move> possiblePathMoves = new ArrayList<>();
        ArrayList<Move> possibleMapMove = new ArrayList<>();

        // Iterate through hand
        for (int cardIndex = 0; cardIndex < cards.size(); ++cardIndex) {

            // Get reference of card at hand
            Card card = cards.get(cardIndex);

            /** Repairing another player **/
            if (card.type() == Card.Type.REPAIR) {
                PlayerActionCard pCard = (PlayerActionCard) card;

                int targetPlayer;

                if (isSabotaged()) {
                    //if the current player is broken then fix self first
                    targetPlayer = myIndex;
                    move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
                    return(move);
                } else {
                    //randomizes the repair to a random player except itself
                    if (role().equals(Role.SABOTEUR) && enemyIndex != 99) {
                        targetPlayer = enemyIndex;

                        Player p = game.playerAt(targetPlayer);
                        Tool tool = pCard.effects()[0];
                        if (p.isRepairable(tool)) {
                            move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
                            possibleMapMove.add(move);
                        }

                    }
                    if (role().equals(Role.GOLD_MINER)) {
                        targetPlayer = getRandomWithExclusion(rand, 0, game.numPlayers() - 1, myIndex, enemyIndex);
                        Player p = game.playerAt(targetPlayer);
                        Tool tool = pCard.effects()[0];
                        if (p.isRepairable(tool)) {
                            move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
                            return(move);
                        }
                    }

                }
            }

            /** When the AI gets a goal card **/
            // It will check from the top > middle > bottom
            // if the goal is already found, it will discard the current map card.

            if (card.type() == Card.Type.MAP) {
                if (searchedTop == false && goldFound == false) {
                    Board.GoalPosition target = Board.GoalPosition.TOP;
                    searchedTop = true;
                    move = Move.NewMapMove(myIndex, cardIndex, target);
                    System.out.println("searched top");
                    return(move);
                } else if (searchedMiddle == false && goldFound == false) {
                    Board.GoalPosition target = Board.GoalPosition.MIDDLE;
                    searchedMiddle = true;
                    move = Move.NewMapMove(myIndex, cardIndex, target);
                    System.out.println("searched middle");
                    return(move);
                } else if (searchedBottom == false && goldFound == false) {
                    Board.GoalPosition target = Board.GoalPosition.BOTTOM;
                    searchedBottom = true;
                    move = Move.NewMapMove(myIndex, cardIndex, target);
                    System.out.println("searched bottom");
                    return(move);
                }
            }


            /** Example of destroying a path **/
            if (card.type() == Card.Type.ROCKFALL) {
                Set<Position> destroyable = game.board().getDestroyable();
                Position temporaryDestroyable;
                Position bestDestroyable = null; //best destroyable position in the board

                if (role().equals(Role.SABOTEUR)) {
                    //CHECK DESTROYABLE X position if it is higher then destroy for saboteur

                    //TODO: if it is a dead end, dont destroy it

                    if(!DestroyMostRightPathway(game, destroyable, bestDestroyable).equals(null)) {
                        bestDestroyable = DestroyMostRightPathway(game, destroyable, bestDestroyable);
                        move = Move.NewRockfallMove(myIndex, cardIndex, bestDestroyable.x, bestDestroyable.y);
                        return (move);
                    }

                }
                if (role().equals(Role.GOLD_MINER)) {
                    bestDestroyable = FindMostRightDeadEnd(game, destroyable, bestDestroyable);
                    if (bestDestroyable == null) { //if no dead end found in the most right then destroy the current rockfall card
                        //System.out.println("no deadends found -- revert to destroy card instead");
                    } else if(bestDestroyable.x != 0){
                        move = Move.NewRockfallMove(myIndex, cardIndex, bestDestroyable.x, bestDestroyable.y);
                        return(move);
                    }
                }


            }

            /** Example of placing a path card **/
            if (card.type() == Card.Type.PATHWAY && role().equals(Role.GOLD_MINER)) {
                PathCard pCard = ((PathCard) card);

                possiblePathMoves.add(GenerateDiggerPathMove(myIndex, cardIndex, pCard));


            } else if (card.type() == Card.Type.DEADEND && role().equals(Role.SABOTEUR)) {
                PathCard pCard = ((PathCard) card);

                possiblePathMoves.add(GenerateSaboteurPathMove(myIndex, cardIndex, pCard));

            }



            /** Example of blocking another player **/
            if (card.type() == Card.Type.BLOCK) {
                PlayerActionCard pCard = ((PlayerActionCard) card);
                //randomizes the block to block a random player except itself
                int targetPlayer;
                if (role().equals(Role.SABOTEUR)) {
                    targetPlayer = getRandomWithExclusion(rand, 0, game.numPlayers() - 1, myIndex,enemyIndex);
                    Player p = game.playerAt(targetPlayer);
                    Tool tool = pCard.effects()[0];
                    if (p.isSabotageable(tool))
                        move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
                    return(move);
                }
                if (role().equals(Role.GOLD_MINER) && enemyIndex != 99) {
                    targetPlayer = enemyIndex;

                    Player p = game.playerAt(targetPlayer);
                    Tool tool = pCard.effects()[0];
                    if (p.isSabotageable(tool))
                        move = Move.NewPlayerActionMove(myIndex, cardIndex, targetPlayer);
                    //enemyIndex

                    possibleMapMove.add(move);

                }
            }





            /** Example of discarding a card **/
            if (role().equals(Role.SABOTEUR)) {
                //System.out.println("[discard card] Saboteur checks : " + card);

                if (card.type() == Card.Type.PATHWAY) {
                    //Saboteur Discards Pathway Cards that (buka buka jalan)
                    PathCard pCard = ((PathCard) card); //Convert card to pathCard so that we can check the type of card
                    if (//Check type of card if matches the pattern
                            pCard.pathType().equals(PathCard.Type.VERTICAL_PATH)
                                    || pCard.pathType().equals(PathCard.Type.VERTICAL_T_PATH)
                                    || pCard.pathType().equals(PathCard.Type.HORIZONTAL_PATH)
                                    || pCard.pathType().equals(PathCard.Type.HORIZONTAL_T_PATH)
                                    || pCard.pathType().equals(PathCard.Type.LEFT_TURN_PATH)
                                    || pCard.pathType().equals(PathCard.Type.RIGHT_TURN_PATH)
                                    || pCard.pathType().equals(PathCard.Type.CROSSROAD_PATH)
                    ) {
                        possibleMapMove.add(Move.NewDiscardMove(index(), cardIndex));
                        //System.out.println("[discard card] SABOTEUR: Found and added DISCARD " + card + " to possibleMapMove");
                    }
                }
                if (card.type() == Card.Type.REPAIR) {
                    //Saboteur Discards Heal Cards
                    possibleMapMove.add(Move.NewDiscardMove(index(), cardIndex));
                    //System.out.println("[discard card] SABOTEUR: Found and added DISCARD REPAIR card to possibleMapMove");
                }
            }
            if (role().equals(Role.GOLD_MINER)) {
                //System.out.println("[discard card] Gold Miner checks : " + card);
                if (card.type() == Card.Type.DEADEND) {
                    //Gold Miner Discard Dead End cards
                    possibleMapMove.add(Move.NewDiscardMove(index(), cardIndex));
                    //System.out.println("[discard card] GOLD MINER: Found and added DISCARD DEADEND card to possibleMapMove");

                }
            }

        }


        /** End Decision **/

        System.out.println("Player: " + index());

        if (possiblePathMoves.size() == 1 && !isSabotaged()) {

            if (possiblePathMoves.get(0) != null) {
                System.out.println("1: " + possiblePathMoves.get(0));
                return possiblePathMoves.get(0);
            }

        } else if (possiblePathMoves.size() > 1 && !isSabotaged()) {
            if (ChooseBestPathMove(possiblePathMoves) != null) {
                System.out.println("2: " + ChooseBestPathMove(possiblePathMoves));
                return ChooseBestPathMove(possiblePathMoves);
            }

        }
        for (Move t : possibleMapMove) {
            if (t != null) {
                System.out.println("3: " + t);
                return t;
            }
        }

        return (null);

    }


    /**
     * @param move the played move
     * @see AI#onOtherPlayerMove(Move)
     */
    @Override
    protected void onOtherPlayerMove(Move move) {
//TODO this
        // Implement this to do something when another player moves
        //System.out.println("History: " + history().moves());


        Move temp = history().moves().get(history().moves().size() - 1);
        // System.out.println("------"+temp);
//
        if(!temp.type().equals(Move.Type.DISCARD)) {
            //System.out.println("------" + temp.card().type() + temp.playerIndex());
            if (temp.card().type().equals(Card.Type.DEADEND)) {
                enemy[temp.playerIndex()]++;//Kalau deadend berarti jahat ++
            }

            for (int printTemp : enemy) {//DEBUG
                //System.out.println(printTemp);
            }


            int max = enemy[0];
            int index = 0;

            for (int i = 0; i < enemy.length; i++) {
                if (max < enemy[i]) {
                    max = enemy[i];
                    enemyIndex = i;
                }
            }
        }



        //System.out.println("Index position of Maximum value in an array is  :  " + enemyIndex);


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

        System.out.println("myindex: " + index() + " figured out goal position: " + position + " goaltype: " + goalType);

        if (goalType.equals(GoalType.GOLD)) {
            System.out.println("This is gold");
            goldPosition = position;
            goldFound = true;
        }

        if (goalType.equals(GoalType.ROCK)) {
            System.out.println("This is rock");
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

    int numPlayers = 0;
    @Override
    public void initialize() {
        numPlayers = game().numPlayers();
        enemy = new int[numPlayers];
        for(int i=0; i<numPlayers; i++)
        {
            enemy[i] = 0;
        }


        // Implement this to initialize the AI when the game starts
        System.out.println("YJ_AI says initialize!");
        // e.g. set this constant to some value, initialize all predictors
    }
}
