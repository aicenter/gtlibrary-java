package cz.agents.gtlibrary.nfg.experimental.domain.transitgame;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPConfig;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/16/13
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class TGState extends MDPStateImpl {
    private int timeStep = -1;
    private int UNITS = 1;
    private int[] row = new int[UNITS];
    private int[] col = new int[UNITS];

    private int hash;
    private boolean changed = true;

    public TGState(Player player) {
        super(player);
        assert (UNITS == 1);
        // starting points for the players
        if (player.getId() == 0) {
            col[0] = -1;
            row[0] = -1;
        } else {
            timeStep = 0;
            col[0] = TGConfig.PATROLLER_BASES[0];
            row[0] = 0;
        }
    }



    public TGState(Player player, int timeStep, int[] col, int[] row) {
        super(player);
        this.timeStep = timeStep;
        this.col = col;
        this.row = row;
    }

    public TGState(TGState state) {
        super(state.getPlayer());
        this.UNITS = state.UNITS;
        this.timeStep = state.timeStep;
        col = new int[UNITS];
        row = new int[UNITS];
        for (int u=0; u<UNITS; u++) {
            col[u] = state.col[u];
            row[u] = state.row[u];
        }
    }

    @Override
    public MDPState performAction(MDPAction action) {
        TGState newState = (TGState)this.copy();
        TGAction a = (TGAction)action;

        if (a.getTargetCol()[0] == -1 && a.getTargetRow()[0] == -1) {
            return newState;
        }

        if (newState.moveUnit(0, a.getTargetCol()[0], a.getTargetRow()[0])) {
            newState.incTimeStep();
            return newState;
        }
        return null;

    }

    @Override
    public MDPState copy() {
        MDPState result = new TGState(this);
        return result;
    }

    @Override
    public boolean isTerminal() {
        return (timeStep >= TGConfig.getMaxTimeStep() ||
            (getPlayer().getId()== 0 && getCol()[0] == TGConfig.LENGTH_OF_GRID));
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        TGState other = (TGState)o;
        if (!this.getPlayer().equals(other.getPlayer()))
             return false;
        if (this.UNITS != other.UNITS)
            return false;
        if (this.timeStep != other.timeStep)
            return false;
        for (int u=0; u<UNITS; u++) {
            if (this.col[u] != other.col[u]) return false;
            if (this.row[u] != other.row[u]) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (changed) {
            HashCodeBuilder hb = new HashCodeBuilder(17, 31);
            hb.append(getPlayer());
            hb.append(UNITS);
            hb.append(timeStep);
            for (int u=0; u<UNITS; u++) {
                hb.append(col[u]);
                hb.append(row[u]);
            }
            hash = hb.toHashCode();
            changed = false;
        }
        return hash;
    }

    public void incTimeStep() {
        changed = true;
        timeStep++;
    }

    public void decTimeStep() {
        changed = true;
        timeStep--;
    }

    protected boolean moveUnit(int unitNumber, int targetCol, int targetRow) {
        assert (unitNumber < UNITS && unitNumber >= 0);
        if (col[unitNumber] >= 0 || row[unitNumber] >= 0) {
            if (Math.abs(col[unitNumber] - targetCol) > 1)
                return false;
            if (Math.abs(row[unitNumber] - targetRow) > 1)
                return false;
        } else {
            if (col[unitNumber] != -1 || row[unitNumber] != -1)
                return false;
        }
        col[unitNumber] = targetCol;
        row[unitNumber] = targetRow;
        changed = true;
        return true;
    }

    public int getTimeStep() {
        return timeStep;
    }

    public int getUNITS() {
        return UNITS;
    }

    public int[] getRow() {
        return row;
    }

    public int[] getCol() {
        return col;
    }

    @Override
    public String toString() {StringBuilder sb = new StringBuilder();
        sb.append("TGState:"+getPlayer()+":T="+getTimeStep());
        for (int i=0; i<col.length; i++) {
            sb.append("[");
            sb.append(row[i]);
            sb.append(",");
            sb.append(col[i]);
            sb.append("]");
        }
        return sb.toString();
    }

    @Override
    public int horizon() {
        return TGConfig.getMaxTimeStep() - getTimeStep() + 1;
    }
}
