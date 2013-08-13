package cz.agents.gtlibrary.nfg.experimental.MDP.implementations;

import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 6/27/13
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MDPActionImpl implements MDPAction{


    private static final long serialVersionUID = -7380941202647059723L;


    public MDPActionImpl() {
    }

    @Override
    public abstract void perform(MDPState state);

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
    abstract public int hashCode();
}
