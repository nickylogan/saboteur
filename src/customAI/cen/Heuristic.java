/*
 * Authors:
 * Cen (https://github.com/Bongcen)
 * Gabriel (https://github.com/gabrieldejan17)
 */

package customAI.cen;

import model.*;
import model.cards.PathCard;

import java.util.ArrayList;

import static java.lang.Float.MAX_VALUE;

public class Heuristic {

    public float k0,k1,k2,k3,k4,k5,c1,c2,c3,c4;
    int possibleGold = 1;

    public Heuristic(float k0, float k1, float k2, float k3, float k4, float k5, float c1, float c2, float c3, float c4) {
        this.k0 = k0;
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.k4 = k4;
        this.k5 = k5;
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
    }


    /*
     * Move
     *   - int      playerIndex()
     *   - int      handIndex()
     *   - int()    args()              {x,y}
     *   - bool     rotated
     */

    /*
     * PathCard (PATHWAY or DEADEND)
     *   - Side[]   sides()             {top, right, bottom, left}
     *
     * Side
     *   - int      val()               {-2(Rock), 0(Deadend), 1(Path)}
     */


    /*
     * PATHCARD
     *      Goldminer
     *          heu += (1+Xk)/9 * 5;
     *
     *          if(g1=1)
     *              heu += (5-yk)/9 * 10;
     *              heu += (a1+a2+a3+a4)
     *
     */

    mHeuristic calcHeuCardPathNEW(PathCard path, ArrayList<Move> targets, boolean isMiner, int[] goalData) {

        float bmove = 0;
        ArrayList<mHeuristic> allMHeuristic = new ArrayList<>();

//        System.out.println("Inside heuChecker");

        for(Move trgtMove : targets) {
            bmove = 0;

            if(isMiner) {
                bmove += (1+trgtMove.args()[0])/9.0f*5.0f;

                bmove += (path.topSide().val()) * 2.0f;
                bmove += (path.rightSide().val()) * 2.0f;
                bmove += (path.bottomSide().val()) * 2.0f;

                if(trgtMove.args()[1] == 0) {
                    bmove -= (path.topSide().val() * 2);
                }
                if(trgtMove.args()[1] == 4) {
                    bmove -= (path.bottomSide().val() * 2);
                }

                if(goalData[0] == 1) {
                    bmove += (4 - trgtMove.args()[1])/4*8.0f;
                } else if(goalData[1] == 1) {
                    bmove += (2 -(Math.abs(trgtMove.args()[1]-2)))/2.0f * 8.0f;
                } else if(goalData[2] == 1) {
                    bmove += (trgtMove.args()[1])/4*8.0f;
                } else {
                    bmove += (2 -(Math.abs(trgtMove.args()[1]-2)))/2.0f * 4.0f;
                }

            } else {

                bmove += (9-trgtMove.args()[0])/9.0f*5.0f;

                bmove -= (path.topSide().val()) * 2.0f;
                bmove -= (path.rightSide().val()) * 2.0f;
                bmove -= (path.bottomSide().val()) * 2.0f;

                if(trgtMove.args()[1] == 0) {
                    bmove += (path.topSide().val() * 2);
                }
                if(trgtMove.args()[1] == 4) {
                    bmove += (path.bottomSide().val() * 2);
                }

                if(goalData[0] == 1) {
                    bmove -= (4 - trgtMove.args()[1])/4*8.0f;
                } else if(goalData[1] == 1) {
                    bmove -= (2 -(Math.abs(trgtMove.args()[1]-2)))/2.0f * 8.0f;
                } else if(goalData[2] == 1) {
                    bmove -= (trgtMove.args()[1])/4*8.0f;
                } else {
                    bmove -= (2 -(Math.abs(trgtMove.args()[1]-2)))/2.0f * 4.0f;
                }
            }
            allMHeuristic.add(new mHeuristic(trgtMove,bmove));
        }
        return maxHeu(allMHeuristic);
    }

