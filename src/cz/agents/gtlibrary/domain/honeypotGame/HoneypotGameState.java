package cz.agents.gtlibrary.domain.honeypotGame;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.*;

/**
 * Created by Petr Tomasek on 29.4.2017.
 */
public class HoneypotGameState extends GameStateImpl {

    protected HoneypotGameNode[] possibleNodes;
    protected boolean[] honeypots;
    protected int[] attackedNodes;
    protected boolean[] observedHoneypots;

    protected Player playerToMove;
    protected double attackerBudget;
    protected double defenderBudget;
//    protected double uniformAttackCost;
    protected double attackerReward;
    protected double highestValueReceived = Integer.MIN_VALUE;
    protected int lastDefendedNode = Integer.MIN_VALUE;
    protected int remainingAttacks;

    public HoneypotGameState(HoneypotGameNode[] possibleNodes) {
        super(HoneypotGameInfo.ALL_PLAYERS);

        this.possibleNodes = possibleNodes;
        this.honeypots = new boolean[possibleNodes.length];
        this.attackedNodes = new int[possibleNodes.length];
        this.observedHoneypots = new boolean[possibleNodes.length];
        this.playerToMove = HoneypotGameInfo.DEFENDER;
        this.attackerReward = 0.0;
        this.highestValueReceived = Integer.MIN_VALUE;

        this.remainingAttacks = HoneypotGameInfo.attacksAllowed;
        this.attackerBudget = HoneypotGameInfo.initialAttackerBudget;
        this.defenderBudget = HoneypotGameInfo.initialDefenderBudget;
//        this.uniformAttackCost = HoneypotGameInfo.uniformAttackCost;
    }

    private HoneypotGameState(HoneypotGameState gameState) {
        super(gameState);

        this.possibleNodes = gameState.possibleNodes;//Arrays.copyOf(gameState.possibleNodes, gameState.possibleNodes.length);
//        this.possibleNodes = Arrays.copyOf(gameState.possibleNodes, gameState.possibleNodes.length);
        this.honeypots = Arrays.copyOf(gameState.honeypots, gameState.honeypots.length);
        this.attackedNodes = Arrays.copyOf(gameState.attackedNodes, gameState.attackedNodes.length);
        this.observedHoneypots = Arrays.copyOf(gameState.observedHoneypots, gameState.observedHoneypots.length);
        this.playerToMove = gameState.playerToMove;
        this.attackerBudget = gameState.attackerBudget;
        this.defenderBudget = gameState.defenderBudget;
//        this.uniformAttackCost = gameState.uniformAttackCost;
        this.attackerReward = gameState.attackerReward;
        this.highestValueReceived = gameState.highestValueReceived;
        this.remainingAttacks = gameState.remainingAttacks;
        if (playerToMove != HoneypotGameInfo.ATTACKER) {
            this.lastDefendedNode = gameState.lastDefendedNode;
        }
    }

    @Override
    public Player getPlayerToMove() {
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new HoneypotGameState(this);
    }

