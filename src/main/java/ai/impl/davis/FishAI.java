/*
 * Authors:
 * Davis (https://github.com/cokpsz)
 * Regy
 */

package ai.impl.davis;

import ai.AI;
import model.*;
import model.cards.*;

import java.util.*;

public class FishAI extends AI {
    public static int playAsMiner = 0;
    public static int playAsSaboteur = 0;
    public static int winAsMiner = 0;
    public static int winAsSaboteur = 0;

    private Boolean[] locatedGoals;
    private double k0 = 0.5;
    private double k1 = k0 + 2.5;
    private double k2 = k0 - 0.1;
    private double k3 = k2;
    private double k4 = k2;
    private double k5 = k2/8;
    private double c1 = 5 * k0;
    private double c2 = c1 + 0.1;
    private double c3 = 0.5;
    private double c4 = c3;
    private Teammate[] teammate;

    private class Teammate {
        private int playerID;
        private Role role;
        private double confidence;
        private double score;
        public Teammate(int playerID) {
            this.playerID = playerID;
            this.confidence = c1;
            this.score = c1;
        }
        public double getConfidence() {return confidence;}
        public double getScore() {return score;}
        public void setConfidence(double confidence) {this.confidence = confidence;}
        public void setScore(double score) {this.score = score;}
        public Role getRole() {return role;}
        public void setRole(Role role) {this.role = role;}
        public int getPlayerID() {return this.playerID;}
    }
    public FishAI(String name) {
        super(name);
        locatedGoals = new Boolean[3];
        teammate = new Teammate[5]; //game().numPlayers()
        Arrays.fill(locatedGoals, Boolean.TRUE); //assume semuanya E
        for (int i=0; i<5; i++) {
            teammate[i] = new Teammate(i);
            if (i == index()) {
                teammate[i].setScore(Double.POSITIVE_INFINITY);
                teammate[i].setScore(Double.POSITIVE_INFINITY);
            }
        }
    }

