/*
 * Authors:
 * Wilbert (https://github.com/wilbertnw)
 * Kaven
 */

package customAI.wilbert;

import ai.AI;
import model.*;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


public class Perceptron extends AI {
    public static int playAsMiner = 0;
    public static int playAsSaboteur = 0;
    public static int winAsMiner = 0;
    public static int winAsSaboteur = 0;

    public Perceptron(String name) {
        super(name);
    }

    Random random = ThreadLocalRandom.current();

    boolean playerType;

    Float playerScore[];

    Brain brain = new Brain();
    //HandHeu allHeu = new HandHeu();

    ArrayList<Float> eachHeu = new ArrayList<>();

    ArrayList<Integer> positionX = new ArrayList<>();
    ArrayList<Integer> positionY = new ArrayList<>();
    ArrayList<Float> heuCell = new ArrayList<>();


    //float[] heucell;





    /*private void CalcDiscardHeu(){
        float z = 0.0f;

        if(z < k5){

        }
        else{

        }
    }*/





    private Move generatePossiblePaths(int cardIndex, PathCard card) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        card.setRotated(false);
        Set<Position> posNormal = game().board().getPlaceable(card);
        posNormal.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardIndex, p.x, p.y, false)));


        /*posNormal.forEach(p -> positionX.add(p.x));
        posNormal.forEach(p -> positionY.add(p.y));

        //posNormal.forEach(p -> CalcHeuCell(p.x, p.y, card));*/

        card.setRotated(true);
        Set<Position> posRotated = game().board().getPlaceable(card);
        posRotated.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardIndex, p.x, p.y, true)));

        for (Move m : possibleMoves) {
            brain.CalcHeuCell(m, card, playerType);
        }
        Float biggest = 0.0f;

        int maxIndex = 0;
        int i = 0;
        for (Float f : brain.heuristik) {
            if (f > biggest) {
                biggest = f;
                maxIndex = i;
            }
            i++;
        }

        //System.out.println(Collections.max(brain.heuristik));
        brain.heuristik.clear();
        //System.out.println(i);
        eachHeu.add(biggest);

        if (possibleMoves.isEmpty()) {
            return null;
        } else {
            return possibleMoves.get(maxIndex);
        }
        // System.out.print(card + " => ");
        // System.out.print(posNormal);
        // System.out.println(posRotated);

    }


    private Move generatePossiblePlayerActions(int cardIndex, PlayerActionCard card) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int numPlayers = game().numPlayers();
        for (int i = 0; i < numPlayers; ++i) {
            if (i == index()) continue;
            Player p = game().playerAt(i);
            if (card.type() == Card.Type.REPAIR && p.isRepairable(card.effects())) {
                possibleMoves.add(Move.NewPlayerActionMove(index(), cardIndex, i));
                for (Move m : possibleMoves) {
                    brain.CalcHeuristicRepair(m,playerScore[m.playerIndex()]);
                }
            } else if (card.type() == Card.Type.BLOCK && p.isSabotageable(card.effects()[0])) {
                possibleMoves.add(Move.NewPlayerActionMove(index(), cardIndex, i));
                for (Move m : possibleMoves) {
                    brain.CalcHeuristicBlock(m,playerScore[m.playerIndex()]);
                }

            }
        }
        //eachHeu.add(0.0f);
        Float biggest = 0.0f;

        int maxIndex = 0;
        int i = 0;
        for (Float f : brain.heuristik) {
            if (f > biggest) {
                biggest = f;
                maxIndex = i;
            }
            i++;
        }

        //System.out.println(Collections.max(brain.heuristik));
        brain.heuristik.clear();
        //System.out.println(i);
        eachHeu.add(biggest);

        if (possibleMoves.isEmpty()) {
            return null;
        } else {
            return possibleMoves.get(maxIndex);
        }

        //return possibleMoves;
    }

    private Move generatePossibleRockfall(int cardIndex) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        Set<Position> positions = game().board().getDestroyable();
        positions.forEach(p -> possibleMoves.add(Move.NewRockfallMove(index(), cardIndex, p.x, p.y)));
        positions.forEach(p -> positionX.add(p.x));
        positions.forEach(p -> positionY.add(p.y));

        //eachHeu.add(0.0f);

        for(Move m : possibleMoves) {

            brain.CalcHeuCellRockFall(m, game().board(), playerType);
            //allMoveHeu.add(new MoveHeu(m, heu));
            //}
        }

        Float index = 0.0f;

        if (playerType == true) {
            int maxIndex = 0;
            int ibesar = 0;
            for (Float f : brain.heuristik) {
                if (f > index) {
                    index = f;
                    maxIndex = ibesar;
                }
                ibesar++;
            }
            brain.heuristik.clear();
            // System.out.println(ibesar);
            eachHeu.add(index);
            if(possibleMoves.isEmpty()){
                return null;
            }
            else {
                return possibleMoves.get(maxIndex);
            }
        } else {
            int minIndex = 10000;
            int ikecil = 0;
            for (Float f : brain.heuristik) {
                if (f > index) {
                    index = f;
                    minIndex = ikecil;
                }
                ikecil++;
            }
            brain.heuristik.clear();
            // System.out.println(ikecil);
            eachHeu.add(index);
            if(possibleMoves.isEmpty()){
                return null;
            }
            else{
                return possibleMoves.get(minIndex);
            }

        }
        //positions.forEach(p -> CalcHeuCellRockFall(p.x, p.y));
    }

    private Move generatePossibleMap(int cardIndex) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.TOP));
        possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.MIDDLE));
        possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.BOTTOM));
        brain.LocationofGoldandRock();
        Float biggest = 0.0f;

        int maxIndex = 0;
        int i = 0;
        for (Float f : brain.heuristik) {
            if (f > biggest) {
                biggest = f;
                maxIndex = i;
            }
            i++;
        }

        //System.out.println(Collections.max(brain.heuristik));
        brain.heuristik.clear();
        //System.out.println(i);
        eachHeu.add(biggest);

        if (possibleMoves.isEmpty()) {
            return null;
        } else {
            return possibleMoves.get(maxIndex);
        }
        //eachHeu.add(1.0f);
        //return possibleMoves;
    }


    @Override
    protected Move makeDecision() {

        //HandHeu allMoves = new HandHeu();
        ArrayList<Move> pathMoves = new ArrayList<>();
        ArrayList<Move> playMoves = new ArrayList<>();
        ArrayList<Move> discardMoves = new ArrayList<>();

        ArrayList<Move> allMoves = new ArrayList<>();


        int len = hand().size();
        for (int i = 0; i < len; ++i) {
            Card c = hand().get(i);
            if (c instanceof PathCard && !isSabotaged()) {

                allMoves.add(generatePossiblePaths(i, (PathCard) c));
                //allMoveData.add(generatePossiblePaths(i, (PathCard) c));
                //heuCardEach.add(Collections.max(heuCell));
            }
            if (c instanceof PlayerActionCard) {
                allMoves.add(generatePossiblePlayerActions(i, (PlayerActionCard) c));
            }
            if (c.type() == Card.Type.MAP) {
                allMoves.add(generatePossibleMap(i));
            }
            if (c.type() == Card.Type.ROCKFALL) {
                allMoves.add(generatePossibleRockfall(i));
            }

            Float biggest = 10000.0f;
            int minIndex = 0;
            int ind = 0;
            for (Float f : eachHeu) {
                if (f < biggest) {
                    biggest = f;
                    minIndex = ind;
                }
                ind++;
            }
            //generateHeuCardForDiscard();
            if(eachHeu.get(minIndex)<brain.k5 || eachHeu.get(minIndex) == null){
                //  int minimumHeu = eachHeu.indexOf(Collections.min(eachHeu));
                allMoves.add(Move.NewDiscardMove(index(), i));
            }
            //System.out.print(g1+"\n"+g2+"\n"+g3+"\n");

            // System.out.print("card:" + i);
            // System.out.print(positionX + "X");
            // System.out.print(positionY + "Y \n");
            positionX.clear();
            positionY.clear();


            //allHeu.eachHeu.clear();

            // System.out.println(allMoves);
            //allMoveData.clear();
            //pathMoves.clear();

            //System.out.println(generateHeuCardForDiscard()+"\n");
            //generateHeuCardForDiscard().clear();
        }
//    System.out.println(hand());
//    System.out.println(pathMoves);

        // System.out.println(eachHeu);

        int index = eachHeu.indexOf(Collections.max(eachHeu));
        eachHeu.clear();
        // System.out.println(index);
        if (allMoves.isEmpty()) {
            return discardMoves.get(0);
        } else {
            return allMoves.get(index);
        }

    }

    /**
     * @param move the played move
     * @see AI#onOtherPlayerMove(Move)
     */
    @Override
    protected void onOtherPlayerMove(Move move) {
        // Implement this to do something when another player moves
        if(move.type() == Move.Type.PLAY_PATH){
            generatePossiblePaths(0,(PathCard) move.card());
            brain.CalcHeuCell(move, (PathCard) move.card(), playerType);
            Float z1 = eachHeu.get(0);
            Float z2 = brain.heuristik.get(0);

            //System.out.println(z1+" z1");
            //System.out.println(z2+" z2");

            brain.calcOtherPlayerPathScore(z1,z2,playerType);

            eachHeu.clear();
            brain.heuristik.clear();

            playerScore[game().currentPlayerIndex()] = brain.playerScore;
            // System.out.println("\n player: "+game().currentPlayerIndex());
            for (Float f : playerScore
                 ) {

                // System.out.println(f);
            }

            //Float z1
        }

        /*if(move.type() == Move.Type.PLAY_ROCKFALL){

        }

        if(move.card().type() == Card.Type.REPAIR){

        }

        if(move.card().type() == Card.Type.BLOCK){

        }*/

        if(move.type() == Move.Type.DISCARD){
            brain.calcOtherPlayerDiscard(playerType,playerScore[game().currentPlayerIndex()]);
            playerScore[game().currentPlayerIndex()] = brain.playerScore;
        }

    }


    @Override
    protected void onGoalOpen(Board.GoalPosition position, GoalType goalType, boolean permanent) {
        // Implement this to do something when a goal card is opened

        if (goalType == GoalType.GOLD && position == Board.GoalPosition.TOP) {
            brain.g1 = 1;
            brain.coordinateofgoldandstone.remove(1);
            brain.coordinateofgoldandstone.remove(2);
        } else if (goalType == GoalType.ROCK && position == Board.GoalPosition.TOP) {
            brain.g1 = -1;
        }

        if (goalType == GoalType.GOLD && position == Board.GoalPosition.MIDDLE) {
            brain.g2 = 1;
            brain.coordinateofgoldandstone.remove(0);
            brain.coordinateofgoldandstone.remove(2);
        } else if (goalType == GoalType.ROCK && position == Board.GoalPosition.MIDDLE) {
            brain.g2 = -1;
        }

        if (goalType == GoalType.GOLD && position == Board.GoalPosition.BOTTOM) {
            brain.g3 = 1;
            brain.coordinateofgoldandstone.remove(0);
            brain.coordinateofgoldandstone.remove(1);
        } else if (goalType == GoalType.ROCK && position == Board.GoalPosition.BOTTOM) {
            brain.g3 = -1;
        }
    }

    @Override
    public void initialize() {
        playerScore  = new Float[game().numPlayers()];
        brain.coordinateofgoldandstone.add(0);
        brain.coordinateofgoldandstone.add(2);
        brain.coordinateofgoldandstone.add(4);
        for(int i = 0; i<playerScore.length; i++) {
            playerScore[i] = 0.0f;
        }

        // Implement this to initialize the AI when the game starts
        // e.g. set this constant to some value, initialize all predictors
        if(role() == Role.GOLD_MINER) {
            playerType = true;
            playAsMiner++;
        } else {
            playerType = false;
            playAsSaboteur++;
        }

    }

    @Override
    protected void onGameFinished(Role role, int lastPlayer) {
        if (role == role() && role == Role.SABOTEUR) ++winAsSaboteur;
        if (role == role() && role == Role.GOLD_MINER) ++winAsMiner;
    }
}