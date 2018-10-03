package cz.agents.gtlibrary.domain.honeypotSMGame;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.AbstractActionProvider;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.ImmediateActionOutcomeProvider;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public class HoneypotAttackerAction extends ActionImpl implements AbstractActionProvider, ImmediateActionOutcomeProvider {
    HoneypotGameNode node;
    private Player player;

    HoneypotAttackerAction(HoneypotGameNode node, InformationSet informationSet, Player player) {
        super(informationSet);
        this.player = player;
        this.node = node;
    }

    @Override
    public void perform(GameState gameState) {
        ((HoneypotGameState) gameState).executeAttackerAction(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HoneypotAttackerAction)) return false;
        if (!super.equals(obj)) return false;

        HoneypotAttackerAction other = (HoneypotAttackerAction) obj;

        if (!player.equals(other.player)) return false;
        if (!node.equals(other.node)) return false;
        if (!informationSet.equals(other.informationSet)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;

        result = node.hashCode();
        result = result * 31 + player.getId();
        result = result * 31 + informationSet.hashCode();

        return result;
//        return new HashCodeBuilder(17,31).append(node).append(player).append(informationSet).toHashCode();
    }

    @Override
    public String toString() {
//        return "node " + node.getID();
        return "("+node+")";
//        return player + " - " + node;
    }


    protected Integer dummySituationAbstraction = 0;

    @Override
    public Object getSituationAbstraction() {
        return dummySituationAbstraction;
    }

    @Override
    public Object getActionAbstraction() {
        return node;
    }

    @Override
    public double getMaximumActionUtility() {
        return HoneypotGameInfo.maximumAttackUtility;
    }

    @Override
    public double[] getAllPossibleOutcomes() {
        return node.getPossibleOutcomes();
    }

    @Override
    public double getImmediateRewardForAction(Action action) {
        if(node.getID() == HoneypotGameInfo.NO_ACTION_ID)
            return 1.0;
        HoneypotDefenderAction a = (HoneypotDefenderAction)action;
        if(a.hitHoneyPot(node.getID()))
            return 1.0;
        return 0.0;
    }

    @Override
    public double getImmediateReward() {
        if(node.getID() == HoneypotGameInfo.NO_ACTION_ID) return 0.0;
        return node.reward;
    }

    @Override
    public double getImmediateCost() {
        if(node.getID() == HoneypotGameInfo.NO_ACTION_ID) return 0.0;
        return node.attackCost;
    }
}
