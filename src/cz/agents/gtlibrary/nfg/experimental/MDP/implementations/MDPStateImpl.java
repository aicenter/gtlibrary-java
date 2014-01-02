package cz.agents.gtlibrary.nfg.experimental.MDP.implementations;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 6/27/13
 * Time: 4:05 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MDPStateImpl implements MDPState {

    private Player player;

    public MDPStateImpl(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public abstract MDPState performAction(MDPAction action);

    @Override
    public abstract MDPState copy();

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
        return true;
    }

    @Override
    public abstract int hashCode();

    @Override
    public boolean isRoot() {
        return false;
    }
}
