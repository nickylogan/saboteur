/*
 * Authors:
 * Ray (https://github.com/rocksus)
 */

package customAI.ray;

import ai.AI;
import model.*;
import model.cards.Card;
import model.cards.PathCard;
import model.cards.PlayerActionCard;

import java.util.*;

import static java.lang.Math.*;

public class HeuristicsAI extends AI {
    public HeuristicsAI(String name) {
        super(name);
    }

    private ArrayList<Move> generatePossiblePaths(int cardIndex, PathCard card) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        card.setRotated(false);
        Set<Position> posNormal = game().board().getPlaceable(card);
        posNormal.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardIndex, p.x, p.y, false)));

        card.setRotated(true);
        Set<Position> posRotated = game().board().getPlaceable(card);
        posRotated.forEach(p -> possibleMoves.add(Move.NewPathMove(index(), cardIndex, p.x, p.y, true)));

        // System.out.print(card + " => ");
        // System.out.print(posNormal);
        // System.out.println(posRotated);
        return possibleMoves;
    }

    private ArrayList<Move> generatePossiblePlayerActions(int cardIndex, PlayerActionCard card) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        for (int i = 0; i < numPlayers; ++i) {
            if (i == index()) continue;
            Player p = game().playerAt(i);
            if (card.type() == Card.Type.REPAIR && p.isRepairable(card.effects())) {
                possibleMoves.add(Move.NewPlayerActionMove(index(), cardIndex, i));
            } else if (card.type() == Card.Type.BLOCK && p.isSabotageable(card.effects()[0])) {
                possibleMoves.add(Move.NewPlayerActionMove(index(), cardIndex, i));
            }
        }
        return possibleMoves;
    }

    private ArrayList<Move> generatePossibleRockfall(int cardIndex) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        Set<Position> positions = game().board().getDestroyable();
        positions.forEach(p -> possibleMoves.add(Move.NewRockfallMove(index(), cardIndex, p.x, p.y)));
        return possibleMoves;
    }

