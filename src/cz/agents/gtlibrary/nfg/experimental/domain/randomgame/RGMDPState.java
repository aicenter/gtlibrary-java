package cz.agents.gtlibrary.nfg.experimental.domain.randomgame;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPConfig;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/12/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class RGMDPState extends MDPStateImpl {

    private int ID = 0;
    private int step = 0;

    private int hash;
    private boolean changed = true;

    public RGMDPState(Player player) {
        super(player);
    }

    public RGMDPState(Player player, int ID, int step) {
        super(player);
        this.ID = ID;
        this.step = step;
    }


    @Override
    public MDPState performAction(MDPAction action) {
        RGMDPState newState = (RGMDPState)this.copy();
        RGMDPAction a = (RGMDPAction)action;

        int newID = ID << (RGMDPConfig.SHIFT);
        newID |= a.getID();

        newState.ID = newID;
        newState.step++;

        return newState;
    }

    @Override
    public MDPState copy() {
        RGMDPState s = new RGMDPState(getPlayer());
        s.step = this.step;
        s.ID = this.ID;
        return s;
    }

    @Override
    public boolean isTerminal() {
        return step >= RGMDPConfig.getMaxSteps();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        RGMDPState other = (RGMDPState)obj;
        if (!this.getPlayer().equals(other.getPlayer()))
            return false;
        if (this.step != other.step)
            return false;
        if (this.ID != other.ID)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (changed) {
            HashCodeBuilder hb = new HashCodeBuilder(17, 31);
            hb.append(getPlayer());
            hb.append(ID);
            hb.append(step);
            hash = hb.toHashCode();
            changed = false;
        }
        return hash;
    }

    public int getID() {
        return ID;
    }

    public int getStep() {
        return step;
    }

    @Override
    public String toString() {
        return "RGState_"+getPlayer()+"_ID:"+ID+"_Step:"+step;
    }

    @Override
    public int horizon() {
        return RGMDPConfig.getMaxSteps() - getStep();
    }
}
