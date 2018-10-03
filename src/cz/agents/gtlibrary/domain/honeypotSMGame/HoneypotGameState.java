package cz.agents.gtlibrary.domain.honeypotSMGame;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.ImmediateActionOutcomeProvider;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.ArrayList;
import java.util.List;

public class HoneypotGameState extends SimultaneousGameState implements ImmediateActionOutcomeProvider {

//    protected HoneypotGameNode[] possibleNodes;
//    protected boolean[] honeypots;
//    protected int[] attackedNodes;
//    protected boolean[] observedHoneypots;

    protected List<Action> sequence;
    protected boolean isPlayerToMoveDefender;
//    protected double attackerBudget;
//    protected double defenderBudget;
//    protected double uniformAttackCost;
    protected double attackerReward;
//    protected double highestValueReceived = Integer.MIN_VALUE;
//    protected int lastDefendedNode = Integer.MIN_VALUE;
//    protected int remainingAttacks;

    public HoneypotGameState() {
        super(HoneypotGameInfo.ALL_PLAYERS);

        this.depth = 0;
        this.isPlayerToMoveDefender = true;
        this.attackerReward = 0.0;
        this.sequence = new ArrayList<Action>();


//        this.possibleNodes = possibleNodes;
//        this.honeypots = new boolean[possibleNodes.length];
//        this.attackedNodes = new int[possibleNodes.length];
//        this.observedHoneypots = new boolean[possibleNodes.length];
//        this.playerToMove = HoneypotGameInfo.DEFENDER;
//        this.highestValueReceived = Integer.MIN_VALUE;
//
//        this.remainingAttacks = HoneypotGameInfo.attacksAllowed;
//        this.attackerBudget = HoneypotGameInfo.initialAttackerBudget;
//        this.defenderBudget = HoneypotGameInfo.initialDefenderBudget;
////        this.uniformAttackCost = HoneypotGameInfo.uniformAttackCost;
    }

    protected HoneypotGameState(HoneypotGameState gameState) {
        super(gameState);

        this.isPlayerToMoveDefender = gameState.isPlayerToMoveDefender;
        this.depth = gameState.depth;
        this.attackerReward = gameState.attackerReward;
        this.sequence = new ArrayList<Action>(gameState.sequence);

//        this.possibleNodes = gameState.possibleNodes;//Arrays.copyOf(gameState.possibleNodes, gameState.possibleNodes.length);
////        this.possibleNodes = Arrays.copyOf(gameState.possibleNodes, gameState.possibleNodes.length);
//        this.honeypots = Arrays.copyOf(gameState.honeypots, gameState.honeypots.length);
//        this.attackedNodes = Arrays.copyOf(gameState.attackedNodes, gameState.attackedNodes.length);
//        this.observedHoneypots = Arrays.copyOf(gameState.observedHoneypots, gameState.observedHoneypots.length);
//        this.playerToMove = gameState.playerToMove;
//        this.attackerBudget = gameState.attackerBudget;
//        this.defenderBudget = gameState.defenderBudget;
////        this.uniformAttackCost = gameState.uniformAttackCost;
//        this.highestValueReceived = gameState.highestValueReceived;
//        this.remainingAttacks = gameState.remainingAttacks;
//        if (playerToMove != HoneypotGameInfo.ATTACKER) {
//            this.lastDefendedNode = gameState.lastDefendedNode;
//        }
    }

//    public int getRemainingAttacks(){
//        return remainingAttacks;
//    }

    @Override
    public Player getPlayerToMove() {
        return isPlayerToMoveDefender ?
                HoneypotGameInfo.DEFENDER : HoneypotGameInfo.ATTACKER;
    }

    @Override
    public GameState copy() {
        return new HoneypotGameState(this);
    }