    ArrayList<Float> calcHeuCardPathFloatsNEW(PathCard path, ArrayList<Move> targets, boolean isMiner, int[] goalData) {
        float bmove = 0;
        ArrayList<Float> heus = new ArrayList<>();

//        System.out.println("Inside heuChecker");

        for(Move trgtMove : targets) {
            bmove = 0;

            if(isMiner) {
                bmove += (1+trgtMove.args()[0])/9.0f*5.0f;

                bmove += (path.topSide().val()) * 2.0f;
                bmove += (path.rightSide().val()) * 2.0f;
                bmove += (path.bottomSide().val()) * 2.0f;

                if(trgtMove.args()[1] == 0) {
                    bmove -= (path.topSide().val() * 2);
                }
                if(trgtMove.args()[1] == 4) {
                    bmove -= (path.bottomSide().val() * 2);
                }

                if(goalData[0] == 1) {
                    bmove += (4 - trgtMove.args()[1])/4*8.0f;
                } else if(goalData[1] == 1) {
                    bmove += (2 -(Math.abs(trgtMove.args()[1]-2)))/2.0f * 8.0f;
                } else if(goalData[2] == 1) {
                    bmove += (trgtMove.args()[1])/4*8.0f;
                } else {
                    bmove += (2 -(Math.abs(trgtMove.args()[1]-2)))/2.0f * 4.0f;
                }

            } else {

                bmove += (9-trgtMove.args()[0])/9.0f*5.0f;

                bmove -= (path.topSide().val()) * 2.0f;
                bmove -= (path.rightSide().val()) * 2.0f;
                bmove -= (path.bottomSide().val()) * 2.0f;

                if(trgtMove.args()[1] == 0) {
                    bmove += (path.topSide().val() * 2);
                }
                if(trgtMove.args()[1] == 4) {
                    bmove += (path.bottomSide().val() * 2);
                }

                if(goalData[0] == 1) {
                    bmove -= (4 - trgtMove.args()[1])/4*8.0f;
                } else if(goalData[1] == 1) {
                    bmove -= (2 -(Math.abs(trgtMove.args()[1]-2)))/2.0f * 8.0f;
                } else if(goalData[2] == 1) {
                    bmove -= (trgtMove.args()[1])/4*8.0f;
                } else {
                    bmove -= (2 -(Math.abs(trgtMove.args()[1]-2)))/2.0f * 4.0f;
                }
            }

            heus.add(bmove);
        }
        return heus;
    }


    float calcHeuCellPathFloatsNEW(PathCard path, Move target, boolean isMiner, int[] goalData) {
        float bmove = 0;

//        System.out.println("Inside heuChecker");

            bmove = 0;

            if(isMiner) {
                bmove += (1+target.args()[0])/9.0f*5.0f;

                bmove += (path.topSide().val()) * 2.0f;
                bmove += (path.rightSide().val()) * 2.0f;
                bmove += (path.bottomSide().val()) * 2.0f;

                if(target.args()[1] == 0) {
                    bmove -= (path.topSide().val() * 2);
                }
                if(target.args()[1] == 4) {
                    bmove -= (path.bottomSide().val() * 2);
                }


                if(goalData[0] == 1) {
                    bmove += (4 - target.args()[1])/4*8.0f;
                } else if(goalData[1] == 1) {
                    bmove += (2 -(Math.abs(target.args()[1]-2)))/2.0f * 8.0f;
                } else if(goalData[2] == 1) {
                    bmove += (target.args()[1])/4*8.0f;
                } else {
                    bmove += (2 -(Math.abs(target.args()[1]-2)))/2.0f * 4.0f;
                }

            } else {

                bmove += (9-target.args()[0])/9.0f*5.0f;

                bmove -= (path.topSide().val()) * 2.0f;
                bmove -= (path.rightSide().val()) * 2.0f;
                bmove -= (path.bottomSide().val()) * 2.0f;

                if(target.args()[1] == 0) {
                    bmove += (path.topSide().val() * 2);
                }
                if(target.args()[1] == 4) {
                    bmove += (path.bottomSide().val() * 2);
                }

                if(goalData[0] == 1) {
                    bmove -= (4 - target.args()[1])/4*8.0f;
                } else if(goalData[1] == 1) {
                    bmove -= (2 -(Math.abs(target.args()[1]-2)))/2.0f * 8.0f;
                } else if(goalData[2] == 1) {
                    bmove -= (target.args()[1])/4*8.0f;
                } else {
                    bmove -= (2 -(Math.abs(target.args()[1]-2)))/2.0f * 4.0f;
                }
            }
        return bmove;
    }