//    private ArrayList<Move> generatePossibleMap(int cardIndex) {
//        ArrayList<Move> possibleMoves = new ArrayList<>();
//        possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.TOP));
//        possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.MIDDLE));
//        possibleMoves.add(Move.NewMapMove(index(), cardIndex, Board.GoalPosition.BOTTOM));
//        return possibleMoves;
//    }
    private double mapRate = 1.8;
    private double trustRate = 0.8;
    private boolean canSee=false;
    private int mapIndex = -1;
    private boolean canMove=false;
    private boolean canBlock=false;
    private boolean canRockfall=false;
    private boolean canRepair=false;
    private boolean goldFound=false;

    private Board oldBoard;
    private ArrayList<int[]> haveSeen;
    private double[] goldProb;
    private double[] Probability;
    private int numPlayers;
    private int myTurn = -1;
    private int shortestPathFrom(Position from, Position to) {
        return 0;
    }

    private double getClosestSabotage(Board board) {
        Set<Position> reachable = board.getReachable();
        double maxH=0;
        double sumH=0;
        for(Position h : reachable) {
            //find closest
            double heu = log1p((goldProb[0]*(((8-h.x))+ 0.2*((4-h.y)))) + (goldProb[1]*(((8-h.x))+0.2*(((2-h.y)<0?-1*(2-h.y):2-h.y)))) + (goldProb[2]*(((8-h.x))+0.2*(h.y))));
            if(maxH<heu) {
                maxH = heu;
            }
            sumH+=heu;
        }
        return (sumH/reachable.size());
    }

    private double getClosest(Board board) {
        Set<Position> reachable = board.getReachable();
        double maxH=0;
        double sumH=0;
        for(Position h : reachable) {
            //find average
            double heu = log1p((goldProb[0]*((9-(8-h.x))+ 0.2*(5-(4-h.y)))) + (goldProb[1]*((9-(8-h.x))+0.2*(5-((2-h.y)<0?-1*(2-h.y):2-h.y)))) + (goldProb[2]*((9-(8-h.x))+0.2*(5-h.y))));
            if(maxH<heu) {
                maxH = heu;
            }
            sumH+=heu;
        }
        return (sumH/reachable.size());
    }

    @Override
    public void initialize() {
        //find out the probability of the players N turns after you
        numPlayers = game().numPlayers();
        haveSeen = new ArrayList<>();
        goldProb = new double[3];
        Probability = new double[numPlayers];
        oldBoard = game().board().copy();
        //sets all of the players to miners at first
        //the lower the value the higher the player is predicted as Saboteur
        for (int i = 0; i < numPlayers; i++) {
            Probability[i] = 0.5;
            haveSeen.add(new int[3]);
        }
        for(int i=0; i<3; i++) {
            //top is 0, middle is 1, bottom is 2
            goldProb[i]=0.1;
        }
        if(role() == Player.Role.SABOTEUR) Probability[index()]=0;
        else if(role() == Player.Role.GOLD_MINER) Probability[index()]=1;
    }

    @Override
    protected void onOtherPlayerMove(Move move) {
        int turnsElapsed = history().moves().size();
        int playerTurn;
//        System.out.println(turnsElapsed);
        playerTurn = (turnsElapsed)%numPlayers;
        double oldVal, newVal;
            switch(move.type()) {
                case DISCARD:
                    //apply decay
                    Probability[playerTurn] -= (0.05 * (1 - Probability[playerTurn]));
                    break;
                case PLAY_MAP:
                    /*ticks the have seen*/
                    haveSeen.get(playerTurn)[move.args()[0]] = 1;
                    //checks if the person is actually going there
                    break;
                case PLAY_PATH:
                    //Checks the heuristic
                    //if the player is lowering the board value, Probability--;
                    //if it gets closer to the goal, probability++;
                    oldVal = getClosest(oldBoard);
                    newVal = getClosest(game().board().copy());
                    Probability[playerTurn] += (newVal - oldVal) * 0.08;
                    break;
                case PLAY_PLAYER:
                    if (move.card().type() == Card.Type.BLOCK) {
                        int target = move.args()[0];
                        Probability[playerTurn] += trustRate*(Probability[playerTurn]-Probability[target])*Probability[playerTurn]+(Probability[playerTurn]-1);
                    }
                    else if (move.card().type() == Card.Type.REPAIR) {
                        int target = move.args()[0];
//                        System.out.println("Targetted "+target);
                        if(Probability[playerTurn]>=0.5 && Probability[target]>=0.5) {
                            Probability[playerTurn]+=0.08;
                            Probability[target]+=0.04;
                        }
                        else if(Probability[playerTurn]<0.5 && Probability[target]<0.5) {
                            Probability[playerTurn]-=0.08;
                            Probability[target]-=0.04;
                        }
                    }
                    //Block & Unblock algorithm for MINER
                    break;
                case PLAY_ROCKFALL:
                    //Checks the heuristic
                    oldVal = getClosest(oldBoard);
                    newVal = getClosest(game().board().copy());
                    Probability[playerTurn] += (newVal - oldVal) * 0.08;
                    break;
            }
            oldBoard = game().board().copy();
    }

    @Override
    protected void onGoalOpen(Board.GoalPosition position, GoalType goalType, boolean permanent) {
        //Executes only when either
        //1. you see the goal yourself
        //2. The path is reachable
        switch(position) {
            case TOP:
                haveSeen.get(myTurn)[0]=1;
//                System.out.println("TOP : "+goalType);
                if(goalType==GoalType.GOLD) {
                    goldFound=true;
                    goldProb[0] = 1;
                    goldProb[1] = 0;
                    goldProb[2] = 0;
                } else if(goalType==GoalType.ROCK && !goldFound) {
                    goldProb[0] = 0;
                    goldProb[1] += 0.2;
                    goldProb[2] += 0.2;
                }
                break;
            case MIDDLE:
                haveSeen.get(myTurn)[1]=1;
//                System.out.println("MIDDLE : "+goalType);
                if(goalType==GoalType.GOLD) {
                    goldFound=true;
                    goldProb[0] = 0;
                    goldProb[1] = 1;
                    goldProb[2] = 0;
                } else if(goalType==GoalType.ROCK && !goldFound) {
                    goldProb[0] += 0.2;
                    goldProb[1] = 0;
                    goldProb[2] += 0.2;
                }
                break;
            case BOTTOM:
                haveSeen.get(myTurn)[2]=1;
//                System.out.println("BOTTOM : "+goalType);
                if(goalType==GoalType.GOLD) {
                    goldFound=true;
                    goldProb[0] = 0;
                    goldProb[1] = 0;
                    goldProb[2] = 1;
                } else if(goalType==GoalType.ROCK && !goldFound) {
                    goldProb[0] += 0.2;
                    goldProb[1] += 0.2;
                    goldProb[2] = 0;
                }
                break;
        }
//        System.out.println(goldProb[0]+" "+goldProb[1]+" "+goldProb[2]);
    }

    @Override
    protected Move makeDecision() {
//        myTurn = history().moves().size() % numPlayers;
        myTurn = index();
        /* block the nearest + most effective player (as in, people who arent blocked) */
        ArrayList<Move> pathMoves = new ArrayList<>();
        ArrayList<Move> playMoves = new ArrayList<>();
        ArrayList<Move> discardMoves = new ArrayList<>();
        Map<Integer,Card> destroyCards = new HashMap<Integer, Card>();
        Map<Integer,Card> repairCards = new HashMap<Integer, Card>();
        Map<Integer,Card> blockCards = new HashMap<Integer,Card>();
        Map<Integer,Card> pathCards = new HashMap<Integer, Card>();
        Set<Position> reachable = game().board().getReachable();
        canSee=false;
        canMove=false;
        canBlock=false;
        canRockfall=false;
        canRepair=false;
        int len = hand().size();
        for (int i = 0; i < len; ++i) {
            Card c = hand().get(i);
            if (c instanceof PathCard && !isSabotaged()) {
                pathCards.put(i,c);
//                pathMoves.addAll(generatePossiblePaths(i, (PathCard) c));
                canMove = true;
            }
            if (c instanceof PlayerActionCard) {
//                playMoves.addAll(generatePossiblePlayerActions(i, (PlayerActionCard) c));
                if(c.type()== Card.Type.BLOCK) {
                    blockCards.put(i, c);
                    canBlock=true;
                }
                else if(c.type()== Card.Type.REPAIR) {
                    repairCards.put(i, c);
                    canRepair=true;
                }
            }
            if (c.type() == Card.Type.MAP) {
//                playMoves.addAll(generatePossibleMap(i));
                mapIndex = i;
                canSee = true;
            }
            if (c.type() == Card.Type.ROCKFALL) {
                destroyCards.put(i,c);
//                playMoves.addAll(generatePossibleRockfall(i));
                canRockfall = true;
            }
            discardMoves.add(Move.NewDiscardMove(index(), i));
        }

        if(canSee) {
            //sum of all heuristics
            double sumGoal[] = new double[3];
            for (Position h : reachable) {
                sumGoal[0]+=(9-(8-h.x)+ 5-(4-h.y));
                sumGoal[1]+=(9-(8-h.x)+ 5-(2-h.y));
                sumGoal[2]+=(9-(8-h.x)+ 5-(-h.y));
            }
            //update goldProb
            for(int i=0; i<3; i++) {
                int seenSum=0;
                for(int j=0; j<numPlayers; j++) {
                    if(j!=myTurn) {
                        if(haveSeen.get(j)[i]==1) seenSum++;
                    }
                }
                goldProb[i] = (i==1?0.5:1)*(1-haveSeen.get(myTurn)[i])*(mapRate*(seenSum/numPlayers)+(sumGoal[i]/10));
            }
        }

//    System.out.println(hand());
//    System.out.println(pathMoves);


        if (role() == Player.Role.GOLD_MINER) {
            //Path
            if(canSee && !goldFound) {
                int selection=0;
                double maxProb=goldProb[0];
                for(int i=0; i<3; i++) {
                    if(goldProb[i]>maxProb) {
                        maxProb = goldProb[i];
                        selection=i;
                    }
                }
                Move look = Move.NewMapMove(index(), mapIndex, Board.GoalPosition.TOP);
                switch(selection) {
                    case 0:
                        look = Move.NewMapMove(index(), mapIndex, Board.GoalPosition.TOP);
                        break;
                    case 1:
                        look = Move.NewMapMove(index(), mapIndex, Board.GoalPosition.MIDDLE);
                        break;
                    case 2:
                        look = Move.NewMapMove(index(), mapIndex, Board.GoalPosition.BOTTOM);
                        break;
                }
                return look;
            }
            if(canMove) {
                int globalIndex = 0;
                double maxH = 0;
                double oldH;
                Position bp = new Position(0,0);
                boolean rotate = false;
                //Find best path to place;
                Board board = game().board().copy();
                oldH = getClosest(board);
//                System.out.print(getClosest(board)+" : ");
                for( Map.Entry<Integer, Card> p : pathCards.entrySet()) {
                    int index = p.getKey();
                    Card path = p.getValue();
                    ((PathCard)path).setRotated(false);
                    Set<Position> placeable = game().board().getPlaceable((PathCard)path);
                    for (Position h : placeable) {
                        Board simulated = board.simulatePlaceCardAt((PathCard)path, h.x, h.y);
//                        System.out.println(path.type());
                        double cH = getClosest(simulated);
                        if(maxH<cH) {
                            bp=h;
                            globalIndex = index;
                            maxH = cH;
                            rotate=false;
                        }
                    }
                    ((PathCard) path).setRotated(true);
                    placeable = game().board().getPlaceable((PathCard)path);
                    for (Position h : placeable) {
                        Board simulated = board.simulatePlaceCardAt((PathCard)path, h.x, h.y);
//                        System.out.println(simulated);
                        double cH = getClosest(simulated);
                        if(maxH<cH) {
                            bp=h;
                            globalIndex = index;
                            rotate=true;
                            maxH = cH;
                        }
                    }
                }
//                System.out.println(oldH+": "+maxH+" ("+abs(maxH-oldH)+")");
                oldBoard = game().board().copy();
                if(maxH>0 && abs(maxH-oldH)>0.01)
                    return Move.NewPathMove(index(), globalIndex, bp.x, bp.y, rotate);
            }
            //Unblock
            if(canRepair) {
                double blockMore = 0.0;
                int targetPlayer = -1;
                int cardIndex = 0;
                for(int i=0; i<numPlayers; i++) {
                    //heal self
                    if(i == myTurn) {
                        for(Map.Entry<Integer, Card> r : repairCards.entrySet()) {
                            int index = r.getKey();
//                            System.out.println(Arrays.toString(game().playerAt(index()).sabotaged()));
                            PlayerActionCard repair = (PlayerActionCard) r.getValue();
                            if (game().playerAt(index()).isRepairable(repair.effects())) {
                                return Move.NewPlayerActionMove(index(),index, index());
                            }
                        }
                    }
                    else if(i!=myTurn) {
                        for(Map.Entry<Integer, Card> r : repairCards.entrySet()) {
                            int index = r.getKey();
                            PlayerActionCard repair = (PlayerActionCard) r.getValue();
//                            System.out.println(Arrays.toString(game().playerAt(i).sabotaged()));
                            if (game().playerAt(i).isRepairable(repair.effects())) {
                                //calculate the should i repair? function
                                double shouldIBlockMore = Probability[i]+(0.03*(numPlayers-i));
                                if(blockMore<shouldIBlockMore) {
                                    blockMore = shouldIBlockMore;
                                    targetPlayer = i;
                                    cardIndex = index;
                                }
                            }
                        }
                    }
                }
                if(blockMore>0.75 && targetPlayer>=0)
                    return Move.NewPlayerActionMove(index(), cardIndex, targetPlayer);
            }
            //Block Enemy
            if(canBlock) {
                double blockMore = 0.0;
                int targetPlayer = -1;
                int cardIndex = 0;
                for (int i = 0; i < numPlayers; i++) {
                    if (i != myTurn) {
                        for (Map.Entry<Integer, Card> b : blockCards.entrySet()) {
                            int index = b.getKey();
                            PlayerActionCard block = (PlayerActionCard) b.getValue();
                            if (game().playerAt(i).isSabotageable(block.effects()[0])) {
                                //calculate the should i repair? function
                                double shouldIBlockMore = (1 - Probability[i]) * ((1 - Probability[i]) + 0.2 * (numPlayers - i));
                                if (blockMore < shouldIBlockMore) {
                                    blockMore = shouldIBlockMore;
                                    targetPlayer = i;
                                    cardIndex = index;
                                }
                            }
                        }
                    }
                }
                if(blockMore>0.6 && targetPlayer>=0)
                    return Move.NewPlayerActionMove(index(), cardIndex, targetPlayer);
            }
            //Fix (Rockfall)
            if(canRockfall) {
                int globalIndex = 0;
                double maxH = 0.0;
                Position bp = new Position(0,0);
                //Find best path to place;
                Board board = game().board().copy();
                for( Map.Entry<Integer, Card> d : destroyCards.entrySet()) {
                    int index = d.getKey();
                    Set<Position> destroyable = game().board().getDestroyable();
                    for (Position h : destroyable) {
                        Board simulated = board.simulateRemoveCardAt(h.x, h.y);
//                        System.out.println(path.type());
                        double cH = getClosest(simulated);
                        if(maxH<cH) {
                            bp=h;
                            globalIndex = index;
                            maxH = cH;
                        }
                    }
                }
                oldBoard = game().board().copy();
                if(maxH>0)
                    return Move.NewRockfallMove(index(), globalIndex, bp.x, bp.y);
            }
            //See map

            //Conserve (Find the best card to discard)

            //calculate the heuristics and adjust accordingly,
            //choose between playing a card or discarding.
        } else if (role() == Player.Role.SABOTEUR) {
            //See map
            if(canSee && !goldFound) {
                int selection=0;
                //specific for saboteurs
                double maxProb=goldProb[0];
                for(int i=0; i<3; i++) {
                    if(goldProb[i]>maxProb) {
                        maxProb = goldProb[i];
                        selection=i;
                    }
                }
                Move look = Move.NewMapMove(index(), mapIndex, Board.GoalPosition.TOP);
                switch(selection) {
                    case 0:
                        look = Move.NewMapMove(index(), mapIndex, Board.GoalPosition.TOP);
                        break;
                    case 1:
                        look = Move.NewMapMove(index(), mapIndex, Board.GoalPosition.MIDDLE);
                        break;
                    case 2:
                        look = Move.NewMapMove(index(), mapIndex, Board.GoalPosition.BOTTOM);
                        break;
                }
                return look;
            }
            //Path
            if(canMove) {
                int globalIndex = 0;
                double maxH = 0.0;
                Position bp = new Position(0,0);
                boolean rotate = false;
                //Find best path to place;
                Board board = game().board().copy();
                double oldH = getClosestSabotage(board);
                for( Map.Entry<Integer, Card> p : pathCards.entrySet()) {
                    int index = p.getKey();
                    Card path = p.getValue();
                    ((PathCard)path).setRotated(false);
                    Set<Position> placeable = game().board().getPlaceable((PathCard)path);
                    for (Position h : placeable) {
                        Board simulated = board.simulatePlaceCardAt((PathCard)path, h.x, h.y);
//                        System.out.println(path.type());
                        double cH = getClosestSabotage(simulated);
                        if(maxH<cH) {
                            bp=h;
                            globalIndex = index;
                            maxH = cH;
                            rotate=false;
                        }
                    }
                    ((PathCard) path).setRotated(true);
                    placeable = game().board().getPlaceable((PathCard)path);
                    for (Position h : placeable) {
                        Board simulated = board.simulatePlaceCardAt((PathCard)path, h.x, h.y);
//                        System.out.println(simulated);
                        double cH = getClosestSabotage(simulated);
                        if(maxH<cH) {
                            bp=h;
                            globalIndex = index;
                            rotate=true;
                            maxH = cH;
                        }
                    }
                }
                oldBoard = game().board().copy();
                if(maxH>0 && abs(maxH-oldH)>0.01)
                    return Move.NewPathMove(index(), globalIndex, bp.x, bp.y, rotate);
            }
            //Unblock Friend
            if(canRepair) {
                double blockMore = 0.0;
                int targetPlayer = -1;
                int cardIndex = 0;
                for(int i=0; i<numPlayers; i++) {
                    if(i == myTurn) {
                        for(Map.Entry<Integer, Card> r : repairCards.entrySet()) {
                            int index = r.getKey();
                            PlayerActionCard repair = (PlayerActionCard) r.getValue();
                            if (game().playerAt(myTurn).isRepairable(repair.effects())) {
                                return Move.NewPlayerActionMove(index(),index, index());
                            }
                        }
                    }
                    else if(i!=myTurn) {
                        for(Map.Entry<Integer, Card> r : repairCards.entrySet()) {
                            int index = r.getKey();
                            PlayerActionCard repair = (PlayerActionCard) r.getValue();
                            if (game().playerAt(i).isRepairable(repair.effects())) {
                                //calculate the should i repair? function
                                double shouldIBlockMore = (1-Probability[i])+(0.03*(numPlayers-i));
                                if(blockMore<shouldIBlockMore) {
                                    blockMore = shouldIBlockMore;
                                    targetPlayer = i;
                                    cardIndex = index;
                                }
                            }
                        }
                    }
                }
                if(blockMore>0.75 && targetPlayer>=0)
                    return Move.NewPlayerActionMove(index(), cardIndex, targetPlayer);
            }
            //Block Enemy
            if(canBlock) {
                double blockMore = 0.0;
                int targetPlayer = -1;
                int cardIndex = 0;
                for (int i = 0; i < numPlayers; i++) {
                    if (i != myTurn) {
                        for (Map.Entry<Integer, Card> b : blockCards.entrySet()) {
                            int index = b.getKey();
                            PlayerActionCard block = (PlayerActionCard) b.getValue();
                            if (game().playerAt(i).isSabotageable(block.effects()[0])) {
                                //calculate the should i repair? function
                                double shouldIBlockMore = (Probability[i]) * ((Probability[i]) + 0.2 * (numPlayers - i));
                                if (blockMore < shouldIBlockMore) {
                                    blockMore = shouldIBlockMore;
                                    targetPlayer = i;
                                    cardIndex = index;
                                }
                            }
                        }
                    }
                }
                if(blockMore>0.6 && targetPlayer>=0)
                    return Move.NewPlayerActionMove(index(), cardIndex, targetPlayer);
            }
            //Sabotage (Rockfall)
            if(canRockfall) {
                int globalIndex = 0;
                double maxH = 0;
                Position bp = new Position(0,0);
                //Find best path to place;
                Board board = game().board().copy();
                for( Map.Entry<Integer, Card> d : destroyCards.entrySet()) {
                    int index = d.getKey();
                    Set<Position> destroyable = game().board().getDestroyable();
                    for (Position h : destroyable) {
                        Board simulated = board.simulateRemoveCardAt(h.x, h.y);
//                        System.out.println(path.type());
                        double cH = getClosestSabotage(simulated);
                        if(maxH<cH) {
                            bp=h;
                            globalIndex = index;
                            maxH = cH;
                        }
                    }
                }
                oldBoard = game().board().copy();
                if(maxH>0)
                    return Move.NewRockfallMove(index(), globalIndex, bp.x, bp.y);
            }
            //Conserve (Find the best card to discard

            //calculate the heuristics and adjust accordingly,
            //choose between playing a card or discarding.
        }
        return discardMoves.get(0);
    }


}
