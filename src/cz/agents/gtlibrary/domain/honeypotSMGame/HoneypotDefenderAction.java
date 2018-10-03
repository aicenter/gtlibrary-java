package cz.agents.gtlibrary.domain.honeypotSMGame;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.AbstractActionProvider;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.BitSet;
import java.util.HashSet;

public class HoneypotDefenderAction extends ActionImpl implements AbstractActionProvider {
    private BitSet nodes;
    private Player player;

    HoneypotDefenderAction(HashSet<HoneypotGameNode> nodes, InformationSet informationSet, Player player) {
        super(informationSet);
        this.player = player;
        this.nodes = new BitSet(HoneypotGameInfo.allNodes.length);
        for(HoneypotGameNode node : nodes)
            this.nodes.set(node.getID(), true);
    }

    public boolean hitHoneyPot(int node){
        return nodes.get(node);
    }

    @Override
    public void perform(GameState gameState) {
        ((HoneypotGameState) gameState).executeDefenderAction(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HoneypotDefenderAction)) return false;
        if (!super.equals(obj)) return false;

        HoneypotDefenderAction other = (HoneypotDefenderAction) obj;

        if (!player.equals(other.player)) return false;
        if (nodes.hashCode() != other.nodes.hashCode()) return false;
        if (!informationSet.equals(other.informationSet)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;

        result = nodes.hashCode();
        result = result * 31 + player.getId();
        result = result * 31 + informationSet.hashCode();

        return result;
//        return new HashCodeBuilder(17,31).append(node).append(player).append(informationSet).toHashCode();
    }

    @Override
    public String toString() {
//        return "node " + node.getID();
        return "("+nodes.toString()+")";
//        return player + " - " + node;
    }


    protected Integer dummySituationAbstraction = 0;

    @Override
    public Object getSituationAbstraction() {
        return dummySituationAbstraction;
    }

    @Override
    public Object getActionAbstraction() {
        return nodes;
    }

    @Override
    public double getMaximumActionUtility() {
        return HoneypotGameInfo.maximumAttackUtility;
    }

    @Override
    public double[] getAllPossibleOutcomes() {
        return new double[]{0.0};
    }
}
