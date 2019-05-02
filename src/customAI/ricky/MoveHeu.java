/*
 * Authors:
 * Ricky (https://github.com/zyphonGT)
 * Albert (https://github.com/mailmancy)
 */

package customAI.ricky;

import model.Move;

import static java.lang.Float.MAX_VALUE;

public class MoveHeu {

    public Move m;
    public float heu;

    public MoveHeu(Move m, float heu) {
        this.m=m;
        this.heu=heu;
    }

    public MoveHeu(Move m) {
        this.m=m;
        this.heu=-MAX_VALUE;
    }

    public MoveHeu(float heu) {
        this.m=null;
        this.heu=heu;
    }
}
