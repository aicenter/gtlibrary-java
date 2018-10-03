package cz.agents.gtlibrary.domain.honeypotGame;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.ImmediateActionOutcomeProvider;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.Arrays;

/**
 * Created by Jakub Cerny on 06/06/2018.
 */
public class HoneypotTurntakingGameState extends HoneypotGameState implements ImmediateActionOutcomeProvider {

    public HoneypotTurntakingGameState(HoneypotGameNode[] possibleNodes) {
        super(possibleNodes);
    }

    protected HoneypotTurntakingGameState(HoneypotTurntakingGameState state){
        super(state);
    }

    @Override
    public GameState copy() {
        return new HoneypotTurntakingGameState(this);
    }

    @Override
    void executeAttackerAction(HoneypotAction action) {
        super.executeAttackerAction(action);
        playerToMove = HoneypotGameInfo.DEFENDER;
        defenderBudget = HoneypotGameInfo.initialDefenderBudget;
        Arrays.fill(honeypots, false);
        Arrays.fill(observedHoneypots, false);
        Arrays.fill(attackedNodes, 0);
        if (action.node.id == HoneypotGameInfo.NO_ACTION_ID) remainingAttacks -= 1;
    }

    @Override
    public double getImmediateRewardForAction(Action action) {
        HoneypotGameNode node = ((HoneypotAction)action).node;
        if(node.id == HoneypotGameInfo.NO_ACTION_ID) return 0.0;
        return honeypots[node.id - 1] ? -node.attackCost : node.reward - node.attackCost;
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
