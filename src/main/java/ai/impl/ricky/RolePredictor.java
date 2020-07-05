/*
 * Authors:
 * Ricky (https://github.com/zyphonGT)
 * Albert (https://github.com/mailmancy)
 */

package ai.impl.ricky;

import model.Move;

import java.util.*;

public class RolePredictor {
    ArrayList<RolePrediction> rp = new ArrayList<>();

    int myIndex;

    int totalPlayer;
    int totalSaboteur;

    int maxMistakes;
    int minCorrect;

    Float maxWrongThreshold;
    Float minCorrectThreshold;

    boolean imMiner;



    ArrayList<RolePrediction> potFriends;
    ArrayList<RolePrediction> potFoes;


    public RolePredictor(int myIndex, int totalPlayer, int totalSaboteur, int maxMistakes, int minCorrect, Float maxWrongThreshold, Float minCorrectThreshold, boolean imMiner) {
        this.myIndex = myIndex;
        this.totalPlayer = totalPlayer;
        this.totalSaboteur = totalSaboteur;
        this.maxMistakes = maxMistakes;
        this.minCorrect = minCorrect;
        this.maxWrongThreshold = maxWrongThreshold;
        this.minCorrectThreshold = minCorrectThreshold;
        this.imMiner = imMiner;

        //Initialize RolePredictions
        for(int i=0; i<totalPlayer; i++) {
            if(i != myIndex) {
                rp.add(new RolePrediction(i));
            }
        }

        if(imMiner) {
            potFriends = new ArrayList<>(totalPlayer-totalSaboteur);
            potFoes = new ArrayList<>(totalSaboteur);
        } else {
            potFriends = new ArrayList<>(totalSaboteur);
            potFoes = new ArrayList<>(totalPlayer-totalSaboteur);
        }

    }

    /**
     * Setiap move musuh
     *  1. [V] Hitung Heu Actual
     *  2. [V] Hitung Semua kemungkinan Heu
     *  3. [V] Hitung Move mana yang dianggap Benar (Berdasarkan rightMoveThreshold)
     *  4. [V] Apakah Move tsb benar?
     *      a. [V] Jika salah(Threshold), badMoves++
     *      b. [V] Jika benar(Threshold), goodMoves++
     *  5. [V] Input ScoreMove ke List
     *
     *  6. [V] Update potFriends dan potFoes
     *      a. [V] jika ada player yg badMoves >= maxBadMoves
     *              if( myRole == MINER )
     *                  masukin ke potFriends
     *              else
     *                  masukin ke potFoes
     *
     *      b. [V] Jika ada player yg goodMoves >= minGoodMoves
     *              if( myRole == SABOTEUR )
     *                  masukin ke potFriends
     *              else
     *                  masukin ke potFoes
     */

    protected void updatePrediction(float actualHeu, ArrayList<Float> heus, int playerIndex) {
        RolePrediction target = getRP(playerIndex);

        Float minHeu = getMinFloat(heus);
        Float maxHeu = getMaxFloat(heus);
        Float diff = maxHeu - minHeu;
        Float batasBawah    = minHeu + diff*(maxWrongThreshold);
        Float batasAtas     = minHeu + diff*(minCorrectThreshold);

        // Add Score to History RP & Update records of mistakes/corrects
        target.addScore(actualHeu, batasBawah, batasAtas);
    }

    protected void updatePredictionBlock(Move move, int myIndex) {
        RolePrediction target = getRP(move.playerIndex());

        int blockVictim = move.args()[0];

        if(blockVictim == myIndex) {
            target.addMistake();
        }

        for (RolePrediction temp : potFriends) {
            if(temp.targetIndex == blockVictim) {
                target.addMistake();
                return;
            }
        }

        for (RolePrediction temp : potFoes) {
            if(temp.targetIndex == blockVictim) {
                target.addCorrect();
                return;
            }
        }
    }

    protected void updatePredictionRepair(Move move, int myIndex) {
        RolePrediction target = getRP(move.playerIndex());

        int repairVictim = move.args()[0];

        if(repairVictim == myIndex) {
            target.addCorrect();
        }

        for (RolePrediction temp : potFriends) {
            if(temp.targetIndex == repairVictim) {
                target.addCorrect();
                return;
            }
        }

        for (RolePrediction temp : potFoes) {
            if(temp.targetIndex == repairVictim) {
                target.addMistake();
                return;
            }
        }
    }

    protected void updateListOfRoles() {
        potFriends.clear();
        potFoes.clear();

        for (RolePrediction temp:rp) {
            if(temp.badMoves > maxMistakes) potFoes.add(temp);
            if(temp.goodMoves > minCorrect) potFriends.add(temp);
        }
//        Sort potFoes, Most mistakes first
        Collections.sort(potFoes, new Comparator<RolePrediction>() {
            public int compare(RolePrediction p1, RolePrediction p2) {
                return p2.badMoves - p1.badMoves; // Ascending
            }
        });
//        Sort potFriends, Most correct first
        Collections.sort(potFriends, new Comparator<RolePrediction>() {
            public int compare(RolePrediction p1, RolePrediction p2) {
                return p2.goodMoves - p1.goodMoves; // Ascending
            }
        });

        for (RolePrediction temp:rp) {
            // System.out.println("bad,good : "+temp.badMoves+","+temp.goodMoves);
        }

    }


    private Float getMaxFloat(ArrayList<Float> heus) {
        Float max = -Float.MAX_VALUE;

        for (Float x:heus) {
            if(x > max) {
                max = x;
            }
        }
        return max;
    }

    private Float getMinFloat(ArrayList<Float> heus) {
        Float min = Float.MAX_VALUE;

        for (Float x:heus) {
            if(x < min) {
                min = x;
            }
        }
        return min;
    }

    private RolePrediction getRP(int targetPlayer) {
        for (RolePrediction temp: this.rp) {
            if(temp.targetIndex == targetPlayer) {
                return temp;
            }
        }
        return null;
    }

}
