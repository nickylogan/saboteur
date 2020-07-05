/*
 * Authors:
 * Cen (https://github.com/Bongcen)
 * Gabriel (https://github.com/gabrieldejan17)
 */

package ai.impl.cen;

import model.Board;
import model.Move;

import java.util.ArrayList;
import java.util.List;

public class BoardAnalyzer{


    public static List<Double> DistanceToGoal(Move move, Board board){
        List<Double> list = new ArrayList<>();
        double dGoal1 = Math.sqrt(Math.pow(board.topGoalPosition().x-move.args()[0],2)+ Math.pow(board.topGoalPosition().y-move.args()[1],2));
        double dGoal2 = Math.sqrt(Math.pow(board.middleGoalPosition().x-move.args()[0],2)+ Math.pow(board.middleGoalPosition().y-move.args()[1],2));
        double dGoal3 = Math.sqrt(Math.pow(board.bottomGoalPosition().x-move.args()[0],2)+ Math.pow(board.bottomGoalPosition().y-move.args()[1],2));

        list.add(dGoal1);
        list.add(dGoal2);
        list.add(dGoal3);

        for(int i=0;i<list.size();i++){
      //      System.out.println("This to Goal"+i+1+" "+list.get(i));
        }
        return list;
    }

    public static Double CalcDistance (int x1, int y1, int x2, int y2){
        return Math.sqrt(Math.pow(x2-x1,2)+ Math.pow(y2-y1,2));
    }
}
