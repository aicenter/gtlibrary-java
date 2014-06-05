package cz.agents.gtlibrary.nfg.MDP.implementations;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.PureStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 6/27/13
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class MDPStateActionMarginal implements PureStrategy {
    private MDPState state;
    private MDPAction action;
//    private double natureProbability;
    private String info;
    private boolean changed = true;
    private int hash;

    public MDPStateActionMarginal(MDPState state, MDPAction action/*, double natureProbability*/) {
        this.state = state;
        this.action = action;
//        this.natureProbability = natureProbability;
        info = new StringBuffer().append("MDPAction: [").append(state).append("->").append(action).append("]").toString();
    }

    public MDPState getState() {
        return state;
    }

    public MDPAction getAction() {
        return action;
    }

    public Player getPlayer() {
        return state.getPlayer();
    }

//    public double getNatureProbability() {
//        return natureProbability;
//    }

    @Override
    public String toString() {
        return info;
    }

    @Override
    public int hashCode() {
        if (changed) {
            HashCodeBuilder hb = new HashCodeBuilder(17,31);
            hb.append(action);
            hb.append(state);
//            hb.append(natureProbability);
            hash = hb.toHashCode();
            changed = false;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (this.hashCode() != obj.hashCode())
            return false;
        MDPStateActionMarginal other = (MDPStateActionMarginal)obj;
        if (!this.info.equals(other.info))
            return false;
        if (!this.getState().equals(other.getState()))
            return false;
        if (!this.getAction().equals(other.getAction()))
            return false;
        return true;
    }

}
