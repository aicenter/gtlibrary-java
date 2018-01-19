package cz.agents.gtlibrary.domain.honeypotGame;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr Tomasek on 29.4.2017.
 */
public class HoneypotExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public HoneypotExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        if (gameState.getPlayerToMove().equals(HoneypotGameInfo.DEFENDER))
            return getDefenderActions((HoneypotGameState) gameState);
        if (gameState.getPlayerToMove().equals(HoneypotGameInfo.ATTACKER))
            return getAttackerActions((HoneypotGameState) gameState);
        return null;
    }

    private List<Action> getDefenderActions(HoneypotGameState gameState) {
        List<Action> actions = new ArrayList<>();

        for (HoneypotGameNode node : gameState.possibleNodes) {
            if (isDefendable(node, gameState))
                actions.add(new HoneypotAction(node, getAlgorithmConfig().getInformationSetFor(gameState), gameState.getPlayerToMove()));
        }
        actions.add(new HoneypotAction(new HoneypotGameNode(HoneypotGameInfo.NO_ACTION_ID, 0, 0, 0), getAlgorithmConfig().getInformationSetFor(gameState), gameState.getPlayerToMove()));

        return actions;
    }

    private List<Action> getAttackerActions(HoneypotGameState gameState) {
        List<Action> actions = new ArrayList<>();

        for (HoneypotGameNode node : gameState.possibleNodes) {
            if (isAttackable(node, gameState)) {
                actions.add(new HoneypotAction(node, getAlgorithmConfig().getInformationSetFor(gameState), gameState.getPlayerToMove()));
            }
        }

        // attacker can also pass -> then the game ends
        actions.add(new HoneypotAction(new HoneypotGameNode(HoneypotGameInfo.NO_ACTION_ID, 0, 0, 0), getAlgorithmConfig().getInformationSetFor(gameState), gameState.getPlayerToMove()));

        return actions;
    }

    private boolean isDefendable(HoneypotGameNode node, HoneypotGameState gameState){
        if (node.defendCost > gameState.defenderBudget) return false;
        if (gameState.honeypots[node.id - 1]) return false;
        if (node.id < gameState.lastDefendedNode) return false;

        return true;
    }

    private boolean isAttackable(HoneypotGameNode node, HoneypotGameState gameState){
        if (!HoneypotGameInfo.CAN_ATTACK_WITH_NEGATIVE_POINTS && node.attackCost > gameState.attackerBudget ) return false;
        if (gameState.observedHoneypots[node.id - 1]) return false;
        if (node.getRewardAfterNumberOfAttacks(gameState.attackedNodes[node.id-1]) <= 0.0) return false;
//        if (realNodeValue(node, gameState.attackedNodes[node.id - 1]) < gameState.highestValueReceived / 2) return false;

        return true;
    }

//    private double realNodeValue(HoneypotGameNode node, int attacks) {
//        if (attacks > 0) {
//            return node.reward / 2;
//        }
//        return node.reward;
//    }
}
