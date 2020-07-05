/*
 * Authors:
 * Ricky (https://github.com/zyphonGT)
 * Albert (https://github.com/mailmancy)
 */

package ai.impl.ricky;

import java.util.ArrayList;

public class RolePrediction {
    int targetIndex;                            // Index dari player yang tertuduh
    double p;                                   // Probability of Being a Gold Miner
    ArrayList<Float> scores;
    int badMoves;
    int goodMoves;

    public RolePrediction(int trgtIndex) {
        this.targetIndex = trgtIndex;
        this.p = -1;
        scores = new ArrayList<>();
        badMoves = 0;
        goodMoves = 0;
    }

    public void addScore(Float heu, Float batasBawah, Float batasAtas) {
        scores.add(heu);
        // System.out.println("Batas bawah atas: " + batasBawah + " "+batasAtas);
        if(batasAtas.equals(batasBawah)){
            // System.out.println("Normal Move (only one heu)");
        } else if(heu <= batasBawah){
            // System.out.println("BAD MOVE!");
            addMistake();
        } else if (heu >= batasAtas) {
            // System.out.println("GOOD MOVE!");
            addCorrect();
        } else {
            // System.out.println("Normal Move. (Not in extreme range)");
        }
    }

    public void addMistake() {
        if(goodMoves == 0) {
            this.badMoves++;
        } else {
            this.goodMoves--;
        }
    }

    public void addCorrect() {
        if(badMoves == 0) {
            this.goodMoves++;
        } else {
            this.badMoves--;
        }
    }

    public void addScoreBlockRepair() {

    }


}
