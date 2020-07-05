/*
 * Authors:
 * Cen (https://github.com/Bongcen)
 * Gabriel (https://github.com/gabrieldejan17)
 */

package ai.impl.cen;

import model.Move;

import static java.lang.Float.MAX_VALUE;

public class mHeuristic {

    public Move m;
    public float heu;

    public mHeuristic(Move m, float heu) {
        this.m=m;
        this.heu=heu;
    }

    public mHeuristic(Move m) {
        this.m=m;
        this.heu=-MAX_VALUE;
    }

    public mHeuristic(float heu) {
        this.m=null;
        this.heu=heu;
    }
}