    mHeuristic calcHeuCardMap(int playerIndex, int cardIndex, int[] goalData) {
        ArrayList<mHeuristic> allMHeuristic = new ArrayList<>();

        float topHeu = 1;
        float midHeu = 1;
        float botHeu = 1;

        //Gold Known
        if(goalData[0] == 1 || goalData[1] == 1 || goalData[2] == 1) {
            return new mHeuristic(Move.NewDiscardMove(playerIndex,cardIndex), 2.0f);
        }

        if(goalData[0] == 0) {
            Move topMove = Move.NewMapMove(playerIndex, cardIndex, Board.GoalPosition.TOP);
            topHeu = k1;
            allMHeuristic.add(new mHeuristic(topMove, topHeu));
        }

        if(goalData[1] == 0) {
            Move topMove = Move.NewMapMove(playerIndex, cardIndex, Board.GoalPosition.MIDDLE);
            midHeu = k1;
            allMHeuristic.add(new mHeuristic(topMove, midHeu));
        }

        if(goalData[2] == 0) {
            Move topMove = Move.NewMapMove(playerIndex, cardIndex, Board.GoalPosition.BOTTOM);
            botHeu = k1;
            allMHeuristic.add(new mHeuristic(topMove, botHeu));
        }
        return maxHeu(allMHeuristic);
    }

    mHeuristic calcHeuCardRockFall(ArrayList<Move> targets, boolean isMiner, Board board, int[] goalData) {
        ArrayList<mHeuristic> allMHeuristic = new ArrayList<>();
        float heu;

        for(Move m : targets) {

            heu = 0;
            PathCard c = (PathCard) board.cellAt(m.args()[0],m.args()[1]).card();

            if(!isMiner) {
                int top     = c.topSide().val();
                int right   = c.rightSide().val();
                int bottom  = c.bottomSide().val();

                heu += (top+ right + bottom/6)*4;
                heu += (1+m.args()[0])/9.0f*5.0f;
                if(goalData[0] == 1) {
                    heu += (4-m.args()[1])/4.0f*5.0f;
                } else if(goalData[1] == 1) {
                    heu += (2 -(Math.abs(m.args()[1]-2)))/2.0f * 5.0f;
                } else if(goalData[2] == 1) {
                    heu += m.args()[1] / 4.0f * 5.0f;
                } else {
                    heu += (2 -(Math.abs(m.args()[1]-2.0f)))/2.0f * 3.0f;
                }
      //          System.out.println("Heu Rockfall: "+heu);
            } else {
                int top     = c.topSide().val();
                int right   = c.rightSide().val();
                int bottom  = c.bottomSide().val();
                int left    = c.leftSide().val();

                heu += ((top + right + bottom + left)/(-8.0f))*10.0f;
                heu += (1+m.args()[0])/9.0f*2.0f;
                if(goalData[0] == 1) {
                    heu += (4-m.args()[1])/4.0f*2.0f;
                } else if(goalData[1] == 1) {
                    heu += (2 -(Math.abs(m.args()[1]-2)))/2.0f * 2.0f;
                } else if(goalData[2] == 1) {
                    heu += m.args()[1] / 4.0f * 5.0f;
                } else {
                    heu += (2 -(Math.abs(m.args()[1]-2.0f)))/2.0f * 2.0f;
                }
     //           System.out.println("Heu Rockfall: "+heu);
            }


            allMHeuristic.add(new mHeuristic(m,heu));
        }

        return maxHeu(allMHeuristic);

    }

    ArrayList<Float> calcHeuCardRockFallFloats(ArrayList<Move> targets, boolean isMiner, Board board, int[] goalData) {
        ArrayList<Float> heus = new ArrayList<>();
        float heu;

        for(Move m : targets) {

            heu = 0;
            PathCard c = (PathCard) board.cellAt(m.args()[0],m.args()[1]).card();

            if(!isMiner) {
                int top     = c.topSide().val();
                int right   = c.rightSide().val();
                int bottom  = c.bottomSide().val();

                heu += (top+ right + bottom/6)*4;
                heu += (1+m.args()[0])/9.0f*5.0f;
                if(goalData[0] == 1) {
                    heu += (4-m.args()[1])/4.0f*5.0f;
                } else if(goalData[1] == 1) {
                    heu += (2 -(Math.abs(m.args()[1]-2)))/2.0f * 5.0f;
                } else if(goalData[2] == 1) {
                    heu += m.args()[1] / 4.0f * 5.0f;
                } else {
                    heu += (2 -(Math.abs(m.args()[1]-2.0f)))/2.0f * 3.0f;
                }
     //           System.out.println("Heu Rockfall: "+heu);
            } else {
                int top     = c.topSide().val();
                int right   = c.rightSide().val();
                int bottom  = c.bottomSide().val();
                int left    = c.leftSide().val();

                heu += ((top + right + bottom + left)/(-8.0f))*10.0f;
                heu += (1+m.args()[0])/9.0f*2.0f;
                if(goalData[0] == 1) {
                    heu += (4-m.args()[1])/4.0f*2.0f;
                } else if(goalData[1] == 1) {
                    heu += (2 -(Math.abs(m.args()[1]-2)))/2.0f * 2.0f;
                } else if(goalData[2] == 1) {
                    heu += m.args()[1] / 4.0f * 5.0f;
                } else {
                    heu += (2 -(Math.abs(m.args()[1]-2.0f)))/2.0f * 2.0f;
                }
      //          System.out.println("Heu Rockfall: "+heu);
            }
            heus.add(heu);
        }

        return heus;
    }

