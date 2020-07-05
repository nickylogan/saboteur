/*
 * Authors:
 * Wilbert (https://github.com/wilbertnw)
 * Kaven
 */

package ai.impl.wilbert;

import model.*;
import model.cards.Card;
import model.cards.PathCard;

import java.util.ArrayList;

public class Brain {

    int g1, g2, g3 = 0;
    Float playerScore = 0.0f;

    ArrayList<Integer> coordinateofgoldandstone = new ArrayList<>();

    final float k0 = 5;
    final float k1 = 8;
    final float k2 = 3;
    final float k3 = 2;
    final float k4 = 1;
    final float k5 = 1;

    final float c1 = 26;
    final float c2 = 27;
    final float c3 = 30;
    final float c4 = 29;
    final float c5 = 31;



    //Perceptron role;

    protected ArrayList<Float> heuristik = new ArrayList<>();

    protected void CalcHeuCell(Move m, PathCard card, boolean playerType) {

        float bmove = 0.0f;
        //PathCard card = (PathCard) m.card();

        if (m.args()[1] == 0 && card.rightSide() == PathCard.Side.PATH && card.bottomSide() == PathCard.Side.PATH) {
            //bmove.add((float) (0 + 0.3 * (1+1)));
            bmove = bmove + 0.3f * (card.rightSide().val() + card.bottomSide().val());
            //System.out.print("1");
        }

        if (m.args()[1] == 1 || m.args()[1] == 2 || m.args()[1] == 3) {
            //bmove.add((float) (0 + 0.3 * (1+1+1)+0.1));
            //System.out.println(card.bottomSide().val());
            bmove = bmove + 0.3f * (card.topSide().val() + card.rightSide().val() + card.bottomSide().val()) + 0.1f;
        }

        if (m.args()[1] == 4 && card.rightSide() == PathCard.Side.PATH && card.topSide() == PathCard.Side.PATH) {
            //bmove.add((float) (0 + 0.3 * (1+1)));
            bmove = bmove + 0.3f * (card.topSide().val() + card.rightSide().val());
            //System.out.print("3");
        }

        if (card.type() == Card.Type.PATHWAY && playerType == true) {
            //bmove.add((float) (0 + 0.1 * (1+(x+1)));
            bmove = bmove + 0.1f * (1 + m.args()[0]) / coordinateofgoldandstone.size();
            //System.out.print("4");
        }

        if (card.type() == Card.Type.PATHWAY && playerType == false) {
            //bmove.add((float) (0 + 0.1 * (9-(x+1)));
            bmove = bmove + 0.1f * (9 - m.args()[0]) / coordinateofgoldandstone.size();
            //System.out.print("5");
        }

        if (card.type() == Card.Type.DEADEND && playerType == true) {
            //bmove.add((float) (0 + 0.1 * (9-(x+1)));
            bmove = bmove + 0.1f * (9 - m.args()[0]) / coordinateofgoldandstone.size();
            //System.out.print("6");
        }

        if (card.type() == Card.Type.DEADEND && playerType == false) {
            //bmove.add((float) (0 + 0.1 * (1+(x+1)));
            bmove = bmove + 0.1f * (1 + m.args()[0]) / coordinateofgoldandstone.size();
            //System.out.print("7");
        }

        /**
         *
         */
        //heuCell.add(bmove+k0);
        heuristik.add(bmove + k0);
    }

    protected void CalcHeuCellRockFall(Move m, Board board, Boolean playertype) {
        float heuristic = 0.0f;

        if (m.args()[0] < 6) {
            heuristic = 0.5f * k2;
        } else {
            Cell trgtCell = board.cellAt(m.args()[0], m.args()[1]);
            int top = trgtCell.topSide().val();
            int right = trgtCell.rightSide().val();
            int bot = trgtCell.bottomSide().val();
            int left = trgtCell.leftSide().val();

            heuristic = top + right + bot + left;
        }
        heuristik.add(heuristic);

    }

    protected void CalcHeuristicBlock(Move m, float playerscore){
        float heuristic = 0.0f;

        if(playerscore>1){
            heuristic = 0.5f*k3;
        }
        else{
            heuristic = k3;
        }

        heuristik.add(heuristic);
    }

    protected void LocationofGoldandRock() {
        float heuristik1 = 1 + (0.5f*g1)*k1;
        float heuristik2 = 1 + (0.5f*g2)*k1;
        float heuristik3 = 1 + (0.5f*g3)*k1;
        heuristik.add(heuristik1);
        heuristik.add(heuristik2);
        heuristik.add(heuristik3);
    }

    protected void CalcHeuristicRepair(Move m, float playerscore){
        float heuristic = 0.0f;

        if(playerscore>1 && playerscore<3){
            heuristic = 2*k4;
        }
        else{
            heuristic = 0.5f*k3;
        }

        heuristik.add(heuristic);
    }


    protected void calcOtherPlayerPathScore(Float z1, Float z2, boolean playertype){
        Float score = 0.0f;

        if(z1 == z2 && playertype == true){
            score = score + z1;
        }
        if(z1 == z2 && playertype == false){
            score = score - z1;
        }
        if(z1 > z2 && playertype == true){
            score = score -(z1-z2);
        }
        if(z1 > z2 && playertype == false){
            score = score + (z1-z2);
        }

        playerScore = score;
    }

    protected void calcOtherPlayerDiscard(boolean playertype, Float f){
        playerScore = f;
        //goldminer
        if(playertype == true){
            playerScore = playerScore - c4;
        }
        else if(playertype == false){
            playerScore = playerScore + c4;
        }
    }



}