    @Override
    public double[] getUtilities() {
        double[] utilities = new double[2];
//        utilities[0] = defenderBudget;
//        utilities[1] = attackerBudget;
//
//        for (HoneypotGameNode attackedNode : attackedNodes) {
//            if (!observedHoneypots.contains(attackedNode)) {
//                utilities[0] -= attackedNode.reward;
//                utilities[1] += attackedNode.reward;
//            }
//        }

        utilities[0] = -attackerReward;
        utilities[1] = attackerReward;

        return utilities;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public boolean isGameEnd() {
        return remainingAttacks == 0 || !attackerCanAttack() || attackerPassed(); }

    private boolean attackerPassed(){
        Sequence sequence = history.getSequenceOf(HoneypotGameInfo.ATTACKER);
        if (sequence == null || sequence.size() < HoneypotGameInfo.NUMBER_OF_PASSES_TO_END_GAME) return false;
        for (int i = 0; i < HoneypotGameInfo.NUMBER_OF_PASSES_TO_END_GAME; i++){
            if (((HoneypotAction)sequence.get(sequence.size() - i - 1)).node.id != HoneypotGameInfo.NO_ACTION_ID)
                return false;
        }
        return true;
//        return history != null && history.getLength() > 0  && HoneypotGameInfo.ATTACKER.equals(history.getLastPlayer()) && history.getLastAction() != null
//                && ((HoneypotAction)history.getLastAction()).node.id == HoneypotGameInfo.NO_ACTION_ID;
    }
    private boolean attackerCanAttack(){
        if (HoneypotGameInfo.CAN_ATTACK_WITH_NEGATIVE_POINTS) return true;
        for (HoneypotGameNode node : HoneypotGameInfo.allNodes)
            if (node.attackCost <= attackerBudget)
                return true;
        return false;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return playerToMove.getId() == 2;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        switch (playerToMove.getId()) {
            case 0:
                return new PerfectRecallISKey(getSequenceForPlayerToMove().hashCode(), getSequenceForPlayerToMove());
            case 1:
                return new PerfectRecallISKey(Arrays.hashCode(attackedNodes) + Arrays.hashCode(observedHoneypots), getSequenceForPlayerToMove());
        }
        return null;
    }

    @Override
    public int hashCode() {
//        if (true) return new HashCodeBuilder().append(history.getSequenceOf(HoneypotGameInfo.ATTACKER)).append(history.getSequenceOf(HoneypotGameInfo.DEFENDER)).toHashCode();
        int result;
        long temp;

        result = attackedNodes != null ? Arrays.hashCode(attackedNodes) : 0;
        result = 31 * result + (honeypots != null ? Arrays.hashCode(honeypots) : 0);
        result = 31 * result + (observedHoneypots != null ? Arrays.hashCode(observedHoneypots) : 0);
        result = 31 * result + playerToMove.getId();

//        temp = Double.doubleToLongBits(attackerBudget);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        temp = Double.doubleToLongBits(remainingAttacks);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        temp = Double.doubleToLongBits(defenderBudget);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + history.hashCode();

        return result;

//        return new HashCodeBuilder(17, 31).append(attackedNodes).append(honeypots).
//                append(observedHoneypots).append(history).toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        HoneypotGameState other = (HoneypotGameState) object;

//        if (!history.getSequenceOf(HoneypotGameInfo.DEFENDER).equals(other.history.getSequenceOf(HoneypotGameInfo.DEFENDER))) return false;
//        if (!history.getSequenceOf(HoneypotGameInfo.ATTACKER).equals(other.history.getSequenceOf(HoneypotGameInfo.ATTACKER))) return false;

        if (!Arrays.equals(honeypots, other.honeypots)) return false;
        if (!Arrays.equals(attackedNodes, other.attackedNodes)) return false;
        if (!Arrays.equals(observedHoneypots, other.observedHoneypots)) return false;
        if (!playerToMove.equals(other.playerToMove)) return false;
//        if (attackerBudget != other.attackerBudget) return false;
//        if (remainingAttacks != other.remainingAttacks) return false;
//        if (defenderBudget != other.defenderBudget) return false;
        if (!history.equals(other.history)) return false;

        return true;
    }

    void executeDefenderAction(HoneypotAction action) {
        HoneypotGameNode node = action.node;
        if (node.id != HoneypotGameInfo.NO_ACTION_ID) {
            honeypots[node.id - 1] = true;
            defenderBudget -= node.defendCost;
            lastDefendedNode = node.id;

            double minValue = Double.MAX_VALUE;
            for (HoneypotGameNode possibleNode : possibleNodes) {
                if (!honeypots[possibleNode.id - 1] && possibleNode.defendCost < minValue && possibleNode.id > lastDefendedNode) {
                    minValue = possibleNode.defendCost;
                }
            }

            if (defenderBudget < minValue) {
                playerToMove = HoneypotGameInfo.ATTACKER;
            }
        } else {
            playerToMove = HoneypotGameInfo.ATTACKER;
        }
    }

    void executeAttackerAction(HoneypotAction action) {
        HoneypotGameNode node = action.node;

        if (node.id == HoneypotGameInfo.NO_ACTION_ID) return;

        attackerBudget -= node.attackCost;//uniformAttackCost;
        remainingAttacks--;

        if (honeypots[node.id - 1]) {
            // no reward in this case
            observedHoneypots[node.id - 1] = true;
        } else{
//            if (attackedNodes[node.id - 1] > 0) {
//                // zero reward after
//                attackerReward += node.reward / 2;
//            } else {
//                attackerReward += node.reward;
//
//                if (node.reward > highestValueReceived) {
//                    highestValueReceived = node.reward;
//                }
//            }

            attackerReward += node.getRewardAfterNumberOfAttacks(attackedNodes[node.id - 1]);
            if (node.getRewardAfterNumberOfAttacks(attackedNodes[node.id - 1]) > highestValueReceived) {
                    highestValueReceived = node.getRewardAfterNumberOfAttacks(attackedNodes[node.id - 1]);
            }
//            node.updateRewardAfterAttack(); // blbost, nodes jsou sdilene pro vsechny vetve
        }
        attackedNodes[node.id - 1]++;
    }

    public void setRemainingAttacks(int remainingAttacks){
        this.remainingAttacks = remainingAttacks;
    }

    @Override
    public String toString() {
        return defenderBudget + " " + attackerBudget;
    }
}
