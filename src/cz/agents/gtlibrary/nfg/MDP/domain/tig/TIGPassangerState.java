/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.nfg.MDP.domain.tig;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateImpl;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.NotImplementedException;

/**
 * Ticket inspection game patrol state.
 *
 * @author viliam
 */
public class TIGPassangerState extends MDPStateImpl {
    int amount = 0; //also indicated stage of the MDP: 0 root, >0 decision, <0 end
    int fromStop = 0;
    int toStop = 0;
    int trainDir = -1;
    int trainNum = -1;

    private int hash;
    private boolean changed = true;

    
    
    public TIGPassangerState(Player player) {
        super(player);
        assert (player.getId() == 1); //assert it is the passanger
    }

    public TIGPassangerState(Player player, int fromStop, int toStop, int trainDir, int trainNum) {
        super(player);
        this.amount = TIGConfig.getPassangerAmount(trainDir, trainNum, fromStop, toStop);
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.trainDir = trainDir;
        this.trainNum = trainNum;
    }
    
    public TIGPassangerState(TIGPassangerState state) {
        super(state.getPlayer());
        this.amount = state.amount;
        this.fromStop = state.fromStop;
        this.toStop = state.toStop;
        this.trainDir = state.trainDir;
        this.trainNum = state.trainNum;
        this.hash = state.hash;
        this.changed = true;
    }

    @Override
    public MDPState performAction(MDPAction action) {
        throw new NotImplementedException();
    }

    @Override
    public MDPState copy() {
        MDPState result = new TIGPassangerState(this);
        return result;
    }

    @Override
    public boolean isTerminal() {
        return amount<0;
    }
    
    @Override
    public boolean isRoot() {
        return amount==0;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        TIGPassangerState other = (TIGPassangerState)o;
        if (!this.getPlayer().equals(other.getPlayer()))
             return false;
        if (this.fromStop != other.fromStop)
            return false;
        if (this.toStop != other.toStop)
            return false;
        if (this.trainDir != other.trainDir || this.trainNum != other.trainNum || this.amount != other.amount)
             return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (changed) {
            HashCodeBuilder hb = new HashCodeBuilder(17, 31);
            hb.append(getPlayer());
            hb.append(amount);
            hb.append(toStop);
            hb.append(fromStop);
            hb.append(trainDir);
            hb.append(trainNum);
            hash = hb.toHashCode();
            
            changed = false;
        }
        return hash;
    }

    public int getTime() {
        return fromStop;
    }

    @Override
    public String toString() {StringBuilder sb = new StringBuilder();
        if (amount==0) return "Root";
        if (amount<0) return "End";
        sb.append(amount + "pas:"+fromStop);
        sb.append("->"+toStop+";");
        sb.append("T="+trainDir+","+trainNum+";");
        return sb.toString();
    }

    @Override
    public int horizon() {
        if (amount==0) return 2;
        if (amount>0) return 1;
        return 0;
    }
}