    private ArrayList<Move> generatePossiblePaths(int cardHandIndex, PathCard card) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        card.setRotated(false);
        Set<Position> posNormal = game().board().getPlaceable(card);
        posNormal.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardHandIndex, p.x, p.y, false)));
        card.setRotated(true);
        Set<Position> posRotated = game().board().getPlaceable(card);
        posRotated.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardHandIndex, p.x, p.y, true)));
        if (!isSabotaged()) return possibleMoves;
        else return new ArrayList<>();
    }
    private ArrayList<Move> generatePossiblePlayerActions(int cardHandIndex, PlayerActionCard card) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int numPlayers = game().numPlayers();
        for (int i = 0; i < numPlayers; ++i) {
            if (i == index()) continue;
            Player p = game().playerAt(i);
            if (card.type() == Card.Type.REPAIR && p.isRepairable(card.effects())) {
                possibleMoves.add(Move.NewPlayerActionMove(index(), cardHandIndex, i));
            } else if (card.type() == Card.Type.BLOCK && p.isSabotageable(card.effects()[0])) {
                possibleMoves.add(Move.NewPlayerActionMove(index(), cardHandIndex, i));
            }
        }
        return possibleMoves;
    }
    private ArrayList<Move> generatePossibleRockfall(int cardHandIndex) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        Set<Position> positions = game().board().getDestroyable();
        positions.forEach(p -> possibleMoves.add(Move.NewRockfallMove(index(), cardHandIndex, p.x, p.y)));
        return possibleMoves;
    }
    private ArrayList<Move> generatePossibleMap(int cardIndex) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.TOP));
        possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.MIDDLE));
        possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.BOTTOM));
        return possibleMoves;
    }
    private void ensureSize(ArrayList<?> list, int size) {
        list.ensureCapacity(size);
        while (list.size() < size) {
            list.add(null);
        }
    }
    private void printList(ArrayList<?> list) {
        for(int i=0; i<list.size(); i++) {
            // System.out.print(list.get(i).toString() + " ");
        }
    }



    @Override
    protected Move makeDecision() {
        locateGoals();
        int len = hand().size();
        ArrayList<Double> heucard = new ArrayList<>();
        ArrayList<Move> bestMove = new ArrayList<>();
        for (int i = 0; i<hand().size(); i++) heucard.add(0.0);
        ensureSize(bestMove, len);

        for (int ca = 0; ca < len; ca++) {
            Card card = hand().get(ca);
            //1. Check card type of every move
            //heucell corresponds to every move
            if (card.type() == Card.Type.PATHWAY || card.type() == Card.Type.DEADEND) {
                ArrayList<Move> moves = generatePossiblePaths(ca,(PathCard) card);
                //generate possible moves from this particular card
                for (Move i: moves) {
                    PathCard c =  (PathCard) card;
                    double bmove = getPathCardHeucell(role(),c,i);
                    if (bmove > heucard.get(ca)) {
                        heucard.set(ca, bmove);
                        bestMove.set(ca, i);
                    }
                }
            }
                else if (card.type() == Card.Type.MAP) {
                    ArrayList<Move> moves = generatePossibleMap(ca);
                    for (Move i: moves) {
                        double bmove = 0.0;
                        BoardActionCard c = (BoardActionCard) i.card();
                        int args[] = i.args();
                        if (args[0] == 0) {
                            bmove += (1 + 0.5 * (locatedGoals[0] ? 1 : 0)) * k1;
                        } else if (args[0] == 1) {
                            bmove += (1 + 0.5 * (locatedGoals[1] ? 1 : 0)) * k1;
                        } else if (args[0] == 2) {
                            bmove += (1 + 0.5 * (locatedGoals[2] ? 1 : 0)) * k1;
                        }
                        if (bmove > heucard.get(ca)) {
                            heucard.set(ca, bmove);
                            bestMove.set(ca, i);
                        }
                    }
                    locateGoals();
                }
                    else if (card.type() == Card.Type.ROCKFALL) {
                        ArrayList<Move> moves = generatePossibleRockfall(ca);
                        for (Move i: moves) {
                            double bmove = 0.0;
                            int xk = i.args()[0];
                            int yk = i.args()[1];
                            if (xk < 6) {
                                bmove = 0.5 * k2;
                            } else {
                                int a2 = game().board().cellAt(xk, yk).topSide().val();
                                int a3 = game().board().cellAt(xk, yk).rightSide().val();
                                int a4 = game().board().cellAt(xk, yk).bottomSide().val();
                                int a5 = game().board().cellAt(xk, yk).leftSide().val();
                                try {
                                    bmove = a2 + a3 + a4 + a5;
                                } catch (NullPointerException e) {
                                    bmove = 0;
                                }
                            }
                            if (bmove > heucard.get(ca)) {
                                heucard.set(ca, bmove);
                                bestMove.set(ca, i);
                            }
                        }
                    }
                        else if (card.type() == Card.Type.BLOCK || card.type() == Card.Type.REPAIR) {
                            ArrayList<Move> moves = generatePossiblePlayerActions(ca, (PlayerActionCard) card);
                            for (Move i : moves) {
                                double bmove = 0.0;
                                Card c = card;
                                if (c.type() == Card.Type.BLOCK) {
                                    ArrayList<Teammate> enemies = new ArrayList<>();
                                    for (int l = 0; l < game().numPlayers(); l++) {
                                        if (teammate[l].getRole() != role()) enemies.add(teammate[l]);
                                    }
                                    int maxBlocked = 0;
                                    for (Teammate e : enemies) {
                                        Player p = game().playerAt(e.getPlayerID());
                                        if (p.sabotaged().length > maxBlocked) {
                                            maxBlocked = p.sabotaged().length;
                                        }
                                    }
                                    if (maxBlocked > 1) bmove = 0.5 * k3;
                                    else bmove = k3;
                                }
                                else if (c.type() == Card.Type.REPAIR) {
                                    ArrayList<Teammate> friends = new ArrayList<>();
                                    for (int l = 0; l < game().numPlayers(); l++) {
                                        if (teammate[l].getRole() == role()) friends.add(teammate[l]);
                                    }
                                    PlayerActionCard pac = (PlayerActionCard) c;
                                    Tool[] t = pac.effects();
                                    Integer[] x = new Integer[5];
                                    Arrays.fill(x, 0);
                                    for (Tool tool : t) {
                                        if (tool == Tool.CART) x[2] = 1;
                                        if (tool == Tool.LANTERN) x[3] = 1;
                                        if (tool == Tool.PICKAXE) x[4] = 1;
                                    }
                                    int maxZ = 0;
                                    for (Teammate e : friends) {
                                        int currRepaired;
                                        Player p = game().playerAt(e.getPlayerID());
                                        Integer[] b = new Integer[5];
                                        Arrays.fill(b,0);
                                        for (Tool tool : p.sabotaged()) {
                                            if (tool == Tool.CART) b[2] += 1;
                                            if (tool == Tool.LANTERN) b[3] += 1;
                                            if (tool == Tool.PICKAXE) b[4] += 1;
                                        }
                                        currRepaired = b[2] * x[2] + b[3] * x[3] + b[4] * x[4];
                                        if (currRepaired > maxZ) maxZ = currRepaired;
                                    }
                                    if (maxZ == 2) bmove = 2 * k4; else bmove = 0.5 * k4;
                                }
                                if (bmove > heucard.get(ca)) {
                                    heucard.set(ca, bmove);
                                    bestMove.set(ca, i);
                                }
                            }
                        }
        }

        /**
         * TODO:
         * 1. implement heucell and heucard
         * 2. Implement the rest of the algorithims similar to path and deadend cards
         * 3. DETERMINE THE FREAKING OPTIMUM CONSTANTS
         * 4. Do playtest using CLIMain (NO.)
         */
        Move returnee = Move.NewDiscardMove(index(), 0);
        double heucardMax = Double.NEGATIVE_INFINITY;
        int handMax = 0, handMin = 0;
        double heucardMin = Double.POSITIVE_INFINITY;
        for (int i=0; i<hand().size(); i++) {
            if (heucardMax < heucard.get(i)) {
                heucardMax = heucard.get(i);
                handMax = i;
            }
            if (heucardMin > heucard.get(i)) {
                heucardMin = heucard.get(i);
                handMin = i;
            }
        }
        if (heucardMin > k5) {
            returnee = Move.NewDiscardMove(index(), handMin);
        } else if (bestMove.get(handMax) != null) returnee = bestMove.get(handMax);


       /* System.out.println("-----------------------------------");
        System.out.println("         " + name() + ":" + index());
        System.out.println("-----------------------------------");
        System.out.println();
        System.out.println("Card heuristics: ");
        printList(heucard);
        System.out.println();
        System.out.println("HandMax = " + handMax);
        System.out.println("HandMin = " + handMin);
        System.out.println("Goal1 : " + knownGoals().get(Board.GoalPosition.TOP));
        System.out.println("Goal2 : " + knownGoals().get(Board.GoalPosition.MIDDLE));
        System.out.println("Goal3 : " + knownGoals().get(Board.GoalPosition.BOTTOM));
        System.out.println(returnee.toString());

        */

        return returnee;

    }

    private boolean isTeammate(int targetPlayerIndex) {
        for (int i=0; i<game().numPlayers(); i++) {
            if (i != index()) {
                if (teammate[i].getRole() == role()) return true;
            }
        }
        return false;
    }
    private void classifyPlayers() {
        Arrays.sort(teammate,(o1, o2) -> {return (int) (o1.getConfidence() - o2.getConfidence());});
        int sab = game().numSaboteurs();
        if (role() == Role.SABOTEUR) sab-=1;
        for (int i=0; i<game().numPlayers(); i++) {
            if (i != index()) {
                if (sab > 0) {
                    teammate[i].setRole(Role.SABOTEUR);
                    sab-=1;
                } else {
                    teammate[i].setRole(Role.GOLD_MINER);
                }
            }
        }
    }

    private void setPlayerConfidence(int playerId, double score) {
        double maxScore = 0.0;
        for (int i=0; i<game().numPlayers(); i++) {
            if (i != this.index() && teammate[i].getScore() > maxScore ) {
                maxScore = teammate[i].getScore();
            }
        }
        teammate[playerId].setConfidence(teammate[playerId].getScore()/maxScore);
    }
    private void setPlayerScore(int playerID, double score) {
        teammate[playerID].setScore(score);
        setPlayerConfidence(playerID,score);
    }
    private void locateGoals() {
        knownGoals().forEach((goalPosition, goalType) -> {
            if (goalPosition == Board.GoalPosition.TOP) {
                if (goalType == GoalType.ROCK) {
                    locatedGoals[0] = false;
                } else {
                    locatedGoals[0] = true;
                    locatedGoals[1] = false;
                    locatedGoals[2] = false;
                }
            }
            if (goalPosition == Board.GoalPosition.MIDDLE) {
                if (goalType == GoalType.ROCK) {
                    locatedGoals[1] = false;
                } else {
                    locatedGoals[0] = false;
                    locatedGoals[1] = true;
                    locatedGoals[2] = false;
                }
            }
            if (goalPosition == Board.GoalPosition.BOTTOM) {
                if (goalType == GoalType.ROCK) {
                    locatedGoals[2] = false;
                } else {
                    locatedGoals[0] = false;
                    locatedGoals[1] = false;
                    locatedGoals[2] = true;
                }

            }
        });
    }
    private int getEffectiveMoveSize() {
        int e = 0;
        for (int i=0; i<=2; i++) {
            if (locatedGoals[i]) e++;
        }
        return e;

    }
    private void prediction(int Ny, Move move) {
        Card c = move.card();
        if (move.type() == Move.Type.PLAY_PATH) {
               double z2 = getPathCardHeucell(this.role(),(PathCard) c,move);
               ArrayList<Move> possiblePaths = generatePossiblePaths(c.id(),(PathCard) c);
               double z1 = 0.0;
               for (Move i: possiblePaths){
                   double currHeucell = getPathCardHeucell(this.role(),(PathCard) c,i);
                   if (currHeucell > z1) {
                        z1 = currHeucell;
                   }
               }
               double score = 0.0;
               if (z1 == z2 && role() == Role.GOLD_MINER) score += z1;
               else if (z1 == z2 && role() == Role.SABOTEUR) score -= z1;
               else if (z1 > z2 && role() == Role.GOLD_MINER) score -= (z1 - z2);
               else if (z1 > z2 && role() == Role.SABOTEUR) score -= (z1 + z2);
               setPlayerScore(Ny, score);
        }
        else if (move.type() == Move.Type.PLAY_ROCKFALL) {
            History h = history();
            Move prevMove = h.moves().get(h.moves().size()-2);
            if (prevMove.card().type() == Card.Type.PATHWAY && !prevMove.equals(move) || prevMove == null) {
                setPlayerConfidence(Ny, 0);
            }
            else {
                double maxScore = 0.0;
                for (int i=0; i<game().numPlayers(); i++) {
                    if (i != this.index() && teammate[i].getScore() > maxScore ) {
                        teammate[i].setScore(maxScore);
                    }
                }
                setPlayerScore(Ny,maxScore);
            }
        }
        else if (move.type() == Move.Type.PLAY_PLAYER && c.type() == Card.Type.BLOCK) {
            int z_target = move.args()[0];
            if (isTeammate(z_target)) {
                setPlayerScore(Ny,teammate[Ny].getScore() - c2);
                setPlayerScore(z_target, teammate[z_target].getScore() + c2);
            } else {
                setPlayerScore(Ny,teammate[Ny].getScore() + c2);
                setPlayerScore(z_target, teammate[z_target].getScore() - c2);
            }
        }
        else if (move.type() == Move.Type.PLAY_PLAYER && c.type() == Card.Type.REPAIR) {
            int z_target = move.args()[0];
            if (isTeammate(z_target)) {
                setPlayerScore(Ny,teammate[Ny].getScore() + c3);
                setPlayerScore(z_target, teammate[z_target].getScore() + c3);
            } else {
                setPlayerScore(Ny,teammate[Ny].getScore() - c3);
                setPlayerScore(z_target, teammate[z_target].getScore() - c3);
            }
        }
        else if (move.type() == Move.Type.DISCARD) {
            if (teammate[Ny].getRole() == Role.GOLD_MINER) setPlayerScore(Ny, teammate[Ny].getScore() - c4);
            else setPlayerScore(Ny, teammate[Ny].getScore() + c4);
        }
        classifyPlayers();
    }
    @Override
    protected void onOtherPlayerMove (Move move) {
        locateGoals();
        prediction(move.playerIndex(), move);
    }
    private double getPathCardHeucell(Role playerRole, PathCard card, Move move) {
        double bmove = 0.0;
        int[] move_details = move.args();
        int xk = move_details[0];
        int yk = move_details[1];
        int x2 = card.topSide().val();
        int x3 = card.rightSide().val();
        int x4 = card.bottomSide().val();
        int x5 = card.leftSide().val();
        //executions
        if (yk == 0 && x3 > 0 && x4 > 0) bmove += 0.3 * (x3 + x4);
         else if (yk >= 1 && yk <= 3) bmove += 0.3 * (x2+x3+x4) + 0.1;
             else if (yk == 4 && x3 > 0 && x2 > 0) bmove += 0.3 * (x2+x3);
        if (card.type() == Card.Type.PATHWAY && playerRole == Role.GOLD_MINER) bmove += 0.1 * (1 + xk)/(double) getEffectiveMoveSize();
            else if (card.type() == Card.Type.PATHWAY && playerRole == Role.SABOTEUR) bmove += 0.1 * (9 - xk)/(double) getEffectiveMoveSize();
                else if (card.type() == Card.Type.DEADEND && playerRole == Role.GOLD_MINER) bmove += 0.1 * (9 - xk)/(double) getEffectiveMoveSize();
                    else if (card.type() == Card.Type.DEADEND && playerRole == Role.SABOTEUR) bmove += 0.1 * (1 + xk)/(double) getEffectiveMoveSize();
        bmove += k0;
        return bmove;
    }

    @Override
    public void initialize() {
        playAsSaboteur += role() == Role.SABOTEUR ? 1 : 0;
        playAsMiner += role() == Role.GOLD_MINER ? 1 : 0;
    }

    @Override
    protected void onGameFinished(Role role, int lastPlayer) {
        if (role == role() && role == Role.SABOTEUR) ++winAsSaboteur;
        if (role == role() && role == Role.GOLD_MINER) ++winAsMiner;
    }
}