    Float calcHeuCellRockFall(Move target, boolean isMiner, Board board, int[] goalData) {
        float heu;

        heu = 0;
        PathCard c = (PathCard) board.cellAt(target.args()[0],target.args()[1]).card();

        if(!isMiner) {
            int top     = c.topSide().val();
            int right   = c.rightSide().val();
            int bottom  = c.bottomSide().val();

            heu += (top+ right + bottom/6)*4;
            heu += (1+target.args()[0])/9.0f*5.0f;
            if(goalData[0] == 1) {
                heu += (4-target.args()[1])/4.0f*5.0f;
            } else if(goalData[1] == 1) {
                heu += (2 -(Math.abs(target.args()[1]-2)))/2.0f * 5.0f;
            } else if(goalData[2] == 1) {
                heu += target.args()[1] / 4.0f * 5.0f;
            } else {
                heu += (2 -(Math.abs(target.args()[1]-2.0f)))/2.0f * 3.0f;
            }
//                System.out.println("Heu Rockfall: "+heu);
        } else {
            int top     = c.topSide().val();
            int right   = c.rightSide().val();
            int bottom  = c.bottomSide().val();
            int left    = c.leftSide().val();

            heu += ((top + right + bottom + left)/(-8.0f))*10.0f;
            heu += (1+target.args()[0])/9.0f*2.0f;
            if(goalData[0] == 1) {
                heu += (4-target.args()[1])/4.0f*2.0f;
            } else if(goalData[1] == 1) {
                heu += (2 -(Math.abs(target.args()[1]-2)))/2.0f * 2.0f;
            } else if(goalData[2] == 1) {
                heu += target.args()[1] / 4.0f * 5.0f;
            } else {
                heu += (2 -(Math.abs(target.args()[1]-2.0f)))/2.0f * 2.0f;
            }
//                System.out.println("Heu Rockfall: "+heu);
        }

        return heu;

    }

    mHeuristic calcHeuCardBlock(int cardIndex, ArrayList<Move> targets, GameLogicController game, ArrayList<rPredict> potFoes) {
        ArrayList<mHeuristic> allMHeuristic = new ArrayList<>();
        float tempHeu;

        for (Move m: targets) {
            tempHeu = 0;

            for(rPredict r: potFoes) {
                if (m.args()[0] == r.targetIndex) {
                    tempHeu = k3;
                    allMHeuristic.add(new mHeuristic(m,tempHeu));
                }
            }
        }

        return maxHeu(allMHeuristic);
    }

    mHeuristic calcHeuCardRepair(int cardIndex, ArrayList<Move> targets, GameLogicController game, ArrayList<rPredict> potFriends) {
        ArrayList<mHeuristic> allMHeuristic = new ArrayList<>();
        float tempHeu;

        for (Move m: targets) {
            tempHeu = 0;

            for(rPredict r: potFriends) {
                if (m.args()[0] == r.targetIndex) {
                    tempHeu = k4;
                    allMHeuristic.add(new mHeuristic(m,tempHeu));
                }
            }
        }

        return maxHeu(allMHeuristic);
    }

    mHeuristic maxHeu(ArrayList<mHeuristic> possibleMoves) {
        mHeuristic bestMove = new mHeuristic(-MAX_VALUE);

        for (mHeuristic curr:possibleMoves) {
            if(curr.heu > bestMove.heu) {
                bestMove = curr;
            }
        }
        return bestMove;

    }

/*    private mHeuristic minHeu(ArrayList<mHeuristic> possibleMoves) {
        mHeuristic bestMove = new mHeuristic(MAX_VALUE);

        for (mHeuristic curr:possibleMoves) {
            if(curr.heu < bestMove.heu) {
                bestMove = curr;
            }
        }
        return bestMove;

    }*/


}
