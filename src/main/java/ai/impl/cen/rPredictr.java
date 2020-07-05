/*
 * Authors:
 * Cen (https://github.com/Bongcen)
 * Gabriel (https://github.com/gabrieldejan17)
 */

package ai.impl.cen;

import model.Move;

import java.util.*;

public class rPredictr {
    ArrayList<rPredict> rp = new ArrayList<>();

    int myIndex;

    int totalPlayer;
    int totalSaboteur;

    int maxMistakes;
    int minCorrect;

    Float maxWrongThreshold;
    Float minCorrectThreshold;

    boolean imMiner;

    ArrayList<rPredict> potFriends;
    ArrayList<rPredict> potFoes;

    public rPredictr(int myIndex, int totalPlayer, int totalSaboteur, int maxMistakes, int minCorrect, Float maxWrongThreshold, Float minCorrectThreshold, boolean imMiner) {
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
                rp.add(new rPredict(i));
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

    protected void updatePrediction(float actualHeu, ArrayList<Float> heus, int playerIndex) {
        rPredict target = getRP(playerIndex);

        Float minHeu = getMinFloat(heus);
        Float maxHeu = getMaxFloat(heus);
        Float diff = maxHeu - minHeu;
        Float batasBawah    = minHeu + diff*(maxWrongThreshold);
        Float batasAtas     = minHeu + diff*(minCorrectThreshold);

        // Add Score to History RP & Update records of mistakes/corrects
        target.addScore(actualHeu, batasBawah, batasAtas);
    }

    protected void updatePredictionBlock(Move move, int myIndex) {
        rPredict target = getRP(move.playerIndex());

        int blockVictim = move.args()[0];

        if(blockVictim == myIndex) {
            target.addMistake();
        }

        for (rPredict temp : potFriends) {
            if(temp.targetIndex == blockVictim) {
                target.addMistake();
                return;
            }
        }

        for (rPredict temp : potFoes) {
            if(temp.targetIndex == blockVictim) {
                target.addCorrect();
                return;
            }
        }
    }

    protected void updatePredictionRepair(Move move, int myIndex) {
        rPredict target = getRP(move.playerIndex());

        int repairVictim = move.args()[0];

        if(repairVictim == myIndex) {
            target.addCorrect();
        }

        for (rPredict temp : potFriends) {
            if(temp.targetIndex == repairVictim) {
                target.addCorrect();
                return;
            }
        }

        for (rPredict temp : potFoes) {
            if(temp.targetIndex == repairVictim) {
                target.addMistake();
                return;
            }
        }
    }

    protected void updateListOfRoles() {
        potFriends.clear();
        potFoes.clear();

        for (rPredict temp:rp) {
            if(temp.badMoves > maxMistakes) potFoes.add(temp);
            if(temp.goodMoves > minCorrect) potFriends.add(temp);
        }
//        Sort potFoes, Most mistakes first
        Collections.sort(potFoes, new Comparator<rPredict>() {
            public int compare(rPredict p1, rPredict p2) {
                return p2.badMoves - p1.badMoves; // Ascending
            }
        });
//        Sort potFriends, Most correct first
        Collections.sort(potFriends, new Comparator<rPredict>() {
            public int compare(rPredict p1, rPredict p2) {
                return p2.goodMoves - p1.goodMoves; // Ascending
            }
        });

        for (rPredict temp:rp) {
       //     System.out.println("bad,good : "+temp.badMoves+","+temp.goodMoves);
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

    private rPredict getRP(int targetPlayer) {
        for (rPredict temp: this.rp) {
            if(temp.targetIndex == targetPlayer) {
                return temp;
            }
        }
        return null;
    }

}