    @Override
    public double[] getUtilities() {
        double[] utilities = new double[2];

        utilities[0] = -attackerReward;
        utilities[1] = attackerReward;

        return utilities;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    protected double[] getEndGameUtilities() {
        return getUtilities();
    }

    @Override
    public boolean isActualGameEnd() {
        return isGameEnd();
    }

    @Override
    public boolean isDepthLimit() {
        return getSequenceFor(HoneypotGameInfo.ATTACKER).size() == HoneypotGameInfo.attacksAllowed;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public boolean isGameEnd() {
        return getSequenceFor(HoneypotGameInfo.ATTACKER).size() == HoneypotGameInfo.attacksAllowed ||
                attackerPassed(); }

    private boolean attackerPassed(){
        Sequence sequence = history.getSequenceOf(HoneypotGameInfo.ATTACKER);
        if (sequence == null || sequence.size() < HoneypotGameInfo.NUMBER_OF_PASSES_TO_END_GAME) return false;
        for (int i = 0; i < HoneypotGameInfo.NUMBER_OF_PASSES_TO_END_GAME; i++){
            if (((HoneypotAttackerAction)sequence.get(sequence.size() - i - 1)).node.id != HoneypotGameInfo.NO_ACTION_ID)
                return false;
        }
        return true;
//        return history != null && history.getLength() > 0  && HoneypotGameInfo.ATTACKER.equals(history.getLastPlayer()) && history.getLastAction() != null
//                && ((HoneypotAttackerAction)history.getLastAction()).node.id == HoneypotGameInfo.NO_ACTION_ID;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return new PerfectRecallISKey(sequence.hashCode(), getSequenceForPlayerToMove());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HoneypotGameState that = (HoneypotGameState) o;

        if(depth != that.depth) return false;
        if (isPlayerToMoveDefender != that.isPlayerToMoveDefender) return false;
        if (Double.compare(that.attackerReward, attackerReward) != 0) return false;
        return sequence.equals(that.sequence);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = sequence.hashCode();
        result = 31 * result + depth;
        result = 31 * result + (isPlayerToMoveDefender ? 1 : 0);
        temp = Double.doubleToLongBits(attackerReward);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    void executeDefenderAction(HoneypotDefenderAction action) {
        isPlayerToMoveDefender = false;
//        HoneypotGameNode node = action.node;
//
//        // make honeypots
//
//        playerToMove = HoneypotGameInfo.ATTACKER;
//
//        if (node.id != HoneypotGameInfo.NO_ACTION_ID) {
//            honeypots[node.id - 1] = true;
//            defenderBudget -= node.defendCost;
//            lastDefendedNode = node.id;
//
//            double minValue = Double.MAX_VALUE;
//            for (HoneypotGameNode possibleNode : possibleNodes) {
//                if (!honeypots[possibleNode.id - 1] && possibleNode.defendCost < minValue && possibleNode.id > lastDefendedNode) {
//                    minValue = possibleNode.defendCost;
//                }
//            }
//
//            if (defenderBudget < minValue) {
//                playerToMove = HoneypotGameInfo.ATTACKER;
//            }
//        } else {
//            playerToMove = HoneypotGameInfo.ATTACKER;
//        }
    }

    void executeAttackerAction(HoneypotAttackerAction attackerAction) {
        HoneypotGameNode node = attackerAction.node;
        HoneypotDefenderAction defenderAction = (HoneypotDefenderAction)getSequenceFor(HoneypotGameInfo.DEFENDER).getLast();

        if(node.id != HoneypotGameInfo.NO_ACTION_ID && !defenderAction.hitHoneyPot(node.getID())){
            attackerReward += node.reward;//getRewardAfterNumberOfAttacks(0);
        }

        if(node.id != HoneypotGameInfo.NO_ACTION_ID){
            attackerReward -= node.attackCost;
        }

        sequence.add(defenderAction);
        sequence.add(attackerAction);

        isPlayerToMoveDefender = true;
        depth += 1;

//        if (honeypots[node.id - 1]) {
//            // no reward in this case
//            observedHoneypots[node.id - 1] = true;
//        } else{
//            attackerReward += node.getRewardAfterNumberOfAttacks(0);
////            if (node.getRewardAfterNumberOfAttacks(attackedNodes[node.id - 1]) > highestValueReceived) {
////                    highestValueReceived = node.getRewardAfterNumberOfAttacks(attackedNodes[node.id - 1]);
////            }
////            node.updateRewardAfterAttack(); // blbost, nodes jsou sdilene pro vsechny vetve
//        }
////        attackedNodes[node.id - 1]++;
//
//            playerToMove = HoneypotGameInfo.DEFENDER;
////            defenderBudget = HoneypotGameInfo.initialDefenderBudget;
//            Arrays.fill(honeypots, false);
////            Arrays.fill(observedHoneypots, false);
////            Arrays.fill(attackedNodes, 0);
////            if (action.node.id == HoneypotGameInfo.NO_ACTION_ID) remainingAttacks -= 1;
    }


    @Override
    public String toString() {
        return depth + " " + attackerReward;
    }

    @Override
    public double getImmediateRewardForAction(Action action) {
        HoneypotGameNode node = ((HoneypotAttackerAction)action).node;
        HoneypotDefenderAction defenderAction = (HoneypotDefenderAction) sequence.get(sequence.size()-2);
        if(node.id == HoneypotGameInfo.NO_ACTION_ID) return 0.0;
        return defenderAction.hitHoneyPot(node.getID()) ? -node.attackCost : node.reward - node.attackCost;
    }

    @Override
    public double getImmediateReward() {

        return 0;
    }

    @Override
    public double getImmediateCost() {
        return 0;
    }

}
