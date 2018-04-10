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

/**
 *
 * @author viliam
 */
package cz.agents.gtlibrary.nfg.MDP.domain.tig;

import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.*;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateImpl;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.NotImplementedException;

/**
 * Ticket inspection game patrol state.
 * @author viliam
 */
public class TIGPatrolState extends MDPStateImpl {
    boolean onTrain = false;
    int time = -2;
    int stop = TIGConfig.NUM_STOPS/2;
    int trainDir = -1;
    int trainNum = -1;

    private int hash;
    private boolean changed = true;

    public TIGPatrolState(Player player) {
        super(player);
        assert (player.getId() == 0); //assert it is the patrol
    }


    public TIGPatrolState(TIGPatrolState state) {
        super(state.getPlayer());
        this.onTrain = state.onTrain;
        this.time = state.time;
        this.stop = state.stop;
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
        MDPState result = new TIGPatrolState(this);
        return result;
    }

    @Override
    public boolean isTerminal() {
        return (time >= TIGConfig.getMaxTimeStep());
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        TIGPatrolState other = (TIGPatrolState)o;
        if (!this.getPlayer().equals(other.getPlayer()))
             return false;
        if (this.onTrain != other.onTrain || this.time != other.time)
            return false;
        if (this.stop != other.stop)
            return false;
        if (this.trainDir != other.trainDir || this.trainNum != other.trainNum)
             return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (changed) {
            HashCodeBuilder hb = new HashCodeBuilder(17, 31);
            hb.append(getPlayer());
            hb.append(stop);
            hb.append(time);
            hb.append(trainDir);
            hb.append(trainNum);
            hb.append(onTrain);
            
            hash = hb.toHashCode();
            changed = false;
        }
        return hash;
    }

    public int getTime() {
        return time;
    }
    
    @Override
    public boolean isRoot() {
        return time==-2;
    }

    @Override
    public String toString() {StringBuilder sb = new StringBuilder();
        if (time==-2) return "Root";
        sb.append("Pat:T="+getTime()+";");
        sb.append("S="+stop+";");
        sb.append("T="+trainDir+","+trainNum+";");
        return sb.toString();
    }

    @Override
    public int horizon() {//upper bound on the number of actions that will still be performed
        return TGConfig.getMaxTimeStep() - getTime() + 1;
    }
}
