package cz.agents.gtlibrary.domain.honeypotGame;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.*;

/**
 * Created by Petr Tomasek on 29.4.2017.
 */
public class HoneypotGameState extends GameStateImpl {

    HoneypotGameNode[] possibleNodes;
    boolean[] honeypots;
    private int[] attackedNodes;
    boolean[] observedHoneypots;

    protected Player playerToMove;
    double attackerBudget;
    double defenderBudget;
    double attackCost;
    double attackerActualReward;
    int lastDefendedNode = Integer.MIN_VALUE;

    HoneypotGameState(HoneypotGameNode[] possibleNodes) {
        super(HoneypotGameInfo.ALL_PLAYERS);

        this.possibleNodes = possibleNodes;
        honeypots = new boolean[possibleNodes.length];
        attackedNodes = new int[possibleNodes.length];
        observedHoneypots = new boolean[possibleNodes.length];
        playerToMove = HoneypotGameInfo.DEFENDER;
        attackerActualReward = 0;
    }

    private HoneypotGameState(HoneypotGameState gameState) {
        super(gameState);

        this.possibleNodes = gameState.possibleNodes;//Arrays.copyOf(gameState.possibleNodes, gameState.possibleNodes.length);
        this.honeypots = Arrays.copyOf(gameState.honeypots, gameState.honeypots.length);
        this.attackedNodes = Arrays.copyOf(gameState.attackedNodes, gameState.attackedNodes.length);
        this.observedHoneypots = Arrays.copyOf(gameState.observedHoneypots, gameState.observedHoneypots.length);
        this.playerToMove = gameState.playerToMove;
        this.attackerBudget = gameState.attackerBudget;
        this.defenderBudget = gameState.defenderBudget;
        this.attackCost = gameState.attackCost;
        this.attackerActualReward = gameState.attackerActualReward;
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
//                utilities[0] -= attackedNode.value;
//                utilities[1] += attackedNode.value;
//            }
//        }

        utilities[0] = -attackerActualReward;
        utilities[1] = attackerActualReward;

        return utilities;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public boolean isGameEnd() {
        return attackerBudget == 0;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return playerToMove.getId() == 2;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        switch (playerToMove.getId()) {
            case 0:
                return new PerfectRecallISKey(Arrays.hashCode(honeypots), getSequenceForPlayerToMove());
            case 1:
                return new PerfectRecallISKey(Arrays.hashCode(attackedNodes) + Arrays.hashCode(observedHoneypots), getSequenceForPlayerToMove());
        }
        return null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;

        result = attackedNodes != null ? Arrays.hashCode(attackedNodes) : 0;
        result = 31 * result + (honeypots != null ? Arrays.hashCode(honeypots) : 0);
        result = 31 * result + (observedHoneypots != null ? Arrays.hashCode(observedHoneypots) : 0);
        result = 31 * result + playerToMove.getId();

        temp = Double.doubleToLongBits(attackerBudget);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(defenderBudget);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
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

        if (!Arrays.equals(honeypots, other.honeypots)) return false;
        if (!Arrays.equals(attackedNodes, other.attackedNodes)) return false;
        if (!Arrays.equals(observedHoneypots, other.observedHoneypots)) return false;
        if (!playerToMove.equals(other.playerToMove)) return false;
        if (attackerBudget != other.attackerBudget) return false;
        if (defenderBudget != other.defenderBudget) return false;
        if (!history.equals(other.history)) return false;

        return true;
    }

    void executeDefenderAction(HoneypotAction action) {
        HoneypotGameNode node = action.node;
        if (node.id != HoneypotGameInfo.NO_ACTION_ID) {
            honeypots[node.id - 1] = true;
            defenderBudget -= node.value;
            lastDefendedNode = node.id;

            double minValue = Double.MAX_VALUE;
            for (HoneypotGameNode possibleNode : possibleNodes) {
                if (!honeypots[possibleNode.id - 1] && possibleNode.value < minValue && possibleNode.id > lastDefendedNode) {
                    minValue = possibleNode.value;
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

        attackerBudget -= attackCost;
        if (honeypots[node.id - 1]) {
            observedHoneypots[node.id - 1] = true;
        } else if (attackedNodes[node.id - 1] > 0) {
            attackerActualReward += node.value / 2;
        } else {
            attackerActualReward += node.value;
        }
        attackedNodes[node.id - 1]++;
    }

    double getDefenderBudget() {
        return defenderBudget;
    }

    @Override
    public String toString() {
        return defenderBudget + " " + attackerBudget;
    }
}
